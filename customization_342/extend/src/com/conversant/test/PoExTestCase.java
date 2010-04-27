package com.conversant.test;

import java.util.ArrayList;

import org.compiere.model.MProduct;
import org.compiere.model.MSubscription;
import org.compiere.model.PoEx;

public class PoExTestCase extends AdempiereTestCase  
{
	private static String[] productColumns = new String[]{"C_TaxCategory_ID", "C_UOM_ID", "M_Product_Category_ID", "Name", "ProductType", "Value"};
	private static String[] subscriptionColumns = new String[]{"C_BPartner_ID", "C_SubscriptionType_ID", "IsDue", "M_Product_ID", "Name", "PaidUntilDate", "RenewalDate", "StartDate"};

	public void testGetMandatoryColumns()
	{
		ArrayList<String> mandatoryColumns = PoEx.getMandatoryColumns(new MProduct(getCtx(), 0, null));
		
		if (mandatoryColumns.size() != productColumns.length)
			fail("");
		
		for (String productColumn : productColumns)
		{		
			boolean found = false;
			for (String mandatoryColumn : mandatoryColumns)
			{
				if (mandatoryColumn.equalsIgnoreCase(productColumn))
					found = true;
			}
			
			if (!found)
				fail("Failed to load mandatory column ");
		}
		
		mandatoryColumns = PoEx.getMandatoryColumns(new MSubscription(getCtx(), 0, null));
		
		if (mandatoryColumns.size() != subscriptionColumns.length)
			fail("");
		
		for (String subscriptionColumn : subscriptionColumns)
		{
			boolean found = false;
			for (String mandatoryColumn : mandatoryColumns)
			{
				if (mandatoryColumn.equalsIgnoreCase(subscriptionColumn))
					found = true;
			}
			
			if (!found)
				fail("");
		}
	}

}
