package org.compiere.wstore;

import java.util.ArrayList;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.ecs.xhtml.br;
import org.apache.ecs.xhtml.div;
import org.compiere.util.CLogMgt;
import org.compiere.util.CLogger;
import org.compiere.util.HtmlCode;
import org.compiere.util.WebInfo;
import org.compiere.util.WebUser;

public class CreateSIPAccountLinkTag  extends TagSupport
{
	/**	Logger	*/
	private static CLogger log = CLogger.getCLogger (CreateSIPAccountLinkTag.class);

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
		WebUser wu = WebUser.get(request);
		
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
			
			String element = null;
			ArrayList<String> domains = SIPDomainListTag.getDomainNames();
//			String didNumber = WebUtil.getParameter(request, "didNumber");
//			if (didNumber != null && didNumber.length() > 0)
//			{	
//				for (SIPAccount account : MySQLConnector.getSIPAccounts(wu.getC_BPartner_ID()))
//				{
//					if (account.getUsername().equalsIgnoreCase(didNumber))
//					{
//						domains.remove(account.getDomain());
//					}
//				}
//			}
			
			if (domains.size() > 0)
			{
//				element = "<a href=\"/" + SIPServlet.CREATE_ACCOUNT_JSP + (didNumber != null && didNumber.length() > 0 ? "?didNumber=" + didNumber : "") + "\">Create SIP account</a>";
				element = "<input class=\"button\" type=\"button\" value=\"Create SIP Account\" onClick=\"window.location.href='" + SIPServlet.CREATE_ACCOUNT_JSP + /*(didNumber != null && didNumber.length() > 0 ? "?didNumber=" + didNumber : "") + */ "'\"/>";
			}
			
			if (element != null)
			{
				html.addElement(new div(element).setClass("createLink"));
				html.addElement(new br());

				JspWriter out = pageContext.getOut();
				html.output(out);
			}
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
