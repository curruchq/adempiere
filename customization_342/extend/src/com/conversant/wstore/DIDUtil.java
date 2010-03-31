package com.conversant.wstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPO;
import org.compiere.model.MProductPrice;
import org.compiere.util.CLogger;
import org.compiere.wstore.DIDDescription;

import com.conversant.db.BillingConnector;
import com.conversant.model.DID;
import com.conversant.model.DIDAreaCode;
import com.conversant.model.DIDCountry;

public class DIDUtil 
{
	private static CLogger log = CLogger.getCLogger(DIDUtil.class);

// *****************************************************************************************************************************************
	

	
// *****************************************************************************************************************************************	
	
	public static MProduct[] getBySubscription(Properties ctx, boolean subscribed)
	{
		return getProducts(ctx, DIDConstants.ATTRIBUTE_ID_DID_SUBSCRIBED, Boolean.toString(subscribed));
	}
	
	public static MProduct[] getAllDIDProducts(Properties ctx)
	{
		return getProducts(ctx, DIDConstants.ATTRIBUTE_ID_DID_NUMBER, "%");
	}
	
	public static MProduct[] getDIDProducts(Properties ctx, String didNumber)
	{
		return getProducts(ctx, DIDConstants.ATTRIBUTE_ID_DID_NUMBER, didNumber);
	}
	
	public static MProduct[] getSIPProducts(Properties ctx, String address)
	{
		return getProducts(ctx, DIDConstants.ATTRIBUTE_ID_SIP_ADDRESS, address);
	}
	
	public static MProduct[] getVoicemailProducts(Properties ctx, String mailboxNumber)
	{
		return getProducts(ctx, DIDConstants.ATTRIBUTE_ID_VM_MAILBOX_NUMBER, mailboxNumber);
	}
	
	public static MProduct[] getProducts(Properties ctx, int M_Attribute_ID, String value)
	{
		MProduct[] products = MProduct.get(ctx, 
			"M_Product_ID IN" + 
				"(" +
					"SELECT M_PRODUCT_ID" +
					" FROM " + MAttributeInstance.Table_Name + " ai, " + MProduct.Table_Name + " p" +
					" WHERE " +
						"ai." + MAttributeInstance.COLUMNNAME_M_AttributeSetInstance_ID + " = p." + MProduct.COLUMNNAME_M_AttributeSetInstance_ID +
					" AND " +
						"ai." + MAttributeInstance.COLUMNNAME_M_Attribute_ID + " = " + M_Attribute_ID +
					" AND " +
						"UPPER(ai." + MAttributeInstance.COLUMNNAME_Value + ") LIKE UPPER('" + value + "')" +
				")" +
			" AND UPPER(IsActive) = 'Y'", null);
		
		return products;
	}
	
// *****************************************************************************************************************************************
	
	public static boolean isSubscribed(Properties ctx, MProduct product)
	{
		String subscribed = getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_DID_SUBSCRIBED, product.getM_AttributeSetInstance_ID());
		
		if (subscribed != null)
			return !subscribed.equalsIgnoreCase("false"); // unless explicitly false treat as subscribed
		
		return true;
	}
	
	public static boolean isSetup(Properties ctx, MProduct product)
	{
		String isSetup = getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_DID_ISSETUP, product.getM_AttributeSetInstance_ID());
		
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
	
	public static String getDIDNumber(Properties ctx, MProduct product)
	{
		return getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_DID_NUMBER, product.getM_AttributeSetInstance_ID());
	}	
	
	public static String getSIPAddress(Properties ctx, MProduct product)
	{
		return getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_SIP_ADDRESS, product.getM_AttributeSetInstance_ID());
	}
	
	public static String getSIPDomain(Properties ctx, MProduct product)
	{
		return getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_SIP_DOMAIN, product.getM_AttributeSetInstance_ID());
	}
	
	public static String getSIPURI(Properties ctx, MProduct product)
	{
		String address = getSIPAddress(ctx, product);
		String domain = getSIPDomain(ctx, product);

		return address + "@" + domain;
	}
	
	public static String getVoicemailMailboxNumber(Properties ctx, MProduct product)
	{
		return getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_VM_MAILBOX_NUMBER, product.getM_AttributeSetInstance_ID());
	}
	
	public static DIDDescription getDIDDescription(Properties ctx, MProduct product)
	{
		String countryCode = getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_DID_COUNTRYCODE, product.getM_AttributeSetInstance_ID());
		String areaCode = getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_DID_AREACODE, product.getM_AttributeSetInstance_ID());;
		String perMinCharges = getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_DID_PERMINCHARGES, product.getM_AttributeSetInstance_ID());
		String freeMins = getAttributeInstanceValue(ctx, DIDConstants.ATTRIBUTE_ID_DID_FREEMINS, product.getM_AttributeSetInstance_ID());
		
		DIDDescription didDesc = new DIDDescription(countryCode, areaCode, perMinCharges, freeMins);

		return didDesc;
	}
	
	public static String getAttributeInstanceValue(Properties ctx, int M_Attribute_ID, int M_AttributeSetInstance_ID)
	{
		MAttribute attribute = new MAttribute(ctx, M_Attribute_ID, null);
		MAttributeInstance attributeInstance = attribute.getMAttributeInstance(M_AttributeSetInstance_ID);
		
		if (attributeInstance == null || attributeInstance.getValue() == null || attributeInstance.getValue().length() < 1)
		{
			log.severe("Failed to load value for MAttribute[" + M_Attribute_ID + "] and MAttributeSetInstance[" + M_AttributeSetInstance_ID + "]");
			return null;
		}
		
		return attributeInstance.getValue();
	}
	
	public static MProduct getSetupOrMonthlyProduct(Properties ctx, MProduct prodA, MProduct prodB, boolean setup)
	{
		if (prodA != null && prodB != null)
		{	
			if (isSetup(ctx, prodA) == setup)
				return prodA;
			else
				return prodB;
		}
		return null;
	}
	
	public static ArrayList<String> getNumbersFromOrder(Properties ctx, MOrder order, boolean didxOnly)
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
				numbers.add(getDIDNumber(ctx, product));
		}		
		
		return numbers;
	}
	
// *****************************************************************************************************************************************
	

	public static void loadLocalDIDs(Properties ctx, DIDCountry country)
	{
		loadLocalDIDsByCountry(ctx, country, false);
	}
	
	public static void loadLocalAreaCodes(Properties ctx, DIDCountry country)
	{
		loadLocalDIDsByCountry(ctx, country, true);
	}
	
	private static void loadLocalDIDsByCountry(Properties ctx, DIDCountry country, boolean onlyAreaCodes)
	{
		if (country == null)
		{
			log.severe("Cannot load DID products into a NULL DIDCountry object");
			return;
		}
		
		// Hashmaps to hold products		
		HashMap<String, MProduct> setupProducts = new HashMap<String, MProduct>();
		HashMap<String, MProduct> monthlyProducts = new HashMap<String, MProduct>();
		
		// DID attributes		
		MAttribute didIsSetupAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_ISSETUP, null); 
		MAttribute didNumberAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_NUMBER, null); 
		MAttribute didCountryIdAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_COUNTRYID, null);
		MAttribute didCountryCodeAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_COUNTRYCODE, null); 
		MAttribute didAreaCodeAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_AREACODE, null); 
		MAttribute didPerMinChargesAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_PERMINCHARGES, null); 
		MAttribute didDescriptionAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_DESCRIPTION, null); 
		
		// Load existing DID products (loaded all at once)
		MProduct[] existingUnsubscribedDIDProducts = DIDUtil.getBySubscription(ctx, false);
		
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
					int M_PriceList_Version_ID = 1000000;//WebUtil.getParameterAsInt(request, "M_PriceList_Version_ID");
					
					// Load prices
					MProductPrice setupPrice = MProductPrice.get(ctx, M_PriceList_Version_ID, setupProduct.getM_Product_ID(), null);
					MProductPrice monthlyPrice = MProductPrice.get(ctx, M_PriceList_Version_ID, monthlyProduct.getM_Product_ID(), null);
										
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
	}	
	
// *****************************************************************************************************************************************
	
	/**
	 * Gets all subscribed fax numbers (monthly products with DID_SUBSCRIBED and DID_FAX_ISFAX flagged)
	 * 
	 * @return list of subscribed fax numbers
	 */
	public static ArrayList<String> getSubscribedFaxNumbers(Properties ctx)
	{
		ArrayList<String> subscribedNumbers = new ArrayList<String>();
		
		MAttribute didIsSetupAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_ISSETUP, null);
		MAttribute didNumberAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_NUMBER, null); 
		MAttribute didFaxIsFaxAttribute = new MAttribute(ctx, DIDConstants.ATTRIBUTE_ID_DID_FAX_ISFAX, null); 

		MProduct[] products = DIDUtil.getBySubscription(ctx, true);
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
}
