package com.conversant.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.I_C_BPartner;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoicePaySchedule;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.DB;

public class WLRCreateInvoice extends SvrProcess {
	private static final Object C_BParnter_ID = null;

	/** Logger 																	*/
	private static CLogger log = CLogger.getCLogger(WLRCreateInvoice.class);
	
	/** Conversant Client														*/
	private int AD_Client_ID = 1000000; // Conversant
	
	/** Conversant Org															*/
	private int AD_Org_ID = 1000001; // Conversant
	
	/** Document Type of invoice's to select									*/
	private int C_DocType_ID = 0;

	/** Business Partner Group									*/
	private int C_BP_Group_ID = 0;
	
	/** Only show list, don't complete any documents							*/
	private boolean listOnly = true;
	
	/** complete any documents based on isComplete is true or false							*/
	private boolean isComplete = true;
	private List<Integer> eligibleEndCustomerList=new ArrayList<Integer>();
	private List<MInvoice> eligibleInvoices=new ArrayList<MInvoice>();
	
	@Override
	protected String doIt() throws Exception {
		getEligibleEndCustomers();
		if(eligibleEndCustomerList.isEmpty())
			return "There are no eligible end customers for the Business Partner Group selected";
		getEligibleInvoices();
		if(eligibleInvoices.isEmpty())
			return "There are no eligible invoices to be processed";
		String created=processInvoices();
		return created;
	}
	
	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("C_DocType_ID"))
			{
				C_DocType_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("isComplete"))
			{
				isComplete = "Y".equals(para[i].getParameter());
			}
			else if (name.equals("C_BP_Group_ID"))
			{
				C_BP_Group_ID = ((BigDecimal)para[i].getParameter()).intValue();
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
	
	private List<Integer> getEligibleEndCustomers()
	{	
		String sql="SELECT C_BPartner_ID FROM C_BPartner WHERE AD_CLIENT_ID= ? AND SalesRep_ID IN " +
				   "(SELECT AD_USER_ID FROM AD_USER WHERE C_BPARTNER_ID IN " +
				   "(SELECT C_BPARTNER_ID FROM C_BPARTNER WHERE C_BP_GROUP_ID = ?)" +
				   ")";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{	
			// Create statement and set parameters
			pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setInt(2, C_BP_Group_ID);
			// Execute query and process result set
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				eligibleEndCustomerList.add(rs.getInt(1));
			}
				
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
		return eligibleEndCustomerList;
	}
	
	private List<MInvoice> getEligibleInvoices()
	{
		String res="";
		for (Iterator<Integer> iterator = eligibleEndCustomerList.iterator(); iterator.hasNext();) {
            res += iterator.next() + (iterator.hasNext() ? "," : "");
        }
		String sql="SELECT INV.C_INVOICE_ID FROM C_INVOICE INV LEFT OUTER JOIN C_PAYMENT PAY ON (INV.C_INVOICE_ID=PAY.C_INVOICE_ID) " +
				"WHERE INV.C_DOCTYPE_ID = ? AND INV.C_BPARTNER_ID IN ("+res +")";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{	
			// Create statement and set parameters
			pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			pstmt.setInt(1, C_DocType_ID);
			// Execute query and process result set
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				MInvoice invoice=new MInvoice(getCtx(),rs.getInt(1),get_TrxName());
				eligibleInvoices.add(invoice);
			}
				
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
		return eligibleInvoices;
	}

	private String processInvoices()
	{
		// Keep count of completed and failed documents
		int countSuccess = 0;
		int countError = 0;
		String docCN="",docInv="";
		for(MInvoice invoice:eligibleInvoices)
		{
			if(!listOnly)
			{
				//credit note creation
				MInvoice creditNote=new MInvoice(getCtx(),0,get_TrxName());
				creditNote.setC_DocType_ID(1000004);
				creditNote.setC_Currency_ID(invoice.getC_Currency_ID());
				creditNote.setDateInvoiced(invoice.getDateInvoiced());
				creditNote.setDateAcct(invoice.getDateAcct());
				creditNote.setSalesRep_ID(invoice.getSalesRep_ID());
				creditNote.setAD_User_ID(invoice.getAD_User_ID());
				creditNote.setC_BPartner_ID(invoice.getC_BPartner_ID());
				creditNote.setC_BPartner_Location_ID(invoice.getC_BPartner_Location_ID());
				creditNote.setC_PaymentTerm_ID(invoice.getC_PaymentTerm_ID());
				creditNote.setGrandTotal(invoice.getGrandTotal());
				creditNote.setTotalLines(invoice.getTotalLines());
				creditNote.setM_PriceList_ID(invoice.getM_PriceList_ID());
				creditNote.setC_DocTypeTarget_ID(1000004);
				creditNote.setIsActive(true);
				creditNote.setIsSOTrx(true);
				creditNote.save();
				creditNote.copyLinesFrom(invoice, false, false);
				docCN+=creditNote.getDocumentNo()+" ";
				if(isComplete)
				{
					creditNote.completeIt();
					creditNote.save();
				}
				
				//invoice creation
				MInvoice dupInvoice=new MInvoice(getCtx(),0,get_TrxName());
				dupInvoice.setC_DocType_ID(invoice.getC_DocType_ID());
				dupInvoice.setC_Currency_ID(invoice.getC_Currency_ID());
				dupInvoice.setDateInvoiced(invoice.getDateInvoiced());
				dupInvoice.setDateAcct(invoice.getDateAcct());
				MBPartner reseller=getResellerDetails(invoice.getC_Invoice_ID());
				dupInvoice.setAD_User_ID(getUserContact(reseller.getC_BPartner_ID()));
				dupInvoice.setC_BPartner_ID(reseller.getC_BPartner_ID());
				dupInvoice.setC_BPartner_Location_ID(getResellerLocation(reseller.getC_BPartner_ID()));
				dupInvoice.setC_PaymentTerm_ID(reseller.getC_PaymentTerm_ID());
				dupInvoice.setDescription(reseller.getValue()+"-"+invoice.getDocumentNo());
				dupInvoice.setGrandTotal(invoice.getGrandTotal());
				dupInvoice.setTotalLines(invoice.getTotalLines());
				dupInvoice.setM_PriceList_ID(invoice.getM_PriceList_ID());
				dupInvoice.setC_DocTypeTarget_ID(invoice.getC_DocTypeTarget_ID());
				dupInvoice.setIsActive(true);
				dupInvoice.setIsSOTrx(true);
				dupInvoice.save();
				dupInvoice.copyLinesFrom(invoice, false, false);
				docInv+=dupInvoice.getDocumentNo()+" ";
				if(isComplete)
				{
					dupInvoice.completeIt();
					dupInvoice.save();
				}
			}
		}
		
		//return null;
		return "Credit Notes = " + docCN + " - Invoices = " + docInv;
	}
	
	private MBPartner getResellerDetails(int C_Invoice_ID)
	{
		String sql="SELECT C_BPARTNER_ID FROM AD_USER WHERE AD_USER_ID= (SELECT SALESREP_ID FROM C_BPARTNER WHERE C_BPARTNER_ID=(SELECT C_BPARTNER_ID FROM C_INVOICE WHERE C_INVOICE_ID=?))";
		int bp_id=DB.getSQLValue(null, sql, C_Invoice_ID);
		MBPartner reseller=new MBPartner(getCtx(),bp_id,null);
        return reseller;
	}
	
	private int getResellerLocation(int C_BPartner_ID)
	{
		String sql="SELECT C_BPartner_Location_ID FROM C_BPartner_Location WHERE C_BPARTNER_ID=? AND ISACTIVE='Y'";
		int bp_loc_id=DB.getSQLValue(null, sql, C_BPartner_ID);
        return bp_loc_id;
	}
	
	private int getUserContact(int C_BPartner_ID)
	{
		String sql="SELECT U.AD_USER_ID FROM AD_USER U, AD_USER_ROLES UR WHERE U.C_BPARTNER_ID=? AND U.AD_USER_ID=UR.AD_USER_ID AND UR.AD_ROLE_ID=1000019 AND ROWNUM<=1";
		int user_id=DB.getSQLValue(null, sql, C_BPartner_ID);
		if(user_id==-1)
		{
			sql="SELECT AD_USER_ID FROM AD_USER WHERE C_BPARTNER_ID=? AND ISACTIVE='Y'";
			user_id=DB.getSQLValue(null, sql, C_BPartner_ID);
		}
		return user_id;
	}
}
