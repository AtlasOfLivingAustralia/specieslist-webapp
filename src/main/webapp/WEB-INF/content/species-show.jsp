<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>

<html>
    <head>
        <meta name="pageName" content="species" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>ALA Biodiversity Information Explorer: ${tcTitle}</title>
        <!-- Combo-handled YUI CSS files: -->
        <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?2.8.0r4/build/paginator/assets/skins/sam/paginator.css&2.8.0r4/build/datatable/assets/skins/sam/datatable.css">
        <!-- Combo-handled YUI JS files: -->
        <script type="text/javascript" src="http://yui.yahooapis.com/combo?2.8.0r4/build/yahoo-dom-event/yahoo-dom-event.js&2.8.0r4/build/connection/connection-min.js&2.8.0r4/build/element/element-min.js&2.8.0r4/build/paginator/paginator-min.js&2.8.0r4/build/datasource/datasource-min.js&2.8.0r4/build/datatable/datatable-min.js&2.8.0r4/build/json/json-min.js"></script>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/fancybox/jquery.fancybox-1.2.6.css" media="screen" />

	<script type="text/javascript" src="${pageContext.request.contextPath}/fancybox/jquery.fancybox-1.2.6.pack.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			$("a.popup").fancybox({
                            'frameWidth' : 800,
                            'frameHeight' : 500,
                            'hideOnContentClick' : false
                        });

			$("a.image").fancybox({
				'imageScale' : true,
                                'hideOnContentClick' : false
			});
		});
	</script>

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
            <%--<h3>Taxon Profile</h3>--%>
            <div id="speciesHeader">
                <s:if test="%{!images.isEmpty()}">
                    <div id="speciesPhoto">
                        <img src="${images[0].photoSourceUrl}" width="250px" alt="species photo"/>
                    </div>
                </s:if>
                <div id="speciesTitle">
                    <h2>${tcTitle}</h2>
                    <div class="speciesInfo"><b>Scientific name: </b>
                        <s:if test="%{taxonNames.get(0).rank.contains('Species') || taxonNames.get(0).rank.contains('Genus')}"><i>${taxonNames[0].nameComplete}</i></s:if>
                        <s:else>${taxonNames[0].nameComplete}</s:else>
                    </div>
                    <div class="speciesInfo"><b>Taxon Rank: </b><s:property value="%{taxonNames.get(0).rank.replace('TaxonRank.', '')}" /></div>
                    <div class="speciesInfo"><b>GUID: </b><a href="${taxonNames[0].source}" target="_blank">${title}</a></div>
                </div>
                <ul style="float:left;">
                    <s:if test="%{!taxonNames.isEmpty()}"><li><a href="${pageContext.request.contextPath}/properties/${taxonNames[0].nameComplete}" class="popup">View
                        the complete set of harvested properties</a></li></s:if>
                    <s:if test="%{!images.isEmpty()}"><li><a href="#images">Images</a></li></s:if>
                    <s:if test="%{!htmlPages.isEmpty()}"><li><a href="#htmlpages">HTML Pages</a></li></s:if>
                    <li><a href="#properties">Properties</a></li>
                </ul>

            </div>
            <div style="clear: both;">&nbsp;</div>
            <s:if test="%{taxonNames.size() > 1}">
                <h4>Names<a name="names">&nbsp;</a></h4>
                <table class="propertyTable">
                    <!-- Table headings. -->
                    <tr>
                        <th>Title</th>
                        <th>Scientific&nbsp;Name</th>
                        <th>Taxon&nbsp;Rank</th>
                        <%--<th>Source</th>--%>
                    </tr>
                    <!-- Dynamic table content. -->
                    <s:iterator value="taxonNames">
                        <tr>
                            <td><a href="${source}" target="_blank">${title}</a></td>
                            <td>
                                <s:if test="%{rank.contains('Species') || rank.contains('Genus')}"><i>${nameComplete}</i></s:if>
                                <s:else>${nameComplete}</s:else>
                            </td>
                            <td><s:property value="%{rank.replace('TaxonRank.', '')}" /></td>
                            <%--<td>${source}</td>--%>
                        </tr>
                    </s:iterator>
                </table>
            </s:if>

            <s:if test="%{!images.isEmpty()}">
                <h4>Images<a name="images">&nbsp;</a></h4>
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
                            <td><a href="${photoPage}" target="_blank">${title}</a></td>
                            <td>${description}</td>
                            <!-- <td>${scientificName}</td> -->
                            <td><a href="${photoSourceUrl}" class="image" target="_blank" title="${title}"><img src="${photoSourceUrl}" height="55"/></a></td>
                        </tr>
                    </s:iterator>
                </table>
            </s:if>

            <s:if test="%{!htmlPages.isEmpty()}">
                <h4>HTML Pages<a name="htmlpages">&nbsp;</a></h4>
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
                            <td><a href="http://${source}" target="_blank"><s:text name="source.%{source}"/></a></td>
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

            <h4>Properties<a name="properties">&nbsp;</a></h4>
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
                        <td>
                            <s:if test="%{relationship.startsWith('has') && value.contains('.taxon:')}"><a href="show?guid=${value}">${value}</a></s:if>
                            <s:else>${value}</s:else>
                        </td>
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
