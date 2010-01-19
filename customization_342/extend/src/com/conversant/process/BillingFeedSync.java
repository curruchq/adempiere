package com.conversant.process;

import java.io.StringReader;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;

import au.com.bytecode.opencsv.CSVReader;

import com.conversant.db.BillingConnector;
import com.conversant.model.BillingRecord;

public class BillingFeedSync extends SvrProcess
{
	private static String LIVE_2TALK_URL = "https://live.2talk.co.nz";
	private static String BILLING_FEED_URL = LIVE_2TALK_URL + "/billingfeed.php";
	
	private static boolean FOLLOW_2TALK_POINTER = true;
	
	private static long RESULT_SET_SIZE = 10000;
	
	private static int MAX_CONNECTION_ATTEMPTS = 3;
	
	private static String LOGIN_PARAM = "login";
	private static String PASSWORD_PARAM = "password";
	private static String FROM_ID_PARAM = "fromid";
	
	private static String LOGIN = "028891398";
	private static String PASSWORD = "l70kw62z";
	
	private static String[] HEADERS = new String[]{"ID","Billing Group","Origin Number","Destination Number","Description","Status","Terminated","Date","Time",
												   "Date/Time","Call Length (seconds)","Call Cost (NZD)","Smartcode","Smartcode Description","Type","SubType","MP3"};
	
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(BillingFeedSync.class);
	
	public static void main(String[] args)
	{
		getBillingRecords(0, 14000000);
	}
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare()
	{

	}

	/**
	 * 	Process
	 *	@return message
	 *	@throws Exception
	 */
	@Override
	protected String doIt() throws Exception
	{
		getBillingRecords(0, 0);
		return "@Success@";
	}
	
	public static void getBillingRecords(long startFromId, long endFromId)
	{	
		long start = System.currentTimeMillis();		
		int count = 0;
		
		// Validate start and end ids
		if (startFromId < 0)
			startFromId = 0;
		
		if (endFromId > 0 && endFromId < startFromId)
		{
			log.warning("End ID is less than start ID - StartFromId=" + startFromId + " & EndFromId=" + endFromId);
			return;
		}
		
		// Load existing BillingRecords Ids
		ArrayList<Long> existingBillingRecordIds = new ArrayList<Long>();
		for (BillingRecord br : BillingConnector.getBillingRecords())
			existingBillingRecordIds.add(br.getTwoTalkId());
		
		boolean endFound = false;
		long fromId = startFromId;
		
		while (!endFound)
		{
			List<String[]> billingFeed = loadBillingFeed(fromId);

			// Check not null (timed out or error)
			if (billingFeed == null)
			{
				log.info("Could not load Billing Feed, check logs. FromID[" + fromId + "]"); // Need this message? Already logged from loadBillingFeed()
				break;
				// TODO: Void all data and tell user to try again later? Not if it's the end
			}

			// Load 1st row to determine what kind of data was returned
			String[] firstRow = billingFeed.get(0);

			String twoTalkId = firstRow[0];
			String originNumber = firstRow[2];
			String destinationNumber = firstRow[3];
			
			// If no ID then end of billing feed has been reached
			if (twoTalkId == null || twoTalkId.length() < 1)
			{
				endFound = true;
			}
			// Check if 2talk returned a pointer
			else if ((originNumber == null || originNumber.length() < 1) && 
				(destinationNumber == null || destinationNumber.length() < 1))
			{
				if (FOLLOW_2TALK_POINTER)
				{
					try
					{
						// Round down to closest 10,000th
						long tmpId = Long.parseLong(twoTalkId);
						Double doubleId = Math.floor(tmpId * 0.0001) * 10000;
						fromId = doubleId.longValue();						
					}
					catch (NumberFormatException ex)
					{
						log.severe("Failed to parse 2talk pointer ID, incrementing by " + RESULT_SET_SIZE + " instead.");
						fromId += RESULT_SET_SIZE;
					}
				}
				else
					fromId += RESULT_SET_SIZE;
			}
			// Else its call data
			else
			{
				System.out.print("FromId[" + fromId + "] --> ");
				for (String[] row : billingFeed)
				{
					if (validateRow(row))
					{
						BillingRecord br = BillingRecord.createFromBillingFeed(row);
						if (br != null)
						{
							if (existingBillingRecordIds.contains(br.getTwoTalkId()))
								log.info("Skipping BillingRecord[" + br.getTwoTalkId() + "], already in DB");
							else if (!br.save())
								System.out.print("Save Failed - " + br.toString());
							count++;
						}
						else
							System.out.print("Create Failed - BillingRecord[" + row[0] + "," + row[2] + "," + row[3] + "] // "); // Same data as BillingRecord.toString()
					}
				}
				System.out.println();
				
				fromId += RESULT_SET_SIZE;
			}
			
			// Check if end ID has been reached
			if (endFromId > 0 && fromId > endFromId)
				endFound = true;
		}
		
		// 
		long time = System.currentTimeMillis() - start;
		long range = (endFromId + RESULT_SET_SIZE) - startFromId;
		long timePerId = time / range;
		System.out.println("Time: " + time + "ms, StartId:" + startFromId + ", EndId:" + endFromId + ", Range:" + range + ", Time/Id:" + timePerId + ", LastFromId=" + fromId);
		System.out.println("Count: " + count);
	}
	
	public static List<String[]> loadBillingFeed(long fromId)
	{
		HttpClient client = null;		
		GetMethod getBillingFeed = null;
		
		try
		{
			// Create HTTP Client
			client = new HttpClient();
			
			// Create URL with params
			String url = BILLING_FEED_URL + "?" + LOGIN_PARAM + "="  + LOGIN + "&" + 
				PASSWORD_PARAM + "=" + PASSWORD + "&" + FROM_ID_PARAM + "=" + fromId;
			
			// Create Get Method
			getBillingFeed = new GetMethod(url);
			
			// Send request
			int connectionAttemps = 0;
			while (connectionAttemps < MAX_CONNECTION_ATTEMPTS)
			{
				try
				{
					int returnCode = client.executeMethod(getBillingFeed);
					if (returnCode == HttpStatus.SC_OK)
					{
						String res = getBillingFeed.getResponseBodyAsString();
	
						// Parse CSV response list of arrays
						CSVReader reader = new CSVReader(new StringReader(res));
						List<String[]> billingFeed = reader.readAll();
						
						// Validate headers
						String[] headers = billingFeed.remove(0);
						if (!validateHeaders(headers))
						{
							log.severe("Header validation failed");
							break;
						}
						// Validate a row exists
						else if (billingFeed.size() < 1)
						{
							log.info("No rows were returned - End reached or error");
							break;
						}
						else						
							return billingFeed;				
					}
					else
					{
						log.severe("Failed to get 2talk Billing Feed [" + returnCode + "]");
						break;
					}
				}
				catch (ConnectException ex)
				{
					connectionAttemps++;
					if (connectionAttemps == MAX_CONNECTION_ATTEMPTS)
						log.severe("Reached maximum connection attempts of " + MAX_CONNECTION_ATTEMPTS + " - " + ex);
					else
						log.info("ConnectionException raised, retrying [Attempts=" + connectionAttemps + ", FromId=" + fromId + "]");
				}
			}
		}
		catch (Exception ex) 
		{
			log.severe(ex.toString());
		} 
		finally 
		{
			if (getBillingFeed != null) 
				getBillingFeed.releaseConnection();
		}
		
		return null;
	}
	
	public static boolean validateHeaders(String[] headers)
	{
		if (headers == null)
		{
			log.severe("Headers are NULL");
		}
		
		else if (headers.length != HEADERS.length)
		{
			log.severe("Number of headers does not match static list");
		}
		else
		{
			for (int i=0; i<HEADERS.length; i++)
			{
				if (!HEADERS[i].equalsIgnoreCase(headers[i]))
				{
					log.severe("Headers returned from 2talk Billing Feed don't match static list");
					return false;
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	public static boolean validateRow(String[] row)
	{
		if (row == null)
		{
			log.info("Invalid row - NULL");
			return false;
		}		
		else if (row.length < 4)
		{
			log.info("Invalid row - Length=" + row.length);
			return false;
		}
		else if ((row[0] == null || row[0].length() < 1)	||		// 2talk ID
			(row[2] == null || row[2].length() < 1)	|| 		// Origin Number
			(row[3] == null || row[3].length() < 1))		// Destination Number
		{
			log.info("Invalid row[" + row[0] + "," + row[2] + "," + row[3] + "]");
			return false;
		}
		else			
			return true;
	}
}
