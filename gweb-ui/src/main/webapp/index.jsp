<%@page session="false"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="org.metagene.gweb.service.dto.Job"%>
<%@page import="org.metagene.gweb.service.dto.DB"%>
<%@page import="org.metagene.gweb.service.dto.User"%>
<%@page import="org.metagene.gweb.service.dto.Person"%>
<%@page import="org.metagene.gweb.Version"%>
<%
String version = Version.class.getPackage().getImplementationVersion();
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Genestrip Web</title>
<jsp:include page="js.jsp" />
<link rel="stylesheet" type="text/css" href="ui.css?v=<%=version%>">
</head>
<body onload="main()">
	<div>
		<table class="formtable">
			<tbody>
				<tr>
					<td style="height: 40px;width: 1%;white-space: nowrap;text-align: left;"><jsp:include page="topleft.jsp" /></td>
					<td style="text-align: left;"><span class="title">Genestrip Web</span></td>
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
	<div class="down" id="screens" style="position: relative;">
		<div id="login" style="display: none;" class="center">
			<span class="down" style="font-weight: bold;" data-i18n="pleaseLogin"></span>
			<form id="loginform"></form>
			<table class="down formtable">
				<colgroup>
					<col span="1" style="width: 100px;">
					<col span="1" style="width: 250px;">
				</colgroup>
				<tbody>
					<tr>
						<td align="right"><span data-i18n="user"></span></td>
						<td><input class="mandatory" type="text" id="fuser"
							oninput="checkLogin()" maxlength="<%=User.LOGIN_SIZE%>"
							form="loginform"></td>
					</tr>
					<tr>
						<td align="right"><span data-i18n="password"></span></td>
						<td><input type="password" id="fpassword"
							oninput="checkLogin()" maxlength="<%=User.PASSWORD_SIZE%>"
							form="loginform"></td>
					</tr>
					<tr>
						<td></td>
						<td align="right">
							<button type="button" id="loginbutton" onclick="loginUser()"
								form="loginform">
								<span data-i18n="login"></span>
								<svg class="featherc">
									<use href="feather-sprite.svg#log-in" /></svg>
							</button>
						<td>
					</tr>
				</tbody>
			</table>
			<div id="loginfailed">
				<span data-i18n="loginfailed"></span>
			</div>
			<div id="autologout">
				<span data-i18n="autologout"></span>
			</div>
		</div>
		<div id="main" style="display: none; position: relative;">
			<div id="tabs">
				<div class="tab" id="jobstab" onclick="showTab('jobs')">
					<svg class="feather">
									<use href="feather-sprite.svg#cpu" /></svg>
					<span data-i18n="jobstab"></span><span id="jobschanged"></span>
				</div>
				<div class="tab" id="restab" onclick="showTab('res')">
					<svg class="feather">
									<use href="feather-sprite.svg#cloud-snow" /></svg>
					<span data-i18n="restab"></span><span id="reschanged"></span>
				</div>
				<div class="tab" id="dbstab" onclick="showTab('dbs')">
					<svg class="feather">
									<use href="feather-sprite.svg#database" /></svg>
					<span data-i18n="dbstab"></span><span id="dbschanged"></span>
				</div>
				<div class="tab" id="userstab" onclick="showTab('users')">
					<svg class="feather">
									<use href="feather-sprite.svg#user" /></svg>
					<span data-i18n="userstab"></span><span id="userschanged"></span>
				</div>
				<div class="tab" id="personstab" onclick="showTab('persons')">
					<svg class="feather">
									<use href="feather-sprite.svg#smile" /></svg>
					<span data-i18n="personstab"></span><span id="personschanged"></span>
				</div>
				<div class="hline" style="display: block; margin-top: 2px;"></div>
			</div>
			<div id="contents">
				<div class="content" id="jobs" style="display: none;">
					<div
						style="display: inline-block; white-space: nowrap; padding: 0px 4px 4px 0px;">
						<span data-i18n="search"></span> <input type="text"
							id="jobsearchfield" oninput="searchInTable('job')" maxlength="10"
							style="width: auto">
						<button type="button" class="sbutton" id="clearjobsearch"
							onclick="document.getElementById('jobsearchfield').value='';searchInTable('job');">
							<svg class="featherc">
												<use href="feather-sprite.svg#x" /></svg>
						</button>
					</div>
					<div class="div-table-scroll">
						<table class="datatable" id="jobtable">
							<thead>
								<tr>
									<th class="sortable" onclick="sortTableData('job', 'id')"><span
										data-i18n="id"></span>
										<div style="width: 16px; display: inline-block">
											<svg id="jobiddown" class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
											<svg id="jobidup" style="display: none;"
												class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
										</div></th>
									<th class="sortable" onclick="sortTableData('job', 'name')"><span
										data-i18n="jobname"></span>
										<div style="width: 16px; display: inline-block">
											<svg id="jobnamedown" style="display: none;"
												class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
											<svg id="jobnameup" style="display: none;"
												class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
										</div></th>
									<th><span data-i18n="dbid"></span></th>
									<th><span data-i18n="userid"></span></th>
									<th><span data-i18n="fastqfile"></span></th>
									<th><span data-i18n="fastqfile2"></span></th>
									<th><span data-i18n="jobstatus"></span></th>
									<th><span data-i18n="queuepos"></span></th>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
					</div>
					<div class="hline"></div>
					<div class="down">
						<span data-i18n="editjob"></span>
					</div>
					<table class="formtable" id="jobform">
						<tbody>
							<tr>
								<td align="right"><span data-i18n="idtext"></span></td>
								<td id="jobidtext"></td>
								<td align="right"><span data-i18n="jobstatusf"></span></td>
								<td id="jobstatustext"></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="jobnamef"></span></td>
								<td><input type="text" id="jobnamefield"
									oninput="checkJobInForm()" maxlength="<%=Job.NAME_SIZE%>"></td>
								<td align="right"><span data-i18n="jprogress"></span></td>
								<td id="jprogress"></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="fordb"></span></td>
								<td><select id="fordb" oninput="checkJobInForm()"></select></td>
								<td align="right"><span data-i18n="jbytes"></span></td>
								<td id="jbytes"></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="foruser"></span></td>
								<td><select id="foruser" oninput="jobUserChanged()"></select></td>
								<td align="right"><span data-i18n="jduration"></span></td>
								<td id="jduration"></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="jobtype"></span></td>
								<td><select id="jobtype" oninput="jobTypeChanged()">
										<option id="LOCAL_MATCH" value="LOCAL_MATCH"></option>
										<option id="UPLOAD_MATCH" value="UPLOAD_MATCH" disabled></option>
										<option id="RES_MATCH" value="RES_MATCH"></option>
										<option id="DB_INFO" value="DB_INFO" disabled></option>
										<option id="INSTALL_DB" value="INSTALL_DB" disabled></option>
								</select></td>
								<td align="right"><span data-i18n="jenqueued"></span></td>
								<td id="jenqueued"></td>
							</tr>
							<tr>
								<td align="right"><div id="crtext">
										<span data-i18n="classifyReads"></span>
									</div></td>
								<td><div id="crcheckbox">
										<input type="checkbox" id="classifyreads"
											oninput="checkJobInForm()"> <span
											data-i18n="errorrate"></span> <input type="number" min="0"
											step="0.01" id="errorratefield" style="width: 200px;"
											oninput="checkJobInForm()" maxlength="20">
									</div></td>
								<td align="right"><span data-i18n="jstarted"></span></td>
								<td id="jstarted"></td>
							</tr>
							<tr>
								<td rowspan="4" align="right"><div id="filesdivtext">
										<span data-i18n="fastqfilef"></span>
									</div>
									<div id="urlsdivtext">
										<span data-i18n="fastqurlf"></span>
									</div>
									<div id="uploaddivtext">
										<span data-i18n="fastquploadf"></span>
									</div></td>
								<td rowspan="4"><div id="filesdiv">
										<select id="fastqfilesel" oninput="fastqSelectionChanged()"
											multiple></select>
									</div>
									<div id="urlsdiv">
										<select id="fastqurlsel" oninput="fastqURLSelectionChanged()"
											multiple></select>
									</div>
									<div id="uploaddiv">
										<form id="uploadform" method="POST"
											enctype="multipart/form-data">
											<input type="hidden" value="" id="jobid" name="jobid" /> <input
												type="hidden" value="" name="filenames" id="filenames" /><input
												type="hidden" value="" name="filesizes" id="filesizes" /> <input
												type="file" style="display: none;" id="fastqfiles"
												name="fastq" multiple accept=".fq,.fastq,.gz,.gzip"
												onchange="updateUploadFormData()" />
										</form>
									</div>
									<div id="choosefilesdiv">
										<button type="button" id="choosefiles"
											onclick="updateFormFiles()">
											<span data-i18n="choosefiles"></span>
											<svg class="featherc">
												<use href="feather-sprite.svg#folder" /></svg>
										</button>
									</div></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="jfinished"></span></td>
								<td id="jfinished"></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="jlogf"></span></td>
								<td id="jlogf"></td>
							</tr>
							<tr>
								<td align="right"><div id="jrestext">
										<span data-i18n="jres"></span>
									</div></td>
								<td id="jres"></td>
							</tr>
							<tr>
								<td></td>
								<td align="right"><div id="showdirdiv">
										<button type="button" id="jshowdir" onclick="showFastqDir()">
											<span data-i18n="jshowdir"></span>
											<svg class="featherc">
												<use href="feather-sprite.svg#folder" /></svg>
										</button>
									</div></td>
							</tr>
							<tr>
								<td align="right"><div id="jfilestitle">
										<span data-i18n="jfiles"></span>
									</div></td>
								<td colspan="4" id="jfiles"></td>
							</tr>
							<tr>
								<td colspan="4" class="fbline" align="right">
									<div id="startbuttondiv" style="display: inline">
										<button type="button" id="startjobbutton" class="jobstart"
											onclick="startJob()">
											<span data-i18n="enqueue"></span>
											<svg class="featherc">
											<use href="feather-sprite.svg#play" /></svg>
										</button>
									</div>
									<div id="stopbuttondiv" style="display: none">
										<button type="button" id="stopjobbutton" class="jobstop"
											onclick="stopJob()">
											<span data-i18n="stopjob"></span>
											<svg class="featherc">
											<use href="feather-sprite.svg#x-octagon" /></svg>
										</button>
									</div>
									<div id="delaybuttondiv" style="display: none">
										<button type="button" id="delayjobbutton" class="jobdelay"
											onclick="delayedJob()">
											<span data-i18n="enqueue"></span>
											<svg class="featherc">
											<use href="feather-sprite.svg#pause" /></svg>
										</button>
									</div>
									<button type="button" id="deletejobbutton"
										onclick="deleteJob()">
										<span data-i18n="delete"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#trash" /></svg>
									</button>
									<button type="button" id="createjobbutton"
										onclick="createJob()">
										<span data-i18n="create"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#plus-circle" /></svg>
									</button>
									<button type="button" id="savejobbutton" onclick="saveJob()">
										<span data-i18n="save"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#save" /></svg>
									</button>
									<button type="button" id="resetjobbutton" onclick="resetJob()">
										<span data-i18n="reset"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#rotate-ccw" /></svg>
									</button>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
				<div class="content" id="res" style="display: none;">
					<div
						style="display: inline-block; white-space: nowrap; padding: 0px 4px 4px 0px;">
						<span data-i18n="search"></span> <input type="text"
							id="ressearchfield" oninput="searchInTable('res')" maxlength="10"
							style="width: auto">
						<button type="button" class="sbutton" id="clearressearch"
							onclick="document.getElementById('ressearchfield').value='';searchInTable('res');">
							<svg class="featherc">
												<use href="feather-sprite.svg#x" /></svg>
						</button>
					</div>
					<div class="div-table-scroll">
						<table class="datatable" id="restable">
							<thead>
								<tr>
									<th class="sortable" onclick="sortTableData('res', 'id')"><span
										data-i18n="id"></span>
										<div style="width: 16px; display: inline-block">
											<svg id="residdown" class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
											<svg id="residup" style="display: none;"
												class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
										</div></th>
									<th class="sortable" onclick="sortTableData('res', 'name')"><span
										data-i18n="resname"></span>
										<div style="width: 16px; display: inline-block">
											<svg id="resnamedown" style="display: none;"
												class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
											<svg id="resnameup" style="display: none;"
												class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
										</div></th>
									<th><span data-i18n="userid"></span></th>
									<th><span data-i18n="resurl"></span></th>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
					</div>
					<div class="hline"></div>
					<div class="down">
						<span data-i18n="editres"></span>
					</div>
					<table class="formtable" id="resform">
						<tbody>
							<tr>
								<td align="right"><span data-i18n="idtext"></span></td>
								<td id="residtext"></td>
								<td></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="resnamef"></span></td>
								<td><input type="text" id="resnamefield"
									oninput="checkResInForm()" maxlength="<%=DB.NAME_SIZE%>"></td>
								<td></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="foruser"></span></td>
								<td><select id="forresuser" oninput="checkResInForm()"></select></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="restype"></span></td>
								<td><select id="restype" oninput="checkResInForm()">
										<option id="HTTP_URL" value="HTTP_URL"></option>
										<option id="FILE_PATH" value="FILE_PATH" disabled></option>
								</select></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="resurlf"></span></td>
								<td><input type="text" id="resurlfield"
									oninput="checkResInForm()"
									maxlength="<%=DB.DB_FILE_PREFIX_SIZE%>"></td>
								<td></td>
							</tr>
							<tr>
								<td colspan="3" class="fbline" align="right">
									<button type="button" id="deleteresbutton"
										onclick="deleteRes()">
										<span data-i18n="delete"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#trash" /></svg>
									</button>
									<button type="button" id="createresbutton"
										onclick="createRes()">
										<span data-i18n="create"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#plus-circle" /></svg>
									</button>
									<button type="button" id="saveresbutton" onclick="saveRes()">
										<span data-i18n="save"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#save" /></svg>
									</button>
									<button type="button" id="resetresbutton" onclick="resetRes()">
										<span data-i18n="reset"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#rotate-ccw" /></svg>
									</button>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
				<div class="content" id="dbs" style="display: none;">
					<div
						style="display: inline-block; white-space: nowrap; padding: 0px 4px 4px 0px;">
						<span data-i18n="search"></span> <input type="text"
							id="dbsearchfield" oninput="searchInTable('db')" maxlength="10"
							style="width: auto">
						<button type="button" class="sbutton" id="clearressearch"
							onclick="document.getElementById('dbsearchfield').value='';searchInTable('db');">
							<svg class="featherc">
												<use href="feather-sprite.svg#x" /></svg>
						</button>
					</div>
					<div class="div-table-scroll">
						<table class="datatable" id="dbtable">
							<thead>
								<tr>
									<th class="sortable" onclick="sortTableData('db', 'id')"><span
										data-i18n="id"></span>
										<div style="width: 16px; display: inline-block">
											<svg id="dbiddown" class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
											<svg id="dbidup" style="display: none;"
												class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
										</div></th>
									<th class="sortable" onclick="sortTableData('db', 'name')"><span
										data-i18n="dbname"></span>
										<div style="width: 16px; display: inline-block">
											<svg id="dbnamedown" style="display: none;"
												class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
											<svg id="dbnameup" style="display: none;"
												class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
										</div></th>
									<th><span data-i18n="dbfileprefix"></span></th>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
					</div>
					<div class="hline"></div>
					<div class="down">
						<span data-i18n="editdb"></span>
					</div>
					<table class="formtable" id="dbform">
						<tbody>
							<tr>
								<td align="right"><span data-i18n="idtext"></span></td>
								<td id="dbidtext"></td>
								<td></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="dbnamef"></span></td>
								<td><input type="text" id="dbnamefield"
									oninput="checkDBInForm()" maxlength="<%=DB.NAME_SIZE%>"></td>
								<td></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="dbfileprefixf"></span></td>
								<td><input type="text" id="dbfileprefixfield"
									oninput="checkDBInForm()"
									maxlength="<%=DB.DB_FILE_PREFIX_SIZE%>"></td>
								<td></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="dbisinstalled"></span></td>
								<td><input type="checkbox" id="dbisinstalled"
									disabled="true"></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="dbinstallurl"></span></td>
								<td><input type="text" id="dburlfield"
									oninput="checkDBInForm()" maxlength="<%=DB.URL_SIZE%>"></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="dbinstallmd5"></span></td>
								<td><input type="text" id="dbmd5field"
									oninput="checkDBInForm()" maxlength="<%=DB.MD5_SIZE%>"></td>
								<td><button type="button" id="dbinstallbutton"
										onclick="installDB()">
										<span data-i18n="installdb"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#download-cloud" /></svg>
									</button></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="dbinfof"></span></td>
								<td id="dbinfof"></td>
								<td><button type="button" id="dbinfobutton"
										onclick="createDBInfo()">
										<span data-i18n="createdbinfo"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#cpu" /></svg>
									</button></td>
							</tr>
							<tr>
								<td colspan="3" class="fbline" align="right">
									<button type="button" id="deletedbbutton" onclick="deleteDB()">
										<span data-i18n="delete"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#trash" /></svg>
									</button>
									<button type="button" id="createdbbutton" onclick="createDB()">
										<span data-i18n="create"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#plus-circle" /></svg>
									</button>
									<button type="button" id="savedbbutton" onclick="saveDB()">
										<span data-i18n="save"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#save" /></svg>
									</button>
									<button type="button" id="resetdbbutton" onclick="resetDB()">
										<span data-i18n="reset"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#rotate-ccw" /></svg>
									</button>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
				<div class="content" id="users" style="display: none;">
					<div
						style="display: inline-block; white-space: nowrap; padding: 0px 4px 4px 0px;">
						<span data-i18n="search"></span> <input type="text"
							id="usersearchfield" oninput="searchInTable('user')"
							maxlength="10" style="width: auto">
						<button type="button" class="sbutton" id="clearusersearch"
							onclick="document.getElementById('usersearchfield').value='';searchInTable('user');">
							<svg class="featherc">
												<use href="feather-sprite.svg#x" /></svg>
						</button>
					</div>
					<div class="div-table-scroll">
						<table class="datatable table-scroll" id="usertable">
							<thead>
								<tr>
									<th class="sortable" onclick="sortTableData('user', 'id')"><span
										data-i18n="id"></span>
										<div style="width: 16px; display: inline-block">
											<svg id="useriddown" class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
											<svg id="useridup" style="display: none;"
												class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
										</div></th>
									<th class="sortable" onclick="sortTableData('user', 'login')"><span
										data-i18n="username"></span>
										<div style="width: 16px; display: inline-block">
											<svg id="userlogindown" style="display: none;"
												class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
											<svg id="userloginup" style="display: none;"
												class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
										</div></th>
									<th><span data-i18n="throle"></span></th>
									<th><span data-i18n="personid"></span></th>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
					</div>
					<div class="hline"></div>
					<div class="down">
						<span data-i18n="edituser"></span>
					</div>
					<table class="formtable" id="userform">
						<tbody>
							<tr>
								<td align="right"><span data-i18n="idtext"></span></td>
								<td id="useridtext"></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="user"></span></td>
								<td><input type="text" id="usernamefield"
									oninput="checkUserInForm()" maxlength="<%=User.LOGIN_SIZE%>"></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="password"></span></td>
								<td><input type="password" id="passwordfield"
									oninput="checkUserInForm()" maxlength="<%=User.PASSWORD_SIZE%>"></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="selectrole"></span></td>
								<td><select id="selectrole" oninput="checkUserInForm()">
										<option value="NO_LOGIN" data-i18n="NO_LOGIN"></option>
										<option value="VIEW" data-i18n="VIEW"></option>
										<option value="RUN_JOBS" data-i18n="RUN_JOBS"></option>
										<option value="ADMIN" data-i18n="ADMIN"></option>
								</select></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="forperson"></span></td>
								<td><select id="forperson" oninput="checkUserInForm()"></select></td>
							</tr>
							<tr>
								<td colspan="2" class="fbline" align="right">
									<button type="button" id="deleteuserbutton"
										onclick="deleteUser()">
										<span data-i18n="delete"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#trash" /></svg>
									</button>
									<button type="button" id="createuserbutton"
										onclick="createUser()">
										<span data-i18n="create"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#plus-circle" /></svg>
									</button>
									<button type="button" id="saveuserbutton" onclick="saveUser()">
										<span data-i18n="save"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#save" /></svg>
									</button>
									<button type="button" id="resetuserbutton"
										onclick="resetUser()">
										<span data-i18n="reset"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#rotate-ccw" /></svg>
									</button>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
				<div class="content" id="persons" style="display: none;">
					<div
						style="display: inline-block; white-space: nowrap; padding: 0px 4px 4px 0px;">
						<span data-i18n="search"></span> <input type="text"
							id="personsearchfield" oninput="searchInTable('person')"
							maxlength="10" style="width: auto">
						<button type="button" class="sbutton" id="clearpersonsearch"
							onclick="document.getElementById('personsearchfield').value='';searchInTable('person');">
							<svg class="featherc">
												<use href="feather-sprite.svg#x" /></svg>
						</button>
					</div>
					<div class="div-table-scroll">
						<table class="datatable" id="persontable">
							<thead>
								<tr>
									<th class="sortable" onclick="sortTableData('person', 'id')"><span
										data-i18n="id"></span>
										<div style="width: 16px; display: inline-block">
											<svg id="personiddown" class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
											<svg id="personidup" style="display: none;"
												class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
										</div></th>
									<th><span data-i18n="firstname"></span></th>
									<th class="sortable"
										onclick="sortTableData('person', 'lastName')"><span
										data-i18n="lastname"></span>
										<div style="width: 16px; display: inline-block">
											<svg id="personlastNamedown" style="display: none;"
												class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-down" /></svg>
											<svg id="personlastNameup" style="display: none;"
												class="featherc sorticon">
												<use href="feather-sprite.svg#chevron-up" /></svg>
										</div></th>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
					</div>
					<div class="hline"></div>
					<div class="down">
						<span data-i18n="editperson"></span>
					</div>
					<table class="formtable" id="personform">
						<tbody>
							<tr>
								<td align="right"><span data-i18n="idtext"></span></td>
								<td id="personidtext"></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="firstnamef"></span></td>
								<td><input type="text" id="firstnamefield"
									oninput="checkPersonInForm()"
									maxlength="<%=Person.FIRST_NAME_SIZE%>"></td>
							</tr>
							<tr>
								<td align="right"><span data-i18n="lastnamef"></span></td>
								<td><input type="text" id="lastnamefield"
									oninput="checkPersonInForm()"
									maxlength="<%=Person.LAST_NAME_SIZE%>"></td>
							</tr>
							<tr>
								<td colspan="2" class="fbline" align="right">
									<button type="button" id="deletepersonbutton"
										onclick="deletePerson()">
										<span data-i18n="delete"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#trash" /></svg>
									</button>
									<button type="button" id="createpersonbutton"
										onclick="createPerson()">
										<span data-i18n="create"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#plus-circle" /></svg>
									</button>
									<button type="button" id="savepersonbutton"
										onclick="savePerson()">
										<span data-i18n="save"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#save" /></svg>
									</button>
									<button type="button" id="resetpersonbutton"
										onclick="resetPerson()">
										<span data-i18n="reset"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#rotate-ccw" /></svg>
									</button>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
			<div class="hline"></div>
			<div id="footer" style="position: relative;">
				<div class="down left"><jsp:include page="leftfooter.jsp" /></div>
				<div id="userinfo">
					<div class="down right">
						<span data-i18n="loggedinas"></span> <span id="username"></span> /
						<span data-i18n="role"></span> <span id="userrole"></span>
						<button type="button" onclick="logoutUser()">
							<span data-i18n="logout"></span>
							<svg class="featherc">
									<use href="feather-sprite.svg#log-out" /></svg>
						</button>
					</div>
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
		<div id="modaldialog" class="modal" style="display: none;">
			<div class="modal-content">
				<div id="mconfirm">
					<table class="formtable">
						<tbody>
							<tr>
								<td id="dialogmessage"></td>
							</tr>
							<tr>
								<td align="right">
									<button type="button" onclick="cancelConfirm()">
										<span data-i18n="cancel"></span>
										<svg class="featherc">
									<use href="feather-sprite.svg#x" /></svg>
									</button>
									<button type="button" onclick="okConfirm()">
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
</body>
</html>