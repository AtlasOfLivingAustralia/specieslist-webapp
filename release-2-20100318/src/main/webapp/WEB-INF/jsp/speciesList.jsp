<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta name="pageName" content="species"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <!-- Combo-handled YUI CSS files: -->
    <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?2.8.0r4/build/fonts/fonts-min.css&2.8.0r4/build/base/base-min.css&2.8.0r4/build/paginator/assets/skins/sam/paginator.css&2.8.0r4/build/datatable/assets/skins/sam/datatable.css">
    <!-- Combo-handled YUI JS files: -->
    <script type="text/javascript" src="http://yui.yahooapis.com/combo?2.8.0r4/build/yahoo/yahoo-debug.js&2.8.0r4/build/event/event-debug.js&2.8.0r4/build/connection/connection-debug.js&2.8.0r4/build/datasource/datasource-debug.js&2.8.0r4/build/dom/dom-debug.js&2.8.0r4/build/element/element-debug.js&2.8.0r4/build/paginator/paginator-debug.js&2.8.0r4/build/datatable/datatable-debug.js&2.8.0r4/build/history/history-debug.js&2.8.0r4/build/json/json-debug.js&2.8.0r4/build/logger/logger-debug.js"></script>
    <script type="text/javascript">
        $(document).ready(function() {
            var facetLinksSize = $("ul#subnavlist li").size();
            if (facetLinksSize == 0) {
                // Hide an empty facet link list
                $("#facetBar > h4").hide();
                $("#facetBar #navlist").hide();
            }
        });
    </script>
    <title>Species Search</title>
</head>
<body>
    <h1>${title}</h1>
    <c:if test="${empty searchResults.taxonConcepts}"><p>Search for <span style="font-weight: bold"><c:out value="${query}"/></span> did not match any documents</p></c:if>
    <c:if test="${not empty searchResults.taxonConcepts}">
            <div id="facetBar">
                <h4>Refine your results:</h4>
                <ul id="navlist">
                    <c:if test="${not empty query}"><c:set var="queryParam">q=<c:out value="${query}" escapeXml="true"/></c:set></c:if>
                    <c:forEach var="facetResult" items="${searchResults.facetResults}">
                        <c:if test="${!fn:containsIgnoreCase(facetQuery, facetResult.fieldResult[0].label)}">
                            <li><span class="FieldName"><fmt:message key="facet.${facetResult.fieldName}"/></span></li>
                            <ul id="subnavlist">
                                <c:forEach var="fieldResult" items="${facetResult.fieldResult}">
                                    <%-- test to see if the current facet search is also a listed facet link --%>
                                    <%--<c:if test="${!fn:containsIgnoreCase(facetQuery, fieldResult.label)}">--%>
                                        <li id="subactive">
                                            <a href="?fq=${facetResult.fieldName}:${fieldResult.label}&${queryParam}">
                                                <fmt:message key="${facetResult.fieldName}.${fieldResult.label}"/> (${fieldResult.count})</a>
                                        </li>
                                    <%--</c:if>--%>
                                </c:forEach>
                            </ul>
                        </c:if>
                    </c:forEach>
                </ul>
            <br/>
            <c:if test="${not empty facetQuery}">
                <div id="removeFacet">
                    <h4>Displaying subset of results, restricted to: <span id="facetName">
                            <fmt:message key="rank.${fn:substringAfter(facetQuery, 'rank:')}"/></span></h4>
                    <p>&bull; <a href="?<c:out value="${queryParam}"/>">Return to full result list</a></p>
                </div>
            </c:if>
            </div>
        <div id="content" class="yui-skin-sam">
            <div class="datatable-summary">Search for <a href="?q=<c:out value='${query}'/>"><c:out value="${query}"/></a> 
                returned <c:out value="${searchResults.totalRecords}"/> record<c:if test="${searchResults.totalRecords > 1}">s</c:if></div>
                <iframe id="yui-history-iframe" src="<c:url value='/static/css/blank.html'/>"></iframe>
            <input id="yui-history-field" type="hidden">
            <div id="dt-pag-head"></div>
            <div id="results"></div>
            <div id="dt-pag-nav"></div>
            <jsp:include page="yui-datatable.jsp"/>
            <script type="text/javascript">
                <%--var searchQuery = "<s:property value="propertyValue" escapeJavaScript="true" escape="false"/>";//escape("${propertyValue}");//$("#_propertyValue").val();--%>
                var searchQuery = '${queryJsEscaped}';
                
                if (searchQuery) {
                    query = escape(searchQuery); // escape(this.utf8_encode(searchQuery));
                } else {
                    query = "${solrQuery}";
                }

                facetQuery = "${facetQuery}";
                loadDatatable(query, facetQuery);
            </script>
        </div>
    </c:if>
</body>
</html>
