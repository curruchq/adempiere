package com.conversant.test;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MProduct;
import org.compiere.util.CLogger;

import test.AdempiereTestCase;

public class MySQLIntegrityTestCase extends AdempiereTestCase
{
	private static CLogger log = CLogger.getCLogger(MySQLIntegrityTestCase.class);
	
	private static final String ATTRIBUTE_DID_NUMBER = "DID_NUMBER";
	private static final int DID_ISSETUP_ATTRIBUTE = 1000008;
	private static final int DID_NUMBER_ATTRIBUTE = 1000015;
	private static final int DID_SUBSCRIBED_ATTRIBUTE = 1000016;
	
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
	
	public void testSubscribedDIDProvisioning()
	{
		MAttribute didIsSetupAttribute = new MAttribute(getCtx(), DID_ISSETUP_ATTRIBUTE, null);
		MAttribute didNumberAttribute = new MAttribute(getCtx(), DID_NUMBER_ATTRIBUTE, null);
		MAttribute didSubscribedAttribute = new MAttribute(getCtx(), DID_SUBSCRIBED_ATTRIBUTE, null);
		
		MProduct[] allProducts = getDIDProducts();
		
		for (MProduct product : allProducts)
		{
			MAttributeInstance mai_isSetup = didIsSetupAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			MAttributeInstance mai_subscribed = didSubscribedAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			
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
			
			if (mai_subscribed == null || mai_subscribed.getValue() == null || mai_subscribed.getValue().length() < 1)
			{
				log.severe("Failed to load DID_SUBSCRIBED for " + product);
				attributeError = true;
			}
			
			if (attributeError)
				continue;
			
			// If monthly product and subscribed then validate MySQL provisioning
			if (mai_isSetup.getValue().equalsIgnoreCase("false") && mai_subscribed.getValue().equalsIgnoreCase("true"))
			{
				
			}
		}
	}
}
