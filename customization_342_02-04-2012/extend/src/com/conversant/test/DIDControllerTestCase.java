package com.conversant.test;

import org.compiere.util.CLogger;

public class DIDControllerTestCase extends AdempiereTestCase 
{
	private static CLogger log = CLogger.getCLogger(DIDControllerTestCase.class);
	
	private static boolean SHOW_TIMING = true;
	
	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
		log.info("Done setting up DIDControllerTestCase");
	}
	
	@Override
	protected void tearDown() throws Exception 
	{
		super.tearDown();
		log.info("Done tearing down DIDControllerTestCase");
	}
}
