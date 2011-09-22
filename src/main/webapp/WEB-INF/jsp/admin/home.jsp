<%@ page contentType="text/html" pageEncoding="UTF-8" session="false" %>
<%@ page import = "java.util.Date" %> 
<%@ page import = "org.springframework.context.ApplicationContext,  
		org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ include file="/common/taglibs.jsp" %>		

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="pageName" content="home"/>
  		<link rel="stylesheet" href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/themes/base/jquery-ui.css" type="text/css" media="all" />
  		<script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery-ui-1.8.custom.min.js"></script>        
        <title>Atlas of Living Australia - BIE Admin</title>
    </head>
    
    <body bgcolor="white">
        <div id="header">
            <div id="breadcrumb">
                <a href="${initParam.centralServer}">Home</a>
                <span class="current">Admin</span>
            </div>
        </div><!--close header-->
        <div class="section">
            <h1 align="center">Welcome to the Atlas of Living Australia: <strong>BIE Admin</strong>.</h1>
            <hr/>
            <h2> Photo Upload </h2>	
            <table>
                    <tr><td><a href="admin/upload">Upload Single File to BIE</a></td></tr>
                    <tr><td><a href="admin/multiUpload">Upload Multiple Files to BIE</a></td></tr>                    
            </table>
     
            <c:if test='<%= request.isUserInRole("ROLE_SYSTEM_ADMIN")%>'>

<div style="display:none" id="dialog-modal" title="Process dialog">
	<p>Process in progress. <br/>Please wait..........</p>
</div>
<script type="text/javascript">
function callAdminWs(wsName) {
          var url = "${pageContext.request.contextPath}/admin/" + wsName;
  		$( "#dialog:ui-dialog" ).dialog( "destroy" );
  		
  		$( "#dialog-modal" ).dialog({
  			height: 140,
  			modal: true
  		});

		var jqxhr = $.getJSON(url, function(data){
	
		$( "#dialog-modal" ).dialog("close");
                alert("response Text: " + jqxhr.responseText + " (" + jqxhr.status + ")");

}).error(function(jqxhr) {
	$( "#dialog-modal" ).dialog("close");
                // catch ajax errors (requiers JQuery 1.5+) - usually 500 error
                if(jqxhr.responseText != null && jqxhr.responseText.length > 0){
                	alert('response Text: ' + jqxhr.responseText + " (" + jqxhr.status + ")");
	}
});

}
</script>


            <hr/>
            <h2> Backend Data Process</h2>	
            <table>
            		<tr><td><a href="javascript:callAdminWs('isReadOnly')">isReadOnly</a></td></tr>
                    <tr><td><a href="javascript:callAdminWs('reloadAllRanks')">Reload All Ranks</a></td></tr>
                    <tr><td><a href="javascript:callAdminWs('loadCaab')">Reload Prefered Common Name (Fish only)</a></td></tr>
                    <tr><td><a href="javascript:callAdminWs('optimise')">Optimise Solr Index</a></td></tr>
                    <tr><td><a href="javascript:callAdminWs('reloadCollections')">Reload Collections</a></td></tr>
                    <tr><td><a href="javascript:callAdminWs('reloadInstitutions')">Reload Institutions</a></td></tr>
                    <tr><td><a href="javascript:callAdminWs('reloadDataProviders')">Reload Data Providers</a></td></tr>
                    <tr><td><a href="javascript:callAdminWs('reloadDataResources')">Reload Data Resources</a></td></tr>                    
            </table>
            <hr/>
            </c:if>
            
            <h3>current date is :<%= new Date().toString()%></h3>
            <h2> Request Information </h2>
            <div>
                    JSP Request Method: <%= request.getMethod() %><br/>
                    Request URI: <%= request.getRequestURI() %><br/>
                    Request Protocol: <%= request.getProtocol() %><br/>
                    Servlet path: <%= request.getServletPath() %><br/>
                    Path info: <%= request.getPathInfo() %><br/>
                    Server name: <%= request.getServerName() %><br/>
                    Server port: <%= request.getServerPort() %><br/>
                    Remote user: <%= request.getRemoteUser() %><br/>
                    Remote address: <%= request.getRemoteAddr() %><br/>
                    Remote host: <%= request.getRemoteHost() %><br/>
                    X-Forwarded-For: <%= request.getHeader("X-Forwarded-For") %><br/>
            </div>
        </div>
    </body>    
</html>