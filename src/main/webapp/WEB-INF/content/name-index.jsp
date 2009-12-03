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
        <meta name="pageName" content="name" />
        <!-- <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"> -->
        <!-- Combo-handled YUI CSS files: -->
        <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?2.8.0r4/build/paginator/assets/skins/sam/paginator.css&2.8.0r4/build/datatable/assets/skins/sam/datatable.css">
        <!-- Combo-handled YUI JS files: -->
        <script type="text/javascript" src="http://yui.yahooapis.com/combo?2.8.0r4/build/yahoo-dom-event/yahoo-dom-event.js&2.8.0r4/build/connection/connection-min.js&2.8.0r4/build/element/element-min.js&2.8.0r4/build/paginator/paginator-min.js&2.8.0r4/build/datasource/datasource-min.js&2.8.0r4/build/datatable/datatable-min.js&2.8.0r4/build/json/json-min.js"></script>
        <title>Taxon Name Index</title>
    </head>
    <body>
            <h1>Taxon Name Index</h1>
            <s:if test="%{searchResults.isEmpty()}"><h2>There are no Taxon Name objects in the repository</h2></s:if>
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
                        query = "ala.TaxonNameContentModel"; // "ala.TaxonNameContentModel"
                        loadDatatable(propertyName, query, "");
                    </script>
                </div>
            </s:else>
    </body>
</html>
