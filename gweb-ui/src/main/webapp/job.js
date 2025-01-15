/* Software: gweb, License: Apache 2.0 subject to “Commons Clause” License Condition v1.0, Licensor: Daniel Pfeifer (daniel.pfeifer@progotec.de) */
//
allData["job"] = null;
var selectedJob = null;

var pendingJobsInterval = null;

var startedJobId = null;

var cachedUserId = null;
var cachedFileSel1 = null;
var cachedFileSel2 = null;

var delayUploadStart = false;
var ssState = "start";
var selfCancelledJobId = null;
var enqueuedUploadJobId = null;

function clearJobForm() {
	bindJobToForm(newJob());
	checkJobInForm();
	enableJobForm(false);
}

function loadJobs() {
	if (loggedInUser == null) {
		if (pendingJobsInterval != null) {
			clearInterval(pendingJobsInterval);
		}
		allData["job"] = [];
		selectedJob = null;
		clearTempJobGlobals();
	}
	else if (allData["job"] == null) {
		if (pendingJobsInterval != null) {
			clearInterval(pendingJobsInterval);
		}
		var request = createAjaxRequest();
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				allData["job"] = evalOrHandleError(request);
				selectedJob = null;
				clearTempJobGlobals();
				clearJobForm();

				loadRes(false);
				loadDBs(false);
				loadUsers(false);
				updateJobTable();
				pendingJobsInterval = setInterval(checkJobsRound, 1000);
			}
		};
		if (isAdmin(loggedInUser)) {
			request.open("GET", restPath + "/JobService/getAll/", true);
		}
		else {
			request.open("GET", restPath + "/JobService/getByUser/" + loggedInUser.id, true);
		}
		request.send();
	}
}

function clearJobData() {
	allData["job"] = null;
	selectedJob = null;
	if (pendingJobsInterval != null) {
		clearInterval(pendingJobsInterval);
	}
	clearTempJobGlobals();
	clearJobForm();
	clearTable(document.getElementById("jobtable"));
}


function clearTempJobGlobals() {
	startedJobId = null;
	cachedUserId = null;
	cachedFileSel1 = null;
	cachedFileSel2 = null;
}

function updateJobTable() {
	var table = document.getElementById("jobtable");
	var input = document.getElementById("jobsearchfield");
	var filter = input.value.toUpperCase();
	clearTable(table);

	for (var i = 0; i < allData["job"].length; i++) {
		var row = createTableRowForJob(i);
		filterTableRow(filter, row);
		table.children[1].appendChild(row);
	}
}

function createTableRowForJob(index) {
	var job = allData["job"][index];
	var fastq1 = "";
	var fastq2 = "";
	if (job.jobType == "LOCAL_MATCH" || job.jobType == "UPLOAD_MATCH") {
		fastq1 = job.fastqFile == null ? "" : htmlEscape(job.fastqFile);
		fastq2 = job.fastqFile2 == null ? "" : htmlEscape(job.fastqFile2);
	}
	else if (job.jobType == "RES_MATCH") {
		if (resById != null) {
			if (job.resourceId >= 0) {
				fastq1 = job.resourceId;
				if (resById[job.resourceId] != null) {
					fastq1 = fastq1 + ": " + htmlEscape(resById[job.resourceId].name);
				}
			}
			if (job.resourceId2 >= 0) {
				fastq2 = job.resourceId2;
				if (resById[job.resourceId2] != null) {
					fastq2 = fastq2 + ": " + htmlEscape(resById[job.resourceId2].name);
				}
			}
		}
	}

	var tr = "<tr class=\"";
	tr = tr + ((job == selectedJob) ? "tselected" : "tnormal");
	tr = tr + "\" onclick=\"selectJob(" + index + ")\">";
	tr = tr + "<td>" + htmlEscape(job.id) + "</td>";
	tr = tr + "<td>" + htmlEscape(job.name) + "</td>";
	tr = tr + "<td>" + htmlEscape(job.dbId);
	if (dbsById != null && dbsById[job.dbId] != null) {
		tr = tr + ": " + htmlEscape(dbsById[job.dbId].name);
	}
	tr = tr + "</td>";
	tr = tr + "<td>" + htmlEscape(job.userId);
	if (usersById != null && usersById[job.userId] != null) {
		tr = tr + ": " + htmlEscape(usersById[job.userId].login);
	}
	tr = tr + "</td>";
	tr = tr + "<td class=\"limcol\">" + fastq1 + "</td>";
	tr = tr + "<td  class=\"limcol\">" + fastq2 + "</td>";
	tr = tr + "<td><span id=\"tstatusid" + job.id + "\" data-i18n=\"" + job.status + "\">" + i18n[state.currentLan][job.status] + "</span></td>";
	tr = tr + "<td id=\"tjobid" + job.id + "\">";
	if (job.status == "FINISHED") {
		tr = tr + "100%";
	}
	tr = tr + "</td>";
	tr = tr + "</tr>";

	return createTableRow(tr);
}

function updateJobTableEntry(index) {
	var table = document.getElementById("jobtable");
	// The header counts as part of the rows - so +1.
	table.children[1].deleteRow(index);
	var row = table.children[1].insertRow(index);
	row.innerHTML = createTableRowForJob(index).innerHTML;
	row.onclick = function() { selectJob(index); };
	row.class = "tnormal";
}

function selectJob(i) {
	if (selectedJob == allData["job"][i]) {
		return;
	}
	var changeF = () => {
		selectedJob = allData["job"][i];
		var table = document.getElementById("jobtable");
		unselectTableRows(table);
		if (i < table.children[1].children.length) {
			table.children[1].children[i].className = "tselected";
		}

		bindJobToForm(selectedJob);
		checkJobInForm();
		doSelectedJobEnabling();
	};
	if (hasJobInFormChanged()) {
		cConfirm("reallyChangeSelection", changeF);
	}
	else {
		changeF();
	}
}

function doSelectedJobEnabling() {
	enableJobForm((selectedJob.status == null || selectedJob.status == "CREATED") && isJobRunner(loggedInUser));
}

function resetJob() {
	if (selectedJob != null) {
		bindJobToForm(selectedJob);
		checkJobInForm();
	}
}

function checkJobInForm() {
	var validated = validateJobInForm();
	var changed = hasJobInFormChanged();

	dataChangedInTab("jobs", changed);

	document.getElementById("deletejobbutton").disabled = !isJobRunner(loggedInUser) || selectedJob == null || changed || selectedJob.id == -1;
	document.getElementById("createjobbutton").disabled = !isJobRunner(loggedInUser) || (selectedJob != null && changed);
	document.getElementById("resetjobbutton").disabled = !changed;
	document.getElementById("savejobbutton").disabled = !validated || !changed;

	var startButtonEnabled = validated && !changed && selectedJob != null && selectedJob.status == "CREATED" && (selectedJob.jobType != "UPLOAD_MATCH" ||
		(selectedJob.jobType == "UPLOAD_MATCH" && document.getElementById("fastqfiles").files.length > 0))
	document.getElementById("startjobbutton").disabled = !startButtonEnabled;
	document.getElementById("delayjobbutton").disabled = !startButtonEnabled;

	var classify = document.getElementById("classifyreads").checked;
	document.getElementById("errorratefield").disabled = !classify;
	if (!classify) {
		var v = parseFloat(document.getElementById("errorratefield").value);
		if (!(v >= 0)) {
			document.getElementById("errorratefield").value = errorRateDefault;
		}
	}

	var jobType = document.getElementById("jobtype").value;
	document.getElementById("choosefiles").disabled = !(selectedJob != null && ((selectedJob.status == null || selectedJob.status == "CREATED") && jobType == "UPLOAD_MATCH"));

	updateStartStopButtons((selectedJob != null && (selectedJob.status == "STARTED" || selectedJob.status == "ENQUEUED")) ? "stop" : "start");
}

function updateStartStop() {
	updateStartStopButtons(ssState)
}

function updateStartStopButtons(s) {
	document.getElementById("delaybuttondiv").style.display = "none";
	document.getElementById("stopbuttondiv").style.display = "none";
	document.getElementById("startbuttondiv").style.display = "none";

	document.getElementById("stopjobbutton").disabled = false;
	ssState = s;
	if (s == "start") {
		var jobType = document.getElementById("jobtype").value;
		if (jobType == "UPLOAD_MATCH" && delayUploadStart) {
			s = "delay";
		}
	}
	else if (s == "stop" && selectedJob != null) {
		if (enqueuedUploadJobId == selectedJob.id && selectedJob.status == "ENQUEUED") {
			document.getElementById("stopjobbutton").disabled = true;
		}
	}
	document.getElementById(s + "buttondiv").style.display = "inline";
}

function hasJobInFormChanged() {
	return selectedJob != null && !deepEqual(selectedJob, completeJobData(extractJobFromForm(newJob()), selectedJob));
}

function completeJobData(job, fromJob) {
	job.started = fromJob.started;
	job.enqueued = fromJob.enqueued;
	job.finished = fromJob.finished;
	job.status = fromJob.status;
	//	job.jobType = fromJob.jobType;
	job.coveredBytes = fromJob.coveredBytes;
	if (fromJob.status == "FINISHED" || fromJob.status == "E_CANCELED" || fromJob.status == "S_CANCELED") {
		job.fastqFile = fromJob.fastqFile;
		job.fastqFile2 = fromJob.fastqFile2;
		job.resourceId = fromJob.resourceId;
		job.resourceId2 = fromJob.resourceId2;
	}
	else if (job.jobType == "UPLOAD_MATCH" && fromJob.jobType == "UPLOAD_MATCH") {
		job.fastqFile = fromJob.fastqFile;
		job.fastqFile2 = fromJob.fastqFile2;
	}

	return job;
}

function validateJobInForm() {
	var job = newJob();
	extractJobFromForm(job);

	var isMatchType = job.jobType == "RES_MATCH" || job.jobType == "LOCAL_MATCH" || job.jobType == "UPLOAD_MATCH";

	var validU = !updateMandatory("jobnamefield", selectedJob != null && job.name == "");
	var validF = !updateMandatory("fastqfilesel", selectedJob != null && job.status == null && job.fastqFile == null && isMatchType);
	var validURL = !updateMandatory("fastqurlsel", selectedJob != null && job.status == null && job.resourceId == -1 && isMatchType);
	var validD = !updateMandatory("fordb", selectedJob != null && job.dbId == -1);
	var validUser = !updateMandatory("foruser", selectedJob != null && job.userId == -1);
	var validER = !updateMandatory("errorratefield", job.classifyReads && selectedJob != null && isMatchType && !(job.errorRate >= 0));

	return validU && (validF || validURL || job.jobType == "UPLOAD_MATCH") && validD && validUser && validER;
}

function createJob() {
	var table = document.getElementById("jobtable");
	unselectTableRows(table);

	selectedJob = newJob();
	selectedJob.userId = loggedInUser.id;
	bindJobToForm(selectedJob);

	checkJobInForm();
	doSelectedJobEnabling();
}

function newJob() {
	var job = {};
	job.id = -1;
	job.name = "";
	job.fastqFile = null;
	job.fastqFile2 = null;
	job.resourceId = -1;
	job.resourceId2 = -1;
	job.dbId = -1;
	job.userId = -1;
	job.enqueued = null;
	job.started = null;
	job.finished = null;
	job.status = null;
	job.coveredBytes = null;
	job.jobType = "UPLOAD_MATCH";
	job.classifyReads = false;
	job.errorRate = errorRateDefault;

	return job;
}

function saveJob() {
	extractJobFromForm(selectedJob);

	var request = createAjaxRequest();
	if (selectedJob.id == -1) {
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				selectedJob.id = evalOrHandleError(request);
				selectedJob.status = "CREATED";
				bindJobToForm(selectedJob);
				allData["job"].push(selectedJob);
				updateJobTableAndForm();
			}
		};
		request.open("POST", restPath + "/JobService/create", true);
	}
	else {
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				evalOrHandleError(request);
				updateJobTableAndForm();
			}
		};
		request.open("POST", restPath + "/JobService/update", true);
	}
	request.setRequestHeader("Content-Type", "application/json");
	request.send(JSON.stringify(selectedJob));
}

function updateJobTableAndForm() {
	clearSearchField("job");
	sortByCurrentField("job");
	updateJobTable();
	selectJobById(selectedJob.id);
	checkJobInForm();
	enableJobForm(true);
}

function deleteJob() {
	cConfirm("reallyDeleteSelection", () => {
		var request = createAjaxRequest();
		if (selectedJob.id != -1) {
			request.onreadystatechange = function() {
				if (request.readyState == 4) {
					evalOrHandleError(request);
					var index = allData["job"].indexOf(selectedJob);
					if (index != -1) {
						allData["job"].splice(index, 1);
					}
					selectedJob = null;
					clearSearchField("job");
					sortByCurrentField("job");
					updateJobTable();
					bindJobToForm(newJob());
					checkJobInForm();
					enableJobForm(false);
				}
			};
			request.open("GET", restPath + "/JobService/remove/" + selectedJob.id, true);
			request.setRequestHeader("Content-Type", "application/json");
			request.send();
		}
	});
}

function selectJobById(id) {
	for (var i = 0; i < allData["job"].length; i++) {
		if (allData["job"][i].id == id) {
			var table = document.getElementById("jobtable");
			var child = table.children[1].children[i];
			if (child != null) {
				child.className = "tselected";
			}
		}
	}
}

function getJobById(id) {
	for (var i = 0; i < allData["job"].length; i++) {
		if (allData["job"][i].id == id) {
			return allData["job"][i];
		}
	}
	return null;
}

function extractJobFromForm(job) {
	var id = document.getElementById("jobidtext").innerHTML;
	job.id = id == "" ? -1 : parseInt(id);
	job.name = document.getElementById("jobnamefield").value;

	job.jobType = document.getElementById("jobtype").value;
	job.resourceId = -1;
	job.resourceId2 = -1;
	job.fastqFile = null;
	job.fastqFile2 = null;
	if (job.jobType == "LOCAL_MATCH") {
		var fileOptions = document.getElementById("fastqfilesel").selectedOptions;
		job.fastqFile = fileOptions.length >= 1 ? fileOptions[0].value : null;
		job.fastqFile2 = fileOptions.length >= 2 ? fileOptions[1].value : null;
	}
	else if (job.jobType == "RES_MATCH") {
		var urlOptions = document.getElementById("fastqurlsel").selectedOptions;
		job.resourceId = urlOptions.length >= 1 ? parseInt(urlOptions[0].value) : -1;
		job.resourceId2 = urlOptions.length >= 2 ? parseInt(urlOptions[1].value) : -1;
	}

	var options = document.getElementById("fordb").selectedOptions;
	job.dbId = options.length == 0 ? -1 : parseInt(options[0].value);

	job.userId = getJobUserFromForm();

	job.classifyReads = document.getElementById("classifyreads").checked && (job.jobType == "LOCAL_MATCH" || job.jobType == "RES_MATCH" || job.jobType == "UPLOAD_MATCH");
	job.errorRate = parseFloat(document.getElementById("errorratefield").value);

	return job;
}

function bindJobToForm(job) {
	document.getElementById("jobidtext").innerHTML = job.id == -1 ? "" : job.id;
	document.getElementById("jobnamefield").value = job.name;
	var status = job.status == null ? "NEW" : job.status;
	document.getElementById("jobstatustext").innerHTML = "<span data-i18n=\"" + status + "\">" + i18n[state.currentLan][status] + "</span>";

	document.getElementById("foruser").value = job.userId;
	document.getElementById("fordb").value = job.dbId;
	document.getElementById("jobtype").value = job.jobType;
	switchFastqTypeDiv(job.jobType);

	loadFastqFileList(job.userId, true);
	selectFastqs(job.userId, job.fastqFile, job.fastqFile2);
	updateFastqURLList(job.userId);
	selectFastqURLs(job.resourceId, job.resourceId2);
	updateFormDates(job);

	if (job.started != null && job.finished != null) {
		var duration = (job.finished - job.started) / 1000 / 60;
		document.getElementById("jduration").innerHTML = Math.floor(duration) + " min";
	}
	else {
		document.getElementById("jduration").innerHTML = "";
	}
	document.getElementById("jbytes").innerHTML = job.coveredBytes != null && job.status == "FINISHED" ? (Math.floor(job.coveredBytes / 1024 / 1024) + " MBytes") : "";
	document.getElementById("jprogress").innerHTML = job.status == "FINISHED" ? "100%" : "";

	document.getElementById("jres").innerHTML = "";
	document.getElementById("jlogf").innerHTML = "";

	var match = job.jobType == "LOCAL_MATCH" || job.jobType == "RES_MATCH" || job.jobType == "UPLOAD_MATCH";
	document.getElementById("classifyreads").checked = match && job.classifyReads;
	document.getElementById("errorratefield").value = job.errorRate;
	document.getElementById("crtext").style.display = match ? "block" : "none";
	document.getElementById("crcheckbox").style.display = match ? "block" : "none";

	if (job.id != -1) {
		if (job.status == "STARTED" || job.status == "S_CANCELED" || job.status == "FINISHED") {
			if (isJobRunner(loggedInUser)) {
				var request = createAjaxRequest();
				request.onreadystatechange = function() {
					if (request.readyState == 4) {
						var exists = evalOrHandleError(request);
						if (exists) {
							var anchor = "<a class=\"datalink\" target=\"_blank\" href=\"" + restPath + "/JobService/getLog/" + job.id + "\">" + job.id + ".log</a>"
							var buttonText = i18n[state.currentLan]["viewjobres"];
							var button = "<button type=\"button\" onclick=\"viewLogRes(" + job.id + ")\">" +
								"<span data-i18n=\"viewjobres\">" + buttonText + "</span> " +
								"<svg class=\"featherc\"><use href=\"feather-sprite.svg#file-text\"/></svg></button>";
							document.getElementById("jlogf").innerHTML = anchor + " " + button;
						}
					}
				};
				request.open("GET", restPath + "/JobService/isLogExists/" + job.id, false);
				request.send();
			}
		}
		document.getElementById("jrestext").style.display = match ? "block" : "none";
		if (match && job.status == "FINISHED") {
			var request = createAjaxRequest();
			request.onreadystatechange = function() {
				if (request.readyState == 4) {
					var exists = evalOrHandleError(request);
					if (exists) {
						var anchor = "<a class=\"datalink\" target=\"_blank\" href=\"" + restPath + "/JobService/getCSV/" + job.id + "\">" + job.id + ".csv</a>";
						var buttonText = i18n[state.currentLan]["viewjobres"];
						var button = "<button type=\"button\" onclick=\"viewJobRes(" + job.id + ")\">" +
							"<span data-i18n=\"viewjobres\">" + buttonText + "</span> " +
							"<svg class=\"featherc\"><use href=\"feather-sprite.svg#table\"/></svg></button>";
						document.getElementById("jres").innerHTML = anchor + " " + button;
					}
				}
			};
			request.open("GET", restPath + "/JobService/isCSVExists/" + job.id, false);
			request.send();
		}
		if (job.jobType == "UPLOAD_MATCH") {
			if (job.status == "CREATED") {
				updateUploadFormData();
			}
			else {
				var files = "";
				var file1 = job.fastqFile;
				if (file1 != null && file1 != "") {
					files = file1;
				}
				var file2 = job.fastqFile2;
				if (file2 != null && file2 != "") {
					files = files + ", " + file2;
				}
				document.getElementById("jfiles").innerHTML = htmlEscape(files);
			}
			document.getElementById("choosefiles").disabled = !(job.status == "CREATED" || job.status == null);
		}
	}
}

function viewJobRes(jobid) {
	window.open(mainPath + "/view?lan=" + state.currentLan + "&jobid=" + jobid, '_blank').focus();
}

function viewLogRes(jobid) {
	window.open(mainPath + "/log?lan=" + state.currentLan + "&jobid=" + jobid, '_blank').focus();
}

function updateFormDates(job) {
	document.getElementById("jenqueued").innerHTML = getDateTimeStr(job.enqueued);
	document.getElementById("jstarted").innerHTML = getDateTimeStr(job.started);
	document.getElementById("jfinished").innerHTML = getDateTimeStr(job.finished);
}

function getDateTimeStr(datenum) {
	if (datenum == null) {
		return "";
	}
	var date = new Date(datenum);
	return date.toLocaleDateString(state.currentLan) + " " + date.toLocaleTimeString(state.currentLan);
}

function enableJobForm(enable) {
	document.getElementById("jobnamefield").disabled = !enable;
	document.getElementById("fastqfilesel").disabled = !enable;
	document.getElementById("fastqurlsel").disabled = !enable;
	document.getElementById("foruser").disabled = !enable;
	document.getElementById("fordb").disabled = !enable;
	document.getElementById("jshowdir").disabled = !(enable && (isAdmin(loggedInUser) || localInstall));
	document.getElementById("jobtype").disabled = !enable;
	document.getElementById("classifyreads").disabled = !enable;
	var classify = document.getElementById("classifyreads").checked;
	document.getElementById("errorratefield").disabled = !enable || !classify;

	var options = document.getElementById("jobtype").options;
	for (var i = 0; i < options.length; i++) {
		if (options[i].value == "UPLOAD_MATCH") {
			options[i].disabled = uploadPathRole == null ||
				(uploadPathRole == "ADMIN" && !isAdmin(loggedInUser)) ||
				(uploadPathRole == "RUN_JOBS" && !isJobRunner(loggedInUser));
			break;
		}
	}
}

function startJob() {
	var job = selectedJob;
	if (isJobRunner(loggedInUser)) {
		if (job != null) {
			if (job.status == "CREATED") {
				if (job.jobType == "UPLOAD_MATCH") {
					document.getElementById("jobid").value = job.id;
					var formData = new FormData(document.getElementById("uploadform"));
					job.status = "ENQUEUED";
					document.getElementById("choosefiles").disabled = true;
					enqueuedUploadJobId = job.id;
					updateStartStopButtons("stop");
					cAlert("uploadInfo");
					uploadFastqsViaForm(job.id, formData);
				}
				else {
					var request = createAjaxRequest();
					request.onreadystatechange = function() {
						if (request.readyState == 4) {
							var status = evalOrHandleError(request);
							if (status == "ENQUEUED" || status == "STARTED") {
								updateStartStopButtons("stop");
							}
							reloadJob(job);
						}
					};
					request.open("GET", restPath + "/JobService/enqueue/" + job.id, false);
					updateStartStopButtons("stop");
					job.status = "ENQUEUED";
					request.send();
				}
			}
		}
	}
}

function uploadFastqsViaForm(jobId, formData) {
	var request = createAjaxRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			if (request.status == 200 || request.status == 204) {
				cAlert("uploadSuccess")
			} else if (!(selfCancelledJobId != null && selfCancelledJobId == jobId)) {
				var error = request.status + " " + htmlEscape(request.statusText) + " " + htmlEscape(request.responseText);
				cInfo(i18n[state.currentLan]["uploadError"] + "\n" + error);
			}
		}
	};
	request.open("POST", mainPath + "/fastqupload", true);
	request.send(formData);
}

function stopJob() {
	if (isJobRunner(loggedInUser)) {
		if (selectedJob != null) {
			if (selectedJob.status == "ENQUEUED" || selectedJob.status == "STARTED") {
				var request = createAjaxRequest();
				request.onreadystatechange = function() {
					if (request.readyState == 4) {
						var status = evalOrHandleError(request);
						if (status == "ENQUEUED" || status == "STARTED") {
							updateStartStopButtons("start");
						}
					}
				};
				updateStartStopButtons("start");
				selfCancelledJobId = selectedJob.id;
				request.open("GET", restPath + "/JobService/cancel/" + selectedJob.id, false);
				request.send();
			}
		}
	}
}

function checkJobsRound() {
	if (isJobRunner(loggedInUser)) { // No pinging and auto refresh for job viewers...
		checkStatusForJobs();
		checkPendingJobs();
		if (selectedJob != null) {
			var value = document.getElementById("jobtype").value;
			if (value == "LOCAL_MATCH") {
				loadFastqFileList(getJobUserFromForm(), true);
			}
		}
		if (startedJobId != null) {
			loadJobProgress(startedJobId);
		}
	}
}

function checkStatusForJobs() {
	var ids = [];
	var request = createAjaxRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			var statusForJobs = evalOrHandleError(request);
			startedJobId = null;
			for (var i = 0; i < statusForJobs.length; i++) {
				var job = getJobById(ids[i]);
				if (job != null) {
					if (statusForJobs[i] == "STARTED") {
						startedJobId = job.id;
					}
					else if (startedJobId == job.id) {
						// Job must have ended:
						startedJobId = null;
					}
					var db = null;
					// During fastq upload the status "CREATED" might last for a while until the server
					// starts processing. We therefore must not reload or update if statusForJobs[i] == "CREATED"...
					if (job.status != statusForJobs[i] && statusForJobs[i] != "CREATED") {
						job.status = statusForJobs[i];
						if (job.status == "STARTED" || job.status == "S_CANCELED") {
							if (job.jobType == "INSTALL_DB") {
								db = getDBById(job.dbId);
								if (db != null) {
									db.installed = false;
									db.infoExists = false;
									updateForDB();
								}
							}
							else if (job.jobType == "DB_INFO") {
								db = getDBById(job.dbId);
								if (db != null) {
									db.infoExists = false;
								}
							}
						}
						else if (job.status == "FINISHED") {
							if (job.jobType == "INSTALL_DB") {
								db = getDBById(job.dbId);
								if (db != null) {
									db.installed = true;
									db.infoExists = false;
									updateForDB();
								}
							}
							else if (job.jobType == "DB_INFO") {
								db = getDBById(job.dbId);
								if (db != null) {
									db.infoExists = true;
								}
							}
						}
						if (selectedDB == db && (job.jobType == "DB_INFO" || job.jobType == "INSTALL_DB")) {
							updateDBInstalled(selectedDB);
							bindDBInfoCSV(selectedDB);
						}
						reloadJob(job);
					}
				}
			}
		}
	};
	var counter = 0;
	if (allData["job"] != null) {
		for (var i = 0; i < allData["job"].length; i++) {
			var job = allData["job"][i];
			if (job.status == "ENQUEUED" || job.status == "STARTED") {
				ids[counter++] = allData["job"][i].id;
			}
		}
		request.open("POST", restPath + "/JobService/getStatusForJobs/", true);
		request.setRequestHeader("Content-Type", "application/json");
		request.send(JSON.stringify(ids));
	}
}

function checkPendingJobs() {
	var request = createAjaxRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			var pendingJobIds = evalOrHandleError(request);
			for (var i = 0; i < pendingJobIds.length; i++) {
				if (pendingJobIds[i] != null) {
					var job = getJobById(pendingJobIds[i]);
					if (job != null && job.status != "STARTED") {
						var td = document.getElementById("tjobid" + pendingJobIds[i]);
						if (td != null) {
							td.innerHTML = "<span data-i18n=\"waitpos\">" + i18n[state.currentLan].waitpos + "</span>" + " " + (i + 1);
						}
					}
				}
			}
			delayUploadStart = pendingJobIds.length > 0;
			updateStartStop();
		}
	};
	request.open("GET", restPath + "/JobService/getActiveJobIds", true);
	request.send();
}

function jobUserChanged() {
	document.getElementById("jfiles").value = "";
	selectedJob.fastqFile = null;
	selectedJob.fastqFile2 = null;
	selectedJob.resourceId = -1;
	selectedJob.resourceId2 = -1;
	checkJobInForm();
	loadFastqFileList(getJobUserFromForm(), false);
	updateFastqURLList(getJobUserFromForm());
}

function getJobUserFromForm() {
	var options = document.getElementById("foruser").selectedOptions;
	return options.length == 0 ? -1 : parseInt(options[0].value);
}

function fastqSelectionChanged() {
	var fileOptions = document.getElementById("fastqfilesel").selectedOptions;
	if (fileOptions.length == 1 && fileOptions[0].value == "") {
		selectFastqs(getJobUserFromForm(), null, null);
	}
	else {
		var sel1 = fileOptions.length >= 1 ? fileOptions[0].value : null;
		var sel2 = fileOptions.length >= 2 ? fileOptions[1].value : null;
		selectFastqs(getJobUserFromForm(), sel1, sel2);
	}
	checkJobInForm();
}

function fastqURLSelectionChanged() {
	var fileOptions = document.getElementById("fastqurlsel").selectedOptions;
	if (fileOptions.length == 1 && fileOptions[0].value == "") {
		selectFastqURLs(null, null);
	}
	else {
		var sel1 = fileOptions.length >= 1 ? fileOptions[0].value : null;
		var sel2 = fileOptions.length >= 2 ? fileOptions[1].value : null;
		selectFastqURLs(sel1, sel2);
	}
	checkJobInForm();
}

function reloadJob(oldJob) {
	var index = null;
	for (var i = 0; i < allData["job"].length; i++) {
		if (allData["job"][i].id == oldJob.id) {
			index = i;
			break;
		}
	}
	if (index != null) {
		var request = createAjaxRequest();
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				var newJob = evalOrHandleError(request);
				if (newJob != null) {
					allData["job"][index] = newJob;
					updateJobTableEntry(index);
					if (oldJob == selectedJob) {
						selectJob(index);
					}
				}
			}
		};
		request.open("GET", restPath + "/JobService/get/" + oldJob.id, true);
		request.send();
	}
}

function loadFastqFileList(userId, sync) {
	if (!isJobRunner(loggedInUser)) {
		return;
	}
	if (cachedUserId != userId) {
		cachedFileSel1 = null;
		cachedFileSel2 = null;
		cachedUserId = userId;
	}
	if (userId == -1) {
		clearFastqs();
		return;
	}
	var select = document.getElementById("fastqfilesel");
	if (document.activeElement === select) {
		// Don't mess with users current edits...
		return;
	}
	var request = createAjaxRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			var files = evalOrHandleError(request);
			if (files == null) {
				files = [];
			}
			clearFastqs();
			if (files.length == 0) {
				files[0] = "";
			}
			for (var i = 0; i < files.length; i++) {
				var opt = document.createElement('option');
				opt.value = files[i];
				opt.innerHTML = htmlEscape(files[i]);
				select.appendChild(opt);
			}
			selectFastqs(cachedUserId, cachedFileSel1, cachedFileSel2);
		}
	};
	request.open("GET", restPath + "/FastqFileService/getFastqFilesForUser/" + userId, !sync);
	request.send();
}

function clearFastqs() {
	var select = document.getElementById("fastqfilesel");
	var len = select.options.length;
	for (var i = 0; i < len; i++) {
		select.remove(0);
	}
	document.getElementById("jfiles").innerHTML = "";
}

function selectFastqs(userId, file1, file2) {
	cachedUserId = userId;
	cachedFileSel1 = file1;
	cachedFileSel2 = file2;
	var select = document.getElementById("fastqfilesel");
	var len = select.options.length;
	for (var i = 0; i < len; i++) {
		var opt = select.options[i];
		opt.selected = (opt.value == file1 || opt.value == file2);
	}
	var files = "";
	if (file1 != null && file1 != "") {
		files = file1;
	}
	if (file2 != null && file2 != "") {
		files = files + ", " + file2;
	}
	document.getElementById("jfiles").innerHTML = htmlEscape(files);
}

function showFastqDir() {
	var userId = getJobUserFromForm();
	if (userId != null) {
		var request = createAjaxRequest();
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				var path = evalOrHandleError(request);
				if (path == null) {
					path = "/?/" + userId;
				}
				cInfo(i18n[state.currentLan]["fastqFolder"] + "<br><a href=\"file://" + path + "\">" + path + "</a>&nbsp;" +
					"<button type=\"button\" onclick='navigator.clipboard.writeText(\"" + path + "\")'><svg class=\"featherc\">" +
					"<use href=\"feather-sprite.svg#clipboard\" /></svg></button>");
			}
		};
		request.open("GET", restPath + "/FastqFileService/getFastqFolderForUser/" + userId, false);
		request.send();
	}
}

function loadJobProgress(jobId) {
	var request = createAjaxRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			var progress = evalOrHandleError(request);
			var percent = "";
			var mCoveredBytes = "";
			var time = "";
			if (progress != null) {
				var ratio = progress.progressRatio;
				percent = ratio == -1 ? "? " : Math.floor(ratio * 100) + "%";
				mCoveredBytes = Math.floor(progress.coveredBytes / 1024 / 1024) + " / " + (progress.totalBytes == -1 ? "?" : Math.floor(progress.totalBytes / 1024 / 1024)) + " MBytes";
				time = Math.floor(progress.elapsedTimeMs / 1000 / 60) + " / " + (progress.totalBytes == -1 ? "?" : Math.floor(progress.totalTimeMs / 1000 / 60)) + " min";
			}
			if (selectedJob != null && selectedJob.id == jobId) {
				document.getElementById("jprogress").innerHTML = percent;
				document.getElementById("jbytes").innerHTML = mCoveredBytes;
				document.getElementById("jduration").innerHTML = time;
			}
			var tableEntry = document.getElementById("tjobid" + jobId);
			if (tableEntry != null) {
				tableEntry.innerHTML = percent;
			}
		}
	};
	request.open("GET", restPath + "/JobService/getProgress/" + jobId, true);
	request.send();
}

function jobTypeChanged() {
	var jobType = document.getElementById("jobtype").value;
	switchFastqTypeDiv(jobType);
	if (jobType == "UPLOAD_MATCH") {
		updateUploadFormData();
	}
	checkJobInForm();
}

function switchFastqTypeDiv(jobType) {
	var fileDiv = jobType == "LOCAL_MATCH";
	var urlDiv = jobType == "RES_MATCH";
	var upload = jobType == "UPLOAD_MATCH";
	document.getElementById("filesdivtext").style.display = fileDiv ? "block" : "none";
	document.getElementById("uploaddivtext").style.display = upload ? "block" : "none";
	document.getElementById("filesdiv").style.display = fileDiv ? "block" : "none";
	document.getElementById("choosefilesdiv").style.display = upload ? "block" : "none";
	document.getElementById("showdirdiv").style.display = fileDiv ? "block" : "none";
	document.getElementById("urlsdivtext").style.display = urlDiv ? "block" : "none";
	document.getElementById("urlsdiv").style.display = urlDiv ? "block" : "none";
	document.getElementById("jfilestitle").style.display = (fileDiv || urlDiv || upload) ? "block" : "none"
	document.getElementById("jfiles").innerHTML = "";
}

function updateFastqURLList(userId) {
	var sel1 = null;
	var sel2 = null;
	var fileOptions = document.getElementById("fastqurlsel").selectedOptions;
	if (fileOptions.length >= 1 && fileOptions[0].value != "") {
		sel1 = fileOptions.length >= 1 ? fileOptions[0].value : null;
		sel2 = fileOptions.length >= 2 ? fileOptions[1].value : null;
	}

	clearFastqURLs();
	if (userId == null) {
		return;
	}
	if (allData["res"] != null) {
		var select = document.getElementById("fastqurlsel");
		for (var i = 0; i < allData["res"].length; i++) {
			if (userId == allData["res"][i].userId) {
				var opt = document.createElement('option');
				opt.value = allData["res"][i].id;
				opt.innerHTML = htmlEscape(allData["res"][i].id + ": " + allData["res"][i].name);
				select.appendChild(opt);
			}
		}
	}
	selectFastqURLs(sel1, sel2);
}

function clearFastqURLs() {
	var select = document.getElementById("fastqurlsel");
	var len = select.options.length;
	for (var i = 0; i < len; i++) {
		select.remove(0);
	}
	document.getElementById("jfiles").innerHTML = "";
}

function selectFastqURLs(file1, file2) {
	var select = document.getElementById("fastqurlsel");
	var len = select.options.length;
	var file1Text = null;
	var file2Text = null;
	for (var i = 0; i < len; i++) {
		var opt = select.options[i];
		opt.selected = (opt.value == file1 || opt.value == file2);
		if (opt.value == file1) {
			file1Text = opt.innerHTML;
		}
		else if (opt.value == file2) {
			file2Text = opt.innerHTML;
		}
	}
	var files = "";
	if (file1Text != null && file1Text != "") {
		files = file1Text;
	}
	if (file2Text != null && file2Text != "") {
		files = files + ", " + file2Text;
	}
	document.getElementById("jfiles").innerHTML = htmlEscape(files);
}

function updateUploadFormData() {
	var files = document.getElementById("fastqfiles").files;
	var s = "";
	var names = "";
	var inputNames = "";
	for (var i = 0; i < files.length; i++) {
		if (i > 0) {
			s = s + ",";
			names = names + ", "
			inputNames = inputNames + '/';
		}
		s = s + files[i].size;
		names = names + files[i].name;
		inputNames = inputNames + files[i].name;
	}
	var fsfield = document.getElementById("filesizes");
	fsfield.value = s;
	var fsnamefield = document.getElementById("filenames");
	fsnamefield.value = inputNames;
	if (names.length > 100) {
		document.getElementById("jfiles").innerHTML = files.length + ' <span data-i18n="files">' + i18n[state.currentLan]["files"] + '</span>';
	}
	else {
		document.getElementById("jfiles").innerHTML = htmlEscape(names);
	}
	checkJobInForm();
}

function delayedJob() {
	cAlert("jobdelayed");
}

function updateFormFiles() {
	document.getElementById('fastqfiles').click();	
}

function updownJobTable(up) {
	if (selectedJob != null) {
		let table = document.getElementById("jobtable");
		let nextIndex = -1;
		let index = allData["job"].indexOf(selectedJob);
		if (!up) {
			for (var i = index + 1; i < table.children[1].children.length; i++) {
				if (table.children[1].children[i].style.display != "none") {
					nextIndex = i;
					break;
				}
			}
		} else {
			for (var i = index - 1; i >= 0; i--) {
				if (table.children[1].children[i].style.display != "none") {
					nextIndex = i;
					break;
				}
			}
		}
		if (nextIndex != -1) {
			selectJob(nextIndex);
			table.children[1].children[i].scrollIntoView({behavior: "smooth", block: "center"});
			e.preventDefault();
		}
	}
}