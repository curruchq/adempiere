<%@ include file="/WEB-INF/jspf/page.jspf" %>
<html>
	<!--
	  - Author:  Josh Hill
	  - Version: $Id: productDescription.jsp,v 1.0 2008/05/20 10:24:14 Exp $
	  - Compiere ERP & CRM Smart Business Solution - Copyright (c) 1999-2003 Jorg Janke
	  - - -
	  - Product Description
	  -->
	<head>
		<%@ include file="/WEB-INF/jspf/light_head.jspf" %>
		<title><c:out value='${ctx.name}'/> - Product Description</title>
	</head>
	<body>
		<div id="page">
			<div id="header">
				<div id="headerIcon">
					<img src="/images/<c:out value='${ctx.webParam1}'/>" alt="Conversant Ltd"/>
				</div>
			</div>
			<div id="main">
				<div id="productDescription"> 
					<b>ID:</b> <c:out value='${product._ID}'/><br/>
					<b>Name:</b> <c:out value='${product.name}'/><br/>
					<b>Description:</b> <c:out value='${product.description}'/><br/>
					<b>Price:</b> <fmt:formatNumber value='${param.price}' type="currency" currencySymbol=""/> <i>(<c:out value='${priceList.currency}'/>)</i><br/>
					<c:if test='${not empty didDesc}'>
						<b>Country Code:</b> <c:out value='${didDesc.countryCode}'/><br/>
						<b>Area Code:</b> <c:out value='${didDesc.areaCode}'/><br/>
						<b>Per Min Charges:</b> <c:out value='${didDesc.perMinCharges}'/><br/>
						<b>Free Minutes:</b> <c:out value='${didDesc.freeMins}'/><br/>
					</c:if>
				</div>
			</div>
		</div>
	</body>
</html>