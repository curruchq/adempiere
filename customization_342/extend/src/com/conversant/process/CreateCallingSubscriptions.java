package com.conversant.process;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MProduct;
import org.compiere.model.MSubscription;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Trx;

import com.conversant.did.DIDConstants;
import com.conversant.did.DIDUtil;
import com.conversant.util.Validation;

public class CreateCallingSubscriptions extends SvrProcess
{
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(CreateCallingSubscriptions.class);

	private static String PROCESS_MSG_SUCCESS = "@Success@";
	private static String PROCESS_MSG_ERROR = "@Error@";
	
	private static int CALL_STD_OUT_PRODUCT_ID = 1000002;
	
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
// ------------ Hack to allow product retrieval ---------------
		
		int AD_Client_ID = Env.getAD_Client_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Client_ID", "1000000");

//------------------------------------------------------------
		
		String trxName = Trx.createTrxName("createCallSubscriptions");
		
		try
		{	
			// Load subscribed DID products
			HashMap<String, MProduct> didProducts = new HashMap<String, MProduct>();
			for (MProduct product : DIDUtil.getAllDIDProducts(Env.getCtx(), trxName))
			{
				if (DIDUtil.isMSubscribed(Env.getCtx(), product))
				{
					String number = DIDUtil.getDIDNumber(Env.getCtx(), product, trxName);				
					didProducts.put(number, product);
				}
			}
			
			// Load subscribed Call products
			HashMap<String, MProduct> callProducts = new HashMap<String, MProduct>();
			for (MProduct product : DIDUtil.getAllCallProducts(Env.getCtx(), trxName))
			{
				if (DIDUtil.isMSubscribed(Env.getCtx(), product))
				{
					String number = DIDUtil.getCDRNumber(Env.getCtx(), product, trxName);
					callProducts.put(number, product);
				}
			}
			
			// Load CALL-STD-OUT subscriptions for their dates
			MSubscription[] callStdOutSubscriptions = MSubscription.getSubscriptions(getCtx(), CALL_STD_OUT_PRODUCT_ID, trxName);

			//
			ArrayList<String> createdCallSubscriptionNumbers = new ArrayList<String>();
			
			// Loop through each subscribed DID
			Iterator<String> didIterator = didProducts.keySet().iterator();
			while(didIterator.hasNext())
			{
				String didNumber = (String)didIterator.next();
				MProduct didProduct = (MProduct)didProducts.get(didNumber);
				
				boolean found = false;
				
				// Check if Calling subscriptions already exist
				Iterator<String> callIterator = callProducts.keySet().iterator();
				while(callIterator.hasNext())
				{
					String cdrNumber = (String)callIterator.next();
					MProduct callProduct = (MProduct)callProducts.get(cdrNumber);
					
					if (didNumber.equalsIgnoreCase(cdrNumber))
					{
						found = true;
						break;
					}
				}
				
				if (!found)
				{
					// Make sure CALL product pair exists
					MProduct inboundCallProduct = null;
					MProduct outboundCallProduct = null;
					MProduct[] existingProducts = DIDUtil.getCallProducts(Env.getCtx(), didNumber, trxName);		
					if (existingProducts.length == 2)
					{
						inboundCallProduct = DIDUtil.getInboundOrOutboundProduct(Env.getCtx(), existingProducts[0], existingProducts[1], true, trxName);	
						outboundCallProduct = DIDUtil.getInboundOrOutboundProduct(Env.getCtx(), existingProducts[0], existingProducts[1], false, trxName);			
					}
					else
						throw new Exception("Failed to load MProduct[" + DIDConstants.CALL_IN_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber) + "]" + 
											" and/or MProduct[" + DIDConstants.CALL_OUT_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber) + "]");
					
					// Double check products
					if (inboundCallProduct == null)
						throw new Exception("Failed to load MProduct[" + DIDConstants.CALL_IN_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber) + "]");
					
					if (outboundCallProduct == null)
						throw new Exception("Failed to load MProduct[" + DIDConstants.CALL_OUT_PRODUCT_SEARCH_KEY.replace(DIDConstants.NUMBER_IDENTIFIER, didNumber) + "]");
										
					// Load active DID subscriptions (either setup or monthly)
					MSubscription[] allSubscriptions = MSubscription.getSubscriptions(Env.getCtx(), didProduct.getM_Product_ID(), trxName);
					
					ArrayList<MSubscription> activeSubscriptions = new ArrayList<MSubscription>();
					for (MSubscription subscription : allSubscriptions)
					{
						if (DIDUtil.isActiveMSubscription(Env.getCtx(), subscription))
							activeSubscriptions.add(subscription);
					}
					
					if (activeSubscriptions.size() != 1)
						throw new Exception("Loaded " + activeSubscriptions.size() + " subscription(s) for " + didProduct);
					
					// Load business partner and location ids
					int businessPartnerId = activeSubscriptions.get(0).getC_BPartner_ID();
					int businessPartnerLocationId = (Integer)activeSubscriptions.get(0).get_Value(MBPartnerLocation.COLUMNNAME_C_BPartner_Location_ID);
					
					if (!Validation.validateADId(MBPartner.Table_Name, businessPartnerId, trxName))
						throw new Exception("Failed to load Business Partner Id[" + businessPartnerId + "] from " + didProduct + "'s subscription " + activeSubscriptions.get(0));
					
					if (!Validation.validateADId(MBPartnerLocation.Table_Name, businessPartnerLocationId, trxName))
						throw new Exception("Failed to load Business Partner Location Id[" + businessPartnerLocationId + "] from " + didProduct + "'s subscription " + activeSubscriptions.get(0));
					
					// Load active CALL-STD-OUT subscription for DID number
					MSubscription callStdOutSubscription = null;
					for (MSubscription subscription : callStdOutSubscriptions)
					{
						if (!DIDUtil.isActiveMSubscription(getCtx(), subscription))
							continue;
						
						if (subscription.getName().equals("+" + didNumber))
						{
							callStdOutSubscription = subscription;
							break;
						}
					}
					
					// Need CALL-STD-OUT to pull dates from for new Calling subscriptions
					if (callStdOutSubscription == null)
						throw new Exception("Failed to load CALL-STD-OUT subscription for " + didNumber + " - It is needed to load dates from for new Calling subscriptions");
					
					// Load dates from CALL-STD-OUT
					HashMap<String, Timestamp> dates = new HashMap<String, Timestamp>();
					dates.put(MSubscription.COLUMNNAME_StartDate, callStdOutSubscription.getStartDate());
					dates.put(MSubscription.COLUMNNAME_PaidUntilDate, callStdOutSubscription.getPaidUntilDate());
					dates.put(MSubscription.COLUMNNAME_RenewalDate, callStdOutSubscription.getRenewalDate());
					
					// Create inbound subscription
					MSubscription inboundSubscription = DIDUtil.createCallSubscription(Env.getCtx(), didNumber, dates, businessPartnerId, businessPartnerLocationId, inboundCallProduct.getM_Product_ID(), trxName);
					if (inboundSubscription == null)
						throw new Exception("Failed to create subscription for " + inboundCallProduct + " MBPartner[" + businessPartnerId + "]");
					
					// Create outbound subscription
					MSubscription outboundSubscription = DIDUtil.createCallSubscription(Env.getCtx(), didNumber, dates, businessPartnerId, businessPartnerLocationId, outboundCallProduct.getM_Product_ID(), trxName);
					if (outboundSubscription == null)
						throw new Exception("Failed to create subscription for " + outboundCallProduct + " MBPartner[" + businessPartnerId + "]");					
				
					// End date CALL-STD-OUT subscription
					callStdOutSubscription.setRenewalDate(callStdOutSubscription.getPaidUntilDate());
					if (!callStdOutSubscription.save())
						throw new Exception("Failed to end date CALL-STD-OUT subscription " + callStdOutSubscription);
					
					createdCallSubscriptionNumbers.add(didNumber);
				}
			}
			
			Trx trx = null;
			try
			{
				trx = Trx.get(trxName, false);	
				if (trx != null)
				{
					if (!trx.commit())
						throw new Exception("Failed to commit trx");
				}
			}
			catch (Exception ex)
			{
				// Catches Trx.get() IllegalArgumentExceptions
				throw new Exception("Failed to get trx");
			}
			finally
			{
				if (trx != null && trx.isActive())
					trx.close();
			}
	
			return PROCESS_MSG_SUCCESS + "\n\n" + "Created " + createdCallSubscriptionNumbers.size() + " CALL subscriptions";
		}
		catch (Exception ex)
		{
			log.warning("CreateCallingSubscriptions process failed - " + ex.getMessage());
			return PROCESS_MSG_ERROR + "\n\n" + ex.getMessage();
		}
		finally
		{
			// Rollback trx
			Trx trx = Trx.get(trxName, false);
			if (trx != null && trx.isActive())
			{
				trx.rollback();
				trx.close();
			}		
// ------------ Remove Hack -----------------------------------
			
			Env.setContext(getCtx(), "#AD_Client_ID", AD_Client_ID);

// ------------------------------------------------------------
		}
	}
}

