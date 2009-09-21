package org.compiere.wstore;

import java.util.ArrayList;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.ecs.xhtml.label;
import org.apache.ecs.xhtml.option;
import org.apache.ecs.xhtml.select;
import org.compiere.model.MDIDxCountry;
import org.compiere.util.CLogger;
import org.compiere.util.HtmlCode;
import org.compiere.util.Util;

import com.conversant.model.DIDCountry;

public class DIDCountryListTag  extends TagSupport
{
	public static final String COUNTRY_SELECT_NAME = "form.select.country";
	
	/**	Logging			*/
	private static CLogger log = CLogger.getCLogger(DIDCountryListTag.class);

	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 * 	@throws JspException
	 */
	public int doStartTag() throws JspException
	{	
		// Get Select and Option elements
		option[] options = getCountries();
		select sel = new select(COUNTRY_SELECT_NAME, options);
		sel.setID(COUNTRY_SELECT_NAME);

		// Assemble
		HtmlCode html = new HtmlCode();
		html.addElement(new label(COUNTRY_SELECT_NAME, "C", "Country:"));
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
	 * Gets Countries as options
	 * @return
	 */
	private option[] getCountries()
	{	
		// set up request and session objects
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		HttpSession session = request.getSession();

		Properties ctx = JSPEnv.getCtx(request);		
//		ArrayList<DIDCountry> countries = DIDXService.getDIDxCountries(ctx);//loadStaticCountryList();
		
		// get country list from session of query didx.net then set country list in session
		ArrayList<MDIDxCountry> countries = (ArrayList<MDIDxCountry>)session.getAttribute(DIDServlet.ATTR_COUNTRYLIST);
		if (countries == null || countries.size() < 1)
		{
			// get list of countries
			countries = MDIDxCountry.getCountries(ctx);
			
			// sort them
			MDIDxCountry.sortByCode(countries, true);
			
			// set county list in session for use later
			session.setAttribute(DIDServlet.ATTR_COUNTRYLIST, countries);
		}
		
		// create options
		option[] options = new option[countries.size() + 1];
		if (countries.size() > 0)
		{
			options[0] = new option("-1").addElement(Util.maskHTML("Select country.."));
			
			MDIDxCountry selectedCountry = (MDIDxCountry)request.getAttribute(DIDServlet.ATTR_SELECTED_COUNTRY);
			boolean selectedSet = false; // to make sure only one option gets selected
			for (int i = 1; i < countries.size(); i++)
			{
				MDIDxCountry country = countries.get(i-1);
				options[i] = new option(country.getDIDX_COUNTRY_CODE() + "," + country.getDIDX_COUNTRYID() + "," + country.getDIDX_COUNTRY_NAME());
				options[i].addElement(Util.maskHTML(country.getDIDX_COUNTRY_CODE() + " - " + country.getDIDX_COUNTRY_NAME()));
				
				if (!selectedSet && 
					selectedCountry != null && 
					selectedCountry.getDIDX_COUNTRYID() == country.getDIDX_COUNTRYID() &&
					selectedCountry.getDIDX_COUNTRY_CODE() == country.getDIDX_COUNTRY_CODE())
				{
					options[i].setSelected(true);
					selectedSet = true;
				}
			}
		}
		else
		{
			options[0] = new option("-1").addElement(Util.maskHTML("No countries found"));
		}

		return options;
	}
		
//	private void addLocalCountries(HttpServletRequest request, ArrayList<DIDCountry> existingCountries)
//	{
//		ArrayList<DIDCountry> localCountries = DIDController.loadLocalDIDCountries(request);
//		for (DIDCountry localCountry : localCountries)
//		{
//			boolean countryAlreadyExists = false;
//			for (DIDCountry existingCountry : existingCountries)
//			{
//				if ((localCountry.getCountryId() != null && localCountry.getCountryId().equals(existingCountry.getCountryId())) && 
//					(localCountry.getCountryCode() != null && localCountry.getCountryCode().equals(existingCountry.getCountryCode())))
//				{					
//					countryAlreadyExists = true;
//				}
//			}
//			if (!countryAlreadyExists)
//			{
//				existingCountries.add(localCountry);
//			}
//		}
//		
//		// sort
//		DIDCountry.sortCountriesByDescription(existingCountries, true);
//	}
}
