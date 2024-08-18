<%@page session="false"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="org.metagene.gweb.Version"%>
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
%>
<script type="text/javascript">
var state = {};
state.currentLan = "<%=lan%>";
state.currentTab = "<%=tab%>";

var mainPath = "<%=request.getContextPath()%>";
var restPath = mainPath + "/rest";
</script>
<script src="ui.js?v=<%=version%>"></script>
<script src="login.js?v=<%=version%>"></script>
<script src="job.js?v=<%=version%>"></script>
<script src="res.js?v=<%=version%>"></script>
<script src="db.js?v=<%=version%>"></script>
<script src="person.js?v=<%=version%>"></script>
<script src="user.js?v=<%=version%>"></script>
