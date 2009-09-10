<%@ include file="/WEB-INF/jspf/page.jspf" %>
<c:if test='${empty webUser || !webUser.loggedIn}'>
	<c:redirect url='loginServlet?ForwardTo=contract.jsp'/>
</c:if>
<%@ page import="org.compiere.wstore.*" %>
<html>
<!--
  - Author:  Josh Hill
  - Version: $Id: contract.jsp,v 1.2 2008/05/26 00:41:33 jhill Exp $
  - Compiere ERP & CRM Smart Business Solution - Copyright (c) 1999-2003 Jorg Janke
  - - -
  - Web Store - Contract
  -->
<head>
	<%@ include file="/WEB-INF/jspf/light_head.jspf" %>

	<script type="text/JavaScript">
		function printIFrame(iframe)
		{
			iframe.focus();
			iframe.print();
		} 
	</script>

	<title><c:out value='${ctx.name}'/></title>
	
	<c:set var="SERVLET_NAME" value="<%= ContractServlet.SERVLET_NAME %>"/>
	<c:set var="FILE_NAME" value="<%= ContractServlet.FILE_NAME %>"/>
	<c:set var="USERS_NAME" value="<%= ContractServlet.USERS_NAME %>"/>
	<c:set var="REFERER_NAME" value="<%= ContractServlet.REFERER_NAME %>"/>
</head>
<body>
	<div id="page">
		<form action="/<c:out value='${SERVLET_NAME}'/>" method="POST">			
			<c:if test='${not empty infoMsg}'>
				<div id="infoMsg">
					<c:out value="${infoMsg}"/>
				</div>
			</c:if>
			<iframe name="contract" id="contract" src="<c:out value='${ctx.webParam2}'/>"></iframe>
			<div align="center">
				<input type="button" value="Print 'Terms and Conditions'" onclick="printIFrame(contract);" />
			</div>
			<div id="contractButtons">
				<label>Please type your full name: </label>
				<input 
					type="text" 
					name="<c:out value='${USERS_NAME}'/>" 
					id="<c:out value='${USERS_NAME}'/>" 
					value=""
				/>
				<input type="submit" name="submit" id="submit" value="Accept">
				<input type="button" name="logout" id="logout" value="Logout" onClick="window.location.href='/loginServlet?mode=logout'">
			</div>
			<input 
				type="hidden" 
				name="<c:out value='${FILE_NAME}'/>" 
				id="<c:out value='${FILE_NAME}'/>"
				value="<c:out value='${ctx.webParam2}'/>"
			/>
			<input 
				type="hidden" 
				name="<c:out value='${REFERER_NAME}'/>" 
				id="<c:out value='${REFERER_NAME}'/>" 
				value="<c:out value='${param.refererName}'/>"
			/>
		</form>
	</div>
</body>
</html>
