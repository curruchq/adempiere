package com.conversant.test;

import java.util.HashMap;

import org.compiere.model.MCountry;

import test.AdempiereTestCase;

public class AdempiereDataIntegrityTestCase extends AdempiereTestCase
{
//	private static CLogger log = CLogger.getCLogger(AdempiereDataIntegrityTestCase.class);
	
	private static final boolean SHOW_DETAIL = true;
	
	private static final int SUBSCRIBED_VALID = 1;
	private static final int SUBSCRIBED_MISSING_SUBSCRIPTION = 2;
	private static final int SUBSCRIBED_MULTIPLE_SUBSCRIPTIONS = 3;
	private static final int UNSUBSCRIBED_SUBSCRIPTIONS_FOUND = 4;
	
	private static final String ATTRIBUTE_DID_NUMBER = "DID_NUMBER";	
	private static final int STANDARD_SELLING_M_PRICELIST_ID = 1000000;
	private static final int DID_M_ATTRIBUTESET_ID = 1000002;
	private static final int DID_ISSETUP_ATTRIBUTE = 1000008;
	private static final int DID_NUMBER_ATTRIBUTE = 1000015;
	private static final int DID_SUBSCRIBED_ATTRIBUTE = 1000016;
	
	private static final String INVALID_PRODUCT_NAME = "INVALID PRODUCT";
	
	private static final String NUMBER_IDENTIFIER = "##NUMBER##";
	
	// TODO: Check with Cameron if all these products are used
	protected static final HashMap<Integer, String> DID_PRODUCTS_TO_SKIP = new HashMap<Integer, String>() 
	{
		{
//			put(1000044, "DIDSU-643-CHC");
//			put(1000045, "DIDSU-643-DUN");
//			put(1000036, "DIDSU-644-WGT");
//			put(1000046, "DIDSU-647-HAM");
//			put(1000048, "DIDSU-649-AKL");
//			put(1000047, "DIDSU-649-HBC");
//			put(1000050, "DIDSU-649-WHG");
//			put(1000063, "DID-331706");
//			put(1000061, "DID-441902");
//			put(1000062, "DID-44208");
//			put(1000052, "DID-61427-SMS");
			
//			put(1000029, "DID-643-CHC");
//			put(1000026, "DID-643-DUN");
//			put(1000027, "DID-644-WGT");
//			put(1000028, "DID-647-HAM");
//			put(1000001, "DID-649-AKL");
//			put(1000043, "DID-649-HBC");
//			put(1000049, "DID-649-WHG");
//			put(1000024, "DID-800");
//			put(1000064, "DID-ST-MIN");
//			put(1000530, "DID/DDI monthly charge template");
//			put(1000531, "DID/DDI setup fee template");
		}
	};
	
	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
	}
	
	@Override
	protected void tearDown() throws Exception 
	{
		super.tearDown();
	}
	
//	private HashMap[] seperateDIDProducts()
//	{
//		// Static reference to attributes
//		MAttribute didIsSetupAttribute = new MAttribute(getCtx(), DID_ISSETUP_ATTRIBUTE, null);
//		MAttribute didNumberAttribute = new MAttribute(getCtx(), DID_NUMBER_ATTRIBUTE, null); 
//		
//		// Hashmaps to hold products		
//		HashMap<String, MProduct> setupProducts = new HashMap<String, MProduct>();
//		HashMap<String, MProduct> monthlyProducts = new HashMap<String, MProduct>();
//		
//		// Sort products in lists
//		for (MProduct product : DIDUtil.getAllDIDProducts(getCtx(), null))
//		{
//			MAttributeInstance mai_isSetup = didIsSetupAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
//			MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
//			
//			// Check values for both attributes exist
//			boolean attributeError = false;
//			if (mai_isSetup == null || mai_isSetup.getValue() == null || mai_isSetup.getValue().length() < 1)
//			{
//				print("Failed to load DID_ISSETUP for " + product);
//				attributeError = true;
//			}
//			
//			if (mai_didNumber == null || mai_didNumber.getValue() == null || mai_didNumber.getValue().length() < 1)
//			{
//				print("Failed to load DID_NUMBER for " + product);
//				attributeError = true;
//			}
//			
//			if (attributeError)
//				continue;
//						
//			// Load DID number
//			String didNumber = mai_didNumber.getValue().trim();
//						
//			// Put product in either setup or monthly struct
//			if (mai_isSetup.getValue().equalsIgnoreCase("true"))
//				setupProducts.put(didNumber, product);
//			
//			else if (mai_isSetup.getValue().equalsIgnoreCase("false"))
//				monthlyProducts.put(didNumber, product);
//			
//			else
//				print("Invalid DID_ISSETUP value for " + product + "DID_ISSETUP=" + mai_isSetup.getValue());
//		}
//		
//		return new HashMap[]{setupProducts, monthlyProducts};
//	}
//	
//	public void testSearchKeyAttributeValueMatch()
//	{
//		MAttribute didNumberAttribute = new MAttribute(getCtx(), DID_NUMBER_ATTRIBUTE, null); 
//		
//		
//		for (MProduct product : DIDUtil.getAllDIDProducts(getCtx(), null))
//		{
//			MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
//			
//			if (mai_didNumber == null || mai_didNumber.getValue() == null || mai_didNumber.getValue().length() < 1)
//			{
//				print("Failed to load DID_NUMBER for " + product);
//				continue;
//			}
//						
//			// Load DID number
//			String didNumber = mai_didNumber.getValue().trim();
//			String searchKeyDID = product.getValue().substring(product.getValue().lastIndexOf("-") + 1, product.getValue().length());
//			
//			if (!didNumber.equalsIgnoreCase(searchKeyDID))
//			{
//				print(product.getValue() + " doesn't not match DID_NUMBER[" + didNumber + "]");
//			}
//		}
//	}
//	
//	public void testGetDIDProducts()
//	{
//		MProduct[] allProducts_bySearchKey = MProduct.get(getCtx(), "(value LIKE 'DID-%' OR value LIKE 'DIDSU-%') AND UPPER(IsActive) = 'Y'", null);	
//		MProduct[] allProducts_byAttributeSet = MProduct.get(getCtx(), "M_AttributeSet_ID = " + DID_M_ATTRIBUTESET_ID + " AND UPPER(IsActive) = 'Y'", null);	
//		MProduct[] allProducts_byDIDUtil = DIDUtil.getAllDIDProducts(getCtx(), null);
//		
//		if (allProducts_bySearchKey.length != allProducts_byDIDUtil.length ||
//			allProducts_bySearchKey.length != allProducts_byAttributeSet.length || 
//			allProducts_byAttributeSet.length != allProducts_byDIDUtil.length)
//		{
//			print("Count mismatch --> " + 
//					   "BySearchKey=" + allProducts_bySearchKey.length + " " +
//					   "ByAttributeSet=" + allProducts_byAttributeSet.length + " " +
//					   "ByDIDUtil=" + allProducts_byDIDUtil.length);
//			
//			if (SHOW_DETAIL)
//			{
//				/* Show which products aren't in both bySearchKey & byAttributeSet */
//				if (allProducts_bySearchKey.length > allProducts_byAttributeSet.length)
//				{
//					for (MProduct product_bySearchKey : allProducts_bySearchKey)
//					{
//						if (!DID_PRODUCTS_TO_SKIP.containsKey(product_bySearchKey.getM_Product_ID()))
//						{
//							boolean found = false;
//							for (MProduct product_byAttributeSet : allProducts_byAttributeSet)
//							{
//								if (product_bySearchKey.getM_Product_ID() == product_byAttributeSet.getM_Product_ID())
//									found = true;
//							}
//							
//							if (!found)
//								System.out.println(product_bySearchKey + " was in BySearchKey but not ByAttributeSet");
//						}
//					}
//				}
//				// byAttributeSet is bigger or they're same size
//				else 
//				{
//					for (MProduct product_byAttributeSet : allProducts_byAttributeSet)
//					{
//						if (!DID_PRODUCTS_TO_SKIP.containsKey(product_byAttributeSet.getM_Product_ID()))
//						{
//							boolean found = false;
//							for (MProduct product_bySearchKey : allProducts_byAttributeSet)
//							{
//								if (product_byAttributeSet.getM_Product_ID() == product_bySearchKey.getM_Product_ID())
//									found = true;
//							}
//							
//							if (!found)
//								System.out.println(product_byAttributeSet + " was in ByAttributeSet but not BySearchKey");
//						}
//					}
//				}
//				
//				/* Show which products aren't in both bySearchKey & ByAttributeSetAttributeName */
//				if (allProducts_bySearchKey.length > allProducts_byDIDUtil.length)
//				{
//					for (MProduct product_bySearchKey : allProducts_bySearchKey)
//					{
//						if (!DID_PRODUCTS_TO_SKIP.containsKey(product_bySearchKey.getM_Product_ID()))
//						{
//							boolean found = false;
//							for (MProduct product_byAttributeSetAttributeName : allProducts_byDIDUtil)
//							{
//								if (product_bySearchKey.getM_Product_ID() == product_byAttributeSetAttributeName.getM_Product_ID())
//									found = true;
//							}
//							
//							if (!found)
//								System.out.println(product_bySearchKey + " was in BySearchKey but not ByDIDUtil");
//						}
//					}
//				}
//				// byAttributeSet is bigger or they're same size
//				else 
//				{
//					for (MProduct product_byAttributeSetAttributeName : allProducts_byDIDUtil)
//					{
//						if (!DID_PRODUCTS_TO_SKIP.containsKey(product_byAttributeSetAttributeName.getM_Product_ID()))
//						{
//							boolean found = false;
//							for (MProduct product_bySearchKey : allProducts_bySearchKey)
//							{
//								if (product_byAttributeSetAttributeName.getM_Product_ID() == product_bySearchKey.getM_Product_ID())
//									found = true;
//							}
//							
//							if (!found)
//								System.out.println(product_byAttributeSetAttributeName + " was in ByDIDUtil but not BySearchKey");
//						}
//					}
//				}
//				
//				/* Show which products aren't in both ByAttributeSet & ByAttributeSetAttributeName */
//				if (allProducts_byAttributeSet.length > allProducts_byDIDUtil.length)
//				{
//					for (MProduct product_byAttributeSet : allProducts_byAttributeSet)
//					{
//						if (!DID_PRODUCTS_TO_SKIP.containsKey(product_byAttributeSet.getM_Product_ID()))
//						{
//							boolean found = false;
//							for (MProduct product_byAttributeSetAttributeName : allProducts_byDIDUtil)
//							{
//								if (product_byAttributeSet.getM_Product_ID() == product_byAttributeSetAttributeName.getM_Product_ID())
//									found = true;
//							}
//							
//							if (!found)
//								System.out.println(product_byAttributeSet + " was in ByAttributeSet but not ByDIDUtil");
//						}
//					}
//				}
//				// ByAttributeSetAttributeName is bigger or they're same size
//				else 
//				{
//					for (MProduct product_byAttributeSetAttributeName : allProducts_byDIDUtil)
//					{
//						if (!DID_PRODUCTS_TO_SKIP.containsKey(product_byAttributeSetAttributeName.getM_Product_ID()))
//						{
//							boolean found = false;
//							for (MProduct product_byAttributeSet : allProducts_byAttributeSet)
//							{
//								if (product_byAttributeSetAttributeName.getM_Product_ID() == product_byAttributeSet.getM_Product_ID())
//									found = true;
//							}
//							
//							if (!found)
//								System.out.println(product_byAttributeSetAttributeName + " was in ByDIDUtil but not ByAttributeSet");
//						}
//					}
//				}
//			}
//		}
//	}
//	
//	public void testDIDProductPairs()
//	{
//		// Static reference to DID_ISSETUP
//		MAttribute didIsSetupAttribute = new MAttribute(getCtx(), DID_ISSETUP_ATTRIBUTE, null);
//		MAttribute didNumberAttribute = new MAttribute(getCtx(), DID_NUMBER_ATTRIBUTE, null); 
//		
//		// Hashmaps to hold products		
//		HashMap<String, MProduct> setupProducts = new HashMap<String, MProduct>();
//		HashMap<String, MProduct> monthlyProducts = new HashMap<String, MProduct>();
//		
//		// Sort products in lists
//		for (MProduct product : DIDUtil.getAllDIDProducts(getCtx(), null))
//		{
//			MAttributeInstance mai_isSetup = didIsSetupAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
//			MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
//			
//			// Check values for both attributes exist
//			boolean attributeError = false;
//			if (mai_isSetup == null || mai_isSetup.getValue() == null)
//			{
//				print("Failed to load DID_ISSETUP for " + product);
//				attributeError = true;
//			}
//			
//			if (mai_didNumber == null || mai_didNumber.getValue() == null || mai_didNumber.getValue().length() < 1)
//			{
//				print("Failed to load DID_NUMBER for " + product);
//				attributeError = true;
//			}
//			
//			if (attributeError)
//				continue;
//						
//			// Load DID number
//			String didNumber = mai_didNumber.getValue().trim();
//			
//			// TODO: Validate DID number?
//						
//			// Put product in either setup or monthly struct
//			if (mai_isSetup.getValue().equalsIgnoreCase("true"))
//				setupProducts.put(didNumber, product);
//			
//			else if (mai_isSetup.getValue().equalsIgnoreCase("false"))
//				monthlyProducts.put(didNumber, product);
//			
//			else
//				print("Invalid DID_ISSETUP value for " + product + "DID_ISSETUP=" + mai_isSetup.getValue());
//		}
//		
//		if (setupProducts.size() != monthlyProducts.size())
//		{
//			print("No. of setup products[" + setupProducts.size() + "] doesn't match no. of monthly products[" + monthlyProducts.size() + "]");
//			
//			if (SHOW_DETAIL)
//			{
//				System.out.println("Missing DID products");
//				Iterator<String> productIterator = setupProducts.keySet().iterator();				
//				while(productIterator.hasNext())
//				{
//					// Get key
//					String didNumber = productIterator.next();
//							
//					// Load products
//					MProduct setupProduct = setupProducts.get(didNumber);
//					MProduct monthlyProduct = monthlyProducts.get(didNumber);
//					
//					// Check both exist
//					if (setupProduct == null)
//						System.out.println("DIDSU-" + didNumber);
//
//					
//					if (monthlyProduct == null)
//						System.out.println("DID-" + didNumber);					
//				}
//				System.out.println("..");
//				productIterator = monthlyProducts.keySet().iterator();				
//				while(productIterator.hasNext())
//				{
//					// Get key
//					String didNumber = productIterator.next();
//							
//					// Load products
//					MProduct setupProduct = setupProducts.get(didNumber);
//					MProduct monthlyProduct = monthlyProducts.get(didNumber);
//					
//					// Check both exist
//					if (setupProduct == null)
//						System.out.println("DIDSU-" + didNumber);
//
//					
//					if (monthlyProduct == null)
//						System.out.println("DID-" + didNumber);					
//				}
//			}
//		}		
//	}
//	
//	public void testDIDProductNameInvalid()
//	{
//		for (MProduct product : DIDUtil.getAllDIDProducts(getCtx(), null))
//		{
//			if (product.getName() == null)
//				print(product + " NULL name");
//			else if (product.getName().equals(INVALID_PRODUCT_NAME))
//				print(product + " found invalid name");
//		}
//	}
//	
//	public void testDIDProductSearchKeys()
//	{
//		for (MProduct product : DIDUtil.getAllDIDProducts(getCtx(), null))
//		{
//			String searchKey = product.getValue();
//			if (searchKey == null)
//				print(product + " NULL search key");
//			else if (searchKey.matches(".*\\s+.*"))
//				print(product + " search key contains whitespace");			
//		}
//	}
//	
//	public void testDIDProductPrices()
//	{
//		MPriceList pl = MPriceList.get(getCtx(), STANDARD_SELLING_M_PRICELIST_ID, null);
//		MPriceListVersion plv = pl.getPriceListVersion(new Timestamp(System.currentTimeMillis()));
//		
//		for (MProduct product : DIDUtil.getAllDIDProducts(getCtx(), null))
//		{
//			MProductPrice productPrice = MProductPrice.get(getCtx(), plv.getM_PriceList_Version_ID(), product.getM_Product_ID(), null);
//			
//			if (productPrice == null)
//				print(product + " no MProductPrice found " + pl + " " + plv);
//			else
//			{
//				BigDecimal priceLimit = (BigDecimal)productPrice.get_Value(MProductPrice.COLUMNNAME_PriceLimit);
//				BigDecimal priceList = (BigDecimal)productPrice.get_Value(MProductPrice.COLUMNNAME_PriceList);
//				BigDecimal priceStd = (BigDecimal)productPrice.get_Value(MProductPrice.COLUMNNAME_PriceStd);
//				
//				if (priceLimit == null)
//					print(product + " no MProductPrice-PriceLimit found");
//				
//				if (priceList == null)
//					print(product + " no MProductPrice-PriceList found");
//				
//				if (priceStd == null)
//					print(product + " no MProductPrice-PriceStd found");
//			}
//		}
//	}
//	
//	public void testDIDSubscribedSubscriptions()
//	{
//		// TODO: Pull out into seperate method as could be re used
//		// Static reference to DID_ISSETUP
//		MAttribute didIsSetupAttribute = new MAttribute(getCtx(), DID_ISSETUP_ATTRIBUTE, null);
//		MAttribute didNumberAttribute = new MAttribute(getCtx(), DID_NUMBER_ATTRIBUTE, null); 
//		
//		// Hashmaps to hold products		
//		HashMap<String, MProduct> setupProducts = new HashMap<String, MProduct>();
//		HashMap<String, MProduct> monthlyProducts = new HashMap<String, MProduct>();
//		
//		// Sort products in lists
//		for (MProduct product : DIDUtil.getAllDIDProducts(getCtx(), null))
//		{
//			MAttributeInstance mai_isSetup = didIsSetupAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
//			MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
//			
//			// Check values for both attributes exist
//			boolean attributeError = false;
//			if (mai_isSetup == null || mai_isSetup.getValue() == null || mai_isSetup.getValue().length() < 1)
//			{
//				print("Failed to load DID_ISSETUP for " + product);
//				attributeError = true;
//			}
//			
//			if (mai_didNumber == null || mai_didNumber.getValue() == null || mai_didNumber.getValue().length() < 1)
//			{
//				print("Failed to load DID_NUMBER for " + product);
//				attributeError = true;
//			}
//			
//			if (attributeError)
//				continue;
//						
//			// Load DID number
//			String didNumber = mai_didNumber.getValue().trim();
//						
//			// Put product in either setup or monthly struct
//			if (mai_isSetup.getValue().equalsIgnoreCase("true"))
//				setupProducts.put(didNumber, product);
//			
//			else if (mai_isSetup.getValue().equalsIgnoreCase("false"))
//				monthlyProducts.put(didNumber, product);
//			
//			else
//				print("Invalid DID_ISSETUP value for " + product + "DID_ISSETUP=" + mai_isSetup.getValue());
//		}
//		
//		// Sort products into four lists
//		HashMap<Integer, ArrayList<MProduct>> sortedSetupProducts = sortProductsBySubscription(setupProducts);
//		HashMap<Integer, ArrayList<MProduct>> sortedMonthlyProducts = sortProductsBySubscription(monthlyProducts);
//		
//		// Seperate out the four lists
//		ArrayList<MProduct> subscribedValidSetupProducts = sortedSetupProducts.get(SUBSCRIBED_VALID);
//		ArrayList<MProduct> subscribedValidMonthlyProducts = sortedMonthlyProducts.get(SUBSCRIBED_VALID);
//		
//		ArrayList<MProduct> subscribedMissingSubscriptionSetupProducts = sortedSetupProducts.get(SUBSCRIBED_MISSING_SUBSCRIPTION);
//		ArrayList<MProduct> subscribedMissingSubscriptionMonthlyProducts = sortedMonthlyProducts.get(SUBSCRIBED_MISSING_SUBSCRIPTION);
//		
//		ArrayList<MProduct> subscribedMultipleSubscriptionsSetupProducts = sortedSetupProducts.get(SUBSCRIBED_MULTIPLE_SUBSCRIPTIONS);
//		ArrayList<MProduct> subscribedManySubscriptionsMonthlyProducts = sortedMonthlyProducts.get(SUBSCRIBED_MULTIPLE_SUBSCRIPTIONS);
//		
//		ArrayList<MProduct> unsubscribedSubscriptionsFoundSetupProducts = sortedSetupProducts.get(UNSUBSCRIBED_SUBSCRIPTIONS_FOUND);
//		ArrayList<MProduct> unsubscribedSubscriptionsFoundMonthlyProducts = sortedMonthlyProducts.get(UNSUBSCRIBED_SUBSCRIPTIONS_FOUND);
//		
//		// Check size between setup and monthly matching lists
//		if (subscribedValidSetupProducts.size() != subscribedValidMonthlyProducts.size())
//			print("Valid subscribed DIDSU[" + subscribedValidSetupProducts.size() + "] count does not match DID[" + subscribedValidMonthlyProducts.size() + "] count");
//		
//		if (subscribedMissingSubscriptionSetupProducts.size() != subscribedMissingSubscriptionMonthlyProducts.size())
//			print("Missing subscriptions for DIDSU[" + subscribedMissingSubscriptionSetupProducts.size() + "] count does not match DID[" + subscribedMissingSubscriptionMonthlyProducts.size() + "] count");
//		
//		if (subscribedMultipleSubscriptionsSetupProducts.size() != subscribedManySubscriptionsMonthlyProducts.size())
//			print("Multiple subscriptions for DIDSU[" + subscribedMultipleSubscriptionsSetupProducts.size() + "] count does not match DID[" + subscribedManySubscriptionsMonthlyProducts.size() + "] count");
//		
//		if (unsubscribedSubscriptionsFoundSetupProducts.size() != unsubscribedSubscriptionsFoundMonthlyProducts.size())
//			print("Unsubscribed subscriptions found for DIDSU[" + unsubscribedSubscriptionsFoundSetupProducts.size() + "] count does not match DID[" + unsubscribedSubscriptionsFoundMonthlyProducts.size() + "] count");
//		
//		
//		print("\n--- Subscribed products (DID_SUBSCRIBED flag true) without an active subscription ---");
//		for (MProduct product : subscribedMissingSubscriptionSetupProducts)
//			print(product.getValue());
//		
//		for (MProduct product : subscribedMissingSubscriptionMonthlyProducts)
//			print(product.getValue());
//		
//		print("\n--- Multiple active subscriptions ---");
//		for (MProduct product : subscribedMultipleSubscriptionsSetupProducts)
//			print(product.getValue());
//		
//		for (MProduct product : subscribedManySubscriptionsMonthlyProducts)
//			print(product.getValue());
//		
//		print("\n--- Unsubscribed products (DID_SUBSCRIBED flag false) with active subscriptions ---");
//		for (MProduct product : unsubscribedSubscriptionsFoundSetupProducts)
//			print(product.getValue());
//		
//		for (MProduct product : unsubscribedSubscriptionsFoundMonthlyProducts)
//			print(product.getValue());
//		
////		// Sort odd products from the two valid subscription lists (outer loop needs to be largest)
////		print("\n----------------- Odd products from the two valid subscription lists -----------------");
////		if (subscribedValidSetupProducts.size() >= subscribedValidMonthlyProducts.size())
////		{
////			for (MProduct setupProduct : subscribedValidSetupProducts)
////			{
////				boolean monthlyProductMatchFound = false; 
////				
////				MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(setupProduct.getM_AttributeSetInstance_ID());
////				String setupDIDNumber = mai_didNumber.getValue().trim();
////				
////				for (MProduct monthlyProduct : subscribedValidMonthlyProducts)
////				{
////					mai_didNumber = didNumberAttribute.getMAttributeInstance(monthlyProduct.getM_AttributeSetInstance_ID());
////					String monthlyDIDNumber = mai_didNumber.getValue().trim();
////					
////					if (setupDIDNumber.equalsIgnoreCase(monthlyDIDNumber))
////					{
////						monthlyProductMatchFound = true;
////						break;
////					}
////				}
////				
////				if (!monthlyProductMatchFound)
////					print("Missing subscription for DID[" + setupDIDNumber + "]");
////			}
////		}
////		else
////		{
////			for (MProduct monthlyProduct : subscribedValidMonthlyProducts)
////			{
////				boolean setupProductMatchFound = false; 
////				
////				MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(monthlyProduct.getM_AttributeSetInstance_ID());
////				String monthlyDIDNumber = mai_didNumber.getValue().trim();
////				
////				for (MProduct setupProduct : subscribedValidSetupProducts)
////				{
////					mai_didNumber = didNumberAttribute.getMAttributeInstance(setupProduct.getM_AttributeSetInstance_ID());
////					String setupDIDNumber = mai_didNumber.getValue().trim();
////					
////					if (setupDIDNumber.equalsIgnoreCase(monthlyDIDNumber))
////					{
////						setupProductMatchFound = true;
////						break;
////					}
////				}
////				
////				if (!setupProductMatchFound)
////					print("Missing subscription for DIDSU[" + monthlyDIDNumber + "]");
////			}
////		}
//		
//		// TODO: Check product pairs SUBSCRIBED flags match
//	}
//	
//	public void testSubscriptionBusinessPartnerLocations()
//	{
//		Timestamp currentDate = new Timestamp(System.currentTimeMillis());
//		
//		for (int id : MSubscription.getAllIDs(MSubscription.Table_Name, "UPPER(IsActive)='Y' AND AD_Client_ID=1000000", null))
//		{
//			MSubscription subscription = new MSubscription(getCtx(), id, null);
//			if (subscription != null)
//			{
//				if (currentDate.compareTo(subscription.getRenewalDate()) <= 0)
//				{
//					if (subscription.get_Value(MBPartnerLocation.COLUMNNAME_C_BPartner_Location_ID) != null)
//					{
//						int businessPartnerLocationId = (Integer)subscription.get_Value(MBPartnerLocation.COLUMNNAME_C_BPartner_Location_ID);
//						if (!Validation.validateADId(MBPartnerLocation.Table_Name, businessPartnerLocationId, null))
//							System.out.println("MSubscription[" + subscription.getC_Subscription_ID() + "-" + subscription.getName() + "] BPLocationId[" + businessPartnerLocationId + "]");
//					}
////					else
////						System.out.println("No BP Location set for MSubscription[" + id + "]");
//				}
//			}
//			else
//				System.out.println("Failed to load MSubscription[" + id + "]");
//		}
//	}
//	
//	public void testCallProducts()
//	{
//		// Static reference to DID_ISSETUP
//		MAttribute cdrDirectionAttribute = new MAttribute(getCtx(), DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION, null);
//		MAttribute cdrNumberAttribute = new MAttribute(getCtx(), DIDConstants.ATTRIBUTE_ID_CDR_NUMBER, null); 
//		
//		// Hashmaps to hold products		
//		HashMap<String, MProduct> inboundProducts = new HashMap<String, MProduct>();
//		HashMap<String, MProduct> outboundProducts = new HashMap<String, MProduct>();
//		
//		// Sort products in lists
//		for (MProduct product : DIDUtil.getAllCallProducts(getCtx(), null))
//		{
//			MAttributeInstance mai_direction = cdrDirectionAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
//			MAttributeInstance mai_number = cdrNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
//			
//			// Check values for both attributes exist
//			boolean attributeError = false;
//			if (mai_direction == null || mai_direction.getValue() == null)
//			{
//				print("Failed to load CDR_DIRECTION for " + product);
//				attributeError = true;
//			}
//			
//			if (mai_number == null || mai_number.getValue() == null || mai_number.getValue().length() < 1)
//			{
//				print("Failed to load CDR_NUMBER for " + product);
//				attributeError = true;
//			}
//			
//			if (attributeError)
//				continue;
//						
//			// Load number
//			String number = mai_number.getValue().trim();
//						
//			// Put product in either inbound or outbound struct
//			if (mai_direction.getM_AttributeValue_ID() == DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_INBOUND)
//				inboundProducts.put(number, product);			
//			else if (mai_direction.getM_AttributeValue_ID() == DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_OUTBOUND)
//				outboundProducts.put(number, product);			
//			else
//				print("Invalid CDR_DIRECTION value id for " + product + "CDR_DIRECTION=" + mai_direction.getM_AttributeValue_ID());
//		}
//		
//		if (inboundProducts.size() != outboundProducts.size())
//		{
//			print("No. of inbound products[" + inboundProducts.size() + "] doesn't match no. of outbound products[" + outboundProducts.size() + "]");
//			
//			if (SHOW_DETAIL)
//			{
//				System.out.println("Missing CALL products");
//				Iterator<String> productIterator = outboundProducts.keySet().iterator();				
//				while(productIterator.hasNext())
//				{
//					// Get key
//					String number = productIterator.next();
//							
//					// Load products
//					MProduct outboundProduct = outboundProducts.get(number);
//					MProduct inboundProduct = inboundProducts.get(number);
//					
//					// Check both exist
//					if (outboundProduct == null)
//						System.out.println("CALL-OUT-" + number);
//
//					
//					if (inboundProduct == null)
//						System.out.println("CALL-IN-" + number);					
//				}
//				
//				System.out.println("..");
//				productIterator = inboundProducts.keySet().iterator();				
//				while(productIterator.hasNext())
//				{
//					// Get key
//					String number = productIterator.next();
//							
//					// Load products
//					MProduct outboundProduct = outboundProducts.get(number);
//					MProduct inboundProduct = inboundProducts.get(number);
//					
//					// Check both exist
//					if (outboundProduct == null)
//						System.out.println("CALL-OUT-" + number);
//
//					
//					if (inboundProduct == null)
//						System.out.println("CALL-IN-" + number);					
//				}
//			}
//		}		
//	}
	
//	public void testCallStdSubs()
//	{		
//		String trxName = Trx.createTrxName("testCallStdSubs");
//		
//		System.out.println("Numbers missing CALL-STD-OUT subs");
//		
//// ------------ Hack to allow product retrieval ---------------
////		
////		int AD_Client_ID = Env.getAD_Client_ID(getCtx());
////		Env.setContext(getCtx(), "#AD_Client_ID", "1000000");
//
////------------------------------------------------------------
//		
//		try
//		{	
//			HashMap<String, MProduct> didProducts = new HashMap<String, MProduct>();
//			for (MProduct product : DIDUtil.getAllDIDProducts(getCtx(), trxName))
//			{
//				if (DIDUtil.isMSubscribed(getCtx(), product))
//				{
//					String number = DIDUtil.getDIDNumber(getCtx(), product, trxName);				
//					didProducts.put(number, product);
//				}
//			}
//			
//			MSubscription[] callStdOutSubs = MSubscription.getSubscriptions(getCtx(), 1000002, trxName);
//			
//			Iterator<String> didIterator = didProducts.keySet().iterator();
//			while(didIterator.hasNext())
//			{
//				String didNumber = (String)didIterator.next();
//				MProduct didProduct = (MProduct)didProducts.get(didNumber);
//				
//				boolean found = false;
//								
//				for (MSubscription subscription : callStdOutSubs)
//				{
//					if (!DIDUtil.isActiveMSubscription(getCtx(), subscription))
//						continue;
//					
//					if (subscription.getName().equals("+" + didNumber))
//					{
//						found = true;
//						break;
//					}
//				}
//				
//				if (!found)
//				{
//					for (MProduct callProduct : DIDUtil.getCallProducts(getCtx(), didNumber, trxName))
//					{
//						for (MSubscription subscription : MSubscription.getSubscriptions(getCtx(), callProduct.getM_Product_ID(), trxName))
//						{
//							if (!DIDUtil.isActiveMSubscription(getCtx(), subscription))
//								continue;
//							
//							if (subscription.getName().contains(didNumber))
//							{
//								found = true;
//								break;
//							}
//						}
//					}
//				}
//				
//				if (!found)
//					System.out.println(didNumber);
//			}
//		}
//		catch (Exception ex)
//		{
//			
//		}
//		finally
//		{
//			// Rollback trx
//			Trx trx = Trx.get(trxName, false);
//			if (trx != null && trx.isActive())
//			{
//				trx.rollback();
//				trx.close();
//			}		
//		}
//	}
//	
//	private HashMap<Integer, ArrayList<MProduct>> sortProductsBySubscription(HashMap<String, MProduct> products)
//	{
//		ArrayList<MProduct> subscribedValid = new ArrayList<MProduct>();
//		ArrayList<MProduct> subscribedMissingSubscription = new ArrayList<MProduct>();
//		ArrayList<MProduct> subscribedMultipleSubscriptions = new ArrayList<MProduct>();
//		ArrayList<MProduct> unsubscribedSubscriptionsFound = new ArrayList<MProduct>();
//		
//		Iterator<String> productIterator = products.keySet().iterator();				
//		while(productIterator.hasNext())
//		{
//			String didNumber = productIterator.next();			
//			MProduct product = products.get(didNumber);				
//			boolean subscribed = isProductSubscribed(product);
//
//			int validSubscriptionCount = 0;
//
//			for (MSubscription subscription : MSubscription.getSubscriptions(getCtx(), product.getM_Product_ID(), null))
//			{			
//				if (isSubscriptionActive(subscription))
//					validSubscriptionCount++;
//			}
//			
//			if (subscribed && validSubscriptionCount == 1)
//			{
//				subscribedValid.add(product);
//			}
//			else if (subscribed && validSubscriptionCount < 1)
//			{
//				subscribedMissingSubscription.add(product);
//			}
//			else if (subscribed && validSubscriptionCount > 1)
//			{
//				subscribedMultipleSubscriptions.add(product);	
//			}
//			else if (!subscribed && validSubscriptionCount > 0)
//			{
//				unsubscribedSubscriptionsFound.add(product);
//			}
//		}
//		
//		HashMap<Integer, ArrayList<MProduct>> sortedProducts = new HashMap<Integer, ArrayList<MProduct>>();
//		sortedProducts.put(SUBSCRIBED_VALID, subscribedValid);
//		sortedProducts.put(SUBSCRIBED_MISSING_SUBSCRIPTION, subscribedMissingSubscription);
//		sortedProducts.put(SUBSCRIBED_MULTIPLE_SUBSCRIPTIONS, subscribedMultipleSubscriptions);			
//		sortedProducts.put(UNSUBSCRIBED_SUBSCRIPTIONS_FOUND, unsubscribedSubscriptionsFound);
//		
//		return sortedProducts;
//	}
//	
//	private boolean isSubscriptionActive(MSubscription subscription)
//	{
//		// Get current date without time
//		Calendar calendar = new GregorianCalendar();
//		calendar.setTimeInMillis(System.currentTimeMillis());
//		calendar.set(Calendar.HOUR_OF_DAY, 0);
//		calendar.set(Calendar.MINUTE, 0);
//		calendar.set(Calendar.SECOND, 0);
//		calendar.set(Calendar.MILLISECOND, 0);
//		
//		Timestamp currentDate = new Timestamp(calendar.getTimeInMillis());						
//		Timestamp startDate = subscription.getStartDate();
//		Timestamp renewalDate = subscription.getRenewalDate();
//		
//		// Check if current date is equal to or after start date
//		// Check if current date is before or equal to renewal date
//		if ((currentDate.compareTo(startDate) >= 0) && (currentDate.compareTo(renewalDate) <= 0))
//		{
//			return true;
//		}
//		
//		return false;
//	}
//	
//	private boolean isProductSubscribed(MProduct product)
//	{
//		if (product == null)
//			return false;
//		
//		MAttribute didSubscribedAttribute = new MAttribute(getCtx(), DID_SUBSCRIBED_ATTRIBUTE, null);		
//		MAttributeInstance mai_didSubscribed = didSubscribedAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
//
//		if (mai_didSubscribed == null || mai_didSubscribed.getValue() == null || mai_didSubscribed.getValue().length() < 1)
//			print("Failed to load DID_SUBSCRIBED for " + product);
//		else
//			return mai_didSubscribed.getValue().equals("true");
//		
//		return false;
//	}
	
//	public void testProductPO()
//	{
//		MAttribute didNumberAttribute = new MAttribute(getCtx(), DID_NUMBER_ATTRIBUTE, null); 
//		MAttribute didIsSetupAttribute = new MAttribute(getCtx(), DID_ISSETUP_ATTRIBUTE, null);
//		
//		for (MProduct product : DIDUtil.getAllDIDProducts(getCtx(), null))
//		{
//			MAttributeInstance mai_isSetup = didIsSetupAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
//			MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
//			
//			// Check values for both attributes exist
//			boolean attributeError = false;
//			if (mai_isSetup == null || mai_isSetup.getValue() == null || mai_isSetup.getValue().length() < 1)
//			{
//				print("Failed to load DID_ISSETUP for " + product);
//				attributeError = true;
//			}
//			
//			if (mai_didNumber == null || mai_didNumber.getValue() == null || mai_didNumber.getValue().length() < 1)
//			{
//				print("Failed to load DID_NUMBER for " + product);
//				attributeError = true;
//			}
//			
//			if (attributeError)
//				continue;
//			
//			String didNumber = mai_didNumber.getValue().trim();
//			boolean isSetup = mai_isSetup.getValue().equals("true");
//			
//			MProductPO[] productPOs = MProductPO.getOfProduct(getCtx(), product.getM_Product_ID(), null);
//			
//			if (productPOs.length < 1)
//			{
//				print(product + " doesn't have any PO data");
//			}
//			else if (productPOs.length > 1)
//			{
//				print(product + " has " + productPOs.length + " PO entires");
//			}
//			else
//			{
//				MProductPO productPO = productPOs[0];
//				String setupCorrect = DIDConstants.PRODUCT_PO_SETUP_VENDOR_PRODUCT_NO.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber);
//				String monthlyCorrect = DIDConstants.PRODUCT_PO_MONTHLY_VENDOR_PRODUCT_NO.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber);
//				
////				String vendorProductNo = monthlyCorrect;
////				if (isSetup)
////					vendorProductNo = setupCorrect;
////				
////				productPO.setVendorProductNo(vendorProductNo);
////				if (!productPO.save())
////					print("Failed to save " + product);
//				
//				if (!productPO.getVendorProductNo().equals(setupCorrect) && !productPO.getVendorProductNo().equals(monthlyCorrect))
//				{
//					print(product + "'s PO is invalid -> " + productPO.getVendorProductNo());
//				}
//			}
//			
//		}
//	}
	
//	public void testDIDProvisioned()
//	{
//		
//	}
//	
//	private static void print(String s)
//	{
//		System.out.println(s);
//	}
	
	public void testPrintCountries()
	{
		int[] countryIds = MCountry.getAllIDs(MCountry.Table_Name, "IsActive='Y' ORDER BY Name", null);
		for (int id : countryIds)
		{
			MCountry country = new MCountry(getCtx(), id, null);
			System.out.println("'" + country.getC_Country_ID() + "' => t('" + country.getName() + "'),");
		}
	}
	
	public static void main(String[] args)
	{
		
	}
}
