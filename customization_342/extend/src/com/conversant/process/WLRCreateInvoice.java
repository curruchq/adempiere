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


import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MBPartner;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.PO;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereSystemError;
import org.compiere.util.CLogger;
import org.compiere.util.DB;

public class WLRCreateInvoice extends SvrProcess {


	/** Logger 																	*/
	private static CLogger log = CLogger.getCLogger(WLRCreateInvoice.class);
	
	/** Conversant Client														*/
	private int AD_Client_ID = 1000000; // Conversant
	
	/** Conversant Org															*/
	//private int AD_Org_ID = 1000001; // Conversant
	
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
		//String sql="SELECT INV.C_INVOICE_ID FROM C_INVOICE INV LEFT OUTER JOIN C_PAYMENT PAY ON (INV.C_INVOICE_ID=PAY.C_INVOICE_ID) " +
			//	"WHERE INV.C_DOCTYPE_ID = ? AND INV.C_BPARTNER_ID IN ("+res +")";
		
		String sql="SELECT INV.C_INVOICE_ID FROM C_INVOICE INV LEFT OUTER JOIN C_ALLOCATIONLINE PAY ON (INV.C_INVOICE_ID=PAY.C_INVOICE_ID) " +
		"WHERE INV.C_DOCTYPE_ID = ? AND INV.DOCSTATUS='CO' AND INV.C_BPARTNER_ID IN ("+res +") AND PAY.C_INVOICE_ID IS NULL";
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

	private String processInvoices() throws Exception
	{
		// Keep count of completed and failed documents
		String docInv="",oriInv="",cNotes="";
		Timestamp now = new Timestamp(System.currentTimeMillis());
		List<MInvoice> comInv=new ArrayList<MInvoice>();
		for(MInvoice invoice:eligibleInvoices)
		{
			if(!listOnly)
			{
				//invoice creation
				if(comInv.contains(invoice))
					continue;
				MInvoice dupInvoice=new MInvoice(getCtx(),0,get_TrxName());
				dupInvoice.setC_DocType_ID(invoice.getC_DocType_ID());
				dupInvoice.setC_Currency_ID(invoice.getC_Currency_ID());
				dupInvoice.setDateInvoiced(now);
				dupInvoice.setDateAcct(now);
				MBPartner reseller=getResellerDetails(invoice.getC_Invoice_ID());
				dupInvoice.setAD_User_ID(getUserContact(reseller.getC_BPartner_ID()));
				dupInvoice.setC_BPartner_ID(reseller.getC_BPartner_ID());
				
				int bpLocationID = getResellerLocation(reseller.getC_BPartner_ID());
				dupInvoice.setC_BPartner_Location_ID(bpLocationID);
				//dupInvoice.setC_BPartner_Location_ID(getResellerLocation(reseller.getC_BPartner_ID()));
				
				String paymentRule = getPaymentRule(reseller.getC_BPartner_ID(), bpLocationID);
				dupInvoice.setPaymentRule(paymentRule);
				
				dupInvoice.setC_PaymentTerm_ID(reseller.getC_PaymentTerm_ID());
				dupInvoice.setSalesRep_ID(reseller.getSalesRep_ID());
				dupInvoice.setM_PriceList_ID(reseller.getM_PriceList_ID());
			    dupInvoice.setGrandTotal(invoice.getGrandTotal());
				dupInvoice.setTotalLines(invoice.getTotalLines());
				dupInvoice.setC_DocTypeTarget_ID(invoice.getC_DocTypeTarget_ID());
				dupInvoice.setIsActive(true);
				dupInvoice.setIsSOTrx(true);
				dupInvoice.save();
				copyLinesFrom(invoice, dupInvoice);
				//dupInvoice.copyLinesFrom(invoice, false, false);
				for(MInvoice conInvoice:eligibleInvoices)
				{
					if(!comInv.contains(conInvoice))
					{
					if(conInvoice.getC_Invoice_ID()!=invoice.getC_Invoice_ID())
					{
						MBPartner mbp=getResellerDetails(conInvoice.getC_Invoice_ID());
						if(mbp.getC_BPartner_ID()==reseller.getC_BPartner_ID())
						{
							copyLinesFrom(conInvoice,dupInvoice);
							conInvoice.processIt("RC");
							conInvoice.save();
							comInv.add(conInvoice);
							cNotes+=conInvoice.getDescription()+" ";
						}
					}}
				}
				docInv+=dupInvoice.getDocumentNo()+" ";
				if(isComplete)
				{
					dupInvoice.processIt("CO");
					dupInvoice.save();
				}
				
				invoice.processIt("RC");
				invoice.save();
				cNotes+=invoice.getDescription()+" ";
			}
			else {
				oriInv+=invoice.getDocumentNo()+" ";
			}
		}
		
		if(listOnly)
			return "Original Invoices to be processed : "+oriInv;
		
		return "Invoices created = " + docInv+" Reversed Invoices = "+cNotes;
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
	
	private void copyLinesFrom(MInvoice originalInvoice,MInvoice wlrInvoice) throws Exception
	{
		MInvoiceLine[] originalInvoiceLines=originalInvoice.getLines();
	
		for (int i = 0; i < originalInvoiceLines.length; i++)
		{
			MInvoiceLine line = new MInvoiceLine (getCtx(), 0, get_TrxName());
			MInvoiceLine fromLine = originalInvoiceLines[i];
			PO.copyValues(fromLine, line);
			String desc=(fromLine.getDescription()!=null) ?fromLine.getDescription():" ";
			line.setDescription(originalInvoice.getC_BPartner().getValue()+"."+originalInvoice.getDocumentNo()+"."+desc);
			line.setC_Invoice_ID(wlrInvoice.getC_Invoice_ID());
			line.setInvoice(wlrInvoice);
			line.setC_OrderLine_ID(0);
			line.setRef_InvoiceLine_ID(0);
			line.setM_InOutLine_ID(0);
			line.setA_Asset_ID(0);
			if(fromLine.getM_AttributeSetInstance_ID() > 0)
			{
				MAttributeSetInstance masi = new MAttributeSetInstance(getCtx() ,fromLine.getM_AttributeSetInstance_ID() ,get_TrxName());
				MAttributeSetInstance masi_new = new MAttributeSetInstance(getCtx() , 0 ,get_TrxName());
				masi_new.setM_AttributeSet_ID(masi.getM_AttributeSet_ID());
			    MAttributeSetInstance.copyValues(masi, masi_new);
			    masi_new.setDescription(masi.getDescription()+"_"+originalInvoice.getC_BPartner().getValue()+"_"+originalInvoice.getDocumentNo());
				if(masi_new.save())
				   line.setM_AttributeSetInstance_ID(masi_new.get_ID());
				else
					line.setM_AttributeSetInstance_ID(0);
			}
			line.setS_ResourceAssignment_ID(0);
			line.setLine(getLineNo(wlrInvoice.getC_Invoice_ID()));
			line.setPeriodQty(fromLine.getPeriodQty());
			
			if (wlrInvoice.getC_BPartner_ID() != originalInvoice.getC_BPartner_ID())
				line.setTax();	//	recalculate
			line.setProcessed(false);
			if (!line.save(wlrInvoice.get_TrxName()))
				throw new AdempiereSystemError("Cannot save Invoice Line");
		
			line.copyLandedCostFrom(fromLine);
			line.allocateLandedCosts();
		}
	}
	
	private int getLineNo(int C_Invoice_ID)
	{
		String sqlLineNo="SELECT MAX(LINE) FROM C_INVOICELINE WHERE C_INVOICE_ID="+C_Invoice_ID;
		int lno=DB.getSQLValue(null, sqlLineNo);
		if(lno==-1)
			return 10;
		return lno;
	}
	
	
	private String getPaymentRule(int C_BPartner_ID , int C_BPartner_Location_ID)
	{
		String sql="SELECT COALESCE(l.PaymentRule,n''||bp.PaymentRule,n'P') FROM C_BPartner_Location l " +
				   "INNER JOIN C_BPartner bp ON (bp.C_BPARTNER_ID = l.C_BPARTNER_ID) " +
				   "WHERE bp.C_BPARTNER_ID=? AND l.ISACTIVE='Y' AND l.IsBillTo='Y' AND l.C_BPartner_Location_ID = ?";
		String paymentRule=DB.getSQLValueString(null, sql, C_BPartner_ID , C_BPartner_Location_ID);
		/*if(paymentRule.equals("") || paymentRule == null)
			return "P";*/
		return paymentRule;
	}
}
