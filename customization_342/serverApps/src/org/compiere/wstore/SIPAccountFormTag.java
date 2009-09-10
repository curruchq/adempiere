package org.compiere.wstore;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.ecs.Element;
import org.apache.ecs.xhtml.br;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.fieldset;
import org.apache.ecs.xhtml.form;
import org.apache.ecs.xhtml.input;
import org.apache.ecs.xhtml.label;
import org.apache.ecs.xhtml.legend;
import org.apache.ecs.xhtml.select;
import org.compiere.util.CLogger;
import org.compiere.util.HtmlCode;
import org.compiere.util.WebSessionCtx;
import org.compiere.util.WebUtil;

import com.conversant.model.SIPAccount;

public class SIPAccountFormTag  extends TagSupport
{
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger (SIPAccountFormTag.class);

	/** Default mode	*/
	private static final String DEFAULT_MODE = "create";
	
	/** Mode			*/
	private String mode = DEFAULT_MODE; 
	
	public String getMode()
	{
		return mode;
	}
	
	public void setMode(String mode)
	{
		if (mode == null || !mode.equalsIgnoreCase("edit"))
			this.mode = DEFAULT_MODE;
		else
			this.mode = mode;
	}
	
	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 * 	@throws JspException
	 */
	public int doStartTag() throws JspException
	{
		String modeText = "Create";
		
		if (mode.equalsIgnoreCase("edit"))
		{
			modeText = "Edit";
		}
		
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		
		String usernameValue = WebUtil.getParameter(request, "username");
		String domainValue = WebUtil.getParameter(request, SIPDomainListTag.DOMAIN_LIST_SELECT_NAME);
		String passwordValue = WebUtil.getParameter(request, "password");
		String confirmValue = WebUtil.getParameter(request, "confirm");
		String timezoneValue = WebUtil.getParameter(request, SIPTimezoneListTag.TIMEZONE_LIST_SELECT_NAME);
		
		SIPAccount sipAccount = null;
		String id = WebUtil.getParameter(request, "id");
		if (id != null && id.length() > 0)
		{
			try
			{
				int subscriberId = Integer.parseInt(id);
				sipAccount = SERConnector.getSIPAccount(subscriberId);
			}
			catch (Exception ex){}
		}
		else
		{
			sipAccount = SERConnector.getSIPAccount(usernameValue, domainValue);
		}
		
		HtmlCode html = new HtmlCode();
		
		if ((mode.equalsIgnoreCase("edit") && sipAccount != null) || (mode.equalsIgnoreCase("create")))
		{
			if (mode.equalsIgnoreCase("edit"))
			{
				if (usernameValue == null || usernameValue.length() < 1) 
					usernameValue = sipAccount.getUsername();
				
				if (domainValue == null || domainValue.length() < 1)
					domainValue = sipAccount.getDomain();
				
				if (timezoneValue == null || timezoneValue.length() < 1)
					timezoneValue = sipAccount.getTimezone();
			}		
			
			ArrayList<String> invalidFields = (ArrayList<String>)request.getAttribute(SIPServlet.INVALID_FIELDS);
	
			// dummy array to avoid null check on each element
			if (invalidFields == null) invalidFields = new ArrayList<String>();
			
			fieldset fieldset = new fieldset();
			fieldset.addElement(new legend(modeText + " SIP account"));		
			
			fieldset.addElement(getDiv("Phone number", true, 
					"Enter your telephone number including full international dialing prefix e.g. 12125556678 or 6474556677. Please note that this will become your SIP account name and will be required to connect to the Conversant SIP Service.", 
					new input("text", "username", usernameValue).setSize(20).setMaxlength(20).setTabindex(0).setDisabled(mode.equalsIgnoreCase("edit")), invalidFields.contains("username"), "Please enter a valid telephone number. Enter numbers only and do not include characters such as - or +."));
			
			fieldset.addElement(getDiv("Domain", true, 
					"Choose the domain you would like to register your DID with.", 
					SIPDomainListTag.getHtml(pageContext, domainValue, mode.equalsIgnoreCase("edit")), invalidFields.contains(SIPDomainListTag.DOMAIN_LIST_SELECT_NAME), "You must select a domain"));
			
			// disabled fields don't get set with request so add hidden fields
			if (mode.equalsIgnoreCase("edit"))
			{
				fieldset.addElement(new input("hidden", "username", usernameValue));
				fieldset.addElement(new input("hidden", SIPDomainListTag.DOMAIN_LIST_SELECT_NAME, domainValue));
			}
			
			ArrayList<Element> passwordFields = new ArrayList<Element>();
			passwordFields.add(new input("password", "password", passwordValue).setMaxlength(16));
			if (mode.equalsIgnoreCase("edit"))
				passwordFields.add(new div("(Leave blank to not change)").setClass("hint"));
			fieldset.addElement(getDiv("Password", true, 
					"Enter your password. The password must be between 8 and 16 characters long inclusive. This is the password that you will use to connect to the Conversant SIP Service. Note that this password is separate to your website login password.", 
					passwordFields.toArray(new Element[passwordFields.size()]), invalidFields.contains("password"), "Please enter a password with at least 8 characters and less than 16."));
						
			fieldset.addElement(getDiv("Confirm password", true, 
					"Re-enter your password. This must be the same password as entered in the previous field.", 
					new input("password", "confirm", confirmValue), invalidFields.contains("confirm"), "Please confirm the password entered in the previous field."));
			
			fieldset.addElement(getDiv("Timezone", true, 
					"Choose the timezone in which you are usually located. You may change this if you move to a different timezone.", 
					SIPTimezoneListTag.getHtml(pageContext, timezoneValue), invalidFields.contains(SIPTimezoneListTag.TIMEZONE_LIST_SELECT_NAME), "Please select the timezone your are usually located in."));
/*			
			WebSessionCtx wsc = WebSessionCtx.get(request);
			String terms = "terms.html";
			if (wsc != null && wsc.wstore != null && 
				wsc.wstore.getWebParam2() != null && 
				wsc.wstore.getWebParam2().length() > 0)
				terms = wsc.wstore.getWebParam2();
			
			div helpDiv = new div();
			helpDiv.setClass("formHelp");
			helpDiv.addElement(new input("checkbox", "terms", "terms").setChecked(WebUtil.getParameter(request, "terms") == null ? false : true));
			helpDiv.addElement("I have read and agree to the <a href=\"/" + terms + "\" target=\"_blank\">terms and conditions</a>.");
			fieldset.addElement(getDiv("Terms", true, null, helpDiv, invalidFields.contains("terms"), "You must accept our terms in order to use our services."));
*/
			if (mode.equalsIgnoreCase("edit") && sipAccount != null)
				fieldset.addElement(new input("hidden", "id", sipAccount.getId()));
			
			fieldset.addElement(new input("hidden", "action", mode == null || mode.length() < 1 ? "create" : mode));
			fieldset.addElement(new input("submit", "form.button." + mode + "Account", modeText.equalsIgnoreCase("Edit") ? "Save" : modeText));
			
			input cancelBtn = new input("button", "form.button.cancel", "Cancel");
			cancelBtn.setOnClick("window.location.href='/" + SIPServlet.DEFAULT_JSP /* + (didNumber != null && didNumber.length() > 0 ? "?didNumber=" + didNumber : "") */ + "'");
			fieldset.addElement(cancelBtn);
			
			form form = new form(SIPServlet.NAME, "post", "application/x-www-form-urlencoded");
			if (invalidFields.size() > 0 && request.getAttribute(SIPServlet.INFO_MSG) == null)
			{
				form.addElement(new div("Please correct the errors shown").setID("infoMsg"));
			}
			else if (request.getAttribute(SIPServlet.INFO_MSG) != null)
			{
				form.addElement(new div((String)request.getAttribute(SIPServlet.INFO_MSG)).setClass("infoMsg"));
			}
				
			form.addElement(fieldset);
					
			html.addElement(form);
			html.addElement(new br());
		}
		else
		{
			html.addElement("<h2>Could not find account to edit..</h2>");
			input backBtn = new input("button", "button.back", "Back");
			backBtn.setOnClick("window.location.href='/" + SIPServlet.DEFAULT_JSP + "'");
			html.addElement(backBtn);
		}
		
		JspWriter out = pageContext.getOut();
		html.output(out);
		
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
	
	private div getDiv(String labelText, boolean required, String helpText, Element field, boolean error, String errorMsg)
	{
		return getDiv(labelText, required, helpText, new Element[]{field}, error, errorMsg);
	}
	
	private div getDiv(String labelText, boolean required, String helpText, Element[] fields, boolean error, String errorMsg)
	{
		div div = new div();
		div.setClass("field");
		
		div.addElement(new label().addElement(labelText));	
		
		if (helpText != null)
		{
			div.addElement(new div(helpText).setClass("formHelp"));
		}
		
		if (error && required && errorMsg != null && errorMsg.length() > 0)
		{
			div.addElement(new div(errorMsg).setClass("errorText"));
		}
		
		for (Element field : fields)
		{			
			if (error && required)
				if (field instanceof input)
					((input)field).setClass("mandatory");
				else if (field instanceof select)
					((select)field).setClass("mandatory");
			
			div.addElement(field);
		}
		
		return div;
	}	// getDiv
}