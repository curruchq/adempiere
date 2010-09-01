/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2007 ComPiere, Inc. All Rights Reserved.                *
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
/** Generated Model - DO NOT CHANGE */
package org.compiere.model;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.logging.Level;
import org.compiere.util.Env;

/** Generated Model for MOD_Billing_Record
 *  @author Adempiere (generated) 
 *  @version 3.4.2s+P20100205 - $Id$ */
public class X_MOD_Billing_Record extends PO implements I_MOD_Billing_Record, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

    /** Standard Constructor */
    public X_MOD_Billing_Record (Properties ctx, int MOD_Billing_Record_ID, String trxName)
    {
      super (ctx, MOD_Billing_Record_ID, trxName);
      /** if (MOD_Billing_Record_ID == 0)
        {
			setC_InvoiceLine_ID (0);
			setC_Invoice_ID (0);
			setMod_Billing_Record_ID (0);
			setProcessed (false);
			setRadAcctId (0);
			setSyncronised (false);
        } */
    }

    /** Load Constructor */
    public X_MOD_Billing_Record (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer ("X_MOD_Billing_Record[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set AcctStartTime.
		@param AcctStartTime AcctStartTime	  */
	public void setAcctStartTime (Timestamp AcctStartTime)
	{
		set_Value (COLUMNNAME_AcctStartTime, AcctStartTime);
	}

	/** Get AcctStartTime.
		@return AcctStartTime	  */
	public Timestamp getAcctStartTime () 
	{
		return (Timestamp)get_Value(COLUMNNAME_AcctStartTime);
	}

	public I_C_InvoiceLine getC_InvoiceLine() throws Exception 
    {
        Class<?> clazz = MTable.getClass(I_C_InvoiceLine.Table_Name);
        I_C_InvoiceLine result = null;
        try	{
	        Constructor<?> constructor = null;
	    	constructor = clazz.getDeclaredConstructor(new Class[]{Properties.class, int.class, String.class});
    	    result = (I_C_InvoiceLine)constructor.newInstance(new Object[] {getCtx(), new Integer(getC_InvoiceLine_ID()), get_TrxName()});
        } catch (Exception e) {
	        log.log(Level.SEVERE, "(id) - Table=" + Table_Name + ",Class=" + clazz, e);
	        log.saveError("Error", "Table=" + Table_Name + ",Class=" + clazz);
           throw e;
        }
        return result;
    }

	/** Set Invoice Line.
		@param C_InvoiceLine_ID 
		Invoice Detail Line
	  */
	public void setC_InvoiceLine_ID (int C_InvoiceLine_ID)
	{
		if (C_InvoiceLine_ID < 1)
			 throw new IllegalArgumentException ("C_InvoiceLine_ID is mandatory.");
		set_Value (COLUMNNAME_C_InvoiceLine_ID, Integer.valueOf(C_InvoiceLine_ID));
	}

	/** Get Invoice Line.
		@return Invoice Detail Line
	  */
	public int getC_InvoiceLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_InvoiceLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_C_Invoice getC_Invoice() throws Exception 
    {
        Class<?> clazz = MTable.getClass(I_C_Invoice.Table_Name);
        I_C_Invoice result = null;
        try	{
	        Constructor<?> constructor = null;
	    	constructor = clazz.getDeclaredConstructor(new Class[]{Properties.class, int.class, String.class});
    	    result = (I_C_Invoice)constructor.newInstance(new Object[] {getCtx(), new Integer(getC_Invoice_ID()), get_TrxName()});
        } catch (Exception e) {
	        log.log(Level.SEVERE, "(id) - Table=" + Table_Name + ",Class=" + clazz, e);
	        log.saveError("Error", "Table=" + Table_Name + ",Class=" + clazz);
           throw e;
        }
        return result;
    }

	/** Set Invoice.
		@param C_Invoice_ID 
		Invoice Identifier
	  */
	public void setC_Invoice_ID (int C_Invoice_ID)
	{
		if (C_Invoice_ID < 1)
			 throw new IllegalArgumentException ("C_Invoice_ID is mandatory.");
		set_Value (COLUMNNAME_C_Invoice_ID, Integer.valueOf(C_Invoice_ID));
	}

	/** Get Invoice.
		@return Invoice Identifier
	  */
	public int getC_Invoice_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Invoice_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set MOD Billing Record.
		@param Mod_Billing_Record_ID MOD Billing Record	  */
	public void setMod_Billing_Record_ID (int Mod_Billing_Record_ID)
	{
		if (Mod_Billing_Record_ID < 1)
			 throw new IllegalArgumentException ("Mod_Billing_Record_ID is mandatory.");
		set_ValueNoCheck (COLUMNNAME_Mod_Billing_Record_ID, Integer.valueOf(Mod_Billing_Record_ID));
	}

	/** Get MOD Billing Record.
		@return MOD Billing Record	  */
	public int getMod_Billing_Record_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Mod_Billing_Record_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Normalized.
		@param Normalized Normalized	  */
	public void setNormalized (String Normalized)
	{

		if (Normalized != null && Normalized.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			Normalized = Normalized.substring(0, 200);
		}
		set_Value (COLUMNNAME_Normalized, Normalized);
	}

	/** Get Normalized.
		@return Normalized	  */
	public String getNormalized () 
	{
		return (String)get_Value(COLUMNNAME_Normalized);
	}

	/** Set Price.
		@param Price 
		Price
	  */
	public void setPrice (BigDecimal Price)
	{
		set_Value (COLUMNNAME_Price, Price);
	}

	/** Get Price.
		@return Price
	  */
	public BigDecimal getPrice () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Price);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Processed.
		@param Processed 
		The document has been processed
	  */
	public void setProcessed (boolean Processed)
	{
		set_Value (COLUMNNAME_Processed, Boolean.valueOf(Processed));
	}

	/** Get Processed.
		@return The document has been processed
	  */
	public boolean isProcessed () 
	{
		Object oo = get_Value(COLUMNNAME_Processed);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set RadAcctId.
		@param RadAcctId RadAcctId	  */
	public void setRadAcctId (int RadAcctId)
	{
		set_Value (COLUMNNAME_RadAcctId, Integer.valueOf(RadAcctId));
	}

	/** Get RadAcctId.
		@return RadAcctId	  */
	public int getRadAcctId () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_RadAcctId);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Rate.
		@param Rate 
		Rate or Tax or Exchange
	  */
	public void setRate (String Rate)
	{

		if (Rate != null && Rate.length() > 1000)
		{
			log.warning("Length > 1000 - truncated");
			Rate = Rate.substring(0, 1000);
		}
		set_Value (COLUMNNAME_Rate, Rate);
	}

	/** Get Rate.
		@return Rate or Tax or Exchange
	  */
	public String getRate () 
	{
		return (String)get_Value(COLUMNNAME_Rate);
	}

	/** Set SipApplicationType.
		@param SipApplicationType SipApplicationType	  */
	public void setSipApplicationType (String SipApplicationType)
	{

		if (SipApplicationType != null && SipApplicationType.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			SipApplicationType = SipApplicationType.substring(0, 200);
		}
		set_Value (COLUMNNAME_SipApplicationType, SipApplicationType);
	}

	/** Get SipApplicationType.
		@return SipApplicationType	  */
	public String getSipApplicationType () 
	{
		return (String)get_Value(COLUMNNAME_SipApplicationType);
	}

	/** Set Syncronised.
		@param Syncronised Syncronised	  */
	public void setSyncronised (boolean Syncronised)
	{
		set_Value (COLUMNNAME_Syncronised, Boolean.valueOf(Syncronised));
	}

	/** Get Syncronised.
		@return Syncronised	  */
	public boolean isSyncronised () 
	{
		Object oo = get_Value(COLUMNNAME_Syncronised);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Registered EMail.
		@param UserName 
		Email of the responsible for the System
	  */
	public void setUserName (String UserName)
	{

		if (UserName != null && UserName.length() > 200)
		{
			log.warning("Length > 200 - truncated");
			UserName = UserName.substring(0, 200);
		}
		set_Value (COLUMNNAME_UserName, UserName);
	}

	/** Get Registered EMail.
		@return Email of the responsible for the System
	  */
	public String getUserName () 
	{
		return (String)get_Value(COLUMNNAME_UserName);
	}
}