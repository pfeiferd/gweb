/* Software: gweb, License: Apache 2.0 subject to “Commons Clause” License Condition v1.0, Licensor: Daniel Pfeifer (daniel.pfeifer@progotec.de) */
//
allData["person"] = null;
var personsById = null;
var selectedPerson = null;

function clearPersonForm() {
	bindPersonToForm(newPerson());
	checkPersonInForm();
	enablePersonForm(false);
}

function loadPersons(async) {
	if (loggedInUser == null) {
		allData["person"] = [];
		personsById = {};
		selectedPerson = null;
	}
	else if (allData["person"] == null) {
		if (!isAdmin(loggedInUser)) {
			var request = createAjaxRequest();
			request.onreadystatechange = function() {
				if (request.readyState == 4) {
					allData["person"] = [evalOrHandleError(request)];
					personsById = {};
					for (var i = 0; i < allData["person"].length; i++) {
						personsById[allData["person"][i].id] = allData["person"][i];
					}
					selectedPerson = null;
					clearPersonForm();
					updatePersonTable();
				}
			};
			request.open("GET", restPath + "/PersonService/get/" + loggedInUser.personId, async);
			request.send();
		}
		else {
			var request = createAjaxRequest();
			request.onreadystatechange = function() {
				if (request.readyState == 4) {
					allData["person"] = evalOrHandleError(request);
					personsById = {};
					for (var i = 0; i < allData["person"].length; i++) {
						personsById[allData["person"][i].id] = allData["person"][i];
					}
					selectedPerson = null;
					clearPersonForm();
					updatePersonTable();
				}
			};
			request.open("GET", restPath + "/PersonService/getAll/", async);
			request.send();
		}
	}
}

function updatePersonTable() {
	var table = document.getElementById("persontable");
	var input = document.getElementById("personsearchfield");
	var filter = input.value.toUpperCase();
	clearTable(table);

	let recs = 0;
	for (var i = 0; i < allData["person"].length; i++) {
		var person = allData["person"][i];

		var tr = "<tr class=\"";
		tr = tr + ((person == selectedPerson) ? "tselected" : "tnormal");
		tr = tr + "\" onclick=\"selectPerson(" + i + ")\">";
		tr = tr + "<td>" + htmlEscape(person.id) + "</td>";
		tr = tr + "<td>" + htmlEscape(person.firstName) + "</td>";
		tr = tr + "<td>" + htmlEscape(person.lastName) + "</td>";
		tr = tr + "</tr>";

		var row = createTableRow(tr);
		filterTableRow(filter, row)
		if (filterTableRow(filter, row)) {
			recs++;
		}
		table.children[1].appendChild(row);
	}
	var ntrecs = document.getElementById("ntperson");
	ntrecs.innerHTML = allData["person"].length;
	var nrecs = document.getElementById("nperson");
	nrecs.innerHTML = recs;

	var select = document.getElementById("forperson");
	var len = select.options.length;
	for (var i = 0; i < len; i++) {
		select.remove(0);
	}
	for (var i = 0; i < allData["person"].length; i++) {
		var person = allData["person"][i];

		var opt = document.createElement('option');
		opt.value = person.id;
		opt.innerHTML = person.id + ": " + htmlEscape(person.firstName) + " " + htmlEscape(person.lastName);
		select.appendChild(opt);
	}
	select.value = -1;
}

function selectPerson(i) {
	if (selectedPerson == allData["person"][i]) {
		return;
	}
	var changeF = () => {
		selectedPerson = allData["person"][i];
		var table = document.getElementById("persontable");
		unselectTableRows(table);
		table.children[1].children[i].className = "tselected";

		bindPersonToForm(selectedPerson);
		checkPersonInForm();
		enablePersonForm(isAdmin(loggedInUser));
	};
	if (hasPersonInFormChanged()) {
		cConfirm("reallyChangeSelection", changeF);
	}
	else {
		changeF();
	}
}

function resetPerson() {
	if (selectedPerson != null) {
		bindPersonToForm(selectedPerson);
		checkPersonInForm();
	}
}

function checkPersonInForm() {
	var validated = validatePersonInForm();
	var changed = hasPersonInFormChanged();

	dataChangedInTab("persons", changed);

	document.getElementById("deletepersonbutton").disabled = !isAdmin(loggedInUser) || selectedPerson == null || changed || selectedPerson.id == -1;
	document.getElementById("createpersonbutton").disabled = !isAdmin(loggedInUser) || (selectedPerson != null && changed);
	document.getElementById("resetpersonbutton").disabled = !changed;
	document.getElementById("savepersonbutton").disabled = !validated || !changed;
}

function hasPersonInFormChanged() {
	return selectedPerson != null && !deepEqual(selectedPerson, extractPersonFromForm(newPerson()));
}

function validatePersonInForm() {
	var person = newPerson();
	extractPersonFromForm(person);

	var validU = !updateMandatory("firstnamefield", selectedPerson != null && person.firstName == "");
	var validF = !updateMandatory("lastnamefield", selectedPerson != null && person.lastName == "");

	return validU && validF;
}

function createPerson() {
	var table = document.getElementById("persontable");
	unselectTableRows(table);

	selectedPerson = newPerson();
	bindPersonToForm(selectedPerson);

	checkPersonInForm();
	enablePersonForm(isAdmin(loggedInUser));
}

function newPerson() {
	var person = {};
	person.id = -1;
	person.firstName = "";
	person.lastName = "";

	return person;
}

function savePerson() {
	extractPersonFromForm(selectedPerson);

	var request = createAjaxRequest();
	if (selectedPerson.id == -1) {
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				selectedPerson.id = evalOrHandleError(request);
				bindPersonToForm(selectedPerson);
				allData["person"].push(selectedPerson);
				updatePersonTableAndForm();
			}
		};
		request.open("POST", restPath + "/PersonService/create", true);
	}
	else {
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				evalOrHandleError(request);
				updatePersonTableAndForm();
				updateUserTable();
			}
		};
		request.open("POST", restPath + "/PersonService/update", true);
	}
	request.setRequestHeader("Content-Type", "application/json");
	request.send(JSON.stringify(selectedPerson));
}

function updatePersonTableAndForm() {
	clearSearchField("person");
	sortByCurrentField("person");
	updatePersonTable();
	selectPersonById(selectedPerson.id);
	checkPersonInForm();
	checkUserInForm();
}

function deletePerson() {
	cConfirm("reallyDeleteSelection", () => {
		var request = createAjaxRequest();
		if (selectedPerson.id != -1) {
			request.onreadystatechange = function() {
				if (request.readyState == 4) {
					evalOrHandleError(request);
					var index = allData["person"].indexOf(selectedPerson);
					if (index != -1) {
						allData["person"].splice(index, 1);
					}
					selectedPerson = null;
					clearSearchField("person");
					sortByCurrentField("person");
					updatePersonTable();
					bindPersonToForm(newPerson());
					checkPersonInForm();
					checkUserInForm();
					enablePersonForm(false);
				}
			};
			request.open("GET", restPath + "/PersonService/remove/" + selectedPerson.id, true);
			request.setRequestHeader("Content-Type", "application/json");
			request.send();
		}
	});
}

function selectPersonById(id) {
	for (var i = 0; i < allData["person"].length; i++) {
		if (allData["person"][i].id == id) {
			var table = document.getElementById("persontable");
			table.children[1].children[i].className = "tselected";
		}
	}
}

function extractPersonFromForm(person) {
	var id = document.getElementById("personidtext").innerHTML;
	person.id = id == "" ? -1 : parseInt(id);
	person.firstName = document.getElementById("firstnamefield").value;
	person.lastName = document.getElementById("lastnamefield").value;

	return person;
}

function bindPersonToForm(person) {
	document.getElementById("personidtext").innerHTML = person.id == -1 ? "" : person.id;
	document.getElementById("firstnamefield").value = person.firstName;
	document.getElementById("lastnamefield").value = person.lastName;
}

function enablePersonForm(enable) {
	document.getElementById("firstnamefield").disabled = !enable;
	document.getElementById("lastnamefield").disabled = !enable;
}

function updownPersonTable(up) {
	if (selectedPerson != null) {
		let table = document.getElementById("persontable");
		let nextIndex = -1;
		let index = allData["person"].indexOf(selectedPerson);
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
			selectPerson(nextIndex);
			table.children[1].children[i].scrollIntoView({behavior: "smooth", block: "center"});
		}
	}
}