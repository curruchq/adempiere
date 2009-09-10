<%@ include file="/WEB-INF/jspf/page.jspf" %>
<c:if test='${empty webUser || !webUser.loggedIn}'>
  <c:redirect url='loginServlet?ForwardTo=provision.jsp'/>
</c:if>
<c:if test='${!webUser.employee}'>
  <c:redirect url='index.jsp'/>
</c:if>
<%@page import="org.compiere.wstore.*"%>
<html>
<!--
  - Author:  Josh Hill
  - Version: $Id: provision.jsp,v 1.0 2008/08/27 14:41:33 jhill Exp $
  - Compiere ERP & CRM Smart Business Solution - Copyright (c) 1999-2003 Jorg Janke
  - - -
  - Web Store - Provision DID/SIP products on an order
  -->
<head>
	<%@ include file="/WEB-INF/jspf/head.jspf" %>
	<title><c:out value='${ctx.name}'/> - Provision</title>
	
	<c:set var="PARAM_ACTION" value="<%= ProvisioningServlet.PARAM_ACTION %>"/>
	<c:set var="PARAM_ORDER_ID" value="<%= ProvisioningServlet.PARAM_ORDER_ID %>"/>
	<c:set var="PARAM_MODE" value="<%= SearchServlet.P_MODE %>"/>
	<c:set var="PARAM_SET_INPUT" value="<%= SearchServlet.P_SET_INPUT %>"/>
	
	<c:set var="GET_ORDERS" value="<%= ProvisioningServlet.GET_ORDERS %>"/>
	<c:set var="PROVISION_ORDER" value="<%= ProvisioningServlet.PROVISION_ORDER %>"/>
	<c:set var="MODE_USER" value="<%= SearchServlet.M_USER %>"/>
	
	<c:set var="REQUIRED_FIELD_NAMES" value="<%= ValidationServlet.REQUIRED_FIELD_NAMES %>"/>
	<c:set var="ALL_FIELDS" value="<%= ValidationServlet.ALL_FIELDS %>"/>
	<c:set var="SUCCESS_FORWARD_URL" value="<%= ValidationServlet.SUCCESS_FORWARD_URL %>"/>
	<c:set var="REFERER_NAME" value="<%= ValidationServlet.REFERER_NAME %>"/>
	
	<c:set var="THIS_JSP" value="<%= ProvisioningServlet.DEFAULT_JSP %>"/>
	<c:set var="PROVISIONING_SERVLET" value="<%= ProvisioningServlet.NAME %>"/>
	
</head>
<body>
	<div id="page">
		<%@ include file="/WEB-INF/jspf/header.jspf" %>
		<div id="main">
			<%@ include file="/WEB-INF/jspf/menu.jspf" %>
			<div id="content">				
				<fieldset>
					<legend>Provisioning</legend>
					<c:if test='${not empty infoMsg}'>
						<div id="infoMsg">
							<c:out value="${infoMsg}" />
						</div>
					</c:if>
					<form action="/validationServlet" method="POST">																			
						<div class="fieldDiv">
							<label for="form.userId">User Id</label>
							<input 
								class="mandatory" 
								type="text" 
								name="form.userId" 
								id="form.userId" 
								<% 
									String value = request.getParameter("form.userId");
									if (value != null)
										out.println("value=\"" + value + "\"");
								%>
							/>
							<% 
								java.util.ArrayList invalidFields = (java.util.ArrayList)request.getAttribute(org.compiere.wstore.ValidationServlet.INVALID_FIELDS);	
								if (invalidFields != null && invalidFields.contains("form.userId")) 
									out.println("<span class=\"errorIcon\"></span>");
							%>
							<a class="search" target="_blank" href="/search.jsp?<c:out value='${PARAM_SET_INPUT}' />=form.userId&<c:out value='${PARAM_MODE}' />=<c:out value='${MODE_USER}' />" onClick="popUpSearch(this.href, 'SearchUser'); return false"></a>													
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
								value="<c:out value='${GET_ORDERS}' />" 
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
								value="<c:out value='${PROVISIONING_SERVLET}' />"
							/>
							<br/>
							<label for="submit">&nbsp;</label>	
							<input type="submit" name="submit" id="submit" value="Get Orders" />
							<input type="reset" name="Reset" value="Clear" />
						</div>					
					</form>	
					<c:if test='${not empty orders}'>
						<br/>
						<form action="/<c:out value='${PROVISIONING_SERVLET}' />" method="POST">
							<table class="contentTable">
						        <tr> 
						        	<th>Document No</th>
						          	<th>Desciption</th>
						          	<th>Status</th>
						          	<th>Date</th>
						          	<th>Total Lines</th>
						          	<th colspan="2">Grand Total</th>
						        </tr>
						        
						        <c:forEach items='${orders}' var='order' varStatus='status'> 
						            <jsp:useBean id="status" type="javax.servlet.jsp.jstl.core.LoopTagStatus" />
						        	<c:choose>
						        		<c:when test="<%= status.getCount() %2 == 0 %>">
							        		<c:set var="rowClass" value="evenRow"/>
						        		</c:when>
						        		<c:otherwise>
							        		<c:set var="rowClass" value="oddRow"/>
						        		</c:otherwise>
						        	</c:choose>
							        <tr> 
							        	<td class="<c:out value='${rowClass}' />"><c:out value='${order.documentNo}'/></td>
							          	<td class="<c:out value='${rowClass}' />"><c:out value='${order.description}'/>&nbsp;</td>
							          	<td class="<c:out value='${rowClass}' />"><c:out value='${order.docStatusName}'/></td>
							          	<td class="<c:out value='${rowClass}' />"><fmt:formatDate value='${order.dateOrdered}'/></td>
							          	<td class="<c:out value='${rowClass}' /> amount"><fmt:formatNumber value='${order.totalLines}' type="currency" currencySymbol=""/></td>
							          	<td class="<c:out value='${rowClass}' /> amount"><c:out value='${order.currencyISO}'/>&nbsp;<fmt:formatNumber value='${order.grandTotal}' type="currency" currencySymbol=""/></td>
							          	<td class="<c:out value='${rowClass}' />">
							          		<input name="Provision" id="Provision" value="Run provisioning" 
									    	onClick="window.location.href='/<c:out value='${PROVISIONING_SERVLET}' />?<c:out value='${PARAM_ACTION}' />=<c:out value='${PROVISION_ORDER}'/>&<c:out value='${PARAM_ORDER_ID}'/>=<c:out value='${order.c_Order_ID}'/>'" type="button">
							          	</td>
							        </tr>
						    	</c:forEach> 
							</table>
						</form>
					</c:if>
				</fieldset>
			</div>
		</div>
		<%@ include file="/WEB-INF/jspf/footer.jspf" %>
	</div>
</body>
</html>
