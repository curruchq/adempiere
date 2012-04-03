package org.compiere.wstore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.compiere.util.CLogger;
import org.compiere.util.WebEnv;
import org.compiere.util.WebUtil;

public class ValidationServlet extends HttpServlet
{
	/** Logger												*/
	private static CLogger log = CLogger.getCLogger(ValidationServlet.class);
	
	/** Name used in Servlet-Mapping in web.xml				*/
	public static final String NAME = "validationServlet";
	
	/** Referer												*/
	public static final String REFERER_NAME = "refererName";
	
	/** Required field names request parameter				*/
	public static final String REQUIRED_FIELD_NAMES = "requiredFieldNames";
	
	/** Required field names delimeter						*/
	public static final String REQUIRED_FIELD_NAME_DELIM = ",";
	
	/** All fields are required identifier				 	*/
	public static final String ALL_FIELDS = "*";
	
	/** URL to forward to on success request parameter 		*/
	public static final String SUCCESS_FORWARD_URL = "successForwardURL";
	
	/** Invalid fields attribute							*/
	public static final String INVALID_FIELDS = "invalidFields";
	
	/**
	 * 	Initialize global variables
	 *  @param config servlet configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WebEnv.initWeb(config))
			throw new ServletException("ValidationServlet.init");
	} 	// init
	
	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Validation Servlet";
	}	// getServletInfo
	
	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.fine("destroy");
	}   // destroy
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		log.info("Get from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		this.handleRequest(request, response);
	}	// doGet

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		log.info("Post from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		this.handleRequest(request, response);
	}	// doPost

	private void handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{	
		// check required field names have been set
		String requiredFieldNames = WebUtil.getParameter(request, REQUIRED_FIELD_NAMES);
		if (requiredFieldNames == null || requiredFieldNames.length() < 1)
		{
			forwardToSuccessURL(request, response);
			return;
		}
		
		// if all fields load field names which start with "form."
		if (requiredFieldNames.equalsIgnoreCase(ALL_FIELDS))
		{
			StringBuilder fieldNames = new StringBuilder();
			Enumeration paramNames = request.getParameterNames();
		    boolean first = true;
			while(paramNames.hasMoreElements()) 
		    {
		    	String name = (String)paramNames.nextElement();
		    	if (name.startsWith("form."))
		    	{
		    		if (!first)
		    			fieldNames.append(REQUIRED_FIELD_NAME_DELIM);
		    		else
		    			first = false;
		    		
		    		fieldNames.append(name);		
		    	}	
		    }
			
			requiredFieldNames = fieldNames.toString();
		}
		
		// tokenize the required field names
		ArrayList<String> fieldNames = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(requiredFieldNames, REQUIRED_FIELD_NAME_DELIM);
		while (st.hasMoreTokens())
		{
			String name = st.nextToken();
			if (!fieldNames.contains(name))
				fieldNames.add(name);
		}
		
		// get each field from request and check value is not null and not empty
		ArrayList<String> invalidFields = new ArrayList<String>();
		for (String fieldName : fieldNames)
		{
			String fieldValue = WebUtil.getParameter(request, fieldName);
			if (fieldValue == null || fieldValue.length() < 1)
				invalidFields.add(fieldName);
		}
		
		// set even if empty
		request.setAttribute(INVALID_FIELDS, invalidFields);
		
		// forward back to referer if there are any errors
		if (invalidFields.size() > 0)
		{
			forwardToReferer(request, response);
		}
		// forward to the supplied success URL 
		else
			forwardToSuccessURL(request, response);
	}
	
	private void forwardToReferer(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		String referer = WebUtil.getParameter(request, REFERER_NAME);
		forward(request, response, referer != null ? referer : "index.jsp");
	}
	
	private void forwardToSuccessURL(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		String successURL = WebUtil.getParameter(request, SUCCESS_FORWARD_URL);
		if (successURL != null && successURL.length() > 0)
			forward(request, response, successURL);
		else
			forwardToReferer(request, response);
	}
	
	private void forward(HttpServletRequest request, HttpServletResponse response, String url)
		throws ServletException, IOException
	{
		if (!url.startsWith("/")) url = "/" + url;
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
	}
}
