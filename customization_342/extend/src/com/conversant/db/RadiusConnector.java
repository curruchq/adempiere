package com.conversant.db;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.compiere.util.CLogger;
import org.compiere.util.Env;

import com.conversant.model.BillingRecord;
import com.conversant.model.RadiusAccount;

public class RadiusConnector extends MySQLConnector
{
	private static CLogger log = CLogger.getCLogger(RadiusConnector.class);

	private static final String SCHEMA = "radius";
	
	private static Connection getConnection()
	{
		return getConnection(Env.getCtx(), SCHEMA);
	}
	
	public static ArrayList<RadiusAccount> getRadiusAccounts()
	{
		ArrayList<RadiusAccount> allAccounts = new ArrayList<RadiusAccount>();
		
		String table = "radacct";
		String[] columns = new String[]{"*"};
		String whereClause = "RadAcctId NOT IN (SELECT RadAcctId FROM radacctinvoice) AND Normalized='1'";
		ArrayList<Object> whereValues = new ArrayList<Object>();
		
		// Execute sql
		ArrayList<Object[]> rows = select(getConnection(), table, columns, whereClause, whereValues.toArray());
		
		// Process data
		for (Object[] row : rows)
		{
			RadiusAccount radiusAccount = RadiusAccount.get(row);
			if (radiusAccount != null) 
				allAccounts.add(radiusAccount);
		}
		
		return allAccounts;
	}
	
	public static ArrayList<RadiusAccount> getRadiusAccounts(ArrayList<Integer> idsToExclude, Timestamp acctStartTimeFrom, Timestamp acctStartTimeTo)
	{
		ArrayList<RadiusAccount> allAccounts = new ArrayList<RadiusAccount>();
		
		String table = "radacct";
		String[] columns = new String[]{"*"};
		String whereClause = null;
		ArrayList<Object> whereValues = new ArrayList<Object>();
		
		// build where clause
		if (idsToExclude != null && idsToExclude.size() > 0)
		{
			StringBuilder whereClauseBuilder = new StringBuilder("RadAcctId NOT IN (");
			for (int i=0; i<idsToExclude.size(); i++)
			{
				whereClauseBuilder.append("?,");
				whereValues.add(idsToExclude.get(i));
			}
			whereClauseBuilder.replace(whereClauseBuilder.length() - 1, whereClauseBuilder.length(), ""); // replace trailing comma
			whereClauseBuilder.append(")");
			whereClause = whereClauseBuilder.toString();
		}
		
		if (acctStartTimeFrom != null && acctStartTimeTo != null)
		{
			StringBuilder whereClauseBuilder = new StringBuilder();
			if (whereClause != null)
				whereClauseBuilder.append(whereClause).append(" AND ");
			
			whereClauseBuilder.append("AcctStartTime BETWEEN ? AND ?");
			
			whereClause = whereClauseBuilder.toString();
			whereValues.add(acctStartTimeFrom);
			whereValues.add(acctStartTimeTo);
		}
//		else if (acctStartTimeFrom != null && acctStartTimeTo == null)
//		{
//			StringBuilder whereClauseBuilder = new StringBuilder();
//			if (whereClause != null)
//				whereClauseBuilder.append(whereClause).append(" AND ");
//			
//			whereClauseBuilder.append("AcctStartTime > ?");
//			
//			whereClause = whereClauseBuilder.toString();
//			whereValues.add(acctStartTimeFrom);
//		}
//		else if (acctStartTimeFrom == null && acctStartTimeTo != null)
//		{
//			StringBuilder whereClauseBuilder = new StringBuilder();
//			if (whereClause != null)
//				whereClauseBuilder.append(whereClause).append(" AND ");
//			
//			whereClauseBuilder.append("AcctStartTime < ?");
//			
//			whereClause = whereClauseBuilder.toString();
//			whereValues.add(acctStartTimeTo);
//		}
		else // one or both null
		{
			log.severe("Cannot get Radius accounts without valid start time to and from values. acctStartTimeFrom=" + acctStartTimeFrom + ", acctStartTimeTo=" + acctStartTimeTo);
			return allAccounts;
		}

		for (Object[] row : select(getConnection(), table, columns, whereClause, whereValues.toArray()))
		{
			RadiusAccount radiusAccount = RadiusAccount.get(row);
			if (radiusAccount != null) allAccounts.add(radiusAccount);
		}
		
		return allAccounts;
	}
	
	public static Long getRadiusAccountInvoiceCount()
	{
		Long count = -2L; // to avoid matching a failed getModBillingRecordCount()
		
		String table = "radacctinvoice";
		String[] columns = new String[]{"COUNT(*)"};
		String whereClause = "isActive=?";
		Object[] whereValues = new Object[]{new Boolean(true)};
		
		// Execute sql
		ArrayList<Object[]> rows = select(getConnection(), table, columns, whereClause, whereValues);
		
		// Process data
		Object[] row = (Object[])rows.get(0);
		count = (Long)row[0];		
		
		return count;
	}
	
	public static boolean addRadiusAccount(BillingRecord br)
	{
		String table = "radacct";
		String[] columns = new String[]{"AcctSessionId", "UserName", "Realm", "NASIPAddress", "NASPortId",  
										"AcctStartTime", "`AcctStopTime`", "AcctSessionTime", "CalledStationId", 
										"CallingStationId", "Rate", "RTPStatistics"};
				
		// Strip leading 0
		String billingGroup = br.getBillingGroup();
		if (billingGroup.startsWith("0"))
			billingGroup = billingGroup.substring(1, billingGroup.length());
		
		// Calculate account start time
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(br.getDateTime());
		calendar.add(GregorianCalendar.SECOND, Integer.parseInt(br.getCallLength()) * -1);
		
		// Transform data for RadAcct
		String acctSessionId = br.getTwoTalkId() + "-202.180.76.16";
		String userName = "64" + billingGroup + "@conversant.co.nz";
		String realm = "conversant.co.nz";
		String nASIPAddress = "202.180.76.164";
		String nASPortId = "5060";
		Date acctStartTime = calendar.getTime();
		Date acctStopTime = br.getDateTime();
		Integer acctSessionTime = Integer.parseInt(br.getCallLength()); // TODO: Catch error?
		String calledStationId = "00" + br.getDestinationNumber() + "@conversant.co.nz";
		String callingStationId = br.getOriginNumber() + "@conversant.co.nz";
		String rate = "";
		String rTPStatistics = "";
		
		// Change appropriate values for inbound calls
		if (br.getType().equals(BillingRecord.TYPE_INBOUND))
		{
			userName = "+" + br.getDestinationNumber() + "@inbound.conversant.co.nz";
			realm = "inbound.conversant.co.nz";
			
			if (br.getOriginNumber().equalsIgnoreCase("restricted"))
			{
				if (br.getDescription().startsWith("Mobile"))
					calledStationId = "00642000@conversant.co.nz";
				else
					calledStationId = "00640000@conversant.co.nz";
			}
			else
				calledStationId = "00" +  br.getOriginNumber() + "@conversant.co.nz";
			
			callingStationId = "+" + br.getDestinationNumber() + "@inbound.conversant.co.nz";
		}		
		
		Object[] values = new Object[]{acctSessionId, userName, realm, nASIPAddress, nASPortId, 
									   acctStartTime, acctStopTime, acctSessionTime, calledStationId, 
									   callingStationId, rate, rTPStatistics};
									   	
		return insert(getConnection(), table, columns, values);
	}
	
	public static boolean addRadiusAccountInvoice(Integer radAcctId, Integer invoiceId, Integer invoiceLineId)
	{
		String table = "radacctinvoice";
		String[] columns = new String[]{"RadAcctId", "invoiceId", "invoiceLineId"};
		Object[] values = new Object[]{radAcctId, invoiceId, invoiceLineId};
		
		return insert(getConnection(), table, columns, values);
	}
	
	public static boolean updateRadiusAccountInvoice(Integer radAcctId, Integer invoiceId, Integer invoiceLineId)
	{
		// Set parameters	
		String table = "radacctinvoice";
		String[] columnsToUpdate = new String[]{"invoiceId", "invoiceLineId"};
		Object[] valuesToUpdate = new Object[]{invoiceId, invoiceLineId};		
		
		String[] whereColumns = new String[]{"RadAcctId"};
		Object[] whereValues = new Object[]{radAcctId};
		
		if (!update(getConnection(), table, columnsToUpdate, valuesToUpdate, whereColumns, whereValues))
		{
			log.severe("Failed to update RadAcctId[" + radAcctId + "] with InvoiceId[" + invoiceId + "]  and InvoiceLineId[" + invoiceLineId + "]");
			return false;
		}
		
		return true;
	}
	
	public static void main (String[] args)
	{
	/*
		ArrayList<RadiusAccount> accounts = RadiusConnector.getRadiusAccounts();
		ArrayList<Integer> ids = new ArrayList<Integer>();
		boolean first = true;
		for (RadiusAccount ra : accounts)
		{
			if (!first)
				ids.add(ra.getRadAcctId());
			else
				first = false;
		}	
		accounts = RadiusConnector.getRadiusAccounts(ids);
		for (RadiusAccount ra : accounts)
		{
			System.out.println(ra.getRadAcctId());
		}
		
		
		Connection conn = getDefaultConnection();
		PreparedStatement ps = null;
		String sql = "SELECT * FROM RADACCT";
		Object[] whereValues = null;
		try
        {
			ps = conn.prepareStatement(sql);
			
			if (setValues(ps, whereValues))
			{
				// Execute Query
				ResultSet rs = ps.executeQuery();
				ResultSetMetaData rsmd = (ResultSetMetaData)rs.getMetaData();
				for (int i=1;i<=rsmd.getColumnCount();i++)
				{
					String className = rsmd.getColumnClassName(i);
					className = className.substring(className.lastIndexOf(".") + 1, className.length());
					System.out.println("private " + className + " " + rsmd.getColumnName(i) + " = null;");
				}
				
				for (int i=1;i<=rsmd.getColumnCount();i++)
				{
					String className = rsmd.getColumnClassName(i);
					className = className.substring(className.lastIndexOf(".") + 1, className.length());
					System.out.println(rsmd.getColumnName(i) + " = (" + className + ")dbRow[" + (i - 1) + "];");
				}
			}
        }
        catch (SQLException ex)
        {
        	log.log(Level.SEVERE, "Select failed. SQL='" + sql.toString() + "'", ex);
        }
        finally
        {
        	try
        	{
        		if (ps != null) ps.close();
        		if (conn != null) conn.close();
        	}
        	catch (SQLException ex)
        	{
        		log.log(Level.WARNING, "Couldn't close either PreparedStatment or Connection", ex);
        	}
        }
    */
	}
}
