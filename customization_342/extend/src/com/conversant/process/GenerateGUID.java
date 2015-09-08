package com.conversant.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.compiere.model.MInvoice;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class GenerateGUID extends SvrProcess 
{
	
	/** Logger 																	*/
	private static CLogger log = CLogger.getCLogger(GenerateGUID.class);
	
	/** Conversant Client														*/
	private int AD_Client_ID = 1000000; 
	
	/** Conversant Org															*/
	private int AD_Org_ID ; 


	@Override
	protected String doIt() throws Exception {
		// Set client and org (useful when run via scheduler)
		int originalAD_Client_ID = Env.getAD_Client_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Client_ID", AD_Client_ID);
		
		AD_Org_ID = Env.getAD_Org_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Org_ID", AD_Org_ID);
		
		try
		{
			String msg = null;
			if(msg==null)
			{
					msg=generateGUID();
			}
			return "Process completed successfully";
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			// Reset client and org 
			Env.setContext(getCtx(), "#AD_Client_ID", originalAD_Client_ID);
		}
	}

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		
	}

	public String generateGUID()
	{
		// Keep count of completed and failed documents
		int countError = 0;
		String errorMsg=null;
		for(MInvoice invoice : getInvoiceList())
		{
			UUID guid=UUID.randomUUID();
			invoice.setGUID(guid.toString());
			if(!invoice.save())
			{
				countError++;
				 errorMsg = errorMsg + " Could not generate GUID for Invoice [ "+ invoice.get_ID() +" ]";
				 addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, errorMsg);
			}
			String msg = "GUID generated for Invoice [ "+invoice.get_ID() + " ]";
			addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
		}
		return errorMsg;
	}
	
	private List<MInvoice> getInvoiceList()
	{
		// Load invoice(s)
		ArrayList<MInvoice> invoices = new ArrayList<MInvoice>();		
		String sql = "SELECT * FROM " + MInvoice.Table_Name + " INV "; 
			sql+=" WHERE INV.AD_Client_ID=?" + // 1
			   " AND INV.AD_Org_ID = "+AD_Org_ID  + 
			   " AND INV.GUID IS null AND rownum <=10000";
		
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
		
		return invoices;
	}
}
