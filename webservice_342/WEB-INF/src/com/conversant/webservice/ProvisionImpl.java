package com.conversant.webservice;

import java.util.Properties;

import javax.jws.WebService;

import org.compiere.model.MBPartner;
import org.compiere.model.MProduct;
import org.compiere.model.MSubscription;
import org.compiere.util.CLogger;
import org.compiere.util.Env;

import com.conversant.webservice.util.WebServiceConstants;
import com.conversant.wstore.DIDUtil;

@WebService(endpointInterface = "com.conversant.webservice.Provision")
public class ProvisionImpl extends GenericWebServiceImpl implements Provision
{
	private static CLogger log = CLogger.getCLogger(ProvisionImpl.class);
	
	/* Notes
	 * 
	 * - If WS Parameter set as "Free" perhaps could use "Constant Value" field as default value?
	 * - Trim inward parameters?
	 */
		
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
		String didNumber = createDIDSubscriptionRequest.getDidNumber();
		if (didNumber == null || didNumber.length() < 1)
			return getErrorStandardResponse("Invalid number", trxName);
		
		Integer businessPartnerId = createDIDSubscriptionRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || MBPartner.get(ctx, businessPartnerId) == null)
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		// Check for existing DID product pair exists
		MProduct monthlyProduct = null;
		MProduct[] existingProducts = DIDUtil.getDIDProducts(ctx, didNumber);		
		if (existingProducts.length == 2)
			monthlyProduct = DIDUtil.getSetupOrMonthlyProduct(ctx, existingProducts[0], existingProducts[1], false);
		else
			return getErrorStandardResponse("Failed to load MProduct[DID-" + didNumber + "] and/or MProduct[DIDSU-" + didNumber + "]", trxName);
		
		// Check monthly product exists
		if (monthlyProduct == null)
			return getErrorStandardResponse("Failed to load MProduct[DID-" + didNumber + "]", trxName);
		
		// Validate and/or retrieve businessPartnerLocationId
		Integer businessPartnerLocationId = validateBusinessPartnerLocationId(ctx, businessPartnerId, createDIDSubscriptionRequest.getBusinessPartnerLocationId());
		
		// Check for existing subscription
		if (DIDUtil.isMSubscribed(ctx, monthlyProduct))
			return getErrorStandardResponse(monthlyProduct + " is already subscribed", trxName);
		
		// Create subscription
		MSubscription subscription = DIDUtil.createDIDSubscription(ctx, didNumber, businessPartnerId, businessPartnerLocationId, monthlyProduct.getM_Product_ID(), trxName);
		if (subscription == null)
			return getErrorStandardResponse("Failed to create subscription for " + monthlyProduct + "MBPartner[" + businessPartnerId + "]", trxName);
		
		return getStandardResponse(true, "DID Subscription has been created", trxName, subscription.getC_Subscription_ID());
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
		String sipAddress = createSIPProductRequest.getSipAddress();
		if (sipAddress == null || sipAddress.length() < 1)
			return getErrorStandardResponse("Invalid sipAddress", trxName);
		
		String sipDomain = createSIPProductRequest.getSipDomain();
		if (sipDomain == null || sipDomain.length() < 1)
			return getErrorStandardResponse("Invalid sipDomain", trxName);
		
		// Check for existing SIP product(s)
		MProduct[] existingProducts = DIDUtil.getSIPProducts(ctx, sipAddress, sipDomain);
		if (existingProducts.length > 0)
			return getErrorStandardResponse("Found " + existingProducts.length + " SIP product(s) using sipAddress=" + sipAddress + " & sipDomain=" + sipDomain +", please remove manually", trxName);
		
		
		
		return null;
	}
	
	public StandardResponse createSIPSubscription(CreateSIPSubscriptionRequest createSIPSubscriptionRequest)
	{
		
		return null;
	}
}
