package org.compiere.wstore;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.ecs.xhtml.option;
import org.apache.ecs.xhtml.select;
import org.compiere.util.CLogger;
import org.compiere.util.HtmlCode;
import org.compiere.util.Util;
import org.compiere.util.WebUser;
import org.compiere.util.WebUtil;

import com.conversant.model.SIPAccount;

public class SIPDomainListTag  extends TagSupport
{
	public static final String DOMAIN_LIST_SELECT_NAME = "domain";
	public static final String ATTR_DOMAIN_LIST = "domainList";
	
	/**	Logging			**/
	private static CLogger log = CLogger.getCLogger(SIPDomainListTag.class);

	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 * 	@throws JspException
	 */
	public int doStartTag() throws JspException
	{	
		// Write
		JspWriter out = pageContext.getOut();
		getHtml(pageContext, WebUtil.getParameter((HttpServletRequest)pageContext.getRequest(), DOMAIN_LIST_SELECT_NAME), false).output(out);
		
		return (SKIP_BODY);
	}	// doStartTag
	
	/**
	 * 	End Tag - NOP
	 * 	@return EVAL_PAGE
	 * 	@throws JspException
	 */
	public int doEndTag() throws JspException
	{
		return EVAL_PAGE;
	}	//	doEndTag
	
	/**
	 * Gets HTML code
	 * 
	 * @param pageContext
	 * @return
	 */
	public static HtmlCode getHtml(PageContext pageContext, String previousSelectedDomain, boolean disabled)
	{
		// Get Select and Option elements
		option[] options = getDomainOptions(pageContext, previousSelectedDomain);
		
		select sel = new select(DOMAIN_LIST_SELECT_NAME, options);
		sel.setID(DOMAIN_LIST_SELECT_NAME);
		sel.setDisabled(disabled);
		
		// when no options
		if (options.length < 1)
		{
			sel.addElement(new option("No domains found").addElement(Util.maskHTML("No domains found")));
			sel.setDisabled(true);
		}
		
		// Assemble
		HtmlCode html = new HtmlCode();
		html.addElement(sel);

		return html;
	}
	
	/**
	 * Gets domains as an array of options
	 * @return
	 */
	private static option[] getDomainOptions(PageContext pageContext, String previousSelectedDomain)
	{	
		// Set up request and session objects
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		HttpSession session = request.getSession();
		WebUser wu = WebUser.get(request);
		
		ArrayList<String> domains = (ArrayList<String>)session.getAttribute(ATTR_DOMAIN_LIST);
		if (domains == null || domains.size() < 1)
		{
			domains = getDomainNames(); // TODO: Add method to get domain name list from somewhere? compiere? mysql?
			session.setAttribute(ATTR_DOMAIN_LIST, domains);
		}	
		
		// Create options
		option[] options = new option[domains.size()];
		for (int i = 0; i < domains.size(); i++)
		{
			String domain = domains.get(i);
			options[i] = new option(domain);
			options[i].addElement(Util.maskHTML(domain));
			if (previousSelectedDomain != null && previousSelectedDomain.length() > 0 && domain.equalsIgnoreCase(previousSelectedDomain))
				options[i].setSelected(true);
		}

		return options;
	}	// getTimezones
	
	public static ArrayList<String> getDomainNames()
	{
		ArrayList<String> domains = new ArrayList<String>();
		domains.add("conversant.co.nz");
		return domains;
	}
	
}	// SIPDomainListTag
