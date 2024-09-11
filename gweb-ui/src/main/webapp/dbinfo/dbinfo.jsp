<%@page import="org.metagene.gweb.service.dto.Job"%>
<%@page import="org.metagene.gweb.service.dto.DB"%>
<%@page import="org.metagene.gweb.service.dto.User"%>
<%@page import="org.metagene.gweb.service.dto.Person"%>
<%@page import="org.metagene.gweb.Version"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
String version = Version.class.getPackage().getImplementationVersion();
%>
<!DOCTYPE html>
<%
String lan = request.getParameter("lan");
if (!"de".equals(lan)) {
	lan = "en";
}
long dbid = -1;
try {
	dbid = Long.valueOf(request.getParameter("dbid"));
} catch (NumberFormatException e) {
	// Ignore.
}
%>
<html>
<head>
<meta charset="UTF-8">
<title>Genestrip Web: DB <%=dbid%></title>
<script type="text/javascript">
var state = {};
state.currentLan = "<%=lan%>";
dbId=<%=dbid%>;

var mainApp = "<%=request.getContextPath()%>";
var restPath = "<%=request.getContextPath()%>" + "/rest";
</script>
<script src="dbinfo/dbinfo.js?v=<%=version%>"></script>
<link rel="stylesheet" type="text/css" href="ui.css?v=<%=version%>">
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
									<use href="feather-sprite.svg#bar-chart-2" /></svg>
					<span id="dbtitle"></span>
				</div>
				<div class="hline" style="display: block; margin-top: 2px;"></div>
			</div>
			<div class="content">
				<div
					style="display: inline-block; width: 200px; white-space: nowrap;">
					<span data-i18n="search"></span> <input type="text"
						id="dbsearchfield" oninput="searchInTable()" maxlength="10">
					<button type="button" class="sbutton" id="cleardatasearch"
						onclick="document.getElementById('dbsearchfield').value='';searchInTable();">
						<svg class="featherc">
												<use href="feather-sprite.svg#x" /></svg>
					</button>
				</div>
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
		style="position: relative; flex-grow: 1; overflow: scroll; margin: 0 12px 8px 12px;">
		<table class="datatable" id="dbtable">
			<thead>
				<tr>
					<th class="sortable" onclick="sortTableData('line')"><span
						data-i18n="line"></span>
					<div style="width: 16px; display: inline-block">
							<svg id="linedown" class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="lineup" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('name')"><span
						data-i18n="name"></span>
					<div style="width: 16px; display: inline-block">
							<svg id="namedown" style="display: none;" class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="nameup" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('rank')"><span
						data-i18n="rank"></span>
					<div style="width: 16px; display: inline-block">
							<svg id="rankdown" style="display: none;" class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="rankup" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('taxid')"><span
						data-i18n="taxid"></span>
					<div style="width: 16px; display: inline-block">
							<svg id="taxiddown" style="display: none;" class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="taxidup" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('kmers')"><span
						data-i18n="kmers"></span>
					<div style="width: 16px; display: inline-block">
							<svg id="kmersdown" style="display: none;" class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="kmersup" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
				</tr>
			</thead>
			<tbody>
			</tbody>
		</table>
	</div>
</body>
</html>