package com.conversant.process;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MAttributeUse;
import org.compiere.model.MAttributeUseEx;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.Trx;

// TODO: Important to update M_AttributeSetInstance.Description??

public class AddAttribute extends SvrProcess
{
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(AddAttribute.class);

	private int M_AttributeSet_ID;
	private String newAttributeName;
	private String newAtrributeDefaultValue;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare()
	{
		M_AttributeSet_ID = 1000002;
		newAttributeName = "DID_FAX_ISFAX";
		newAtrributeDefaultValue = "false";
	}
	
	/**
	 * 	Process
	 *	@return message
	 *	@throws Exception
	 */
	@Override
	protected String doIt() throws Exception
	{
		String trxName = Trx.createTrxName("AddAttribute");
		Trx trx = Trx.get(trxName, false);
		
		try
		{		
			MAttribute newAtrribute = new MAttribute(getCtx(), 0, trxName);
			newAtrribute.setName(newAttributeName);
			
			// TODO: No naming constraint, check no identical names?
			
			if (newAtrribute.save(trxName))
			{
				MAttributeUse attributeUse = new MAttributeUse(getCtx(), 0, trxName);
				attributeUse.setM_Attribute_ID(newAtrribute.getM_Attribute_ID());
				attributeUse.setM_AttributeSet_ID(M_AttributeSet_ID);
				attributeUse.setSeqNo(MAttributeUseEx.getNextSeqNo(M_AttributeSet_ID));				
				
				if (attributeUse.save(trxName))
				{
					// Create attribute instance for every attribute instance set that exists
					
					if (!trx.commit())
						return "@Error@ Failed to commit to DB";
					else
						return "@Success@";
				}
				else
					return "@Error@ Failed to assign attribute to attribute set";
			}
			else
				return "@Error@ Failed to save new attribute";
		}
		catch (Exception ex)
		{
			log.severe(ex.toString());
		}
		finally
		{
			if (trx != null && trx.isActive())
			{
				trx.rollback();
				trx.close();
			}
		}
		
		return "@Error@ Failed to add attribute";
	}
}
