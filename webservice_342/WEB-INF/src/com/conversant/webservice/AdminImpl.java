package com.conversant.webservice;

import java.util.HashMap;
import java.util.Properties;

import javax.jws.WebService;

import org.compiere.model.MBPGroup;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerEx;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MCountry;
import org.compiere.model.MLocation;
import org.compiere.model.MRegion;
import org.compiere.model.MUser;
import org.compiere.model.X_C_City;
import org.compiere.util.Env;

import com.conversant.util.Validation;
import com.conversant.webservice.util.WebServiceConstants;
import com.conversant.webservice.util.WebServiceUtil;

@WebService(endpointInterface = "com.conversant.webservice.Admin")
public class AdminImpl extends GenericWebServiceImpl implements Admin
{
	// TODO: Update invoice schedule? WebUtil.updateInvoiceSchedule()
	public StandardResponse createBusinessPartner(CreateBusinessPartnerRequest createBusinessPartnerRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(createBusinessPartnerRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.ADMIN_WEBSERVICE_ID, WebServiceConstants.CREATE_BUSINESS_PARTNER, createBusinessPartnerRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);

		// Load and validate parameters
		String searchKey = createBusinessPartnerRequest.getSearchKey();
		if (searchKey == null || searchKey.trim().length() < 1) // TODO: add trim to all other validation check
			searchKey = null; // Allow ADempiere auto sequencing to set Search Key
		else
			searchKey = searchKey.trim();
		
		String name = createBusinessPartnerRequest.getName();
		if (name == null || name.length() < 1)
			return getErrorStandardResponse("Invalid name", trxName);
		else
			name = name.trim();
		
		Integer businessPartnerGroupId = createBusinessPartnerRequest.getBusinessPartnerGroupId();
		if (businessPartnerGroupId == null || businessPartnerGroupId < 1 || !validateADId(MBPGroup.Table_Name, businessPartnerGroupId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerGroupId", trxName);
		else
			name = name.trim();

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
		
		if (!businessPartner.save())
			return getErrorStandardResponse("Failed to save Business Partner", trxName);
			
		return getStandardResponse(true, "Business Partner has been created for " + name, trxName, businessPartner.getC_BPartner_ID());
	}
	
	public StandardResponse createBusinessPartnerLocation(CreateBusinessPartnerLocationRequest createBusinessPartnerLocationRequest)
	{
		// Create ctx and trxName (if not specified)
		Properties ctx = Env.getCtx(); 
		String trxName = getTrxName(createBusinessPartnerLocationRequest.getLoginRequest());
		
		// Login to ADempiere
		String error = login(ctx, WebServiceConstants.ADMIN_WEBSERVICE_ID, WebServiceConstants.CREATE_BUSINESS_PARTNER_LOCATION, createBusinessPartnerLocationRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);
		
		// Load and validate parameters
		String name = createBusinessPartnerLocationRequest.getName();
		if (name == null || name.length() < 1)
			return getErrorStandardResponse("Invalid name", trxName);
		else
			name = name.trim();
		
		Integer businessPartnerId = createBusinessPartnerLocationRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		Integer locationId = createBusinessPartnerLocationRequest.getLocationId();
		if (locationId == null || locationId < 1 || !validateADId(MLocation.Table_Name, locationId, trxName))
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
		String error = login(ctx, WebServiceConstants.ADMIN_WEBSERVICE_ID, WebServiceConstants.CREATE_LOCATION, createLocationRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);
		
		// Load and validate parameters
		String address1 = createLocationRequest.getAddress1(); // Mandatory
		if (address1 == null || address1.length() < 1)
			return getErrorStandardResponse("Invalid address1", trxName);
		else
			address1 = address1.trim();
		
		String address2 = createLocationRequest.getAddress2();
		if (address2 != null && address2.length() < 1)
			address2 = null;
		else if (address2 != null && address2.length() > 1)
			address2 = address2.trim();
		
		String address3 = createLocationRequest.getAddress3();
		if (address3 != null && address3.length() < 1)
			address3 = null;
		else if (address3 != null && address3.length() > 1)
			address3 = address3.trim();
		
		String address4 = createLocationRequest.getAddress4();
		if (address4 != null && address4.length() < 1)
			address4 = null;
		else if (address4 != null && address4.length() > 1)
			address4 = address4.trim();
		
		String city = createLocationRequest.getCity(); // Mandatory
		if (city == null || city.length() < 1)
			return getErrorStandardResponse("Invalid city", trxName);
		else
			city = city.trim();
		
		Integer cityId = createLocationRequest.getCityId();
		if (cityId != null && cityId > 0 && !validateADId(X_C_City.Table_Name, cityId, trxName))
			return getErrorStandardResponse("Invalid cityId", trxName);
		
		String zip = createLocationRequest.getZip();
		if (zip != null && zip.length() < 1)
			zip = null;
		else if (zip != null && zip.length() > 1)
			zip = zip.trim();		
		
		String region = createLocationRequest.getRegion();
		if (region != null && region.length() < 1)
			region = null;
		else if (region != null && region.length() > 1)
			region = region.trim();	
		
		Integer regionId = createLocationRequest.getRegionId();
		if (regionId != null && regionId > 0 && !validateADId(MRegion.Table_Name, regionId, trxName))
			return getErrorStandardResponse("Invalid regionId", trxName);
		
		Integer countryId = createLocationRequest.getCountryId(); // Mandatory
		if (countryId == null || countryId < 1 || !validateADId(MCountry.Table_Name, countryId, trxName))
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
		String error = login(ctx, WebServiceConstants.ADMIN_WEBSERVICE_ID, WebServiceConstants.CREATE_USER, createUserRequest.getLoginRequest(), trxName);		
		if (error != null)	
			return getErrorStandardResponse(error, trxName);
		
		String name = createUserRequest.getName();
		if (name == null || name.length() < 1)
			return getErrorStandardResponse("Invalid name", trxName);
		else
			name = name.trim();
		
		String password = createUserRequest.getPassword();
		if (password == null || password.length() < 1)
			return getErrorStandardResponse("Invalid password", trxName);
		else
			password = password.trim();
		
		String email = createUserRequest.getEmail();
		if (email == null || email.length() < 1 || !WebServiceUtil.isEmailValid(email))
			return getErrorStandardResponse("Invalid email", trxName);
		else
			email = email.trim();
		
		Integer businessPartnerId = createUserRequest.getBusinessPartnerId();
		if (businessPartnerId == null || businessPartnerId < 1 || !validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
			return getErrorStandardResponse("Invalid businessPartnerId", trxName);
		
		Integer businessPartnerLocationId = createUserRequest.getBusinessPartnerLocationId();
		if (businessPartnerLocationId == null || businessPartnerLocationId < 1 || !validateADId(MBPartnerLocation.Table_Name, businessPartnerLocationId, trxName))
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
}
