<%@ include file="/WEB-INF/jspf/page.jspf" %>
<%@page import="org.compiere.wstore.*"%>
<html>
<!--
- Author: Jorg Janke
- Version: $Id: login.jsp,v 1.2 2006/05/06 00:41:33 mdeaelfweald Exp $
- Compiere ERP & CRM Smart Business Solution - Copyright (c) 1999-2003 Jorg Janke
- - -
- Web Login
-->

<head>
    <%@ include file="/WEB-INF/jspf/head.jspf" %>
    <title><c:out value='${ctx.name}'/> - Login</title>
    
    <c:set var="REQUIRED_FIELD_NAMES" value="<%= ValidationServlet.REQUIRED_FIELD_NAMES %>"/>
	<c:set var="SUCCESS_FORWARD_URL" value="<%= ValidationServlet.SUCCESS_FORWARD_URL %>"/>
	<c:set var="REFERER_NAME" value="<%= ValidationServlet.REFERER_NAME %>"/>
	<c:set var="VALIDATION_SERVLET" value="<%= ValidationServlet.NAME %>"/>
	<c:set var="THIS_JSP" value="<%= LoginServlet.JSP_DEFAULT %>"/>
	<c:set var="LOGIN_SERVLET" value="<%= LoginServlet.NAME %>"/>
</head>

<body><div id="page">
    <%@ include file="/WEB-INF/jspf/header.jspf" %>
    <div id="main">
        <%@ include file="/WEB-INF/jspf/menu.jspf" %>
        <div id="content">
            <form action="<c:out value='${VALIDATION_SERVLET}' />" method="post" enctype="application/x-www-form-urlencoded" name="Login" target="_top">
                <fieldset>
                    <legend>Login</legend>

					<input 
						type="hidden" 
						name="<c:out value='${REQUIRED_FIELD_NAMES}' />" 
						id="<c:out value='${REQUIRED_FIELD_NAMES}' />" 
						value="EMail,Password"
					/>
					<input 
						type="hidden" 
						name="<c:out value='${REFERER_NAME}' />" 
						id="<c:out value='${REFERER_NAME}' />" 
						value="<c:out value='${THIS_JSP}' />"
					/>
					<input 
						type="hidden" 
						name="<c:out value='${SUCCESS_FORWARD_URL}' />" 
						id="<c:out value='${SUCCESS_FORWARD_URL}' />" 
						value="<c:out value='${LOGIN_SERVLET}' />"
					/>
		            <input name="AD_Client_ID" type="hidden" value='<c:out value="${initParam.#AD_Client_ID}" default="0"/>'/>
		            <input name="Source" type="hidden" value=""/>
		            <input name="Info" type="hidden" value=""/>
		            <input name="Mode" type="hidden" value="Login"/>		            		            
		            
		            <script language="Javascript">
		                document.Login.Source.value = document.referrer;
		                document.Login.Info.value = document.lastModified;
		            </script>
		
		            <div class="fieldDiv">
			            <label id="LBL_EMail" for="EMail"><cws:message txt="EMail"/></label>
			            <input 
			            	class="mandatory" 
			            	size="40" 
			            	id="ID_EMail" 			            	
			            	name="EMail" 
			            	maxlength="60" 
			            	type="text"
			            	<% 
								String value = request.getParameter("EMail");
								if (value != null && value.length() > 0)
								{
									out.println("value=\"" + value + "\"");
								}
								else
								{ 
									org.compiere.util.WebUser wu = org.compiere.util.WebUser.get(request);
									if (wu != null && wu.getEmail() != null)
										out.println("value=\"" + wu.getEmail() + "\"");
								}
							%>										            		
			            	/>   
			            <% 
							java.util.ArrayList invalidFields = (java.util.ArrayList)request.getAttribute(ValidationServlet.INVALID_FIELDS);
							if (invalidFields != null && invalidFields.contains("EMail")) 
								out.println("<span class=\"errorIcon\"></span>");
						%> 
			        </div>		            
					
					<div class="fieldDiv">
			            <label id="LBL_Password" for="Password"><cws:message txt="Password"/></label>
			            <input class="mandatory" size="40" type="password" id="ID_Password" value="" name="Password" maxlength="40"/>			           
		            	<% 							
							if (invalidFields != null && invalidFields.contains("Password")) 
								out.println("<span class=\"errorIcon\"></span>");
						%>
		            </div>
		
		            <div class="buttons">
		                <input type="submit" name="Login" id="Login" value="Login">
		                <input type="reset" name="Reset" value="Reset">		   
		            </div>
		            
		            <div align="center">
		            	Enter all <b class="mandatory">mandatory</b> data. 
		            </div>
                </fieldset>
            </form>
        </div>
    </div>
    <%@ include file="/WEB-INF/jspf/footer.jspf" %>
</div>
</body>
</html>
