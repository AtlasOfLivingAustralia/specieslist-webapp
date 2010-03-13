<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta name="pageName" content="species"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Dataset List</title>
</head>
<body>
    <h1>Dataset List</h1>
    <c:if test="${not empty infoSourcesSet1}">
        <h3>Taxonomic datasets</h3>
        <table class="datasets">
            <tr>
                <th>Dataset Name</th>
                <th>Number of documents</th>
            </tr>
            <c:forEach var="dataset" items="${infoSourcesSet1}">
                <tr>
                    <td><a href="${dataset.websiteUrl}" target="_blank">${dataset.name}</a></td>
                    <td>###</td>
                </tr>
            </c:forEach>
            
        </table>
    </c:if>
    <c:if test="${not empty infoSourcesSet3}">
        <h3>Image Libraries</h3>
        <table class="datasets">
            <tr>
                <th>Dataset Name</th>
                <th>Number of documents</th>
            </tr>
            <c:forEach var="dataset" items="${infoSourcesSet3}">
                <tr>
                    <td><a href="${dataset.websiteUrl}" target="_blank">${dataset.name}</a></td>
                    <td>###</td>
                </tr>
            </c:forEach>

        </table>
    </c:if>
    <c:if test="${not empty infoSourcesSet2}">
        <h3>External Websites</h3>
        <table class="datasets">
            <tr>
                <th>Dataset Name</th>
                <th>Number of documents</th>
            </tr>
            <c:forEach var="dataset" items="${infoSourcesSet2}">
                <tr>
                    <td><a href="${dataset.websiteUrl}" target="_blank">${dataset.name}</a></td>
                    <td>###</td>
                </tr>
            </c:forEach>

        </table>
    </c:if>
</body>
</html>
