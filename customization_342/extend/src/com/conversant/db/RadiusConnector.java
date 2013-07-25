package com.conversant.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.Env;

import com.conversant.model.BillingRecord;
import com.conversant.model.RadiusAccount;
import com.conversant.model.RadiusAccountInvoice;

public class RadiusConnector extends MySQLConnector
{
	private static CLogger log = CLogger.getCLogger(RadiusConnector.class);

	private static final String SCHEMA = "radius";
	
	private static Connection getConnection()
	{
		return getConnection(Env.getCtx(), SCHEMA);
	}
	
	public static ArrayList<RadiusAccount> getRadiusAccountsToBill(int maxRows, String afterAcctStartTime)
	{
		ArrayList<RadiusAccount> allAccounts = new ArrayList<RadiusAccount>();
		
		String table = "radacct";
		String[] columns = new String[]{"*"};
		String whereClause = "RadAcctId NOT IN (SELECT RadAcctId FROM radacctinvoice) AND Normalized='1' AND AcctStartTime>='" + afterAcctStartTime + "'";
		
		ArrayList<Object> whereValues = new ArrayList<Object>();
		
		// Execute sql
		ArrayList<Object[]> rows = select(getConnection(), table, columns, whereClause, whereValues.toArray(), maxRows);
		
		// Process data
		for (Object[] row : rows)
		{
			RadiusAccount radiusAccount = RadiusAccount.get(row);
			if (radiusAccount != null) 
				allAccounts.add(radiusAccount);
		}
		
		return allAccounts;
	}
	
	private static ArrayList<RadiusAccount> getRadiusAccounts(ArrayList<Integer> idsToExclude, Timestamp acctStartTimeFrom, Timestamp acctStartTimeTo)
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
	
	public static ArrayList<RadiusAccount> getRaidusAccountsSearch(String inboundUsername, String outboundUsername, String calledStationId, String dateFrom, String dateTo, String billingId)
	{
		ArrayList<RadiusAccount> radiusAccounts = new ArrayList<RadiusAccount>();
		
		String table = "radacct";
		String[] columns = new String[]{"*"};
		String whereClause = "(Username=? OR Username=?) AND CalledStationId LIKE ? AND AcctStartTime >= ? AND AcctStartTime <= ?";
		Object[] whereValues = new Object[]{inboundUsername, outboundUsername, "%" + calledStationId + "%", dateFrom, dateTo};
		
		if (billingId != null)
		{
			whereClause += "AND billingId = ?";
			whereValues = new Object[]{inboundUsername, outboundUsername, "%" + calledStationId + "%", dateFrom, dateTo, billingId};
		}
		
		// Execute sql
		ArrayList<Object[]> rows = select(getConnection(), table, columns, whereClause, whereValues);
		
		// Process data
		for (Object[] row : rows)
		{
			RadiusAccount radiusAccount = RadiusAccount.get(row);
			if (radiusAccount != null) 
				radiusAccounts.add(radiusAccount);
		}
		
		return radiusAccounts;
	}
	
	public static ArrayList<Object[]> getRaidusAccountsByInvoice(int invoiceId)
	{	
		ArrayList<Object[]> rows = new ArrayList<Object[]>();
		
		String sql = "SELECT ra.RadAcctId, ra.Username, ra.BillingId, ra.AcctStartTime, ra.AcctStopTime, ra.CallingStationId, ra.CalledStationId, ra.DestinationId, ra.Price, ra.Rate, rai.InvoiceId, rai.InvoiceLineId ";
		sql += "FROM radacct ra ";
		sql += "INNER JOIN radacctinvoice rai ON ra.RadAcctId = rai.RadAcctId ";
		sql += "WHERE rai.InvoiceId = ?";
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
        {
			conn = getConnection();
			ps = conn.prepareStatement(sql.toString());
			ps.setInt(1, invoiceId);

			rs = ps.executeQuery();
			while (rs.next())
			{
				Object[] row = new Object[12];
				
				for (int i = 0; i < 12; i++)
					row[i] = rs.getObject(i + 1);
				
				rows.add(row);
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
        		if (rs != null) rs.close();
        		if (ps != null) ps.close();
        		if (conn != null) conn.close();
        		
        		rs = null;
        		ps = null;
        		conn = null;
        	}
        	catch (SQLException ex)
        	{
        		log.log(Level.WARNING, "Couldn't close either ResultSet, PreparedStatment, or Connection", ex);
        	}
        }
		
		return rows;
	}
	
	public static ArrayList<RadiusAccountInvoice> old_getRaidusAccountsByInvoice(int invoiceId)
	{	
		ArrayList<RadiusAccount> radiusAccounts = new ArrayList<RadiusAccount>();
		
		String table = "radacct";
		String[] columns = new String[]{"*"};
		String whereClause = "RadAcctId IN (SELECT RadAcctId FROM radacctinvoice WHERE invoiceId=?)";
		Object[] whereValues = new Object[]{invoiceId};
		
		// Execute sql
		ArrayList<Object[]> rows = select(getConnection(), table, columns, whereClause, whereValues);
		
		// Process data
		for (Object[] row : rows)
		{
			RadiusAccount radiusAccount = RadiusAccount.get(row);
			if (radiusAccount != null) 
				radiusAccounts.add(radiusAccount);
		}
		
		// Return list
		ArrayList<RadiusAccountInvoice> radiusAccountInvoices = new ArrayList<RadiusAccountInvoice>();
		
		table = "radacctinvoice";
		columns = new String[]{"*"};
		whereClause = "invoiceId=?";
		whereValues = new Object[]{invoiceId};
		
		// Execute sql
		rows = select(getConnection(), table, columns, whereClause, whereValues);
		
		// Process data
		for (Object[] row : rows)
		{
			Integer radAcctId = ((Long)row[0]).intValue();
			
			for (RadiusAccount radiusAccount : radiusAccounts)
			{
				if (radiusAccount.getRadAcctId().compareTo(radAcctId) == 0)
				{
					RadiusAccountInvoice radiusAccountInvoice = new RadiusAccountInvoice(radiusAccount, (Integer)row[1], (Integer)row[2]);
					if (radiusAccountInvoice != null) 
						radiusAccountInvoices.add(radiusAccountInvoice);
					
					break;
				}
			}
			
			
		}
		
		return radiusAccountInvoices;
	}
	
	public static RadiusAccount getRadiusAccount(int radAcctId)
	{
		String table = "radacct";
		String[] columns = new String[]{"*"};
		String whereClause = "RadAcctId=?";
		Object[] whereValues = new Object[]{radAcctId};
		
		// Execute sql
		ArrayList<Object[]> rows = select(getConnection(), table, columns, whereClause, whereValues);
		
		// Process data
		for (Object[] row : rows)
		{
			RadiusAccount radiusAccount = RadiusAccount.get(row);
			if (radiusAccount != null) 
				return radiusAccount;
		}
		
		return null;
	}

	public static Long getRadiusAccountInvoiceCount()
	{
		Long count = -2L; // to avoid matching a failed getModBillingRecordCount()
		
		String table = "radacctinvoice";
		String[] columns = new String[]{"COUNT(*)"};
		String whereClause = null;
		Object[] whereValues = null;
		
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
		calendar.add(GregorianCalendar.SECOND, Integer.parseInt(br.getCallLength()));
		
		// Transform data for RadAcct
		String acctSessionId = br.getTwoTalkId() + "-202.180.76.16";
		String userName = "64" + billingGroup + "@conversant.co.nz";
		String realm = "conversant.co.nz";
		String nASIPAddress = "202.180.76.164";
		String nASPortId = "5060";
		Date acctStartTime = br.getDateTime();
		Date acctStopTime = calendar.getTime();
		Integer acctSessionTime = Integer.parseInt(br.getCallLength()); // TODO: Catch error?
		String calledStationId = "00" + br.getDestinationNumber() + "@conversant.co.nz";
		String callingStationId = br.getOriginNumber() + "@conversant.co.nz";
		String rate = "";
		String rTPStatistics = "";
		
		// Change appropriate values for inbound calls
		if (br.getType().equals(BillingRecord.TYPE_INBOUND) || br.getType().equals("IS") || br.getType().equals("IM"))
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
		
		
		Connection conn = getConnection(DEFAULT_HOST, DEFAULT_PORT, SCHEMA, DEFAULT_USERNAME, DEFAULT_PASSWORD);
		PreparedStatement ps = null;
		String sql = "SELECT * FROM RADACCTINVOICE";
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
	
	public static ArrayList<RadiusAccount> getRadiusAccountsSearch(String inboundUsername, String outboundUsername, String domain ,String realm ,String calledStationId, String dateFrom, String dateTo, String billingId)
	{
		ArrayList<RadiusAccount> radiusAccounts = new ArrayList<RadiusAccount>();
		
		String table = "radacct";
		String[] columns = new String[]{"*"};
		String whereClause = "(Username=? OR Username=?) AND (Realm LIKE ? OR Realm LIKE ?) AND CalledStationId LIKE ? AND AcctStartTime >= ? AND AcctStartTime <= ?";
		Object[] whereValues = new Object[]{inboundUsername, outboundUsername,"%" + domain + "%","%" + realm + "%","%" + calledStationId + "%", dateFrom, dateTo};
		
		if (billingId != null)
		{
			whereClause += "AND billingId = ?";
			whereValues = new Object[]{inboundUsername, outboundUsername,"%" + domain + "%","%" + realm + "%","%" + calledStationId + "%", dateFrom, dateTo, billingId};
		}
		
		// Execute sql
		ArrayList<Object[]> rows = select(getConnection(), table, columns, whereClause, whereValues);
		
		// Process data
		for (Object[] row : rows)
		{
			RadiusAccount radiusAccount = RadiusAccount.get(row);
			if (radiusAccount != null) 
				radiusAccounts.add(radiusAccount);
		}
		
		return radiusAccounts;
	}
}
