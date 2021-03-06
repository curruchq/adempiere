package com.conversant.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.model.MDiscountSchema;
import org.compiere.model.MDiscountSchemaBreak;
import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MProduct;
import org.compiere.model.MProductCategory;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Login;
import org.compiere.util.Trx;


public class InvoiceDiscount extends SvrProcess
{
	/** Logger 																	*/
	private static CLogger log = CLogger.getCLogger(InvoiceDiscount.class);
	
	/** Conversant Client														*/
	private int AD_Client_ID = 1000000; 
	
	/** Conversant Org															*/
	private int AD_Org_ID ; 

	/** Invoice to apply discount to (optional)									*/
	private int C_Invoice_ID = 0;
	
	/** Discount schema to get discount values from								*/
	private int M_DiscountSchema_ID = 0;
	
	/** Only show list, don't add lines											*/
	private boolean listOnly = true;
	
	/** Business Partner to apply discount to (optional)                        */
	private int p_C_BPartner_ID =0;
	
	/**	BP Group					*/
	private int			p_C_BP_Group_ID = 0;
	private int p_M_DiscountSchema_ID=0;
    private boolean applyMaxDiscount;
    private boolean addDiscountLines;
    private boolean removeDiscountLines;
	private List<Integer> discountProductsList=new ArrayList<Integer>();
	private List<Integer> discountProdCategoryList=new ArrayList<Integer>();
	/**	Breaks							*/
	private MDiscountSchemaBreak[]	m_breaks  = null;
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
			else if (name.equals("C_Invoice_ID"))
			{
				C_Invoice_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("M_DiscountSchema_ID"))
			{
				M_DiscountSchema_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("ListOnly"))
			{
				listOnly = "Y".equals(para[i].getParameter());
			}
			else if (name.equals("C_BP_Group_ID"))
			{
				p_C_BP_Group_ID=para[i].getParameterAsInt();
			}
			else if(name.equals("C_BPartner_ID"))
			{
				p_C_BPartner_ID=para[i].getParameterAsInt();
			}
			else if(name.equals("isAddDiscountLines"))
			{
				addDiscountLines = "Y".equals(para[i].getParameter());
			}
			else if(name.equals("isRemoveDiscountLines"))
			{
				removeDiscountLines = "Y".equals(para[i].getParameter());
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
		/*int originalAD_Client_ID = Env.getAD_Client_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Client_ID", AD_Client_ID);
		
		int originalAD_Org_ID = Env.getAD_Org_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Org_ID", AD_Org_ID);*/
		
		try
		{
			// Validate parameters
			String msg = validate();
			if(msg==null)
			{
				if(addDiscountLines)
					msg=applyEscDiscountV2();
				else
					msg=applyEscDiscount();
				
			}
			return msg;
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			// Reset client and org 
			/*Env.setContext(getCtx(), "#AD_Client_ID", originalAD_Client_ID);
			Env.setContext(getCtx(), "#AD_Org_ID", originalAD_Org_ID);*/
		}		
	}
	
	

	private String validate()
	{
		StringBuilder sb = new StringBuilder();

		// Check invoice (if set) is valid and doc status is Drafted
		if (C_Invoice_ID > 0)
		{
			MInvoice invoice = new MInvoice(getCtx(), C_Invoice_ID, get_TrxName());
			if (invoice == null || invoice.get_ID() == 0)
				sb.append("Cannot load MInvoice[" + C_Invoice_ID + "]");
			else if (!MInvoice.DOCSTATUS_Drafted.equals(invoice.getDocStatus()))
				sb.append("Invoice's document status does not match 'Drafted'");
			if(M_DiscountSchema_ID == 0 && p_C_BPartner_ID == 0)
			{
				MBPartner bp=new MBPartner(getCtx(),invoice.getC_BPartner_ID(),get_TrxName());
				if(bp == null || bp.get_ID() == 0)
				{
					if (sb.length() > 0)
						sb.append(", ");
					
					sb.append("Cannot load Business Partner [" + p_C_BPartner_ID + "] for Invoice ["+ C_Invoice_ID +"]");
				}
				else
				{
					if(bp.getM_DiscountSchema_ID() == 0)
					{
						if (sb.length() > 0)
							sb.append(", ");
						
						sb.append("No Discount Schema for Business Partner [" + bp.getName() + "]");
					}
					else
						p_M_DiscountSchema_ID=bp.getM_DiscountSchema_ID();
				}
				if(p_C_BP_Group_ID > 0 && bp.getC_BP_Group_ID() != p_C_BP_Group_ID)
				{
					if (sb.length() > 0)
						sb.append(", ");
					
					sb.append("Business Partner doesnt belong to the selected Business Partner Group");
				}
			}
			else if(M_DiscountSchema_ID == 0 && p_C_BPartner_ID > 0)
			{
				MBPartner bp=null;
				if(invoice.getC_BPartner_ID() != p_C_BPartner_ID)
				{
					if (sb.length() > 0)
						sb.append(", ");
					
					sb.append("Cant apply discount to the invoice as Invoice BP and selected BP do not match");
				}
				else
				{
					bp=new MBPartner(getCtx(),p_C_BPartner_ID,get_TrxName());
					if(bp == null || bp.get_ID() == 0)
					{
						if (sb.length() > 0)
							sb.append(", ");
						
						sb.append("Cannot load Business Partner [" + p_C_BPartner_ID + "] for Invoice ["+ C_Invoice_ID +"]");
					}
					else
					{
						if(bp.getM_DiscountSchema_ID() == 0)
						{
							if (sb.length() > 0)
								sb.append(", ");
							
							sb.append("No Discount Schema for Business Partner [" + bp.getName() + "]");
						}
						else
							p_M_DiscountSchema_ID=bp.getM_DiscountSchema_ID();
					}
					if(p_C_BP_Group_ID > 0 && bp.getC_BP_Group_ID() != p_C_BP_Group_ID)
					{
						if (sb.length() > 0)
							sb.append(", ");
						
						sb.append("Business Partner doesnt belong to the selected Business Partner Group");
					}
				}
			}
		}
		
		// Check discount schema is set and has at least one break
		String error=validateDiscountSchema(M_DiscountSchema_ID);
		if(error != null)
		{
			if (sb.length() > 0)
				sb.append(", ");
			
			sb.append(error);
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
		
		if(p_C_BPartner_ID > 0 && M_DiscountSchema_ID > 0)
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
				if(bp.getM_DiscountSchema_ID() != M_DiscountSchema_ID)
				{
					if (sb.length() > 0)
						sb.append(", ");
					
					sb.append("Business Partner Discount Schema and selected Discount Schema are different");
				}
			}
		}
		
		if (sb.length() > 0)
			return "@Error@" + sb.toString();
		
		return null;
	}
	
	/*private String applyDiscount()
	{
		// Load invoice(s)
		ArrayList<MInvoice> invoices = new ArrayList<MInvoice>();		
		if (C_Invoice_ID > 0)
		{
			invoices.add(new MInvoice(getCtx(), C_Invoice_ID, get_TrxName()));
		}
		else
		{
			String sql = "SELECT * FROM " + MInvoice.Table_Name + " WHERE " + 
			   " AD_Client_ID=?" + // 1
			   " AND Processing='N'" + 
			   " AND Posted='N'" + 
			   " AND IsActive='Y'" + 
			   " AND DocStatus='DR'";
			
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
		
		// Load discount schema
		MDiscountSchema discountSchema = new MDiscountSchema(getCtx(), M_DiscountSchema_ID, get_TrxName());
		MDiscountSchemaBreak[] discountSchemaBreaks = discountSchema.getBreaks(true);
		
		// Keep count of completed and failed documents
		int countSuccess = 0;
		int countError = 0;
		
		// Loop through each invoice and schema discount breaks
		for (MInvoice invoice : invoices)
		{
			for (MDiscountSchemaBreak discountSchemaBreak : discountSchemaBreaks)
			{				
				MProduct discountProduct = new MProduct(getCtx(), discountSchemaBreak.getM_Product_ID(), get_TrxName());				
				MProductCategory discountProductCategory = new MProductCategory(getCtx(), discountSchemaBreak.getM_Product_Category_ID(), get_TrxName());
				
				// Loop through each line and match product or product category
				int matches = 0;
				BigDecimal amountToDiscount = Env.ZERO;
				for (MInvoiceLine line : invoice.getLines())
				{
					// Check it line represents a product
					MProduct lineProduct = new MProduct(getCtx(), line.getM_Product_ID(), get_TrxName());	
					if (lineProduct == null || lineProduct.get_ID() == 0)
						continue;
					
					// Use breaks product (first choice)
					boolean match = false;
					if (discountProduct != null && discountProduct.get_ID() > 0)
					{
						if (discountProduct.getM_Product_ID() == lineProduct.getM_Product_ID())							
							match = true;
					}
					// Use product category (second choice, always set if product isn't, else validation would fail)
					else
					{
						if (discountProductCategory.getM_Product_Category_ID() == lineProduct.getM_Product_Category_ID())
							match = true;
					}	
					
					if (match)
					{
						matches += line.getQtyInvoiced().intValue();
						
						if (matches > discountSchemaBreak.getBreakValue().intValue())
							amountToDiscount = amountToDiscount.add(line.getLineNetAmt());
					}
				}
				
				// Add discount line to invoice
				if (amountToDiscount.compareTo(Env.ZERO) > 0)
				{
					// discount = total line amount / 100 * break discount
					BigDecimal discount = amountToDiscount.divide(Env.ONEHUNDRED).multiply(discountSchemaBreak.getBreakDiscount());
					
					if (!listOnly)
					{
						MInvoiceLine discountLine = new MInvoiceLine(getCtx(), 0, get_TrxName());						
						discountLine.setC_Invoice_ID(invoice.getC_Invoice_ID());
						discountLine.setC_Charge_ID((Integer)discountSchemaBreak.get_Value(MCharge.COLUMNNAME_C_Charge_ID));
						discountLine.setPrice(discount.negate()); 					
						discountLine.setQty(1);
						discountLine.setDescription(discountSchema.getDescription());
						
						String msg = "A discount line for " + discount;
						
						if (discountLine.save())
						{
							msg += " has been ";
							countSuccess++;
						}
						else
						{
							msg += " failed to be ";
							countError++;
						}
						
						// Log message regardless of outcome
						msg += " added to " + invoice.getDocumentNo();
						addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
					}
					else
					{
						String msg = "A discount line for " + discount + " would have been added to " + invoice.getDocumentNo();
						addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
					}
				}
//				else if (matches > 0)
//				{						
//					String msg = "One of more lines matched the Discount Schema but the discount was not a positive amount for " + invoice.getDocumentNo();
//					addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
//				}
			}			
		}
		
		return "@Completed@ = " + countSuccess + " - @Errors@ = " + countError;
	}
	*/
	public static void main(String[] args)
	{
		// Init server
		Login.initTest(false);

		// Set context
		Env.setContext(Env.getCtx(), "#AD_Client_ID", 1000000);
		Env.setContext(Env.getCtx(), "#AD_Org_ID", 1000001);
		
		// Create trx
		String trxName = Trx.createTrxName();
		
		// Create test discount schema and break
		MDiscountSchema discountSchema = new MDiscountSchema(Env.getCtx(), 0, trxName);
		discountSchema.setName("TEST_DISCOUNT_SCHEMA");
		discountSchema.setDiscountType(MDiscountSchema.DISCOUNTTYPE_Breaks);
		discountSchema.setCumulativeLevel(MDiscountSchema.CUMULATIVELEVEL_Line);
		discountSchema.save();
		
		MDiscountSchemaBreak discountSchemaBreak = new MDiscountSchemaBreak(Env.getCtx(), 0, trxName);
		discountSchemaBreak.setSeqNo(10);
		discountSchemaBreak.setM_DiscountSchema_ID(discountSchema.getM_DiscountSchema_ID());
		discountSchemaBreak.setM_Product_Category_ID(0);
		discountSchemaBreak.setM_Product_ID(1000000);
		discountSchemaBreak.setBreakValue(new BigDecimal(3.0));
		discountSchemaBreak.setBreakDiscount(new BigDecimal(10.0));
		discountSchemaBreak.save();
		
		// Create test invoice(s)
		MInvoice invoice = new MInvoice(Env.getCtx(), 0, trxName);
		invoice.setC_DocTypeTarget_ID(MDocType.DOCBASETYPE_ARInvoice);
		invoice.setC_BPartner_ID(1000000);
		invoice.setC_BPartner_Location_ID(1000000);
		invoice.save();
		
		MInvoiceLine line1 = new MInvoiceLine(Env.getCtx(), 0, trxName);
		line1.setC_Invoice_ID(invoice.getC_Invoice_ID());
		line1.setM_Product_ID(1000000);
		line1.setPrice(Env.ONEHUNDRED);
		line1.setQty(1);
		line1.save();
		
		MInvoiceLine line2 = new MInvoiceLine(Env.getCtx(), 0, trxName);
		line2.setC_Invoice_ID(invoice.getC_Invoice_ID());
		line2.setM_Product_ID(1000000); 
		line2.setPrice(Env.ONEHUNDRED); 
		line2.setQty(1);
		line2.save();
		
		MInvoiceLine line3 = new MInvoiceLine(Env.getCtx(), 0, trxName);
		line3.setC_Invoice_ID(invoice.getC_Invoice_ID());
		line3.setM_Product_ID(1000000); 
		line3.setPrice(Env.ONEHUNDRED); 
		line3.setQty(1);
		line3.save();
		
		InvoiceDiscount invoiceDiscount = new InvoiceDiscount();
		invoiceDiscount.M_DiscountSchema_ID = discountSchema.getM_DiscountSchema_ID();
		invoiceDiscount.C_Invoice_ID = invoice.getC_Invoice_ID();
		
		ProcessInfo pi = new ProcessInfo("InvoiceDiscount", 1);
		invoiceDiscount.startProcess(Env.getCtx(), pi, Trx.get(trxName, false));

		invoice = new MInvoice(Env.getCtx(), invoice.get_ID(), trxName);
		System.out.println("MInvoice[" + invoice.get_ID() + "-" + invoice.getDocumentInfo() + "-" + invoice.getGrandTotal() + "]");
		for (MInvoiceLine line : invoice.getLines())
		{
			System.out.println("Line: " + line.getLine() + ", Product: " + line.getM_Product_ID() + ", Qty: " + line.getQtyInvoiced() + ", Amt: " + line.getLineNetAmt());
		}
		
		Trx.get(trxName, false).rollback();
		
		System.exit(0);
	}
	
	public String applyEscDiscount()
	{
		// Load discount schema
		// Keep count of completed and failed documents
		int countError = 0;
		String errorMsg=null;
		for(MInvoice invoice:getInvoiceList())
		{  		
				if(M_DiscountSchema_ID == 0)
				{
					MBPartner bp=new MBPartner(getCtx(),invoice.getC_BPartner_ID(),get_TrxName());
					if(bp==null || bp.get_ID()==0)
						continue;
					else
					{
						p_M_DiscountSchema_ID=bp.getM_DiscountSchema_ID();
						if(p_M_DiscountSchema_ID == 0)
						{
							String msg = "Discount Schema not set for Business Partner " +bp.getName();
							addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
							continue;
						}
						else
						{
							errorMsg=validateDiscountSchema(p_M_DiscountSchema_ID);
							if(errorMsg!=null)
							{
								addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, errorMsg);
								continue;
							}
						}
					}
				}
				else
					p_M_DiscountSchema_ID=M_DiscountSchema_ID;
			
			getDiscountSchemaDetails(p_M_DiscountSchema_ID);
			MDiscountSchema discountSchema = new MDiscountSchema(getCtx(), p_M_DiscountSchema_ID, get_TrxName());
			
			for(Integer prods:discountProductsList)
			{
				if(!discountSchema.isApplyMaxDiscount())
				{
				BigDecimal discountAmtCharge=Env.ZERO;
				String chrgsql="SELECT C_CHARGE_ID FROM M_DISCOUNTSCHEMABREAK WHERE M_DISCOUNTSCHEMA_ID="+p_M_DiscountSchema_ID+" AND M_PRODUCT_ID="+prods.intValue()+" AND ROWNUM=1";
				int chargeId=DB.getSQLValue(null, chrgsql, new Object[]{});
						
				if(chargeId>0)
				{
					String sql="SELECT MIN(BreakValue) FROM M_DISCOUNTSCHEMABREAK WHERE M_DISCOUNTSCHEMA_ID="+p_M_DiscountSchema_ID+" AND M_PRODUCT_ID="+prods.intValue()+" AND C_CHARGE_ID = "+chargeId;
					int minimumDiscountBreak=DB.getSQLValueBD(null, sql, new Object[]{}).intValue();
					
					sql="SELECT BreakDiscount FROM M_DISCOUNTSCHEMABREAK WHERE M_DISCOUNTSCHEMA_ID="+p_M_DiscountSchema_ID+" AND M_PRODUCT_ID="+prods.intValue() +" AND BREAKVALUE ="+minimumDiscountBreak+" AND C_CHARGE_ID = "+chargeId;
					BigDecimal DiscountBreak=DB.getSQLValueBD(null, sql, new Object[]{});
					
					int discountQty=0;
					int nextDiscountBreak=getNextDiscountBreak(prods.intValue(), minimumDiscountBreak,chargeId,0,p_M_DiscountSchema_ID);
					BigDecimal nextDiscount=getNextDiscount(prods.intValue(), nextDiscountBreak,chargeId,0,p_M_DiscountSchema_ID);
					for(MInvoiceLine line:getInvoiceLines(invoice.getC_Invoice_ID(),prods.intValue()))
					{
						discountQty+=line.getQtyInvoiced().intValue();
						if(discountQty>minimumDiscountBreak)
						{
							if(discountQty>nextDiscountBreak && nextDiscountBreak >0)
							{
								minimumDiscountBreak=nextDiscountBreak;
								DiscountBreak=nextDiscount;
								nextDiscountBreak=getNextDiscountBreak(prods.intValue(), minimumDiscountBreak,chargeId,0,p_M_DiscountSchema_ID);
								nextDiscount=getNextDiscount(prods.intValue(), nextDiscountBreak,chargeId,0,p_M_DiscountSchema_ID);
							}
							discountAmtCharge=discountAmtCharge.add(line.getLineNetAmt().divide(Env.ONEHUNDRED).multiply(DiscountBreak));
						}
					}
					//add a discount line to the invoice if charge is not null
					addDiscountLine(invoice,discountAmtCharge,chargeId);
				}
				else
				{
					String sql="SELECT MIN(BreakValue) FROM M_DISCOUNTSCHEMABREAK WHERE M_DISCOUNTSCHEMA_ID="+p_M_DiscountSchema_ID+" AND M_PRODUCT_ID="+prods.intValue();
					int minimumDiscountBreak=DB.getSQLValueBD(null, sql, new Object[]{}).intValue();
					
					sql="SELECT BreakDiscount FROM M_DISCOUNTSCHEMABREAK WHERE M_DISCOUNTSCHEMA_ID="+p_M_DiscountSchema_ID+" AND M_PRODUCT_ID="+prods.intValue() +" AND BREAKVALUE ="+minimumDiscountBreak;
					BigDecimal DiscountBreak=DB.getSQLValueBD(null, sql, new Object[]{});
					
					int discountQty=0;
					int nextDiscountBreak=getNextDiscountBreak(prods.intValue(), minimumDiscountBreak,0,0,p_M_DiscountSchema_ID);
					BigDecimal nextDiscount=getNextDiscount(prods.intValue(), nextDiscountBreak,0,0,p_M_DiscountSchema_ID);
					BigDecimal discountAmt=Env.ZERO;
					BigDecimal discountPrice=Env.ZERO;
					for(MInvoiceLine line:getInvoiceLines(invoice.getC_Invoice_ID(),prods.intValue()))
					{
						discountQty+=line.getQtyInvoiced().intValue();
						if(discountQty>minimumDiscountBreak)
						{
							
							if(discountQty>nextDiscountBreak && nextDiscountBreak >0)
							{
								minimumDiscountBreak=nextDiscountBreak;
								DiscountBreak=nextDiscount;
								nextDiscountBreak=getNextDiscountBreak(prods.intValue(), minimumDiscountBreak,0,0,p_M_DiscountSchema_ID);
								nextDiscount=getNextDiscount(prods.intValue(), nextDiscountBreak,0,0,p_M_DiscountSchema_ID);
							}	
							if(!listOnly)
							{
								discountAmt = (line.getLineNetAmt()).divide(Env.ONEHUNDRED).multiply(DiscountBreak);
								discountPrice=line.getPriceEntered().divide(Env.ONEHUNDRED).multiply(DiscountBreak);
								System.out.println(discountQty+" has passed the minimum break level");
								line.setPriceActual(line.getPriceEntered().subtract(discountPrice));
								line.setLineNetAmt(line.getLineNetAmt().subtract(discountAmt));
								if(!line.save())
								{
									countError++;
									log.warning("Unable to update invoice lines with the discount information for Line :"+line.getLine()+" of invoice no :"+ invoice.getDocumentNo());
								}
							
							}
							else
							{
								String msg = "A discount of " + DiscountBreak + " % would have been applied to Line No : "+ line.getLine() + " of Invoice : "+invoice.getDocumentNo();
								addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
							}
						}
					}
			}//charge id if statement
				}
				else
				{
					BigDecimal discountAmtCharge=Env.ZERO;
					String chrgsql="SELECT C_CHARGE_ID FROM M_DISCOUNTSCHEMABREAK WHERE M_DISCOUNTSCHEMA_ID="+p_M_DiscountSchema_ID+" AND M_PRODUCT_ID="+prods.intValue()+" AND ROWNUM=1";
					int chargeId=DB.getSQLValue(null, chrgsql, new Object[]{});
					BigDecimal TotalQtyInvoiced=getTotalQtyInvoiced(prods.intValue(),invoice.getC_Invoice_ID());
					BigDecimal discountPercent=Env.ZERO;
					if(TotalQtyInvoiced.compareTo(Env.ZERO)>0)
					{
						for(MDiscountSchemaBreak breaks:getBreaks(prods.intValue(), 0,p_M_DiscountSchema_ID))
						{
							if(TotalQtyInvoiced.compareTo(breaks.getBreakValue())>0)
							{
								discountPercent=breaks.getBreakDiscount();
							}
						}
						if(chargeId>0 && discountPercent.compareTo(Env.ZERO)>0)
						{
							for(MInvoiceLine line:getInvoiceLines(invoice.getC_Invoice_ID(),prods.intValue()))
							{
								discountAmtCharge=discountAmtCharge.add(line.getLineNetAmt().divide(Env.ONEHUNDRED).multiply(discountPercent));
							}
							addDiscountLine(invoice,discountAmtCharge,chargeId);
						}
						else
						{
							BigDecimal discountAmt=Env.ZERO;
							BigDecimal discountPrice=Env.ZERO;
							for(MInvoiceLine line:getInvoiceLines(invoice.getC_Invoice_ID(),prods.intValue()))
							{
								if(!listOnly)
								{
									discountAmt = (line.getLineNetAmt()).divide(Env.ONEHUNDRED).multiply(discountPercent);
									discountPrice=line.getPriceEntered().divide(Env.ONEHUNDRED).multiply(discountPercent);
									line.setPriceActual(line.getPriceEntered().subtract(discountPrice));
									line.setLineNetAmt(line.getLineNetAmt().subtract(discountAmt));
									if(!line.save())
									{
										countError++;
										log.warning("Unable to update invoice lines with the discount information for Line :"+line.getLine()+" of invoice no :"+ invoice.getDocumentNo());
									}
								}
								else
								{
									String msg = "A discount of " + discountPercent + " % would have been applied to Line No : "+ line.getLine() + " of Invoice : "+invoice.getDocumentNo();
									addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
								}
							}
						}//else
				}
				}
			}
			
			for(Integer prodCategory:discountProdCategoryList)
			{
				if(!discountSchema.isApplyMaxDiscount())
				{
				BigDecimal discountAmtCharge=Env.ZERO;
				String chrgsql="SELECT C_CHARGE_ID FROM M_DISCOUNTSCHEMABREAK WHERE M_DISCOUNTSCHEMA_ID="+p_M_DiscountSchema_ID+" AND M_PRODUCT_ID IS NULL AND M_PRODUCT_CATEGORY_ID="+prodCategory.intValue()+" AND ROWNUM=1";
				int chargeId=DB.getSQLValue(null, chrgsql, new Object[]{});
						
				if(chargeId>0)
				{
					String sql="SELECT MIN(BreakValue) FROM M_DISCOUNTSCHEMABREAK WHERE M_DISCOUNTSCHEMA_ID="+p_M_DiscountSchema_ID+" AND M_PRODUCT_ID IS NULL AND M_PRODUCT_CATEGORY_ID="+prodCategory.intValue()+" AND C_CHARGE_ID = "+chargeId;
					int minimumDiscountBreak=DB.getSQLValueBD(null, sql, new Object[]{}).intValue();
					
					sql="SELECT BreakDiscount FROM M_DISCOUNTSCHEMABREAK WHERE M_DISCOUNTSCHEMA_ID="+p_M_DiscountSchema_ID+" AND M_PRODUCT_ID IS NULL AND M_PRODUCT_CATEGORY_ID="+prodCategory.intValue() +" AND BREAKVALUE ="+minimumDiscountBreak+" AND C_CHARGE_ID = "+chargeId;
					BigDecimal DiscountBreak=DB.getSQLValueBD(null, sql, new Object[]{});
					
					int discountQty=0;
					int nextDiscountBreak=getNextDiscountBreak(0, minimumDiscountBreak,chargeId,prodCategory.intValue(),p_M_DiscountSchema_ID);
					BigDecimal nextDiscount=getNextDiscount(0, nextDiscountBreak,chargeId,prodCategory.intValue(),p_M_DiscountSchema_ID);
					for(MInvoiceLine line:getInvoiceLines(invoice.getC_Invoice_ID(), discountProductsList, prodCategory.intValue()))
					{
						discountQty+=line.getQtyInvoiced().intValue();
						if(discountQty>minimumDiscountBreak)
						{
							if(discountQty>nextDiscountBreak && nextDiscountBreak >0)
							{
								minimumDiscountBreak=nextDiscountBreak;
								DiscountBreak=nextDiscount;
								nextDiscountBreak=getNextDiscountBreak(0, minimumDiscountBreak,chargeId,prodCategory.intValue(),p_M_DiscountSchema_ID);
								nextDiscount=getNextDiscount(0, nextDiscountBreak,chargeId,prodCategory.intValue(),p_M_DiscountSchema_ID);
							}
							discountAmtCharge=discountAmtCharge.add(line.getLineNetAmt().divide(Env.ONEHUNDRED).multiply(DiscountBreak));
						}
					}
					//add a discount line to the invoice if charge is not null
					addDiscountLine(invoice,discountAmtCharge,chargeId);
				}
				else
				{

					String sql="SELECT MIN(BreakValue) FROM M_DISCOUNTSCHEMABREAK WHERE M_DISCOUNTSCHEMA_ID="+p_M_DiscountSchema_ID+" AND M_PRODUCT_ID IS NULL AND M_PRODUCT_CATEGORY_ID="+prodCategory.intValue();
					int minimumDiscountBreak=DB.getSQLValueBD(null, sql, new Object[]{}).intValue();
					
					sql="SELECT BreakDiscount FROM M_DISCOUNTSCHEMABREAK WHERE M_DISCOUNTSCHEMA_ID="+p_M_DiscountSchema_ID+" AND M_PRODUCT_ID IS NULL AND M_PRODUCT_CATEGORY_ID="+prodCategory.intValue() +" AND BREAKVALUE ="+minimumDiscountBreak;
					BigDecimal DiscountBreak=DB.getSQLValueBD(null, sql, new Object[]{});
					
					int discountQty=0;
					int nextDiscountBreak=getNextDiscountBreak(0, minimumDiscountBreak,0,prodCategory.intValue(),p_M_DiscountSchema_ID);
					BigDecimal nextDiscount=getNextDiscount(0, nextDiscountBreak,0,prodCategory.intValue(),p_M_DiscountSchema_ID);
					BigDecimal discountAmt=Env.ZERO;
					BigDecimal discountPrice=Env.ZERO;
					for(MInvoiceLine line:getInvoiceLines(invoice.getC_Invoice_ID(),discountProductsList, prodCategory.intValue()))
					{
						discountQty+=line.getQtyInvoiced().intValue();
						if(discountQty>minimumDiscountBreak)
						{
							
							if(discountQty>nextDiscountBreak && nextDiscountBreak >0)
							{
								minimumDiscountBreak=nextDiscountBreak;
								DiscountBreak=nextDiscount;
								nextDiscountBreak=getNextDiscountBreak(0, minimumDiscountBreak,0,prodCategory.intValue(),p_M_DiscountSchema_ID);
								nextDiscount=getNextDiscount(0, nextDiscountBreak,0,prodCategory.intValue(),p_M_DiscountSchema_ID);
							}	
							if(!listOnly)
							{
								discountAmt = (line.getLineNetAmt()).divide(Env.ONEHUNDRED).multiply(DiscountBreak);
								discountPrice=line.getPriceEntered().divide(Env.ONEHUNDRED).multiply(DiscountBreak);
								System.out.println(discountQty+" has passed the minimum break level");
								line.setPriceActual(line.getPriceEntered().subtract(discountPrice));
								line.setLineNetAmt(line.getLineNetAmt().subtract(discountAmt));
								if(!line.save())
								{
									countError++;
									log.warning("Unable to update invoice lines with the discount information for Line :"+line.getLine()+" of invoice no :"+ invoice.getDocumentNo());
								}
							
							}
							else
							{
								String msg = "A discount of " + DiscountBreak + " % would have been applied to Line No : "+ line.getLine() + " of Invoice : "+invoice.getDocumentNo();
								addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
							}
						}
					}
			
				}
			}else
			{
				BigDecimal discountAmtCharge=Env.ZERO;
				String chrgsql="SELECT C_CHARGE_ID FROM M_DISCOUNTSCHEMABREAK WHERE M_DISCOUNTSCHEMA_ID="+p_M_DiscountSchema_ID+" AND M_PRODUCT_ID IS NULL AND M_PRODUCT_CATEGORY_ID="+prodCategory.intValue()+" AND ROWNUM=1";
				int chargeId=DB.getSQLValue(null, chrgsql, new Object[]{});	
				BigDecimal TotalQtyInvoiced=getTotalQtyInvoiced(invoice.getC_Invoice_ID(),discountProductsList,prodCategory.intValue());
				BigDecimal discountPercent=Env.ZERO;
				if(TotalQtyInvoiced.compareTo(Env.ZERO)>0)
				{
					for(MDiscountSchemaBreak breaks:getBreaks( 0,prodCategory.intValue(),p_M_DiscountSchema_ID))
					{
						if(TotalQtyInvoiced.compareTo(breaks.getBreakValue())>0)
						{
							discountPercent=breaks.getBreakDiscount();
						}
					}
					if(chargeId>0 && discountPercent.compareTo(Env.ZERO)>0)
					{
						for(MInvoiceLine line:getInvoiceLines(invoice.getC_Invoice_ID(), discountProductsList, prodCategory.intValue()))
						{
							discountAmtCharge=discountAmtCharge.add(line.getLineNetAmt().divide(Env.ONEHUNDRED).multiply(discountPercent));
						}
						addDiscountLine(invoice,discountAmtCharge,chargeId);
					}
					else
					{
						BigDecimal discountAmt=Env.ZERO;
						BigDecimal discountPrice=Env.ZERO;
						for(MInvoiceLine line:getInvoiceLines(invoice.getC_Invoice_ID(),discountProductsList, prodCategory.intValue()))
						{
							if(!listOnly)
							{
								discountAmt = (line.getLineNetAmt()).divide(Env.ONEHUNDRED).multiply(discountPercent);
								discountPrice=line.getPriceEntered().divide(Env.ONEHUNDRED).multiply(discountPercent);
								line.setPriceActual(line.getPriceEntered().subtract(discountPrice));
								line.setLineNetAmt(line.getLineNetAmt().subtract(discountAmt));
								if(!line.save())
								{
									countError++;
									log.warning("Unable to update invoice lines with the discount information for Line :"+line.getLine()+" of invoice no :"+ invoice.getDocumentNo());
								}
							}
							else
							{
								String msg = "A discount of " + discountPercent + " % would have been applied to Line No : "+ line.getLine() + " of Invoice : "+invoice.getDocumentNo();
								addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
							}
						}
					}//else
			}
			}
			}
			errorMsg=null;
			discountProductsList.clear();
			discountProdCategoryList.clear();
		}
		if (countError>0)
		    return "@Errors@ = " + countError;
		return "Discount applied successfully";
	}

	private void getDiscountBreakProductList(int M_DiscountSchema_ID)
	{
		log.info("Retrieving all the products in the discount schema");
    	String sql="SELECT DISTINCT(M_PRODUCT_ID) FROM M_DISCOUNTSCHEMABREAK WHERE M_DISCOUNTSCHEMA_ID=? AND M_PRODUCT_ID IS NOT NULL";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{	
			// Create statement and set parameters
			pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			pstmt.setInt(1, M_DiscountSchema_ID);
			// Execute query and process result set
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				discountProductsList.add(rs.getInt(1));
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
	}
	
	private List<MInvoiceLine> getInvoiceLines(int c_invoice_id,int m_product_id)
	{
		ArrayList<MInvoiceLine> list = new ArrayList<MInvoiceLine>();
		String sql="SELECT * FROM C_INVOICELINE WHERE C_Invoice_ID=? AND M_Product_ID=? ORDER BY Description ";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, get_TrxName());
			pstmt.setInt(1,c_invoice_id);
			pstmt.setInt(2,m_product_id);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				MInvoiceLine il = new MInvoiceLine(getCtx(), rs, get_TrxName());
				list.add(il);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "getLines", e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{log.log(Level.SEVERE, sql.toString(), e);}
			pstmt = null;
		}
			return list;
	}
	
	private int getNextDiscountBreak(int product_id,int prevDiscountBreak,int charge_id,int prod_category_id,int M_DiscountSchema_ID)
	{
		String sql="SELECT MIN(BreakValue) FROM M_DISCOUNTSCHEMABREAK WHERE M_DISCOUNTSCHEMA_ID="+M_DiscountSchema_ID+" AND BREAKVALUE > "+prevDiscountBreak;
		if(product_id>0)
			sql+=" AND M_PRODUCT_ID="+product_id;
		else
			sql+="AND M_PRODUCT_ID IS NULL AND M_PRODUCT_CATEGORY_ID="+prod_category_id;
		if(charge_id>0)
			sql+=" AND C_CHARGE_ID ="+charge_id;
		int DiscountBreak=DB.getSQLValue(null, sql, new Object[]{});
		return DiscountBreak;
	}
	
	private BigDecimal getNextDiscount(int product_id,int minimumDiscountBreak,int charge_id,int prod_category_id,int M_DiscountSchema_ID)
	{
		String sql="SELECT BreakDiscount FROM M_DISCOUNTSCHEMABREAK WHERE M_DISCOUNTSCHEMA_ID="+M_DiscountSchema_ID+" AND BREAKVALUE ="+minimumDiscountBreak;
		if(product_id>0)
			sql+=" AND M_PRODUCT_ID="+product_id;
		else
			sql+="AND M_PRODUCT_ID IS NULL AND M_PRODUCT_CATEGORY_ID="+prod_category_id;
		if(charge_id>0)
			sql+=" AND C_CHARGE_ID ="+charge_id;
		BigDecimal DiscountBreak=DB.getSQLValueBD(null, sql, new Object[]{});
		return DiscountBreak;
	}
	
	/**
	 * 	Get Breaks
	 *	@param reload reload
	 *	@return breaks
	 */
	public MDiscountSchemaBreak[] getBreaks(int m_Product_ID,int m_Prod_Category_ID,int M_DiscountSchema_ID)
	{	
		String sql = "SELECT * FROM M_DiscountSchemaBreak WHERE M_DiscountSchema_ID=? AND IsActive = 'Y' AND " ;
		if(m_Product_ID>0)
		    sql+="M_Product_ID=? ORDER BY BreakValue";
		if(m_Prod_Category_ID>0)
			sql+="M_Product_Category_ID= ? AND M_PRODUCT_ID IS NULL ORDER BY BreakValue";
		ArrayList<MDiscountSchemaBreak> list = new ArrayList<MDiscountSchemaBreak>();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, get_TrxName());
			pstmt.setInt (1, M_DiscountSchema_ID);
			if(m_Product_ID>0)
				pstmt.setInt(2, m_Product_ID);
			if(m_Prod_Category_ID>0)
				pstmt.setInt(2,m_Prod_Category_ID );
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add(new MDiscountSchemaBreak(getCtx(), rs, get_TrxName()));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		m_breaks = new MDiscountSchemaBreak[list.size ()];
		list.toArray (m_breaks);
		return m_breaks;
	}//	getBreaks
	
	private void getDiscountBreakProductCategoryList(int M_DiscountSchema_ID)
	{
		log.info("Retrieving all the product categories in the discount schema");
    	String sql="SELECT DISTINCT(M_Product_Category_ID) FROM M_DISCOUNTSCHEMABREAK WHERE M_DISCOUNTSCHEMA_ID=? AND M_PRODUCT_ID IS NULL";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{	
			// Create statement and set parameters
			pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			pstmt.setInt(1, M_DiscountSchema_ID);
			// Execute query and process result set
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				discountProdCategoryList.add(rs.getInt(1));
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
	}
	
	private List<MInvoiceLine> getInvoiceLines(int c_invoice_id,List<Integer> productList,int productCategory_id)
	{
		String res="";
		for (Iterator<Integer> iterator = productList.iterator(); iterator.hasNext();) {
            res += iterator.next() + (iterator.hasNext() ? "," : "");
        }
		
		ArrayList<MInvoiceLine> list = new ArrayList<MInvoiceLine>();
		String sql="SELECT INVLINE.* FROM C_INVOICELINE INVLINE " +
				   "INNER JOIN M_PRODUCT PROD ON (PROD.M_PRODUCT_ID=INVLINE.M_PRODUCT_ID) " +
				   "INNER JOIN M_PRODUCT_CATEGORY PRODCAT ON (PRODCAT.M_PRODUCT_CATEGORY_ID=PROD.M_PRODUCT_CATEGORY_ID) " +
				   "WHERE INVLINE.C_Invoice_ID=? " ;
				   if(!res.equals(""))
					   sql+=" AND INVLINE.M_Product_ID NOT IN (" +res+")" ;
				   sql+=" AND PROD.M_PRODUCT_CATEGORY_ID=?" +
				        " ORDER BY INVLINE.Description";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, get_TrxName());
			pstmt.setInt(1,c_invoice_id);
			pstmt.setInt(2,productCategory_id);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				MInvoiceLine il = new MInvoiceLine(getCtx(), rs, get_TrxName());
				list.add(il);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "getLines", e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{log.log(Level.SEVERE, sql.toString(), e);}
			pstmt = null;
		}
			return list;
	}
	
	
	private BigDecimal getTotalQtyInvoiced(int m_Product_ID,int invoice_id)
	{
		String sql="SELECT SUM(QTYINVOICED) FROM C_INVOICELINE WHERE M_PRODUCT_ID= ? AND C_INVOICE_ID=? GROUP BY QTYINVOICED";
		BigDecimal QtyInvoiced=DB.getSQLValueBD(null, sql, m_Product_ID , invoice_id);
		if(QtyInvoiced ==null)
			return Env.ZERO;
		return QtyInvoiced;
	}

	private BigDecimal getTotalQtyInvoiced(int c_invoice_id,List<Integer> productList,int productCategory_id)
	{
		String res="";
		for (Iterator<Integer> iterator = productList.iterator(); iterator.hasNext();) {
            res += iterator.next() + (iterator.hasNext() ? "," : "");
        }
		
		String sql="SELECT SUM(QTYINVOICED) FROM C_INVOICELINE INVLINE "+
		           "INNER JOIN M_PRODUCT PROD ON (PROD.M_PRODUCT_ID=INVLINE.M_PRODUCT_ID) " +
		           "INNER JOIN M_PRODUCT_CATEGORY PRODCAT ON (PRODCAT.M_PRODUCT_CATEGORY_ID=PROD.M_PRODUCT_CATEGORY_ID) " +
		           "WHERE INVLINE.C_Invoice_ID= "+ c_invoice_id;
		   if(!res.equals(""))
			   sql+=" AND INVLINE.M_Product_ID NOT IN (" +res+")" ;
		       sql+=" AND PROD.M_PRODUCT_CATEGORY_ID="+productCategory_id;
		BigDecimal QtyInvoiced=DB.getSQLValueBD(null, sql, new Object[]{});
		if(QtyInvoiced ==null)
			return Env.ZERO;
		return QtyInvoiced;
	}
	
	private List<MInvoice> getInvoiceList()
	{
		// Load invoice(s)
		ArrayList<MInvoice> invoices = new ArrayList<MInvoice>();		
		if (C_Invoice_ID > 0)
		{
			invoices.add(new MInvoice(getCtx(), C_Invoice_ID, get_TrxName()));
		}
		else
		{	
			String sql = "SELECT * FROM " + MInvoice.Table_Name + " INV "; 
			if(p_C_BP_Group_ID > 0)
				sql+=" INNER JOIN C_BPARTNER BP ON (BP.C_BPARTNER_ID=INV.C_BPARTNER_ID) ";
			sql+=" WHERE INV.AD_Client_ID=?" + // 1
			   " AND INV.AD_Org_ID = "+AD_Org_ID + " AND INV.Processing='N'" + 
			   " AND INV.Posted='N'" + 
			   " AND INV.IsActive='Y'" + 
			   " AND INV.DocStatus='DR'";
			if(p_C_BPartner_ID > 0)
				sql+=" AND INV.C_BPartner_ID = "+p_C_BPartner_ID;
			if(p_C_BP_Group_ID > 0)
				sql+=" AND BP.C_BP_Group_ID = "+p_C_BP_Group_ID;
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
	
	/*private void addDiscountLine(MInvoice invoice,BigDecimal discountAmtCharge,int chargeId,String description)
	{
		if(discountAmtCharge.compareTo(Env.ZERO)>0)
		{
			if (!listOnly)
			{
				MInvoiceLine discountLine = new MInvoiceLine(getCtx(), 0, get_TrxName());						
				discountLine.setC_Invoice_ID(invoice.getC_Invoice_ID());
				discountLine.setC_Charge_ID(chargeId); 		
				discountLine.setPrice(discountAmtCharge.negate());
				discountLine.setQty(1);
				discountLine.setDescription(description);
			
				String msg="A discount line for "+discountAmtCharge;
				if (discountLine.save())
				{
					msg += " has been ";
				}
				else
				{
					msg += " failed to be ";
				}
				
				// Log message regardless of outcome
				msg += " added to " + invoice.getDocumentNo();
				addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
			}
			else
			{
				String msg="A discount line for " +discountAmtCharge+ " would have been added to "+invoice.getDocumentNo();
				addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
			}
		}
	}*/
	
	private void addDiscountLine(MInvoice invoice,BigDecimal discountAmtCharge,int chargeId)
	{
		if(discountAmtCharge.compareTo(Env.ZERO)>0)
		{
			if (!listOnly)
			{
				MInvoiceLine discountLine = new MInvoiceLine(getCtx(), 0, get_TrxName());						
				discountLine.setC_Invoice_ID(invoice.getC_Invoice_ID());
				discountLine.setC_Charge_ID(chargeId); 		
				discountLine.setPrice(discountAmtCharge.negate());
				discountLine.setQty(1);
			
				String msg="A discount line for "+discountAmtCharge;
				if (discountLine.save())
				{
					msg += " has been ";
				}
				else
				{
					msg += " failed to be ";
				}
				
				// Log message regardless of outcome
				msg += " added to " + invoice.getDocumentNo();
				addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
			}
			else
			{
				String msg="A discount line for " +discountAmtCharge+ " would have been added to "+invoice.getDocumentNo();
				addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
			}
		}
	}
	
	private String validateDiscountSchema(int discountSchema_ID)
	{
		StringBuilder sb = new StringBuilder();
		// Check discount schema is set and has at least one break
		if (discountSchema_ID > 0)
		{
			MDiscountSchema discountSchema = new MDiscountSchema(getCtx(), discountSchema_ID, get_TrxName());
			applyMaxDiscount=discountSchema.isApplyMaxDiscount();
			if (discountSchema == null || discountSchema.get_ID() == 0)
			{
				if (sb.length() > 0)
					sb.append(", ");
				
				sb.append("Cannot load MDiscountSchema[" + discountSchema_ID + "]");
			}
			else if (!discountSchema.getCumulativeLevel().equals("I"))
			{
				if (sb.length() > 0)
					sb.append(", ");
				
				sb.append("Accumulation Level must be 'Invoice' for MDiscountSchema[" + discountSchema_ID + "]");
			}
			else
			{
				MDiscountSchemaBreak[] discountSchemaBreaks = discountSchema.getBreaks(true);
				// TODO: Only allow one break per schema for now (need to decide how to handle compound discounts first)
				if (discountSchemaBreaks.length > 1)
				{
					for (MDiscountSchemaBreak discountSchemaBreak : discountSchemaBreaks)
					{
						// Check that either product or product category are set
						MProduct product = new MProduct(getCtx(), discountSchemaBreak.getM_Product_ID(), get_TrxName());
						if (product == null || product.get_ID() == 0)
						{
							MProductCategory productCategory = new MProductCategory(getCtx(), discountSchemaBreak.getM_Product_Category_ID(), get_TrxName());
							if (productCategory == null || productCategory.get_ID() == 0)
							{
								if (sb.length() > 0)
									sb.append(", ");
								
								sb.append("MDiscountSchema[" + discountSchema.get_ID() + "-" + discountSchema.getName() + "]'s break with Sequence No " + discountSchemaBreak.getSeqNo() + " has neither product or product category set");
							}
						}
						
						// Check discount rate is set and greater than 0
						if (discountSchemaBreak.getBreakDiscount() == null || discountSchemaBreak.getBreakDiscount().compareTo(Env.ZERO) <= 0)
						{
							if (sb.length() > 0)
								sb.append(", ");
							
							sb.append("MDiscountSchema[" + discountSchema.get_ID() + "-" + discountSchema.getName() + "]'s break with Sequence No " + discountSchemaBreak.getSeqNo() + " has an invalid discount value (must be greater than 0%)");
						}
					}
					//sb.append("MDiscountSchema[" + discountSchema.get_ID() + "-" + discountSchema.getName() + "] has more than ONE break");
				}
				else if (discountSchemaBreaks.length > 0)
				{
					for (MDiscountSchemaBreak discountSchemaBreak : discountSchemaBreaks)
					{
						// Check that either product or product category are set
						MProduct product = new MProduct(getCtx(), discountSchemaBreak.getM_Product_ID(), get_TrxName());
						if (product == null || product.get_ID() == 0)
						{
							MProductCategory productCategory = new MProductCategory(getCtx(), discountSchemaBreak.getM_Product_Category_ID(), get_TrxName());
							if (productCategory == null || productCategory.get_ID() == 0)
							{
								if (sb.length() > 0)
									sb.append(", ");
								
								sb.append("MDiscountSchema[" + discountSchema.get_ID() + "-" + discountSchema.getName() + "]'s break with Sequence No " + discountSchemaBreak.getSeqNo() + " has neither product or product category set");
							}
						}
						
						// Check discount rate is set and greater than 0
						if (discountSchemaBreak.getBreakDiscount() == null || discountSchemaBreak.getBreakDiscount().compareTo(Env.ZERO) <= 0)
						{
							if (sb.length() > 0)
								sb.append(", ");
							
							sb.append("MDiscountSchema[" + discountSchema.get_ID() + "-" + discountSchema.getName() + "]'s break with Sequence No " + discountSchemaBreak.getSeqNo() + " has an invalid discount value (must be greater than 0%)");
						}
					}
				}
				else
				{
					if (sb.length() > 0)
						sb.append(", ");
				
					sb.append("Cannot find any DiscountSchemaBreak's for MDiscountSchema[" + discountSchema.get_ID() + "-" + discountSchema.getName() + "]");
				}
			}
		}

		if (sb.length() > 0)
			return "@Error@" + sb.toString();
		
		return null;
	}
	
	private void getDiscountSchemaDetails(int p_M_DiscountSchema_ID)
	{
		getDiscountBreakProductList(p_M_DiscountSchema_ID);
		getDiscountBreakProductCategoryList(p_M_DiscountSchema_ID);
		
	}
	
	public String applyEscDiscountV2()
	{
		// Load discount schema
		// Keep count of completed and failed documents
		int countError = 0;
		String errorMsg=null;
		for(MInvoice invoice:getInvoiceList())
		{  		
				
			MBPartner bp=new MBPartner(getCtx(),invoice.getC_BPartner_ID(),get_TrxName());
			if(bp==null || bp.get_ID()==0)
				continue;
			else
			{
				p_M_DiscountSchema_ID=bp.getM_DiscountSchema_ID();
				if(p_M_DiscountSchema_ID == 0)
				{
					String msg = "Discount Schema not set for Business Partner " +bp.getName();
					addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
					continue;
				}
				else
				{
					errorMsg=validateDiscountSchema(p_M_DiscountSchema_ID);
					if(errorMsg!=null)
					{
						addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, errorMsg);
						continue;
						}
					}
				}	
			
			getDiscountSchemaDetails(p_M_DiscountSchema_ID);
			MDiscountSchema discountSchema = new MDiscountSchema(getCtx(), p_M_DiscountSchema_ID, get_TrxName());
			
			if(removeDiscountLines)
			{
				String deletesql = "DELETE FROM C_INVOICELINE WHERE C_Invoice_ID =? AND isDiscountLine = 'Y'";
				int deleted = DB.executeUpdate(deletesql, invoice.get_ID(), null);
			}	
			
			for(Integer prods:discountProductsList)
			{
				BigDecimal discountAmtCharge=Env.ZERO;
				BigDecimal discountPercent=Env.ZERO;
				String description ="";
				int nextDiscountBreak =0;
				BigDecimal nextDiscount =Env.ZERO;
				BigDecimal discountQty = Env.ZERO;
				HashMap<Integer,BigDecimal[]> invoiceLines = getInvoiceLines(invoice.get_ID() ,prods.intValue(),true);
				Iterator<Integer> keySetIterator = invoiceLines.keySet().iterator();
				//loop through the Map and create a discount line(s) for each map entry
				while(keySetIterator.hasNext()) 
				{ 
					Integer key = keySetIterator.next(); 
					System.out.println("key: " + key + " value: " + invoiceLines.get(key));
					BigDecimal[] values = invoiceLines.get(key);
					BigDecimal TotalQtyInvoiced = values[0];
					BigDecimal priceEntered  = values[2];
					BigDecimal periodQty = values[1];
					if(TotalQtyInvoiced.compareTo(Env.ZERO)>0)
					{
						for(MDiscountSchemaBreak breaks:getBreaks(prods.intValue(), 0,p_M_DiscountSchema_ID))
						{
							if(TotalQtyInvoiced.compareTo(breaks.getBreakValue())>0)
							{
								discountPercent=breaks.getBreakDiscount();
								if (breaks.getDescription() != null && !breaks.getDescription().equals(""))
									description = breaks.getDescription();
								nextDiscountBreak=getNextDiscountBreak(prods.intValue(), breaks.getBreakValue().intValue(),0,0,p_M_DiscountSchema_ID);
								nextDiscount=getNextDiscount(prods.intValue(), nextDiscountBreak,0,0,p_M_DiscountSchema_ID);
								if(nextDiscountBreak > 0 && TotalQtyInvoiced.intValue() > nextDiscountBreak)
								{
									discountQty = new BigDecimal(nextDiscountBreak).subtract(breaks.getBreakValue());
								}
								else
								{
									discountQty = TotalQtyInvoiced.subtract(breaks.getBreakValue());
								}
								
								discountAmtCharge=discountAmtCharge.add(priceEntered.divide(Env.ONEHUNDRED).multiply(discountPercent));
								
								//addDiscountLineByBreak(invoice,discountAmtCharge,prods.intValue(),description,discountQty);
								if(discountAmtCharge.compareTo(Env.ZERO)>0)
								{
									if (!listOnly)
									{
										MInvoiceLine discountLine = new MInvoiceLine(getCtx(), 0, get_TrxName());						
										discountLine.setC_Invoice_ID(invoice.getC_Invoice_ID());
										discountLine.setM_Product_ID(prods.intValue()); 		
										discountLine.setPrice(discountAmtCharge.negate());
										discountLine.setPeriodQty(periodQty);
										discountLine.setQty(discountQty);
										discountLine.setDescription(description);
										discountLine.setIsDiscountLine(true);
									
										String msg="A discount line for "+discountAmtCharge;
										if (discountLine.save())
										{
											msg += " has been ";
										}
										else
										{
											msg += " failed to be ";
										}
										
										// Log message regardless of outcome
										msg += " added to " + invoice.getDocumentNo();
										addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
									}
									else
									{
										String msg="A discount line for " +discountAmtCharge+ " would have been added to "+invoice.getDocumentNo();
										addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, msg);
									}
								}
								discountAmtCharge = Env.ZERO;
							}	
				
						}	
					}
				}	
			}
			discountProductsList.clear();
			} // loop through each invoice
			
			errorMsg=null;
			discountProductsList.clear();
		if (countError>0)
		    return "@Errors@ = " + countError;
		return "Discount applied successfully";
	}
	
	private HashMap<Integer,BigDecimal[]> getInvoiceLines(int c_invoice_id,int m_product_id , boolean flag)
	{
		ArrayList<MInvoiceLine> list = new ArrayList<MInvoiceLine>();
		HashMap<Integer,BigDecimal[]> hm=new HashMap<Integer,BigDecimal[]>();  
		
		String sql="SELECT SUM(QTYINVOICED),PERIODQTY,PRICEENTERED FROM C_INVOICELINE WHERE C_Invoice_ID=? AND M_Product_ID=?  GROUP BY PERIODQTY , QTYINVOICED , PRICEENTERED";
		PreparedStatement pstmt = null;
		int i =0;
		try
		{
			pstmt = DB.prepareStatement(sql, get_TrxName());
			pstmt.setInt(1,c_invoice_id);
			pstmt.setInt(2,m_product_id);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				/*MInvoiceLine il = new MInvoiceLine(getCtx(), rs, get_TrxName());
				list.add(il);*/
				BigDecimal qtyInvoiced = rs.getBigDecimal(1);
				BigDecimal periodQty = rs.getBigDecimal(2);
				BigDecimal priceEntered = rs.getBigDecimal(3);
				hm.put(i++, new BigDecimal[] {qtyInvoiced, periodQty, priceEntered});
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "getLines", e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{log.log(Level.SEVERE, sql.toString(), e);}
			pstmt = null;
		}
			return hm;
	}
	
}
