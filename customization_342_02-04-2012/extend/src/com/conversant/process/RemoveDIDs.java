package com.conversant.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MProduct;
import org.compiere.model.MSubscription;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;


public class RemoveDIDs extends SvrProcess
{	
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(RemoveDIDs.class);
	
	private static final int DID_M_ATTRIBUTESET_ID = 1000002;
	private static final int DID_ISSETUP_ATTRIBUTE = 1000008;
	private static final int DID_NUMBER_ATTRIBUTE = 1000015;
	private static final int DID_SUBSCRIBED_ATTRIBUTE = 1000016;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare()
	{

	}

	/**
	 * 	Process
	 *	@return message
	 *	@throws Exception
	 */
	@Override
	protected String doIt() throws Exception
	{
		ArrayList<MProduct> toDelete = new ArrayList<MProduct>();
		
		// Static reference to DID_ISSETUP
		MAttribute didIsSetupAttribute = new MAttribute(getCtx(), DID_ISSETUP_ATTRIBUTE, null);
		MAttribute didNumberAttribute = new MAttribute(getCtx(), DID_NUMBER_ATTRIBUTE, null); 
		MAttribute didSubscribedAttribute = new MAttribute(getCtx(), DID_SUBSCRIBED_ATTRIBUTE, null);
		
		// Hashmaps to hold products		
		HashMap<String, MProduct> setupProducts = new HashMap<String, MProduct>();
		HashMap<String, MProduct> monthlyProducts = new HashMap<String, MProduct>();
		
		MProduct[] allProducts = MProduct.get(getCtx(), "M_AttributeSet_ID = " + DID_M_ATTRIBUTESET_ID + " AND UPPER(IsActive) = 'Y'", null);
		
		// Sort products in lists
		for (MProduct product : allProducts)
		{
			MAttributeInstance mai_isSetup = didIsSetupAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
			
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

		// Set outside loop to bigger collection (hashmap)
		Iterator<String> productIterator = monthlyProducts.keySet().iterator();
		if (setupProducts.size() > monthlyProducts.size())
			productIterator = setupProducts.keySet().iterator();
						
		while(productIterator.hasNext())
		{
			// Get key
			String didNumber = productIterator.next();
			
			MProduct setupProduct = setupProducts.get(didNumber);
			MProduct monthlyProduct = monthlyProducts.get(didNumber);
			
			if (setupProduct == null)
			{
				log.severe("DIDSU-" + didNumber + " doesn't exist");
				continue;
			}
			
			if (monthlyProduct == null)
			{
				log.severe("DID-" + didNumber + " doesn't exist");
				continue;
			}
			
			MAttributeInstance mai_setup_didSubscribed = didSubscribedAttribute.getMAttributeInstance(setupProduct.getM_AttributeSetInstance_ID());
			MAttributeInstance mai_monthly_didSubscribed = didSubscribedAttribute.getMAttributeInstance(monthlyProduct.getM_AttributeSetInstance_ID());
			
			boolean attributeError = false;
			if (mai_setup_didSubscribed == null || mai_setup_didSubscribed.getValue() == null || mai_setup_didSubscribed.getValue().length() < 1)
			{
				log.severe("Failed to load DID_SUBSCRIBED for " + setupProduct);
				attributeError = true;
			}
			
			if (mai_monthly_didSubscribed == null || mai_monthly_didSubscribed.getValue() == null || mai_monthly_didSubscribed.getValue().length() < 1)
			{
				log.severe("Failed to load DID_SUBSCRIBED for " + monthlyProduct);
				attributeError = true;
			}
			
			if (attributeError)
				continue;
			
			boolean setupSubscribed = mai_setup_didSubscribed.getValue().equals("true");
			boolean monthlySubscribed = mai_monthly_didSubscribed.getValue().equals("true");
				
			// Both subscribed
			if (setupSubscribed && monthlySubscribed)
			{
				// ignore
			}
			// Bother not subscribed
			else if (!setupSubscribed && !monthlySubscribed)
			{
				// Make sure no subscriptions exist				
				MSubscription[] monthlySubscriptions = MSubscription.getSubscriptions(getCtx(), monthlyProduct.getM_Product_ID(), null);
				MSubscription[] setupSubscriptions = MSubscription.getSubscriptions(getCtx(), setupProduct.getM_Product_ID(), null);
				
				if (monthlySubscriptions.length > 0)
					log.severe("DID_SUBSCRIBED is false but subscriptions " + monthlySubscriptions.length + " found for " + monthlyProduct.getValue());
				
				if (setupSubscriptions.length > 0)
					log.severe("DID_SUBSCRIBED is false but subscriptions " + setupSubscriptions.length + " found for " + setupProduct.getValue());
				
				if (setupSubscriptions.length == 0 && monthlySubscriptions.length == 0)
				{
					System.out.println(didNumber);
					toDelete.add(setupProduct);
					toDelete.add(monthlyProduct);
				}
			}
			// Mismatch
			else
			{
				log.severe("DID_SUBSCRIBED attribute doesn't match between " + setupProduct.getValue() + "[" + setupSubscribed + "] and " + monthlyProduct.getValue() + "[" + monthlySubscribed + "] products");
			}
		}

		String productsToDelete = "";
		for (MProduct product : toDelete)
		{
			
			productsToDelete += product.getValue() + ", ";
		}
		
		return "";
	}
}
