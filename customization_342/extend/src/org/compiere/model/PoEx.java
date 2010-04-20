package org.compiere.model;

import java.util.ArrayList;

public class PoEx
{
	private static String[] columns = new String[]{"AD_Client_ID", "AD_Org_ID", "Created", "CreatedBy", "IsActive", "Updated", "UpdatedBy"};
	
	public static ArrayList<String> getMandatoryColumns(PO po)
	{
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
					for (String column : columns)
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
}
