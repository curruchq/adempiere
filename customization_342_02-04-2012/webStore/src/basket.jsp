<%@ include file="/WEB-INF/jspf/page.jspf" %>
<html>
<!--
  - Author:  Jorg Janke
  - Version: $Id: basket.jsp,v 1.2 2006/05/06 00:41:33 mdeaelfweald Exp $
  - Compiere ERP & CRM Smart Business Solution - Copyright (c) 1999-2003 Jorg Janke
  - - -
  - Web Store Basket
  -->
<head>
<%@ include file="/WEB-INF/jspf/head.jspf" %>
<title><c:out value='${ctx.name}'/> - My Basket</title>
</head>
<body><div id="page">
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div id="main">
	<%@ include file="/WEB-INF/jspf/menu.jspf" %>
	<div id="content">
	<!-- Display message if set -->
	<c:if test='${not empty infoMsg}'>
		<div id="infoMsg">
			<c:out value="${infoMsg}" />
		</div>
	</c:if>
	  <h1>Web Basket</h1>
      <c:if test='${empty webBasket}'> 
      <p>Empty Basket (timeout) - Please go back, refresh the page and add products 
        again.</p>
      </c:if> <c:if test='${not empty webBasket}'> 
      <form action="basketServlet" method="post" enctype="application/x-www-form-urlencoded" name="basket" id="basket" >
        <table class="contentTable">
          <tr> 
            <th>Product</th>
            <th>Price</th>
            <th>Quantity</th>
            <th><c:out value='${priceList.currency}'/> Total</th>
            <th>&nbsp;</th>
          </tr>
          <c:forEach items='${webBasket.lines}' var='line' varStatus='status'> 
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
	            <td class="<c:out value='${rowClass}' />"><c:out value='${line.name}'/> <a class="conversant" target="_blank" href="/basketServlet?action=desc&id=<c:out value='${line.m_Product_ID}'/>&price=<c:out value='${line.price}'/>" onClick="return popUpDesc(this.href, '<c:out value='${line.name}'/>');">[Description]</a></td>
	            <td class="<c:out value='${rowClass}' /> amount"><fmt:formatNumber value='${line.price}' type="currency" currencySymbol=""/></td>
	            <td class="<c:out value='${rowClass}' /> quantity"><fmt:formatNumber value='${line.quantity}'/></td>
	            <td class="<c:out value='${rowClass}' /> amount"><fmt:formatNumber value='${line.total}' type="currency" currencySymbol=""/></td>
	            <td class="<c:out value='${rowClass}' />"><input type="submit" name="Delete_<c:out value='${line.line}'/>" value="Delete"></td>
          	</tr>
          </c:forEach> 
          <tr> 
            <th><c:out value='${webBasket.lineCount}'/></th>
            <th>&nbsp;</th>
            <th>&nbsp;</th>
            <th>
              <div class="amount"><c:out value='${priceList.currency}'/> <fmt:formatNumber value='${webBasket.total}' type="currency" currencySymbol=""/></div></th>
            <th><input  type="button" name="Checkout" id="Checkout" value="Create Secure Order" 
	    	onClick="window.top.location.replace('<c:out value='https://${ctx.context}/'/>checkOutServlet');">
			<!-- removed by request of BBB
			&nbsp; <input name="CheckoutUnsecure" id="CheckoutUnsecure" value="Create Order" 
	    	onClick="window.top.location.replace('checkOutServlet');" type="button">  -->
            </th>
          </tr>
        </table>
      </form>
      </c:if> <p> 
        <input name="Back" type="button" id="Back" value="Back to Web Store" onClick="window.top.location.replace('index.jsp');">
      </p></div>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
</div></body>
</html>
