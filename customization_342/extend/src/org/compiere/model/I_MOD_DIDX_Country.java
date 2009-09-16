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

/** Generated Interface for MOD_DIDX_Country
 *  @author Trifon Trifonov (generated) 
 *  @version 3.4.2s+P20090827
 */
public interface I_MOD_DIDX_Country 
{

    /** TableName=MOD_DIDX_Country */
    public static final String Table_Name = "MOD_DIDX_Country";

    /** AD_Table_ID=1000005 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(3);

    /** Load Meta Data */

    /** Column name DIDX_COUNTRYID */
    public static final String COLUMNNAME_DIDX_COUNTRYID = "DIDX_COUNTRYID";

	/** Set DIDx Country Id	  */
	public void setDIDX_COUNTRYID (int DIDX_COUNTRYID);

	/** Get DIDx Country Id	  */
	public int getDIDX_COUNTRYID();

    /** Column name DIDX_COUNTRY_CODE */
    public static final String COLUMNNAME_DIDX_COUNTRY_CODE = "DIDX_COUNTRY_CODE";

	/** Set DIDx Country Code	  */
	public void setDIDX_COUNTRY_CODE (int DIDX_COUNTRY_CODE);

	/** Get DIDx Country Code	  */
	public int getDIDX_COUNTRY_CODE();

    /** Column name DIDX_COUNTRY_NAME */
    public static final String COLUMNNAME_DIDX_COUNTRY_NAME = "DIDX_COUNTRY_NAME";

	/** Set DIDx Country Name	  */
	public void setDIDX_COUNTRY_NAME (String DIDX_COUNTRY_NAME);

	/** Get DIDx Country Name	  */
	public String getDIDX_COUNTRY_NAME();

    /** Column name DIDX_SEARCH */
    public static final String COLUMNNAME_DIDX_SEARCH = "DIDX_SEARCH";

	/** Set DIDx Search	  */
	public void setDIDX_SEARCH (boolean DIDX_SEARCH);

	/** Get DIDx Search	  */
	public boolean isDIDX_SEARCH();

    /** Column name MOD_DIDX_Account_ID */
    public static final String COLUMNNAME_MOD_DIDX_Account_ID = "MOD_DIDX_Account_ID";

	/** Set DIDx Account	  */
	public void setMOD_DIDX_Account_ID (int MOD_DIDX_Account_ID);

	/** Get DIDx Account	  */
	public int getMOD_DIDX_Account_ID();

    /** Column name MOD_DIDX_Country_ID */
    public static final String COLUMNNAME_MOD_DIDX_Country_ID = "MOD_DIDX_Country_ID";

	/** Set DIDx Country	  */
	public void setMOD_DIDX_Country_ID (int MOD_DIDX_Country_ID);

	/** Get DIDx Country	  */
	public int getMOD_DIDX_Country_ID();
}
