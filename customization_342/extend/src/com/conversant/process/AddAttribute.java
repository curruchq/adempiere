package com.conversant.process;

import java.util.logging.Level;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MAttributeSet;
import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MAttributeSetInstanceEx;
import org.compiere.model.MAttributeUse;
import org.compiere.model.MAttributeUseEx;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.Trx;

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
//		M_AttributeSet_ID = 1000002;
//		newAttributeName = "DID_FAX_ISFAX";
//		newAtrributeDefaultValue = "false";
		
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("M_AttributeSet_ID"))
			{
				M_AttributeSet_ID = Integer.parseInt((String)para[i].getParameter());
			}
			else if (name.equals("AttributeName"))
			{
				newAttributeName = (String)para[i].getParameter();		
			}
			else if (name.equals("AttributeDefaultValue"))
			{
				newAtrributeDefaultValue = (String)para[i].getParameter();	
			}
			else
			{
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
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
		// Check M_AttributeSet_ID exists
		MAttributeSet attributeSet = MAttributeSet.get(getCtx(), M_AttributeSet_ID);
		if (attributeSet == null || attributeSet.getM_AttributeSet_ID() != M_AttributeSet_ID)
		{
			return "@Error@ Invalid Attribute Set Id";
		}
		
		// Check attribute name is valid
		if (newAttributeName == null || newAttributeName.length() < 1)
		{
			return "@Error@ Invalid Atribute Name";
		}
		else
		{
			newAttributeName = newAttributeName.trim();
		}
		
		// Check attribute default value
		if (newAtrributeDefaultValue == null || newAtrributeDefaultValue.length() < 1)
		{
			newAtrributeDefaultValue = "";
		}
		else
		{
			newAtrributeDefaultValue = newAtrributeDefaultValue.trim();
		}
		
		// Check attribute name is unique
		MAttribute[] allAttributes = MAttribute.getOfClient(getCtx(), false, false);
		for (MAttribute attribute : allAttributes)
		{
			if (attribute != null && attribute.getName() != null && attribute.getName().equalsIgnoreCase(newAttributeName))
			{
				return "@Error@ An attribute with the name '" + newAttributeName + "' already exists";
			}
		}		
		
		// Create transaction 
		String trxName = Trx.createTrxName("AddAttribute");
		Trx trx = Trx.get(trxName, true);
		
		try
		{		
			// Create the new attribute
			MAttribute newAtrribute = new MAttribute(getCtx(), 0, trxName);
			newAtrribute.setName(newAttributeName);
			
			if (newAtrribute.save(trxName))
			{
				// Assign new attribute to an MAttributeSet
				MAttributeUse attributeUse = new MAttributeUse(getCtx(), 0, trxName);
				attributeUse.setM_Attribute_ID(newAtrribute.getM_Attribute_ID());
				attributeUse.setM_AttributeSet_ID(M_AttributeSet_ID);
				attributeUse.setSeqNo(MAttributeUseEx.getNextSeqNo(getCtx(), M_AttributeSet_ID));				
				
				if (attributeUse.save(trxName))
				{
					int attributeSetInstanceCount = 0;
					
					// Create an MAttributeInstance for every MAttributeSetInstance and set default value
					MAttributeSetInstance[] allAttributeSetInstances = MAttributeSetInstanceEx.getAttributeSetInstances(getCtx(), M_AttributeSet_ID, trxName);
					for (MAttributeSetInstance attributeSetInstance : allAttributeSetInstances)
					{
						MAttributeInstance attributeInstance = new MAttributeInstance(getCtx(), newAtrribute.getM_Attribute_ID(), attributeSetInstance.getM_AttributeSetInstance_ID(), newAtrributeDefaultValue, trxName);
						if (!attributeInstance.save())
						{
							return "@Error@ Failed to save MAttributeInstance for MAttributeSetInstance[" + attributeSetInstance.getDescription() + "]";
						}
						
						// Update MAttributeSetInstance description to included new attribute value
						attributeSetInstance.setDescription();
												
						// TODO: Maybe not fail when updating description fails? Does it get set next time attributes are viewed? The description isn't used for anything
						if (!attributeSetInstance.save())
						{
							return "@Error@ Failed to save MAttributeSetInstance[" + attributeSetInstance.getM_AttributeSetInstance_ID() + "] after updating description";
						}
						
						attributeSetInstanceCount++;
					}
					
					if (!trx.commit())
					{
						return "@Error@ Failed to commit to database";
					}
					else
					{
						return "@Success@ Updated " + attributeSetInstanceCount + " attribute set instances by adding MAttribute[" + newAtrribute.getM_Attribute_ID() + "-" + newAtrribute.getName() + "] and setting each value to '" + newAtrributeDefaultValue + "'";
					}
				}
				else
				{
					return "@Error@ Failed to assign attribute to attribute set";
				}
			}
			else
			{
				return "@Error@ Failed to save new attribute";
			}
		}
		catch (Exception ex)
		{
			log.severe(ex.toString());
		}
		finally
		{
			// Rollback transaction if it hasn't been committed yet
			if (trx != null && trx.isActive())
			{
				trx.rollback();
				trx.close();
			}
		}
		
		return "@Error@ Failed to add attribute";
	}
}
