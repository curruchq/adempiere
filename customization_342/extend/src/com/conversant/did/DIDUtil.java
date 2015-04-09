package com.conversant.did;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MAttributeValue;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPO;
import org.compiere.model.MProductPrice;
import org.compiere.model.MSubscription;
import org.compiere.util.CLogger;
import org.compiere.util.Trx;
import org.compiere.wstore.DIDDescription;

import com.conversant.model.DID;
import com.conversant.model.DIDAreaCode;
import com.conversant.model.DIDCountry;
import com.conversant.util.Validation;

public class DIDUtil 
{
	private static CLogger log = CLogger.getCLogger(DIDUtil.class);

// *****************************************************************************************************************************************
	
	public static MProduct createDIDProduct(Properties ctx, HashMap<Integer, Object> attributes, String trxName)
	{
		// Load or create new trx
		boolean createdTrx = false;		
		if (trxName == null || trxName.length() < 1)
		{
			trxName = Trx.createTrxName("createDIDProduct");
			createdTrx = true;
		}

		try
		{
			// Validate attributes
			if (!Validation.validateAttributes(ctx, Integer.parseInt(DIDConstants.DID_ATTRIBUTE_SET_ID), attributes))
				throw new Exception("Failed to validate attributes");
			
			// Load attribute values
			String number = (String)attributes.get(DIDConstants.ATTRIBUTE_ID_DID_NUMBER);
			String didDescription = (String)attributes.get(DIDConstants.ATTRIBUTE_ID_DID_DESCRIPTION);
			boolean isSetup = ((String)attributes.get(DIDConstants.ATTRIBUTE_ID_DID_ISSETUP)).equalsIgnoreCase("true");
			
			// Monthly product fields
			String searchKey = DIDConstants.DID_MONTHLY_PRODUCT_SEARCH_KEY;
			String name = DIDConstants.DID_MONTHLY_PRODUCT_NAME;
			String description = DIDConstants.DID_MONTHLY_PRODUCT_DESCRIPTION;
			String uom = DIDConstants.UOM_MONTH_8DEC;
			String productCategory = DIDConstants.VOICE_SERVICES_RECUR_NONCALL_CATEGORY_ID;
			
			// Setup product fields
			if (isSetup)
			{
				searchKey = DIDConstants.DID_SETUP_PRODUCT_SEARCH_KEY;
				name = DIDConstants.DID_SETUP_PRODUCT_NAME;
				description = DIDConstants.DID_SETUP_PRODUCT_DESCRIPTION;
				uom = DIDConstants.UOM_EACH;
				productCategory = DIDConstants.VOICE_SERVICES_NONRECUR_NONCALL_CATEGORY_ID;
			}
			
			searchKey = searchKey.replace(DIDConstants.NUMBER_IDENTIFIER, number);
			name = name.replace(DIDConstants.NUMBER_IDENTIFIER, number);
			description = description.replace(DIDConstants.DID_DESCRIPTION_IDENTIFIER, didDescription);
			
			HashMap<String, Object> fields = new HashMap<String, Object>();
			fields.put(MProduct.COLUMNNAME_Value, searchKey);
			fields.put(MProduct.COLUMNNAME_Name, name);
			fields.put(MProduct.COLUMNNAME_Description, description);
			fields.put(MProduct.COLUMNNAME_M_Product_Category_ID, productCategory);
			fields.put(MProduct.COLUMNNAME_C_TaxCategory_ID, DIDConstants.STANDARD_15_TAX_CATEGORY); 
			fields.put(MProduct.COLUMNNAME_C_UOM_ID, uom);  
			fields.put(MProduct.COLUMNNAME_M_AttributeSet_ID, DIDConstants.DID_ATTRIBUTE_SET_ID);
			fields.put(MProduct.COLUMNNAME_ProductType, DIDConstants.PRODUCT_TYPE_SERVICE);
			fields.put(MProduct.COLUMNNAME_IsSelfService, DIDConstants.NOT_SELF_SERVICE);
					
			MProduct product = createProduct(ctx, fields, trxName);
			if (product == null)
				throw new Exception("Failed to create MProduct[" + searchKey + "]");
			
			// Create new attribute set instance
			MAttributeSetInstance masi = new MAttributeSetInstance(ctx, 0, trxName);
			masi.setM_AttributeSet_ID(Integer.parseInt(DIDConstants.DID_ATTRIBUTE_SET_ID));
			if (!masi.save())
				throw new Exception("Failed to create MAttributeSetInstance for " + product);
			
			// Save new attribute set instance id to product
			product.setM_AttributeSetInstance_ID(masi.getM_AttributeSetInstance_ID());
			if (!product.save())
				throw new Exception("Failed to save MAttributeSetInstance for " + product);
			
			// Create attribute instances
			Iterator<Integer> iterator = attributes.keySet().iterator();
			while(iterator.hasNext())
			{
				Integer attributeId = (Integer)iterator.next();
				String attributeValue = (String)attributes.get(attributeId);
				
				MAttributeInstance attributeInstance = new MAttributeInstance(ctx, attributeId, masi.getM_AttributeSetInstance_ID(), attributeValue, trxName);
				if (!attributeInstance.save())
					throw new Exception("Failed to save MAttributeInstance[" + attributeId + "-" + attributeValue + "]  for " + product);
			}
			
			// Update MAttributeSetInstance description (attribute values seperated with _, don't need to worry if it fails)
			masi.setDescription();
			if (!masi.save())
				log.warning("Failed to update " + masi + "'s description");
			
			// If created trx in this method try to load then commit trx
			if (createdTrx)
			{
				Trx trx = Trx.get(trxName, false);
				if (trx == null)
					throw new Exception("Failed to load trx");
				else if (!trx.isActive())
					throw new Exception("Trx no longer active");
				else if (!trx.commit())
					throw new Exception("Failed to commit trx");
			}
			
			// To reset trxName
//			product.load(trxName);
			
			return product;
		}
		catch(Exception ex)
		{
			if (createdTrx)
			{
				Trx trx = Trx.get(trxName, false);
				if (trx != null && trx.isActive())
					trx.rollback();
			}
			
			log.severe(ex.getMessage());
		}
		finally
		{
			if (createdTrx)
			{
				Trx trx = Trx.get(trxName, false);
				if (trx != null && trx.isActive())
					trx.close();
			}
		}
					
		return null;
	}
	
	public static MProduct createCallProduct(Properties ctx, HashMap<Integer, Object> attributes, String trxName)
	{
		// Load or create new trx
		boolean createdTrx = false;		
		if (trxName == null || trxName.length() < 1)
		{
			trxName = Trx.createTrxName("createCallingProduct");
			createdTrx = true;
		}

		try
		{
			// Validate attributes
			if (!Validation.validateAttributes(ctx, Integer.parseInt(DIDConstants.CDR_ATTRIBUTE_SET_ID), attributes))
				throw new Exception("Failed to validate attributes");
			
			// Load param values
			String number = (String)attributes.get(DIDConstants.ATTRIBUTE_ID_CDR_NUMBER);
			Integer direction = (Integer)attributes.get(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION);
			
			// Inbound product fields
			String searchKey = DIDConstants.CALL_IN_PRODUCT_SEARCH_KEY;
			String name = DIDConstants.CALL_IN_PRODUCT_NAME;
			String description = DIDConstants.CALL_IN_PRODUCT_DESCRIPTION;
			
			// Outbound product fields
			if (direction == DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_OUTBOUND)
			{
				searchKey = DIDConstants.CALL_OUT_PRODUCT_SEARCH_KEY;
				name = DIDConstants.CALL_OUT_PRODUCT_NAME;
				description = DIDConstants.CALL_OUT_PRODUCT_DESCRIPTION;
			}
			
			searchKey = searchKey.replace(DIDConstants.NUMBER_IDENTIFIER, number);
			name = name.replace(DIDConstants.NUMBER_IDENTIFIER, number);
			description = description.replace(DIDConstants.NUMBER_IDENTIFIER, number);
			
			HashMap<String, Object> fields = new HashMap<String, Object>();
			fields.put(MProduct.COLUMNNAME_Value, searchKey);
			fields.put(MProduct.COLUMNNAME_Name, name);
			fields.put(MProduct.COLUMNNAME_Description, description);
			fields.put(MProduct.COLUMNNAME_M_Product_Category_ID, DIDConstants.VOICE_SERVICES_RECUR_CALL_CATEGORY_ID);
			fields.put(MProduct.COLUMNNAME_C_TaxCategory_ID, DIDConstants.STANDARD_15_TAX_CATEGORY); 
			fields.put(MProduct.COLUMNNAME_C_UOM_ID, DIDConstants.UOM_EACH); 
			fields.put(MProduct.COLUMNNAME_M_AttributeSet_ID, DIDConstants.CDR_ATTRIBUTE_SET_ID);
			fields.put(MProduct.COLUMNNAME_ProductType, DIDConstants.PRODUCT_TYPE_SERVICE);
			fields.put(MProduct.COLUMNNAME_IsSelfService, DIDConstants.NOT_SELF_SERVICE);
					
			MProduct product = createProduct(ctx, fields, trxName);
			if (product == null)
				throw new Exception("Failed to create MProduct[" + searchKey + "]");
			
			// Create new attribute set instance
			MAttributeSetInstance masi = new MAttributeSetInstance(ctx, 0, trxName);
			masi.setM_AttributeSet_ID(Integer.parseInt(DIDConstants.CDR_ATTRIBUTE_SET_ID));
			if (!masi.save())
				throw new Exception("Failed to create MAttributeSetInstance for " + product);
			
			// Save new attribute set instance id to product
			product.setM_AttributeSetInstance_ID(masi.getM_AttributeSetInstance_ID());
			if (!product.save())
				throw new Exception("Failed to save MAttributeSetInstance for " + product);
			
			// Create attribute instances
			Iterator<Integer> iterator = attributes.keySet().iterator();
			while(iterator.hasNext())
			{
				Integer attributeId = (Integer)iterator.next();
				if (attributes.get(attributeId) instanceof String)
				{
					String attributeValue = (String)attributes.get(attributeId);
					
					MAttributeInstance attributeInstance = new MAttributeInstance(ctx, attributeId, masi.getM_AttributeSetInstance_ID(), attributeValue, trxName);
					if (!attributeInstance.save())
						throw new Exception("Failed to save MAttributeInstance[" + attributeId + "-" + attributeValue + "]  for " + product);
				}
				else if (attributes.get(attributeId) instanceof Integer)
				{
					Integer attributeValueId = (Integer)attributes.get(attributeId);
					MAttributeValue attributeValue = new MAttributeValue(ctx, attributeValueId, trxName);
					
					MAttributeInstance attributeInstance = new MAttributeInstance(ctx, attributeId, masi.getM_AttributeSetInstance_ID(), attributeValueId, attributeValue.getName(), trxName);
					if (!attributeInstance.save())
						throw new Exception("Failed to save MAttributeInstance[" + attributeId + "-" + attributeValueId + "]  for " + product);
				}
			}
			
			// Update MAttributeSetInstance description (attribute values seperated with _, don't need to worry if it fails)
			masi.setDescription();
			if (!masi.save())
				log.warning("Failed to update " + masi + "'s description");
			
			// If created trx in this method try to load then commit trx
			if (createdTrx)
			{
				Trx trx = Trx.get(trxName, false);
				if (trx == null)
					throw new Exception("Failed to load trx");
				else if (!trx.isActive())
					throw new Exception("Trx no longer active");
				else if (!trx.commit())
					throw new Exception("Failed to commit trx");
			}
			
			// To reset trxName
//			product.load(trxName);
			
			return product;
		}
		catch(Exception ex)
		{
			if (createdTrx)
			{
				Trx trx = Trx.get(trxName, false);
				if (trx != null && trx.isActive())
					trx.rollback();
			}
			
			log.severe(ex.getMessage());
		}
		finally
		{
			if (createdTrx)
			{
				Trx trx = Trx.get(trxName, false);
				if (trx != null && trx.isActive())
					trx.close();
			}
		}
					
		return null;
	}
	
	public static MProduct createSIPProduct(Properties ctx, HashMap<Integer, Object> attributes, String trxName)
	{
		// Load or create new trx
		boolean createdTrx = false;		
		if (trxName == null || trxName.length() < 1)
		{
			trxName = Trx.createTrxName("createSIPProduct");
			createdTrx = true;
		}

		try
		{		
			// Validate attributes
			if (!Validation.validateAttributes(ctx, Integer.parseInt(DIDConstants.SIP_ATTRIBUTE_SET_ID), attributes))
				throw new Exception("Failed to validate attributes");
			
			// Load attribute values
			String sipAddress = (String)attributes.get(DIDConstants.ATTRIBUTE_ID_SIP_ADDRESS);
			
			String searchKey = DIDConstants.SIP_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, sipAddress);
			String name = DIDConstants.SIP_PRODUCT_NAME.replace(DIDConstants.NUMBER_IDENTIFIER, sipAddress);
			String description = DIDConstants.SIP_PRODUCT_DESCRIPTION.replace(DIDConstants.NUMBER_IDENTIFIER, sipAddress);
			
			HashMap<String, Object> fields = new HashMap<String, Object>();
			fields.put(MProduct.COLUMNNAME_Value, searchKey);
			fields.put(MProduct.COLUMNNAME_Name, name);
			fields.put(MProduct.COLUMNNAME_Description, description);	
			fields.put(MProduct.COLUMNNAME_M_Product_Category_ID, DIDConstants.VOICE_SERVICES_CATEGORY_ID);
			fields.put(MProduct.COLUMNNAME_C_TaxCategory_ID, DIDConstants.STANDARD_15_TAX_CATEGORY); 
			fields.put(MProduct.COLUMNNAME_C_UOM_ID, DIDConstants.UOM_MONTH_8DEC); 	
			fields.put(MProduct.COLUMNNAME_M_AttributeSet_ID, DIDConstants.SIP_ATTRIBUTE_SET_ID); 
			fields.put(MProduct.COLUMNNAME_ProductType, DIDConstants.PRODUCT_TYPE_SERVICE); 
			fields.put(MProduct.COLUMNNAME_IsSelfService, DIDConstants.NOT_SELF_SERVICE); 
			
			MProduct product = createProduct(ctx, fields, trxName);
			if (product == null)	
				throw new Exception("Failed to create MProduct[" + searchKey + "]");
			
			// Create new attribute set instance
			MAttributeSetInstance masi = new MAttributeSetInstance(ctx, 0, trxName);
			masi.setM_AttributeSet_ID(Integer.parseInt(DIDConstants.SIP_ATTRIBUTE_SET_ID));
			if (!masi.save())
				throw new Exception("Failed to create MAttributeSetInstance for " + product);
			
			// Save new attribute set instance id to product
			product.setM_AttributeSetInstance_ID(masi.getM_AttributeSetInstance_ID());
			if (!product.save())
				throw new Exception("Failed to save MAttributeSetInstance for " + product);
			
			// Create attribute instances
			Iterator<Integer> iterator = attributes.keySet().iterator();
			while(iterator.hasNext())
			{
				Integer attributeId = (Integer)iterator.next();
				String attributeValue = (String)attributes.get(attributeId);
				
				MAttributeInstance attributeInstance = new MAttributeInstance(ctx, attributeId, masi.getM_AttributeSetInstance_ID(), attributeValue, trxName);
				if (!attributeInstance.save())
					throw new Exception("Failed to save MAttributeInstance[" + attributeId + "-" + attributeValue + "]  for " + product);
			}
			
			// Update MAttributeSetInstance description (attribute values seperated with _ , don't need to worry if it fails)
			masi.setDescription();
			if (!masi.save())
				log.warning("Failed to update " + masi + "'s description");
			
			// If created trx in this method try to load then commit trx
			if (createdTrx)
			{
				Trx trx = Trx.get(trxName, false);
				if (trx == null)
					throw new Exception("Failed to load trx");
				else if (!trx.isActive())
					throw new Exception("Trx no longer active");
				else if (!trx.commit())
					throw new Exception("Failed to commit trx");
			}
			
			// To reset trxName
			product.load(trxName);
			
			return product;		
		}
		catch(Exception ex)
		{
			if (createdTrx)
			{
				Trx trx = Trx.get(trxName, false);
				if (trx != null && trx.isActive())
					trx.rollback();
			}
			
			log.severe(ex.getMessage());		
		}
		finally
		{
			if (createdTrx)
			{
				Trx trx = Trx.get(trxName, false);
				if (trx != null && trx.isActive())
					trx.close();
			}
		}
		
		return null;
	}
	
	public static MProduct createVoicemailProduct(Properties ctx, HashMap<Integer, Object> attributes, String trxName)
	{
		// Load or create new trx
		boolean createdTrx = false;		
		if (trxName == null || trxName.length() < 1)
		{
			trxName = Trx.createTrxName("createVoicemailProduct");
			createdTrx = true;
		}

		try
		{		
			// Validate attributes
			if (!Validation.validateAttributes(ctx, Integer.parseInt(DIDConstants.VOICEMAIL_ATTRIBUTE_SET_ID), attributes))
				throw new Exception("Failed to validate attributes");
			
			// Load attribute values
			String mailboxNumber = (String)attributes.get(DIDConstants.ATTRIBUTE_ID_VM_MAILBOX_NUMBER);
			
			String searchKey = DIDConstants.VOICEMAIL_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, mailboxNumber);
			String name = DIDConstants.VOICEMAIL_PRODUCT_NAME.replace(DIDConstants.NUMBER_IDENTIFIER, mailboxNumber);
			String description = DIDConstants.VOICEMAIL_PRODUCT_DESC.replace(DIDConstants.NUMBER_IDENTIFIER, mailboxNumber);
			
			HashMap<String, Object> fields = new HashMap<String, Object>();		
			fields.put(MProduct.COLUMNNAME_Value, searchKey);
			fields.put(MProduct.COLUMNNAME_Name, name);
			fields.put(MProduct.COLUMNNAME_Description, description);	
			fields.put(MProduct.COLUMNNAME_C_UOM_ID, DIDConstants.UOM_MONTH_8DEC); 		
			fields.put(MProduct.COLUMNNAME_M_AttributeSet_ID, DIDConstants.VOICEMAIL_ATTRIBUTE_SET_ID); 
			fields.put(MProduct.COLUMNNAME_M_Product_Category_ID, DIDConstants.VOICE_SERVICES_CATEGORY_ID);
			fields.put(MProduct.COLUMNNAME_C_TaxCategory_ID, DIDConstants.STANDARD_15_TAX_CATEGORY); 
			fields.put(MProduct.COLUMNNAME_ProductType, DIDConstants.PRODUCT_TYPE_SERVICE); 
			fields.put(MProduct.COLUMNNAME_IsSelfService, DIDConstants.NOT_SELF_SERVICE); 

			MProduct product = createProduct(ctx, fields, trxName);
			if (product == null)	
				throw new Exception("Failed to create MProduct[" + searchKey + "]");
			
			// Create new attribute set instance
			MAttributeSetInstance masi = new MAttributeSetInstance(ctx, 0, trxName);
			masi.setM_AttributeSet_ID(Integer.parseInt(DIDConstants.VOICEMAIL_ATTRIBUTE_SET_ID));
			if (!masi.save())
				throw new Exception("Failed to create MAttributeSetInstance for " + product);
			
			// Save new attribute set instance id to product
			product.setM_AttributeSetInstance_ID(masi.getM_AttributeSetInstance_ID());
			if (!product.save())
				throw new Exception("Failed to save MAttributeSetInstance for " + product);
			
			// Create attribute instances
			Iterator<Integer> iterator = attributes.keySet().iterator();
			while(iterator.hasNext())
			{
				Integer attributeId = (Integer)iterator.next();
				String attributeValue = (String)attributes.get(attributeId);
				
				MAttributeInstance attributeInstance = new MAttributeInstance(ctx, attributeId, masi.getM_AttributeSetInstance_ID(), attributeValue, trxName);
				if (!attributeInstance.save())
					throw new Exception("Failed to save MAttributeInstance[" + attributeId + "-" + attributeValue + "]  for " + product);
			}
			
			// Update MAttributeSetInstance description (attribute values seperated with _ , don't need to worry if it fails)
			masi.setDescription();
			if (!masi.save())
				log.warning("Failed to update " + masi + "'s description");
			
			// If created trx in this method try to load then commit trx
			if (createdTrx)
			{
				Trx trx = Trx.get(trxName, false);
				if (trx == null)
					throw new Exception("Failed to load trx");
				else if (!trx.isActive())
					throw new Exception("Trx no longer active");
				else if (!trx.commit())
					throw new Exception("Failed to commit trx");
			}
			
			// To reset trxName
			product.load(trxName);
			
			return product;		
		}
		catch(Exception ex)
		{
			if (createdTrx)
			{
				Trx trx = Trx.get(trxName, false);
				if (trx != null && trx.isActive())
					trx.rollback();
			}
			
			log.severe(ex.getMessage());		
		}
		finally
		{
			if (createdTrx)
			{
				Trx trx = Trx.get(trxName, false);
				if (trx != null && trx.isActive())
					trx.close();
			}
		}
		
		return null;
	}
	
	// TODO: Dynamically set values (if statements?)
	// TODO: Alter Constants to int from String (change DIDValidation.validateMandatoryFields() to handle Integer too)
	public static MProduct createProduct(Properties ctx, HashMap<String, Object> fields, String trxName)
	{	
		MProduct product = new MProduct(ctx, 0, trxName);
		
		if (Validation.validateMandatoryFields(product, fields))
		{
			try
			{
				product.setValue((String)fields.get("Value"));
				product.setName((String)fields.get("Name"));
				product.setDescription((String)fields.get("Description"));
				product.setM_Product_Category_ID(Integer.parseInt((String)fields.get("M_Product_Category_ID")));
				product.setC_TaxCategory_ID(Integer.parseInt((String)fields.get("C_TaxCategory_ID")));
				product.setC_UOM_ID(Integer.parseInt((String)fields.get("C_UOM_ID")));				
				product.setProductType((String)fields.get("ProductType"));
				product.setIsSelfService(fields.get("IsSelfService").equals("Y"));
				
				if (fields.get("M_AttributeSet_ID") != null && fields.get("M_AttributeSet_ID") instanceof String)
					product.setM_AttributeSet_ID(Integer.parseInt((String)fields.get("M_AttributeSet_ID")));
				
				// Save product
				if (product.save())
					return product;
			}
			catch (Exception ex)
			{
				log.severe("Failed to create MProduct - Raised Exception: " + ex);
			}
		}
		else
			log.severe("Failed to create MProduct - Please set all mandatory fields with valid values");

		return null;
	}
	
	public static MSubscription createDIDSetupSubscription(Properties ctx, String number, int C_BPartner_ID, int C_BPartner_Location_ID, int M_Product_ID, Timestamp startDate,Timestamp paidUntilDate, String trxName)
	{
		HashMap<String, Object> fields = new HashMap<String, Object>();
		
		// Create name
		String name = DIDConstants.DID_SUBSCRIPTION_NAME.replace(DIDConstants.NUMBER_IDENTIFIER, number);
		
		// Get dates
		HashMap<String, Timestamp> dates = getSubscriptionDates(true, null);
		
		// Mandatory
		fields.put(MSubscription.COLUMNNAME_Name, name);
		fields.put(MSubscription.COLUMNNAME_C_BPartner_ID, C_BPartner_ID); 
		fields.put(MBPartnerLocation.COLUMNNAME_C_BPartner_Location_ID, C_BPartner_Location_ID);
		fields.put(MSubscription.COLUMNNAME_M_Product_ID, M_Product_ID);
		fields.put(MSubscription.COLUMNNAME_C_SubscriptionType_ID, DIDConstants.C_SUBSCRIPTIONTYPE_ID_ONE_OFF); 		
		fields.put(MSubscription.COLUMNNAME_StartDate, startDate != null ? startDate : dates.get(MSubscription.COLUMNNAME_StartDate));
		fields.put(MSubscription.COLUMNNAME_PaidUntilDate, paidUntilDate != null ? paidUntilDate: dates.get(MSubscription.COLUMNNAME_PaidUntilDate)); 
		fields.put(MSubscription.COLUMNNAME_RenewalDate, dates.get(MSubscription.COLUMNNAME_RenewalDate)); 
		fields.put(MSubscription.COLUMNNAME_IsDue, false); 
		fields.put(MSubscription.COLUMNNAME_BillInAdvance, false);
		
		return createSubscription(ctx, fields, trxName);
	}
	
	public static MSubscription createDIDMonthlySubscription(Properties ctx, String number, int C_BPartner_ID, int C_BPartner_Location_ID, int M_Product_ID, Timestamp startDate, String trxName)
	{
		HashMap<String, Object> fields = new HashMap<String, Object>();
		
		// Create name
		String name = DIDConstants.DID_SUBSCRIPTION_NAME.replace(DIDConstants.NUMBER_IDENTIFIER, number);
		
		// Get dates
		HashMap<String, Timestamp> dates = getSubscriptionDates(false, null);
		
		// Mandatory
		fields.put(MSubscription.COLUMNNAME_Name, name);
		fields.put(MSubscription.COLUMNNAME_C_BPartner_ID, C_BPartner_ID); 
		fields.put(MBPartnerLocation.COLUMNNAME_C_BPartner_Location_ID, C_BPartner_Location_ID);
		fields.put(MSubscription.COLUMNNAME_M_Product_ID, M_Product_ID);
		fields.put(MSubscription.COLUMNNAME_C_SubscriptionType_ID, DIDConstants.C_SUBSCRIPTIONTYPE_ID_MONTH_1); 		
		fields.put(MSubscription.COLUMNNAME_StartDate, startDate != null ? startDate : dates.get(MSubscription.COLUMNNAME_StartDate));
		fields.put(MSubscription.COLUMNNAME_PaidUntilDate, dates.get(MSubscription.COLUMNNAME_PaidUntilDate)); 
		fields.put(MSubscription.COLUMNNAME_RenewalDate, dates.get(MSubscription.COLUMNNAME_RenewalDate)); 
		fields.put(MSubscription.COLUMNNAME_IsDue, false); 
		fields.put(MSubscription.COLUMNNAME_BillInAdvance, true);
		
		return createSubscription(ctx, fields, trxName);
	}
	
	public static MSubscription createCallSubscription(Properties ctx, String number, int C_BPartner_ID, int C_BPartner_Location_ID, int M_Product_ID, String trxName)
	{
		return createCallSubscription(ctx, number, getSubscriptionDates(false, null), C_BPartner_ID, C_BPartner_Location_ID, M_Product_ID, trxName);
	}
	
	public static MSubscription createCallSubscription(Properties ctx, String number, HashMap<String, Timestamp> dates, int C_BPartner_ID, int C_BPartner_Location_ID, int M_Product_ID, String trxName)
	{
		HashMap<String, Object> fields = new HashMap<String, Object>();
		
		// Create name
		String name = DIDConstants.CALL_SUBSCRIPTION_NAME.replace(DIDConstants.NUMBER_IDENTIFIER, number);
		
		// Mandatory
		fields.put(MSubscription.COLUMNNAME_Name, name);
		fields.put(MSubscription.COLUMNNAME_C_BPartner_ID, C_BPartner_ID); 
		fields.put(MBPartnerLocation.COLUMNNAME_C_BPartner_Location_ID, C_BPartner_Location_ID);
		fields.put(MSubscription.COLUMNNAME_M_Product_ID, M_Product_ID);
		fields.put(MSubscription.COLUMNNAME_C_SubscriptionType_ID, DIDConstants.C_SUBSCRIPTIONTYPE_ID_MONTHLY_CALLS); 		
		fields.put(MSubscription.COLUMNNAME_StartDate, dates.get(MSubscription.COLUMNNAME_StartDate));
		fields.put(MSubscription.COLUMNNAME_PaidUntilDate, dates.get(MSubscription.COLUMNNAME_PaidUntilDate)); 
		fields.put(MSubscription.COLUMNNAME_RenewalDate, dates.get(MSubscription.COLUMNNAME_RenewalDate)); 
		fields.put(MSubscription.COLUMNNAME_IsDue, false); 
		fields.put(MSubscription.COLUMNNAME_BillInAdvance, false);
		
		return createSubscription(ctx, fields, trxName);
	}
	
	public static MSubscription createSIPSubscription(Properties ctx, String sipAddress, String sipDomain, int C_BPartner_ID, int C_BPartner_Location_ID, int M_Product_ID, Timestamp startDate, String trxName)
	{
		HashMap<String, Object> fields = new HashMap<String, Object>();
		
		// Create name
		String name = DIDConstants.SIP_SUBSCRIPTION_NAME.replace(DIDConstants.ADDRESS_IDENTIFIER, sipAddress).replace(DIDConstants.DOMAIN_IDENTIFIER, sipDomain);
		
		// Get dates
		HashMap<String, Timestamp> dates = getSubscriptionDates(false, null);
		
		// Mandatory
		fields.put(MSubscription.COLUMNNAME_Name, name);
		fields.put(MSubscription.COLUMNNAME_C_BPartner_ID, C_BPartner_ID); 
		fields.put(MBPartnerLocation.COLUMNNAME_C_BPartner_Location_ID, C_BPartner_Location_ID);
		fields.put(MSubscription.COLUMNNAME_M_Product_ID, M_Product_ID);
		fields.put(MSubscription.COLUMNNAME_C_SubscriptionType_ID, DIDConstants.C_SUBSCRIPTIONTYPE_ID_MONTH_1); 		
		fields.put(MSubscription.COLUMNNAME_StartDate, startDate != null ? startDate : dates.get(MSubscription.COLUMNNAME_StartDate));
		fields.put(MSubscription.COLUMNNAME_PaidUntilDate, dates.get(MSubscription.COLUMNNAME_PaidUntilDate)); 
		fields.put(MSubscription.COLUMNNAME_RenewalDate, dates.get(MSubscription.COLUMNNAME_RenewalDate)); 
		fields.put(MSubscription.COLUMNNAME_IsDue, false); 
		fields.put(MSubscription.COLUMNNAME_BillInAdvance, true);
		
		return createSubscription(ctx, fields, trxName);
	}
	
	public static MSubscription createVoicemailSubscription(Properties ctx, String mailboxNumber, int C_BPartner_ID, int C_BPartner_Location_ID, int M_Product_ID, Timestamp startDate, String trxName)
	{
		HashMap<String, Object> fields = new HashMap<String, Object>();
		
		// Create name
		String name = DIDConstants.VOICEMAIL_SUBSCRIPTION_NAME.replace(DIDConstants.NUMBER_IDENTIFIER, mailboxNumber);
		
		// Get dates
		HashMap<String, Timestamp> dates = getSubscriptionDates(false, null);
		
		// Mandatory
		fields.put(MSubscription.COLUMNNAME_Name, name);
		fields.put(MSubscription.COLUMNNAME_C_BPartner_ID, C_BPartner_ID); 
		fields.put(MBPartnerLocation.COLUMNNAME_C_BPartner_Location_ID, C_BPartner_Location_ID);
		fields.put(MSubscription.COLUMNNAME_M_Product_ID, M_Product_ID);
		fields.put(MSubscription.COLUMNNAME_C_SubscriptionType_ID, DIDConstants.C_SUBSCRIPTIONTYPE_ID_MONTH_1); 		
		fields.put(MSubscription.COLUMNNAME_StartDate, startDate != null ? startDate : dates.get(MSubscription.COLUMNNAME_StartDate));
		fields.put(MSubscription.COLUMNNAME_PaidUntilDate, dates.get(MSubscription.COLUMNNAME_PaidUntilDate)); 
		fields.put(MSubscription.COLUMNNAME_RenewalDate, dates.get(MSubscription.COLUMNNAME_RenewalDate)); 
		fields.put(MSubscription.COLUMNNAME_IsDue, false); 
		fields.put(MSubscription.COLUMNNAME_BillInAdvance, true);
		
		return createSubscription(ctx, fields, trxName);
	}
	
	public static MSubscription createNumberPortSubscription(Properties ctx, String number, int C_BPartner_ID, int C_BPartner_Location_ID, int M_Product_ID, String trxName)
	{
		return createNumberPortSubscription(ctx, number, getSubscriptionDates(true, null), C_BPartner_ID, C_BPartner_Location_ID, M_Product_ID, trxName);
	}
	
	public static MSubscription createNumberPortSubscription(Properties ctx, String number, HashMap<String, Timestamp> dates, int C_BPartner_ID, int C_BPartner_Location_ID, int M_Product_ID, String trxName)
	{
		HashMap<String, Object> fields = new HashMap<String, Object>();
		
		// Create name
		String name = DIDConstants.NUMBER_PORT_SUBSCRIPTION_NAME.replace(DIDConstants.NUMBER_IDENTIFIER, number);
		
		// Mandatory
		fields.put(MSubscription.COLUMNNAME_Name, name);
		fields.put(MSubscription.COLUMNNAME_C_BPartner_ID, C_BPartner_ID); 
		fields.put(MBPartnerLocation.COLUMNNAME_C_BPartner_Location_ID, C_BPartner_Location_ID);
		fields.put(MSubscription.COLUMNNAME_M_Product_ID, M_Product_ID);
		fields.put(MSubscription.COLUMNNAME_C_SubscriptionType_ID, DIDConstants.C_SUBSCRIPTIONTYPE_ID_ONE_OFF); 		
		fields.put(MSubscription.COLUMNNAME_StartDate, dates.get(MSubscription.COLUMNNAME_StartDate));
		fields.put(MSubscription.COLUMNNAME_PaidUntilDate, dates.get(MSubscription.COLUMNNAME_PaidUntilDate)); 
		fields.put(MSubscription.COLUMNNAME_RenewalDate, dates.get(MSubscription.COLUMNNAME_RenewalDate)); 
		fields.put(MSubscription.COLUMNNAME_IsDue, false); 
		fields.put(MSubscription.COLUMNNAME_BillInAdvance, false);
		
		return createSubscription(ctx, fields, trxName);
	}
	
	public static MSubscription createSubscription(Properties ctx, HashMap<String, Object> fields, String trxName)
	{ 
		MSubscription subscription = new MSubscription(ctx, 0, trxName);
		
		if (Validation.validateMandatoryFields(subscription, fields))
		{
			try
			{
				// Mandatory
				subscription.setName((String)fields.get(MSubscription.COLUMNNAME_Name));
				subscription.setC_BPartner_ID((Integer)fields.get(MSubscription.COLUMNNAME_C_BPartner_ID));				
				subscription.setM_Product_ID((Integer)fields.get(MSubscription.COLUMNNAME_M_Product_ID));
				subscription.setC_SubscriptionType_ID((Integer)fields.get(MSubscription.COLUMNNAME_C_SubscriptionType_ID));
				subscription.setStartDate((Timestamp)fields.get(MSubscription.COLUMNNAME_StartDate));
				subscription.setPaidUntilDate((Timestamp)fields.get(MSubscription.COLUMNNAME_PaidUntilDate));
				subscription.setRenewalDate((Timestamp)fields.get(MSubscription.COLUMNNAME_RenewalDate));
				subscription.setIsDue((Boolean)fields.get(MSubscription.COLUMNNAME_IsDue));	
	
				// Optional
				if (fields.get(MBPartnerLocation.COLUMNNAME_C_BPartner_Location_ID) != null && fields.get(MBPartnerLocation.COLUMNNAME_C_BPartner_Location_ID) instanceof Integer)
					subscription.set_ValueOfColumn(MBPartnerLocation.COLUMNNAME_C_BPartner_Location_ID, (Integer)fields.get(MBPartnerLocation.COLUMNNAME_C_BPartner_Location_ID));
				
				if (fields.get(MSubscription.COLUMNNAME_BillInAdvance) != null && fields.get(MSubscription.COLUMNNAME_BillInAdvance) instanceof Boolean)
					subscription.setBillInAdvance((Boolean)fields.get(MSubscription.COLUMNNAME_BillInAdvance));
				
				// Save subscription
				if (subscription.save())
					return subscription;			
			}
			catch (Exception ex)
			{
				log.severe("Failed to create MSubscription - Raised Exception: " + ex);
			}
		}
		else 
			log.severe("Failed to create MSubscription - Please set all mandatory fields with valid values");
		
		return null;
	}
	
// *****************************************************************************************************************************************	

	public static boolean updateAttributes(Properties ctx, int M_AttributeSetInstance_ID, HashMap<Integer, String> attributePairs, String trxName)
	{
		if (M_AttributeSetInstance_ID == 0)
		{
			log.severe("Cannot update attributes for MAttributeSetInstance[0]");
			return false;
		}
		
		boolean localTrx = false;
		if (trxName == null)
		{
			localTrx = true;
			trxName = Trx.createTrxName("updateAttributes");
		}
		
		try
		{
			boolean failure = false;
			boolean updated = false;
			Iterator<Integer> iterator = attributePairs.keySet().iterator();
			while(iterator.hasNext() && !failure)
			{
				Integer attributeId = (Integer)iterator.next();
				String attributeValue = attributePairs.get(attributeId);
				
				if (attributeId != null && attributeId > 0 && attributeValue != null && attributeValue.length() > 0)
				{			
					MAttributeInstance attributeInstance = getAttributeInstance(ctx, attributeId, M_AttributeSetInstance_ID, trxName);
					if (attributeInstance != null)
					{
						attributeInstance.setValue(attributeValue);
						if (!attributeInstance.save())
							throw new Exception("Failed to save MAttributeInstance for MAttribute[" + attributeId + "] MAttributeSetInstance[" + M_AttributeSetInstance_ID + "]");
						else
							updated = true;
					}		
					else
						throw new Exception("Failed to update because MAttributeInstance doesn't exist for MAttribute[" + attributeId + "] MAttributeSetInstance[" + M_AttributeSetInstance_ID + "]");
				}
				else
					throw new Exception("Invalid attribute id and/or value");
			}
			
			// Update description
			if (updated)
			{
				MAttributeSetInstance masi = new MAttributeSetInstance(ctx, M_AttributeSetInstance_ID, trxName);
				if (masi == null)
					log.severe("Failed to load MAttributeSetInstance[" + M_AttributeSetInstance_ID + "] when updating description"); // updating not important enought to rollback trx
				
				masi.setDescription();
				
				if (!masi.save())
					log.severe("Failed to save MAttributeSetInstance[" + M_AttributeSetInstance_ID + "] when updating description");
			}
			
			// Commit local trx if needed
			if (localTrx)
			{
				Trx trx = null;
				try
				{
					trx = Trx.get(trxName, false);	
					if (trx != null)
					{
						if (trx.commit())
						{
							return true;
						}
						else
						{
							trx.rollback();
							log.severe("Failed to commit trx for MAttributeSetInstance[" + M_AttributeSetInstance_ID + "]'s attribute instances");
						}
					}
				}
				catch (Exception ex)
				{
					// Catches Trx.get() IllegalArgumentExceptions
				}
				finally
				{
					if (trx != null)
						trx.close();
				}
			}
			
			return true;
		}
		catch (Exception ex)
		{
			log.severe(ex.getMessage());
		}
	
		return false;
	}
	
// *****************************************************************************************************************************************	
	
	public static MProduct[] getBySubscription(Properties ctx, boolean subscribed, String trxName)
	{
		return getProducts(ctx, DIDConstants.ATTRIBUTE_ID_DID_SUBSCRIBED, Boolean.toString(subscribed), trxName);
	}
	
	public static MProduct[] getAllDIDProducts(Properties ctx, String trxName)
	{
		return getProducts(ctx, DIDConstants.ATTRIBUTE_ID_DID_NUMBER, "%", trxName);
	}
	
	public static MProduct[] getDIDProducts(Properties ctx, String didNumber, String trxName)
	{
		return getProducts(ctx, DIDConstants.ATTRIBUTE_ID_DID_NUMBER, didNumber, trxName);
	}
	
	public static MProduct[] getSIPProducts(Properties ctx, String address, String trxName)
	{
		return getProducts(ctx, DIDConstants.ATTRIBUTE_ID_SIP_ADDRESS, address, trxName);
	}
	
	public static MProduct[] getSIPProducts(Properties ctx, String address, String domain, String trxName)
	{
		return getProducts(ctx, new int[]{DIDConstants.ATTRIBUTE_ID_SIP_ADDRESS, DIDConstants.ATTRIBUTE_ID_SIP_DOMAIN}, new String[]{address, domain}, trxName);
	}
	
	public static MProduct[] getVoicemailProducts(Properties ctx, String mailboxNumber, String trxName)
	{
		return getProducts(ctx, DIDConstants.ATTRIBUTE_ID_VM_MAILBOX_NUMBER, mailboxNumber, trxName);
	}
	
	public static MProduct[] getAllCallProducts(Properties ctx, String trxName)
	{
		return getProducts(ctx, DIDConstants.ATTRIBUTE_ID_CDR_NUMBER, "%", trxName);
	}
	
	public static MProduct[] getCallProducts(Properties ctx, String number, String trxName)
	{
		return getProducts(ctx, DIDConstants.ATTRIBUTE_ID_CDR_NUMBER, number, trxName);
	}
	
	public static MProduct[] getCallProductsByUsername(Properties ctx, String username, String trxName)
	{
		return getProducts(ctx, DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, username, trxName);
	}
	
	public static MProduct[] getProducts(Properties ctx, int M_Attribute_ID, String value, String trxName)
	{
		MProduct[] products = MProduct.get(ctx, 
				MProduct.COLUMNNAME_M_Product_ID + " IN" + 
				"(" +
					"SELECT " + MProduct.COLUMNNAME_M_Product_ID +
					" FROM " + MAttributeInstance.Table_Name + " ai, " + MProduct.Table_Name + " p" +
					" WHERE " +
						"ai." + MAttributeInstance.COLUMNNAME_M_AttributeSetInstance_ID + " = p." + MProduct.COLUMNNAME_M_AttributeSetInstance_ID +
					" AND " +
						"ai." + MAttributeInstance.COLUMNNAME_M_Attribute_ID + " = " + M_Attribute_ID +
					" AND " +
						"UPPER(ai." + MAttributeInstance.COLUMNNAME_Value + ") LIKE UPPER('" + value + "')" +
				")" +
			" AND UPPER(IsActive) = 'Y'", trxName);
		
		return products;
	}
	
	public static MProduct[] getProducts(Properties ctx, int[] attributeIds, String[] attributeValues, String trxName)
	{	
		String whereClause = 
			MProduct.COLUMNNAME_M_Product_ID + " IN" + 
			"(" +
				" SELECT " + MProduct.COLUMNNAME_M_Product_ID +
				" FROM " + MAttributeSetInstance.Table_Name + " asi, " + MProduct.Table_Name + " p" +
				" WHERE " +
					"asi." + MAttributeSetInstance.COLUMNNAME_M_AttributeSetInstance_ID + " = p." + MProduct.COLUMNNAME_M_AttributeSetInstance_ID +
				" AND " +
					"asi." + MAttributeSetInstance.COLUMNNAME_M_AttributeSetInstance_ID + " IN" + 
					"(";
		
		for (int i=0; i<attributeIds.length; i++)
		{
			whereClause += "SELECT M_AttributeSetInstance_ID FROM M_AttributeInstance ";
			
			// if last id
			if (i+1 >= attributeIds.length)
				whereClause += "WHERE M_Attribute_ID = " + attributeIds[i] + " AND Value = '" + attributeValues[i] + "'" ;			
			else
				whereClause += "WHERE M_AttributeSetInstance_ID IN (";
		}
		
		whereClause += ")";
		
		for (int i=0; i<attributeIds.length-1; i++)
		{
			whereClause += " AND M_Attribute_ID = " + attributeIds[i] + " AND Value = '" + attributeValues[i] + "')" ;
		}
		
		whereClause += ")  AND UPPER(IsActive) = 'Y'";
		
		MProduct[] products = MProduct.get(ctx, whereClause, trxName);
		return products;
	}
	
// *****************************************************************************************************************************************
	
	public static boolean isMSubscribed(Properties ctx, MProduct product, String trxName)
	{
		MSubscription[] subscriptions = MSubscription.getSubscriptions(ctx, product.getM_Product_ID(), trxName);

		for (MSubscription subscription : subscriptions)
		{						
			if (isActiveMSubscription(ctx, subscription))
				return true;
		}
		
		return false;
	}
	
	public static boolean isMSubscribed(Properties ctx, MProduct product, int C_BPartner_ID, String trxName)
	{
		MSubscription[] subscriptions = MSubscription.getSubscriptions(ctx, product.getM_Product_ID(), trxName);

		for (MSubscription subscription : subscriptions)
		{				
			if (subscription.getC_BPartner_ID() != C_BPartner_ID)
				continue;
			
			if (isActiveMSubscription(ctx, subscription))
				return true;
		}
		
		return false;
	}
	
	public static boolean isActiveMSubscription(Properties ctx, MSubscription subscription)
	{
		// Get current date without time
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Timestamp currentDate = new Timestamp(calendar.getTimeInMillis());
		
		// Check if current date is equal to or after start date
		// Check if current date is before or equal to renewal date
		if ((currentDate.compareTo(subscription.getStartDate()) >= 0) && (currentDate.compareTo(subscription.getRenewalDate()) <= 0))
			return true;
		
		return false;
	}
	
	public static boolean isSubscribed(Properties ctx, MProduct product, String trxName)
	{
		String subscribed = getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_DID_SUBSCRIBED, product.getM_AttributeSetInstance_ID(), trxName);
		
		if (subscribed != null)
			return !subscribed.equalsIgnoreCase("false"); // unless explicitly false treat as subscribed
		
		return true;
	}
	
	public static boolean isSetup(Properties ctx, MProduct product, String trxName)
	{
		String isSetup = getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_DID_ISSETUP, product.getM_AttributeSetInstance_ID(), trxName);
		
		if (isSetup != null)
			return isSetup.equalsIgnoreCase("true");

		return false;
	}
	
	public static boolean isDIDxNumber(Properties ctx, MProduct product)
	{
		for (MProductPO productPO : MProductPO.getOfProduct(ctx, product.getM_Product_ID(), null))
		{
			if (productPO.getC_BPartner_ID() == DIDConstants.BP_SUPER_TECH_INC_ID)
				return true;
		}
		
		return false;
	}
	
	public static boolean isInbound(Properties ctx, MProduct product, String trxName)
	{
		Integer direction = getAttributeInstanceValueId(ctx, DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION, product.getM_AttributeSetInstance_ID(), trxName);
		
		if (direction != null)
			return direction == DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_INBOUND;

		return false;
	}
	
//	public static ArrayList<String> getDIDNumbers(Properties ctx, String trxName)
//	{
//		ArrayList<String> allDIDs = new ArrayList<String>();
//		
//		for (MProduct product : getAllDIDProducts(ctx, trxName))
//		{
//			String number = getDIDNumber(ctx, product, trxName);
//			if (number != null && number.length() > 0)
//			{
//				boolean found = false;
//				for (String existingNumber : allDIDs)
//				{
//					if (existingNumber.equalsIgnoreCase(number))
//					{
//						found = true;
//						break;
//					}
//				}
//				
//				if (!found)
//					allDIDs.add(number);
//			}
//		}
//		
//		return allDIDs;
//	}
	
	public static String getDIDNumber(Properties ctx, MProduct product, String trxName)
	{
		return getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_DID_NUMBER, product.getM_AttributeSetInstance_ID(), trxName);
	}	
	
	public static String getCDRUsername(Properties ctx, MProduct product, String trxName)
	{
		return getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, product.getM_AttributeSetInstance_ID(), trxName);
	}
	
	public static String getCDRNumber(Properties ctx, MProduct product, String trxName)
	{
		return getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_CDR_NUMBER, product.getM_AttributeSetInstance_ID(), trxName);
	}	
	
	public static String getSIPAddress(Properties ctx, MProduct product, String trxName)
	{
		return getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_SIP_ADDRESS, product.getM_AttributeSetInstance_ID(), trxName);
	}
	
	public static String getSIPDomain(Properties ctx, MProduct product, String trxName)
	{
		return getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_SIP_DOMAIN, product.getM_AttributeSetInstance_ID(), trxName);
	}
	
	public static String getSIPURI(Properties ctx, MProduct product, String trxName)
	{
		String address = getSIPAddress(ctx, product, trxName);
		String domain = getSIPDomain(ctx, product, trxName);

		return address + "@" + domain;
	}
	
	public static String getVoicemailMailboxNumber(Properties ctx, MProduct product, String trxName)
	{
		return getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_VM_MAILBOX_NUMBER, product.getM_AttributeSetInstance_ID(), trxName);
	}
	
	public static DIDDescription getDIDDescription(Properties ctx, MProduct product, String trxName)
	{
		String countryCode = getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_DID_COUNTRYCODE, product.getM_AttributeSetInstance_ID(), trxName);
		String areaCode = getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_DID_AREACODE, product.getM_AttributeSetInstance_ID(), trxName);
		String perMinCharges = getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_DID_PERMINCHARGES, product.getM_AttributeSetInstance_ID(), trxName);
		String freeMins = getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_DID_FREEMINS, product.getM_AttributeSetInstance_ID(), trxName);
		
		DIDDescription didDesc = new DIDDescription(countryCode, areaCode, perMinCharges, freeMins);

		return didDesc;
	}
	
	public static MAttributeInstance getAttributeInstance(Properties ctx, int M_Attribute_ID, int M_AttributeSetInstance_ID, String trxName)
	{
		if (M_Attribute_ID > 0 && M_AttributeSetInstance_ID > 0)
		{
			MAttribute attribute = new MAttribute(ctx, M_Attribute_ID, trxName);
			if (attribute != null && attribute.get_ID() != 0)
				return attribute.getMAttributeInstance(M_AttributeSetInstance_ID);	
			else
				log.severe("Failed to load MAttributeInstance for MAttribute[" + M_Attribute_ID + "] and MAttributeSetInstance[" + M_AttributeSetInstance_ID + "]");
		}
	
		return null;
	}
	
	public static String getAttributeInstanceValue(Properties ctx, int M_Attribute_ID, int M_AttributeSetInstance_ID, String trxName)
	{
		MAttributeInstance attributeInstance = getAttributeInstance(ctx, M_Attribute_ID, M_AttributeSetInstance_ID, trxName);
		if (attributeInstance != null && attributeInstance.getValue() != null && attributeInstance.getValue().length() > 0)
			return attributeInstance.getValue();
		else
			return null;
	}
	
	public static Integer getAttributeInstanceValueId(Properties ctx, int M_Attribute_ID, int M_AttributeSetInstance_ID, String trxName)
	{
		MAttributeInstance attributeInstance = getAttributeInstance(ctx, M_Attribute_ID, M_AttributeSetInstance_ID, trxName);
		if (attributeInstance != null && attributeInstance.getM_AttributeValue_ID() > 0)
			return attributeInstance.getM_AttributeValue_ID();
		else
			return null;
	}
	
	public static MProduct getSetupOrMonthlyProduct(Properties ctx, MProduct prodA, MProduct prodB, boolean setup, String trxName)
	{
		if (prodA != null && prodB != null)
		{	
			if (isSetup(ctx, prodA, trxName) == setup)
				return prodA;
			else
				return prodB;
		}
		return null;
	}
	
	public static MProduct getInboundOrOutboundProduct(Properties ctx, MProduct prodA, MProduct prodB, boolean inbound, String trxName)
	{
		if (prodA != null && prodB != null)
		{	
			if (isInbound(ctx, prodA, trxName) == inbound)
				return prodA;
			else
				return prodB;
		}
		return null;
	}
	
	public static ArrayList<String> getNumbersFromOrder(Properties ctx, MOrder order, boolean didxOnly, String trxName)
	{
		ArrayList<String> numbers = new ArrayList<String>();
		
		if (order == null)
			return numbers;
		
		for (MOrderLine ol : order.getLines())
		{
			MProduct product = ol.getProduct();			
			
			if (product == null)
				continue;

			if (!didxOnly || isDIDxNumber(ctx, product))
			{
				String number = getDIDNumber(ctx, product, trxName);
				
				if (number == null || number.length() < 1)
					continue;
				
				boolean found = false;
				for (String existingNumber : numbers)
				{
					if (existingNumber.equals(number))
						found = true;
				}
				
				if (!found)
					numbers.add(number);
			}
		}		
		
		return numbers;
	}
	
	public static ArrayList<String> getNumbersToPortFromOrder(Properties ctx, MOrder order, String trxName)
	{
		ArrayList<String> numbers = new ArrayList<String>();
		
		if (order == null)
			return numbers;
		
		for (MOrderLine ol : order.getLines())
		{
			MProduct product = ol.getProduct();			
			
			if (product == null)
				continue;

			if (product.getM_Product_ID() == DIDConstants.M_PRODUCT_ID_NUMBER_PORT_FEE || product.getM_Product_ID() == DIDConstants.M_PRODUCT_ID_AUSTRALIAN_NUMBER_PORT_FEE)
			{
				String number = ol.getDescription();
				if (number != null && number.length() > 0)
					numbers.add(number.trim());
				else
					numbers.add(DIDConstants.NUMBER_PORT_PLACEHOLDER); 
			}
		}		
		
		return numbers;
	}
	
	public static HashMap<String, Timestamp> getSubscriptionDates(boolean isOneOffSubscription, Timestamp effectiveDate)
	{
		// TODO: What happens if i generate dates three times (for each sub) and the last one gets generated 1sec after midnight?		
		HashMap<String, Timestamp> dates = new HashMap<String, Timestamp>();
		
		// If not set then use current time
		if (effectiveDate == null)
			effectiveDate = new Timestamp(System.currentTimeMillis());
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeInMillis(effectiveDate.getTime());
		
		cal.set(GregorianCalendar.MONTH, cal.get(GregorianCalendar.MONTH) + 1); // add month
		cal.add(GregorianCalendar.DAY_OF_MONTH, -1); // subtract one day
//		Timestamp paidUntil = new Timestamp(cal.getTimeInMillis());	
		Timestamp paidUntil = effectiveDate;
		
		cal.add(GregorianCalendar.YEAR, 200); // add 200 years		
		Timestamp distantFuture = new Timestamp(cal.getTimeInMillis());
		
		// If type is One-off subscription then set paid until date as distant future
//		if (isOneOffSubscription)
//			paidUntil = new Timestamp(cal.getTimeInMillis());
		
		dates.put(MSubscription.COLUMNNAME_StartDate, effectiveDate);
		dates.put(MSubscription.COLUMNNAME_PaidUntilDate, paidUntil);
		dates.put(MSubscription.COLUMNNAME_RenewalDate, distantFuture);
		
		return dates;
	}
	
// *****************************************************************************************************************************************
	

	public static void loadLocalDIDs(Properties ctx, DIDCountry country, String trxName)
	{
		loadLocalDIDsByCountry(ctx, country, false, trxName);
	}
	
	public static void loadLocalAreaCodes(Properties ctx, DIDCountry country, String trxName)
	{
		loadLocalDIDsByCountry(ctx, country, true, trxName);
	}
	
	private static void loadLocalDIDsByCountry(Properties ctx, DIDCountry country, boolean onlyAreaCodes, String trxName)
	{
		if (country == null)
		{
			log.severe("Cannot load DID products into a NULL DIDCountry object");
			return;
		}
		
		// If caller not using trx create local
		boolean localTrx = false;
		if (trxName == null)
		{	
			localTrx = true;			
			trxName = Trx.createTrxName("loadLocalDIDsByCountry");
		}
		
		try
		{
			// Hashmaps to hold products		
			HashMap<String, MProduct> setupProducts = new HashMap<String, MProduct>();
			HashMap<String, MProduct> monthlyProducts = new HashMap<String, MProduct>();
			
			// DID attributes		
			MAttribute didIsSetupAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_ISSETUP, trxName); 
			MAttribute didNumberAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_NUMBER, trxName); 
			MAttribute didCountryIdAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_COUNTRYID, trxName);
			MAttribute didCountryCodeAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_COUNTRYCODE, trxName); 
			MAttribute didAreaCodeAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_AREACODE, trxName); 
			MAttribute didPerMinChargesAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_PERMINCHARGES, trxName); 
			MAttribute didDescriptionAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_DESCRIPTION, trxName); 
			
			// Load existing DID products (loaded all at once)
			MProduct[] existingUnsubscribedDIDProducts = DIDUtil.getBySubscription(ctx, false, trxName);
			
			// Seperate products into either setup or monthly list
			for (MProduct product : existingUnsubscribedDIDProducts)
			{
				MAttributeInstance mai_isSetup = didIsSetupAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
				MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
				
				// Check values for both attributes exist
				boolean attributeError = false;
				if (mai_isSetup == null || mai_isSetup.getValue() == null)
				{
					log.severe("Failed to load DID_ISSETUP for " + product);
					attributeError = true;
				}
				
				if (mai_didNumber == null || mai_didNumber.getValue() == null || mai_didNumber.getValue().length() < 1)
				{
					log.severe("Failed to load DID_NUMBER for " + product);
					attributeError = true;
				}
				
				if (attributeError)
					continue;
							
				// Load DID number
				String didNumber = mai_didNumber.getValue().trim();
				
				// TODO: Validate DID number?
							
				// Put product in either setup or monthly struct
				if (mai_isSetup.getValue().equalsIgnoreCase("true"))
					setupProducts.put(didNumber, product);
				
				else if (mai_isSetup.getValue().equalsIgnoreCase("false"))
					monthlyProducts.put(didNumber, product);
				
				else
					log.severe("Invalid DID_ISSETUP value for " + product);
	
			}
			
			// Log message if not equal numbers for further investigation
			if (setupProducts.size() != monthlyProducts.size())
			{
				log.warning("Number of setup products [" + setupProducts.size() + "] does not match the number of monthly products [" + 
						monthlyProducts.size() + "]. Needs to be investigated further.");
			}
	
			// Loop through each product pair and determine which to add to DIDCountry
			Iterator<String> productIterator = setupProducts.keySet().iterator();
			
			// Loop through list with most elements
			if (monthlyProducts.size() > setupProducts.size())
				productIterator = monthlyProducts.keySet().iterator();
			
			while(productIterator.hasNext())
			{
				// Get key
				String didNumber = productIterator.next();
						
				// Load products
				MProduct setupProduct = setupProducts.get(didNumber);
				MProduct monthlyProduct = monthlyProducts.get(didNumber);
				
				// Check both products exist (esp monthly)
				boolean productError = false;
				if (setupProduct == null)
				{
					log.severe("Failed to load setup product. Array was populated with a null setup product. DIDSU-" + didNumber + " is missing");
					productError = true;
				}
				
				if (monthlyProduct == null)
				{
					log.severe("Failed to load monthly product. Array was populated with a null monthly product. DID-" + didNumber + " is missing");
					productError = true;
				}
				
				if (productError)
					continue;
				
				// Load attribute instances
				MAttributeInstance mai_CountryId = didCountryIdAttribute.getMAttributeInstance(setupProduct.getM_AttributeSetInstance_ID());
				MAttributeInstance mai_CountryCode = didCountryCodeAttribute.getMAttributeInstance(setupProduct.getM_AttributeSetInstance_ID());
				MAttributeInstance mai_AreaCode = didAreaCodeAttribute.getMAttributeInstance(setupProduct.getM_AttributeSetInstance_ID());
				MAttributeInstance mai_PerMinCharges = didPerMinChargesAttribute.getMAttributeInstance(setupProduct.getM_AttributeSetInstance_ID());
				MAttributeInstance mai_Description = didDescriptionAttribute.getMAttributeInstance(setupProduct.getM_AttributeSetInstance_ID());
				
				// Check instances exist and contain values
				boolean maiError = false;
				if (mai_CountryId == null || mai_CountryId.getValue() == null)
				{
					log.severe("Failed to load DID_COUNTRYID attribute instance or attribute instance value " + setupProduct);
					maiError = true;
				}
				
				if (mai_CountryCode == null || mai_CountryCode.getValue() == null)
				{
					log.severe("Failed to load DID_COUNTRYCODE attribute instance or attribute instance value " + setupProduct);
					maiError = true;
				}
				
				if (mai_AreaCode == null || mai_AreaCode.getValue() == null)
				{
					log.severe("Failed to load DID_AREACODE attribute instance or attribute instance value " + setupProduct);
					maiError = true;
				}
				
				if (mai_PerMinCharges == null || mai_PerMinCharges.getValue() == null)
				{
					log.severe("Failed to load DID_PERMINCHARGES attribute instance or attribute instance value " + setupProduct);
					maiError = true;
				}
				
				if (mai_Description == null || mai_Description.getValue() == null)
				{
					log.severe("Failed to load DID_DESCRIPTION attribute instance or attribute instance value " + setupProduct);
					maiError = true;
				}
				
				if (maiError)
					continue;
				
				// Load attibute values
				String countryId = mai_CountryId.getValue();
				String countryCode = mai_CountryCode.getValue();
				String areaCode = mai_AreaCode.getValue();
				String perMinCharges = mai_PerMinCharges.getValue();
				String description = mai_Description.getValue();
				
				// Make sure countryId and countryCode match			
				if (!countryId.equalsIgnoreCase(country.getCountryId()) || !countryCode.equalsIgnoreCase(country.getCountryCode()))
					continue;
				
				// Populate DIDCountry data
				if (onlyAreaCodes)
					country.addAreaCode(areaCode, description);
				else
				{
					// Check if area code belongs to country
					boolean found = false;
					for (DIDAreaCode didAreaCode : country.getAreaCodes())
					{
						if (areaCode.equalsIgnoreCase(didAreaCode.getCode()))
						{
							found = true;
							break;
						}
					}
					
					if (found)
					{
						// Load PriceList Version ID
						// TODO: Get price list from somewhere?
						int M_PriceList_Version_ID = DIDConstants.PRICELIST_VERSION_ID_STANDARD;//WebUtil.getParameterAsInt(request, "M_PriceList_Version_ID");
						
						// Load prices
						MProductPrice setupPrice = MProductPrice.get(ctx, M_PriceList_Version_ID, setupProduct.getM_Product_ID(), trxName);
						MProductPrice monthlyPrice = MProductPrice.get(ctx, M_PriceList_Version_ID, monthlyProduct.getM_Product_ID(), trxName);
											
						// Validate prices before setting
						boolean priceError = false;
						if (setupPrice == null)
						{
							log.severe("Failed to load price for MProduct[" + setupProduct.getM_Product_ID() + "], DIDNumber[" + didNumber + "] & MPriceList[" + M_PriceList_Version_ID + "]");
							priceError = true;
						}
						
						if (monthlyPrice == null)
						{
							log.severe("Failed to load price for MProduct[" + monthlyProduct.getM_Product_ID() + "], DIDNumber[" + didNumber + "] & MPriceList[" + M_PriceList_Version_ID + "]");
							priceError = true;
						}
						
						if (priceError)
							continue; // TODO: Check this applies to outer loop
						
						// Create DID and add to country (areacode within country)
						DID did = new DID();
						did.setNumber(didNumber);
						did.setPerMinCharges(perMinCharges);
						did.setDescription(description);					
						did.setSetupCost(setupPrice.getPriceList().toString());
						did.setMonthlyCharges(monthlyPrice.getPriceList().toString());
						
						DIDAreaCode didAreaCode	= country.getAreaCode(areaCode);
						didAreaCode.addDID(did); // handles duplicate DIDs (they don't get added)
					}
				}
			}	
			
			// Don't need to commit trx, only using for reading data (prevents maxing connections). Finally will close it
		}
		catch(Exception ex)
		{
			log.severe(ex.getMessage());
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
	
// *****************************************************************************************************************************************
	
	/**
	 * Gets all subscribed fax numbers (monthly products with DID_SUBSCRIBED and DID_FAX_ISFAX flagged)
	 * 
	 * @return list of subscribed fax numbers
	 */
	public static ArrayList<String> getSubscribedFaxNumbers(Properties ctx, String trxName)
	{
		ArrayList<String> subscribedNumbers = new ArrayList<String>();
		
		MAttribute didIsSetupAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_ISSETUP, trxName);
		MAttribute didNumberAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_NUMBER, trxName); 
		MAttribute didFaxIsFaxAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_FAX_ISFAX, trxName); 

		MProduct[] products = DIDUtil.getBySubscription(ctx, true, trxName);
		for (MProduct product : products)
		{
			MAttributeInstance mai_isSetup = didIsSetupAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			MAttributeInstance mai_faxIsFax = didFaxIsFaxAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			
			// Check values for both attributes exist
			boolean attributeError = false;
			if (mai_isSetup == null || mai_isSetup.getValue() == null || mai_isSetup.getValue().length() < 1)
			{
				log.severe("Failed to load DID_ISSETUP for " + product);
				attributeError = true;
			}
			
			if (mai_didNumber == null || mai_didNumber.getValue() == null || mai_didNumber.getValue().length() < 1)
			{
				log.severe("Failed to load DID_NUMBER for " + product);
				attributeError = true;
			}
			
			if (mai_faxIsFax == null || mai_faxIsFax.getValue() == null || mai_faxIsFax.getValue().length() < 1)
			{
				log.severe("Failed to load DID_FAX_ISFAX for " + product);
				attributeError = true;
			}
			
			if (attributeError)
				continue;
			
			// Get values
			String didNumber = mai_didNumber.getValue().trim();
			boolean isMonthly = mai_isSetup.getValue().equals("false");
			boolean isFax = mai_faxIsFax.getValue().equals("true");
			
			// Add subscribed monthly fax products
			if (isFax && isMonthly)
				subscribedNumbers.add(didNumber);
		}
		
		return subscribedNumbers;
	}
	
// *****************************************************************************************************************************************
	
	/**
	 * Gets all subscribed fax numbers (monthly products with DID_SUBSCRIBED and DID_FAX_ISFAX flagged)
	 * 
	 * @return list of subscribed fax numbers
	 */
	public static HashMap<String,String> getSubscribedFaxNumbersAndCallType(Properties ctx, String trxName)
	{
		HashMap<String,String> subscribedNumbers = new HashMap<String,String>();
		
		MAttribute didIsSetupAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_ISSETUP, trxName);
		MAttribute didNumberAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_NUMBER, trxName); 
		MAttribute didFaxIsFaxAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_FAX_ISFAX, trxName); 

		MProduct[] products = DIDUtil.getBySubscription(ctx, true, trxName);
		for (MProduct product : products)
		{
			MAttributeInstance mai_isSetup = didIsSetupAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			MAttributeInstance mai_faxIsFax = didFaxIsFaxAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			
			// Check values for both attributes exist
			boolean attributeError = false;
			if (mai_isSetup == null || mai_isSetup.getValue() == null || mai_isSetup.getValue().length() < 1)
			{
				log.severe("Failed to load DID_ISSETUP for " + product);
				attributeError = true;
			}
			
			if (mai_didNumber == null || mai_didNumber.getValue() == null || mai_didNumber.getValue().length() < 1)
			{
				log.severe("Failed to load DID_NUMBER for " + product);
				attributeError = true;
			}
			
			if (mai_faxIsFax == null || mai_faxIsFax.getValue() == null || mai_faxIsFax.getValue().length() < 1)
			{
				log.severe("Failed to load DID_FAX_ISFAX for " + product);
				attributeError = true;
			}
			
			if (attributeError)
				continue;
			
			// Get values
			String didNumber = mai_didNumber.getValue().trim();
			boolean isMonthly = mai_isSetup.getValue().equals("false");
			boolean isFax = mai_faxIsFax.getValue().equals("true") || mai_faxIsFax.getValue().equals("in") || mai_faxIsFax.getValue().equals("out");
			String fax=mai_faxIsFax.getValue();
			
			// Add subscribed monthly fax products
			if (isFax && isMonthly)
				subscribedNumbers.put(didNumber,fax);
		}
		
		return subscribedNumbers;
	}
	//changes by lavanya 
	
	public static MProduct createAdditionalCallProduct(Properties ctx, HashMap<Integer, Object> attributes, String trxName,String domain)
	{
		// Load or create new trx
		boolean createdTrx = false;		
		if (trxName == null || trxName.length() < 1)
		{
			trxName = Trx.createTrxName("createAdditionalCallingProduct");
			createdTrx = true;
		}

		try
		{
			// Validate attributes
			if (!Validation.validateAttributes(ctx, Integer.parseInt(DIDConstants.CDR_ATTRIBUTE_SET_ID), attributes))
				throw new Exception("Failed to validate attributes");
			
			// Load param values
			String number = (String)attributes.get(DIDConstants.ATTRIBUTE_ID_CDR_NUMBER);
			Integer direction = (Integer)attributes.get(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION);
			
			// Inbound product fields
			String searchKey = DIDConstants.CALL_IN_PRODUCT_SEARCH_KEY_NEW;
			String name = DIDConstants.CALL_IN_PRODUCT_NAME;
			String description = DIDConstants.CALL_IN_PRODUCT_DESCRIPTION;
			
			// Outbound product fields
			if (direction == DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_OUTBOUND)
			{
				searchKey = DIDConstants.CALL_OUT_PRODUCT_SEARCH_KEY_NEW;
				name = DIDConstants.CALL_OUT_PRODUCT_NAME;
				description = DIDConstants.CALL_OUT_PRODUCT_DESCRIPTION;
			}
			
			searchKey = searchKey.replace(DIDConstants.NUMBER_IDENTIFIER, number).replace(DIDConstants.DOMAIN_IDENTIFIER,domain);
			name = name.replace(DIDConstants.NUMBER_IDENTIFIER, number);
			description = description.replace(DIDConstants.NUMBER_IDENTIFIER, number);
			
			HashMap<String, Object> fields = new HashMap<String, Object>();
			fields.put(MProduct.COLUMNNAME_Value, searchKey);
			fields.put(MProduct.COLUMNNAME_Name, name);
			fields.put(MProduct.COLUMNNAME_Description, description);
			fields.put(MProduct.COLUMNNAME_M_Product_Category_ID, DIDConstants.VOICE_SERVICES_RECUR_CALL_CATEGORY_ID);
			fields.put(MProduct.COLUMNNAME_C_TaxCategory_ID, DIDConstants.STANDARD_15_TAX_CATEGORY); 
			fields.put(MProduct.COLUMNNAME_C_UOM_ID, DIDConstants.UOM_EACH); 
			fields.put(MProduct.COLUMNNAME_M_AttributeSet_ID, DIDConstants.CDR_ATTRIBUTE_SET_ID);
			fields.put(MProduct.COLUMNNAME_ProductType, DIDConstants.PRODUCT_TYPE_SERVICE);
			fields.put(MProduct.COLUMNNAME_IsSelfService, DIDConstants.NOT_SELF_SERVICE);
					
			MProduct product = createProduct(ctx, fields, trxName);
			if (product == null)
				throw new Exception("Failed to create MProduct[" + searchKey + "]");
			
			// Create new attribute set instance
			MAttributeSetInstance masi = new MAttributeSetInstance(ctx, 0, trxName);
			masi.setM_AttributeSet_ID(Integer.parseInt(DIDConstants.CDR_ATTRIBUTE_SET_ID));
			if (!masi.save())
				throw new Exception("Failed to create MAttributeSetInstance for " + product);
			
			// Save new attribute set instance id to product
			product.setM_AttributeSetInstance_ID(masi.getM_AttributeSetInstance_ID());
			if (!product.save())
				throw new Exception("Failed to save MAttributeSetInstance for " + product);
			
			// Create attribute instances
			Iterator<Integer> iterator = attributes.keySet().iterator();
			while(iterator.hasNext())
			{
				Integer attributeId = (Integer)iterator.next();
				if (attributes.get(attributeId) instanceof String)
				{
					String attributeValue = (String)attributes.get(attributeId);
					
					MAttributeInstance attributeInstance = new MAttributeInstance(ctx, attributeId, masi.getM_AttributeSetInstance_ID(), attributeValue, trxName);
					if (!attributeInstance.save())
						throw new Exception("Failed to save MAttributeInstance[" + attributeId + "-" + attributeValue + "]  for " + product);
				}
				else if (attributes.get(attributeId) instanceof Integer)
				{
					Integer attributeValueId = (Integer)attributes.get(attributeId);
					MAttributeValue attributeValue = new MAttributeValue(ctx, attributeValueId, trxName);
					
					MAttributeInstance attributeInstance = new MAttributeInstance(ctx, attributeId, masi.getM_AttributeSetInstance_ID(), attributeValueId, attributeValue.getName(), trxName);
					if (!attributeInstance.save())
						throw new Exception("Failed to save MAttributeInstance[" + attributeId + "-" + attributeValueId + "]  for " + product);
				}
			}
			
			// Update MAttributeSetInstance description (attribute values seperated with _, don't need to worry if it fails)
			masi.setDescription();
			if (!masi.save())
				log.warning("Failed to update " + masi + "'s description");
			
			// If created trx in this method try to load then commit trx
			if (createdTrx)
			{
				Trx trx = Trx.get(trxName, false);
				if (trx == null)
					throw new Exception("Failed to load trx");
				else if (!trx.isActive())
					throw new Exception("Trx no longer active");
				else if (!trx.commit())
					throw new Exception("Failed to commit trx");
			}
			
			// To reset trxName
//			product.load(trxName);
			
			return product;
		}
		catch(Exception ex)
		{
			if (createdTrx)
			{
				Trx trx = Trx.get(trxName, false);
				if (trx != null && trx.isActive())
					trx.rollback();
			}
			
			log.severe(ex.getMessage());
		}
		finally
		{
			if (createdTrx)
			{
				Trx trx = Trx.get(trxName, false);
				if (trx != null && trx.isActive())
					trx.close();
			}
		}
					
		return null;
	}
	
	public static MProduct[] getCallProductsByDomain(Properties ctx, String username, String domain , String trxName)
	{
		return getProductsByDomain(ctx, DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, username, domain ,trxName);
	}
	
	public static MProduct[] getProductsByDomain(Properties ctx, int M_Attribute_ID, String value, String domain ,String trxName)
	{
		MProduct[] products = MProduct.get(ctx, 
				MProduct.COLUMNNAME_M_Product_ID + " IN" + 
				"(" +
					"SELECT " + MProduct.COLUMNNAME_M_Product_ID +
					" FROM " + MAttributeInstance.Table_Name + " ai, " + MProduct.Table_Name + " p" +
					" WHERE " +
						"ai." + MAttributeInstance.COLUMNNAME_M_AttributeSetInstance_ID + " = p." + MProduct.COLUMNNAME_M_AttributeSetInstance_ID +
					" AND " +
						"ai." + MAttributeInstance.COLUMNNAME_M_Attribute_ID + " = " + M_Attribute_ID +
					" AND " +
						"UPPER(ai." + MAttributeInstance.COLUMNNAME_Value + ") LIKE UPPER('%" + value + "%" + domain + "%')" +
				")" +
			" AND UPPER(IsActive) = 'Y'", trxName);
		
		return products;
	}
	
	/*public static MSubscription createDIDSetupSubscription(Properties ctx, String number, int C_BPartner_ID, int C_BPartner_Location_ID, int M_Product_ID, Timestamp startDate, Timestamp paidUntilDate ,String trxName)
	{
		HashMap<String, Object> fields = new HashMap<String, Object>();
		
		// Create name
		String name = DIDConstants.DID_SUBSCRIPTION_NAME.replace(DIDConstants.NUMBER_IDENTIFIER, number);
		
		// Get dates
		HashMap<String, Timestamp> dates = getSubscriptionDates(true, null);
		
		// Mandatory
		fields.put(MSubscription.COLUMNNAME_Name, name);
		fields.put(MSubscription.COLUMNNAME_C_BPartner_ID, C_BPartner_ID); 
		fields.put(MBPartnerLocation.COLUMNNAME_C_BPartner_Location_ID, C_BPartner_Location_ID);
		fields.put(MSubscription.COLUMNNAME_M_Product_ID, M_Product_ID);
		fields.put(MSubscription.COLUMNNAME_C_SubscriptionType_ID, DIDConstants.C_SUBSCRIPTIONTYPE_ID_ONE_OFF); 		
		fields.put(MSubscription.COLUMNNAME_StartDate, startDate != null ? startDate : dates.get(MSubscription.COLUMNNAME_StartDate));
		fields.put(MSubscription.COLUMNNAME_PaidUntilDate,paidUntilDate!=null ? paidUntilDate: dates.get(MSubscription.COLUMNNAME_PaidUntilDate)); 
		fields.put(MSubscription.COLUMNNAME_RenewalDate, dates.get(MSubscription.COLUMNNAME_RenewalDate)); 
		fields.put(MSubscription.COLUMNNAME_IsDue, false); 
		fields.put(MSubscription.COLUMNNAME_BillInAdvance, false);
		
		return createSubscription(ctx, fields, trxName);
	}*/
}
