<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta name="pageName" content="datasets"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/screen.css" type="text/css" media="screen" charset="utf-8"/>
    <title>Contributors</title>
</head>
<body>
<div id="decoratorBody">
    <h1>Contributors list</h1>
    <c:if test="${not empty occurrenceInfoSources}">
        <h3>&bull; Specimen and Observation providers</h3>
        <table class="datasets">
            <tr>
                <th>Provider Name</th>
                <th>Records</th>
            </tr>
            <c:forEach var="dataset" items="${occurrenceInfoSources}">
                    <tr>
                        <td><a  href="${dataset.websiteUrl}" target="_blank">${dataset.name}</a></td>
                        <td><c:if test="${dataset.documentCount == 0}">N/A</c:if><c:if test="${dataset.documentCount != 0}">${dataset.documentCount}</c:if></td>
                    </tr>
            </c:forEach>
        </table>
    </c:if>    
    <c:if test="${not empty infoSources}">
        <h3>&bull; Taxonomic datasets</h3>
        <table class="datasets">
            <tr>
                <th>Dataset Name</th>
                <th>Species & higher taxa</th>
                <th>Taxa indexed</th>
            </tr>
            <c:forEach var="dataset" items="${infoSources}">
                <c:set var="datasetId"><c:out value="${dataset.id}"/></c:set>
                <c:if test="${fn:contains(dataset.datasetType, '1')}">
                    <tr>
                        <td><a class="external" href="${dataset.websiteUrl}" target="_blank">${dataset.name}</a></td>
                        <td><a href="${pageContext.request.contextPath}/species/search?q=dataset:${dataset.id}&title=${dataset.name}">${not empty countsMap[datasetId] ? countsMap[datasetId] : '0'}</a></td>
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
                <c:set var="datasetId"><c:out value="${dataset.id}"/></c:set>
                <c:if test="${fn:contains(dataset.datasetType, '3')}">
                    <tr>
                        <td><a class="external" href="${dataset.websiteUrl}" target="_blank">${dataset.name}</a> 
                        	<c:if test="${fn:contains(infoSourceIDWithVocabulariesMapList, dataset.id)}">
                        		<span class="termMappingLink">(see <a href="${pageContext.request.contextPath}/species/vocabularies/${dataset.id}">term mapping</a>)</span>
                        	</c:if>
                        </td>
                        <td><a href="${pageContext.request.contextPath}/species/search?q=dataset:${dataset.id}&title=${dataset.name}">${not empty countsMap[datasetId] ? countsMap[datasetId] : '0'}</a></td>
                        <td><c:if test="${dataset.documentCount == 0}">N/A</c:if><c:if test="${dataset.documentCount != 0}">${dataset.documentCount}</c:if></td>
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
                <th>Species & higher taxa</th>
                <th>Pages indexed</th>
            </tr>
            <c:forEach var="dataset" items="${infoSources}">
                <c:set var="datasetId"><c:out value="${dataset.id}"/></c:set>
                <c:if test="${fn:contains(dataset.datasetType, '2')}">
                    <tr>
                        <td><a class="external" href="${dataset.websiteUrl}" target="_blank">${dataset.name}</a> 
                        	<c:if test="${fn:contains(infoSourceIDWithVocabulariesMapList, dataset.id)}">
                        		<span class="termMappingLink">(see <a href="${pageContext.request.contextPath}/species/vocabularies/${dataset.id}">term mapping</a>)</span>
                        	</c:if>
                        </td>
                        <td><a href="${pageContext.request.contextPath}/species/search?q=dataset:${dataset.id}&title=${dataset.name}">${not empty countsMap[datasetId] ? countsMap[datasetId] : '0'}</a></td>
                        <td><c:if test="${dataset.documentCount == 0}">N/A</c:if><c:if test="${dataset.documentCount != 0}">${dataset.documentCount}</c:if></td>
                    </tr>
                </c:if>
            </c:forEach>
        </table>
    </c:if>
</div>
</body>
</html>
