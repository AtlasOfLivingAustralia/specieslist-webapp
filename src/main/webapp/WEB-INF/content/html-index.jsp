<%-- 
    Document   : html-index
    Created on : 21/08/2009, 11:15:08 AM
    Author     : oak021
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
    <head>
        <meta name="pageName" content="html" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <!-- Combo-handled YUI CSS files: -->
        <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?2.8.0r4/build/paginator/assets/skins/sam/paginator.css&2.8.0r4/build/datatable/assets/skins/sam/datatable.css">
        <!-- Combo-handled YUI JS files: -->
        <script type="text/javascript" src="http://yui.yahooapis.com/combo?2.8.0r4/build/yahoo-dom-event/yahoo-dom-event.js&2.8.0r4/build/connection/connection-min.js&2.8.0r4/build/element/element-min.js&2.8.0r4/build/paginator/paginator-min.js&2.8.0r4/build/datasource/datasource-min.js&2.8.0r4/build/datatable/datatable-min.js&2.8.0r4/build/json/json-min.js"></script>
        <title>HTML Index</title>
    </head>
    <body>
            <h1>HTML Index</h1>
            <s:if test="%{searchResults.isEmpty()}"><h2>There are no HTML pages in the repository</h2></s:if>
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
                <div class="yui-skin-sam">
                    <div id="results"></div>
                    <jsp:include page="yui-datatable.jsp"/>
                    <script type="text/javascript">
                        propertyName = "rdf.hasModel"; // "rdf.hasModel"
                        query = "ala.HtmlPageContentModel"; // "ala.TaxonNameContentModel"
                        loadDatatable(propertyName, query, "");
                    </script>
                </div>
            </s:else>
    </body>
</html>
