package org.compiere.wstore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
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
import org.compiere.model.MMailMsg;
import org.compiere.model.MStore;
import org.compiere.model.MUserMail;
import org.compiere.model.X_W_MailMsg;
import org.compiere.util.CLogger;
import org.compiere.util.EMail;
import org.compiere.util.WebEnv;
import org.compiere.util.WebSessionCtx;
import org.compiere.util.WebUser;
import org.compiere.util.WebUtil;

import com.conversant.db.SERConnector;
import com.conversant.model.CallRecord;
import com.conversant.model.SIPAccount;

public class oldCallRecordingServlet  extends HttpServlet
{
	// TODO: Handle when servlet's called without JSP first
	
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(oldCallRecordingServlet.class);
	
	/** Name 										*/
	public static final String NAME = "callRecordingServlet";
	
	/** Default JSP page 							*/
	public static final String JSP_DEFAULT = "callRecording.jsp";
	
	/** URLs										*/
	private static final String DOMAIN = "account.2talk.co.nz";
	private static final String LOGIN_ACCOUNT_URL = "https://" + DOMAIN + "/login.aspx?ReturnUrl=%2fDefault.aspx";
	private static final String HTTP_SEARCH_URL = "http://" + DOMAIN + "/forms/search.aspx";

	/** Account creditials							*/
	private static final String ACCOUNT_USERNAME = "10104115";
	private static final String ACCOUNT_PASSWORD = "l70kw62z";

	/** Cookie names								*/
	private static final String COOKIE_ASPNET_SESSION_ID = "ASP.NET_SessionId";
	private static final String COOKIE_VISIBILL_ASPXAUTH = "VISIBILL.ASPXAUTH";
	
	/** Page attribute names						*/
	private static final String EVENTTARGET = "__EVENTTARGET";
	private static final String EVENTARGUMENT = "__EVENTARGUMENT";
	private static final String LASTFOCUS = "__LASTFOCUS";
	private static final String VIEWSTATE = "__VIEWSTATE";
	private static final String EVENTVALIDATION = "__EVENTVALIDATION";
	
	/** Request and session attributes 				*/
	public static final String ATTR_CALL_RECORDS = "callRecords";
	
	/**	Request parameters							*/
	public static final String PARAM_FILENAME = "filename";
	public static final String PARAM_ACTION = "action";
	public static final String ACTION_SEARCH = "search";
	public static final String ACTION_DOWNLOAD = "download";
	public static final String ACTION_DOWNLOAD_FILE = "downloadFile";
	
	/** Number of columns in call recording table	*/
	private static final int NUMBER_OF_COLUMNS = 9;
	
	/** 2Talk billing day of month					*/
	private static final int BILLING_DAY_OF_MONTH = 13;
	
	/** Date format 								*/
	private static final String DATE_FORMAT = "dd/MM/yyyy";
	
	/** Info message identifier						*/
	public static final String INFO_MSG = "infoMsg";
	
	/** Call Recording directory					*/
	protected static final String CALL_RECORDING_DIR = "recordings";
	
	/** Tomcat temp directory						*/
	protected static final String TOMCAT_TMP_DIR = "temp";
	
	/** MP3 file extension							*/
	protected static final String EXT_MP3 = ".mp3";
	
	/** TMP file extension							*/
	protected static final String EXT_TMP = ".tmp";
	
	/** */
	private static final String CTL00_SM_HIDDENFIELD_NAME = "ctl00_sm_HiddenField";
	
	/** */
	private static final String CTL00_SM_HIDDENFIELD_VALUE = ";;System.Web.Extensions, Version=3.5.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35:en-US:3bbfe379-348b-450d-86a7-bb22e53c1978:ea597d4b:b25378d2;Telerik.Web.UI, Version=2009.2.826.35, Culture=neutral, PublicKeyToken=121fae78165ba3d4:en-US:d2d891f5-3533-469c-b9a2-ac7d16eb23ff:16e4e7cd:ed16cbdc";//";;System.Web.Extensions, Version=3.5.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35:en-US:3bbfe379-348b-450d-86a7-bb22e53c1978:52817a7d:67c678a8;Telerik.Web.UI, Version=2008.2.826.35, Culture=neutral, PublicKeyToken=121fae78165ba3d4:en-US:2b1b618c-2ad2-4d4e-8b5a-afa3e17baf61:393f5085;Telerik.Web.UI, Version=2008.2.826.35, Culture=neutral, PublicKeyToken=121fae78165ba3d4:en-US:2b1b618c-2ad2-4d4e-8b5a-afa3e17baf61:34f9d57d";

	/** */
	private static final String TIME_12AM = " 12:00:00 a.m.";
	
	/** */
	private static final String CONTENT_TYPE_NAME = "Content-Type";
	
	/** */
	private static final String CONTENT_TYPE_AUDIO_MP3 = "audio/mp3";
	
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
			// Get web user
			WebUser wu = WebUser.get(request);
			
			// Process action
			String action = WebUtil.getParameter(request, PARAM_ACTION);
			
			// Catch action when isn't set or doesn't have a value
			if (action == null || action.length() < 1)
			{
				// Do nothing
			}
			else if (action.equals(ACTION_SEARCH))
			{
				String originNumber = WebUtil.getParameter(request, OriginNumberListTag.ORIGIN_NUMBER_SELECT_NAME);
				String destinationNumber = WebUtil.getParameter(request, "form.input.destinationNumber");
				String callDate = WebUtil.getParameter(request, "form.input.callDate");
				
				// Validate parameters set
				String errorMsg = "";
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
					
								
				// validate values
				boolean originNumberFound = false;
				ArrayList<SIPAccount> sipAccounts = SERConnector.getSIPAccounts(wu.getC_BPartner_ID());
				for (SIPAccount sipAccount : sipAccounts)
				{
					if (sipAccount.getUsername() != null && sipAccount.getUsername().equals(originNumber))
						originNumberFound = true;					
				}
				
				if (!originNumberFound)
				{
					if (errorMsg.length() > 1)
						errorMsg = errorMsg + " - ";
					else
						errorMsg = "The following error(s) occurred: ";
					
					errorMsg = errorMsg + "The origin number does not belong to you";
				}
				
				
				
				if (errorMsg.length() > 0)
				{
					setInfoMsg(request, errorMsg);
					forward(request, response, JSP_DEFAULT);
					return;
				}
	
				// TODO: Need to trim values?
				
				// date fix
				callDate = callDate + " 12:00:00 a.m.";
				
				HashMap<String, String> cookieData = loginToAccount(ACCOUNT_USERNAME, ACCOUNT_PASSWORD);
				if (cookieData != null)
				{
					ArrayList<CallRecord> callRecords = searchCalls(cookieData, destinationNumber, originNumber, callDate);
					request.setAttribute(ATTR_CALL_RECORDS, callRecords);
				}
				else
				{
					setInfoMsg(request, "An internal error occurred. Please try again later");
					log.severe("Failed to get cookie data");
				}
			}
			else if (action.equals(ACTION_DOWNLOAD))
			{
				String listenId = null;
				Enumeration en = request.getParameterNames();
				while (en.hasMoreElements())
				{
					String parameter = (String)en.nextElement();
					if (parameter.startsWith("Download_"))
					{
						listenId = parameter.substring(9).trim();
						break;
					}
				}

				// Get position of submitted listenId
				String[] values = request.getParameterValues("listenId");
				int row = -1;
				for (int i=0; i < values.length; i++)
				{
					if (values[i].equals(listenId))
					{
						row = i;
						break;
					}
				}
				
				if (row >= 0 && listenId != null)
				{			
					String originNumber = request.getParameterValues("originNumber")[row];
					String destinationNumber = request.getParameterValues("destinationNumber")[row];
					String date = request.getParameterValues("date")[row];
					String time = request.getParameterValues("time")[row];
					String duration = request.getParameterValues("duration")[row];
					
					if (originNumber != null && originNumber.length() > 0 &&
						destinationNumber != null && destinationNumber.length() > 0 &&
						date != null && date.length() > 0 &&
						time != null && time.length() > 0 &&
						duration != null && duration.length() > 0)
					{					
						boolean matchingCallRecord = false;
						ArrayList<SIPAccount> sipAccounts = SERConnector.getSIPAccounts(wu.getC_BPartner_ID());
						for (SIPAccount sipAccount : sipAccounts)
						{
							if (sipAccount.getUsername() != null && sipAccount.getUsername().equals(originNumber))
							{
								matchingCallRecord = true;
								break;
							}
						}
						
						if (matchingCallRecord)
						{
							HashMap<String, String> cookieData = loginToAccount(ACCOUNT_USERNAME, ACCOUNT_PASSWORD);
							if (cookieData != null)
							{
								WebSessionCtx wsc = WebSessionCtx.get(request);
								MStore wStore = wsc.wstore;
								
								// Start process to download recording from 2talk
								(new DownloadRecordingWorker(originNumber, destinationNumber, date, listenId, cookieData, request.getServerName(), request.getContextPath(), wStore, wu)).start();
								
								setInfoMsg(request, "Your call recording is being prepared, an email with a direct link will be sent shortly.");						
							}
						}
						else
						{
							log.warning("User tried to download a recording which did not belong to them");
							setInfoMsg(request, "An internal error occurred. Please try again later");
						}
					}
					else
					{
						log.severe("User clicked download call recording but some parameters weren't set, debug");
						setInfoMsg(request, "An internal error occurred. Please try again later");
					}
				}
			}
			else if (action.equals(ACTION_DOWNLOAD_FILE))
			{
				String path = getCallRecordingPath();
				String filename = WebUtil.getParameter(request, PARAM_FILENAME);				
				
				File recording = new File(path + filename + EXT_MP3);
				if (recording.exists())
				{
					streamToResponse(request, response, recording);
					return;
				}
				else
				{
					log.warning("Could not find file " + recording.getAbsolutePath() + ". Error serving file.");
					setInfoMsg(request, "Could not find file, please download again.");
				}
			}
			
			// Send back to JSP
			forward(request, response, JSP_DEFAULT);
		}
	}
	
	private void forward(HttpServletRequest request, HttpServletResponse response, String url)
		throws ServletException, IOException
	{
		if (!url.startsWith("/")) url = "/" + url;
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
	}
	
	private static void setInfoMsg(HttpServletRequest request, String msg)
	{
		request.setAttribute(INFO_MSG, msg);
	}
	
	private static String getTodaysDate()
	{
		return getFormattedDate(new Date(System.currentTimeMillis()));
	}
	
	private static String stripLeadingZero(String s)
	{
		if (s.startsWith("0"))
			return s.substring(1, s.length());
		else
			return s;
	}
	
	private static String getFormattedDate(Date date)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);		
		return sdf.format(date);
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

	/**
	 * Gets page attributes needed for other requests
	 * 
	 * @return Page attributes
	 */
	private static HashMap<String, String> getPageAttributes(String url, HashMap<String, String> cookieData, HashMap<String, String> parameters)
	{
		HashMap<String, String> data = new HashMap<String, String>();
		HttpClient client = new HttpClient();
		
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
	private static HashMap<String, String> extractCookieData(Cookie[] cookies)
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
	
	/**
	 * Login to 2talk account portal
	 * 
	 * @param username
	 * @param password
	 * @param pageAttributes
	 * @return Cookie data if login successful
	 */
	private static HashMap<String, String> loginToAccount(String username, String password)
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
	 * Search 2talk call records and return recording Id
	 * 
	 * @param cookieData
	 * @param pageAttributes
	 * @param destinationNumber
	 * @param originNumber
	 * @param date
	 * @return call records
	 */
	private static ArrayList<CallRecord> searchCalls(HashMap<String, String> cookieData, String destinationNumber, String originNumber, String date)
	{
		ArrayList<CallRecord> callRecords = new ArrayList<CallRecord>();
		
		HttpState initialState = new HttpState();
		Cookie aspSessionId = new Cookie(DOMAIN, COOKIE_ASPNET_SESSION_ID, cookieData.get(COOKIE_ASPNET_SESSION_ID), "/", null, false);
		initialState.addCookie(aspSessionId);
		Cookie visibillAuth = new Cookie(DOMAIN, COOKIE_VISIBILL_ASPXAUTH, cookieData.get(COOKIE_VISIBILL_ASPXAUTH), "/", null, false);
		initialState.addCookie(visibillAuth);

		HttpClient client = new HttpClient();
		client.setState(initialState);
		
		PostMethod postSearch = null;

		// Get billing period
		String billingPeriod = getBillingPeriod(date);
		
		// Get initial page attributes
		HashMap<String, String> pageAttributes = getPageAttributes(HTTP_SEARCH_URL, cookieData, null);
		
		// Today
		String today = stripLeadingZero(getTodaysDate() + TIME_12AM);
		
		// Create billing period field value
		String billingPeriodFieldValue = "";
		HashMap<String, String> getPageAttribsParameters = null;
		if (billingPeriod != null && billingPeriod.length() > 0)
		{
			billingPeriodFieldValue = billingPeriod + TIME_12AM;
			
			getPageAttribsParameters = new HashMap<String, String>();
			getPageAttribsParameters.put(CTL00_SM_HIDDENFIELD_NAME, CTL00_SM_HIDDENFIELD_VALUE);
			getPageAttribsParameters.put(EVENTTARGET, "ctl00$Navigation1$ddlBillingPeriod");
			getPageAttribsParameters.put(EVENTARGUMENT , pageAttributes.get(EVENTARGUMENT));
			getPageAttribsParameters.put(LASTFOCUS , pageAttributes.get(LASTFOCUS));
			getPageAttribsParameters.put(VIEWSTATE, pageAttributes.get(VIEWSTATE));
			getPageAttribsParameters.put(EVENTVALIDATION, pageAttributes.get(EVENTVALIDATION));
			getPageAttribsParameters.put("ctl00$Navigation1$ddlBillingPeriod", billingPeriodFieldValue); 
			getPageAttribsParameters.put("txtListenID", "");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$txtSeconds", "");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$txtDestination", "");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$txtOrigin", "");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbType", "");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbFrom", today);
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbTo", today);
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$txtCharge", "");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbBillingGroup", "");
			
			// Get page attributes simulating reload of page to change billing period
			pageAttributes = getPageAttributes(HTTP_SEARCH_URL, cookieData, getPageAttribsParameters);
		}

		try
		{
			postSearch = new PostMethod(HTTP_SEARCH_URL);
			postSearch.addParameter(CTL00_SM_HIDDENFIELD_NAME, CTL00_SM_HIDDENFIELD_VALUE);
			postSearch.addParameter(EVENTTARGET , pageAttributes.get(EVENTTARGET));
			postSearch.addParameter(EVENTARGUMENT , pageAttributes.get(EVENTARGUMENT));
			postSearch.addParameter(LASTFOCUS , pageAttributes.get(LASTFOCUS));
			postSearch.addParameter(VIEWSTATE, pageAttributes.get(VIEWSTATE));
			postSearch.addParameter(EVENTVALIDATION, pageAttributes.get(EVENTVALIDATION));
			postSearch.addParameter("ctl00$Navigation1$ddlBillingPeriod", ""); // Always empty (either to indicated current billing period or empty once alternative billing period selected)
			postSearch.addParameter("txtListenID", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$txtSeconds", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$txtDestination", destinationNumber);
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$txtOrigin", originNumber);
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbType", "");
			
			// If current billing period
			if (billingPeriodFieldValue.equals(""))
			{
				postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbFrom", stripLeadingZero(date));
				postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbTo", stripLeadingZero(date));
			}
			else 
			{
				SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);	
				
				Date callDate = null;
				try
				{
					callDate = sdf.parse(date);
				}
				catch (ParseException ex)
				{
					log.severe("Failed to parse call date");
					return callRecords;
				}
				
				Calendar callDateCalendar = GregorianCalendar.getInstance();
				callDateCalendar.setTime(callDate);
				
				String[] monthName = {"Jan", "Feb",
			            "Mar", "Apr", "May", "Jun", "Jul",
			            "Aug", "Sep", "Oct", "Nov",
			            "Dec"
			        };
				String dateDay = Integer.toString(callDateCalendar.get(Calendar.DATE));
				if (dateDay.length() == 1)
					dateDay = "0" + dateDay;
				String monthYear = monthName[callDateCalendar.get(Calendar.MONTH)] + " " + callDateCalendar.get(Calendar.YEAR);
				
				Date billingPeriodDate = null;
				try
				{
					billingPeriodDate = sdf.parse(billingPeriod);
				}
				catch (ParseException ex)
				{
					log.severe("Failed to parse billing period date" + ex);
					return callRecords;
				}				
				
				Calendar billingPeriodCalendar = GregorianCalendar.getInstance();
				billingPeriodCalendar.setTime(billingPeriodDate);
				
				//
				billingPeriodCalendar.add(Calendar.MONTH, -1);
				String fromDate = getFormattedDate(billingPeriodCalendar.getTime()) + TIME_12AM;
				String toDate = billingPeriodFieldValue;
				
				postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbFrom", fromDate);
				postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbTo", toDate);
				postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbTheDateDay", dateDay);
				postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbTheDateMontYear", monthYear);
			}							
			
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$txtCharge", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbBillingGroup", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$chkRecordedOnly", "");
			postSearch.addParameter("ctl00$plhContent$cmdSearch", "Submit");
			
			// Send request
			int returnCode = client.executeMethod(postSearch);
			if (returnCode == HttpStatus.SC_OK)
			{		
				String res = postSearch.getResponseBodyAsString();				
				
				// Extract table containing call records
				String resultTableExpr = "<table.*?id=\"ctl00_plhContent_Searchdata1_gv\"[^>]*>(.*?)</table>";
				String tableContents = "";
				Pattern patt = Pattern.compile(resultTableExpr, Pattern.DOTALL | Pattern.UNIX_LINES);
				Matcher m = patt.matcher(res);
				if (m.find()) 
					tableContents = m.group(1);
			
				// Extract table rows
				ArrayList<String> tableRows = new ArrayList<String>();
				String tableRowsExpr = ".*?<tr\\s+class=\"[^>]*>(.*?)</tr>.*?";
				patt = Pattern.compile(tableRowsExpr, Pattern.DOTALL | Pattern.UNIX_LINES);
				m = patt.matcher(tableContents);
				while (m.find())
					tableRows.add(m.group(1));
				
				// Extract fields from each row
				for (String row : tableRows)
				{								
					String rowFieldsExpr = "<td>(.*?)</td>";
					patt = Pattern.compile(rowFieldsExpr, Pattern.DOTALL | Pattern.UNIX_LINES);
					m = patt.matcher(row);
					
					ArrayList<String> fields = new ArrayList<String>();
					while (m.find())
						fields.add(m.group(1));
					
					// When status field is present
					if (fields.size() == NUMBER_OF_COLUMNS)
					{	
						CallRecord callRecord = new CallRecord();
						
						callRecord.setOriginNumber(fields.get(0));
						callRecord.setDestinationNumber(fields.get(1));
						callRecord.setDescription(fields.get(2));
						callRecord.setStatus(fields.get(3));
						callRecord.setDate(fields.get(4));
						callRecord.setTime(fields.get(5));
						callRecord.setDuration(fields.get(6));
						callRecord.setCharge(fields.get(7));
						
						// Extract Listen Id
						String listenIdExpr = "<a\\s+href='javascript:listen\\(([0-9]*)\\)'>";
						patt = Pattern.compile(listenIdExpr, Pattern.DOTALL | Pattern.UNIX_LINES);
						m = patt.matcher(fields.get(8));
						if (m.find())
							callRecord.setListenId(m.group(1));

						// Commented out to allow for calls which haven't been recorded
//						else
//						{
//							log.severe("Could not find listenId in call record table row, debug.");
//							break;
//						}
						
						callRecords.add(callRecord);
					}
					// When no status field is present
					else if (fields.size() == NUMBER_OF_COLUMNS - 1)
					{
						CallRecord callRecord = new CallRecord();
						
						callRecord.setOriginNumber(fields.get(0));
						callRecord.setDestinationNumber(fields.get(1));
						callRecord.setDescription(fields.get(2));
//						callRecord.setStatus(fields.get(3));
						callRecord.setDate(fields.get(3));
						callRecord.setTime(fields.get(4));
						callRecord.setDuration(fields.get(5));
						callRecord.setCharge(fields.get(6));
						
						// Extract Listen Id
						String listenIdExpr = "<a\\s+href='javascript:listen\\(([0-9]*)\\)'>";
						patt = Pattern.compile(listenIdExpr, Pattern.DOTALL | Pattern.UNIX_LINES);
						m = patt.matcher(fields.get(7));
						if (m.find())
							callRecord.setListenId(m.group(1));
						
						// Commented out to allow for calls which haven't been recorded
//						else
//						{
//							log.severe("Could not find listenId in call record table row, debug.");
//							break;
//						}
						
						callRecords.add(callRecord);
					}
					else
					{
						log.severe("Number of columns did not match, debug");
						break;
					}
				}
			}
			else 
			{
				log.severe("Search for calls failed, debug");
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
		
		return callRecords;
	}
	
	/**
	 * Get call recording 
	 * 
	 * @param cookieData
	 * @param listenId
	 * @param destinationNumber
	 * @param originNumber
	 */
	private static File getCallRecording(HashMap<String, String> cookieData, String listenId, String destinationNumber, String originNumber)
	{
		HttpState initialState = new HttpState();
		Cookie aspSessionId = new Cookie("account.2talk.co.nz", COOKIE_ASPNET_SESSION_ID, cookieData.get(COOKIE_ASPNET_SESSION_ID), "/", null, false); // TODO: constant
		initialState.addCookie(aspSessionId);
		Cookie visibillAuth = new Cookie("account.2talk.co.nz", COOKIE_VISIBILL_ASPXAUTH, cookieData.get(COOKIE_VISIBILL_ASPXAUTH), "/", null, false);
		initialState.addCookie(visibillAuth);

		HttpClient client = new HttpClient();
		client.setState(initialState);

		PostMethod postSearch = null;
		
		// Get page attributes
		HashMap<String, String> pageAttributes = getPageAttributes(HTTP_SEARCH_URL, cookieData, null);
		
		// Today
		String today = getTodaysDate() + TIME_12AM;
		
		try
		{
			postSearch = new PostMethod(HTTP_SEARCH_URL);
			postSearch.addParameter(CTL00_SM_HIDDENFIELD_NAME, CTL00_SM_HIDDENFIELD_VALUE);
			postSearch.addParameter(EVENTTARGET , "ctl00$plhContent$Searchdata1$cmdListen");
			postSearch.addParameter(EVENTARGUMENT , pageAttributes.get(EVENTARGUMENT));
			postSearch.addParameter(LASTFOCUS , pageAttributes.get(LASTFOCUS));
			postSearch.addParameter(VIEWSTATE, pageAttributes.get(VIEWSTATE));
			postSearch.addParameter(EVENTVALIDATION, pageAttributes.get(EVENTVALIDATION));
			postSearch.addParameter("ctl00$Navigation1$ddlBillingPeriod", "");
			postSearch.addParameter("txtListenID", listenId);
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$txtSeconds", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$txtDestination", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$txtOrigin", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbType", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbFrom", today); 
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbTo", today); 
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$txtCharge", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbBillingGroup", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$chkRecordedOnly", "");

			// Send request
			int returnCode = client.executeMethod(postSearch);			
			if (returnCode == HttpStatus.SC_OK)
			{		
				// Check mp3 is returned
				Header type = postSearch.getResponseHeader(CONTENT_TYPE_NAME); 
				if (type != null && type.getValue() != null && type.getValue().equals(CONTENT_TYPE_AUDIO_MP3)) 
				{
					InputStream in = null;
					OutputStream out = null;
					
					try
					{
						File recording = new File(listenId + EXT_MP3); 
						in = postSearch.getResponseBodyAsStream();
						out = new FileOutputStream(recording);
					    
				        // Transfer bytes from in to out
				        byte[] buf = new byte[1024];
				        int len;
				        while ((len = in.read(buf)) > 0) 
				        {
				            out.write(buf, 0, len);
				        }
				        
				        return recording;
					}
					catch (Exception ex)
					{
						log.severe("Error streaming to file, debug.");
					}
					finally 
					{
				        if (in != null) in.close();
				        if (out != null) out.close();
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
	
	/**
	 * Streams a file to response
	 * 
	 * @param request
	 * @param response
	 * @param file
	 */
	private static void streamToResponse(HttpServletRequest request, HttpServletResponse response, File file)
	{
		if (file == null || !file.exists()) 
		{
			log.warning("File does not exist - " + file);
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
				
//				time = System.currentTimeMillis() - time;
//				double speed = (totalSize/1024) / ((double)time/1000);
//				log.fine("Length=" 
//					+ totalSize + " - " 
//					+ time + " ms - " 
//					+ speed + " kB/sec");
			}
			catch (IOException ex)
			{
				log.log(Level.SEVERE, ex.toString());
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
					log.log(Level.SEVERE, ex.toString());
				}
				
				try
				{
					if (in != null) 
						in.close();
				}
				catch (IOException ex)
				{
					log.log(Level.SEVERE, ex.toString());
				}
			}
		}
	}
	
	/**
	 * 
	 * Note: Billing periods are from 14th to 13th (inclusive) e.g. "13 Jun 09" is 14/05/2009 to 13/06/2009
	 * @param sCallDate
	 * @return
	 */
	public static String getBillingPeriod(String sCallDate)
	{		
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);		
		Date callDate = null;
		try
		{
			callDate = sdf.parse(sCallDate);
		}
		catch (ParseException ex)
		{
			log.severe("Failed to parse call date");
			return "";
		}
		
		Calendar callCalendar = GregorianCalendar.getInstance();
		callCalendar.setTime(callDate);
		
		// Add a month
		if (callCalendar.get(Calendar.DATE) > BILLING_DAY_OF_MONTH)
			callCalendar.add(Calendar.MONTH, 1);		
		
		// Set day to billing date
		callCalendar.set(Calendar.DATE, BILLING_DAY_OF_MONTH);		
		
		String billingPeriod = sdf.format(callCalendar.getTime());
		
		Calendar todayCalendar = GregorianCalendar.getInstance();
		todayCalendar.setTimeInMillis(System.currentTimeMillis());
		
		// Check if is in current billing period
		if (
				(
						(todayCalendar.get(Calendar.MONTH) == callCalendar.get(Calendar.MONTH)) && 
						(todayCalendar.get(Calendar.DATE) <= callCalendar.get(Calendar.DATE))
				) 
				||  
				(
						((todayCalendar.get(Calendar.MONTH) + 1) == callCalendar.get(Calendar.MONTH)) &&
						 (todayCalendar.get(Calendar.DATE) > callCalendar.get(Calendar.DATE)) // TODO: Test test test
				)
			)
		{
			return "";
		}
		
		return billingPeriod;
	}
	
	/**
	 * Check if logged in
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
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
	}	// isLoggedIn
	
	private static String getCallRecordingPath()
	{
		String callRecordingPath = CALL_RECORDING_DIR + File.separator;
		
		// Check exists, if not then create it
		File callRecordingDir = new File(callRecordingPath);
		if(!callRecordingDir.isDirectory())
		{
			// If cannot make directory use tomcat temp dir
			if (!callRecordingDir.mkdir())
				callRecordingPath = TOMCAT_TMP_DIR + File.separator;
		}
			
		return callRecordingPath;
	}
	
	private static void enableDebugLogging()
	{
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.content", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
	}

	public static void main(String[] args)
	{
	
		HashMap<String, String> cookieData = loginToAccount(ACCOUNT_USERNAME, ACCOUNT_PASSWORD);
		
//		ArrayList<CallRecord> callRecords = oldCallRecordingServlet.searchCalls(cookieData, "6421404921", "6499744530", "19/11/2009");
//		for (CallRecord c : callRecords)
//			System.out.println(c);
		
		// Result tracker
		boolean success = false;
		
		PostMethod postSearch = null;			
		try
		{
			// Create client and post method
			HttpState initialState = new HttpState();
			Cookie aspSessionId = new Cookie(DOMAIN, COOKIE_ASPNET_SESSION_ID, cookieData.get(COOKIE_ASPNET_SESSION_ID), "/", null, false); 
			initialState.addCookie(aspSessionId);
			Cookie visibillAuth = new Cookie(DOMAIN, COOKIE_VISIBILL_ASPXAUTH, cookieData.get(COOKIE_VISIBILL_ASPXAUTH), "/", null, false);
			initialState.addCookie(visibillAuth);
			
			HttpClient client = new HttpClient();
			client.setState(initialState);

			// Get page attributes
			HashMap<String, String> pageAttributes = oldCallRecordingServlet.getPageAttributes(HTTP_SEARCH_URL, cookieData, null);
			
			HashMap<String, String> getPageAttribsParameters = new HashMap<String, String>();
			getPageAttribsParameters.put(CTL00_SM_HIDDENFIELD_NAME, CTL00_SM_HIDDENFIELD_VALUE);
			getPageAttribsParameters.put(EVENTTARGET, "ctl00$Navigation1$ddlBillingPeriod");
			getPageAttribsParameters.put(EVENTARGUMENT , pageAttributes.get(EVENTARGUMENT));
			getPageAttribsParameters.put(LASTFOCUS , pageAttributes.get(LASTFOCUS));
			getPageAttribsParameters.put(VIEWSTATE, pageAttributes.get(VIEWSTATE));
			getPageAttribsParameters.put(EVENTVALIDATION, pageAttributes.get(EVENTVALIDATION));
			getPageAttribsParameters.put("ctl00$Navigation1$ddlBillingPeriod", "13/12/2009 12:00:00 a.m."); 
			getPageAttribsParameters.put("txtListenID", "");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$txtSeconds", "");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$txtDestination", "");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$txtOrigin", "");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbType", "");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbFrom", "8/02/2010 12:00:00 a.m.");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbTo", "8/02/2010 12:00:00 a.m.");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$txtCharge", "");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbBillingGroup", "");
			
			// Get page attributes simulating reload of page to change billing period
//			pageAttributes = getPageAttributes(HTTP_SEARCH_URL, cookieData, getPageAttribsParameters);
			
			getPageAttribsParameters = new HashMap<String, String>();
			getPageAttribsParameters.put(CTL00_SM_HIDDENFIELD_NAME, CTL00_SM_HIDDENFIELD_VALUE);
			getPageAttribsParameters.put(EVENTTARGET, pageAttributes.get(EVENTTARGET));
			getPageAttribsParameters.put(EVENTARGUMENT , pageAttributes.get(EVENTARGUMENT));
			getPageAttribsParameters.put(LASTFOCUS , pageAttributes.get(LASTFOCUS));
			getPageAttribsParameters.put(VIEWSTATE, pageAttributes.get(VIEWSTATE));
			getPageAttribsParameters.put(EVENTVALIDATION, pageAttributes.get(EVENTVALIDATION));
//			getPageAttribsParameters.put("ctl00$Navigation1$ddlBillingPeriod", ""); 
//			getPageAttribsParameters.put("txtListenID", "");
//			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$txtSeconds", "");
//			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$txtDestination", "");//"6421404921");
//			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$txtOrigin", "");//"6499744530");
//			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbType", "");
//			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbFrom", "8/02/2010 12:00:00 a.m.");//"13/11/2009 12:00:00 a.m.");
//			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbTo", "8/02/2010 12:00:00 a.m.");//"13/12/2009 12:00:00 a.m.");
////			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbTheDateDay", "20");
////			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbTheDateMontYear", "Nov 2009");
//			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$txtCharge", "");
//			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbBillingGroup", "");
//			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$chkRecordedOnly", "");
			getPageAttribsParameters.put("ctl00$plhContent$cmdSearch", "Submit");
			
			// Get page attributes simulating reload of page to search for record
			pageAttributes = getPageAttributes(HTTP_SEARCH_URL, cookieData, getPageAttribsParameters);

			// Today
			String today = getTodaysDate() + TIME_12AM;
			
			// Create post search
			postSearch = new PostMethod(HTTP_SEARCH_URL);
			postSearch.addParameter(CTL00_SM_HIDDENFIELD_NAME, CTL00_SM_HIDDENFIELD_VALUE);
			postSearch.addParameter(EVENTTARGET , "ctl00$plhContent$Searchdata1$cmdListen");
			postSearch.addParameter(EVENTARGUMENT , pageAttributes.get(EVENTARGUMENT));
			postSearch.addParameter(LASTFOCUS , pageAttributes.get(LASTFOCUS));
			postSearch.addParameter(VIEWSTATE, pageAttributes.get(VIEWSTATE));//"/wEPDwUKMTYyNDkyMTk2Ng9kFgJmD2QWAgIDD2QWCAILD2QWBgIBDxYCHgdWaXNpYmxlZ2QCBQ8WAh8AZ2QCBw9kFgJmD2QWAmYPZBYCZg8WAh4EaHJlZgUSL2Zvcm1zL3NlYXJjaC5hc3B4ZAIND2QWAgIFDxYCHwBoFgJmD2QWAgIHDw8WBB4EVGV4dGUfAGhkZAIPD2QWBgIBDw8WAh8CBQZTZWFyY2hkZAIDDw8WAh8CBRIgLSBDYW1lcm9uIEJlYXR0aWVkZAIFD2QWAgIBDw8WAh8CBQo5NS0wODctNjQ3ZGQCEQ9kFgQCAQ8PFgIeDUN1cnJlbnRTZWFyY2gy1QQAAQAAAP////8BAAAAAAAAAAwCAAAAS0FjY291bnRzLkFwcGxpY2F0aW9uLCBWZXJzaW9uPTEuMC4wLjAsIEN1bHR1cmU9bmV1dHJhbCwgUHVibGljS2V5VG9rZW49bnVsbAUBAAAAI0FjY291bnRzLkFwcGxpY2F0aW9uLlNlYXJjaENyaXRlcmlhDQAAAAx0eXBlT2ZTZWFyY2gGdGFyZ2V0B3NlY29uZHMLZGVzdGluYXRpb24Gb3JpZ2luB3N1YlR5cGUIZnJvbURhdGUGdG9EYXRlB3RoZURhdGUGY2hhcmdlDGJpbGxpbmdHcm91cAlzbWFydENvZGUMcmVjb3JkZWRPbmx5BAQAAQEBAAAAAAEBAB9BY2NvdW50cy5BcHBsaWNhdGlvbi5TZWFyY2hUeXBlAgAAACRBY2NvdW50cy5BcHBsaWNhdGlvbi5Eb3dubG9hZEZvcm1hdHMCAAAACA0NDQUBAgAAAAX9////H0FjY291bnRzLkFwcGxpY2F0aW9uLlNlYXJjaFR5cGUBAAAAB3ZhbHVlX18ACAIAAAACAAAABfz///8kQWNjb3VudHMuQXBwbGljYXRpb24uRG93bmxvYWRGb3JtYXRzAQAAAAd2YWx1ZV9fAAgCAAAACAAAAAAAAAAGBQAAAAo2NDIxNDA0OTIxBgYAAAAKNjQ5OTc0NDUzMAYHAAAAAABAp2ALMswIAMALWZ5JzAgAwCFfwjbMCB4tNzkyMjgxNjI1MTQyNjQzMzc1OTM1NDM5NTAzMzUJBwAAAAkHAAAAAAtkZAIDDw8WAh4OU2VhcmNoQ3JpdGVyaWEy1QQAAQAAAP////8BAAAAAAAAAAwCAAAAS0FjY291bnRzLkFwcGxpY2F0aW9uLCBWZXJzaW9uPTEuMC4wLjAsIEN1bHR1cmU9bmV1dHJhbCwgUHVibGljS2V5VG9rZW49bnVsbAUBAAAAI0FjY291bnRzLkFwcGxpY2F0aW9uLlNlYXJjaENyaXRlcmlhDQAAAAx0eXBlT2ZTZWFyY2gGdGFyZ2V0B3NlY29uZHMLZGVzdGluYXRpb24Gb3JpZ2luB3N1YlR5cGUIZnJvbURhdGUGdG9EYXRlB3RoZURhdGUGY2hhcmdlDGJpbGxpbmdHcm91cAlzbWFydENvZGUMcmVjb3JkZWRPbmx5BAQAAQEBAAAAAAEBAB9BY2NvdW50cy5BcHBsaWNhdGlvbi5TZWFyY2hUeXBlAgAAACRBY2NvdW50cy5BcHBsaWNhdGlvbi5Eb3dubG9hZEZvcm1hdHMCAAAACA0NDQUBAgAAAAX9////H0FjY291bnRzLkFwcGxpY2F0aW9uLlNlYXJjaFR5cGUBAAAAB3ZhbHVlX18ACAIAAAACAAAABfz///8kQWNjb3VudHMuQXBwbGljYXRpb24uRG93bmxvYWRGb3JtYXRzAQAAAAd2YWx1ZV9fAAgCAAAACAAAAAAAAAAGBQAAAAo2NDIxNDA0OTIxBgYAAAAKNjQ5OTc0NDUzMAYHAAAAAABAp2ALMswIAMALWZ5JzAgAwCFfwjbMCB4tNzkyMjgxNjI1MTQyNjQzMzc1OTM1NDM5NTAzMzUJBwAAAAkHAAAAAAtkFgJmD2QWAmYPZBYUAgEPZBYCZg9kFgICAQ8PFgIfAgUcQ2FsbHMgbG9uZ2VyIHRoYW4gKHNlY29uZHMpOmRkAgMPZBYCZg9kFgICAQ8PFgIfAgUOQ2FsbHMgbWFkZSB0bzpkZAIFD2QWAmYPZBYCAgEPDxYCHwIFEENhbGxzIG1hZGUgZnJvbTpkZAIHD2QWBGYPZBYCAgEPDxYCHwIFCkNhbGwgdHlwZTpkZAIBD2QWAgIBDxAPFgYeDURhdGFUZXh0RmllbGQFCHR5cGVkZXNjHg5EYXRhVmFsdWVGaWVsZAUEdHlwZR4LXyFEYXRhQm91bmRnZBAVDQAUU2VydmljZSBQbGFuIENoYXJnZXMOTmF0aW9uYWwgQ2FsbHMQTmV0d29yayBTZXJ2aWNlcwtNaXNjIERlYml0cxRQ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ODAzNTI1CTA5MjgwMzg1OAkwOTI4MDM4NTkJMDkyODAzODYwCTA5MjgwMzg2MQkwOTI4MDM4NjIJMDkyODAzODYzCTA5MjgwMzk0MgkwOTI4MDM5NDMJMDkyODAzOTQ0CTA5MjgwNDQwMAkwOTI4MDQ0MDEJMDkyODA0NDAyCTA5MjgwNDQwMwkwOTI4MDQ0NTUJMDkyODA0NDg1CTA5MjgwNDQ4NgkwOTI4MDQ0ODcJMDkyODA0NDg4CTA5MjgwNDQ4OQkwOTI4MDQ3MjcJMDkzMDIyMDkyCTA5MzAzMjMxNgkwOTMwNzc4NjAJMDkzMDc3OTU1CTA5MzU5OTA2OAkwOTM2MDk1NDYJMDkzNjIwMjAyCTA5MzYyMDgyMgkwOTM2Mzg4ODgJMDkzNzYyNzMwCTA5Mzc2NTYwOAkwOTM3NjczMDAJMDkzNzcyMjI3CTA5Mzc3NzUwMAkwOTM3Nzc4ODEJMDkzNzg0ODQ4CTA5Mzc4NjI5OQkwOTM3ODc1ODAJMDk0MTgxMzQ3CTA5NDQ1ODY2MAkwOTQ0NjA0ODYJMDk0ODY1MzE3CTA5NDg2NzAxNAkwOTUyMDUxMDAJMDk1MjMwMTg0CTA5NTIzMDgyMAkwOTUyOTQyNDYJMDk2MjMxODQ4CTA5NjMwNjMxNwkwOTYzMDkyMDgJMDk2MzA5MjA5CTA5NjM2Mzc5MwkwOTYzNjM3OTYJMDk2MzYzOTMzCTA5OTE0NzExOQkwOTkxNTA1MDAJMDk5MTUwNTAxCTA5OTE1MDUwMwkwOTkxNTA1MDgJMDk5MTUwNTQwCTA5OTE1MDU0NwkwOTkxNTA1NDgJMDk5MTUyNTM2CTA5OTE2NjE2NgkwOTkxNjYxNjgJMDk5MjE1NTI5CTA5OTIxNTgzMgkwOTkyMTU4MzMJMDk5NTA0OTQ0CTA5OTUwNzk0NQkwOTk1MDc5NDcJMDk5NTA3OTQ4CTA5OTY4NDUwMAkwOTk2ODQ1MDEJMDk5Njg0NTAyCTA5OTY4NDUwMwkwOTk2ODQ1MDQJMDk5Njg0NTA1CTA5OTY5NTc2NQkwOTk3MzQyMDAJMDk5NzM0NDAwCTA5OTczNDc0MAkwOTk3MzQ3NDEJMDk5NzM0NzQyCTA5OTczNDc0MwkwOTk3MzQ3NDQJMDk5NzM0NzQ1CTA5OTczNDc0NgkwOTk3MzQ3NDcJMDk5NzM0NzQ4CTA5OTczNDc0OQkwOTk3MzQ3NTkJMDk5NzM0NzcwCTA5OTczNDc3MQkwOTk3MzQ3NzIJMDk5NzM0NzczCTA5OTczNDc3NAkwOTk3NDIwOTQJMDk5NzQyNjQzCTA5OTc0NDExMwkwOTk3NDQ1MzAJMDk5NzQ0NTMyCTA5OTc0NDU0MgkwOTk3NDQ1NzkJMDk5NzQ0NTgyCTA5OTc0NDYwOQkwOTk3NDkyNTEJMDk5NzQ5MjY2CTA5OTc0OTg5MQkwOTk3NDk4OTIJMDk5NzQ5ODkzCTA5OTg0MDkyMAs2MTI4MDExMTg5MRWkAQAACzAyODI1NTAzOTc1CTAyODg5MTM5OAkwMjg4OTEzOTkJMDI4ODkyNTAwCTAyODg5MjUwMQkwMjg4OTI1MDMJMDI4ODkyNTA0CTAyODg5MjUwNQkwMjg4OTI1MDYJMDI4ODkyNTA3CTAyODg5MjUwOAkwMjg4OTc0ODAJMDI4ODk3NDg1CTAyODg5NzQ4NgkwMzI4MTgxNzAJMDMyODE4MTcxCTAzMjgxODg0MAkwMzI4MTg4NDMJMDMyODE4ODQ0CTAzNDQxMjc3OQkwMzQ0MTgyMzAJMDM0NDI0NDIwCTAzNDQyOTA0MQkwMzk3NDMyOTcJMDM5NzQzMzQxCTAzOTc0NDY4MAkwMzk3NDYwODYJMDQyOTM2Njk5CTA0OTc0MDUyNQkwNDk3NDA1OTAJMDQ5NzQ0MzU0CTA0OTc0ODc3MAkwNDk3NDg3NzQJMDQ5NzQ5MjUwCTA3OTI5MjE3OQkwNzkyOTQ0NDUKMDgwMDEyNjYzMwowODAwMjcyNjM4CjA4MDA2MjI3NzQKMDgwMDg5MTMzMwowODAwODk0MTExCjA4MDA4OTQxNDQKMDgwMDg5NDE4OAowODAwODk0NTExCjA4MDA4OTUxODQKMDgwMDk1MDc5NAkwOTI3MTIxNDIJMDkyNzE0MDE4CTA5MjgwMzUyNQkwOTI4MDM4NTgJMDkyODAzODU5CTA5MjgwMzg2MAkwOTI4MDM4NjEJMDkyODAzODYyCTA5MjgwMzg2MwkwOTI4MDM5NDIJMDkyODAzOTQzCTA5MjgwMzk0NAkwOTI4MDQ0MDAJMDkyODA0NDAxCTA5MjgwNDQwMgkwOTI4MDQ0MDMJMDkyODA0NDU1CTA5MjgwNDQ4NQkwOTI4MDQ0ODYJMDkyODA0NDg3CTA5MjgwNDQ4OAkwOTI4MDQ0ODkJMDkyODA0NzI3CTA5MzAyMjA5MgkwOTMwMzIzMTYJMDkzMDc3ODYwCTA5MzA3Nzk1NQkwOTM1OTkwNjgJMDkzNjA5NTQ2CTA5MzYyMDIwMgkwOTM2MjA4MjIJMDkzNjM4ODg4CTA5Mzc2MjczMAkwOTM3NjU2MDgJMDkzNzY3MzAwCTA5Mzc3MjIyNwkwOTM3Nzc1MDAJMDkzNzc3ODgxCTA5Mzc4NDg0OAkwOTM3ODYyOTkJMDkzNzg3NTgwCTA5NDE4MTM0NwkwOTQ0NTg2NjAJMDk0NDYwNDg2CTA5NDg2NTMxNwkwOTQ4NjcwMTQJMDk1MjA1MTAwCTA5NTIzMDE4NAkwOTUyMzA4MjAJMDk1Mjk0MjQ2CTA5NjIzMTg0OAkwOTYzMDYzMTcJMDk2MzA5MjA4CTA5NjMwOTIwOQkwOTYzNjM3OTMJMDk2MzYzNzk2CTA5NjM2MzkzMwkwOTkxNDcxMTkJMDk5MTUwNTAwCTA5OTE1MDUwMQkwOTkxNTA1MDMJMDk5MTUwNTA4CTA5OTE1MDU0MAkwOTkxNTA1NDcJMDk5MTUwNTQ4CTA5OTE1MjUzNgkwOTkxNjYxNjYJMDk5MTY2MTY4CTA5OTIxNTUyOQkwOTkyMTU4MzIJMDk5MjE1ODMzCTA5OTUwNDk0NAkwOTk1MDc5NDUJMDk5NTA3OTQ3CTA5OTUwNzk0OAkwOTk2ODQ1MDAJMDk5Njg0NTAxCTA5OTY4NDUwMgkwOTk2ODQ1MDMJMDk5Njg0NTA0CTA5OTY4NDUwNQkwOTk2OTU3NjUJMDk5NzM0MjAwCTA5OTczNDQwMAkwOTk3MzQ3NDAJMDk5NzM0NzQxCTA5OTczNDc0MgkwOTk3MzQ3NDMJMDk5NzM0NzQ0CTA5OTczNDc0NQkwOTk3MzQ3NDYJMDk5NzM0NzQ3CTA5OTczNDc0OAkwOTk3MzQ3NDkJMDk5NzM0NzU5CTA5OTczNDc3MAkwOTk3MzQ3NzEJMDk5NzM0NzcyCTA5OTczNDc3MwkwOTk3MzQ3NzQJMDk5NzQyMDk0CTA5OTc0MjY0MwkwOTk3NDQxMTMJMDk5NzQ0NTMwCTA5OTc0NDUzMgkwOTk3NDQ1NDIJMDk5NzQ0NTc5CTA5OTc0NDU4MgkwOTk3NDQ2MDkJMDk5NzQ5MjUxCTA5OTc0OTI2NgkwOTk3NDk4OTEJMDk5NzQ5ODkyCTA5OTc0OTg5MwkwOTk4NDA5MjALNjEyODAxMTE4OTEUKwOkAWdnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZGQCEw8WAh8AaBYEZg9kFgICAQ8PFgIfAgUdQ2FsbHMgZm9yIHRoZSBTbWFydENvZGUgVXNlcjpkZAIBD2QWAgIBDxBkEBUBABUBABQrAwFnFgFmZBgDBR5fX0NvbnRyb2xzUmVxdWlyZVBvc3RCYWNrS2V5X18WAQUwY3RsMDAkcGxoQ29udGVudCRTZWFyY2hjcml0ZXJpYTEkY2hrUmVjb3JkZWRPbmx5BSNjdGwwMCRwbGhDb250ZW50JFNlYXJjaGNyaXRlcmlhMSRtdg8PZGZkBR9jdGwwMCRwbGhDb250ZW50JFNlYXJjaGRhdGExJGd2DzwrAAoBCAIBZFbTKnSdBui4vNWFnDX8WvIXTrWy");//pageAttributes.get(VIEWSTATE));
			postSearch.addParameter(EVENTVALIDATION, pageAttributes.get(EVENTVALIDATION));//"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");//pageAttributes.get(EVENTVALIDATION));
//			postSearch.addParameter("ctl00$Navigation1$ddlBillingPeriod", ""); // empty
			postSearch.addParameter("txtListenID", "51643155");
//			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$txtSeconds", "");
//			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$txtDestination", "");//"6421404921");
//			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$txtOrigin", "");//"6499744530");
//			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbType", "");
//			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbFrom", "");
//			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbTo", "");
////			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbTheDateDay", "19");
////			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbTheDateMontYear", "Nov 2009");
//			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$txtCharge", "");
//			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbBillingGroup", "");
//			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$chkRecordedOnly", "");

			// Send request
			int returnCode = client.executeMethod(postSearch);			
			if (returnCode == HttpStatus.SC_OK)
			{		
				// Check mp3 is returned
				Header type = postSearch.getResponseHeader(CONTENT_TYPE_NAME); 
				if (type != null && type.getValue() != null && type.getValue().equals(CONTENT_TYPE_AUDIO_MP3)) 
				{
					String path = getCallRecordingPath();
					
					// Create tmp file
					File tmpRecording = new File(path + "test" + EXT_TMP); 
					
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
						oldCallRecordingServlet.log.severe("Error streaming to file: " + ex);
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
						File recording = new File(path + "test" + EXT_MP3);
				        success = tmpRecording.renameTo(recording);
				        
				        if (!success)
				        {
				        	log.severe("Failed to rename recording from temporary name, debug (deleting recording(s))");
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
				log.severe("Error retrieving call recording from 2talk, debug.");
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
	}
	
	
	class DownloadRecordingWorker extends Thread
	{
		private static final int HASH_LENGTH = 16;
		
		private String originNumber;
		private String destinationNumber;
		private String date;
		private String listenId;
		private HashMap<String, String> cookieData;
		private String serverName;
		private String contextPath;
		private MStore webStore;
		private WebUser webUser;
		
		private String filename;
		private String hashId;
		
		public DownloadRecordingWorker(String originNumber, String destinationNumber, String date, String listenId, HashMap<String, String> cookieData, String serverName, String contextPath, MStore webStore, WebUser webUser)
		{
			this.originNumber = originNumber;
			this.destinationNumber = destinationNumber;
			this.date = date;
			this.listenId = listenId;
			this.cookieData = cookieData;
			this.serverName = serverName;
			this.contextPath = contextPath;
			this.webStore = webStore;
			this.webUser = webUser;
		}
		
		public void run()
		{
			boolean downloadSuccess = downloadRecording();			
			if (!downloadSuccess)
				log.severe("Failed to download call from 2talk [ListenId=" + listenId + ", OriginNumber=" + originNumber + ", DestinationNumber=" + destinationNumber + ", Date=" + date + "]");
			
			String emailSent = sendEmail(downloadSuccess);			
			if (emailSent == null || !emailSent.equals(EMail.SENT_OK))
			{
				log.severe("Failed to send email, debug");
			}
		}
		
		private String getHashId()
		{
			if (hashId == null)
			{
				Random rn = new Random();
				hashId = Long.toString(System.currentTimeMillis() * (1000 + rn.nextInt(9999))).substring(0, HASH_LENGTH);
			}
			
			return hashId;
		}
		
		private String stripDate()
		{
			return date.replace("/", "");
		}
		
		private String getFileURL()
		{
			return "https://" + serverName + "/" + NAME + "?" + PARAM_ACTION + "=" + ACTION_DOWNLOAD_FILE + "&" + PARAM_FILENAME + "=" + getFilename();
		}
		
		private String getFilename()
		{	
			if (filename == null)
				filename = originNumber + "-" + destinationNumber + "-" + stripDate() + "_" + getHashId();
			
			return filename;
		}
		
		private boolean downloadRecording()
		{	
			// Result tracker
			boolean success = false;
			
			PostMethod postSearch = null;			
			try
			{
				// Create client and post method
				HttpClient client = createHttpClient();
				postSearch = createPostSearch();

				// Send request
				int returnCode = client.executeMethod(postSearch);			
				if (returnCode == HttpStatus.SC_OK)
				{		
					// Check mp3 is returned
					Header type = postSearch.getResponseHeader(CONTENT_TYPE_NAME); 
					if (type != null && type.getValue() != null && type.getValue().equals(CONTENT_TYPE_AUDIO_MP3)) 
					{
						String path = getCallRecordingPath();
						
						// Create tmp file
						File tmpRecording = new File(path + getFilename() + EXT_TMP); 
						
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
							oldCallRecordingServlet.log.severe("Error streaming to file: " + ex);
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
							File recording = new File(path + getFilename() + EXT_MP3);
					        success = tmpRecording.renameTo(recording);
					        
					        if (!success)
					        {
					        	log.severe("Failed to rename recording from temporary name, debug (deleting recording(s))");
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
					log.severe("Error retrieving call recording from 2talk, debug.");
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
			
			return success;				
		}
		
		private HttpClient createHttpClient()
		{
			HttpState initialState = new HttpState();
			Cookie aspSessionId = new Cookie(DOMAIN, COOKIE_ASPNET_SESSION_ID, cookieData.get(COOKIE_ASPNET_SESSION_ID), "/", null, false); 
			initialState.addCookie(aspSessionId);
			Cookie visibillAuth = new Cookie(DOMAIN, COOKIE_VISIBILL_ASPXAUTH, cookieData.get(COOKIE_VISIBILL_ASPXAUTH), "/", null, false);
			initialState.addCookie(visibillAuth);
			
			HttpClient client = new HttpClient();
			client.setState(initialState);
			
			return client;
		}
			
		private PostMethod createPostSearch()
		{
			// Get page attributes
			HashMap<String, String> pageAttributes = oldCallRecordingServlet.getPageAttributes(HTTP_SEARCH_URL, cookieData, null);
			
			// Today
			String today = getTodaysDate() + TIME_12AM;
			
			// Create post search
			PostMethod postSearch = new PostMethod(HTTP_SEARCH_URL);
			postSearch.addParameter(CTL00_SM_HIDDENFIELD_NAME, CTL00_SM_HIDDENFIELD_VALUE);
			postSearch.addParameter(EVENTTARGET , "ctl00$plhContent$Searchdata1$cmdListen");
			postSearch.addParameter(EVENTARGUMENT , pageAttributes.get(EVENTARGUMENT));
			postSearch.addParameter(LASTFOCUS , pageAttributes.get(LASTFOCUS));
			postSearch.addParameter(VIEWSTATE, pageAttributes.get(VIEWSTATE));
			postSearch.addParameter(EVENTVALIDATION, pageAttributes.get(EVENTVALIDATION));
			postSearch.addParameter("ctl00$Navigation1$ddlBillingPeriod", "");
			postSearch.addParameter("txtListenID", listenId);
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$txtSeconds", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$txtDestination", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$txtOrigin", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbType", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbFrom", today); 
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbTo", today); 
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$txtCharge", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbBillingGroup", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$chkRecordedOnly", "");
			
			return postSearch;
		}
		
		private String sendEmail(boolean downloadSuccess)
		{
			MMailMsg mailMsg;
			
			if (downloadSuccess)
				mailMsg = webStore.getMailMsg(X_W_MailMsg.MAILMSGTYPE_CallRecordingDownload);
			else 	
				mailMsg = webStore.getMailMsg(X_W_MailMsg.MAILMSGTYPE_CallRecordingDownloadError);

			StringBuffer message = new StringBuffer();			
			String hdr = webStore.getEMailFooter();
			if (hdr != null && hdr.length() > 0)
				message.append(hdr).append("\n");
			
			// Append Dear <name>,
			message.append("Dear " + webUser.getName() + ",\n\n");			
			message.append(mailMsg.getMessage() + "\n\n");
			
			if (downloadSuccess)
				message.append(getFileURL());				
			
			String ftr = webStore.getEMailFooter();
			if (ftr != null && ftr.length() > 0)
				message.append(ftr);
			
			//	Create email
			EMail email = webStore.createEMail(webUser.getEmail(), mailMsg.getSubject(), message.toString(), false);		
			
			//	Send
			String retValue = email.send();
			
			//	Log
			MUserMail um = new MUserMail(mailMsg, webUser.getAD_User_ID(), email);
			um.save();
			
			return retValue;
		}
	}
}
