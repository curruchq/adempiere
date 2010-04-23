package com.conversant.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MDBProfile;
import org.compiere.util.CLogger;

public abstract class MySQLConnector
{
	/** Logger						*/
	private static CLogger log = CLogger.getCLogger(MySQLConnector.class);
	
	/** Driver Class Name			*/
	protected static final String		DRIVER = "com.mysql.jdbc.Driver";

	/** Default Host				*/
	protected static final String		DEFAULT_HOST = "localhost";
	
	/** Default Port            	*/
	protected static final int 			DEFAULT_PORT = 3306;
	
	/** Connection Properties		*/
	protected static final String 		DEFAULT_CONNECTION_PROPERTIES = "?zeroDateTimeBehavior=convertToNull";

	/** Default username			*/
	protected static final String		DEFAULT_USERNAME = "erp_local";
	
	/** Default password 			*/
	protected static final String		DEFAULT_PASSWORD = "naFJ487CB(Xp";
	
	/** Max rows					*/
	protected static final int 			MAX_ROWS = 100;

	protected static Connection getConnection(Properties ctx, String schema)
	{
		MDBProfile profile = MDBProfile.getBySchema(ctx, schema, null);
		if (profile != null && profile.getDB_SCHEMA().equalsIgnoreCase(schema))
			return getConnection(profile.getDB_HOST(), profile.getDB_PORT(), schema, profile.getDB_USERNAME(), profile.getDB_PASSWORD());
		else
		{
			log.warning("Failed to get MDBProfile for schema '" + schema + "', using defaults");
			return getConnection(DEFAULT_HOST, DEFAULT_PORT, schema, DEFAULT_USERNAME, DEFAULT_PASSWORD);
		}
	}
	
	private static Connection getConnection(String host, int port, String schema, String username, String password)
	{
		// Define URL of database server
		String url = "jdbc:mysql://" + host + ":" + port + "/" + schema + DEFAULT_CONNECTION_PROPERTIES;
		
		log.fine("Getting connection to " + url);
		
		try
		{
			// Register the JDBC driver for MySQL
			Class.forName(DRIVER);
			
			// Get Connection 
			Connection conn = DriverManager.getConnection(url, username, password);
			
			return conn;
		}
		catch (ClassNotFoundException ex)
		{
			log.log(Level.SEVERE, "", ex);
		}
		catch (SQLException ex)
		{
			log.log(Level.SEVERE, "Could not get connection from " + url + " where username=" + username, ex);
		}
		
		return null;
	}
	
	protected static ArrayList<Object[]> select(Connection conn, String table, String[] columns, String[] whereFields, Object[] whereValues)
	{
		StringBuilder whereClause = new StringBuilder();
		for (String field : whereFields)
		{
			whereClause.append(field + "=? AND ");
		}
		whereClause.delete(whereClause.length()-5, whereClause.length());
		
		return select(conn, table, columns, whereClause.toString(), whereValues);
	}
	
	protected static ArrayList<Object[]> select(Connection conn, String table, String[] columns, String whereClause, Object[] whereValues)
	{
		return select(conn, table, columns, whereClause, whereValues, false);
	}
	
	protected static ArrayList<Object[]> select(Connection conn, String table, String[] columns, String whereClause, Object[] whereValues, boolean limitRows)
	{
		// Set up return list
		ArrayList<Object[]> allRows = new ArrayList<Object[]>();
		
		// Validate all parameters
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("Connection", conn);
		parameters.put("TableName", table);
		parameters.put("Columns", columns);
		if (!validateParameters(parameters)) 
			return allRows;


		if (whereClause != null && whereClause.contains("?"))
		{
			if (whereValues != null)
			{
				int questionMarkCount = 0;
				char[] chars = new char[whereClause.length()];
				whereClause.getChars(0, whereClause.length(), chars, 0);
				for (char c : chars)
				{
					if (c == '?')
					{
						questionMarkCount++;
					}
				}
				
				if (questionMarkCount != whereValues.length)
				{
					log.warning("The WHERE clause supplied required " + questionMarkCount + " values but only " + whereValues.length + " were given");
					return allRows;
				}
			}
			else
			{
				log.warning("The WHERE clause which was supplied required values but value array is NULL");
				return allRows;
			}
		}
		else
		{
			// set whereClause & whereValues to NULL as the WHERE clause is NULL or contains no '?' characters
			whereClause = null;
			whereValues = null;
		}
		
		// Build SQL string
		StringBuilder sql = new StringBuilder("SELECT ");
		
		for (String column : columns)
		{
			sql.append(column + ", ");
		}
		
		sql.delete(sql.length()-2, sql.length()); // remove last comma and space
		sql.append(" FROM " + table);
		
		if (whereClause != null && whereClause.length() > 0)
		{
			sql.append(" WHERE " + whereClause);
		}
		
		if (limitRows)
			sql.append(" LIMIT " + MAX_ROWS);
		
		// Create Statement
		PreparedStatement ps = null;
		try
        {
			ps = conn.prepareStatement(sql.toString());
			
			if (setValues(ps, whereValues))
			{
				// Execute Query
				ResultSet rs = ps.executeQuery();
				ResultSetMetaData rsmd = (ResultSetMetaData)rs.getMetaData();
				int columnCount = rsmd.getColumnCount();
				while (rs.next())
				{
					Object[] row = new Object[columnCount];
					for (int i=0; i<columnCount; i++)
					{
						row[i] = rs.getObject(i+1); // column index starts at 1
					}
					allRows.add(row);
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
		
		
		return allRows;
	}
	
	protected static boolean insert(Connection conn, String table, String[] columns, Object[] values)
	{
		// Validate all parameters
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("Connection", conn);
		parameters.put("TableName", table);
		parameters.put("Columns", columns);
		parameters.put("Values", values);
		if (!validateParameters(parameters)) 
			return false;
		
		if (columns.length != values.length)
		{
			log.warning("Length of columns doesn't equal that of values. Columns=" + columns.length + ", Values=" + values.length);
			return false;
		}
		
		// Build SQL string
		StringBuilder sql = new StringBuilder("INSERT INTO " + table + "(");
		
		for (String column : columns)
		{
			sql.append(column + ", ");
		}
		
		sql.delete(sql.length()-2, sql.length()); // remove last comma and space
		sql.append(") VALUES (");
		
		for (int i=0; i<values.length; i++)
		{
			sql.append("?, ");
		}
		
		sql.delete(sql.length()-2, sql.length()); // remove last comma and space
		sql.append(")");
		
		// Create Statement
		PreparedStatement ps = null;
		try
        {
			ps = conn.prepareStatement(sql.toString());
        	
    		if (setValues(ps, values))
    		{
    			// Execute
    			int no = ps.executeUpdate();
    			
    			if (no == 1)
    				return true;
    			else
    				log.warning("Insert unsuccessful, returned value=" + no + ", SQL='" + sql.toString() + "'");
    		}
        }
        catch (SQLException ex)
        {
        	log.log(Level.SEVERE, "Insert failed, SQL='" + sql.toString() + "'", ex);
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
        
        return false;
	}
	
	protected static boolean update(Connection conn, String table, String rowToUpdateName, Object rowToUpdateValue, String[] columns, Object[] values)
	{
		// Validate all parameters
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("Connection", conn);
		parameters.put("TableName", table);
		parameters.put("RowToUpdateName", rowToUpdateName);
		parameters.put("RowToUpdateValue", rowToUpdateValue);
		parameters.put("Columns", columns);
		parameters.put("Values", values);
		if (!validateParameters(parameters)) 
			return false;
		
		if (columns.length != values.length)
		{
			log.warning("Length of columns doesn't equal that of values. Columns=" + columns.length + ", Values=" + values.length);
			return false;
		}
		
		// Build SQL string
		StringBuilder sql = new StringBuilder("UPDATE " + table + " SET ");
		
		for (String column : columns)
		{
			sql.append(column + "=?, ");
		}
		
		sql.delete(sql.length()-2, sql.length()); // remove last comma and space
		sql.append(" WHERE " + rowToUpdateName + "=?");
		
		// Create new array to add the rowToUpdateValue to the end
		Object[] newValues = new Object[values.length + 1]; 
		System.arraycopy(values, 0, newValues, 0, values.length);
		newValues[newValues.length-1] = rowToUpdateValue;
		
		// Create Statement
		PreparedStatement ps = null;
		try
        {
			ps = conn.prepareStatement(sql.toString());
        	
    		if (setValues(ps, newValues))
    		{
    			// Execute
    			int no = ps.executeUpdate();
    			
    			if (no == 1)
    				return true;
    			else
    				log.warning("Update unsuccessful, returned value=" + no + ", SQL='" + sql.toString() + "'");
    		}
        }
        catch (SQLException ex)
        {
        	log.log(Level.SEVERE, "Update failed, SQL='" + sql.toString() + "'", ex);
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
		
		return false;
	}
	
	protected static boolean delete (Connection conn, String table, String whereClause, Object[] whereValues)
	{
		// Validate all parameters
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("Connection", conn);
		parameters.put("TableName", table);
		if (!validateParameters(parameters)) 
			return false;

		// Validate where clause
		if (whereClause != null && whereClause.contains("?"))
		{
			if (whereValues != null)
			{
				int questionMarkCount = 0;
				char[] chars = new char[whereClause.length()];
				whereClause.getChars(0, whereClause.length(), chars, 0);
				for (char c : chars)
				{
					if (c == '?')
					{
						questionMarkCount++;
					}
				}
				
				if (questionMarkCount != whereValues.length)
				{
					log.warning("The WHERE clause supplied required " + questionMarkCount + " values but only " + whereValues.length + " were given, DELETE failed.");
					return false;
				}
			}
			else
			{
				log.warning("The WHERE clause which was supplied required values but value array is NULL, DELETE failed.");
				return false;
			}
		}
		else
		{
			log.warning("Invalid WHERE clause supplied, DELETE failed.");
			return false;
		}
		
		// Build SQL string
		StringBuilder sql = new StringBuilder("DELETE FROM " + table);
		
		if (whereClause != null && whereClause.length() > 0)
		{
			sql.append(" WHERE " + whereClause);
		}
		
		// Create Statement
		PreparedStatement ps = null;
		try
        {
			ps = conn.prepareStatement(sql.toString());
			
			if (setValues(ps, whereValues))
			{
				// Execute delete
				int result = ps.executeUpdate();
				
				log.info("DELETE for sql='" + sql.toString() + "' returned " + result);
				
				if (result > 0)
					return true;
			}
        }
        catch (SQLException ex)
        {
        	log.log(Level.SEVERE, "DELETE failed. SQL='" + sql.toString() + "'", ex);
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
		
		
		return false;
	}
	
	/**
	 * Checks each object is not null, if string it's not empty & if array it's not empty
	 * 
	 * @param parameters
	 * @return
	 */
	protected static boolean validateParameters(HashMap<String, Object> parameters)
	{
		boolean allValid = true;
		Iterator<String> paramIterator = parameters.keySet().iterator();
		while(paramIterator.hasNext())
		{
			String name = (String)paramIterator.next();
			Object parameter = parameters.get(name);
			
			if (parameter == null)
			{
				log.fine(name + " is NULL");
				allValid = false;
			}
			else if (parameter instanceof String && ((String)parameter).length() < 1)
			{
				log.fine(name + " is a String and it is empty");
				allValid = false;
			}
			else if (parameter instanceof Object[] && ((Object[])parameter).length < 1)
			{
				log.fine(name + " is an Array and it is empty");
				allValid = false;
			}
		}
		
		return allValid;
	}
	
	protected static boolean setValues(PreparedStatement ps, Object[] values)
	{
		if (ps != null && values != null && values.length > 0)
		{
			try
			{
	    		for (int i=0; i<values.length; i++)
	        	{
	        		Object value = values[i];
	        		int pos = i + 1;
	        		
	        		if (value == null)
	        			ps.setNull(pos, java.sql.Types.NULL);
	        		else if (value instanceof String)
	        			ps.setString(pos, (String)value);
	        		else if (value instanceof Boolean)
	        			ps.setBoolean(pos, (Boolean)value);
	        		else if (value instanceof Integer)
	        			ps.setInt(pos, (Integer)value);
	        		else if (value instanceof Long)
	        			ps.setLong(pos, (Long)value);
	        		else if (value instanceof Timestamp)
	        			ps.setTimestamp(pos, (Timestamp)value);
	        		else if (value instanceof java.sql.Date)
	        			ps.setDate(pos, (Date)value);
	        		else if (value instanceof java.util.Date)
	        		{
	        			java.util.Date date = (java.util.Date)value;
	        			ps.setTimestamp(pos, new Timestamp(date.getTime()));
	        		}	        		
	        		else
	        		{
	        			log.warning("Could not match value to a type, value's class=" + value.getClass() + ". Setting to NULL");
	        			ps.setNull(pos, java.sql.Types.NULL);
		        	}
	        	}
			}
			catch (SQLException ex)
			{
				log.log(Level.SEVERE, "Failed to set values", ex);
				return false;
			}
		}
		
		return true;
	}
}
