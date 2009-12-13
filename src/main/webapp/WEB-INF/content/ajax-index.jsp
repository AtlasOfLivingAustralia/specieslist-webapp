<%--
    Document   : datastream-error
    Created on : 25/08/2009, 14:08:08 AM
    Author     : oak021
--%>
<%@ page contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@ taglib prefix="fn" uri="/WEB-INF/tld/fn.tld" %>
<json:object prettyPrint="true">
    <json:property name="pageSize" value="${results}"/>
    <%--<json:property name="recordsReturned" value="${results}"/>--%>
    <json:property name="startIndex" value="${startIndex}"/>
    <json:property name="sort" value="${sort}"/>
    <json:property name="dir" value="${dir}"/>
    <json:property name="totalRecords" value="${solrResults.NResults}"/>
    <json:property name="status" value="${status}"/>
    <json:property name="msg" value="${actionErrors}"/>
    <json:array name="records" var="result" items="${solrResults.searchResults}">
        <json:object>
            <json:property name="pid" value="${result.pid}"/>
            <json:property name="guid" value="${result.guid}"/>
            <json:property name="title" value="${result.title}"/>
            <json:property name="contentModel" value="${result.contentModel}"/>
            <json:property name="rank"><s:set name="rankStr">${result.rank}</s:set><s:text name="rank.%{rankStr}"/></json:property>
            <json:property name="rankId" value="${fn:replace(result.rankId, 'rank.', '')}"/>
            <json:property name="urlMapper" value="${result.urlMapper}"/>
            <json:property name="contentModelInitial" value="${result.contentModelInitial}"/>
            <json:property name="contentModelLabel" escapeXml="false">
                <s:set name="type">${result.contentModelInitial}</s:set>
                <s:text name="fedora.type.%{type}"/>
            </json:property>
        </json:object>
    </json:array>
</json:object>