package com.conversant.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;

import org.compiere.model.MBillingRecord;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

import com.conversant.db.RadiusConnector;
import com.conversant.model.RadiusAccount;

public class RadAcctSync extends SvrProcess
{	
	private static long ONE_DAY_MILLISECONDS = 1 * 24 * 60 * 60 * 1000;
	private static long ONE_SECOND_MILLISECONDS = 1000;
	
	private Timestamp acctStartTimeFrom = null;
	private Timestamp acctStartTimeTo = null;
	
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
			else if (name.equals("AcctStartTimeFrom"))
			{
				acctStartTimeFrom = (Timestamp)para[i].getParameter();
			}
			else if (name.equals("AcctStartTimeTo"))
			{
				acctStartTimeTo = (Timestamp)para[i].getParameter();
				
				// increment by one day (because use < operater in sql statement)
				acctStartTimeTo.setTime(acctStartTimeTo.getTime() + (ONE_DAY_MILLISECONDS - ONE_SECOND_MILLISECONDS)); // minus one second to make it 23:59:59
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
			return "@Error@ Failed to retrieve existing records from database";
		
		if (acctStartTimeFrom == null)
			return "@Error@ Invalid value for AcctStartTimeFrom";
		
		if (acctStartTimeTo == null)
			return "@Error@ Invalid value for AcctStartTimeTo";
		
		// Get radius accounts to insert into the MOD_Billing_Record table
		ArrayList<RadiusAccount> accounts = RadiusConnector.getRadiusAccounts(idsToExclude, acctStartTimeFrom, acctStartTimeTo);			
		
		return insertModBillingRecords(accounts);
	}
	
	private ArrayList<Integer> getModBillingRecordRadAcctIds()
	{
		ArrayList<Integer> list = new ArrayList<Integer>();
		String sql = "SELECT " + MBillingRecord.COLUMNNAME_RADACCTID + " FROM " + MBillingRecord.Table_Name; 
		
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
			MBillingRecord billingRecord = MBillingRecord.createNew(getCtx(), account);
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
