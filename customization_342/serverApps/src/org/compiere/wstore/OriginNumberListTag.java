package org.compiere.wstore;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.ecs.xhtml.label;
import org.apache.ecs.xhtml.option;
import org.apache.ecs.xhtml.select;
import org.compiere.util.CLogger;
import org.compiere.util.HtmlCode;
import org.compiere.util.Util;
import org.compiere.util.WebUser;

import com.conversant.db.SERConnector;
import com.conversant.model.SIPAccount;

public class OriginNumberListTag extends TagSupport
{
	public static final String ORIGIN_NUMBER_SELECT_NAME = "form.select.originNumber";
	
	/**	Logging			*/
	private static CLogger log = CLogger.getCLogger(OriginNumberListTag.class);
	
	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 * 	@throws JspException
	 */
	public int doStartTag() throws JspException
	{	
		// Get Select and Option elements
		option[] options = getOriginNumbers();
		select sel = new select(ORIGIN_NUMBER_SELECT_NAME, options);
		sel.setID(ORIGIN_NUMBER_SELECT_NAME);

		// Assemble
		HtmlCode html = new HtmlCode();
		html.addElement(new label(ORIGIN_NUMBER_SELECT_NAME, "O", "Origin Number:&nbsp;"));
		html.addElement(sel);
		
		// Write
		JspWriter out = pageContext.getOut();
		html.output(out);
		
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
	 * Gets origin numbers as options
	 * @return array of options
	 */
	private option[] getOriginNumbers()
	{	
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		WebUser wu = WebUser.get(request);
		
		if (wu != null && wu.isLoggedIn())
		{
			String selectedNumber = request.getParameter(ORIGIN_NUMBER_SELECT_NAME);			
			
			ArrayList<SIPAccount> sipAccounts = SERConnector.getSIPAccounts(wu.getC_BPartner_ID());
			
			if (sipAccounts.size() > 0)
			{
				// create options
				option[] options = new option[sipAccounts.size() + 1];
				options[0] = new option("-1").addElement(Util.maskHTML("Select number.."));
				
				boolean selectedSet = false;
				for (int i = 0; i < sipAccounts.size(); i++)
				{
					SIPAccount sipAccount = sipAccounts.get(i);
					options[i+1] = new option(sipAccount.getUsername()).addElement(Util.maskHTML(sipAccount.getUsername()));
										
					if (!selectedSet && selectedNumber != null && selectedNumber.equals(sipAccount.getUsername()))
					{
						options[i+1].setSelected(true);
						selectedSet = true;
					}	
				}
				
				return options;
			}
		}
		
		// Set no numbers found option
		option noNumbersFound = new option("-1").addElement("No numbers found");
		option[] options = new option[]{noNumbersFound};
		
		return options;
	
//			DIDCountry selectedCountry = (DIDCountry)request.getAttribute(DIDServlet.ATTR_SELECTED_COUNTRY);
//			boolean selectedSet = false; // to make sure only one option gets selected
//			for (int i = 1; i < countries.size(); i++)
//			{
//				DIDCountry country = countries.get(i-1);
//				options[i] = new option(country.getCountryCode() + "," + country.getCountryId() + "," + country.getDescription());
//				options[i].addElement(Util.maskHTML(country.getCountryCode() + " - " + country.getDescription()));
//				
//				if (!selectedSet && 
//					selectedCountry != null && 
//					selectedCountry.getCountryId().equalsIgnoreCase(country.getCountryId()) &&
//					selectedCountry.getCountryCode().equalsIgnoreCase(country.getCountryCode()))
//				{
//					options[i].setSelected(true);
//					selectedSet = true;
//				}
//			}
	}
}
