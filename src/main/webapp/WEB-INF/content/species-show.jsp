<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="c" uri="/WEB-INF/tld/c.tld" %>
<%@ taglib prefix="fn" uri="/WEB-INF/tld/fn.tld" %>
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
            //var scientificNameId, scientificName;

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

                // Lookup portal for species info
                $.getJSON("http://data.ala.org.au/search/scientificNames/%22${taxonNames[0].nameComplete}%22/json?callback=?", function(data){
                    //alert("inspecting JSON data: " + data);
                    if (data.result.length > 0) {
                        var scientificNameId = data.result[0].scientificNameId;
                        var scientificName = data.result[0].scientificName;
                        var scientificNameUrl = "http://data.ala.org.au" + data.result[0].scientificNameUrl;
                        var occurrenceCount = data.result[0].occurrenceCount;
                        var occurrenceTableUrl = "http://data.ala.org.au/occurrences/searchWithTable.htm?c[0].s=20&c[0].p=0&c[0].o=" + scientificNameId;
                        // modify page DOM with values
                        $("a#portalLink").attr("href", scientificNameUrl);
                        //$("a#occurTableLink").attr("href", occurrenceTableUrl);
                        var countText = "<a href='" + occurrenceTableUrl + "' target='_blank' " +
                            "title='View list of all occurrences'>" + occurrenceCount + "</a>";
                        $("#occurrenceCount").html(countText);
                        $("#portalBookmark").fadeIn();
                        $("#portalInfo").slideDown();
                        loadMap(scientificName,scientificNameId);
                    }
               });
            });
        </script>
        <script type="text/javascript" src="http://extjs.cachefly.net/builds/ext-cdn-771.js"></script>
        <link rel="stylesheet" type="text/css" href="http://extjs.cachefly.net/ext-2.2.1/resources/css/ext-all.css" />
        <link rel="stylesheet" type="text/css" href="http://extjs.cachefly.net/ext-2.2.1/examples/shared/examples.css" />
        <script type="text/javascript" src="http://openlayers.org/api/OpenLayers.js"></script>
        <script type="text/javascript">
            var map;
            function loadMap(scientificName,scientificNameId) {
                var mapPanel;
                //Ext.onReady(function() {
                    var options = {
                        numZoomLevels: 12,
                        controls: []
                        // maxExtent: new OpenLayers.Bounds()
                    }; 
                    map = new OpenLayers.Map('mappanel', options);
                    var layer = new OpenLayers.Layer.WMS(
                        "Global Imagery",
                        "http://maps.opengeo.org/geowebcache/service/wms",
                        {layers: "bluemarble"},
                        {wrapDateLine: true}
                    );

                    var cellDensityLayerUrl = 'http://data.ala.org.au';
                    //var cellDensityArray = [cellDensityLayerUrl+'/wms?'];
                    var entityName = '<span class="genera">' + scientificName + ' </span>';
                    var entityId = scientificNameId;
                    var entityType = 1; // species type

                    cellLayer = new OpenLayers.Layer.WMS(
                        entityName + " 1 degree cells",
                        "http://maps.ala.org.au/wms",  //cellDensityArray, // "http://maps.ala.org.au/wms",
                        {   layers: "ala:tabDensityLayer",
                            srs: 'EPSG:4326',
                            version: "1.0.0",
                            transparent: "true",
                            format: "image/png",
                            filter: "(<Filter><PropertyIsEqualTo><PropertyName>url</PropertyName><Literal><![CDATA["+cellDensityLayerUrl+"/mapping/simple/?id="+entityId+"&type="+entityType+"&unit=1]]></Literal></PropertyIsEqualTo></Filter>)"
                        },
                        {opacity: "0.65", wrapDateLine: true, buffer: 0}
                    );

                    var statesLayer = new OpenLayers.Layer.WMS("Political States",
                        "http://maps.ala.org.au/wms",
                        {layers: "ala:as",
                        srs: 'EPSG:4326',
                        version: "1.0.0",
                        transparent: "true",
                        format: "image/png",
                        maxExtent: new OpenLayers.Bounds(112.91,-54.76,159.11,-10.06)},
                        {alpha: true}
                    );

                    //map.addLayer(layer);
                    //map.addLayer(cellLayer);
                    map.addLayers([layer,cellLayer,statesLayer]);
                    map.addControl(new OpenLayers.Control.Navigation({zoomWheelEnabled: false}));
                    map.addControl(new OpenLayers.Control.PanZoom({zoomWorldIcon: false}));
                    map.setCenter(new OpenLayers.LonLat(133, -27), 4);

                    <%--mapPanel = new GeoExt.MapPanel({
                        title: "Species Density Map",
                        renderTo: "mappanel",
                        height: 450,
                        width: 550,
                        map: map,
                        center: new OpenLayers.LonLat(133, -27),
                        zoom: 4
                    });--%>
                //});
            }
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
            <s:set name="sciNameFormatted">
                <%--<s:if test="%{taxonNames.get(0).rank.contains('Species') || taxonNames.get(0).rank.contains('Genus')}"><i>${taxonNames[0].nameComplete}</i></s:if>--%>
                <c:choose>
                    <c:when test="${fn:containsIgnoreCase(taxonNames[0].rank,'species')}"><i>${taxonNames[0].nameComplete}</i></c:when>
                    <c:when test="${fn:containsIgnoreCase(taxonNames[0].rank,'genus')}"><i>${taxonNames[0].nameComplete}</i></c:when>
                    <c:otherwise>${taxonNames[0].nameComplete}</c:otherwise>
                </c:choose>
            </s:set>
            <div id="speciesHeader">
                <s:if test="%{!images.isEmpty()}">
                    <div id="speciesPhoto">
                        <img src="${images[0].photoSourceUrl}" width="250px" alt="species photo"/>
                    </div>
                </s:if>
                <div id="speciesTitle">
                    <h2>${fn:replace(tcTitle, taxonNames[0].nameComplete, sciNameFormatted)}</h2>
                    <div class="speciesInfo"><b>Scientific name: </b> <s:property value="sciNameFormatted" escape="false"/>
                    </div>
                    <div class="speciesInfo"><b>Taxon Rank: </b><s:property value="%{taxonNames.get(0).rank.replace('TaxonRank.', '')}" /></div>
                    <div class="speciesInfo"><b>GUID: </b><a href="${taxonNames[0].source}" target="_blank">${title}</a></div>
                </div>
                <div id="toc">
                    <p style="margin-bottom: 5px;"><b>Table of Contents</b> (click to jump to)</p>
                    <ul>
                        <li id="portalBookmark"><a href="#portal">Occurrences</a></li>
                        <s:if test="%{!images.isEmpty()}"><li><a href="#images">Images</a></li></s:if>
                        <s:if test="%{!htmlPages.isEmpty()}"><li><a href="#htmlpages">HTML Pages</a></li></s:if>
                        <li><a href="#properties">Properties</a></li>
                        <s:if test="%{!taxonNames.isEmpty()}"><li><a href="${pageContext.request.contextPath}/properties/${taxonNames[0].nameComplete}" class="popup">
                            Harvested Properties Table</a></li></s:if>
                    </ul>
                </div>
            </div>
            <div style="clear: both;"></div>

            <s:if test="%{taxonNames.size() > 1}"><a name="names">&nbsp;</a>
                <h4 class="divider">Names</h4>
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

            <div id="portalInfo">
                <h4 class="divider">Occurrence Data<a name="portal">&nbsp;</a></h4>
                <ul>
                    <li><a href="#" id="portalLink" target="_blank">View the "<s:property value="sciNameFormatted" escape="false"/>"
                          species page</a> on the ALA Portal</li>
                    <li>Number of occurrences: <span id="occurrenceCount"></span></li>
                </ul>
                <div id="mappanel"></div>
                <div style="float:right;font-size:11px;width:550px;">
                    <p style="margin:4px;">Occurrences per cell</p>
                    <table id="cellCountsLegend">
                        <tr style="width: 50px;">
                          <!--<td style="width:auto;">Cell Colour:</td>-->
                          <td style="width:50px;background-color:#ffff00;">&nbsp;</td>
                          <td style="width:50px;background-color:#ffcc00;">&nbsp;</td>

                          <td style="width:50px;background-color:#ff9900;">&nbsp;</td>
                          <td style="width:50px;background-color:#ff6600;">&nbsp;</td>
                          <td style="width:50px;background-color:#ff3300;">&nbsp;</td>
                          <td style="width:50px;background-color:#cc0000;">&nbsp;</td>
                        </tr>
                        <tr style="text-align: center;">
                          <!--<td style="width: auto;">No. Occurrences:</td>-->
                          <td>1&ndash;9</td>
                          <td>10&ndash;49</td>
                          <td>50&ndash;99</td>
                          <td>100&ndash;249</td>
                          <td>250&ndash;499</td>
                          <td>500&ndash;&infin;</td>
                        </tr>
                    </table>
                </div>

                <div style="clear: both;"></div>
            </div>

            <s:if test="%{!images.isEmpty()}">
                <h4 class="divider">Images<a name="images">&nbsp;</a></h4>
                <table class ="propertyTable">
                    <!-- Table headings. -->
                    <tr>
                        <th>Title</th>
                        <th>Desciption</th>
                        <th>Thumbnail</th>
                    </tr>
                    <!-- Dynamic table content. -->
                    <s:iterator value="images">
                        <tr>
                            <td><a href="${photoPage}" target="_blank">${title}</a></td>
                            <td>${description}</td>
                            <td><a href="${photoSourceUrl}" class="image" target="_blank" title="${title}"><img src="${photoSourceUrl}" height="55"/></a></td>
                        </tr>
                    </s:iterator>
                </table>
            </s:if>

            <s:if test="%{!htmlPages.isEmpty()}"><a name="htmlpages">&nbsp;</a>
                <h4 class="divider">HTML Pages</h4>
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
                                <table class="rdfProperties">
                                    <s:iterator value="rdfProperties" var="prop">
                                    <tr>
                                        <td>${prop.key}</td>
                                        <td>${prop.value}</td>
                                    </tr>
                                    </s:iterator>
                                </table>

                                <%--<ul><s:iterator value="rdfProperties" var="prop">
                                        <li><b>${prop.key}</b>: ${prop.value}<br/></li>
                                    </s:iterator></ul>--%>
                            </td>
                            <td><a href="http://${source}" target="_blank"><s:text name="source.%{source}"/></a></td>
                        </tr>
                    </s:iterator>
                </table>
            </s:if>

            <a name="properties">&nbsp;</a>
            <h4 class="divider">Properties</h4>
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
