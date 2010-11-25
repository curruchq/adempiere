package com.conversant.webservice;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Properties;

import javax.jws.WebService;

import org.compiere.model.MBPGroup;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerEx;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MCountry;
import org.compiere.model.MInvoiceSchedule;
import org.compiere.model.MLocation;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderEx;
import org.compiere.model.MRegion;
import org.compiere.model.MSubscription;
import org.compiere.model.MUser;
import org.compiere.model.X_C_City;
import org.compiere.util.Env;

import com.conversant.did.DIDUtil;
import com.conversant.util.Validation;
import com.conversant.webservice.util.WebServiceConstants;
import com.conversant.webservice.util.WebServiceUtil;

@WebService(endpointInterface = "com.conversant.webservice.Admin")
public class AdminImpl extends GenericWebServiceImpl implements Admin
{
	public StandardResponse createBusinessPartner(CreateBusinessPartnerRequest createBusinessPartnerRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(createBusinessPartnerRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("CREATE_BUSINESS_PARTNER_METHOD_ID"), createBusinessPartnerRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		String searchKey = createBusinessPartnerRequest.getSearchKey();
		if (!validateString(searchKey)) 
			searchKey = null; // Allow ADempiere auto sequencing to set Search Key
		else
			searchKey = searchKey.trim();
		
		String name = createBusinessPartnerRequest.getName();
		if (!validateString(name))
			return getErrorStandardResponse("Invalid name", trxName);
		else
			name = name.trim();
		
		Integer businessPartnerGroupId = createBusinessPartnerRequest.getBusinessPartnerGroupId();
		if (businessPartnerGroupId == null || businessPartnerGroupId < 1 || !Validation.validateADId(MBPGroup.Table_Name, businessPartnerGroupId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerGroupId", trxName);

		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put(MBPartner.COLUMNNAME_Name, name);
		fields.put(MBPartner.COLUMNNAME_C_BP_Group_ID, businessPartnerGroupId);
		
		if (searchKey != null)
			fields.put(MBPartner.COLUMNNAME_Value, searchKey);

		MBPartner businessPartner = new MBPartner(ctx, 0, trxName);
		if (!Validation.validateMandatoryFields(businessPartner, fields))
			return getErrorStandardResponse("Missing mandatory fields", trxName);

		if (fields.get(MBPartner.COLUMNNAME_Value) != null)
			businessPartner.setValue((String)fields.get(MBPartner.COLUMNNAME_Value));
		
		businessPartner.setName((String)fields.get(MBPartner.COLUMNNAME_Name));
		businessPartner.setBPGroup(MBPGroup.get(ctx, (Integer)fields.get(MBPartner.COLUMNNAME_C_BP_Group_ID)));
		
		// Set invoice schedule
		MInvoiceSchedule invoiceSchedule = getInvoiceSchedule(ctx);
		if (invoiceSchedule != null)
			businessPartner.setC_InvoiceSchedule_ID(invoiceSchedule.getC_InvoiceSchedule_ID());
		else
			log.warning("Failed to set MInvoiceSchedule for BPartner[" + searchKey + "]");
		
		if (!businessPartner.save())
			return getErrorStandardResponse("Failed to save Business Partner", trxName);
		
		return getStandardResponse(true, "Business Partner has been created for " + name, trxName, businessPartner.getC_BPartner_ID());
	}
	
	public StandardResponse readBusinessPartner(ReadBusinessPartnerRequest readBusinessPartnerRequest)
	{
		return getErrorStandardResponse("readBusinessPartner() hasn't been implemented yet", null);
	}
	
	public StandardResponse updateBusinessPartner(UpdateBusinessPartnerRequest updateBusinessPartnerRequest)
	{
		return getErrorStandardResponse("updateBusinessPartner() hasn't been implemented yet", null);
	}
	
	public StandardResponse deleteBusinessPartner(DeleteBusinessPartnerRequest deleteBusinessPartnerRequest)
	{
		return getErrorStandardResponse("deleteBusinessPartner() hasn't been implemented yet", null);
	}
	
	public ReadBusinessPartnersResponse readBusinessPartnersByGroup(ReadBusinessPartnersByGroupRequest readBusinessPartnersByGroupRequest)
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadBusinessPartnersResponse readBusinessPartnersResponse = objectFactory.createReadBusinessPartnersResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readBusinessPartnersByGroupRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("READ_BUSINESS_PARTNERS_BY_GROUP_METHOD_ID"), readBusinessPartnersByGroupRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readBusinessPartnersResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readBusinessPartnersResponse;
		}

		// Load and validate parameters
		Integer businessPartnerGroupId = readBusinessPartnersByGroupRequest.getBusinessPartnerGroupId();
		if (businessPartnerGroupId == null || businessPartnerGroupId < 1 || !Validation.validateADId(MBPGroup.Table_Name, businessPartnerGroupId, trxName))
		{
			readBusinessPartnersResponse.setStandardResponse(getErrorStandardResponse("Invalid businessPartnerGroupId", trxName));
			return readBusinessPartnersResponse;
		}

		// Get all business partners belonging to group
		ArrayList<MBPartner> businessPartners = MBPartnerEx.getByBPGroup(ctx, businessPartnerGroupId, trxName);
		
		// Create response elements
		ArrayList<BusinessPartner> xmlBusinessPartners = new ArrayList<BusinessPartner>();		
		for (MBPartner businessPartner : businessPartners)
		{
			BusinessPartner xmlBusinessPartner = objectFactory.createBusinessPartner();
			xmlBusinessPartner.setBusinessPartnerId(businessPartner.getC_BPartner_ID());
			xmlBusinessPartner.setSearchKey(businessPartner.getValue());
			xmlBusinessPartner.setName(businessPartner.getName());
			
			xmlBusinessPartners.add(xmlBusinessPartner);
		}
		
		// Set response elements
		readBusinessPartnersResponse.businessPartners = xmlBusinessPartners;		
		readBusinessPartnersResponse.setStandardResponse(getStandardResponse(true, "Business Partners have been read for MBPGroup[" + businessPartnerGroupId + "]", trxName, xmlBusinessPartners.size()));
		
		return readBusinessPartnersResponse;
	}
	
	public StandardResponse createBusinessPartnerLocation(CreateBusinessPartnerLocationRequest createBusinessPartnerLocationRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(createBusinessPartnerLocationRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("CREATE_BUSINESS_PARTNER_LOCATION_METHOD_ID"), createBusinessPartnerLocationRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);
		
		// Load and validate parameters
		String name = createBusinessPartnerLocationRequest.getName();
		if (!validateString(name))
			return getErrorStandardResponse("Invalid name", trxName);
		else
			name = name.trim();
		
		Integer businessPartnerId = createBusinessPartnerLocationRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		Integer locationId = createBusinessPartnerLocationRequest.getLocationId();
		if (locationId == null || locationId < 1 || !Validation.validateADId(MLocation.Table_Name, locationId, trxName))
			return getErrorStandardResponse("Invalid locationId", trxName);
		
		MBPartner businessPartner = MBPartnerEx.get(ctx, businessPartnerId, trxName);
		if (businessPartner == null)
			return getErrorStandardResponse("Failed to load Business Parter", trxName);
		
		MBPartnerLocation businessPartnerLocation = new MBPartnerLocation(businessPartner);
		businessPartnerLocation.setName(name);
		businessPartnerLocation.setC_Location_ID(locationId);
		
		if (!businessPartnerLocation.save())
			return getErrorStandardResponse("Failed to save Business Partner Location", trxName);
			
		return getStandardResponse(true, "Business Partner Location has been created for " + name, trxName, businessPartnerLocation.getC_BPartner_Location_ID());
	}
	
	public StandardResponse createLocation(CreateLocationRequest createLocationRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(createLocationRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("CREATE_LOCATION_METHOD_ID"), createLocationRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);
		
		// Load and validate parameters
		String address1 = createLocationRequest.getAddress1(); // Mandatory
		if (!validateString(address1))
			return getErrorStandardResponse("Invalid address1", trxName);
		else
			address1 = address1.trim();
		
		String address2 = createLocationRequest.getAddress2();
		if (!validateString(address2))
			address2 = null;
		else 
			address2 = address2.trim();
		
		String address3 = createLocationRequest.getAddress3();
		if (!validateString(address3))
			address3 = null;
		else 
			address3 = address3.trim();
		
		String address4 = createLocationRequest.getAddress4();
		if (!validateString(address4))
			address4 = null;
		else 
			address4 = address4.trim();
		
		String city = createLocationRequest.getCity(); // Mandatory
		if (!validateString(city))
			return getErrorStandardResponse("Invalid city", trxName);
		else
			city = city.trim();
		
		Integer cityId = createLocationRequest.getCityId();
		if (cityId != null && cityId > 0 && !Validation.validateADId(X_C_City.Table_Name, cityId, trxName))
			return getErrorStandardResponse("Invalid cityId", trxName);
		
		String zip = createLocationRequest.getZip();
		if (zip != null && zip.trim().length() < 1)
			zip = null;
		else if (zip != null && zip.trim().length() > 0)
			zip = zip.trim();		
		
		String region = createLocationRequest.getRegion();
		if (!validateString(region))
			region = null;
		else 
			region = region.trim();
		
		Integer regionId = createLocationRequest.getRegionId();
		if (regionId != null && regionId > 0 && !Validation.validateADId(MRegion.Table_Name, regionId, trxName))
			return getErrorStandardResponse("Invalid regionId", trxName);
		
		Integer countryId = createLocationRequest.getCountryId(); // Mandatory
		if (countryId == null || countryId < 1 || !Validation.validateADId(MCountry.Table_Name, countryId, trxName))
			return getErrorStandardResponse("Invalid countryId", trxName);
		
		MLocation location = new MLocation(ctx, 0, trxName);
		location.setAddress1(address1); // Mandatory
		
		if (address2 != null)
			location.setAddress2(address2);
		
		if (address3 != null)
			location.setAddress3(address3);
		
		if (address4 != null)
			location.setAddress4(address4);
		
		location.setCity(city); // Mandatory
		
		if (cityId != null && cityId > 0)
			location.setC_City_ID(cityId);
		
		if (zip != null)
			location.setPostal(zip);
		
		if (region != null)
			location.setRegionName(region);
		
		if (regionId != null && regionId > 0)
			location.setC_Region_ID(regionId);
		
		location.setC_Country_ID(countryId); // Mandatory
		
		if (!location.save())
			return getErrorStandardResponse("Failed to save Location", trxName);
			
		return getStandardResponse(true, "Location has been created for " + city, trxName, location.getC_Location_ID());
	}
	
	public StandardResponse createUser(CreateUserRequest createUserRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(createUserRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("CREATE_USER_METHOD_ID"), createUserRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);
		
		String name = createUserRequest.getName();
		if (!validateString(name))
			return getErrorStandardResponse("Invalid name", trxName);
		else
			name = name.trim();
		
		String password = createUserRequest.getPassword();
		if (!validateString(password))
			return getErrorStandardResponse("Invalid password", trxName);
		else
			password = password.trim();
		
		String email = createUserRequest.getEmail();
		if (!validateString(email) || !WebServiceUtil.isEmailValid(email))
			return getErrorStandardResponse("Invalid email", trxName);
		else
			email = email.trim();
		
		Integer businessPartnerId = createUserRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		Integer businessPartnerLocationId = createUserRequest.getBusinessPartnerLocationId();
		if (businessPartnerLocationId == null || businessPartnerLocationId < 1 || !Validation.validateADId(MBPartnerLocation.Table_Name, businessPartnerLocationId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerLocationId", trxName);
		
		MUser user = new MUser(ctx, 0, trxName);
		user.setName(name);
		user.setEMail(email);
		user.setPassword(password);
		user.setC_BPartner_ID(businessPartnerId);
		user.setC_BPartner_Location_ID(businessPartnerLocationId);
		
		if (!user.save())
			return getErrorStandardResponse("Failed to save User", trxName);
		
		return getStandardResponse(true, "User has been created for " + name, trxName, user.getAD_User_ID());
	}
	
	public StandardResponse createSubscription(CreateSubscriptionRequest createSubscriptionRequest)
	{
		return getErrorStandardResponse("createSubscription() hasn't been implemented yet", null);
	}
	
	public StandardResponse readSubscription(ReadSubscriptionRequest readSubscriptionRequest)
	{
		return getErrorStandardResponse("readSubscription() hasn't been implemented yet", null);
	}
	
	public StandardResponse updateSubscription(UpdateSubscriptionRequest updateSubscriptionRequest)
	{
		return getErrorStandardResponse("updateSubscription() hasn't been implemented yet", null);
	}
	
	public StandardResponse deleteSubscription(DeleteSubscriptionRequest deleteSubscriptionRequest)
	{
		return getErrorStandardResponse("deleteSubscription() hasn't been implemented yet", null);
	}
	
	public ReadSubscriptionsResponse readSubscriptions(ReadSubscriptionsRequest readSubscriptionsRequest)
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadSubscriptionsResponse readSubscriptionsResponse = objectFactory.createReadSubscriptionsResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readSubscriptionsRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("READ_SUBSCRIPTIONS_METHOD_ID"), readSubscriptionsRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readSubscriptionsResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readSubscriptionsResponse;
		}

		// Load and validate parameters
		Integer businessPartnerId = readSubscriptionsRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
		{
			readSubscriptionsResponse.setStandardResponse(getErrorStandardResponse("Invalid businessPartnerId", trxName));
			return readSubscriptionsResponse;
		}

		// Get all subscriptions belonging to business partner
		MSubscription[] subscriptions = MSubscription.getSubscriptions(ctx, null, businessPartnerId, trxName);
		
		// Create response elements
		ArrayList<Subscription> xmlSubscriptions = new ArrayList<Subscription>();		
		for (MSubscription subscription : subscriptions)
		{
			Subscription xmlSubscription = objectFactory.createSubscription();
			xmlSubscription.setSubscriptionId(subscription.getC_Subscription_ID());
			xmlSubscription.setName(subscription.getName());
			
			xmlSubscriptions.add(xmlSubscription);
		}
		
		// Set response elements
		readSubscriptionsResponse.subscriptions = xmlSubscriptions;		
		readSubscriptionsResponse.setStandardResponse(getStandardResponse(true, "Subscriptions have been read for MBPartner[" + businessPartnerId + "]", trxName, xmlSubscriptions.size()));
		
		return readSubscriptionsResponse;
	}
	
	public StandardResponse createOrder(CreateOrderRequest createOrderRequest)
	{
		return getErrorStandardResponse("createOrder() hasn't been implemented yet", null);
	}
	
	public ReadOrderResponse readOrder(ReadOrderRequest readOrderRequest)
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadOrderResponse readOrderResponse = objectFactory.createReadOrderResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readOrderRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("READ_ORDER_METHOD_ID"), readOrderRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readOrderResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readOrderResponse;
		}

		// Load and validate parameters
		String documentNo = readOrderRequest.getDocumentNo();
		if (!validateString(documentNo))
		{
			readOrderResponse.setStandardResponse(getErrorStandardResponse("Invalid documentNo", trxName));
			return readOrderResponse;
		}
		else
			documentNo = documentNo.trim();
		
		MOrder order = MOrderEx.getOrder(ctx, documentNo, trxName);
		if (order == null)
		{
			readOrderResponse.setStandardResponse(getErrorStandardResponse("Failed to read Order for DocumentNo[" + documentNo + "]", trxName));
			return readOrderResponse;
		}	
		
		// Create XML order
		Order xmlOrder = objectFactory.createOrder();
		xmlOrder.setOrderId(order.getC_Order_ID());
		xmlOrder.setDocumentNo(order.getDocumentNo());
		xmlOrder.setBusinessPartnerId(order.getC_BPartner_ID());
		xmlOrder.setBusinessPartnerLocationId(order.getC_BPartner_Location_ID());
		
		// Set success response
		readOrderResponse.setOrder(xmlOrder);
		readOrderResponse.setStandardResponse(getStandardResponse(true, "Order has been read for DocumentNo[" + documentNo + "]", trxName, 1));
		
		return readOrderResponse;
	}
	
	public StandardResponse updateOrder(UpdateOrderRequest updateOrderRequest)
	{
		return getErrorStandardResponse("updateOrder() hasn't been implemented yet", null);
	}
	
	public StandardResponse deleteOrder(DeleteOrderRequest deleteOrderRequest)
	{
		return getErrorStandardResponse("deleteOrder() hasn't been implemented yet", null);
	}
	
	public ReadOrderDIDsResponse readOrderDIDs(ReadOrderDIDsRequest readOrderDIDsRequest)
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadOrderDIDsResponse readOrderDIDsResponse = objectFactory.createReadOrderDIDsResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readOrderDIDsRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("READ_ORDER_DIDS_METHOD_ID"), readOrderDIDsRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readOrderDIDsResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readOrderDIDsResponse;
		}

		// Load and validate parameters
		Integer orderId = readOrderDIDsRequest.getOrderId();
		if (orderId == null || orderId < 1 || !Validation.validateADId(MOrder.Table_Name, orderId, trxName))
		{
			readOrderDIDsResponse.setStandardResponse(getErrorStandardResponse("Invalid orderId", trxName));
			return readOrderDIDsResponse;
		}

		// Get order
		MOrder order = new MOrder(ctx, orderId, trxName);
		if (order == null)
		{
			readOrderDIDsResponse.setStandardResponse(getErrorStandardResponse("Failed to read Order[" + orderId + "]", trxName));
			return readOrderDIDsResponse;
		}
		
		// Check if order complete
		if (!order.isComplete())
		{
			readOrderDIDsResponse.setStandardResponse(getErrorStandardResponse("Order is not complete Order[" + orderId + "]", trxName));
			return readOrderDIDsResponse;
		}
		
		// Create response elements
		ArrayList<String> dids = DIDUtil.getNumbersFromOrder(ctx, order, false, trxName);
		
		// Set response elements
		readOrderDIDsResponse.dids = dids;		
		readOrderDIDsResponse.setStandardResponse(getStandardResponse(true, "DIDs have been read from MOrder[" + orderId + "]", trxName, dids.size()));
		
		return readOrderDIDsResponse;
	}
	
	private MInvoiceSchedule getInvoiceSchedule(Properties ctx)
	{
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		int dayOfMonth = cal.get(GregorianCalendar.DAY_OF_MONTH);
		
		MInvoiceSchedule invoiceSchedule = MInvoiceSchedule.getByInvoiceDay(ctx, dayOfMonth, null);
		if (invoiceSchedule == null)
			log.severe("Failed to load MInvoiceSchedule for DAY_OF_MONTH[" + dayOfMonth + "]");
		
		return invoiceSchedule;
	}	
}
