<%@ include file="/WEB-INF/jspf/page.jspf" %>
<c:if test='${empty webUser || !webUser.loggedIn}'>
	<c:redirect url='loginServlet?ForwardTo=search.jsp'/>
</c:if>
<c:if test='${!webUser.employee}'>
  	<c:redirect url='index.jsp'/>
</c:if>
<%@ page import="org.compiere.wstore.*" %>
<html>
<!--
  - Author:  Josh Hill
  - Version: $Id: search.jsp,v 1.2 2008/05/26 00:41:33 jhill Exp $
  - Compiere ERP & CRM Smart Business Solution - Copyright (c) 1999-2003 Jorg Janke
  - - -
  - Web Store - Search
  -->
<head>
	<%@ include file="/WEB-INF/jspf/light_head.jspf" %>
	<script>
		function setInput(value, name)
		{
			var input=window.opener.document.getElementById(name);
			if (input != null)
			{
				input.value=value;
			}
			self.close();
		}
	</script>
	
	<title><c:out value='${ctx.name}'/> - Search</title>
	
	<c:set var="PARAM_MODE" value="<%= SearchServlet.P_MODE %>"/>
	<c:set var="PARAM_SET_INPUT" value="<%= SearchServlet.P_SET_INPUT %>"/>
	<c:set var="MODE_BUSINESS_PARTNER" value="<%= SearchServlet.M_BUSINESS_PARTNER %>"/>
	<c:set var="MODE_COUNTRY_ID" value="<%= SearchServlet.M_COUNTRY_ID %>"/>
	<c:set var="MODE_USER" value="<%= SearchServlet.M_USER %>"/>
</head>
<body>
	<div id="page">
		<div id="header">
			<div id="headerIcon">
				<img src="/images/<c:out value='${ctx.webParam1}'/>" alt="Conversant Ltd"/>
			</div>
		</div>
		<div id="searchForm">
			<c:if test='${not empty infoMsg}'>
				<div id="infoMsg">
					<c:out value="${infoMsg}"/>
				</div>
			</c:if>
			<c:if test='${empty param.mode}'>
				<form action="" method="GET">
					<fieldset>
						<legend>Search mode</legend>
						<div class="fieldDiv">
							<input type="radio" name="mode" value="<c:out value='${MODE_BUSINESS_PARTNER}' />" checked> Business Partner
							<br/>
							<input type="radio" name="mode" value="<c:out value='${MODE_COUNTRY_ID}' />"> Country Id
						</div>
						<div class="fieldDiv">
							<input type="submit" name="submit" id="submit" value="Select" />
						</div>
					</fieldset>
				</form>
			</c:if>
			<c:if test='${not empty param.mode}'>
				<fieldset>
					<legend>
						<c:if test='${param.mode eq MODE_BUSINESS_PARTNER}'>Business Partner</c:if>
						<c:if test='${param.mode eq MODE_COUNTRY_ID}'>Country Id</c:if>
						<c:if test='${param.mode eq MODE_USER}'>User</c:if>
						Search
					</legend>
					<form action="/searchServlet" method="GET">
						<div class="fieldDiv">
							<label for="name" style="padding: 0 10px 0 0;">Name </label>
							<input type="text" name="name" id="name" style="padding: 0 10px 0 0;" />							
							<input type="submit" name="submit" id="submit" value="Search" />
							<input 
								type="hidden" 
								name="<c:out value='${PARAM_MODE}' />" 
								id="<c:out value='${PARAM_MODE}' />" 
								value="<c:out value='${param.mode}' />"
							/>
							<input 
								type="hidden" 
								name="<c:out value='${PARAM_SET_INPUT}' />" 
								id="<c:out value='${PARAM_SET_INPUT}' />" 
								value="<c:out value='${param.setInput}' />"
							/>
						</div>						
					</form>
				</fieldset>
				<c:if test='${not empty result}'>
					<table>
						<c:choose>
							<c:when test='${param.mode eq MODE_BUSINESS_PARTNER}'>
								<tr>	
									<th>ID</th>
									<th colspan="2">Name</th>
								</tr>								
								<c:forEach items='${result}' var='bp' varStatus='bRowCount'>
									<jsp:useBean id="bRowCount" type="javax.servlet.jsp.jstl.core.LoopTagStatus" />
							  		<c:choose>
								  		<c:when test="<%= bRowCount.getCount() % 2 == 0 %>">
								   			<c:set var="rowClass" value="evenRow"/>
								  		</c:when>
								  		<c:otherwise>
								   			<c:set var="rowClass" value="oddRow"/>
								  		</c:otherwise>
						  			</c:choose>						
						  			<tr> 		
										<td class="<c:out value='${rowClass}' />"><c:out value='${bp._ID}'/></td>
										<td class="<c:out value='${rowClass}' />"><c:out value='${bp.name}'/></td>
										<c:if test='${not empty param.setInput}'>
											<td class="<c:out value='${rowClass}' />">
												<a href="#" onClick="setInput('<c:out value='${bp._ID}'/>', '<c:out value='${param.setInput}'/>')">Select</a>
											</td>
										</c:if>
								   	</tr>						  			
								</c:forEach>
							</c:when>
							<c:when test='${param.mode eq MODE_COUNTRY_ID}'>
								<tr>	
									<th>Country Id</th>
									<th colspan="2">Name</th>
								</tr>	
								<c:forEach items='${result}' var='country' varStatus='cRowCount'>
									<jsp:useBean id="cRowCount" type="javax.servlet.jsp.jstl.core.LoopTagStatus" />
							  		<c:choose>
								  		<c:when test="<%= cRowCount.getCount() % 2 == 0 %>">
								   			<c:set var="rowClass" value="evenRow"/>
								  		</c:when>
								  		<c:otherwise>
								   			<c:set var="rowClass" value="oddRow"/>
								  		</c:otherwise>
						  			</c:choose>						
						  			<tr> 		
										<td class="<c:out value='${rowClass}' />"><c:out value='${country.countryId}'/></td>
										<td class="<c:out value='${rowClass}' />"><c:out value='${country.description}'/></td>
										<c:if test='${not empty param.setInput}'>
											<td class="<c:out value='${rowClass}' />">
												<a href="#" onClick="setInput('<c:out value='${country.countryId}'/>', '<c:out value='${param.setInput}'/>')">Select</a>
											</td>
										</c:if>
								   	</tr>						  			
								</c:forEach>
							</c:when>
							<c:when test='${param.mode eq MODE_USER}'>
								<tr>	
									<th>User Id</th>
									<th colspan="2">Name</th>
								</tr>	
								<c:forEach items='${result}' var='user' varStatus='uRowCount'>
									<jsp:useBean id="uRowCount" type="javax.servlet.jsp.jstl.core.LoopTagStatus" />
							  		<c:choose>
								  		<c:when test="<%= uRowCount.getCount() % 2 == 0 %>">
								   			<c:set var="rowClass" value="evenRow"/>
								  		</c:when>
								  		<c:otherwise>
								   			<c:set var="rowClass" value="oddRow"/>
								  		</c:otherwise>
						  			</c:choose>						
						  			<tr> 		
										<td class="<c:out value='${rowClass}' />"><c:out value='${user.AD_User_ID}'/></td>
										<td class="<c:out value='${rowClass}' />"><c:out value='${user.name}'/></td>
										<c:if test='${not empty param.setInput}'>
											<td class="<c:out value='${rowClass}' />">
												<a href="#" onClick="setInput('<c:out value='${user.AD_User_ID}'/>', '<c:out value='${param.setInput}'/>')">Select</a>
											</td>
										</c:if>
								   	</tr>						  			
								</c:forEach>
							</c:when>
						</c:choose>
					</table>
				</c:if>
			</c:if>
		</div>
	</div>
</body>
</html>
