package org.compiere.wstore;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.compiere.model.MAttachment;
import org.compiere.model.MBPBankAccount;
import org.compiere.model.MMailMsg;
import org.compiere.model.MPayment;
import org.compiere.model.X_C_Payment;
import org.compiere.process.DocAction;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.WebEnv;
import org.compiere.util.WebSessionCtx;
import org.compiere.util.WebUser;
import org.compiere.util.WebUtil;

import com.conversant.model.SIPAccount;

public class CreditCardValidationServlet extends HttpServlet
{
	/** Logger														*/
	private static CLogger log = CLogger.getCLogger(CreditCardValidationServlet.class);
	
	/** Name 														*/
	public static final String SERVLET_NAME = "creditCardValidationServlet";
	
	/** Default JSP													*/
	public static final String DEFAULT_JSP = "creditCardValidation.jsp";
	
	/** Creditcard validation payment flag							*/
	public static final String ATTR_CCV_PAYMENT = "ccvPayment";
	
	/** Error request parameter (prevents infinite redirect loop)	*/
	public static final String PARAM_ERROR = "error";
	
	/**	Action request parameter									*/
	public static final String PARAM_ACTION = "action";
	
	/** Charge account action parameter value						*/
	public static final String ACTION_CHARGE_ACCOUNT = "chargeAccount";
	
	/** Validate amount action parameter value 						*/
	public static final String ACTION_VALIDATE_AMOUNT = "validateAmount";
	
	/** No bank account found action parameter value				*/
	public static final String ACTION_NO_BANK_ACCOUNT = "noBankAccount";
	
	/** Name of amount field on form								*/
	public static final String FORM_AMOUNT_NAME = "form.amount";
	
	/** Info message to be display to user							*/
	public static final String INFO_MSG = "infoMsg";
	
	/** Max validation attempts										*/
	private static final int MAX_VALIDATION_ATTEMPTS = 3;
	
	/**
	 * 	Initialize global variables
	 *  @param config servlet configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WebEnv.initWeb(config))
			throw new ServletException("CreditCardValidationServlet.init");
	} 	// init
	
	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Credit Card Validation Servlet";
	}	//	getServletInfo
	
	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.fine("destroy");
	}   //  destroy
	
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

	/**
	 * Handle Request, both GET and POST
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		// Get session and remove any existing header message
		HttpSession session = request.getSession(true);
		session.removeAttribute(WebSessionCtx.HDR_MESSAGE);
		
		if (isLoggedIn(request, response))
		{
			// Set forward url
			String url = DEFAULT_JSP;
			
			// Init message
			String msg = null;
			
			// Process action
			String action = WebUtil.getParameter(request, PARAM_ACTION);
			if (action == null || action.length() < 1)
			{
				// Do nothing
			}
			else if (action.equalsIgnoreCase(ACTION_CHARGE_ACCOUNT))
			{
				msg = chargeAccount(this, request, response);
			}
			else if (action.equalsIgnoreCase(ACTION_VALIDATE_AMOUNT))
			{
				msg = validateAmount(request);
			}
			else if (action.equalsIgnoreCase(ACTION_NO_BANK_ACCOUNT))
			{
				url = handleNoBankAccount(request);
			}
			
			// set the message to display to user
			if (msg != null)
				request.setAttribute(INFO_MSG, msg);
			
			if (!response.isCommitted())
				forward(this, request, response, url);
		}
	}
	
	protected static String chargeAccount(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response)
	{
		return chargeAccount(servlet, request, response, null);
	}
	
	protected static String chargeAccount(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response, MPayment p)
	{
		String retValue = null;
		
		// Get context
		Properties ctx = JSPEnv.getCtx(request);

		// Load WebUser and BP_BankAccount
		WebUser wu = WebUser.get(request);
		MBPBankAccount ba = wu.getBPBankAccount();
		
		// Check card isn't already validated AND a validation amount hasn't already been charged
		if (ba != null && !ba.isBP_CC_Validated() && 
				(ba.getBP_CC_ValidationAmount().compareTo(Env.ZERO) == 0))
		{
			// Clear validation data from BP's bank account
			clearValidationData(ba);
			
			if (p == null)
				p = getRandomAmountPayment(ctx, ba);
			
			// Process 
			if (p.processOnline())
			{
				ba.setBP_CC_ValidationAmount(p.getPayAmt());
				ba.setBP_CC_ValidationDateCharged(new Timestamp(System.currentTimeMillis()));
				ba.save();
				
				p.processIt(DocAction.ACTION_Complete);
				p.save();					
			}
			else
			{			
				if (p.getPaymentProcessor() == null)
				{
					// remove error message because we know its regarding no payment processor
					p.setErrorMessage(null);
					
					// set payment and credit card validation flag in session for use on paymentInfo.jsp
					HttpSession session = request.getSession();
					session.setAttribute(PaymentServlet.ATTR_PAYMENT, p);
					session.setAttribute(ATTR_CCV_PAYMENT, "1");	
					
					// forward to paymentInfo.jsp to gather their creditcard details
					try
					{
						if (!response.isCommitted())
							forward(servlet, request, response, "paymentInfo.jsp");
					}
					catch (Exception ex){}
					
					return null;
				}
				else
				{	
					if (p.getErrorMessage() != null && p.getErrorMessage().length() > 0)
						retValue = p.getErrorMessage();
					else
						retValue = "An internal error occured while charging your account, please try again later.";
					
					// remove payment
					p.delete(true);
				}
			}
			
			sendValidationEmail(request, wu, retValue);				
		}
		
		return retValue;
	}
	
	private static void clearValidationData(MBPBankAccount ba)
	{
		if (ba != null)
		{
			ba.setBP_CC_Validated(false);
			ba.setBP_CC_ValidationAmount(Env.ZERO);
			ba.setBP_CC_ValidationDateCharged(null);
			ba.setBP_CC_ValidationInvalidCount(0);
			ba.save();
		}
	}
	
	private static MPayment getRandomAmountPayment(Properties ctx, MBPBankAccount ba)
	{
		// Generate random amount between 1.01 and 1.99 (min amount allowed to be charged to cc is $1)
		Random randGen = new Random();
		int cents = randGen.nextInt(99) + 101;			
		BigDecimal amount = new BigDecimal(cents / 100.0);
		amount = amount.round(new MathContext(3));
	
		// Create payment
		MPayment p = new MPayment(ctx, 0, null);
		p.setIsSelfService(true);
		p.setAmount(0, amount);		// for CC use default currency from account
		p.setIsOnline(true);

		// Sales CC Trx
		p.setC_DocType_ID(true);
		p.setTrxType(X_C_Payment.TRXTYPE_Sales);
		p.setTenderType(X_C_Payment.TENDERTYPE_CreditCard);
		
		// Payment Info
		p.setC_Invoice_ID(0);
		
		// BP Info
		p.setBP_BankAccount(ba);
		
		return p;
	}
	
	private String validateAmount(HttpServletRequest request)
	{
		String msg = null;

		WebUser wu = WebUser.get(request);
		MBPBankAccount ba = wu.getBPBankAccount();
		String formAmount = WebUtil.getParameter(request, FORM_AMOUNT_NAME);
		
		if (ba != null && !ba.isBP_CC_Validated())
		{
			boolean invalidAmount = false;
			try
			{
				// remove dollar signs and commas
				if (formAmount != null)
				{
					formAmount = formAmount.replace("$", "");
					formAmount = formAmount.replace(",", "");
				}
				
				// try parse the amount entered by the user
				BigDecimal amount = new BigDecimal(formAmount);
				
				if (ba.getBP_CC_ValidationAmount().compareTo(amount) == 0)
				{
					// set credit card as validated
					ba.setBP_CC_Validated(true);
					ba.save();
					
					// set all barred DIDs to active
					String uuid = Integer.toString(wu.getC_BPartner_ID());
					MAttachment attachment = ba.createAttachment();
					String barredDIDs = attachment.getTextMsg();
					
					StringTokenizer st = new StringTokenizer(barredDIDs, ",");
					while(st.hasMoreTokens())
					{						
						String didNumber = st.nextToken().replaceAll(" ", "");
//						SERConnector.updateUserPreference(uuid, didNumber, SIPServlet.USR_PREF_COUT_ATTRIBUTE, SIPServlet.USR_PREF_ACTIVE);
						SERConnector.endDateUserPreference(uuid, didNumber, SIPServlet.USR_PREF_COUT_ATTRIBUTE);
						
						// TODO: Remove use of constant domain
						SIPAccount sipAccount = SERConnector.getSIPAccount(didNumber, DIDConstants.DEFAULT_SIP_DOMAIN);
						SERConnector.addUserPreference(uuid, didNumber, DIDConstants.DEFAULT_SIP_DOMAIN, SIPServlet.USR_PREF_COUT_ATTRIBUTE, SIPServlet.USR_PREF_ACTIVE, SIPServlet.USR_PREF_ATTR_TYPE_NUMERIC, sipAccount != null ? sipAccount.getId().toString() : "");
						
					}
					
					// remove attachment as its no longer needed
					attachment.delete(true);
				}
				else
				{
					// get current count and reset to 0 if negative
					int currentCount = ba.getBP_CC_ValidationInvalidCount();
					if (currentCount < 0)
						currentCount = 0;
					
					// increment invalid count and store it
					int newCount = currentCount + 1;
					ba.setBP_CC_ValidationInvalidCount(newCount);
					
					if (newCount < MAX_VALIDATION_ATTEMPTS)
					{
						msg = "The amount you entered does not match the amount charged. You have entered an incorrect amount " + newCount + " time(s), " + (MAX_VALIDATION_ATTEMPTS - newCount) + " attempts left.";					
					}
					else
					{
						// reset fields
						ba.setBP_CC_Validated(false);
						ba.setBP_CC_ValidationAmount(Env.ZERO);
						ba.setBP_CC_ValidationInvalidCount(0);
						
						msg = "You have entered an incorrect amount " + MAX_VALIDATION_ATTEMPTS + " times. You are now required to revalidate your credit card with a new amount.";
					}
					
					// save after possible reset
					ba.save();
					
					invalidAmount = true;
				}
			}
			catch (Exception ex)
			{
				msg = "The amount you enter is not in the correct format e.g. $1.77 is the correct format";				
				invalidAmount = true;
			}
			
			// send back form.amount as an invalid field (JSP will render error icon next to it)
			if (invalidAmount)
			{					
				ArrayList<String> invalidFields = new ArrayList<String>();
				invalidFields.add(FORM_AMOUNT_NAME);
				request.setAttribute(ValidationServlet.INVALID_FIELDS, invalidFields);
			}
		}
		
		return msg;
	}
	
	private String handleNoBankAccount(HttpServletRequest request)
	{
		// Get context
		Properties ctx = JSPEnv.getCtx(request);

		// Get session
		HttpSession session = request.getSession(true);
		
		// Set default url
		String url = DEFAULT_JSP;
		
		// Load WebUser and BP_BankAccount
		WebUser wu = WebUser.get(request);
		MBPBankAccount ba = wu.getBankAccount(true, true);
		
		if (ba != null && !ba.isBP_CC_Validated() && 
				(ba.getBP_CC_ValidationAmount().compareTo(Env.ZERO) == 0))
		{
			MPayment p = getRandomAmountPayment(ctx, ba);
			session.setAttribute(PaymentServlet.ATTR_PAYMENT, p);
			session.setAttribute(ATTR_CCV_PAYMENT, "1");			
			
			url = "/paymentInfo.jsp";
		}
		
		return url;
	}
	
	private static void sendValidationEmail(HttpServletRequest request, WebUser wu, String errorMsg)
	{
		String couldNotChargeMsg = "";
		if (errorMsg != null)
		{
			couldNotChargeMsg = "\n\n** An error occured charging your card - " + errorMsg + ". Please visit the above link to try again. **";
		}
		
		JSPEnv.sendEMail(request, wu, MMailMsg.MAILMSGTYPE_CreditcardValidation,
				new Object[]{
					"",
					wu.getName() + ",",
					"http://" + request.getServerName() + "/" + CreditCardValidationServlet.DEFAULT_JSP,
					couldNotChargeMsg});
	}
	
	protected static void sendValidationRequestEmail(HttpServletRequest request, WebUser wu)
	{
		JSPEnv.sendEMail(request, wu, MMailMsg.MAILMSGTYPE_CreditcardValidationRequest,
				new Object[]{
					"",
					wu.getName() + ",",
					"http://" + request.getServerName() + "/" + CreditCardValidationServlet.DEFAULT_JSP});
	}
	
	private static void forward(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response, String url)
		throws ServletException, IOException
	{
		if (!url.startsWith("/")) url = "/" + url;
		RequestDispatcher dispatcher = servlet.getServletContext().getRequestDispatcher(url);
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
		StringBuilder allDIDs = new StringBuilder("");
		if (allDIDs.length() > 0)
			allDIDs.replace(allDIDs.length()-1, allDIDs.length(), "");
		System.err.println(allDIDs.toString());
	}
}
