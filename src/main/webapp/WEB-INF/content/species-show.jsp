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
        <%--<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.galleriffic.js"></script>--%>
        <!-- Combo-handled YUI CSS files: -->
        <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?2.8.0r4/build/tabview/assets/skins/sam/tabview.css">
        <!-- Combo-handled YUI JS files: -->
        <script type="text/javascript" src="http://yui.yahooapis.com/combo?2.8.0r4/build/yahoo-dom-event/yahoo-dom-event.js&2.8.0r4/build/element/element-min.js&2.8.0r4/build/tabview/tabview-min.js"></script>

        <script type="text/javascript">
            //var scientificNameId, scientificName;
            var solrServer = "${solrServerUrl}";//"http://localhost:8080/solr"; "${solrServerUrl}"// TODO: read from properties file

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
                    'frameHeight' : 150
                });

                $("#view2").hide();
                $('a.hideShow').click(
                    function(e) {
                        e.preventDefault(); //Cancel the link behavior
                        var num = $(this).text();
                        var otherNum = (num == 1) ? 2 : 1;
                        $("#view"+otherNum).slideUp();
                        $("#view"+num).slideDown();
                    }
                );
                
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
                    } else {
                        // no records - check for other sections and if none then remove the "jumpt to" text
                        var tocListItems = $("div#toc ul").children();
                        if (tocListItems.length < 2) {
                            $("div#toc").hide();
                        }
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

                $(".divider").hide();

                // Gallerific JQuery image gallery
                <%--jQuery(document).ready(function($) {
                    var gallery = $('#thumbs').galleriffic({
                        delay:                     3000, // in milliseconds
                        numThumbs:                 10, // The number of thumbnails to show page
                        preloadAhead:              20, // Set to -1 to preload all images
                        enableTopPager:            false,
                        enableBottomPager:         true,
                        maxPagesToShow:            7,  // The maximum number of pages to display in either the top or bottom pager
                        imageContainerSel:         '', // The CSS selector for the element within which the main slideshow image should be rendered
                        controlsContainerSel:      '', // The CSS selector for the element within which the slideshow controls should be rendered
                        captionContainerSel:       '', // The CSS selector for the element within which the captions should be rendered
                        loadingContainerSel:       '', // The CSS selector for the element within which should be shown when an image is loading
                        renderSSControls:          true, // Specifies whether the slideshow's Play and Pause links should be rendered
                        renderNavControls:         true, // Specifies whether the slideshow's Next and Previous links should be rendered
                        playLinkText:              'Play',
                        pauseLinkText:             'Pause',
                        prevLinkText:              'Previous',
                        nextLinkText:              'Next',
                        nextPageLinkText:          'Next &rsaquo;',
                        prevPageLinkText:          '&lsaquo; Prev',
                        enableHistory:             false, // Specifies whether the url's hash and the browser's history cache should update when the current slideshow image changes
                        enableKeyboardNavigation:  true, // Specifies whether keyboard navigation is enabled
                        autoStart:                 false, // Specifies whether the slideshow should be playing or paused when the page first loads
                        syncTransitions:           false, // Specifies whether the out and in transitions occur simultaneously or distinctly
                        defaultTransitionDuration: 1000, // If using the default transitions, specifies the duration of the transitions
                        onSlideChange:             undefined, // accepts a delegate like such: function(prevIndex, nextIndex) { ... }
                        onTransitionOut:           undefined, // accepts a delegate like such: function(slide, caption, isSync, callback) { ... }
                        onTransitionIn:            undefined, // accepts a delegate like such: function(slide, caption, isSync) { ... }
                        onPageTransitionOut:       undefined, // accepts a delegate like such: function(callback) { ... }
                        onPageTransitionIn:        undefined, // accepts a delegate like such: function() { ... }
                        onImageAdded:              undefined, // accepts a delegate like such: function(imageData, $li) { ... }
                        onImageRemoved:            undefined  // accepts a delegate like such: function(imageData, $li) { ... }
                    });
                });--%>
            });

            /**
             * Escape special characters for SOLR query
             */
            function filterQuery(data) {
                data = data.replace(/\:/g, "\\:");
                data = data.replace(/\-/g, "\\-");
                return data;
            }

            //$("ul.yui-nav").hide();
            var tabView = new YAHOO.widget.TabView('tabs');
            
            // function to load YUI tabs
            function showTabs() {
                $("ul.yui-nav").show();
                $("#yui-box").addClass("yui-content");
                $(".divider").hide();
                tabView = new YAHOO.widget.TabView('tabs');
            }

            function hideTabs() {
                $("ul.yui-nav").hide();
                $("#harvestedInfo").show();
                $("#portalInfo").show();
                $("#images").show();
                $(".divider").show();
                $("#yui-box").removeClass("yui-content");
                tabView = null;
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
                <p>Examples:<br/><a href='${pageContext.request.contextPath}/species/?propertyValue="Pogona+barbata"'>&quot;Pogona barbata&quot;</a><br/>
                    <a href='${pageContext.request.contextPath}/species/?propertyValue="Hypertropha+chlaenota"'>&quot;Hypertropha chlaenota&quot;</a><br/>
                    <a href='${pageContext.request.contextPath}/species/?propertyValue="Argyropelecus+gigas"'>&quot;Argyropelecus gigas&quot;</a><br/>
                    <a href='${pageContext.request.contextPath}/species/?propertyValue="Podargus+strigoides"'>&quot;Podargus strigoides&quot;</a><br/>
                    <a href='${pageContext.request.contextPath}/species/?propertyValue="Glossopsitta+concinna"'>&quot;Glossopsitta concinna&quot;</a><br/>
                    <a href='${pageContext.request.contextPath}/species/?propertyValue="Hippotion+scrofa"'>&quot;Hippotion scrofa&quot;</a><br/>
                    <a href='${pageContext.request.contextPath}/species/?propertyValue=Asplenium+australasicum'>&quot;Asplenium australasicum&quot;</a><br/>
                    <a href='${pageContext.request.contextPath}/species/?propertyValue=Malacorhynchus+membranaceus'>&quot;Malacorhynchus membranaceus&quot;</a><br/>
                </p>
            </div>
            <script type="text/javascript">
                $("#menuSearch").hide();
            </script>
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
                    <div id="speciesPhoto" class="cropBig">
                        <img src="${images[0].photoSourceUrl}" style="/*max-width:250px;max-height:280px;*/" width="300"  alt="species photo"/>
                    </div>
                </s:if>
                <div id="speciesTitle">
                    <h2>${sciNameFormatted}</h2>
                    <table class="noBorders" style="max-width:90%;margin:0;">
                        <tr>
                            <td class="propertyName">Classification:</td>
                            <td><s:text name="rank.%{taxonConceptRank}" /></td>
                        </tr>
                        <c:if test="${fn:length(authorship) > 0}"><tr>
                            <td class="propertyName">Authorship:</td>
                            <td>${authorship}</td>
                        </tr></c:if>
                        <c:if test="${fn:length(taxonConcept.parentTaxa) > 0}"><tr>
                                <td class="propertyName">Parent <s:if test="%{taxonConcept.parentTaxa.size() > 1}">Taxa</s:if><s:else>Taxon</s:else>:</td>
                            <td><s:iterator value="taxonConcept.parentTaxa" var="parent">
                                <a href="show?guid=${parent}" class="lsidLink">${parent}</a><br/>
                            </s:iterator></td>
                        </tr></c:if>
                        <c:if test="${fn:length(taxonConcept.childTaxa) > 0}"><tr>
                            <td class="propertyName">Child <s:if test="%{taxonConcept.childTaxa.size() > 1}">Taxa</s:if><s:else>Taxon</s:else>:</td>
                            <td><s:iterator value="taxonConcept.childTaxa" var="child">
                                <a href="show?guid=${child}" class="lsidLink">${child}</a><br/>
                            </s:iterator></td>
                        </tr></c:if>
                        <tr>
                            <td class="propertyName">Source:</td>
                            <td><a href="<s:text name="source.%{taxonConcept.source}.url" />" target="_blank"><s:text name="source.%{taxonConcept.source}" /></a></td>
                        </tr>
                    </table>
                    <div id="lsidText" style="display:none;">
                        <b><a href="http://lsids.sourceforge.net/" target="_blank">Life Science Identifier (LSID):</a></b>
                        <%--<input type="text" size="50" value="${taxonConcept.guid}"/>--%>
                        <p style="margin: 10px 0;"><a href="http://lsid.tdwg.org/summary/${taxonConcept.guid}" target="_blank">${taxonConcept.guid}</a></p>
                        <p style="font-size: 12px;">LSIDs are persistent, location-independent,resource identifiers for uniquely naming biologically
                             significant resources including species names, concepts, occurrences, genes or proteins,
                             or data objects that encode information about them. To put it simply,
                            LSIDs are a way to identify and locate pieces of biological information on the web. </p>
                    </div>
                    <%--<div id="LSID_icon"><a href="show?guid=${taxonConcept.guid}" onclick="prompt('Life Science Identifier (LSID):','${taxonConcept.guid}');"><img src="${pageContext.request.contextPath}/images/lsid.png"/></a></div>--%>
                    <div id="LSID_icon"><a href="#lsidText" id="lsid"><img src="${pageContext.request.contextPath}/images/lsid.png"/></a></div>
                </div>
                <%--<div id="toc">
                    <p style="margin-bottom: 5px;"><b>Jump to: </b></p>
                    <ul>
                        <c:if test="${fn:length(orderedDocuments) > 0}"><li><a href="#properties">Information from Other Sources</a></li></c:if>
                        <li id="portalBookmark"><a href="#portal">Distribution Map</a></li>
                        <s:if test="%{!images.isEmpty()}"><li><a href="#images">Images</a></li></s:if>
                        <s:if test="%{!htmlPages.isEmpty()}"><li><a href="#htmlpages">HTML Pages</a></li></s:if>
                    </ul>
                </div>--%>
            </div>
            <%--<div style="clear: both;"></div>--%>
    <div id="tabs" class="yui-navset" style="clear: both;">
        <ul class="yui-nav">
            <li class="selected"><a href="#harvestedInfo"><em>Information from Other Sources</em></a></li>
            <li><a href="#portalInfo"><em>Distribution Map</em></a></li>
            <li><a href="#images"><em>Images</em></a></li>
        </ul>
        <div id="yui-box" class="yui-content">

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

                
            <div id="harvestedInfo">
                <c:if test="${fn:length(orderedDocuments) > 0 && fn:length(orderedProperties) > 0}">
                    <h4 class="divider" style="">Information from Other Sources<a name="properties"></a></h4>
                    <div style="float:right;width:20%;margin-top:-35px;text-align:right;">(Alternative View: <a href="#view1" class="hideShow">1</a> |
                        <a href="#view2" class="hideShow">2</a> | 
                        <a href="${pageContext.request.contextPath}/properties/${taxonNames[0].nameComplete}?sort=true" class="popup">raw</a>)
                    </div>
                    <div id="view2">
                        <c:forEach items="${orderedDocuments}" var="orderedDocument">
                            <div id="harvestedProperties">
                                <p id="sourceTitle">${orderedDocument.infoSourceName} &ndash; <a href="${orderedDocument.sourceUrl}">${orderedDocument.sourceTitle} </a></p>
                                <table class="propertyTable">
                                    <c:forEach var="categorisedProperties" items="${orderedDocument.categorisedProperties}">
                                        <c:if test="${categorisedProperties.category.name!='0Taxonomic' && categorisedProperties.category.name!='Media'}">
                                            <!--<p>${categorisedProperties.category.name}</p>-->
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
                            </div>
                        </c:forEach>
                    </div>
                    <div id="view1">
                        <%--<div id="harvestedProperties">--%>
                            <table class="propertyTable">
                                <tr>
                                    <th>Property</th>
                                    <th>Value</th>
                                    <th>Source</th>
                                </tr>
                                <c:forEach items="${orderedProperties}" var="orderedProperty">
                                    <tr class="${orderedProperty.category.name}">
                                        <td class="propertyName">
                                            <s:set var="propertyName2">${orderedProperty.propertyName}</s:set>
                                            <s:text name="%{propertyName2}"/>
                                        </td>
                                        <td>
                                             <c:choose>
                                                <c:when test="${fn:startsWith(orderedProperty.propertyValue, 'http') && orderedProperty.propertyName == 'rdf.hasImageUrl'}">
                                                    <a href="${orderedProperty.propertyValue}" target="_blank" class="popup" title="${taxonConceptTitle}">${orderedProperty.propertyValue}</a>
                                                </c:when>
                                                <c:when test="${fn:startsWith(orderedProperty.propertyValue, 'http')}">
                                                    <a href="${orderedProperty.propertyValue}" target="_blank">${orderedProperty.propertyValue}</a>
                                                </c:when>
                                                <c:otherwise>
                                                    ${orderedProperty.propertyValue}
                                                </c:otherwise>
                                             </c:choose>
                                        </td>
                                        <td style="white-space: nowrap;">
                                            <%--<c:choose>
                                                <c:when test="${fn:length(orderedProperty.sources) > 1}">
                                                    <ul class="compact">
                                                    --%><c:forEach items="${orderedProperty.sources}" var="source">
                                                        <%--<li>--%><a href="${source.sourceUrl}" target="_blank">${source.infoSourceName}</a><br/><%--</li>--%>
                                                    </c:forEach>
                                                    <%--</ul>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="${orderedProperty.sources[0].sourceUrl}" target="_blank">${orderedProperty.sources[0].infoSourceName}</a>
                                                </c:otherwise>
                                            </c:choose>--%>
                                            
                                        </td>
                                        <%--<td>${orderedProperty.category.name}</td>
                                        <td>${orderedProperty.sourceUrl}</td>
                                        <td>${orderedProperty.sourceTitle}</td>
                                        <td>${orderedProperty.infoSourceUrl}</td>
                                        <td>${orderedProperty.infoSourceName}</td>--%>
                                    </tr>
                                </c:forEach>
                            </table>
                        <%--</div>--%>
                    </div>
                </c:if>
            </div>

            <div id="portalInfo">
                <h4 class="divider">Distribution Map <a name="portal">&nbsp;</a></h4>
                <div id="left">
                    <p>Species density layer generated from specimen & observation occurrence data</p>
                    <ul>
                        <%--<li>Number of occurrences of ${sciNameFormatted}: <span id="occurrenceCount"></span></li>--%>
                        <li><a href="#" id="occurrenceTableLink">View table of all occurrence records
                                for ${sciNameFormatted}</a></li>
                        <li>Total number of records: <span id="occurrenceCount"></span></li>
                        <li>Breakdown by Regions</li>
                        <ul style="list-style-type: circle;">
                            <li>States:

                            </li>
                            <li>Local Government Areas:
                            </li>
                            <li>Biogeographical Regions:
                            </li>
                        </ul>
                    </ul>
                </div>
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
                <div id="images">
                    <h4 class="divider">Images<a name="images">&nbsp;</a></h4>
                    <%--<div id="gallery" class="content">
                        <div id="controls" class="controls"></div>
                        <div class="slideshow-container">
                                <div id="loading" class="loader"></div>
                                <div id="slideshow" class="slideshow"></div>
                        </div>
                        <div id="caption" class="caption-container"></div>
                    </div>
                    <div id="thumbs" class="navigation">

                        <ul class="thumbs noscript">
                            <c:forEach items="${images}" var="image">
                            <li>
                                <a class="thumb" name="optionalCustomIdentifier" href="${image.photoSourceUrl}" title="${image.title}">
                                    <img src="${image.photoSourceUrl}" alt="${image.title}" width="75" height="75"/>
                                </a>
                                <div class="caption">${image.description}</div>
                            </li>
                            </c:forEach>
                        </ul>
                    </div>
                    <div style="clear: both;"></div>--%>

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
                                <td class="crop">
                                    <a href="${photoSourceUrl}" class="image" target="_blank" title="${title}"><img src="${photoSourceUrl}" width="120" /></a>
                                </td>
                            </tr>
                        </s:iterator>
                    </table>
                </div>
            </s:if>
        </div>
    </div>
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

<!--            <a name="properties">&nbsp;</a>
            <h4 class="divider">Properties</h4>
            <table class ="propertyTable" style="display:none;">
                <tr>
                    <th>Property</th>
                    <th>Value</th>
                    <th>Harvested</th>
                    <th>Source</th>
                </tr>
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
            <br /> -->
            <%--
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
            <div style="width:40%;float:right;text-align:right;margin-top: 10px;">
                <a href="#" onClick="showTabs();">show tabs</a> --
                <a href="#" onClick="hideTabs();">hide tabs</a>
            </div>
        </s:else>
    </body>
</html>
