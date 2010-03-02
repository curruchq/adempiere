package com.conversant.process;

import java.io.StringReader;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogMgt;
import org.compiere.util.CLogger;
import org.compiere.util.Ini;

import au.com.bytecode.opencsv.CSVReader;

import com.conversant.db.BillingConnector;
import com.conversant.model.BillingRecord;

public class BillingFeedSync extends SvrProcess
{
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(BillingFeedSync.class);
	
	private static String LIVE_2TALK_URL = "https://live.2talk.co.nz";
	private static String BILLING_FEED_URL = LIVE_2TALK_URL + "/billingfeed.php";
	
//	private static boolean FOLLOW_2TALK_POINTER = true;
	
//	private static long RESULT_SET_SIZE = 5000;
	
	private static int MAX_CONNECTION_ATTEMPTS = 3;
	
	private static String LOGIN_PARAM = "login";
	private static String PASSWORD_PARAM = "password";
	private static String FROM_ID_PARAM = "fromid";
	
	private static String LOGIN = "028891398";
	private static String PASSWORD = "l70kw62z";
	
	private static String PROCESS_MSG_SUCCESS = "@Success@";
	private static String PROCESS_MSG_ERROR = "@Error@";
	
	private static String[] HEADERS = new String[]{"ID","Billing Group","Origin Number","Destination Number","Description","Status","Terminated","Date","Time",
												   "Date/Time","Call Length (seconds)","Call Cost (NZD)","Smartcode","Smartcode Description","Type","SubType","MP3"};
	
	private static String FAILED_FROM_ID = "failedFromId";
	
	private static Long startFromId = null;
	private static Long endFromId = null;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare()
	{
//		ProcessInfoParameter[] para = getParameter();
//		for (int i = 0; i < para.length; i++)
//		{
//			String name = para[i].getParameterName();
//			if (para[i].getParameter() == null)
//				;
//			else if (name.equals("StartFromId"))
//			{
//				BigDecimal tmp = (BigDecimal)para[i].getParameter();
//				startFromId = tmp.longValue();				
//			}
//			else if (name.equals("EndFromId"))
//			{
//				BigDecimal tmp = (BigDecimal)para[i].getParameter();
//				endFromId = tmp.longValue();			
//			}
//			else
//				log.log(Level.SEVERE, "Unknown Parameter: " + name);
//		}
		
		// Default values
		if (startFromId == null)
			startFromId = new Long(0);
		
		if (endFromId == null)
			endFromId = new Long(0);
	}

	/**
	 * 	Process
	 *	@return message
	 *	@throws Exception
	 */
	@Override
	protected String doIt() throws Exception
	{		
		return loadBillingRecords();
	}
	
	public static String loadBillingRecords()
	{	
		// Tracking variables
		long start = System.currentTimeMillis();		
		int count = 0;
		
		// Set startId at the 10,000th down from latest 2talk record Id
		Long latestTwoTalkId = BillingConnector.getLatestTwoTalkId();
		if (latestTwoTalkId != null && latestTwoTalkId > 0)
			startFromId = latestTwoTalkId;
//			startFromId = roundDown(latestTwoTalkId);	

		// Start from 0 if now 2talk record Ids found
		if (startFromId == null)
			startFromId = new Long(0);
		
	/*
		// Validate start and end ids
		if (startFromId < 1)
		{
			BillingRecord billingRecord = BillingConnector.getLatestBillingRecord();
			if (billingRecord != null && billingRecord.getTwoTalkId() > 0)
				startFromId = billingRecord.getTwoTalkId();
			else
				startFromId = 0;
		}
		else if (startFromId == 1) // TODO: Need to fix how to init from start
			startFromId = 0;
		
		if (endFromId > 0 && endFromId < startFromId)
		{
			log.warning("End ID is less than start ID - StartFromId=" + startFromId + " & EndFromId=" + endFromId);
			return PROCESS_MSG_ERROR + "End ID is less than start ID - StartFromId=" + startFromId + " & EndFromId=" + endFromId;
		}		
	*/
	
		// Get existing Ids
//		ArrayList<Long> existingBillingRecordIds = BillingConnector.getTwoTalkIds(startFromId);
//		if (existingBillingRecordIds == null)
//			return PROCESS_MSG_ERROR + "Failed to load existing record(s), check DB connection & settings";
	
		// Set up array for failed fromIds
		ArrayList<String> failedFromIds = new ArrayList<String>();
		
		// Set up array for failed to create records
		ArrayList<String> failedToCreateBillingRecords = new ArrayList<String>();
		
		// Set up array for failed to save records
		ArrayList<BillingRecord> failedToSaveBillingRecords = new ArrayList<BillingRecord>();
		
		// Loop from startFromId to endFromId or end
		boolean endFound = false;
		long fromId = startFromId;
		while (!endFound)
		{
			List<String[]> billingFeed = getBillingFeed(fromId);
							
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
			
			// If no ID then end of billing feed has been reached
			if (twoTalkId == null || twoTalkId.length() < 1)
			{
				endFound = true;
			}
			// Check if 2talk returned a pointer
			else if ((originNumber == null || originNumber.length() < 1) && 
				(destinationNumber == null || destinationNumber.length() < 1))
			{
				log.severe("2talk returned a \"pointer\" row, API has changed");
//				if (FOLLOW_2TALK_POINTER)
//				{
//					try
//					{
//						// Round down to closest 10,000th
//						long tmpId = Long.parseLong(twoTalkId);
//						long tmpFromId = roundDown(tmpId);	
//						
//						// Incase they keep returning same pointer Id
//						if (tmpFromId > fromId)
//							fromId = tmpFromId;							
//						else 
//							fromId += RESULT_SET_SIZE;
//					}
//					catch (NumberFormatException ex)
//					{
//						log.severe("Failed to parse 2talk pointer ID, incrementing by " + RESULT_SET_SIZE + " instead.");
//						fromId += RESULT_SET_SIZE;
//					}
//				}
//				else
//					fromId += RESULT_SET_SIZE;
			}
			// Else its call data
			else
			{
				for (String[] row : billingFeed)
				{
					if (validateRow(row))
					{
						BillingRecord br = BillingRecord.createFromBillingFeed(row);
						if (br != null)
						{
//							if (!checkIdExists(existingBillingRecordIds, br.getTwoTalkId()))
//							{
								if (!br.save())
									failedToSaveBillingRecords.add(br);
								else
									count++;								
//							}
						}
						else
							failedToCreateBillingRecords.add("BillingRecord[" + row[0] + "," + row[2] + "," + row[3] + "]"); // Same data as BillingRecord.toString()
					}
				}
				
				// Load next fromId
				latestTwoTalkId = BillingConnector.getLatestTwoTalkId();
				if (latestTwoTalkId != null && latestTwoTalkId > 0)
					fromId = latestTwoTalkId;
				else
				{
					log.severe("Failed to get next fromId from local DB BillingConnector.getLatestTwoTalkId()");
					break;
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
			return PROCESS_MSG_SUCCESS + "\n\n" + "Syncronized " + count + " records in " + time + "ms";
	}
	
	public static List<String[]> getBillingFeed(long fromId)
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
		else if (row.length != 17)
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
	
	public static long roundDown(long num)
	{
		Double doubleId = Math.floor(num * 0.0001) * 10000;
		return doubleId.longValue();	
	}
	
	public static boolean checkIdExists(ArrayList<Long> existingIds, Long newId)
	{
		for (Long existingId : existingIds)
		{
			if (newId.compareTo(existingId) == 0)
				return true;
		}
		
		return false;
	}
	
	public static List<String[]> createFailedFromIdList(long fromId)
	{
		List<String[]> failedFromIdList = new ArrayList<String[]>();
		failedFromIdList.add(new String[]{FAILED_FROM_ID});
		failedFromIdList.add(new String[]{Long.toString(fromId)});
		return failedFromIdList;
	}
	
	public static String getFailedFromId(List<String[]> billingFeed)
	{
		String[] firstRow = billingFeed.get(0);
		if (firstRow != null && firstRow.length > 0 && firstRow[0].equals(FAILED_FROM_ID))
		{
			String[] secondRow = billingFeed.get(1);
			if (secondRow != null && secondRow.length > 0)
				return secondRow[0];
		}
		return null;
	}

	public static void main(String[] args)
	{
		BillingFeedSync bfs = new BillingFeedSync();
		try
		{
			System.out.println(bfs.doIt());
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
	}
	
	public static void startADempiere()
	{
		System.setProperty("PropertyFile", "E:\\workspace\\customization_342\\test.properties");
		Ini.setClient(false);
		org.compiere.Adempiere.startup(false);
		CLogMgt.setLevel("FINEST");
	}
//		String sql = "update billing.billingrecord set id = ? where id = ?";
//		
//		for (int i=435604; i>340647; i--)
//		{
//			Connection conn = BillingConnector.getConnection("localhost", 3306, "billing", "erp_local", "naFJ487CB(Xp");
//			PreparedStatement ps = null;
//
//			try
//	        {				
//				ps = conn.prepareStatement(sql.toString());
//				ps.setInt(1, i + 1);
//				ps.setInt(2, i);
//				
//	    		// Execute
//	    		int no = ps.executeUpdate();
//	    		if (no != 1)
//	    		{
//	    			System.out.println("Failed at " + i);
//	    			break;
//	    		}
//	        }
//	        catch (SQLException ex)
//	        {
//	        	log.log(Level.SEVERE, "Update failed, SQL='" + sql.toString() + "'", ex);
//	        }
//	        finally
//	        {
//	        	try
//	        	{
//	        		if (ps != null) ps.close();
//	        		if (conn != null) conn.close();
//	        	}
//	        	catch (SQLException ex)
//	        	{
//	        		log.log(Level.WARNING, "Couldn't close either PreparedStatement or Connection", ex);
//	        	}
//	        }
//		}

}
