package org.compiere.wstore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.compiere.model.MBPBankAccount;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderEx;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.util.CLogger;
import org.compiere.util.WebEnv;
import org.compiere.util.WebSessionCtx;
import org.compiere.util.WebUser;
import org.compiere.util.WebUtil;

import com.conversant.did.DIDUtil;

public class ProvisioningServlet extends HttpServlet
{
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(ProvisioningServlet.class);
	
	/** Name 										*/
	public static final String NAME = "provisioningServlet";
		
	/**	Request Parameters							*/
	public static final String PARAM_ACTION = "action";
	
	public static final String PARAM_ORDER_ID = "orderId";
	
	/** Get orders request parameter value			*/
	public static final String GET_ORDERS = "getOrders";
	
	/** Provision order rquest parameter value		*/
	public static final String PROVISION_ORDER = "provisionOrder";
	
	/** Default JSP 								*/
	public static final String DEFAULT_JSP = "provision.jsp";
	
	/** Info message identifier						*/
	public static final String INFO_MSG = "infoMsg";
	
	/**
	 * 	Initialize global variables
	 *  @param config servlet configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WebEnv.initWeb(config))
			throw new ServletException("ProvisioningServlet.init");
	} 	// init
	
	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Provisioning Servlet";
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
		
		Properties ctx = JSPEnv.getCtx(request);
		
		if (isLoggedIn(request, response))
		{	
			// Process action
			String action = WebUtil.getParameter(request, PARAM_ACTION);
			if (action == null || action.equals(""))
			{
				// do nothing
			}
			else if (action.equalsIgnoreCase(GET_ORDERS))
			{				
				String AD_User_ID = WebUtil.getParameter(request, "form.userId");

				if (AD_User_ID != null)
				{
					try
					{
						MOrder[] orders = MOrderEx.getOrders(ctx, Integer.parseInt(AD_User_ID), null);
						ArrayList<MOrder> didOrders = new ArrayList<MOrder>();
						
						// get orders containing one or more DID products
						for (MOrder order : orders)
						{
							MOrderLine[] lines = order.getLines();
							for (MOrderLine line : lines)
							{
								MProduct product = line.getProduct();
								
								if (product != null)
								{
									String didNumber = DIDUtil.getDIDNumber(ctx, product);
									if (didNumber != null)
									{
										didOrders.add(order);
										break;
									}
								}
							}
						}
						
						request.setAttribute("orders", didOrders);
					}
					catch (NumberFormatException ex)
					{
						setInfoMsg(request, "You entered an invalid User Id, please try again...");
					}
				}
				else
					setInfoMsg(request, "You entered an invalid User Id, please try again...");

				forward(request, response, DEFAULT_JSP);
			}
			else if (action.equalsIgnoreCase(PROVISION_ORDER))
			{
				String C_Order_ID = WebUtil.getParameter(request, PARAM_ORDER_ID);
				if (C_Order_ID != null)
				{
					try
					{
						MOrder order = new MOrder(ctx, Integer.parseInt(C_Order_ID), null);
						if (order != null)
						{
							ArrayList<String> invalidDIDs = DIDController.purchaseFromDIDx(ctx, order);
							if (invalidDIDs != null)
							{
								setInfoMsg(request, "Could not purchase the following DIDs from DIDx.net - " + invalidDIDs.toString() + ". Please void order an re-create without the offending DID(s).");
								if (!response.isCommitted())
									forward(request, response, DEFAULT_JSP);
								return;
							}
							
							WebUser wu = WebUser.get(ctx, order.getAD_User_ID());
							if (wu != null)
							{					
								MBPBankAccount ba = wu.getBankAccount(true, true);
								boolean validated = ba.isBP_CC_Validated();
								
								HashMap<String, ArrayList<String>> errorMsgs = DIDController.provisionDIDs(request, ctx, wu, order, validated);
								Iterator<String> keyIt = errorMsgs.keySet().iterator();
								boolean error = false;							
								StringBuilder infoMsg = new StringBuilder();
								while (keyIt.hasNext())
								{
									error = true;
									String didNumber = keyIt.next();
									ArrayList<String> didErrorMsgs = errorMsgs.get(didNumber);
									
									if (infoMsg.length() > 0)
										infoMsg.append("\n\n");
									
									infoMsg.append("[" + didNumber + "]\n");
									for (String msg : didErrorMsgs)
									{
										infoMsg.append(" - " + msg);
									}														
								}
								
								if (error)
								{
									setInfoMsg(request, infoMsg.toString());
								}
								else
								{
									setInfoMsg(request, "Provisioning for order " + order.getDocumentNo() + " has been completed successfully");
								}
							}
						}
					}
					catch (NumberFormatException ex)
					{
						setInfoMsg(request, "Invalid Order Id. Please try again...");
					}
				}
				else
				{
					setInfoMsg(request, "Invalid Order Id. Please try again...");
				}
			}
			
			if (!response.isCommitted())
				forward(request, response, DEFAULT_JSP);
		}
	}
	
	private void setInfoMsg(HttpServletRequest request, String msg)
	{
		request.setAttribute(INFO_MSG, msg);
	}

	private void forward(HttpServletRequest request, HttpServletResponse response, String url)
		throws ServletException, IOException
	{
		if (!url.startsWith("/")) url = "/" + url;
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
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
	}	// isLoggedIn
}
