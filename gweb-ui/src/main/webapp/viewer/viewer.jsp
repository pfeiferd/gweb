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
<title>Genestrip Web: Job <%=jobid%></title>
<script type="text/javascript">
var state = {};
state.currentLan = "<%=lan%>";
jobId=<%=jobid%>;

var mainApp = "<%=request.getContextPath()%>";
var restPath = "<%=request.getContextPath()%>" + "/rest";
</script>
<script src="viewer/viewer.js?v=<%=version%>"></script>
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
									<use href="feather-sprite.svg#table" /></svg>
					<span id="jobtitle"></span><span data-i18n="on"></span><span
						id="dbtitle"></span>
				</div>
				<div class="hline" style="display: block; margin-top: 2px;"></div>
			</div>
			<div class="content">
				<div style="display: inline-block; white-space: nowrap;">
					<span data-i18n="search"></span> <input class="other" type="text"
						id="datasearchfield" oninput="searchInTable()" maxlength="10" style="width:auto">
					<button type="button" class="sbutton" id="cleardatasearch"
						onclick="document.getElementById('datasearchfield').value='';searchInTable();">
						<svg class="featherc">
												<use href="feather-sprite.svg#x" /></svg>
					</button>
					<div style="width: 8px; display: inline-block"></div>
				</div>
				<div
					style="display: inline-block; white-space: nowrap;">
					<span data-i18n="colorby"></span> <select id="colorby" style="width:auto" autocomplete="off"
						oninput="coloringChanged();">
						<option class="colopt" id="optnocol" value="nocol" selected="selected"></option>
						<option class="colopt" id="optreads" value="reads"></option>
						<option class="colopt" id="optkmersfr" value="kmersfr"></option>
						<option class="colopt" id="optkmers" value="kmers"></option>
						<option class="colopt" id="optukmers" value="ukmers"></option>
						<option class="colopt" id="optcontigs" value="contigs"></option>
						<option class="colopt" id="optavgclen" value="avgclen"></option>
						<option class="colopt" id="optmaxclen" value="maxclen"></option>
						<option class="colopt" id="optdbcov" value="dbcov"></option>
						<option class="colopt" id="optukeukr" value="ukeukr"></option>
					</select>
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
		<table class="datatable" id="datatable">
			<thead>
				<tr>
					<th class="sortable" onclick="sortTableData('pos')"><span
						data-i18n="pos"></span>
						<div style="width: 16px; display: inline-block">
							<svg id="posdown" class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="posup" style="display: none;" class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('name')"><span
						data-i18n="name"></span>
						<div style="width: 16px; display: inline-block">
							<svg id="namedown" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="nameup" style="display: none;" class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('rank')"><span
						data-i18n="rank"></span>
						<div style="width: 16px; display: inline-block">
							<svg id="rankdown" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="rankup" style="display: none;" class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('taxid')"><span
						data-i18n="taxid"></span>
						<div style="width: 16px; display: inline-block">
							<svg id="taxiddown" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="taxidup" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('reads')"><span
						data-i18n="reads"></span>
						<div style="width: 16px; display: inline-block">
							<svg id="readsdown" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="readsup" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('kmersfr')"><span
						data-i18n="kmersfr"></span>
						<div style="width: 16px; display: inline-block">
							<svg id="kmersfrdown" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="kmersfrup" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('kmers')"><span
						data-i18n="kmers"></span>
						<div style="width: 16px; display: inline-block">
							<svg id="kmersdown" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="kmersup" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('ukmers')"><span
						data-i18n="ukmers"></span>
						<div style="width: 16px; display: inline-block">
							<svg id="ukmersdown" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="ukmersup" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('contigs')"><span
						data-i18n="contigs"></span>
						<div style="width: 16px; display: inline-block">
							<svg id="contigsdown" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="contigsup" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('avgclen')"><span
						data-i18n="avgclen"></span>
						<div style="width: 16px; display: inline-block">
							<svg id="avgclendown" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="avgclenup" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('maxclen')"><span
						data-i18n="maxclen"></span>
						<div style="width: 16px; display: inline-block">
							<svg id="maxclendown" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="maxclenup" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('reads1k')"><span
						data-i18n="reads1k"></span>
						<div style="width: 16px; display: inline-block">
							<svg id="reads1kdown" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="reads1kup" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('readsbps')"><span
						data-i18n="readsbps"></span>
						<div style="width: 16px; display: inline-block">
							<svg id="readsbpsdown" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="readsbpsup" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('avgreadlen')"><span
						data-i18n="avgreadlen"></span>
						<div style="width: 16px; display: inline-block">
							<svg id="avgreadlendown" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="avgreadlenup" style="display: none;" class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('dbcov')"><span
						data-i18n="dbcov"></span>
						<div style="width: 16px; display: inline-block">
							<svg id="dbcovdown" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="dbcovup" style="display: none;"
								class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('euk')"><span
						data-i18n="euk"></span>
						<div style="width: 16px; display: inline-block">
							<svg id="eukdown" style="display: none;" class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="eukup" style="display: none;" class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('ukeukr')"><span
							data-i18n="ukeukr"></span>
						<div style="width: 16px; display: inline-block">
							<svg id="ukeukrdown" style="display: none;" class="featherc sorticon">
								<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="ukeukrup" style="display: none;" class="featherc sorticon">
								<use href="feather-sprite.svg#chevron-up" /></svg>
						</div></th>
					<th class="sortable" onclick="sortTableData('dbkmers')"><span
							data-i18n="dbkmers"></span>
						<div style="width: 16px; display: inline-block">
							<svg id="dbkmersdown" style="display: none;" class="featherc sorticon">
								<use href="feather-sprite.svg#chevron-down" /></svg>
							<svg id="dbkmersup" style="display: none;" class="featherc sorticon">
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