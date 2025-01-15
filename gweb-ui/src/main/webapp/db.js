/* Software: gweb, License: Apache 2.0 subject to “Commons Clause” License Condition v1.0, Licensor: Daniel Pfeifer (daniel.pfeifer@progotec.de) */
//
allData["db"] = null;
var dbsById = null;
var selectedDB = null;

function clearDBForm() {
	bindDBToForm(newDB());
	checkDBInForm();
	enableDBForm(false);
}

function loadDBs(async) {
	if (loggedInUser == null) {
		allData["db"] = [];
		dbsById = {};
		selectedDB = null;
	}
	else if (allData["db"] == null) {
		var request = createAjaxRequest();
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				allData["db"] = evalOrHandleError(request);
				dbsById = {};
				for (var i = 0; i < allData["db"].length; i++) {
					dbsById[allData["db"][i].id] = allData["db"][i];
				}
				selectedDB = null;
				clearDBForm();
				updateDBTable();
				updateForDB();
			}
		};
		request.open("GET", restPath + "/DBService/getAll/", async);
		request.send();
	}
}

function updateDBTable() {
	var table = document.getElementById("dbtable");
	var input = document.getElementById("dbsearchfield");
	var filter = input.value.toUpperCase();
	clearTable(table);

	for (var i = 0; i < allData["db"].length; i++) {
		var DB = allData["db"][i];

		var tr = "<tr class=\"";
		tr = tr + ((DB == selectedDB) ? "tselected" : "tnormal");
		tr = tr + "\" onclick=\"selectDB(" + i + ")\">";
		tr = tr + "<td>" + htmlEscape(DB.id) + "</td>";
		tr = tr + "<td>" + htmlEscape(DB.name) + "</td>";
		tr = tr + "<td>" + htmlEscape(DB.dbFilePrefix) + "</td>";
		tr = tr + "</tr>";

		table.children[1].appendChild(filterTableRow(filter, createTableRow(tr)));
	}
}

function updateForDB() {
	var select = document.getElementById("fordb");
	var v = -1;
	if (select.selectedOptions.length == 1) {
		v = select.selectedOptions[0].value;
	}
	var len = select.options.length;
	for (var i = 0; i < len; i++) {
		select.remove(0);
	}
	for (var i = 0; i < allData["db"].length; i++) {
		var db = allData["db"][i];
		var opt = document.createElement('option');
		opt.value = db.id;
		opt.innerHTML = db.id + ": " + htmlEscape(db.name);
		opt.disabled = !db.installed;
		select.appendChild(opt);
	}
	select.value = v;
}

function selectDB(i) {
	if (selectedDB == allData["db"][i]) {
		return;
	}
	var changeF = () => {
		selectedDB = allData["db"][i];
		var table = document.getElementById("dbtable");
		unselectTableRows(table);
		table.children[1].children[i].className = "tselected";

		bindDBToForm(selectedDB);
		checkDBInForm();
		enableDBForm(isAdmin(loggedInUser));
	};
	if (hasDBInFormChanged()) {
		cConfirm("reallyChangeSelection", changeF);
	}
	else {
		changeF();
	}
}

function resetDB() {
	if (selectedDB != null) {
		bindDBToForm(selectedDB);
		checkDBInForm();
	}
}

function checkDBInForm() {
	var validated = validateDBInForm();
	var changed = hasDBInFormChanged();

	dataChangedInTab("dbs", changed);

	document.getElementById("deletedbbutton").disabled = !isAdmin(loggedInUser) || selectedDB == null || changed || selectedDB.id == -1;
	document.getElementById("createdbbutton").disabled = !isAdmin(loggedInUser) || (selectedDB != null && changed);
	document.getElementById("resetdbbutton").disabled = !changed;
	document.getElementById("savedbbutton").disabled = !validated || !changed;

	updateDBInstalled(selectedDB);
}

function hasDBInFormChanged() {
	return selectedDB != null && !deepEqual(selectedDB, completeDBData(extractDBFromForm(newDB()), selectedDB));
}

function completeDBData(db, fromDB) {
	db.infoExists = fromDB.infoExists;
	db.installed = fromDB.installed;

	return db;
}

function validateDBInForm() {
	var db = newDB();
	extractDBFromForm(db);

	var validU = !updateMandatory("dbnamefield", selectedDB != null && db.name == "");
	var validF = !updateMandatory("dbfileprefixfield", selectedDB != null && db.dbFilePrefix == "");
	var validUrl = db.installURL == null || db.installURL == "" || isValidHttpUrl(db.installURL);
	var validMD5 = db.installMD5 == null || (isValidHex(db.installMD5) && db.installMD5.length == 32);

	return validU && validF && validUrl && validMD5;
}

function isValidHex(h) {
	var res = h.match(/^[0-9a-fA-F]+$/g);
	return res != null;
}

function createDB() {
	var table = document.getElementById("dbtable");
	unselectTableRows(table);

	selectedDB = newDB();
	bindDBToForm(selectedDB);

	checkDBInForm();
	enableDBForm(isAdmin(loggedInUser));
}

function newDB() {
	var db = {};
	db.id = -1;
	db.name = "";
	db.dbFilePrefix = "";
	db.installURL = null;
	db.installMD5 = null;
	db.installed = false;
	db.infoExists = false;

	return db;
}

function saveDB() {
	extractDBFromForm(selectedDB);

	var request = createAjaxRequest();
	if (selectedDB.id == -1) {
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				selectedDB.id = evalOrHandleError(request);
				bindDBToForm(selectedDB);
				allData["db"].push(selectedDB);
				updateDBTableAndForm();
			}
		};
		request.open("POST", restPath + "/DBService/create", true);
	}
	else {
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				evalOrHandleError(request);
				updateDBTableAndForm();
				updateJobTable();
			}
		};
		request.open("POST", restPath + "/DBService/update", true);
	}
	request.setRequestHeader("Content-Type", "application/json");
	request.send(JSON.stringify(selectedDB));
}

function updateDBTableAndForm() {
	clearSearchField("db");
	sortByCurrentField("db");
	updateDBTable();
	selectDBById(selectedDB.id);
	checkDBInForm();
	checkJobInForm();
	enableDBForm(true);
}

function deleteDB() {
	cConfirm("reallyDeleteSelection", () => {
		var request = createAjaxRequest();
		if (selectedDB.id != -1) {
			request.onreadystatechange = function() {
				if (request.readyState == 4) {
					evalOrHandleError(request);
					var index = allData["db"].indexOf(selectedDB);
					if (index != -1) {
						allData["db"].splice(index, 1);
					}
					selectedDB = null;
					clearSearchField("db");
					sortByCurrentField("db");
					updateDBTable();
					bindDBToForm(newDB());
					checkDBInForm();
					checkJobInForm();
					enableDBForm(false);
				}
			};
			request.open("GET", restPath + "/DBService/remove/" + selectedDB.id, true);
			request.setRequestHeader("Content-Type", "application/json");
			request.send();
		}
	});
}

function selectDBById(id) {
	for (var i = 0; i < allData["db"].length; i++) {
		if (allData["db"][i].id == id) {
			var table = document.getElementById("dbtable");
			table.children[1].children[i].className = "tselected";
		}
	}
}

function getDBById(id) {
	for (var i = 0; i < allData["db"].length; i++) {
		if (allData["db"][i].id == id) {
			return allData["db"][i];
		}
	}
	return null;
}

function extractDBFromForm(db) {
	var id = document.getElementById("dbidtext").innerHTML;
	db.id = id == "" ? -1 : parseInt(id);
	db.name = document.getElementById("dbnamefield").value;
	db.dbFilePrefix = document.getElementById("dbfileprefixfield").value;
	db.installURL = document.getElementById("dburlfield").value;
	if (db.installURL == "") {
		db.installURL = null;
	}
	db.installMD5 = document.getElementById("dbmd5field").value;
	if (db.installMD5 == "") {
		db.installMD5 = null;
	}

	return db;
}

function bindDBToForm(db) {
	document.getElementById("dbidtext").innerHTML = db.id == -1 ? "" : db.id;
	document.getElementById("dbnamefield").value = db.name;
	document.getElementById("dbfileprefixfield").value = db.dbFilePrefix;
	document.getElementById("dbinfof").innerHTML = "";
	document.getElementById("dbinfobutton").disabled = true;
	document.getElementById("dbinstallbutton").disabled = true;
	document.getElementById("dburlfield").value = db.installURL = null ? "" : db.installURL;
	document.getElementById("dbmd5field").value = db.installMD5 = null ? "" : db.installMD5;
	if (db.id != -1) {
		updateDBInstalled(db);
		bindDBInfoCSV(db);
	}
	else {
		document.getElementById("dbisinstalled").checked = false;
	}
}

function bindDBInfoCSV(db) {
	if (db.infoExists) {
		var anchor = "<a class=\"datalink\" target=\"_blank\" href=\"" + restPath + "/DBService/getInfo/" + db.id + "\">" + db.dbFilePrefix + "_dbinfo.csv</a>"
		var buttonText = i18n[state.currentLan]["viewjobres"];
		var button = "<button type=\"button\" onclick=\"viewDBInfo(" + db.id + ")\">" +
			"<span data-i18n=\"viewdbres\">" + buttonText + "</span> " +
			"<svg class=\"featherc\"><use href=\"feather-sprite.svg#bar-chart-2\"/></svg></button>";
		document.getElementById("dbinfof").innerHTML = anchor + " " + button;
	}
	else {
		document.getElementById("dbinfof").innerHTML = "";
	}
}

function updateDBInstalled(db) {
	document.getElementById("dbisinstalled").checked = db != null && db.installed;
	document.getElementById("dbinstallbutton").disabled = db == null || hasDBInFormChanged() || !isAdmin(loggedInUser) || db.id == -1 || db.installURL == null;
	document.getElementById("dbinfobutton").disabled = db == null || !db.installed  || !isAdmin(loggedInUser);
}

function installDB() {
	if (isAdmin(loggedInUser)) {
		var changeF = () => {
			var request = createAjaxRequest();
			request.onreadystatechange = function() {
				if (request.readyState == 4) {
					evalOrHandleError(request);
					clearJobData();
					loadJobs();
				}
			};
			request.open("GET", restPath + "/JobService/enqueueDBInstall/" + selectedDB.id + "/" + loggedInUser.id, false);
			request.send();
		};
		var checkAndRun = () => {
			if (hasJobInFormChanged()) {
				cConfirm("reallyChangeSelection", changeF);
			}
			else {
				changeF();
			}
		};
		cConfirm(selectedDB.installed ? "reallyReplaceDBInstallation" : "reallyDoDBInstallation", checkAndRun);
	}
}

function viewDBInfo(dbid) {
	window.open(mainPath + "/dbinfo?lan=" + state.currentLan + "&dbid=" + dbid, '_blank').focus();
}

function enableDBForm(enable) {
	document.getElementById("dbnamefield").disabled = !enable;
	document.getElementById("dbfileprefixfield").disabled = !enable || (selectedDB != null && selectedDB.id >= 0);
	document.getElementById("dburlfield").disabled = !enable;
	document.getElementById("dbmd5field").disabled = !enable;
}

function createDBInfo() {
	if (isAdmin(loggedInUser)) {
		var changeF = () => {
			var request = createAjaxRequest();
			request.onreadystatechange = function() {
				if (request.readyState == 4) {
					evalOrHandleError(request);
					clearJobData();
					loadJobs();
				}
			};
			document.getElementById("dbinfobutton").disabled = true;
			request.open("GET", restPath + "/JobService/enqueueDBInfo/" + selectedDB.id + "/" + loggedInUser.id, false);
			request.send();
		};
		var checkAndRun = () => {
			if (hasJobInFormChanged()) {
				cConfirm("reallyChangeSelection", changeF);
			}
			else {
				changeF();
			}
		}
		cConfirm(selectedDB.infoExists ? "reallyReplaceDBInfo" : "reallyDoDBInfo", checkAndRun);
	}
}

function updownDBTable(up) {
	if (selectedDB != null) {
		let table = document.getElementById("dbtable");
		let nextIndex = -1;
		let index = allData["db"].indexOf(selectedDB);
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
			selectDB(nextIndex);
			table.children[1].children[i].scrollIntoView({behavior: "smooth", block: "center"});
			e.preventDefault();
		}
	}
}