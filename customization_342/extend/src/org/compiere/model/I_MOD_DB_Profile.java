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

/** Generated Interface for MOD_DB_Profile
 *  @author Trifon Trifonov (generated) 
 *  @version 3.4.2s+P20090916
 */
public interface I_MOD_DB_Profile 
{

    /** TableName=MOD_DB_Profile */
    public static final String Table_Name = "MOD_DB_Profile";

    /** AD_Table_ID=1000006 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 4 - System 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(4);

    /** Load Meta Data */

    /** Column name DB_HOST */
    public static final String COLUMNNAME_DB_HOST = "DB_HOST";

	/** Set DB Host	  */
	public void setDB_HOST (String DB_HOST);

	/** Get DB Host	  */
	public String getDB_HOST();

    /** Column name DB_PASSWORD */
    public static final String COLUMNNAME_DB_PASSWORD = "DB_PASSWORD";

	/** Set DB Password	  */
	public void setDB_PASSWORD (String DB_PASSWORD);

	/** Get DB Password	  */
	public String getDB_PASSWORD();

    /** Column name DB_PORT */
    public static final String COLUMNNAME_DB_PORT = "DB_PORT";

	/** Set DB Port	  */
	public void setDB_PORT (int DB_PORT);

	/** Get DB Port	  */
	public int getDB_PORT();

    /** Column name DB_SCHEMA */
    public static final String COLUMNNAME_DB_SCHEMA = "DB_SCHEMA";

	/** Set DB Schema	  */
	public void setDB_SCHEMA (String DB_SCHEMA);

	/** Get DB Schema	  */
	public String getDB_SCHEMA();

    /** Column name DB_USERNAME */
    public static final String COLUMNNAME_DB_USERNAME = "DB_USERNAME";

	/** Set DB Username	  */
	public void setDB_USERNAME (String DB_USERNAME);

	/** Get DB Username	  */
	public String getDB_USERNAME();

    /** Column name MOD_DB_Profile_ID */
    public static final String COLUMNNAME_MOD_DB_Profile_ID = "MOD_DB_Profile_ID";

	/** Set DB Profile	  */
	public void setMOD_DB_Profile_ID (int MOD_DB_Profile_ID);

	/** Get DB Profile	  */
	public int getMOD_DB_Profile_ID();

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
