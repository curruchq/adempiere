/**
 * 
 */
package com.conversant.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MProductPricing;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

/**
 * @author lnandyal
 * 
 */
public class InvoiceRepricing extends SvrProcess {

	/** Logger */
	private static CLogger log = CLogger.getCLogger(InvoiceRepricing.class);

	/** Conversant Client */
	private int AD_Client_ID = 1000000;

	/** Conversant Org */
	private int AD_Org_ID = 1000001;

	/** Invoice to apply discount to (optional) */
	private int p_C_Invoice_ID = 0;

	/** Reprice the invoices related to a Business Partner Group */
	private int p_C_BP_Group_ID = 0;

	/** Reprice all the invoices related to a Business Partner */
	private int p_C_BPartner_ID = 0;

	/** Ignore zero priced invoices */
	private boolean listOnly = true;
	
	/** Document Type of invoice's to select									*/
	private int p_C_DocType_ID = 0;
	
	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++) {
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("AD_Client_ID")) {
				AD_Client_ID = ((BigDecimal) para[i].getParameter()).intValue();
			} else if (name.equals("AD_Org_ID")) {
				AD_Org_ID = ((BigDecimal) para[i].getParameter()).intValue();
			} else if (name.equals("C_Invoice_ID")) {
				p_C_Invoice_ID = ((BigDecimal) para[i].getParameter())
						.intValue();
			} else if (name.equals("ListOnly")) {
				listOnly = "Y".equals(para[i].getParameter());
			} else if (name.equals("C_BP_Group_ID")) {
				p_C_BP_Group_ID = para[i].getParameterAsInt();
			} else if (name.equals("C_BPartner_ID")) {
				p_C_BPartner_ID = para[i].getParameterAsInt();
			}else if (name.equals("C_DocType_ID")) 
			{
				p_C_DocType_ID=para[i].getParameterAsInt();
			}
			else {
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
		}
	}

	@Override
	protected String doIt() throws Exception {
		// Set client and org (useful when run via scheduler)
		int originalAD_Client_ID = Env.getAD_Client_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Client_ID", AD_Client_ID);
		
		int originalAD_Org_ID = Env.getAD_Org_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Org_ID", AD_Org_ID);
		try
		{
			// Validate parameters
			String msg = validate();
			if(msg==null)
			{
					msg=repriceInvoices();
			}
			else
				return msg;
			//addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
			return "Invoices Repriced successfully";
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
	
	private List<MInvoice> getInvoiceList()
	{
		// Load invoice(s)
		ArrayList<MInvoice> invoices = new ArrayList<MInvoice>();		
		if (p_C_Invoice_ID > 0)
		{
			invoices.add(new MInvoice(getCtx(), p_C_Invoice_ID, get_TrxName()));
		}
		else
		{	
			String sql = "SELECT * FROM " + MInvoice.Table_Name + " INV "; 
			if(p_C_BP_Group_ID > 0)
				sql+=" INNER JOIN C_BPARTNER BP ON (BP.C_BPARTNER_ID=INV.C_BPARTNER_ID) ";
			sql+=" WHERE INV.AD_Client_ID=?" + // 1
			   " AND INV.Processing='N'" + 
			   " AND INV.Posted='N'" + 
			   " AND INV.IsActive='Y'" + 
			   " AND INV.DocStatus='DR'";
			if(p_C_BPartner_ID > 0)
				sql+=" AND INV.C_BPartner_ID = "+p_C_BPartner_ID;
			if(p_C_BP_Group_ID > 0)
				sql+=" AND BP.C_BP_Group_ID = "+p_C_BP_Group_ID;
			if(p_C_DocType_ID > 0)
				sql+=" AND INV.C_DocTypeTarget_ID = "+p_C_DocType_ID;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{
				// Create statement and set parameters
				pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
				pstmt.setInt(1, AD_Client_ID);
				
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
		}
		return invoices;
	}
	
	private String validate()
	{
		StringBuilder sb = new StringBuilder();

		// Check invoice (if set) is valid and doc status is Drafted
		if (p_C_Invoice_ID > 0)
		{
			MInvoice invoice = new MInvoice(getCtx(), p_C_Invoice_ID, get_TrxName());
			if (invoice == null || invoice.get_ID() == 0)
				sb.append("Cannot load MInvoice[" + p_C_Invoice_ID + "]");
			else if (!MInvoice.DOCSTATUS_Drafted.equals(invoice.getDocStatus()))
				sb.append("Invoice's document status does not match 'Drafted'");
			
			if(p_C_DocType_ID > 0 && invoice.getC_DocTypeTarget_ID() != p_C_DocType_ID)
				sb.append("Invoice Document Type and the parameter Document Type do not match");
			
			if(p_C_BPartner_ID > 0 && invoice.getC_BPartner_ID() != p_C_BPartner_ID)
				sb.append("Invoice Business partner and parameter Business partner are different");
		}
		if(p_C_BP_Group_ID > 0 && p_C_BPartner_ID > 0)
		{
			MBPartner bp=new MBPartner(getCtx(),p_C_BPartner_ID,get_TrxName());
			if(bp == null || bp.get_ID() == 0)
			{
				if (sb.length() > 0)
					sb.append(", ");
				
				sb.append("Cannot load Business Partner [" + p_C_BPartner_ID + "]");
			}
			else
			{
				if( bp.getC_BP_Group_ID() != p_C_BP_Group_ID)
				{
					if (sb.length() > 0)
						sb.append(", ");
					
					sb.append("Business Partner doesnt belong to the selected Business Partner Group");
				}
			}
		}
		if (sb.length() > 0)
			return "@Error@" + sb.toString();

		return null;
}
	
	private String repriceInvoices()
	{
		// Keep count of completed and failed documents
		int countError = 0;
		String retValue = "";
		MProductPricing	 m_productPricing = null;
		for(MInvoice invoice:getInvoiceList())
		{  		
			BigDecimal oldPrice = invoice.getGrandTotal();
			MInvoiceLine[] lines = invoice.getLines(false);
			if(lines.length == 0)
			{
				retValue = invoice.getDocumentNo() + ":  has no invoice lines " ;
				addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, retValue);
				continue;
			}
				
			for (int i = 0; i < lines.length; i++)
			{
				m_productPricing = new MProductPricing (lines[i].getM_Product_ID(), 
						invoice.getC_BPartner_ID(), lines[i].getQtyInvoiced(), true);
					m_productPricing.setM_PriceList_ID(invoice.getM_PriceList_ID());
					m_productPricing.setPriceDate(invoice.getDateInvoiced());
				
				if(!listOnly && m_productPricing.getPriceList().compareTo(Env.ZERO) != 0)
				{
					lines[i].setPrice(invoice.getM_PriceList_ID(), invoice.getC_BPartner_ID());
					lines[i].save();
				}
				else continue;
			}
			
			MInvoice invoiceTemp = new MInvoice (getCtx(), invoice.get_ID(), get_TrxName());
			BigDecimal newPrice = invoiceTemp.getGrandTotal();
	
			retValue = invoice.getDocumentNo() + ":  " + oldPrice + " -> " + newPrice;
			addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, retValue);
		}
		
		if (countError>0)
		    return "@Errors@ = " + countError;
		return null;
	}
	
}