package com.conversant.test;

import java.util.ArrayList;

import org.compiere.util.CLogger;

import com.conversant.did.DIDUtil;
import com.conversant.process.BillingFeedSync;

public class BillingFeedSyncTestCase  extends AdempiereTestCase 
{
	private static CLogger log = CLogger.getCLogger(BillingFeedSyncTestCase.class);
	
	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
		log.info("Done setting up BillingFeedSyncTestCase");
	}
	
	@Override
	protected void tearDown() throws Exception 
	{
		super.tearDown();
		log.info("Done tearing down BillingFeedSyncTestCase");
	}

	public void testGetBillingRecords()
	{
//		BillingFeedSync.getBillingRecords(0, 0);
	}
	
	public void testLoadBillingFeed()
	{
//		BillingFeedSync.loadBillingFeed(0);
		
		// Get subscribed fax numbers to update billing data
		ArrayList<String> subscribedFaxNumbers = DIDUtil.getSubscribedFaxNumbers(getCtx(), null);
		
		for (String subscribedFaxNumber : subscribedFaxNumbers)
			System.out.print("'" + subscribedFaxNumber + "', ");	
	}
	
	public void testValidateHeaders()
	{
//		BillingFeedSync.validateHeaders(null);
	}
	
	public void testValidateRow()
	{
		// Valid tests
		
		
		// Invalid tests
		assertFalse("NULL row was incorrectly validated", BillingFeedSync.validateRow(null)); // null 
		assertFalse("Row with missing data was incorrectly validated", BillingFeedSync.validateRow(new String[]{"1","2"})); // missing data
	}
}
