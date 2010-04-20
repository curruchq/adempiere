package com.conversant.test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MProduct;
import org.compiere.model.MSubscription;
import org.compiere.util.CLogger;
import org.compiere.wstore.DIDController;
import org.compiere.wstore.DIDDescription;

import com.conversant.model.DID;
import com.conversant.model.DIDAreaCode;
import com.conversant.model.DIDCountry;
import com.conversant.wstore.DIDConstants;
import com.conversant.wstore.DIDUtil;

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
	
	private String createDIDProduct(boolean superTech, boolean subscribed)
	{
		Random rn = new Random();
		String didNumber = COUNTRY_CODE + AREA_CODE + (rn.nextInt(8999) + 1000);
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
		Random rn = new Random();
		String didNumber = "64" + AREA_CODE + (rn.nextInt(8999) + 1000);
		
		MProduct cvoiceProduct = DIDController.createMProduct(getCtx(), DIDController.getCVoiceFields(didNumber));
		DIDController.updateSIPProductAttributes(getCtx(), cvoiceProduct, didNumber, DIDConstants.DEFAULT_SIP_DOMAIN);
		
		System.out.println("Created CVoice product " + cvoiceProduct);
		
		return didNumber;
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

	public void testCreateSubscription()
	{
		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put(MSubscription.COLUMNNAME_Name, "Test Subscription");
		fields.put(MSubscription.COLUMNNAME_C_BPartner_ID, 1000071); 
		fields.put(MSubscription.COLUMNNAME_M_Product_ID, 1000000);
		fields.put(MSubscription.COLUMNNAME_C_SubscriptionType_ID, 1000004); 		
		fields.put(MSubscription.COLUMNNAME_StartDate, new Timestamp(System.currentTimeMillis()));
		fields.put(MSubscription.COLUMNNAME_PaidUntilDate, new Timestamp(System.currentTimeMillis())); 
		fields.put(MSubscription.COLUMNNAME_RenewalDate, new Timestamp(System.currentTimeMillis())); 
		fields.put(MSubscription.COLUMNNAME_IsDue, true);
		
		MSubscription subscription = DIDUtil.createSubscription(getCtx(), fields, null);
		if (subscription == null)
			fail("Failed to create subscription");
		else
			subscription.delete(true);
	}
	
// *****************************************************************************************************************************************
	
	public void testGetBySubscription()
	{
		MAttribute didIsSubscribedAttribute = new MAttribute(getCtx(), DIDConstants.ATTRIBUTE_ID_DID_SUBSCRIBED, null); 
		Long start = System.currentTimeMillis();		
		MProduct[] products = DIDUtil.getBySubscription(getCtx(), false);
		
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
		MProduct[] products = DIDUtil.getAllDIDProducts(getCtx());
		
		if (SHOW_TIMING) 
			System.out.println("DIDUtil.getAllDIDProducts() ran in " + (System.currentTimeMillis() - start) + "ms");
		
		boolean found = false;
		for (MProduct product : products)
		{
			if (didNumber.equals(DIDUtil.getDIDNumber(getCtx(), product)))
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
		MProduct[] products = DIDUtil.getDIDProducts(getCtx(), didNumber);
		
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
			String num1 = DIDUtil.getDIDNumber(getCtx(), products[0]);
			String num2 = DIDUtil.getDIDNumber(getCtx(), products[1]);
			
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
		String didNumber = createSIPProduct();
		Long start = System.currentTimeMillis();		
		MProduct[] products = DIDUtil.getSIPProducts(getCtx(), didNumber);
		
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
			String sipAddress = DIDUtil.getSIPAddress(getCtx(), products[0]);
			
			if (!didNumber.equals(sipAddress))
			{
				fail("SIP Address " + sipAddress + " doesn't match DID number " + didNumber);
			}
		}
	}
	
	public void testGetVoicemailProducts()
	{
		String didNumber = createVoicemailProduct();
		Long start = System.currentTimeMillis();		
		MProduct[] products = DIDUtil.getVoicemailProducts(getCtx(), didNumber);
		
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
			String voicemailMailboxNumber = DIDUtil.getVoicemailMailboxNumber(getCtx(), products[0]);
			
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
	
// *****************************************************************************************************************************************

	public void testIsMSubscribed()
	{
		// TODO
	}
	
	public void testIsSubscribed()
	{
		String didNumber = createDIDProduct(true, true);
		MProduct[] products = DIDUtil.getDIDProducts(getCtx(), didNumber);
		
		if (!DIDUtil.isSubscribed(getCtx(), products[0]))
			fail("Product isn't subscribed " + products[0]);
		
		if (!DIDUtil.isSubscribed(getCtx(), products[1]))
			fail("Product isn't subscribed " + products[1]);
		
		didNumber = createDIDProduct(true, false);
		products = DIDUtil.getDIDProducts(getCtx(), didNumber);
		
		if (DIDUtil.isSubscribed(getCtx(), products[0]))
			fail("Product is subscribed " + products[0]);
		
		if (DIDUtil.isSubscribed(getCtx(), products[1]))
			fail("Product is subscribed " + products[1]);
	}
	
	public void testIsSetup()
	{
		String didNumber = createDIDProduct(true, false);
		MProduct[] products = DIDUtil.getDIDProducts(getCtx(), didNumber);
		
		boolean prod1IsSetup = DIDUtil.isSetup(getCtx(), products[0]);
		boolean prod2IsSetup = DIDUtil.isSetup(getCtx(), products[1]);
		
		if (prod1IsSetup == prod2IsSetup)
			fail("No setup product " + products[0] + " and " + products[1]);
	}
	
	public void testIsDIDxNumber()
	{
		String didNumber = createDIDProduct(true, false);
		MProduct[] products = DIDUtil.getDIDProducts(getCtx(), didNumber);
		
		boolean prod1IsDIDx = DIDUtil.isDIDxNumber(getCtx(), products[0]);
		boolean prod2IsDIDx = DIDUtil.isDIDxNumber(getCtx(), products[0]);
		
		if (!prod1IsDIDx)
			fail("Product isn't DIDx number " + products[0]);
		
		if (!prod2IsDIDx)
			fail("Product isn't DIDx number " + products[1]);
		
		didNumber = createDIDProduct(false, false);
		products = DIDUtil.getDIDProducts(getCtx(), didNumber);
		
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
		MProduct[] products = DIDUtil.getDIDProducts(getCtx(), didNumber);
		
		String prod1DIDNumber = DIDUtil.getDIDNumber(getCtx(), products[0]);
		String prod2DIDNumber = DIDUtil.getDIDNumber(getCtx(), products[1]);
		
		if (!prod1DIDNumber.equals(prod2DIDNumber))
			fail("Product numbers don't match each other " + prod1DIDNumber + " and " + prod2DIDNumber);
		else if (!didNumber.equals(prod1DIDNumber))
			fail("Initial number " + didNumber + " doesn't match saved DID number " + prod1DIDNumber);
	}
	
	public void testGetSIPAddress()
	{
		String didNumber = createSIPProduct();
		MProduct[] products = DIDUtil.getSIPProducts(getCtx(), didNumber);
		
		String sipAddress = DIDUtil.getSIPAddress(getCtx(), products[0]);
		
		if (!didNumber.equals(sipAddress))
			fail("Initial number " + didNumber + " doesn't match saved SIP address " + sipAddress);
	}
	
	public void testGetSIPDomain()
	{
		String didNumber = createSIPProduct();
		MProduct[] products = DIDUtil.getSIPProducts(getCtx(), didNumber);
		
		String sipDomain = DIDUtil.getSIPDomain(getCtx(), products[0]);
		
		if (!DIDConstants.DEFAULT_SIP_DOMAIN.equals(sipDomain))
			fail("Initial number " + didNumber + " doesn't match saved SIP domain " + sipDomain);
	}
	
	public void testGetSIPURI()
	{
		String didNumber = createSIPProduct();
		MProduct[] products = DIDUtil.getSIPProducts(getCtx(), didNumber);
		
		String sipURI = DIDUtil.getSIPURI(getCtx(), products[0]);
		
		String correctURI = didNumber + "@" + DIDConstants.DEFAULT_SIP_DOMAIN;
		
		if (!sipURI.equals(correctURI))
			fail("SIP URI " + didNumber + " doesn't match correct URI " + correctURI);
	}
	
	public void testGetVoicemailMailboxNumber()
	{
		String didNumber = createVoicemailProduct();
		MProduct[] products = DIDUtil.getVoicemailProducts(getCtx(), didNumber);
		
		String mailboxNumber = DIDUtil.getVoicemailMailboxNumber(getCtx(), products[0]);
		
		if (!didNumber.equals(mailboxNumber))
			fail("Initial number " + didNumber + " doesn't match saved mailbox number " + mailboxNumber);
	}
	
	public void testGetDIDDescription()
	{
		String didNumber = createDIDProduct(true, false);
		MProduct[] products = DIDUtil.getDIDProducts(getCtx(), didNumber);
		
		DIDDescription didDesc = DIDUtil.getDIDDescription(getCtx(), products[0]);
		
		if (!AREA_CODE.equals(didDesc.getAreaCode()))
			fail("Area code " + AREA_CODE + " doesn't match saved area code " + didDesc.getAreaCode());
		else if (!COUNTRY_CODE.equals(didDesc.getCountryCode()))
			fail("Country code " + COUNTRY_CODE + " doesn't match saved country code " + didDesc.getCountryCode());
		else if (!FREE_MINS.equals(didDesc.getFreeMins()))
			fail("Free minutes " + FREE_MINS + " doesn't match saved free minutes " + didDesc.getFreeMins());
		else if (!PER_MIN_CHARGES.equals(didDesc.getPerMinCharges()))
			fail("Per min charges " + PER_MIN_CHARGES + " doesn't match saved area code " + didDesc.getPerMinCharges());
	}
	
	public void testGetAttributeInstanceValue()
	{
		// Tested by other methods
	}
	
	public void testGetSetupOrMonthlyProduct()
	{
		String didNumber = createDIDProduct(true, false);
		MProduct[] products = DIDUtil.getDIDProducts(getCtx(), didNumber);
		
		MProduct setupProduct = DIDUtil.getSetupOrMonthlyProduct(getCtx(), products[0], products[1], true);
		MProduct monthlyProduct = DIDUtil.getSetupOrMonthlyProduct(getCtx(), products[0], products[1], false);
		
		if (!DIDUtil.isSetup(getCtx(), setupProduct))
			fail("Setup product expected " + setupProduct);
		else if (DIDUtil.isSetup(getCtx(), monthlyProduct))
			fail("Monthly product expected " + monthlyProduct);
	}
	
	public void testGetNumbersFromOrder()
	{
		// TODO: Finish me
		// Need to create mock order
	}
	
// *****************************************************************************************************************************************
	
	public void testloadLocalDIDCountryProducts() 
	{		
		String didNumber = createDIDProduct(true, false);
		DIDCountry country = createNZCountry();
		
		long start = System.currentTimeMillis();
		DIDUtil.loadLocalAreaCodes(getCtx(), country); // load all area codes
		
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
		DIDUtil.loadLocalDIDs(getCtx(), country); // load all unsubscribed DIDs for each area code
		
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
}
