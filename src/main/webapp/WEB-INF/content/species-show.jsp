<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>

<html>
    <head>
        <meta name="pageName" content="species" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>ALA Repository Taxon Concept: ${tcTitle}</title>
        <!-- Combo-handled YUI CSS files: -->
        <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?2.8.0r4/build/paginator/assets/skins/sam/paginator.css&2.8.0r4/build/datatable/assets/skins/sam/datatable.css">
        <!-- Combo-handled YUI JS files: -->
        <script type="text/javascript" src="http://yui.yahooapis.com/combo?2.8.0r4/build/yahoo-dom-event/yahoo-dom-event.js&2.8.0r4/build/connection/connection-min.js&2.8.0r4/build/element/element-min.js&2.8.0r4/build/paginator/paginator-min.js&2.8.0r4/build/datasource/datasource-min.js&2.8.0r4/build/datatable/datatable-min.js&2.8.0r4/build/json/json-min.js"></script>
    </head>
    <body>
        <s:if test="%{id.startsWith('search')}">
            <div id="searchBox">
                <h3>Search for Species and Taxa</h3>
                <s:form action="/species/" namespace="search" method="GET" theme="simple">
                    <s:textfield key="propertyValue" size="30"/>
                    <s:submit value="Search"/>
                </s:form>
                <p>
                <p>Examples: <a href='${pageContext.request.contextPath}/species/?propertyValue="Pogona+barbata"'>&quot;Pogona barbata&quot;</a></p>
            </div>
        </s:if>
        <s:else>
            <h3>Species Profile</h3>
            <div id="speciesTitle">
                <h2>${tcTitle}</h2>
                <div id="lsid">${title}</div>
            </div>

            <s:if test="%{!taxonNames.isEmpty()}">
                <h4>Names</h4>
                <table class="propertyTable">
                    <!-- Table headings. -->
                    <tr>
                        <th>Title</th>
                        <th>Scientific&nbsp;Name</th>
                        <th>Rank</th>
                        <th>Source</th>
                    </tr>
                    <!-- Dynamic table content. -->
                    <s:iterator value="taxonNames">
                        <tr>
                            <td><a href="${source}" target="_blank">${title}</a></td>
                            <td>${nameComplete}</td>
                            <td>${rank}</td>
                            <td>${source}</td>
                        </tr>
                    </s:iterator>
                </table>
            </s:if>

            <s:if test="%{!images.isEmpty()}">
                <h4>Images</h4>
                <table class ="propertyTable">
                    <!-- Table headings. -->
                    <tr>
                        <th>Title</th>
                        <th>Desciption</th>
                        <!-- <th>Scientific&nbsp;Name</th> -->
                        <th>Thumbnail</th>
                    </tr>
                    <!-- Dynamic table content. -->
                    <s:iterator value="images">
                        <tr>
                            <td><a href="../image/${pid}">${title}</a></td>
                            <td>${description}</td>
                            <!-- <td>${scientificName}</td> -->
                            <td><a href="${photoPage}" target="_blank"><img src="${photoSourceUrl}" height="55"/></a></td>
                        </tr>
                    </s:iterator>
                </table>
            </s:if>

            <s:if test="%{!htmlPages.isEmpty()}">
                <h4>HTML Pages</h4>
                <table class ="propertyTable">
                    <!-- Table headings. -->
                    <tr>
                        <th>Title</th>
                        <th>Properties</th>
                        <th>Source</th>
                    </tr>
                    <!-- Dynamic table content. -->
                    <s:iterator value="htmlPages">
                        <tr>
                            <td><a href="${url}">${title}</a></td>
                            <td>
                                <ul><s:iterator value="rdfProperties" var="prop">
                                        <li><b>${prop.key}</b>: ${prop.value}<br/></li>
                                    </s:iterator></ul>
                            </td>
                            <td>${source}</td>
                        </tr>
                    </s:iterator>
                </table>
            </s:if>

            <%--<div class="yui-skin-sam">
                <h4>Search results for <i>${taxonNames[0].nameComplete}</i></h4>
                <div id="results"></div>
                <jsp:include page="yui-datatable.jsp"/>
                <script type="text/javascript">
                    propertyName = "rdf.hasScientificName";
                    query = "\"${taxonNames[0].nameComplete}\"";// "${images[0].scientificName}";
                    loadDatatable(propertyName, query, "");

              // Catch bookmarked state and trigger facet search
              var anchor = window.location.hash;
              if (anchor.length > 0) {
                  anchor = anchor.replace(/#/,""); // remove the hash character
                  var args = anchor.split(":");
                  var fieldname = args[0];
                  var label = args[1];
                  var displayLabel = (contentModelMap[label]) ? contentModelMap[label] : label;
                  doFacetSearch(fieldname, label, displayLabel);
              }
          </script>
      </div>--%>

            <h4>Properties:</h4>
            <table class ="propertyTable">
                <!-- Table headings. -->
                <tr>
                    <th>Property</th>
                    <th>Value</th>
                    <th>Harvested</th>
                    <th>Source</th>
                </tr>

                <!-- Dynamic table content. -->
                <s:iterator value="objProperties">
                    <tr>
                        <td><s:property value="relationship" /></td>
                        <td>${value}</td>
                        <td>
                            <s:property value="harvested" />
                        </td>
                        <td>
                            <a href="/fedora/get/${pid}/<s:property value="sourceDSID"/>"><s:property value="dataSource" /></a>
                        </td>
                    </tr>
                </s:iterator>
            </table>
            <br />
        </s:else>
    </body>
</html>
