<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
        <title>Upload File to BIE</title>
    </head>
    <body>
        <div id="header">
            <div id="breadcrumb">
                <a href="${initParam.centralServer}">Home</a>
                <a href="${pageContext.request.contextPath}/">Admin</a>
                <span class="current">Photo Upload</span>
            </div>
        </div><!--close header-->
        <div class="section">
            <h2>Upload File to BIE</h2>
            <fieldset>
                <legend>Upload File</legend>		
                <c:choose>
                    <c:when test="${not empty fileName}">
                        FileName : "<strong> ${fileName} </strong>"&nbsp;
                        <strong><font color="blue">Uploaded successful.</font></strong> 
                    </c:when>
                    <c:when test="${not empty error}">
                        <strong><font color="red">Error: ${error}</font></strong> 
                    </c:when>
                    <c:otherwise>
                        <strong><font color="red">Uploaded Unsuccessful.</font></strong> 
                    </c:otherwise>
                </c:choose>
            </fieldset>
            <br/>
            <a href="">Try again</a>
        </div>
    </body>		
</html>