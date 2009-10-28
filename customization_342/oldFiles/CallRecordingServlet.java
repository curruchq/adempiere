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
import org.compiere.util.CLogger;
import org.compiere.util.WebEnv;
import org.compiere.util.WebSessionCtx;
import org.compiere.util.WebUser;
import org.compiere.util.WebUtil;

import com.conversant.db.SERConnector;
import com.conversant.model.CallRecord;
import com.conversant.model.SIPAccount;

public class CallRecordingServlet  extends HttpServlet
{
	// TODO: Handle when servlet's called without JSP first
	
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
	public static final String PARAM_ACTION = "action";
	public static final String ACTION_SEARCH = "search";
	public static final String ACTION_DOWNLOAD = "download";
	
	/** Number of columns in call recording table	*/
	private static final int NUMBER_OF_COLUMNS = 9;
	
	/** 2Talk billing day of month					*/
	private static final int BILLING_DAY_OF_MONTH = 13;
	
	/** Date format 								*/
	private static final String DATE_FORMAT = "dd/MM/yyyy";
	
	/** Info message identifier						*/
	public static final String INFO_MSG = "infoMsg";
	
	/** MP3 file extension							*/
	private static final String EXT_MP3 = ".mp3";
	
	/** */
	private static final String CTL00_SM_HIDDENFIELD_NAME = "ctl00_sm_HiddenField";
	
	/** */
	private static final String CTL00_SM_HIDDENFIELD_VALUE = ";;System.Web.Extensions, Version=3.5.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35:en-US:3bbfe379-348b-450d-86a7-bb22e53c1978:52817a7d:67c678a8;Telerik.Web.UI, Version=2008.2.826.35, Culture=neutral, PublicKeyToken=121fae78165ba3d4:en-US:2b1b618c-2ad2-4d4e-8b5a-afa3e17baf61:393f5085;Telerik.Web.UI, Version=2008.2.826.35, Culture=neutral, PublicKeyToken=121fae78165ba3d4:en-US:2b1b618c-2ad2-4d4e-8b5a-afa3e17baf61:34f9d57d";
	
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
								File recording = getCallRecording(cookieData, listenId, originNumber, destinationNumber);
								if (recording != null)
								{
									streamToResponse(request, response, recording);
									
									// delete temp file
									recording.delete();
									
									return; 
								}
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
//		String callDate = "24/06/2009 12:00:00 a.m.";
//		String originNumber = "6499744530";
//		String destinationNumber = "6494466548";
//		
//		HashMap<String, String> cookieData = loginToAccount(ACCOUNT_USERNAME, ACCOUNT_PASSWORD);
//		if (cookieData != null)
//		{
//			File recording = getCallRecording(cookieData, "29190316", originNumber, destinationNumber);
////			ArrayList<CallRecord> calls = searchCalls(cookieData, destinationNumber, originNumber, callDate);
////			for (CallRecord call : calls)
////			{
////				System.out.println(call.toString());
////			}
//		}
//		else
//		{
//			System.err.println("Failed to get cookie data");
//		}
	}
	
	private static ArrayList<CallRecord> test(HashMap<String, String> cookieData)
	{
		HttpState initialState = new HttpState();
		Cookie aspSessionId = new Cookie(DOMAIN, COOKIE_ASPNET_SESSION_ID, cookieData.get(COOKIE_ASPNET_SESSION_ID), "/", null, false);
		initialState.addCookie(aspSessionId);
		Cookie visibillAuth = new Cookie(DOMAIN, COOKIE_VISIBILL_ASPXAUTH, cookieData.get(COOKIE_VISIBILL_ASPXAUTH), "/", null, false);
		initialState.addCookie(visibillAuth);

		HttpClient client = new HttpClient();
		client.setState(initialState);
		
		PostMethod postSearch = null;

		try
		{		
			// Get initial page attributes
			HashMap<String, String> pageAttributes = getPageAttributes(HTTP_SEARCH_URL, cookieData, null);
		
			System.out.println("__VIEWSTATE: " + pageAttributes.get("__VIEWSTATE").substring(0, 10));
			System.out.println("__EVENTVALIDATION: " + pageAttributes.get("__EVENTVALIDATION").substring(0, 10));
			
			// Create billing period field value
			HashMap<String, String> getPageAttribsParameters = new HashMap<String, String>();
			getPageAttribsParameters.put(CTL00_SM_HIDDENFIELD_NAME, CTL00_SM_HIDDENFIELD_VALUE);
			getPageAttribsParameters.put(EVENTTARGET, "ctl00$Navigation1$ddlBillingPeriod");
			getPageAttribsParameters.put(EVENTARGUMENT , pageAttributes.get(EVENTARGUMENT));
			getPageAttribsParameters.put(LASTFOCUS , pageAttributes.get(LASTFOCUS));
			getPageAttribsParameters.put(VIEWSTATE, pageAttributes.get(VIEWSTATE));
			getPageAttribsParameters.put(EVENTVALIDATION, pageAttributes.get(EVENTVALIDATION));
			getPageAttribsParameters.put("ctl00$Navigation1$ddlBillingPeriod", "13/07/2009 12:00:00 a.m.");
			getPageAttribsParameters.put("txtListenID", "");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$txtSeconds", "");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$txtDestination", "");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$txtOrigin", "");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbType", "");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbFrom", "15/07/2009 12:00:00 a.m.");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbTo", "14/07/2009 12:00:00 a.m.");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$txtCharge", "");
			getPageAttribsParameters.put("ctl00$plhContent$Searchcriteria1$cmbBillingGroup", "");

			// Get page attributes simulating reload of page to change billing period
			pageAttributes = getPageAttributes(HTTP_SEARCH_URL, cookieData, getPageAttribsParameters);
					
			System.out.println("__VIEWSTATE: " + pageAttributes.get("__VIEWSTATE").substring(0, 10));
			System.out.println("__EVENTVALIDATION: " + pageAttributes.get("__EVENTVALIDATION").substring(0, 10));
			
			postSearch = new PostMethod(HTTP_SEARCH_URL);
			postSearch.addParameter(CTL00_SM_HIDDENFIELD_NAME, CTL00_SM_HIDDENFIELD_VALUE);
			postSearch.addParameter("__EVENTTARGET", pageAttributes.get("__EVENTTARGET"));
			postSearch.addParameter("__EVENTARGUMENT", pageAttributes.get("__EVENTARGUMENT"));
			postSearch.addParameter("__LASTFOCUS", pageAttributes.get("__LASTFOCUS"));
			postSearch.addParameter("__VIEWSTATE", pageAttributes.get("__VIEWSTATE"));
			postSearch.addParameter("__EVENTVALIDATION", pageAttributes.get("__EVENTVALIDATION"));
			postSearch.addParameter("ctl00$Navigation1$ddlBillingPeriod", "");//"13/07/2009 12:00:00 a.m.");
			postSearch.addParameter("txtListenID", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$txtSeconds", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$txtDestination", "6494466548");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$txtOrigin", "6499744530");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbType", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbFrom", "13/07/2009 12:00:00 a.m.");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbTo", "13/07/2009 12:00:00 a.m.");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbTheDateDay", "13");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbTheDateMontYear", "Jul 2009");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$txtCharge", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$cmbBillingGroup", "");
			postSearch.addParameter("ctl00$plhContent$Searchcriteria1$chkRecordedOnly", "on");
			postSearch.addParameter("ctl00$plhContent$cmdSearch", "Submit");
		
			// Send request
			int returnCode = client.executeMethod(postSearch);
			System.out.println("Response Code: " + returnCode);
			System.out.println("Response Body: \n" + postSearch.getResponseBodyAsString());
		}
		catch (Exception ex)
		{
			System.err.println("Exception Raised: " + ex);
		}
		finally 
		{
			if (postSearch != null)
				postSearch.releaseConnection();
		}
		
		return null;
	}
}
