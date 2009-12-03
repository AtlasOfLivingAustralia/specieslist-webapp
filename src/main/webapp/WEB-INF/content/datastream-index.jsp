<%-- 
    Document   : datastream-index
    Created on : 11/08/2009, 10:06:08 AM
    Author     : oak021
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
    <head>
        <meta name="pageName" content="datastream"/>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <!-- Combo-handled YUI CSS files: -->
        <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?2.8.0r4/build/fonts/fonts-min.css&2.8.0r4/build/base/base-min.css&2.8.0r4/build/paginator/assets/skins/sam/paginator.css&2.8.0r4/build/datatable/assets/skins/sam/datatable.css">
        <!-- Combo-handled YUI JS files: -->
        <script type="text/javascript" src="http://yui.yahooapis.com/combo?2.8.0r4/build/yahoo/yahoo-debug.js&2.8.0r4/build/event/event-debug.js&2.8.0r4/build/connection/connection-debug.js&2.8.0r4/build/datasource/datasource-debug.js&2.8.0r4/build/dom/dom-debug.js&2.8.0r4/build/element/element-debug.js&2.8.0r4/build/paginator/paginator-debug.js&2.8.0r4/build/datatable/datatable-debug.js&2.8.0r4/build/history/history-debug.js&2.8.0r4/build/json/json-debug.js&2.8.0r4/build/logger/logger-debug.js"></script>
        <script type="text/javascript">
            var contentModelMap = {}; // store the display name for document types (facets)
        </script>
        <style type="text/css">
            /* custom styles YUI datatable history component */
            #yui-history-iframe {
              position:absolute;
              top:0; left:0;
              width:1px; height:1px; /* avoid scrollbars */
              visibility:hidden;
            }
        </style>

        <title>ALA Repository Index</title>
    </head>
    <body>
        <h1>ALA Repository Index</h1>
        <div id="facetBar">
            <s:if test="%{!solrResults.facetResults.isEmpty()}">
                <h4>Refine your results:</h4>
                <ul id="navlist">
                    <s:iterator value="solrResults.facetResults">
                        <li><span class="FieldName"><s:text name="facet.%{fieldName}"/></span></li>
                        <ul id="subnavlist">
                            <s:iterator value="fieldResult">
                                <li id="subactive">
                                    <script type="text/javascript">
                                        // Add the document type's display names to a JS Map (used for catching bookmarked state)
                                        contentModelMap['<s:property value="label"/>'] = '<s:text name="fedora.%{label}"/>';
                                    </script>
                                    <a href="${pageContext.request.contextPath}/datastream/fq=<s:property value="fieldName"/>:<s:property value="label"/>">
                                        <s:text name="fedora.%{label}"/> (<s:property value="count"/>)</a>
                                </li>
                            </s:iterator>
                        </ul>
                    </s:iterator>
                </ul>
            </s:if>
        <br/>
        <s:if test="%{id.startsWith('fq')}">
            <div id="removeFacet">
                <h4>Displaying subset of results, restricted to: <span id="facetName"><s:text name='fedora.%{fieldConstraint.replace(".","_")}'/></span></h4>
                <p>&bull; <a href="../datastream">Return to full result list</a></p>
            </div>
        </s:if>
        </div>
        <div id="content" class="yui-skin-sam">
            <s:if test="%{solrResults.searchResults.isEmpty()}">
                <h2>${responseMessage}</h2>
            </s:if>
            <s:else>
                <div class="datatable-summary">Total records: <s:property value="solrResults.nResults"/></div>
                <iframe id="yui-history-iframe" src='<s:url value="/css/blank.html"/>'></iframe>
                <input id="yui-history-field" type="hidden">
                <div id="dt-pag-head"></div>
                <div id="results"></div>
                <div id="dt-pag-nav"></div>
                <jsp:include page="yui-datatable.jsp"/>
                <script type="text/javascript">
                    propertyName = "PID";
                    query = "ala*";
                    var fq = "${id}";
                    var ids = fq.split("=");
                    facetQuery = ids[1];
                    loadDatatable(propertyName, query, facetQuery);
                </script>
            </s:else>
        </div>

    </body>
</html>
