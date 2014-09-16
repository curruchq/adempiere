<%@ include file="/WEB-INF/jspf/page.jspf" %>
<c:if test='${empty webUser || !webUser.loggedIn}'>
  <c:redirect url='loginServlet?ForwardTo=createProduct.jsp'/>
</c:if>
<c:if test='${!webUser.employee}'>
  <c:redirect url='index.jsp'/>
</c:if>
<%@page import="org.compiere.wstore.*"%>
<html>
<!--
  - Author:  Josh Hill
  - Version: $Id: createProduct.jsp,v 1.2 2008/05/26 00:41:33 jhill Exp $
  - Compiere ERP & CRM Smart Business Solution - Copyright (c) 1999-2003 Jorg Janke
  - - -
  - Web Store - Create Product
  -->
<head>
	<%@ include file="/WEB-INF/jspf/head.jspf" %>
	<title><c:out value='${ctx.name}'/> - Create Product</title>
	
	<c:set var="PARAM_ACTION" value="<%= CreateProductServlet.PARAM_ACTION %>"/>
	<c:set var="PARAM_MODE" value="<%= SearchServlet.P_MODE %>"/>
	<c:set var="PARAM_SET_INPUT" value="<%= SearchServlet.P_SET_INPUT %>"/>
	
	<c:set var="CREATE_DID_PAIR" value="<%= CreateProductServlet.CREATE_DID_PAIR %>"/>
	<c:set var="MODE_BUSINESS_PARTNER" value="<%= SearchServlet.M_BUSINESS_PARTNER %>"/>
	<c:set var="MODE_COUNTRY_ID" value="<%= SearchServlet.M_COUNTRY_ID %>"/>
	
	<c:set var="REQUIRED_FIELD_NAMES" value="<%= ValidationServlet.REQUIRED_FIELD_NAMES %>"/>
	<c:set var="ALL_FIELDS" value="<%= ValidationServlet.ALL_FIELDS %>"/>
	<c:set var="SUCCESS_FORWARD_URL" value="<%= ValidationServlet.SUCCESS_FORWARD_URL %>"/>
	<c:set var="REFERER_NAME" value="<%= ValidationServlet.REFERER_NAME %>"/>
	<c:set var="THIS_JSP" value="<%= CreateProductServlet.DEFAULT_JSP %>"/>
	
</head>
<body>
	<div id="page">
		<%@ include file="/WEB-INF/jspf/header.jspf" %>
		<div id="main">
			<%@ include file="/WEB-INF/jspf/menu.jspf" %>
			<div id="content">				
				<fieldset>
					<legend>Create Product</legend>
					<c:if test='${not empty infoMsg}'>
						<div id="infoMsg">
							<c:out value="${infoMsg}" />
						</div>
					</c:if>
					<form action="/validationServlet" method="POST">				
						<div class="fieldDiv">
							<label for="form.didNumber">DID Number</label>
							<input 
								class="mandatory" 
								type="text" 
								name="form.didNumber" 
								id="form.didNumber" 
								<% 
									String value = request.getParameter("form.didNumber");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>
							/>
							<% 
								java.util.ArrayList invalidFields = (java.util.ArrayList)request.getAttribute(org.compiere.wstore.ValidationServlet.INVALID_FIELDS);
								if (invalidFields != null && invalidFields.contains("form.didNumber")) 
									out.println("<span class=\"errorIcon\"></span>");
							%>
						</div>
						
						<div class="fieldDiv">
							<label for="form.countryCode">Country Code</label>
							<input 
								class="mandatory" 
								type="text" 
								name="form.countryCode" 
								id="form.countryCode" 
								<% 
									value = request.getParameter("form.countryCode");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>	
							/>
							<% 
								if (invalidFields != null && invalidFields.contains("form.countryCode")) 
									out.println("<span class=\"errorIcon\"></span>");
							%>
						</div>
						
						<div class="fieldDiv">
							<label for="form.countryId">Country Id</label>
							<input 
								class="mandatory" 
								type="text" 
								name="form.countryId" 
								id="form.countryId" 
								<% 
									value = request.getParameter("form.countryId");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>	
							/>
							<% 
								if (invalidFields != null && invalidFields.contains("form.countryId")) 
									out.println("<span class=\"errorIcon\"></span>");
							%>
							<a class="search" target="_blank" href="/search.jsp?<c:out value='${PARAM_SET_INPUT}' />=form.countryId&<c:out value='${PARAM_MODE}' />=<c:out value='${MODE_COUNTRY_ID}' />" onClick="popUpSearch(this.href, 'SearchCountryId'); return false"></a>													
						</div>
						
						<div class="fieldDiv">
							<label for="form.areaCode">Area Code</label>
							<input 
								class="mandatory" 
								type="text" 
								name="form.areaCode" 
								id="form.areaCode" 
								<% 
									value = request.getParameter("form.areaCode");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>
							/>
							<% 
								if (invalidFields != null && invalidFields.contains("form.areaCode")) 
									out.println("<span class=\"errorIcon\"></span>");
							%>
						</div>
						
						<div class="fieldDiv">
							<label for="form.areaCodeDescription">Area Code Description</label>
							<input 
								class="mandatory" 
								type="text" 
								name="form.areaCodeDescription" 
								id="form.areaCodeDescription" 
								<% 
									value = request.getParameter("form.areaCodeDescription");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>	
							/>
							<% 
								if (invalidFields != null && invalidFields.contains("form.areaCodeDescription")) 
									out.println("<span class=\"errorIcon\"></span>");
							%>
						</div>
						
						<div class="fieldDiv">
							<label for="form.setupCost">Setup Cost*</label>
							<input 
								class="mandatory" 
								type="text" 
								name="form.setupCost" 
								id="form.setupCost" 
								<% 
									value = request.getParameter("form.setupCost");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>
							/>
							<% 
								if (invalidFields != null && invalidFields.contains("form.setupCost")) 
									out.println("<span class=\"errorIcon\"></span>");
							%>
						</div>
						
						<div class="fieldDiv">
							<label for="form.monthlyCharge">Monthly Charge*</label>
							<input 
								class="mandatory" 
								type="text" 
								name="form.monthlyCharge" 
								id="form.monthlyCharge" 
								<% 
									value = request.getParameter("form.monthlyCharge");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>
							/>
							<% 
								if (invalidFields != null && invalidFields.contains("form.monthlyCharge")) 
									out.println("<span class=\"errorIcon\"></span>");
							%>
						</div>
						
						<div class="fieldDiv">
							<label for="form.perMinuteCharge">Per Minute Charge*</label>
							<input 
								class="mandatory" 
								type="text" 
								name="form.perMinuteCharge" 
								id="form.perMinuteCharge" 
								<% 
									value = request.getParameter("form.perMinuteCharge");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>
							/>
							<% 
								if (invalidFields != null && invalidFields.contains("form.perMinuteCharge")) 
									out.println("<span class=\"errorIcon\"></span>");
							%>
							<span></span>
						</div>
						
						<div class="fieldDiv">
							<label for="form.freeMinutes">Free Minutes</label>
							<input 
								class="mandatory" 
								type="text" 
								name="form.freeMinutes" 
								id="form.freeMinutes" 
								<% 
									value = request.getParameter("form.freeMinutes");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>	
							/>
							<% 
								if (invalidFields != null && invalidFields.contains("form.freeMinutes")) 
									out.println("<span class=\"errorIcon\"></span>");
							%>
						</div>
						
						<div class="fieldDiv">
							<label for="form.businessPartnerId">Business Partner Id</label>
							<input 
								class="mandatory" 
								type="text" 
								name="form.businessPartnerId" 
								id="form.businessPartnerId" 
								<% 
									value = request.getParameter("form.businessPartnerId");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>
							/>
							<% 
								if (invalidFields != null && invalidFields.contains("form.businessPartnerId")) 
									out.println("<span class=\"errorIcon\"></span>");
							%>
							<a class="search" target="_blank" href="/search.jsp?<c:out value='${PARAM_SET_INPUT}' />=form.businessPartnerId&<c:out value='${PARAM_MODE}' />=<c:out value='${MODE_BUSINESS_PARTNER}' />" onClick="popUpSearch(this.href, 'SearchBP'); return false"></a>													
						</div>
						
						<div class="fieldDiv">
							<label for="form.currency">Currency</label>
							<%
								value = request.getParameter("form.currency");
							%>
							<select name="form.currency">
								<option <%= (value != null && value.equalsIgnoreCase("NZD") ? "selected" : "") %>>NZD</option>
								<option <%= (value != null && value.equalsIgnoreCase("USD") ? "selected" : "") %>>USD</option>
							</select>
						</div>
						
						<div class="fieldDiv">
							<label for="form.domain">Domain</label>
							<input 
								class="mandatory" 
								type="text" 
								name="form.domain" 
								id="form.domain" 
								<% 
									value = request.getParameter("form.domain");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>
							/>
							<% 
								if (invalidFields != null && invalidFields.contains("form.domain")) 
									out.println("<span class=\"errorIcon\"></span>");
							%>
						</div>
						
						<div class="fieldDiv">
							<cws:priceList/>
							<input 
								type="hidden" 
								name="M_PriceList_Version_ID" 
								id="M_PriceList_Version_ID" 
								value="<c:out value='${priceList.priceList_Version_ID}' />"
							/>
							<input 
								type="hidden" 
								name="<c:out value='${PARAM_ACTION}' />" 
								id="<c:out value='${PARAM_ACTION}' />" 
								value="<c:out value='${CREATE_DID_PAIR}' />" 
							/>										
							<input 
								type="hidden" 
								name="<c:out value='${REQUIRED_FIELD_NAMES}' />" 
								id="<c:out value='${REQUIRED_FIELD_NAMES}' />" 
								value="<c:out value='${ALL_FIELDS}' />"
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
								value="createProductServlet"
							/>
							<br/>
							<label for="submit">&nbsp;</label>	
							<input type="submit" name="submit" id="submit" value="Create Product" />
							<input type="reset" name="Reset" value="Clear" />
						</div>	
						
						<div class="fieldDiv">
						    <br/>
						    <label>&nbsp;</label>
						    <span>* <i>Enter prices in the selected currency.</i></span>						
						</div>					
					</form>	
				</fieldset>
			</div>
		</div>
		<%@ include file="/WEB-INF/jspf/footer.jspf" %>
	</div>
</body>
</html>
