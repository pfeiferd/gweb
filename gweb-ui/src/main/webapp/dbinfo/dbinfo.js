/* Software: gweb, License: Apache 2.0 subject to “Commons Clause” License Condition v1.0, Licensor: Daniel Pfeifer (daniel.pfeifer@progotec.de) */
//
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
			history.replaceState(state, "", "?lan=" + state.currentLan + "&dbid=" + dbId);
		}
		else {
			history.pushState(state, "", "?lan=" + state.currentLan + "&dbid=" + dbId);
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
		kmers: "Gespeicherte k-Mere",
		ok: "OK",
		nosession: "Ihre Sitzung wurde beendet. Bitte melden Sie sich bei der Hauptanwendung an.",
		novaliddb: "Ungültige oder fehlende DB ID. Keine Daten verfügbar.",
		on: " auf "
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
		kmers: "Stored k-mers",
		ok: "OK",
		nosession: "Your session has terminated. Please login via the main application.",
		novaliddb: "Inavlid oder missing DB ID. No data to show.",
		on: " on "
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
	loadDB(dbId);
	downloadCSV(dbId);
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
	var input = document.getElementById("dbsearchfield");
	var filter = input.value.toUpperCase();
	var table = document.getElementById("dbtable");
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

var db = null;
var sortOrder = true;
var lastSortField = null;
var allData = null;
var fields = ["name", "rank", "taxid", "kmers"];
var fieldTypes = ["s", "s", "s", "i"];

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
		updateDBTable();
	}
}

function downloadCSV(dbId) {
	var request = createAjaxRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			if (request.status == 200 || request.status == 204) {
				allData = csvToObjs(request.responseText);
				updateDBTable();
			}
			else {
				var tab = document.getElementById("dbtitle");
				tab.textContent = "DB ?";
				handleRequestError(request);
			}
		};
	}
	request.open("GET", restPath + "/DBService/getInfo/" + dbId, true);
	request.setRequestHeader("Content-Type", "application/json");
	request.send();
}

function handleRequestError(request) {
	if (request.status == 401) {
		cAlert("nosession", () => { window.close(); });
	}
	else if (request.status == 404) {
		cAlert("novaliddb", () => { window.close(); });
	} else {
		showError(request);
		throw "Server request error";
	}
}

function loadDB(id) {
	var request = createAjaxRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			if (request.status == 200 || request.status == 204) {
				db = request.responseText == "" ? null : JSON.parse(request.responseText);
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

function updateDBTable() {
	var table = document.getElementById("dbtable");
	var input = document.getElementById("dbsearchfield");
	var filter = input.value.toUpperCase();
	var rowCount = table.rows.length;
	for (var i = 1; i < rowCount; i++) {
		table.deleteRow(1);
	}
	for (var i = 0; i < allData.length; i++) {
		var line = allData[i];

		var tr = "<tr class=\"tnormal\">";
		tr = tr + "<td>" + line.line + "</td>";
		tr = tr + "<td>" + htmlEscape(line.name) + "</td>";
		tr = tr + "<td>" + htmlEscape(line.rank) + "</td>";
		tr = tr + "<td>" + htmlEscape(line.taxid) + "</td>";
		tr = tr + "<td>" + htmlEscape(line.kmers) + "</td>";
		tr = tr + "</tr>";

		table.children[1].appendChild(filterTableRow(filter, createTableRow(tr)));
	}
}