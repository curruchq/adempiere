package com.conversant.db;

import java.sql.Connection;
import java.util.ArrayList;

import org.compiere.util.CCache;
import org.compiere.util.CLogger;
import org.compiere.util.Env;

public class CDRToolConnector extends MySQLConnector
{
	private static CLogger log = CLogger.getCLogger(CDRToolConnector.class);

	private static final String SCHEMA = "cdrtool";
	
	/**	Cache						*/
	private static CCache<String, String> destination_cache = new CCache<String, String>("Destinations", 20, 0);
	
	private static Connection getConnection()
	{
		return getConnection(Env.getCtx(), SCHEMA);
	}

	public static String getDestination(String destinationId)
	{
		if (destination_cache.containsKey(destinationId)) {
			return destination_cache.get(destinationId);
		}
		
		String table = "destinations";
		String[] columns = new String[]{"dest_name"};
		String whereClause = "dest_id=?";
		Object[] whereValues = new Object[]{destinationId};
		
		// Execute sql
		ArrayList<Object[]> rows = select(getConnection(), table, columns, whereClause, whereValues);
		
		//
		if (rows.size() > 0 && rows.get(0) != null && rows.get(0)[0] != null && rows.get(0)[0] instanceof String)
		{
			String destinationName = (String)rows.get(0)[0];
			destination_cache.put(destinationId, destinationName);
			return destinationName;
		}
		else
			log.warning("Failed to load Destination for id " + destinationId);
		
		return "";
	}
}
