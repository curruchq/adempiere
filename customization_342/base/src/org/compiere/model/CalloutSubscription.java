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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.logging.*;
import org.compiere.util.*;


/**
 *	Bank Statement Callout	
 *	
 *  @author Jorg Janke
 *  @version $Id: CalloutBankStatement.java,v 1.3 2006/07/30 00:51:05 jjanke Exp $
 */
public class CalloutSubscription extends CalloutEngine
{
	public String bPartner (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		Integer C_BPartner_ID = (Integer)value;
		if (C_BPartner_ID == null || C_BPartner_ID.intValue() == 0)
			return "";
		MBPartner bp = new MBPartner(ctx, C_BPartner_ID.intValue(), null);
		long bpCreatedDays ;
		
		Timestamp currentDate = new Timestamp(System.currentTimeMillis());
		Date date = new Date(currentDate.getTime());
				
		Timestamp createdDate = bp.getCreated();
		Date bpCreatedDate = new Date(createdDate.getTime());
		 
		bpCreatedDays = date.getTime() - bpCreatedDate.getTime();
		int diffDays = (int)(bpCreatedDays / (24 * 60 * 60 * 1000));
		
		int diff = 0;
		if(bp.getSubscriptionDelay()>diffDays)
			diff = bp.getSubscriptionDelay()-diffDays;
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(currentDate.getTime());
		cal.add(Calendar.DAY_OF_MONTH, diff);
		Timestamp  startDate = new Timestamp(cal.getTime().getTime());
		
		cal.add(Calendar.YEAR , 214);
		Timestamp  paidUntilDate = new Timestamp(cal.getTime().getTime());
		
	    mTab.setValue("StartDate", startDate);
	    mTab.setValue("PaidUntilDate", paidUntilDate);
	    mTab.setValue("RenewalDate", paidUntilDate);
		
		return "";
	}	//	bPartner



}	//	CalloutSubscription
