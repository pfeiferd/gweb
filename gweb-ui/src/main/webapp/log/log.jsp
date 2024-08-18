<%@page import="org.metagene.gweb.service.dto.Job"%>
<%@page import="org.metagene.gweb.service.dto.DB"%>
<%@page import="org.metagene.gweb.service.dto.User"%>
<%@page import="org.metagene.gweb.service.dto.Person"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
String lan = request.getParameter("lan");
if (!"de".equals(lan)) {
	lan = "en";
}
long jobid = -1;
try {
	jobid = Long.valueOf(request.getParameter("jobid"));
} catch (NumberFormatException e) {
	// Ignore.
}
%>
<html>
<head>
<meta charset="UTF-8">
<title>Genestrip Web: Log: Job <%=jobid%></title>
<script type="text/javascript">
var state = {};
state.currentLan = "<%=lan%>";
jobId=<%=jobid%>;

var mainApp = "<%=request.getContextPath()%>";
var restPath = "<%=request.getContextPath()%>" + "/rest";
</script>
<script src="log/log.js"></script>
<link rel="stylesheet" type="text/css" href="ui.css">
</head>
<body onload="main()"
	style="margin: 0px; height: 100vh; display: flex; flex-direction: column; flex-wrap: nowrap;">
	<div style="margin: 8px 8px 0 8px;">
		<table class="formtable">
			<tbody>
				<tr>
					<td style="width: 40px; height: 40px;"><img width="35"
						height="35" src="logo.svg" style="vertical-align: text-bottom;" /></td>
					<td><span class="title">Genestrip Web</span></td>
					<td style="text-align: right; vertical-align: bottom;">
						<div id="lan" style="display: inline;">
							<div id="enlan" class="lan">
								<svg class="feather">
									<use href="feather-sprite.svg#globe" /></svg>
								<span data-i18n="en" onclick="changeLan('en')"></span>
							</div>
							<div id="delan" class="lan">
								<svg class="feather">
									<use href="feather-sprite.svg#flag" /></svg>
								<span data-i18n="de" onclick="changeLan('de')"></span>
							</div>
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	</div>
	<div id="screens" style="margin: 4px 8px 0 8px;">
		<div id="main" style="position: relative;">
			<div id="tabs">
				<div class="selectedtab">
					<svg class="feather">
									<use href="feather-sprite.svg#file-text" /></svg>
					<span data-i18n="logfor"></span><span id="jobtitle"></span>
				</div>
				<div class="hline" style="display: block; margin-top: 2px;"></div>
			</div>
		</div>
		<div id="error" style="display: none;">
			<div class="errortitle">
				<span data-i18n="servererror"></span>
			</div>
			<div>
				<span data-i18n="statuscode"></span> <span id="statuscode"></span>
			</div>
			<div>
				<span data-i18n="statustext"></span> <span id="statustext"></span>
			</div>
			<div>
				<span data-i18n="responsetext"></span> <span id="responsetext"></span>
			</div>
		</div>
		<div id="modalalert" class="modal" style="display: none;">
			<div class="modal-content">
				<div id="malert">
					<table class="formtable">
						<tbody>
							<tr>
								<td id="alertmessage"></td>
							</tr>
							<tr>
								<td align="right">
									<button type="button" onclick="okAlert()">
										<span data-i18n="ok"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#check" /></svg>
									</button>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>
	</div>
	<div id="tablediv"
		style="position: relative; flex-grow: 1; overflow-y: scroll; margin: 8px 12px 8px 12px; white-space: pre-line; font-family: monospace; font-size: 10pt;">
	</div>
</body>
</html>