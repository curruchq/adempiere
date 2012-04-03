package com.conversant.test;

import org.compiere.util.CLogger;

import com.conversant.model.BillingRecord;

public class BillingRecordTestCase extends AdempiereTestCase 
{
	private static CLogger log = CLogger.getCLogger(BillingRecordTestCase.class);
	
	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
		log.info("Done setting up BillingRecordTestCase");
	}
	
	@Override
	protected void tearDown() throws Exception 
	{
		super.tearDown();
		log.info("Done tearing down BillingRecordTestCase");
	}

	private String[] createTestBillingFeedData()
	{
		return new String[]{
				"1000000",						// 0  - ID
				"049740590",					// 1  - Billing Group
				"6449740590",					// 2  - Origin Number
				"6499744530",					// 3  - Destination Number
				"Cameron Beattie",				// 4  - Description
				"Ok",							// 5  - Status
				"",								// 6  - Termination
				"31/08/2009 12:00:00 a.m.",		// 7  - Date
				"1/01/1900 10:57:10 a.m.",		// 8  - Time
				"31/08/2009 10:57:10 a.m.",		// 9  - Date/Time
				"142",							// 10 - Call Length (seconds)
				"0",							// 11 - Call Cost (NZD)
				"",								// 12 - Smartcode 
				"",								// 13 - Smartcode Description
				"S",							// 14 - Type
				"2T",							// 15 - SubType
				"0"};							// 16 - MP3
	}
	
	public void testCreateFromBillingFeed()
	{
		String[] data = createTestBillingFeedData();
	
		/* ***************  Valid data *************************/
		BillingRecord billingRecord = BillingRecord.createFromBillingFeed(data);
		assertNotNull("Failed to create billing record with valid data", billingRecord);
		
		data[9] = "7/07/2009 1:57:10 a.m."; // DateTime - single digit day of month
		billingRecord = BillingRecord.createFromBillingFeed(data);
		assertNotNull("Failed to create billing record with valid single digit day of month - Date/Time[" + data[9] + "]", billingRecord);
		
		data[9] = "21/7/2009 1:57:10 a.m."; // DateTime - single digit month
		billingRecord = BillingRecord.createFromBillingFeed(data);
		assertNotNull("Failed to create billing record with valid single digit month - Date/Time[" + data[9] + "]", billingRecord);
		
		data[9] = "12/08/2009 1:57:10 a.m."; // DateTime - single digit hour
		billingRecord = BillingRecord.createFromBillingFeed(data);
		assertNotNull("Failed to create billing record with valid single digit hour - Date/Time[" + data[9] + "]", billingRecord);
		
		data[9] = "16/08/2009 5:32:10 a.m."; // DateTime - AM
		billingRecord = BillingRecord.createFromBillingFeed(data);
		assertNotNull("Failed to create billing record with valid a.m. marker - Date/Time[" + data[9] + "]", billingRecord);
		
		data[9] = "26/08/2009 5:32:10 p.m."; // DateTime - PM
		billingRecord = BillingRecord.createFromBillingFeed(data);
		assertNotNull("Failed to create billing record with valid p.m. marker - Date/Time[" + data[9] + "]", billingRecord);
		
		data = createTestBillingFeedData(); // Reset data
		
		data[16] = "1"; // MP3 - 1 (true)
		billingRecord = BillingRecord.createFromBillingFeed(data);
		assertNotNull("Failed to create billing record with valid mp3 value - MP3[" + data[16] + "]", billingRecord);
		
		data = createTestBillingFeedData(); // Reset data
		
		
		
		/* ***************  Invalid data *************************/
		data[9] = "31/777/2009 12:00:00 a.m."; // DateTime - invalid month (huge)
		billingRecord = BillingRecord.createFromBillingFeed(data);
		assertNull("Created billing record with invalid month - Date/Time[" + data[9] + "]", billingRecord);
		
		data[9] = "31/13/2009 12:00:00 a.m."; // DateTime - invalid month (out of range)
		billingRecord = BillingRecord.createFromBillingFeed(data);
		assertNull("Created billing record with invalid month - Date/Time[" + data[9] + "]", billingRecord);
		
		data[9] = "777/08/2009 12:00:00 a.m."; // DateTime - invalid day (huge)
		billingRecord = BillingRecord.createFromBillingFeed(data);
		assertNull("Created billing record with invalid day of month - Date/Time[" + data[9] + "]", billingRecord);
		
		data[9] = "31/06/2009 12:00:00 a.m."; // DateTime - invalid day (out of range)
		billingRecord = BillingRecord.createFromBillingFeed(data);
		assertNull("Created billing record with invalid day of month - Date/Time[" + data[9] + "]", billingRecord);
		
		data[9] = "31/08/2009 12:00:00"; // DateTime - no am/pm marker
		billingRecord = BillingRecord.createFromBillingFeed(data);
		assertNull("Created billing record with missing am/pm marker - Date/Time[" + data[9] + "]", billingRecord);
		
		data[9] = "31/08/2009 12:00:00 s.m."; // DateTime - invalid am/pm marker
		billingRecord = BillingRecord.createFromBillingFeed(data);
		assertNull("Created billing record with invalid am/pm marker - Date/Time[" + data[9] + "]", billingRecord);
		
		data[9] = "31/082009 12:00:00 a.m."; // DateTime - missing /
		billingRecord = BillingRecord.createFromBillingFeed(data);
		assertNull("Created billing record with missing / - Date/Time[" + data[9] + "]", billingRecord);
		
		data[9] = "31/08/2009 12:0000 a.m."; // DateTime - missing :
		billingRecord = BillingRecord.createFromBillingFeed(data);
		assertNull("Created billing record with missing : - Date/Time[" + data[9] + "]", billingRecord);
		
		data = createTestBillingFeedData(); // Reset data
		
		data[16] = "2"; // MP3 - invalid value
		billingRecord = BillingRecord.createFromBillingFeed(data);
		assertNull("Created billing record with invalid mp3 value - MP3[" + data[16] + "]", billingRecord);
	}
	
	public void testCreateFromDB()
	{
		
	}
	
	public void testSave()
	{
		String[] data = createTestBillingFeedData();
		BillingRecord billingRecord = BillingRecord.createFromBillingFeed(data);
				
		boolean saved = billingRecord.save();
		
		// Remove test record
		if (!billingRecord.delete())
			log.severe("Failed to delete test data - " + billingRecord.toString() + ", please remove manually");
		
		assertTrue("Failed to save " + billingRecord.toString(), saved);
		
		
	}
	
	public void testDelete()
	{
		
	}
}
