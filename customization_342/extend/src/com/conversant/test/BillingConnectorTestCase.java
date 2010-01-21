package com.conversant.test;

import java.util.ArrayList;

import org.compiere.util.CLogger;

import com.conversant.db.BillingConnector;
import com.conversant.model.BillingRecord;

public class BillingConnectorTestCase  extends AdempiereTestCase 
{
	
	// TODO: Remove BillingRecord creation and removal to setUp and tearDown?
	
	private static CLogger log = CLogger.getCLogger(BillingConnectorTestCase.class);
	
	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
		log.info("Done setting up BillingConnectorTestCase");
	}
	
	@Override
	protected void tearDown() throws Exception 
	{
		super.tearDown();
		log.info("Done tearing down BillingConnectorTestCase");
	}

	private String[] createTestBillingFeedData()
	{
		return new String[]{
				"12345",						// 0  - ID
				"12345",						// 1  - Billing Group
				"12345",						// 2  - Origin Number
				"12345",						// 3  - Destination Number
				"Test",							// 4  - Description
				"Ok",							// 5  - Status
				"",								// 6  - Termination
				"31/08/2009 12:00:00 a.m.",		// 7  - Date
				"1/01/1900 10:57:10 a.m.",		// 8  - Time
				"31/08/2009 10:57:10 a.m.",		// 9  - Date/Time
				"1",							// 10 - Call Length (seconds)
				"0",							// 11 - Call Cost (NZD)
				"",								// 12 - Smartcode 
				"",								// 13 - Smartcode Description
				"S",							// 14 - Type
				"2T",							// 15 - SubType
				"0"};							// 16 - MP3
	}
	
	public void testAddBillingRecord()
	{
		// Create test billing record
		BillingRecord billingRecord = BillingRecord.createFromBillingFeed(createTestBillingFeedData());
		
		// Add (and remove if created successfully)
		long newId = BillingConnector.addBillingRecord(billingRecord);
		if (newId > 0)
		{
			billingRecord.setId(newId);
			if (!billingRecord.delete())
				log.severe("Failed to delete test data - " + billingRecord.toString() + ", please remove manually");
		}
		
		// Check it was created successfully
		assertTrue("Failed to add " + billingRecord.toString(), newId > 0);
	}
	
	public void testRemoveBillingRecord()
	{
		// Create and save test billing record
		BillingRecord billingRecord = BillingRecord.createFromBillingFeed(createTestBillingFeedData());
		if (!billingRecord.save())
			log.severe("Failed to save test billing record, can't test BillingConnector.removeBillingRecord()");
		else
			assertTrue("Failed to remove " + billingRecord.toString() + ", please remove manually", BillingConnector.removeBillingRecord(billingRecord));
	}
/*
 * Too many records to test this
 * 
	public void testGetBillingRecords()
	{
		// Create and save test billing record
		BillingRecord testBillingRecord = BillingRecord.createFromBillingFeed(createTestBillingFeedData());
		if (!testBillingRecord.save())
			log.severe("Failed to save test billing record, can't test BillingConnector.getBillingRecords()");
		else
		{			
			// Load all billing records and check test record exists
			boolean found = false;
			ArrayList<BillingRecord> billingRecords = BillingConnector.getBillingRecords();
			for (BillingRecord billingRecord : billingRecords)
			{
				if (billingRecord.getTwoTalkId() == testBillingRecord.getTwoTalkId() && 
					billingRecord.getOriginNumber().equals(testBillingRecord.getOriginNumber()) && 
					billingRecord.getDestinationNumber().equals(testBillingRecord.getDestinationNumber()))
				{
					found = true;
					break;
				}
			}
	
			assertTrue("Failed to load test billing record - " + testBillingRecord.toString(), found);
			
			// Remove test record
			if (!testBillingRecord.delete())
				log.severe("Failed to delete test data - " + testBillingRecord.toString() + ", please remove manually");
		}
	}
*/
	
	public void testGet2talkIds()
	{
		// TODO test using a param
		long start = System.currentTimeMillis();
		ArrayList<Long> allIds = BillingConnector.getTwoTalkIds(null);
		System.out.println("Took " + (System.currentTimeMillis() - start) + "ms to get " + allIds.size() + " ids");
		
		start = System.currentTimeMillis();
		for (Long id : allIds)
		{
			if (id < 0)	
				System.out.println(id);
		}
		
		System.out.print("Took " + (System.currentTimeMillis() - start) + "ms to loop through " + allIds.size() + " ids");
	}
	
	public void testGetLatestTwoTalkId()
	{
		
	}
}
