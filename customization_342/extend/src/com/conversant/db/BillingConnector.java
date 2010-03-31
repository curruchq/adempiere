package com.conversant.db;

import java.sql.Connection;
import java.util.Date;
import java.util.ArrayList;

import org.compiere.util.CLogger;
import org.compiere.util.Env;

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
										"smartCode", "smartCodeDescription", "type", "subType", "mp3"};
				
		Object[] values = new Object[]{br.getTwoTalkId(), br.getBillingGroup(), br.getOriginNumber(), br.getDestinationNumber(), br.getDescription(), 
									   br.getStatus(), br.getTerminated(), br.getDate(), br.getTime(), br.getDateTime(), br.getCallLength(), br.getCallCost(), 
									   br.getSmartCode(), br.getSmartCodeDescription(), br.getType(), br.getSubType(), br.isMp3()};
									   	
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

	public static ArrayList<BillingRecord> getBillingRecords(String originNumber, String destinationNumber, Date date)
	{
		ArrayList<BillingRecord> allBillingRecords = new ArrayList<BillingRecord>();
		
		String table = "billingrecord";
		String[] columns = new String[]{"*"};
		
		StringBuilder whereClause = new StringBuilder();		
		
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
		
		Object[] whereValues = new Object[]{originNumber, destinationNumber};
		
		if (date != null)
		{
			whereClause.append(" AND date=?");
			whereValues = new Object[]{originNumber, destinationNumber, date};
		}					
		
		for (Object[] row : select(getConnection(), table, columns, whereClause.toString(), whereValues, true))
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
	
	public static Long getLatestTwoTalkId()
	{
		String table = "billingrecord";
		String[] columns = new String[]{"MAX(twoTalkId)"};
		String whereClause = "";
		Object[] whereValues = null;
		
		for (Object[] row : select(getConnection(), table, columns, whereClause, whereValues))
		{
			if (row != null)
				return (Long)row[0];
		}
		
		return null;
	}
	
	public static int addSubscribedNumber(String number)
	{
		String table = "subscribedNumber";
		String[] columns = new String[]{"number"};
				
		Object[] values = new Object[]{number};
									   	
		if (insert(getConnection(), table, columns, values))
		{
			// Get newly created id
			columns =  new String[]{"id"};
			String[] whereFields = new String[]{"number"};
			Object[] whereValues = new Object[]{number};
			
			ArrayList<Object[]> rows = select(getConnection(), table, columns, whereFields, whereValues);
			if (rows != null && rows.size() > 0 && rows.get(0) != null)
			{
				return (Integer)((Object[])rows.get(0))[0];
			}
			else
				log.severe("Failed to select newly created row");
		}
		else
			log.severe("Failed to add subscribed number");
		
		return -1;
	}
	
	public static ArrayList<String> getSubscribedNumbers()
	{
		ArrayList<String> numbers = new ArrayList<String>();
		
		String table = "subscribedNumber";
		String[] columns = new String[]{"number"};
		
		ArrayList<Object[]> rows = select(getConnection(), table, columns, "", null);
		for (Object[] row : rows)
		{
			if (row != null && row[0] != null && row[0] instanceof String)
				numbers.add((String)row[0]);
		}
		
		return numbers;
	}
}
