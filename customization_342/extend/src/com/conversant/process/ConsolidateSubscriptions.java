package com.conversant.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
/*import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;*/
import org.compiere.model.MProduct;
import org.compiere.model.MBPGroup;
import org.compiere.model.MProductCategory;
import org.compiere.model.MSubscription;
import org.compiere.model.MBPartnerLocation;

import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;



public class ConsolidateSubscriptions extends SvrProcess 
{
	/** Logger 																	*/
	private static CLogger log = CLogger.getCLogger(ConsolidateSubscriptions.class);
	
	/** Conversant Client														*/
	private int p_AD_Client_ID = 1000000; 
	
	/** Conversant Org															*/
	private int p_AD_Org_ID ;
	
	/** Business Partner subscriptions to consolidate                        */
	private int p_C_BPartner_ID =0;
	
	/**	BP Group				*/
	private int			p_C_BP_Group_ID = 0;
	
	private int p_M_Product_ID = 0;
	
	/** Product Category*/
	private int p_M_Prod_Category_ID = 0;
	
	private String SubscriptionName = null;

	@Override
	protected String doIt() throws Exception 
	{
		p_AD_Client_ID = Env.getAD_Client_ID(getCtx());
			
		p_AD_Org_ID = Env.getAD_Org_ID(getCtx());
		
		List<MSubscription> subs = null;
		
		log.info("Entered doIt() of the Consolidate Subscriptions Process");
		
		String msg = validate();
		if (msg != null)
			return msg;
		if (p_C_BPartner_ID > 0)
		{
			MBPartnerLocation[] bpLocations = MBPartnerLocation.getForBPartner(getCtx(), p_C_BPartner_ID) ;
			
				if (p_M_Product_ID > 0)
				{
					for (int i = 0; i < bpLocations.length; i++) 
					{
						subs = getSubscriptions(p_C_BPartner_ID, p_M_Product_ID,bpLocations[i].get_ID());
						if (!subs.isEmpty())
							createSubscription(subs);
					}
				}
				else if (p_M_Prod_Category_ID > 0)
				{
					Integer[] products = getProductList(p_M_Prod_Category_ID);
					for (int i = 0; i < bpLocations.length; i++) 
					{
						for(int j = 0; j<products.length ;j++)
						{
							subs =getSubscriptions(p_C_BPartner_ID, products[j],bpLocations[i].get_ID());
							if (!subs.isEmpty())
								createSubscription(subs);
						}
					}
				}
				else
				{
					for (int i = 0; i < bpLocations.length; i++) 
					{
						subs =getSubscriptions(p_C_BPartner_ID, 0,bpLocations[i].get_ID());
						if (!subs.isEmpty())
							createSubscription(subs);
					}
				}
			/*if (p_M_Product_ID > 0)
			{
				subs = getSubscriptions(p_C_BPartner_ID, p_M_Product_ID);
				if (!subs.isEmpty())
					createSubscription(subs);
			}
			else if (p_M_Prod_Category_ID > 0)
			{
				Integer[] products = getProductList(p_M_Prod_Category_ID);
				for(int i = 0; i<products.length ;i++)
				{
					subs =getSubscriptions(p_C_BPartner_ID, products[i]);
					if (!subs.isEmpty())
						createSubscription(subs);
				} 
			}
			else
			{
				subs =getSubscriptions(p_C_BPartner_ID, 0);
				if (!subs.isEmpty())
					createSubscription(subs);
			}*/
		}
		else
		{
			Integer[] bps = getBPartnerList(p_C_BP_Group_ID);
			for(int i = 0; i<bps.length ;i++)
			{
				MBPartnerLocation[] bpLocations = MBPartnerLocation.getForBPartner(getCtx(), bps[i]) ;
				if (p_M_Product_ID > 0)
				{
					for (int k = 0; k < bpLocations.length; k++) {
						subs = getSubscriptions(bps[i], p_M_Product_ID , bpLocations[k].get_ID());
						if (!subs.isEmpty())
							createSubscription(subs);
					}
				}
				else if (p_M_Prod_Category_ID > 0)
				{
					Integer[] products = getProductList(p_M_Prod_Category_ID);
					for(int j = 0; j<products.length ;j++)
					{
						for (int k = 0; k < bpLocations.length; k++) {
							subs = getSubscriptions(bps[i], products[j],bpLocations[k].get_ID());
							if (!subs.isEmpty())
								createSubscription(subs);
						}
					} 
				}
				else
				{
					for (int k = 0; k < bpLocations.length; k++) {
						subs = getSubscriptions(bps[i], 0 , bpLocations[k].get_ID());
						if (!subs.isEmpty())
							createSubscription(subs);
					}
				}
			} 
		}
		if(subs == null || subs.isEmpty())
			return "No subscriptions to be processed";
		log.info("Exiting doIt() of the Consolidate Subscriptions Process");
		return  "Consolidate Subscriptions Process Ended Successfully";
		
	}

	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			if (name.equals("Name"))
			{
				SubscriptionName = para[i].getParameter().toString();
			}
			if (name.equals("C_BP_Group_ID"))
			{
				p_C_BP_Group_ID=para[i].getParameterAsInt();
			}
			else if(name.equals("C_BPartner_ID"))
			{
				p_C_BPartner_ID=para[i].getParameterAsInt();
			}
			else if (name.equals("M_Product_ID"))
			{
				p_M_Product_ID =  para[i].getParameterAsInt();
			}
			else if (name.equals("M_Product_Category_ID"))
			{
				p_M_Prod_Category_ID =  para[i].getParameterAsInt();
			}
			else
			{
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
		}

	}
	
	private List<MSubscription> getSubscriptions(int bpID , int productID , int bpLocationID)
	{
		log.info("Getting Subscription list");
	  
		List<MSubscription> sub_list = new ArrayList<MSubscription>();
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT C_Subscription_ID FROM C_SUBSCRIPTION WHERE ");
		if (bpID > 0)
			sql.append("ISACTIVE='Y' AND C_BPARTNER_ID = ? ");
		
		if (productID > 0)
			sql.append(" AND M_PRODUCT_ID =  " + productID);
		
		if (bpLocationID > 0)
			sql.append(" AND C_BPartner_Location_ID =  " + bpLocationID);
		
		sql.append(" AND RENEWALDATE <> PAIDUNTILDATE  AND RENEWALDATE >= ADD_MONTHS(SYSDATE,2)");
		
		sql.append(" ORDER BY C_BPARTNER_ID , M_PRODUCT_ID");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{	
			// Create statement and set parameters
			pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			pstmt.setInt(1, bpID);
			/*if (productID > 0)
				pstmt.setInt(2, productID );
			if(bpLocationID > 0)
				pstmt.setInt(3, bpLocationID);*/
			// Execute query and process result set
			rs = pstmt.executeQuery();
			
			while (rs.next())
			{
				MSubscription c_Sub =new MSubscription(getCtx(),rs.getInt(1) , get_TrxName()); 
				sub_list.add(c_Sub);
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
		
		return sub_list ;
	}
	
	/** Get Products belonging to Product Category*/
	private Integer[] getProductList(int product_Category_ID)
	{
		Integer[] products ;
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
				
		String sql = "SELECT M_PRODUCT_ID FROM M_PRODUCT P " +
				     // "INNER JOIN M_PRODUCT_CATEGORY PC ON (P.M_PRODUCT_ID = PC.M_PRODUCT_ID) " +
				     "WHERE P.M_PRODUCT_CATEGORY_ID = ? AND P.ISACTIVE = 'Y'";
		
		try
		{
			pstmt = DB.prepareStatement(sql, get_TrxName());
			pstmt.setInt(1, product_Category_ID);
			
			// Execute query and process result set
			rs = pstmt.executeQuery();
			while (rs.next())
				list.add(rs.getInt(1));
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
		
		products = (Integer[]) list.toArray(new Integer[list.size()]);
		
		return products;
		
	}
	
	/** Create Subscription */
	public void createSubscription(List<MSubscription> subs)
	{
		int count = 0;
		MSubscription cons_record = null;
		//Timestamp renewalDate = null;
		
		Timestamp startDate= new Timestamp(System.currentTimeMillis());
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		cal.add(Calendar.DAY_OF_WEEK, 1);
		startDate = new Timestamp(cal.getTime().getTime());
		
		for(MSubscription sub : subs)
		{
			
			if (count == 0)
			{		
				cons_record = new MSubscription(getCtx() , 0 , get_TrxName());
				cons_record.setName(SubscriptionName);
				cons_record.setC_BPartner_ID(sub.getC_BPartner_ID());
				cons_record.setC_BPartner_Location_ID(sub.getC_BPartner_Location_ID());
				cons_record.setM_Product_ID(sub.getM_Product_ID());
				cons_record.setC_SubscriptionType_ID(sub.getC_SubscriptionType_ID());
				cons_record.setBillInAdvance(true);
				cons_record.setPaidUntilDate(new Timestamp(System.currentTimeMillis()));
				cons_record.setRenewalDate(sub.getRenewalDate());
				cons_record.setStartDate(startDate);
				cons_record.setIsDue(false);
				cons_record.setQty(Env.ZERO);
				
				cons_record.save();
				
				count++ ;
				//continue;
			}
			
				sub.setRenewalDate(new Timestamp(System.currentTimeMillis()));
				sub.save();
				
				cons_record.setQty(cons_record.getQty().add(sub.getQty()));
				cons_record.save();
				
		}
	}
	
	/** Get Business Partners belonging to Business Partner Group*/
	private Integer[] getBPartnerList(int bp_Group_ID)
	{
		Integer[] bPartners ;
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
				
		String sql = "SELECT C_BPARTNER_ID FROM C_BPARTNER BP " +
				     "WHERE BP.C_BP_GROUP_ID = ? AND BP.ISACTIVE = 'Y' ORDER BY BP.C_BPARTNER_ID";
		
		try
		{
			pstmt = DB.prepareStatement(sql, get_TrxName());
			pstmt.setInt(1, bp_Group_ID);
			
			// Execute query and process result set
			rs = pstmt.executeQuery();
			while (rs.next())
				list.add(rs.getInt(1));
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
		
		bPartners = (Integer[])list.toArray(new Integer[list.size()]);
		
		return bPartners;
		
	}

	private String validate()
	{
		StringBuilder sb = new StringBuilder();
		
		if (p_C_BP_Group_ID > 0 && p_C_BPartner_ID  == 0)
		{
			MBPGroup  bpg=new MBPGroup(getCtx(),p_C_BP_Group_ID,get_TrxName());
			if(bpg == null || bpg.get_ID() == 0)
			{
				if (sb.length() > 0)
					sb.append(", ");
				
				sb.append("Cannot load Business Partner Group[" + p_C_BP_Group_ID + "]");
			}
		}
		else if (p_C_BP_Group_ID == 0 && p_C_BPartner_ID  > 0)
		{

			MBPartner bp=new MBPartner(getCtx(),p_C_BPartner_ID,get_TrxName());
			if(bp == null || bp.get_ID() == 0)
			{
				if (sb.length() > 0)
					sb.append(", ");
				
				sb.append("Cannot load Business Partner [" + p_C_BPartner_ID + "]");
			}
		}
		
		if (p_M_Prod_Category_ID > 0 && p_M_Product_ID  == 0)
		{
			MProductCategory  pc=new MProductCategory(getCtx(),p_M_Prod_Category_ID,get_TrxName());
			if(pc == null || pc.get_ID() == 0)
			{
				if (sb.length() > 0)
					sb.append(", ");
				
				sb.append("Cannot load Product Category[" + p_M_Prod_Category_ID + "]");
			}
		}
		else if (p_M_Prod_Category_ID == 0 && p_M_Product_ID  > 0)
		{

			MProduct bp=new MProduct(getCtx(),p_M_Product_ID,get_TrxName());
			if(bp == null || bp.get_ID() == 0)
			{
				if (sb.length() > 0)
					sb.append(", ");
				
				sb.append("Cannot load Product [" + p_M_Product_ID + "]");
			}
		}
		
		if(SubscriptionName == null)
		{
			if (sb.length() > 0)
				sb.append(", ");
			
			sb.append("Subscription Name cannot be Empty ");
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
					
					sb.append("Business Partner doesnot belong to the selected Business Partner Group");
				}
			}
		}
		
		if(p_M_Prod_Category_ID > 0 && p_M_Product_ID > 0)
		{
			MProduct bp=new MProduct(getCtx(),p_M_Product_ID,get_TrxName());
			if(bp == null || bp.get_ID() == 0)
			{
				if (sb.length() > 0)
					sb.append(", ");
				
				sb.append("Cannot load Product [" + p_M_Product_ID + "]");
			}
			else
			{
				if( bp.getM_Product_Category_ID() != p_M_Prod_Category_ID)
				{
					if (sb.length() > 0)
						sb.append(", ");
					
					sb.append("Product doesnot belong to the selected Product Category ");
				}
			}
		}
		if (sb.length() > 0)
			return "@Error@" + sb.toString();
		
		return null;
	}
	
}
