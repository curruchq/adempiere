<%@ include file="/WEB-INF/jspf/page.jspf" %>
<c:if test='${empty webUser || !webUser.loggedIn}'>
  <c:redirect url='loginServlet?ForwardTo=callRecording.jsp'/>
</c:if>
<%@page import="org.compiere.wstore.*"%>
<html>
<!--
  - Author:  Josh Hill
  - Version: $Id: callRecording.jsp,v 1.0 2009/06/11 14:24:14 Exp $
  - Compiere ERP & CRM Smart Business Solution - Copyright (c) 1999-2003 Jorg Janke
  - - -
  - Web Store - Call recordings
  -->
  
<head>
	<%@ include file="/WEB-INF/jspf/head.jspf" %>  
	<title><c:out value='${ctx.name}'/> - Call Recording</title>
</head>

<body>
<div id="page">
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div id="main">
	<%@ include file="/WEB-INF/jspf/menu.jspf" %>
	<div id="content"> 
		<c:if test='${not empty infoMsg}'>
			<div id="infoMsg">
				<c:out value="${infoMsg}" />
			</div>
		</c:if>
	    <form action="<%=CallRecordingServlet.NAME %>" method="post" enctype="application/x-www-form-urlencoded">
	    	<input type="hidden" name="<%=CallRecordingServlet.PARAM_ACTION %>" id="<%=CallRecordingServlet.PARAM_ACTION %>" value="<%=CallRecordingServlet.ACTION_SEARCH %>"/>
	    	<fieldset>
	        	<legend>Search Calls</legend>
	    		
	    		<div id="form.div.billingGroup" class="formDiv">
	    			<c:if test='${not empty webUser && webUser.employee}'>
						<label accesskey="B" for="form.select.billingGroup">Billing Group:&nbsp;</label>
						<% if (request.getParameter("form.select.billingGroup") != null) { %>
							<input name="form.select.billingGroup" id="form.select.billingGroup" type="text" value="<%=request.getParameter("form.select.billingGroup")%>">
						<% } else if (request.getAttribute("form.select.billingGroup") != null) { %>
							<input name="form.select.billingGroup" id="form.select.billingGroup" type="text" value="<%=request.getAttribute("form.select.billingGroup")%>">
						<% } else { %>
							<input name="form.select.billingGroup" id="form.select.billingGroup" type="text">
						<% } %>		
					</c:if>
					<c:if test='${empty webUser || !webUser.employee}'>
						<cws:billingGroupList/>
					</c:if>	    			
	    		</div>
	    		    	
	    		<div id="form.div.originNumber" class="formDiv">	
					<label for="form.input.originNumber">Origin Number:&nbsp;</label>
					<% if (request.getParameter("form.input.originNumber") != null) { %>
						<input name="form.input.originNumber" id="form.input.originNumber" type="text" value="<%=request.getParameter("form.input.originNumber")%>">
					<% } else if (request.getAttribute("form.input.originNumber") != null) { %>
						<input name="form.input.originNumber" id="form.input.originNumber" type="text" value="<%=request.getAttribute("form.input.originNumber")%>">
					<% } else { %>
						<input name="form.input.originNumber" id="form.input.originNumber" type="text">
					<% } %>		
	    		</div>
	    		    	
				<div id="form.div.destinationNumber" class="formDiv">	
					<label for="form.input.destinationNumber">Destination Number:&nbsp;</label>
					<% if (request.getParameter("form.input.destinationNumber") != null) { %>
						<input name="form.input.destinationNumber" id="form.input.destinationNumber" type="text" value="<%=request.getParameter("form.input.destinationNumber")%>">
					<% } else if (request.getAttribute("form.input.destinationNumber") != null) { %>
						<input name="form.input.destinationNumber" id="form.input.destinationNumber" type="text" value="<%=request.getAttribute("form.input.destinationNumber")%>">
					<% } else { %>
						<input name="form.input.destinationNumber" id="form.input.destinationNumber" type="text">
					<% } %>		
	    		</div>

				<div id="form.div.callDate" class="formDiv">
	    			<label for="form.input.date">Call Date:&nbsp;</label>
	    			<% if (request.getParameter("form.input.callDate") != null) { %>
						<input name="form.input.callDate" id="form.input.callDate" type="date" value="<%=request.getParameter("form.input.callDate")%>">
					<% } else if (request.getAttribute("form.input.callDate") != null) { %>
						<input name="form.input.callDate" id="form.input.callDate" type="date" value="<%=request.getAttribute("form.input.callDate")%>">
					<% } else { %>
						<input name="form.input.callDate" id="form.input.callDate" type="date">
					<% } %>
	    		</div>

	    		<div id="form.div.submit" class="formDiv">
	            	<label for="form.input.submit">&nbsp;</label>
	            	<input id="form.input.submit" type="submit" value="Search"/>
	            </div>
		    </fieldset>
	    </form>	   
	    <form action="<%=CallRecordingServlet.NAME %>" method="post" enctype="application/x-www-form-urlencoded">
	    	<input type="hidden" name="<%=CallRecordingServlet.PARAM_ACTION %>" id="<%=CallRecordingServlet.PARAM_ACTION %>" value="<%=CallRecordingServlet.ACTION_DOWNLOAD %>"/>	    	
	    	
		    <table class="contentTable">
		    	<tr>
					<th>Origin</th>
					<th>Destination</th>
					<th>Date/Time</th>
					<th>Call Length (seconds)</th>
					<th>Recording</th>
				</tr>
				
				<c:forEach items='${callRecords}' var='callRecord' varStatus='statusCount'>
					<jsp:useBean id="statusCount" type="javax.servlet.jsp.jstl.core.LoopTagStatus" />
					<c:choose>
						<c:when test="<%= statusCount.getCount() % 2 == 0 %>">
							<c:set var="rowClass" value="evenRow"/>
						</c:when>
						<c:otherwise>
							<c:set var="rowClass" value="oddRow"/>
						</c:otherwise>
					</c:choose>
					<tr>
						<td class="<c:out value='${rowClass}' />"><c:out value='${callRecord.originNumber}'/></td>
						<td class="<c:out value='${rowClass}' />"><c:out value='${callRecord.destinationNumber}'/></td>
						<c:if test='${empty callRecord.formattedDateTime}'>
							<td class="<c:out value='${rowClass}' />"><c:out value='${callRecord.dateTime}'/></td>
						</c:if>
						<c:if test='${not empty callRecord.formattedDateTime}'>
							<td class="<c:out value='${rowClass}' />"><c:out value='${callRecord.formattedDateTime}'/></td>
						</c:if>
						<td class="<c:out value='${rowClass}' />"><c:out value='${callRecord.callLength}'/></td>
						<td class="<c:out value='${rowClass}' />">
							<input type="submit" name="Download_<c:out value='${callRecord.twoTalkId}'/>" id="Download_<c:out value='${callRecord.twoTalkId}'/>" value="Download"/>
							<input type="hidden" name="billingGroup" value="<%=request.getParameter("form.select.billingGroup")%>"/>
							<input type="hidden" name="originNumber" value="<c:out value='${callRecord.originNumber}'/>"/>
							<input type="hidden" name="destinationNumber" value="<c:out value='${callRecord.destinationNumber}'/>"/>							
							<c:if test='${empty callRecord.formattedDateTime}'>
								<input type="hidden" name="dateTime" value="<c:out value='${callRecord.dateTime}'/>"/>
							</c:if>
							<c:if test='${not empty callRecord.formattedDateTime}'>
								<input type="hidden" name="dateTime" value="<c:out value='${callRecord.formattedDateTime}'/>"/>
							</c:if>
							<input type="hidden" name="callLength" value="<c:out value='${callRecord.callLength}'/>"/>
							<input type="hidden" name="twoTalkId" value="<c:out value='${callRecord.twoTalkId}'/>"/>						
							<input type="hidden" name="form.select.billingGroup" value="<%=request.getParameter("form.select.billingGroup")%>"/>	
							<input type="hidden" name="form.input.originNumber" value="<%=request.getParameter("form.input.originNumber")%>"/>
							<input type="hidden" name="form.input.destinationNumber" value="<%=request.getParameter("form.input.destinationNumber")%>"/>
							<input type="hidden" name="form.input.callDate" value="<%=request.getParameter("form.input.callDate")%>"/>
						</td>
					</tr>
				</c:forEach>
		    </table> 
		</form>
    </div>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
</div>
</body>
</html>