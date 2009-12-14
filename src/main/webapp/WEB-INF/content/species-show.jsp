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
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/fancybox/jquery.fancybox-1.2.6.css" media="screen" />
        <script type="text/javascript" src="${pageContext.request.contextPath}/fancybox/jquery.fancybox-1.2.6.pack.js"></script>
        <script type="text/javascript">
            //var scientificNameId, scientificName;
            var solrServer = "${fedoraDAO.serverUrl}"; // TODO: read from properties file

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
                
                $("a#lsid").fancybox({
                    'hideOnContentClick' : false,
                    'frameWidth' : 600,
                    'frameHeight' : 100
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
                        $("#occurrenceCount").html(occurrenceCount);
                        $("a#occurrenceTableLink").attr("href", occurrenceTableUrl);
                        $("#portalBookmark").fadeIn();
                        $("#portalInfo").slideDown();
                        loadMap(scientificName,scientificNameId);
                        //loadSpeciesRdf(scientificNameUrl);
                    }
                });

                // convert lsid link text to TC title via SOLR
                $("a.lsidLink").each(function() {
                    var link = $(this);
                    var lsid = link.text();
                    //link.text("foo");
                    var uri = encodeURIComponent(filterQuery(lsid));
                    var query = "q=dc.identifier:"+uri+" AND ContentModel:ala.TaxonConceptContentModel&wt=json&rows=1&indent=true&json.wrf=?"
                    $.getJSON(solrServer+"/select?"+query, function(data){
                        if (data.response.numFound > 0) {
                            var doc = data.response.docs[0];
                            var title = doc['dc.title'];
                            var rank = doc.Rank;
                            link.text(title+" ("+rank+")");
                            link.css("display","inline");
                        } else {
                            link.text("[name not known]");
                            link.attr("title", lsid);
                            link.css("display","inline");
                        }
                    });
                });
            });

            /**
             * Escape special characters for SOLR query
             */
            function filterQuery(data) {
                data = data.replace(/\:/g, "\\:");
                data = data.replace(/\-/g, "\\-");
                return data;
            }

            /**
             * Perform AJAX request for RDF version of portal Species page
             * parse list of datasets and georegions and inject values
             * into pageDOM.
             *
             * Won't work without proxy due to cross domain security restriction
             */
            function loadSpeciesRdf(speciesPageUrl) {
                alert("Ajax request for: " + speciesPageUrl);
                $.ajax({
                    type: "GET",
                    url: speciesPageUrl,
                    data: "schema=rdf",
                    datatype: "xml",
                    success: function(xml){
                        alert( "Data Saved: " + xml );
                        $(xml).find("dataset:occurrencesCollectionSet").each(function() {
                            var name = $(this).find("dc:title").text();
                            var url = $(this).find("coll:Collection").attr("rdf:about");
                            alert("DataSet name: "+name+"; URI: "+url);
                        });
                    }
                });
            }
        </script>
        <script type="text/javascript" src="http://extjs.cachefly.net/builds/ext-cdn-771.js"></script>
        <link rel="stylesheet" type="text/css" href="http://extjs.cachefly.net/ext-2.2.1/resources/css/ext-all.css" />
        <link rel="stylesheet" type="text/css" href="http://extjs.cachefly.net/ext-2.2.1/examples/shared/examples.css" />
        <script type="text/javascript" src="http://openlayers.org/api/OpenLayers.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/GeoExt.js"></script>
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
                    var entityName = '<span class="genera">' + scientificName + ' </span>';
                    var entityId = scientificNameId;
                    var entityType = 1; // species type

                    cellLayer = new OpenLayers.Layer.WMS(
                        entityName + " 1 degree cells",
                        "http://maps.ala.org.au/wms",  //cellDensityArray, 
                        {layers: "ala:tabDensityLayer",
                        srs: 'EPSG:4326',
                        version: "1.0.0",
                        transparent: "true",
                        format: "image/png",
                        filter: "(<Filter><PropertyIsEqualTo><PropertyName>url</PropertyName><Literal><![CDATA["+cellDensityLayerUrl+"/mapping/simple/?id="+entityId+"&type="+entityType+"&unit=1]]></Literal></PropertyIsEqualTo></Filter>)"},
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

                    map.addLayers([layer,cellLayer,statesLayer]);
                    map.addControl(new OpenLayers.Control.Navigation({zoomWheelEnabled: false}));
                    map.addControl(new OpenLayers.Control.PanZoom({zoomWorldIcon: false}));
                    map.setCenter(new OpenLayers.LonLat(133, -27), 4);

                    mapPanel = new GeoExt.MapPanel({
                        //title: "Species Density Map",
                        renderTo: "mappanel",
                        border: false,
                        heder: false,
                        height: 450,
                        width: 550,
                        map: map,
                        center: new OpenLayers.LonLat(133, -27),
                        zoom: 4
                    });
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
            <c:set var="taxonConceptTitle">
                <c:choose>
                    <c:when test="${fn:length(taxonNames) > 0}">${taxonNames[0].nameComplete}</c:when>
                    <c:otherwise>${taxonConcept.title}</c:otherwise>
                </c:choose>
            </c:set>
            <s:set name="taxonConceptTitle">${taxonConceptTitle}</s:set>
            <c:set var="taxonConceptRank">
                <c:choose>
                    <c:when test="${!empty taxonConcept}">${taxonConcept.rank}</c:when>
                    <c:when test="${fn:length(taxonNames) > 0}">${taxonNames[0].rank}</c:when>
                    <c:otherwise>(rank not known)</c:otherwise>
                </c:choose>
            </c:set>
            <s:set name="taxonConceptRank">${taxonConceptRank}</s:set>
            <s:set name="sciNameFormatted">
                <c:choose>
                    <c:when test="${fn:containsIgnoreCase(taxonConceptRank,'species')}"><i>${taxonConceptTitle}</i></c:when>
                    <c:when test="${fn:containsIgnoreCase(taxonConceptRank,'genus')}"><i>${taxonConceptTitle}</i></c:when>
                    <c:otherwise>${taxonConceptTitle}</c:otherwise>
                </c:choose>
            </s:set>
            <c:set var="authorship">${fn:substringAfter(taxonNames[0].title, taxonNames[0].nameComplete)}</c:set>
            <div id="speciesHeader">
                <s:if test="%{!images.isEmpty()}">
                    <div id="speciesPhoto">
                        <img src="${images[0].photoSourceUrl}" width="250px" alt="species photo"/>
                    </div>
                </s:if>
                <div id="speciesTitle">
                    <h2>${sciNameFormatted}</h2>
                    <table class="noBorders" style="max-width:90%;margin:0;">
                        <tr>
                            <td>Classification:</td>
                            <td><s:text name="rank.%{taxonConceptRank}" /></td>
                        </tr>
                        <c:if test="${fn:length(authorship) > 0}"><tr>
                            <td>Authorship:</td>
                            <td>${authorship}</td>
                        </tr></c:if>
                        <c:if test="${fn:length(taxonConcept.parentTaxa) > 0}"><tr>
                                <td>Parent <s:if test="%{taxonConcept.parentTaxa.size() > 1}">Taxa</s:if><s:else>Taxon</s:else>:</td>
                            <td><s:iterator value="taxonConcept.parentTaxa" var="parent">
                                <a href="show?guid=${parent}" class="lsidLink">${parent}</a><br/>
                            </s:iterator></td>
                        </tr></c:if>
                        <c:if test="${fn:length(taxonConcept.childTaxa) > 0}"><tr>
                            <td>Child <s:if test="%{taxonConcept.childTaxa.size() > 1}">Taxa</s:if><s:else>Taxon</s:else>:</td>
                            <td><s:iterator value="taxonConcept.childTaxa" var="child">
                                <a href="show?guid=${child}" class="lsidLink">${child}</a><br/>
                            </s:iterator></td>
                        </tr></c:if>
                        <tr>
                            <td>Source:</td>
                            <td><a href="<s:text name="source.%{taxonConcept.source}.url" />" target="_blank"><s:text name="source.%{taxonConcept.source}" /></a></td>
                        </tr>
                    </table>
                    <div id="lsidText" style="display:none;">
                        <b><a href="http://lsids.sourceforge.net/" target="_blank">Life Science Identifier (LSID):</a></b>
                        <%--<input type="text" size="50" value="${taxonConcept.guid}"/>--%>
                        <p style="margin: 10px 0;"><a href="http://lsid.tdwg.org/summary/${taxonConcept.guid}" target="_blank">${taxonConcept.guid}</a></p>
                    </div>
                    <%--<div id="LSID_icon"><a href="show?guid=${taxonConcept.guid}" onclick="prompt('Life Science Identifier (LSID):','${taxonConcept.guid}');"><img src="${pageContext.request.contextPath}/images/lsid.png"/></a></div>--%>
                    <div id="LSID_icon"><a href="#lsidText" id="lsid"><img src="${pageContext.request.contextPath}/images/lsid.png"/></a></div>
                </div>
                <div id="toc">
                    <p style="margin-bottom: 5px;"><b>Jump to: </b></p>
                    <ul>
                        <c:if test="${fn:length(orderedDocuments) > 0}"><li><a href="#properties">Information from Other Sources</a></li></c:if>
                        <li id="portalBookmark"><a href="#portal">Distribution Map</a></li>
                        <s:if test="%{!images.isEmpty()}"><li><a href="#images">Images</a></li></s:if>
                        <%--<s:if test="%{!htmlPages.isEmpty()}"><li><a href="#htmlpages">HTML Pages</a></li></s:if>--%>
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

                <c:if test="${fn:length(orderedDocuments) > 0}">
                <div id="harvestedInfo">
                    <h4 class="divider">Information from Other Sources<a name="properties">&nbsp;</a>
                        <a href="${pageContext.request.contextPath}/properties/${taxonNames[0].nameComplete}?sort=true" class="popup">&beta;</a></h4>
                    <c:forEach items="${orderedDocuments}" var="orderedDocument">
                        <div id="harvestedProperties">
                            <p id="sourceTitle">${orderedDocument.infoSourceName} &ndash; <a href="${orderedDocument.sourceUrl}">${orderedDocument.sourceTitle}</a></p>
                            <table class="propertyTable">
                                <c:forEach var="categorisedProperties" items="${orderedDocument.categorisedProperties}">
                                    <c:if test="${categorisedProperties.category.name!='Taxonomic' && categorisedProperties.category.name!='Media'}">
                                        <%--<p>${categorisedProperties.category.name}</p>--%>
                                            <c:forEach var="entry" items="${categorisedProperties.propertyMap}">
                                                <c:if test="${fn:length(entry.value) > 1}">
                                                    <tr><s:set var="entryKey">${entry.key}</s:set>
                                                        <td class="propertyName"><s:text name="%{entryKey}"/></td>
                                                        <td>${entry.value}</td>
                                                    </tr>
                                                </c:if>
                                            </c:forEach>
                                    </c:if>
                                </c:forEach>
                            </table>
                                <%--<c:if test="${categorisedProperties.category.name=='Media'}">
                                    <h5>${categorisedProperties.category.name}</h5>
                                    <table>
                                        <tr>
                                            <c:forEach var="entry" items="${categorisedProperties.propertyMap}">
                                                <c:if test="${fn:startsWith(entry.value,'http://')}">
                                                    <td><img src ="${entry.value}"/></td>
                                                    </c:if>
                                            </c:forEach>
                                        </tr>
                                    </table>
                                </c:if>--%>
                        </div>
                    </c:forEach>
                </div>
            </c:if>

            <div id="portalInfo">
                <h4 class="divider">Distribution Map (generated from specimen & observation occurrence data)<a name="portal">&nbsp;</a></h4>
                <ul>
                    <li>Number of occurrences of ${sciNameFormatted}: <span id="occurrenceCount"></span></li>
                    <li><a href="#" id="occurrenceTableLink">View table of all occurrence records
                            for ${sciNameFormatted}</a></li>
                </ul>
                <div id="mappanel"></div>
                <div style="float:right;font-size:11px;width:550px;">
                    <table id="cellCountsLegend">
                        <tr>
                          <td style="background-color:#333; color:white; text-align:right;">Occurrences per cell:&nbsp;</td>
                          <td style="width:60px;background-color:#ffff00;">1&ndash;9</td>
                          <td style="width:60px;background-color:#ffcc00;">10&ndash;49</td>
                          <td style="width:60px;background-color:#ff9900;">50&ndash;99</td>
                          <td style="width:60px;background-color:#ff6600;">100&ndash;249</td>
                          <td style="width:60px;background-color:#ff3300;">250&ndash;499</td>
                          <td style="width:60px;background-color:#cc0000;">500+</td>
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
                        <th>Source</th>
                        <th>Thumbnail</th>
                    </tr>
                    <!-- Dynamic table content. -->
                    <s:iterator value="images">
                        <tr>
                            <td><a href="${photoPage}" target="_blank">${title}</a></td>
                            <td>${description}</td>
                            <td><a href="http://${source}" target="_blank"><s:text name="source.%{source}"/></a></td>
                            <td><a href="${photoSourceUrl}" class="image" target="_blank" title="${title}"><img src="${photoSourceUrl}" height="55"/></a></td>
                        </tr>
                    </s:iterator>
                </table>
            </s:if>

            <%-- HTML Pages --%>
            <%--<s:if test="%{!htmlPages.isEmpty()}"><a name="htmlpages">&nbsp;</a>
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
                            </td>
                            <td><a href="http://${source}" target="_blank"><s:text name="source.%{source}"/></a></td>
                        </tr>
                    </s:iterator>
                </table>
            </s:if>--%>

<%--            <a name="properties">&nbsp;</a>
            <h4 class="divider">Properties</h4>
            <table class ="propertyTable" style="display:none;">
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
            <s:if test="%{taxonConcept != null}">
                <h4 class="divider">Taxon Concept Properties</h4>
                <table class ="propertyTable">
                    <!-- Table headings. -->
                    <tr>
                        <th>Property</th>
                        <th>Value</th>
                    </tr>
                    <tr>
                        <td>PID</td>
                        <td>${taxonConcept.pid}</td>
                    </tr>
                    <tr>
                        <td>GUID</td>
                        <td>${taxonConcept.guid}</td>
                    </tr>
                    <tr>
                        <td>title</td>
                        <td>${taxonConcept.title}</td>
                    </tr>
                    <tr>
                        <td>Scientific Name</td>
                        <td>${taxonConcept.scientificName}</td>
                    </tr>
                    <tr>
                        <td>rank</td>
                        <td>${taxonConcept.rank}</td>
                    </tr>
                    <tr>
                        <td>Source</td>
                        <td><s:property value="taxonConcept.source" /></td>
                    </tr>
                    <tr>
                        <td>Parent Taxa</td>
                        <td><s:iterator value="taxonConcept.parentTaxa" var="parent">
                            <a href="show?guid=${parent}" class="lsidLink">${parent}</a><br/>
                        </s:iterator></td>
                    </tr>
                    <tr>
                        <td>Child Taxa</td>
                        <td><s:iterator value="taxonConcept.childTaxa" var="child">
                            <a href="show?guid=${child}" class="lsidLink">${child}</a><br/>
                        </s:iterator></td>
                    </tr>
                </table>
            </s:if>--%>
        </s:else>
    </body>
</html>
