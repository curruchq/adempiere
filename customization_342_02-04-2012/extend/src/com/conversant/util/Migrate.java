package com.conversant.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import org.compiere.Adempiere;
import org.compiere.model.MProduct;
import org.compiere.model.MSubscription;
import org.compiere.util.CLogMgt;
import org.compiere.util.Env;
import org.compiere.util.Trx;
import org.compiere.wstore.DIDController;

import com.conversant.did.DIDConstants;
import com.conversant.did.DIDUtil;

public class Migrate
{
	private static final int BATCH_COUNT = 50;
	
	private static final int TCC_0800_IN = 1000035;
	private static final int TCC_STD_IN = 1000896;
	private static final int TCC_STD_OUT = 1000002;
	
	public static void main(String[] args)
	{
		startup();
		
		String trxName = Trx.createTrxName("createCallProducts");
		
		MProduct[] allDIDProducts = DIDUtil.getAllDIDProducts(Env.getCtx(), trxName);
		MProduct[] allCallProducts = DIDUtil.getAllCallProducts(Env.getCtx(), trxName);
		
		HashMap<String, MProduct> didProducts = new HashMap<String, MProduct>();
		for (MProduct product : allDIDProducts)
		{
			String number = DIDUtil.getDIDNumber(Env.getCtx(), product, trxName);
			didProducts.put(number, product);
		}
		
		HashMap<String, MProduct> callProducts = new HashMap<String, MProduct>();
		for (MProduct product : allCallProducts)
		{
			String number = DIDUtil.getCDRNumber(Env.getCtx(), product, trxName);
			callProducts.put(number, product);
		}
		
		System.out.println("DID Products " + didProducts.size());
		System.out.println("Call Products " + callProducts.size());

		ArrayList<String> missingCallProducts = new ArrayList<String>();
		
		Iterator<String> didIterator = didProducts.keySet().iterator();
		while(didIterator.hasNext())
		{
			String number = (String)didIterator.next();
			MProduct didProduct = (MProduct)didProducts.get(number);
			
			boolean found = false;
			
			Iterator<String> callIterator = callProducts.keySet().iterator();
			while(callIterator.hasNext())
			{
				String username = (String)callIterator.next();
				MProduct callProduct = (MProduct)callProducts.get(username);
				
				if (number.equalsIgnoreCase(username))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				HashMap<Integer, Object> attributes = new HashMap<Integer, Object>();
				attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, DIDConstants.ATTRIBUTE_VALUE_INBOUND_CDR_USERNAME.replace(DIDConstants.NUMBER_IDENTIFIER, number).replace(DIDConstants.DOMAIN_IDENTIFIER, "conversant.co.nz"));
				attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_APPLICATION, DIDConstants.ATTRIBUTE_ID_CDR_APPLICATION_VALUE_AUDIO);
				attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION, DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_INBOUND);
				attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_NUMBER, number);
				
				MProduct inbound = DIDUtil.createCallProduct(Env.getCtx(), attributes, trxName);
				if (!DIDController.updateProductPrice(Env.getCtx(), 1000000, inbound.getM_Product_ID(), Env.ZERO, trxName))
					System.out.println("Failed to create price for " + inbound);
				
				if (inbound != null)
				{						
					attributes.remove(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME);
					attributes.remove(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION);
					attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, DIDConstants.ATTRIBUTE_VALUE_OUTBOUND_CDR_USERNAME.replace(DIDConstants.NUMBER_IDENTIFIER, number).replace(DIDConstants.DOMAIN_IDENTIFIER, "conversant.co.nz"));
					attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION, DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_OUTBOUND);
					
					MProduct outbound = DIDUtil.createCallProduct(Env.getCtx(), attributes, trxName);
					if (!DIDController.updateProductPrice(Env.getCtx(), 1000000, outbound.getM_Product_ID(), Env.ZERO, trxName))
						System.out.println("Failed to create price for " + outbound);
					
					if (outbound != null)
						;
					else
						System.out.println("Outbound product is NULL for " + number + " either create it or delete " + inbound);
				}
				else
					System.out.println("Inbound product is NULL for " + number);		
			}
		}
		
		for (String number : missingCallProducts)
		{
			System.out.println(number);
		}
		
		System.out.println("Missing Call Products: " + missingCallProducts.size() * 2);
		
		Trx trx = null;
		try
		{
			trx = Trx.get(trxName, false);	
			if (trx != null)
			{
				if (!trx.commit())
					System.out.println("Failed to commit local trx");
			}
		}
		catch (Exception ex)
		{
			// Catches Trx.get() IllegalArgumentExceptions
		}
		finally
		{
			if (trx != null && trx.isActive())
				trx.close();
		}
		
//		ArrayList<String> createdCallProductsFor = new ArrayList<String>();
//		int count = 0;
//		for (MProduct product : allDIDProducts)
//		{
//			// delay to avoid max conns
//			if (count >= BATCH_COUNT)
//			{
//				long wait2sec = System.currentTimeMillis() + 2000;
//				while (System.currentTimeMillis() < wait2sec)
//				{
//					
//				}
//				count = 0;
//			}
//			
//			boolean isSetup = DIDUtil.isSetup(Env.getCtx(), product, null);
//			if (isSetup)
//			{
//				String number = DIDUtil.getDIDNumber(Env.getCtx(), product, null);
//				
//				boolean found = false;
//				for (String existingNumber : createdCallProductsFor)
//				{
//					if (existingNumber.equals(number))
//					{
//						found = true;
//						break;
//					}
//				}
//				
//				if (!found)
//				{				
//					MProduct[] callProducts = DIDUtil.getCallProducts(Env.getCtx(), number, null);
//					if (callProducts.length == 0)
//					{
//						HashMap<Integer, Object> attributes = new HashMap<Integer, Object>();
//						attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, DIDConstants.ATTRIBUTE_VALUE_INBOUND_CDR_USERNAME.replace(DIDConstants.NUMBER_IDENTIFIER, number).replace(DIDConstants.DOMAIN_IDENTIFIER, "conversant.co.nz"));
//						attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_APPLICATION, DIDConstants.ATTRIBUTE_ID_CDR_APPLICATION_VALUE_AUDIO);
//						attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION, DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_INBOUND);
//						attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_NUMBER, number);
//						
//						MProduct inbound = DIDUtil.createCallProduct(Env.getCtx(), attributes, null);
//						if (inbound == null)
//							System.out.println("Inbound product is NULL for " + number);
//						
//						attributes.remove(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME);
//						attributes.remove(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION);
//						attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, DIDConstants.ATTRIBUTE_VALUE_OUTBOUND_CDR_USERNAME.replace(DIDConstants.NUMBER_IDENTIFIER, number).replace(DIDConstants.DOMAIN_IDENTIFIER, "conversant.co.nz"));
//						attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION, DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_OUTBOUND);
//						
//						MProduct outbound = DIDUtil.createCallProduct(Env.getCtx(), attributes, null);
//						if (outbound == null)
//							System.out.println("Outbound product is NULL for " + number);
//						
//						createdCallProductsFor.add(number);
//					}
//					else if (callProducts.length == 1)
//					{
//						System.out.println("Only found 1 call product " + callProducts[0] + " for " + number);
//					}
//					else if (callProducts.length > 2)
//					{
//						System.out.println("Found " + callProducts.length + " call products for " + number);
//					}
//				}
//				else
//				{
//					System.out.println("Found two setup products for " + number);
//				}
//				
////				boolean attributeSubscribed = DIDUtil.isSubscribed(Env.getCtx(), product, null);
////				boolean subscriptionSubscribed = DIDUtil.isMSubscribed(Env.getCtx(), product);
////				
////				if (attributeSubscribed != subscriptionSubscribed)
////					System.out.println(product + "--- AttributeSubscribed[" + attributeSubscribed + "] SubscriptionSubscribed[" + subscriptionSubscribed + "]");
////				else
////					System.out.println(product);
//			}
//			
//			count++;
//		}
//		
//		
//		for (String number : createdCallProductsFor)
//		{
//			System.out.println(number);
//		}
//		
//		System.out.println(createdCallProductsFor.size());
	}
	
	private static void startup()
	{
		Adempiere.startupEnvironment(false);
		CLogMgt.setLevel(Level.INFO);
		
		Env.setContext(Env.getCtx(), "#AD_Client_ID", "1000000");
		Env.setContext(Env.getCtx(), "#AD_Org_ID", "1000001");
	}
	
	private static MProduct createDIDCallingProducts(String number)
	{
		HashMap<Integer, Object> attributes = new HashMap<Integer, Object>();
		attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_USERNAME, DIDConstants.ATTRIBUTE_VALUE_INBOUND_CDR_USERNAME.replace(DIDConstants.NUMBER_IDENTIFIER, number).replace(DIDConstants.DOMAIN_IDENTIFIER, "conversant.co.nz"));
		attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_APPLICATION, DIDConstants.ATTRIBUTE_ID_CDR_APPLICATION_VALUE_AUDIO);
		attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION, DIDConstants.ATTRIBUTE_ID_CDR_DIRECTION_VALUE_INBOUND);
		attributes.put(DIDConstants.ATTRIBUTE_ID_CDR_NUMBER, number);
		
		MProduct inbound = DIDUtil.createCallProduct(Env.getCtx(), attributes, null);
		
		System.out.println(inbound);
		
		return inbound;
	}
	
	private static void createDIDCallingSubscription(String number, MProduct product)
	{
		MSubscription sub = DIDUtil.createCallSubscription(Env.getCtx(), number, 1000022, 1000023, product.getM_Product_ID(), null);
		
		System.out.println(sub);
	}
	
	private static MSubscription get0800InboundSubscription(String number)
	{
		MSubscription[] subs = MSubscription.getSubscriptions(Env.getCtx(), TCC_0800_IN, null);
		for (MSubscription sub : subs)
		{
			if (sub.getName().equals("+" + number))
				return sub;
		}
		
		return null;
	}
	
	private static MSubscription getStdInboundSubscription(String number)
	{
		MSubscription[] subs = MSubscription.getSubscriptions(Env.getCtx(), TCC_STD_IN, null);
		for (MSubscription sub : subs)
		{
			if (sub.getName().equals("+" + number))
				return sub;
		}
		
		return null;
	}
	
	private static MSubscription getStdOutboundSubscription(String number)
	{
		MSubscription[] subs = MSubscription.getSubscriptions(Env.getCtx(), TCC_STD_OUT, null);
		for (MSubscription sub : subs)
		{
			if (sub.getName().equals("+" + number))
				return sub;
		}
		
		return null;
	}
	
	private static MProduct getDIDProduct(String number, boolean setup)
	{
		MProduct[] products = DIDUtil.getDIDProducts(Env.getCtx(), number, null);
		if (products.length == 2)
			return DIDUtil.getSetupOrMonthlyProduct(Env.getCtx(), products[0], products[1], setup, null);	
		else
			System.err.println(number + " doesn't have both products");
		
		return null;
	}
}
