package com.conversant.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;

import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class BatchInvoiceComplete extends SvrProcess
{
	/** Logger 																	*/
	private static CLogger log = CLogger.getCLogger(BatchInvoiceComplete.class);
	
	/** Conversant Client														*/
	private int AD_Client_ID = 1000000; // Conversant
	
	/** Conversant Org															*/
	private int AD_Org_ID = 1000001; // Conversant
	
	/** Document Type of invoice's to select									*/
	private int C_DocType_ID = 0;
	
	/** Should process use BPartner's isProspect field when selecting invoices	*/
	private boolean useIsProspect = true;
	
	/** Date Invoiced Range				    									*/
	private Timestamp dateInvoicedFrom = null;
	private Timestamp dateInvoicedTo = null;
	
	/** Only show list, don't complete any documents							*/
	private boolean listOnly = true;
	
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
			else if (name.equals("C_DocType_ID"))
			{
				C_DocType_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("UseIsProspect"))
			{
				useIsProspect = "Y".equals(para[i].getParameter());
			}
			else if (name.equals("DateInvoiced"))
			{
				dateInvoicedFrom = (Timestamp)para[i].getParameter();
				dateInvoicedTo = (Timestamp)para[i].getParameter_To();
			}
			else if (name.equals("ListOnly"))
			{
				listOnly = "Y".equals(para[i].getParameter());
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
		// Set client and org (useful when run via scheduler)
		int originalAD_Client_ID = Env.getAD_Client_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Client_ID", AD_Client_ID);
		
		int originalAD_Org_ID = Env.getAD_Org_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Org_ID", AD_Org_ID);
		
		try
		{
			// Validate parameters
			String msg = validate();
			
			// If validation passed validate invoices
			if (msg == null)
				msg = processInvoices();
			
			return msg;
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			// Reset client and org 
			Env.setContext(getCtx(), "#AD_Client_ID", originalAD_Client_ID);
			Env.setContext(getCtx(), "#AD_Org_ID", originalAD_Org_ID);
		}		
	}
	
	private String validate()
	{
		StringBuilder sb = new StringBuilder();

		// Check document type target is valid and its base is AP Invoice
		if (C_DocType_ID > 0)
		{
			MDocType docType = new MDocType(getCtx(), C_DocType_ID, get_TrxName());
			if (docType == null || docType.get_ID() == 0 || !MDocType.DOCBASETYPE_ARInvoice.equals(docType.getDocBaseType()))
				sb.append("Cannot load Document Type " + C_DocType_ID + " and/or does not have a DocBaseType of " + MDocType.DOCBASETYPE_ARInvoice);
		}
		
		// Check dateInvoicedFrom timestamp before dateInvoicedTo
		if (dateInvoicedFrom != null && dateInvoicedTo != null)
		{
			if (!dateInvoicedFrom.before(dateInvoicedTo))
			{
				if (sb.length() > 0)
					sb.append(", ");
				sb.append("DateInvoicedFrom isn't before DateInvoicedTo");
			}
		}
		// Only one field set
		else if ((dateInvoicedFrom == null && dateInvoicedTo != null) || (dateInvoicedFrom != null && dateInvoicedTo == null)) 
		{
			if (sb.length() > 0)
				sb.append(", ");
			
			sb.append("Date Invoiced range must be complete or empty, a single date is not accepted");
		}
		
		if (sb.length() > 0)
			return "@Error@" + sb.toString();
		
		return null;
	}
	
	private String processInvoices()
	{
		// Where clause to select subset of invoices
		String sql = "SELECT * FROM " + MInvoice.Table_Name + " WHERE " + 
					   " AD_Client_ID=?" +
					   " AND Processing='N'" + 
					   " AND Posted='N'" + 
					   " AND IsActive='Y'" + 
					   " AND DocStatus='DR'";

		if (C_DocType_ID > 0)
			sql += " AND C_DocTypeTarget_ID=?";
		
		if (useIsProspect)
			sql += " AND C_BPartner_ID IN (SELECT C_BPartner_ID FROM C_BPartner WHERE IsProspect='N')";
		
		if (dateInvoicedFrom != null && dateInvoicedTo != null)
			sql += " AND DateInvoiced BETWEEN ? AND ?";
						
		// Keep count of completed and failed documents
		int countSuccess = 0;
		int countError = 0;
		
		// Load invoices
		ArrayList<MInvoice> invoices = new ArrayList<MInvoice>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			// Track parameter index
			int index = 1;
			
			// Create statement and set parameters
			pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			pstmt.setInt(index++, AD_Client_ID);
			
			if (C_DocType_ID > 0)
				pstmt.setInt(index++, C_DocType_ID);
			
			if (dateInvoicedFrom != null && dateInvoicedTo != null)
			{
				pstmt.setTimestamp(index++, dateInvoicedFrom);
				pstmt.setTimestamp(index++, dateInvoicedTo);
			}
			
			// Execute query and process result set
			rs = pstmt.executeQuery();
			while (rs.next())
				invoices.add(new MInvoice(getCtx(), rs, get_TrxName()));
		}
		catch (SQLException ex)
		{
			log.log(Level.SEVERE, sql.toString(), ex);
		}
		finally 
		{
			DB.close(rs, pstmt);
			rs = null; 
			pstmt = null;
		}

		// Loop through invoices
		for (MInvoice invoice : invoices)
		{
			if (!listOnly)
			{
				// Get original doc status name for log msg below
				String originalDocStatusName = invoice.getDocStatusName();
				
				// Try to complete invoice
				invoice.completeIt();
				
				// Save regardless
				invoice.save();
				
				// Check status
				if (MInvoice.DOCSTATUS_Completed.equals(invoice.getDocStatus()))
					countSuccess++;
				else
					countError++;
	
				// Log message regardless of outcome
				String msg = originalDocStatusName + "->" + invoice.getDocStatusName() + " - " + invoice.getDocumentInfo();
				addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);	
			}
			else
			{
				String msg = invoice.getDocStatusName() + " - " + invoice.getDocumentInfo();
				addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);	
			}
		}
		
		// Report counts
		return "@Completed@ = " + countSuccess + " - @Errors@ = " + countError;
	}
	
	public static void main(String[] args)
	{

	}
}
