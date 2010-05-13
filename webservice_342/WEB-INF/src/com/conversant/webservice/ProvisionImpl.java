package com.conversant.webservice;

import java.util.HashMap;
import java.util.Properties;

import javax.jws.WebService;

import org.compiere.model.MBPartner;
import org.compiere.model.MProduct;
import org.compiere.model.MSubscription;
import org.compiere.util.CLogger;
import org.compiere.util.Env;

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
		MProduct[] existingProducts = DIDUtil.getDIDProducts(ctx, number, trxName);		
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
		String error = login(ctx, WebServiceConstants.PROVISION_WEBSERVICE_ID, WebServiceConstants.CREATE_SUBSCRIBER_METHOD_ID, createSubscriberRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String username = createSubscriberRequest.getUsername();
		if (username == null || username.length() < 1)
			return getErrorStandardResponse("Invalid username", null);
		
		String password = createSubscriberRequest.getPassword();
		if (password == null || password.length() < 1)
			return getErrorStandardResponse("Invalid password", null);
		
		String domain = createSubscriberRequest.getDomain();
		if (domain == null || domain.length() < 1)
			return getErrorStandardResponse("Invalid domain", null);
		
		String timezone = createSubscriberRequest.getTimezone();
		if (timezone == null || timezone.length() < 1)
			return getErrorStandardResponse("Invalid timezone", null);
		
		Integer businessPartnerId = createSubscriberRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || MBPartner.get(ctx, businessPartnerId) == null)
			return getErrorStandardResponse("Invalid businessPartnerId", null);

		// Add subscriber account
		int subscriberId = SERConnector.addSIPAccount(username, password, domain, "", "", "", timezone, Integer.toString(businessPartnerId));
		if (subscriberId < 1)
			return getErrorStandardResponse("Failed to create subscriber for " + username + "@" + domain + " & MBPartner[" + businessPartnerId + "]", null);
		
		return getStandardResponse(true, "Subscriber has been created", null, subscriberId);
	}
	
	public StandardResponse deleteSubscriber(DeleteSubscriberRequest deleteSubscriberRequest)
	{
		// Create ctx
		Properties ctx = Env.getCtx();		
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.PROVISION_WEBSERVICE_ID, WebServiceConstants.DELETE_SUBSCRIBER_METHOD_ID, deleteSubscriberRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String username = deleteSubscriberRequest.getUsername();
		if (username == null || username.length() < 1)
			return getErrorStandardResponse("Invalid username", null);
		
		String domain = deleteSubscriberRequest.getDomain();
		if (domain == null || domain.length() < 1)
			return getErrorStandardResponse("Invalid domain", null);
		
		Integer businessPartnerId = deleteSubscriberRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || MBPartner.get(ctx, businessPartnerId) == null)
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
		String error = login(ctx, WebServiceConstants.PROVISION_WEBSERVICE_ID, WebServiceConstants.CREATE_USER_PREFERENCE_METHOD_ID, createUserPreferenceRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String uuid = createUserPreferenceRequest.getUuid();
		if (uuid == null || uuid.length() < 1)
			return getErrorStandardResponse("Invalid uuid", null);
		
		String username = createUserPreferenceRequest.getUsername();
		if (username == null || username.length() < 1)
			return getErrorStandardResponse("Invalid username", null);
		
		String domain = createUserPreferenceRequest.getDomain();
		if (domain == null || domain.length() < 1)
			return getErrorStandardResponse("Invalid domain", null);
		
		String attribute = createUserPreferenceRequest.getAttribute();
		if (attribute == null || attribute.length() < 1)
			return getErrorStandardResponse("Invalid attribute", null);
		
		// Validate attribute
		if (!attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE) && !attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT))
			return getErrorStandardResponse("Invalid attribute (valid values are " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE + " or " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT + ")", null);
		
		String value = createUserPreferenceRequest.getValue();
		if (value == null || value.length() < 1)
			return getErrorStandardResponse("Invalid value", null);
		
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
		
		// Validate attribute/type pair
		if (attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE) && !type.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_TEXT))
			return getErrorStandardResponse("Invalid type for " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE + "(use " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_TEXT + ")", null);
		
		if (attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT) && !type.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_NUMERIC))
			return getErrorStandardResponse("Invalid type for " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT + "(use " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_NUMERIC + ")", null);
		
		String subscriberId = createUserPreferenceRequest.getSubscriberId();
		if (subscriberId == null || subscriberId.length() < 1)
			return getErrorStandardResponse("Invalid subscriberId", null);

		// Add user preference
		if (!SERConnector.addUserPreference(uuid, username, domain, attribute, value, type, subscriberId))
			return getErrorStandardResponse("Failed to create User Preference UUID[" + uuid + "] Attribute[" + attribute + "]", null);
			
		return getStandardResponse(true, "User Preference has been created", null, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
	
	public StandardResponse deleteUserPreference(DeleteUserPreferenceRequest deleteUserPreferenceRequest)
	{
		// Create ctx
		Properties ctx = Env.getCtx();		
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.PROVISION_WEBSERVICE_ID, WebServiceConstants.DELETE_USER_PREFERENCE_METHOD_ID, deleteUserPreferenceRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String uuid = deleteUserPreferenceRequest.getUuid();
		if (uuid == null || uuid.length() < 1)
			return getErrorStandardResponse("Invalid uuid", null);
		
		String username = deleteUserPreferenceRequest.getUsername();
		if (username == null || username.length() < 1)
			return getErrorStandardResponse("Invalid username", null);
		
		String domain = deleteUserPreferenceRequest.getDomain();
		if (domain == null || domain.length() < 1)
			return getErrorStandardResponse("Invalid domain", null);
		
		String attribute = deleteUserPreferenceRequest.getAttribute();
		if (attribute == null || attribute.length() < 1)
			return getErrorStandardResponse("Invalid attribute", null);
		
		// Validate attribute 
		if (!attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE) && !attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT))
			return getErrorStandardResponse("Invalid attribute (valid values are " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE + " or " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT + ")", null);
		
		String value = deleteUserPreferenceRequest.getValue();
		if (value == null || value.length() < 1)
			return getErrorStandardResponse("Invalid value", null);
		
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
		
		// Validate attribute/type pair
		if (attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE) && !type.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_TEXT))
			return getErrorStandardResponse("Invalid type for " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_CONVERSEVOICE + "(use " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_TEXT + ")", null);
		
		if (attribute.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT) && !type.equals(DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_NUMERIC))
			return getErrorStandardResponse("Invalid type for " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_COUT + "(use " + DIDConstants.SER_USER_PREFERENCE_ATTRIBUTE_TYPE_NUMERIC + ")", null);
		
		String subscriberId = deleteUserPreferenceRequest.getSubscriberId();
		if (subscriberId == null || subscriberId.length() < 1)
			return getErrorStandardResponse("Invalid subscriberId", null);

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
		String error = login(ctx, WebServiceConstants.PROVISION_WEBSERVICE_ID, WebServiceConstants.CREATE_VOICEMAIL_USER_METHOD_ID, createVoicemailUserRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String mailboxNumber = createVoicemailUserRequest.getMailboxNumber();
		if (mailboxNumber == null || mailboxNumber.length() < 1)
			return getErrorStandardResponse("Invalid mailboxNumber", null);
		
		Integer businessPartnerId = createVoicemailUserRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || MBPartner.get(ctx, businessPartnerId) == null)
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
		String error = login(ctx, WebServiceConstants.PROVISION_WEBSERVICE_ID, WebServiceConstants.DELETE_VOICEMAIL_USER_METHOD_ID, deleteVoicemailUserRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String mailboxNumber = deleteVoicemailUserRequest.getMailboxNumber();
		if (mailboxNumber == null || mailboxNumber.length() < 1)
			return getErrorStandardResponse("Invalid mailboxNumber", null);
		
		Integer businessPartnerId = deleteVoicemailUserRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || MBPartner.get(ctx, businessPartnerId) == null)
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
		String error = login(ctx, WebServiceConstants.PROVISION_WEBSERVICE_ID, WebServiceConstants.CREATE_VOICEMAIL_USER_PREFERENCES_METHOD_ID, createVoicemailUserPreferencesRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String mailboxNumber = createVoicemailUserPreferencesRequest.getMailboxNumber();
		if (mailboxNumber == null || mailboxNumber.length() < 1)
			return getErrorStandardResponse("Invalid mailboxNumber", null);
		
		String domain = createVoicemailUserPreferencesRequest.getDomain();
		if (domain == null || domain.length() < 1)
			return getErrorStandardResponse("Invalid domain", null);
		
		Integer businessPartnerId = createVoicemailUserPreferencesRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || MBPartner.get(ctx, businessPartnerId) == null)
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
		String error = login(ctx, WebServiceConstants.PROVISION_WEBSERVICE_ID, WebServiceConstants.DELETE_VOICEMAIL_USER_PREFERENCES_METHOD_ID, deleteVoicemailUserPreferencesRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String mailboxNumber = deleteVoicemailUserPreferencesRequest.getMailboxNumber();
		if (mailboxNumber == null || mailboxNumber.length() < 1)
			return getErrorStandardResponse("Invalid mailboxNumber", null);
		
		String domain = deleteVoicemailUserPreferencesRequest.getDomain();
		if (domain == null || domain.length() < 1)
			return getErrorStandardResponse("Invalid domain", null);
		
		Integer businessPartnerId = deleteVoicemailUserPreferencesRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || MBPartner.get(ctx, businessPartnerId) == null)
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
		String error = login(ctx, WebServiceConstants.PROVISION_WEBSERVICE_ID, WebServiceConstants.CREATE_VOICEMAIL_DIALPLAN_METHOD_ID, createVoicemailDialPlanRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String mailboxNumber = createVoicemailDialPlanRequest.getMailboxNumber();
		if (mailboxNumber == null || mailboxNumber.length() < 1)
			return getErrorStandardResponse("Invalid mailboxNumber", null);
		
		Integer businessPartnerId = createVoicemailDialPlanRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || MBPartner.get(ctx, businessPartnerId) == null)
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
		String error = login(ctx, WebServiceConstants.PROVISION_WEBSERVICE_ID, WebServiceConstants.DELETE_VOICEMAIL_DIALPLAN_METHOD_ID, deleteVoicemailDialPlanRequest.getLoginRequest(), null);		
		if (error != null)	
			return getErrorStandardResponse(error, null);
		
		// Load and validate parameters
		String mailboxNumber = deleteVoicemailDialPlanRequest.getMailboxNumber();
		if (mailboxNumber == null || mailboxNumber.length() < 1)
			return getErrorStandardResponse("Invalid mailboxNumber", null);
		
		Integer businessPartnerId = deleteVoicemailDialPlanRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || MBPartner.get(ctx, businessPartnerId) == null)
			return getErrorStandardResponse("Invalid businessPartnerId", null);
		
		MBPartner businessPartner = MBPartner.get(ctx, businessPartnerId);
		String bpSearchKey = businessPartner.getValue();
		
		if (!AsteriskConnector.removeVoicemailFromDialPlan(mailboxNumber, bpSearchKey))
			return getErrorStandardResponse("Failed to delete Voicemail DialPlan[" + mailboxNumber + "-" + bpSearchKey + "]", null);
		
		return getStandardResponse(true, "Voicemail DialPlan has been deleted", null, WebServiceConstants.STANDARD_RESPONSE_DEFAULT_ID);
	}
	
}
