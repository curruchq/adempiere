package com.conversant.webservice;

import java.io.File;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import javax.jws.WebService;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.compiere.model.MBPGroup;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerEx;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MCountry;
import org.compiere.model.MInvoiceSchedule;
import org.compiere.model.MLocation;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderEx;
import org.compiere.model.MProduct;
import org.compiere.model.MRegion;
import org.compiere.model.MRole;
import org.compiere.model.MSubscription;
import org.compiere.model.X_C_SubscriptionType;
import org.compiere.model.MUser;
import org.compiere.model.MUserEx;
import org.compiere.model.MUserRoles;
import org.compiere.model.X_C_City;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.conversant.db.BillingConnector;
import com.conversant.db.RadiusConnector;
import com.conversant.did.DIDUtil;
import com.conversant.model.BillingRecord;
import com.conversant.util.TwoTalkConnector;
import com.conversant.util.Validation;
import com.conversant.webservice.util.WebServiceConstants;
import com.conversant.webservice.util.WebServiceUtil;
import com.conversant.db.AsteriskConnector;

@WebService(endpointInterface = "com.conversant.webservice.Admin")
public class AdminImpl extends GenericWebServiceImpl implements Admin
{
	//private final static String CALL_RECORDING_DIR = "/opt/drupal/drupal-current/sites/c.conversant.co.nz/files/callrecordings/"; // C:\\Program Files\\xampp\\htdocs\\drupal-6.19-v2\\sites\\default\\files\\callrecordings\\
	private final static String CALL_RECORDING_DIR = "/opt/drupal/drupal-current/sites/default/files/callrecordings/";
	private final static String CALL_RECORDING_URL = "https://c.conversant.co.nz/sites/c.conversant.co.nz/files/callrecordings/"; // http://c.localhost/sites/c.conversant.co.nz/files/callrecordings/
	
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
		
		boolean taxExempt = createBusinessPartnerRequest.isTaxExempt();
		
		Integer businessPartnerGroupId = createBusinessPartnerRequest.getBusinessPartnerGroupId();
		if (businessPartnerGroupId == null || businessPartnerGroupId < 1 || !Validation.validateADId(MBPGroup.Table_Name, businessPartnerGroupId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerGroupId", trxName);

		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put(MBPartner.COLUMNNAME_Name, name);
		fields.put(MBPartner.COLUMNNAME_IsTaxExempt, taxExempt);
		fields.put(MBPartner.COLUMNNAME_C_BP_Group_ID, businessPartnerGroupId);
		
		if (searchKey != null)
			fields.put(MBPartner.COLUMNNAME_Value, searchKey);

		MBPartner businessPartner = new MBPartner(ctx, 0, trxName);
		if (!Validation.validateMandatoryFields(businessPartner, fields))
			return getErrorStandardResponse("Missing mandatory fields", trxName);

		if (fields.get(MBPartner.COLUMNNAME_Value) != null)
			businessPartner.setValue((String)fields.get(MBPartner.COLUMNNAME_Value));
		
		businessPartner.setName((String)fields.get(MBPartner.COLUMNNAME_Name));
		businessPartner.setIsTaxExempt((Boolean)fields.get(MBPartner.COLUMNNAME_IsTaxExempt));
		businessPartner.setBPGroup(MBPGroup.get(ctx, (Integer)fields.get(MBPartner.COLUMNNAME_C_BP_Group_ID)));
		Timestamp ts=new Timestamp(System.currentTimeMillis());
		Calendar cal=Calendar.getInstance();
		cal.setTime(ts);
		cal.add(Calendar.MONTH, 1);
		ts = new Timestamp(cal.getTime().getTime());
		businessPartner.setBillingStartDate(ts);
		// Set invoice schedule
		MInvoiceSchedule invoiceSchedule = getInvoiceSchedule(ctx);
		if (invoiceSchedule != null)
			businessPartner.setC_InvoiceSchedule_ID(invoiceSchedule.getC_InvoiceSchedule_ID());
		else
			log.warning("Failed to set MInvoiceSchedule for BPartner[" + searchKey + "]");
		
		if (!businessPartner.save())
			return getErrorStandardResponse("Failed to save Business Partner", trxName);
		AsteriskConnector.addAvp("CALLTRACE/"+businessPartner.getValue(), "");
		return getStandardResponse(true, "Business Partner has been created for " + name, trxName, businessPartner.getC_BPartner_ID());
	}
	
	public ReadBusinessPartnerResponse readBusinessPartner(ReadBusinessPartnerRequest readBusinessPartnerRequest)
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadBusinessPartnerResponse readBusinessPartnerResponse = objectFactory.createReadBusinessPartnerResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readBusinessPartnerRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("READ_BUSINESS_PARTNER_METHOD_ID"), readBusinessPartnerRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readBusinessPartnerResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readBusinessPartnerResponse;
		}

		// Load and validate parameters
		Integer businessPartnerId = readBusinessPartnerRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
		{
			readBusinessPartnerResponse.setStandardResponse(getErrorStandardResponse("Invalid businessPartnerId", trxName));
			return readBusinessPartnerResponse;
		}

		// Get all business partners belonging to group
		MBPartner businessPartner = new MBPartner(ctx, businessPartnerId, trxName);
		
		// Create response element
		BusinessPartner xmlBusinessPartner = objectFactory.createBusinessPartner();
		xmlBusinessPartner.setBusinessPartnerId(businessPartner.getC_BPartner_ID());
		xmlBusinessPartner.setSearchKey(businessPartner.getValue());
		xmlBusinessPartner.setName(businessPartner.getName());

		// Set response elements
		readBusinessPartnerResponse.setBusinessPartner(xmlBusinessPartner);	
		readBusinessPartnerResponse.setStandardResponse(getStandardResponse(true, "Business Partner has been read for MBPartner[" + businessPartnerId + "]", trxName, businessPartnerId));
		
		return readBusinessPartnerResponse;
	}
	
	public ReadBusinessPartnerResponse readBusinessPartnerBySearchKey(ReadBusinessPartnerBySearchKeyRequest readBusinessPartnerBySearchKeyRequest)
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadBusinessPartnerResponse readBusinessPartnerResponse = objectFactory.createReadBusinessPartnerResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readBusinessPartnerBySearchKeyRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("READ_BUSINESS_PARTNER_BY_SEARCHKEY_METHOD_ID"), readBusinessPartnerBySearchKeyRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readBusinessPartnerResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readBusinessPartnerResponse;
		}

		// Load and validate parameters
		String value = readBusinessPartnerBySearchKeyRequest.getSearchKey();
		if (!validateString(value))
		{
			readBusinessPartnerResponse.setStandardResponse(getErrorStandardResponse("Invalid search key value", trxName));
			return readBusinessPartnerResponse;
		}

		// Get all business partners belonging to group
		MBPartner businessPartner = MBPartnerEx.getBySearchKey(ctx, value, trxName);
		
		// Create response element
		BusinessPartner xmlBusinessPartner = objectFactory.createBusinessPartner();
		xmlBusinessPartner.setBusinessPartnerId(businessPartner.getC_BPartner_ID());
		xmlBusinessPartner.setSearchKey(businessPartner.getValue());
		xmlBusinessPartner.setName(businessPartner.getName());

		// Set response elements
		readBusinessPartnerResponse.setBusinessPartner(xmlBusinessPartner);	
		readBusinessPartnerResponse.setStandardResponse(getStandardResponse(true, "Business Partner has been read for MBPartner[" + value + "]", trxName, businessPartner.getC_BPartner_ID()));
		
		return readBusinessPartnerResponse;
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
		readBusinessPartnersResponse.businessPartner = xmlBusinessPartners;		
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
	
	public StandardResponse updateBusinessPartnerLocation(UpdateBusinessPartnerLocationRequest updateBusinessPartnerLocationRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(updateBusinessPartnerLocationRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("UPDATE_BUSINESS_PARTNER_LOCATION_METHOD_ID"), updateBusinessPartnerLocationRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);
		
		// Load and validate parameters
		String name = updateBusinessPartnerLocationRequest.getName();
		if (!validateString(name))
			return getErrorStandardResponse("Invalid name", trxName);
		else
			name = name.trim();
		
		String address1 = updateBusinessPartnerLocationRequest.getAddress1(); // Mandatory
		if (!validateString(address1))
			return getErrorStandardResponse("Invalid address1", trxName);
		else
			address1 = address1.trim();
		
		String address2 = updateBusinessPartnerLocationRequest.getAddress2();
		if (!validateString(address2))
			address2 = null;
		else 
			address2 = address2.trim();
		
		String address3 = updateBusinessPartnerLocationRequest.getAddress3();
		if (!validateString(address3))
			address3 = null;
		else 
			address3 = address3.trim();
		
		String address4 = updateBusinessPartnerLocationRequest.getAddress4();
		if (!validateString(address4))
			address4 = null;
		else 
			address4 = address4.trim();
		
		String city = updateBusinessPartnerLocationRequest.getCity(); // Mandatory
		if (!validateString(city))
			return getErrorStandardResponse("Invalid city", trxName);
		else
			city = city.trim();
		
		Integer cityId = updateBusinessPartnerLocationRequest.getCityId();
		if (cityId != null && cityId > 0 && !Validation.validateADId(X_C_City.Table_Name, cityId, trxName))
			return getErrorStandardResponse("Invalid cityId", trxName);
		
		String zip = updateBusinessPartnerLocationRequest.getZip();
		if (zip != null && zip.trim().length() < 1)
			zip = null;
		else if (zip != null && zip.trim().length() > 0)
			zip = zip.trim();		
		
		String region = updateBusinessPartnerLocationRequest.getRegion();
		if (!validateString(region))
			region = null;
		else 
			region = region.trim();
		
		Integer regionId = updateBusinessPartnerLocationRequest.getRegionId();
		if (regionId != null && regionId > 0 && !Validation.validateADId(MRegion.Table_Name, regionId, trxName))
			return getErrorStandardResponse("Invalid regionId", trxName);
		
		Integer countryId = updateBusinessPartnerLocationRequest.getCountryId(); // Mandatory
		if (countryId == null || countryId < 1 || !Validation.validateADId(MCountry.Table_Name, countryId, trxName))
			return getErrorStandardResponse("Invalid countryId", trxName);
		
		// Load Business Partner
		ArrayList<MBPartner> businessPartners = MBPartnerEx.getByName(ctx, name); // TODO: Loop through each bp using their name to see if it only loads them OR more?
		if (businessPartners.size() != 1)
			return getErrorStandardResponse("Loaded " + businessPartners.size() + " business partner(s) using the name '" + name + "'", trxName);

		MBPartner businessPartner = businessPartners.get(0);
		MBPartnerLocation businessPartnerLocation = businessPartner.getPrimaryC_BPartner_Location();
		MLocation location = businessPartnerLocation.getLocation(true);
		
		return null;
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
		
		// Load and validate parameters
		String searchKey = createUserRequest.getSearchKey();
		if (!validateString(searchKey)) 
			searchKey = null; // Allow ADempiere auto sequencing to set Search Key
		else
			searchKey = searchKey.trim();
		
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
		
		String phone = createUserRequest.getPhone();
		if (!validateString(phone))
			phone = null;
		else
			phone = phone.trim();
		
		String mobile = createUserRequest.getMobile();
		if (!validateString(mobile))
			mobile = null;
		else
			mobile = mobile.trim();
		
		Integer businessPartnerId = createUserRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		Integer businessPartnerLocationId = createUserRequest.getBusinessPartnerLocationId();
		if (businessPartnerLocationId == null || businessPartnerLocationId < 1)
		{
			// Set to first BP Location it finds
			MBPartnerLocation[] locations = MBPartnerLocation.getForBPartner(ctx, businessPartnerId);
			if (locations.length > 0)
				businessPartnerLocationId = locations[0].getC_BPartner_Location_ID();
			else
				businessPartnerLocationId = null;
		}
		else if (!Validation.validateADId(MBPartnerLocation.Table_Name, businessPartnerLocationId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerLocationId", trxName);
		
		MUser user = new MUser(ctx, 0, trxName);
		 
		if (searchKey != null)
			user.setValue(searchKey);
		
		user.setName(name);
		user.setEMail(email);
		
		if (phone != null)
			user.setPhone(phone);
		
		if (mobile != null)
			user.setPhone2(mobile);
		
		user.setPassword(password);
		user.setC_BPartner_ID(businessPartnerId);
		
		if (businessPartnerLocationId != null)
			user.setC_BPartner_Location_ID(businessPartnerLocationId);
		
		if (!user.save())
			return getErrorStandardResponse("Failed to save User", trxName);
		
		return getStandardResponse(true, "User has been created for " + name, trxName, user.getAD_User_ID());
	}
	
	public ReadUserResponse readUser(ReadUserRequest readUserRequest)
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadUserResponse readUserResponse = objectFactory.createReadUserResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readUserRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("READ_USER_METHOD_ID"), readUserRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readUserResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readUserResponse;
		}

		// Load and validate parameters
		Integer userId = readUserRequest.getUserId();
		if (userId == null || userId < 1 || !Validation.validateADId(MUser.Table_Name, userId, trxName))
		{
			readUserResponse.setStandardResponse(getErrorStandardResponse("Invalid userId", trxName));
			return readUserResponse;
		}
		
		// Get User
		MUser user = MUserEx.getIgnoreCache(ctx, userId);
		
		// Create response user element
		User xmlUser = objectFactory.createUser();
		xmlUser.setUserId(user.getAD_User_ID());
		xmlUser.setName(user.getName());
		xmlUser.setEmail(user.getEMail() != null ? user.getEMail() : "");
		xmlUser.setPhone(user.getPhone() != null ? user.getPhone() : "");
		xmlUser.setMobile(user.getPhone2() != null ? user.getPhone2() : "");
		xmlUser.setBusinessPartnerId(user.getC_BPartner_ID());
		
		ArrayList<Role> xmlRoles = new ArrayList<Role>();
		MUserRoles[] userRoles = MUserRoles.getOfUser(ctx, user.getAD_User_ID());
		for (MUserRoles userRole : userRoles)
		{
			Role xmlRole = objectFactory.createRole();
			xmlRole.setRoleId(userRole.getAD_Role_ID());
			try
			{
				xmlRole.setName(userRole.getAD_Role().getName());
			}
			catch(Exception ex)
			{
				log.severe("Failed to load MUserRoles from MUser[" + userRole.getAD_User_ID() + "] and MRole[" + userRole.getAD_Role_ID() + "]");
			}
			
			xmlRoles.add(xmlRole);
		}
		
		xmlUser.role = xmlRoles;
		
		// Set response elements
		readUserResponse.user = xmlUser;		
		readUserResponse.setStandardResponse(getStandardResponse(true, "User has been read for MUser[" + userId + "]", trxName, user.getAD_User_ID()));
		
		return readUserResponse;
	}
	
	public StandardResponse updateUser(UpdateUserRequest updateUserRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(updateUserRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("UPDATE_USER_METHOD_ID"), updateUserRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);
		
		// Load and validate parameters
		Integer userId = updateUserRequest.getUserId();
		if (userId == null || userId < 1 || !Validation.validateADId(MUser.Table_Name, userId, trxName))
			return getErrorStandardResponse("Invalid userId", trxName);
		
		String searchKey = updateUserRequest.getSearchKey();
		if (!validateString(searchKey))
			searchKey = null;
		else
			searchKey = searchKey.trim();
		
		String name = updateUserRequest.getName();
		if (!validateString(name))
			name = null;
		else
			name = name.trim();
		
		String password = updateUserRequest.getPassword();
		if (!validateString(password))
			password = null;
		else
			password = password.trim();
		
		String email = updateUserRequest.getEmail();
		if (!validateString(email))
			email = null;
		else if (!WebServiceUtil.isEmailValid(email))
			return getErrorStandardResponse("Invalid email", trxName);
		else
			email = email.trim();
		
		String phone = updateUserRequest.getPhone();
		if (!validateString(phone))
			phone = null;
		else if (phone.equals("<empty>"))
			phone = "";
		else
			phone = phone.trim();
		
		String mobile = updateUserRequest.getMobile();
		if (!validateString(mobile))
			mobile = null;
		else if (mobile.equals("<empty>"))
			mobile = "";
		else
			mobile = mobile.trim();
		
		// Update required?
		if (searchKey == null && name == null && password == null && email == null && phone == null && mobile == null)
			return getStandardResponse(true, "Nothing to update for User " + userId, trxName, userId);
		
		// Load user and update
		MUser user = MUser.get(ctx, userId);
		
		if (searchKey != null)
			user.setValue(searchKey);
		
		if (name != null)
			user.setName(name);
		
		if (email != null)
			user.setEMail(email);
		
		if (password != null)
			user.setPassword(password);
		
		if (phone != null)
			user.setPhone(phone);
		
		if (mobile != null)
			user.setPhone2(mobile);
		
		if (!user.save())
			return getErrorStandardResponse("Failed to save User " + name, trxName);
		
		return getStandardResponse(true, "User " + userId + " has been updated", trxName, userId);
	}
	
	public StandardResponse deleteUser(DeleteUserRequest deleteUserRequest)
	{
		return getErrorStandardResponse("deleteUser() hasn't been implemented yet", null);
	}
	
	public StandardResponse createSubscription(CreateSubscriptionRequest createSubscriptionRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(createSubscriptionRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("CREATE_SUBSCRIPTION_METHOD_ID"), createSubscriptionRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);
		
		String name=createSubscriptionRequest.getName();
		
		if (!validateString(name))
			return getErrorStandardResponse("Invalid subscription name", trxName);
		else
			name = name.trim();
		
		Integer subscriptionTypeId = createSubscriptionRequest.getSubscriptionTypeId();
		if(subscriptionTypeId==null || subscriptionTypeId < 1 || !Validation.validateADId(X_C_SubscriptionType.Table_Name, subscriptionTypeId, trxName))
		return getErrorStandardResponse("Invalid Subscription Type Id",trxName);
		
		Integer businessPartnerId = createSubscriptionRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		Integer businessPartnerLocationId=createSubscriptionRequest.getBusinessPartnerLocationId();
		if (businessPartnerLocationId == null || businessPartnerLocationId < 1 || !Validation.validateADId(MBPartnerLocation.Table_Name, businessPartnerLocationId, trxName))
			return getErrorStandardResponse("Invalid businessPartner Location Id", trxName);
		
		Integer productId=createSubscriptionRequest.getProductId();
		if(productId==null || productId < 1 || !Validation.validateADId(MProduct.Table_Name, productId, trxName))
			return getErrorStandardResponse("Invalid Product Id",trxName);
		
		Integer userId=createSubscriptionRequest.getUserId();
		if(!Validation.validateADId(MUser.Table_Name, userId, trxName))
			return getErrorStandardResponse("Invalid User Id",trxName);
		
		Timestamp startDate=new Timestamp(createSubscriptionRequest.getStartDate().toGregorianCalendar().getTimeInMillis());
		if(startDate==null)
			return getErrorStandardResponse("Invalid Start Date",trxName);
		
		Timestamp paidUntilDate=new Timestamp(createSubscriptionRequest.getPaidUntilDate().toGregorianCalendar().getTimeInMillis());
		if(paidUntilDate==null)
			return getErrorStandardResponse("Invalid Paid Until Date",trxName);
		
		Timestamp renewalDate=new Timestamp(createSubscriptionRequest.getRenewalDate().toGregorianCalendar().getTimeInMillis());
		if(renewalDate==null)
			return getErrorStandardResponse("Invalid Renewal Date",trxName);
		
		Boolean billInAdvance=createSubscriptionRequest.isBillInAdvance();
		Boolean isDue=createSubscriptionRequest.isIsDue();
		BigDecimal qty=createSubscriptionRequest.getQty();

		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put(MSubscription.COLUMNNAME_Name, name);
		fields.put(MSubscription.COLUMNNAME_C_SubscriptionType_ID, subscriptionTypeId);
		fields.put(MSubscription.COLUMNNAME_C_BPartner_ID, businessPartnerId);
		//fields.put(MSubscription, value);
		fields.put(MSubscription.COLUMNNAME_M_Product_ID, productId);
		fields.put(MSubscription.COLUMNNAME_PaidUntilDate,paidUntilDate);
		fields.put(MSubscription.COLUMNNAME_StartDate,startDate);
		fields.put(MSubscription.COLUMNNAME_RenewalDate, renewalDate);
		if(billInAdvance !=null)
			fields.put(MSubscription.COLUMNNAME_BillInAdvance, billInAdvance);
		if(isDue!=null)
			fields.put(MSubscription.COLUMNNAME_IsDue,isDue);
		fields.put(MSubscription.COLUMNNAME_Qty, qty);
		if(userId!=null && userId >0)
			fields.put(MSubscription.COLUMNNAME_AD_User_ID,userId);

		MSubscription subscription= new MSubscription(ctx, 0, trxName);
		if (!Validation.validateMandatoryFields(subscription, fields))
			return getErrorStandardResponse("Missing mandatory fields", trxName);
	    
		subscription.setName((String)fields.get(MSubscription.COLUMNNAME_Name));
		subscription.setC_SubscriptionType_ID((Integer)fields.get(MSubscription.COLUMNNAME_C_SubscriptionType_ID));
		subscription.setC_BPartner_ID((Integer)fields.get(MSubscription.COLUMNNAME_C_BPartner_ID));
		subscription.setC_BPartner_Location_ID(businessPartnerLocationId);
		subscription.setM_Product_ID((Integer)fields.get(MSubscription.COLUMNNAME_M_Product_ID));
		subscription.setStartDate((Timestamp)fields.get(MSubscription.COLUMNNAME_StartDate));
		subscription.setPaidUntilDate((Timestamp)fields.get(MSubscription.COLUMNNAME_PaidUntilDate));
		subscription.setRenewalDate((Timestamp)fields.get(MSubscription.COLUMNNAME_RenewalDate));
		subscription.setBillInAdvance((Boolean)fields.get(MSubscription.COLUMNNAME_BillInAdvance));
		subscription.setIsDue((Boolean)fields.get(MSubscription.COLUMNNAME_IsDue));
		subscription.setQty((BigDecimal)fields.get(MSubscription.COLUMNNAME_Qty));
		subscription.setAD_User_ID((Integer)fields.get(MSubscription.COLUMNNAME_AD_User_ID));
		
		if (!subscription.save())
			return getErrorStandardResponse("Failed to save Subscription", trxName);
		
		return getStandardResponse(true, "subscription has been created for " + name, trxName, subscription.getC_Subscription_ID());

	}
	
	public ReadSubscriptionResponse readSubscription(ReadSubscriptionRequest readSubscriptionRequest)
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadSubscriptionResponse readSubscriptionResponse = objectFactory.createReadSubscriptionResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readSubscriptionRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("READ_SUBSCRIPTION_METHOD_ID"), readSubscriptionRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readSubscriptionResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readSubscriptionResponse;
		}

		// Load and validate parameters
		Integer subscriptionId = readSubscriptionRequest.getSubscriptionId();
		if (subscriptionId == null || subscriptionId < 1 || !Validation.validateADId(MSubscription.Table_Name, subscriptionId, trxName))
		{
			readSubscriptionResponse.setStandardResponse(getErrorStandardResponse("Invalid subscription Id", trxName));
			return readSubscriptionResponse;
		}
		
		// Get Subscription
		MSubscription subscription =new MSubscription(ctx, subscriptionId,trxName);
		
		// Create response user element
		Subscription xmlSubscription = objectFactory.createSubscription();
		xmlSubscription.setSubscriptionId(subscription.getC_Subscription_ID());
		xmlSubscription.setName(subscription.getName());
		xmlSubscription.setSubscriptionTypeId(subscription.getC_SubscriptionType_ID());
		xmlSubscription.setBusinessPartnerId(subscription.getC_BPartner_ID());
		xmlSubscription.setBusinessPartnerLocationId(subscription.getC_BPartner_Location_ID());
		xmlSubscription.setProductId(subscription.getM_Product_ID());
		xmlSubscription.setQty(subscription.getQty().intValue());
		xmlSubscription.setBillInAdvance(subscription.isBillInAdvance());
		xmlSubscription.setUserId(subscription.getAD_User_ID());
		
		if (subscription.getQty() != null)
			xmlSubscription.setQty(subscription.getQty().intValue());
		
		try
		{
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(subscription.getStartDate());
			xmlSubscription.setStartDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
		}
		catch (DatatypeConfigurationException ex)
		{
			log.severe("Failed to set Start date for web service request to readSubscription() for " + subscription + " - " + ex);
		}
		
		try
		{
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(subscription.getPaidUntilDate());
			xmlSubscription.setPaidUntilDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
		}
		catch (DatatypeConfigurationException ex)
		{
			log.severe("Failed to set Paid Until date for web service request to readSubscription() for " + subscription + " - " + ex);
		}
		
		try
		{
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(subscription.getRenewalDate());
			xmlSubscription.setRenewalDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
		}
		catch (DatatypeConfigurationException ex)
		{
			log.severe("Failed to set Renewal date for web service request to readSubscription() for " + subscription + " - " + ex);
		}
		
		// Set response elements
		readSubscriptionResponse.subscription = xmlSubscription;		
		readSubscriptionResponse.setStandardResponse(getStandardResponse(true, "Subscription have been read fro Subscription Id[" + subscriptionId + "]", trxName, subscriptionId));
		
		return readSubscriptionResponse;
	}
	
	public StandardResponse updateSubscription(UpdateSubscriptionRequest updateSubscriptionRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(updateSubscriptionRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("UPDATE_SUBSCRIPTION_METHOD_ID"), updateSubscriptionRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);
		// Load and validate parameters
		Integer subscriptionId = updateSubscriptionRequest.getSubscriptionId();
		if (subscriptionId == null || subscriptionId < 1 || !Validation.validateADId(MSubscription.Table_Name, subscriptionId, trxName))
			return getErrorStandardResponse("Invalid subscription id", trxName);
		
		MSubscription subscription=new MSubscription(ctx,subscriptionId,trxName);
		
		String name=updateSubscriptionRequest.getName();
		if (!validateString(name))
			name = null;
		else
			name = name.trim();
		if (name != null)
			subscription.setName(name);
		
		Integer subscriptionTypeId=updateSubscriptionRequest.getSubscriptionTypeId();
		if(subscriptionTypeId !=null && subscriptionTypeId >0 && Validation.validateADId(X_C_SubscriptionType.Table_Name, subscriptionTypeId, trxName))
			subscription.setC_SubscriptionType_ID(subscriptionTypeId);
		
		Integer businessPartnerId=updateSubscriptionRequest.getBusinessPartnerId();
		if (businessPartnerId != null && businessPartnerId > 0 && Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
		    subscription.setC_BPartner_ID(businessPartnerId)	;
		
		Integer bpLocationId=updateSubscriptionRequest.getBusinessPartnerLocationId();
		if(bpLocationId !=null && bpLocationId >0 && Validation.validateADId(MBPartnerLocation.Table_Name, bpLocationId, trxName))
			subscription.setC_BPartner_Location_ID(bpLocationId);
		
		Integer productId=updateSubscriptionRequest.getProductId();
		if(productId !=null && productId >0 && Validation.validateADId(MProduct.Table_Name, productId, trxName))
			subscription.setM_Product_ID(productId);
        
		Integer userId=updateSubscriptionRequest.getUserId();
		if(userId !=null && userId >0 && Validation.validateADId(MUser.Table_Name, userId, trxName));
		    subscription.setAD_User_ID(userId);  
		
		XMLGregorianCalendar paidUntilDate= updateSubscriptionRequest.getPaidUntilDate();
		if(paidUntilDate !=null)
		{
			Timestamp pUntilDate=new Timestamp(updateSubscriptionRequest.getPaidUntilDate().toGregorianCalendar().getTimeInMillis());
			if(pUntilDate==null)
				return getErrorStandardResponse("Invalid Paid Until Date",trxName);
			else
				subscription.setPaidUntilDate(pUntilDate);
		}
		
		XMLGregorianCalendar startDate= updateSubscriptionRequest.getStartDate();
		if(startDate !=null)
		{
			Timestamp stDate=new Timestamp(updateSubscriptionRequest.getStartDate().toGregorianCalendar().getTimeInMillis());
			if(stDate==null)
				return getErrorStandardResponse("Invalid Start Date",trxName);
			else
				subscription.setStartDate(stDate);
		}
		
		XMLGregorianCalendar renewalDate= updateSubscriptionRequest.getRenewalDate();
		if(renewalDate !=null)
		{
			Timestamp renDate=new Timestamp(updateSubscriptionRequest.getRenewalDate().toGregorianCalendar().getTimeInMillis());
			if(renDate==null)
				return getErrorStandardResponse("Invalid Renewal Date",trxName);
			else
				subscription.setRenewalDate(renDate);
		}
		
		Boolean billInAdvance=updateSubscriptionRequest.isBillInAdvance();
		if(billInAdvance!=null)
			subscription.setBillInAdvance(billInAdvance);
		
		Boolean isDue=updateSubscriptionRequest.isIsDue();
		if(isDue!=null)
			subscription.setIsDue(isDue);

		BigDecimal qty=updateSubscriptionRequest.getQty();
		if(qty !=null && qty.compareTo(Env.ZERO)!=-1)
			subscription.setQty(qty);
		
		if (!subscription.save())
			return getErrorStandardResponse("Failed to save subscription " + name, trxName);
		
		return getStandardResponse(true, "Subscription " + subscriptionId + " has been updated", trxName, subscriptionId);
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
			xmlSubscription.setBusinessPartnerId(subscription.getC_BPartner_ID());
			xmlSubscription.setBusinessPartnerLocationId(subscription.getC_BPartner_Location_ID());
			xmlSubscription.setProductId(subscription.getM_Product_ID());
			xmlSubscription.setSubscriptionTypeId(subscription.getC_SubscriptionType_ID());
			xmlSubscription.setUserId(subscription.getAD_User_ID());
			
			try
			{
				GregorianCalendar c = new GregorianCalendar();
				c.setTime(subscription.getStartDate());
				xmlSubscription.setStartDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
			}
			catch (DatatypeConfigurationException ex)
			{
				log.severe("Failed to set Start date for web service request to readSubscriptions() for " + subscription + " - " + ex);
			}
			
			try
			{
				GregorianCalendar c = new GregorianCalendar();
				c.setTime(subscription.getPaidUntilDate());
				xmlSubscription.setPaidUntilDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
			}
			catch (DatatypeConfigurationException ex)
			{
				log.severe("Failed to set Paid Until date for web service request to readSubscriptions() for " + subscription + " - " + ex);
			}
			
			try
			{
				GregorianCalendar c = new GregorianCalendar();
				c.setTime(subscription.getRenewalDate());
				xmlSubscription.setRenewalDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
			}
			catch (DatatypeConfigurationException ex)
			{
				log.severe("Failed to set Renewal date for web service request to readSubscriptions() for " + subscription + " - " + ex);
			}
			
			xmlSubscription.setBillInAdvance(subscription.isBillInAdvance());
			
			if (subscription.getQty() != null)
				xmlSubscription.setQty(subscription.getQty().intValue());
			
			xmlSubscriptions.add(xmlSubscription);
		}
		
		// Set response elements
		readSubscriptionsResponse.subscription = xmlSubscriptions;		
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
		xmlOrder.setBusinessPartnerLocationId(order.getBill_Location_ID());

		try
		{
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(order.getDatePromised());
			xmlOrder.setDatePromised(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
		}
		catch (DatatypeConfigurationException ex)
		{
			log.severe("Failed to set Date Promised for web service request to readOrder() for " + order + " - " + ex);
		}
		
		try
		{
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(order.getDateOrdered());
			xmlOrder.setDateOrdered(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
		}
		catch (DatatypeConfigurationException ex)
		{
			log.severe("Failed to set Date Ordered for web service request to readOrder() for " + order + " - " + ex);
		}
		
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
	
	public ReadUsersByEmailResponse readUsersByEmail(ReadUsersByEmailRequest readUsersByEmailRequest)
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadUsersByEmailResponse readUsersByEmailResponse = objectFactory.createReadUsersByEmailResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readUsersByEmailRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("READ_USERS_BY_EMAIL_METHOD_ID"), readUsersByEmailRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readUsersByEmailResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readUsersByEmailResponse;
		}

		// Load and validate parameters
		String email = readUsersByEmailRequest.getEmail();
		if (!validateString(email) || email.contains("%"))
		{
			readUsersByEmailResponse.setStandardResponse(getErrorStandardResponse("Invalid email", trxName));
			return readUsersByEmailResponse;
		}
		else
			email = email.trim();
		
		// Get User(s)
		MUser[] users = MUserEx.getUsersByEmail(ctx, email);
		
		// Create response elements
		ArrayList<User> xmlUsers = new ArrayList<User>();		
		for (MUser user : users)
		{
			User xmlUser = objectFactory.createUser();
			xmlUser.setUserId(user.getAD_User_ID());
			xmlUser.setName(user.getName());
			xmlUser.setEmail(user.getEMail() != null ? user.getEMail() : "");
			xmlUser.setBusinessPartnerId(user.getC_BPartner_ID());
			
			xmlUsers.add(xmlUser);
		}
		
		// Set response elements
		readUsersByEmailResponse.user = xmlUsers;		
		readUsersByEmailResponse.setStandardResponse(getStandardResponse(true, "Users have been read for Email[" + email + "]", trxName, xmlUsers.size()));
		
		return readUsersByEmailResponse;
	}
	
	public ReadOrderNumberPortsResponse readOrderNumberPorts(ReadOrderNumberPortsRequest readOrderNumberPortsRequest)
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadOrderNumberPortsResponse readOrderNumberPortsResponse = objectFactory.createReadOrderNumberPortsResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readOrderNumberPortsRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("READ_ORDER_NUMBER_PORTS_METHOD_ID"), readOrderNumberPortsRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readOrderNumberPortsResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readOrderNumberPortsResponse;
		}

		// Load and validate parameters
		Integer orderId = readOrderNumberPortsRequest.getOrderId();
		if (orderId == null || orderId < 1 || !Validation.validateADId(MOrder.Table_Name, orderId, trxName))
		{
			readOrderNumberPortsResponse.setStandardResponse(getErrorStandardResponse("Invalid orderId", trxName));
			return readOrderNumberPortsResponse;
		}

		// Get order
		MOrder order = new MOrder(ctx, orderId, trxName);
		if (order == null)
		{
			readOrderNumberPortsResponse.setStandardResponse(getErrorStandardResponse("Failed to read Order[" + orderId + "]", trxName));
			return readOrderNumberPortsResponse;
		}
		
		// Check if order complete
		if (!order.isComplete())
		{
			readOrderNumberPortsResponse.setStandardResponse(getErrorStandardResponse("Order is not complete Order[" + orderId + "]", trxName));
			return readOrderNumberPortsResponse;
		}
		
		// Create response elements
		ArrayList<String> numbers = DIDUtil.getNumbersToPortFromOrder(ctx, order, trxName);
		
		// Set response elements
		readOrderNumberPortsResponse.numbers = numbers;		
		readOrderNumberPortsResponse.setStandardResponse(getStandardResponse(true, "Numbers to be ported have been read from MOrder[" + orderId + "]", trxName, numbers.size()));
		
		return readOrderNumberPortsResponse;
	}
	
	public ReadSubscribedNumbersResponse readSubscribedNumbers(ReadSubscribedNumbersRequest readSubscribedNumbersRequest)
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadSubscribedNumbersResponse readSubscribedNumbersResponse = objectFactory.createReadSubscribedNumbersResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readSubscribedNumbersRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("READ_SUBSCRIBED_NUMBERS_METHOD_ID"), readSubscribedNumbersRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readSubscribedNumbersResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readSubscribedNumbersResponse;
		}

		// Load and validate parameters
		Integer businessPartnerId = readSubscribedNumbersRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
		{
			readSubscribedNumbersResponse.setStandardResponse(getErrorStandardResponse("Invalid businessPartnerId", trxName));
			return readSubscribedNumbersResponse;
		}
		List<Integer> myList=new ArrayList<Integer>();
		myList.add(businessPartnerId);
  
        ArrayList<String> numbers = new ArrayList<String>();;
        for(int i=0;i<myList.size();i++)
        {
        	Integer BPID=myList.get(i);
        	List<Integer> resellerIDs=getResellerIDs(ctx, BPID, trxName);
        	if(!(resellerIDs.isEmpty()))
        		myList.addAll(resellerIDs);
			// Get all subscriptions belonging to business partner
			MSubscription[] subscriptions = MSubscription.getSubscriptions(ctx, null, BPID, trxName);
			
			// Store numbers belonging to subscriptions linked to CALLing products
			
			for (MSubscription subscription : subscriptions)
			{
				if (DIDUtil.isActiveMSubscription(ctx, subscription))
				{				
					MProduct product = MProduct.get(ctx, subscription.getM_Product_ID());
					if (product != null && product.get_ID () != 0)
					{
						String number = DIDUtil.getCDRNumber(ctx, product, trxName);
						if (number != null && !numbers.contains(number))
							numbers.add(number);
					}
				}
			}
        }
		readSubscribedNumbersResponse.numbers = numbers;		
		readSubscribedNumbersResponse.setStandardResponse(getStandardResponse(true, "Subscribed numbers have been read for BusinessPartner[" + businessPartnerId + "]", trxName, numbers.size()));
		
		return readSubscribedNumbersResponse;
	}
	
	public StandardResponse createUserRole(CreateUserRoleRequest createUserRoleRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(createUserRoleRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("CREATE_USER_ROLE_METHOD_ID"), createUserRoleRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		Integer userId = createUserRoleRequest.getUserId();
		if (userId == null || userId < 1 || !Validation.validateADId(MUser.Table_Name, userId, trxName))
			return getErrorStandardResponse("Invalid userId", trxName);

		Integer roleId = createUserRoleRequest.getRoleId();
		if (roleId == null || roleId < 1 || !Validation.validateADId(MRole.Table_Name, roleId, trxName))
			return getErrorStandardResponse("Invalid roleId", trxName);
		
		// Create user role
		MUserRoles userRole = new MUserRoles(ctx, userId, roleId, trxName);		
		if (!userRole.save())
			return getErrorStandardResponse("Failed to save UserRole", trxName);
		
		return getStandardResponse(true, "UserRole has been created for MUser[" + userId + "] & MRole[" + roleId + "]", trxName, userId);
	}
	
	public StandardResponse readUserRole(ReadUserRoleRequest readUserRoleRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readUserRoleRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("READ_USER_ROLE_METHOD_ID"), readUserRoleRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		Integer userId = readUserRoleRequest.getUserId();
		if (userId == null || userId < 1 || !Validation.validateADId(MUser.Table_Name, userId, trxName))
			return getErrorStandardResponse("Invalid userId", trxName);

		Integer roleId = readUserRoleRequest.getRoleId();
		if (roleId == null || roleId < 1 || !Validation.validateADId(MRole.Table_Name, roleId, trxName))
			return getErrorStandardResponse("Invalid roleId", trxName);
		
		// Match user role
		MUserRoles[] roles = MUserRoles.getOfUser(ctx, userId);
		for (MUserRoles role : roles)
		{
			if (role.getAD_Role_ID() == roleId)
				return getStandardResponse(true, "MUser[" + userId + "] has MRole[" + roleId + "]", trxName, userId);
		}

		return getErrorStandardResponse("Failed to load role for MUser[" + userId + "]", trxName);
	}
	
	public StandardResponse updateUserRole(UpdateUserRoleRequest updateUserRoleRequest)
	{
		return getErrorStandardResponse("updateUserRole() hasn't been implemented yet", null);
	}
	
	public StandardResponse deleteUserRole(DeleteUserRoleRequest deleteUserRoleRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(deleteUserRoleRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("DELETE_USER_ROLE_METHOD_ID"), deleteUserRoleRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		Integer userId = deleteUserRoleRequest.getUserId();
		if (userId == null || userId < 1 || !Validation.validateADId(MUser.Table_Name, userId, trxName))
			return getErrorStandardResponse("Invalid userId", trxName);

		Integer roleId = deleteUserRoleRequest.getRoleId();
		if (roleId == null || roleId < 1 || !Validation.validateADId(MRole.Table_Name, roleId, trxName))
			return getErrorStandardResponse("Invalid roleId", trxName);
		
		// Create user role
		boolean success = false;
		MUserRoles[] userRoles = MUserRoles.getOfUser(ctx, userId);
		for (MUserRoles userRole : userRoles)
		{
			if (userRole.getAD_Role_ID() == roleId)
			{
				if (userRole.delete(true))
				{
					success = true;
					break;
				}
				else
					return getErrorStandardResponse("Failed to delete UserRole for MUser[" + userId + "] & MRole[" + roleId + "]", trxName);
			}
		}

		if (success)
			return getStandardResponse(true, "UserRole has been deleted for MUser[" + userId + "] & MRole[" + roleId + "]", trxName, userId);
		else
			return getErrorStandardResponse("Failed to load UserRole for MUser[" + userId + "] & MRole[" + roleId + "]", trxName);
	}
	
	public ReadRolesResponse readRoles(ReadRolesRequest readRolesRequest)
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadRolesResponse readRolesResponse = objectFactory.createReadRolesResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readRolesRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("READ_ROLES_METHOD_ID"), readRolesRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readRolesResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readRolesResponse;
		}
		
		// Read roles
		ArrayList<Role> xmlRoles = new ArrayList<Role>();
		MRole[] roles = MRole.getOfClient(ctx);
		for (MRole role : roles)
		{
			Role xmlRole = objectFactory.createRole();
			xmlRole.setRoleId(role.getAD_Role_ID());
			xmlRole.setName(role.getName());
			
			xmlRoles.add(xmlRole);
		}

		// Set response elements
		readRolesResponse.role = xmlRoles;		
		readRolesResponse.setStandardResponse(getStandardResponse(true, "Roles have been read", trxName, xmlRoles.size()));
		
		return readRolesResponse;
	}
	
	public ReadUsersByBusinessPartnerResponse readUsersByBusinessPartner(ReadUsersByBusinessPartnerRequest readUsersByBusinessPartnerRequest)
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadUsersByBusinessPartnerResponse readUsersByBusinessPartnerResponse = objectFactory.createReadUsersByBusinessPartnerResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readUsersByBusinessPartnerRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("READ_USERS_BY_BUSINESS_PARTNER_METHOD_ID"), readUsersByBusinessPartnerRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readUsersByBusinessPartnerResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readUsersByBusinessPartnerResponse;
		}

		// Load and validate parameters
		Integer businessPartnerId = readUsersByBusinessPartnerRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
		{
			readUsersByBusinessPartnerResponse.setStandardResponse(getErrorStandardResponse("Invalid businessPartnerId", trxName));
			return readUsersByBusinessPartnerResponse;
		}
		
		// Get User(s)
		MUser[] users = MUser.getOfBPartner(ctx, businessPartnerId);
		
		// Create response elements
		ArrayList<User> xmlUsers = new ArrayList<User>();		
		for (MUser user : users)
		{
			User xmlUser = objectFactory.createUser();
			xmlUser.setUserId(user.getAD_User_ID());
			xmlUser.setName(user.getName());
			xmlUser.setEmail(user.getEMail() != null ? user.getEMail() : "");
			xmlUser.setPhone(user.getPhone() != null ? user.getPhone() : "");
			xmlUser.setMobile(user.getPhone2() != null ? user.getPhone2() : "");
			xmlUser.setBusinessPartnerId(user.getC_BPartner_ID());
			
			ArrayList<Role> xmlRoles = new ArrayList<Role>();
			MUserRoles[] userRoles = MUserRoles.getOfUser(ctx, user.getAD_User_ID());
			for (MUserRoles userRole : userRoles)
			{
				Role xmlRole = objectFactory.createRole();
				xmlRole.setRoleId(userRole.getAD_Role_ID());
				try
				{
					xmlRole.setName(userRole.getAD_Role().getName());
				}
				catch(Exception ex)
				{
					log.severe("Failed to load MUserRoles from MUser[" + userRole.getAD_User_ID() + "] and MRole[" + userRole.getAD_Role_ID() + "]");
				}
				
				xmlRoles.add(xmlRole);
			}
			
			xmlUser.role = xmlRoles;
			xmlUsers.add(xmlUser);
		}
		
		// Set response elements
		readUsersByBusinessPartnerResponse.user = xmlUsers;		
		readUsersByBusinessPartnerResponse.setStandardResponse(getStandardResponse(true, "Users have been read for MBPartner[" + businessPartnerId + "]", trxName, xmlUsers.size()));
		
		return readUsersByBusinessPartnerResponse;
	}
	
	public ReadCallRecordingResponse readCallRecording(ReadCallRecordingRequest readCallRecordingRequest)
	{
		// Create response
		ObjectFactory objectFactory = new ObjectFactory();
		ReadCallRecordingResponse readCallRecordingResponse = objectFactory.createReadCallRecordingResponse();
		
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(readCallRecordingRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"), WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("READ_CALL_RECORDING_METHOD_ID"), readCallRecordingRequest.getLoginRequest(), trxName);		
		if (error != null)	
		{
			readCallRecordingResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readCallRecordingResponse;
		}

		// Load and validate parameters
		Integer businessPartnerId = readCallRecordingRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
		{
			readCallRecordingResponse.setStandardResponse(getErrorStandardResponse("Invalid businessPartnerId", trxName));
			return readCallRecordingResponse;
		}
		
		Integer radAcctId = readCallRecordingRequest.getRadAcctId();
		if (radAcctId == null || radAcctId < 1)
		{
			readCallRecordingResponse.setStandardResponse(getErrorStandardResponse("Invalid radAcctId", trxName));
			return readCallRecordingResponse;
		}
		
		// Load RadAcct
		com.conversant.model.RadiusAccount radiusAccount = RadiusConnector.getRadiusAccount(radAcctId);
		
		// Get username and format
		String username = radiusAccount.getUserName();
		username = username.replace("+", "");
		username = username.substring(0, username.indexOf("@"));
		while (username.startsWith("0"))
			username = username.substring(1, username.length());

		// Load calling products
		MProduct[] callingProducts = DIDUtil.getCallProducts(ctx, username, trxName);
		if (callingProducts.length != 2)
		{
			readCallRecordingResponse.setStandardResponse(getErrorStandardResponse("Failed to load calling products for " + username, trxName));
			return readCallRecordingResponse;
		}
		
		MProduct inboundCallProduct = callingProducts[0];
		MProduct outboundCallProduct = callingProducts[1];		
		if (!DIDUtil.isInbound(ctx, inboundCallProduct, trxName))
		{
			inboundCallProduct = callingProducts[1];
			outboundCallProduct = callingProducts[0];
		}

		// Load calling subscriptions
		MSubscription[] inboundCallSubscriptions = MSubscription.getSubscriptions(ctx, inboundCallProduct.getM_Product_ID(), businessPartnerId, trxName);
		boolean inboundCallSubscriptionFound = false;
		for (MSubscription subscription : inboundCallSubscriptions)
		{				
			if (DIDUtil.isActiveMSubscription(ctx, subscription))
			{
				inboundCallSubscriptionFound = true;
				break;
			}
		}
		
		if (!inboundCallSubscriptionFound)
		{
			readCallRecordingResponse.setStandardResponse(getErrorStandardResponse("Failed to load calling subscription for " + inboundCallProduct, trxName));
			return readCallRecordingResponse;
		}
		
		MSubscription[] outboundCallSubscriptions = MSubscription.getSubscriptions(ctx, outboundCallProduct.getM_Product_ID(), businessPartnerId, trxName);
		boolean outboundCallSubscriptionFound = false;
		for (MSubscription subscription : outboundCallSubscriptions)
		{				
			if (DIDUtil.isActiveMSubscription(ctx, subscription))
			{
				outboundCallSubscriptionFound = true;
				break;
			}
		}
		
		if (!outboundCallSubscriptionFound)
		{
			readCallRecordingResponse.setStandardResponse(getErrorStandardResponse("Failed to load calling subscription for " + outboundCallProduct, trxName));
			return readCallRecordingResponse;
		}
		
		// Find the Radius Account's matching listenId (mp3)
		String listenId = null;
		try
		{
			BillingRecord billingRecord = BillingConnector.getBillingRecord(radiusAccount);
			if (!billingRecord.getMp3().equals("0")) // before new id was stored
				listenId = billingRecord.getMp3();
			else
				listenId = Long.toString(billingRecord.getTwoTalkId()); // use as fallback
		}
		catch (Exception ex)
		{
			log.warning(ex.getMessage());
			
			readCallRecordingResponse.setStandardResponse(getErrorStandardResponse(ex.getMessage(), trxName));
			return readCallRecordingResponse;
		}
		
		File recording = TwoTalkConnector.getCallRecording(listenId);
		if (recording == null)
		{
			readCallRecordingResponse.setStandardResponse(getErrorStandardResponse("Failed to download recording", trxName));
			return readCallRecordingResponse;
		}

		if (!recording.renameTo(new File(CALL_RECORDING_DIR + recording.getName())))
		{
			readCallRecordingResponse.setStandardResponse(getErrorStandardResponse("Failed to rename/move recording", trxName));
			return readCallRecordingResponse;
		}
		
		String url = CALL_RECORDING_URL + recording.getName();
		
		// Set response elements
		readCallRecordingResponse.url = url;		
		readCallRecordingResponse.setStandardResponse(getStandardResponse(true, "Call recording has been read for " + radiusAccount, trxName, null));
		
		return readCallRecordingResponse;
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
	
	public ReadBPLocationResponse readBPLocations(ReadBPLocationRequest readBPLocationRequest)
	{
		// Create response
		ObjectFactory objectFactory=new ObjectFactory();
		ReadBPLocationResponse readBPLocationResponse=objectFactory.createReadBPLocationResponse();
		
		//Create ctx and trxName (if not specified)
		Properties ctx=Env.getCtx();
		String trxName=getTrxName(readBPLocationRequest.getLoginRequest());
		
		//Login to Adempiere
		String error=login(ctx,WebServiceConstants.WEBSERVICES.get("ADMIN_WEBSERVICE"),WebServiceConstants.ADMIN_WEBSERVICE_METHODS.get("READ_BUSINESS_PARTNER_LOCATION_METHOD_ID"),readBPLocationRequest.getLoginRequest(), trxName);
		if (error != null)	
		{
			readBPLocationResponse.setStandardResponse(getErrorStandardResponse(error, trxName));
			return readBPLocationResponse;
		}
		
		// Load and validate parameters
		Integer businessPartnerId = readBPLocationRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
		{
			readBPLocationResponse.setStandardResponse(getErrorStandardResponse("Invalid businessPartnerId", trxName));
			return readBPLocationResponse;
		}
		// Get business partner based on id
		MBPartner businessPartner = new MBPartner(ctx, businessPartnerId, trxName);
		MBPartnerLocation[] bplocations=businessPartner.getLocations(false);
		// Create response elements
		ArrayList<BPLocation> xmlBPLocations = new ArrayList<BPLocation>();
		if (bplocations != null)
		{
			for (int i = 0; i < bplocations.length; i++)
			{
			    BPLocation xmlBPLocation=objectFactory.createBPLocation();
			    MLocation location=bplocations[i].getLocation(false);
			    xmlBPLocation.setBusinessPartnerId(bplocations[i].getC_BPartner_ID());
			    xmlBPLocation.setName(bplocations[i].getName());
			    xmlBPLocation.setPhone(bplocations[i].getPhone());
			    xmlBPLocation.setSecondPhone(bplocations[i].getPhone2());
			    xmlBPLocation.setFax(bplocations[i].getFax());
			    xmlBPLocation.setIsdn(bplocations[i].getISDN());
			    xmlBPLocation.setLocationId(bplocations[i].getC_Location_ID());
			    xmlBPLocation.setIsActive(bplocations[i].isActive());
			    xmlBPLocation.setAddress1(location.getAddress1());
			    xmlBPLocation.setAddress2(location.getAddress2());
			    xmlBPLocation.setAddress3(location.getAddress3());
			    xmlBPLocation.setAddress4(location.getAddress4());
			    xmlBPLocation.setCity(location.getCity());
			    xmlBPLocation.setCityId(location.getC_City_ID());
			    xmlBPLocation.setZip(location.getPostal());
			    xmlBPLocation.setRegion(location.getRegionName());
			    xmlBPLocation.setRegionId(location.getC_Region_ID());
			    xmlBPLocation.setCountryId(location.getC_Country_ID());
			    xmlBPLocation.setBusinessPartnerLocationId(bplocations[i].getC_BPartner_Location_ID());
			    
			    xmlBPLocations.add(xmlBPLocation);
			}
			
		}
		
		// Set response elements
		readBPLocationResponse.bpLocation = xmlBPLocations;		
		readBPLocationResponse.setStandardResponse(getStandardResponse(true, "Business Partner locations have been read for MBPartner[" + businessPartnerId + "]", trxName, xmlBPLocations.size()));
		
		return readBPLocationResponse;
	}
	
	private List<Integer> getResellerIDs(Properties ctx,int C_BPartner_ID,String trxName)
	{ 
		List<Integer> eligibleEndCustomerList=new ArrayList<Integer>();
		int AD_Client_ID=Env.getAD_Client_ID(ctx);
		String sql="SELECT C_BPartner_ID FROM C_BPartner WHERE AD_CLIENT_ID= ? AND SalesRep_ID IN " +
		   "(SELECT AD_USER_ID FROM AD_USER WHERE C_BPARTNER_ID = ? )";
		log.log(Level.INFO, sql);
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{	
				// Create statement and set parameters
				pstmt = DB.prepareStatement(sql.toString(), trxName);
				pstmt.setInt(1, AD_Client_ID);
				pstmt.setInt(2, C_BPartner_ID);
				// Execute query and process result set
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					eligibleEndCustomerList.add(rs.getInt(1));
				}
					
			}
			catch (SQLException ex)
			{
				log.log(Level.SEVERE, sql.toString(), ex);
			}
			finally 
			{
				DB.close(rs, pstmt);
				rs = null; 
				pstmt = null;
			}
			return eligibleEndCustomerList;
	}
}
