<%@ include file="/WEB-INF/jspf/page.jspf" %>
<c:if test='${empty webUser || !webUser.loggedIn}'>
  <c:redirect url='loginServlet?ForwardTo=createSIPAccount.jsp'/>
</c:if>
<html>
<!--
  - Author:  Josh Hill
  - Version: $Id: createSIPAccount.jsp,v 1.0 2008/04/23 15:12:22 Exp $
  - Compiere ERP & CRM Smart Business Solution - Copyright (c) 1999-2003 Jorg Janke
  - - -
  - Web Store Create SIP account
  -->
<head>
<%@ include file="/WEB-INF/jspf/head.jspf" %>  
<title><c:out value='${ctx.name}'/> - Create SIP Account</title>
</head>
<body>
	<div id="page">
		<%@ include file="/WEB-INF/jspf/header.jspf" %>
		<div id="main">
			<%@ include file="/WEB-INF/jspf/menu.jspf" %>
			<div id="content"> 
				<cws:sipAccountForm mode="create"/>
			</div>
		</div>
	</div>
</body>
</html>