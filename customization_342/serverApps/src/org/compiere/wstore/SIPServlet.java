package org.compiere.wstore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.compiere.Adempiere;
import org.compiere.model.MBPBankAccount;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.WebEnv;
import org.compiere.util.WebSessionCtx;
import org.compiere.util.WebUser;
import org.compiere.util.WebUtil;

import com.conversant.db.AsteriskConnector;
import com.conversant.db.SERConnector;
import com.conversant.did.DIDConstants;
import com.conversant.model.SIPAccount;

public class SIPServlet extends HttpServlet
{
	/** Name 								*/
	public static final String NAME = "SIPServlet";
	
	/**	Request Parameters					*/
	public static final String PARAM_ACTION = "action";
	
	/** Logger								*/
	private static CLogger log = CLogger.getCLogger(SIPServlet.class);
	
	/** Info message identifier				*/
	public static final String INFO_MSG = "infoMsg";
	
	/** Invalid fields identifier			*/
	public static final String INVALID_FIELDS = "invalidFields";
	
	/** Default JSP							*/
	public static final String DEFAULT_JSP = "sipAccount.jsp";
	
	/** Create SIP Account JSP				*/
	public static final String CREATE_ACCOUNT_JSP = "createSIPAccount.jsp";
	
	/** Edit SIP Account JSP				*/
	public static final String EDIT_ACCOUNT_JSP = "editSIPAccount.jsp";
	
	/** */
	public static final String USR_PREF_CONVERSE_VOICE_ATTRIBUTE  = "ConverseVoice";
	
	/** */
	public static final String USR_PREF_COUT_ATTRIBUTE = "42001";
	
	/** */
	public static final String USR_PREF_ACTIVE = "Active";
	
	/** */
	public static final String USR_PREF_INACTIVE = "Inactive";
	
	/** User preference value for attribute type text e.g ConverseVoice	*/
	public static final String USR_PREF_ATTR_TYPE_TEXT = "0";
	
	/** User preference value for attribute type numberic e.g 42001 (out bound calling) */
	public static final String USR_PREF_ATTR_TYPE_NUMERIC = "2";
	
	// TODO: Remove all commented out references to didNumber
	
	/**
	 * 	Initialize global variables
	 *  @param config servlet configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WebEnv.initWeb(config))
			throw new ServletException("SIPServlet.init");
	} 	// init
	
	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "SIP Servlet";
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
		this.handleRequest(request, response);
	}	// doGet

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		log.info("Post from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		this.handleRequest(request, response);
	}	// doPost

	private void handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		// Get session and remove any existing header message
		HttpSession session = request.getSession(true);
		session.removeAttribute(WebSessionCtx.HDR_MESSAGE);
		
		Properties ctx = JSPEnv.getCtx(request);
		
		if (isLoggedIn(request, response))
		{
			String url = DEFAULT_JSP;
			
			// Load user
			WebUser wu = WebUser.get(request);
			
			// Process action
			String action = WebUtil.getParameter(request, PARAM_ACTION);
			
			// Catch action when isn't set or doesn't have a value
			if (action == null || action.equals(""))
			{
				// do nothing
			}
/********************************************************************************************************************************
			else if (action.equalsIgnoreCase("set"))
			{
				String username = WebUtil.getParameter(request, "username");
				String domain = WebUtil.getParameter(request, "domain");
				String didNumber = WebUtil.getParameter(request, "didNumber");
				
				boolean error = false;
				if (username == null || username.length() < 1)
				{
					error = true;
					log.warning("username parameter is null or empty, table should not contain any null or empty usernames");
				}
				
				if (domain == null || domain.length() < 1)
				{
					error = true;
					log.warning("domain parameter is null or empty, table should not contain any null or empty domains");
				}
				
				if (didNumber == null || didNumber.length() < 1)
				{
					error = true;
					log.warning("DID Number is null or empty");
				}
				
				if (!error)
				{
					if (DIDController.validDIDSubscription(ctx, wu.getC_BPartner_ID(), didNumber))
					{
						String address = username + "@" + domain;
						if (!DIDXService.updateSIPAddress(didNumber, address))
						{
							request.setAttribute(INFO_MSG, "Could not update SIP address, please try again");
							log.warning("Could not update SIP address with DIDX.net, DIDNumber=" + didNumber + ", address=" + address);
						}
						else
						{
							request.setAttribute(INFO_MSG, "Your SIP address has been updated successfully.");
						}
					}
					else
					{
						request.setAttribute(INFO_MSG, "You do not have an active subscription for " + didNumber);
						log.warning("SIP account was not created for " + wu.getName() + " as they do not have an active subscription for " + didNumber);
					}
				}
				else
				{
					request.setAttribute(INFO_MSG, "An error occured processing your request, please try again");
				}
				
				forward(request, response, url);
			}
***********************************************************************************************************************************/
			else if (action.equalsIgnoreCase("create") || action.equalsIgnoreCase("edit"))
			{
				String username = WebUtil.getParameter(request, "username");
//				String didNumber = WebUtil.getParameter(request, "didNumber");
				String domain = WebUtil.getParameter(request, SIPDomainListTag.DOMAIN_LIST_SELECT_NAME);
				String password = WebUtil.getParameter(request, "password");
				String confirm = WebUtil.getParameter(request, "confirm");
				String timezone = WebUtil.getParameter(request, SIPTimezoneListTag.TIMEZONE_LIST_SELECT_NAME);
//				String terms = WebUtil.getParameter(request, "terms");
				
				// remove white space
				if (username != null)
					username = stripWhiteSpace(username);
//				if (didNumber != null)
//					didNumber = stripWhiteSpace(didNumber);
				
				// remove @ symbol
				if (domain != null)
					domain = domain.replaceAll("@", "");
				
				if (validateFields(request, response, wu,/* didNumber,*/ username, domain, password, confirm, timezone, action.equalsIgnoreCase("edit")))
				{	
					if (action.equalsIgnoreCase("edit"))					
						editSIPAccount(request, response, ctx, wu, username, password, domain, timezone);
					else if (action.equalsIgnoreCase("create"))
						createSIPAccount(request, response, ctx, wu, /*didNumber,*/ username, password, domain, timezone);
				}
				
				// TODO: Allow user to specify call forwarding options
			}
		}
	}	// handleRequest
	
	public static boolean containsOnlyNumbers(String s) 
	{
        // It can't contain only numbers if it's null or empty...
        if (s == null || s.length() == 0)
            return false;
        
        for (int i = 0; i < s.length(); i++) 
        {
            if (!Character.isDigit(s.charAt(i)))
                return false;
        }
        
        return true;
    }

	public static String stripWhiteSpace(String s)
	{
		return s.replaceAll("\\s+", "");
	}
	
	private boolean validateFields(HttpServletRequest request, HttpServletResponse response, WebUser wu, 
									/*String didNumber,*/ String username, String domain,
									String password, String confirm, String timezone, boolean isEdit)
		throws ServletException, IOException
	{
		ArrayList<String> invalidFields = new ArrayList<String>();
		
		// validate fields
		if (username == null || username.length() < 1)
		{
			invalidFields.add("username");
		}
		
		if (domain == null || domain.length() < 1)
		{
			invalidFields.add(SIPDomainListTag.DOMAIN_LIST_SELECT_NAME);
		}
		
		// only check when not edit mode and username or domain haven't already been found invalid
		if (!isEdit && !invalidFields.contains("username") && !invalidFields.contains(SIPDomainListTag.DOMAIN_LIST_SELECT_NAME))
		{
			// Check account for username+domain combo doesn't already exist
			for (SIPAccount account : SERConnector.getSIPAccounts(wu.getC_BPartner_ID()))
			{
				if (account.getUsername().equals(username) && account.getDomain().equals(domain))
				{
					request.setAttribute(INFO_MSG, "An account already exists for that username/domain combination.");
					invalidFields.add("username");
					invalidFields.add(SIPDomainListTag.DOMAIN_LIST_SELECT_NAME);
					break;
				}
			}
		}

		if ((password == null || password.length() < 8 || password.length() > 16) && 
			(!isEdit || (isEdit && password != null && password.length() > 0)))
		{
			invalidFields.add("password");
		}
		
		if ((confirm == null || confirm.length() < 1 || (password != null && !confirm.equals(password))) && 
			(!isEdit || (isEdit && confirm != null && confirm.length() > 0)))
		{
			invalidFields.add("confirm");
		}
		
		if (timezone == null || timezone.length() < 1)
		{
			invalidFields.add(SIPTimezoneListTag.TIMEZONE_LIST_SELECT_NAME);
		}
		
//		if (terms == null || terms.length() < 1)
//		{
//			invalidFields.add("terms");
//		}
		
		// invalid fields found
		if (invalidFields.size() > 0)
		{
			String url = DEFAULT_JSP;
			
			// no valid domains left
			if (domain == null)
			{
				request.setAttribute(INFO_MSG, "There are no valid domains left");
			}
			else
			{
				request.setAttribute(INVALID_FIELDS, invalidFields);
				if (isEdit)
					url = "/" + EDIT_ACCOUNT_JSP;
				else
					url = "/" + CREATE_ACCOUNT_JSP;// + (didNumber != null && didNumber.length() > 0 ? "?didNumber=" + didNumber : "");
			}
			
			forward(request, response, url);
			return false;
		}
		
		return true;
	}

	protected static boolean createDefaultVoicemailAccount(WebUser wu, String didNumber)
	{
		String domain = DIDConstants.DEFAULT_SIP_DOMAIN;
	
		boolean retValue = AsteriskConnector.addVoicemailUser(Integer.toString(wu.getC_BPartner_ID()), wu.getBP_SearchKey(), didNumber, wu.getName(), wu.getEmail());		
		if (retValue)
		{
			retValue = AsteriskConnector.addVoicemailToDialPlan(didNumber, wu.getBP_SearchKey());
			if (retValue)
			{
				retValue = SERConnector.addVoicemailPreferences(Integer.toString(wu.getC_BPartner_ID()), didNumber, domain, wu.getBP_SearchKey());
				if (!retValue)
				{
					AsteriskConnector.removeVoicemailUser(Integer.toString(wu.getC_BPartner_ID()), wu.getBP_SearchKey(), didNumber, wu.getName(), wu.getEmail());	
					AsteriskConnector.removeVoicemailFromDialPlan(didNumber, wu.getBP_SearchKey());
				}
			}
			else
				AsteriskConnector.removeVoicemailUser(Integer.toString(wu.getC_BPartner_ID()), wu.getBP_SearchKey(), didNumber, wu.getName(), wu.getEmail());		
		}

		return retValue;
	}
	
	protected static boolean createDefaultSIPAccount(WebUser wu, String didNumber, boolean outboundActive)
	{
		boolean retValue = true;
		
		String password = DIDConstants.DEFAULT_SIP_PASSWORD;
		String domain = DIDConstants.DEFAULT_SIP_DOMAIN;
		String timezone = DIDConstants.DEFAULT_SIP_TIMEZONE;

		int subscriberId = SERConnector.addSIPAccount(didNumber, password, domain, wu.getFirstName(), wu.getLastName(), wu.getEmail(), timezone, Integer.toString(wu.getC_BPartner_ID()));
		if (subscriberId > -1)
		{
			retValue = SERConnector.addUserPreference(Integer.toString(wu.getC_BPartner_ID()), didNumber, domain, USR_PREF_CONVERSE_VOICE_ATTRIBUTE, USR_PREF_ACTIVE, USR_PREF_ATTR_TYPE_TEXT, Integer.toString(subscriberId));
			if (retValue)
			{
				String cOutValue = USR_PREF_INACTIVE;
				if (outboundActive)
					cOutValue = USR_PREF_ACTIVE;
				
				retValue = SERConnector.addUserPreference(Integer.toString(wu.getC_BPartner_ID()), didNumber, domain, USR_PREF_COUT_ATTRIBUTE, cOutValue, USR_PREF_ATTR_TYPE_NUMERIC, Integer.toString(subscriberId));
				if (!retValue)
				{
					SERConnector.removeUserPreference(Integer.toString(wu.getC_BPartner_ID()), didNumber, domain, USR_PREF_CONVERSE_VOICE_ATTRIBUTE, USR_PREF_ACTIVE, USR_PREF_ATTR_TYPE_TEXT, Integer.toString(subscriberId));
					SERConnector.removeSIPAccount(didNumber, domain, Integer.toString(wu.getC_BPartner_ID()));
					log.warning("Failed to set user preference '" + USR_PREF_COUT_ATTRIBUTE + "' to '" + cOutValue + "' where didNumber=" + didNumber + ", domain=" + domain + ", AD_User_ID=" + wu.getAD_User_ID() + ", C_BPartner_ID=" + wu.getC_BPartner_ID());
				}
			}
			else
			{
				SERConnector.removeSIPAccount(didNumber, domain, Integer.toString(wu.getC_BPartner_ID()));
				log.warning("Failed to set user preference '" + USR_PREF_CONVERSE_VOICE_ATTRIBUTE + "' to '" + USR_PREF_ACTIVE + "' where didNumber=" + didNumber + ", domain=" + domain + ", AD_User_ID=" + wu.getAD_User_ID() + ", C_BPartner_ID=" + wu.getC_BPartner_ID());
			}
		}	
		else
		{
			log.warning("Failed to add SIP account where didNumber=" + didNumber + ", domain=" + domain + ", AD_User_ID=" + wu.getAD_User_ID() + ", C_BPartner_ID=" + wu.getC_BPartner_ID());
			retValue = false;
		}
		
		return retValue;
	}
	
	protected static void updateSIPAccountCOut(WebUser wu, String didNumber, boolean active)
	{
		String cOutValue = USR_PREF_INACTIVE;
		if (active)
			cOutValue = USR_PREF_ACTIVE;
		
		String uuid = Integer.toString(wu.getC_BPartner_ID());
		if (!SERConnector.updateUserPreference(uuid, didNumber, USR_PREF_COUT_ATTRIBUTE, cOutValue))
		{
			log.warning("Failed to update user preference '" + USR_PREF_COUT_ATTRIBUTE + "' to '" + cOutValue + "'");
		}
	}
	
	private void createSIPAccount(HttpServletRequest request, HttpServletResponse response, Properties ctx, 
								WebUser wu, String username, String password, String domain, String timezone)
		throws ServletException, IOException
	{
		String url = DEFAULT_JSP;
		
		boolean validated = false;
		if (wu != null)
		{
			MBPBankAccount ba = wu.getBankAccount(true, true);
			if (ba != null)
				validated = ba.isBP_CC_Validated();				
		
		// create SIP account without assigning to DIDx.net DID
//		if (didNumber == null || didNumber.length() < 1)
//		{
			if (addSIPAccount(request, wu, validated, username, password, domain, timezone))
			{				
				String msg = "Your SIP account has been created.";
				if (!validated)
				{
					msg = msg + " Outbound calling is barred until your credit card has been validated. Please check your email for details.";
					CreditCardValidationServlet.sendValidationRequestEmail(request, wu);
				}
				
				request.setAttribute(INFO_MSG, msg);
				
				if (ba != null)
					DIDController.recordBarredDIDNumbers(validated, wu, username);
				else
					log.warning("Couldn't create bank account to store attachment on. The following SIP account is barred: UUID=" + wu.getC_BPartner_ID() + ", Username=" + username + ", Domain=" + domain);
				
			}
		}
//		}
//		else if (!containsOnlyNumbers(didNumber))
//		{
//			request.setAttribute(INFO_MSG, "Invalid DID - " + didNumber);
//			url = "/subscriptions.jsp";
//		}
//		else if (DIDController.validDIDSubscription(ctx, wu.getC_BPartner_ID(), didNumber))
//		{				
//			if (addSIPAccount(request, wu, username, password, domain, timezone))
//			{
//				String address = username + "@" + domain;
//				if (!DIDXService.updateSIPAddress(didNumber, address))
//				{
//					log.warning("Could not update SIP address with DIDx.net, address=" + address);
//				}
//				
//				request.setAttribute(INFO_MSG, "Your SIP account has been created and " + didNumber + " has been set up to use this account");
//			}
//		}
//		else
//		{
//			log.warning("SIP account not created for " + wu.getName() + " as they do not have an active subscription for " + didNumber);
//			request.setAttribute(INFO_MSG, "You do not have an active subscription for " + didNumber);
//			url = "/" + SubscriptionsTableTag.DEFAULT_JSP;
//		}
		
		forward(request, response, url);
	}
	
	private boolean addSIPAccount(HttpServletRequest request, WebUser wu, boolean outboundActive, String username, String password, String domain, String timezone)
	{
		int subscriberId = SERConnector.addSIPAccount(username, password, domain, wu.getFirstName(), wu.getLastName(), wu.getEmail(), timezone, Integer.toString(wu.getC_BPartner_ID()));
		if (subscriberId > -1)
		{
			if (!SERConnector.addUserPreference(Integer.toString(wu.getC_BPartner_ID()), username, domain, USR_PREF_CONVERSE_VOICE_ATTRIBUTE, USR_PREF_ACTIVE, USR_PREF_ATTR_TYPE_TEXT, Integer.toString(subscriberId)))
			{
				log.warning("Failed to set user preference '" + USR_PREF_CONVERSE_VOICE_ATTRIBUTE + "' to '" + USR_PREF_ACTIVE + "'");
			}
			
			String cOutValue = USR_PREF_INACTIVE;
			if (outboundActive)
				cOutValue = USR_PREF_ACTIVE;
			
			if (!SERConnector.addUserPreference(Integer.toString(wu.getC_BPartner_ID()), username, domain, USR_PREF_COUT_ATTRIBUTE, cOutValue, USR_PREF_ATTR_TYPE_NUMERIC, Integer.toString(subscriberId)))
			{
				log.warning("Failed to set user preference '" + USR_PREF_COUT_ATTRIBUTE + "' to '" + cOutValue + "'");
			}
			
			return true;
		}
		else
		{
			log.warning("Could not create SIP account, MySQLConnector.addSubscriber() returned subscriberId=" + subscriberId);
			request.setAttribute(INFO_MSG, "That account is not available. Please choose another account.");
		}
		
		return false;
	}
	
	private void editSIPAccount(HttpServletRequest request, HttpServletResponse response, Properties ctx, 
								WebUser wu, String username, String password, String domain, String timezone)
		throws ServletException, IOException
	{
		String url = DEFAULT_JSP;

		SIPAccount account = null;
		String id = WebUtil.getParameter(request, "id");
		if (id != null && id.length() > 0)
		{
			try
			{
				account = SERConnector.getSIPAccount(Integer.parseInt(id));
			}
			catch (Exception ex){}
		}
		else
		{
			account = SERConnector.getSIPAccount(username, domain);
		}
		
		if (account != null)
		{
			// either username or domain is being editted
			boolean validUsernameDomainCombo = true;
			if (!account.getUsername().equalsIgnoreCase(username) || !account.getDomain().equalsIgnoreCase(domain))
			{
				validUsernameDomainCombo = !SERConnector.checkSIPUsernameDomainComboExist(username, domain);
			}
			
			if (validUsernameDomainCombo)
			{
				boolean updated = false;
				if (password == null || password.length() < 1)
					updated = SERConnector.updateSIPAccount(id, username, domain, timezone);
				else
					updated = SERConnector.updateSIPAccount(id, username, domain, password, timezone);
				
				if (updated)
				{
					request.setAttribute(INFO_MSG, "Your SIP account has been updated");
				}
				else
				{
					log.warning("Could not update SIP account where id=" + id);			
					request.setAttribute(INFO_MSG, "Your SIP account could not be updated at this time, please try again later..");			
				}
			}
			else
			{
				ArrayList<String> invalidFields = new ArrayList<String>();
				invalidFields.add("username");
				invalidFields.add(SIPDomainListTag.DOMAIN_LIST_SELECT_NAME);
				
				request.setAttribute(INVALID_FIELDS, invalidFields);
				request.setAttribute(INFO_MSG, "An account already exists for that username/domain combination.");
				
				url = EDIT_ACCOUNT_JSP;
			}
		}
		else
		{
			log.warning("Could not find SIP account with id=" + id);
			request.setAttribute(INFO_MSG, "Could not update account, please try again.");
		}
			
		forward(request, response, url);
	}
	
	private void forward(HttpServletRequest request, HttpServletResponse response, String url)
		throws ServletException, IOException
	{
		if (!url.startsWith("/")) url = "/" + url;
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
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

	public static void main(String[] args)
	{
		Adempiere.startup(false);
		Properties ctx = Env.getCtx();
		ctx.put("#AD_Client_ID", "1000000");
		ctx.put("#AD_Org_ID", "1000001");
	
		WebUser wu = WebUser.get(ctx, 1000167);
		String[] didNumbers = new String[]{"6499150500", "6499150501", "6499150503", "6499150504", "6499150506", "6499150508", "6499150540", "6499150547", "6499150548", "6499152536", "648006227747"};
		for (String didNumber : didNumbers)
		{
			System.out.println("INSERT INTO voicemail_users (uuid, context, mailbox, password, fullname, email, pager) VALUES ('" 
					+ Integer.toString(wu.getC_BPartner_ID()) + "', '" + wu.getBP_SearchKey() + "', '" + didNumber + "', '" + didNumber + "', '" + wu.getName() + "', '" +  wu.getEmail() + "', '');");
		}
	}
}

