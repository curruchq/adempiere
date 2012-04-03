package org.compiere.wstore;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.compiere.util.CLogger;
import org.compiere.util.WebEnv;
import org.compiere.util.WebUser;
import org.compiere.util.WebUtil;

public class ContractServlet extends HttpServlet
{
	/** Logger										*/
	private static CLogger log = CLogger.getCLogger(ContractServlet.class);
	
	/** Name 										*/
	public static final String SERVLET_NAME = "contract";
	
	/** Referer										*/
	public static final String REFERER_NAME = "refererName";
	
	/** Default JSP									*/
	public static final String JSP_DEFAULT = "contract.jsp";
	
	/** */
	public static final String USERS_NAME = "usersName";
	
	/** */
	public static final String FILE_NAME = "fileName";
	
	/**
	 * 	Initialize global variables
	 *  @param config servlet configuration
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WebEnv.initWeb(config))
			throw new ServletException("ContractServlet.init");
	} 	// init
	
	/**
	 * Get Servlet information
	 * @return Info
	 */
	public String getServletInfo()
	{
		return "Contract Servlet";
	}	//	getServletInfo
	
	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.fine("destroy");
	}   //  destroy
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		log.info("Get from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		handleRequest(request, response);
	}	// doGet

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		log.info("Post from " + request.getRemoteHost() + " - " + request.getRemoteAddr());
		handleRequest(request, response);
	}	// doPost

	/**
	 * Handle Request, both GET and POST
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		String name = WebUtil.getParameter(request, USERS_NAME);
		String filename = WebUtil.getParameter(request, FILE_NAME);
		
		if (name != null && name.length() > 0 && filename != null)
		{
			String ip = request.getRemoteAddr();
			
			// get root path of http://<hostname>/ e.g /Compiere2/jboss/server/compiere/deploy/compiereWebStore.war/
			String path = getServletContext().getRealPath("/");
	
			WebUser wu = WebUser.get(request);
			if (wu != null)
			{
				if (wu.addContractAttachment(name, ip, path, filename))
				{
					forwardToReferer(request, response);
					return;
				}
			}
		}
		else if (name == null || name.length() < 1)
		{
			request.setAttribute("infoMsg", "Please enter your full name..");
		}
		
		forward(request, response, JSP_DEFAULT);
	}
	
	private void forwardToReferer(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		String referer = WebUtil.getParameter(request, REFERER_NAME);
		forward(request, response, referer != null ? referer : "index.jsp");
	}
	
	private void forward(HttpServletRequest request, HttpServletResponse response, String url)
		throws ServletException, IOException
	{
		if (!url.startsWith("/")) url = "/" + url;
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
		dispatcher.forward(request, response);
	}
}