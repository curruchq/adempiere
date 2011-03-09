package org.compiere.wstore;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.compiere.model.MAttachment;
import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MAttributeSet;
import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MBPBankAccount;
import org.compiere.model.MBPartner;
import org.compiere.model.MConversionRate;
import org.compiere.model.MDIDxCountry;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPriceList;
import org.compiere.model.MPriceListVersion;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPO;
import org.compiere.model.MProductPrice;
import org.compiere.model.MRelatedProduct;
import org.compiere.model.MSubscription;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.WebUser;
import org.compiere.util.WebUtil;

import com.conversant.did.DIDConstants;
import com.conversant.did.DIDUtil;
import com.conversant.model.DID;
import com.conversant.model.DIDAreaCode;
import com.conversant.model.DIDCountry;

public class DIDController 
{
	private static CLogger log = CLogger.getCLogger(DIDController.class);
	
	private DIDController() {}

// *************************************************************************************************
// 		DIDx.net Methods
// *************************************************************************************************
	
	/**
	 * Purchase DIDs contained on the WebOrder from DIDx.net
	 * 
	 * @param order the order
	 * @return list of invalid DIDs
	 */
	protected static ArrayList<String> purchaseFromDIDx(Properties ctx, MOrder order) 
	{
		ArrayList<String> invalidDIDs = new ArrayList<String>();
		ArrayList<String> boughtDIDs = new ArrayList<String>();
		
		if (order == null)
		{
			log.severe("Order is NULL, check logs!");
			return invalidDIDs;
		}
				
		boolean error = false;
		
		// get didx numbers
		for (String didNumber : DIDUtil.getNumbersFromOrder(ctx, order, true, null))
		{
			if (!DIDXService.buyDID(ctx, didNumber))
			{
				invalidDIDs.add(didNumber);
				error = true;
			}
			else
				boughtDIDs.add(didNumber);
		}
		
		// If a DID could not be purchased release all DIDs which were purchased
		if (error)
		{
			for (String didNumber : boughtDIDs)
			{
				if (!DIDXService.releaseDID(ctx, didNumber))
					log.warning("Could not release DID from DIDx.net, didNumber=" + didNumber);
			}
		}
		
		return invalidDIDs.size() > 0 ? invalidDIDs : null;
	}
	
	/**
	 * Releases DIDs contained on the WebOrder from DIDx.net
	 * 
	 * @param wo
	 */
	protected static void releaseFromDIDx(Properties ctx, MOrder order)
	{
		if (order != null)
		{		
			for (String didNumber : DIDUtil.getNumbersFromOrder(ctx, order, true, null))
			{
				if (!DIDXService.releaseDID(ctx, didNumber))
					log.warning("Could not release DID from DIDx.net, DID number=" + didNumber);
			}
		}
	}
	
// *************************************************************************************************
//		Provisioning Methods
// *************************************************************************************************
	
	/**
	 * Creates default SIP accounts and subscriptions for DIDs contained on an order
	 * 
	 * @param ctx
	 * @param wu
	 * @param wo
	 * @return true if WebOrder contained one or more DID products
	 */
	protected static HashMap<String, ArrayList<String>> provisionDIDs(HttpServletRequest request, Properties ctx, WebUser wu, MOrder order, boolean validatedCC)
	{
		// create CVoice products, default SIP accounts, Voicemail products and default voicemail accounts
		ArrayList<MProduct> cvoiceProducts = new ArrayList<MProduct>();
		ArrayList<MProduct> voicemailProducts = new ArrayList<MProduct>();
		
		HashMap<String, ArrayList<String>> allErrorMsgs = new HashMap<String, ArrayList<String>>();
		
		// to record barred numbers if not validated
		ArrayList<String> allDIDs = DIDUtil.getNumbersFromOrder(ctx, order, false, null);
		
		for (String didNumber : allDIDs)
		{		
			ArrayList<String> errorMsgs = new ArrayList<String>();
			
			if (!SIPServlet.createDefaultSIPAccount(wu, didNumber, validatedCC))
				errorMsgs.add("Default SIP Account wasn't created.");
			
			if (!SIPServlet.createDefaultVoicemailAccount(wu, didNumber))
				errorMsgs.add("Default Voicemail Account wasn't created.");
			
			MProduct cvoiceProduct = null;
			MProduct[] existingProducts = DIDUtil.getSIPProducts(ctx, didNumber, null);
			if (existingProducts.length > 0)
			{
				if (existingProducts.length > 1)
				{
					for (int i=1; i<existingProducts.length; i++)
					{
						DIDController.invalidateProduct(existingProducts[i]);
					}
				}
				
				cvoiceProduct = existingProducts[0];
				updateProductNameSearchKeyDesc(cvoiceProduct, getCVoiceFields(didNumber));
			}
			else
			{
				cvoiceProduct = createMProduct(ctx, getCVoiceFields(didNumber));
			}
			if (!updateSIPProductAttributes(ctx, cvoiceProduct, didNumber, DIDConstants.DEFAULT_SIP_DOMAIN))
				errorMsgs.add("CVoice product attributes were not set.");
			
			MProduct voicemailProduct = null;
			existingProducts = DIDUtil.getVoicemailProducts(ctx, didNumber, null);
			if (existingProducts.length > 0)
			{
				if (existingProducts.length > 1)
				{
					for (int i=1; i<existingProducts.length; i++)
					{
						DIDController.invalidateProduct(existingProducts[i]);
					}
				}
				
				voicemailProduct = existingProducts[0];
				updateProductNameSearchKeyDesc(voicemailProduct, getVoicemailFields(didNumber));
			}
			else
			{
				voicemailProduct = createMProduct(ctx, getVoicemailFields(didNumber));
			}
			if (!updateVoicemailProductAttributes(ctx, voicemailProduct, didNumber, "proxy_default", "proxy-vm")) // TODO: Move to constants?
				errorMsgs.add("Voicemail product attributes were not set.");
			
			// TODO: What if pricelist isn't in session?
			HttpSession session = request.getSession(true);
			PriceList pl = (PriceList)session.getAttribute(PriceList.NAME);
			if (pl != null)
			{
				updateProductPrice(ctx, pl.getPriceList_Version_ID(), cvoiceProduct.get_ID(), Env.ZERO);
				updateProductPrice(ctx, pl.getPriceList_Version_ID(), voicemailProduct.get_ID(), Env.ZERO);
			}
			else
			{
				log.warning("Couldn't update prices for CVoice and Voicemail products as session did not contain pricelist (DID = " + didNumber + ")");
				errorMsgs.add("Prices for CVoice and Voicemail products were not set.");
			}
			
			if (cvoiceProduct != null)
				cvoiceProducts.add(cvoiceProduct);
			else
				errorMsgs.add("CVoice product was not created.");
			
			if (voicemailProduct != null)
				voicemailProducts.add(voicemailProduct);
			else
				errorMsgs.add("Voicemail product was not created.");
			
			// add error messages for this DID to hashmap
			if (errorMsgs.size() > 0)
				allErrorMsgs.put(didNumber, errorMsgs);
		}

		// add subscriptions for each DID monthly product, each cvoice product & each voicemail product
		addSubscriptions(ctx, wu, order, cvoiceProducts, voicemailProducts, allErrorMsgs);
		
		// add DID number to BP's bank account's MAttachment (to record which DIDs are barred)
		if (!recordBarredDIDNumbers(validatedCC, wu, allDIDs))
		{
			log.warning("Failed to record barred DID(s) against Bank Account, the barred DID(s) are " + allDIDs.toString());
			ArrayList<String> msgs = allErrorMsgs.get(DIDConstants.ERROR_MSG_GENERIC_KEY);
			if (msgs == null)
				msgs = new ArrayList<String>();
			
			msgs.add("Failed to record barred DID(s) against Bank Account, the barred DID(s) are " + allDIDs.toString());
			
			allErrorMsgs.put(DIDConstants.ERROR_MSG_GENERIC_KEY, msgs);
		}
		
		return allErrorMsgs;
	}

	protected static boolean recordBarredDIDNumbers(boolean validatedCC, WebUser wu, String DIDs)
	{
		if (!validatedCC)
		{	
			MBPBankAccount ba = wu.getBankAccount(true, true);
			
			MAttachment attachment = ba.createAttachment();

			StringBuilder barredDIDs = new StringBuilder();
			String currentBarredDIDs = attachment.getTextMsg();
			if (currentBarredDIDs != null && currentBarredDIDs.length() > 0)
				barredDIDs.append(currentBarredDIDs + ",");
			
			barredDIDs.append(DIDs);
			
			attachment.setTextMsg(barredDIDs.toString());
			return attachment.save();
		}
		
		return true;
	}
	
	protected static boolean recordBarredDIDNumbers(boolean validatedCC, WebUser wu, ArrayList<String> DIDs)
	{
		String csDIDs = DIDs.toString();
		csDIDs = csDIDs.substring(1, csDIDs.length() - 1);		
		return recordBarredDIDNumbers(validatedCC, wu, csDIDs);
	}
	
	private static void addSubscriptions(Properties ctx, WebUser wu, MOrder order, ArrayList<MProduct> cvoiceProducts, ArrayList<MProduct> voicemailProducts, HashMap<String, ArrayList<String>> errorMsgs)
	{	
		if (order != null)
		{
			for (MOrderLine ol : order.getLines())
			{
				MProduct product = ol.getProduct();
				
				if (product != null)
				{
					String didNumber = DIDUtil.getDIDNumber(ctx, product, null);
					if (didNumber != null && didNumber.length() > 0)
					{		
						ArrayList<String> msgs = errorMsgs.get(didNumber);
						if (msgs == null)
							msgs = new ArrayList<String>();
						
						boolean updateErrorMsgs = false;
						
						int M_Product_ID = product.getM_Product_ID();
						int M_AttributeSetInstance_ID = product.getM_AttributeSetInstance_ID();
						
						// Only add subscriptions for monthly product
						if (!DIDUtil.isSetup(ctx, product, null))
						{
							if (!createMSubscription(ctx, wu.getC_BPartner_ID(), M_Product_ID, "+" + didNumber))
							{
								log.warning("Could not create DID monthly prodyct subscription for " + didNumber + ", MProduct[" + M_Product_ID + "], MOrder[" + order.getC_Order_ID() + "]");								
								msgs.add("DID monthly product subscription, MProduct[" + M_Product_ID + "]");								
								updateErrorMsgs = true;
							}
	
							// Add Calling products
//							HashMap<Integer, Object> attributes = new HashMap<Integer, Object>();
//							attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, DIDConstants.ATTRIBUTE_VALUE_INBOUND_CDR_USERNAME.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber).replace(DIDConstants.DOMAIN_IDENTIFIER, "conversant.co.nz"));
//							attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_APPLICATION, DIDConstants.ATTRIBUTE_ID_CDR_APPLICATION_VALUE_AUDIO);
//							attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION, DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_INBOUND);
//							attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_NUMBER, didNumber);
//							
//							MProduct inbound = DIDUtil.createCallProduct(ctx, attributes, null);
//							if (inbound == null)
//							{
//								log.warning("Failed to create CALL-IN-" + didNumber);
//								msgs.add("Failed to create CALL-IN-" + didNumber);
//								updateErrorMsgs = true;
//							}
//							
//							if (!DIDController.updateProductPrice(ctx, 1000000, inbound.getM_Product_ID(), Env.ZERO, null))
//							{
//								log.warning("Failed to create price for " + inbound);
//								msgs.add("Failed to create price for " + inbound);
//								updateErrorMsgs = true;
//							}
//							
//							attributes.remove(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME);
//							attributes.remove(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION);
//							attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, DIDConstants.ATTRIBUTE_VALUE_OUTBOUND_CDR_USERNAME.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber).replace(DIDConstants.DOMAIN_IDENTIFIER, "conversant.co.nz"));
//							attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION, DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_OUTBOUND);
//							
//							MProduct outbound = DIDUtil.createCallProduct(ctx, attributes, null);
//							if (outbound == null)
//							{
//								log.warning("Failed to create CALL-OUT-" + didNumber);
//								msgs.add("Failed to create CALL-OUT-" + didNumber);
//								updateErrorMsgs = true;
//							}
//							
//							if (!DIDController.updateProductPrice(ctx, 1000000, outbound.getM_Product_ID(), Env.ZERO, null))
//							{
//								log.warning("Failed to create price for " + outbound);
//								msgs.add("Failed to create price for " + outbound);
//								updateErrorMsgs = true;
//							}
							
							// Load Calling products
							MProduct[] callProducts = DIDUtil.getCallProducts(ctx, didNumber, null);
							if (callProducts.length == 2)
							{
								MProduct inbound = callProducts[0];
								MProduct outbound = callProducts[1];
								
								if (!DIDUtil.isInbound(ctx, inbound, null))
								{
									inbound = callProducts[1];
									outbound = callProducts[0];
								}
								
								// Add Calling subscriptions
								MSubscription inboundSubscription = DIDUtil.createCallSubscription(ctx, didNumber, wu.getC_BPartner_ID(), wu.getC_BPartner_Location_ID(), inbound.getM_Product_ID(), null);
								if (inboundSubscription == null)
								{
									log.warning("Failed to create inbound subscription for " + inbound);
									msgs.add("Failed to create inbound subscription for " + inbound);
									updateErrorMsgs = true;
								}
								
								MSubscription outboundSubscription = DIDUtil.createCallSubscription(ctx, didNumber, wu.getC_BPartner_ID(), wu.getC_BPartner_Location_ID(), outbound.getM_Product_ID(), null);
								if (outboundSubscription == null)
								{
									log.warning("Failed to create outbound subscription for " + outbound);
									msgs.add("Failed to create outbound subscription for " + outbound);
									updateErrorMsgs = true;
								}
							}
							else
							{
								log.warning("Failed to load two Call products for " + didNumber + " - Calling subscriptions were not created");
								msgs.add("Failed to load two Call products for " + didNumber + " - Calling subscriptions were not created");
								updateErrorMsgs = true;
							}
						}
		
						if (!setDIDSubscribed(ctx, M_Product_ID, M_AttributeSetInstance_ID, true))
						{
							log.warning("Could not set DID product as subscribed for " + didNumber + ", MProduct[" + M_Product_ID + "]");
							msgs.add("DID monthly product subscribed attribute, MProduct[" + M_Product_ID + "]");
							updateErrorMsgs = true;
						}

						if (updateErrorMsgs)
							errorMsgs.put(didNumber, msgs);	
					}
				}
			}
		}
		else
		{
			log.warning("Order == NULL, couldn't create DID monthly product subscriptions");
		}
		
		// create subscription for each product in sipProducts array
		for (MProduct product : cvoiceProducts)
		{
			if (!createMSubscription(ctx, wu.getC_BPartner_ID(), product.get_ID(), DIDUtil.getSIPURI(ctx, product, null)))
			{
				log.warning("Could not create CVoice product subscription, MProduct[" + product.get_ID() + "]");
				
				String didNumber = DIDUtil.getSIPAddress(ctx, product, null);
				ArrayList<String> msgs = errorMsgs.get(didNumber);
				if (msgs == null)
					msgs = new ArrayList<String>();
				
				msgs.add("CVoice product subscription, MProduct[" + product.get_ID() + "]");				
				errorMsgs.put(didNumber, msgs);
			}
		}
		
		// create subscription for each product in voicemailProducts array
		for (MProduct product : voicemailProducts)
		{
			String mailboxNumber = DIDUtil.getVoicemailMailboxNumber(ctx, product, null);
			if (!createMSubscription(ctx, wu.getC_BPartner_ID(), product.get_ID(), DIDConstants.VOICEMAIL_SUBSCRIBER_NAME.replace(DIDConstants.NUMBER_IDENTIFIER, mailboxNumber)))
			{
				log.warning("Could not create Voicemail product subscription, MProduct[" + product.get_ID() + "]");
				
				ArrayList<String> msgs = errorMsgs.get(mailboxNumber);
				if (msgs == null)
					msgs = new ArrayList<String>();
				
				msgs.add("Voicemail product subscription, MProduct[" + product.get_ID() + "]");				
				errorMsgs.put(mailboxNumber, msgs);
			}
		}
	}
	
	private static boolean createMSubscription(Properties ctx, int C_BPartner_ID, int M_Product_ID, String name)
	{
		Timestamp now = new Timestamp(System.currentTimeMillis());
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeInMillis(now.getTime());
		
		cal.set(GregorianCalendar.MONTH, cal.get(GregorianCalendar.MONTH) + 1); // add month
		cal.add(GregorianCalendar.DAY_OF_MONTH, -1); // subtract one day
		Timestamp paidUntil = new Timestamp(cal.getTimeInMillis());
		
		cal.add(GregorianCalendar.YEAR, 200); // add 200 years		
		Timestamp distantFuture = new Timestamp(cal.getTimeInMillis());
		
		// Remove any existing subscriptions which shouldn't be there
		MSubscription[] subscriptions = MSubscription.getSubscriptions(ctx, M_Product_ID, C_BPartner_ID, null);
		if (subscriptions.length > 0)
		{
			for (int i=0; i < subscriptions.length; i++)
			{
				if (!subscriptions[i].delete(true))
				{
					subscriptions[i].setIsActive(false);
					subscriptions[i].save();
				}
			}
		}
		
		MSubscription subscription = new MSubscription(ctx, 0, null);
		subscription.setC_BPartner_ID(C_BPartner_ID);
		subscription.setC_SubscriptionType_ID(DIDConstants.C_SUBSCRIPTIONTYPE_ID_MONTH_1);
		subscription.setM_Product_ID(M_Product_ID);
		subscription.setName(name);
		subscription.setStartDate(now);
		subscription.setPaidUntilDate(paidUntil);
		subscription.setRenewalDate(distantFuture);
		subscription.setIsDue(false);
		
		if (subscription.save())
		{
			return true;
		}
		else
		{
			// TODO: What should happen when a subscription isn't saved? Because both setup and monthly product will have SUBSCRIBED attribute set to true
			log.severe("Couldn't save subscription, M_Product_ID=" + M_Product_ID + ", C_BPartner_ID=" + C_BPartner_ID + ", Name=" + name);						
		}
		
		return false;
	}
	
	private static boolean setDIDSubscribed(Properties ctx, int M_Product_ID, int M_AttributeSetInstance_ID, boolean subscribed)
	{	
		if (M_AttributeSetInstance_ID > 0)
		{
			boolean attributeSet = false;
			
			MAttributeSetInstance masi = MAttributeSetInstance.get(ctx, M_AttributeSetInstance_ID, M_Product_ID);
			if (masi == null) 
			{
				log.severe("Could not load MAttributeSetInstance using M_Product_ID=" + M_Product_ID + " and M_AttributeSetInstance_ID=" + M_AttributeSetInstance_ID);
			}
			else
			{
				MAttributeSet as = masi.getMAttributeSet();
				for (MAttribute attribute : as.getMAttributes(false))
				{
					String attributeName = attribute.getName();
					if (attributeName.equalsIgnoreCase("DID_SUBSCRIBED"))
					{
						attribute.setMAttributeInstance(M_AttributeSetInstance_ID, Boolean.toString(true));
						attributeSet = true;
						break;
					}
				}
				
				// save AttributeSetInstance once values have been set
				if (attributeSet)
				{
					masi.setDescription();
					return masi.save();
				}
				else
					log.warning("Could not find DID_SUBSCRIBED for M_Product_ID=" + M_Product_ID);
			}
		}
		else
		{
			log.severe("Product does not have any attributes set! M_Product_ID=" + M_Product_ID);
		}
		
		return false;
	}
	
// *************************************************************************************************	
	
	/**
	 * Get DID products to add to the web basket
	 * 
	 * @param request
	 * @return 
	 */
	protected static ArrayList<MProduct> getDIDProductsToAdd(HttpServletRequest request)
	{
		Properties ctx = JSPEnv.getCtx(request);
		
		// setup return arraylist
		ArrayList<MProduct> allProducts = new ArrayList<MProduct>();
		
		// search for DIDNumber to add ( Add_12345678 = 12345678 )
		String didNumber = null;
		Enumeration en = request.getParameterNames();
		while (en.hasMoreElements())
		{
			String parameter = (String)en.nextElement();
			if (parameter.startsWith("Add_"))
			{
				didNumber = parameter.substring(4).trim();
				break;
			}
		}

		// get position of submitted DIDNumber
		String[] values = request.getParameterValues("didNumber");
		int row = -1;
		for (int i=0; i < values.length; i++)
		{
			if (values[i].equals(didNumber))
			{
				row = i;
				break;
			}
		}
		
		if (row >= 0 && didNumber != null)
		{
			// create new product flag to handle data inconsistency 
			boolean createNewProducts = true;
			
			// setup product and price objects
			MProduct setupProduct = null;
			MProduct monthlyProduct = null;
			
			// check if products exist (using didNumber attribute of each product)
			MProduct[] products = DIDUtil.getDIDProducts(ctx, didNumber, null);
			
			// make sure both setup and monthly products exist 
			if (products.length == 2) 
			{		
				for (MProduct product : products)
				{
					MAttributeSet as = product.getAttributeSet();
					if (as != null)
					{
						for (MAttribute attribute : as.getMAttributes(false))
						{
							if (attribute.getName().equalsIgnoreCase("DID_ISSETUP"))
							{
								MAttributeInstance mai = attribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
								if (mai != null && mai.getValue().equalsIgnoreCase("true"))
									setupProduct = product;					
								else
									monthlyProduct = product;
							}
							else if (attribute.getName().equalsIgnoreCase("DID_SUBSCRIBED"))
							{
								MAttributeInstance mai = attribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
								
								// product is subscribed unless explicitly set to false
								if (mai != null && !mai.getValue().equalsIgnoreCase("false"))
								{
									log.info("Product(s) relating to DID number " + didNumber + " are currently flagged as subscribed");
									allProducts.clear();
									return allProducts;
								}
							}
						}
					}
				}	

				// not didx number so updating below can be skipped
				if (!DIDUtil.isDIDxNumber(ctx, monthlyProduct))
				{
					log.info("DID number " + didNumber + " is not a DIDx.net number and will not be updated");
					allProducts.add(setupProduct);
					allProducts.add(monthlyProduct);
					return allProducts;
				}
				
				createNewProducts = false;
			}
			// products returned but only 1
			else if (products.length == 1)
			{
				log.warning("Only ONE product matching DIDNumber=" + didNumber + " was found, invalidating M_Product_ID=" + products[0].get_ID() + " as " + DIDConstants.INVALID_PRODUCT_NAME);
				invalidateProduct(products[0]);
			}
			// products returned but more than 2
			else if (products.length > 2)
			{
				log.warning("More than TWO products matching DIDNumber=" + didNumber + " were found, renaming as " + DIDConstants.INVALID_PRODUCT_NAME);
				for (MProduct product : products)
				{
					log.warning("Invalidating M_Product_ID=" + product.get_ID() + "..");
					invalidateProduct(product);
				}
			}
			
			// load DID number's attribute values
			String countryId = request.getParameterValues("countryId")[row];
			String countryCode = request.getParameterValues("countryCode")[row];
			String areaCode = request.getParameterValues("areaCode")[row];
			String setupCost = request.getParameterValues("setupCost")[row];
			String monthlyCharge = request.getParameterValues("monthlyCharge")[row];
			String perMinCharges = request.getParameterValues("perMinCharges")[row];
			String vendorRating = request.getParameterValues("vendorRating")[row];
			String areaCodeDescription = request.getParameterValues("description")[row];
			
			// get fields and values
			HashMap<String, String> setupFields = getDIDSetupFields(didNumber, areaCodeDescription);
			HashMap<String, String> monthlyFields = getDIDMonthlyFields(didNumber, areaCodeDescription);
			
			// no products found 
			if (createNewProducts)
			{		
				setupProduct = createMProduct(ctx, setupFields);
				if (setupProduct == null)
				{
					log.severe("Failed to create DID Setup Product");
					
					allProducts.clear();
					return allProducts;
				}
				
				monthlyProduct = createMProduct(ctx, monthlyFields);
				if (monthlyProduct == null)
				{
					log.severe("Failed to create DID Monthly Product");
					
					if (setupProduct != null)
						invalidateProduct(setupProduct);
					
					allProducts.clear();
					return allProducts;
				}
				
/* *****
				// set up WSWindow and field objects
				WSWindow win = new WSWindow(DIDConstants.AD_WINDOW_ID_M_PRODUCT, DIDConstants.AD_MENU_ID_M_PRODUCT, request);
				
				// create Product for Setup of DID Number 
				int DIDSetup_M_Product_ID = win.createRecord(DIDConstants.M_PRODUCT_PRODUCT_TAB_ID, setupFields);
				if (DIDSetup_M_Product_ID == WSWindow.RECORD_NOT_SAVED)
				{	
					log.severe("Failed to create DID Setup Product");
					allProducts.clear();
					return allProducts;
				}
						
				// create Product for Monthly DID Number
				int DIDMonthly_M_Product_ID = win.createRecord(DIDConstants.M_PRODUCT_PRODUCT_TAB_ID, monthlyFields);
				if (DIDMonthly_M_Product_ID  == WSWindow.RECORD_NOT_SAVED)
				{	
					// invalidate the SETUP product if failed while creating monthly product 
					if (DIDSetup_M_Product_ID != WSWindow.RECORD_NOT_SAVED)
						invalidateProduct(MProduct.get(ctx, DIDSetup_M_Product_ID));
			
					log.severe("Failed to create DID Monthly Product, invalidated the previously created DID Setup Product - M_Product_ID=" + DIDSetup_M_Product_ID);
					allProducts.clear();					
					return allProducts;
				}			
				
				// get newly created MProduct objects
				setupProduct = MProduct.get(ctx, DIDSetup_M_Product_ID);
				monthlyProduct = MProduct.get(ctx, DIDMonthly_M_Product_ID);
* *****/
			}
			
			// load PriceList Version ID
			int M_PriceList_Version_ID = WebUtil.getParameterAsInt (request, "M_PriceList_Version_ID");
			
			BigDecimal setupPrice = null;
			BigDecimal monthlyPrice = null;
			
			// parse prices
			try
			{
				setupPrice = new BigDecimal(setupCost);
				monthlyPrice = new BigDecimal(monthlyCharge);
			}
			catch (NumberFormatException ex)
			{
				log.log(Level.SEVERE, "Could not parse setupCost and/or monthlyCharge", ex);
				return allProducts;
			}
			
			// double check both products and both prices exist
			if (setupProduct != null && monthlyProduct != null && setupPrice != null && monthlyPrice != null)
			{
				// get Free Mins
				String freeMins = DIDXService.getDIDFreeMinInfo(ctx, didNumber);
				
				// update products name, search key, description, attributes, prices, BP info, purchaser info & product relations
				updateProducts(ctx, setupProduct, monthlyProduct, DIDConstants.BP_SUPER_TECH_INC_ID, areaCodeDescription, didNumber, perMinCharges, 
							   areaCode, vendorRating, countryId, countryCode, freeMins, M_PriceList_Version_ID, setupPrice, monthlyPrice,
							   setupFields, monthlyFields);
				
				allProducts.add(setupProduct);
				allProducts.add(monthlyProduct);
			}
			else
			{
				log.severe("Invalidating both DID Setup & Monthly products as one or both are null - DIDSetup=" + (setupProduct == null ? "null" : setupProduct.toString()) + " DIDMonthly=" + (monthlyProduct == null ? "null" : monthlyProduct.toString()));
				invalidateProduct(setupProduct);
				invalidateProduct(monthlyProduct);
				allProducts.clear();
				return allProducts;
			}
		}
			
		return allProducts;
	}

	public static MProduct createMProduct(Properties ctx, HashMap<String, String> fields)
	{	
		MProduct product = new MProduct(ctx, 0, null);
		product.setValue(fields.get("Value"));
		product.setName(fields.get("Name"));
		product.setDescription(fields.get("Description"));
		product.setM_Product_Category_ID(Integer.parseInt(fields.get("M_Product_Category_ID")));
		product.setC_TaxCategory_ID(Integer.parseInt(fields.get("C_TaxCategory_ID")));
		product.setC_UOM_ID(Integer.parseInt(fields.get("C_UOM_ID")));
		product.setM_AttributeSet_ID(Integer.parseInt(fields.get("M_AttributeSet_ID")));
		product.setProductType(fields.get("ProductType"));
		product.setIsSelfService(fields.get("IsSelfService").equals("Y"));
		
		product.save();
		
		return product;
	}

	/**
	 * Update products name, search key, description, attributes, prices, BP info, purchaser info & product relations
	 */
	private static void updateProducts(Properties ctx, MProduct setupProduct, MProduct monthlyProduct, int C_BPartner_ID, String areaCodeDescription, 
								String didNumber, String perMinCharges, String areaCode, String vendorRating, String countryId, String countryCode, 
								String freeMins, int M_PriceList_Version_ID, BigDecimal setupPrice, BigDecimal monthlyPrice,
								HashMap<String, String> setupFields, HashMap<String, String> monthlyFields)
	{	
		// convert prices NZD back to original USD
		BigDecimal setupPriceUSD = MConversionRate.convert(ctx,  setupPrice, DIDConstants.NZD_CURRENCY_ID, DIDConstants.USD_CURRENCY_ID, null, DIDConstants.CUSTOM_CONV_TYPE_ID, Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
		BigDecimal monthlyPriceUSD = MConversionRate.convert(ctx, monthlyPrice, DIDConstants.NZD_CURRENCY_ID, DIDConstants.USD_CURRENCY_ID, null, DIDConstants.CUSTOM_CONV_TYPE_ID, Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
		
		// if rate not found use fall back constant
		if (setupPriceUSD == null || monthlyPriceUSD == null) 
		{
			setupPriceUSD = setupPrice.divide(new BigDecimal(DIDConstants.FALLBACK_USD_TO_NZD_RATE));
			monthlyPriceUSD = monthlyPrice.divide(new BigDecimal(DIDConstants.FALLBACK_USD_TO_NZD_RATE));
			log.severe("Could not find a rate in CurrencyRate table, using fallback USD to NZD rate of " + DIDConstants.FALLBACK_USD_TO_NZD_RATE + "NZD = 1USD");
		}
		
		// setup product
		updateProductNameSearchKeyDesc(setupProduct, setupFields);
		updateProductAttributes(ctx, setupProduct.getM_AttributeSetInstance_ID(), setupProduct.get_ID(), areaCodeDescription, didNumber, perMinCharges, areaCode, vendorRating, countryId, countryCode, freeMins, true, false);
		
		// update product incase M_AttributeSet was created (reload M_AttributeSetInstance_ID)
		setupProduct.load(null);
		
		updateProductPrice(ctx, M_PriceList_Version_ID, setupProduct.get_ID(), setupPrice);
		updateBPPriceListPrice(ctx, C_BPartner_ID, setupProduct.get_ID(), setupPriceUSD);
		updateProductPO(ctx, C_BPartner_ID, setupProduct, setupPriceUSD, DIDConstants.USD_CURRENCY_ID);
		
		// monthly product
		updateProductNameSearchKeyDesc(monthlyProduct, monthlyFields);
		updateProductAttributes(ctx, monthlyProduct.getM_AttributeSetInstance_ID(), monthlyProduct.get_ID(), areaCodeDescription, didNumber, perMinCharges, areaCode, vendorRating, countryId, countryCode, freeMins, false, false);
		
		// update product incase M_AttributeSet was created (reload M_AttributeSetInstance_ID)
		monthlyProduct.load(null);
		
		updateProductPrice(ctx, M_PriceList_Version_ID, monthlyProduct.get_ID(), monthlyPrice);
		updateBPPriceListPrice(ctx, C_BPartner_ID, monthlyProduct.get_ID(), monthlyPriceUSD);
		updateProductPO(ctx, C_BPartner_ID, monthlyProduct, monthlyPriceUSD, DIDConstants.USD_CURRENCY_ID);
		
		// add relation
		updateProductRelations(ctx, monthlyProduct.get_ID(), setupProduct.get_ID()); 
	}
	
	protected static void updateProductNameSearchKeyDesc(MProduct product, HashMap<String, String> fields)
	{
		if (product != null && fields != null)
		{
			product.setValue(fields.get("Value"));
			product.setName(fields.get("Name"));
			product.setDescription(fields.get("Description"));
			product.save();
		}
	}
	
	public static boolean updateProductRelations(Properties ctx, int M_Product_ID, int M_RelatedProduct_ID)
	{
		return updateProductRelations(ctx, M_Product_ID, M_RelatedProduct_ID, null);
	}

	public static boolean updateProductRelations(Properties ctx, int M_Product_ID, int M_RelatedProduct_ID, String trxName)
	{
		MRelatedProduct[] allRelatedProducts = MRelatedProduct.getOfProduct(ctx, M_Product_ID, trxName);
		
		if (allRelatedProducts.length > 1)
		{
			for (MRelatedProduct rprod : allRelatedProducts)
			{
				if (rprod.getRelatedProductType().equalsIgnoreCase(DIDConstants.RELATED_PRODUCT_TYPE_SETUP))
				{
					if (!rprod.delete(true))
					{
						rprod.setIsActive(false);
						rprod.setName(DIDConstants.INVALID_RELATION_NAME);
						rprod.setDescription(DIDConstants.INVALID_RELATION_NAME);
						rprod.save();
					}
				}
			}
		}
		
		MRelatedProduct relatedProduct = null;
		
		if (allRelatedProducts.length > 0)
		{
			relatedProduct = allRelatedProducts[0];
		}
		else
		{
			// Remove all entries from M_Product_PO where no product is found for them
			DB.executeUpdate("DELETE FROM M_RELATEDPRODUCT WHERE M_PRODUCT_ID NOT IN (SELECT M_PRODUCT_ID FROM M_PRODUCT)", trxName);
			
			relatedProduct = new MRelatedProduct(ctx, 0, trxName);
		}

		if (relatedProduct != null)
		{
			relatedProduct.setName(DIDConstants.RELATED_PRODUCT_NAME);
			relatedProduct.setDescription(DIDConstants.RELATED_PRODUCT_DESC);
			relatedProduct.setM_Product_ID(M_Product_ID);
			relatedProduct.setRelatedProduct_ID(M_RelatedProduct_ID);
			relatedProduct.setRelatedProductType(DIDConstants.RELATED_PRODUCT_TYPE_SETUP);

			return relatedProduct.save();
		}
		else 
		{
			log.severe("RelatedProduct == NULL, check coding");
			return false;
		}
	}

	public static boolean updateProductPO(Properties ctx, int C_BPartner_ID, MProduct product, BigDecimal price, int C_Currency_ID)
	{
		return updateProductPO(ctx, C_BPartner_ID, product, price, C_Currency_ID, null);
	}
	
	public static boolean updateProductPO(Properties ctx, int C_BPartner_ID, MProduct product, BigDecimal price, int C_Currency_ID, String trxName)
	{
		// Get existing PO's and set to inactive
		MProductPO[] allProductsPO = MProductPO.getOfProduct(ctx, product.get_ID(), trxName);
		if (allProductsPO.length > 1)
		{
			// skip first product po as will update this one and use as current
			for (int i=1; i < allProductsPO.length; i++)
			{
				MProductPO productPO = allProductsPO[i];
				if (productPO != null && (productPO.isActive() || !productPO.getVendorProductNo().contains(DIDConstants.PRODUCT_PO_INVALID_VENDOR_PRODUCT_NO)))
				{
					productPO.setIsActive(false); 
					productPO.setDiscontinued(true);
					productPO.setIsCurrentVendor(false);
					productPO.setVendorProductNo(DIDConstants.PRODUCT_PO_INVALID_VENDOR_PRODUCT_NO + " " + new Date());
					productPO.save();
				}
			}
		}

		MProductPO productPO = null;
		
		if (allProductsPO.length > 0)
		{
			productPO = allProductsPO[0];
		}
		else
		{
			// Remove all entries from M_Product_PO where no product is found for them
			DB.executeUpdate("DELETE FROM M_PRODUCT_PO WHERE M_PRODUCT_ID NOT IN (SELECT M_PRODUCT_ID FROM M_PRODUCT)", trxName);
			
			productPO = new MProductPO(ctx, 0, trxName);
		}
		
		if (productPO != null)
		{
			String number = DIDUtil.getDIDNumber(ctx, product, trxName);
			String vendorProductNo = "";
			if (number != null)
			{			
				if (DIDUtil.isSetup(ctx, product, trxName))
					vendorProductNo = DIDConstants.PRODUCT_PO_SETUP_VENDOR_PRODUCT_NO.replace(DIDConstants.NUMBER_IDENTIFIER, number);
				else
					vendorProductNo = DIDConstants.PRODUCT_PO_MONTHLY_VENDOR_PRODUCT_NO.replace(DIDConstants.NUMBER_IDENTIFIER, number);
			}
			else
			{				
				log.warning("Couldn't load products DID number, setting VendorProductNo with time and date.");
				
				// setting vendor product no with time/date to avoid unique constraint violations
				Calendar cal = Calendar.getInstance();
			    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
			    vendorProductNo = sdf.format(cal.getTime());
			}
			
			productPO.setC_BPartner_ID(C_BPartner_ID);
			productPO.setM_Product_ID(product.get_ID());
			productPO.setIsCurrentVendor(true);
			productPO.setC_Currency_ID(C_Currency_ID);
			productPO.setC_UOM_ID(product.getC_UOM_ID());
			productPO.setPriceList(price);
			productPO.setPricePO(price);	
			productPO.setVendorProductNo(vendorProductNo);
			
			return productPO.save();
		}
		else 
		{
			log.severe("ProductPO = null, check coding");
			return false;
		}
	}
	
	public static boolean updateBPPriceListPrice(Properties ctx, int C_BPartner_ID, int M_Product_ID, BigDecimal price)
	{
		return updateBPPriceListPrice(ctx, C_BPartner_ID, M_Product_ID, price, null);
	}
	
	/**
	 * Updates Business Partner PriceList price
	 * 
	 * @param ctx
	 * @param C_BPartner_ID
	 * @param M_Product_ID
	 * @param price
	 * @return
	 */
	public static boolean updateBPPriceListPrice(Properties ctx, int C_BPartner_ID, int M_Product_ID, BigDecimal price, String trxName)
	{
		int M_PriceList_ID = 0;
		MPriceList pl = null;
		MPriceListVersion plv = null;
		
		MBPartner bp = MBPartner.get(ctx, C_BPartner_ID);
		if (bp != null)
		{
			M_PriceList_ID = bp.getPO_PriceList_ID();
			if (M_PriceList_ID != 0)
			{
				pl = MPriceList.get(ctx, M_PriceList_ID, trxName);
				if (pl != null)
				{
					plv = pl.getPriceListVersion(null); // null == today
					if (plv != null)
					{
						return updateProductPrice(ctx, plv.getM_PriceList_Version_ID(), M_Product_ID, price, trxName);
					}
				}
			}
		}
		log.warning("BPartner PriceList entry was not added - " + (bp == null ? "MBPartner=null" : bp.toString()) + "M_PriceList_ID=" + M_PriceList_ID + (plv == null ? "MPriceListVersion=null" : "MPriceListVersion_ID=" + plv.get_ID()));
		return false;
	}

	public static boolean updateProductPrice(Properties ctx, int M_PriceList_Version_ID, int M_Product_ID, BigDecimal price)
	{
		return updateProductPrice(ctx, M_PriceList_Version_ID, M_Product_ID, price, null);
	}
	
	/**
	 * Updates Product Price (creates new if doesn't exist)
	 * 
	 * @param ctx
	 * @param M_PriceList_Version_ID
	 * @param M_Product_ID
	 * @param price
	 * @return true if successfully saved
	 */
	public static boolean updateProductPrice(Properties ctx, int M_PriceList_Version_ID, int M_Product_ID, BigDecimal price, String trxName)
	{
		MProductPrice productPrice = MProductPrice.get(ctx, M_PriceList_Version_ID, M_Product_ID, trxName);
		// Create new if null
		if (productPrice == null)
		{
			productPrice = new MProductPrice(ctx, M_PriceList_Version_ID, M_Product_ID, trxName);
		}
		productPrice.setPrices(price, price, price);
		return productPrice.save();
	}	// updateProductPrice
	
	/**
	 * Adds/Updates a Products attributes
	 * 
	 * @param ctx
	 * @param M_AttributeSetInstance_ID
	 * @param M_Product_ID
	 * @param areaCodeDescription
	 * @param didNumber
	 * @param perMinCharges
	 * @param areaCode
	 * @param vendorRating
	 * @param countryCode
	 * @param freeMins
	 * @param isSetup
	 * @return
	 */
	public static boolean updateProductAttributes(Properties ctx, int M_AttributeSetInstance_ID, int M_Product_ID, String areaCodeDescription, String didNumber, String perMinCharges, String areaCode, String vendorRating, String countryId, String countryCode, String freeMins, boolean isSetup, boolean isSubscribed)
	{	
		// Create new AttributeSetInstance
		MAttributeSetInstance masi = MAttributeSetInstance.get(ctx, M_AttributeSetInstance_ID, M_Product_ID);
		if (masi == null) 
		{
			log.severe("Could not load MAttributeSetInstance using M_Product_ID=" + M_Product_ID);
			return false;
		}
		if (masi.getM_AttributeSetInstance_ID() == 0) masi.save ();
		
		// Get AttributeSetInstanceId and set values
		if (M_AttributeSetInstance_ID == 0) M_AttributeSetInstance_ID = masi.getM_AttributeSetInstance_ID();
		MAttributeSet as = masi.getMAttributeSet();
		for (MAttribute attribute : as.getMAttributes(false))
		{
			String attributeName = attribute.getName();
			String value = "";
			
			if (attributeName.equalsIgnoreCase("DID_DESCRIPTION")) value = areaCodeDescription;
			else if (attributeName.equalsIgnoreCase("DID_NUMBER")) value = didNumber;
			else if (attributeName.equalsIgnoreCase("DID_PERMINCHARGES")) value = perMinCharges;
			else if (attributeName.equalsIgnoreCase("DID_AREACODE")) value = areaCode;
			else if (attributeName.equalsIgnoreCase("DID_VENDORRATING")) value = vendorRating;
			else if (attributeName.equalsIgnoreCase("DID_COUNTRYID")) value = countryId;
			else if (attributeName.equalsIgnoreCase("DID_COUNTRYCODE")) value = countryCode;
			else if (attributeName.equalsIgnoreCase("DID_FREEMINS")) value = freeMins;
			else if (attributeName.equalsIgnoreCase("DID_ISSETUP")) value = Boolean.toString(isSetup);
			else if (attributeName.equalsIgnoreCase("DID_SUBSCRIBED")) value = Boolean.toString(isSubscribed);
			else if (attributeName.equalsIgnoreCase("DID_FAX_ISFAX")) value = Boolean.toString(false);
			else if (attributeName.equalsIgnoreCase("DID_FAX_TOEMAIL")) value = "-";
			else if (attributeName.equalsIgnoreCase("DID_FAX_FROMEMAIL")) value = "-";
			
			if (value == null || value.equals(""))
			{
				value = "-";
				log.warning("A value for " + attributeName + " could not be loaded, setting value to '" + value + "'. Check AttributeSet hasn't changed.");
			}
			
			attribute.setMAttributeInstance(M_AttributeSetInstance_ID, value);
		}
		
		// Save AttributeSetInstance once values have been set
		masi.setDescription();
		masi.save();
		
		// Update product's AttributeSetInstance Id
		MProduct product = MProduct.get(ctx, M_Product_ID);
		product.setM_AttributeSetInstance_ID(M_AttributeSetInstance_ID);
		return product.save();
	}

	public static boolean updateSIPProductAttributes(Properties ctx, MProduct product, String address, String domain)
	{
		boolean retValue = false;
		
		if (product != null)
		{
			// Create new AttributeSetInstance
			MAttributeSetInstance masi = MAttributeSetInstance.get(ctx, product.getM_AttributeSetInstance_ID(), product.get_ID());
			if (masi == null) 
			{
				log.severe("Could not load MAttributeSetInstance using M_Product_ID=" + product.get_ID());
				return retValue;
			}
			if (masi.getM_AttributeSetInstance_ID() == 0) 
			{
				masi.save();
				product.setM_AttributeSetInstance_ID(masi.get_ID());
				product.save();
			}
			
			MAttributeSet as = masi.getMAttributeSet();
			for (MAttribute attribute : as.getMAttributes(false))
			{
				String attributeName = attribute.getName();
				String value = "";
				
				if (attributeName.equalsIgnoreCase("SIP_ADDRESS")) value = address;
				else if (attributeName.equalsIgnoreCase("SIP_DOMAIN")) value = domain;
				
				if (value == null || value.equals(""))
				{
					value = "-";
					log.warning("A value for " + attributeName + " could not be loaded, setting value to '" + value + "'. Check AttributeSet hasn't changed.");
				}
				
				attribute.setMAttributeInstance(product.getM_AttributeSetInstance_ID(), value);
			}
			
			// Save AttributeSetInstance once values have been set
			masi.setDescription();
			retValue = masi.save();
		}
		
		return retValue;
	}
	
	public static boolean updateVoicemailProductAttributes(Properties ctx, MProduct product, String mailboxNumber, String context, String macroName)
	{
		boolean retValue = false;
		
		if (product != null)
		{
			// Create new AttributeSetInstance
			MAttributeSetInstance masi = MAttributeSetInstance.get(ctx, product.getM_AttributeSetInstance_ID(), product.get_ID());
			if (masi == null) 
			{
				log.severe("Could not load MAttributeSetInstance using M_Product_ID=" + product.get_ID());
				return retValue;
			}
			if (masi.getM_AttributeSetInstance_ID() == 0) 
			{
				masi.save();
				product.setM_AttributeSetInstance_ID(masi.get_ID());
				product.save();
			}
			
			MAttributeSet as = masi.getMAttributeSet();
			for (MAttribute attribute : as.getMAttributes(false))
			{
				String attributeName = attribute.getName();
				String value = "";
				
				if (attributeName.equalsIgnoreCase("VM_MAILBOX_NUMBER")) value = mailboxNumber;
				else if (attributeName.equalsIgnoreCase("VM_CONTEXT")) value = context;
				else if (attributeName.equalsIgnoreCase("VM_MACRO_NAME")) value = macroName;
				
				if (value == null || value.equals(""))
				{
					value = "-";
					log.warning("A value for " + attributeName + " could not be loaded, setting value to '" + value + "'. Check AttributeSet hasn't changed.");
				}
				
				attribute.setMAttributeInstance(product.getM_AttributeSetInstance_ID(), value);
			}
			
			// Save AttributeSetInstance once values have been set
			masi.setDescription();
			retValue = masi.save();
		}
		
		return retValue;
	}
	
	protected static void invalidateProduct(MProduct product)
	{
		if (product != null)
		{
			product.setDescription(product.getDescription() + " -- " + DIDConstants.INVALID_PRODUCT_NAME);
			product.setIsActive(false);
			product.save();
		}
	}
	
	public static HashMap<String, String> getDIDSetupFields(String didNumber, String areaCodeDescription)
	{
		HashMap<String, String> fields = new HashMap<String, String>();

		String searchKey = DIDConstants.DID_SETUP_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber);
		String name = DIDConstants.DID_SETUP_PRODUCT_NAME.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber);
		String desc = DIDConstants.DID_SETUP_PRODUCT_DESCRIPTION.replace(DIDConstants.DID_DESCRIPTION_IDENTIFIER, areaCodeDescription);
		
		fields.put("Value", searchKey);
		fields.put("Name", name); 
		fields.put("Description", desc);
		fields.put("M_Product_Category_ID", DIDConstants.VOICE_SERVICES_NONRECUR_NONCALL_CATEGORY_ID);
		fields.put("C_TaxCategory_ID", DIDConstants.STANDARD_15_TAX_CATEGORY); 
		fields.put("C_UOM_ID", DIDConstants.UOM_EACH);  
		fields.put("M_AttributeSet_ID", DIDConstants.DID_ATTRIBUTE_SET_ID);
		fields.put("ProductType", DIDConstants.PRODUCT_TYPE_SERVICE);
		fields.put("IsSelfService", DIDConstants.NOT_SELF_SERVICE);
		
		return fields;
	}
	
	public static HashMap<String, String> getDIDMonthlyFields(String didNumber, String areaCodeDescription)
	{
		HashMap<String, String> fields = new HashMap<String, String>();
		
		String searchKey = DIDConstants.DID_MONTHLY_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber);
		String name = DIDConstants.DID_MONTHLY_PRODUCT_NAME.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber);
		String desc = DIDConstants.DID_MONTHLY_PRODUCT_DESCRIPTION.replace(DIDConstants.DID_DESCRIPTION_IDENTIFIER, areaCodeDescription);
		
		fields.put("Value", searchKey);
		fields.put("Name", name); 
		fields.put("Description", desc);
		fields.put("M_Product_Category_ID", DIDConstants.VOICE_SERVICES_RECUR_NONCALL_CATEGORY_ID);
		fields.put("C_TaxCategory_ID", DIDConstants.STANDARD_15_TAX_CATEGORY); 
		fields.put("C_UOM_ID", DIDConstants.UOM_MONTH_8DEC); 
		fields.put("M_AttributeSet_ID", DIDConstants.DID_ATTRIBUTE_SET_ID); 
		fields.put("ProductType", DIDConstants.PRODUCT_TYPE_SERVICE);
		fields.put("IsSelfService", DIDConstants.NOT_SELF_SERVICE);
		
		return fields;
	}
	
	public static HashMap<String, String> getCVoiceFields(String didNumber)
	{
		HashMap<String, String> fields = new HashMap<String, String>();
		
		String searchKey = DIDConstants.SIP_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber);
		String name = DIDConstants.SIP_PRODUCT_NAME.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber);
		String desc = DIDConstants.SIP_PRODUCT_DESCRIPTION.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber);
		
		fields.put("Value", searchKey);
		fields.put("Name", name); 
		fields.put("Description", desc);
		
		fields.put("M_Product_Category_ID", DIDConstants.VOICE_SERVICES_CATEGORY_ID);
		fields.put("C_TaxCategory_ID", DIDConstants.STANDARD_15_TAX_CATEGORY); 
		fields.put("C_UOM_ID", DIDConstants.UOM_MONTH_8DEC); 
		fields.put("M_AttributeSet_ID", DIDConstants.SIP_ATTRIBUTE_SET_ID); 
		fields.put("ProductType", DIDConstants.PRODUCT_TYPE_SERVICE);
		fields.put("IsSelfService", DIDConstants.NOT_SELF_SERVICE);
		
		return fields;
	}
	
	public static HashMap<String, String> getVoicemailFields(String number)
	{
		HashMap<String, String> fields = new HashMap<String, String>();
		
		String searchKey = DIDConstants.VOICEMAIL_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, number);
		String name = DIDConstants.VOICEMAIL_PRODUCT_NAME.replace(DIDConstants.NUMBER_IDENTIFIER, number);
		String desc = DIDConstants.VOICEMAIL_PRODUCT_DESC.replace(DIDConstants.NUMBER_IDENTIFIER, number);
		
		fields.put("Value", searchKey);
		fields.put("Name", name); 
		fields.put("Description", desc);
		
		fields.put("M_Product_Category_ID", DIDConstants.VOICE_SERVICES_CATEGORY_ID);
		fields.put("C_TaxCategory_ID", DIDConstants.STANDARD_15_TAX_CATEGORY); 
		fields.put("C_UOM_ID", DIDConstants.UOM_MONTH_8DEC); 
		fields.put("M_AttributeSet_ID", DIDConstants.VOICEMAIL_ATTRIBUTE_SET_ID); 
		fields.put("ProductType", DIDConstants.PRODUCT_TYPE_SERVICE);
		fields.put("IsSelfService", DIDConstants.NOT_SELF_SERVICE);
		
		return fields;
	}
	
	public static void main(String[] args)
	{

	}
}
