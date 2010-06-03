package org.compiere.wstore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.ecs.xhtml.input;
import org.apache.ecs.xhtml.table;
import org.apache.ecs.xhtml.td;
import org.apache.ecs.xhtml.th;
import org.apache.ecs.xhtml.tr;
import org.compiere.model.MProduct;
import org.compiere.model.MSubscription;
import org.compiere.model.X_C_SubscriptionType;
import org.compiere.util.CLogMgt;
import org.compiere.util.CLogger;
import org.compiere.util.HtmlCode;
import org.compiere.util.WebInfo;
import org.compiere.util.WebUser;

import com.conversant.did.DIDUtil;

public class SubscriptionsTableTag extends TagSupport
{
	/**	Logger	*/
	private static CLogger log = CLogger.getCLogger (SubscriptionsTableTag.class);

	public static final String DEFAULT_JSP = "subscriptions.jsp";
	
	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 * 	@throws JspException
	 */
	public int doStartTag() throws JspException
	{
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		Properties ctx = JSPEnv.getCtx(request);	
		HttpSession session = pageContext.getSession();
		WebUser wu = (WebUser)session.getAttribute(WebUser.NAME);
		
		if (wu != null && wu.isLoggedIn())
		{
			if (ctx != null)
			{
				WebInfo info = (WebInfo)session.getAttribute(WebInfo.NAME);
				if (info == null || wu.getAD_User_ID() != info.getAD_User_ID())
				{
					info = new WebInfo (ctx, wu);
					session.setAttribute(WebInfo.NAME, info);
				}
			}
			
			HtmlCode html = new HtmlCode();
			
			// Set up table and header elements
			table table = new table();
			table.setClass("contentTable");

			table.addElement(new th("Subscription Name"));
			table.addElement(new th("Product Name"));
			table.addElement(new th("Subscription Type"));
			table.addElement(new th("Start Date").setColSpan(2));
		
			int count = 0;
			for (MSubscription subscription : MSubscription.getSubscriptions(ctx, null, wu.getC_BPartner_ID(), null))
			{
				tr tableRow = new tr();
		
				if (count % 2 == 0) 
					tableRow.setClass("evenRow");
				else 
					tableRow.setClass("oddRow");
				
				count++;
				
				MProduct product = MProduct.get(ctx, subscription.getM_Product_ID());
				String sipURI = DIDUtil.getSIPURI(ctx, product, null);
				String didNumber = DIDUtil.getDIDNumber(ctx, product, null);
				String mailboxNumber = DIDUtil.getVoicemailMailboxNumber(ctx, product, null);
				if (sipURI != null || (didNumber != null && didNumber.length() > 0) || mailboxNumber != null)
				{
					X_C_SubscriptionType subType = new X_C_SubscriptionType(ctx, subscription.getC_SubscriptionType_ID(), null);
					
					tableRow.addElement(new td(subscription.getName()));
					tableRow.addElement(new td(product.getName()));
					tableRow.addElement(new td(subType == null ? "" : subType.getDescription()));

					SimpleDateFormat format = new SimpleDateFormat("dd-MMMM-yyyy");
					td startDate = new td(format.format(new Date(subscription.getStartDate().getTime())));
					
					td editSIPAccount = null;
					if (sipURI != null)	
					{
						String username = sipURI.substring(0, sipURI.indexOf("@"));
						String domain = sipURI.substring(sipURI.indexOf("@") + 1, sipURI.length());
				
						input editBtn = new input("button", "form.button.edit", "Edit SIP Account");
						editBtn.setOnClick("window.location.href='/" + SIPServlet.EDIT_ACCOUNT_JSP + "?username=" + username + "&domain=" + domain + "'");
						editSIPAccount = new td(editBtn).setAlign("center");
					}
					else
						startDate.setColSpan(2);
					
					tableRow.addElement(startDate);
					tableRow.addElement(editSIPAccount);
					
					table.addElement(tableRow);
				}
			}
			
			// No subscriptions found
			if (count == 0)
			{
				table.addElement(new tr().addElement(new td("No subscriptions found").setColSpan(6)));
			}
			
			html.addElement(table);
			
			JspWriter out = pageContext.getOut();
			html.output(out);
		}
		else
		{
			if (CLogMgt.isLevelFiner())
				log.fine("No WebUser");
			if (session.getAttribute(WebInfo.NAME) == null)
				session.setAttribute (WebInfo.NAME, WebInfo.getGeneral());
		}
		
		return SKIP_BODY;
	}	// doStartTag

	/**
	 * 	End Tag
	 * 	@return EVAL_PAGE
	 * 	@throws JspException
	 */
	public int doEndTag() throws JspException
	{
		return EVAL_PAGE;
	}	// doEndTag
}
