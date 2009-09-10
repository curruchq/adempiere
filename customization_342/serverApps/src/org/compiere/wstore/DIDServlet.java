package org.compiere.wstore;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MAttributeSet;
import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MBPartner;
import org.compiere.model.MConversionRate;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPriceList;
import org.compiere.model.MPriceListVersion;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPO;
import org.compiere.model.MProductPrice;
import org.compiere.model.MRelatedProduct;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.compiere.util.WebEnv;
import org.compiere.util.WebSessionCtx;
import org.compiere.util.WebUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.conversant.model.DID;
import com.conversant.model.DIDAreaCode;
import com.conversant.model.DIDCountry;

public class DIDServlet extends HttpServlet
{
	/** New line								*/
	public static final String NEWLINE = "\n";
	
	/**	Request/Session attributes				*/
	public static final String ATTR_DID_COUNTRY = "didCountry";		
	public static final String ATTR_COUNTRYLIST = "countryList";
	public static final String ATTR_SELECTED_COUNTRY = "selectedCountry";
	public static final String ATTR_AREA_CODE_XML = "areaCodeXML";
	public static final String ATTR_AREA_CODE_XML_COUNTRY = "areaCodeXMLCountry";
	
	/**	Request Parameters						*/
	public static final String PARAM_ACTION = "action";
	public static final String PARAM_COUNTRY = "country";
	
	/** Area code select name 					*/
	public static final String AREA_CODES_SELECT_NAME = "form.select.areacodes";
	
	/** Action Parameter Values			 		*/
	public static final String ACTION_SEARCH = "search";
	public static final String ACTION_ADD_DID_TO_WB = "addDIDToWB";
	public static final String ACTION_GET_AREA_CODES = "getAreaCodes";
	
	/** Default JSP Page for this servlet 		*/
	public static final String DEFAULT_DID_JSP = "didSearch.jsp";
		
	/** Info message identifier				*/
	public static final String INFO_MSG = "infoMsg";
	
	/** Area code XML parent element  			*/
	public static final String XML_AREA_CODE_PARENT = "areacodes";
	
	/** Area code XML child element				*/
	public static final String XML_AREA_CODE_CHILD = "areacode";
	
	/** Logger								*/
	private static CLogger log = CLogger.getCLogger(DIDServlet.class);
	
/*	 
 * TODO: Add a new table to compiere to store DID info (class constants)
 */
	
	/**
	 * 	Initialize global variables
	 *  @param config servlet configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WebEnv.initWeb(config))
			throw new ServletException("DIDServlet.init");
	} 	// init
	
	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "DID Servlet";
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

	private void handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		// Get session and remove any existing header message
		HttpSession session = request.getSession(true);
		session.removeAttribute(WebSessionCtx.HDR_MESSAGE);
		
		// Process action
		String action = WebUtil.getParameter(request, PARAM_ACTION);
		
		// set default forward url
		String url = "/" + DEFAULT_DID_JSP;
		
		// catch action when isn't set or doesn't have a value
		if (action == null || action.equals(""))
		{
			// do nothing
		}
		else if (action.equalsIgnoreCase(ACTION_SEARCH))
		{
			String selectedCountry = WebUtil.getParameter(request, DIDCountryListTag.COUNTRY_SELECT_NAME);
			String[] countryParams = this.parseCountryParams(selectedCountry);
			if (countryParams != null && countryParams.length == 3)
			{
				String countryCode = countryParams[0];
				String countryId = countryParams[1];
				String description = countryParams[2];			
				String areaCode = WebUtil.getParameter(request, DIDAreaCodeListTag.AREA_CODE_SELECT_NAME);
				
				log.info("Action request to '" + ACTION_SEARCH + "', countryCode=" + countryCode + ", countryId=" + countryId + ", description=" + description + (areaCode == null ? "" : ", areaCode=" + areaCode));
				
				// get the current area codes lists country
				String currentAreaCodeXMLCountry = (String)session.getAttribute(ATTR_AREA_CODE_XML_COUNTRY);
				
				// when area code list has already been loaded but no valid area code has been selected
				if (currentAreaCodeXMLCountry != null && currentAreaCodeXMLCountry.equalsIgnoreCase(selectedCountry) && areaCode != null && areaCode.length() < 1)
				{
					// do nothing
				}
				// load area code list is is empty or new country selected
				else if (currentAreaCodeXMLCountry == null || !currentAreaCodeXMLCountry.equalsIgnoreCase(selectedCountry))
				{
					session.setAttribute(ATTR_AREA_CODE_XML_COUNTRY, selectedCountry);
					session.setAttribute(ATTR_AREA_CODE_XML, getAreaCodeXML(loadAreaCodes(request, description, countryCode, countryId), null));
				}
				// search for DIDs in areaCode
				else if (areaCode != null && areaCode.length() > 0)
				{					
					DIDCountry country = DIDXService.getAvailableDIDS(JSPEnv.getCtx(request), countryCode, countryId, areaCode, description);
					DIDController.loadLocalDIDs(request, country);
					
					request.setAttribute(ATTR_DID_COUNTRY, country);
					
					// update xml to include selected area code
					String areaCodeXML = (String)session.getAttribute(ATTR_AREA_CODE_XML);
					String updatedAreaCodeXML = "";
					if (areaCodeXML != null && areaCodeXML.length() > 0)
					{
						updatedAreaCodeXML = updateAreaCodeXML(areaCodeXML, areaCode);
					}
					else
					{
						updatedAreaCodeXML = getAreaCodeXML(loadAreaCodes(request, description, countryCode, countryId), areaCode);
					}
					session.setAttribute(ATTR_AREA_CODE_XML, updatedAreaCodeXML);				
				}
				
				// set selected country to hold selected option in drop-down on page reload
				DIDCountry country = new DIDCountry(description, countryCode, countryId);
				request.setAttribute(ATTR_SELECTED_COUNTRY, country);
			}
			// invalid country selected, remove area code list
			else
			{
				session.removeAttribute(ATTR_AREA_CODE_XML_COUNTRY);
				session.removeAttribute(ATTR_AREA_CODE_XML);
				log.fine("Invalid country selected, " + DIDCountryListTag.COUNTRY_SELECT_NAME + "=" + countryParams);
			}
		}
		else if (action.equalsIgnoreCase(ACTION_ADD_DID_TO_WB))
		{
			addDIDToWB(request);
			url = "/basket.jsp";
		}
		else if (action.equalsIgnoreCase(ACTION_GET_AREA_CODES))
		{
			outputAreaCodes(request, response);
			return; // ajax method, return to avoid being forwarded below
		}
		
		if (!url.startsWith("/")) url = "/" + url;
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
		
	}	// handleRequest
	
	/**
	 * Parse Country parameter in String[] of parameters
	 * 
	 * @param countryParam
	 * @return String[] of parameters or null if any are invalid
	 */
	private String[] parseCountryParams(String countryParam)
	{
		// check input paramter
		if (countryParam != null && countryParam.length() > 0 && countryParam.contains(","))
		{
			String[] parsedParams = countryParam.split(",");

			for (String param : parsedParams)
			{
				param = param.trim();
				// if any parameters are empty return null
				if (param.equals(""))
				{
					return null;
				}
			}
			
			// all tests passed, return the parsed parameters
			return parsedParams;		
		}
		else if (countryParam != null && countryParam.length() > 0)
		{
			return new String[]{countryParam};
		}
		
		return null;
	}	// parseCountryParams

	/**
	 * Add DID lines to WebBasket
	 * 
	 * @param request
	 * @return
	 */
	private boolean addDIDToWB(HttpServletRequest request)
	{
		boolean lineAdded = false;
		
		Properties ctx = JSPEnv.getCtx(request);
		
		// Create WebBasket
		HttpSession session = request.getSession(true);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);
		if (wb == null)
			wb = new WebBasket(ctx);
		session.setAttribute(WebBasket.NAME, wb);
		
		// SalesRep
		int SalesRep_ID = WebUtil.getParameterAsInt (request, BasketServlet.P_SalesRep_ID);
		if (SalesRep_ID != 0)
		{
			wb.setSalesRep_ID(SalesRep_ID);
			log.fine("SalesRep_ID=" + SalesRep_ID);
		}

		// Get Price List
		PriceList pl = (PriceList)session.getAttribute(PriceList.NAME_CATEGORY);
		if (pl == null)
		{
			log.fine("No Price List in session");
			pl = (PriceList)request.getAttribute(PriceList.NAME_CATEGORY);
		}
		log.fine("PL=" + pl);
		
		// Get Parameter
		int M_PriceList_ID = WebUtil.getParameterAsInt (request, "M_PriceList_ID");
		int M_PriceList_Version_ID = WebUtil.getParameterAsInt (request, "M_PriceList_Version_ID");
		wb.setM_PriceList_ID (M_PriceList_ID);
		wb.setM_PriceList_Version_ID (M_PriceList_Version_ID);
		
		ArrayList<MProduct> productsToAdd = DIDController.getDIDProductsToAdd(request);
		
		for (MProduct product : productsToAdd) 
		{
			// Check for existing product in web basket already
			boolean productInWB = false;
			for (Object o : wb.getLines())
			{
				if (o instanceof WebBasketLine)
				{
					WebBasketLine wbl = (WebBasketLine)o;
					if (wbl.getM_Product_ID() == product.get_ID())
					{
						productInWB = true;
						break;
					}
				}
			}
			
			// Only add if product isn't already in web basket
			if (!productInWB)
			{
				// Find info in current price list
				int M_Product_ID = product.get_ID();
				BigDecimal price = null;
				String name = "";
				if (M_Product_ID != 0 && pl != null)
				{
					PriceListProduct plp = pl.getPriceListProduct(M_Product_ID);
					if (plp != null)
					{
						price = plp.getPrice ();
						name = plp.getName ();
						log.fine("Found in PL = " + name + " - " + price);
					}
				}
				
				// Price not in session price list and not as parameter 
				if (price == null)
				{
					//	Create complete Price List
					int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
					pl = PriceList.get(ctx, AD_Client_ID, M_PriceList_ID, product.getM_Product_Category_ID());
					session.setAttribute(PriceList.NAME_CATEGORY, pl);	//	set on session level
					PriceListProduct plp = pl.getPriceListProduct(M_Product_ID);
					if (plp != null)
					{
						price = plp.getPrice ();
						name = plp.getName ();
						log.fine("Found in complete PL = " + name + " - " + price);
					}
				}
				
				if (price != null)
				{
	
					WebBasketLine wbl = wb.add(product.get_ID(), product.getName(), Env.ONE, price);
					log.fine(wbl.toString());
					lineAdded = true;
				}
				else 
					log.warning ("Product Price not found - M_Product_ID=" + M_Product_ID);
			}
			else
				lineAdded = true; // to show basket
		}
		
		if (!lineAdded)
			request.setAttribute(INFO_MSG, "Product could not be added to your basket");
		
		return lineAdded;
	}
	
//*************************************************************************************************
//		AJAX Methods
//*************************************************************************************************

	private void outputAreaCodes(HttpServletRequest request, HttpServletResponse response)
	{
		PrintWriter out = null;
		try
		{
			// set up response
			response.setHeader("Cache-Control", "no-cache");
	        response.setContentType("text/xml; charset=UTF-8");
	        response.setCharacterEncoding("UTF-8");
	        out = response.getWriter();
	        
	        String selectedCountry = WebUtil.getParameter(request, PARAM_COUNTRY);
			String[] countryParams = this.parseCountryParams(selectedCountry);
			if (countryParams != null && countryParams.length == 3)
			{
				String countryCode = countryParams[0];
				String countryId = countryParams[1];
				String description = countryParams[2];
				
				String areaCodeXML = getAreaCodeXML(loadAreaCodes(request, description, countryCode, countryId), null);
				
				// keep area code xml in session
				HttpSession session = request.getSession(true);
				String areaCodeXMLCountry = (String)session.getAttribute(ATTR_AREA_CODE_XML_COUNTRY);
				if (areaCodeXMLCountry == null || !areaCodeXMLCountry.equalsIgnoreCase(selectedCountry))
				{
					session.setAttribute(ATTR_AREA_CODE_XML_COUNTRY, selectedCountry);
					session.setAttribute(ATTR_AREA_CODE_XML, areaCodeXML);
				}
				
				out.println(areaCodeXML);	
			}
			else
			{
				out.println(getNoAreaCodeXML());
			}
		}
		catch (IOException ex)
		{
			log.log(Level.WARNING, "Error printing areacodes to response", ex);
		}
		finally
		{
			if (out != null)
			{
				out.flush();
				out.close();
			}
		}
	}	// outputAreaCodes
	
	private static DIDCountry loadAreaCodes(HttpServletRequest request, String description, String countryCode, String countryId)
	{
		DIDCountry country = DIDXService.getDIDAreas(description, countryCode, countryId);
		
		if (country == null)
			country = new DIDCountry(description, countryCode, countryId);
		
		DIDController.loadLocalAreaCodes(request, country);
		
		return country;
	}
	
	private static String getAreaCodeXML(DIDCountry country, String selected)
	{	
		StringBuilder xml = new StringBuilder();

		if (country != null && country.getAreaCodes().size() > 0)
		{
			xml.append("<" + XML_AREA_CODE_PARENT + ">");
			xml.append("<" + XML_AREA_CODE_CHILD + " id=''>Select area code..</" + XML_AREA_CODE_CHILD + ">");

			for (DIDAreaCode areacode : country.getAreaCodes())
			{		
				String value = Util.maskHTML("(" + areacode.getCode() + ") " + areacode.getDesc());
				xml.append("<" + XML_AREA_CODE_CHILD + " id='" + areacode.getCode() + "'");
				if (selected != null && selected.length() > 0 && selected.equalsIgnoreCase(areacode.getCode()))
				{
					xml.append(" selected='selected'");
				}
				xml.append(">" + value + "</" + XML_AREA_CODE_CHILD + ">");			        	
			}

			xml.append("</" + XML_AREA_CODE_PARENT + ">");
		}
		else
		{
			xml.append(getNoAreaCodeXML());
		}

		return xml.toString();
	}	// getAreaCodeXML

	private static String updateAreaCodeXML(String oldAreaCodeXML, String selectedAreaCode)
	{
		try 
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = factory.newDocumentBuilder();

			InputSource inStream = new InputSource();
			inStream.setCharacterStream(new StringReader(oldAreaCodeXML));

			Document doc = db.parse(inStream);
			doc.getDocumentElement().normalize();

			NodeList nodeLst = doc.getDocumentElement().getChildNodes();
			if (nodeLst.getLength() > 0)
			{
				StringBuilder updatedAreaCodeXML = new StringBuilder();
				updatedAreaCodeXML.append("<" + doc.getDocumentElement().getNodeName() + ">");

				for (int i = 0; i < nodeLst.getLength(); i++) 
				{
					Node firstNode = nodeLst.item(i);
					if (firstNode.getNodeType() == Node.ELEMENT_NODE) 
					{					
						Element firstElement = (Element)firstNode;

						String nodeName = firstNode.getNodeName();
						String id = firstElement.getAttribute("id");
						String text = firstElement.getTextContent();

						updatedAreaCodeXML.append("<" + nodeName + " id='" + id + "'");

						if (id != null && id.equalsIgnoreCase(selectedAreaCode))
						{
							updatedAreaCodeXML.append(" selected='selected'");														
						}

						updatedAreaCodeXML.append(">");
						updatedAreaCodeXML.append(Util.maskHTML(text));
						updatedAreaCodeXML.append("</" + nodeName + ">");
					}
				}

				updatedAreaCodeXML.append("</" + doc.getDocumentElement().getNodeName() + ">");
				return updatedAreaCodeXML.toString();
			}
		} 
		catch (Exception ex) 
		{
			log.log(Level.WARNING, "Error parsing XML", ex);
		}

		return oldAreaCodeXML;
	}	// updateAreaCodeXML

	private static String getNoAreaCodeXML()
	{
		StringBuilder xml = new StringBuilder();
		xml.append("<" + XML_AREA_CODE_PARENT + ">");
		xml.append("<" + XML_AREA_CODE_CHILD + " id=''>None</" + XML_AREA_CODE_CHILD + ">");
		xml.append("</" + XML_AREA_CODE_PARENT + ">");
		return xml.toString();
	}

	
}
