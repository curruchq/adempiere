package com.conversant.test;

import java.sql.Timestamp;
import java.util.HashMap;

import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MProduct;
import org.compiere.model.MSubscription;

import com.conversant.did.DIDConstants;
import com.conversant.did.DIDUtil;
import com.conversant.did.DIDValidation;

public class DIDValidationTestCase extends AdempiereTestCase  
{
	public void testValidatedAttributes()
	{
		HashMap<Integer, String> attributes = new HashMap<Integer, String>();

		// Invalid MAttributeSet
		if (DIDValidation.validateAttributes(getCtx(), 777, attributes))
			fail("Validated invalid MAttributeSet");
		
		// No attributes
		if (DIDValidation.validateAttributes(getCtx(), Integer.parseInt(DIDConstants.DID_ATTRIBUTE_SET_ID), attributes))
			fail("Validated with no attributes");
		
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_AREACODE, "9426");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_COUNTRYCODE, "64");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_COUNTRYID, "147");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_DESCRIPTION, "Hibiscus Coast. Test");
		
		// Incomplete set
		if (DIDValidation.validateAttributes(getCtx(), Integer.parseInt(DIDConstants.DID_ATTRIBUTE_SET_ID), attributes))
			fail("Validated with incomplete set");
		
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_FAX_FROMEMAIL, "");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_FAX_ISFAX, "false");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_FAX_TOEMAIL, "");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_FREEMINS, "");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_ISSETUP, null);
		
		// Invalid values & incomplete
		if (DIDValidation.validateAttributes(getCtx(), Integer.parseInt(DIDConstants.DID_ATTRIBUTE_SET_ID), attributes))
			fail("Validated with invalid values and incomplete set");
		
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_NUMBER, "");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_PERMINCHARGES, "");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_SUBSCRIBED, "true");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_VENDORRATING, null);
		
		// Invalid values & complete
		if (DIDValidation.validateAttributes(getCtx(), Integer.parseInt(DIDConstants.DID_ATTRIBUTE_SET_ID), attributes))
			fail("Validated with invalid values and complete set");
		
		attributes.clear();
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_AREACODE, "9426");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_COUNTRYCODE, "64");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_COUNTRYID, "147");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_DESCRIPTION, "Hibiscus Coast. Test");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_FAX_FROMEMAIL, "-");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_FAX_ISFAX, "false");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_FAX_TOEMAIL, "-");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_FREEMINS, "0");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_ISSETUP, "true");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_NUMBER, "6494261879");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_PERMINCHARGES, "0");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_SUBSCRIBED, "true");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_VENDORRATING, "5");
		
		// Valid values & complete
		if (!DIDValidation.validateAttributes(getCtx(), Integer.parseInt(DIDConstants.DID_ATTRIBUTE_SET_ID), attributes))
			fail("Didn't valiate with complete set and valid values");
		
		attributes.clear();
		attributes.put(DIDConstants.ATTRIBUTE_ID_SIP_ADDRESS, "1234567890");
		
		// Incomplete set
		if (DIDValidation.validateAttributes(getCtx(), Integer.parseInt(DIDConstants.SIP_ATTRIBUTE_SET_ID), attributes))
			fail("Validated with incomplete set");
		
		attributes.put(DIDConstants.ATTRIBUTE_ID_SIP_DOMAIN, "conversant.co.nz");
		
		// Valid values & complete
		if (!DIDValidation.validateAttributes(getCtx(), Integer.parseInt(DIDConstants.SIP_ATTRIBUTE_SET_ID), attributes))
			fail("Didn't valiate with complete set and valid values");
		
		attributes.clear();
		attributes.put(DIDConstants.ATTRIBUTE_ID_VM_CONTEXT, "ctx");
		attributes.put(DIDConstants.ATTRIBUTE_ID_VM_MACRO_NAME, "macroName");
		
		// Incomplete set
		if (DIDValidation.validateAttributes(getCtx(), Integer.parseInt(DIDConstants.VOICEMAIL_ATTRIBUTE_SET_ID), attributes))
			fail("Validated with incomplete set");
		
		attributes.put(DIDConstants.ATTRIBUTE_ID_VM_MAILBOX_NUMBER, "1234567890");
		
		// Valid values & complete
		if (!DIDValidation.validateAttributes(getCtx(), Integer.parseInt(DIDConstants.VOICEMAIL_ATTRIBUTE_SET_ID), attributes))
			fail("Didn't valiate with complete set and valid values");
	}
	
	public void testValidateMandatoryFields()
	{
		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put(MProduct.COLUMNNAME_Value, "TestSearchKey");
		fields.put(MProduct.COLUMNNAME_Name, "TestName");
		fields.put(MProduct.COLUMNNAME_Description, "TestDescription");	
		fields.put(MProduct.COLUMNNAME_M_Product_Category_ID, DIDConstants.VOICE_SERVICES_CATEGORY_ID);
		fields.put(MProduct.COLUMNNAME_C_TaxCategory_ID, DIDConstants.STANDARD_TAX_CATEGORY); 
		fields.put(MProduct.COLUMNNAME_C_UOM_ID, DIDConstants.UOM_MONTH_8DEC); 	
		fields.put(MProduct.COLUMNNAME_ProductType, DIDConstants.PRODUCT_TYPE_SERVICE); 
		fields.put(MProduct.COLUMNNAME_IsSelfService, DIDConstants.NOT_SELF_SERVICE); 
		
		if (!DIDValidation.validateMandatoryFields(new MProduct(getCtx(), 0, null), fields))
			fail("Failed to validate attributes");
		
		MProduct product = DIDUtil.createProduct(getCtx(), fields, null);
		if (product == null)
			fail("Failed to create product");
		
		fields.clear();
		fields.put(MSubscription.COLUMNNAME_Name, "TestSubscriptionName");
		fields.put(MSubscription.COLUMNNAME_C_BPartner_ID, 1000071); 
		fields.put(MBPartnerLocation.COLUMNNAME_C_BPartner_Location_ID, 1000149);
		fields.put(MSubscription.COLUMNNAME_M_Product_ID, product.getM_Product_ID());
		fields.put(MSubscription.COLUMNNAME_C_SubscriptionType_ID, DIDConstants.C_SUBSCRIPTIONTYPE_ID_MONTH_1); 		
		fields.put(MSubscription.COLUMNNAME_StartDate, new Timestamp(System.currentTimeMillis()));
		fields.put(MSubscription.COLUMNNAME_PaidUntilDate, new Timestamp(System.currentTimeMillis())); 
		fields.put(MSubscription.COLUMNNAME_RenewalDate, new Timestamp(System.currentTimeMillis())); 
		fields.put(MSubscription.COLUMNNAME_IsDue, false); 
		
		if (!DIDValidation.validateMandatoryFields(new MSubscription(getCtx(), 0, null), fields))
			fail("Failed to validate attributes");

		MSubscription subscription = DIDUtil.createSubscription(getCtx(), fields, null);
		if (subscription == null)
			fail("Failed to create subscription");	
		
		subscription.delete(true);
		product.delete(true);
	}
	
	public void testValidateDIDsInWebBasket()
	{
		// TODO: Finish me
	}
	
	public void testValidateDIDsInOrder()
	{
		// TODO: Finish me
	}
	
	public void testValidateDID()
	{
		// TODO: Finish me
	}
}
