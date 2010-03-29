package com.conversant.wstore;

import java.util.ArrayList;
import java.util.Properties;

import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.util.CLogger;
import org.compiere.wstore.DIDXService;
import org.compiere.wstore.WebBasket;
import org.compiere.wstore.WebBasketLine;

public class DIDValidation 
{
	private static CLogger log = CLogger.getCLogger(DIDValidation.class);

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
		String didNumber = DIDUtil.getDIDNumber(ctx, product);		
		
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
		else if (DIDUtil.isSubscribed(ctx, product)) 
			return didNumber;
		
		return null;
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