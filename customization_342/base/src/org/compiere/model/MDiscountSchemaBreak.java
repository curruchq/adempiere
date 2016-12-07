/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                        *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.model;

import java.math.*;
import java.sql.*;
import java.util.*;

import org.compiere.util.DB;
import org.compiere.util.Env;


/**
 *	Discount Break Schema (Model)
 *	
 *  @author Jorg Janke
 *  @version $Id: MDiscountSchemaBreak.java,v 1.3 2006/07/30 00:51:05 jjanke Exp $
 */
public class MDiscountSchemaBreak extends X_M_DiscountSchemaBreak
{

	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param M_DiscountSchemaBreak_ID id
	 *	@param trxName transaction
	 */
	public MDiscountSchemaBreak (Properties ctx, int M_DiscountSchemaBreak_ID, String trxName)
	{
		super (ctx, M_DiscountSchemaBreak_ID, trxName);
	}	//	MDiscountSchemaBreak

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName transaction
	 */
	public MDiscountSchemaBreak (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MDiscountSchemaBreak

	
	/**
	 * 	Criteria apply
	 *	@param Value amt or qty
	 *	@param M_Product_ID product
	 *	@param M_Product_Category_ID category
	 *	@return true if criteria met
	 */
	public boolean applies (BigDecimal Value, int M_Product_ID, int M_Product_Category_ID)
	{
		if (!isActive())
			return false;
		
		//	below break value
		if (Value.compareTo(getBreakValue()) < 0)
			return false;
		
		//	No Product / Category 
		if (getM_Product_ID() == 0 
			&& getM_Product_Category_ID() == 0)
			return true;
		
		//	Product
		if (getM_Product_ID() == M_Product_ID)
			return true;
		
		//	Category
		if (M_Product_Category_ID != 0)
			return getM_Product_Category_ID() == M_Product_Category_ID;

		//	Look up Category of Product
		return MProductCategory.isCategory(getM_Product_Category_ID(), M_Product_ID);
	}	//	applies
	
	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MDiscountSchemaBreak[");
		sb.append(get_ID()).append("-Seq=").append(getSeqNo());
		if (getM_Product_Category_ID() != 0)
			sb.append(",M_Product_Category_ID=").append(getM_Product_Category_ID());
		if (getM_Product_ID() != 0)
			sb.append(",M_Product_ID=").append(getM_Product_ID());
		sb.append(",Break=").append(getBreakValue());
		if (isBPartnerFlatDiscount())
			sb.append(",FlatDiscount");
		else
			sb.append(",Discount=").append(getBreakDiscount());
		sb.append ("]");
		return sb.toString ();
	}	//	toString
	
	/**
	 * 	Criteria apply
	 *	@param Value qty
	 *	@param M_Product_ID product
	 *	@return true if criteria met
	 
	public boolean applies (BigDecimal Value, int M_Product_ID, int M_Product_Category_ID, MDiscountSchemaBreak br, int C_Invoice_ID)
	{
		if (!isActive())
			return false;
		BigDecimal qtyTotal = Env.ZERO ;
		
		String sql = "SELECT COUNT(*) FROM C_INVOICELINE WHERE C_INVOICE_ID = ?";
		Integer count = DB.getSQLValue(null, sql, C_Invoice_ID);
		
		if(count > 0 )
		{
		     sql = "SELECT SUM(QTYENTERED) FROM C_INVOICELINE WHERE C_INVOICE_ID = ? AND M_PRODUCT_ID = ?";
		     qtyTotal = DB.getSQLValueBD(null, sql, C_Invoice_ID, M_Product_ID);
		     Value    = qtyTotal ;
		}
		
		//	below break value
		if ((Value.compareTo(br.getBreakValue()) < 0) && (br.getM_Product_ID() == M_Product_ID)) 
			return true;
		else
			return false;
		
		//return false;
	}	//	applies*/
	
	public boolean applies (BigDecimal Value, int M_Product_ID, int M_Product_Category_ID, int C_Invoice_ID)
	{
		if (!isActive())
			return false;
		
//		Product
		if (getM_Product_ID() != M_Product_ID)
			return false;
		
		if ((Value.compareTo(getBreakValue()) < 0)) 
			return false;
		
		String sql="SELECT MIN(BreakValue) FROM M_DISCOUNTSCHEMABREAK WHERE M_DISCOUNTSCHEMA_ID="+getM_DiscountSchema_ID()+" AND BREAKVALUE > "+getBreakValue();
		if(M_Product_ID>0)
			sql+=" AND M_PRODUCT_ID="+M_Product_ID;
		
		int DiscountBreak=DB.getSQLValue(null, sql, new Object[]{});
		int intValue = Value.intValue();
		if ((DiscountBreak > 0) && (intValue > DiscountBreak)) 
			return false;
		
		return true;
	}	//	applies
	
}	//	MDiscountSchemaBreak
