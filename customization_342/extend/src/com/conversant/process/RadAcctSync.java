package com.conversant.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;

import org.compiere.model.MBillingRecord;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;

import com.conversant.db.RadiusConnector;
import com.conversant.model.RadiusAccount;

public class RadAcctSync extends SvrProcess
{	
	// TODO: Optimisation needed? 
	// TODO: Time how long it takes to check oracle/mysql sync with a million rows
	// TODO: Check negative invoiceIds? (james processing got stuck)
	
	private static final String AFTER_ACCT_START_TIME = "2010-10-01 00:00:00";
	private static final int MAX_ROWS_PER_ITERATION = 10000;
	private static final int MAX_ROWS_PER_PROCESS = 100000;
	
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
		/*if (!checkOracleMySQLSyncronisation())
			return "@Error@ Oracle's MOD_Billing_Record and MySQL's RadAcctInvoice aren't in sync";
			*/
		// Sync processed MOD_Billing_Records which haven't been syncronised to RadAcctInvoice
		syncroniseProcessedBillingRecords();
		
		int rowCount = 0;
		boolean moreRadiusAccounts = true;
		while (moreRadiusAccounts)
		{
			// Get radius accounts to insert into the MOD_Billing_Record table
			ArrayList<RadiusAccount> accounts = RadiusConnector.getRadiusAccountsToBill(MAX_ROWS_PER_ITERATION, AFTER_ACCT_START_TIME);	
			
			if (accounts.size() < 1)
				break;
			
			// Insert records into MOD_Billing_Record
			boolean success = insertModBillingRecords(accounts);
			
			// Insert empty records (only RadAcctId) into RadAcctInvoice
			if (success)
				insertRadAcctInvoiceRecords(accounts);
			else
				return "@Error@ Failed to insert MOD_Billing_Records. RowCount=" + rowCount + " & AccountCount=" + accounts.size();
			
			rowCount += accounts.size();
			if (rowCount >= MAX_ROWS_PER_PROCESS)
			{
				log.warning("Reached MAX_ROWS_PER_PROCESS of " + MAX_ROWS_PER_ITERATION + ", if run automatically this needs to be addressed");
				break;
			}
			
			if (accounts.size() < MAX_ROWS_PER_ITERATION)
				moreRadiusAccounts = false;
		}
		
		return "@Success@ " + rowCount + " Radius Account(s) syncronised";
	}

	private boolean insertModBillingRecords(ArrayList<RadiusAccount> accounts)
	{
		// Check accounts exist
		if (accounts.size() < 1)
			return false;
		
// ------------ Hack to set Client/Org when run via scheduler -------
		
		int AD_Client_ID = Env.getAD_Client_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Client_ID", "1000000");
		
		int AD_Org_ID = Env.getAD_Org_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Org_ID", "1000001");
        
//-------------------------------------------------------------------
		
		// Create trx
		String trxName = Trx.createTrxName("insertModBillingRecords");
		
		// Process radaccts
		try
		{
			for (RadiusAccount account : accounts)
			{				
				MBillingRecord billingRecord = MBillingRecord.createNew(getCtx(), account, trxName);
				if (!billingRecord.save())
					throw new Exception("Failed to save " + billingRecord + " for " + account);				
			}
			
			Trx trx = null;
			try
			{
				trx = Trx.get(trxName, false);	
				if (trx != null)
				{
					if (!trx.commit())
						throw new Exception("Failed to commit trx");
				}
			}
			catch (Exception ex)
			{
				// Catches Trx.get() IllegalArgumentExceptions
				throw new Exception("Failed to get trx");
			}
			finally
			{
				if (trx != null && trx.isActive())
					trx.close();
			}
			
			return true;
		}
		catch (Exception ex)
		{
			log.severe(ex.getMessage());	
		}
		finally
		{
			// Rollback trx
			Trx trx = Trx.get(trxName, false);
			if (trx != null && trx.isActive())
			{
				trx.rollback();
				trx.close();
			}	
			
// ------------ Remove Hack -----------------------------------------
			
			Env.setContext(getCtx(), "#AD_Client_ID", AD_Client_ID);
			Env.setContext(getCtx(), "#AD_Org_ID", AD_Org_ID);
			
// ------------------------------------------------------------------
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
		for (MBillingRecord billingRecord : processedBillingRecordsToSync)
		{
			// Check invoice and line id have been set
			if (billingRecord.getC_Invoice_ID() > 0 && billingRecord.getC_InvoiceLine_ID() > 0)
			{
				if (RadiusConnector.updateRadiusAccountInvoice(billingRecord.getRadAcctId(), billingRecord.getC_Invoice_ID(), billingRecord.getC_InvoiceLine_ID()))
				{
					billingRecord.setSyncronised(true);
					if (!billingRecord.save())
						log.severe("Failed to set SYNCRONISED to 'Y' for " + billingRecord);
				} 
				else
					failedIds.add(billingRecord.getRadAcctId());
			}
			else
				log.severe(billingRecord + " has been flagged as processed with invalid invoice or invoice line ids");
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
		String sql = "SELECT * FROM " + MBillingRecord.Table_Name + " WHERE IsActive='Y' AND Processed='Y' AND Syncronised='N'"; 
		
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
		String sql = "SELECT COUNT(*) FROM " + MBillingRecord.Table_Name + " WHERE IsActive='Y'"; 
		
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
