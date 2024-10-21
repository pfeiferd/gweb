/* Software: gweb, License: Apache 2.0 subject to “Commons Clause” License Condition v1.0, Licensor: Daniel Pfeifer (daniel.pfeifer@progotec.de) */
//
/* History */

var inPopState = false;

if (history.pushState) {
	window.onpopstate = function(event) {
		if (event.state != null) {
			inPopState = true;
			state = event.state;
			changeLan(state.currentLan);
			inPopState = false;
		}
	};
}


function updateHistory() {
	if (history.pushState && !inPopState) {
		if (inMain) {
			history.replaceState(state, "", "?lan=" + state.currentLan + "&jobid=" + jobId);
		}
		else {
			history.pushState(state, "", "?lan=" + state.currentLan + "&jobid=" + jobId);
		}
	}
}

/* I18n */

var i18n = {
	de: {
		de: "Deutsch",
		en: "English",
		servererror: "Fehler bei Server-Aufruf",
		statuscode: "Status Code:",
		statustext: "Status Text:",
		responsetext: "Meldung:",
		search: "Suchen:",
		line: "Zeile",
		rank: "Rang",
		name: "Name",
		taxid: "Tax Id",
		reads: "Reads",
		kmersfr: "k-Mere v. Reads",
		kmers: "k-Mere",
		ukmers: "Eindeutige k-Mere",
		contigs: "Contigs",
		avgclen: "Durchn C.länge",
		maxclen: "Max C.länge",
		maxcdesc: "Max C. Deskr.",
		dbcov: "Abdeckung",
		nkmers: "Norm. k-Mere",
		euk: "Erw. eind. K-Mere",
		ukeukr: "Eind. K-Mere / Erw.",
		qp: "Qualitätspred.",
		ok: "OK",
		nosession: "Ihre Sitzung wurde beendet. Bitte melden Sie sich bei der Hauptanwendung an.",
		novalidjob: "Ungültige oder fehlende Job ID. Keine Daten verfügbar.",
		on: " auf ",
		colorby: "Färben nach:",
		nocol: "Keine Färbung"
	},
	en: {
		de: "Deutsch",
		en: "English",
		user: "User:",
		servererror: "Error during server call",
		statuscode: "Status Code:",
		statustext: "Status Text:",
		responsetext: "Message:",
		search: "Search:",
		line: "Line",
		rank: "Rank",
		name: "Name",
		taxid: "Tax Id",
		reads: "Reads",
		kmersfr: "k-mers f. Reads",
		kmers: "k-mers",
		ukmers: "Unique k-mers",
		contigs: "Contigs",
		avgclen: "Avr. C. Length",
		maxclen: "Max C. Length",
		maxcdesc: "Max C. Descr.",
		dbcov: "Coverage",
		nkmers: "Norm. k-mers",
		euk: "Exp. u. k-mers",
		ukeukr: "U. k-mers / Exp.",
		qp: "Quality Prediction",
		ok: "OK",
		nosession: "Your session has terminated. Please login via the main application.",
		novalidjob: "Inavlid oder missing job ID. No data to show.",
		on: " on ",
		colorby: "Color by:",
		nocol: "No Coloring"
	}
};

function changeLan(lan) {
	state.currentLan = lan;
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

	var opts = document.getElementsByTagName("option");
	for (var i = 0; i < opts.length; i++) {
		if (opts[i].className == "colopt") {
			opts[i].textContent = i18n[state.currentLan][opts[i].value];
		}
	}
	updateHistory();
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
	loadLoggedInUser(); // Ensures default user is in session if existing. 
	loadJob(jobId);
	downloadCSV(jobId);
	inMain = false;
}

/* Common */

function createAjaxRequest() {
	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		return new XMLHttpRequest();
	} else {// code for IE6, IE5
		return new ActiveXObject("Microsoft.XMLHTTP");
	}
}

function htmlEscape(str) {
	return String(str).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(
		/'/g, '&#39;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function createTableRow(htmlString) {
	var table = document.createElement('table');
	table.innerHTML = htmlString.trim();
	return table.firstChild.firstChild;
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

/* Ajax request handling */

function showError(request) {
	if (request != null) {
		document.getElementById("statuscode").innerHTML = request.status;
		document.getElementById("statustext").innerHTML = htmlEscape(request.statusText);
		document.getElementById("responsetext").innerHTML = htmlEscape(request.responseText);
	}
	showScreen("error");
}

/* Tabs and screens */

var screens = ["main", "error"];

function showScreen(div) {
	for (var i = 0; i < screens.length; i++) {
		document.getElementById(screens[i]).style.display = "none";
	}
	document.getElementById(div).style.display = "block";
	document.getElementById("tablediv").style.display = div == "main" ? "block" : "none";
}


function searchInTable() {
	var input = document.getElementById("datasearchfield");
	var filter = input.value.toUpperCase();
	var table = document.getElementById("datatable");
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

function clearSearchField() {
	document.getElementById("datasearchfield").value = "";
}

var job = null;
var sortOrder = true;
var lastSortField = null;
var allData = null;
var fields = ["name", "rank", "taxid", "reads", "kmersfr", "kmers", "ukmers", "contigs", "avgclen", "maxclen", "maxcdesc", "dbcov", "nkmers", "euk", "ukeukr", "qp"];
var fieldTypes = ["s", "s", "s", "i", "i", "i", "i", "i", "d", "i", "s", "d", "d", "d", "d", "d"];

function sortTableData(field) {
	if (allData != null) {
		sortOrder = !sortOrder;
		var elems = document.getElementsByClassName("sorticon");
		for (var i = 0; i < elems.length; i++) {
			elems[i].style.display = "none";
		}
		document.getElementById(field + (sortOrder ? "down" : "up")).style.display = "";
		lastSortField = field;
		if (lastSortField == null) {
			lastSortField = "line";
		}
		allData.sort((a, b) => {
			if (a[lastSortField] == b[lastSortField]) {
				return 0;
			}
			else if (a[lastSortField] < b[lastSortField]) {
				return sortOrder ? -1 : 1;
			}
			else {
				return sortOrder ? 1 : -1;
			}
		});
		updateDataTable();
	}
}

function downloadCSV(jobId) {
	var request = createAjaxRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			if (request.status == 200 || request.status == 204) {
				allData = csvToObjs(request.responseText);
				updateDataTable();
			}
			else {
				var tab = document.getElementById("jobtitle");
				tab.textContent = "Job ?";
				tab = document.getElementById("dbtitle");
				tab.textContent = "DB ?";
				handleRequestError(request);
			}
		};
	}
	request.open("GET", restPath + "/JobService/getCSV/" + jobId, true);
	request.setRequestHeader("Content-Type", "application/json");
	request.send();
}

function handleRequestError(request) {
	if (request.status == 401) {
		cAlert("nosession", () => { window.close(); });
	}
	else if (request.status == 404) {
		cAlert("novalidjob", () => { window.close(); });
	} else {
		showError(request);
		throw "Server request error";
	}
}

function loadLoggedInUser() {
	var request = createAjaxRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			// Do nothing on purpose.
		}
	};
	// Must be synchronous.
	request.open("GET", restPath + "/UserService/getLoggedInUser", false);
	request.send();
}

function loadJob(id) {
	var request = createAjaxRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			if (request.status == 200 || request.status == 204) {
				job = request.responseText == "" ? null : JSON.parse(request.responseText);
				if (job != null) {
					var tab = document.getElementById("jobtitle");
					tab.textContent = "Job " + job.id + ": " + job.name;
					loadDB(job.dbId);
				}
			}
			else {
				handleRequestError(request);
			}
		}
	};
	request.open("GET", restPath + "/JobService/get/" + id, true);
	request.send();
}

function loadDB(id) {
	var request = createAjaxRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			if (request.status == 200 || request.status == 204) {
				var db = request.responseText == "" ? null : JSON.parse(request.responseText);
				if (db != null) {
					var tab = document.getElementById("dbtitle");
					tab.textContent = "DB " + db.id + ": " + db.name;
				}
			}
			else {
				handleRequestError(request);
			}
		}
	};
	request.open("GET", restPath + "/DBService/get/" + id, true);
	request.send();
}

function csvToObjs(csv) {
	var lines = csv.split("\n");
	var result = [];
	for (var i = 1; i < lines.length; i++) {
		var currentline = lines[i].split(";");
		if (currentline.length >= fields.length) {
			var obj = {};
			obj.line = i;
			for (var j = 0; j < fields.length; j++) {
				if (fieldTypes[j] == "i") {
					obj[fields[j]] = parseInt(currentline[j]);
				}
				else if (fieldTypes[j] == "d") {
					obj[fields[j]] = parseFloat(currentline[j]);
				}
				else {
					obj[fields[j]] = currentline[j];
				}
			}
			result.push(obj);
		}
	}
	return result;
}

function updateDataTable() {
	var table = document.getElementById("datatable");
	var input = document.getElementById("datasearchfield");
	var filter = input.value.toUpperCase();
	var rowCount = table.rows.length;
	for (var i = 1; i < rowCount; i++) {
		table.deleteRow(1);
	}
	var options = document.getElementById("colorby").selectedOptions;
	var colorBy = options[0].value;
	var minmax = [null, null];
	if (colorBy == "ukeukr") {
		minmax[0] = 0;
		minmax[1] = 1;
	}
	else {
		getMinMax(minmax, colorBy);
	}

	for (var i = 0; i < allData.length; i++) {
		var line = allData[i];

		var tr = "<tr class=\"tnormal\">";
		tr = tr + "<td>" + line.line + "</td>";
		tr = tr + "<td>" + htmlEscape(line.name) + "</td>";
		tr = tr + "<td>" + htmlEscape(line.rank) + "</td>";
		tr = tr + "<td>" + htmlEscape(line.taxid) + "</td>";
		tr = tr + "<td>" + htmlEscape(line.reads) + "</td>";
		tr = tr + "<td>" + htmlEscape(line.kmersfr) + "</td>";
		tr = tr + "<td>" + htmlEscape(line.kmers) + "</td>";
		tr = tr + "<td>" + htmlEscape(line.ukmers) + "</td>";
		tr = tr + "<td>" + htmlEscape(line.contigs) + "</td>";
		tr = tr + "<td>" + htmlEscape(line.avgclen) + "</td>";
		tr = tr + "<td>" + htmlEscape(line.maxclen) + "</td>";
		tr = tr + "<td>" + htmlEscape(line.dbcov) + "</td>";
		tr = tr + "<td>" + htmlEscape(line.nkmers) + "</td>";
		tr = tr + "<td>" + htmlEscape(line.euk) + "</td>";
		tr = tr + "<td>" + htmlEscape(line.ukeukr) + "</td>";
		tr = tr + "<td>" + htmlEscape(line.qp) + "</td>";
/*		tr = tr + "<td>" + htmlEscape(line.maxcdesc) + "</td>"; */
		tr = tr + "</tr>";

		table.children[1].appendChild(colorTableRow(colorBy, line, filterTableRow(filter, createTableRow(tr)), minmax[0], minmax[1]));
	}
}

function coloringChanged() {
	var options = document.getElementById("colorby").selectedOptions;
	if (options.length != 1) {
		return;
	}
	var colorBy = options[0].value;
	var table = document.getElementById("datatable");
	var minmax = [null, null];
	if (colorBy == "ukeukr") {
		minmax[0] = 0;
		minmax[1] = 1;
	}
	else {
		getMinMax(minmax, colorBy);
	}
	for (var i = 0; i < allData.length; i++) {
		colorTableRow(colorBy, allData[i], table.rows[i + 1], minmax[0], minmax[1]);
	}
}

function colorTableRow(colorBy, line, row, min, max) {
	if (colorBy == "nocol" || line.name == "root" || line.name == "TOTAL") {
		row.style.backgroundColor = "";
	}
	else {
		var v = (line[colorBy] - min) / (max - min) / 2;
		row.style.backgroundColor = isNaN(v) ? "" : ("rgba(0, 0, 255, " + v + ")");
	}
	return row;
}

function getMinMax(minmax, colorBy) {
	minmax[0] = minmax[1] = null;
	for (var i = 1; i < allData.length; i++) {
		if (allData[i].name != "root" && allData[i].name != "TOTAL") {
			var v = allData[i][colorBy]
			if (minmax[1] == null || v > minmax[1]) {
				minmax[1] = v;
			}
			if (minmax[0] == null || v < minmax[0]) {
				minmax[0] = v;
			}
		}
	}
}
