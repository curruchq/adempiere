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
package org.compiere.wstore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.WebSessionCtx;

import com.conversant.did.DIDUtil;


/**
 *  Web Basket
 *
 *  @author Jorg Janke
 *  @author Josh Hill
 *  @version $Id: WebBasket.java,v 1.2 2006/07/30 00:53:21 jjanke Exp $
 */
public class WebBasket
{
	/**
	 * 	Constructor
	 */
	public WebBasket(Properties ctx)
	{
		this.ctx = ctx;
	}	//	WebBasket

	/** Context (JH)						*/
	private Properties ctx = null;

	/**	Attribute Name - also in JSPs	*/
	public static final String NAME = "webBasket";

	/**	Logging						*/
	private static CLogger log = CLogger.getCLogger(WebBasket.class);
	/**	Lines						*/
	private ArrayList<WebBasketLine>	m_lines = new ArrayList<WebBasketLine>();
	/** Total w/o tax				*/
	private BigDecimal		m_total;
	/**	Line (max) counter			*/
	private int				m_lineNo = 0;
	private int 			m_PriceList_Version_ID = -1;
	private int 			m_PriceList_ID = -1;
	/**	Sales Rep					*/
	private int 			m_SalesRep_ID = 0;

	/**
	 * 	String Representation
	 * 	@return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("WebBasket[Lines=");
		sb.append(m_lines.size()).append(",Total=").append(m_total)
			.append(",M_PriceList_ID=" + m_PriceList_ID)
			.append("]");
		return sb.toString();
	}	//	toString

	/**
	 * 	Get Total
	 *	@return	total
	 */
	public BigDecimal getTotal ()
	{
		return getTotal(false);
	}	//	getTotal

	/**
	 * 	Get (recalculated) Total
	 *	@return	total
	 */
	public BigDecimal getTotal (boolean recalc)
	{
		if (recalc)
		{
			m_total = Env.ZERO;
			for (int i = 0; i < m_lines.size(); i++)
			{
				WebBasketLine wbl = (WebBasketLine)m_lines.get(i);
				m_total = m_total.add(wbl.getTotal());
			}
		}
		if (m_total == null)
			return Env.ZERO;
		return m_total;
	}	//	getTotal

	/**
	 * 	Get Line Count
	 *	@return line count
	 */
	public int getLineCount()
	{
		return m_lines.size();
	}	//	getLineCount

	/**
	 *	Get Lines
	 * 	@return lines
	 */
	public ArrayList getLines()
	{
		return m_lines;
	}	//	getLines

	/**
	 * 	Add Line
	 *	@param wbl line
	 *	@return added line
	 */
	public WebBasketLine add (WebBasketLine wbl)
	{
		wbl.setLine (m_lineNo++);
		m_lines.add (wbl);
		getTotal (true);
		return wbl;
	}	//	add
	
	/**
	 * 	Add Line.
	 * 	Adds qty to the line, if same product 
	 * 	@param M_Product_ID product
	 * 	@param Name Name
	 *	@param Qty Qty
	 * 	@param Price Price
	 *	@return created / modified line
	 */	
	public WebBasketLine add (int M_Product_ID, String Name, BigDecimal Qty, BigDecimal Price)
	{
		//	try adding to existing line
		for (int i = 0; i < m_lines.size(); i++)
		{
			WebBasketLine wbl = (WebBasketLine)m_lines.get(i);
			if (wbl.getM_Product_ID() == M_Product_ID)
			{
				wbl.addQuantity (Qty);
				getTotal (true);
				return wbl;
			}
		}
		//	new line
		WebBasketLine wbl = new WebBasketLine (M_Product_ID, Name, Qty,	Price);
		return add (wbl);
	}	//	add


	/**
	 * 	Delete Line
	 *	@param no line no
	 */
	public void delete(int no)
	{
		for (int i = 0; i < m_lines.size(); i++)
		{
			WebBasketLine wbl = (WebBasketLine)m_lines.get(i);
			if (wbl.getLine() == no)
			{
				// Remove other product in pair if DID product - JH
				WebBasketLine removedLine = m_lines.remove(i);
				removeDIDPair(removedLine.getM_Product_ID());
				getTotal(true);
				break;
			}
		}
	}	//	delete

	/**
	 * Delete a line representing a particular product - JH 25/03/2010
	 * 
	 * @param M_Product_ID
	 * @return true if deleted
	 */
	public boolean deleteByProductId(int M_Product_ID)
	{
		for (int i = 0; i < m_lines.size(); i++)
		{
			WebBasketLine webBasketLine = (WebBasketLine)m_lines.get(i);
			
			if (webBasketLine.getM_Product_ID() > 0 &&  webBasketLine.getM_Product_ID() == M_Product_ID)
			{
				m_lines.remove(i);
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Remove pair product of specific DID product
	 * @param M_Product_ID
	 */
	public void removeDIDPair(int M_Product_ID)
	{
		String didNumber = DIDUtil.getDIDNumber(ctx, MProduct.get(ctx, M_Product_ID), null);
		if (didNumber != null && didNumber.length() > 0)
		{
			for (int i = m_lines.size()-1; i >= 0; i--)
			{
				WebBasketLine line = (WebBasketLine)m_lines.get(i);
				String lineDIDNumber = DIDUtil.getDIDNumber(ctx, MProduct.get(ctx, line.getM_Product_ID()), null);
				if (lineDIDNumber != null && lineDIDNumber.equalsIgnoreCase(didNumber))
				{
					m_lines.remove(i);
				}
			}
		}
	}	//	removeDIDPair

	public int getM_PriceList_Version_ID()
	{
		return m_PriceList_Version_ID;
	}
	public void setM_PriceList_Version_ID(int PriceList_Version_ID)
	{
		if (PriceList_Version_ID > 0)
			m_PriceList_Version_ID = PriceList_Version_ID;
	}
	
	
	public int getM_PriceList_ID()
	{
		return m_PriceList_ID;
	}
	public void setM_PriceList_ID(int PriceList_ID)
	{
		if (PriceList_ID > 0)
			m_PriceList_ID = PriceList_ID;
	}

	
	/**
	 * @return Returns the salesRep_ID.
	 */
	public int getSalesRep_ID ()
	{
		return m_SalesRep_ID;
	}
	/**
	 * @param salesRep_ID The salesRep_ID to set.
	 */
	public void setSalesRep_ID (int salesRep_ID)
	{
		m_SalesRep_ID = salesRep_ID;
	}
	
	// TODO: pass in invalid productIds not DID numbers
	public static void recreateWB(HttpServletRequest request, MOrder order, ArrayList<String> invalidDIDs)
	{
		log.info("Forced to VOID order which contained invalid or sold DID, OrderID=" + order.get_ID());
		log.info("Adding valid DIDs and all other products to Basket so that customer can create a new order");
		
		Properties ctx = JSPEnv.getCtx(request);
		HttpSession session = request.getSession(true);
		WebBasket wb = (WebBasket)session.getAttribute(WebBasket.NAME);
		if (wb == null)
			wb = new WebBasket(ctx);
		session.setAttribute(WebBasket.NAME, wb);
		
		for (MOrderLine ol : order.getLines())
		{
			MProduct product = ol.getProduct();
			if (product != null)
			{
				String didNumber = DIDUtil.getDIDNumber(ctx, product, null);
				if (didNumber == null || !invalidDIDs.contains(didNumber))
					wb.add(product.get_ID(), product.getName(), Env.ONE, ol.getPriceActual());
			}
		}
		
		OrderServlet.processOrder("VO", order); // void order

		session.setAttribute(WebSessionCtx.HDR_MESSAGE, "Invalid DIDs removed, please create a new order via your Basket.");
	}	//	recreateWB
	
}	//	WebBasket
