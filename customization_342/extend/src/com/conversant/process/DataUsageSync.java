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
import org.compiere.util.Env;

import au.com.bytecode.opencsv.CSVReader;

import com.conversant.db.BillingConnector;
import com.conversant.model.BillingAccount;
import com.conversant.model.BillingRecord;
import com.conversant.model.DataUsage;

public class DataUsageSync extends SvrProcess 
{
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(DataUsageSync.class);
	
	private static String ACCOUNT_2TALK_URL = "http://account.2talk.com/api/?action=livefeed&";
	
	private static int MAX_CONNECTION_ATTEMPTS = 3;
	
	private static String ACCOUNT_PARAM = "account";
	private static String PASSWORD_PARAM = "password";
	private static String FROM_ID_PARAM = "fromid";
	
	
	private static String PROCESS_MSG_SUCCESS = "@Success@";
	private static String PROCESS_MSG_ERROR = "@Error@";
	
	private static String[] HEADERS = new String[]{"ID","Billing Group","Login Name","Description","Date","Time",
		   "Date/Time","Usage (KBs)","Cost (NZD)","Account Code","BillDate"};

	@Override
	protected String doIt() throws Exception {
         String msg = "";
		
		for (BillingAccount account : BillingConnector.getBillingAccounts())
		{
			msg += "Account[" + account.getUsername() + "] -> ";
			msg += loadDataUsageRecords(account);
			msg += "\n";
		}
		
		return msg;
	}

	@Override
	protected void prepare() {
		
	}

	public String loadDataUsageRecords(BillingAccount account)
	{
		// Tracking variables
		long start = System.currentTimeMillis();		
		int count = 0;
				
		// Get latest 2talk id for account
		Integer latestTwoTalkId = BillingConnector.getLatestDataUsageId(account.getUsername());

		if(latestTwoTalkId == null)
			latestTwoTalkId = new Integer(1);
		
		// Set up array for failed fromIds
		ArrayList<String> failedFromIds = new ArrayList<String>();
		
		// Set up array for failed to create records
		ArrayList<String> failedToCreateDataUsageRecords = new ArrayList<String>();
		
		// Set up array for failed to save records
		ArrayList<DataUsage> failedToSaveDataUsageRecords = new ArrayList<DataUsage>();
		
// ------------ Hack to allow product retrieval (when run via Scheduler (as SYS) ---------------
		
		int AD_Client_ID = Env.getAD_Client_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Client_ID", "1000000");
		
		// Loop from startFromId to endFromId or end
		boolean endFound = false;
		long fromId = latestTwoTalkId;
		
		while (!endFound)
		{
			List<String[]> dataUsageFeed = getDataUsageRecords(account, fromId);
			
			// Check not null (timed out or error)
			if (dataUsageFeed == null)
			{
				failedFromIds.add(Long.toString(fromId));
				break;
			}
			
			// Load 1st row to determine what kind of data was returned
			String[] firstRow = dataUsageFeed.get(0);
			String twoTalkId = firstRow[0];
			// If no ID then end of billing feed has been reached
			if (twoTalkId == null || twoTalkId.length() < 1)
			{
				endFound = true;
			}
			else
			{
				for (String[] row : dataUsageFeed)
				{
					if (validateRow(row))
					{
						DataUsage du = DataUsage.createFromDataUsageFeed(row);
						
						if (du != null)
						{
							if (du.save())
							{
								count++;
							}
							else
									failedToSaveDataUsageRecords.add(du);
						}
						else
							failedToCreateDataUsageRecords.add("Data Usage[" + row[0] + "," + row[2] + "," + row[3] + "]"); // Same data as BillingRecord.toString()
					}
				}
				// Load next id
				latestTwoTalkId = BillingConnector.getLatestDataUsageId(account.getUsername());
				if (latestTwoTalkId != null && latestTwoTalkId > 0)
				{
					fromId = latestTwoTalkId;
				}
				else
				{
					log.severe("Failed to get next fromId from local DB BillingConnector.getLatestDataUsageId()");
					break;
				}
			}
		}
		// Calc time it took
		long time = System.currentTimeMillis() - start;
		
		// Create msg for user
		if (failedFromIds.size() > 0 || failedToCreateDataUsageRecords.size() > 0 || failedToSaveDataUsageRecords.size() > 0)
		{
			StringBuilder msg = new StringBuilder(PROCESS_MSG_ERROR);

			if (failedFromIds.size() > 0)
			{
				msg.append("\n\n");
					
				msg.append("Failed to retrieve records using the following FromId(s): ");
			
				// Add ids
				for (String failedFromId : failedFromIds)
					msg.append(failedFromId + ", ");
				
				msg.replace(msg.lastIndexOf(","), msg.length(), ""); // replace trailing comma
				

			}
			
			if (failedToCreateDataUsageRecords.size() > 0)
			{
				msg.append("\n\n");
					
				msg.append("Failed to create the following records: ");
			
				// Add ids
				for (String failedToCreateBillingRecord : failedToCreateDataUsageRecords)
					msg.append(failedToCreateBillingRecord + ", ");
				
				msg.replace(msg.lastIndexOf(","), msg.length(), ""); // replace trailing comma
			}
			
			if (failedToSaveDataUsageRecords.size() > 0)
			{
				msg.append("\n\n");
					
				msg.append("Failed to save the following records: ");
			
				// Add ids
				for (DataUsage failedToSaveDataUsageRecord : failedToSaveDataUsageRecords)
					msg.append(failedToSaveDataUsageRecords.toString() + ", ");
				
				msg.replace(msg.lastIndexOf(","), msg.length(), ""); // replace trailing comma
			}
	
			msg.append("\n\n");			
			msg.append("Syncronized " + count + " records in " + time + "ms");
			
			return msg.toString();
		}
		else
			return "Syncronized " + count + " records in " + time + "ms";
	}
	
	public static List<String[]> getDataUsageRecords(BillingAccount account, long fromId)
	{
		HttpClient client = null;		
		GetMethod getDataUsageRecords = null;
		
		try
		{
			// Create HTTP Client
			client = new HttpClient();
			
			// Create URL with params
			String url = ACCOUNT_2TALK_URL + ACCOUNT_PARAM + "="  + account.getUsername() + "&" + 
				PASSWORD_PARAM + "=" + account.getPassword() + "&" + FROM_ID_PARAM + "=-" + fromId;
			
			// Create Get Method
			getDataUsageRecords = new GetMethod(url);
			
			// Send request
			int connectionAttemps = 0;
			while (connectionAttemps < MAX_CONNECTION_ATTEMPTS)
			{
				try
				{
					int returnCode = client.executeMethod(getDataUsageRecords);
					if (returnCode == HttpStatus.SC_OK)
					{
						String res = getDataUsageRecords.getResponseBodyAsString();	
						res = res.replaceAll("\\\\", "\\\\\\\\");
						
						// Parse CSV response list of arrays
						CSVReader reader = new CSVReader(new StringReader(res));
						List<String[]> billingFeed = reader.readAll();
						
						if (billingFeed == null)
							break;
						
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
							billingFeed.add(new String[]{"","","",""}); //  blank row to signify end
						}
											
						return billingFeed;				
					}
					else
					{
						log.severe("Failed to get 2talk Data Usage records [" + returnCode + "]");
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
			if (getDataUsageRecords != null) 
				getDataUsageRecords.releaseConnection();
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
					log.severe("Headers returned from 2talk Data Usage Feed don't match static list");
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
		else if (row.length != 11)
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
