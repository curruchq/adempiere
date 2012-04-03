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
import org.compiere.model.*;
import org.compiere.util.KeyNamePair;

/** Generated Interface for MOD_DIDX_Account
 *  @author Trifon Trifonov (generated) 
 *  @version 3.4.2s+P20090827
 */
public interface I_MOD_DIDX_Account 
{

    /** TableName=MOD_DIDX_Account */
    public static final String Table_Name = "MOD_DIDX_Account";

    /** AD_Table_ID=1000004 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(3);

    /** Load Meta Data */

    /** Column name DIDX_PASSWORD */
    public static final String COLUMNNAME_DIDX_PASSWORD = "DIDX_PASSWORD";

	/** Set DIDx Password	  */
	public void setDIDX_PASSWORD (String DIDX_PASSWORD);

	/** Get DIDx Password	  */
	public String getDIDX_PASSWORD();

    /** Column name DIDX_USERID */
    public static final String COLUMNNAME_DIDX_USERID = "DIDX_USERID";

	/** Set DIDx User Id	  */
	public void setDIDX_USERID (String DIDX_USERID);

	/** Get DIDx User Id	  */
	public String getDIDX_USERID();

    /** Column name MAX_VENDOR_RATING */
    public static final String COLUMNNAME_MAX_VENDOR_RATING = "MAX_VENDOR_RATING";

	/** Set Max Vendor Rating	  */
	public void setMAX_VENDOR_RATING (int MAX_VENDOR_RATING);

	/** Get Max Vendor Rating	  */
	public int getMAX_VENDOR_RATING();

    /** Column name MIN_VENDOR_RATING */
    public static final String COLUMNNAME_MIN_VENDOR_RATING = "MIN_VENDOR_RATING";

	/** Set Min Vendor Rating	  */
	public void setMIN_VENDOR_RATING (int MIN_VENDOR_RATING);

	/** Get Min Vendor Rating	  */
	public int getMIN_VENDOR_RATING();

    /** Column name MOD_DIDX_Account_ID */
    public static final String COLUMNNAME_MOD_DIDX_Account_ID = "MOD_DIDX_Account_ID";

	/** Set DIDx Account	  */
	public void setMOD_DIDX_Account_ID (int MOD_DIDX_Account_ID);

	/** Get DIDx Account	  */
	public int getMOD_DIDX_Account_ID();

    /** Column name Value */
    public static final String COLUMNNAME_Value = "Value";

	/** Set Search Key.
	  * Search key for the record in the format required - must be unique
	  */
	public void setValue (String Value);

	/** Get Search Key.
	  * Search key for the record in the format required - must be unique
	  */
	public String getValue();
}
