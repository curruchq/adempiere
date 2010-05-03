package com.conversant.webservice;

import java.util.HashMap;
import java.util.Properties;

import javax.jws.WebService;

import org.compiere.model.MBPartner;
import org.compiere.model.MProduct;
import org.compiere.model.MSubscription;
import org.compiere.util.CLogger;
import org.compiere.util.Env;

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
	 */
	
// ********************************************************************************************************************************************************************************
	
	// TODO: - Call this twice (once for setup and monthly)?
	// 		 - Have other methods for adding prices, purchaser tab, and product relations?
	public StandardResponse createDIDProduct(CreateDIDProductRequest createDIDProductRequest)
	{
		
		return null;
	}
		
	public StandardResponse createSIPProduct(CreateSIPProductRequest createSIPProductRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(createSIPProductRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.PROVISION_WEBSERVICE_ID, WebServiceConstants.CREATE_SIP_PRODUCT_METHOD_ID, createSIPProductRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		String address = createSIPProductRequest.getAddress();
		if (address == null || address.length() < 1)
			return getErrorStandardResponse("Invalid address", trxName);
		
		String domain = createSIPProductRequest.getDomain();
		if (domain == null || domain.length() < 1)
			return getErrorStandardResponse("Invalid domain", trxName);
		
		// Check for existing SIP product
		MProduct[] existingProducts = DIDUtil.getSIPProducts(ctx, address, domain);
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
		String error = login(ctx, WebServiceConstants.PROVISION_WEBSERVICE_ID, WebServiceConstants.CREATE_VOICEMAIL_PRODUCT_METHOD_ID, createVoicemailProductRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		String context = createVoicemailProductRequest.getContext();
		if (context == null || context.length() < 1)
			return getErrorStandardResponse("Invalid context", trxName);
		
		String macroName = createVoicemailProductRequest.getMacroName();
		if (macroName == null || macroName.length() < 1)
			return getErrorStandardResponse("Invalid macroName", trxName);
		
		String mailboxNumber = createVoicemailProductRequest.getMailboxNumber();
		if (mailboxNumber == null || mailboxNumber.length() < 1)
			return getErrorStandardResponse("Invalid mailboxNumber", trxName);
		
		// Check for existing Voicemail product
		MProduct[] existingProducts = DIDUtil.getVoicemailProducts(ctx, mailboxNumber);
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
		String error = login(ctx, WebServiceConstants.PROVISION_WEBSERVICE_ID, WebServiceConstants.CREATE_DID_SUBSCRIPTION_METHOD_ID, createDIDSubscriptionRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		String number = createDIDSubscriptionRequest.getNumber();
		if (number == null || number.length() < 1)
			return getErrorStandardResponse("Invalid number", trxName);
		
		Integer businessPartnerId = createDIDSubscriptionRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || MBPartner.get(ctx, businessPartnerId) == null)
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		// Check for existing DID product pair exists
		MProduct monthlyProduct = null;
		MProduct[] existingProducts = DIDUtil.getDIDProducts(ctx, number);		
		if (existingProducts.length == 2)
			monthlyProduct = DIDUtil.getSetupOrMonthlyProduct(ctx, existingProducts[0], existingProducts[1], false);
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
		String error = login(ctx, WebServiceConstants.PROVISION_WEBSERVICE_ID, WebServiceConstants.CREATE_SIP_SUBSCRIPTION_METHOD_ID, createSIPSubscriptionRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		String address = createSIPSubscriptionRequest.getAddress();
		if (address == null || address.length() < 1)
			return getErrorStandardResponse("Invalid sipAddress", trxName);
		
		String domain = createSIPSubscriptionRequest.getDomain();
		if (domain == null || domain.length() < 1)
			return getErrorStandardResponse("Invalid sipDomain", trxName);
		
		Integer businessPartnerId = createSIPSubscriptionRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || MBPartner.get(ctx, businessPartnerId) == null)
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		// Check for existing SIP product
		MProduct sipProduct = null;
		MProduct[] existingProducts = DIDUtil.getSIPProducts(ctx, address, domain);
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
		String error = login(ctx, WebServiceConstants.PROVISION_WEBSERVICE_ID, WebServiceConstants.CREATE_VOICEMAIL_SUBSCRIPTION_METHOD_ID, createVoicemailSubscriptionRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		String mailboxNumber = createVoicemailSubscriptionRequest.getMailboxNumber();
		if (mailboxNumber == null || mailboxNumber.length() < 1)
			return getErrorStandardResponse("Invalid mailboxNumber", trxName);
		
		Integer businessPartnerId = createVoicemailSubscriptionRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || MBPartner.get(ctx, businessPartnerId) == null)
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		// Check for existing Voicemail product
		MProduct voicemailProduct = null;
		MProduct[] existingProducts = DIDUtil.getVoicemailProducts(ctx, mailboxNumber);
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
	
	public StandardResponse createSERSubscriber(CreateSERSubscriberRequest createSERSubscriberRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx();		
		String trxName = getTrxName(createSERSubscriberRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.PROVISION_WEBSERVICE_ID, WebServiceConstants.CREATE_VOICEMAIL_SUBSCRIPTION_METHOD_ID, createSERSubscriberRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);
		
		// Load and validate parameters
		String username = createSERSubscriberRequest.getUsername();
		if (username == null || username.length() < 1)
			return getErrorStandardResponse("Invalid username", trxName);
		
		String password = createSERSubscriberRequest.getPassword();
		if (password == null || password.length() < 1)
			return getErrorStandardResponse("Invalid password", trxName);
		
		String domain = createSERSubscriberRequest.getDomain();
		if (domain == null || domain.length() < 1)
			return getErrorStandardResponse("Invalid domain", trxName);
		
		String timezone = createSERSubscriberRequest.getTimezone();
		if (timezone == null || timezone.length() < 1)
			return getErrorStandardResponse("Invalid timezone", trxName);
		
		Integer businessPartnerId = createSERSubscriberRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || MBPartner.get(ctx, businessPartnerId) == null)
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);

		// Add subscriber account to SER
		int serSubscriberId = SERConnector.addSIPAccount(username, password, domain, "", "", "", timezone, Integer.toString(businessPartnerId));
		if (serSubscriberId < 1)
			return getErrorStandardResponse("Failed to create SER subscriber for " + username + "@" + domain + " & MBPartner[" + businessPartnerId + "]", trxName);
		
		return getStandardResponse(true, "SER subscriber has been created", trxName, serSubscriberId);
	}

	public StandardResponse createSERUserPreference(CreateSERUserPreferenceRequest createSERUserPreferenceRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx();		
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.PROVISION_WEBSERVICE_ID, WebServiceConstants.CREATE_SER_USERPREFERENCE_METHOD_ID, createSERUserPreferenceRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String uuid = createSERUserPreferenceRequest.getUuid();
		if (uuid == null || uuid.length() < 1)
			return getErrorStandardResponse("Invalid uuid", null);
		
		String username = createSERUserPreferenceRequest.getUsername();
		if (username == null || username.length() < 1)
			return getErrorStandardResponse("Invalid username", null);
		
		String domain = createSERUserPreferenceRequest.getDomain();
		if (domain == null || domain.length() < 1)
			return getErrorStandardResponse("Invalid domain", null);
		
		String attribute = createSERUserPreferenceRequest.getAttribute();
		if (attribute == null || attribute.length() < 1)
			return getErrorStandardResponse("Invalid attribute", null);
		
		if (!attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE) && !attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT))
			return getErrorStandardResponse("Invalid attribute (valid values are " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE + " or " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT + ")", null);
		
		String value = createSERUserPreferenceRequest.getValue();
		if (value == null || value.length() < 1)
			return getErrorStandardResponse("Invalid value", null);
		
		String type = createSERUserPreferenceRequest.getType();
		if (type == null || type.length() < 1)
			return getErrorStandardResponse("Invalid type", null);
		
		if (attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE) && !type.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_TEXT))
			return getErrorStandardResponse("Invalid type for " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE + "(use " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_TEXT + ")", null);
		
		if (attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT) && !type.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_NUMERIC))
			return getErrorStandardResponse("Invalid type for " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT + "(use " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_NUMERIC + ")", null);
		
		String subscriberId = createSERUserPreferenceRequest.getSubscriberId();
		if (subscriberId == null || subscriberId.length() < 1)
			return getErrorStandardResponse("Invalid subscriberId", null);

		if (!SERConnector.addUserPreference(uuid, username, domain, attribute, value, type, subscriberId))
			return getErrorStandardResponse("Failed to create SER User Preference", null);
			
		return getStandardResponse(true, "SER User Preference has been created", null, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
}
