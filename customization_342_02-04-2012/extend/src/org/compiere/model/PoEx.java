package org.compiere.model;

import java.util.ArrayList;

public class PoEx
{
	private static String[] columns = new String[]{"AD_Client_ID", "AD_Org_ID", "Created", "CreatedBy", "IsActive", "Updated", "UpdatedBy"};
	private static String[] productColumns = new String[]{"IsBOM", "IsInvoicePrintDetails", "IsPickListPrintDetails", "IsPurchased", "IsSold", "IsStocked", "IsSummary", "IsVerified", "IsWebStoreFeatured", "IsSelfService", "IsDropShip", "IsExcludeAutoDelivery", "M_AttributeSetInstance_ID"};
	private static String[] subscriptionColumns = new String[]{};
	private static String[] businessPartnerColumns = new String[]{"Value", "IsCustomer", "IsEmployee", "IsOneTime", "IsProspect", "IsSalesRep", "IsSummary", "IsVendor", "SO_CreditLimit", "SO_CreditUsed", "SendEMail"};
	
	public static ArrayList<String> getMandatoryColumns(PO po)
	{
		if (po == null)
			throw new IllegalArgumentException();
		
		ArrayList<String> mandatoryColumns = new  ArrayList<String>();		
		
		for (int i=0; i<po.get_ColumnCount(); i++)
		{
			if (po.isColumnMandatory(i))
			{
				String columnName = po.get_ColumnName(i);
				String idColumnName = po.get_TableName() + "_ID";
				
				if (!columnName.equalsIgnoreCase(idColumnName))
				{
					boolean found = false;
					for (String column : getIgnoreColumns(po))
					{
						if (column.equalsIgnoreCase(columnName))
						{
							found = true;
							break;
						}
					}

					if (!found)
						mandatoryColumns.add(columnName);
				}				
			}
		}
		
		return mandatoryColumns;
	}
	
	private static String[] getIgnoreColumns(PO po)
	{
		String[] additionIgnoreColumns = null;
		
		if (po instanceof MProduct)
			additionIgnoreColumns = productColumns;
		else if (po instanceof MSubscription)
			additionIgnoreColumns = subscriptionColumns;
		else if (po instanceof MBPartner)
			additionIgnoreColumns = businessPartnerColumns;
		
		if (additionIgnoreColumns != null && additionIgnoreColumns.length > 0)
		{
			String[] ignoreColumns = new String[columns.length + additionIgnoreColumns.length];
			
			int i = 0;
			for (String column : columns)
			{
				ignoreColumns[i] = column;
				i++;
			}
			
			for (String column : additionIgnoreColumns)
			{
				ignoreColumns[i] = column;
				i++;
			}
			
			return ignoreColumns;
		}

		return columns;
	}
}
