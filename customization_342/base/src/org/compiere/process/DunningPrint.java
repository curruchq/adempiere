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
package org.compiere.process;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.model.MClient;
import org.compiere.model.MDunningLevel;
import org.compiere.model.MDunningRun;
import org.compiere.model.MDunningRunEntry;
import org.compiere.model.MMailText;
import org.compiere.model.MQuery;
import org.compiere.model.MUser;
import org.compiere.model.MUserMail;
import org.compiere.model.PrintInfo;
import org.compiere.model.X_C_DunningRunEntry;
import org.compiere.print.MPrintFormat;
import org.compiere.print.ReportEngine;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.EMail;
import org.compiere.util.SecureEngine;

/**
 *	Dunning Letter Print
 *	
 *  @author Jorg Janke
 *  @version $Id: DunningPrint.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class DunningPrint extends SvrProcess
{
	/**	Mail PDF				*/
	private boolean		p_EMailPDF = false;
	/** Mail Template			*/
	private int			p_R_MailText_ID = 0;
	/** Dunning Run				*/
	private int			p_C_DunningRun_ID = 0;
	/** Print only Outstanding	*/
	private boolean		p_IsOnlyIfBPBalance = true;
	/** Print only unprocessed lines */
	private boolean p_PrintUnprocessedOnly = true;
	private int  m_AD_Role_ID=1000019;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("EMailPDF"))
				p_EMailPDF = "Y".equals(para[i].getParameter());
			else if (name.equals("R_MailText_ID"))
				p_R_MailText_ID = para[i].getParameterAsInt();
			else if (name.equals("C_DunningRun_ID"))
				p_C_DunningRun_ID = para[i].getParameterAsInt();
			else if (name.equals("IsOnlyIfBPBalance"))
				p_IsOnlyIfBPBalance = "Y".equals(para[i].getParameter());
			else if (name.equals("PrintUnprocessedOnly"))
				p_PrintUnprocessedOnly = "Y".equals(para[i].getParameter());
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}	//	prepare

	/**
	 * Pocess
	 * @return info
	 * @throws Exception
	 */
	protected String doIt () throws Exception
	{
		log.info("C_DunningRun_ID=" + p_C_DunningRun_ID + ",R_MailText_ID=" + p_R_MailText_ID 
			+ ", EmailPDF=" + p_EMailPDF + ",IsOnlyIfBPBalance=" + p_IsOnlyIfBPBalance 
			+ ",PrintUnprocessedOnly=" + p_PrintUnprocessedOnly);
		
		//	Need to have Template
		if (p_EMailPDF && p_R_MailText_ID == 0)
			throw new AdempiereUserError ("@NotFound@: @R_MailText_ID@");
//		String subject = "";
		MMailText mText = null;
		if (p_EMailPDF)
		{
			mText = new MMailText (getCtx(), p_R_MailText_ID, get_TrxName());
			if (p_EMailPDF && mText.get_ID() == 0)
				throw new AdempiereUserError ("@NotFound@: @R_MailText_ID@ - " + p_R_MailText_ID);
//			subject = mText.getMailHeader();
		}
		//
		MDunningRun run = new MDunningRun (getCtx(), p_C_DunningRun_ID, get_TrxName());
		if (run.get_ID() == 0)
			throw new AdempiereUserError ("@NotFound@: @C_DunningRun_ID@ - " + p_C_DunningRun_ID);
		//	Print Format on Dunning Level
		MDunningLevel level = new MDunningLevel (getCtx(), run.getC_DunningLevel_ID(), get_TrxName());
		MPrintFormat format = MPrintFormat.get (getCtx(), level.getDunning_PrintFormat_ID(), false);
		
		MClient client = MClient.get(getCtx());
		
		int count = 0;
		int errors = 0;
		MDunningRunEntry[] entries = run.getEntries(false);
		for (int i = 0; i < entries.length; i++)
		{
			MDunningRunEntry entry = entries[i];
			if (p_IsOnlyIfBPBalance && entry.getAmt().signum() <= 0)
				continue;
			if (p_PrintUnprocessedOnly && entry.isProcessed())
				continue;
			//	To BPartner
			MBPartner bp = new MBPartner (getCtx(), entry.getC_BPartner_ID(), get_TrxName());
			if (bp.get_ID() == 0)
			{
				addLog (entry.get_ID(), null, null, "@NotFound@: @C_BPartner_ID@ " + entry.getC_BPartner_ID());
				errors++;
				continue;
			}
			
			
			// send emails to all the contacts with Billing Contact role(if billing contact role exists) or send to active contacts
			//
			for(MUser to :getBillingContacts(bp.get_ID()))
			{
					//	To User
					//MUser to = new MUser (getCtx(), entry.getAD_User_ID(), get_TrxName());
					if (p_EMailPDF)
					{
						if (to.get_ID() == 0)
						{
							addLog (entry.get_ID(), null, null, "@NotFound@: @AD_User_ID@ - " + bp.getName());
							errors++;
							continue;
						}
						else if (to.getEMail() == null || to.getEMail().length() == 0)
						{
							addLog (entry.get_ID(), null, null, "@NotFound@: @EMail@ - " + to.getName());
							errors++;
							continue;
						}
					}
					//	query
					MQuery query = new MQuery("C_Dunning_Header_v");
					query.addRestriction("C_DunningRunEntry_ID", MQuery.EQUAL, 
						new Integer(entry.getC_DunningRunEntry_ID()));
		
					//	Engine
					PrintInfo info = new PrintInfo(
						bp.getName(),
						X_C_DunningRunEntry.Table_ID,
						entry.getC_DunningRunEntry_ID(),
						entry.getC_BPartner_ID());
					info.setDescription(bp.getName() + ", Amt=" + entry.getAmt());
					ReportEngine re = new ReportEngine(getCtx(), format, query, info);
					boolean printed = false;
					if (p_EMailPDF)
					{
						EMail email = client.createEMail(to.getEMail(), null, null);
						if (!email.isValid())
						{
							addLog (entry.get_ID(), null, null, 
								"@RequestActionEMailError@ Invalid EMail: " + to);
							errors++;
							continue;
						}
						mText.setUser(to);	//	variable context
						mText.setBPartner(bp);
						mText.setPO(entry);
						String message = mText.getMailText(true);
						if (mText.isHtml())
							email.setMessageHTML(mText.getMailHeader(), message);
						else
						{
							email.setSubject (mText.getMailHeader());
							email.setMessageText (message);
						}
						//
						File attachment = re.getPDF(File.createTempFile("Dunning", ".pdf"));
						log.fine(to + " - " + attachment);
						email.addAttachment(attachment);
						//
						String msg = email.send();
						MUserMail um = new MUserMail(mText, entry.getAD_User_ID(), email);
						um.save();
						if (msg.equals(EMail.SENT_OK))
						{
							addLog (entry.get_ID(), null, null,
								bp.getName() + " @RequestActionEMailOK@");
							count++;
							printed = true;
						}
						else
						{
							addLog (entry.get_ID(), null, null,
								bp.getName() + " @RequestActionEMailError@ " + msg);
							errors++;
						}
					}
					else
					{
						re.print ();
						count++;
						printed = true;
					}
					if (printed)
					{
						entry.setProcessed (true);
						entry.save ();
					}
		
				}	//	for all dunning letters
				if (errors==0) {
					run.setProcessed(true);
					run.save();
				}
		
		} //end of for loop
		
		if (p_EMailPDF)
			return "@Sent@=" + count + " - @Errors@=" + errors;
		return "@Printed@=" + count;
	}	//	doIt
	
	private List<MUser> getBillingContacts(int m_C_BPartner_ID)
	{
		List<MUser> contacts=new ArrayList<MUser>();
		String sql="SELECT COUNT(*) FROM AD_USER USR INNER JOIN AD_USER_ROLES USRROLE ON (USRROLE.AD_USER_ID=USR.AD_USER_ID) " +
				"INNER JOIN AD_ROLE ROLE ON (USRROLE.AD_ROLE_ID=ROLE.AD_ROLE_ID) WHERE USR.C_BPARTNER_ID = "+m_C_BPartner_ID +" AND USRROLE.ISACTIVE='Y' AND USR.ISACTIVE='Y' AND LOWER(ROLE.NAME)='billing contact'";
		int no=DB.getSQLValue(null, sql);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		if(no>0)
		{
			sql="SELECT DISTINCT USR.AD_USER_ID FROM AD_USER USR INNER JOIN AD_USER_ROLES USRROLE ON (USRROLE.AD_USER_ID=USR.AD_USER_ID) INNER JOIN AD_ROLE ROLE ON (USRROLE.AD_ROLE_ID=ROLE.AD_ROLE_ID) WHERE USR.C_BPARTNER_ID = ? AND USR.ISACTIVE='Y' AND LOWER(ROLE.NAME)='billing contact'  AND USRROLE.ISACTIVE='Y' AND USR.EMAIL IS NOT NULL";
			try
			{
				pstmt = DB.prepareStatement (sql, null);
				pstmt.setInt (1, m_C_BPartner_ID);
				//pstmt.setInt (2, m_AD_Role_ID);
				rs = pstmt.executeQuery ();
				while (rs.next ())
				{
					contacts.add(new MUser (getCtx(), rs.getInt(1), null));
				}
	 		}
			catch (Exception e)
			{
				log.log(Level.SEVERE, sql, e);
			}
			finally
			{
				DB.close(rs, pstmt);
				rs = null; pstmt = null;
			}
		}
		else
		{
			sql="SELECT DISTINCT USR.AD_USER_ID FROM AD_USER USR WHERE USR.ISACTIVE='Y' AND USR.C_BPARTNER_ID = ? AND USR.EMAIL IS NOT NULL";
			try
			{
				pstmt = DB.prepareStatement (sql, null);
				pstmt.setInt (1, m_C_BPartner_ID);
				rs = pstmt.executeQuery ();
				while (rs.next ())
				{
					contacts.add(new MUser (getCtx(), rs.getInt(1), null));
				}
	 		}
			catch (Exception e)
			{
				log.log(Level.SEVERE, sql, e);
			}
			finally
			{
				DB.close(rs, pstmt);
				rs = null; pstmt = null;
			}
		}
		return contacts;
	}
	
}	//	DunningPrint
