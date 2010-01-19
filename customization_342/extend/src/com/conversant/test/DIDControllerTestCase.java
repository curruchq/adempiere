package com.conversant.test;

import java.util.Properties;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MProduct;
import org.compiere.util.CLogger;
import org.compiere.wstore.DIDController;

import com.conversant.model.DIDAreaCode;
import com.conversant.model.DIDCountry;

public class DIDControllerTestCase extends AdempiereTestCase 
{
	private static CLogger log = CLogger.getCLogger(DIDControllerTestCase.class);
	
	private static boolean SHOW_TIMING = true;
	
	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
		log.info("Done setting up DIDControllerTestCase");
	}
	
	@Override
	protected void tearDown() throws Exception 
	{
		super.tearDown();
		log.info("Done tearing down DIDControllerTestCase");
	}

	private DIDCountry createNZCountry()
	{
		return new DIDCountry("New Zealand", "64", "147");
	}
	
	public void testOldLoadLocalDIDCountryProducts()
	{
		Properties ctx  = getCtx();
		DIDCountry country = createNZCountry();
		
		long start = System.currentTimeMillis();
		DIDController.oldLoadLocalDIDCountryProducts(ctx, country, true); // load all area codes
		if (SHOW_TIMING) System.out.println("oldLoadLocalDIDCountryProducts()[area code only] ran in " + (System.currentTimeMillis() - start) + "ms");
		
		start = System.currentTimeMillis();
		DIDController.oldLoadLocalDIDCountryProducts(ctx, country, false); // load all unsubscribed DIDs for each area code
		if (SHOW_TIMING) System.out.println("oldLoadLocalDIDCountryProducts()[load DIDs] ran in " + (System.currentTimeMillis() - start) + "ms");
	}
	
	public void testloadLocalDIDCountryProducts() 
	{		
		Properties ctx  = getCtx();
		DIDCountry country = createNZCountry();
		
		long start = System.currentTimeMillis();
		DIDController.loadLocalDIDCountryProducts(ctx, country, true); // load all area codes
		if (SHOW_TIMING) System.out.println("newLoadLocalDIDCountryProducts()[area code only] ran in " + (System.currentTimeMillis() - start) + "ms");
		
		start = System.currentTimeMillis();
		DIDController.loadLocalDIDCountryProducts(ctx, country, false); // load all unsubscribed DIDs for each area code
		if (SHOW_TIMING) System.out.println("newLoadLocalDIDCountryProducts()[load DIDs] ran in " + (System.currentTimeMillis() - start) + "ms");
	}
	
	public void testGetUnsubscribedDIDProducts()
	{
		Properties ctx  = getCtx();
		
		long start = System.currentTimeMillis();
		MProduct[] products = DIDController.getUnsubscribedDIDProducts(ctx);
		if (SHOW_TIMING) System.out.println("getUnsubscribedDIDProducts() ran in " + (System.currentTimeMillis() - start) + "ms");
		
		MAttribute didIsSubscribedAttribute = new MAttribute(ctx, 1000016, null); 
		
		int unsubscribedCount = 0;
		for (MProduct product : products)
		{
			MAttributeInstance mai_isSubscribed = didIsSubscribedAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			
			if (mai_isSubscribed == null || mai_isSubscribed.getValue() == null)
			{
				fail("getUnsubscribedDIDProducts() returned a DID product which has no subscribed attribute and or value, MProduct[" + product.getM_Product_ID() + "]");
			}			
			else if (mai_isSubscribed.getValue().equalsIgnoreCase("true"))
			{
				fail("getUnsubscribedDIDProducts() returned a subscribed DID product, MProduct[" + product.getM_Product_ID() + "]");
			}
			else if (mai_isSubscribed.getValue().equalsIgnoreCase("false"))
			{
				unsubscribedCount++;
			}
			else
			{
				fail("getUnsubscribedDIDProducts() returned a DID product whos subscribed attribute value is neither 'true' or 'false', MProduct[" + product.getM_Product_ID() + "]");
			}
		}
		
		assertEquals("Number of products doesn't match unsubscribed count", products.length, unsubscribedCount);
	}
}
