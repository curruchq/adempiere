package org.compiere.wstore;

import java.util.ArrayList;
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
import org.compiere.util.CLogMgt;
import org.compiere.util.CLogger;
import org.compiere.util.HtmlCode;
import org.compiere.util.WebInfo;
import org.compiere.util.WebUser;

import com.conversant.db.SERConnector;
import com.conversant.model.SIPAccount;

public class SIPAccountsTableTag extends TagSupport
{
	/**	Logger	*/
	private static CLogger log = CLogger.getCLogger (SIPAccountsTableTag.class);
	
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
			
			ArrayList<SIPAccount> accounts = SERConnector.getSIPAccounts(wu.getBpartnerID());
			
//			String didNumber = WebUtil.getParameter(request, "didNumber");
			
			HtmlCode html = new HtmlCode();
				
			// Set up table and header elements
			table table = new table();
			table.setClass("contentTable");

			table.addElement(new th("Username"));
			table.addElement(new th("Domain").setColSpan(2));
			
			int count = 0;
			for (SIPAccount account : accounts)
			{
				tr tableRow = new tr();
				
				if (count % 2 == 0) 
					tableRow.setClass("evenRow");
				else 
					tableRow.setClass("oddRow");
				
				count++;
				
				tableRow.addElement(new td(account.getUsername()));
				tableRow.addElement(new td(account.getDomain()));
				
				td buttonCell = new td().setAlign("center");
				
				String params = "id=" + account.getId() + "&username=" + account.getUsername() + "&domain=" + account.getDomain();// + (didNumber != null && didNumber.length() > 0 ? "&didNumber=" + didNumber : "");

/**************************************************************************
				// only show set button when didNumber is specified
				if (didNumber != null && didNumber.length() > 0)
				{
					input selectBtn = new input("button", "Set_" + account.getUsername(), "Set");
					selectBtn.setOnClick("window.location.href='/" + SIPServlet.NAME + "?action=set&" + params + "'");
					buttonCell.addElement(selectBtn);
				}
***************************************************************************/
				
				input editBtn = new input("button", "Edit_" + account.getUsername(), "Edit");
				editBtn.setOnClick("window.location.href='/" + SIPServlet.EDIT_ACCOUNT_JSP + "?" + params + "'");
				buttonCell.addElement(editBtn);
				
				tableRow.addElement(buttonCell);
				
				table.addElement(tableRow);
			}
			
			// No SIP accounts found
			if (count == 0)
			{
				table.addElement(new tr().addElement(new td("No SIP accounts were found").setColSpan(3)));
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
		
		return (SKIP_BODY);
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
