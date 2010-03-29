package com.conversant.test;

import java.util.Properties;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MProduct;
import org.compiere.util.CLogger;
import org.compiere.wstore.DIDController;

import com.conversant.model.DIDAreaCode;
import com.conversant.model.DIDCountry;

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
