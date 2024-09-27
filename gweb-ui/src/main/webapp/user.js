/* Software: gweb, License: Apache 2.0 subject to “Commons Clause” License Condition v1.0, Licensor: Daniel Pfeifer (daniel.pfeifer@progotec.de) */
//
allData["user"] = null;
var usersById = null;
var selectedUser = null;

function clearUserForm() {
	bindUserToForm(newUser());
	checkUserInForm();
	enableUserForm(false);
}

function loadUsers(async) {
	if (loggedInUser == null) {
		allData["user"] = [];
		usersById = {};
		selectedUser = null;
	}
	else if (!isAdmin(loggedInUser)) {
		allData["user"] = [loggedInUser];
		usersById = {};
		usersById[loggedInUser.id] = loggedInUser;
		selectedUser = null;
		clearUserForm();
		loadPersons(false);
		updateUserTable();
	}
	else if (allData["user"] == null) {
		var request = createAjaxRequest();
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				allData["user"] = evalOrHandleError(request);
				usersById = {};
				for (var i = 0; i < allData["user"].length; i++) {
					usersById[allData["user"][i].id] = allData["user"][i];
				}
				selectedUser = null;
				clearUserForm();
				loadPersons(false);
				updateUserTable();
			}
		};
		request.open("GET", restPath + "/UserService/getAll/", async);
		request.send();
	}
}

function updateUserTable() {
	var table = document.getElementById("usertable");
	var input = document.getElementById("usersearchfield");
	var filter = input.value.toUpperCase();
	clearTable(table);

	for (var i = 0; i < allData["user"].length; i++) {
		var user = allData["user"][i];

		var tr = "<tr class=\"";
		tr = tr + ((user == selectedUser) ? "tselected" : "tnormal");
		tr = tr + "\"  onclick=\"selectUser(" + i + ")\">";
		tr = tr + "<td>" + htmlEscape(user.id) + "</td>";
		tr = tr + "<td>" + htmlEscape(user.login) + "</td>";
		tr = tr + "<td><span data-i18n=\"" + user.role + "\">" + i18n[state.currentLan][user.role] + "</span></td>";
		tr = tr + "<td> " + user.personId;
		if (personsById != null && personsById[user.personId] != null) {
			tr = tr + ": " + htmlEscape(personsById[user.personId].firstName + " " + personsById[user.personId].lastName);
		}
		tr = tr + "</td>";
		tr = tr + "</tr>";

		table.children[1].appendChild(filterTableRow(filter, createTableRow(tr)));
	}

	updateUserOption("foruser");
	updateUserOption("forresuser");
}

function updateUserOption(optionId) {
	var select = document.getElementById(optionId);
	var len = select.options.length;
	for (var i = 0; i < len; i++) {
		select.remove(0);
	}
	for (var i = 0; i < allData["user"].length; i++) {
		var user = allData["user"][i];

		var opt = document.createElement('option');
		opt.value = user.id;
		opt.innerHTML = user.id + ": " + htmlEscape(user.login);
		select.appendChild(opt);
	}
	select.value = -1;
}

function selectUser(i) {
	if (selectedUser == allData["user"][i]) {
		return;
	}
	var changeF = () => {
		selectedUser = allData["user"][i];
		var table = document.getElementById("usertable");
		unselectTableRows(table);
		table.children[1].children[i].className = "tselected";

		bindUserToForm(selectedUser);
		checkUserInForm();
		enableUserForm(isAdmin(loggedInUser));
	};
	if (hasUserInFormChanged()) {
		cConfirm("reallyChangeSelection", changeF);
	}
	else {
		changeF();
	}
}

function resetUser() {
	if (selectedUser != null) {
		bindUserToForm(selectedUser);
		checkUserInForm();
	}
}

function checkUserInForm() {
	var validated = validateUserInForm();
	var changed = hasUserInFormChanged();

	dataChangedInTab("users", changed);

	document.getElementById("deleteuserbutton").disabled = !isAdmin(loggedInUser) || selectedUser == null || changed || selectedUser.id == -1;
	document.getElementById("createuserbutton").disabled = !isAdmin(loggedInUser) || (selectedUser != null && changed);
	document.getElementById("resetuserbutton").disabled = !changed;
	document.getElementById("saveuserbutton").disabled = !validated || !changed;
}

function hasUserInFormChanged() {
	return selectedUser != null && !deepEqual(selectedUser, extractUserFromForm(newUser()));
}

function validateUserInForm() {
	var user = newUser();
	extractUserFromForm(user);

	var validU = !updateMandatory("usernamefield", selectedUser != null && user.login == "");
	var validF = !updateMandatory("passwordfield", selectedUser != null && user.id == -1 && (user.password == null || user.password == ""));
	var validP = !updateMandatory("forperson", selectedUser != null && user.personId == -1);

	return validU && validF && validP;
}

function createUser() {
	var table = document.getElementById("usertable");
	unselectTableRows(table);

	selectedUser = newUser();
	bindUserToForm(selectedUser);

	checkUserInForm();
	enableUserForm(isAdmin(loggedInUser));
}

function newUser() {
	var user = {};
	user.id = -1;
	user.login = "";
	user.password = null;
	user.role = "NO_LOGIN";
	user.personId = -1;

	return user;
}

function saveUser() {
	extractUserFromForm(selectedUser);

	var request = createAjaxRequest();
	if (selectedUser.id == -1) {
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				selectedUser.id = evalOrHandleError(request);
				bindUserToForm(selectedUser);
				allData["user"].push(selectedUser);
				updateUserTableAndForm();
			}
		};
		request.open("POST", restPath + "/UserService/create", true);
	}
	else {
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				evalOrHandleError(request);
				updateUserTableAndForm();
				updateResTable();
				updateJobTable();
			}
		};
		request.open("POST", restPath + "/UserService/update", true);
	}
	request.setRequestHeader("Content-Type", "application/json");
	request.send(JSON.stringify(selectedUser));
}

function updateUserTableAndForm() {
	clearSearchField("user");
	sortByCurrentField("user");
	updateUserTable();
	selectUserById(selectedUser.id);
	checkUserInForm();
	checkJobInForm();
}


function deleteUser() {
	cConfirm("reallyDeleteSelection", () => {
		var request = createAjaxRequest();
		if (selectedUser.id != -1) {
			request.onreadystatechange = function() {
				if (request.readyState == 4) {
					evalOrHandleError(request);
					var index = allData["user"].indexOf(selectedUser);
					if (index != -1) {
						allData["user"].splice(index, 1);
					}
					selectedUser = null;
					clearSearchField("user");
					sortByCurrentField("user");
					updateUserTable();
					bindUserToForm(newUser());
					checkUserInForm();
					checkJobInForm();
					enableUserForm(false);
				}
			};
			request.open("GET", restPath + "/UserService/remove/" + selectedUser.id, true);
			request.setRequestHeader("Content-Type", "application/json");
			request.send();
		}
	});
}

function selectUserById(id) {
	for (var i = 0; i < allData["user"].length; i++) {
		if (allData["user"][i].id == id) {
			var table = document.getElementById("usertable");
			table.children[1].children[i].className = "tselected";
		}
	}
}

function extractUserFromForm(user) {
	var id = document.getElementById("useridtext").innerHTML;
	user.id = id == "" ? -1 : parseInt(id);
	user.login = document.getElementById("usernamefield").value;
	user.password = document.getElementById("passwordfield").value;
	if (user.password == "") {
		user.password = null;
	}

	var roleoptions = document.getElementById("selectrole").selectedOptions;
	user.role = roleoptions.length == 0 ? "NONE" : roleoptions[0].value;

	var options = document.getElementById("forperson").selectedOptions;
	user.personId = options.length == 0 ? -1 : parseInt(options[0].value);

	return user;
}

function bindUserToForm(user) {
	document.getElementById("useridtext").innerHTML = user.id == -1 ? "" : user.id;
	document.getElementById("usernamefield").value = user.login;
	document.getElementById("passwordfield").value = user.password;
	document.getElementById("selectrole").value = user.role;

	document.getElementById("forperson").value = user.personId;
	/*
	var options = document.getElementById("forperson").options;
	for (var i = 0; i < options.length; i++) {
		if (options[i].value == user.personId) {
			document.getElementById("forperson").value = user.personId;
			break;
		}
	}
	*/
}

function enableUserForm(enable) {
	document.getElementById("usernamefield").disabled = !enable;
	document.getElementById("passwordfield").disabled = !enable;
	document.getElementById("selectrole").disabled = !enable;
	document.getElementById("forperson").disabled = !enable;
}
