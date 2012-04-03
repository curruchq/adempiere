package org.compiere.wstore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.ecs.Element;
import org.apache.ecs.ElementAttributes;
import org.apache.ecs.xhtml.br;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.fieldset;
import org.apache.ecs.xhtml.form;
import org.apache.ecs.xhtml.input;
import org.apache.ecs.xhtml.label;
import org.apache.ecs.xhtml.legend;
import org.apache.ecs.xhtml.option;
import org.apache.ecs.xhtml.select;
import org.apache.ecs.xhtml.span;
import org.compiere.model.MBPBankAccount;
import org.compiere.model.MPayment;
import org.compiere.util.CLogger;
import org.compiere.util.HtmlCode;
import org.compiere.util.ValueNamePair;
import org.compiere.util.WebUser;
import org.compiere.util.WebUtil;

public class PaymentInfoFormTag extends TagSupport
{
 	/**	Logger			*/
 	private static CLogger log = CLogger.getCLogger (PaymentInfoFormTag.class);
 	
 	private static final String DEFAULT_JSP = "paymentInfo.jsp";
 	
 	private static final String ID_PREFIX = "ID_";
 	
 	private static final int NUM_EXPIRY_YEARS = 15;
 	
 	private static final String CREDIT_CARD_SELECT_NAME = "CreditCard";
 	private static final String EXPIRY_MONTH_SELECT_NAME = "CreditCardExpMM";
 	private static final String EXPIRY_YEAR_SELECT_NAME = "CreditCardExpYY";
 	
 	/**
	 *  Start Tag
	 *  @return SKIP_BODY
	 * 	@throws JspException
	 */
	public int doStartTag() throws JspException
	{
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		HttpSession session = request.getSession(true);
		
		// get existing MPayment object 
		MPayment payment = (MPayment)session.getAttribute("payment");
		if (payment == null)
			payment = (MPayment)request.getAttribute("payment"); 
		
		// get invalid fields (if there are any)
		ArrayList<String> invalidFields = (ArrayList<String>)request.getAttribute(ValidationServlet.INVALID_FIELDS);

		// dummy array to avoid null check on each element
		if (invalidFields == null) invalidFields = new ArrayList<String>();
		
		// set up fieldset and legend
		fieldset fieldset = new fieldset();
		fieldset.setID("fieldsetPayment");
		fieldset.addElement(new legend("Payment Information"));
		
		// add info message div
		if (invalidFields.size() > 0)
			fieldset.addElement(new div("Please correct the errors shown").setID("infoMsg"));
		
		// add fields
		StringBuilder requiredFieldNames = new StringBuilder();
		fieldset.addElement(getDiv("Card Type", 
								   getCreditCardSelect(payment, WebUtil.getParameter(request, CREDIT_CARD_SELECT_NAME)),
								   true, invalidFields.contains(CREDIT_CARD_SELECT_NAME)));
		requiredFieldNames.append(CREDIT_CARD_SELECT_NAME + ValidationServlet.REQUIRED_FIELD_NAME_DELIM);
		
		String creditCardNumberValue = WebUtil.getParameter(request, "CreditCardNumber");
		if (creditCardNumberValue == null && payment != null)
			creditCardNumberValue = payment.getCreditCardNumber();
		fieldset.addElement(getDiv("Credit Card Number",  
								   new input("text", "CreditCardNumber", creditCardNumberValue).setSize(30).setMaxlength(16).setID(ID_PREFIX + "CreditCardNumber"), 
								   true, invalidFields.contains("CreditCardNumber")));
		requiredFieldNames.append("CreditCardNumber" + ValidationServlet.REQUIRED_FIELD_NAME_DELIM);
		
		fieldset.addElement(getDiv("Expiry Date",  
								   new Element[]{getExpiryMonthSelect(payment, WebUtil.getParameterAsInt(request, EXPIRY_MONTH_SELECT_NAME)), 
												 getExpiryYearSelect(payment, WebUtil.getParameterAsInt(request, EXPIRY_YEAR_SELECT_NAME))}, 
								   true, invalidFields.contains(EXPIRY_YEAR_SELECT_NAME) || invalidFields.contains(EXPIRY_MONTH_SELECT_NAME)));
		requiredFieldNames.append(EXPIRY_YEAR_SELECT_NAME + ValidationServlet.REQUIRED_FIELD_NAME_DELIM);
		requiredFieldNames.append(EXPIRY_MONTH_SELECT_NAME + ValidationServlet.REQUIRED_FIELD_NAME_DELIM);
		
		String validationCodeValue = WebUtil.getParameter(request, "CreditCardVV");
		if (validationCodeValue == null && payment != null)
			validationCodeValue = payment.getCreditCardVV();
		fieldset.addElement(getDiv("Validation Code",
								   new input("text", "CreditCardVV", validationCodeValue).setSize(5).setMaxlength(4).setID(ID_PREFIX + "CreditCardVV"),
								   true, invalidFields.contains("CreditCardVV")));
		requiredFieldNames.append("CreditCardVV" + ValidationServlet.REQUIRED_FIELD_NAME_DELIM);
		
		String nameValue = WebUtil.getParameter(request, "A_Name");
		if (nameValue == null && payment != null)
			nameValue = payment.getA_Name();
		fieldset.addElement(getDiv("Name on Credit Card",
								   new input("text", "A_Name", nameValue).setSize(40).setMaxlength(60).setID(ID_PREFIX + "A_Name"),
								   true, invalidFields.contains("A_Name")));
		requiredFieldNames.append("A_Name" + ValidationServlet.REQUIRED_FIELD_NAME_DELIM);
		
		String streetValue = WebUtil.getParameter(request, "A_Street");
		if (streetValue == null && payment != null)
			streetValue = payment.getA_Street();
		fieldset.addElement(getDiv("Street",
								   new input("text", "A_Street", streetValue).setSize(40).setMaxlength(60).setID(ID_PREFIX + "A_Street"),
								   true, invalidFields.contains("A_Street")));
		requiredFieldNames.append("A_Street" + ValidationServlet.REQUIRED_FIELD_NAME_DELIM);
		
		String cityValue = WebUtil.getParameter(request, "A_City");
		if (cityValue == null && payment != null)
			cityValue = payment.getA_City();
		fieldset.addElement(getDiv("City",
								   new input("text", "A_City", cityValue).setSize(40).setMaxlength(40).setID(ID_PREFIX + "A_City"),
								   true, invalidFields.contains("A_City")));
		requiredFieldNames.append("A_City" + ValidationServlet.REQUIRED_FIELD_NAME_DELIM);
		
		String zipValue = WebUtil.getParameter(request, "A_Zip");
		if (zipValue == null && payment != null)
			zipValue = payment.getA_Zip();
		fieldset.addElement(getDiv("Zip",
								   new input("text", "A_Zip", zipValue).setSize(10).setMaxlength(10).setID(ID_PREFIX + "A_Zip"),
								   false, invalidFields.contains("A_Zip")));
		
		String stateValue = WebUtil.getParameter(request, "A_State");
		if (stateValue == null && payment != null)
			stateValue = payment.getA_State();
		fieldset.addElement(getDiv("State",
								   new input("text", "A_State", stateValue).setSize(40).setMaxlength(20).setID(ID_PREFIX + "A_State"),
								   false, invalidFields.contains("A_State")));
		
		String countryValue = WebUtil.getParameter(request, "A_Country");
		if (countryValue == null && payment != null)
			countryValue = payment.getA_Country();
		fieldset.addElement(getDiv("Country",
								   new input("text", "A_Country", countryValue).setSize(40).setMaxlength(20).setID(ID_PREFIX + "A_Country"),
								   false, invalidFields.contains("A_Country")));
		
		// add save payment checkbox
		input savePaymentCheckBox = new input("checkbox", "SavePayment", "SavePayment");
		savePaymentCheckBox.setID("SavePayment");
		savePaymentCheckBox.setChecked(true);
		savePaymentCheckBox.addElement("Save Payment Information");
		fieldset.addElement(((div)new div().addAttribute("align", "center")).addElement(savePaymentCheckBox));
		
		// add confirmation for charging customers credit card (credit card valistion) checkbox
		WebUser wu = WebUser.get(request);
		if (wu != null)
		{
			// if bank account doesn't exist of isn't validated
			MBPBankAccount ba = wu.getBPBankAccount();
			if (ba == null || !ba.isBP_CC_Validated())
			{
				input validationConfirmCheckBox = new input("checkbox", "validationConfirm", "validationConfirm");
				validationConfirmCheckBox.setID("validationConfirm");
				validationConfirmCheckBox.setChecked(false);
				validationConfirmCheckBox.addElement("Charge account");
				div validationConfirmDiv = (div)new div().addAttribute("align", "center");
				validationConfirmDiv.addElement(new span(
						"<br />" + 
						"<b>Credit card validation</b><br />" +
						"In order to prevent fraudulent activity we require you to validate your credit card.<br />" + 
                        "You will not be able to make outbound calls to the telephone network (PSTN) until <br />" +
                        "your credit card has been validated.<br />" + 
                        "<br />" +
                        "By ticking the 'Charge account' checkbox below you agree that Conversant Ltd<br />" + 
                        "will charge a random amount (less the $2.00) to your credit card. Please confirm the<br />" + 
                        "amount on your credit card statement and then complete the credit card validation<br />" + 
                        "process on our website (you will be sent an email with instructions). The amount charged<br />" + 
                        "will be credited to your Conversant account and will be offset against future charges.<br />"));
				
				if (invalidFields.contains("validationConfirm"))
					validationConfirmDiv.setClass("errorBackground");
				validationConfirmDiv.addElement(validationConfirmCheckBox);
				
				fieldset.addElement(validationConfirmDiv);
				fieldset.addElement(new br());
//				requiredFieldNames.append("validationConfirm");
			}
		}
		
		// add hidden fields
		fieldset.addElement(new input("hidden", ValidationServlet.REQUIRED_FIELD_NAMES, requiredFieldNames.toString()));
		fieldset.addElement(new input("hidden", ValidationServlet.REFERER_NAME, DEFAULT_JSP));
		fieldset.addElement(new input("hidden", ValidationServlet.SUCCESS_FORWARD_URL, "paymentServlet"));
		
		// create buttons
		div buttonsDiv = new div();
		buttonsDiv.setClass("buttons");
		buttonsDiv.addElement(new input("reset", "Reset", "Reset"));
		buttonsDiv.addElement(new input("submit", "Submit", "Submit Payment").setID("Submit"));

		// add buttons to fieldset
		fieldset.addElement(buttonsDiv);
		
		// display payment error message if exists
		if (payment != null && payment.getErrorMessage() != null && payment.getErrorMessage().length() > 0)
		{
			fieldset.addElement(new div(payment.getErrorMessage()).setClass("error"));
		}
		
		// create form & add fieldset
		form form = new form(ValidationServlet.NAME, "POST", "application/x-www-form-urlencoded");
		form.addElement(fieldset);

		// create html
		HtmlCode html = new HtmlCode();
		html.addElement(form);
		html.addElement(new br());
		
		// write to page
		JspWriter out = pageContext.getOut();
		html.output(out);
		
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
	
	private div getDiv(String labelText, Element field, boolean required, boolean error)
	{
		return getDiv(labelText, new Element[]{field}, required, error);
	}
	
	private div getDiv(String labelText, Element[] fields, boolean required, boolean error)
	{
		div div = new div();
		
		div.addElement(new label().addElement(labelText));

		for (Element field : fields)
		{
			if (required && error && field instanceof ElementAttributes)
				((ElementAttributes)field).setClass("errorBackground");
			
			div.addElement(field);
		}
		
		if (required)
			div.addElement(((span)new span().setClass("required")).addAttribute("title", "Required"));
		
		return div;
	}	// getDiv
	
	private select getCreditCardSelect(MPayment payment, String requestCreditCardSelected)
	{
		option[] options = null;
		if (payment != null && payment.getCreditCards() != null)
		{
			ValueNamePair[] cards = payment.getCreditCards();
			options = new option[payment.getCreditCards().length];
			boolean selected = false;
			int i = 0;
			for (ValueNamePair card : cards)
			{
				options[i] = new option(card.getValue()).addElement(card.getName());
				if ((requestCreditCardSelected != null && requestCreditCardSelected.equalsIgnoreCase(card.getValue())) ||
					(payment.getCreditCardType() != null && payment.getCreditCardType().equalsIgnoreCase(card.getValue())))
				{
					if (!selected)
						options[i].setSelected(true);
					selected = true;
				}
				i++;
			}
		}
		else
		{
			options = new option[]{new option("").addElement("None")};
		}
		
		select sel = new select(CREDIT_CARD_SELECT_NAME, options);
		sel.setID(ID_PREFIX + CREDIT_CARD_SELECT_NAME);
		sel.setSize(1);
		
		return sel;
	}

	private select getExpiryMonthSelect(MPayment payment, int requestSelectedMonth)
	{
		option[] options = new option[12];
		boolean selected = false;
		for (int month=1; month <= 12; month++)
		{
			options[month-1] = new option(Integer.toString(month)).addElement(Integer.toString(month));
			if ((requestSelectedMonth == month) || (payment != null && payment.getCreditCardExpMM() == month))
			{
				if (!selected)
					options[month-1].setSelected(true);
				selected = true;
			}
		}
		
		select sel = new select(EXPIRY_MONTH_SELECT_NAME, options);
		sel.setID(ID_PREFIX + EXPIRY_MONTH_SELECT_NAME);
		sel.setSize(1);
		
		return sel;
	}
	
	private select getExpiryYearSelect(MPayment payment, int requestSelectedYear)
	{
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		int year = cal.get(GregorianCalendar.YEAR) - 2000; 
		
		option[] options = new option[NUM_EXPIRY_YEARS];
		boolean selected = false;
		for (int i=0; i<NUM_EXPIRY_YEARS; i++)
		{
			options[i] = new option(Integer.toString(year)).addElement(Integer.toString(year));
			if ((requestSelectedYear == year) || (payment != null && payment.getCreditCardExpYY() == year))
			{
				if (!selected)
					options[i].setSelected(true);
				selected = true;
			}
			year++;
		}
		
		select sel = new select(EXPIRY_YEAR_SELECT_NAME, options);
		sel.setID(ID_PREFIX + EXPIRY_YEAR_SELECT_NAME);
		sel.setSize(1);
		
		return sel;
	}
}