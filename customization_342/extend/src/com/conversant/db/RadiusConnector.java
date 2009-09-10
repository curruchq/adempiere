package com.conversant.db;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.compiere.util.CLogger;

import com.conversant.model.RadiusAccount;

public class RadiusConnector extends MySQLConnector
{
	private static CLogger log = CLogger.getCLogger(RadiusConnector.class);

	private static final String SCHEMA = "radius";
	private static final String	USERNAME = "erp_local";
	private static final String	PASSWORD = "naFJ487CB(Xp";
	
	private static Connection getDefaultConnection()
	{
		return getConnection(DEFAULT_HOST, DEFAULT_PORT, SCHEMA, USERNAME, PASSWORD);
	}
	
	public static ArrayList<RadiusAccount> getRadiusAccounts()
	{
		return getRadiusAccounts(null, null);
	}
	
	public static ArrayList<RadiusAccount> getRadiusAccounts(ArrayList<Integer> idsToExclude, Timestamp acctStartTime)
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
		
		if (acctStartTime != null)
		{
			StringBuilder whereClauseBuilder = new StringBuilder();
			if (whereClause != null)
			{
				whereClauseBuilder.append(whereClause).append(" AND ");
			}
			
			whereClauseBuilder.append("AcctStartTime < ?");
			
			whereClause = whereClauseBuilder.toString();
			whereValues.add(acctStartTime);
		}

		for (Object[] row : select(getDefaultConnection(), table, columns, whereClause, whereValues.toArray()))
		{
			RadiusAccount radiusAccount = RadiusAccount.get(row);
			if (radiusAccount != null) allAccounts.add(radiusAccount);
		}
		
		return allAccounts;
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
