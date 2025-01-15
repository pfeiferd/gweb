/* Software: gweb, License: Apache 2.0 subject to “Commons Clause” License Condition v1.0, Licensor: Daniel Pfeifer (daniel.pfeifer@progotec.de) */
//
allData["res"] = null;
var resById = null;
var selectedRes = null;


function clearResForm() {
	bindResToForm(newRes());
	checkResInForm();
	enableResForm(false);
}

function loadRes(async) {
	if (loggedInUser == null) {
		allData["res"] = [];
		resById = {};
		selectedRes = null;
	}
	else if (allData["res"] == null) {
		var request = createAjaxRequest();
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				allData["res"] = evalOrHandleError(request);
				resById = {};
				for (var i = 0; i < allData["res"].length; i++) {
					resById[allData["res"][i].id] = allData["res"][i];
				}
				selectedRes = null;
				clearResForm();
				loadUsers(false);
				updateResTable();

			}
		};
		if (isAdmin(loggedInUser)) {
			request.open("GET", restPath + "/ResourceService/getAll/", async);
		}
		else {
			request.open("GET", restPath + "/ResourceService/getByUser/" + loggedInUser.id, async);
		}
		request.send();
	}
}

function updateResTable() {
	var table = document.getElementById("restable");
	var input = document.getElementById("ressearchfield");
	var filter = input.value.toUpperCase();
	clearTable(table);

	let recs = 0;
	for (var i = 0; i < allData["res"].length; i++) {
		var res = allData["res"][i];

		var tr = "<tr class=\"";
		tr = tr + ((res == selectedRes) ? "tselected" : "tnormal");
		tr = tr + "\"  onclick=\"selectRes(" + i + ")\">";
		tr = tr + "<td>" + htmlEscape(res.id) + "</td>";
		tr = tr + "<td>" + htmlEscape(res.name) + "</td>";
		tr = tr + "<td>" + htmlEscape(res.userId);
		if (usersById != null && usersById[res.userId] != null) {
			tr = tr + ": " + htmlEscape(usersById[res.userId].login);
		}
		tr = tr + "</td>";
		tr = tr + "<td>" + htmlEscape(res.url) + "</td>";
		tr = tr + "</tr>";

		var row = createTableRow(tr)
		filterTableRow(filter, row);
		if (filterTableRow(filter, row)) {
			recs++;
		}
		table.children[1].appendChild(row);
	}
	var ntrecs = document.getElementById("ntres");
	ntrecs.innerHTML = allData["res"].length;
	var nrecs = document.getElementById("nres");
	nrecs.innerHTML = recs;

	updateFastqURLList(getJobUserFromForm());
}

function selectRes(i) {
	if (selectedRes == allData["res"][i]) {
		return;
	}
	var changeF = () => {
		selectedRes = allData["res"][i];
		var table = document.getElementById("restable");
		unselectTableRows(table);
		table.children[1].children[i].className = "tselected";

		bindResToForm(selectedRes);
		checkResInForm();
		enableResForm(isJobRunner(loggedInUser));
	};
	if (hasResInFormChanged()) {
		cConfirm("reallyChangeSelection", changeF);
	}
	else {
		changeF();
	}
}

function resetRes() {
	if (selectedRes != null) {
		bindResToForm(selectedRes);
		checkResInForm();
	}
}

function checkResInForm() {
	var validated = validateResInForm();
	var changed = hasResInFormChanged();

	dataChangedInTab("res", changed);

	document.getElementById("deleteresbutton").disabled = !isJobRunner(loggedInUser) || selectedRes == null || changed || selectedRes.id == -1;
	document.getElementById("createresbutton").disabled = !isJobRunner(loggedInUser) || (selectedRes != null && changed);
	document.getElementById("resetresbutton").disabled = !changed;
	document.getElementById("saveresbutton").disabled = !validated || !changed;
}

function hasResInFormChanged() {
	return selectedRes != null && !deepEqual(selectedRes, extractResFromForm(newRes()));
}

function validateResInForm() {
	var res = newRes();
	extractResFromForm(res);

	var validU = !updateMandatory("resnamefield", selectedRes != null && res.name == "");
	var validF = !updateMandatory("resurlfield", selectedRes != null && 
		((res.type == "HTTP_URL" && !isValidHttpUrl(res.url)) || 
		 (res.type == "FILE_PATH" && (typeof(res.url) != "string" || res.url == ""))));
	var validUser = !updateMandatory("forresuser", selectedRes != null && res.userId == -1);

	return validU && validF && validUser;
}

function createRes() {
	var table = document.getElementById("restable");
	unselectTableRows(table);

	selectedRes = newRes();
	selectedRes.userId = loggedInUser.id;
	bindResToForm(selectedRes);

	checkResInForm();
	enableResForm(isJobRunner(loggedInUser));
}

function newRes() {
	var res = {};
	res.id = -1;
	res.name = "";
	res.url = "";
	res.type = "HTTP_URL";
	res.userId = -1;

	return res;
}

function saveRes() {
	extractResFromForm(selectedRes);

	var request = createAjaxRequest();
	if (selectedRes.id == -1) {
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				selectedRes.id = evalOrHandleError(request);
				bindResToForm(selectedRes);
				allData["res"].push(selectedRes);
				updateResTableAndForm();
			}
		};
		request.open("POST", restPath + "/ResourceService/create", true);
	}
	else {
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				evalOrHandleError(request);
				updateResTableAndForm();
				updateJobTable();
			}
		};
		request.open("POST", restPath + "/ResourceService/update", true);
	}
	request.setRequestHeader("Content-Type", "application/json");
	request.send(JSON.stringify(selectedRes));
}

function updateResTableAndForm() {
	clearSearchField("res");
	sortByCurrentField("res");
	updateResTable();
	selectResById(selectedRes.id);
	checkResInForm();
	enableResForm(true);
}

function deleteRes() {
	cConfirm("reallyDeleteSelection", () => {
		var request = createAjaxRequest();
		if (selectedRes.id != -1) {
			request.onreadystatechange = function() {
				if (request.readyState == 4) {
					evalOrHandleError(request);
					var index = allData["res"].indexOf(selectedRes);
					if (index != -1) {
						allData["res"].splice(index, 1);
					}
					selectedRes = null;
					clearSearchField("res");
					sortByCurrentField("res");
					updateResTable();
					bindResToForm(newRes());
					checkResInForm();
					enableResForm(false);
				}
			};
			request.open("GET", restPath + "/ResourceService/remove/" + selectedRes.id, true);
			request.setRequestHeader("Content-Type", "application/json");
			request.send();
		}
	});
}

function selectResById(id) {
	for (var i = 0; i < allData["res"].length; i++) {
		if (allData["res"][i].id == id) {
			var table = document.getElementById("restable");
			table.children[1].children[i].className = "tselected";
		}
	}
}

function extractResFromForm(res) {
	var id = document.getElementById("residtext").innerHTML;
	res.id = id == "" ? -1 : parseInt(id);
	res.name = document.getElementById("resnamefield").value;
	res.url = document.getElementById("resurlfield").value;

	var options = document.getElementById("forresuser").selectedOptions;
	res.userId = options.length == 0 ? -1 : parseInt(options[0].value);
	options = document.getElementById("restype").selectedOptions;
	res.type = options[0].value;

	return res;
}

function bindResToForm(res) {
	document.getElementById("residtext").innerHTML = res.id == -1 ? "" : res.id;
	document.getElementById("resnamefield").value = res.name;
	document.getElementById("resurlfield").value = res.url;

	var options = document.getElementById("forresuser").options;
	for (var i = 0; i < options.length; i++) {
		if (options[i].value == res.userId) {
			document.getElementById("forresuser").value = res.userId;
			break;
		}
	}
	
	options = document.getElementById("restype").options;
	for (var i = 0; i < options.length; i++) {
		if (options[i].value == res.type) {
			document.getElementById("restype").value = res.type;
			break;
		}
	}
}

function enableResForm(enable) {
	document.getElementById("resnamefield").disabled = !enable;
	document.getElementById("resurlfield").disabled = !enable;
	document.getElementById("forresuser").disabled = !enable;
	document.getElementById("restype").disabled = !enable;
	
	var options = document.getElementById("restype").options;
	for (var i = 0; i < options.length; i++) {
		if (options[i].value == "FILE_PATH") {
			options[i].disabled = filePathRole == null || 
				(filePathRole == "ADMIN" && !isAdmin(loggedInUser)) || 
				(filePathRole == "RUN_JOBS" && !isJobRunner(loggedInUser));
			break;
		}
	}
}

function updownResTable(up) {
	if (selectedRes != null) {
		let table = document.getElementById("restable");
		let nextIndex = -1;
		let index = allData["res"].indexOf(selectedRes);
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
			selectRes(nextIndex);
			table.children[1].children[i].scrollIntoView({behavior: "smooth", block: "center"});
		}
	}
}