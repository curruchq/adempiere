package com.conversant.test;

import java.math.BigDecimal;
import java.util.HashMap;

import org.compiere.model.MDiscountSchema;
import org.compiere.model.MDiscountSchemaBreak;
import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MProcessPara;
import org.compiere.util.Env;

public class InvoiceDiscountProcessTestCase extends ProcessTestCase
{
	private static final String PROCESS_CLASSNAME = "com.conversant.process.InvoiceDiscount";
	
	@Override
	public void setUp() throws Exception 
	{
		// Must be set before super.setUp()
		className = PROCESS_CLASSNAME;
		
		super.setUp();
		
		// Create process parameters
		MProcessPara pp = new MProcessPara(getCtx(), 0, getTrxName());
		pp.setAD_Process_ID(process.getAD_Process_ID());
		pp.setName("Invoice");
		pp.setColumnName("C_Invoice_ID");
		pp.setAD_Reference_ID(11); // Integer
		pp.setSeqNo(10);
		pp.save();
		
		pp = new MProcessPara(getCtx(), 0, getTrxName());
		pp.setAD_Process_ID(process.getAD_Process_ID());
		pp.setName("Discount Schema");
		pp.setColumnName("M_DiscountSchema_ID");
		pp.setAD_Reference_ID(11); // Integer
		pp.setSeqNo(20);
		pp.save();
	}
	
	public void testRun()
	{
		// Create test discount schema and break
		MDiscountSchema discountSchema = new MDiscountSchema(getCtx(), 0, getTrxName());
		discountSchema.setName("TEST_DISCOUNT_SCHEMA");
		discountSchema.setDiscountType(MDiscountSchema.DISCOUNTTYPE_Breaks);
		discountSchema.setCumulativeLevel(MDiscountSchema.CUMULATIVELEVEL_Line);
		discountSchema.save();
		
		MDiscountSchemaBreak discountSchemaBreak = new MDiscountSchemaBreak(getCtx(), 0, getTrxName());
		discountSchemaBreak.setSeqNo(10);
		discountSchemaBreak.setM_DiscountSchema_ID(discountSchema.getM_DiscountSchema_ID());
		discountSchemaBreak.setM_Product_Category_ID(0);
		discountSchemaBreak.setM_Product_ID(1000000);
		discountSchemaBreak.setBreakValue(new BigDecimal(3.0));
		discountSchemaBreak.setBreakDiscount(new BigDecimal(10.0));
		discountSchemaBreak.save();
		
		// Create test invoice(s)
		MInvoice invoice = new MInvoice(getCtx(), 0, getTrxName());
		invoice.setAD_Org_ID(1000001);
		invoice.setC_DocTypeTarget_ID(MDocType.DOCBASETYPE_ARInvoice);
		invoice.setC_BPartner_ID(1000000);
		invoice.setC_BPartner_Location_ID(1000000);
		invoice.save();
		
		MInvoiceLine line1 = new MInvoiceLine(getCtx(), 0, getTrxName());
		line1.setAD_Org_ID(1000001);
		line1.setC_Invoice_ID(invoice.getC_Invoice_ID());
		line1.setM_Product_ID(1000000);
		line1.setPrice(Env.ONEHUNDRED);
		line1.setQty(1);
		line1.save();
		
		MInvoiceLine line2 = new MInvoiceLine(getCtx(), 0, getTrxName());
		line2.setAD_Org_ID(1000001);
		line2.setC_Invoice_ID(invoice.getC_Invoice_ID());
		line2.setM_Product_ID(1000000); 
		line2.setPrice(Env.ONEHUNDRED); 
		line2.setQty(1);
		line2.save();
		
		MInvoiceLine line3 = new MInvoiceLine(getCtx(), 0, getTrxName());
		line3.setAD_Org_ID(1000001);
		line3.setC_Invoice_ID(invoice.getC_Invoice_ID());
		line3.setM_Product_ID(1000000); 
		line3.setPrice(Env.ONEHUNDRED); 
		line3.setQty(1);
		line3.save();

		// Create instance and set parameters 
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("C_Invoice_ID", new Integer(invoice.getC_Invoice_ID()));
		parameters.put("M_DiscountSchema_ID", new Integer(discountSchema.getM_DiscountSchema_ID()));		
		createProcessInstance(parameters);
	
		assertTrue(processInfo.getSummary(), runProcess());				
	}
}
