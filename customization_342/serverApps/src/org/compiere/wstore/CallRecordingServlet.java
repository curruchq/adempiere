package org.compiere.wstore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.compiere.util.CLogger;
import org.compiere.util.WebEnv;
import org.compiere.util.WebSessionCtx;
import org.compiere.util.WebUser;
import org.compiere.util.WebUtil;

import com.conversant.db.BillingConnector;
import com.conversant.db.SERConnector;
import com.conversant.model.BillingAccount;
import com.conversant.model.BillingRecord;
import com.conversant.model.SIPAccount;

public class CallRecordingServlet  extends HttpServlet
{
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(CallRecordingServlet.class);
	
	/** Name 										*/
	public static final String NAME = "callRecordingServlet";
	
	/** Default JSP page 							*/
	public static final String JSP_DEFAULT = "callRecording.jsp";
	
	/** URLs										*/
	private static final String DOMAIN = "account.2talk.co.nz";
	private static final String LOGIN_ACCOUNT_URL = "https://" + DOMAIN + "/login.aspx?ReturnUrl=%2fDefault.aspx";
	private static final String HTTP_SEARCH_URL = "http://" + DOMAIN + "/forms/search.aspx";

	/** Request parameters							*/
	public static final String PARAM_ACTION = "action";
	public static final String ACTION_SEARCH = "search";
	public static final String ACTION_DOWNLOAD = "download";
	
	/** Account creditials							*/
//	private static final String ACCOUNT_USERNAME = "10104115";
//	private static final String ACCOUNT_PASSWORD = "l70kw62z";
	
	/** Account creditials							*/
//	public static final HashMap<String, String> CALL_RECORDING_ACCOUNTS = new HashMap<String, String>(){
//		{
//			put("10104115", "l70kw62z"); // 028891398
//			put("10159615", "g57tr26k"); // 028892520
//			put("10667064", "s60nq27u"); // 02825503272
//		}
//	};
	
	/** Cookie names								*/
	private static final String COOKIE_ASPNET_SESSION_ID = "ASP.NET_SessionId";
	private static final String COOKIE_VISIBILL_ASPXAUTH = "VISIBILL.ASPXAUTH";
	
	/** Page attribute names						*/
	private static final String EVENTTARGET = "__EVENTTARGET";
	private static final String EVENTARGUMENT = "__EVENTARGUMENT";
	private static final String LASTFOCUS = "__LASTFOCUS";
	private static final String VIEWSTATE = "__VIEWSTATE";
	private static final String EVENTVALIDATION = "__EVENTVALIDATION";
	
	/** Date format 								*/
	private static final String DATE_FORMAT = "dd/MM/yyyy";
	
	/** Info message identifier						*/
	public static final String INFO_MSG = "infoMsg";
	
	/** Request and session attributes 				*/
	public static final String ATTR_CALL_RECORDS = "callRecords";
	
	/** Call Recording directory					*/
	protected static final String CALL_RECORDING_DIR = "recordings";
	
	/** Tomcat temp directory						*/
	protected static final String TOMCAT_TMP_DIR = "temp";
	
	/** */
	protected static final String WEBAPPS_DIR = "webapps";
	
	/** */
	protected static final String ROOT_DIR = "ROOT";
	
	/** MP3 file extension							*/
	protected static final String EXT_MP3 = ".mp3";
	
	/** TMP file extension							*/
	protected static final String EXT_TMP = ".tmp";
	
	/** */
	private static final String CTL00_SM_HIDDENFIELD_NAME = "ctl00_sm_HiddenField";
	
	/** */
	private static final String CTL00_SM_HIDDENFIELD_VALUE = ";;System.Web.Extensions, Version=3.5.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35:en-US:3bbfe379-348b-450d-86a7-bb22e53c1978:52817a7d:67c678a8;Telerik.Web.UI, Version=2008.2.826.35, Culture=neutral, PublicKeyToken=121fae78165ba3d4:en-US:2b1b618c-2ad2-4d4e-8b5a-afa3e17baf61:393f5085;Telerik.Web.UI, Version=2008.2.826.35, Culture=neutral, PublicKeyToken=121fae78165ba3d4:en-US:2b1b618c-2ad2-4d4e-8b5a-afa3e17baf61:34f9d57d";
	
	/** */
	private static final String CONTENT_TYPE_NAME = "Content-Type";
	
	/** */
	private static final String CONTENT_TYPE_AUDIO_MP3 = "audio/mp3";
	
	/** */
	private static final int HASH_LENGTH = 16;
	
//	// TODO: Move to init
//	private MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
//	private HttpClient httpClient = new HttpClient(connectionManager);
	
	/**
	 * 	Initialize global variables
	 *  @param config servlet configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WebEnv.initWeb(config))
			throw new ServletException("CallRecordingServlet.init");
	} 	// init
	
	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Call Recording Servlet";
	}	// getServletInfo
	
	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.fine("destroy");
	}   // destroy
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		log.info("Get from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		handleRequest(request, response);
	}	// doGet

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		log.info("Post from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		handleRequest(request, response);
	}	// doPost

	private void handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		// Get session and remove any existing header message
		HttpSession session = request.getSession(true);
		session.removeAttribute(WebSessionCtx.HDR_MESSAGE);
		
		if (isLoggedIn(request, response))
		{
			// Process action
			String action = WebUtil.getParameter(request, PARAM_ACTION);
			
			boolean forward = true;
			
			// Catch action when isn't set or doesn't have a value
			if (action == null || action.length() < 1)
			{
				// Do nothing
			}
			else if (action.equals(ACTION_SEARCH))
			{
				String billingGroup = WebUtil.getParameter(request, BillingGroupListTag.BILLING_GROUP_SELECT_NAME);
				String originNumber = WebUtil.getParameter(request, "form.input.originNumber");
				String destinationNumber = WebUtil.getParameter(request, "form.input.destinationNumber");
				String callDate = WebUtil.getParameter(request, "form.input.callDate");
				
				searchCallRecords(request, billingGroup, originNumber, destinationNumber, callDate);
			}
			else if (action.equals(ACTION_DOWNLOAD))
			{
				if (downloadRecording(request, response))
					forward = false;
				else
				{
					// Save form data from disappearing on reload (differen't form submitted when downloading)					
					request.setAttribute(BillingGroupListTag.BILLING_GROUP_SELECT_NAME, WebUtil.getParameter(request, BillingGroupListTag.BILLING_GROUP_SELECT_NAME));
					request.setAttribute("form.input.originNumber", WebUtil.getParameter(request, "form.input.originNumber"));
					request.setAttribute("form.input.destinationNumber", WebUtil.getParameter(request, "form.input.destinationNumber"));
					request.setAttribute("form.input.callDate", WebUtil.getParameter(request, "form.input.callDate"));
					
					ArrayList<BillingRecord> callRecords = new ArrayList<BillingRecord>();
					
					String[] values = request.getParameterValues("twoTalkId");
					for (int i=0; i<values.length; i++)
					{
						try
						{
							BillingRecord br = new BillingRecord();
							
							values = request.getParameterValues("twoTalkId");
							br.setTwoTalkId(new Long(values[i]));
							
							values = request.getParameterValues("billingGroup");
							br.setBillingGroup(values[i]);
							
							values = request.getParameterValues("originNumber");
							br.setOriginNumber(values[i]);
							
							values = request.getParameterValues("destinationNumber");
							br.setDestinationNumber(values[i]);
							
							values = request.getParameterValues("dateTime");
							br.setFormattedDateTime(values[i]);
							
							values = request.getParameterValues("callLength");
							br.setCallLength(values[i]);
							
							callRecords.add(br);
						}
						catch (Exception ex)
						{
							log.warning("Failed to parse table row back into BillingRecord object");
						}
					}
					
					request.setAttribute(ATTR_CALL_RECORDS, callRecords);
				}
			}
			
			if (forward)
				forward(request, response, JSP_DEFAULT);
		}
	}
	
	private void searchCallRecords(HttpServletRequest request, String billingGroup, String originNumber, String destinationNumber, String callDate)
	{
		// Get web user
		WebUser wu = WebUser.get(request);
		
		// Validate parameters
		String errorMsg = "";
		if (billingGroup == null || billingGroup.length() < 1)
		{
			if (errorMsg.length() > 1)
				errorMsg = errorMsg + " - ";
			else
				errorMsg = "The following error(s) occurred: ";
			
			errorMsg = errorMsg + "Billing Group is invalid";
		}
		
		if (originNumber == null || originNumber.length() < 1)
		{
			if (errorMsg.length() > 1)
				errorMsg = errorMsg + " - ";
			else
				errorMsg = "The following error(s) occurred: ";
			
			errorMsg = errorMsg + "Origin number is invalid";
		}
		
		if (destinationNumber == null || destinationNumber.length() < 1)
		{
			if (errorMsg.length() > 1)
				errorMsg = errorMsg + " - ";
			else
				errorMsg = "The following error(s) occurred: ";
			
			errorMsg = errorMsg + "Destination number is invalid";
		}
		
		if (callDate == null || callDate.length() < 1)
		{
			if (errorMsg.length() > 1)
				errorMsg = errorMsg + " - ";
			else
				errorMsg = "The following error(s) occurred: ";
			
			errorMsg = errorMsg + "Call date is invalid";
		}
		else if (!isValidDate(callDate))
		{
			if (errorMsg.length() > 1)
				errorMsg = errorMsg + " - ";
			else
				errorMsg = "The following error(s) occurred: ";
			
			errorMsg = errorMsg + "Invalid format for Call date e.g. dd/mm/yyyy";
		}
			
		boolean billingGroupFound = false;
		if (wu.isEmployee())
		{
			billingGroupFound = true;
		}
		else
		{
			// Validate user owns SIP account
			ArrayList<SIPAccount> sipAccounts = SERConnector.getSIPAccounts(wu.getC_BPartner_ID());
			for (SIPAccount sipAccount : sipAccounts)
			{
				if (sipAccount.getUsername() != null && sipAccount.getUsername().equals(billingGroup))
					billingGroupFound = true;					
			}
		}
		
		if (!billingGroupFound)
		{
			if (errorMsg.length() > 1)
				errorMsg = errorMsg + " - ";
			else
				errorMsg = "The following error(s) occurred: ";
			
			errorMsg = errorMsg + "The billing group does not belong to you";
		}
		
		// If no error then search call records
		if (errorMsg.length() < 1)
		{		
			// Format billing group
			if (billingGroup.startsWith("64"))
				billingGroup = "0" + billingGroup.substring(2, billingGroup.length());
			
			ArrayList<BillingRecord> callRecords = BillingConnector.getBillingRecords(billingGroup, originNumber, destinationNumber, parseDate(callDate));
			request.setAttribute(ATTR_CALL_RECORDS, callRecords);
		}
		else
			setInfoMsg(request, errorMsg);
	}
	
	private boolean downloadRecording(HttpServletRequest request, HttpServletResponse response)
	{
		WebUser wu = WebUser.get(request);
		
		String twoTalkId = null;
		Enumeration en = request.getParameterNames();
		while (en.hasMoreElements())
		{
			String parameter = (String)en.nextElement();
			if (parameter.startsWith("Download_"))
			{
				twoTalkId = parameter.substring(9).trim();
				break;
			}
		}
		
		// Get position of submitted listenId
		String[] values = request.getParameterValues("twoTalkId");
		int row = -1;
		for (int i=0; i < values.length; i++)
		{
			if (values[i].equals(twoTalkId))
			{
				row = i;
				break;
			}
		}
		
		if (row >= 0 && twoTalkId != null)
		{			
			String billingGroup = request.getParameterValues("billingGroup")[row];
			String originNumber = request.getParameterValues("originNumber")[row];
			String destinationNumber = request.getParameterValues("destinationNumber")[row];
			String dateTime = request.getParameterValues("dateTime")[row];
			String callLength = request.getParameterValues("callLength")[row];
			
			if (billingGroup != null && billingGroup.length() > 0 &&
			    originNumber != null && originNumber.length() > 0 &&
				destinationNumber != null && destinationNumber.length() > 0 &&
				dateTime != null && dateTime.length() > 0 &&
				callLength != null && callLength.length() > 0)
			{					
				boolean userOwnsSIPAccount = false;
				
				if (wu.isEmployee())
				{
					userOwnsSIPAccount = true;
				}
				else
				{
					ArrayList<SIPAccount> sipAccounts = SERConnector.getSIPAccounts(wu.getC_BPartner_ID());
					for (SIPAccount sipAccount : sipAccounts)
					{
						if (sipAccount.getUsername() != null && sipAccount.getUsername().equals(billingGroup))
						{
							userOwnsSIPAccount = true;
							break;
						}
					}
				}
				
				if (userOwnsSIPAccount)
				{
					for (BillingAccount account : BillingConnector.getBillingAccounts())
					{																
						HashMap<String, String> cookieData = loginToAccount(account.getUsername(), account.getPassword());
						if (cookieData != null)
						{
							File recording = getCallRecording(cookieData, request.getServerName(), twoTalkId, originNumber, destinationNumber, dateTime);
							
							if (recording != null)
							{
								if (streamToResponse(request, response, recording))
								{
									if (!recording.delete())
										log.severe("Failed to delete recording after streaming to user File[" + recording + "]");																	
								}
								else			
									log.warning("Failed to stream recording to user");
								
								return true;
							}
						}
						else
							log.severe("Failed to login to 2talk username=" + account.getUsername());
					}
					
					return false;
				}
				else
					log.warning("User tried to download a recording which did not belong to them");
			}
			else
				log.severe("User clicked download call recording but some parameters weren't set, debug");
		
		}
		
		setInfoMsg(request, "An internal error occurred. Please try again later");
		
		return false;
	}
	
	private boolean isValidDate(String date)
	{
	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

	    Date testDate = null;
	    try
	    {
	      testDate = sdf.parse(date);
	    }
	    catch (ParseException ex)
	    {
	      return false;
	    }
	    
	    // Make sure date's match (SDF rolls dec 32 over to 01 jan)
	    if (!sdf.format(testDate).equals(date))
	      return false;
	    

	    return true;
	}
	
	public Date parseDate(String date)
	{
		// Create Simple Date Format
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		
		// Parse
		try
		{
			Date parsedDate = sdf.parse(date);
			
			// Make sure date's match (SDF rolls dec 32 over to 01 jan)
		    String sParsedDate = sdf.format(parsedDate);
			if (sParsedDate.equalsIgnoreCase(date))
		    {
		    	return parsedDate;
		    }
		    else
		    	log.severe("Parsed date doesn't match original date ParsedDate[" + sParsedDate + "] OriginalDate[" + date + "]");
		}
		catch (ParseException ex)
	    {
			log.severe("Failed to parse Date[" + date + "]\n" + ex);
		}

		return null;
	}
	
	private void forward(HttpServletRequest request, HttpServletResponse response, String url)
		throws ServletException, IOException
	{
		if (!url.startsWith("/")) url = "/" + url;
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
	}
	
	private boolean streamToResponse(HttpServletRequest request, HttpServletResponse response, File file)
	{
		boolean success = false;
		
		if (file == null || !file.exists()) 
		{
			log.warning("Cannot stream file which doesn't exist File[" + file + "]");
		}
		else
		{
			FileInputStream in = null;
			ServletOutputStream out = null;
			try
			{
				int bufferSize = 2048; //	2k Buffer
				int fileLength = (int)file.length();
				//
				response.setContentType(CONTENT_TYPE_AUDIO_MP3);
				response.setBufferSize(bufferSize);
				response.setContentLength(fileLength);
				response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
			
				log.fine(file.getAbsolutePath() + ", length=" + fileLength);
//				long time = System.currentTimeMillis();		//	timer start
				
				in = new FileInputStream (file);
				out = response.getOutputStream();
				byte[] buffer = new byte[bufferSize];
				double totalSize = 0;
				int count = 0;
				do
				{
					count = in.read(buffer, 0, bufferSize);
					if (count > 0)
					{
						totalSize += count;
						out.write (buffer, 0, count);
					}
				} while (count != -1);

				out.flush();
				
				success = true;
//				time = System.currentTimeMillis() - time;
//				double speed = (totalSize/1024) / ((double)time/1000);
//				log.fine("Length=" 
//					+ totalSize + " - " 
//					+ time + " ms - " 
//					+ speed + " kB/sec");
			}
			catch (IOException ex)
			{
				log.severe(ex.toString());
			}
			finally
			{
				try
				{
					if (out != null) 
						out.close();
				}
				catch (IOException ex)
				{
					log.severe(ex.toString());
				}
				
				try
				{
					if (in != null) 
						in.close();
				}
				catch (IOException ex)
				{
					log.severe(ex.toString());
				}
			}
		}
		
		return success;
	}
	
	private void setInfoMsg(HttpServletRequest request, String msg)
	{
		request.setAttribute(INFO_MSG, msg);
	}
	
	private boolean isLoggedIn(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException
	{
		// Check user logged in
		WebUser wu = WebUser.get(request);
		if (wu == null || !wu.isLoggedIn())
		{
			String url = request.getContextPath() + "/loginServlet";
			log.info ("Not logged in -> Redirecting to " + url);
			url = response.encodeRedirectURL(url);
			if (!response.isCommitted())
				response.sendRedirect(url);
			return false;
		}
		else
			return true;
	}	
	
	/**
	 * Login to 2talk account portal
	 * 
	 * @param username
	 * @param password
	 * @param pageAttributes
	 * @return Cookie data if login successful
	 */
	private HashMap<String, String> loginToAccount(String username, String password)
	{
		HttpClient client = new HttpClient();
		
		PostMethod postLogin = null;

		// Get page attributes
		HashMap<String, String> pageAttributes = getPageAttributes(LOGIN_ACCOUNT_URL, null, null);
		
		try
		{
			// Create POST method to login
			postLogin = new PostMethod(LOGIN_ACCOUNT_URL);
			postLogin.addParameter(EVENTTARGET , pageAttributes.get(EVENTTARGET));
			postLogin.addParameter(EVENTARGUMENT , pageAttributes.get(EVENTARGUMENT));
			postLogin.addParameter(LASTFOCUS , pageAttributes.get(LASTFOCUS));
			postLogin.addParameter(VIEWSTATE, pageAttributes.get(VIEWSTATE));
			postLogin.addParameter(EVENTVALIDATION, pageAttributes.get(EVENTVALIDATION));
			postLogin.addParameter("txtUsername", username); 
			postLogin.addParameter("txtPassword", password);
			postLogin.addParameter("cmdLogin", "Log In"); 
			
			// Send request
			int returnCode = client.executeMethod(postLogin);

			// Check code returned from request
			if (returnCode == HttpStatus.SC_MOVED_TEMPORARILY) 
			{
				// Get cookie data
				HashMap<String, String> cookieData = extractCookieData(client.getState().getCookies());

				// Get header returned from login POST request
				Header header = postLogin.getResponseHeader("location"); 
				if (header != null) 
				{
					String newURL = URLDecoder.decode(header.getValue(), "UTF-8"); // TODO: Check what happens when string null or empty
					if (newURL == null || newURL.length() < 1) 
					{
						log.severe("Could not load header location URL, invalid redirect");
						return null;
					}
					else if (newURL.toLowerCase().contains("default.aspx")) 
					{
						return cookieData;
					}
				} 
				else 
				{
					log.severe("Could not find location header, invalid redirect");
					return null;
				}
			} 
			else
			{
				log.severe("Returned unexpected code " + returnCode + ", debug.");
				return null;
			}
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
		
		return null;
	}
	
	/**
	 * Gets page attributes needed for other requests
	 * 
	 * @return Page attributes
	 */
	private HashMap<String, String> getPageAttributes(String url, HashMap<String, String> cookieData, HashMap<String, String> parameters)
	{
		HttpClient client = new HttpClient();
		
		HashMap<String, String> data = new HashMap<String, String>();
		
		if (cookieData != null && cookieData.size() > 0)
		{
			// Create and set cookies
			HttpState initialState = new HttpState();
			Cookie aspSessionId = new Cookie(DOMAIN, COOKIE_ASPNET_SESSION_ID, cookieData.get(COOKIE_ASPNET_SESSION_ID), "/", null, false);
			initialState.addCookie(aspSessionId);
			Cookie visibillAuth = new Cookie(DOMAIN, COOKIE_VISIBILL_ASPXAUTH, cookieData.get(COOKIE_VISIBILL_ASPXAUTH), "/", null, false);
			initialState.addCookie(visibillAuth);
	
			client.setState(initialState);
		}

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
	
	/**
	 * Extracts cookie data
	 * 
	 * @param cookies
	 * @return
	 */
	private HashMap<String, String> extractCookieData(Cookie[] cookies)
	{
		HashMap<String, String> cookieData = new HashMap<String, String>();
		
		// Handle null param
		if (cookies == null)
			return cookieData;
		
		for (Cookie cookie : cookies) 
		{
			if (cookie.getName() == null || cookie.getValue().length() < 1)
			{
				log.severe("Found cookie without a name, debug.");
			}
			else if (cookie.getValue() == null || cookie.getValue().length() < 1)
			{
				log.severe("Found cookie without a value, debug.");
			}
			
			if (cookie.getName().equals(COOKIE_ASPNET_SESSION_ID))
			{
				cookieData.put(COOKIE_ASPNET_SESSION_ID, cookie.getValue());
			}
			else if (cookie.getName().equals(COOKIE_VISIBILL_ASPXAUTH))
			{
				cookieData.put(COOKIE_VISIBILL_ASPXAUTH, cookie.getValue());
			}
		}
		
		return cookieData;
	}
	
	private String getCallRecordingPath()
	{
		String callRecordingPath =  WEBAPPS_DIR + File.separator + ROOT_DIR + File.separator + CALL_RECORDING_DIR + File.separator;
		
		boolean dirExists = false;
		
		// Check exists, if not then create it
		File callRecordingDir = new File(callRecordingPath);
		if(!callRecordingDir.isDirectory())
		{
			// If cannot make directory use tomcat temp dir
			if (!callRecordingDir.mkdir())
				log.severe("Failed to create recording directory - " + callRecordingPath);
			else
				dirExists = true;
		}
		else
			dirExists = true;
		
		if (dirExists)
		{
			// Create .htaccess file to disallow directory listings
			String htaccessFilename = callRecordingDir.getAbsolutePath() + File.separator + ".htaccess";
			
			File htaccess = new File(htaccessFilename);
			if (!htaccess.exists())
			{
				FileWriter out = null;
				
				try
				{
					out = new FileWriter(htaccessFilename); 
					out.write("IndexIgnore *"); 
				}
				catch (IOException ex)
				{
					log.severe("Failed to create " + htaccessFilename + " - " + ex);
				}
				finally
				{
					try
					{
						if (out != null)
							out.close();
					}
					catch (IOException ex)
					{
						log.severe("Failed to close " + htaccessFilename + " - " + ex);
					}
				}
			}
		}
			
		return callRecordingPath;
	}
	
	private HashMap<String, String> loadPageAttributesForDownload(HashMap<String, String> cookieData, String dateTime)
	{
		// Get page attributes from blank /forms/search.aspx
		HashMap<String, String> pageAttributes = getPageAttributes(HTTP_SEARCH_URL, cookieData, null);

		HashMap<String, String> pageAttributeParameters = new HashMap<String, String>();
		pageAttributeParameters.put(CTL00_SM_HIDDENFIELD_NAME, CTL00_SM_HIDDENFIELD_VALUE);
		pageAttributeParameters.put(EVENTTARGET, pageAttributes.get(EVENTTARGET));
		pageAttributeParameters.put(EVENTARGUMENT , pageAttributes.get(EVENTARGUMENT));
		pageAttributeParameters.put(LASTFOCUS , pageAttributes.get(LASTFOCUS));
		pageAttributeParameters.put(VIEWSTATE, pageAttributes.get(VIEWSTATE));
		pageAttributeParameters.put(EVENTVALIDATION, pageAttributes.get(EVENTVALIDATION));
		pageAttributeParameters.put("ctl00$plhContent$cmdSearch", "Submit");
		
		// Get page attributes when submitting form to /forms/search.aspx
		pageAttributes = getPageAttributes(HTTP_SEARCH_URL, cookieData, pageAttributeParameters);
		
		return pageAttributes;
	}
	
	private File getCallRecording(HashMap<String, String> cookieData, String serverName, String twoTalkId, String originNumber, String destinationNumber, String dateTime)
	{
		HttpState initialState = new HttpState();
		Cookie aspSessionId = new Cookie(DOMAIN, COOKIE_ASPNET_SESSION_ID, cookieData.get(COOKIE_ASPNET_SESSION_ID), "/", null, false); 
		initialState.addCookie(aspSessionId);
		Cookie visibillAuth = new Cookie(DOMAIN, COOKIE_VISIBILL_ASPXAUTH, cookieData.get(COOKIE_VISIBILL_ASPXAUTH), "/", null, false);
		initialState.addCookie(visibillAuth);
		
		HttpClient client = new HttpClient();
		client.setState(initialState);
		
		PostMethod postSearch = null;
		
		// Get page attributes
		HashMap<String, String> pageAttributes = loadPageAttributesForDownload(cookieData, dateTime);
		
		try
		{
			postSearch = new PostMethod(HTTP_SEARCH_URL);
			postSearch.addParameter(CTL00_SM_HIDDENFIELD_NAME, CTL00_SM_HIDDENFIELD_VALUE);
			postSearch.addParameter(EVENTTARGET , "ctl00$plhContent$Searchdata1$cmdListen");
			postSearch.addParameter(EVENTARGUMENT , pageAttributes.get(EVENTARGUMENT));
			postSearch.addParameter(LASTFOCUS , pageAttributes.get(LASTFOCUS));
			postSearch.addParameter(VIEWSTATE, pageAttributes.get(VIEWSTATE));
			postSearch.addParameter(EVENTVALIDATION, pageAttributes.get(EVENTVALIDATION));
			postSearch.addParameter("txtListenID", twoTalkId);

			// Send request
			int returnCode = client.executeMethod(postSearch);			
			if (returnCode == HttpStatus.SC_OK)
			{		
				// Check mp3 is returned
				Header type = postSearch.getResponseHeader(CONTENT_TYPE_NAME); 
				if (type != null && type.getValue() != null && type.getValue().equals(CONTENT_TYPE_AUDIO_MP3)) 
				{
					boolean success = false;
					
					// Extract just date and remove -'s
					String filenameDate = dateTime.replace("-", "");
					filenameDate = filenameDate.substring(0, filenameDate.indexOf(" "));
					
					String path = "";//TOMCAT_TMP_DIR + File.separator; //getCallRecordingPath();
					String filename = originNumber + "-" + destinationNumber + "-" + filenameDate;
					
					// Create tmp file
					File tmpRecording = new File(path + filename + EXT_TMP); 
					
					InputStream in = null;
					OutputStream out = null;
					
					try
					{						
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
					}
					catch (Exception ex)
					{
						log.severe("Error streaming to file: " + ex);
					}
					finally 
					{
				        if (out != null) out.flush();
				        
				        if (in != null) in.close();
				        if (out != null) out.close();
					}
					
					if (success)
					{
						// Rename with mp3 extension
						File recording = new File(path + filename + EXT_MP3);
				        success = tmpRecording.renameTo(recording);
				        
				        if (success)
				        {
				        	return recording;
				        }
				        else
				        {
				        	log.severe("Failed to rename recording from temporary name, debug (deleting recordings)");
				        	recording.delete();
				        	tmpRecording.delete();
				        }
					}
					else
					{
						log.severe("Failed to download recording, debug");
						tmpRecording.delete();
					}
				}
				else
				{
					log.severe("Content-Type was not audio/mp3 when trying to download call recording, debug.");
				}
			}
			else 
			{
				log.severe("Error retrieving call recording, debug.");
			}
		}
		catch (Exception ex)
		{
			log.severe("Exception Raised: " + ex);
		}
		finally 
		{
			if (postSearch != null)
				postSearch.releaseConnection();
		}
		
		return null;
	}
	
	private String getHashId()
	{
		Random rn = new Random();
		return Long.toString(System.currentTimeMillis() * (1000 + rn.nextInt(9999))).substring(0, HASH_LENGTH);
	}
	
	public static void main(String[] args)
	{
//		CallRecordingServlet crs = new CallRecordingServlet();
//		
//		HashMap<String, String> cookieData = crs.loginToAccount(ACCOUNT_USERNAME, ACCOUNT_PASSWORD);
//		if (cookieData != null)
//		{
//			File recording = crs.getCallRecording(cookieData, "http://dev", "51736920", "1234", "5678", "1/1/10 10:00:00");
//			System.out.println(recording);
//		}
	}
}
