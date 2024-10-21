/* Software: gweb, License: Apache 2.0 subject to “Commons Clause” License Condition v1.0, Licensor: Daniel Pfeifer (daniel.pfeifer@progotec.de) */
//
var loggedInUser = null;

function checkLogin() {
	var user = document.getElementById("fuser").value.trim();
	var password = document.getElementById("fpassword").value.trim();

	var v1 = updateMandatory("fuser", user == "");
	var v2 = updateMandatory("fpassword", password == "");
	document.getElementById("loginbutton").disabled = v1 || v2;
}

function loginUser() {
	var request = createAjaxRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			var result = evalOrHandleError(request);
			loggedIn(result);
			if (result == null) {
				clearLoginFields(true);
				showScreen("login");
			}
			else {
				showTab(state.currentTab);
			}
		}
	};
	clearData();
	var user = document.getElementById("fuser").value.trim();
	var password = document.getElementById("fpassword").value.trim();
	loggedIn(null);
	clearLoginFields(false);
	request.open("POST", restPath + "/UserService/login/", false);
	request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
	var body = "login=" + encodeURIComponent(user) + "&" + "password=" + encodeURIComponent(password);
	request.send(body);
}

function logoutUser() {
	var changeF = () => {
		clearData();
		loggedInUser = null;
		showScreen("login");
		var request = createAjaxRequest();
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				evalOrHandleError(request);
			}
		};
		request.open("GET", restPath + "/UserService/logout/", true);
		request.send();
	};
	if (someDataInFormsChanged()) {
		cConfirm("reallyLogout", changeF);
	}
	else {
		changeF();
	}
}

function loggedIn(user) {
	loggedInUser = user;

	document.getElementById("userinfo").style.display = loggedInUser == null ? "none" : "block";
	document.getElementById("username").textContent = loggedInUser == null ? "" : loggedInUser.login;
	updateRole();
	if (loggedInUser != null) {
		document.getElementById("tabs").style.display = "block";
	}
}

function loadLoggedInUser() {
	var request = createAjaxRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			var result = evalOrHandleError(request);
			loggedIn(result);
			if (result == null) {
				clearData();
				showScreen("login");
			}
			else {
				showTab(state.currentTab);
				clearSearchFields();
			}
		}
	};
	loggedInUser = null;
	clearLoginFields(false);
	// Must be synchronous.
	request.open("GET", restPath + "/UserService/getLoggedInUser", false);
	request.send();
}

function clearLoginFields(loginfailed) {
	document.getElementById("loginfailed").style.display = loginfailed ? "inline" : "none";
	document.getElementById("autologout").style.display = "none";
	document.getElementById("fuser").value = "";
	document.getElementById("fpassword").value = "";
	checkLogin();
}
