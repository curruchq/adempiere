package com.conversant.db;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;

import org.compiere.util.CLogger;
import org.compiere.util.Env;

import com.conversant.model.BillingAccount;
import com.conversant.model.BillingRecord;

public class BillingConnector extends MySQLConnector 
{
	private static CLogger log = CLogger.getCLogger(BillingConnector.class);

	private static final String SCHEMA = "billing";
	
	public static Connection getConnection()
	{
		return getConnection(Env.getCtx(), SCHEMA);
	}
	
	public static long addBillingRecord(BillingRecord br)
	{
		String table = "billingrecord";
		String[] columns = new String[]{"twoTalkId", "billingGroup", "originNumber", "destinationNumber", "description",  
										"status", "`terminated`", "date", "time", "dateTime", "callLength", "callCost", 
										"smartCode", "smartCodeDescription", "type", "subType", "mp3", "billingAccountId"};
				
		Object[] values = new Object[]{br.getTwoTalkId(), br.getBillingGroup(), br.getOriginNumber(), br.getDestinationNumber(), br.getDescription(), 
									   br.getStatus(), br.getTerminated(), br.getDate(), br.getTime(), br.getDateTime(), br.getCallLength(), br.getCallCost(), 
									   br.getSmartCode(), br.getSmartCodeDescription(), br.getType(), br.getSubType(), br.isMp3(), br.getBillingAccountId()};
									   	
		if (insert(getConnection(), table, columns, values))
		{
			// Get newly created id
			columns =  new String[]{"id"};
			String[] whereFields = new String[]{"twoTalkId"};
			Object[] whereValues = new Object[]{br.getTwoTalkId()};
			
			ArrayList<Object[]> rows = select(getConnection(), table, columns, whereFields, whereValues);
			if (rows != null && rows.size() > 0 && rows.get(0) != null)
			{
				return (Integer)((Object[])rows.get(0))[0];
			}
			else
				log.severe("Failed to select newly created row");
		}
		else
			log.severe("Failed to create Billing Record");
		
		return -1;
	}
	
	public static boolean removeBillingRecord(BillingRecord br)
	{
		String table = "billingrecord";
		String whereClause = "id=? AND twoTalkId=? AND originNumber=? AND destinationNumber=?";
		Object[] whereValues = new Object[]{br.getId(), br.getTwoTalkId(), br.getOriginNumber(), br.getDestinationNumber()};
		
		return delete(getConnection(), table, whereClause, whereValues);
	}

	public static ArrayList<BillingRecord> getBillingRecordsForNumber(String number, String afterDateTime, String beforeDateTime)
	{
		ArrayList<BillingRecord> allBillingRecords = new ArrayList<BillingRecord>();
		
		String table = "billingrecord";
		String[] columns = new String[]{"*"};
		
		StringBuilder whereClause = new StringBuilder();		
		whereClause.append("(originNumber = ? OR destinationNumber = ?) AND dateTime BETWEEN ? AND ?");
		
		Object[] whereValues = new Object[]{number, number, afterDateTime, beforeDateTime};					
		
		for (Object[] row : select(getConnection(), table, columns, whereClause.toString(), whereValues))
		{
			BillingRecord br = BillingRecord.createFromDB(row);
			if (br != null) allBillingRecords.add(br);
		}
		
		return allBillingRecords;
	}
	
	public static ArrayList<BillingRecord> getBillingRecords(String billingGroup, String originNumber, String destinationNumber, Date date)
	{
		ArrayList<BillingRecord> allBillingRecords = new ArrayList<BillingRecord>();
		
		String table = "billingrecord";
		String[] columns = new String[]{"*"};
		
		StringBuilder whereClause = new StringBuilder();			
		if (billingGroup == null || billingGroup.length() < 1 || billingGroup.equals("*"))
		{
			whereClause.append("billingGroup LIKE ?");
			billingGroup = "%";
		}
		else
			whereClause.append("billingGroup=?");
		
		whereClause.append(" AND ");
		
		if (originNumber == null || originNumber.length() < 1 || originNumber.equals("*"))
		{
			whereClause.append("originNumber LIKE ?");
			originNumber = "%";
		}
		else
			whereClause.append("originNumber=?");
		
		whereClause.append(" AND ");
		
		if (destinationNumber == null || destinationNumber.length() < 1 || destinationNumber.equals("*"))
		{
			whereClause.append("destinationNumber LIKE ?");
			destinationNumber = "%";
		}
		else 
			whereClause.append("destinationNumber=?");
		
		Object[] whereValues = new Object[]{billingGroup, originNumber, destinationNumber};
		
		if (date != null)
		{
			whereClause.append(" AND date=?");
			whereValues = new Object[]{billingGroup, originNumber, destinationNumber, date};
		}					
		
		for (Object[] row : select(getConnection(), table, columns, whereClause.toString(), whereValues, 100))
		{
			BillingRecord br = BillingRecord.createFromDB(row);
			if (br != null) allBillingRecords.add(br);
		}
		
		return allBillingRecords;
	}
	
	public static ArrayList<Long> getTwoTalkIds(Long fromTwoTalkId)
	{
		ArrayList<Long> allIds = new ArrayList<Long>();
		
		String table = "billingrecord";
		String[] columns = new String[]{"twoTalkId"};
		String whereClause = "";
		Object[] whereValues = null;
		
		if (fromTwoTalkId != null && fromTwoTalkId > 0)
		{
			whereClause = "twoTalkId >= ?";
			whereValues = new Object[]{fromTwoTalkId};
		}
		
		for (Object[] row : select(getConnection(), table, columns, whereClause, whereValues))
		{
			if (row != null)
				allIds.add((Long)row[0]);
		}
		
		return allIds;
	}
	
	public static Long getLatestTwoTalkId(int billingAccountId)
	{
		String table = "billingrecord";
		String[] columns = new String[]{"MAX(twoTalkId)"};
		String whereClause = "billingAccountId=?";
		Object[] whereValues = new Object[]{billingAccountId};
		
		for (Object[] row : select(getConnection(), table, columns, whereClause, whereValues))
		{
			if (row != null)
				return (Long)row[0];
		}
		
		return null;
	}
	
	public static ArrayList<BillingAccount> getBillingAccounts()
	{
		ArrayList<BillingAccount> allBillingAccounts = new ArrayList<BillingAccount>();
		
		String table = "billingaccount";
		String[] columns = new String[]{"*"};
		String whereClause = "";
		Object[] whereValues = null;
		
		for (Object[] row : select(getConnection(), table, columns, whereClause.toString(), whereValues))
		{
			BillingAccount account = BillingAccount.createFromDB(row);
			if (account != null) 
				allBillingAccounts.add(account);
		}
		
		return allBillingAccounts;
	}
}
