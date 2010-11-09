package com.conversant.test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MProduct;
import org.compiere.model.MSubscription;
import org.compiere.util.CLogger;
import org.compiere.util.Trx;
import org.compiere.wstore.DIDController;
import org.compiere.wstore.DIDDescription;

import com.conversant.did.DIDConstants;
import com.conversant.did.DIDUtil;
import com.conversant.model.DID;
import com.conversant.model.DIDAreaCode;
import com.conversant.model.DIDCountry;

public class DIDUtilTestCase extends AdempiereTestCase  
{
	private static CLogger log = CLogger.getCLogger(DIDUtilTestCase.class);
	
	private static final boolean SHOW_TIMING = true;
	
	private static String COUNTRY_ID = "147";
	private static String COUNTRY_CODE = "64";
	private static String AREA_CODE = "9429";
	private static String AREA_CODE_DESCRIPTION = "Auckland - Red Beach Test";
	private static String PER_MIN_CHARGES = "1";
	private static String FREE_MINS = "10";
	private static String VENDOR_RATING = "5";
	private static int BP_2TALK_ID = 1000076;
	
	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
		log.info("Done setting up DIDUtilTestCase");
	}
	
	@Override
	protected void tearDown() throws Exception 
	{
		super.tearDown();
		log.info("Done tearing down DIDUtilTestCase");
	}
	
	private DIDCountry createNZCountry()
	{
		return new DIDCountry("New Zealand", "64", "147"); // TODO: Add as constants
	}
	
	private String getRandomDID()
	{
		Random rn = new Random();
		String didNumber = COUNTRY_CODE + AREA_CODE + (rn.nextInt(8999) + 1000);
		return didNumber;
	}
	
	private String createDIDProduct(boolean superTech, boolean subscribed)
	{
		String didNumber = getRandomDID();
		String countryId = COUNTRY_ID;
		String countryCode = COUNTRY_CODE;
		String areaCode = AREA_CODE;
		String areaCodeDescription = AREA_CODE_DESCRIPTION;
		String perMinCharge = PER_MIN_CHARGES;
		String freeMinutes = FREE_MINS;
		String vendorRating = VENDOR_RATING;
		int M_PriceList_Version_ID = 1000000;
		BigDecimal setupCost = new BigDecimal(5);
		BigDecimal monthlyCharge = new BigDecimal(5);
		int C_BPartner_ID = DIDConstants.BP_SUPER_TECH_INC_ID;
		int C_Currency_ID = DIDConstants.NZD_CURRENCY_ID;
		
		if (!superTech)
			C_BPartner_ID = BP_2TALK_ID;
		
		HashMap<String, String> setupFields = DIDController.getDIDSetupFields(didNumber, areaCodeDescription);
		HashMap<String, String> monthlyFields = DIDController.getDIDMonthlyFields(didNumber, areaCodeDescription);
		
		MProduct setupProduct = DIDController.createMProduct(getCtx(), setupFields);
		MProduct monthlyProduct = DIDController.createMProduct(getCtx(), monthlyFields);

		DIDController.updateProductAttributes(getCtx(), setupProduct.getM_AttributeSetInstance_ID(), 
				setupProduct.get_ID(), areaCodeDescription, didNumber, perMinCharge, areaCode, 
				vendorRating, countryId, countryCode, freeMinutes, true, subscribed);
		DIDController.updateProductAttributes(getCtx(), monthlyProduct.getM_AttributeSetInstance_ID(), 
				monthlyProduct.get_ID(), areaCodeDescription, didNumber, perMinCharge, areaCode, 
				vendorRating, countryId, countryCode, freeMinutes, false, subscribed);
		
		// Reload product values so that M_AttributeSetInstance_ID is refreshed
		setupProduct.load(null);
		monthlyProduct.load(null);
		
		DIDController.updateProductPrice(getCtx(), M_PriceList_Version_ID, setupProduct.get_ID(), setupCost);
		DIDController.updateProductPrice(getCtx(), M_PriceList_Version_ID, monthlyProduct.get_ID(), monthlyCharge);
		
		DIDController.updateBPPriceListPrice(getCtx(), C_BPartner_ID, setupProduct.get_ID(), setupCost);
		DIDController.updateBPPriceListPrice(getCtx(), C_BPartner_ID, monthlyProduct.get_ID(), monthlyCharge);
		
		DIDController.updateProductPO(getCtx(), C_BPartner_ID, setupProduct, setupCost, C_Currency_ID);
		DIDController.updateProductPO(getCtx(), C_BPartner_ID, monthlyProduct, monthlyCharge, C_Currency_ID);
		
		DIDController.updateProductRelations(getCtx(), monthlyProduct.get_ID(), setupProduct.get_ID()); 
		
		System.out.println("Created monthly " + monthlyProduct + " and setup " + setupProduct);
		
		return didNumber;
	}
	
	public String createSIPProduct()
	{
		return createSIPProduct(DIDConstants.DEFAULT_SIP_DOMAIN);
	}
	
	public String createSIPProduct(String domain)
	{
		Random rn = new Random();
		String address = "64" + AREA_CODE + (rn.nextInt(8999) + 1000);
		
		MProduct cvoiceProduct = DIDController.createMProduct(getCtx(), DIDController.getCVoiceFields(address));
		DIDController.updateSIPProductAttributes(getCtx(), cvoiceProduct, address, domain);
		
		System.out.println("Created CVoice product " + cvoiceProduct);
		
		return address;
	}
	
	public String createVoicemailProduct()
	{
		Random rn = new Random();
		String didNumber = "64" + AREA_CODE + (rn.nextInt(8999) + 1000);
		
		MProduct voicemailProduct = DIDController.createMProduct(getCtx(), DIDController.getVoicemailFields(didNumber));
		DIDController.updateVoicemailProductAttributes(getCtx(), voicemailProduct, didNumber, "proxy_default", "proxy-vm");
		
		System.out.println("Created Voicemail product " + voicemailProduct);
		
		return didNumber;
	}

// *****************************************************************************************************************************************

	public void testCreateDIDProduct()
	{
		String number = getRandomDID();
		
		HashMap<Integer, Object> attributes = new HashMap<Integer, Object>();
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_AREACODE, AREA_CODE);
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_COUNTRYCODE, COUNTRY_CODE);
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_COUNTRYID, COUNTRY_ID);
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_DESCRIPTION, AREA_CODE_DESCRIPTION);
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_FAX_FROMEMAIL, "-");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_FAX_ISFAX, "false");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_FAX_TOEMAIL, "-");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_FREEMINS, FREE_MINS);
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_ISSETUP, "true");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_NUMBER, number);
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_PERMINCHARGES, PER_MIN_CHARGES);
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_SUBSCRIBED, "false");
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_VENDORRATING, VENDOR_RATING);
		
		MProduct setupProduct = DIDUtil.createDIDProduct(getCtx(), attributes, null);
		if (setupProduct == null)
			fail("Failed to create setup product");
		
		attributes.remove(DIDConstants.ATTRIBUTE_ID_DID_ISSETUP);
		attributes.put(DIDConstants.ATTRIBUTE_ID_DID_ISSETUP, "false");
		
		MProduct monthlyProduct = DIDUtil.createDIDProduct(getCtx(), attributes, null);
		if (monthlyProduct == null)
			fail("Failed to create monthly product");
		
		MProduct[] products = DIDUtil.getDIDProducts(getCtx(), number, null);
		if (products.length != 2)
			fail("Either didn't create or can't load both products");	
		
		// Create trx
		String trxName = Trx.createTrxName();
		Trx trx = Trx.get(trxName, false);
		
		try
		{
			// Reset number
			number = getRandomDID();
			attributes.remove(DIDConstants.ATTRIBUTE_ID_DID_NUMBER);
			attributes.put(DIDConstants.ATTRIBUTE_ID_DID_NUMBER, number);
			
			attributes.remove(DIDConstants.ATTRIBUTE_ID_DID_ISSETUP);
			attributes.put(DIDConstants.ATTRIBUTE_ID_DID_ISSETUP, "true");
			
			setupProduct = DIDUtil.createDIDProduct(getCtx(), attributes, trxName);
			if (setupProduct == null)
				throw new Exception("Failed to create setup product");
			
			attributes.remove(DIDConstants.ATTRIBUTE_ID_DID_ISSETUP);
			attributes.put(DIDConstants.ATTRIBUTE_ID_DID_ISSETUP, "false");
			
			monthlyProduct = DIDUtil.createDIDProduct(getCtx(), attributes, trxName);
			if (monthlyProduct == null)
				throw new Exception("Failed to create monthly product");
			
			products = DIDUtil.getDIDProducts(getCtx(), number, null);
			if (products.length > 0)
				throw new Exception("Was able to load products before commiting trx");
			
			if (!trx.commit())
				throw new Exception("Failed to commit trx");
			
			products = DIDUtil.getDIDProducts(getCtx(), number, null);
			if (products.length != 2)
				throw new Exception("Either didn't create or can't load both products");
		}
		catch (Exception ex)
		{
			fail(ex.getMessage());
		}
		finally
		{
			if (trx != null && trx.isActive())
				trx.close();
		}
	}
	
	public void testCreateSIPProduct()
	{
		String sipAddress = getRandomDID();
		String sipDomain = DIDConstants.DEFAULT_SIP_DOMAIN;
		
		HashMap<Integer, Object> attributes = new HashMap<Integer, Object>();
		attributes.put(DIDConstants.ATTRIBUTE_ID_SIP_ADDRESS, sipAddress);
		attributes.put(DIDConstants.ATTRIBUTE_ID_SIP_DOMAIN, sipDomain);
		
		MProduct sipProduct = DIDUtil.createSIPProduct(getCtx(), attributes, null);
		if (sipProduct == null)
			fail("Failed to create SIP product");
		
		MProduct[] products = DIDUtil.getSIPProducts(getCtx(), sipAddress, sipDomain, null);
		if (products.length != 1)
			fail("Either didn't create or can't load product");
		
		// Create trx
		String trxName = Trx.createTrxName();
		Trx trx = Trx.get(trxName, false);
		
		try
		{
			// Reset sipAddress
			sipAddress = getRandomDID();
			attributes.remove(DIDConstants.ATTRIBUTE_ID_SIP_ADDRESS);
			attributes.put(DIDConstants.ATTRIBUTE_ID_SIP_ADDRESS, sipAddress);
			
			sipProduct = DIDUtil.createSIPProduct(getCtx(), attributes, trxName);
			if (sipProduct == null)
				throw new Exception("Failed to create SIP product");
			
			products = DIDUtil.getSIPProducts(getCtx(), sipAddress, sipDomain);
			if (products.length > 0)
				throw new Exception("Was able to load product before commiting trx");
			
			if (!trx.commit())
				throw new Exception("Failed to commit trx");
			
			products = DIDUtil.getSIPProducts(getCtx(), sipAddress, sipDomain);
			if (products.length != 1)
				throw new Exception("Either didn't create or can't load product");
		}
		catch (Exception ex)
		{
			fail(ex.getMessage());
		}
		finally
		{
			if (trx != null && trx.isActive())
				trx.close();
		}
	}
	
	public void testCreateProduct()
	{
		HashMap<String, Object> fields = new HashMap<String, Object>();
		
		MProduct product = DIDUtil.createProduct(getCtx(), fields, null);
		if (product != null)
		{
			product.delete(true);
			fail("Created product with no fields");
		}
		
		fields.put(MProduct.COLUMNNAME_Value, "TestSearchKey");
		fields.put(MProduct.COLUMNNAME_Name, "TestName");
		fields.put(MProduct.COLUMNNAME_M_Product_Category_ID, DIDConstants.VOICE_SERVICES_CATEGORY_ID);
		fields.put(MProduct.COLUMNNAME_C_TaxCategory_ID, DIDConstants.STANDARD_15_TAX_CATEGORY); 
		
		product = DIDUtil.createProduct(getCtx(), fields, null);
		if (product != null)
		{			
			product.delete(true);
			fail("Created product with incomplete fields");
		}
		
		fields.put(MProduct.COLUMNNAME_C_UOM_ID, DIDConstants.UOM_MONTH_8DEC); 	
		fields.put(MProduct.COLUMNNAME_ProductType, DIDConstants.PRODUCT_TYPE_SERVICE); 
		fields.put(MProduct.COLUMNNAME_IsSelfService, DIDConstants.NOT_SELF_SERVICE); 
		
		product = DIDUtil.createProduct(getCtx(), fields, null);
		if (product == null)
			fail("Failed to create product");
		else
			product.delete(true);
	}
	
	public void testCreateDIDSubscription()
	{
		// TODO: Finish me
	}
	
	public void testCreateSubscription()
	{
		HashMap<String, Object> fields = new HashMap<String, Object>();
		
		MSubscription subscription = DIDUtil.createSubscription(getCtx(), fields, null);
		if (subscription != null)
		{
			subscription.delete(true);
			fail("Created subscription with no fields");			
		}
		
		fields.put(MSubscription.COLUMNNAME_Name, "Test Subscription");
		fields.put(MSubscription.COLUMNNAME_C_BPartner_ID, 1000071); 
		fields.put(MSubscription.COLUMNNAME_M_Product_ID, 1000000);
		fields.put(MSubscription.COLUMNNAME_C_SubscriptionType_ID, 1000004); 		
		
		subscription = DIDUtil.createSubscription(getCtx(), fields, null);
		if (subscription != null)
		{
			subscription.delete(true);
			fail("Created subscription with incomplete fields");
		}
		
		fields.put(MSubscription.COLUMNNAME_StartDate, new Timestamp(System.currentTimeMillis()));
		fields.put(MSubscription.COLUMNNAME_PaidUntilDate, new Timestamp(System.currentTimeMillis())); 
		fields.put(MSubscription.COLUMNNAME_RenewalDate, new Timestamp(System.currentTimeMillis())); 
		fields.put(MSubscription.COLUMNNAME_IsDue, true);
		
		subscription = DIDUtil.createSubscription(getCtx(), fields, null);
		if (subscription == null)
			fail("Failed to create subscription");
		else
			subscription.delete(true);
	}
	
// *****************************************************************************************************************************************
	
	public void testUpdateAttributes()
	{
		HashMap<Integer, String> attributePairs = new HashMap<Integer, String>();
		
		attributePairs.put(DIDConstants.ATTRIBUTE_ID_DID_NUMBER, "123456789");
		
		if (DIDUtil.updateAttributes(getCtx(), 0, attributePairs, null))
		{
			fail("Updated attribute with invalid attribute set instance");
		}
		
		// Create product pair
		String didNumber = createDIDProduct(true, false);
		MProduct[] products = DIDUtil.getDIDProducts(getCtx(), didNumber, null);
		
		attributePairs.put(null, "123456789");
		
		if (DIDUtil.updateAttributes(getCtx(), products[0].getM_AttributeSetInstance_ID(), attributePairs, null))
		{
			fail("Updated attribute with NULL attributeId");
		}
		
		attributePairs.clear();
		attributePairs.put(DIDConstants.ATTRIBUTE_ID_DID_NUMBER, null);
		
		if (DIDUtil.updateAttributes(getCtx(), products[0].getM_AttributeSetInstance_ID(), attributePairs, null))
		{
			fail("Updated attribute with NULL value");
		}
		
		attributePairs.clear();
		attributePairs.put(DIDConstants.ATTRIBUTE_ID_DID_NUMBER, "");
		
		if (DIDUtil.updateAttributes(getCtx(), products[0].getM_AttributeSetInstance_ID(), attributePairs, null))
		{
			fail("Updated attribute with empty value");
		}
		
		attributePairs.clear();
		attributePairs.put(DIDConstants.ATTRIBUTE_ID_DID_SUBSCRIBED, "true");
		
		if (!DIDUtil.updateAttributes(getCtx(), products[0].getM_AttributeSetInstance_ID(), attributePairs, null) || 
			!DIDUtil.updateAttributes(getCtx(), products[1].getM_AttributeSetInstance_ID(), attributePairs, null))
		{
			fail("Failed to set " + products[0] + " and/or " + products[1] + " DID_SUBSCRIBED to \"true\"");
		}
		
		attributePairs.clear();
		attributePairs.put(DIDConstants.ATTRIBUTE_ID_DID_COUNTRYCODE, "Changed");
		attributePairs.put(DIDConstants.ATTRIBUTE_ID_DID_AREACODE, "Changed");
		attributePairs.put(DIDConstants.ATTRIBUTE_ID_DID_NUMBER, "Changed");
		
		if (!DIDUtil.updateAttributes(getCtx(), products[0].getM_AttributeSetInstance_ID(), attributePairs, null) || 
			!DIDUtil.updateAttributes(getCtx(), products[1].getM_AttributeSetInstance_ID(), attributePairs, null))
		{
			fail("Failed to set multiple attributes for " + products[0] + " and/or " + products[1]);
		}
		
		attributePairs.clear();
		attributePairs.put(DIDConstants.ATTRIBUTE_ID_DID_NUMBER, "ShouldntChangeDueToRollback");
		attributePairs.put(0, "InvalidMAttributeId");
		attributePairs.put(DIDConstants.ATTRIBUTE_ID_DID_DESCRIPTION, "ShouldntChangeWontGetThisFar");
		
		if (DIDUtil.updateAttributes(getCtx(), products[0].getM_AttributeSetInstance_ID(), attributePairs, null))
		{
			fail("Updated invalid attributes");
		}
		else
		{
			if (DIDUtil.getDIDNumber(getCtx(), products[0], null).equals("ShouldntChangeDueToRollback"))
				fail("Updated attribute and didn't rollback when invalid attribute found");
			
			if (DIDUtil.getDIDDescription(getCtx(), products[0], null).equals("ShouldntChangeWontGetThisFar"))
				fail("Updated attribute after invalid attribute found");
		}
	}
	
// *****************************************************************************************************************************************
	
	public void testGetBySubscription()
	{
		MAttribute didIsSubscribedAttribute = new MAttribute(getCtx(), DIDConstants.ATTRIBUTE_ID_DID_SUBSCRIBED, null); 
		Long start = System.currentTimeMillis();		
		MProduct[] products = DIDUtil.getBySubscription(getCtx(), false, null);
		
		if (SHOW_TIMING) 
			System.out.println("DIDUtil.getBySubscription() ran in " + (System.currentTimeMillis() - start) + "ms");

		int unsubscribedCount = 0;
		for (MProduct product : products)
		{
			MAttributeInstance mai_isSubscribed = didIsSubscribedAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			
			if (mai_isSubscribed == null || mai_isSubscribed.getValue() == null)
			{
				fail("getUnsubscribedDIDProducts() returned a DID which has no subscribed attribute and or value - " + product.toString());
			}			
			else if (mai_isSubscribed.getValue().equalsIgnoreCase("true"))
			{
				fail("getUnsubscribedDIDProducts() returned a subscribed DID " + product.toString());
			}
			else if (mai_isSubscribed.getValue().equalsIgnoreCase("false"))
			{
				unsubscribedCount++;
			}
			else
			{
				fail("getUnsubscribedDIDProducts() returned a DID with a subscribed attribute value which is neither 'true' or 'false' - " + product.toString());
			}
		}
		
		assertEquals("Number of products doesn't match unsubscribed count", products.length, unsubscribedCount);
	}
	
	public void testGetAllDIDProducts()
	{
		String didNumber = createDIDProduct(true, false);		
		Long start = System.currentTimeMillis();		
		MProduct[] products = DIDUtil.getAllDIDProducts(getCtx(), null);
		
		if (SHOW_TIMING) 
			System.out.println("DIDUtil.getAllDIDProducts() ran in " + (System.currentTimeMillis() - start) + "ms");
		
		boolean found = false;
		for (MProduct product : products)
		{
			if (didNumber.equals(DIDUtil.getDIDNumber(getCtx(), product, null)))
			{
				found = true;
				break;
			}
		}
		
		if (!found)
			fail("Failed to load newly created DID[" + didNumber + "]");
	}
	
	public void testGetDIDProducts()
	{
		String didNumber = createDIDProduct(true, false);		
		Long start = System.currentTimeMillis();		
		MProduct[] products = DIDUtil.getDIDProducts(getCtx(), didNumber, null);
		
		if (SHOW_TIMING) 
			System.out.println("DIDUtil.getDIDProducts() ran in " + (System.currentTimeMillis() - start) + "ms");
		
		if (products.length < 2)
		{
			fail("Found " + products.length + " product(s)");
		}
		else if (products.length > 2)
		{
			fail("Found " + products.length + " products");
		}
		else 
		{
			String num1 = DIDUtil.getDIDNumber(getCtx(), products[0], null);
			String num2 = DIDUtil.getDIDNumber(getCtx(), products[1], null);
			
			if (!num1.equals(num2))
			{
				fail("Found products with mismatched numbers - Number1=" + num1 + ", Number2=" + num2);
			}
			else if (!num1.equals(didNumber))
			{
				fail("Returned number " + num1 + " doesn't match initial number " + didNumber);
			}
		}
	}
	
	public void testGetSIPProducts()
	{
		String address = createSIPProduct();
		Long start = System.currentTimeMillis();		
		MProduct[] products = DIDUtil.getSIPProducts(getCtx(), address, null);
		
		if (SHOW_TIMING) 
			System.out.println("DIDUtil.getSIPProduct() ran in " + (System.currentTimeMillis() - start) + "ms");
		
		if (products.length < 1)
		{
			fail("No products found");
		}
		else if (products.length > 1)
		{
			fail("Found " + products.length + " products");
		}
		else 
		{
			String sipAddress = DIDUtil.getSIPAddress(getCtx(), products[0], null);
			
			if (!address.equals(sipAddress))
			{
				fail("SIP Address " + sipAddress + " doesn't match DID number " + address);
			}
		}
	}
	
	public void testGetSipProductsByAddressAndDomain()
	{
		String domain = "test.co.nz";
		String address = createSIPProduct(domain);
		Long start = System.currentTimeMillis();		
		MProduct[] products = DIDUtil.getSIPProducts(getCtx(), address, domain, null);
		
		if (SHOW_TIMING) 
			System.out.println("DIDUtil.getSIPProduct() ran in " + (System.currentTimeMillis() - start) + "ms");
		
		if (products.length < 1)
		{
			fail("No products found");
		}
		else if (products.length > 1)
		{
			fail("Found " + products.length + " products");
		}
		else 
		{
			String sipAddress = DIDUtil.getSIPAddress(getCtx(), products[0], null);
			String sipDomain = DIDUtil.getSIPDomain(getCtx(), products[0], null);
			
			if (!address.equals(sipAddress))
			{
				fail("SIP Address " + sipAddress + " doesn't match assigned address " + address);
			}
			
			if (!domain.equals(sipDomain))
			{
				fail("SIP Domain " + sipDomain + " doesn't match assigned domain " + domain);
			}
		}
	}
	
	public void testGetVoicemailProducts()
	{
		String didNumber = createVoicemailProduct();
		Long start = System.currentTimeMillis();		
		MProduct[] products = DIDUtil.getVoicemailProducts(getCtx(), didNumber, null);
		
		if (SHOW_TIMING) 
			System.out.println("DIDUtil.getVoicemailProducts() ran in " + (System.currentTimeMillis() - start) + "ms");
		
		if (products.length < 1)
		{
			fail("No products found");
		}
		else if (products.length > 1)
		{
			fail("Found " + products.length + " products");
		}
		else 
		{
			String voicemailMailboxNumber = DIDUtil.getVoicemailMailboxNumber(getCtx(), products[0], null);
			
			if (!didNumber.equals(voicemailMailboxNumber))
			{
				fail("Mailbox number " + voicemailMailboxNumber + " doesn't match DID number " + didNumber);
			}
		}
	}
	
	public void testGetProducts()
	{
		// Tested by other methods
	}
	
	public void testGetProductsMultiAttributes()
	{
		String didNumber = createDIDProduct(true, true);
		
		int[] attributeIds = new int[]{DIDConstants.ATTRIBUTE_ID_DID_AREACODE, DIDConstants.ATTRIBUTE_ID_DID_NUMBER, 
									   DIDConstants.ATTRIBUTE_ID_DID_COUNTRYCODE, DIDConstants.ATTRIBUTE_ID_DID_COUNTRYID, 
									   DIDConstants.ATTRIBUTE_ID_DID_SUBSCRIBED, DIDConstants.ATTRIBUTE_ID_DID_PERMINCHARGES};
		
		String[] attributeValues = new String[]{AREA_CODE, didNumber, COUNTRY_CODE, COUNTRY_ID, "true", PER_MIN_CHARGES};
		
		MProduct[] products = DIDUtil.getProducts(getCtx(), attributeIds, attributeValues, null);
		if (products.length != 2)
			fail("Returned " + products.length + " products using 6 attributes");
		
		attributeIds = new int[]{DIDConstants.ATTRIBUTE_ID_DID_NUMBER};
		attributeValues = new String[]{didNumber};

		products = DIDUtil.getProducts(getCtx(), attributeIds, attributeValues, null);
		
		if (products.length != 2)
			fail("Returned " + products.length + " products using 1 attribute");
	}
	
// *****************************************************************************************************************************************

	public void testIsMSubscribed()
	{
		// TODO
	}
	
	public void testIsSubscribed()
	{
		String didNumber = createDIDProduct(true, true);
		MProduct[] products = DIDUtil.getDIDProducts(getCtx(), didNumber, null);
		
		if (!DIDUtil.isSubscribed(getCtx(), products[0], null))
			fail("Product isn't subscribed " + products[0]);
		
		if (!DIDUtil.isSubscribed(getCtx(), products[1], null))
			fail("Product isn't subscribed " + products[1]);
		
		didNumber = createDIDProduct(true, false);
		products = DIDUtil.getDIDProducts(getCtx(), didNumber, null);
		
		if (DIDUtil.isSubscribed(getCtx(), products[0], null))
			fail("Product is subscribed " + products[0]);
		
		if (DIDUtil.isSubscribed(getCtx(), products[1], null))
			fail("Product is subscribed " + products[1]);
	}
	
	public void testIsSetup()
	{
		String didNumber = createDIDProduct(true, false);
		MProduct[] products = DIDUtil.getDIDProducts(getCtx(), didNumber, null);
		
		boolean prod1IsSetup = DIDUtil.isSetup(getCtx(), products[0], null);
		boolean prod2IsSetup = DIDUtil.isSetup(getCtx(), products[1], null);
		
		if (prod1IsSetup == prod2IsSetup)
			fail("No setup product " + products[0] + " and " + products[1]);
	}
	
	public void testIsDIDxNumber()
	{
		String didNumber = createDIDProduct(true, false);
		MProduct[] products = DIDUtil.getDIDProducts(getCtx(), didNumber, null);
		
		boolean prod1IsDIDx = DIDUtil.isDIDxNumber(getCtx(), products[0]);
		boolean prod2IsDIDx = DIDUtil.isDIDxNumber(getCtx(), products[0]);
		
		if (!prod1IsDIDx)
			fail("Product isn't DIDx number " + products[0]);
		
		if (!prod2IsDIDx)
			fail("Product isn't DIDx number " + products[1]);
		
		didNumber = createDIDProduct(false, false);
		products = DIDUtil.getDIDProducts(getCtx(), didNumber, null);
		
		prod1IsDIDx = DIDUtil.isDIDxNumber(getCtx(), products[0]);
		prod2IsDIDx = DIDUtil.isDIDxNumber(getCtx(), products[0]);
		
		if (prod1IsDIDx)
			fail("Product is DIDx number " + products[0]);
		
		if (prod2IsDIDx)
			fail("Product is DIDx number " + products[1]);
	}
	
	public void testGetDIDNumber()
	{
		String didNumber = createDIDProduct(true, false);
		MProduct[] products = DIDUtil.getDIDProducts(getCtx(), didNumber, null);
		
		String prod1DIDNumber = DIDUtil.getDIDNumber(getCtx(), products[0], null);
		String prod2DIDNumber = DIDUtil.getDIDNumber(getCtx(), products[1], null);
		
		if (!prod1DIDNumber.equals(prod2DIDNumber))
			fail("Product numbers don't match each other " + prod1DIDNumber + " and " + prod2DIDNumber);
		else if (!didNumber.equals(prod1DIDNumber))
			fail("Initial number " + didNumber + " doesn't match saved DID number " + prod1DIDNumber);
	}
	
	public void testGetSIPAddress()
	{
		String didNumber = createSIPProduct();
		MProduct[] products = DIDUtil.getSIPProducts(getCtx(), didNumber, null);
		
		String sipAddress = DIDUtil.getSIPAddress(getCtx(), products[0], null);
		
		if (!didNumber.equals(sipAddress))
			fail("Initial number " + didNumber + " doesn't match saved SIP address " + sipAddress);
	}
	
	public void testGetSIPDomain()
	{
		String didNumber = createSIPProduct();
		MProduct[] products = DIDUtil.getSIPProducts(getCtx(), didNumber, null);
		
		String sipDomain = DIDUtil.getSIPDomain(getCtx(), products[0], null);
		
		if (!DIDConstants.DEFAULT_SIP_DOMAIN.equals(sipDomain))
			fail("Initial number " + didNumber + " doesn't match saved SIP domain " + sipDomain);
	}
	
	public void testGetSIPURI()
	{
		String didNumber = createSIPProduct();
		MProduct[] products = DIDUtil.getSIPProducts(getCtx(), didNumber, null);
		
		String sipURI = DIDUtil.getSIPURI(getCtx(), products[0], null);
		
		String correctURI = didNumber + "@" + DIDConstants.DEFAULT_SIP_DOMAIN;
		
		if (!sipURI.equals(correctURI))
			fail("SIP URI " + didNumber + " doesn't match correct URI " + correctURI);
	}
	
	public void testGetVoicemailMailboxNumber()
	{
		String didNumber = createVoicemailProduct();
		MProduct[] products = DIDUtil.getVoicemailProducts(getCtx(), didNumber, null);
		
		String mailboxNumber = DIDUtil.getVoicemailMailboxNumber(getCtx(), products[0], null);
		
		if (!didNumber.equals(mailboxNumber))
			fail("Initial number " + didNumber + " doesn't match saved mailbox number " + mailboxNumber);
	}
	
	public void testGetDIDDescription()
	{
		String didNumber = createDIDProduct(true, false);
		MProduct[] products = DIDUtil.getDIDProducts(getCtx(), didNumber, null);
		
		DIDDescription didDesc = DIDUtil.getDIDDescription(getCtx(), products[0], null);
		
		if (!AREA_CODE.equals(didDesc.getAreaCode()))
			fail("Area code " + AREA_CODE + " doesn't match saved area code " + didDesc.getAreaCode());
		else if (!COUNTRY_CODE.equals(didDesc.getCountryCode()))
			fail("Country code " + COUNTRY_CODE + " doesn't match saved country code " + didDesc.getCountryCode());
		else if (!FREE_MINS.equals(didDesc.getFreeMins()))
			fail("Free minutes " + FREE_MINS + " doesn't match saved free minutes " + didDesc.getFreeMins());
		else if (!PER_MIN_CHARGES.equals(didDesc.getPerMinCharges()))
			fail("Per min charges " + PER_MIN_CHARGES + " doesn't match saved area code " + didDesc.getPerMinCharges());
	}
	
	public void testGetAttributeInstance()
	{
		// Tested by other methods
	}
	
	public void testGetAttributeInstanceValue()
	{
		// Tested by other methods
	}
	
	public void testGetSetupOrMonthlyProduct()
	{
		String didNumber = createDIDProduct(true, false);
		MProduct[] products = DIDUtil.getDIDProducts(getCtx(), didNumber, null);
		
		MProduct setupProduct = DIDUtil.getSetupOrMonthlyProduct(getCtx(), products[0], products[1], true, null);
		MProduct monthlyProduct = DIDUtil.getSetupOrMonthlyProduct(getCtx(), products[0], products[1], false, null);
		
		if (!DIDUtil.isSetup(getCtx(), setupProduct, null))
			fail("Setup product expected " + setupProduct);
		else if (DIDUtil.isSetup(getCtx(), monthlyProduct, null))
			fail("Monthly product expected " + monthlyProduct);
	}
	
	public void testGetNumbersFromOrder()
	{
		// TODO: Finish me
		// Need to create mock order
		// Make sure method returns only one number for product pair
	}
	
	public void testGetSubscriptionDates()
	{
		// TODO: Finish me
	}
	
// *****************************************************************************************************************************************
	
	public void testloadLocalDIDCountryProducts() 
	{		
		String didNumber = createDIDProduct(true, false);
		DIDCountry country = createNZCountry();
		
		long start = System.currentTimeMillis();
		DIDUtil.loadLocalAreaCodes(getCtx(), country, null); // load all area codes
		
		if (SHOW_TIMING)
			System.out.println("loadLocalDIDCountryProducts()[area code only] ran in " + (System.currentTimeMillis() - start) + "ms");
		
		boolean found = false;
		for (DIDAreaCode areaCode : country.getAreaCodes())
		{
			if (AREA_CODE.equals(areaCode.getCode()))
				found = true;
		}
		
		if (!found)
			fail("Didn't load test area code " + AREA_CODE);
		
		start = System.currentTimeMillis();		
		DIDUtil.loadLocalDIDs(getCtx(), country, null); // load all unsubscribed DIDs for each area code
		
		if (SHOW_TIMING) 
			System.out.println("loadLocalDIDCountryProducts()[load DIDs] ran in " + (System.currentTimeMillis() - start) + "ms");
		
		found = false;
		for (DIDAreaCode areaCode : country.getAreaCodes())
		{
			if (AREA_CODE.equals(areaCode.getCode()))
			{
				for (DID did : areaCode.getAllDIDs())
				{
					if (didNumber.equals(did.getNumber()))
						found = true;
				}
			}
		}
		
		if (!found)
			fail("Didn't load test DID " + didNumber);
	}
	
// *****************************************************************************************************************************************
	
	public void testGetSubscribedFaxNumbers()
	{
		// Get subscribed fax numbers to update billing data
		ArrayList<String> subscribedFaxNumbers = DIDUtil.getSubscribedFaxNumbers(getCtx(), null);
		
		for (String subscribedFaxNumber : subscribedFaxNumbers)
			System.out.println(subscribedFaxNumber);		
	}
}
