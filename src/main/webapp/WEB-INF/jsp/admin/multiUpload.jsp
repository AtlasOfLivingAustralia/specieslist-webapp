<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="org.jasig.cas.client.authentication.AttributePrincipal" %>
<%@ page session="false" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HTML>
	<HEAD>
		<META HTTP-EQUIV="Content-Type"	CONTENT="text/html; charset=windows-1252" />
		<TITLE>File Upload Page</TITLE>
		<script>
		function clearForms()
		{
		  var i;
		  for (i = 0; (i < document.forms.length); i++) {
		    document.forms[i].reset();
		  }
		}
        
        $(document).ready(function() {
            $('#showHideUploads').toggle(
                function(e) {
                    e.preventDefault();
                    $($(this).attr('href')).slideDown();
                },
                function(e) {
                    e.preventDefault();
                    $($(this).attr('href')).slideUp();
                }
            );
        });		
		</script>
		        		        <style type="text/css">
            .error {
                color: #DD3102;
                font-size: 12px;
            }
            input[type="text"], textarea {
                width: 170px;
            }
        </style>		        		
	</HEAD>	
<%
String remoteuser = request.getRemoteUser();
String userName = "";	
if (remoteuser != null && request.isUserInRole("ROLE_ADMIN")) {
    AttributePrincipal principal = (AttributePrincipal) request.getUserPrincipal();    
    if (principal != null) {
        userName = principal.getAttributes().get("firstname").toString() + " " + principal.getAttributes().get("lastname").toString();
    }
}         
%>	
	<BODY  onunload="clearForms()" bgcolor="white" >
		<div id="header">
            <div id="breadcrumb">
                <a href="${initParam.centralServer}">Home</a>
                <a href="${pageContext.request.contextPath}/">Admin</a>
                <span class="current">Photo Upload</span>
            </div>
        </div><!--close header-->	
<% if(userName.length() > 0) {%>	
		<FORM name="filesForm" method="post" enctype="multipart/form-data">
			<fieldset>
                <legend>Upload Multiple Files</legend>
				<table>
					<tbody>
						<tr>
							<td>Scientific Name: </td><td><input type="text" size="40" value="" name="scientificName" /></td>
							<td>User Email: </td><td><input type="text" size="40" id="Email" value="<%= remoteuser%>" name="email" /></td>
						</tr>
						<tr>
							<td>Common Name: </td><td><input type="text" size="40" value="" name="commonName" /></td>
							<td>User Name: </td><td><input type="text" size="40" value="<%= userName%>" name="userName" /></td>
						</tr>
						<tr>
							<td>Title: </td><td><input type="text" size="40" value="" name="title" /></td>
							<td>Rank: </td>
							<td><select name="rank">
								<option value=""></option>
								<option value="Kingdom">Kingdom</option>
								<option value="Phylum">Phylum</option>
								<option value="Class">Class</option>
								<option value="Order">Order</option>
								<option value="Family">Family</option>
								<option value="Genus">Genus</option>
								<option selected value="Species">Species</option>
								</select></td>
						</tr>
						<tr>
							<td>Description:</td><td><textarea value="" name="description"></textarea></td>
						</tr>
						<tr>
							<td>Attribution:</td><td><input type="text" value="name &amp; date - default" name="attribn" /></td>
						</tr>
						<tr>
							<td>Choose the Licence: </td><td colspan = '3'><select name="licence"><option value="CC-BY &ndash; Creative Commons Attribution 3.0 Australia">CC-BY &ndash; Creative Commons Attribution 3.0 Australia</option><option value="CC-BY-NC &ndash; Creative Commons Attribution-NonCommercial 3.0 Australia">CC-BY-NC &ndash; Creative Commons Attribution-NonCommercial 3.0 Australia</option><option value="CC-BY-SA &ndash; Creative Commons Attribution-ShareAlike 3.0 Australia">CC-BY-SA &ndash; Creative Commons Attribution-ShareAlike 3.0 Australia</option><option value="CC-BY-NC-SA &ndash; Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Australia">CC-BY-NC-SA &ndash; Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Australia</option></select></td>
						</tr>																	
					</tbody>
				</table>
				<hr/>                
                	
                <table>	
					<tr><td>File 1:</td><td><input type="file" name="file1" /></td></tr>
					<tr><td>File 2:</td><td><input type="file" name="file2" /></td></tr>
					<tr><td>File 3:</td><td><input type="file" name="file3" /></td></tr>
					<tr><td>File 4:</td><td><input type="file" name="file4" /></td></tr>
					<tr><td>File 5:</td><td><input type="file" name="file5" /></td></tr>
					<tr><td>File 6:</td><td><input type="file" name="file6" /></td></tr>
					<tr><td>File 7:</td><td><input type="file" name="file7" /></td></tr>
					<tr><td>File 8:</td><td><input type="file" name="file8" /></td></tr>
					<tr><td>File 9:</td><td><input type="file" name="file9" /></td></tr>
					<tr><td>File 10:</td><td><input type="file" name="file10" /></td></tr>
				</table>				
			</fieldset>
			<input type="submit" name="Submit" value="Upload Files" onlick="clearForms"/>
		</FORM>
<% } else {%>	
    <div class="section">
        <h2>Permission Denied</h2>
        <p>You need to have the appropriate role (ROLE_ADMIN) to access this page.</p>
    </div>
<% } %>
	</BODY>
</HTML>
