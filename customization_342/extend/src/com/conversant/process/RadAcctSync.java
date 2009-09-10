package com.conversant.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;

import org.compiere.model.MModBillingRecord;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

import com.conversant.db.RadiusConnector;
import com.conversant.model.RadiusAccount;

public class RadAcctSync extends SvrProcess
{	
	private Timestamp acctStartTime = null;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("AcctStartTime"))
			{
				acctStartTime = (Timestamp)para[i].getParameter();
				// increment by one day
				long oneDay = 1 * 24 * 60 * 60 * 1000;
				acctStartTime.setTime(acctStartTime.getTime() + oneDay);
			}
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}

	/**
	 * 	Process
	 *	@return message
	 *	@throws Exception
	 */
	@Override
	protected String doIt() throws Exception
	{
		ArrayList<Integer> idsToExclude = getModBillingRecordRadAcctIds();
		if (idsToExclude == null)
			return "@Error@ Failed to retrieve existing records from Oracle";
		
		ArrayList<RadiusAccount> accounts = RadiusConnector.getRadiusAccounts(idsToExclude, acctStartTime);			
		return insertModBillingRecords(accounts);
	}
	
	private ArrayList<Integer> getModBillingRecordRadAcctIds()
	{
		ArrayList<Integer> list = new ArrayList<Integer>();
		String sql = "SELECT " + MModBillingRecord.COLUMNNAME_RADACCTID + " FROM " + MModBillingRecord.Table_Name; 
		
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, null);
			
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add(rs.getInt(1));
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

	private String insertModBillingRecords(ArrayList<RadiusAccount> accounts)
	{
		ArrayList<Integer> failedIds = new ArrayList<Integer>();
		int count = 0;
		for (RadiusAccount account : accounts)
		{
			MModBillingRecord billingRecord = MModBillingRecord.createNew(getCtx(), account);
			if (billingRecord.save())
				count++;
			else
				failedIds.add(account.getRadAcctId());
		}
		
		StringBuilder msg = new StringBuilder("@Success@, ").append(count).append(" records were syncronized.");
		
		if (failedIds.size() > 0)
		{
			msg.append(" Failed to syncronize the following RadAcctId's: ");
			for (Integer id : failedIds)
			{
				msg.append(Integer.toString(id)).append(", ");
			}
			msg.replace(msg.length() - 2, msg.length(), "");
		}
		
		return msg.toString();
	}
}
