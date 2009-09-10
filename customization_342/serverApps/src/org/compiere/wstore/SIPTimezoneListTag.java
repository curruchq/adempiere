package org.compiere.wstore;

import java.util.ArrayList;
import java.util.Properties;

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
import org.compiere.util.WebUtil;

public class SIPTimezoneListTag  extends TagSupport
{
	public static final String TIMEZONE_LIST_SELECT_NAME = "timezone";
	public static final String ATTR_TIMEZONE_LIST = "timezoneList";
	
	/**	Logging	**/
	private static CLogger log = CLogger.getCLogger(SIPTimezoneListTag.class);

	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 * 	@throws JspException
	 */
	public int doStartTag() throws JspException
	{	
		// Write
		JspWriter out = pageContext.getOut();
		getHtml(pageContext, WebUtil.getParameter((HttpServletRequest)pageContext.getRequest(), TIMEZONE_LIST_SELECT_NAME)).output(out);
		
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
	public static HtmlCode getHtml(PageContext pageContext, String previousSelectedTimezone)
	{
		// Get Select and Option elements
		option[] options = getTimezones(pageContext, previousSelectedTimezone);
		select sel = new select(TIMEZONE_LIST_SELECT_NAME, options);
		sel.setID(TIMEZONE_LIST_SELECT_NAME);

		// when no options
		if (options.length < 1)
		{
			sel.addElement(new option("No timezones found").addElement(Util.maskHTML("No timezones found")));
			sel.setDisabled(true);
		}
		
		// Assemble
		HtmlCode html = new HtmlCode();
		html.addElement(sel);
		
		return html;
	}
	
	/**
	 * Gets C-Voice timezones as an array of options
	 * @return
	 */
	private static option[] getTimezones(PageContext pageContext, String previousSelectedTimezone)
	{	
		// Set up request and session objects
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		HttpSession session = request.getSession();
		
		ArrayList<String> timezones = (ArrayList<String>)session.getAttribute(ATTR_TIMEZONE_LIST);
		if (timezones == null || timezones.size() < 1)
		{
			timezones = SERConnector.getTimezones();
			session.setAttribute(ATTR_TIMEZONE_LIST, timezones);
		}

		// Create options
		String selected =  previousSelectedTimezone == null || previousSelectedTimezone.equals("") ? DIDConstants.DEFAULT_SIP_TIMEZONE : previousSelectedTimezone;
		option[] options = new option[timezones.size()];
		for (int i = 0; i < timezones.size(); i++)
		{
			String timezone = timezones.get(i);
			options[i] = new option(timezone);
			options[i].addElement(Util.maskHTML(timezone));
			if (timezone.contains(selected))
				options[i].setSelected(true);
		}

		return options;
	}	// getTimezones
	
}	// CVoiceTimezoneListTag
