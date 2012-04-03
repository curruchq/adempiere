/**********************************************************************
 * This file is part of Adempiere ERP Bazaar                          *
 * http://www.adempiere.org                                           *
 *                                                                    *
 * Copyright (C) Trifon Trifonov.                                     *
 * Copyright (C) Contributors                                         *
 *                                                                    *
 * This program is free software;
 you can redistribute it and/or      *
 * modify it under the terms of the GNU General Public License        *
 * as published by the Free Software Foundation;
 either version 2     *
 * of the License, or (at your option) any later version.             *
 *                                                                    *
 * This program is distributed in the hope that it will be useful,    *
 * but WITHOUT ANY WARRANTY;
 without even the implied warranty of     *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the       *
 * GNU General Public License for more details.                       *
 *                                                                    *
 * You should have received a copy of the GNU General Public License  *
 * along with this program;
 if not, write to the Free Software        *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,         *
 * MA 02110-1301, USA.                                                *
 *                                                                    *
 * Contributors:                                                      *
 * - Trifon Trifonov (trifonnt@users.sourceforge.net)                 *
 *                                                                    *
 * Sponsors:                                                          *
 * - Company (http://www.site.com)                                    *
 **********************************************************************/
package org.compiere.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.util.KeyNamePair;

/** Generated Interface for MOD_Billing_Record
 *  @author Trifon Trifonov (generated) 
 *  @version 3.4.2s+P20100205
 */
public interface I_MOD_Billing_Record 
{

    /** TableName=MOD_Billing_Record */
    public static final String Table_Name = "MOD_Billing_Record";

    /** AD_Table_ID=1000003 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(3);

    /** Load Meta Data */

    /** Column name AcctStartTime */
    public static final String COLUMNNAME_AcctStartTime = "AcctStartTime";

	/** Set AcctStartTime	  */
	public void setAcctStartTime (Timestamp AcctStartTime);

	/** Get AcctStartTime	  */
	public Timestamp getAcctStartTime();

    /** Column name C_InvoiceLine_ID */
    public static final String COLUMNNAME_C_InvoiceLine_ID = "C_InvoiceLine_ID";

	/** Set Invoice Line.
	  * Invoice Detail Line
	  */
	public void setC_InvoiceLine_ID (int C_InvoiceLine_ID);

	/** Get Invoice Line.
	  * Invoice Detail Line
	  */
	public int getC_InvoiceLine_ID();

	public I_C_InvoiceLine getC_InvoiceLine() throws Exception;

    /** Column name C_Invoice_ID */
    public static final String COLUMNNAME_C_Invoice_ID = "C_Invoice_ID";

	/** Set Invoice.
	  * Invoice Identifier
	  */
	public void setC_Invoice_ID (int C_Invoice_ID);

	/** Get Invoice.
	  * Invoice Identifier
	  */
	public int getC_Invoice_ID();

	public I_C_Invoice getC_Invoice() throws Exception;

    /** Column name Mod_Billing_Record_ID */
    public static final String COLUMNNAME_Mod_Billing_Record_ID = "Mod_Billing_Record_ID";

	/** Set MOD Billing Record	  */
	public void setMod_Billing_Record_ID (int Mod_Billing_Record_ID);

	/** Get MOD Billing Record	  */
	public int getMod_Billing_Record_ID();

    /** Column name Normalized */
    public static final String COLUMNNAME_Normalized = "Normalized";

	/** Set Normalized	  */
	public void setNormalized (String Normalized);

	/** Get Normalized	  */
	public String getNormalized();

    /** Column name Price */
    public static final String COLUMNNAME_Price = "Price";

	/** Set Price.
	  * Price
	  */
	public void setPrice (BigDecimal Price);

	/** Get Price.
	  * Price
	  */
	public BigDecimal getPrice();

    /** Column name Processed */
    public static final String COLUMNNAME_Processed = "Processed";

	/** Set Processed.
	  * The document has been processed
	  */
	public void setProcessed (boolean Processed);

	/** Get Processed.
	  * The document has been processed
	  */
	public boolean isProcessed();

    /** Column name RadAcctId */
    public static final String COLUMNNAME_RadAcctId = "RadAcctId";

	/** Set RadAcctId	  */
	public void setRadAcctId (int RadAcctId);

	/** Get RadAcctId	  */
	public int getRadAcctId();

    /** Column name Rate */
    public static final String COLUMNNAME_Rate = "Rate";

	/** Set Rate.
	  * Rate or Tax or Exchange
	  */
	public void setRate (String Rate);

	/** Get Rate.
	  * Rate or Tax or Exchange
	  */
	public String getRate();

    /** Column name SipApplicationType */
    public static final String COLUMNNAME_SipApplicationType = "SipApplicationType";

	/** Set SipApplicationType	  */
	public void setSipApplicationType (String SipApplicationType);

	/** Get SipApplicationType	  */
	public String getSipApplicationType();

    /** Column name Syncronised */
    public static final String COLUMNNAME_Syncronised = "Syncronised";

	/** Set Syncronised	  */
	public void setSyncronised (boolean Syncronised);

	/** Get Syncronised	  */
	public boolean isSyncronised();

    /** Column name UserName */
    public static final String COLUMNNAME_UserName = "UserName";

	/** Set Registered EMail.
	  * Email of the responsible for the System
	  */
	public void setUserName (String UserName);

	/** Get Registered EMail.
	  * Email of the responsible for the System
	  */
	public String getUserName();
}
