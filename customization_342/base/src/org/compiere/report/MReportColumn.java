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
package org.compiere.report;

import java.math.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;
import org.compiere.model.*;

/**
 *  Report Column Model
 *
 *  @author Jorg Janke
 *  @version $Id: MReportColumn.java,v 1.3 2006/08/03 22:16:52 jjanke Exp $
 */
public class MReportColumn extends X_PA_ReportColumn
{
	/**
	 * 	Constructor
	 * 	@param ctx context
	 * 	@param PA_ReportColumn_ID id
	 * 	@param trxName transaction
	 */
	public MReportColumn (Properties ctx, int PA_ReportColumn_ID, String trxName)
	{
		super (ctx, PA_ReportColumn_ID, trxName);
		if (PA_ReportColumn_ID == 0)
		{
			setIsPrinted (true);
			setSeqNo (0);
		}
	}	//	MReportColumn

	/**
	 * 	Constructor
	 * 	@param ctx context
	 * 	@param rs ResultSet to load from
	 * 	@param trxName transaction
	 */
	public MReportColumn (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MReportColumn

	/**************************************************************************
	 * 	Get Column SQL Select Clause.
	 * 	@param withSum with SUM() function
	 * 	@return select clause - AmtAcctCR+AmtAcctDR/etc or "null" if not defined
	 */
	public String getSelectClause (boolean withSum)
	{
		String amountType = getPAAmountType();	//	first character

		StringBuffer sb = new StringBuffer();
		if (withSum)
			sb.append("SUM(");
		if (PAAMOUNTTYPE_BalanceExpectedSign.equals(amountType))
		//	sb.append("AmtAcctDr-AmtAcctCr");
			sb.append("acctBalance(Account_ID,AmtAcctDr,AmtAcctCr)");
		else if ( PAAMOUNTTYPE_BalanceAccountedSign.equals(amountType) )
			sb.append("AmtAcctDr-AmtAcctCr");
		else if (PAAMOUNTTYPE_CreditOnly.equals(amountType))
			sb.append("AmtAcctCr");
		else if (PAAMOUNTTYPE_DebitOnly.equals(amountType))
			sb.append("AmtAcctDr");
		else if (PAAMOUNTTYPE_QuantityAccountedSign.equals(amountType))
			sb.append("Qty");
		else if (PAAMOUNTTYPE_QuantityExpectedSign.equals(amountType))
			sb.append("acctBalance(Account_ID,Qty,0)");
		else
		{
			log.log(Level.SEVERE, "AmountType=" + getPAAmountType () + ", at=" + amountType);
			return "NULL";
		}		
		if (withSum)
			sb.append(")");
		return sb.toString();
	}	//	getSelectClause

	/**
	 * 	Is it Period Info ?
	 * 	@return true if Period Amount Type
	 */
	public boolean isPeriod()
	{
		String pt = getPAPeriodType();
		if (pt == null)
			return false;
		return PAPERIODTYPE_Period.equals(pt);
	}	//	isPeriod

	/**
	 * 	Is it Year Info ?
	 * 	@return true if Year Amount Type
	 */
	public boolean isYear()
	{
		String pt = getPAPeriodType();
		if (pt == null)
			return false;
		return PAPERIODTYPE_Year.equals(pt);
	}	//	isYear

	/**
	 * 	Is it Total Info ?
	 * 	@return true if Year Amount Type
	 */
	public boolean isTotal()
	{
		String pt = getPAPeriodType();
		if (pt == null)
			return false;
		return PAPERIODTYPE_Total.equals(pt);
	}	//	isTotalBalance


	/**
	 * 	Get String Representation
	 * 	@return	String Representation
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MReportColumn[")
			.append(get_ID()).append(" - ").append(getName()).append(" - ").append(getDescription())
			.append(", SeqNo=").append(getSeqNo()).append(", AmountType=").append(getPAAmountType())
			.append(", PeriodType=").append(getPAPeriodType())
			.append(", CurrencyType=").append(getCurrencyType()).append("/").append(getC_Currency_ID())
			.append(" - ColumnType=").append(getColumnType());
		if (isColumnTypeCalculation())
			sb.append(" - Calculation=").append(getCalculationType())
				.append(" - ").append(getOper_1_ID()).append(" - ").append(getOper_2_ID());
		else if (isColumnTypeRelativePeriod())
			sb.append(" - Period=").append(getRelativePeriod());
		else
			sb.append(" - SegmentValue ElementType=").append(getElementType());
		sb.append ("]");
		return sb.toString ();
	}	//	toString

	/**
	 * 	Calculation Type Range
	 *	@return true if range
	 */
	public boolean isCalculationTypeRange()
	{
		return CALCULATIONTYPE_AddRangeOp1ToOp2.equals(getCalculationType());
	}
	/**
	 * 	Calculation Type Add
	 *	@return true id add
	 */
	public boolean isCalculationTypeAdd()
	{
		return CALCULATIONTYPE_AddOp1PlusOp2.equals(getCalculationType());
	}
	/**
	 * 	Calculation Type Subtract
	 *	@return true if subtract
	 */
	public boolean isCalculationTypeSubtract()
	{
		return CALCULATIONTYPE_SubtractOp1_Op2.equals(getCalculationType());
	}
	/**
	 * 	Calculation Type Percent
	 *	@return true if percent
	 */
	public boolean isCalculationTypePercent()
	{
		return CALCULATIONTYPE_PercentageOp1OfOp2.equals(getCalculationType());
	}


	/**
	 * 	Column Type Calculation
	 *	@return true if calculation
	 */
	public boolean isColumnTypeCalculation()
	{
		return COLUMNTYPE_Calculation.equals(getColumnType());
	}
	/**
	 * 	Column Type Relative Period
	 *	@return true if ralative period
	 */
	public boolean isColumnTypeRelativePeriod()
	{
		return COLUMNTYPE_RelativePeriod.equals(getColumnType());
	}
	/**
	 * 	Column Type Segment Value
	 *	@return true if segment value
	 */
	public boolean isColumnTypeSegmentValue()
	{
		return COLUMNTYPE_SegmentValue.equals(getColumnType());
	}
	/**
	 * 	Get Relative Period As Int
	 *	@return relative period
	 */
	public int getRelativePeriodAsInt ()
	{
		BigDecimal bd = getRelativePeriod();
		if (bd == null)
			return 0;
		return bd.intValue();
	}	//	getRelativePeriodAsInt


	/**************************************************************************

	/**
	 * 	Copy
	 * 	@param ctx context
	 * 	@param AD_Client_ID parent
	 * 	@param AD_Org_ID parent
	 * 	@param PA_ReportColumnSet_ID parent
	 * 	@param source copy source
	 * 	@param trxName transaction
	 * 	@return Report Column
	 */
	public static MReportColumn copy (Properties ctx, int AD_Client_ID, int AD_Org_ID, 
		int PA_ReportColumnSet_ID, MReportColumn source, String trxName)
	{
		MReportColumn retValue = new MReportColumn (ctx, 0, trxName);
		MReportColumn.copyValues(source, retValue, AD_Client_ID, AD_Org_ID);
		//
		retValue.setPA_ReportColumnSet_ID(PA_ReportColumnSet_ID);	//	parent
		retValue.setOper_1_ID(0);
		retValue.setOper_2_ID(0);
		return retValue;
	}	//	copy

}	//	MReportColumn
