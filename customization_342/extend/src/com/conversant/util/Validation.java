package com.conversant.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeSet;
import org.compiere.model.MAttributeValue;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.PO;
import org.compiere.model.PoEx;
import org.compiere.util.CLogger;
import org.compiere.wstore.DIDXService;
import org.compiere.wstore.WebBasket;
import org.compiere.wstore.WebBasketLine;

import com.conversant.did.DIDUtil;

public class Validation 
{
	private static CLogger log = CLogger.getCLogger(Validation.class);

	public static boolean validateAttributes(Properties ctx, int M_AttributeSet_ID, HashMap<Integer, Object> attributes)
	{
		if (attributes == null)
			return false;
		
		MAttributeSet attributeSet = MAttributeSet.get(ctx, M_AttributeSet_ID);
		if (attributeSet != null && !attributeSet.is_new())
		{
			for (MAttribute attribute : attributeSet.getMAttributes(false))
			{
				boolean found = false;
				boolean validValue = false;
				
				Iterator<Integer> iterator = attributes.keySet().iterator();
				while(iterator.hasNext())
				{
					Integer attributeId = (Integer)iterator.next();
					if (attributeId != null && attributeId > 0 && attribute.getM_Attribute_ID() == attributeId)
					{				
						if (attributes.get(attributeId) instanceof String)
						{
							String attributeValue = (String)attributes.get(attributeId);
							
							if (attributeValue != null && attributeValue.length() > 0)
								validValue = true;
							
							found = true;
							break;
							
						}
						else if (attributes.get(attributeId) instanceof Integer)
						{
							Integer attributeValueId = (Integer)attributes.get(attributeId);
														
							if (attributeValueId != null && attributeValueId > 0 && validateADId(MAttributeValue.Table_Name, attributeValueId, null))
								validValue = true;
							
							found = true;
							break;							
						}
						else
						{
							log.severe("Attribute value is neither String or Integer - " + attributes.get(attributeId).getClass());
						}
					}
				}
				
				if (!found)
				{
					log.severe("Couldn't find " + attribute);
					return false;
				}
				else if (found && !validValue)
				{
					log.severe("Loaded " + attribute + " with invalid value");
					return false;
				}				
			}
			
			return true;
		}
		else
			log.severe("Failed to load MAttibuteSet[" + M_AttributeSet_ID + "]");
		
		return false;
	}
	
	public static boolean validateMandatoryFields(PO po, HashMap<String, Object> fields)
	{
		if (po == null || fields == null)
			return false;
		
		ArrayList<String> mandatoryFields = PoEx.getMandatoryColumns(po);
		for (String mandatoryField : mandatoryFields)
		{
			if ((fields.get(mandatoryField) == null) || 
			    (fields.get(mandatoryField) instanceof String && ((String)fields.get(mandatoryField)).length() < 1)) // Some ID's can be 0
			{
				return false;
			}
		}
		
		return true;
	} 
	
	public static boolean validateDIDsInWebBasket(Properties ctx, WebBasket webBasket)
	{
		ArrayList<Integer> invalidDIDProductIds = new ArrayList<Integer>();
		ArrayList<String> invalidDIDs = new ArrayList<String>();
		
		// Get invalid product Ids
		for (Object line : webBasket.getLines())
		{
			if (line == null)
				continue;
			
			WebBasketLine webBasketLine = (WebBasketLine)line;
			MProduct product = MProduct.get(ctx, webBasketLine.getM_Product_ID());
			
			String didNumber = validateDID(ctx, product, invalidDIDs);
			if (didNumber != null)
			{
				invalidDIDs.add(didNumber); // to help validateDID() with product pairs
				invalidDIDProductIds.add(product.getM_Product_ID());
			}
		}
		
		// Remove invalid products from Web Basket
		for (Integer M_Product_ID : invalidDIDProductIds)
		{
			webBasket.deleteByProductId(M_Product_ID);
		}

		// If web basket changed
		if (invalidDIDProductIds.size() > 0)
		{
			// Recalculate web basket total
			webBasket.getTotal(true);
			
			return false;
		}	
		else
			return true;
	}
	
	// TODO: Use to load ctx from product, whats better? HttpServletRequest ctx or product ctx?
	public static boolean validateDIDsInOrder(Properties ctx, MOrder order)
	{
		ArrayList<String> invalidDIDs = new ArrayList<String>();
		
		for (MOrderLine orderLine : order.getLines())
		{
			MProduct product = orderLine.getProduct();
			String didNumber = validateDID(ctx, product, invalidDIDs);
			if (didNumber != null)
				return false;
		}

		return true;
	}
	
	public static String validateDID(Properties ctx, MProduct product, ArrayList<String> invalidDIDs)
	{
		if (product == null)
		{
			log.warning("Cannot validate a DID with NULL product");
			return null;
		}

		// Load DID number (if is DID product)
		String didNumber = DIDUtil.getDIDNumber(ctx, product, null);		
		
		// Check product has DID number
		if (didNumber == null || didNumber.length() < 1)
			return null;
		
		// Check against existing numbers to save time
		for (String invalidDID : invalidDIDs)
		{
			if (invalidDID.equals(didNumber))
				return didNumber;
		}
		
		// Check if is DIDx number
		boolean isDIDxNumber = DIDUtil.isDIDxNumber(ctx, product);
		
		// Isn't available?
		if (isDIDxNumber && !DIDXService.isDIDAvailable(ctx, didNumber)) 
			return didNumber;	
		
		// Isn't subscribed?
		else if (DIDUtil.isSubscribed(ctx, product, null)) 
			return didNumber;
		
		return null;
	}
	
	public static boolean validateADId(String tableName, int id, String trxName)
	{
		int[] actualIds = PO.getAllIDs(tableName, "UPPER(IsActive)='Y'", trxName); 
		if (actualIds == null)
		{
			log.severe("Failed to load all Table Ids for " + tableName);
			return false;
		}
		
		for (int actualId : actualIds)
		{
			if (id == actualId)
				return true;
		}
		
		return false;
	}
}

/*
	 
	
	/**
	 * Validate status of DIDs part of an order, returns list of invalid DIDs
	 * 
	 * @param order
	 * @return list of invalid DIDs
	 *
	public static ArrayList<String> old_validateDIDStatus(MOrder order)
	{
		if (order != null)
		{
			ArrayList<String> invalidDIDs = new ArrayList<String>();
			for (MOrderLine ol : order.getLines())
			{
				Properties ctx = order.getCtx();
				MProduct product = ol.getProduct();
				
				if (product != null)
				{
					int M_Product_ID = product.get_ID();
					String didNumber = getProductsDIDNumber(product);
					if ((isDIDxNumber(ctx, M_Product_ID) && 
						 didNumber != null && 
						 didNumber.length() > 0 && 
						 !DIDXService.isDIDAvailable(ctx, didNumber))
					||
						(didNumber != null && isDIDProductSubscribed(product)))
					{
						invalidDIDs.add(didNumber);	
					}
				}
			}
			return invalidDIDs.size() > 0 ? invalidDIDs : null;
		}
		return null;
	}	

* Validate status of DIDs in a WebBasket and removes all invalid DIDs
	 * 
	 * @param request
	 * @param wb
	 * @return
	 *
	public static boolean validateDIDStatus(HttpServletRequest request, WebBasket wb)
	{
		HashMap<String, Integer> invalidLineProductIds = new HashMap<String, Integer>();
		if (wb != null)
		{
			Properties ctx = JSPEnv.getCtx(request);	
			ArrayList<String> checkedDIDs = new ArrayList<String>();
			
			
			for (Object line : wb.getLines())
			{
				WebBasketLine wbl = (WebBasketLine)line;

				MProduct product = MProduct.get(ctx, wbl.getM_Product_ID());
				String didNumber = getProductsDIDNumber(product);
				
				if (didNumber == null || didNumber.length() < 1) continue;
				
				boolean exist = false;
				for (String checkedDID : checkedDIDs)
				{
					if (didNumber.equalsIgnoreCase(checkedDID))
					{
						exist = true;
						break;
					}
				}
				
				if (!exist)
				{
					if (((isDIDxNumber(ctx, wbl.getM_Product_ID())) && (!DIDXService.isDIDAvailable(ctx, didNumber))) || 
						(isDIDProductSubscribed(product))) 
					{
						invalidLineProductIds.put(didNumber, wbl.getM_Product_ID());
					}
					checkedDIDs.add(didNumber);
				}
			}
			
			for (Iterator iter = invalidLineProductIds.entrySet().iterator(); iter.hasNext();)
			{ 
			    Map.Entry entry = (Map.Entry)iter.next();
			    Integer value = (Integer)entry.getValue();
				wb.removeDIDPair(value);
			}
			
			wb.getTotal(true);
		}
		return invalidLineProductIds.size() > 0 ? false : true;
	}	
*/