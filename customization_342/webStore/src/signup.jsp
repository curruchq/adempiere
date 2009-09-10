<%@ include file="/WEB-INF/jspf/page.jspf" %>
<c:if test='${not empty webUser && webUser.loggedIn}'>
  <c:redirect url='/update.jsp'/>
</c:if>
<%@page import="org.compiere.wstore.*"%>
<html>
<!--
  - Author:  Josh Hill
  - Version: $Id: signup.jsp,v 1.0 2008/04/22 14:24:14 Exp $
  - Compiere ERP & CRM Smart Business Solution - Copyright (c) 1999-2003 Jorg Janke
  - - -
  - Web Store - Signup 
  -->
<head>
	<%@ include file="/WEB-INF/jspf/head.jspf" %>  
	<title><c:out value='${ctx.name}'/> - Signup</title>

	<c:set var="REQUIRED_FIELD_NAMES" value="<%= ValidationServlet.REQUIRED_FIELD_NAMES %>"/>
	<c:set var="SUCCESS_FORWARD_URL" value="<%= ValidationServlet.SUCCESS_FORWARD_URL %>"/>
	<c:set var="REFERER_NAME" value="<%= ValidationServlet.REFERER_NAME %>"/>
	<c:set var="VALIDATION_SERVLET" value="<%= ValidationServlet.NAME %>"/>
	<c:set var="THIS_JSP" value="<%= LoginServlet.JSP_SIGNUP %>"/>
	<c:set var="LOGIN_SERVLET" value="<%= LoginServlet.NAME %>"/>
	
	<script type="text/javascript">
		function checkPassword()
		{
			if(document.Signup.Password.value != document.Signup.PasswordNew.value)
            {
                alert("Password and Confirm Password do not match.");
                return false;
            }
		}
	</script>
	
</head>
<body>
	<div id="page">
		<%@ include file="/WEB-INF/jspf/header.jspf" %>
		<div id="main">
			<%@ include file="/WEB-INF/jspf/menu.jspf" %>
			<div id="content"> 
				<c:if test='${not empty infoMsg}'>
					<div id="infoMsg">
						<c:out value="${infoMsg}"/>
					</div>
				</c:if>
				<c:if test="${not empty webUser.saveErrorMessage}">
	                <div class="error">
	                	<c:out value="${webUser.saveErrorMessage}"/>
	                </div>
		        </c:if>
				<form action="<c:out value='${VALIDATION_SERVLET}' />" method="post" enctype="application/x-www-form-urlencoded" name="Signup" target="_top" onSubmit="return checkPassword();">
	                <fieldset>
	                	<legend>Signup</legend>
	                	
	                	<input 
							type="hidden" 
							name="<c:out value='${REQUIRED_FIELD_NAMES}' />" 
							id="<c:out value='${REQUIRED_FIELD_NAMES}' />" 
							value="EMail,Password,PasswordNew,Name,Address,City,Postal,C_Country_ID,<%=CaptchaServlet.CAPTCHA_NAME %>"
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
	                	<input name="Source" type="hidden" value=""/>
		            	<input name="Info" type="hidden" value=""/>
	                	<input name="Mode" type="hidden" value="Submit"/>
	                	<input name="AD_Client_ID" type="hidden" value='<c:out value="${initParam.#AD_Client_ID}" default="0"/>'/>
	                	<input name="AddressConfirm" type="hidden" id="AddressConfirm" value="N">
	                	
	                	<script language="Javascript">
			                document.Signup.Source.value = document.referrer;
			                document.Signup.Info.value = document.lastModified;
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
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>
							/>
							<% 
								java.util.ArrayList invalidFields = (java.util.ArrayList)request.getAttribute(ValidationServlet.INVALID_FIELDS);
								if (invalidFields != null && invalidFields.contains("EMail")) 
									out.println("<span class=\"errorIcon\">&nbsp;</span>");
							%> 
				        </div>
				        
				        <div class="fieldDiv">
				            <label id="LBL_Password" for="Password"><cws:message txt="Password"/></label>
				            <input 
				            	class="mandatory" 
				            	size="20" 
				            	type="password" 
				            	id="ID_Password" 
				            	name="Password" 
				            	maxlength="40"
				            	<% 
									value = request.getParameter("Password");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>	
							/>
							<% 
								if (invalidFields != null && invalidFields.contains("Password")) 
									out.println("<span class=\"errorIcon\">&nbsp;</span>");
							%>				         
			            </div>
				        
				        <div class="fieldDiv">
			                <label id="LBL_PasswordNew" for="PasswordNew">Confirm Password</label>
			                <input
								class="mandatory" 
								size="20" 
								id="ID_PasswordNew" 
								name="PasswordNew"
								maxlength="40" 
								type="password"
								<% 
									value = request.getParameter("PasswordNew");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>	
							/>
							<% 
								if (invalidFields != null && invalidFields.contains("PasswordNew")) 
									out.println("<span class=\"errorIcon\">&nbsp;</span>");
							%>
		                </div>
		
						<div class="fieldDiv">
			                <label id="LBL_Name" for="Name">Name</label>
			                <input 
			                	class="mandatory" 
			                	size="40" 
			                	id="ID_Name"
			                	name="Name" 
			                	maxlength="60" 
			                	type="text"
			                	<% 
									value = request.getParameter("Name");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>	
							/>
							<% 
								if (invalidFields != null && invalidFields.contains("Name")) 
									out.println("<span class=\"errorIcon\">&nbsp;</span>");
							%>
		                </div>
		
						<div class="fieldDiv">
			                <label id="LBL_Company" for="Company">Company</label>
			                <input 
			                	size="40" 
			                	id="ID_Company"
			                	name="Company" 
			                	maxlength="60" 
			                	type="text"
			                	<% 
									value = request.getParameter("Company");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>	
							/>
		                </div>
		
						<div class="fieldDiv">
			                <label id="LBL_Title" for="Title">Title</label>
			                <input 
			                	size="40" 
			                	id="ID_Title" 
			                	name="Title" 
			                	maxlength="60" 
			                	type="text"
			                	<% 
									value = request.getParameter("Title");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>	
							/>
		                </div>
		
						<div class="fieldDiv">
			                <label id="LBL_Address" for="Address">Address</label>
			                <input 
			                	class="mandatory" 
			                	size="40" 
			                	id="ID_Address" 
			                	name="Address" 
			                	maxlength="60" 
			                	type="text"
			                	<% 
									value = request.getParameter("Address");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>	
							/>
							<% 
								if (invalidFields != null && invalidFields.contains("Address")) 
									out.println("<span class=\"errorIcon\">&nbsp;</span>");
							%>
		                </div>
		
						<div class="fieldDiv">
			                <label id="LBL_Address2" for="Address2">Address2</label>
			                <input 
			                	size="40" 
			                	id="ID_Address2" 
			                	name="Address2" 
			                	maxlength="60" 
			                	type="text"
			                	<% 
									value = request.getParameter("Address2");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>	
							/>
		                </div>
		
		                <cws:location countryID='${webUser.countryID}' regionID='${webUser.regionID}' regionName='${webUser.regionName}'
		                              city='${webUser.city}' postal='${webUser.postal}'/>
		
						<div class="fieldDiv">
			                <label id="LBL_Phone" for="Phone">Phone</label>
			                <input 
			                	size="20" 
			                	id="ID_Phone"
			                	name="Phone" 
			                	maxlength="20" 
			                	type="text"
			                	<% 
									value = request.getParameter("Phone");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>	
							/>
		                </div>
		
						<div class="fieldDiv">
			                <label id="LBL_Fax" for="Fax">Fax</label>
			                <input 
			                	size="20" 
			                	id="ID_Fax"
			                	name="Fax" 
			                	maxlength="20" 
			                	type="text"
			                	<% 
									value = request.getParameter("Fax");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>	
							/>
		                </div>
	
						<div class="fieldDiv">
							<label for="captchaimg">&nbsp;</label>
							<img name="captchaimg" src="/captcha" border=1 />
						</div>
						
						<div class="fieldDiv">
							<label for="captchamsg">&nbsp;</label>
							<span id="captchamsg"><i>Type the characters you see in the picture above..</i></span>
							<br/>
							<label for="<%=CaptchaServlet.CAPTCHA_NAME %>">&nbsp;</label>
							<input 
								class="mandatory"
								type="text" 
								name="<%=CaptchaServlet.CAPTCHA_NAME %>" 
								size="45" 
								value=""	
							/>
							<% 
								if (invalidFields != null && invalidFields.contains(CaptchaServlet.CAPTCHA_NAME)) 
									out.println("<span class=\"errorIcon\">&nbsp;</span>");
							%>
						</div>

		                <div class="buttons">
		                    <input type="submit" name="Submit" id="Submit" value="Signup">
		                    <input type="reset" name="Reset" value="Reset">		              
		                    <input type="button" name="Cancel" value="Cancel" onClick="window.location.href='/index.jsp';"/>
		                </div>
				        
				        <div align="center">
			            	Enter all <b class="mandatory">mandatory</b> data. 
			            </div>
				        
	                </fieldset>
	           	</form>   
			</div>
		</div>
	</div>
</body>
</html>