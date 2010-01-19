package com.conversant.db;

import java.sql.Connection;
import java.util.ArrayList;

import org.compiere.util.CLogger;
import org.compiere.util.Env;

import com.conversant.model.BillingRecord;

public class BillingConnector extends MySQLConnector 
{
	private static CLogger log = CLogger.getCLogger(BillingConnector.class);

	private static final String SCHEMA = "billing";
	
	private static Connection getConnection()
	{
		return getConnection(Env.getCtx(), SCHEMA);
	}
	
	public static Integer addBillingRecord(BillingRecord br)
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
	
	public static ArrayList<BillingRecord> getBillingRecords()
	{
		ArrayList<BillingRecord> allBillingRecords = new ArrayList<BillingRecord>();
		
		String table = "billingrecord";
		String[] columns = new String[]{"*"};
		
		for (Object[] row : select(getConnection(), table, columns, "", null))
		{
			BillingRecord br = BillingRecord.createFromDB(row);
			if (br != null) allBillingRecords.add(br);
		}
		
		return allBillingRecords;
	}
}
