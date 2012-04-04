<%@ include file="/WEB-INF/jspf/page.jspf" %>
<%@page import="org.compiere.wstore.*"%>
<c:if test='${empty webUser || !webUser.loggedIn}'>
  <c:redirect url='loginServlet?ForwardTo=sipAccount.jsp'/>
</c:if>
<html>
<!--
  - Author:  Josh Hill
  - Version: $Id: sipAccount.jsp,v 1.0 2008/04/14 14:24:14 Exp $
  - Compiere ERP & CRM Smart Business Solution - Copyright (c) 1999-2003 Jorg Janke
  - - -
  - Web Store SIP Account Management
  -->
<head>
<%@ include file="/WEB-INF/jspf/head.jspf" %>  
<title><c:out value='${ctx.name}'/> - SIP</title>
<style>
.createLink {
	font-weight:bold;
	padding:0.2em 0em 0.2em 0.2em;
}
</style>
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
			  	<h1>SIP Accounts</h1>
				<cws:createSIPAccountLink/>
				<cws:sipAccountsTable/>
			</div>
		</div>
	</div>
</body>
</html>