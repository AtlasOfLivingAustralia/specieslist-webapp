<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>
<%@ page session="false" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<html>
    <head>
        <META http-equiv="Content-Type" content="text/html;charset=UTF-8">
        <title>Upload File to BIE</title>
        <script>
            function clearForms() {
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
    </head>
    <body onunload="clearForms()" onload="clearForms()" bgcolor="white">
        <div id="header">
            <div id="breadcrumb">
                <a href="${initParam.centralServer}">Home</a>
                <a href="${pageContext.request.contextPath}/">Admin</a>
                <span class="current">Edit Photo Upload Field</span>
            </div>
        </div><!--close header-->
        <div class="section">
            <form:form modelAttribute="uploadItem" method="post" enctype="multipart/form-data">
                <fieldset>
                    <legend><h2>Edit Photo Upload Field</h2></legend>
                    <table>
                        <tbody>
							<tr>
                                <td>DocumentId: </td>
                                <td><form:input path="documentId" readonly="true"/></td>
                                <td>guid:</td>
                                <td><form:input path="guid" readonly="true"/></td>
                            </tr>                        
                            <tr>
                                <td>Scientific Name: </td>
                                <td><form:input path="scientificName" readonly="false"/> <form:errors path="scientificName" cssClass="error"/></td>
                                <td>User Name: </td>
                                <td><form:input path="userName" disabled="disabled"/> <form:errors path="userName" cssClass="error"/></td>
                            </tr>
                            <tr>
                                <td>Common Name: </td>
                                <td><form:input path="commonName"/> <form:errors path="commonName" cssClass="error"/></td>
                                <td>User Email: </td>
                                <td><form:input path="email"/>  <form:errors path="email" cssClass="error"/></td>
                            </tr>
                            <tr>
                                <td>Title: </td>
                                <td><form:input path="title"/>  <form:errors path="title" cssClass="error"/></td>
                                <td>Rank: </td>
                                <td><form:input path="rank" value="NOT USED" readonly="true"/></td>                                
                            </tr>
                            <tr>
                                <td>Description:</td>
                                <td><form:textarea path="description" /> <form:errors path="description" cssClass="error"/></td>
                                <td>BlackList:</td>
                                <td><form:checkbox path="blacklist" /> <form:errors path="blacklist" cssClass="error"/></td>
                            </tr>
                            <tr>
                                <td>Attribution:</td>
                                <td><form:input path="attribn" /> <form:errors path="attribn" cssClass="error"/></td>
                            </tr>
                            <tr>
                                <td>Choose the Licence: </td>
                                <td colspan='3'>
                                    <form:select path="licence">
                                        <form:option value="CC-BY &ndash; Creative Commons Attribution 3.0 Australia">CC-BY &ndash; Creative Commons Attribution 3.0 Australia</form:option>
                                        <form:option value="CC-BY-NC &ndash; Creative Commons Attribution-NonCommercial 3.0 Australia">CC-BY-NC &ndash; Creative Commons Attribution-NonCommercial 3.0 Australia</form:option>
                                        <form:option value="CC-BY-SA &ndash; Creative Commons Attribution-ShareAlike 3.0 Australia">CC-BY-SA &ndash; Creative Commons Attribution-ShareAlike 3.0 Australia</form:option>
                                        <form:option value="CC-BY-NC-SA &ndash; Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Australia">CC-BY-NC-SA &ndash; Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Australia</form:option>
                                        <form:option value="Copyright">Copyright</form:option>
                                    </form:select>  <form:errors path="licence" cssClass="error"/>
                                </td>
                            </tr>
                            <tr>
                                <td>File:</td>
                                <td colspan="3">
                                    <form:input path="fileData" type="file" /> <form:errors path="fileData" cssClass="error"/>
                                </td>
                            </tr>
                           
                        </tbody>
                    </table>
                </fieldset>
                <input type="submit" />
            </form:form>
        </div>
    </body>
</html>