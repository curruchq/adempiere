package com.conversant.test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MPriceList;
import org.compiere.model.MPriceListVersion;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPO;
import org.compiere.model.MProductPrice;
import org.compiere.model.MSubscription;
import org.compiere.wstore.DIDConstants;

import test.AdempiereTestCase;

public class AdempiereDataIntegrityTestCase extends AdempiereTestCase
{
//	private static CLogger log = CLogger.getCLogger(AdempiereDataIntegrityTestCase.class);
	
	private static final boolean SHOW_DETAIL = true;
	
	private static final String ATTRIBUTE_DID_NUMBER = "DID_NUMBER";	
	private static final int STANDARD_SELLING_M_PRICELIST_ID = 1000000;
	private static final int DID_M_ATTRIBUTESET_ID = 1000002;
	private static final int DID_ISSETUP_ATTRIBUTE = 1000008;
	private static final int DID_NUMBER_ATTRIBUTE = 1000015;
	private static final int DID_SUBSCRIBED_ATTRIBUTE = 1000016;
	
	private static final String INVALID_PRODUCT_NAME = "INVALID PRODUCT";
	
	private static final String NUMBER_IDENTIFIER = "##NUMBER##";
	private static final String PRODUCT_PO_SETUP_VENDOR_PRODUCT_NO = "S-" + NUMBER_IDENTIFIER;
	private static final String PRODUCT_PO_MONTHLY_VENDOR_PRODUCT_NO = "M-" + NUMBER_IDENTIFIER;
	
	protected static final HashMap<Integer, String> DID_PRODUCTS_TO_SKIP = new HashMap<Integer, String>() 
	{
		{
			put(1000044, "DIDSU-643-CHC");
			put(1000045, "DIDSU-643-DUN");
			put(1000036, "DIDSU-644-WGT");
			put(1000046, "DIDSU-647-HAM");
			put(1000048, "DIDSU-649-AKL");
			put(1000047, "DIDSU-649-HBC");
			put(1000050, "DIDSU-649-WHG");
			put(1000063, "DID-331706");
			put(1000061, "DID-441902");
			put(1000062, "DID-44208");
			put(1000052, "DID-61427-SMS");
			
			put(1000029, "DID-643-CHC");
			put(1000026, "DID-643-DUN");
			put(1000027, "DID-644-WGT");
			put(1000028, "DID-647-HAM");
			put(1000001, "DID-649-AKL");
			put(1000043, "DID-649-HBC");
			put(1000049, "DID-649-WHG");
			put(1000024, "DID-800");
			put(1000064, "DID-ST-MIN");
			put(1000530, "DID/DDI monthly charge template");
			put(1000531, "DID/DDI setup fee template");
		}
	};
	
	private MProduct[] allProducts = null;
	
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

	/**
	 * Gets all DID products 
	 * - Any product which uses an attribute set containg an attribute named DID_NUMBER
	 * 
	 * @return array of products
	 */
	private MProduct[] getDIDProducts()
	{	
		String whereClause = 	"M_Product_ID IN" + 
	    "(" +
			"SELECT M_PRODUCT_ID "+
			"FROM M_AttributeInstance mai, M_Product mp " +
			"WHERE " +
				"mp.M_ATTRIBUTESETINSTANCE_ID = mai.M_ATTRIBUTESETINSTANCE_ID " +
			"AND " +
				"mai.M_ATTRIBUTE_ID = " + 
				"(" + 												
		   	  		"SELECT M_ATTRIBUTE_ID " +
		   	  		"FROM M_ATTRIBUTE " +
		   	  		"WHERE UPPER(NAME) LIKE UPPER('" + ATTRIBUTE_DID_NUMBER + "') AND ROWNUM = 1 " +
		   	  	") " +
   	  	") " +
   	  	"AND " +
   	  	"UPPER(IsActive) = 'Y'";

		if (allProducts == null)
			allProducts = MProduct.get(getCtx(), whereClause, null);		

		return allProducts;
	}
	
	public void testGetDIDProducts()
	{
		MProduct[] allProducts_bySearchKey = MProduct.get(getCtx(), "(value LIKE 'DID-%' OR value LIKE 'DIDSU-%') AND UPPER(IsActive) = 'Y'", null);	
		MProduct[] allProducts_byAttributeSet = MProduct.get(getCtx(), "M_AttributeSet_ID = " + DID_M_ATTRIBUTESET_ID + " AND UPPER(IsActive) = 'Y'", null);	
		MProduct[] allProducts_byAttributeSetAttributeName = getDIDProducts();
		
		if (allProducts_bySearchKey.length != allProducts_byAttributeSetAttributeName.length ||
			allProducts_bySearchKey.length != allProducts_byAttributeSet.length || 
			allProducts_byAttributeSet.length != allProducts_byAttributeSetAttributeName.length)
		{
			print("Count mismatch - " + 
					   "BySearchKey=" + allProducts_bySearchKey.length + " " +
					   "ByAttributeSet=" + allProducts_byAttributeSet.length + " " +
					   "ByAttributeSetAttributeName=" + allProducts_byAttributeSetAttributeName.length);
			
			if (SHOW_DETAIL)
			{
				/* Show which products aren't in both bySearchKey & byAttributeSet */
				if (allProducts_bySearchKey.length > allProducts_byAttributeSet.length)
				{
					for (MProduct product_bySearchKey : allProducts_bySearchKey)
					{
						if (!DID_PRODUCTS_TO_SKIP.containsKey(product_bySearchKey.getM_Product_ID()))
						{
							boolean found = false;
							for (MProduct product_byAttributeSet : allProducts_byAttributeSet)
							{
								if (product_bySearchKey.getM_Product_ID() == product_byAttributeSet.getM_Product_ID())
									found = true;
							}
							
							if (!found)
								System.out.println(product_bySearchKey + " was in BySearchKey but not ByAttributeSet");
						}
					}
				}
				// byAttributeSet is bigger or they're same size
				else 
				{
					for (MProduct product_byAttributeSet : allProducts_byAttributeSet)
					{
						if (!DID_PRODUCTS_TO_SKIP.containsKey(product_byAttributeSet.getM_Product_ID()))
						{
							boolean found = false;
							for (MProduct product_bySearchKey : allProducts_byAttributeSet)
							{
								if (product_byAttributeSet.getM_Product_ID() == product_bySearchKey.getM_Product_ID())
									found = true;
							}
							
							if (!found)
								System.out.println(product_byAttributeSet + " was in ByAttributeSet but not BySearchKey");
						}
					}
				}
				
				/* Show which products aren't in both bySearchKey & ByAttributeSetAttributeName */
				if (allProducts_bySearchKey.length > allProducts_byAttributeSetAttributeName.length)
				{
					for (MProduct product_bySearchKey : allProducts_bySearchKey)
					{
						if (!DID_PRODUCTS_TO_SKIP.containsKey(product_bySearchKey.getM_Product_ID()))
						{
							boolean found = false;
							for (MProduct product_byAttributeSetAttributeName : allProducts_byAttributeSetAttributeName)
							{
								if (product_bySearchKey.getM_Product_ID() == product_byAttributeSetAttributeName.getM_Product_ID())
									found = true;
							}
							
							if (!found)
								System.out.println(product_bySearchKey + " was in BySearchKey but not ByAttributeSetAttributeName");
						}
					}
				}
				// byAttributeSet is bigger or they're same size
				else 
				{
					for (MProduct product_byAttributeSetAttributeName : allProducts_byAttributeSetAttributeName)
					{
						if (!DID_PRODUCTS_TO_SKIP.containsKey(product_byAttributeSetAttributeName.getM_Product_ID()))
						{
							boolean found = false;
							for (MProduct product_bySearchKey : allProducts_bySearchKey)
							{
								if (product_byAttributeSetAttributeName.getM_Product_ID() == product_bySearchKey.getM_Product_ID())
									found = true;
							}
							
							if (!found)
								System.out.println(product_byAttributeSetAttributeName + " was in ByAttributeSetAttributeName but not BySearchKey");
						}
					}
				}
				
				/* Show which products aren't in both ByAttributeSet & ByAttributeSetAttributeName */
				if (allProducts_byAttributeSet.length > allProducts_byAttributeSetAttributeName.length)
				{
					for (MProduct product_byAttributeSet : allProducts_byAttributeSet)
					{
						if (!DID_PRODUCTS_TO_SKIP.containsKey(product_byAttributeSet.getM_Product_ID()))
						{
							boolean found = false;
							for (MProduct product_byAttributeSetAttributeName : allProducts_byAttributeSetAttributeName)
							{
								if (product_byAttributeSet.getM_Product_ID() == product_byAttributeSetAttributeName.getM_Product_ID())
									found = true;
							}
							
							if (!found)
								System.out.println(product_byAttributeSet + " was in ByAttributeSet but not ByAttributeSetAttributeName");
						}
					}
				}
				// ByAttributeSetAttributeName is bigger or they're same size
				else 
				{
					for (MProduct product_byAttributeSetAttributeName : allProducts_byAttributeSetAttributeName)
					{
						if (!DID_PRODUCTS_TO_SKIP.containsKey(product_byAttributeSetAttributeName.getM_Product_ID()))
						{
							boolean found = false;
							for (MProduct product_byAttributeSet : allProducts_byAttributeSet)
							{
								if (product_byAttributeSetAttributeName.getM_Product_ID() == product_byAttributeSet.getM_Product_ID())
									found = true;
							}
							
							if (!found)
								System.out.println(product_byAttributeSetAttributeName + " was in ByAttributeSetAttributeName but not ByAttributeSet");
						}
					}
				}
			}
		}
	}
	
	public void testDIDProductPairs()
	{
		// Static reference to DID_ISSETUP
		MAttribute didIsSetupAttribute = new MAttribute(getCtx(), DID_ISSETUP_ATTRIBUTE, null);
		MAttribute didNumberAttribute = new MAttribute(getCtx(), DID_NUMBER_ATTRIBUTE, null); 
		
		// Hashmaps to hold products		
		HashMap<String, MProduct> setupProducts = new HashMap<String, MProduct>();
		HashMap<String, MProduct> monthlyProducts = new HashMap<String, MProduct>();
		
		// Sort products in lists
		for (MProduct product : getDIDProducts())
		{
			MAttributeInstance mai_isSetup = didIsSetupAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			
			// Check values for both attributes exist
			boolean attributeError = false;
			if (mai_isSetup == null || mai_isSetup.getValue() == null)
			{
				print("Failed to load DID_ISSETUP for " + product);
				attributeError = true;
			}
			
			if (mai_didNumber == null || mai_didNumber.getValue() == null || mai_didNumber.getValue().length() < 1)
			{
				print("Failed to load DID_NUMBER for " + product);
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
				print("Invalid DID_ISSETUP value for " + product + "DID_ISSETUP=" + mai_isSetup.getValue());
		}
		
		if (setupProducts.size() != monthlyProducts.size())
		{
			print("No. of setup products[" + setupProducts.size() + "] doesn't match no. of monthly products[" + monthlyProducts.size() + "]");
			
			if (SHOW_DETAIL)
			{
				System.out.println("Missing DID products");
				Iterator<String> productIterator = setupProducts.keySet().iterator();				
				while(productIterator.hasNext())
				{
					// Get key
					String didNumber = productIterator.next();
							
					// Load products
					MProduct setupProduct = setupProducts.get(didNumber);
					MProduct monthlyProduct = monthlyProducts.get(didNumber);
					
					// Check both exist
					if (setupProduct == null)
						System.out.println("DIDSU-" + didNumber);

					
					if (monthlyProduct == null)
						System.out.println("DID-" + didNumber);					
				}
				System.out.println("..");
				productIterator = monthlyProducts.keySet().iterator();				
				while(productIterator.hasNext())
				{
					// Get key
					String didNumber = productIterator.next();
							
					// Load products
					MProduct setupProduct = setupProducts.get(didNumber);
					MProduct monthlyProduct = monthlyProducts.get(didNumber);
					
					// Check both exist
					if (setupProduct == null)
						System.out.println("DIDSU-" + didNumber);

					
					if (monthlyProduct == null)
						System.out.println("DID-" + didNumber);					
				}
			}
		}		
	}
	
	public void testDIDProductNameInvalid()
	{
		for (MProduct product : getDIDProducts())
		{
			if (product.getName() == null)
				print(product + " NULL name");
			else if (product.getName().equals(INVALID_PRODUCT_NAME))
				print(product + " found invalid name");
		}
	}
	
	public void testDIDProductSearchKeys()
	{
		for (MProduct product : getDIDProducts())
		{
			String searchKey = product.getValue();
			if (searchKey == null)
				print(product + " NULL search key");
			else if (searchKey.matches(".*\\s+.*"))
				print(product + " search key contains whitespace");			
		}
	}
	
	public void testDIDProductPrices()
	{
		MPriceList pl = MPriceList.get(getCtx(), STANDARD_SELLING_M_PRICELIST_ID, null);
		MPriceListVersion plv = pl.getPriceListVersion(new Timestamp(System.currentTimeMillis()));
		
		for (MProduct product : getDIDProducts())
		{
			MProductPrice productPrice = MProductPrice.get(getCtx(), plv.getM_PriceList_Version_ID(), product.getM_Product_ID(), null);
			
			if (productPrice == null)
				print(product + " no MProductPrice found " + pl + " " + plv);
			else
			{
				BigDecimal priceLimit = (BigDecimal)productPrice.get_Value(MProductPrice.COLUMNNAME_PriceLimit);
				BigDecimal priceList = (BigDecimal)productPrice.get_Value(MProductPrice.COLUMNNAME_PriceList);
				BigDecimal priceStd = (BigDecimal)productPrice.get_Value(MProductPrice.COLUMNNAME_PriceStd);
				
				if (priceLimit == null)
					print(product + " no MProductPrice-PriceLimit found");
				
				if (priceList == null)
					print(product + " no MProductPrice-PriceList found");
				
				if (priceStd == null)
					print(product + " no MProductPrice-PriceStd found");
			}
		}
	}
	
	public void testDIDSubscribedSubscriptions()
	{
		// Static reference to DID_ISSETUP
		MAttribute didIsSetupAttribute = new MAttribute(getCtx(), DID_ISSETUP_ATTRIBUTE, null);
		MAttribute didNumberAttribute = new MAttribute(getCtx(), DID_NUMBER_ATTRIBUTE, null); 
		MAttribute didSubscribedAttribute = new MAttribute(getCtx(), DID_SUBSCRIBED_ATTRIBUTE, null);
		
		// Hashmaps to hold products		
		HashMap<String, MProduct> setupProducts = new HashMap<String, MProduct>();
		HashMap<String, MProduct> monthlyProducts = new HashMap<String, MProduct>();
		
		// Sort products in lists
		for (MProduct product : getDIDProducts())
		{
			MAttributeInstance mai_isSetup = didIsSetupAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			
			// Check values for both attributes exist
			boolean attributeError = false;
			if (mai_isSetup == null || mai_isSetup.getValue() == null || mai_isSetup.getValue().length() < 1)
			{
				print("Failed to load DID_ISSETUP for " + product);
				attributeError = true;
			}
			
			if (mai_didNumber == null || mai_didNumber.getValue() == null || mai_didNumber.getValue().length() < 1)
			{
				print("Failed to load DID_NUMBER for " + product);
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
				print("Invalid DID_ISSETUP value for " + product + "DID_ISSETUP=" + mai_isSetup.getValue());
		}
		
		// TODO: Replace check
		if (false)//setupProducts.size() != monthlyProducts.size())
		{
			fail("Couldn't check subscriptions - no. of setup products[" + setupProducts.size() + "] doesn't match no. of monthly products[" + monthlyProducts.size() + "]");
		}
		else
		{
			int validSubscriptionCount = 0;
			
			Iterator<String> productIterator = monthlyProducts.keySet().iterator();				
			while(productIterator.hasNext())
			{
				// Get key
				String didNumber = productIterator.next();
				
				MProduct setupProduct = setupProducts.get(didNumber);
				MProduct monthlyProduct = monthlyProducts.get(didNumber);
				
				// TODO: Remove
				if (setupProduct == null)
					continue;
				
				MAttributeInstance mai_setup_didSubscribed = didSubscribedAttribute.getMAttributeInstance(setupProduct.getM_AttributeSetInstance_ID());
				MAttributeInstance mai_monthly_didSubscribed = didSubscribedAttribute.getMAttributeInstance(monthlyProduct.getM_AttributeSetInstance_ID());
				
				boolean attributeError = false;
				if (mai_setup_didSubscribed == null || mai_setup_didSubscribed.getValue() == null || mai_setup_didSubscribed.getValue().length() < 1)
				{
					print("Failed to load DID_SUBSCRIBED for " + setupProduct);
					attributeError = true;
				}
				
				if (mai_monthly_didSubscribed == null || mai_monthly_didSubscribed.getValue() == null || mai_monthly_didSubscribed.getValue().length() < 1)
				{
					print("Failed to load DID_SUBSCRIBED for " + monthlyProduct);
					attributeError = true;
				}
				
				if (attributeError)
					continue;
				
				boolean setupSubscribed = mai_setup_didSubscribed.getValue().equals("true");
				boolean monthlySubscribed = mai_monthly_didSubscribed.getValue().equals("true");
					
				// Both subscribed
				if (setupSubscribed && monthlySubscribed)
				{
					// Check MSubscription exists for monthly product
					MSubscription[] subscriptions = MSubscription.getSubscriptions(getCtx(), monthlyProduct.getM_Product_ID(), null);
					
					if (subscriptions.length < 1)
					{
						// wasn't found
						print("Didn't find any subscriptions for " + monthlyProduct.getValue());
					}
					else if (subscriptions.length > 1)
					{
						// more than 1 found
						print("Found " + subscriptions.length + " subscriptions for " + monthlyProduct.getValue());
					}
					else
					{
						// one found -> success
						validSubscriptionCount++;
					}
				}
				// Bother not subscribed
				else if (!setupSubscribed && !monthlySubscribed)
				{
					// Make sure no subscriptions exist				
					MSubscription[] monthlySubscriptions = MSubscription.getSubscriptions(getCtx(), monthlyProduct.getM_Product_ID(), null);
					MSubscription[] setupSubscriptions = MSubscription.getSubscriptions(getCtx(), setupProduct.getM_Product_ID(), null);
					
					if (monthlySubscriptions.length > 0)
						print("DID_SUBSCRIBED is false but subscriptions " + monthlySubscriptions.length + " found for " + monthlyProduct.getValue());
					
					if (setupSubscriptions.length > 0)
						print("DID_SUBSCRIBED is false but subscriptions " + setupSubscriptions.length + " found for " + setupProduct.getValue());
				}
				// Mismatch
				else
				{
					print("DID_SUBSCRIBED attribute doesn't match between " + setupProduct.getValue() + "[" + setupSubscribed + "] and " + monthlyProduct.getValue() + "[" + monthlySubscribed + "] products");
				}
			}
			
			System.out.println("Valid subscriptions = " + validSubscriptionCount);
		}
		
		
	}
	
	public void testProductPO()
	{
		MAttribute didNumberAttribute = new MAttribute(getCtx(), DID_NUMBER_ATTRIBUTE, null); 
		MAttribute didIsSetupAttribute = new MAttribute(getCtx(), DID_ISSETUP_ATTRIBUTE, null);
		
		for (MProduct product : getDIDProducts())
		{
			MAttributeInstance mai_isSetup = didIsSetupAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			
			// Check values for both attributes exist
			boolean attributeError = false;
			if (mai_isSetup == null || mai_isSetup.getValue() == null || mai_isSetup.getValue().length() < 1)
			{
				print("Failed to load DID_ISSETUP for " + product);
				attributeError = true;
			}
			
			if (mai_didNumber == null || mai_didNumber.getValue() == null || mai_didNumber.getValue().length() < 1)
			{
				print("Failed to load DID_NUMBER for " + product);
				attributeError = true;
			}
			
			if (attributeError)
				continue;
			
			String didNumber = mai_didNumber.getValue().trim();
			boolean isSetup = mai_isSetup.getValue().equals("true");
			
			MProductPO[] productPOs = MProductPO.getOfProduct(getCtx(), product.getM_Product_ID(), null);
			
			if (productPOs.length < 1)
			{
				print(product + " doesn't have any PO data");
			}
			else if (productPOs.length > 1)
			{
				print(product + " has " + productPOs.length + " PO entires");
			}
			else
			{
				MProductPO productPO = productPOs[0];
				String setupCorrect = DIDConstants.PRODUCT_PO_SETUP_VENDOR_PRODUCT_NO.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber);
				String monthlyCorrect = DIDConstants.PRODUCT_PO_MONTHLY_VENDOR_PRODUCT_NO.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber);
				
//				String vendorProductNo = monthlyCorrect;
//				if (isSetup)
//					vendorProductNo = setupCorrect;
//				
//				productPO.setVendorProductNo(vendorProductNo);
//				if (!productPO.save())
//					print("Failed to save " + product);
				
				if (!productPO.getVendorProductNo().equals(setupCorrect) && !productPO.getVendorProductNo().equals(monthlyCorrect))
				{
					print(product + "'s PO is invalid -> " + productPO.getVendorProductNo());
				}
			}
			
		}
	}
	
	public void testDIDProvisioned()
	{
		
	}
	
	private static void print(String s)
	{
		System.out.println(s);
	}
	
	public static void main(String[] args)
	{

	}
}
