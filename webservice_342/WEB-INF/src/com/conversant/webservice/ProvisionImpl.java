package com.conversant.webservice;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Properties;

import javax.jws.WebService;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.compiere.model.MBPartner;
import org.compiere.model.MConversionRate;
import org.compiere.model.MCurrency;
import org.compiere.model.MInvoice;
import org.compiere.model.MPriceListVersion;
import org.compiere.model.MProduct;
import org.compiere.model.MSubscription;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Trx;
import org.compiere.wstore.DIDController;

import com.conversant.db.AsteriskConnector;
import com.conversant.db.CDRToolConnector;
import com.conversant.db.RadiusConnector;
import com.conversant.db.SERConnector;
import com.conversant.did.DIDConstants;
import com.conversant.did.DIDUtil;
import com.conversant.util.Validation;
import com.conversant.webservice.util.WebServiceConstants;

@WebService(endpointInterface = "com.conversant.webservice.Provision")
public class ProvisionImpl extends GenericWebServiceImpl implements Provision
{
	private static CLogger log = CLogger.getCLogger(ProvisionImpl.class);
	
	/* Notes
	 * 
	 * - If WS Parameter set as "Free" perhaps could use "Constant Value" field as default value?
	 * - Check if Env.getCtx() creates ctx if it doesn't exist yet? And if it's already created does it get previous login ctx?
	 */
	
// ********************************************************************************************************************************************************************************

	public StandardResponse createDIDProduct(CreateDIDProductRequest createDIDProductRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(createDIDProductRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_DID_PRODUCT_METHOD_ID"), createDIDProductRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		String number = createDIDProductRequest.getNumber();
		if (!validateString(number))
			return getErrorStandardResponse("Invalid number", trxName);
		else
			number = number.trim();
		
		String countryId = createDIDProductRequest.getCountryId();
		if (!validateString(countryId))
			return getErrorStandardResponse("Invalid countryId", trxName);
		else
			countryId = countryId.trim();
		
		String countryCode = createDIDProductRequest.getCountryCode();
		if (!validateString(countryCode))
			return getErrorStandardResponse("Invalid countryCode", trxName);
		else
			countryCode = countryCode.trim();
		
		String areaCode = createDIDProductRequest.getAreaCode();
		if (!validateString(areaCode))
			return getErrorStandardResponse("Invalid areaCode", trxName);
		else
			areaCode = areaCode.trim();
		
		String areaCodeDescription = createDIDProductRequest.getAreaCodeDescription();
		if (!validateString(areaCodeDescription))
			return getErrorStandardResponse("Invalid areaCodeDescription", trxName);
		else
			areaCodeDescription = areaCodeDescription.trim();
		
		String freeMinutes = createDIDProductRequest.getFreeMinutes();
		if (!validateString(freeMinutes))
			return getErrorStandardResponse("Invalid freeMinutes", trxName);
		else
			freeMinutes = freeMinutes.trim();
		
		String perMinuteCharge = createDIDProductRequest.getPerMinuteCharge();
		if (!validateString(perMinuteCharge))
			return getErrorStandardResponse("Invalid perMinuteCharge", trxName);
		else
			perMinuteCharge = perMinuteCharge.trim();
		
		Integer businessPartnerId = createDIDProductRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		String setupCost = createDIDProductRequest.getSetupCost();
		if (!validateString(setupCost))
			return getErrorStandardResponse("Invalid setupCost", trxName);
		else
			setupCost = setupCost.trim();
		
		String monthlyCharge = createDIDProductRequest.getMonthlyCharge();
		if (!validateString(monthlyCharge))
			return getErrorStandardResponse("Invalid monthlyCharge", trxName);
		else
			monthlyCharge = monthlyCharge.trim();
		
		Integer currencyId = createDIDProductRequest.getCurrencyId();
		if (currencyId == null || currencyId < 1 || !Validation.validateADId(MCurrency.Table_Name, currencyId, trxName)) 
			return getErrorStandardResponse("Invalid currencyId", trxName);
		
		Integer pricelistVersionId = createDIDProductRequest.getPricelistVersionId();
		if (pricelistVersionId == null || pricelistVersionId < 1 || !Validation.validateADId(MPriceListVersion.Table_Name, pricelistVersionId, trxName))
			return getErrorStandardResponse("Invalid pricelistVersionId", trxName);
		
		// Check product(s) for DID number don't exist
		MProduct[] existingDIDProducts = DIDUtil.getDIDProducts(ctx, number, trxName); // TODO: Test for non existent number, NULL or empty array returned?
		if (existingDIDProducts.length > 0)
			return getErrorStandardResponse(existingDIDProducts.length + " products already exist for " + number, trxName);
				
		// Parse prices to BigDecimal and convert to NZD if needed
		BigDecimal setupCostBD = null;
		BigDecimal monthlyChargeBD = null;
		BigDecimal setupCostNZD = null;
		BigDecimal monthlyChargeNZD = null;
		BigDecimal perMinuteChargeNZD = null;
		try
		{
			setupCostBD = new BigDecimal(setupCost);
			if (currencyId != DIDConstants.NZD_CURRENCY_ID)
				setupCostNZD = MConversionRate.convert(ctx, setupCostNZD, currencyId, DIDConstants.NZD_CURRENCY_ID, null, DIDConstants.CUSTOM_CONV_TYPE_ID, Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
			else
				setupCostNZD = setupCostBD;
		}
		catch (NumberFormatException ex)
		{
			return getErrorStandardResponse("Invalid setupCost (cannot parse)", trxName);
		}

		if (setupCostBD == null || setupCostNZD == null)
			return getErrorStandardResponse("Invalid setupCost (cannot convert to NZD)", trxName);
		
		try
		{
			monthlyChargeBD = new BigDecimal(monthlyCharge);
			if (currencyId != DIDConstants.NZD_CURRENCY_ID)
				monthlyChargeNZD = MConversionRate.convert(ctx, monthlyChargeNZD, currencyId, DIDConstants.NZD_CURRENCY_ID, null, DIDConstants.CUSTOM_CONV_TYPE_ID, Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
			else
				monthlyChargeNZD = monthlyChargeBD;
		}
		catch (NumberFormatException ex)
		{
			return getErrorStandardResponse("Invalid monthlyCharge (cannot parse)", trxName);
		}
				
		if (setupCostBD == null || monthlyChargeNZD == null)
			return getErrorStandardResponse("Invalid monthlyCharge (cannot convert to NZD)", trxName);

		try
		{
			perMinuteChargeNZD = new BigDecimal(perMinuteCharge);
			if (currencyId != DIDConstants.NZD_CURRENCY_ID)
				perMinuteChargeNZD = MConversionRate.convert(ctx, perMinuteChargeNZD, currencyId, DIDConstants.NZD_CURRENCY_ID, null, DIDConstants.CUSTOM_CONV_TYPE_ID, Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
		}
		catch (NumberFormatException ex)
		{
			return getErrorStandardResponse("Invalid perMinuteCharge (cannot parse)", trxName);
		}
				
		if (perMinuteChargeNZD == null)
			return getErrorStandardResponse("Invalid perMinuteCharge (cannot convert to NZD)", trxName);
		
		// If caller not using trx create local
		boolean localTrx = false;
		if (trxName == null)
		{	
			localTrx = true;			
			trxName = Trx.createTrxName("createDIDProduct");
		}
		
		try
		{					
			// Create attributes
			HashMap<Integer, Object> attributes = new HashMap<Integer, Object>();
			attributes.put(DIDConstants.ATTRIBUTE_ID_DID_AREACODE, areaCode);
			attributes.put(DIDConstants.ATTRIBUTE_ID_DID_COUNTRYCODE, countryCode);
			attributes.put(DIDConstants.ATTRIBUTE_ID_DID_COUNTRYID, countryId);
			attributes.put(DIDConstants.ATTRIBUTE_ID_DID_DESCRIPTION, areaCodeDescription);
			attributes.put(DIDConstants.ATTRIBUTE_ID_DID_FREEMINS, freeMinutes);
			attributes.put(DIDConstants.ATTRIBUTE_ID_DID_ISSETUP, Boolean.TRUE.toString());
			attributes.put(DIDConstants.ATTRIBUTE_ID_DID_NUMBER, number);
			attributes.put(DIDConstants.ATTRIBUTE_ID_DID_PERMINCHARGES, perMinuteChargeNZD.toPlainString());
			attributes.put(DIDConstants.ATTRIBUTE_ID_DID_SUBSCRIBED, Boolean.FALSE.toString());
			attributes.put(DIDConstants.ATTRIBUTE_ID_DID_VENDORRATING, "5");
			attributes.put(DIDConstants.ATTRIBUTE_ID_DID_FAX_ISFAX, Boolean.FALSE.toString());
			attributes.put(DIDConstants.ATTRIBUTE_ID_DID_FAX_FROMEMAIL, "-");
			attributes.put(DIDConstants.ATTRIBUTE_ID_DID_FAX_TOEMAIL, "-");
			
			// Create DID products
			MProduct setupDIDProduct = DIDUtil.createDIDProduct(ctx, attributes, trxName);
			if (setupDIDProduct == null)
				throw new Exception("Failed to create setup product for " + number);
	
			attributes.remove(DIDConstants.ATTRIBUTE_ID_DID_ISSETUP);
			attributes.put(DIDConstants.ATTRIBUTE_ID_DID_ISSETUP, Boolean.FALSE.toString());
			
			MProduct monthlyDIDProduct = DIDUtil.createDIDProduct(ctx, attributes, trxName);
			if (monthlyDIDProduct == null)
				throw new Exception("Failed to create monthly product for " + number);
			
			// Set product prices
			if (!DIDController.updateProductPrice(ctx, pricelistVersionId, setupDIDProduct.getM_Product_ID(), setupCostNZD, trxName))
				throw new Exception("Failed to create product price for DIDSU-" + number);
				
			if (!DIDController.updateProductPrice(ctx, pricelistVersionId, monthlyDIDProduct.getM_Product_ID(), monthlyChargeNZD, trxName))
				throw new Exception("Failed to create product price for DID-" + number);
			
			// Set Business Partner's price list prices
			if (!DIDController.updateBPPriceListPrice(ctx, businessPartnerId, setupDIDProduct.getM_Product_ID(), setupCostBD, trxName))
				throw new Exception("Failed to create BP price list price for DIDSU-" + number);
			
			if (!DIDController.updateBPPriceListPrice(ctx, businessPartnerId, monthlyDIDProduct.getM_Product_ID(), monthlyChargeBD, trxName))
				throw new Exception("Failed to create BP price list price for DID-" + number);
			
			// Set product purchaser info
			if (!DIDController.updateProductPO(ctx, businessPartnerId, setupDIDProduct, setupCostBD, currencyId, trxName))
				throw new Exception("Failed to create purchaser info for DIDSU-" + number);
			
			if (!DIDController.updateProductPO(ctx, businessPartnerId, monthlyDIDProduct, monthlyChargeBD, currencyId, trxName))
				throw new Exception("Failed to create purchaser info for DID-" + number);
			
			// Set product relation
			if (!DIDController.updateProductRelations(ctx, monthlyDIDProduct.getM_Product_ID(), setupDIDProduct.getM_Product_ID(), trxName))
				throw new Exception("Failed to create relation between DID-" + number + " & DIDSU-" + number);
			
			// Commit local trx if needed
			if (localTrx)
			{
				Trx trx = null;
				try
				{
					trx = Trx.get(trxName, false);	
					if (trx != null)
					{
						if (!trx.commit())
							return getErrorStandardResponse("Failed to commit local trx and create DID-" + number + " & DIDSU-" + number, trxName);
					}
				}
				catch (Exception ex)
				{
					// Catches Trx.get() IllegalArgumentExceptions
				}
				finally
				{
					if (trx != null && trx.isActive())
						trx.close();
				}
			}
			
			return getStandardResponse(true, "DID products have been created for " + number, localTrx ? null : trxName, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
		}
		catch(Exception ex)
		{
			return getErrorStandardResponse("Failed to create DID[" + number + "] because " + ex.getMessage(), localTrx ? null : trxName);
		}
		finally
		{
			// Rollback if local trx
			if (localTrx)
			{
				Trx trx = Trx.get(trxName, false);
				if (trx != null && trx.isActive())
				{
					trx.rollback();
					trx.close();
				}
			}
		}
	}
		
	public StandardResponse readDIDProduct(ReadDIDProductRequest readDIDProductRequest)
	{
		return getErrorStandardResponse("Failed - readDIDProduct() hasn't been implemented", null);
	}
	
	public StandardResponse updateDIDProduct(UpdateDIDProductRequest updateDIDProductRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(updateDIDProductRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("UPDATE_DID_PRODUCT_METHOD_ID"), updateDIDProductRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		String number = updateDIDProductRequest.getNumber();
		if (!validateString(number))
			return getErrorStandardResponse("Invalid number", trxName);
		else
			number = number.trim();
		
		boolean subscribed = updateDIDProductRequest.isSubscribed();
	
		// Get products associated with number
		MProduct[] products = DIDUtil.getDIDProducts(ctx, number, trxName);
		if (products.length != 2)
			return getErrorStandardResponse("Only " + products.length + " product(s) exist for " + number, trxName);
		
		// Check current subscribed values match
		boolean no1Subscribed = DIDUtil.isSubscribed(ctx, products[0], trxName);
		boolean no2Subscribed = DIDUtil.isSubscribed(ctx, products[1], trxName);
		
		if (no1Subscribed != no2Subscribed)
			return getErrorStandardResponse("Subscribed values between products don't match for " + number, trxName);
		
		if (subscribed == no1Subscribed)
			return getStandardResponse(true, "Subscribed values for " + number + " are already " + subscribed, trxName, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
		
		HashMap<Integer, String> attributes = new HashMap<Integer, String>();
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_SUBSCRIBED, Boolean.toString(subscribed));
		
		if (!DIDUtil.updateAttributes(ctx, products[0].getM_AttributeSetInstance_ID(), attributes, trxName))
			return getErrorStandardResponse("Failed to update DID_SUBSCRIBED value for " + products[0], trxName);
			
		if (!DIDUtil.updateAttributes(ctx, products[1].getM_AttributeSetInstance_ID(), attributes, trxName))
			return getErrorStandardResponse("Failed to update DID_SUBSCRIBED value for " + products[1] + " please undo changes to " + products[0], trxName);
		
		return getStandardResponse(true, "Subscribed values for " + number + " have been updated to " + subscribed, trxName, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);		
	}
	
	public StandardResponse deleteDIDProduct(DeleteDIDProductRequest deleteDIDProductRequest)
	{
		return getErrorStandardResponse("Failed - deleteDIDProduct() hasn't been implemented", null);
	}
	
	public StandardResponse createSIPProduct(CreateSIPProductRequest createSIPProductRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(createSIPProductRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_SIP_PRODUCT_METHOD_ID"), createSIPProductRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		String address = createSIPProductRequest.getAddress();
		if (!validateString(address))
			return getErrorStandardResponse("Invalid address", trxName);
		else
			address = address.trim();
		
		String domain = createSIPProductRequest.getDomain();
		if (!validateString(domain))
			return getErrorStandardResponse("Invalid domain", trxName);
		else
			domain = domain.trim();
		
		// Check for existing SIP product
		MProduct[] existingProducts = DIDUtil.getSIPProducts(ctx, address, domain, trxName);
		if (existingProducts.length > 0)
			return getErrorStandardResponse("Found " + existingProducts.length + " SIP product(s) using address=" + address + " & domain=" + domain +", please remove manually", trxName);
		
		// Create attributes
		HashMap<Integer, Object> attributes = new HashMap<Integer, Object>();
		attributes.put(DIDConstants.ATTRIBUTE_ID_SIP_ADDRESS, address);
		attributes.put(DIDConstants.ATTRIBUTE_ID_SIP_DOMAIN, domain);
		
		// Create SIP product
		MProduct sipProduct = DIDUtil.createSIPProduct(ctx, attributes, trxName);
		if (sipProduct == null)
			return getErrorStandardResponse("Failed to create product for " + address + "@" + domain, trxName);
		
		// Set product price
		if (!DIDController.updateProductPrice(ctx, DIDConstants.PRICELIST_VERSION_ID_STANDARD, sipProduct.getM_Product_ID(), Env.ZERO, trxName))
			return getErrorStandardResponse("Failed to create product price for " + sipProduct + " MPricelistVersion[" + DIDConstants.PRICELIST_VERSION_ID_STANDARD + "]", trxName);
				
		return getStandardResponse(true, "SIP product has been created", trxName, sipProduct.getM_Product_ID());
	}
	
	public StandardResponse createVoicemailProduct(CreateVoicemailProductRequest createVoicemailProductRequest)
	{		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(createVoicemailProductRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_VOICEMAIL_PRODUCT_METHOD_ID"), createVoicemailProductRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		String context = createVoicemailProductRequest.getContext();
		if (!validateString(context))
			return getErrorStandardResponse("Invalid context", trxName);
		else
			context = context.trim();
		
		String macroName = createVoicemailProductRequest.getMacroName();
		if (!validateString(macroName))
			return getErrorStandardResponse("Invalid macroName", trxName);
		else
			macroName = macroName.trim();
		
		String mailboxNumber = createVoicemailProductRequest.getMailboxNumber();
		if (!validateString(mailboxNumber))
			return getErrorStandardResponse("Invalid mailboxNumber", trxName);
		else
			mailboxNumber = mailboxNumber.trim();
		
		// Check for existing Voicemail product
		MProduct[] existingProducts = DIDUtil.getVoicemailProducts(ctx, mailboxNumber, trxName);
		if (existingProducts.length > 0)
			return getErrorStandardResponse("Found " + existingProducts.length + " Voicemail product(s) using mailboxNumber=" + mailboxNumber + ", please remove manually", trxName);
		
		// Create attributes
		HashMap<Integer, Object> attributes = new HashMap<Integer, Object>();
		attributes.put(DIDConstants.ATTRIBUTE_ID_VM_CONTEXT, context);
		attributes.put(DIDConstants.ATTRIBUTE_ID_VM_MACRO_NAME, macroName);
		attributes.put(DIDConstants.ATTRIBUTE_ID_VM_MAILBOX_NUMBER, mailboxNumber);
		
		// Create Voicemail product
		MProduct voicemailProduct = DIDUtil.createVoicemailProduct(ctx, attributes, trxName);
		if (voicemailProduct == null)
			return getErrorStandardResponse("Failed to create product for " + mailboxNumber, trxName);
				
		// Set product price
		if (!DIDController.updateProductPrice(ctx, DIDConstants.PRICELIST_VERSION_ID_STANDARD, voicemailProduct.getM_Product_ID(), Env.ZERO, trxName))
			return getErrorStandardResponse("Failed to create product price for " + voicemailProduct + " MPricelistVersion[" + DIDConstants.PRICELIST_VERSION_ID_STANDARD + "]", trxName);
		
		return getStandardResponse(true, "Voicemail product has been created", trxName, voicemailProduct.getM_Product_ID());
	}
	
// ********************************************************************************************************************************************************************************
	
	public StandardResponse createDIDSubscription(CreateDIDSubscriptionRequest createDIDSubscriptionRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx();		
		String trxName = getTrxName(createDIDSubscriptionRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_DID_SUBSCRIPTION_METHOD_ID"), createDIDSubscriptionRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		String number = createDIDSubscriptionRequest.getNumber();
		if (!validateString(number))
			return getErrorStandardResponse("Invalid number", trxName);
		else
			number = number.trim();
		
		Integer businessPartnerId = createDIDSubscriptionRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		// Check for existing DID product pair exists
		MProduct setupProduct = null;
		MProduct monthlyProduct = null;
		MProduct[] existingProducts = DIDUtil.getDIDProducts(ctx, number, trxName);		
		if (existingProducts.length == 2)
		{
			setupProduct = DIDUtil.getSetupOrMonthlyProduct(ctx, existingProducts[0], existingProducts[1], true, trxName);	
			monthlyProduct = DIDUtil.getSetupOrMonthlyProduct(ctx, existingProducts[0], existingProducts[1], false, trxName);			
		}
		else
			return getErrorStandardResponse("Failed to load MProduct[" + DIDConstants.DID_MONTHLY_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, number) + "]" + 
											" and/or MProduct[" + DIDConstants.DID_SETUP_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, number) + "]", trxName);
		
		// Double check products exists
		if (monthlyProduct == null)
			return getErrorStandardResponse("Failed to load MProduct[" + DIDConstants.DID_MONTHLY_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, number) + "]", trxName);
		
		if (setupProduct == null)
			return getErrorStandardResponse("Failed to load MProduct[" + DIDConstants.DID_SETUP_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, number) + "]", trxName);
		
		// Validate and/or retrieve businessPartnerLocationId
		Integer businessPartnerLocationId = validateBusinessPartnerLocationId(ctx, businessPartnerId, createDIDSubscriptionRequest.getBusinessPartnerLocationId());
		
		// Check for existing subscription
		if (DIDUtil.isMSubscribed(ctx, monthlyProduct))
			return getErrorStandardResponse(monthlyProduct + " is already subscribed", trxName);
		
		// Check for existing subscription
		if (DIDUtil.isMSubscribed(ctx, setupProduct))
			return getErrorStandardResponse(setupProduct + " is already subscribed", trxName);
		
		// Create setup subscription
		MSubscription setupSubscription = DIDUtil.createDIDSetupSubscription(ctx, number, businessPartnerId, businessPartnerLocationId, setupProduct.getM_Product_ID(), trxName);
		if (setupSubscription == null)
			return getErrorStandardResponse("Failed to create subscription for " + setupProduct + " MBPartner[" + businessPartnerId + "]", trxName);
		
		// Create monthly subscription
		MSubscription monthlySubscription = DIDUtil.createDIDMonthlySubscription(ctx, number, businessPartnerId, businessPartnerLocationId, monthlyProduct.getM_Product_ID(), trxName);
		if (monthlySubscription == null)
			return getErrorStandardResponse("Failed to create subscription for " + monthlyProduct + " MBPartner[" + businessPartnerId + "]", trxName);
		
		return getStandardResponse(true, "DID subscriptions have been created", trxName, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
	
	public StandardResponse createSIPSubscription(CreateSIPSubscriptionRequest createSIPSubscriptionRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx();		
		String trxName = getTrxName(createSIPSubscriptionRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_SIP_SUBSCRIPTION_METHOD_ID"), createSIPSubscriptionRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		String address = createSIPSubscriptionRequest.getAddress();
		if (!validateString(address))
			return getErrorStandardResponse("Invalid sipAddress", trxName);
		else
			address = address.trim();
		
		String domain = createSIPSubscriptionRequest.getDomain();
		if (!validateString(domain))
			return getErrorStandardResponse("Invalid sipDomain", trxName);
		else
			domain = domain.trim();
		
		Integer businessPartnerId = createSIPSubscriptionRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		// Check for existing SIP product
		MProduct sipProduct = null;
		MProduct[] existingProducts = DIDUtil.getSIPProducts(ctx, address, domain, trxName);
		if (existingProducts.length == 1)
			sipProduct = existingProducts[0];
		else
			return getErrorStandardResponse("Failed to load MProduct[" + DIDConstants.SIP_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, address) + "]", trxName);

		// Double check SIP product exists
		if (sipProduct == null)
			return getErrorStandardResponse("Failed to load MProduct[" + DIDConstants.SIP_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, address) + "]", trxName);
		
		// Validate and/or retrieve businessPartnerLocationId
		Integer businessPartnerLocationId = validateBusinessPartnerLocationId(ctx, businessPartnerId, createSIPSubscriptionRequest.getBusinessPartnerLocationId());
		
		// Check for existing subscription
		if (DIDUtil.isMSubscribed(ctx, sipProduct))
			return getErrorStandardResponse(sipProduct + " is already subscribed", trxName);
		
		// Create subscription
		MSubscription subscription = DIDUtil.createSIPSubscription(ctx, address, domain, businessPartnerId, businessPartnerLocationId, sipProduct.getM_Product_ID(), trxName);
		if (subscription == null)
			return getErrorStandardResponse("Failed to create subscription for " + sipProduct + " MBPartner[" + businessPartnerId + "]", trxName);
		
		return getStandardResponse(true, "SIP subscription has been created", trxName, subscription.getC_Subscription_ID());
	}
	
	public StandardResponse createVoicemailSubscription(CreateVoicemailSubscriptionRequest createVoicemailSubscriptionRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx();		
		String trxName = getTrxName(createVoicemailSubscriptionRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_VOICEMAIL_SUBSCRIPTION_METHOD_ID"), createVoicemailSubscriptionRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		String mailboxNumber = createVoicemailSubscriptionRequest.getMailboxNumber();
		if (!validateString(mailboxNumber))
			return getErrorStandardResponse("Invalid mailboxNumber", trxName);
		else
			mailboxNumber = mailboxNumber.trim();
		
		Integer businessPartnerId = createVoicemailSubscriptionRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		// Check for existing Voicemail product
		MProduct voicemailProduct = null;
		MProduct[] existingProducts = DIDUtil.getVoicemailProducts(ctx, mailboxNumber, trxName);
		if (existingProducts.length == 1)
			voicemailProduct = existingProducts[0];
		else
			return getErrorStandardResponse("Failed to load MProduct[" + DIDConstants.VOICEMAIL_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, mailboxNumber) + "]", trxName);
		
		// Double check Voicemail product exists
		if (voicemailProduct == null)
			return getErrorStandardResponse("Failed to load MProduct[" + DIDConstants.VOICEMAIL_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, mailboxNumber) + "]", trxName);
		
		// Validate and/or retrieve businessPartnerLocationId
		Integer businessPartnerLocationId = validateBusinessPartnerLocationId(ctx, businessPartnerId, createVoicemailSubscriptionRequest.getBusinessPartnerLocationId());
		
		// Check for existing subscription
		if (DIDUtil.isMSubscribed(ctx, voicemailProduct))
			return getErrorStandardResponse(voicemailProduct + " is already subscribed", trxName);
		
		// Create subscription
		MSubscription subscription = DIDUtil.createVoicemailSubscription(ctx, mailboxNumber, businessPartnerId, businessPartnerLocationId, voicemailProduct.getM_Product_ID(), trxName);
		if (subscription == null)
			return getErrorStandardResponse("Failed to create subscription for " + voicemailProduct + " MBPartner[" + businessPartnerId + "]", trxName);
		
		return getStandardResponse(true, "Voicemail subscription has been created", trxName, subscription.getC_Subscription_ID());
	}
	
// ********************************************************************************************************************************************************************************
	
	// TODO: Check if password isn't meant to be set
	public StandardResponse createSubscriber(CreateSubscriberRequest createSubscriberRequest)
	{
		// Create ctx
		Properties ctx = Env.getCtx();		
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_SUBSCRIBER_METHOD_ID"), createSubscriberRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String username = createSubscriberRequest.getUsername();
		if (!validateString(username))
			return getErrorStandardResponse("Invalid username", null);
		else
			username = username.trim();
		
		String password = createSubscriberRequest.getPassword();
		if (!validateString(password))
			return getErrorStandardResponse("Invalid password", null);
		else
			password = password.trim();
		
		String domain = createSubscriberRequest.getDomain();
		if (!validateString(domain))
			return getErrorStandardResponse("Invalid domain", null);
		else
			domain = domain.trim();
		
		String timezone = createSubscriberRequest.getTimezone();
		if (!validateString(timezone))
			return getErrorStandardResponse("Invalid timezone", null);
		else
			timezone = timezone.trim();
		
		Integer businessPartnerId = createSubscriberRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, null))
			return getErrorStandardResponse("Invalid businessPartnerId", null);

		// Add subscriber account
		int subscriberId = SERConnector.addSIPAccount(username, password, domain, "", "", "", timezone, Integer.toString(businessPartnerId));
		if (subscriberId < 1)
			return getErrorStandardResponse("Failed to create subscriber for " + username + "@" + domain + " & MBPartner[" + businessPartnerId + "]", null);
		
		return getStandardResponse(true, "Subscriber has been created", null, subscriberId);
	}
	
	public StandardResponse readSubscriber(ReadSubscriberRequest readSubscriberRequest)
	{
		return getErrorStandardResponse("Failed - readSubscriber() hasn't been implemented", null);
	}
	
	public StandardResponse updateSubscriber(UpdateSubscriberRequest updateSubscriberRequest)
	{
		return getErrorStandardResponse("Failed - updateSubscriber() hasn't been implemented", null);
	}
	
	public StandardResponse deleteSubscriber(DeleteSubscriberRequest deleteSubscriberRequest)
	{
		// Create ctx
		Properties ctx = Env.getCtx();		
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("DELETE_SUBSCRIBER_METHOD_ID"), deleteSubscriberRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String username = deleteSubscriberRequest.getUsername();
		if (!validateString(username))
			return getErrorStandardResponse("Invalid username", null);
		else
			username = username.trim();
		
		String domain = deleteSubscriberRequest.getDomain();
		if (!validateString(domain))
			return getErrorStandardResponse("Invalid domain", null);
		else
			domain = domain.trim();
		
		Integer businessPartnerId = deleteSubscriberRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, null))
			return getErrorStandardResponse("Invalid businessPartnerId", null);

		// Delete subscriber account
		if (!SERConnector.removeSIPAccount(username, domain, Integer.toString(businessPartnerId)))
			return getErrorStandardResponse("Failed to delete subscriber for " + username + "@" + domain + " & MBPartner[" + businessPartnerId + "]", null);
		
		return getStandardResponse(true, "Subscriber has been deleted", null, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}

	public StandardResponse createUserPreference(CreateUserPreferenceRequest createUserPreferenceRequest)
	{
		// Create ctx
		Properties ctx = Env.getCtx();		
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_USER_PREFERENCE_METHOD_ID"), createUserPreferenceRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String uuid = createUserPreferenceRequest.getUuid();
		if (!validateString(uuid))
			return getErrorStandardResponse("Invalid uuid", null);
		else
			uuid = uuid.trim();
		
		String username = createUserPreferenceRequest.getUsername();
		if (!validateString(username))
			return getErrorStandardResponse("Invalid username", null);
		else
			username = username.trim();
		
		String domain = createUserPreferenceRequest.getDomain();
		if (!validateString(domain))
			return getErrorStandardResponse("Invalid domain", null);
		else
			domain = domain.trim();
		
		String attribute = createUserPreferenceRequest.getAttribute();
		if (!validateString(attribute))
			return getErrorStandardResponse("Invalid attribute", null);
		else 
			attribute = attribute.trim();
		
		// Validate attribute
		if (!attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE) && !attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT))
			return getErrorStandardResponse("Invalid attribute (valid values are " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE + " or " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT + ")", null);
		
		String value = createUserPreferenceRequest.getValue();
		if (!validateString(value))
			return getErrorStandardResponse("Invalid value", null);
		else
			value = value.trim();
		
		// Validate/convert value
		if (value.equalsIgnoreCase("true") || value.equals("1"))
			value = DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_VALUE_ACTIVE;
		else if (value.equalsIgnoreCase("false") || value.equals("0"))
			value = DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_VALUE_INACTIVE;
		else
			return getErrorStandardResponse("Invalid value (only 0, 1, true, and false are accepted)", null);
		
		String type = createUserPreferenceRequest.getType();
		if (!validateString(type))
			return getErrorStandardResponse("Invalid type", null);
		else
			type = type.trim();
		
		// Validate attribute/type pair
		if (attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE) && !type.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_TEXT))
			return getErrorStandardResponse("Invalid type for " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE + "(use " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_TEXT + ")", null);
		
		if (attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT) && !type.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_NUMERIC))
			return getErrorStandardResponse("Invalid type for " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT + "(use " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_NUMERIC + ")", null);
		
		String subscriberId = createUserPreferenceRequest.getSubscriberId();
		if (!validateString(subscriberId))
			return getErrorStandardResponse("Invalid subscriberId", null);
		else 
			subscriberId = subscriberId.trim();

		// Add user preference
		if (!SERConnector.addUserPreference(uuid, username, domain, attribute, value, type, subscriberId))
			return getErrorStandardResponse("Failed to create User Preference uuid[" + uuid + "] attribute[" + attribute + "]", null);
			
		return getStandardResponse(true, "User Preference has been created", null, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
	
	public StandardResponse readUserPreference(ReadUserPreferenceRequest readUserPreferenceRequest)
	{
		return getErrorStandardResponse("Failed - readUserPreference() hasn't been implemented", null);
	}
	
	public StandardResponse updateUserPreferenceValue(UpdateUserPreferenceValueRequest updateUserPreferenceValueRequest)
	{
		// Create ctx
		Properties ctx = Env.getCtx();		
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("UPDATE_USER_PREFERENCE_METHOD_ID"), updateUserPreferenceValueRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String uuid = updateUserPreferenceValueRequest.getUuid();
		if (!validateString(uuid))
			return getErrorStandardResponse("Invalid uuid", null);
		else
			uuid = uuid.trim();
		
		String username = updateUserPreferenceValueRequest.getUsername();
		if (!validateString(username))
			return getErrorStandardResponse("Invalid username", null);
		else
			username = username.trim();
		
		String domain = updateUserPreferenceValueRequest.getDomain();
		if (!validateString(domain))
			return getErrorStandardResponse("Invalid domain", null);
		else
			domain = domain.trim();
		
		String attribute = updateUserPreferenceValueRequest.getAttribute();
		if (!validateString(attribute))
			return getErrorStandardResponse("Invalid attribute", null);
		else 
			attribute = attribute.trim();
		
		// Validate attribute
		if (!attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE) && !attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT))
			return getErrorStandardResponse("Invalid attribute (valid values are " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE + " or " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT + ")", null);
		
		String value = updateUserPreferenceValueRequest.getValue();
		if (!validateString(value))
			return getErrorStandardResponse("Invalid value", null);
		else
			value = value.trim();
		
		// Validate/convert value
		if (value.equalsIgnoreCase("true") || value.equals("1") || value.equalsIgnoreCase("Active"))
			value = DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_VALUE_ACTIVE;
		else if (value.equalsIgnoreCase("false") || value.equals("0") || value.equalsIgnoreCase("Inactive"))
			value = DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_VALUE_INACTIVE;
		else
			return getErrorStandardResponse("Invalid value (only 0, 1, true, and false are accepted)", null);

		// Update user preference value
		if (!SERConnector.updateUserPreferenceValue(uuid, username, domain, attribute, value))
			return getErrorStandardResponse("Failed to update user preference value[" + value + "] for uuid[" + uuid + "] username[" + username + "] domain[" + domain + "] attribute[" + attribute + "]", null);
		
		return getStandardResponse(true, "User Preference value has been updated", null, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
	
	public StandardResponse updateUserPreferenceStartDate(UpdateUserPreferenceStartDateRequest updateUserPreferenceStartDateRequest)
	{
		// Create ctx
		Properties ctx = Env.getCtx();		
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("UPDATE_USER_PREFERENCE_METHOD_ID"), updateUserPreferenceStartDateRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String uuid = updateUserPreferenceStartDateRequest.getUuid();
		if (!validateString(uuid))
			return getErrorStandardResponse("Invalid uuid", null);
		else
			uuid = uuid.trim();
		
		String username = updateUserPreferenceStartDateRequest.getUsername();
		if (!validateString(username))
			return getErrorStandardResponse("Invalid username", null);
		else
			username = username.trim();
		
		String domain = updateUserPreferenceStartDateRequest.getDomain();
		if (!validateString(domain))
			return getErrorStandardResponse("Invalid domain", null);
		else
			domain = domain.trim();
		
		String attribute = updateUserPreferenceStartDateRequest.getAttribute();
		if (!validateString(attribute))
			return getErrorStandardResponse("Invalid attribute", null);
		else 
			attribute = attribute.trim();
		
		// Validate attribute
		if (!attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE) && !attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT))
			return getErrorStandardResponse("Invalid attribute (valid values are " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE + " or " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT + ")", null);
		
		XMLGregorianCalendar startDate = updateUserPreferenceStartDateRequest.getStartDate();
		if (startDate == null)
			return getErrorStandardResponse("Invalid startDate", null); // TODO: Check valid date?
		
		GregorianCalendar gc = startDate.toGregorianCalendar();
		if (gc == null)
			return getErrorStandardResponse("Invalid startDate - Couldn't convert to GregorianCalendar date", null); 

		// Update user preference end date
		Timestamp startDateTimestamp = new Timestamp(gc.getTimeInMillis());
		if (!SERConnector.updateUserPreferenceStartDate(uuid, username, domain, attribute, startDateTimestamp))
			return getErrorStandardResponse("Failed to update user preference startDate[" + startDateTimestamp + "] for uuid[" + uuid + "] username[" + username + "] domain[" + domain + "] attribute[" + attribute + "]", null);							
		
		return getStandardResponse(true, "User Preference startDate has been updated", null, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
	
	public StandardResponse updateUserPreferenceEndDate(UpdateUserPreferenceEndDateRequest updateUserPreferenceEndDateRequest)
	{
		// Create ctx
		Properties ctx = Env.getCtx();		
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("UPDATE_USER_PREFERENCE_METHOD_ID"), updateUserPreferenceEndDateRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String uuid = updateUserPreferenceEndDateRequest.getUuid();
		if (!validateString(uuid))
			return getErrorStandardResponse("Invalid uuid", null);
		else
			uuid = uuid.trim();
		
		String username = updateUserPreferenceEndDateRequest.getUsername();
		if (!validateString(username))
			return getErrorStandardResponse("Invalid username", null);
		else
			username = username.trim();
		
		String domain = updateUserPreferenceEndDateRequest.getDomain();
		if (!validateString(domain))
			return getErrorStandardResponse("Invalid domain", null);
		else
			domain = domain.trim();
		
		String attribute = updateUserPreferenceEndDateRequest.getAttribute();
		if (!validateString(attribute))
			return getErrorStandardResponse("Invalid attribute", null);
		else 
			attribute = attribute.trim();
		
		// Validate attribute
		if (!attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE) && !attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT))
			return getErrorStandardResponse("Invalid attribute (valid values are " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE + " or " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT + ")", null);
		
		XMLGregorianCalendar endDate = updateUserPreferenceEndDateRequest.getEndDate();
		if (endDate == null)
			return getErrorStandardResponse("Invalid endDate", null); // TODO: Check valid date?
		
		GregorianCalendar gc = endDate.toGregorianCalendar();
		if (gc == null)
			return getErrorStandardResponse("Invalid endDate - Couldn't convert to GregorianCalendar date", null); 

		// Update user preference end date
		Timestamp endDateTimestamp = new Timestamp(gc.getTimeInMillis());
		if (!SERConnector.updateUserPreferenceStartDate(uuid, username, domain, attribute, endDateTimestamp))
			return getErrorStandardResponse("Failed to update user preference endDate[" + endDateTimestamp + "] for uuid[" + uuid + "] username[" + username + "] domain[" + domain + "] attribute[" + attribute + "]", null);							
	
		return getStandardResponse(true, "User Preference endDate has been updated", null, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
	
	public StandardResponse deleteUserPreference(DeleteUserPreferenceRequest deleteUserPreferenceRequest)
	{
		// Create ctx
		Properties ctx = Env.getCtx();		
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("DELETE_USER_PREFERENCE_METHOD_ID"), deleteUserPreferenceRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String uuid = deleteUserPreferenceRequest.getUuid();
		if (!validateString(uuid))
			return getErrorStandardResponse("Invalid uuid", null);
		else
			uuid = uuid.trim();
		
		String username = deleteUserPreferenceRequest.getUsername();
		if (!validateString(username))
			return getErrorStandardResponse("Invalid username", null);
		else
			username = username.trim();
		
		String domain = deleteUserPreferenceRequest.getDomain();
		if (!validateString(domain))
			return getErrorStandardResponse("Invalid domain", null);
		else
			domain = domain.trim();
		
		String attribute = deleteUserPreferenceRequest.getAttribute();
		if (!validateString(attribute))
			return getErrorStandardResponse("Invalid attribute", null);
		else
			attribute = attribute.trim();
		
		// Validate attribute 
		if (!attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE) && !attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT))
			return getErrorStandardResponse("Invalid attribute (valid values are " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE + " or " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT + ")", null);
		
		String value = deleteUserPreferenceRequest.getValue();
		if (!validateString(value))
			return getErrorStandardResponse("Invalid value", null);
		else
			value = value.trim();
		
		// Validate/convert value
		if (value.equalsIgnoreCase("true") || value.equals("1"))
			value = DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_VALUE_ACTIVE;
		else if (value.equalsIgnoreCase("false") || value.equals("0"))
			value = DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_VALUE_INACTIVE;
		else
			return getErrorStandardResponse("Invalid value (only 0, 1, true, and false are accepted)", null);
		
		String type = deleteUserPreferenceRequest.getType();
		if (!validateString(type))
			return getErrorStandardResponse("Invalid type", null);
		else
			type = type.trim();
		
		// Validate attribute/type pair
		if (attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE) && !type.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_TEXT))
			return getErrorStandardResponse("Invalid type for " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE + "(use " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_TEXT + ")", null);
		
		if (attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT) && !type.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_NUMERIC))
			return getErrorStandardResponse("Invalid type for " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT + "(use " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_NUMERIC + ")", null);
		
		String subscriberId = deleteUserPreferenceRequest.getSubscriberId();
		if (!validateString(subscriberId))
			return getErrorStandardResponse("Invalid subscriberId", null);
		else
			subscriberId = subscriberId.trim();

		// Delete user preference
		if (!SERConnector.removeUserPreference(uuid, username, domain, attribute, value, type, subscriberId))
			return getErrorStandardResponse("Failed to delete User Preference uuid[" + uuid + "] attribute[" + attribute + "]", null);
			
		return getStandardResponse(true, "User Preference has been deleted", null, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
	
// ********************************************************************************************************************************************************************************
	
	public StandardResponse createVoicemailUser(CreateVoicemailUserRequest createVoicemailUserRequest)
	{
		// Create ctx
		Properties ctx = Env.getCtx();		
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_VOICEMAIL_USER_METHOD_ID"), createVoicemailUserRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String mailboxNumber = createVoicemailUserRequest.getMailboxNumber();
		if (!validateString(mailboxNumber))
			return getErrorStandardResponse("Invalid mailboxNumber", null);
		else 
			mailboxNumber = mailboxNumber.trim();
		
		Integer businessPartnerId = createVoicemailUserRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, null))
			return getErrorStandardResponse("Invalid businessPartnerId", null);
		
		MBPartner businessPartner = MBPartner.get(ctx, businessPartnerId);
		String bpSearchKey = businessPartner.getValue();
		String bpName = businessPartner.getName();
		String bpEmail = ""; // Not used
		
		if (!AsteriskConnector.addVoicemailUser(Integer.toString(businessPartnerId), bpSearchKey, mailboxNumber, bpName, bpEmail))
			return getErrorStandardResponse("Failed to create VoicemailUser[" + mailboxNumber + "-" + businessPartnerId + "]", null);
			
		return getStandardResponse(true, "Voicemail User has been created", null, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
	
	public StandardResponse deleteVoicemailUser(DeleteVoicemailUserRequest deleteVoicemailUserRequest)
	{
		// Create ctx
		Properties ctx = Env.getCtx();		
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("DELETE_VOICEMAIL_USER_METHOD_ID"), deleteVoicemailUserRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String mailboxNumber = deleteVoicemailUserRequest.getMailboxNumber();
		if (!validateString(mailboxNumber))
			return getErrorStandardResponse("Invalid mailboxNumber", null);
		else 
			mailboxNumber = mailboxNumber.trim();
		
		Integer businessPartnerId = deleteVoicemailUserRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, null))
			return getErrorStandardResponse("Invalid businessPartnerId", null);
		
		MBPartner businessPartner = MBPartner.get(ctx, businessPartnerId);
		String bpSearchKey = businessPartner.getValue();
		String bpName = businessPartner.getName();
		String bpEmail = ""; // Not used
		
		if (!AsteriskConnector.removeVoicemailUser(Integer.toString(businessPartnerId), bpSearchKey, mailboxNumber, bpName, bpEmail))
			return getErrorStandardResponse("Failed to delete VoicemailUser[" + mailboxNumber + "-" + businessPartnerId + "]", null);
			
		return getStandardResponse(true, "Voicemail User has been deleted", null, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
	
	public StandardResponse createVoicemailUserPreferences(CreateVoicemailUserPreferencesRequest createVoicemailUserPreferencesRequest)
	{
		// Create ctx
		Properties ctx = Env.getCtx();		
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_VOICEMAIL_USER_PREFERENCES_METHOD_ID"), createVoicemailUserPreferencesRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String mailboxNumber = createVoicemailUserPreferencesRequest.getMailboxNumber();
		if (!validateString(mailboxNumber))
			return getErrorStandardResponse("Invalid mailboxNumber", null);
		else
			mailboxNumber = mailboxNumber.trim();
		
		String domain = createVoicemailUserPreferencesRequest.getDomain();
		if (!validateString(domain))
			return getErrorStandardResponse("Invalid domain", null);
		else 
			domain = domain.trim();
		
		Integer businessPartnerId = createVoicemailUserPreferencesRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, null))
			return getErrorStandardResponse("Invalid businessPartnerId", null);
		
		MBPartner businessPartner = MBPartner.get(ctx, businessPartnerId);
		String bpSearchKey = businessPartner.getValue();
		
		if (!SERConnector.addVoicemailPreferences(Integer.toString(businessPartnerId), mailboxNumber, domain, bpSearchKey))
			return getErrorStandardResponse("Failed to create Voicemail User Preferences for " + mailboxNumber + " & MBPartner[" + businessPartnerId + "]", null);
		
		return getStandardResponse(true, "Voicemail User Preferences have been created", null, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
	
	public StandardResponse deleteVoicemailUserPreferences(DeleteVoicemailUserPreferencesRequest deleteVoicemailUserPreferencesRequest)
	{
		// Create ctx
		Properties ctx = Env.getCtx();		
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("DELETE_VOICEMAIL_USER_PREFERENCES_METHOD_ID"), deleteVoicemailUserPreferencesRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String mailboxNumber = deleteVoicemailUserPreferencesRequest.getMailboxNumber();
		if (!validateString(mailboxNumber))
			return getErrorStandardResponse("Invalid mailboxNumber", null);
		else
			mailboxNumber = mailboxNumber.trim();
		
		String domain = deleteVoicemailUserPreferencesRequest.getDomain();
		if (!validateString(domain))
			return getErrorStandardResponse("Invalid domain", null);
		else
			domain = domain.trim();
		
		Integer businessPartnerId = deleteVoicemailUserPreferencesRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, null))
			return getErrorStandardResponse("Invalid businessPartnerId", null);
		
		MBPartner businessPartner = MBPartner.get(ctx, businessPartnerId);
		String bpSearchKey = businessPartner.getValue();
		
		SERConnector.removeVoicemailPreferences(Integer.toString(businessPartnerId), mailboxNumber, domain, bpSearchKey);
			
		return getStandardResponse(true, "Voicemail User Preferences have been deleted", null, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
	
	public StandardResponse createVoicemailDialPlan(CreateVoicemailDialPlanRequest createVoicemailDialPlanRequest)
	{
		// Create ctx
		Properties ctx = Env.getCtx();		
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_VOICEMAIL_DIALPLAN_METHOD_ID"), createVoicemailDialPlanRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String mailboxNumber = createVoicemailDialPlanRequest.getMailboxNumber();
		if (!validateString(mailboxNumber))
			return getErrorStandardResponse("Invalid mailboxNumber", null);
		else 
			mailboxNumber = mailboxNumber.trim();
		
		Integer businessPartnerId = createVoicemailDialPlanRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, null))
			return getErrorStandardResponse("Invalid businessPartnerId", null);
		
		MBPartner businessPartner = MBPartner.get(ctx, businessPartnerId);
		String bpSearchKey = businessPartner.getValue();
		
		if (!AsteriskConnector.addVoicemailToDialPlan(mailboxNumber, bpSearchKey))
			return getErrorStandardResponse("Failed to create Voicemail DialPlan[" + mailboxNumber + "-" + bpSearchKey + "]", null);
		
		return getStandardResponse(true, "Voicemail DialPlan has been created", null, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
	
	public StandardResponse deleteVoicemailDialPlan(DeleteVoicemailDialPlanRequest deleteVoicemailDialPlanRequest)
	{
		// Create ctx
		Properties ctx = Env.getCtx();		
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("DELETE_VOICEMAIL_DIALPLAN_METHOD_ID"), deleteVoicemailDialPlanRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String mailboxNumber = deleteVoicemailDialPlanRequest.getMailboxNumber();
		if (!validateString(mailboxNumber))
			return getErrorStandardResponse("Invalid mailboxNumber", null);
		else
			mailboxNumber = mailboxNumber.trim();
		
		Integer businessPartnerId = deleteVoicemailDialPlanRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, null))
			return getErrorStandardResponse("Invalid businessPartnerId", null);
		
		MBPartner businessPartner = MBPartner.get(ctx, businessPartnerId);
		String bpSearchKey = businessPartner.getValue();
		
		if (!AsteriskConnector.removeVoicemailFromDialPlan(mailboxNumber, bpSearchKey))
			return getErrorStandardResponse("Failed to delete Voicemail DialPlan[" + mailboxNumber + "-" + bpSearchKey + "]", null);
		
		return getStandardResponse(true, "Voicemail DialPlan has been deleted", null, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
	
// ********************************************************************************************************************************************************************************
		
	// TODO: Validate business logic 
	//			- didNumber numberic only
	//			- DID product pair exists
	// 			- SIP and Voicemail products don't exist
	//			- Current subscriptions for DID, SIP, or Voicemail don't exist
	public StandardResponse validateProvisionDIDParameters(ValidateProvisionDIDParametersRequest validateProvisionDIDParametersRequest)
	{
		// Create ctx
		Properties ctx = Env.getCtx(); 
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("VALIDATE_PROVISION_DID_PARAMETERS_METHOD_ID"), validateProvisionDIDParametersRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);

		// Load and validate parameters
		String didNumber = validateProvisionDIDParametersRequest.getDidNumber();
		if (!validateString(didNumber))
			return getErrorStandardResponse("Invalid didNumber", null);
		
		String sipAddress = validateProvisionDIDParametersRequest.getSipAddress();
		if (!validateString(sipAddress))
			return getErrorStandardResponse("Invalid sipAddress", null);
		
		String sipDomain = validateProvisionDIDParametersRequest.getSipDomain();
		if (!validateString(sipDomain))
			return getErrorStandardResponse("Invalid sipDomain", null);
		
		String sipPassword = validateProvisionDIDParametersRequest.getSipPassword();
		if (!validateString(sipPassword))
			return getErrorStandardResponse("Invalid sipPassword", null);
		
		String sipTimezone = validateProvisionDIDParametersRequest.getSipTimezone();
		if (!validateString(sipTimezone))
			return getErrorStandardResponse("Invalid sipTimezone", null);
		
		String voicemailContext = validateProvisionDIDParametersRequest.getVoicemailContext();
		if (!validateString(voicemailContext))
			return getErrorStandardResponse("Invalid voicemailContext", null);
		
		String voicemailDomain = validateProvisionDIDParametersRequest.getVoicemailDomain();
		if (!validateString(voicemailDomain))
			return getErrorStandardResponse("Invalid voicemailDomain", null);
		
		String voicemailMacroName = validateProvisionDIDParametersRequest.getVoicemailMacroName();
		if (!validateString(voicemailMacroName))
			return getErrorStandardResponse("Invalid voicemailMacroName", null);
		
		String voicemailMailboxNumber = validateProvisionDIDParametersRequest.getVoicemailMailboxNumber();
		if (!validateString(voicemailMailboxNumber))
			return getErrorStandardResponse("Invalid voicemailMailboxNumber", null);
		
		Integer businessPartnerId = validateProvisionDIDParametersRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, null))
			return getErrorStandardResponse("Invalid businessPartnerId", null);
		
		Integer businessPartnerLocationId = validateBusinessPartnerLocationId(ctx, businessPartnerId, validateProvisionDIDParametersRequest.getBusinessPartnerLocationId());
		if (businessPartnerLocationId == null || businessPartnerLocationId < 1)
			return getErrorStandardResponse("Invalid businessPartnerLocationId", null);
		
		boolean converseVoiceActive = validateProvisionDIDParametersRequest.isConverseVoiceActive();
		boolean cOutActive = validateProvisionDIDParametersRequest.isCOutActive();
		
		return getStandardResponse(true, "ProvisionDID parameters have been validated", null, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
	
	public StandardResponse createCallProduct(CreateCallProductRequest createCallProductRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(createCallProductRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_CALL_PRODUCT_METHOD_ID"), createCallProductRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		String number = createCallProductRequest.getNumber();
		if (!validateString(number))
			return getErrorStandardResponse("Invalid number", trxName);
		else
			number = number.trim();
		
		String domain = createCallProductRequest.getDomain();
		if (!validateString(domain))
			return getErrorStandardResponse("Invalid domain", trxName);
		else
			domain = domain.trim();
		
		Integer pricelistVersionId = createCallProductRequest.getPricelistVersionId();
		if (pricelistVersionId == null || pricelistVersionId < 1 || !Validation.validateADId(MPriceListVersion.Table_Name, pricelistVersionId, trxName))
			return getErrorStandardResponse("Invalid pricelistVersionId", trxName);
		
		// If caller not using trx create local
		boolean localTrx = false;
		if (trxName == null)
		{	
			localTrx = true;			
			trxName = Trx.createTrxName("createCallProduct");
		}
		
		try
		{					
			// Create attributes
			HashMap<Integer, Object> attributes = new HashMap<Integer, Object>();
			attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_APPLICATION, DIDConstants.ATTRIBUTE_ID_CDR_APPLICATION_VALUE_AUDIO);
			attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_NUMBER, number);
			attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, DIDConstants.ATTRIBUTE_VALUE_INBOUND_CDR_USERNAME.replace(DIDConstants.NUMBER_IDENTIFIER, number).replace(DIDConstants.DOMAIN_IDENTIFIER, domain));
			attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION, DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_INBOUND);
			
			// Create Call products
			MProduct inboundCallProduct = DIDUtil.createCallProduct(ctx, attributes, trxName);
			if (inboundCallProduct == null)
				throw new Exception("Failed to create inbound CALL product for " + number);
	
			attributes.remove(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME);
			attributes.remove(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION);
			attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, DIDConstants.ATTRIBUTE_VALUE_OUTBOUND_CDR_USERNAME.replace(DIDConstants.NUMBER_IDENTIFIER, number).replace(DIDConstants.DOMAIN_IDENTIFIER, domain));
			attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION, DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_OUTBOUND);
			
			MProduct outboundCallProduct = DIDUtil.createCallProduct(ctx, attributes, trxName);
			if (outboundCallProduct == null)
				throw new Exception("Failed to create outbound CALL product for " + number);
			
			// Set product prices
			if (!DIDController.updateProductPrice(ctx, pricelistVersionId, inboundCallProduct.getM_Product_ID(), Env.ZERO, trxName))
				throw new Exception("Failed to create product price for CALL-IN-" + number + " MPricelistVersion[" + pricelistVersionId + "]");
				
			if (!DIDController.updateProductPrice(ctx, pricelistVersionId, outboundCallProduct.getM_Product_ID(), Env.ZERO, trxName))
				throw new Exception("Failed to create product price for CALL-OUT-" + number + " MPricelistVersion[" + pricelistVersionId + "]");
			
			// Commit local trx if needed
			if (localTrx)
			{
				Trx trx = null;
				try
				{
					trx = Trx.get(trxName, false);	
					if (trx != null)
					{
						if (!trx.commit())
							return getErrorStandardResponse("Failed to commit local trx and create CALL-IN-" + number + " & CALL-OUT-" + number, trxName);
					}
				}
				catch (Exception ex)
				{
					// Catches Trx.get() IllegalArgumentExceptions
				}
				finally
				{
					if (trx != null && trx.isActive())
						trx.close();
				}
			}
			
			return getStandardResponse(true, "CALL products have been created for " + number, localTrx ? null : trxName, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
		}
		catch(Exception ex)
		{
			return getErrorStandardResponse("Failed to create CALL products for " + number + " because " + ex.getMessage(), localTrx ? null : trxName);
		}
		finally
		{
			// Rollback if local trx
			if (localTrx)
			{
				Trx trx = Trx.get(trxName, false);
				if (trx != null && trx.isActive())
				{
					trx.rollback();
					trx.close();
				}
			}
		}
	}
	
	public StandardResponse createCallSubscription(CreateCallSubscriptionRequest createCallSubscriptionRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx();		
		String trxName = getTrxName(createCallSubscriptionRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("CREATE_CALL_SUBSCRIPTION_METHOD_ID"), createCallSubscriptionRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		String number = createCallSubscriptionRequest.getNumber();
		if (!validateString(number))
			return getErrorStandardResponse("Invalid number", trxName);
		else
			number = number.trim();
		
		Integer businessPartnerId = createCallSubscriptionRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		// Check if existing CALL product pair exists
		MProduct inboundCallProduct = null;
		MProduct outboundCallProduct = null;
		MProduct[] existingProducts = DIDUtil.getCallProducts(ctx, number, trxName);		
		if (existingProducts.length == 2)
		{
			inboundCallProduct = DIDUtil.getInboundOrOutboundProduct(ctx, existingProducts[0], existingProducts[1], true, trxName);	
			outboundCallProduct = DIDUtil.getInboundOrOutboundProduct(ctx, existingProducts[0], existingProducts[1], false, trxName);			
		}
		else
			return getErrorStandardResponse("Failed to load MProduct[" + DIDConstants.CALL_IN_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, number) + "]" + 
											" and/or MProduct[" + DIDConstants.CALL_OUT_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, number) + "]", trxName);
		
		// Double check products exists
		if (inboundCallProduct == null)
			return getErrorStandardResponse("Failed to load MProduct[" + DIDConstants.CALL_IN_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, number) + "]", trxName);
		
		if (outboundCallProduct == null)
			return getErrorStandardResponse("Failed to load MProduct[" + DIDConstants.CALL_OUT_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, number) + "]", trxName);
		
		// Validate and/or retrieve businessPartnerLocationId
		Integer businessPartnerLocationId = validateBusinessPartnerLocationId(ctx, businessPartnerId, createCallSubscriptionRequest.getBusinessPartnerLocationId());
		
		// Check for existing subscription
		if (DIDUtil.isMSubscribed(ctx, inboundCallProduct))
			return getErrorStandardResponse(inboundCallProduct + " is already subscribed", trxName);
		
		// Check for existing subscription
		if (DIDUtil.isMSubscribed(ctx, outboundCallProduct))
			return getErrorStandardResponse(outboundCallProduct + " is already subscribed", trxName);
		
		// Create inbound subscription
		MSubscription inboundSubscription = DIDUtil.createCallSubscription(ctx, number, businessPartnerId, businessPartnerLocationId, inboundCallProduct.getM_Product_ID(), trxName);
		if (inboundSubscription == null)
			return getErrorStandardResponse("Failed to create subscription for " + inboundCallProduct + " MBPartner[" + businessPartnerId + "]", trxName);
		
		// Create outbound subscription
		MSubscription outboundSubscription = DIDUtil.createCallSubscription(ctx, number, businessPartnerId, businessPartnerLocationId, outboundCallProduct.getM_Product_ID(), trxName);
		if (outboundSubscription == null)
			return getErrorStandardResponse("Failed to create subscription for " + outboundCallProduct + " MBPartner[" + businessPartnerId + "]", trxName);
		
		return getStandardResponse(true, "Call subscriptions have been created", trxName, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
	
	public ReadRadiusAccountsResponse readRadiusAccountsByInvoice(ReadRadiusAccountsByInvoiceRequest readRadiusAccountsByInvoiceRequest)
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadRadiusAccountsResponse readRadiusAccountsResponse = objectFactory.createReadRadiusAccountsResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readRadiusAccountsByInvoiceRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("PROVISION_WEBSERVICE"), WebServiceConstants.PROVISION_WEBSERVICE_METHODS.get("READ_RADIUS_ACCOUNTS_BY_INVOICE"), readRadiusAccountsByInvoiceRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readRadiusAccountsResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readRadiusAccountsResponse;
		}

		// Load and validate parameters
		Integer invoiceId = readRadiusAccountsByInvoiceRequest.getInvoiceId();
		if (invoiceId == null || invoiceId < 1 || !Validation.validateADId(MInvoice.Table_Name, invoiceId, trxName))
		{
			readRadiusAccountsResponse.setStandardResponse(getErrorStandardResponse("Invalid invoiceId", trxName));
			return readRadiusAccountsResponse;
		}
		
		// Get Radius Accounts for invoice
		ArrayList<com.conversant.model.RadiusAccountInvoice> accounts = RadiusConnector.getRaidusAccountsByInvoice(invoiceId);
		
		// Create response elements
		ArrayList<RadiusAccount> xmlRadiusAccounts = new ArrayList<RadiusAccount>();		
		for (com.conversant.model.RadiusAccountInvoice account : accounts)
		{
			RadiusAccount xmlRadiusAccount = objectFactory.createRadiusAccount();
			xmlRadiusAccount.setRadAcctId(account.getRadAcct().getRadAcctId());
			xmlRadiusAccount.setInvoiceId(account.getInvoiceId());
			xmlRadiusAccount.setInvoiceLineId(account.getInvoiceLineId());
			xmlRadiusAccount.setUsername(account.getRadAcct().getUserName());
			
			try
			{
				GregorianCalendar c = new GregorianCalendar();
				c.setTime(account.getRadAcct().getAcctStartTime());
				xmlRadiusAccount.setAcctStartTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
				
				c = new GregorianCalendar();
				c.setTime(account.getRadAcct().getAcctStopTime());
				xmlRadiusAccount.setAcctStopTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
			}
			catch (DatatypeConfigurationException ex)
			{
				log.severe("Failed to set AcctStartTime or AcctStopTime for web service request to readRadiusAccountsByInvoice() for " + account + " - " + ex);
			}
			
			xmlRadiusAccount.setCalledStationId(account.getRadAcct().getCalledStationId());
			xmlRadiusAccount.setCallingStationId(account.getRadAcct().getCallingStationId());
			xmlRadiusAccount.setDestination(CDRToolConnector.getDestination(account.getRadAcct().getDestinationId()));
			xmlRadiusAccount.setPrice(account.getRadAcct().getPrice());
			xmlRadiusAccount.setRate(account.getRadAcct().getRate());
			
			xmlRadiusAccounts.add(xmlRadiusAccount);
		}
		
		// Set response elements
		readRadiusAccountsResponse.radiusAccount = xmlRadiusAccounts;		
		readRadiusAccountsResponse.setStandardResponse(getStandardResponse(true, "Radius Accounts have been read for Invoice[" + invoiceId + "]", trxName, xmlRadiusAccounts.size()));
		
		return readRadiusAccountsResponse;
	}
}
