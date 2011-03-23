<%@ include file="/common/taglibs.jsp"%><%
/*
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
%><%@ page contentType="application/json; charset=UTF-8" %>
<c:if test="${not empty param['callback']}">${param['callback']}(</c:if>
<json:object prettyPrint="false">
    <json:array name="autoCompleteList" var="item" items="${autoCompleteList}">
        <json:object>
            <json:property name="guid" value="${item.guid}"/>
            <json:property name="name" value="${item.name}"/>
            <json:property name="occurrenceCount" value="${item.occurrenceCount}"/>
            <json:property name="georeferencedCount" value="${item.georeferencedCount}"/>
            <json:array name="scientificNameMatches" var="match" items="${item.scientificNameMatches}">
                <json:property value="${match}" escapeXml="false"/>
            </json:array>
            <json:array name="commonNameMatches" var="match" items="${item.commonNameMatches}">
                <json:property value="${match}" escapeXml="false"/>
            </json:array>
            <json:property name="commonName" value="${item.commonName}"/>
            <json:array name="matchedNames" var="match" items="${item.matchedNames}">
                <json:property value="${match}" escapeXml="false"/>
            </json:array>
            <json:property name="rankId" value="${item.rankId}"/>
            <json:property name="rankString" value="${item.rankString}"/>
            <json:property name="left" value="${item.left}"/>
            <json:property name="right" value="${item.right}"/>
        </json:object>
    </json:array>
</json:object>
<c:if test="${not empty param['callback']}">)</c:if>