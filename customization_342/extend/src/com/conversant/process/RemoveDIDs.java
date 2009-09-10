package com.conversant.process;

import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MAttributeSet;
import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MProduct;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;


public class RemoveDIDs extends SvrProcess
{	
	private String m_AttributeSet_ID = null;
	private String m_AttributeName = null;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("M_ATTRIBUTESET_ID"))
				m_AttributeSet_ID = (String)para[i].getParameter();
			else if (name.equals("M_ATTRIBUTE_NAME"))
				m_AttributeName = (String)para[i].getParameter();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}

	/**
	 * 	Process
	 *	@return message
	 *	@throws Exception
	 */
	@Override
	protected String doIt() throws Exception
	{
		Properties ctx = getCtx();
		String whereClause = "M_ATTRIBUTESET_ID = " + m_AttributeSet_ID;
		MProduct[] products = MProduct.get(ctx, whereClause, null);
		for (MProduct product : products)
		{
			int M_AttributeSetInstance_ID = product.getM_AttributeSetInstance_ID();
			MAttributeSetInstance masi = MAttributeSetInstance.get(ctx, M_AttributeSetInstance_ID, product.get_ID());
			if (masi != null) 
			{
				MAttributeSet as = masi.getMAttributeSet();
				for (MAttribute attribute : as.getMAttributes(false))
				{
					String attributeName = attribute.getName();
					if (attributeName != null & attributeName.equalsIgnoreCase(m_AttributeName))
					{
						MAttributeInstance mai = attribute.getMAttributeInstance(M_AttributeSetInstance_ID);
						String attributeValue = mai.getValue();
						break;
					}
				}
			}
		}
		return null;
	}
}
