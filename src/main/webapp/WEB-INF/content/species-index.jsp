<%-- 
    Document   : taxa-index
    Created on : 15/06/2009, 10:06:08 AM
    Author     : hwa002
--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
    <head>
        <meta name="pageName" content="species"/>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <!-- Combo-handled YUI CSS files: -->
        <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?2.8.0r4/build/fonts/fonts-min.css&2.8.0r4/build/base/base-min.css&2.8.0r4/build/paginator/assets/skins/sam/paginator.css&2.8.0r4/build/datatable/assets/skins/sam/datatable.css">
        <!-- Combo-handled YUI JS files: -->
        <script type="text/javascript" src="http://yui.yahooapis.com/combo?2.8.0r4/build/yahoo/yahoo-debug.js&2.8.0r4/build/event/event-debug.js&2.8.0r4/build/connection/connection-debug.js&2.8.0r4/build/datasource/datasource-debug.js&2.8.0r4/build/dom/dom-debug.js&2.8.0r4/build/element/element-debug.js&2.8.0r4/build/paginator/paginator-debug.js&2.8.0r4/build/datatable/datatable-debug.js&2.8.0r4/build/history/history-debug.js&2.8.0r4/build/json/json-debug.js&2.8.0r4/build/logger/logger-debug.js"></script>
        <script type="text/javascript">
            var contentModelMap = {}; 
        </script>
        <title>Taxon Concept Index</title>
    </head>
    <body>
        <h1>Species Result List</h1>
    <s:if test="%{solrResults.searchResults.isEmpty()}"><p>Search for "${propertyValue}" returned no results</p></s:if>
    <s:elseif test="%{false}">
        <!-- Dynamic table content. -->
        <table class ="propertyTable">
            <tr>
                <th>Type</th>
                <th>Title</th>

            </tr>

            <s:iterator value="searchResults">
                <tr>
                    <td>
                <s:property value="contentModelInitial"/>
                </td>
                <td>
                    <a href="./<s:property value="urlMapper"/>/<s:property value="pid"/>"><s:property value="title"/></a>
                </td>
                </tr>
            </s:iterator>
        </table>
    </s:elseif>
    <s:else>
        <div id="facetBar">
            <s:if test="%{!solrResults.facetResults.isEmpty()}">
                <h4>Refine your results:</h4>
                <ul id="navlist">
                    <s:if test="%{!propertyValue.isEmpty()}"><s:set name="queryParam">?propertyValue=<s:property value="propertyValue" escape="false"/></s:set></s:if>
                    <s:iterator value="solrResults.facetResults">
                        <li><span class="FieldName"><s:text name="facet.%{fieldName}"/></span></li>
                        <ul id="subnavlist">
                            <s:iterator value="fieldResult">
                                <li id="subactive">
                                    <script type="text/javascript">
                                        // Add the document type's display names to a JS Map (used for catching bookmarked state)
                                        contentModelMap['<s:property value="label"/>'] = '<s:text name="fedora.%{label}"/>';
                                    </script>
                                    <a href="${pageContext.request.contextPath}/species/fq=<s:property value="fieldName"/>:<s:property value="label"/><s:property value="queryParam"/>">
                                        <s:text name="rank.%{label}"/> (<s:property value="count"/>)</a>
                                </li>
                            </s:iterator>
                        </ul>
                    </s:iterator>
                </ul>
            </s:if>
        <br/>
        <s:if test="%{id.startsWith('fq')}">
            <div id="removeFacet">
                <h4>Displaying subset of results, restricted to: <span id="facetName"><s:text name='rank.%{fieldConstraint.replace(".","_")}'/></span></h4>
                <p>&bull; <a href="../species/<s:property value="queryParam"/>">Return to full result list</a></p>
            </div>
        </s:if>
        </div>
        <div id="content" class="yui-skin-sam">
            <div class="datatable-summary">Total records: <s:property value="solrResults.nResults"/></div>
            <iframe id="yui-history-iframe" src='<s:url value="/css/blank.html"/>'></iframe>
            <input id="yui-history-field" type="hidden">
            <div id="dt-pag-head"></div>
            <div id="results"></div>
            <div id="dt-pag-nav"></div>
            <jsp:include page="yui-datatable.jsp"/>
            <script type="text/javascript">
                var searchQuery = "<s:property value="propertyValue" escapeJavaScript="true" escape="false"/>";//escape("${propertyValue}");//$("#_propertyValue").val();
                
                if (searchQuery) {
                    propertyName = "all_text";
                    query = escape(searchQuery); // escape(this.utf8_encode(searchQuery));
                } else {
                    propertyName = "${propertyName}";
                    query = "${solrQuery}";
                }

                var fq = "${id}";
                var ids = fq.split("=");
                facetQuery = ids[1];
                loadDatatable(propertyName, query, facetQuery);
            </script>
        </div>
    </s:else>
</body>
</html>
