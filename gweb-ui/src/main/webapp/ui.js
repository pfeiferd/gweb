/* Software: gweb, License: Apache 2.0 subject to “Commons Clause” License Condition v1.0, Licensor: Daniel Pfeifer (daniel.pfeifer@progotec.de) */
//
window.addEventListener('beforeunload', function(e) {
	if (someDataInFormsChanged()) {
		e.preventDefault();
		e.returnValue = '';
	}
});


/* History */

var inPopState = false;

if (history.pushState) {
	window.onpopstate = function(event) {
		if (event.state != null) {
			inPopState = true;
			state = event.state;
			main();
			inPopState = false;
		}
	};
}


function updateHistory() {
	if (history.pushState && !inPopState) {
		if (inMain) {
			history.replaceState(state, null, "?tab=" + state.currentTab + "&lan=" + state.currentLan);
		}
		else {
			history.pushState(state, null, "?tab=" + state.currentTab + "&lan=" + state.currentLan);
		}
	}
}

/* I18n */

var i18n = {
	de: {
		de: "Deutsch",
		en: "English",
		user: "Benutzer:",
		password: "Passwort:",
		login: "Anmelden",
		jobstab: "Jobs",
		dbstab: "Datenbanken",
		userstab: "Benutzer",
		role: "Rolle:",
		loggedinas: "Angemeldet als:",
		logout: "Abmelden",
		loginfailed: "Falscher Benutzer oder falsches Passwort.",
		username: "Benutzer",
		islogin: "Login erlaubt",
		isjobs: "Jobs ausführen",
		isadmin: "Admin",
		personid: "Für Person",
		save: "Speichern",
		create: "Neu",
		reset: "Zurücksetzen",
		checklogin: "Login erlaubt:",
		checkadmin: "Admin:",
		checkjobs: "Jobs ausführen:",
		forperson: "Für Person:",
		id: "ID",
		idtext: "ID:",
		personstab: "Personen",
		firstname: "Vorname",
		lastname: "Nachname",
		firstnamef: "Vorname:",
		lastnamef: "Nachname:",
		statuscode: "Status Code:",
		statustext: "Status Text:",
		responsetext: "Meldung:",
		servererror: "Fehler bei Server-Aufruf",
		edituser: "Benutzer bearbeiten:",
		autologout: "Sie wurden automatisch abmeldet. Bitte melden Sie sich erneut an.",
		reallyChangeSelection: "Neu auswählen und Änderungen verwerfen?",
		reallyLogout: "Änderungen verwerfen und abmelden?",
		delete: "Löschen",
		reallyDeleteSelection: "Datensatz wirklich löschen?",
		pleaseLogin: "Bitte anmelden",
		editperson: "Person bearbeiten:",
		invaliddata: "Der Datensatz ist ungültig und konnte nicht verarbeitet werden.",
		ok: "Ok",
		cancel: "Abbrechen",
		invaliddeletion: "Der Datensatz konnte nicht gelöscht werden, da er noch referenziert wird.",
		dbname: "Name",
		dbfileprefix: "Basis-Dateiname",
		dbnamef: "Name:",
		dbfileprefixf: "Basis-Dateiname:",
		editdb: "Datenbank bearbeiten:",
		jobname: "Name",
		Fastqfile: "Fastq File / URL",
		fastqfile2: "Fastq File / URL 2",
		jobnamef: "Name:",
		fastqfilef: "Fastq Dateien:",
		editjob: "Job bearbeiten:",
		foruser: "Für Benutzer:",
		fordb: "Für Datenbank:",
		dbid: "Datenbank",
		userid: "Für Benutzer",
		jobstatus: "Status",
		jobstatusf: "Status:",
		enqueue: "Starten",
		NEW: "Neu",
		CREATED: "Angelegt",
		ENQUEUED: "Wartend",
		STARTED: "Gestarted",
		FINISHED: "Fertig",
		E_CANCELED: "Abgebrochen",
		S_CANCELED: "Abgebrochen",
		UNKNOWN: "Unbekannt",
		queuepos: "Fortschritt",
		stopjob: "Stoppen",
		jprogress: "Fortschritt:",
		jenqueued: "Eingereiht:",
		jstarted: "Gestartet:",
		jfinished: "Beendet:",
		jduration: "Dauer:",
		jfiles: "Gewählte Dateien / Quellen:",
		jlogf: "Log:",
		jres: "Resultat:",
		jshowdir: "In Ordner",
		fastqFolder: "Die Fastq-Dateien werden in diesem Ordner gesucht:",
		waitpos: "Wartepos.",
		jbytes: "Verarbeitet:",
		NONE: "-",
		NO_LOGIN: "Keine Anmeldung",
		VIEW: "Betrachter",
		RUN_JOBS: "Job-Betreiber",
		ADMIN: "Admin",
		selectrole: "Rolle:",
		throle: "Rolle",
		dbinfof: "Info-Datei:",
		createdbinfo: "Job starten...",
		restab: "Fastq-Quellen",
		resnamef: "Name:",
		resurlf: "URL / Server-Pfad:",
		editres: "Fastq-Quelle bearbeiten:",
		resname: "Name",
		resurl: "URL",
		LOCAL_MATCH: "Analyse von Server-Datei(en)",
		RES_MATCH: "Analyse von Fastq-Quelle(n)",
		DB_INFO: "DB Info",
		INSTALL_DB: "DB Install",
		UPLOAD_MATCH: "Analyse von Upload-Datei(en)",
		jobtype: "Job-Typ:",
		fastqurlf: "Fastq-Quellen:",
		search: "Suchen:",
		viewjobres: "Ansehen...",
		viewdbres: "Ansehen...",
		dbinstallurl: "URL für Datenbank-Datei:",
		installdb: "Installieren...",
		dbisinstalled: "Bereits installiert:",
		reallyReplaceDBInstallation: "Die Datenbank-Datei ist bereits installiert. Wollen Sie sie wirklich ersetzen?",
		reallyDoDBInstallation: "Wollen Sie den Installations-Job wirklich starten?",
		dbinstallmd5: "MD5:",
		reallyReplaceDBInfo: "Die Datenbank-Info wurde bereits erzeugt. Wollen Sie sie wirklich ersetzen?",
		reallyDoDBInfo: "Wollen Sie den Datenkanf-Info-Job wirklich starten?",
		classifyReads: "Mit Read-Klassifikation:",
		errorrate: "Fehlerrate:",
		restype: "Typ:",
		FILE_PATH: "Dateipfad auf Server",
		HTTP_URL: "HTTP(S)",
		choosefiles: "Dateien auswählen...",
		files: "Dateien",	
		uploadError: "Upload und Analyse der Fastq Dateien ist fehlgeschlagen mit Fehler:",
		uploadSuccess: "Upload und Analyse abgeschlossen.",
		uploadInfo: "Upload und Analyse laufen im Hintergrund. Bitte schließen Sie dieses Browser-Tab nicht bis zum Abschluss oder der Job wird abgebrochen.",
		jobdelayed: "Zur Zeit werden andere Jobs bearbeitet. 'Upload und Analyse' ist nicht möglich, solange andere Jobs aktiv sind. Bitte versuchen Sie es später noch einmal.",
		fastquploadf: "Fastq Dateien zum Upload:",
		copy: "Kopieren",
		help: "Hilfe",
		helplink : "https://genestrip.it.hs-heilbronn.de/site/de"
	},
	en: {
		de: "Deutsch",
		en: "English",
		user: "User:",
		password: "Password:",
		login: "Login",
		jobstab: "Jobs",
		dbstab: "Databases",
		userstab: "Users",
		role: "Role:",
		loggedinas: "Logged in as:",
		logout: "Logout",
		loginfailed: "Login failed. Bad user or password.",
		username: "User",
		islogin: "May Login",
		isjobs: "Run Jobs",
		isadmin: "Admin",
		personid: "For Person",
		save: "Save",
		create: "Create",
		reset: "Reset",
		checklogin: "May Login:",
		checkadmin: "Admin:",
		checkjobs: "Run Jobs:",
		forperson: "For Person:",
		id: "ID",
		idtext: "ID:",
		personstab: "Persons",
		firstname: "First Name",
		lastname: "Last Name",
		firstnamef: "First name:",
		lastnamef: "Last name:",
		statuscode: "Status Code:",
		statustext: "Status Text:",
		responsetext: "Message:",
		servererror: "Error during server call",
		edituser: "Edit User:",
		autologout: "You were logged off automatically. Please login again.",
		reallyChangeSelection: "Change selection and discard changes?",
		reallyLogout: "Discard changes and logout?",
		delete: "Delete",
		reallyDeleteSelection: "Really delete entry?",
		pleaseLogin: "Please login",
		editperson: "Edit Person:",
		invaliddata: "The entry is invalid and could not be processed.",
		ok: "Ok",
		cancel: "Cancel",
		invaliddeletion: "The entry could not be deleted as it is still referenced.",
		dbname: "Name",
		dbfileprefix: "Base File Name",
		dbnamef: "Name:",
		dbfileprefixf: "Base File Name:",
		editdb: "Edit Database:",
		jobname: "Name",
		fastqfile: "Fastq File / URL",
		fastqfile2: "Fastq File / URL 2",
		jobnamef: "Name:",
		fastqfilef: "Fastq Files:",
		editjob: "Edit Job:",
		foruser: "For User:",
		fordb: "For Database:",
		dbid: "Database",
		userid: "For User",
		jobstatus: "Status",
		jobstatusf: "Status:",
		enqueue: "Start",
		NEW: "New",
		CREATED: "Created",
		ENQUEUED: "Waiting",
		STARTED: "Running",
		FINISHED: "Finished",
		E_CANCELED: "Canceled",
		S_CANCELED: "Canceled",
		UNKNOWN: "Unknown",
		queuepos: "Progress",
		stopjob: "Stop",
		jprogress: "Progress:",
		jenqueued: "Enqueued:",
		jstarted: "Started:",
		jfinished: "Finished:",
		jduration: "Duration:",
		jfiles: "Chosen Files / Sources:",
		jlogf: "Log:",
		jres: "Result:",
		jshowdir: "In Folder",
		fastqFolder: "Fastq files are searched in this folder:",
		waitpos: "Waiting Pos.",
		jbytes: "Processed:",
		NONE: "-",
		NO_LOGIN: "No Login",
		VIEW: "Viewer",
		RUN_JOBS: "Job Executor",
		ADMIN: "Admin",
		selectrole: "Role:",
		throle: "Role",
		dbinfof: "Info-File:",
		createdbinfo: "Run Job...",
		restab: "Fastq Sources",
		resnamef: "Name:",
		resurlf: "URL / Server Path:",
		editres: "Edit Fastq Source:",
		resname: "Name",
		resurl: "URL",
		LOCAL_MATCH: "Analysis of Server File(s)",
		RES_MATCH: "Analysis of Fastq Source(s)",
		DB_INFO: "DB Info",
		INSTALL_DB: "DB Install",
		UPLOAD_MATCH: "Analysis of Upload File(s)",
		jobtype: "Job Type:",
		fastqurlf: "Fastq Sources:",
		search: "Search:",
		viewjobres: "View...",
		viewdbres: "View...",
		dbinstallurl: "URL for Database File:",
		installdb: "Install...",
		dbisinstalled: "Already Installed:",
		reallyReplaceDBInstallation: "The database file is already installed. Do you really want to replace it?",
		reallyDoDBInstallation: "Do really want to start the installation job?",
		dbinstallmd5: "MD5:",
		reallyReplaceDBInfo: "The database info file is already created. Do you really want to replace it?",
		reallyDoDBInfo: "Do really want to start the DB info job?",
		classifyReads: "With Read Classifcation:",
		errorrate: "Error Rate:",
		restype: "Type:",
		FILE_PATH: "File Path on Server",
		HTTP_URL: "HTTP(S)",
		choosefiles: "Choose Files...",
		files: "Files",
		uploadError: "Upload and analysis of fastq files failed with error:",
		uploadSuccess: "Upload and analysis finished.",
		uploadInfo: "Upload and analysis are now running in background. Please don't close this browser tab or else the job will be canceled.",
		jobdelayed: "Other jobs are being processed. 'Upload and Analysis' cannot be started as long as there are active jobs. Please try again later.",
		fastquploadf: "Fastq Files for Upload:",
		copy: "Copy",
		help: "Help",
		helplink : "https://genestrip.it.hs-heilbronn.de/site"
	}
};

function changeLan(lan) {
	state.currentLan = lan;
	document.documentElement.setAttribute("lang", lan);
	
	var lans = Object.keys(i18n);
	for (var i = 0; i < lans.length; i++) {
		var tab = document.getElementById(lans[i] + "lan");
		if (tab != null) {
			tab.className = "lan";
		}
	}
	var tab = document.getElementById(lan + "lan");
	if (tab != null) {
		tab.className = "selectedlan";
	}

	var all = document.getElementsByTagName("*");
	for (var i = 0; i < all.length; i++) {
		replaceI18n(lan, all[i]);
	}

	document.getElementById("LOCAL_MATCH").textContent = i18n[state.currentLan]["LOCAL_MATCH"];
	document.getElementById("RES_MATCH").textContent = i18n[state.currentLan]["RES_MATCH"];
	document.getElementById("DB_INFO").textContent = i18n[state.currentLan]["DB_INFO"];
	document.getElementById("INSTALL_DB").textContent = i18n[state.currentLan]["INSTALL_DB"];
	document.getElementById("HTTP_URL").textContent = i18n[state.currentLan]["HTTP_URL"];
	document.getElementById("FILE_PATH").textContent = i18n[state.currentLan]["FILE_PATH"];
	document.getElementById("UPLOAD_MATCH").textContent = i18n[state.currentLan]["UPLOAD_MATCH"];

	document.getElementById("helplink").href = i18n[state.currentLan]["helplink"];

	if (loggedInUser != null) {
		updateRole();
		if (selectedJob != null) {
			updateFormDates(selectedJob);
		}
	}
	updateHistory();
}

function updateRole() {
	document.getElementById("userrole").textContent = loggedInUser == null ? "" : i18n[state.currentLan][loggedInUser.role];
}

function replaceI18n(lan, node) {
	var name = node.getAttribute("data-i18n");
	if (name != null) {
		var value = i18n[lan][name];
		if (value == null) {
			value = name;
		}
		if (node.tagName == "IMG") {
			node.alt = value;
		} else {
			node.textContent = value;
		}
	}
}

/* Main */

var inMain = true;

function main() {
	inMain = true;
	changeLan(state.currentLan);
	switchFastqTypeDiv(true);
	loadLoggedInUser();
	inMain = false;
}

/* Common */

function someDataInFormsChanged() {
	return hasJobInFormChanged() || hasResInFormChanged() || hasDBInFormChanged() || hasUserInFormChanged() || hasPersonInFormChanged();
}

function htmlEscape(str) {
	return String(str).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(
		/'/g, '&#39;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function createAjaxRequest() {
	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		return new XMLHttpRequest();
	} else {// code for IE6, IE5
		return new ActiveXObject("Microsoft.XMLHTTP");
	}
}

function createCheckBoxStr(checked) {
	var res = "<input type=\"checkbox\" disabled=\"disabled\" "
	if (checked) {
		res = res + "checked=\"checked\">";
	}
	else {
		res = res + ">";
	}
	return res;
}

function clearTable(table) {
	var rowCount = table.children[1].rows.length;
	for (var i = 0; i < rowCount; i++) {
		table.children[1].deleteRow(0);
	}
}

function createTableRow(htmlString) {
	var table = document.createElement('table');
	table.innerHTML = htmlString.trim();
	return table.firstChild.firstChild;
}

function updateMandatory(id, mandatory) {
	document.getElementById(id).className = mandatory ? "mandatory" : "";
	return mandatory;
}

function unselectTableRows(table) {
	var children = table.children[1].children;
	for (var j = 0; j < children.length; j++) {
		children[j].className = "tnormal";
	}
}

function deepEqual(x, y) {
	const ok = Object.keys, tx = typeof x, ty = typeof y;
	return x && y && tx === 'object' && tx === ty ? (
		ok(x).length === ok(y).length &&
		ok(x).every(key => deepEqual(x[key], y[key]))
	) : (x === y);
}

var okAlertCode = null;

function cAlert(message, okCode) {
	cInfo(i18n[state.currentLan][message], okCode);
}

function cInfo(message, okCode) {
	okAlertCode = okCode;
	document.getElementById("alertmessage").innerHTML = message;
	document.getElementById("modalalert").style.display = "block";
}

function okAlert() {
	document.getElementById("modalalert").style.display = "none";
	var code = okAlertCode;
	okAlertCode = null;
	if (code != null) {
		code();
	}
}

var cancelConfirmCode = null;
var okConfirmCode = null;

function cConfirm(message, okCode, cancelCode) {
	okConfirmCode = okCode;
	cancelConfirmCode = cancelCode;
	document.getElementById("dialogmessage").innerHTML = i18n[state.currentLan][message];
	document.getElementById("modaldialog").style.display = "block";
}

function okConfirm() {
	document.getElementById("modaldialog").style.display = "none";
	var code = okConfirmCode;
	cancelConfirmCode = null;
	okConfirmCode = null;
	if (code != null) {
		code();
	}
}

function cancelConfirm() {
	document.getElementById("modaldialog").style.display = "none";
	var code = cancelConfirmCode;
	cancelConfirmCode = null;
	okConfirmCode = null;
	if (code != null) {
		code();
	}
}

function isAdmin(user) {
	return user != null && user.role == "ADMIN";
}

function isJobRunner(user) {
	return user != null && (user.role == "RUN_JOBS" || user.role == "ADMIN");
}

function isValidHttpUrl(string) {
	var url;

	try {
		url = new URL(string);
	} catch (_) {
		return false;
	}

	return url.protocol == "http:" || url.protocol == "https:";
}

/* Ajax request handling */

function evalOrHandleError(request) {
	if (request.status == 200 || request.status == 204) {
		return request.responseText == "" ? null : JSON.parse(request.responseText);
	} else {
		if (request.status == 401) {
			loadLoggedInUser();
			if (loggedInUser != null) {
				showError(request);
				throw "Server request error";
			}
			else {
				document.getElementById("autologout").style.display = "inline";
				throw "Auto logout during request";
			}
		}
		else if (request.status == 403 || request.status == 410) {
			cAlert("invaliddata");
			throw "Invalid record error";
		}
		else if (request.status == 409) {
			cAlert("invaliddeletion");
			throw "Invalid deletion error";
		}
		else {
			showError(request);
			throw "Server request error";
		}
	}
}

function showError(request) {
	if (request != null) {
		document.getElementById("statuscode").innerHTML = request.status;
		document.getElementById("statustext").innerHTML = htmlEscape(request.statusText);
		document.getElementById("responsetext").innerHTML = htmlEscape(request.responseText);
	}
	showScreen("error");
}


function clearData() {
	allData["user"] = null;
	usersById = null;
	selectedUser = null;
	allData["person"] = null;
	personsById = null
	selectedPerson = null;
	allData["db"] = null;
	dbsById = null;
	selectedDB = null;
	dbInfoJobId = null;
	dbInfoId = null;
	allData["res"] = null;
	resById = null;
	selectedRes = null;

	clearResForm();
	clearDBForm();
	clearUserForm();
	clearPersonForm();
	clearFastqs();
	clearFastqURLs();
	clearSearchFields();
	clearTable(document.getElementById("restable"));
	clearTable(document.getElementById("dbtable"));
	clearTable(document.getElementById("usertable"));
	clearTable(document.getElementById("persontable"));

	clearJobData();
}


/* Tabs and screens */

var screens = ["login", "main", "error"];
var tabs = ["jobs", "res", "users", "dbs", "persons"];

function showScreen(div) {
	for (var i = 0; i < screens.length; i++) {
		document.getElementById(screens[i]).style.display = "none";
	}
	document.getElementById(div).style.display = "block";
}

function showTab(div) {
	state.currentTab = div;
	showScreen("main");
	for (var i = 0; i < tabs.length; i++) {
		document.getElementById(tabs[i]).style.display = "none";
		var tab = document.getElementById(tabs[i] + "tab");
		if (tab != null) {
			tab.className = "tab";
		}
	}
	document.getElementById(div).style.display = "block";
	var tab = document.getElementById(div + "tab");
	if (tab != null) {
		document.getElementById("tabs").style.display = "block";
		tab.className = "selectedtab";
	}
	updateHistory();
	if (div == "jobs") {
		loadJobs();
	}
	else if (div == "res") {
		loadRes(true);
	}
	else if (div == "dbs") {
		loadDBs(true);
	}
	else if (div == "users") {
		loadUsers(true);
	}
	else if (div == "persons") {
		loadPersons(true);
	}
}

function dataChangedInTab(div, change) {
	var span = document.getElementById(div + "changed");
	span.innerHTML = change ? "*" : "";
}

function searchInTable(tab) {
	var input = document.getElementById(tab + "searchfield");
	var filter = input.value.toUpperCase();
	var table = document.getElementById(tab + "table");
	var tr = table.getElementsByTagName("tr");
	for (var i = 1; i < tr.length; i++) {
		filterTableRow(filter, tr[i]);
	}
}

function filterTableRow(filter, row) {
	var tds = row.getElementsByTagName("td");
	var found = false;
	for (var j = 0; j < tds.length; j++) {
		var td = tds[j];
		if (td) {
			txtValue = td.textContent || td.innerText;
			if (txtValue.toUpperCase().indexOf(filter) > -1) {
				found = true;
				break;
			}
		}
	}
	row.style.display = found ? "" : "none";
	return row;
}

var appTypes = ['job', 'res', 'db', 'user', 'person'];

function clearSearchFields() {
	for (var i = 0; i < appTypes.length; i++) {
		clearSearchField(appTypes[i]);
	}
}

function clearSearchField(type) {
	document.getElementById(type + "searchfield").value = "";
}

var sortOrder = {};
var lastSortField = {};
var allData = {};

function sortTableData(type, field) {
	var array = allData[type];
	if (array != null) {
		if (sortOrder[type] == null) {
			sortOrder[type] = {};
		}
		if (sortOrder[type]) {
			sortOrder[type] = false;
		}
		else {
			sortOrder[type] = true;
		}
		var elems = document.getElementsByClassName("sorticon");
		for (var i = 0; i < elems.length; i++) {
			elems[i].style.display = "none";
		}
		document.getElementById(type + field + (sortOrder[type] ? "down" : "up")).style.display = "";
		lastSortField[type] = field;
		sortByCurrentField(type);
		if (type == 'job') {
			updateJobTable();
		}
		else if (type == 'res') {
			updateResTable();
		}
		else if (type == 'db') {
			updateDBTable();
		}
		else if (type == 'user') {
			updateUserTable();
		}
		else if (type == 'person') {
			updatePersonTable();
		}
	}
}

function sortByCurrentField(type) {
	var order = sortOrder[type];
	if (order == null) {
		order = true;
	}
	if (lastSortField[type] == null) {
		lastSortField[type] = "id";
	}
	var field = lastSortField[type];
	var array = allData[type];
	array.sort((a, b) => {
		if (a[field] == b[field]) {
			return 0;
		}
		else if (a[field] < b[field]) {
			return order ? -1 : 1;
		}
		else {
			return order ? 1 : -1;
		}
	});
}