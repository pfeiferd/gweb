<%@page session="false"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="org.metagene.gweb.Version"%>
<%@page import="org.metagene.genestrip.GSConfigKey"%>
<%@page import="org.metagene.gweb.service.create.ServiceCreator"%>
<%@page import="org.metagene.gweb.service.dto.User.UserRole"%>
<%
String version = Version.class.getPackage().getImplementationVersion();
String lan = request.getParameter("lan");
if (!"de".equals(lan)) {
	lan = "en";
}
String tab = request.getParameter("tab");
if (!"dbs".equals(tab) && !"users".equals(tab) && !"persons".equals(tab)) {
	tab = "jobs";
}
UserRole role = null;
try {
	String v = config.getServletContext().getInitParameter(ServiceCreator.FILE_PATH_ROLE);
	role = UserRole.valueOf(v);
	if (!role.subsumes(UserRole.RUN_JOBS)) {
		role = null;
	}
} catch (Exception e) {
	// Ignore on purpose.
}
%>
<script type="text/javascript">
var state = {};
state.currentLan = "<%=lan%>";
state.currentTab = "<%=tab%>";

var mainPath = "<%=request.getContextPath()%>";
var restPath = mainPath + "/rest";

var errorRateDefault = <%=GSConfigKey.MAX_READ_TAX_ERROR_COUNT.getInfo().defaultValue()%>;
var filePathRole = <%=role == null ? "null" : "\"" + role + "\""%>;
</script>
<script src="ui.js?v=<%=version%>"></script>
<script src="login.js?v=<%=version%>"></script>
<script src="job.js?v=<%=version%>"></script>
<script src="res.js?v=<%=version%>"></script>
<script src="db.js?v=<%=version%>"></script>
<script src="person.js?v=<%=version%>"></script>
<script src="user.js?v=<%=version%>"></script>
