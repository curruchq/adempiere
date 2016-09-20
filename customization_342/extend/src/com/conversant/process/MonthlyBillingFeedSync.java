package com.conversant.process;

import java.io.StringReader;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogMgt;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Ini;

import au.com.bytecode.opencsv.CSVReader;

import com.conversant.db.BillingConnector;
import com.conversant.db.RadiusConnector;
import com.conversant.did.DIDUtil;
import com.conversant.model.BillingAccount;
import com.conversant.model.BillingRecord;

public class MonthlyBillingFeedSync extends SvrProcess 
{
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(MonthlyBillingFeedSync.class);
	private static String BILLING_FEED_URL = "http://account.2talk.com/API/";
	
	
	private static int MAX_CONNECTION_ATTEMPTS = 3;
	
	private static String ACCOUNT_PARAM = "account";
	private static String PASSWORD_PARAM = "password";
	private static String ACTION_PARAM = "action";
	
//	private static String LOGIN = "028891398";
//	private static String PASSWORD = "l70kw62z";
	
	private static String PROCESS_MSG_SUCCESS = "@Success@";
	private static String PROCESS_MSG_ERROR = "@Error@";
	
	private static String[] HEADERS = new String[]{"ID","Billing Group","Origin Number","Destination Number","Description","Status","Terminated","Date","Time",
												   "Date/Time","Call Length (seconds)","Call Cost (NZD)","Smartcode","Smartcode Description","Type","SubType","MP3"};
	
	private static String[] HEADER_MONTHLY = new String[]{"ID","Account_Code","Origin","Destination","Date","Time","Seconds","Charge","BillDate","Billing_Group",
		                                              "zone_prefix","azone_prefix","MainType","SubType" };
	
	private static String FAILED_FROM_ID = "failedFromId";
	
	@Override
	protected String doIt() throws Exception {
		String msg = "";
		
		for (BillingAccount account : BillingConnector.getMonthlyBillingAccounts())
		{
			if(account.getFeedtype() == 10)
			{
				//continue ;
				msg += "Account[" + account.getUsername() + "] -> ";
				msg += loadMonthlyBillingRecords(account);
				msg += "\n";
			}
		}
		
		return msg;
	}

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		
	}
	
	public String loadMonthlyBillingRecords(BillingAccount account)
	{	
		// Tracking variables
		long start = System.currentTimeMillis();		
		int count = 0;
		int feedtype = account.getFeedtype();		
		// Get latest 2talk id for account
		Long latestTwoTalkId = BillingConnector.getLatestTwoTalkId(account.getBillingAccountId());

		// Start from 0 if now 2talk record Ids found
		if (latestTwoTalkId == null)
			latestTwoTalkId = new Long(0);
	
		// Set up array for failed fromIds
		ArrayList<String> failedFromIds = new ArrayList<String>();
		
		// Set up array for failed to create records
		ArrayList<String> failedToCreateBillingRecords = new ArrayList<String>();
		
		// Set up array for failed to save records
		ArrayList<BillingRecord> failedToSaveBillingRecords = new ArrayList<BillingRecord>();
		
// ------------ Hack to allow product retrieval (when run via Scheduler (as SYS) ---------------
		
		int AD_Client_ID = Env.getAD_Client_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Client_ID", "1000000");

		// Get subscribed fax numbers to update billing data
		//ArrayList<String> subscribedFaxNumbers = DIDUtil.getSubscribedFaxNumbers(getCtx(), null);
		HashMap<String,String> subFaxNumbers=DIDUtil.getSubscribedFaxNumbersAndCallType(getCtx(), null);
		Env.setContext(getCtx(), "#AD_Client_ID", AD_Client_ID);
		
// ---------------------------------------------------------------------------------------------
		
		// Loop from startFromId to endFromId or end
		boolean endFound = false;
		long fromId = latestTwoTalkId;
		while (!endFound)
		{
			List<String[]> billingFeed = getBillingFeed(account, fromId);
							
			// Check not null (timed out or error)
			if (billingFeed == null)
			{
				failedFromIds.add(Long.toString(fromId));
				break;
			}

			// Load 1st row to determine what kind of data was returned
			String[] firstRow = billingFeed.get(0);

			String twoTalkId = firstRow[0];
			String originNumber = firstRow[2];
			String destinationNumber = firstRow[3];
			boolean pointerRow = ((originNumber == null || originNumber.length() < 1) && 
					(destinationNumber == null || destinationNumber.length() < 1));
			// If no ID then end of billing feed has been reached
			if (twoTalkId == null || twoTalkId.length() < 1)
			{
				endFound = true;
			}
			// Check if 2talk returned a pointer
			// Else its call data
			else
			{
				for (String[] row : billingFeed)
				{
					if (validateRow(row,feedtype))
					{
						BillingRecord br = BillingRecord.createFromBillingFeed(row);
						if (br != null)
						{
							// Add account id
							br.setBillingAccountId(account.getBillingAccountId());

							if (br.save())
							{
								if(!pointerRow)
								{										
									count++;
								}
							}
							else
								failedToSaveBillingRecords.add(br);
						}
						else
							failedToCreateBillingRecords.add("BillingRecord[" + row[0] + "," + row[2] + "," + row[3] + "]"); // Same data as BillingRecord.toString()
					}
					else
						endFound = true;
				}
				
			}
		}
		
		// Calc time it took
		long time = System.currentTimeMillis() - start;
		
		// Create msg for user
		if (failedFromIds.size() > 0 || failedToCreateBillingRecords.size() > 0 || failedToSaveBillingRecords.size() > 0)
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
			
			if (failedToCreateBillingRecords.size() > 0)
			{
				msg.append("\n\n");
					
				msg.append("Failed to create the following records: ");
			
				// Add ids
				for (String failedToCreateBillingRecord : failedToCreateBillingRecords)
					msg.append(failedToCreateBillingRecord + ", ");
				
				msg.replace(msg.lastIndexOf(","), msg.length(), ""); // replace trailing comma
			}
			
			if (failedToSaveBillingRecords.size() > 0)
			{
				msg.append("\n\n");
					
				msg.append("Failed to save the following records: ");
			
				// Add ids
				for (BillingRecord failedToSaveBillingRecord : failedToSaveBillingRecords)
					msg.append(failedToSaveBillingRecord.toString() + ", ");
				
				msg.replace(msg.lastIndexOf(","), msg.length(), ""); // replace trailing comma
			}
	
			msg.append("\n\n");			
			msg.append("Syncronized " + count + " records in " + time + "ms");
			
			return msg.toString();
		}
		else
			return "Syncronized " + count + " records in " + time + "ms";
	}
	
	public static boolean validateRow(String[] row,int feedtype)
	{
		if (row == null)
		{
			log.info("Invalid row - NULL");
			return false;
		}		
		
	    if (feedtype == 10)
		{
			if(row.length != 14)
			{
				log.info("Invalid row - Length=" + row.length);
				return false;
			}
			else if	((row[0] == null || row[0].length() < 1)	||		// 2talk ID
				(row[2] == null || row[2].length() < 1)	|| 		// Origin Number
				(row[3] == null || row[3].length() < 1))		// Destination Number
			{
				log.info("Invalid row[" + row[0] + "," + row[2] + "," + row[3] + "]");
				return false;
			}
		}
			return true;
	}

	
	public static boolean validateHeaders(String[] headers,int feedtype)
	{
		if (headers == null)
		{
			log.severe("Headers are NULL");
		}
		
		else if (feedtype == 10 && headers.length != HEADER_MONTHLY.length)
		{
			log.severe("Number of headers does not match static list");
		}
		else
		{
			if(feedtype == 10)
			for (int i=0; i<HEADER_MONTHLY.length; i++)
			{
				if (!HEADER_MONTHLY[i].equalsIgnoreCase(headers[i]))
				{
					log.severe("Headers returned from 2talk Billing Feed don't match static list");
					return false;
				}
			}
			return true;
		}
		
		return false;
	}
	
	public static List<String[]> getBillingFeed(BillingAccount account, long fromId)
	{
		HttpClient client = null;		
		GetMethod getBillingFeed = null;
		String url=null;
		try
		{
			// Create HTTP Client
			client = new HttpClient();
			int feedtype = account.getFeedtype();
			// Create URL with params 
			if(feedtype == 10)
				url = BILLING_FEED_URL + "?" + ACTION_PARAM + "=MiscOnLastBill"  + "&" + 
				ACCOUNT_PARAM + "=" + account.getUsername() + "&" + PASSWORD_PARAM + "=" + account.getPassword();
				
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
						res = res.replaceAll("\\\\", "\\\\\\\\");
						
						// Parse CSV response list of arrays
						CSVReader reader = new CSVReader(new StringReader(res));
						List<String[]> billingFeed = reader.readAll();
						
						if (billingFeed == null)
							break;
						
						if (billingFeed.isEmpty() && feedtype == 10)
						{
							billingFeed.add(HEADER_MONTHLY); //  blank row to signify end
						}
						
						// Validate headers
						String[] headers = billingFeed.remove(0);
						if (!validateHeaders(headers,feedtype))
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
						billingFeed.add(new String[]{null,"","",""}); 					
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
}
