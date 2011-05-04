package org.compiere.wstore;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.compiere.model.MBPartner;
import org.compiere.model.MConversionRate;
import org.compiere.model.MCurrency;
import org.compiere.model.MProduct;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.WebEnv;
import org.compiere.util.WebSessionCtx;
import org.compiere.util.WebUser;
import org.compiere.util.WebUtil;

import com.conversant.did.DIDConstants;
import com.conversant.did.DIDUtil;

public class CreateProductServlet extends HttpServlet
{
	/** Name 										*/
	public static final String NAME = "createProductServlet";
		
	/**	Request Parameters							*/
	public static final String PARAM_ACTION = "action";
	
	/** Create DID Pair Request Parameter Value		*/
	public static final String CREATE_DID_PAIR = "createDIDPair";
	
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(CreateProductServlet.class);
	
	/** Default JSP 								*/
	public static final String DEFAULT_JSP = "createProduct.jsp";
	
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
			throw new ServletException("CreateProductServlet.init");
	} 	// init
	
	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Create Product Servlet";
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
		this.handleRequest(request, response);
	}	// doGet

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		log.info("Post from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		this.handleRequest(request, response);
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
			else if (action.equalsIgnoreCase(CREATE_DID_PAIR))
			{				
				String didNumber = WebUtil.getParameter(request, "form.didNumber");
				String countryId = WebUtil.getParameter(request, "form.countryId");
				String countryCode = WebUtil.getParameter(request, "form.countryCode");
				String areaCode = WebUtil.getParameter(request, "form.areaCode");
				String areaCodeDescription = WebUtil.getParameter(request, "form.areaCodeDescription");
				
				// get pricelist version id
				int M_PriceList_Version_ID = WebUtil.getParameterAsInt (request, "M_PriceList_Version_ID");
				if (M_PriceList_Version_ID == 0)
				{
					log.severe("Could not load M_PriceList_Version_ID when creating DID product pair");
					setInfoMsg(request, "Could not load M_PriceList_Version_ID when creating DID product pair, reload page and try again. If problem persists check jsp code.");
					forward(request, response, DEFAULT_JSP);
					return;
				}

				// get free minutes
				String freeMinutes = WebUtil.getParameter(request, "form.freeMinutes");
				if (freeMinutes == null || freeMinutes.length() < 1)
					freeMinutes = "0";
				
				// check for existing products
				MProduct[] products = DIDUtil.getDIDProducts(ctx, didNumber, null);
				
				// make sure both setup and monthly products exist 
				if (products.length == 2) 
				{		
					setInfoMsg(request, "DID product pair already exist for " + didNumber);
					forward(request, response, DEFAULT_JSP);
					return;
				}
				// either only 1 part of the pair exists of more than 2
				else if (products.length > 0)
				{
					log.warning("Invalid product pair found for '" + didNumber + "', " + products.length + " products were found!");
					setInfoMsg(request, "Invalid product pair found for '" + didNumber + "', " + products.length + " products were found!");
					forward(request, response, DEFAULT_JSP);
					return;
				}
				
				// check for existing call products
				MProduct[] callProducts = DIDUtil.getCallProducts(ctx, didNumber, null);
				if (callProducts.length == 2) 
				{		
					setInfoMsg(request, "Call product pair already exist for " + didNumber);
					forward(request, response, DEFAULT_JSP);
					return;
				}
				// either only 1 part of the pair exists of more than 2
				else if (callProducts.length > 0)
				{
					log.warning("Invalid call product pair found for '" + didNumber + "', " + callProducts.length + " products were found!");
					setInfoMsg(request, "Invalid call product pair found for '" + didNumber + "', " + callProducts.length + " products were found!");
					forward(request, response, DEFAULT_JSP);
					return;
				}
				
				// check for existing sip product
				MProduct[] sipProducts = DIDUtil.getSIPProducts(ctx, didNumber, null);
				if (sipProducts.length == 1)
				{
					setInfoMsg(request, "SIP product already exists for " + didNumber);
					forward(request, response, DEFAULT_JSP);
					return;
				}
				else if (sipProducts.length > 0)
				{
					log.warning("Invalid SIP products found for '" + didNumber + "', " + sipProducts.length + " products were found!");
					setInfoMsg(request, "Invalid SIP products found for '" + didNumber + "', " + sipProducts.length + " products were found!");
					forward(request, response, DEFAULT_JSP);
					return;
				}
				
				// check for existing voicemail product
				MProduct[] voicemailProducts = DIDUtil.getVoicemailProducts(ctx, didNumber, null);
				if (voicemailProducts.length == 1)
				{
					setInfoMsg(request, "Voicemail product already exists for " + didNumber);
					forward(request, response, DEFAULT_JSP);
					return;
				}
				else if (voicemailProducts.length > 0)
				{
					log.warning("Invalid Voicemail products found for '" + didNumber + "', " + voicemailProducts.length + " products were found!");
					setInfoMsg(request, "Invalid Voicemail products found for '" + didNumber + "', " + voicemailProducts.length + " products were found!");
					forward(request, response, DEFAULT_JSP);
					return;
				}
				
				// setup price objects
				BigDecimal perMinuteCharge = null;
				BigDecimal setupCost = null;
				BigDecimal monthlyCharge = null;
				
				// parse business partner id
				int C_BPartner_ID = -1;
				try
				{
					C_BPartner_ID = Integer.parseInt(WebUtil.getParameter(request, "form.businessPartnerId"));
					
					// check valid C_BPartner_ID
					MBPartner bp = MBPartner.get(ctx, C_BPartner_ID);
					if (bp == null)
					{
						setInfoMsg(request, "Invalid business partner id");
						forward(request, response, DEFAULT_JSP);
						return;
					}						
				}
				catch (NumberFormatException ex)
				{
					log.log(Level.INFO, "Could not parse business partner id " + WebUtil.getParameter(request, "form.businessPartnerId"), ex);
					
					setInfoMsg(request, "Could not parse business partner id");
					forward(request, response, DEFAULT_JSP);
					return;
				}
				
				// parse prices
				try
				{
					setupCost = new BigDecimal(WebUtil.getParameter(request, "form.setupCost"));
				}
				catch (NumberFormatException ex)
				{
					log.log(Level.INFO, "Could not parse setupCost for " + didNumber + " where setupCost=" + WebUtil.getParameter(request, "form.setupCost"), ex);
					
					setInfoMsg(request, "Could not parse setup cost");
					forward(request, response, DEFAULT_JSP);
					return;
				}
				
				try
				{
					monthlyCharge = new BigDecimal(WebUtil.getParameter(request, "form.monthlyCharge"));
				}
				catch (NumberFormatException ex)
				{
					log.log(Level.INFO, "Could not parse monthlyCharge for " + didNumber + " where monthlyCharge=" + WebUtil.getParameter(request, "form.monthlyCharge"), ex);
					
					setInfoMsg(request, "Could not parse monthly charge");
					forward(request, response, DEFAULT_JSP);
					return;
				}
				
				String sPerMinCharge = null;
				boolean parsed = false;
				try
				{
					perMinuteCharge = new BigDecimal(WebUtil.getParameter(request, "form.perMinuteCharge"));
					parsed = true;
				}
				catch (NumberFormatException ex)
				{
					log.log(Level.INFO, "Could not parse perMinuteCharge for " + didNumber + " where perMinuteCharge=" + WebUtil.getParameter(request, "form.perMinuteCharge"), ex);
				
					// will save per min charge as string in DID_PERMINCHARGE attribute
					sPerMinCharge = WebUtil.getParameter(request, "form.perMinuteCharge");
					
					if (sPerMinCharge == null || sPerMinCharge.length() < 1)
					{
						setInfoMsg(request, "Could not parse per minute charge as a BigDecimal or String");
						forward(request, response, DEFAULT_JSP);
						return;
					}
				}
				
				// set currency id
				int C_Currency_ID = 0;
				String currency = WebUtil.getParameter(request, "form.currency");
				if (currency != null)
				{
					if (currency.equalsIgnoreCase("NZD"))
						C_Currency_ID = DIDConstants.NZD_CURRENCY_ID;
					else if (currency.equalsIgnoreCase("USD"))
						C_Currency_ID = DIDConstants.USD_CURRENCY_ID;
				}
				
				
				// display error if cant load from bp/pl
				if (C_Currency_ID == 0)
				{
					log.warning("Couldn't load Currency");
					
					setInfoMsg(request, "Couldn't load Currency");
					forward(request, response, DEFAULT_JSP);
					return;
				}
				
				BigDecimal perMinuteChargeNZD = null;
				BigDecimal setupCostNZD = null;
				BigDecimal monthlyChargeNZD = null;
				
				if (C_Currency_ID == DIDConstants.NZD_CURRENCY_ID)
				{
					if (parsed)
						perMinuteChargeNZD = perMinuteCharge;
					
					setupCostNZD = setupCost;
					monthlyChargeNZD = monthlyCharge;
				}
				else
				{
					// convert prices from selected currency to NZD
					if (parsed)
						perMinuteChargeNZD = MConversionRate.convert(ctx,  perMinuteCharge, C_Currency_ID, DIDConstants.NZD_CURRENCY_ID, null, DIDConstants.CUSTOM_CONV_TYPE_ID, Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
					
					setupCostNZD = MConversionRate.convert(ctx,  setupCost, C_Currency_ID, DIDConstants.NZD_CURRENCY_ID, null, DIDConstants.CUSTOM_CONV_TYPE_ID, Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
					monthlyChargeNZD = MConversionRate.convert(ctx, monthlyCharge, C_Currency_ID, DIDConstants.NZD_CURRENCY_ID, null, DIDConstants.CUSTOM_CONV_TYPE_ID, Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
				}
				
				// display error if custom rate not found
				if ((perMinuteChargeNZD == null && parsed) || setupCostNZD == null || monthlyChargeNZD == null) 
				{
					MCurrency m_currency = MCurrency.get(ctx, C_Currency_ID);
					log.warning("Could not find custom MConversionRate to make conversion from " + (m_currency != null ? m_currency.getDescription() : "selected currency") + " to NZD.");
					
					setInfoMsg(request, "Could not find custom MConversionRate to make conversion from " + (m_currency != null ? m_currency.getDescription() : "selected currency") + " to NZD.");
					forward(request, response, DEFAULT_JSP);
					return;				
				}
				
				// setup product objects and get product fields and values
				MProduct setupProduct = null;
				MProduct monthlyProduct = null;
				HashMap<String, String> setupFields = DIDController.getDIDSetupFields(didNumber, areaCodeDescription);
				HashMap<String, String> monthlyFields = DIDController.getDIDMonthlyFields(didNumber, areaCodeDescription);
				
				// create products
				setupProduct = DIDController.createMProduct(ctx, setupFields);
				if (setupProduct != null)
				{
					monthlyProduct = DIDController.createMProduct(ctx, monthlyFields);
					if (monthlyProduct != null)
					{
						log.info("");
					}
					else
					{
						log.severe("Failed to create monthly product, invalidating setup product for " + didNumber);
						DIDController.invalidateProduct(setupProduct);
						
						setInfoMsg(request, "Failed to create monthly product, invalidating setup product for " + didNumber);
						forward(request, response, DEFAULT_JSP);
						return;
					}
				}
				else
				{
					log.severe("Failed to create setup product for " + didNumber);
					
					setInfoMsg(request, "Failed to create setup product for " + didNumber);
					forward(request, response, DEFAULT_JSP);
					return;
				}

				// set products attributes
				DIDController.updateProductAttributes(ctx, setupProduct.getM_AttributeSetInstance_ID(), 
						setupProduct.get_ID(), areaCodeDescription, didNumber, (parsed ? perMinuteChargeNZD.toString() : sPerMinCharge), areaCode, 
						"5", countryId, countryCode, freeMinutes, true, false);
				DIDController.updateProductAttributes(ctx, monthlyProduct.getM_AttributeSetInstance_ID(), 
						monthlyProduct.get_ID(), areaCodeDescription, didNumber, (parsed ? perMinuteChargeNZD.toString() : sPerMinCharge), areaCode, 
						"5", countryId, countryCode, freeMinutes, false, false);
				
				// reload product values so that M_AttributeID created above is there
				setupProduct.load(null);
				monthlyProduct.load(null);
				
				// set products price
				DIDController.updateProductPrice(ctx, M_PriceList_Version_ID, setupProduct.get_ID(), setupCostNZD);
				DIDController.updateProductPrice(ctx, M_PriceList_Version_ID, monthlyProduct.get_ID(), monthlyChargeNZD);
				
				// set business partners price list price
				DIDController.updateBPPriceListPrice(ctx, C_BPartner_ID, setupProduct.get_ID(), setupCost);
				DIDController.updateBPPriceListPrice(ctx, C_BPartner_ID, monthlyProduct.get_ID(), monthlyCharge);
				
				// set products purchaser info
				DIDController.updateProductPO(ctx, C_BPartner_ID, setupProduct, setupCost, C_Currency_ID);
				DIDController.updateProductPO(ctx, C_BPartner_ID, monthlyProduct, monthlyCharge, C_Currency_ID);
				
				// set product relations
				DIDController.updateProductRelations(ctx, monthlyProduct.get_ID(), setupProduct.get_ID()); 
				
				// create call products
				HashMap<Integer, Object> attributes = new HashMap<Integer, Object>();
				attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, DIDConstants.ATTRIBUTE_VALUE_INBOUND_CDR_USERNAME.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber).replace(DIDConstants.DOMAIN_IDENTIFIER, "conversant.co.nz"));
				attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_APPLICATION, DIDConstants.ATTRIBUTE_ID_CDR_APPLICATION_VALUE_AUDIO);
				attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION, DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_INBOUND);
				attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_NUMBER, didNumber);
				
				MProduct inbound = DIDUtil.createCallProduct(ctx, attributes, null);
				if (inbound == null)
				{
					log.warning("Failed to create CALL-IN-" + didNumber);
				}
				else
				{					
					if (!DIDController.updateProductPrice(ctx, M_PriceList_Version_ID, inbound.getM_Product_ID(), Env.ZERO, null))
					{
						log.warning("Failed to create price for " + inbound);
					}
				}
				
				attributes.remove(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME);
				attributes.remove(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION);
				attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, DIDConstants.ATTRIBUTE_VALUE_OUTBOUND_CDR_USERNAME.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber).replace(DIDConstants.DOMAIN_IDENTIFIER, "conversant.co.nz"));
				attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION, DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_OUTBOUND);
				
				MProduct outbound = DIDUtil.createCallProduct(ctx, attributes, null);
				if (outbound == null)
				{
					log.warning("Failed to create CALL-OUT-" + didNumber);
				}
				else
				{
					if (!DIDController.updateProductPrice(ctx, M_PriceList_Version_ID, outbound.getM_Product_ID(), Env.ZERO, null))
					{
						log.warning("Failed to create price for " + outbound);
					}
				}
				
				// create sip product attributes
				attributes = new HashMap<Integer, Object>();
				attributes.put(DIDConstants.ATTRIBUTE_ID_SIP_ADDRESS, didNumber);
				attributes.put(DIDConstants.ATTRIBUTE_ID_SIP_DOMAIN, DIDConstants.DEFAULT_SIP_DOMAIN);
				
				// create sip product
				MProduct sipProduct = DIDUtil.createSIPProduct(ctx, attributes, null);
				if (sipProduct == null)
				{
					log.warning("Failed to create " + DIDConstants.SIP_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber));
				}
				else
				{
					// create price
					if (!DIDController.updateProductPrice(ctx, M_PriceList_Version_ID, sipProduct.getM_Product_ID(), Env.ZERO, null))
					{
						log.warning("Failed to create price for " + sipProduct);
					}
				}
				
				// create voicemail product attributes
				attributes = new HashMap<Integer, Object>();
				attributes.put(DIDConstants.ATTRIBUTE_ID_VM_CONTEXT, "proxy_default");
				attributes.put(DIDConstants.ATTRIBUTE_ID_VM_MACRO_NAME, "macroName");
				attributes.put(DIDConstants.ATTRIBUTE_ID_VM_MAILBOX_NUMBER, didNumber);
				
				// Create Voicemail product
				MProduct voicemailProduct = DIDUtil.createVoicemailProduct(ctx, attributes, null);
				if (voicemailProduct == null)
				{
					log.warning("Failed to create " + DIDConstants.VOICEMAIL_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber));
				}
				else
				{
					// create price
					if (!DIDController.updateProductPrice(ctx, DIDConstants.PRICELIST_VERSION_ID_STANDARD, voicemailProduct.getM_Product_ID(), Env.ZERO, null))
					{
						log.warning("Failed to create price for " + voicemailProduct);
					}
				}

				setInfoMsg(request, "Product pair for " + didNumber + " has been created successfully!");
				forward(request, response, DEFAULT_JSP);
			}
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
