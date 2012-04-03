package com.conversant.process;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MProduct;
import org.compiere.model.MSubscription;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Trx;

import com.conversant.did.DIDUtil;

public class SubscriptionCheck extends SvrProcess
{
	/** Logger 																	*/
	private static CLogger log = CLogger.getCLogger(SubscriptionCheck.class);
	
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
	
	/** Conversant Client														*/
	private int AD_Client_ID = 1000000; // Conversant
	
	/** Conversant Org															*/
	private int AD_Org_ID = 1000001; // Conversant
	
	
	
	@Override
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("AD_Client_ID"))
			{
				AD_Client_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("AD_Org_ID"))
			{
				AD_Org_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else
			{
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
		}
	}
	
	@Override
	protected String doIt() throws Exception
	{
		// Set client and org (useful when run via scheduler)
		int originalAD_Client_ID = Env.getAD_Client_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Client_ID", AD_Client_ID);
		
		int originalAD_Org_ID = Env.getAD_Org_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Org_ID", AD_Org_ID);
		
		try
		{	
			return process();
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			// Reset client and org 
			Env.setContext(getCtx(), "#AD_Client_ID", originalAD_Client_ID);
			Env.setContext(getCtx(), "#AD_Org_ID", originalAD_Org_ID);
		}
	}

	private String process()
	{
		productFlags();
		
		return "@Success@ - Check log messages for results";
	}
	
	private void productFlags()
	{
		// Get all DID/DID-SU products which DID_SUBSCRIBED=true and check for subs (all four including call-in/out)		
		String trxName = Trx.createTrxName("productFlags");
		
		try
		{
			// Load products with DID_SUBSCRIBED=true
			Set<MProduct> subscribedProducts = new HashSet<MProduct>();

			Set<MProduct> subscribedMissing = new HashSet<MProduct>();
			Set<MProduct> subscribedMultiple = new HashSet<MProduct>();
			Set<MProduct> unsubscribedSubscriptions = new HashSet<MProduct>();
			
			MAttribute didSubscribedAttribute = new MAttribute(getCtx(), DID_SUBSCRIBED_ATTRIBUTE, trxName);			
			MProduct[] didProducts = DIDUtil.getAllDIDProducts(getCtx(), trxName);
			for (MProduct product : didProducts)
			{
				MAttributeInstance mai_didSubscribed = didSubscribedAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());

				if (mai_didSubscribed == null || mai_didSubscribed.getValue() == null || mai_didSubscribed.getValue().length() < 1)
					print("Failed to load DID_SUBSCRIBED for " + product);
				else 
				{	
					boolean subscribed = mai_didSubscribed.getValue().equals("true");
					
					if (subscribed)
						subscribedProducts.add(product); // for use later
					
					// Check active subscriptions for each product
					int activeSubscriptionCount = 0;
					for (MSubscription subscription : MSubscription.getSubscriptions(getCtx(), product.getM_Product_ID(), trxName))
					{			
						if (isSubscriptionActive(subscription))
							activeSubscriptionCount++;
					}
					
					if (subscribed && activeSubscriptionCount < 1)
					{
						subscribedMissing.add(product);
					}
					else if (subscribed && activeSubscriptionCount > 1)
					{
						subscribedMultiple.add(product);	
					}
					else if (!subscribed && activeSubscriptionCount > 0) 
					{
						unsubscribedSubscriptions.add(product);
					}
				}
			}
			
			// Load numbers from subscribed products (using set to avoid duplicates from DID/DID-SU products)
			Set<String> subscribedNumbers = new HashSet<String>();
			MAttribute didNumberAttribute = new MAttribute(getCtx(), DID_NUMBER_ATTRIBUTE, trxName);
			for (MProduct product : subscribedProducts)
			{
				MAttributeInstance mai_didNumber = didNumberAttribute.getMAttributeInstance(product.getM_AttributeSetInstance_ID());
				if (mai_didNumber == null || mai_didNumber.getValue() == null || mai_didNumber.getValue().length() < 1)
					print("Failed to load DID_NUMBER for " + product);
				else
					subscribedNumbers.add(mai_didNumber.getValue().trim());				
			}
			
			// Check CALL-IN/OUT product's subscriptions
			for (String number : subscribedNumbers)
			{
				MProduct[] callProducts = DIDUtil.getCallProducts(getCtx(), number, trxName);
				for (MProduct product : callProducts)
				{
					// Check active subscriptions for each product
					int activeSubscriptionCount = 0;
					for (MSubscription subscription : MSubscription.getSubscriptions(getCtx(), product.getM_Product_ID(), trxName))
					{			
						if (isSubscriptionActive(subscription))
							activeSubscriptionCount++;
					}
					
					if (activeSubscriptionCount < 1)
					{
						subscribedMissing.add(product);
					}
					else if (activeSubscriptionCount > 1)
					{
						subscribedMultiple.add(product);	
					}
				}
			}
			
			// Report results
			print("Flagged without active subscription: (" + subscribedMissing.size() + ") " + subscribedMissing.toString());
			print("Multiple active subscriptions: (" + subscribedMultiple.size() + ") " + subscribedMultiple.toString());
			print("Unsubscribed with active subscription: (" + unsubscribedSubscriptions.size() + ") " + unsubscribedSubscriptions.toString());
		}
		catch (Exception ex)
		{
			print(ex.getMessage());
		}
		finally
		{
			Trx trx = Trx.get(trxName, false);
			if (trx != null && trx.isActive())
				trx.commit();
		}	
	}
	
	private boolean isSubscriptionActive(MSubscription subscription)
	{
		// Get current date without time
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Timestamp currentDate = new Timestamp(calendar.getTimeInMillis());						
		Timestamp startDate = subscription.getStartDate();
		Timestamp renewalDate = subscription.getRenewalDate();
		
		// Check if current date is equal to or after start date
		// Check if current date is before or equal to renewal date
		if ((currentDate.compareTo(startDate) >= 0) && (currentDate.compareTo(renewalDate) <= 0))
		{
			return true;
		}
		
		return false;
	}
	
	private void print(String msg)
	{
		addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);	
	}
}
