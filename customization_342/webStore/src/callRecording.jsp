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
	    		
	    		<div id="form.div.originNumber" class="formDiv">
	    			<cws:originNumberList/>
	    		</div>
	    		    	
				<div id="form.div.destinationNumber" class="formDiv">	
					<label for="form.input.destinationNumber">Destination Number:&nbsp;</label>
					<% if (request.getParameter("form.input.destinationNumber") != null) { %>
						<input name="form.input.destinationNumber" id="form.input.destinationNumber" type="text" value="<%=request.getParameter("form.input.destinationNumber")%>">
					<% } else { %>
						<input name="form.input.destinationNumber" id="form.input.destinationNumber" type="text">
					<% } %>		
	    		</div>

				<div id="form.div.callDate" class="formDiv">
	    			<label for="form.input.date">Call Date:&nbsp;</label>
	    			<% if (request.getParameter("form.input.callDate") != null) { %>
						<input name="form.input.callDate" id="form.input.callDate" type="date" value="<%=request.getParameter("form.input.callDate")%>">
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
					<th>Description</th>
					<th>Date</th>
					<th>Time</th>
					<th>Duration</th>
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
						<td class="<c:out value='${rowClass}' />"><c:out value='${callRecord.description}'/></td>
						<td class="<c:out value='${rowClass}' />"><c:out value='${callRecord.date}'/></td>
						<td class="<c:out value='${rowClass}' />"><c:out value='${callRecord.time}'/></td>
						<td class="<c:out value='${rowClass}' />"><c:out value='${callRecord.duration}'/></td>
						<td class="<c:out value='${rowClass}' />">
							<c:if test='${not empty callRecord.listenId}'>
								<input type="submit" name="Download_<c:out value='${callRecord.listenId}'/>" id="Download_<c:out value='${callRecord.listenId}'/>" value="Download"/>
							</c:if>
							<c:if test='${empty callRecord.listenId}'>
								<input type="submit" name="<c:out value='${callRecord.originNumber}'/>" id="<c:out value='${callRecord.originNumber}'/>" value="No Recording" disabled="disabled"/>
							</c:if>
							<input type="hidden" name="originNumber" value="<c:out value='${callRecord.originNumber}'/>"/>
							<input type="hidden" name="destinationNumber" value="<c:out value='${callRecord.destinationNumber}'/>"/>
							<input type="hidden" name="description" value="<c:out value='${callRecord.description}'/>"/>
							<input type="hidden" name="date" value="<c:out value='${callRecord.date}'/>"/>
							<input type="hidden" name="time" value="<c:out value='${callRecord.time}'/>"/>
							<input type="hidden" name="duration" value="<c:out value='${callRecord.duration}'/>"/>
							<input type="hidden" name="listenId" value="<c:out value='${callRecord.listenId}'/>"/>
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