package com.conversant.webservice;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Properties;

import javax.jws.WebService;

import org.compiere.model.MBPartner;
import org.compiere.model.MConversionRate;
import org.compiere.model.MCurrency;
import org.compiere.model.MPriceListVersion;
import org.compiere.model.MProduct;
import org.compiere.model.MSubscription;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.wstore.DIDController;

import com.conversant.db.AsteriskConnector;
import com.conversant.db.SERConnector;
import com.conversant.did.DIDConstants;
import com.conversant.did.DIDUtil;
import com.conversant.webservice.util.WebServiceConstants;

@WebService(endpointInterface = "com.conversant.webservice.Provision")
public class ProvisionImpl extends GenericWebServiceImpl implements Provision
{
	private static CLogger log = CLogger.getCLogger(ProvisionImpl.class);
	
	/* Notes
	 * 
	 * - If WS Parameter set as "Free" perhaps could use "Constant Value" field as default value?
	 * - Trim inward parameters?
	 * - Check if Env.getCtx() creates ctx if it doesn't exist yet? And if it's already created does it get previous login ctx?
	 * - Set did product subscribed to true when did subscription created
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
		if (number == null || number.length() < 1)
			return getErrorStandardResponse("Invalid number", trxName);
		else
			number = number.trim();
		
		String countryId = createDIDProductRequest.getCountryId();
		if (countryId == null || countryId.length() < 1)
			return getErrorStandardResponse("Invalid countryId", trxName);
		else
			countryId = countryId.trim();
		
		String countryCode = createDIDProductRequest.getCountryCode();
		if (countryCode == null || countryCode.length() < 1)
			return getErrorStandardResponse("Invalid countryCode", trxName);
		else
			countryCode = countryCode.trim();
		
		String areaCode = createDIDProductRequest.getAreaCode();
		if (areaCode == null || areaCode.length() < 1)
			return getErrorStandardResponse("Invalid areaCode", trxName);
		else
			areaCode = areaCode.trim();
		
		String areaCodeDescription = createDIDProductRequest.getAreaCodeDescription();
		if (areaCodeDescription == null || areaCodeDescription.length() < 1)
			return getErrorStandardResponse("Invalid areaCodeDescription", trxName);
		else
			areaCodeDescription = areaCodeDescription.trim();
		
		String freeMinutes = createDIDProductRequest.getFreeMinutes();
		if (number == null || number.length() < 1)
			return getErrorStandardResponse("Invalid freeMinutes", trxName);
		else
			freeMinutes = freeMinutes.trim();
		
		String perMinuteCharge = createDIDProductRequest.getPerMinuteCharge();
		if (perMinuteCharge == null || perMinuteCharge.length() < 1)
			return getErrorStandardResponse("Invalid perMinuteCharge", trxName);
		else
			perMinuteCharge = perMinuteCharge.trim();
		
		Integer businessPartnerId = createDIDProductRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		String setupCost = createDIDProductRequest.getSetupCost();
		if (setupCost == null || setupCost.length() < 1)
			return getErrorStandardResponse("Invalid setupCost", trxName);
		else
			setupCost = setupCost.trim();
		
		String monthlyCharge = createDIDProductRequest.getMonthlyCharge();
		if (monthlyCharge == null || monthlyCharge.length() < 1)
			return getErrorStandardResponse("Invalid monthlyCharge", trxName);
		else
			monthlyCharge = monthlyCharge.trim();
		
		Integer currencyId = createDIDProductRequest.getCurrencyId();
		if (currencyId == null || currencyId < 1 || !validateADId(MCurrency.Table_Name, currencyId, trxName)) 
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		Integer pricelistVersionId = createDIDProductRequest.getPricelistVersionId();
		if (pricelistVersionId == null || pricelistVersionId < 1 || !validateADId(MPriceListVersion.Table_Name, pricelistVersionId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
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
		
		// Create attributes
		HashMap<Integer, String> attributes = new HashMap<Integer, String>();
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
			return getErrorStandardResponse("Failed to create setup product for " + number, trxName);

		attributes.remove(DIDConstants.ATTRIBUTE_ID_DID_ISSETUP);
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_ISSETUP, Boolean.FALSE.toString());
		
		MProduct monthlyDIDProduct = DIDUtil.createDIDProduct(ctx, attributes, trxName);
		if (monthlyDIDProduct == null)
			return getErrorStandardResponse("Failed to create monthly product for " + number, trxName);
		
		// Set product prices
		if (!DIDController.updateProductPrice(ctx, pricelistVersionId, setupDIDProduct.getM_Product_ID(), setupCostNZD, trxName))
			return getErrorStandardResponse("Failed create product price for DIDSU-" + number, trxName);
			
		if (!DIDController.updateProductPrice(ctx, pricelistVersionId, monthlyDIDProduct.getM_Product_ID(), monthlyChargeNZD, trxName))
			return getErrorStandardResponse("Failed create product price for DID-" + number, trxName);
		
		// Set Business Partner's price list prices
		if (!DIDController.updateBPPriceListPrice(ctx, businessPartnerId, setupDIDProduct.getM_Product_ID(), setupCostBD, trxName))
			return getErrorStandardResponse("Failed create BP price list price for DIDSU-" + number, trxName);
		
		if (!DIDController.updateBPPriceListPrice(ctx, businessPartnerId, monthlyDIDProduct.getM_Product_ID(), monthlyChargeBD, trxName))
			return getErrorStandardResponse("Failed create BP price list price for DID-" + number, trxName);
		
		// Set product purchaser info
		if (!DIDController.updateProductPO(ctx, businessPartnerId, setupDIDProduct, setupCostBD, currencyId, trxName))
			return getErrorStandardResponse("Failed create purchaser info for DIDSU-" + number, trxName);
		
		if (!DIDController.updateProductPO(ctx, businessPartnerId, monthlyDIDProduct, monthlyChargeBD, currencyId, trxName))
			return getErrorStandardResponse("Failed create purchaser info for DID-" + number, trxName);
		
		// Set product relation
		if (!DIDController.updateProductRelations(ctx, monthlyDIDProduct.getM_Product_ID(), setupDIDProduct.getM_Product_ID(), trxName))
			return getErrorStandardResponse("Failed create relation between DID-" + number + " & DIDSU-" + number, trxName);
		
		return getStandardResponse(true, "DID products have been created for " + number, trxName, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
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
		if (address == null || address.length() < 1)
			return getErrorStandardResponse("Invalid address", trxName);
		else
			address = address.trim();
		
		String domain = createSIPProductRequest.getDomain();
		if (domain == null || domain.length() < 1)
			return getErrorStandardResponse("Invalid domain", trxName);
		else
			domain = domain.trim();
		
		// Check for existing SIP product
		MProduct[] existingProducts = DIDUtil.getSIPProducts(ctx, address, domain, trxName);
		if (existingProducts.length > 0)
			return getErrorStandardResponse("Found " + existingProducts.length + " SIP product(s) using address=" + address + " & domain=" + domain +", please remove manually", trxName);
		
		// Create attributes
		HashMap<Integer, String> attributes = new HashMap<Integer, String>();
		attributes.put(DIDConstants.ATTRIBUTE_ID_SIP_ADDRESS, address);
		attributes.put(DIDConstants.ATTRIBUTE_ID_SIP_DOMAIN, domain);
		
		// Create SIP product
		MProduct sipProduct = DIDUtil.createSIPProduct(ctx, attributes, trxName);
		if (sipProduct == null)
			return getErrorStandardResponse("Failed to create product for " + address + "@" + domain, trxName);
				
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
		if (context == null || context.length() < 1)
			return getErrorStandardResponse("Invalid context", trxName);
		else
			context = context.trim();
		
		String macroName = createVoicemailProductRequest.getMacroName();
		if (macroName == null || macroName.length() < 1)
			return getErrorStandardResponse("Invalid macroName", trxName);
		else
			macroName = macroName.trim();
		
		String mailboxNumber = createVoicemailProductRequest.getMailboxNumber();
		if (mailboxNumber == null || mailboxNumber.length() < 1)
			return getErrorStandardResponse("Invalid mailboxNumber", trxName);
		else
			mailboxNumber = mailboxNumber.trim();
		
		// Check for existing Voicemail product
		MProduct[] existingProducts = DIDUtil.getVoicemailProducts(ctx, mailboxNumber, trxName);
		if (existingProducts.length > 0)
			return getErrorStandardResponse("Found " + existingProducts.length + " Voicemail product(s) using mailboxNumber=" + mailboxNumber + ", please remove manually", trxName);
		
		// Create attributes
		HashMap<Integer, String> attributes = new HashMap<Integer, String>();
		attributes.put(DIDConstants.ATTRIBUTE_ID_VM_CONTEXT, context);
		attributes.put(DIDConstants.ATTRIBUTE_ID_VM_MACRO_NAME, macroName);
		attributes.put(DIDConstants.ATTRIBUTE_ID_VM_MAILBOX_NUMBER, mailboxNumber);
		
		// Create Voicemail product
		MProduct voicemailProduct = DIDUtil.createVoicemailProduct(ctx, attributes, trxName);
		if (voicemailProduct == null)
			return getErrorStandardResponse("Failed to create product for " + mailboxNumber, trxName);
				
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
		if (number == null || number.length() < 1)
			return getErrorStandardResponse("Invalid number", trxName);
		else
			number = number.trim();
		
		Integer businessPartnerId = createDIDSubscriptionRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		// Check for existing DID product pair exists
		MProduct monthlyProduct = null;
		MProduct[] existingProducts = DIDUtil.getDIDProducts(ctx, number, trxName);		
		if (existingProducts.length == 2)
			monthlyProduct = DIDUtil.getSetupOrMonthlyProduct(ctx, existingProducts[0], existingProducts[1], false, trxName);
		else
			return getErrorStandardResponse("Failed to load MProduct[" + DIDConstants.DID_MONTHLY_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, number) + "]" + 
											" and/or MProduct[" + DIDConstants.DID_SETUP_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, number) + "]", trxName);
		
		// Double check monthly product exists
		if (monthlyProduct == null)
			return getErrorStandardResponse("Failed to load MProduct[" + DIDConstants.DID_MONTHLY_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, number) + "]", trxName);
		
		// Validate and/or retrieve businessPartnerLocationId
		Integer businessPartnerLocationId = validateBusinessPartnerLocationId(ctx, businessPartnerId, createDIDSubscriptionRequest.getBusinessPartnerLocationId());
		
		// Check for existing subscription
		if (DIDUtil.isMSubscribed(ctx, monthlyProduct))
			return getErrorStandardResponse(monthlyProduct + " is already subscribed", trxName);
		
		// Create subscription
		MSubscription subscription = DIDUtil.createDIDSubscription(ctx, number, businessPartnerId, businessPartnerLocationId, monthlyProduct.getM_Product_ID(), trxName);
		if (subscription == null)
			return getErrorStandardResponse("Failed to create subscription for " + monthlyProduct + " MBPartner[" + businessPartnerId + "]", trxName);
		
		return getStandardResponse(true, "DID subscription has been created", trxName, subscription.getC_Subscription_ID());
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
		if (address == null || address.length() < 1)
			return getErrorStandardResponse("Invalid sipAddress", trxName);
		else
			address = address.trim();
		
		String domain = createSIPSubscriptionRequest.getDomain();
		if (domain == null || domain.length() < 1)
			return getErrorStandardResponse("Invalid sipDomain", trxName);
		else
			domain = domain.trim();
		
		Integer businessPartnerId = createSIPSubscriptionRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
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
		if (mailboxNumber == null || mailboxNumber.length() < 1)
			return getErrorStandardResponse("Invalid mailboxNumber", trxName);
		else
			mailboxNumber = mailboxNumber.trim();
		
		Integer businessPartnerId = createVoicemailSubscriptionRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
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
		if (username == null || username.length() < 1)
			return getErrorStandardResponse("Invalid username", null);
		else
			username = username.trim();
		
		String password = createSubscriberRequest.getPassword();
		if (password == null || password.length() < 1)
			return getErrorStandardResponse("Invalid password", null);
		else
			password = password.trim();
		
		String domain = createSubscriberRequest.getDomain();
		if (domain == null || domain.length() < 1)
			return getErrorStandardResponse("Invalid domain", null);
		else
			domain = domain.trim();
		
		String timezone = createSubscriberRequest.getTimezone();
		if (timezone == null || timezone.length() < 1)
			return getErrorStandardResponse("Invalid timezone", null);
		else
			timezone = timezone.trim();
		
		Integer businessPartnerId = createSubscriberRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, null))
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
		if (username == null || username.length() < 1)
			return getErrorStandardResponse("Invalid username", null);
		else
			username = username.trim();
		
		String domain = deleteSubscriberRequest.getDomain();
		if (domain == null || domain.length() < 1)
			return getErrorStandardResponse("Invalid domain", null);
		else
			domain = domain.trim();
		
		Integer businessPartnerId = deleteSubscriberRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, null))
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
		if (uuid == null || uuid.length() < 1)
			return getErrorStandardResponse("Invalid uuid", null);
		else
			uuid = uuid.trim();
		
		String username = createUserPreferenceRequest.getUsername();
		if (username == null || username.length() < 1)
			return getErrorStandardResponse("Invalid username", null);
		else
			username = username.trim();
		
		String domain = createUserPreferenceRequest.getDomain();
		if (domain == null || domain.length() < 1)
			return getErrorStandardResponse("Invalid domain", null);
		else
			domain = domain.trim();
		
		String attribute = createUserPreferenceRequest.getAttribute();
		if (attribute == null || attribute.length() < 1)
			return getErrorStandardResponse("Invalid attribute", null);
		else 
			attribute = attribute.trim();
		
		// Validate attribute
		if (!attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE) && !attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT))
			return getErrorStandardResponse("Invalid attribute (valid values are " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE + " or " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT + ")", null);
		
		String value = createUserPreferenceRequest.getValue();
		if (value == null || value.length() < 1)
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
		if (type == null || type.length() < 1)
			return getErrorStandardResponse("Invalid type", null);
		else
			type = type.trim();
		
		// Validate attribute/type pair
		if (attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE) && !type.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_TEXT))
			return getErrorStandardResponse("Invalid type for " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE + "(use " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_TEXT + ")", null);
		
		if (attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT) && !type.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_NUMERIC))
			return getErrorStandardResponse("Invalid type for " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT + "(use " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_NUMERIC + ")", null);
		
		String subscriberId = createUserPreferenceRequest.getSubscriberId();
		if (subscriberId == null || subscriberId.length() < 1)
			return getErrorStandardResponse("Invalid subscriberId", null);
		else 
			subscriberId = subscriberId.trim();

		// Add user preference
		if (!SERConnector.addUserPreference(uuid, username, domain, attribute, value, type, subscriberId))
			return getErrorStandardResponse("Failed to create User Preference UUID[" + uuid + "] Attribute[" + attribute + "]", null);
			
		return getStandardResponse(true, "User Preference has been created", null, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
	
	public StandardResponse readUserPreference(ReadUserPreferenceRequest readUserPreferenceRequest)
	{
		return getErrorStandardResponse("Failed - readUserPreference() hasn't been implemented", null);
	}
	
	public StandardResponse updateUserPreference(UpdateUserPreferenceRequest updateUserPreferenceRequest)
	{
		return getErrorStandardResponse("Failed - updateUserPreference() hasn't been implemented", null);
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
		if (uuid == null || uuid.length() < 1)
			return getErrorStandardResponse("Invalid uuid", null);
		else
			uuid = uuid.trim();
		
		String username = deleteUserPreferenceRequest.getUsername();
		if (username == null || username.length() < 1)
			return getErrorStandardResponse("Invalid username", null);
		else
			username = username.trim();
		
		String domain = deleteUserPreferenceRequest.getDomain();
		if (domain == null || domain.length() < 1)
			return getErrorStandardResponse("Invalid domain", null);
		else
			domain = domain.trim();
		
		String attribute = deleteUserPreferenceRequest.getAttribute();
		if (attribute == null || attribute.length() < 1)
			return getErrorStandardResponse("Invalid attribute", null);
		else
			attribute = attribute.trim();
		
		// Validate attribute 
		if (!attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE) && !attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT))
			return getErrorStandardResponse("Invalid attribute (valid values are " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE + " or " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT + ")", null);
		
		String value = deleteUserPreferenceRequest.getValue();
		if (value == null || value.length() < 1)
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
		if (type == null || type.length() < 1)
			return getErrorStandardResponse("Invalid type", null);
		else
			type = type.trim();
		
		// Validate attribute/type pair
		if (attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE) && !type.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_TEXT))
			return getErrorStandardResponse("Invalid type for " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE + "(use " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_TEXT + ")", null);
		
		if (attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT) && !type.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_NUMERIC))
			return getErrorStandardResponse("Invalid type for " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT + "(use " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_NUMERIC + ")", null);
		
		String subscriberId = deleteUserPreferenceRequest.getSubscriberId();
		if (subscriberId == null || subscriberId.length() < 1)
			return getErrorStandardResponse("Invalid subscriberId", null);
		else
			subscriberId = subscriberId.trim();

		// Delete user preference
		if (!SERConnector.removeUserPreference(uuid, username, domain, attribute, value, type, subscriberId))
			return getErrorStandardResponse("Failed to delete User Preference UUID[" + uuid + "] Attribute[" + attribute + "]", null);
			
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
		if (mailboxNumber == null || mailboxNumber.length() < 1)
			return getErrorStandardResponse("Invalid mailboxNumber", null);
		else 
			mailboxNumber = mailboxNumber.trim();
		
		Integer businessPartnerId = createVoicemailUserRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, null))
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
		if (mailboxNumber == null || mailboxNumber.length() < 1)
			return getErrorStandardResponse("Invalid mailboxNumber", null);
		else 
			mailboxNumber = mailboxNumber.trim();
		
		Integer businessPartnerId = deleteVoicemailUserRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, null))
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
		if (mailboxNumber == null || mailboxNumber.length() < 1)
			return getErrorStandardResponse("Invalid mailboxNumber", null);
		else
			mailboxNumber = mailboxNumber.trim();
		
		String domain = createVoicemailUserPreferencesRequest.getDomain();
		if (domain == null || domain.length() < 1)
			return getErrorStandardResponse("Invalid domain", null);
		else 
			domain = domain.trim();
		
		Integer businessPartnerId = createVoicemailUserPreferencesRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, null))
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
		if (mailboxNumber == null || mailboxNumber.length() < 1)
			return getErrorStandardResponse("Invalid mailboxNumber", null);
		else
			mailboxNumber = mailboxNumber.trim();
		
		String domain = deleteVoicemailUserPreferencesRequest.getDomain();
		if (domain == null || domain.length() < 1)
			return getErrorStandardResponse("Invalid domain", null);
		else
			domain = domain.trim();
		
		Integer businessPartnerId = deleteVoicemailUserPreferencesRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, null))
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
		if (mailboxNumber == null || mailboxNumber.length() < 1)
			return getErrorStandardResponse("Invalid mailboxNumber", null);
		else 
			mailboxNumber = mailboxNumber.trim();
		
		Integer businessPartnerId = createVoicemailDialPlanRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, null))
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
		if (mailboxNumber == null || mailboxNumber.length() < 1)
			return getErrorStandardResponse("Invalid mailboxNumber", null);
		else
			mailboxNumber = mailboxNumber.trim();
		
		Integer businessPartnerId = deleteVoicemailDialPlanRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, null))
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
		if (didNumber == null || didNumber.length() < 1)
			return getErrorStandardResponse("Invalid didNumber", null);
		
		String sipAddress = validateProvisionDIDParametersRequest.getSipAddress();
		if (sipAddress == null || sipAddress.length() < 1)
			return getErrorStandardResponse("Invalid sipAddress", null);
		
		String sipDomain = validateProvisionDIDParametersRequest.getSipDomain();
		if (sipDomain == null || sipDomain.length() < 1)
			return getErrorStandardResponse("Invalid sipDomain", null);
		
		String sipPassword = validateProvisionDIDParametersRequest.getSipPassword();
		if (sipPassword == null || sipPassword.length() < 1)
			return getErrorStandardResponse("Invalid sipPassword", null);
		
		String sipTimezone = validateProvisionDIDParametersRequest.getSipTimezone();
		if (sipTimezone == null || sipTimezone.length() < 1)
			return getErrorStandardResponse("Invalid sipTimezone", null);
		
		String voicemailContext = validateProvisionDIDParametersRequest.getVoicemailContext();
		if (voicemailContext == null || voicemailContext.length() < 1)
			return getErrorStandardResponse("Invalid voicemailContext", null);
		
		String voicemailDomain = validateProvisionDIDParametersRequest.getVoicemailDomain();
		if (voicemailDomain == null || voicemailDomain.length() < 1)
			return getErrorStandardResponse("Invalid voicemailDomain", null);
		
		String voicemailMacroName = validateProvisionDIDParametersRequest.getVoicemailMacroName();
		if (voicemailMacroName == null || voicemailMacroName.length() < 1)
			return getErrorStandardResponse("Invalid voicemailMacroName", null);
		
		String voicemailMailboxNumber = validateProvisionDIDParametersRequest.getVoicemailMailboxNumber();
		if (voicemailMailboxNumber == null || voicemailMailboxNumber.length() < 1)
			return getErrorStandardResponse("Invalid voicemailMailboxNumber", null);
		
		Integer businessPartnerId = validateProvisionDIDParametersRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, null))
			return getErrorStandardResponse("Invalid businessPartnerId", null);
		
		Integer businessPartnerLocationId = validateBusinessPartnerLocationId(ctx, businessPartnerId, validateProvisionDIDParametersRequest.getBusinessPartnerLocationId());
		if (businessPartnerLocationId == null || businessPartnerLocationId < 1)
			return getErrorStandardResponse("Invalid businessPartnerLocationId", null);
		
		boolean converseVoiceActive = validateProvisionDIDParametersRequest.isConverseVoiceActive();
		boolean cOutActive = validateProvisionDIDParametersRequest.isCOutActive();
		
		return getStandardResponse(true, "ProvisionDID parameters have been validated", null, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
	
}
