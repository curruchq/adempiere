package com.conversant.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.compiere.util.CLogger;

import com.conversant.db.BillingConnector;
import com.conversant.model.BillingAccount;

public class TwoTalkConnector
{
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(TwoTalkConnector.class);
	
	/** URLs										*/
	private static final String DOMAIN = "account.2talk.co.nz";
	private static final String LOGIN_ACCOUNT_URL = "https://" + DOMAIN + "/login.aspx?ReturnUrl=%2fDefault.aspx";
	private static final String HTTP_SEARCH_URL = "https://" + DOMAIN + "/forms/search.aspx";
	
	/** Page attribute names						*/
	private static final String EVENTTARGET = "__EVENTTARGET";
	private static final String EVENTARGUMENT = "__EVENTARGUMENT";
	private static final String LASTFOCUS = "__LASTFOCUS";
	private static final String VIEWSTATE = "__VIEWSTATE";
	private static final String EVENTVALIDATION = "__EVENTVALIDATION";
	
	/** */
	private static final String CONTENT_TYPE_NAME = "Content-Type";
	private static final String CONTENT_TYPE_AUDIO_MP3 = "audio/mp3";
	
	/** MP3 file extension							*/
	protected static final String EXT_MP3 = ".mp3";
	
	/** TMP file extension							*/
	protected static final String EXT_TMP = ".tmp";
	
	/**
	 * Get Call Recording
	 * 
	 * @param listenId Id used to identify recording
	 * @return File Call recording in mp3 format
	 */
	public static File getCallRecording(String listenId)
	{
		HttpClient client = new HttpClient();
		client.setState(new HttpState());

			String cdrid[]=listenId.split(":");	
			log.info("RECORDING ID "+cdrid[0]);
			String testRequest="<request><authentication><accountcode>10104115</accountcode><password>h56gy23f</password></authentication><action>getrecording</action><parameters><cdrid>278509303</cdrid></parameters></request>";
			log.info(testRequest);
			
			PostMethod postSearch = null;
			/*NameValuePair[] params = {  
					   new NameValuePair("RequestXML", testRequest) 
					};	*/
			try
			{	
				postSearch = new PostMethod(getBaseURI());
				postSearch.addParameter("RequestXML", testRequest);
				//postSearch.setRequestBody(params);
	
				// Send request
				int returnCode = client.executeMethod(postSearch);		
				log.info("STATUS RETURNED FROM 2TALK : "+returnCode);
				if (returnCode == HttpStatus.SC_OK)
				{			
					// Check mp3 is returned
					Header type = postSearch.getResponseHeader(CONTENT_TYPE_NAME);
					log.info("CONTENT TYPE RETURNED FROM 2TALK : "+type);
					log.info(postSearch.getResponseBodyAsString());
					if (type != null && type.getValue() != null) 
					{
						log.info("1");
						boolean success = false;
						
						String filename = listenId.replaceAll(":", "");
						
						// Create tmp file
						File tmpRecording = new File(filename + EXT_TMP); 
						
						InputStream in = null;
						OutputStream out = null;
						
						try
						{	
							log.info("2");
							in = postSearch.getResponseBodyAsStream();					
							out = new FileOutputStream(tmpRecording);
						    
					        // Transfer bytes from in to out
					        byte[] buf = new byte[1024];
					        int len;
					        while ((len = in.read(buf)) > 0) 
					        {
					            out.write(buf, 0, len);
					        }		
					        
					        success = true;
					        log.info("SUCCESS---1"+success);
						}
						catch (Exception ex)
						{
							log.info("3");
							log.severe("Error streaming to file: " + ex);
						}
						finally 
						{
							log.info("4");
					        if (out != null) out.flush();
					        
					        if (in != null) in.close();
					        if (out != null) out.close();
						}
						log.info("SUCCESS---2"+success);
						if (success)
						{
							// Rename with mp3 extension
							log.info("INSIDE SUCCESS"+success);
							File recording = new File(filename + "-" + System.currentTimeMillis() + EXT_MP3);
					        success = tmpRecording.renameTo(recording);
					        log.info("SUCCESS---3"+success);
					        log.info("5");
					        if (success){
					        	log.info("SUCCESS---4"+success);
					        	return recording;}
					        else
					        {
					        	log.info("6");
					        	log.severe("Failed to rename recording from temporary name, debug (deleting recordings)");
					        	recording.delete();
					        	tmpRecording.delete();
					        }
						}
						else
						{
							log.info("7");
							log.severe("Failed to download recording, debug");
							tmpRecording.delete();
						}
					}
					else
					{
						log.info("8");
						log.severe("Content-Type was not audio/mp3 when trying to download call recording, debug.");
					}
				}
				else 
				{
					log.info("9");
					log.severe("Error retrieving call recording, debug.");
				}
			}
			catch (Exception ex)
			{
				log.info("10");
				log.severe("Exception Raised: " + ex);
			}
			finally 
			{
				if (postSearch != null)
					postSearch.releaseConnection();
			}
			log.info("11");
		return null;
	}
	
	/**
	 * Login to 2talk account portal
	 * 
	 * @param client
	 * @param account
	 * @return True if login successful
	 */
	private static boolean login(HttpClient client, BillingAccount account)
	{
		PostMethod postLogin = null;

		HashMap<String, String> pageAttributes = getPageAttributes(client, LOGIN_ACCOUNT_URL, null);
		
		try
		{
			postLogin = new PostMethod(LOGIN_ACCOUNT_URL);
			postLogin.addParameter(EVENTTARGET , pageAttributes.get(EVENTTARGET));
			postLogin.addParameter(EVENTARGUMENT , pageAttributes.get(EVENTARGUMENT));
			postLogin.addParameter(LASTFOCUS , pageAttributes.get(LASTFOCUS));
			postLogin.addParameter(VIEWSTATE, pageAttributes.get(VIEWSTATE));
			postLogin.addParameter(EVENTVALIDATION, pageAttributes.get(EVENTVALIDATION));
			postLogin.addParameter("txtUsername", account.getUsername()); 
			postLogin.addParameter("txtPassword", account.getPassword());
			postLogin.addParameter("cmdLogin", "Log In"); 
			
			int returnCode = client.executeMethod(postLogin);
			if (returnCode == HttpStatus.SC_MOVED_TEMPORARILY) 
			{
				Header header = postLogin.getResponseHeader("location"); 
				if (header != null && header.getValue() != null) 
				{
					String newURL = URLDecoder.decode(header.getValue(), "UTF-8"); // TODO: Check what happens when string null or empty
					if (newURL == null || newURL.length() < 1) 
						log.severe("Could not load header location URL, invalid redirect");
					else if (newURL.toLowerCase().contains("default.aspx")) 
						return true;
				} 
				else 
					log.severe("Could not find location header, invalid redirect");
			} 
			else
				log.severe("Returned unexpected code " + returnCode + ", debug.");
		}
		catch (Exception ex) 
		{
			log.severe("Exception Raised: " + ex);
		} 
		finally 
		{
			if (postLogin != null) 
				postLogin.releaseConnection();
		}
		
		return false;
	}
	
	/**
	 * Gets page attributes needed for other requests
	 * 
	 * @return Page attributes
	 */
	private static HashMap<String, String> getPageAttributes(HttpClient client, String url, HashMap<String, String> parameters)
	{
		HashMap<String, String> data = new HashMap<String, String>();

		PostMethod postReq = null;		
		Pattern patt = null;
		Matcher m = null;
		
		String eventTargetValue = null;
		String eventArgumentValue = null;
		String lastFocusValue = null;
		String viewStateValue = null;
		String eventValidationValue = null;
		
		try
		{
			postReq = new PostMethod(url);
			
			// Add parameters
			if (parameters != null && parameters.size() > 0)
			{
				Iterator<String> paramIterator = parameters.keySet().iterator();
				while(paramIterator.hasNext())
				{
					String paramName = paramIterator.next();
					String paramValue = parameters.get(paramName);
					
					postReq.addParameter(paramName, paramValue);
				}
			}
			
			int returnCode = client.executeMethod(postReq);			
			if (returnCode == HttpStatus.SC_OK)
			{
				String res = postReq.getResponseBodyAsString();				
				
				String eventTargetExpr = "<input\\s+type=\"hidden\"\\s+name=\"" + EVENTTARGET + "\"\\s+id=\"" + EVENTTARGET + "\"\\s+value=\"([^\"]*)\".*?/>";
				patt = Pattern.compile(eventTargetExpr, Pattern.DOTALL | Pattern.UNIX_LINES);
				m = patt.matcher(res);
				if (m.find()) 
					eventTargetValue = m.group(1);
				
				String eventArgumentExpr = "<input\\s+type=\"hidden\"\\s+name=\"" + EVENTARGUMENT + "\"\\s+id=\"" + EVENTARGUMENT + "\"\\s+value=\"([^\"]*)\".*?/>";				
				patt = Pattern.compile(eventArgumentExpr, Pattern.DOTALL | Pattern.UNIX_LINES);
				m = patt.matcher(res);
				if (m.find()) 
					eventArgumentValue = m.group(1);				
				
				String lastFocusExpr = "<input\\s+type=\"hidden\"\\s+name=\"" + LASTFOCUS + "\"\\s+id=\"" + LASTFOCUS + "\"\\s+value=\"([^\"]*)\".*?/>";				
				patt = Pattern.compile(lastFocusExpr, Pattern.DOTALL | Pattern.UNIX_LINES);
				m = patt.matcher(res);
				if (m.find()) 
					lastFocusValue = m.group(1);					
				
				String viewStateExpr = "<input\\s+type=\"hidden\"\\s+name=\"" + VIEWSTATE + "\"\\s+id=\"" + VIEWSTATE + "\"\\s+value=\"([^\"]*)\".*?/>";				
				patt = Pattern.compile(viewStateExpr, Pattern.DOTALL | Pattern.UNIX_LINES);
				m = patt.matcher(res);
				if (m.find()) 
					viewStateValue = m.group(1);
				
				String eventValidationExpr = "<input\\s+type=\"hidden\"\\s+name=\"" + EVENTVALIDATION + "\"\\s+id=\"" + EVENTVALIDATION + "\"\\s+value=\"([^\"]*)\".*?/>";				
				patt = Pattern.compile(eventValidationExpr, Pattern.DOTALL | Pattern.UNIX_LINES);
				m = patt.matcher(res);
				if (m.find()) 
					eventValidationValue = m.group(1);						
			}
			else
			{
				log.severe("Return code was not 200 - Failed to get page attributes");
			}

		}
		catch (Exception ex) 
		{
			log.severe("Exception Raised: " + ex);
		} 
		finally 
		{
			if (postReq != null) 
				postReq.releaseConnection();
		}
		
		data.put(EVENTTARGET, eventTargetValue == null ? "" : eventTargetValue);
		data.put(EVENTARGUMENT, eventArgumentValue == null ? "" : eventArgumentValue);
		data.put(LASTFOCUS, lastFocusValue == null ? "" : lastFocusValue);
		data.put(VIEWSTATE, viewStateValue == null ? "" : viewStateValue);
		data.put(EVENTVALIDATION, eventValidationValue == null ? "" : eventValidationValue);
		
		return data;
	}
	
	public static void main(String[] args)
	{
		getCallRecording("226595953:NZ");
	}
	private static String getBaseURI() {
		return "http://www.2talk.co.nz/api/provapi.aspx";
	}
}
