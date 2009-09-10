<%@ include file="/WEB-INF/jspf/page.jspf" %>
<c:if test='${empty webUser || !webUser.loggedIn}'>
  <c:redirect url='loginServlet?ForwardTo=editSIPAccount.jsp'/>
</c:if>
<html>
<!--
  - Author:  Josh Hill
  - Version: $Id: editSIPAccount.jsp,v 1.0 2008/04/24 12:02:42 Exp $
  - Compiere ERP & CRM Smart Business Solution - Copyright (c) 1999-2003 Jorg Janke
  - - -
  - Web Store Edit SIP Account
  -->
<head>
<%@ include file="/WEB-INF/jspf/head.jspf" %>  
<title><c:out value='${ctx.name}'/> - Edit SIP Account</title>
</head>
<body>
	<div id="page">
		<%@ include file="/WEB-INF/jspf/header.jspf" %>
		<div id="main">
			<%@ include file="/WEB-INF/jspf/menu.jspf" %>
			<div id="content"> 
				<cws:sipAccountForm mode="edit"/>
			</div>
		</div>
	</div>
</body>
</html>