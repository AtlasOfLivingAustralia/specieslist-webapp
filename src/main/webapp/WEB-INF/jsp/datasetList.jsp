<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta name="pageName" content="datasets"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Dataset List</title>
</head>
<body>
    <h1>Dataset List</h1>
    <c:if test="${not empty infoSources}">
        <h3>&bull; Taxonomic datasets</h3>
        <table class="datasets">
            <tr>
                <th>Dataset Name</th>
                <th>Species</th>
                <th>Taxa indexed</th>
            </tr>
            <c:forEach var="dataset" items="${infoSources}">
                <c:if test="${fn:contains(dataset.datasetType, '1')}">
                    <tr>
                        <td><a href="${dataset.websiteUrl}" target="_blank">${dataset.name}</a></td>
                        <td><a href="${pageContext.request.contextPath}/species/search?q=dataset:${dataset.id}&title=${dataset.name}">view list</a></td>
                        <td><c:if test="${dataset.documentCount == 0}">N/A</c:if><c:if test="${dataset.documentCount != 0}">${dataset.documentCount}</c:if></td>
                    </tr>
                </c:if>
            </c:forEach>
        </table>
    <%--</c:if>
    <c:if test="${not empty infoSources3}">--%>
        <h3>&bull; Image Libraries</h3>
        <table class="datasets">
            <tr>
                <th>Dataset Name</th>
                <th>Species</th>
                <th>Images indexed</th>
            </tr>
            <c:forEach var="dataset" items="${infoSources}">
                <c:if test="${fn:contains(dataset.datasetType, '3')}">
                    <tr>
                        <td><a href="${dataset.websiteUrl}" target="_blank">${dataset.name}</a></td>
                        <td><a href="${pageContext.request.contextPath}/species/search?q=dataset:${dataset.id}&title=${dataset.name}">view list</a></td>
                        <td>${dataset.documentCount}</td>
                    </tr>
                </c:if>
            </c:forEach>
        </table>
    <%--</c:if>
    <c:if test="${not empty infoSources2}">--%>
        <h3>&bull; External Websites</h3>
        <table class="datasets">
            <tr>
                <th>Dataset Name</th>
                <th>Species</th>
                <th>Pages indexed</th>
            </tr>
            <c:forEach var="dataset" items="${infoSources}">
                <c:if test="${fn:contains(dataset.datasetType, '2')}">
                    <tr>
                        <td><a href="${dataset.websiteUrl}" target="_blank">${dataset.name}</a></td>
                        <td><a href="${pageContext.request.contextPath}/species/search?q=dataset:${dataset.id}&title=${dataset.name}">view list</a></td>
                        <td>${dataset.documentCount}</td>
                    </tr>
                </c:if>
            </c:forEach>
        </table>
    </c:if>
</body>
</html>
