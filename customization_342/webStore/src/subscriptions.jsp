<%@ include file="/WEB-INF/jspf/page.jspf" %>
<c:if test='${empty webUser || !webUser.loggedIn}'>
  <c:redirect url='loginServlet?ForwardTo=subscriptions.jsp'/>
</c:if>
<html>
<!--
  - Author:  Josh Hill
  - Version: $Id: subscriptions.jsp,v 1.0 2008/04/22 14:24:14 Exp $
  - Compiere ERP & CRM Smart Business Solution - Copyright (c) 1999-2003 Jorg Janke
  - - -
  - Web Store - Subscriptions
  -->
<head>
<%@ include file="/WEB-INF/jspf/head.jspf" %>  
<title><c:out value='${ctx.name}'/> - Subscriptions</title>
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
				<h1>Subscriptions</h1>
			  	<cws:subscriptionsTable/>
			</div>
		</div>
	</div>
</body>
</html>