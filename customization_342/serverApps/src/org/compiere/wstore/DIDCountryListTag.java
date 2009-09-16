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
		ArrayList<DIDCountry> countries = (ArrayList<DIDCountry>)session.getAttribute(DIDServlet.ATTR_COUNTRYLIST);
		if (countries == null || countries.size() < 1)
		{
			// get list of countries
			countries = DIDXService.getDIDxCountries(ctx);
			
			// set county list in session for use later
			session.setAttribute(DIDServlet.ATTR_COUNTRYLIST, countries);
		}
		
		// create options
		option[] options = new option[countries.size() + 1];
		if (countries.size() > 0)
		{
			options[0] = new option("-1").addElement(Util.maskHTML("Select country.."));
			
			DIDCountry selectedCountry = (DIDCountry)request.getAttribute(DIDServlet.ATTR_SELECTED_COUNTRY);
			boolean selectedSet = false; // to make sure only one option gets selected
			for (int i = 1; i < countries.size(); i++)
			{
				DIDCountry country = countries.get(i-1);
				options[i] = new option(country.getCountryCode() + "," + country.getCountryId() + "," + country.getDescription());
				options[i].addElement(Util.maskHTML(country.getCountryCode() + " - " + country.getDescription()));
				
				if (!selectedSet && 
					selectedCountry != null && 
					selectedCountry.getCountryId().equalsIgnoreCase(country.getCountryId()) &&
					selectedCountry.getCountryCode().equalsIgnoreCase(country.getCountryCode()))
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
	
//	private ArrayList<DIDCountry> loadStaticCountryList()
//	{
//		ArrayList<DIDCountry> countries = new ArrayList<DIDCountry>();
//		countries.add(new DIDCountry("Argentina", "54", "9"));
//		countries.add(new DIDCountry("Australia", "61", "13"));
//		countries.add(new DIDCountry("Austria", "43", "223"));
//		countries.add(new DIDCountry("Bahrain", "973", "17"));
//		countries.add(new DIDCountry("Belgium", "32", "21"));
//		countries.add(new DIDCountry("Brazil", "55", "29"));
//		countries.add(new DIDCountry("Bulgaria", "359", "32"));
//		countries.add(new DIDCountry("Canada", "1", "37"));
//		countries.add(new DIDCountry("Chile", "56", "42"));
//		countries.add(new DIDCountry("China", "86", "43"));
//		countries.add(new DIDCountry("Colombia", "57", "46"));
//		countries.add(new DIDCountry("Cyprus", "357", "53"));
//		countries.add(new DIDCountry("Czech Republic", "420", "54"));
//		countries.add(new DIDCountry("Denmark", "45", "56"));
//		countries.add(new DIDCountry("Dominican Republic", "1", "224"));
//		countries.add(new DIDCountry("Finland", "358", "68"));
//		countries.add(new DIDCountry("France", "33", "69"));
//		countries.add(new DIDCountry("Georgia", "995", "74"));
//		countries.add(new DIDCountry("Germany", "49", "75"));
//		countries.add(new DIDCountry("Greece", "30", "78"));
//		countries.add(new DIDCountry("Guatemala", "502", "83"));
//		countries.add(new DIDCountry("Honduras", "504", "89"));
//		countries.add(new DIDCountry("Hong Kong", "852", "90"));
//		countries.add(new DIDCountry("Iran", "98", "99"));
//		countries.add(new DIDCountry("Ireland", "353", "101"));
//		countries.add(new DIDCountry("Israel", "972", "102"));
//		countries.add(new DIDCountry("Italy", "39", "103"));
//		countries.add(new DIDCountry("Jamaica", "1", "104"));
//		countries.add(new DIDCountry("Japan", "81", "105"));
//		countries.add(new DIDCountry("Latvia", "371", "114"));
//		countries.add(new DIDCountry("Luxembourg", "352", "121"));
//		countries.add(new DIDCountry("Malaysia", "60", "126"));
//		countries.add(new DIDCountry("Mexico", "52", "134"));
//		countries.add(new DIDCountry("Netherlands", "31", "144"));
//		countries.add(new DIDCountry("New Zealand", "64", "147"));
//		countries.add(new DIDCountry("Norway", "47", "153"));
//		countries.add(new DIDCountry("Pakistan", "92", "155"));
//		countries.add(new DIDCountry("Panama", "507", "157"));
//		countries.add(new DIDCountry("Peru", "51", "160"));
//		countries.add(new DIDCountry("Poland", "48", "162"));
//		countries.add(new DIDCountry("Romania", "40", "167"));
//		countries.add(new DIDCountry("Russia", "7", "168"));
//		countries.add(new DIDCountry("Singapore", "65", "181"));
//		countries.add(new DIDCountry("South Africa", "27", "185"));
//		countries.add(new DIDCountry("South Korea", "82", "110"));
//		countries.add(new DIDCountry("Spain", "34", "186"));
//		countries.add(new DIDCountry("Sweden", "46", "191"));
//		countries.add(new DIDCountry("Switzerland", "41", "192"));
//		countries.add(new DIDCountry("Thailand", "66", "198"));
//		countries.add(new DIDCountry("Turkey", "90", "203"));
//		countries.add(new DIDCountry("Ukraine", "380", "208"));
//		countries.add(new DIDCountry("United Kingdom", "44", "210"));
//		countries.add(new DIDCountry("USA", "1", "211"));
//		countries.add(new DIDCountry("Venezuela", "58", "215"));
//		
//		DIDCountry.sortCountriesByCode(countries, true);
//		
//		return countries;
//	}
	
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
