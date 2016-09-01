package com.conversant.process;

import java.io.StringReader;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.*;

import org.compiere.process.SvrProcess;
import org.compiere.util.CLogMgt;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Ini;

import au.com.bytecode.opencsv.CSVReader;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.conversant.db.BillingConnector;
import com.conversant.db.RadiusConnector;
import com.conversant.did.DIDUtil;
import com.conversant.model.BillingAccount;
import com.conversant.model.BillingRecord;

public class BillingFeedSync extends SvrProcess
{
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(BillingFeedSync.class);
	
	private static String LIVE_2TALK_URL = "https://live.2talk.co.nz";
	private static String BILLING_FEED_URL = LIVE_2TALK_URL + "/billingfeed.php";
	private static String LIVE_2TALK_URL_AU = "https://now.2talk.com.au";
	private static String BILLING_FEED_URL_AU = LIVE_2TALK_URL_AU + "/customer/feed";
	
	private static String LIVE_2TALK_LOGIN_URL_VIBE = "https://wholesale.layer2.co.nz/api/login";
	private static String BILLING_FEED_URL_VIBE = "https://wholesale.layer2.co.nz/api/cdr/recordsince";
	
//	private static boolean FOLLOW_2TALK_POINTER = true;
	
//	private static long RESULT_SET_SIZE = 5000;
	
	private static int MAX_CONNECTION_ATTEMPTS = 3;
	
	private static String LOGIN_PARAM = "login";
	private static String PASSWORD_PARAM = "password";
	private static String FROM_ID_PARAM = "fromid";
	
//	private static String LOGIN = "028891398";
//	private static String PASSWORD = "l70kw62z";
	
	private static String PROCESS_MSG_SUCCESS = "@Success@";
	private static String PROCESS_MSG_ERROR = "@Error@";
	
	private static String[] HEADERS = new String[]{"ID","Billing Group","Origin Number","Destination Number","Description","Status","Terminated","Date","Time",
												   "Date/Time","Call Length (seconds)","Call Cost (NZD)","Smartcode","Smartcode Description","Type","SubType","MP3"};
	
	private static String[] HEADER_AU = new String[]{"ID","BillingGroup","Origin","Destination","ZonePrefix","AZonePrefix","ShortCauseDesc","ThirdNumber","Date","Time",
		                                              "DateTime","NetworkSeconds","Charge","SmartCode","SmartCodeDescription","Type","SubType","MP3" };
	
	private static String FAILED_FROM_ID = "failedFromId";
	
	private static String fromDate;
	private static String toDate;
	/**	Date/Time pattern			*/
	
	private static final String DATE_TIME_PATTERN = "^([0-9]{4})-([0-9]{1,2})-([0-9]{1,2})\\s+([0-9]{1,2}):([0-9]{1,2}):([0-9]{1,2})\\s+(.{2,4})";
	
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
        
		String msg = "";
		
		for (BillingAccount account : BillingConnector.getBillingAccounts())
		{
			if(account.getFeedtype() != 3)
			{
				msg += "Account[" + account.getUsername() + "] -> ";
				msg += loadBillingRecords(account);
				msg += "\n";
			}
			else
			{
				msg += "Account[" + account.getUsername() + "] -> ";
				msg += loadBillingRecordsVibe(account);
				msg += "\n";
			}
		}
		
		return msg;
	}
	
	public String loadBillingRecords(BillingAccount account)
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
			/*else if ((originNumber == null || originNumber.length() < 1) && 
				(destinationNumber == null || destinationNumber.length() < 1))
			{
				log.severe("2talk returned a \"pointer\" row, API has changed");
			}*/
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
									boolean inbound = BillingRecord.TYPE_INBOUND.equals(br.getType()) || br.getType().equals("IS") || br.getType().equals("IM") || br.getType().equals("Inbound");
	
									for (String subscribedFaxNumber : subFaxNumbers.keySet())
									{	
										String callType=subFaxNumbers.get(subscribedFaxNumber);
										if(callType.equals("in") && inbound && subscribedFaxNumber.equals(br.getDestinationNumber()))
										{
											log.info("Adding new record in Radius Account--Destination Number:"+br.getDestinationNumber()+" Type :" +br.getType()+ " 2 Talk ID :"+br.getTwoTalkId() );
											if(feedtype == 1)
												RadiusConnector.addRadiusAccount(br);
											else if (feedtype == 2)
												RadiusConnector.addRadiusAccountAU(br);
										}
										if(callType.equals("out") && !inbound && subscribedFaxNumber.equals(br.getOriginNumber()))
										{
											log.info("Adding new record in Radius Account--Destination Number:"+br.getDestinationNumber()+" Type :" +br.getType()+ " 2 Talk ID :"+br.getTwoTalkId() );
											if(feedtype == 1)
												RadiusConnector.addRadiusAccount(br);
											else if (feedtype == 2)
												RadiusConnector.addRadiusAccountAU(br);
										}
										if(callType.equals("true") && (subscribedFaxNumber.equals(br.getDestinationNumber()) || subscribedFaxNumber.equals(br.getOriginNumber())))
										{
											log.info("Adding new record in Radius Account--Destination Number:"+br.getDestinationNumber()+" Type :" +br.getType()+ " 2 Talk ID :"+br.getTwoTalkId() );
											if(feedtype == 1)
												RadiusConnector.addRadiusAccount(br);
											else if (feedtype == 2)
												RadiusConnector.addRadiusAccountAU(br);
										}
										/*if ((inbound && subscribedFaxNumber.equals(br.getDestinationNumber())) || 
											(!inbound && subscribedFaxNumber.equals(br.getOriginNumber())))
										{
											log.info("Adding new record in Radius Account--Destination Number:"+br.getDestinationNumber()+" Type :" +br.getType()+ " 2 Talk ID :"+br.getTwoTalkId() );
											RadiusConnector.addRadiusAccount(br);
										}*/
									}
																		
									count++;
								}
							}
							else
								failedToSaveBillingRecords.add(br);
						}
						else
							failedToCreateBillingRecords.add("BillingRecord[" + row[0] + "," + row[2] + "," + row[3] + "]"); // Same data as BillingRecord.toString()
					}
				}
				
				// Load next id
				latestTwoTalkId = BillingConnector.getLatestTwoTalkId(account.getBillingAccountId());
				if (latestTwoTalkId != null && latestTwoTalkId > 0)
				{
					fromId = latestTwoTalkId;
				}
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
			return "Syncronized " + count + " records in " + time + "ms";
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
			if(feedtype == 1)
				url = BILLING_FEED_URL + "?" + LOGIN_PARAM + "="  + account.getLogin() + "&" + 
				PASSWORD_PARAM + "=" + account.getPassword() + "&" + FROM_ID_PARAM + "=" + fromId;
			else if (feedtype == 2)
				url = BILLING_FEED_URL_AU + "?" + LOGIN_PARAM + "="  + account.getLogin() + "&" + 
				PASSWORD_PARAM + "=" + account.getPassword() + "&" + FROM_ID_PARAM + "=" + fromId;
				
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
						
						if (billingFeed.isEmpty() && feedtype == 2)
						{
							billingFeed.add(HEADER_AU); //  blank row to signify end
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
	
	public static List<String[]> getBillingFeedVibe(BillingAccount account, long fromId)
	{
		HttpClient client = null;		
		PostMethod getBillingFeedToken = null;
		GetMethod getBillingFeedData = null;
		String url=null;
		JsonObject json = new JsonObject();
		json.addProperty("username", account.getLogin()); 
		json.addProperty("password", account.getPassword());
		// *** note that it's "yyyy-MM-dd hh:mm:ss" not "yyyy-mm-dd hh:mm:ss"  
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        // *** same for the format String below
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
         Date from = new Date(account.getLastAccessDate().getTime()); 
         fromDate = dt.format(from);
                
		
		try
		{
			// Create HTTP Client
			client = new HttpClient();
			int feedtype = account.getFeedtype();
			// Create URL with params 
			
			    url = LIVE_2TALK_LOGIN_URL_VIBE;
				
			// Create Post Method
			getBillingFeedToken = new PostMethod(url);
			getBillingFeedToken.addRequestHeader("Content-Type", "application/json");
			getBillingFeedToken.setRequestBody(json.toString());
	
	
			// Send request
			int connectionAttemps = 0;
			while (connectionAttemps < 1)
			{
				try
				{
					int returnCode = client.executeMethod(getBillingFeedToken);
						
					if (returnCode == HttpStatus.SC_OK)
					{
						String res = getBillingFeedToken.getResponseBodyAsString();	
						//res = res.replaceAll("\\\\", "\\\\\\\\");
						Gson gson = new Gson();
						JsonElement element = gson.fromJson (res, JsonElement.class);
						JsonObject jsonObj = element.getAsJsonObject();
						jsonObj = jsonObj.getAsJsonObject("data");
						String token = jsonObj.get("token").toString().replace("\"", "");
						
						url = BILLING_FEED_URL_VIBE + "?token=" + token + "&limit="+1000+"&changedsince="+fromDate ;
						getBillingFeedData = new GetMethod(url);
						returnCode = client.executeMethod(getBillingFeedData);
						if(returnCode == HttpStatus.SC_OK)
						{
							res = getBillingFeedData.getResponseBodyAsString();	
						}
						element = gson.fromJson (res, JsonElement.class);
						jsonObj = element.getAsJsonObject();
						JsonArray arr = jsonObj.getAsJsonArray("data");
						
						List<String[]> billingFeed =new ArrayList<String[]>();;
						
						for(int i = 0; i < arr.size(); i++)
						{
							JsonElement element1 =  arr.get(i);
							JsonObject temp = element1.getAsJsonObject();
							String twoTalkId = temp.get("call_id").toString();
							String billingGroup = temp.get("account").toString().replace("\"", "");
							String originNumber = temp.get("caller").toString().replace("\"", "");
							String destinationNumber = temp.get("callee").toString().replace("\"", "");
							String description = temp.get("person").toString().replace("\"", "");
							String status = "OK";
						    String terminated = "";
						    String date =  temp.get("calldate").toString().replace("\"", "").substring(0,11)+" 12:00:00 am";
						    String date2 = fixDate(date);
						    String time = "1900-01-01 "+temp.get("calldate").toString().replace("\"", "").substring(11)+" am";
                            String time2 = fixDate(time);
						    String dateTime = temp.get("calldate").toString().replace("\"", "")+" am";
						    String dateTime2 = fixDate(dateTime);
						    String callLength = temp.get("duration").toString().replace("\"", "");
						    String callCost =   temp.get("price").toString();
						    String smartCode = "";
							String smartCodeDescription = "";
							String type = temp.get("type").toString().replace("\"", "");
							String subType = temp.get("type").toString().replace("\"", "");
							String mp3 = "";
							
							billingFeed.add(new String[]{twoTalkId , billingGroup , originNumber ,destinationNumber , description , status , terminated , date2 , time2 ,dateTime2,callLength ,callCost ,smartCode ,smartCodeDescription,type, subType ,mp3});
						    	
						}
						billingFeed.add(new String[]{null,"","",""}); //  blank row to signify end							
							if (billingFeed == null)
								break;
							
							// Validate a row exists
							else if (billingFeed.size() < 1)
							{
								log.info("No rows were returned - End reached or error");
								billingFeed.add(new String[]{"","","",""}); //  blank row to signify end
							}
							connectionAttemps++;		
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
					if (connectionAttemps == 1)
						log.severe("Reached maximum connection attempts of " + MAX_CONNECTION_ATTEMPTS + " - " + ex);
					else
						log.info("ConnectionException raised, retrying [Attempts=" + connectionAttemps + ", From Date=" + fromDate + "]");
				}
			}
		}
		catch (Exception ex) 
		{
			log.severe(ex.toString());
		} 
		finally 
		{
			if (getBillingFeedData != null) 
				getBillingFeedData.releaseConnection();
		}
		
		return null;
	}
	
	public static boolean validateHeaders(String[] headers,int feedtype)
	{
		if (headers == null)
		{
			log.severe("Headers are NULL");
		}
		
		else if (feedtype == 1 && headers.length != HEADERS.length)
		{
			log.severe("Number of headers does not match static list");
		}
		else if (feedtype == 2 && headers.length != HEADER_AU.length)
		{
			log.severe("Number of headers does not match static list");
		}
		else
		{
			if(feedtype == 1)
			for (int i=0; i<HEADERS.length; i++)
			{
				if (!HEADERS[i].equalsIgnoreCase(headers[i]))
				{
					log.severe("Headers returned from 2talk Billing Feed don't match static list");
					return false;
				}
			}
			else if (feedtype == 2)
				for (int i=0; i<HEADER_AU.length; i++)
				{
					if (!HEADER_AU[i].equalsIgnoreCase(headers[i]))
					{
						log.severe("Headers returned from 2talk Billing Feed don't match static list");
						return false;
					}
				}
			
			return true;
		}
		
		return false;
	}
	
	public static boolean validateRow(String[] row,int feedtype)
	{
		if (row == null)
		{
			log.info("Invalid row - NULL");
			return false;
		}		
		
	    if (feedtype == 1)
		{
			if(row.length != 17)
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
		else if (feedtype == 2)
		{
			if(row.length != 18)
			{
				log.info("Invalid row - Length=" + row.length);
				return false;
			}
			else if((row[0] == null || row[0].length() < 1) //2talk ID 
					|| (row[2] == null || row[2].length() < 1)	&& (row[3] == null || row[3].length() < 1)) //Both Origin and Destination Numbers are null
			{
				log.info("Invalid row[" + row[0] + "," + row[2] + "," + row[3] + "]");
				return false;
			}
		}
		else if (feedtype == 3)
		{
			if (row[0] == null)
				return false;
		}
	    
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
		startADempiere();
		
		ArrayList<BillingRecord> records = BillingConnector.getBillingRecordsForNumber("6492977774", "2011-02-01 14:24:39", "2011-03-10 00:00:00");		
		System.out.println(records.size());
		
		
		
//		BillingFeedSync bfs = new BillingFeedSync();
//		try
//		{
//			System.out.println(bfs.doIt());
//		}
//		catch (Exception ex)
//		{
//			System.out.println(ex);
//		}
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
	
	private static String fixDate(String date)
	{
		Pattern datePattern = Pattern.compile(DATE_TIME_PATTERN); 
	    Matcher dateMatcher = datePattern.matcher(date);
		
	    if (dateMatcher.find())
	    {
	    	String year = dateMatcher.group(1);
			String day = dateMatcher.group(3);
			String month = dateMatcher.group(2);
			String hour = dateMatcher.group(4);
			String minute = dateMatcher.group(5);
			String second = dateMatcher.group(6);
			String amPmMarker = dateMatcher.group(7);
			Integer h = new Integer(hour);
						
			if (day.length() == 1)
				day = "0" + day;
			
			if (month.length() == 1)
				month = "0" + month;
			
			if (hour.length() == 1)
				hour = "0" + hour;
			else
			{
				
				if(h.compareTo(12)>0)
				{
					amPmMarker = "PM";
					h = h-12;
				}
				else
				{
					amPmMarker = "AM";
				}
			}
			
			if (minute.length() == 1)
				minute = "0" + minute;
			
			if (second.length() == 1)
				second = "0" + second;
			
			
			//amPmMarker = amPmMarker.replace(".", "").toUpperCase();
			
			return day + "/" + month + "/" + year + " " + h + ":" + minute + ":" + second + " " + amPmMarker;			
	    }
	    else
	    	log.severe("Failed to match date against pattern");

		return date;		
	}
	
	//Load Billing records and get data using Vibe API and update Billing schema using gson API
	public String loadBillingRecordsVibe(BillingAccount account)
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
			List<String[]> billingFeed = getBillingFeedVibe(account , fromId);
							
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
			/*else if ((originNumber == null || originNumber.length() < 1) && 
				(destinationNumber == null || destinationNumber.length() < 1))
			{
				log.severe("2talk returned a \"pointer\" row, API has changed");
			}*/
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
									boolean inbound = BillingRecord.TYPE_INBOUND.equals(br.getType()) || br.getType().equals("IS") || br.getType().equals("IM") || br.getType().equals("Inbound");
	
									for (String subscribedFaxNumber : subFaxNumbers.keySet())
									{	
										String callType=subFaxNumbers.get(subscribedFaxNumber);
										if(callType.equals("in") && inbound && subscribedFaxNumber.equals(br.getDestinationNumber()))
										{
											log.info("Adding new record in Radius Account--Destination Number:"+br.getDestinationNumber()+" Type :" +br.getType()+ " 2 Talk ID :"+br.getTwoTalkId() );
											if(feedtype == 1)
												RadiusConnector.addRadiusAccount(br);
											else if (feedtype == 2)
												RadiusConnector.addRadiusAccountAU(br);
										}
										if(callType.equals("out") && !inbound && subscribedFaxNumber.equals(br.getOriginNumber()))
										{
											log.info("Adding new record in Radius Account--Destination Number:"+br.getDestinationNumber()+" Type :" +br.getType()+ " 2 Talk ID :"+br.getTwoTalkId() );
											if(feedtype == 1)
												RadiusConnector.addRadiusAccount(br);
											else if (feedtype == 2)
												RadiusConnector.addRadiusAccountAU(br);
										}
										if(callType.equals("true") && (subscribedFaxNumber.equals(br.getDestinationNumber()) || subscribedFaxNumber.equals(br.getOriginNumber())))
										{
											log.info("Adding new record in Radius Account--Destination Number:"+br.getDestinationNumber()+" Type :" +br.getType()+ " 2 Talk ID :"+br.getTwoTalkId() );
											if(feedtype == 1)
												RadiusConnector.addRadiusAccount(br);
											else if (feedtype == 2)
												RadiusConnector.addRadiusAccountAU(br);
										}
										
										if(callType.equals("voip") && subscribedFaxNumber.equals(br.getDestinationNumber()))
										{
											log.info("Adding new record in Radius Account--Destination Number:"+br.getDestinationNumber()+" Type :" +br.getType()+ " 2 Talk ID :"+br.getTwoTalkId() );
											if(feedtype == 3)
												RadiusConnector.addRadiusAccount(br);
										}
										if(callType.equals("voip") && subscribedFaxNumber.equals(br.getOriginNumber()))
										{
											log.info("Adding new record in Radius Account--Destination Number:"+br.getDestinationNumber()+" Type :" +br.getType()+ " 2 Talk ID :"+br.getTwoTalkId() );
											if(feedtype == 3)
												RadiusConnector.addRadiusAccount(br);
										}
										/*if ((inbound && subscribedFaxNumber.equals(br.getDestinationNumber())) || 
											(!inbound && subscribedFaxNumber.equals(br.getOriginNumber())))
										{
											log.info("Adding new record in Radius Account--Destination Number:"+br.getDestinationNumber()+" Type :" +br.getType()+ " 2 Talk ID :"+br.getTwoTalkId() );
											RadiusConnector.addRadiusAccount(br);
										}*/
									}
																		
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
					{       
						    boolean billingAccountUpdated = BillingConnector.updateBillingAccount(account.getBillingAccountId().toString());
							endFound = true ;
					}
				}
				
				/*// Load next id
				latestTwoTalkId = BillingConnector.getLatestTwoTalkId(account.getBillingAccountId());
				if (latestTwoTalkId != null && latestTwoTalkId > 0)
				{
					fromId = latestTwoTalkId;
				}
				else
				{
					log.severe("Failed to get next fromId from local DB BillingConnector.getLatestTwoTalkId()");
					break;
				}*/
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
}
