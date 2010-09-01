package com.conversant.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MBillingRecord;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Trx;

import com.conversant.db.RadiusConnector;
import com.conversant.model.RadiusAccount;

public class RadAcctSync extends SvrProcess
{	
	// TODO: Optimisation needed? 
	// TODO: Time how long it takes to check oracle/mysql sync with a million rows
	// TODO: Check negative invoiceIds? (james processing got stuck)
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare()
	{

	}

	/**
	 * 	Process
	 *	@return message
	 *	@throws Exception
	 */
	@Override
	protected String doIt() throws Exception
	{	
		// Check oracle/mysql are in sync
		if (!checkOracleMySQLSyncronisation())
			return "@Error@ Oracle's MOD_Billing_Record and MySQL's RadAcctInvoice aren't in sync";
			
		// Get radius accounts to insert into the MOD_Billing_Record table
		ArrayList<RadiusAccount> accounts = RadiusConnector.getRadiusAccounts();	
		
		// Insert records into MOD_Billing_Record
		boolean insertedModBillingRecords = insertModBillingRecords(accounts);
		
		// Insert empty records (only RadAcctId) into RadAcctInvoice
		if (insertedModBillingRecords)
			insertRadAcctInvoiceRecords(accounts);
		
		// Sync MOD_Billing_Records which haven't been syncronised to RadAcctInvoice
		syncroniseProcessedBillingRecords();
		
		return "@Success@ RadAcct records have been syncronised";
	}

	private boolean insertModBillingRecords(ArrayList<RadiusAccount> accounts)
	{
		// Check accounts exist
		if (accounts.size() < 1)
			return false;
		
		// Create trx
		String trxName = Trx.createTrxName("insertModBillingRecords");
		Trx trx = Trx.get(trxName, true);
		
		// Process radaccts
		try
		{
			for (RadiusAccount account : accounts)
			{				
			// --- Hack to keep Client/Org set when running via scheduler --
				Properties ctx = (Properties)getCtx().clone();
				ctx.setProperty("#AD_Client_ID", "1000000"); // conversant
				ctx.setProperty("#AD_Org_ID", "1000001"); // conversant
			// -------------------------------------------------------------
				
				MBillingRecord billingRecord = MBillingRecord.createNew(ctx, account, trxName);
				if (!billingRecord.save())
				{
					log.severe("Failed to save " + billingRecord + " for " + account);
					throw new Exception();				
				}
			}
			
			if (!trx.commit())
				log.severe("Failed to commit ModBillingRecords");
			
			return true;
		}
		catch (Exception ex)
		{
			if (!trx.rollback())
				log.severe("Failed to rollback ModBillingRecords");	
		}
		finally
		{
			if (trx.isActive())
				trx.close();
		}
		
		return false;
	}
	
	private void insertRadAcctInvoiceRecords(ArrayList<RadiusAccount> accounts)
	{
		ArrayList<Integer> failedIds = new ArrayList<Integer>();
		
		for (RadiusAccount account : accounts)
		{			
			if (!RadiusConnector.addRadiusAccountInvoice(account.getRadAcctId(), 0, 0))
				failedIds.add(account.getRadAcctId());
		}
		
		// Log failed ids 
		if (failedIds.size() > 0)
		{
			StringBuilder sb = new StringBuilder();
			for (Integer id : failedIds)
				sb.append(id + ", ");
			
			// Remove last comma
			sb.replace(sb.length(), sb.length() - 2, ""); 
			
			log.severe("Failed to add RadAcctInvoice records for following RadAcctIds - " + sb.toString());
		}
	}
	
	private void syncroniseProcessedBillingRecords()
	{
		ArrayList<Integer> failedIds = new ArrayList<Integer>();
		
		ArrayList<MBillingRecord> processedBillingRecordsToSync = getProcessedModBillingRecordsToSync();
		for (MBillingRecord br : processedBillingRecordsToSync)
		{
			// Check invoice and line id have been set
			if (br.getC_Invoice_ID() > 0 && br.getC_InvoiceLine_ID() > 0)
			{
				if (RadiusConnector.updateRadiusAccountInvoice(br.getRadAcctId(), br.getC_Invoice_ID(), br.getC_InvoiceLine_ID()))
				{
					br.setSyncronised(true);
					if (!br.save())
						log.severe("Failed to set SYNCRONISED to true for " + br);
				} 
				else
					failedIds.add(br.getRadAcctId());
			}
			else
				log.severe(br + " has been flagged as processed with invalid invoice or invoice line ids");
		}
		
		// Log failed ids 
		if (failedIds.size() > 0)
		{
			StringBuilder sb = new StringBuilder();
			for (Integer id : failedIds)
				sb.append(id + ", ");
			
			// Remove last comma
			sb.replace(sb.length(), sb.length() - 2, ""); 
			
			log.severe("Failed to update RadAcctInvoice records for following RadAcctIds - " + sb.toString());
		}
	}
	
	private ArrayList<MBillingRecord> getProcessedModBillingRecordsToSync()
	{
		ArrayList<MBillingRecord> list = new ArrayList<MBillingRecord>();
		String sql = "SELECT * FROM " + MBillingRecord.Table_Name + " WHERE PROCESSED='Y' AND SYNCRONISED='N'"; 
		
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, null);
			
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next())
				list.add(new MBillingRecord(getCtx(), rs, null));
			
			rs.close ();
			pstmt.close ();
			pstmt = null;
		} 
		catch (Exception ex)
		{
			log.log(Level.SEVERE, sql, ex);
			return null;
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		} 
		catch (Exception e)
		{
			pstmt = null;
		}

		return list;
	}
	
	private boolean checkOracleMySQLSyncronisation()
	{
		Long modBillingRecordCount = getModBillingRecordCount();
		Long radAcctInvoiceCount = RadiusConnector.getRadiusAccountInvoiceCount();
		
		if (modBillingRecordCount.compareTo(radAcctInvoiceCount) != 0)
			return false;
		
		return true;
	}
	
	private Long getModBillingRecordCount()
	{
		Long count = -1L;
		String sql = "SELECT COUNT(*) FROM " + MBillingRecord.Table_Name; 
		
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, null);
			
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next())
				count = rs.getLong(1);
			
			rs.close ();
			pstmt.close ();
			pstmt = null;
		} 
		catch (Exception ex)
		{
			log.log(Level.SEVERE, sql, ex);
			return null;
		}
		
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		} 
		catch (Exception e)
		{
			pstmt = null;
		}

		return count;
	}
}
