package com.conversant.db;

import java.sql.Connection;

import org.compiere.util.CLogger;

public class AsteriskConnector extends MySQLConnector 
{
	/** Logger			 */
	private static CLogger log = CLogger.getCLogger(SERConnector.class);
	
	/** Schema 			 */
	private static final String SCHEMA = "asterisk";
	
	private static Connection getConnection()
	{
		return getConnection(DEFAULT_HOST, DEFAULT_PORT, SCHEMA, DEFAULT_USERNAME, DEFAULT_PASSWORD);
	}
}
