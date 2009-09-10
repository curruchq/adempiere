/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
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
 * Contributor(s): Carlos Ruiz - globalqss                                    *
 *                 Teo Sarca                                                  *
 *                 Trifon Trifonov                                            *
 *****************************************************************************/
package com.conversant.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;

import org.adempiere.util.ModelClassGenerator;
import org.adempiere.util.ModelInterfaceGenerator;
import org.compiere.Adempiere;
import org.compiere.util.CLogMgt;
import org.compiere.util.CLogger;
import org.compiere.util.DB;

/**
 *  Generate Model Classes extending PO.
 *  Base class for CMP interface - will be extended to create byte code directly
 *
 *  @author Jorg Janke
 *  @author Josh Hill
 *  @version $Id: GenerateModel.java,v 1.42 2005/05/08 15:16:56 jjanke Exp $
 */
public class GenerateModel
{
	
	/**	Logger			*/
	private static CLogger	log	= CLogger.getCLogger (GenerateModel.class);
	
	/**
	 * 	String representation
	 * 	@return string representation
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer ("GenerateModel[").append("]");
		return sb.toString();
	}


	/**************************************************************************
	 * 	Generate PO Model Class.
	 * 	<pre>
	 * 	Example: java GenerateModel.class mydirectory myPackage 'U','A'
	 * 	would generate entity type User and Application classes into mydirectory.
	 * 	Without parameters, the default is used:
	 * 	C:\Compiere\compiere-all\extend\src\compiere\model\ compiere.model 'U','A'
	 * 	</pre>
	 * 	@param args directory package entityType 
	 * 	- directory where to save the generated file
	 * 	- package of the classes to be generated
	 * 	- entityType to be generated
	 */
	public static void main (String[] args)
	{
		Adempiere.startupEnvironment(false);
		CLogMgt.setLevel(Level.INFO);
		log.info("Generate Model   $Revision: 1.42 $");
		log.info("----------------------------------");
		//	first parameter
		String directory = System.getProperty("user.dir");
		log.info("Directory: " + directory);
		
		//	second parameter
		String packageName = null;
		if (args.length > 0)
			packageName = args[0]; 
		if (packageName == null || packageName.length() == 0)
		{
			System.err.println("No package");
			System.exit(1);
		}
		log.info("Package:   " + packageName);
		
		ArrayList<String> tableNames = new ArrayList<String>();
		if (args.length > 1)
		{
			for (int i = 1; i < args.length; i++)
				tableNames.add(args[i]);
		}
		else
		{
			System.err.println("No table(s)");
			System.exit(1);
		}
		log.info("Table Names: " + tableNames.toString());

		if (tableNames.size() < 1)
		{
			System.err.println("No table(s)");
			System.exit(1);
		}
		
		//	complete sql
		StringBuffer sql = new StringBuffer();
		sql.insert(0, "SELECT AD_Table_ID "
			+ "FROM AD_Table "
			+ "WHERE (TableName IN ('RV_WarehousePrice','RV_BPartner')"	//	special views
			+ " OR IsView='N')"
			+ " AND TableName NOT LIKE '%_Trl'");
		
		if (tableNames.size() == 1)
		{
			sql.append(" AND TableName LIKE '").append(tableNames.get(0)).append("'");
		}
		else if (tableNames.size() > 1)
		{	
			sql.append(" AND TableName IN (");
			boolean first = true;
			for (String tableName : tableNames)
			{
				if (!first)
					sql.append(", ");
				
				sql.append("'" + tableName + "'");
				
				if (first)
					first = false;
			}
			sql.append(")");
		}
		
		sql.append(" ORDER BY TableName");
		
		//
		int count = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				new ModelInterfaceGenerator(rs.getInt(1), directory, packageName);
				new ModelClassGenerator(rs.getInt(1), directory, packageName);
				count++;
			}
 		}
		catch (Exception e)
		{
			log.severe("main - " + e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		log.info("Generated = " + count);
	}

}
