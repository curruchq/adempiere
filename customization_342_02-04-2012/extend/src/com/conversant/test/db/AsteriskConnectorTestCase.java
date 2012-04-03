package com.conversant.test.db;

import org.compiere.util.CLogger;

import com.conversant.db.AsteriskConnector;
import com.conversant.test.AdempiereTestCase;

public class AsteriskConnectorTestCase extends AdempiereTestCase 
{
	private static CLogger log = CLogger.getCLogger(AsteriskConnectorTestCase.class);
	
	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
		log.info("Done setting up AsteriskConnectorTestCase");
	}
	
	@Override
	protected void tearDown() throws Exception 
	{
		super.tearDown();
		log.info("Done tearing down AsteriskConnectorTestCase");
	}
	
	public void testAddVoicemailToDialPlan()
	{
		String number = "1234567890";
		String bpSearchKey = "1000071";
		
		if (!AsteriskConnector.addVoicemailToDialPlan(number, bpSearchKey))
			fail("Failed to add voicemail to dial plan");
		else if (!AsteriskConnector.removeVoicemailFromDialPlan(number, bpSearchKey))
			fail("Failed to remove voicemail from dial plan - remove manually");
	}
	
	public void testRemoveVoicemailFromDialPlan()
	{
		String number = "1234567890";
		String bpSearchKey = "1000071";
		
		if (AsteriskConnector.removeVoicemailFromDialPlan(number, bpSearchKey))
			fail("Returned true removing non-existent voicemail from dial plan");
		
		if (AsteriskConnector.addVoicemailToDialPlan(number, bpSearchKey))
		{
			if (!AsteriskConnector.removeVoicemailFromDialPlan(number, bpSearchKey))
				fail("Failed to remove voicemail from dial plan - remove manually");
		}
		
	}
}
