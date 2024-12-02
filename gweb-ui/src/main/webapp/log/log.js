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
		ok: "OK",
		nosession: "Ihre Sitzung wurde beendet. Bitte melden Sie sich bei der Hauptanwendung an.",
		novaliddb: "Ungültige oder fehlende DB ID. Keine Daten verfügbar.",
		logfor: "Log für "
	},
	en: {
		de: "Deutsch",
		en: "English",
		user: "User:",
		servererror: "Error during server call",
		statuscode: "Status Code:",
		statustext: "Status Text:",
		responsetext: "Message:",
		ok: "OK",
		nosession: "Your session has terminated. Please login via the main application.",
		novaliddb: "Inavlid oder missing DB ID. No data to show.",
		logfor: "Log for "
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

var job = null;

function downloadCSV(jobId) {
	var request = createAjaxRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			if (request.status == 200 || request.status == 204) {
				var div = document.getElementById("tablediv");
				div.textContent = request.responseText;
			}
			else {
				var tab = document.getElementById("jobtitle");
				tab.textContent = "Job ?";
				handleRequestError(request);
			}
		};
	}
	request.open("GET", restPath + "/JobService/getLog/" + jobId, true);
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

function loadJob(id) {
	var request = createAjaxRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			if (request.status == 200 || request.status == 204) {
				job = request.responseText == "" ? null : JSON.parse(request.responseText);
				if (job != null) {
					var tab = document.getElementById("jobtitle");
					tab.textContent = "Job " + job.id + ": " + job.name;
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