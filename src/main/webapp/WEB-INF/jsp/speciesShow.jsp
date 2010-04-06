<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta name="pageName" content="species" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>ALA Biodiversity Information Explorer: ${extendedTaxonConcept.taxonConcept.nameString}</title>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/static/js/jquery-fancybox/jquery.fancybox-1.3.1.css" media="screen" />
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery-fancybox/jquery.fancybox-1.3.1.pack.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery.easing.1.3.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery-galleryview-1.1/jquery.galleryview-1.1.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery-galleryview-1.1/jquery.timers-1.1.2.js"></script>
        <%--<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.galleriffic.js"></script>--%>
        <!-- Combo-handled YUI CSS files: -->
<%--        <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?2.8.0r4/build/tabview/assets/skins/sam/tabview.css">
        <!-- Combo-handled YUI JS files: -->
        <script type="text/javascript" src="http://yui.yahooapis.com/combo?2.8.0r4/build/yahoo-dom-event/yahoo-dom-event.js&2.8.0r4/build/element/element-min.js&2.8.0r4/build/tabview/tabview-min.js"></script>
--%>
        <script type="text/javascript">
            //var scientificNameId, scientificName;
            var solrServer = "${solrServerUrl}"; //

            $(document).ready(function() {
                $("a.popup").fancybox({
                    'autoDimensions' : false,
                    'width' : 800,
                    'height' : 500,
                    'hideOnContentClick' : false
                });

                $("a.image").fancybox({
                    'autoScale' : true,
                    'hideOnContentClick' : false
                });

                $("a#lsid").fancybox({
                    'hideOnContentClick' : false,
                    'titleShow' : false,
                    'autoDimensions' : false,
                    'width' : 600,
                    'height' : 150
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

                // convert document id to info source name and link
                $("a.docId").each(function() {
                    var link = $(this);
                    var docId = link.text();
                    var uri = "${pageContext.request.contextPath}/species/document/"+docId+".json";
                    $.getJSON(uri, function(data) {
                        if (data.document.id > 0) {
                            if (link.attr("href") == "title") {
                                link.text(data.document.title);
                                link.attr("href", data.document.identifier);
                            } else if (link.attr("href") == "source") {
                                link.text(data.document.infoSourceName);
                                var uri2 = (data.document.infoSourceUri == null) ? "#" : data.document.infoSourceUri;
                                link.attr("href", uri2);
                            } else {
                                link.attr("title", data.document.identifier);
                                link.attr("alt", data.document.identifier);
                            }
                        }
                    });
                });

                $(".divider").hide();

                // Show/Hide long list
                //$(".showHide").siblings().append("<p><a href='#' class='showHideLink'>Show all</a></p>");
                $(".showHide").hide();
                $(".showHideLink").text("+ show more").css("font-size","12px");
                $(".showHideLink").toggle(function() {
                        $(".showHide").slideDown();
                        $(this).text("- show less");
                    }, function() {
                        $(".showHide").slideUp();
                        $(this).text("+ show more");
                    }
                 );

                 $('#photos').galleryView({
                    panel_width: 925,
                    panel_height: 400,
                    frame_width: 100,
                    frame_height: 100,
                    border: 'none'
                    <%--filmstrip_size: 4,
                    frame_width: 100,
                    frame_height: 100,
                    background_color: 'transparent',
                    nav_theme: 'dark',
                    border: 'none',
                    show_captions:true,
                    caption_text_color: 'black'--%>
                });

                /*
                 * Enable Tabs
                 */
                $(function() {
                    $("#tabs").tabs();
                });

                $('#tabs').bind('tabsselect', function(event, ui) {
                    //alert("portalInfo was selected:"+ui.panel.id);
                    if (ui.panel.id == 'portalInfo') {
                        //loadMap("${extendedTaxonConcept.taxonName.nameComplete}")
                    }
                });

            });  // end document ready function

            /**
             * Escape special characters for SOLR query
             */
            function filterQuery(data) {
                data = data.replace(/\:/g, "\\:");
                data = data.replace(/\-/g, "\\-");
                return data;
            }

        </script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/ext-cdn-771.js"></script>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/static/css/ext-all.css" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/static/css/ext-examples.css" />
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/openlayers/OpenLayers.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/GeoExt.js"></script>
        <script type="text/javascript">
            var map, mapPanel;

            /**
             * Initiate an OpenLayers Map
             */
            function loadMap(scientificName, scientificNameId) {
                if (map != null && mapPanel != null) {
                    map.destroy();
                    mapPanel.destroy();
                }

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

                map.addLayer(layer);
                map.addLayer(statesLayer);

                var cellDensityLayerUrl = 'http://data.ala.org.au';
                var entityName = '<span class="genera">' + scientificName + ' </span>';
                var entityId = scientificNameId;
                var entityType = 1; // species type

                if (scientificNameId != null) {
                    var cellLayer = new OpenLayers.Layer.WMS(
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

                    map.addLayer(cellLayer);
                }

                //map.addLayers([layer,cellLayer,statesLayer]);
                map.addControl(new OpenLayers.Control.Navigation({zoomWheelEnabled: false}));
                map.addControl(new OpenLayers.Control.PanZoom({zoomWorldIcon: false}));
                map.setCenter(new OpenLayers.LonLat(133, -27), 4);

                mapPanel = new GeoExt.MapPanel({
                    //title: "Species Density Map",
                    renderTo: "mappanel",
                    stateId: "mappanel",
                    border: false,
                    header: false,
                    height: 450,
                    width: 550,
                    map: map,
                    center: new OpenLayers.LonLat(133, -27),
                    zoom: 4
                });
                
            }

            Ext.onReady(function() {
                $('#debug').append("ext.onReady loadMap<br/>");
                loadMap("${extendedTaxonConcept.taxonName.nameComplete}", null);

                // Lookup portal for species info
                $.getJSON("http://data.ala.org.au/search/scientificNames/%22${extendedTaxonConcept.taxonName.nameComplete}%22/json?callback=?", function(data){
                    //alert("inspecting JSON data: " + data);
                    if (data.result.length > 0) {
                        var sciNameId = data.result[0].scientificNameId;
                        var scientificName = data.result[0].scientificName;
                        var scientificNameUrl = "http://data.ala.org.au" + data.result[0].scientificNameUrl;
                        var occurrenceCount = data.result[0].occurrenceCount;
                        var occurrenceTableUrl = "http://data.ala.org.au/occurrences/searchWithTable.htm?c[0].s=20&c[0].p=0&c[0].o=" + sciNameId;
                        // modify page DOM with values
                        $("a#portalLink").attr("href", scientificNameUrl);
                        //$("a#occurTableLink").attr("href", occurrenceTableUrl);
                        $("#occurrenceCount").html(occurrenceCount);
                        $("a#occurrenceTableLink").attr("href", occurrenceTableUrl);
                        $("#portalBookmark").fadeIn();
                        //$("#portalInfo").slideDown();
                        $('#debug').append("ajax loadMap<br/>");
                        loadMap(scientificName,sciNameId);
                        //loadSpeciesRdf(scientificNameUrl);
                    } else {
                        // no records - check for other sections and if none then remove the "jumpt to" text
                        var tocListItems = $("div#toc ul").children();
                        if (tocListItems.length < 2) {
                            $("div#toc").hide();
                        }
                        $('#debug').append("ajax failed<br/>");
                    }
                });
            });
        </script>
    </head>
    <body>
        <c:set var="taxonConceptTitle">
            <c:choose>
                <c:when test="${fn:length(taxonNames) > 0}">${taxonNames[0].nameComplete}</c:when>
                <c:otherwise>${taxonConcept.title}</c:otherwise>
            </c:choose>
        </c:set>
        <c:set var="taxonConceptRank">
            <c:choose>
                <c:when test="${taxonConcept != null}">${taxonConcept.rank}</c:when>
                <c:when test="${fn:length(extendedTaxonConcept.taxonName.rankLabel) > 0}">${extendedTaxonConcept.taxonName.rankLabel}</c:when>
                <c:otherwise>(rank not known)</c:otherwise>
            </c:choose>
        </c:set>
        <c:set var="sciNameFormatted">
            <c:choose>
                <%--<c:when test="${extendedTaxonConcept.taxonName.nameComplete != null}"><i>${extendedTaxonConcept.taxonName.nameComplete}</i></c:when>--%>
                <c:when test="${fn:endsWith(extendedTaxonConcept.taxonName.rankString,'gen')}"><i>${extendedTaxonConcept.taxonName.nameComplete}</i></c:when>
                <c:when test="${fn:endsWith(extendedTaxonConcept.taxonName.rankString,'sp')}"><i>${extendedTaxonConcept.taxonName.nameComplete}</i></c:when>
                <c:when test="${fn:endsWith(extendedTaxonConcept.taxonName.rankString,'sp')}"><i>${extendedTaxonConcept.taxonName.nameComplete}</i></c:when>
                <c:otherwise>${extendedTaxonConcept.taxonConcept.nameString}</c:otherwise>
            </c:choose>
        </c:set>
        <div id="speciesHeader">
            <div id="speciesTitle">
                <c:if test="${fn:length(extendedTaxonConcept.images) > 0}">
                    <a href="http://${pageContext.request.serverName}:80${fn:replace(extendedTaxonConcept.images[0].repoLocation, "/data/bie", "/repository")}"
                           title="${extendedTaxonConcept.images[0].title} - ${extendedTaxonConcept.images[0].infoSourceName}" class="image">
                        <div id="speciesPhoto" style="background-image:url(${pageContext.request.contextPath}/species/images/${extendedTaxonConcept.images[0].documentId}.jpg?scale=130)"></div>
                    </a>
                </c:if>
                <h2>
                    <span id="rankInTitle" class="show-60"><fmt:message key="rank.${taxonConceptRank}" />:</span>
                    ${fn:replace(extendedTaxonConcept.taxonConcept.nameString, extendedTaxonConcept.taxonName.nameComplete, sciNameFormatted)}
                </h2>
                <div id="commonNames">
                    ${commonNames}
                </div>
                <table class="noBorders" style="max-width:90%;margin:0;">
                    <c:if test="${fn:length(extendedTaxonConcept.taxonName.authorship) > 100}"><tr>
                        <td class="propertyName">Authorship:</td>
                        <td>${extendedTaxonConcept.taxonName.authorship}</td>
                    </tr></c:if>
                    <c:if test="${fn:length(extendedTaxonConcept.parentConcepts) > 0}"><tr>
                        <td class="propertyName">Parent <c:if test="${fn:length(extendedTaxonConcept.parentConcepts) > 1}">Taxa</c:if>
                            <c:if test="${fn:length(extendedTaxonConcept.parentConcepts) < 2}">Taxon</c:if>:
                        </td>
                        <td><c:forEach items="${extendedTaxonConcept.parentConcepts}" var="parent">
                                <a href="<c:url value='/species/${parent.guid}'/>">${parent.nameString}</a><br/>
                            </c:forEach>
                        </td>
                    </tr></c:if>
                    <c:if test="${fn:length(extendedTaxonConcept.childConcepts) > 0}"><tr>
                        <td class="propertyName">Child <c:if test="${fn:length(extendedTaxonConcept.childConcepts) > 1}">Taxa</c:if>
                            <c:if test="${fn:length(extendedTaxonConcept.childConcepts) < 2}">Taxon</c:if>:
                        </td>
                        <td><c:forEach items="${extendedTaxonConcept.childConcepts}" var="child">
                                <a href="<c:url value='/species/${child.guid}'/>">${child.nameString}</a><br/>
                            </c:forEach>
                        </td>
                    </tr></c:if>
                    <tr>
                        <td class="propertyName" style="padding-bottom:20px;">Source:</td>
                        <td>
                            <%--<a href="<fmt:message key="source.${taxonConcept.source}.url" />" target="_blank"><fmt:message key="source.${taxonConcept.source}" /></a>--%>
                            <a href="${extendedTaxonConcept.taxonConcept.infoSourceURL}">${extendedTaxonConcept.taxonConcept.infoSourceName}</a>
                        </td>
                    </tr>
                </table>
                <div style="display:none;">
                    <div id="lsidText">
                        <b><a href="http://lsids.sourceforge.net/" target="_blank">Life Science Identifier (LSID):</a></b>
                        <p style="margin: 10px 0;"><a href="http://lsid.tdwg.org/summary/${extendedTaxonConcept.taxonConcept.guid}" target="_blank">${extendedTaxonConcept.taxonConcept.guid}</a></p>
                        <p style="font-size: 12px;">LSIDs are persistent, location-independent,resource identifiers for uniquely naming biologically
                             significant resources including species names, concepts, occurrences, genes or proteins,
                             or data objects that encode information about them. To put it simply,
                            LSIDs are a way to identify and locate pieces of biological information on the web. </p>
                    </div>
                </div>
                <div id="LSID_icon">
                    <a href="#lsidText" id="lsid" title="LSID info"><img src="${pageContext.request.contextPath}/static/images/lsid.png"/></a>
                    <a href="${pageContext.request.contextPath}/species/${extendedTaxonConcept.taxonConcept.guid}.json" title="View JSON data for this taxon" id="json">JSON</a>
                </div>
            </div>
        </div>
        <div id="tabs" style="clear: both;">
            <ul>
                <c:set var="tabIsFirst" value="false"/>
                <c:if test="${not empty extendedTaxonConcept.conservationStatuses || fn:length(extendedTaxonConcept.pestStatuses) > 0 || fn:length(textProperties) > 0}">
                    <li><a href="#harvestedInfo"><em>Information</em></a></li>
                    <c:set var="tabIsFirst" value="true"/>
                </c:if>
                <c:if test="${not empty extendedTaxonConcept.taxonConcept}">
                    <li<c:if test="${tabIsFirst == 'false'}"> class="selected"</c:if>><a href="#names"><em>Names</em></a></li>
                </c:if>
                <c:if test="${not empty extendedTaxonConcept.classification}">
                    <li><a href="#classification"><em>Classification</em></a></li>
                </c:if>
                <c:if test="${not empty extendedTaxonConcept.references}">
                    <li><a href="#literature"><em>Literature</em></a></li>
                </c:if>
                <li><a href="#portalInfo"><em>Distribution Map</em></a></li>
                <c:if test="${fn:length(extendedTaxonConcept.images) > 0}">
                    <li><a href="#images"><em>Images</em></a></li>
                </c:if>
            </ul>
            <div id="yui-box" class="yui-content">
                <c:if test="${fn:length(textProperties) > 0 || fn:length(extendedTaxonConcept.conservationStatuses) > 0 || fn:length(extendedTaxonConcept.pestStatuses) > 0}">
                    <div id="harvestedInfo">
                        <table class="propertyTable">
                            <tr>
                                <th width="15%"></th>
                                <th width="70%"></th>
                                <th width="15%"></th>
                            </tr>
                            <c:forEach var="status" items="${extendedTaxonConcept.conservationStatuses}">
                                <tr>
                                    <td class="propertyNames">Conservation Status</td>
                                    <td>${status.status}</td>
                                    <td><a href="${status.infoSourceURL}" target="_blank" title="${status.infoSourceName}">${status.infoSourceName}</a></td>
                                </tr>
                            </c:forEach>
                            <c:forEach var="status" items="${extendedTaxonConcept.pestStatuses}">
                                <tr>
                                    <td class="propertyNames">Pest Status</td>
                                    <td>${status.status}</td>
                                    <td><a href="${status.infoSourceURL}" target="_blank" title="${status.infoSourceName}">${status.infoSourceName}</a></td>
                                </tr>
                            </c:forEach>
                            <c:forEach var="status" items="${extendedTaxonConcept.extantStatusus}">
                                <tr>
                                    <td class="propertyNames">Extant Status</td>
                                    <td><fmt:message key="status.${status.status}"/></td>
                                    <td><a href="${status.infoSourceURL}" target="_blank" title="${status.infoSourceName}">${status.infoSourceName}</a></td>
                                </tr>
                            </c:forEach>
                            <c:forEach var="status" items="${extendedTaxonConcept.habitats}">
                                <tr>
                                    <td class="propertyNames">Habitat Status</td>
                                    <td><fmt:message key="habitat.${status.status}"/></td>
                                    <td><a href="${status.infoSourceURL}" target="_blank" title="${status.infoSourceName}">${status.infoSourceName}</a></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${not empty textProperties}">
                                <tr class="textProperty">
                                    <td colspan="3" style="height:0;padding:0;"></td>
                                </tr>
                            </c:if>
                            <c:forEach var="textProperty" items="${textProperties}">
                                <tr>
                                    <td class="propertyNames"><fmt:message key="${fn:substringAfter(textProperty.name, '#')}"/></td>
                                    <td>${textProperty.value}</td>
                                    <td><a href="${textProperty.identifier}" target="_blank" title="${textProperty.title}">${textProperty.infoSourceName}</a></td>
                                </tr>
                            </c:forEach>
                        </table>
                    </div>
                </c:if>
                <c:if test="${not empty extendedTaxonConcept.taxonConcept}">
                    <!--Names-->
                    <div id="names">
                        <table class="propertyTable">
                            <tr>
                                <th width="15%"></th>
                                <th width="70%"></th>
                                <th width="15%"></th>
                            </tr>
                            <tr>
                                <td class="propertyNames">Accepted Name</td>
                                <td>${extendedTaxonConcept.taxonConcept.nameString}</td>
                                <td>Published in: ${extendedTaxonConcept.taxonName.publishedIn}</td>
                            </tr>
                            <c:forEach items="${extendedTaxonConcept.synonyms}" var="synonym">
                                <tr>
                                    <td class="propertyNames">Synonym</td>
                                    <td>${synonym.nameString}</td>
                                    <td>Published in: ${synonym.publishedIn}</td>
                                </tr>
                            </c:forEach>
                            <c:forEach items="${extendedTaxonConcept.commonNames}" var="commonName">
                                <tr>
                                    <td class="propertyNames">Common Name</td>
                                    <td>${commonName.nameString}</td>
                                    <td>Source: ${commonName.infoSourceName}</td>
                                </tr>
                            </c:forEach>
                        </table>
                    </div>
                </c:if>
                <c:if test="${not empty extendedTaxonConcept.classification}">
                    <!-- Classification -->
                    <div id="classification">
                    <c:set var="classfn" value="${extendedTaxonConcept.classification}"/>
                    <c:set var="rankId" value="${classfn.rankId}"/>
                        <table class="propertyTable">
                            <tr>
                                <th width="15%"></th>
                                <th width="70%"></th>
                                <th width="15%"></th>
                            </tr>
                        <c:if test="${rankId >= 1000}">
                            <tr>
                                <td>Kingdom
                                <td>
                                    <c:if test="${not empty classfn.kingdom}"><a href="${classfn.kingdomGuid}">${classfn.kingdom}</a></c:if>
                                    <c:if test="${empty classfn.kingdom && classfn.rankId == 1000}"><a href="">${classfn.scientificName}</a></c:if>
                                </td>
                                <td><c:if test="${not empty classfn.infoSourceName && not empty classfn.infoSourceURL}"><a href="${classfn.infoSourceURL}">${classfn.infoSourceName}</a></c:if></td>
                            </tr>
                        </c:if>
                        <c:if test="${rankId >= 2000}">
                            <tr>
                                <td>Phylum
                                <td>
                                    <c:if test="${not empty classfn.phylum}"><a href="${classfn.phylumGuid}">${classfn.phylum}</a></c:if>
                                    <c:if test="${empty classfn.phylum && classfn.rankId == 2000}"><a href="">${classfn.scientificName}</a></c:if>
                                </td>
                                <td><c:if test="${not empty classfn.infoSourceName && not empty classfn.infoSourceURL}"><a href="${classfn.infoSourceURL}">${classfn.infoSourceName}</a></c:if></td>
                            </tr>
                        </c:if>
                        <c:if test="${rankId >= 3000}">
                            <tr>
                                <td>Class</td>
                                <td>
                                    <c:if test="${not empty classfn.clazz}"><a href="${classfn.clazzGuid}">${classfn.clazz}</a></c:if>
                                    <c:if test="${empty classfn.clazz && classfn.rankId == 3000}"><a href="">${classfn.scientificName}</a></c:if>
                                </td>
                                <td><c:if test="${not empty classfn.infoSourceName && not empty classfn.infoSourceURL}"><a href="${classfn.infoSourceURL}">${classfn.infoSourceName}</a></c:if></td>
                            </tr>
                        </c:if>
                        <c:if test="${rankId >= 4000}">
                            <tr>
                                <td>Order</td>
                                <td>
                                    <c:if test="${not empty classfn.order}"><a href="${classfn.orderGuid}">${classfn.order}</a></c:if>
                                    <c:if test="${empty classfn.order && classfn.rankId == 4000}"><a href="">${classfn.scientificName}</a></c:if>
                                </td>
                                <td><c:if test="${not empty classfn.infoSourceName && not empty classfn.infoSourceURL}"><a href="${classfn.infoSourceURL}">${classfn.infoSourceName}</a></c:if></td>
                            </tr>
                        </c:if>
                        <c:if test="${rankId >= 5000}">
                            <tr>
                                <td>Family</td>
                                <td>
                                    <c:if test="${not empty classfn.family}"><a href="${classfn.familyGuid}">${classfn.family}</a></c:if>
                                    <c:if test="${empty classfn.family && classfn.rankId == 5000}"><a href="">${classfn.scientificName}</a></c:if>
                                </td>
                                <td><c:if test="${not empty classfn.infoSourceName && not empty classfn.infoSourceURL}"><a href="${classfn.infoSourceURL}">${classfn.infoSourceName}</a></c:if></td>
                            </tr>
                        </c:if>
                        <c:if test="${rankId >= 6000}">
                            <tr>
                                <td>Genus</td>
                                <td>
                                    <c:if test="${not empty classfn.genus}"><a href="${classfn.genusGuid}">${classfn.genus}</a></c:if>
                                    <c:if test="${empty classfn.genus && classfn.rankId == 6000}"><a href="">${classfn.scientificName}</a></c:if>
                                </td>
                                <td><c:if test="${not empty classfn.infoSourceName && not empty classfn.infoSourceURL}"><a href="${classfn.infoSourceURL}">${classfn.infoSourceName}</a></c:if></td>
                            </tr>
                        </c:if>
                        <c:if test="${rankId >= 7000}">
                            <tr>
                                <td>Species</td>
                                <td>
                                    <c:if test="${not empty classfn.species}"><a href="${classfn.speciesGuid}">${classfn.species}</a></c:if>
                                    <c:if test="${empty classfn.species && classfn.rankId == 7000}"><a href="">${classfn.scientificName}</a></c:if>
                                </td>
                                <td><c:if test="${not empty classfn.infoSourceName && not empty classfn.infoSourceURL}"><a href="${classfn.infoSourceURL}">${classfn.infoSourceName}</a></c:if></td>
                            </tr>
                        </c:if>
                        <c:if test="${rankId >= 8000}">
                            <tr>
                                <td>Subspecies</td>
                                <td>
                                    <c:if test="${not empty classfn.subspecies}"><a href="${classfn.subspeciesGuid}">${classfn.subspecies}</a></c:if>
                                    <c:if test="${empty classfn.subspecies && classfn.rankId == 8000}"><a href="">${classfn.scientificName}</a></c:if>
                                </td>
                                <td><c:if test="${not empty classfn.infoSourceName && not empty classfn.infoSourceURL}"><a href="${classfn.infoSourceURL}">${classfn.infoSourceName}</a></c:if></td>
                            </tr>
                        </c:if>
                        </table>
                    </div>
                </c:if>
                <!--Literature-->
                <c:if test="${not empty extendedTaxonConcept.references}">
                    <div id="literature">
                        <table class="propertyTable">
                            <tr>
                                <th>Scientific Name</th>
                                <th>Reference</th>
                                <th>Source</th>
                            </tr>
                            <c:forEach items="${extendedTaxonConcept.references}" var="reference">
                                <tr>
                                    <td>${reference.scientificName}</td>
                                    <td>${reference.title}</td>
                                    <td><a href="http://www.biodiversitylibrary.org/item/${reference.identifier}" title="view original publication" target="_blank">Biodiversity Heritage Library</a></td>
                                </tr>
                            </c:forEach>
                        </table>
                    </div>
                </c:if>
                <!-- Map -->
                <div id="portalInfo">
                    <div id="left">
                        <p>Species density layer generated from specimen & observation occurrence data</p>
                        <ul>
                            <li>Total occurrences: <span id="occurrenceCount"></span></li>
                            <li><a href="#" id="occurrenceTableLink">View table of all occurrence records
                                    <%--for ${sciNameFormatted}--%></a></li>
                            <%--<li>Total number of records: <span id="occurrenceCount"></span></li>--%>
                            <li>Breakdown by Regions</li>
                            <c:forEach var="regionType" items="${extendedTaxonConcept.regionTypes}">
                                <c:if test="${fn:containsIgnoreCase(regionType.regionType, 'state') || fn:containsIgnoreCase(regionType.regionType, 'territory')}">
                                    <b>${regionType.regionType}</b>
                                    <ul style="list-style-type: circle;">
                                        <c:forEach var="region" items="${regionType.regions}">
                                            <li>${region.name}:
                                                <a href="http://data.ala.org.au/occurrences/searchWithTable.htm?c[0].s=20&c[0].p=0&c[0].o=${region.taxonId}&c[1].s=36&c[1].p=0&c[1].o=${region.regionId}">${region.occurrences}</a>
                                            </li>
                                        </c:forEach>
                                    </ul>
                                </c:if>
                            </c:forEach>
                            <c:forEach var="regionType" items="${extendedTaxonConcept.regionTypes}">
                                <c:if test="${fn:containsIgnoreCase(regionType.regionType, 'ibra') || fn:containsIgnoreCase(regionType.regionType, 'imcra')}">
                                    <b>${regionType.regionType}</b>
                                    <ul style="list-style-type: circle;">
                                        <c:forEach var="region" items="${regionType.regions}">
                                            <li>${region.name}: ${region.occurrences}</li>
                                        </c:forEach>
                                    </ul>
                                </c:if>
                            </c:forEach>
                        </ul>
                    </div>
                    <div id="mappanel" style="display: block;"></div>
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

                <c:if test="${fn:length(extendedTaxonConcept.images) > 0}">
                    <div id="images">
                        <h4 class="divider">Images<a name="images">&nbsp;</a></h4>
                        <div id="photos" class="galleryview">
                        <c:forEach var="image" items="${extendedTaxonConcept.images}" varStatus="status">
                            <div class="panel" style="text-align: center;">
                                <a href="${image.identifier}" title="View original image" target="_blank">
                                    <img src="http://${pageContext.request.serverName}:80${fn:replace(image.repoLocation, "/data/bie", "/repository")}" class="galleryImage"/>
                                </a>
                                <div class="panel-overlay">
                                    <c:set var="title">
                                        <c:if test="${fn:length(image.title) > 0}">${image.title}</c:if>
                                        <c:if test="${fn:length(image.title) < 1}">[no title]</c:if>
                                    </c:set>
                                        Image ${status.count}: <a href="${image.identifier}" target="_blank">${title}</a>
                                    <br/>
                                    Source: <a href="${image.infoSourceURL}" target="_blank">${image.infoSourceName}</a>
                                </div>
                            </div>
                        </c:forEach>
                          <ul class="filmstrip">
                              <c:forEach var="image" items="${extendedTaxonConcept.images}">
                                   <li><img src="${pageContext.request.contextPath}/species/images/${image.documentId}.jpg" alt="${image.infoSourceName}" title="${image.infoSourceName}" /></li>
                              </c:forEach>
                          </ul>
                        </div>

                    </div>
                </c:if>
            </div>
        </div>
        <p id="debug" style="display: none;"></p>
    </body>
</html>
