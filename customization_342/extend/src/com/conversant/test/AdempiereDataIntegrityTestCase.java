package com.conversant.test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MPriceList;
import org.compiere.model.MPriceListVersion;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPrice;
import org.compiere.util.CLogger;

import test.AdempiereTestCase;

public class AdempiereDataIntegrityTestCase extends AdempiereTestCase
{
	private static CLogger log = CLogger.getCLogger(AdempiereDataIntegrityTestCase.class);
	
	private static final boolean SHOW_DETAIL = true;
	
	private static final String ATTRIBUTE_DID_NUMBER = "DID_NUMBER";	
	private static final int STANDARD_SELLING_M_PRICELIST_ID = 1000000;
	private static final int DID_M_ATTRIBUTESET_ID = 1000002;
	private static final int DID_ISSETUP_ATTRIBUTE = 1000008;
	private static final int DID_NUMBER_ATTRIBUTE = 1000015;
	
	private static final String INVALID_PRODUCT_NAME = "INVALID PRODUCT";
	
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
			
			put(1000646, "DID-61280149835");
			put(1000678, "DID-61290374200");
			put(1000184, "DID-61388070949");
			put(1000392, "DID-271146133201");
			put(1000638, "DID-6434422949");
			put(1000636, "DID-6434428883");
			put(1000351, "DID-61881114529");
			put(1000562, "DID-6492803750");
			put(1000302, "DID-6499705590");
			put(1000734, "DID-6499705599");
			put(1000746, "DID-85236786707");
			
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
		MProduct[] allProducts_bySearchKey = MProduct.get(getCtx(), "value LIKE 'DID-%' OR value LIKE 'DIDSU-%' AND UPPER(IsActive) = 'Y'", null);	
		MProduct[] allProducts_byAttributeSet = MProduct.get(getCtx(), "M_AttributeSet_ID = " + DID_M_ATTRIBUTESET_ID + " AND UPPER(IsActive) = 'Y'", null);	
		MProduct[] allProducts_byAttributeSetAttributeName = getDIDProducts();
		
		if (allProducts_bySearchKey.length != allProducts_byAttributeSetAttributeName.length ||
			allProducts_bySearchKey.length != allProducts_byAttributeSet.length || 
			allProducts_byAttributeSet.length != allProducts_byAttributeSetAttributeName.length)
		{
			log.severe("Count mismatch - " + 
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
				log.severe("Invalid DID_ISSETUP value for " + product + "DID_ISSETUP=" + mai_isSetup.getValue());
		}
		
		if (setupProducts.size() != monthlyProducts.size())
		{
			log.severe("No. of setup products[" + setupProducts.size() + "] doesn't match no. of monthly products[" + monthlyProducts.size() + "]");
			
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
				log.severe(product + " NULL name");
			else if (product.getName().equals(INVALID_PRODUCT_NAME))
				log.severe(product + " found invalid name");
		}
	}
	
	public void testDIDProductSearchKeys()
	{
		for (MProduct product : getDIDProducts())
		{
			String searchKey = product.getValue();
			if (searchKey == null)
				log.severe(product + " NULL search key");
			else if (searchKey.matches(".*\\s+.*"))
				log.severe(product + " search key contains whitespace");			
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
				log.severe(product + " no MProductPrice found " + pl + " " + plv);
			else
			{
				BigDecimal priceLimit = (BigDecimal)productPrice.get_Value(MProductPrice.COLUMNNAME_PriceLimit);
				BigDecimal priceList = (BigDecimal)productPrice.get_Value(MProductPrice.COLUMNNAME_PriceList);
				BigDecimal priceStd = (BigDecimal)productPrice.get_Value(MProductPrice.COLUMNNAME_PriceStd);
				
				if (priceLimit == null)
					log.severe(product + " no MProductPrice-PriceLimit found");
				
				if (priceList == null)
					log.severe(product + " no MProductPrice-PriceList found");
				
				if (priceStd == null)
					log.severe(product + " no MProductPrice-PriceStd found");
			}
		}
	}
}
