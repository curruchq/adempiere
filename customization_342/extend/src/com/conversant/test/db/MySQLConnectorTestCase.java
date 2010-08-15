package com.conversant.test.db;

import java.sql.Timestamp;

import org.compiere.util.CLogger;

import com.conversant.db.MySQLConnector;
import com.conversant.db.SERConnector;
import com.conversant.test.AdempiereTestCase;

public class MySQLConnectorTestCase extends AdempiereTestCase 
{
private static CLogger log = CLogger.getCLogger(MySQLConnectorTestCase.class);
	
	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
		log.info("Done setting up MySQLConnectorTestCase");
	}
	
	@Override
	protected void tearDown() throws Exception 
	{
		super.tearDown();
		log.info("Done tearing down MySQLConnectorTestCase");
	}
	
	public void testUpdate()
	{
		String uuid = "1000071";
		String username = "123456789";
		String domain = "conversant.co.nz";
		String attribute = "42001";
		
		String table = "usr_preferences";
		String[] columnsToUpdate = new String[]{"value", "modified"};
		Object[] valuesToUpdate = new Object[]{"Inactive", new Timestamp(System.currentTimeMillis())};		
		
		String[] whereColumns = new String[]{"uuid", "username", "domain", "attribute"};
		String[] whereValues = new String[]{uuid, username, domain, attribute};
		
		if (!MySQLConnector.update(SERConnector.getConnection(), table, columnsToUpdate, valuesToUpdate, whereColumns, whereValues))
			fail("Failed to update User Preference");
	}
}
