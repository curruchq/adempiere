package com.conversant.process;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;

import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.Env;

import com.conversant.db.BillingConnector;
import com.conversant.db.RadiusConnector;
import com.conversant.model.BillingRecord;

public class InsertRadiusAccountFromBillingRecord extends SvrProcess
{
	/** Logger */
	private static CLogger log = CLogger.getCLogger(InsertRadiusAccountFromBillingRecord.class);
	
	private int AD_Client_ID = 1000000; // Conversant
	
	private int AD_Org_ID = 1000001; // Conversant
	
	private String number;
	
	private String afterDateTime;
	
	private String beforeDateTime;

	private boolean countOnly = true;
	
	private boolean ignoreInbound = true;
	
	/**
	 * Prepare - e.g., get Parameters.
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
			else if (name.equals("AD_Client_ID"))
			{
				AD_Client_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("AD_Org_ID"))
			{
				AD_Org_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("Number"))
			{
				number = (String)para[i].getParameter();
			}
			else if (name.equals("AfterDateTime"))
			{
				afterDateTime = (String)para[i].getParameter();
			}
			else if (name.equals("BeforeDateTime"))
			{
				beforeDateTime = (String)para[i].getParameter();
			}
			else if (name.equals("CountOnly"))
			{
				countOnly = "Y".equals(para[i].getParameter());
			}
			else if (name.equals("IgnoreInbound"))
			{
				ignoreInbound = "Y".equals(para[i].getParameter());
			}
			else
			{
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
		}
	}

	/**
	 * Process
	 * 
	 * @return message
	 * @throws Exception
	 */
	@Override
	protected String doIt() throws Exception
	{
		int originalAD_Client_ID = Env.getAD_Client_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Client_ID", AD_Client_ID);
		
		int originalAD_Org_ID = Env.getAD_Org_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Org_ID", AD_Org_ID);
		
		String msg = validate();
		
		if (msg == null)
			msg = insertRecords();
		
		Env.setContext(getCtx(), "#AD_Client_ID", originalAD_Client_ID);
		Env.setContext(getCtx(), "#AD_Org_ID", originalAD_Org_ID);
		
		return msg;
	}
	
	private String validate()
	{
		// Check number 
		if (number == null || number.length() < 1)
			return "@Error@ - Empty number";
		
		// Check date is right format
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try
		{
			sdf.parse(afterDateTime);
		}
		catch (ParseException ex)
		{
			return "@Error@ Failed to parse Date After";
		}
		
		try
		{
			sdf.parse(beforeDateTime);
		}
		catch (ParseException ex)
		{
			return "@Error@ Failed to parse Date Before";
		}

		return null;
	}
	
	private String insertRecords()
	{
		ArrayList<BillingRecord> billingRecords = BillingConnector.getBillingRecordsForNumber(number, afterDateTime, beforeDateTime);//"2011-02-01 14:24:39", "2011-03-10 00:00:00");		

		// Insert radius accounts 
		StringBuilder failedBillingRecords = new StringBuilder();
		int count = 0;
		for (BillingRecord billingRecord : billingRecords)
		{
			// Skip inbound records
			if (ignoreInbound && (BillingRecord.TYPE_INBOUND.equals(billingRecord.getType()) || !billingRecord.getOriginNumber().equals(number)))
				continue;
				
			if (countOnly)
			{
				count++;
				continue;
			}
			
			if (RadiusConnector.addRadiusAccount(billingRecord))
				count++;
			else
				failedBillingRecords.append(billingRecord.toString() + ", ");
		}
		
		if (countOnly)
			return "@Success@ - [Count Only] " + count + " records found.";
		
		if (failedBillingRecords.length() > 0)
		{
			return "@Error@ " + count + " billingrecords were inserted into radius table. The following records failed: " + failedBillingRecords.substring(0, failedBillingRecords.length() - 2);
		}
		else
			return "@Success@ - " + count + " billingrecords were inserted into radius table.";
	}
	
}
