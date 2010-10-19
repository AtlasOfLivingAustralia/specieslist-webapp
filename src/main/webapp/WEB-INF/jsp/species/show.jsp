<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>
<!DOCTYPE HTML">
<c:set var="spatialPortalUrl">${initParam.centralServer}/explore/species-maps/</c:set>
<c:set var="spatialPortalWMSUrl">http://spatial.ala.org.au/alaspatial/</c:set>
<c:set var="wordPressUrl">${initParam.centralServer}</c:set>
<c:set var="biocacheUrl">http://biocache.ala.org.au/</c:set>
<c:set var="collectoryUrl">http://collections.ala.org.au</c:set>
<html>
    <head>
        <meta name="pageName" content="species" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>${extendedTaxonConcept.taxonConcept.nameString} <c:if test="${not empty extendedTaxonConcept.commonNames}">(${extendedTaxonConcept.commonNames[0].nameString})</c:if> | Atlas of Living Australia</title>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/static/js/jquery-fancybox/jquery.fancybox-1.3.1.css" media="screen" />
        <link type="text/css" media="screen" rel="stylesheet" href="${pageContext.request.contextPath}/static/css/colorbox.css" />
        <script language="JavaScript" type="text/javascript" src="${wordPressUrl}/wp-content/themes/ala/scripts/ui.core.js"></script>
        <script language="JavaScript" type="text/javascript" src="${wordPressUrl}/wp-content/themes/ala/scripts/ui.tabs.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery-fancybox/jquery.fancybox-1.3.1.pack.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery.colorbox.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery.easing.1.3.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery.favoriteIcon.js"></script>
        <script type="text/javascript" src="http://www.google.com/jsapi"></script>
        <script type="text/javascript">

            google.load("visualization", "1", {packages:["corechart"]});
            
            /*
             * OnLoad equivilent in JQuery
             */
            $(document).ready(function() {

                // LSID link to show popup with LSID info and links
                $("a#lsid").fancybox({
                    'hideOnContentClick' : false,
                    'titleShow' : false,
                    'autoDimensions' : false,
                    'width' : 600,
                    'height' : 180
                });
                
                $("a.contributeLink").fancybox({
                    'hideOnContentClick' : false,
                    'titleShow' : false,
                    'autoDimensions' : false,
                    'width' : 680,
                    'height' : 210
                });

                // Dena's tabs implementation
                $('#nav-tabs > ul').tabs();
                //$('#nav-tabs > ul').bind("tabsshow", function(event, ui) {
                //    window.location.hash = ui.tab.hash;
                //})
                // Display full image when thumbnails are clicked
                function formatTitle(title, currentArray, currentIndex, currentOpts) {
                    return '<div id="tip7-title"><span></span>' +
                        (title && title.length ? '<b>' + title + '</b>' : '' ) + '<br/>Image ' + (currentIndex + 1) + ' of ' + currentArray.length + '</div>';
                }

                // Gallery image popups using ColorBox
                $("a.thumbImage").colorbox({
                    title: function() {
                        var titleBits = this.title.split("|");
                        return "<a href='"+titleBits[1]+"'>"+titleBits[0]+"</a>"; },
                    opacity: 0.5,
                    maxWidth: "80%",
                    maxHeight: "80%",
                    onComplete: function() {
                        $("#cboxTitle").html(""); // Clear default title div
                        var index = $(this).attr('id').replace("thumb",""); // get the imdex of this image
                        var titleHtml = $("div#thumbDiv"+index).html(); // use index to load meta data
                        $("<div id='titleText'>"+titleHtml+"</div>").insertAfter("#cboxPhoto");
                        $("div#titleText").css("padding-top","8px");
                        $.fn.colorbox.resize();
                    }
                });

                // images in overview tab should trigger lightbox
                $("#images ul a").click(function(e) {
                    e.preventDefault(); //Cancel the link behavior
                    //$('#nav-tabs > ul').tabs( "select" , 1 );
                    var thumbId = "thumb" + $(this).attr('href');
                    $("a#"+thumbId).click();  // simulate clicking the lightbox links in Gallery tab
                });

                // Check for valid distribution map img URLs (Hacked for IE)
                $('img.distroImg').each(function(i, n) {
                    // if img doesn't load, then hide its surround div
                    $(this).error(function() {
                        //alert("img error");
                        $(this).parent().parent().hide();
                    });
                    // IE hack as IE doesn't trigger the error handler
                    if ($.browser.msie && !n.complete) {
                        //alert("IE img error");
                        $(this).parent().parent().hide();
                    }
                });
                
                // mapping for facet names to display labels
                var facetLabels = {
                    state: "State &amp; Territory",
                    data_resource: "Dataset",
                    month: "Date (by month)", 
                    occurrence_date: "Date (by decade)"
                };
                var months = {
                    "01": "January",
                    "02": "February",
                    "03": "March",
                    "04": "April",
                    "05": "May",
                    "06": "June",
                    "07": "July",
                    "08": "August",
                    "09": "September",
                    "10": "October",
                    "11": "November",
                    "12": "December"
                };
                
                // load the collections that contain specimens
                var colSpecUrl = "${pageContext.request.contextPath}/species/source/${extendedTaxonConcept.taxonConcept.guid}"; 
                $.getJSON(colSpecUrl, function(data) {
                    if (data != null &&data.occurrenceSources != null && data.occurrenceSources.length >0){
                        var content = '<h4>Collections that hold specimens: </h4>';
                        content = content +'<ul>';
                        $.each(data.occurrenceSources, function(i, li) {
                            if(li.uid.match("^co")=="co"){
                                var link1 = '<a href="${collectoryUrl}/public/show/' + li.uid +'">' + li.name + '</a>';
                                var link2 = '(<a href="${biocacheUrl}/occurrences/searchByTaxon?q=${extendedTaxonConcept.taxonConcept.guid}&fq=collection_code_uid:'
                                link2 = link2 + li.uid +'&fq=basis_of_record:specimen">' + li.count + ' records</a>)';
                                content = content+'<li>' + link1 + ' ' + link2+'</li>';

                            }
                        });
                        content = content + '</ul>';
                        $('#recordBreakdowns').append(content);
                    }
                });

                // load occurrence breakdowns for states
                var biocachUrl = "${pageContext.request.contextPath}/species/charts/${extendedTaxonConcept.taxonConcept.guid}";
                $.getJSON(biocachUrl, function(data) {
                    if (data.searchResult != null && data.searchResult.totalRecords > 0) {
                        //alert("hi "+data.searchResult.totalRecords);
                        var count = data.searchResult.totalRecords + ""; // concat of emtyp string forces var to a String
                        $('#occurenceCount').html(count.replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "$1,")); // update link text at top with count (formatted)
                        //console.log('facets: ', data.searchResult.facetResults);
                        var facets = data.searchResult.facetResults;
                        $.each(facets, function(index, facet) {
                            //console.log(node.fieldName, node.fieldResult);
                            //if (node.fieldName == 'state' || node.fieldName == 'state' ||node.fieldName == 'state') {
                            if (facet.fieldName in facetLabels) {
                                // dataTable for chart
                                var data = new google.visualization.DataTable();
                                var chart;
                                data.addColumn('string', facetLabels[facet.fieldName]);
                                data.addColumn('number', 'Records');
                                // HTML content 
                                var isoDateSuffix = '-01-01T12:00:00Z';
                                var content = '<h4>By '+ facetLabels[facet.fieldName] +'</h4>';
                                content = content +'<ul>';
                                $.each(facet.fieldResult, function(i, li) {
                                    if (li.count > 0) {
                                        var label = li.fieldValue;
                                        var displayCount = (li.count + "").replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "$1,");
                                        var link = '<a href="${biocacheUrl}occurrences/searchByTaxon?q=${extendedTaxonConcept.taxonConcept.guid}&fq='
                                        if (facet.fieldName == 'occurrence_date') {
                                            label = label.replace(isoDateSuffix, '');
                                            var toValue = parseInt(label) + 10;
                                            label = label + '-' + toValue;
                                            toValue = toValue + isoDateSuffix;
                                            link = link + facet.fieldName+':['+li.fieldValue+' TO '+toValue+']">';
                                        } else if (facet.fieldName == 'month') {
                                            link = link + facet.fieldName+':'+li.fieldValue+'">';
                                            label = months[label]; // substitiute month name for int value
                                        } else {
                                            link = link + facet.fieldName+':'+li.fieldValue+'">';
                                        }
                                        content = content +'<li>'+label+': ' + link + displayCount + ' records</a></li>';
                                        // add values to chart
                                        data.addRow([label, li.count]);
                                    }
                                });
                                content = content + '</ul><div id="'+facet.fieldName+'_chart_div" style="margin: -15px;"></div>';
                                $('#recordBreakdowns').append(content); 
                                
                                if (facet.fieldName == 'occurrence_date' || facet.fieldName == 'month') {
                                    var dateLabel = (facet.fieldName == 'occurrence_date') ? 'Decade' : 'Month';
                                    chart = new google.visualization.BarChart(document.getElementById(facet.fieldName+'_chart_div'));
                                    chart.draw(data, {width: 630, height: 300, legend: 'none', vAxis: {title: dateLabel}, hAxis: {title: 'Count'}});
                                } else {
                                    chart = new google.visualization.PieChart(document.getElementById(facet.fieldName+'_chart_div'));
                                    chart.draw(data, {width: 630, height: 300, legend: 'left'});
                                }
                            }
                        });
                    } else {
                        // hide the occurrence record section if no data or biocache is offline
                        $('#occurrenceRecords').html("No records found");
                    }
                });

                // Adds an icon (favicon taken from URL) for a given link
                $("#onlineResources a.infosource").favoriteIcon({
                    iconClass : 'favoriteIcon',
                    insertMethod: 'insertBefore',
                    missingImgUrl: ''
                });

                // change body id for Dena's custom CSS
                $("body").attr("id","taxon");

                //Switch the "Open" and "Close" state per click then slide up/down (depending on open/close state)
                $('p.trigger').click(function(){
                    $(this).toggleClass('active').prev().toggleClass('full');
                    //$(this).toggleClass('active').next().toggleClass('full').slideToggle('slow');
                });

                var statusIconsCount = $('div.toggle div#status div').length;
                //alert("Number of status icons: "+statusIconsCount);
                if (statusIconsCount > 4) {
                    $('div.status').css('height','18em');
                } else if (statusIconsCount > 8) {
                    $('div.status').css('height','28em');
                } else if (statusIconsCount > 12) {
                    $('p.trigger').show();
                }

                // Add the class 'last' to every 4th status div
                $('div.toggle div#status div').each(function(i, el) {
                    if ((i+1) % 4 == 0) {
                        //console.log("4th div? ", i);
                        $(this).addClass("last");
                    }
                });

                // truncate text to 250 chars
                var limit = 250;
                $('span.truncate').each(function(i, el) {
                    var length = $(this).html().length;
                    //console.log("truncate length " + i, length);
                    if (length > limit) {
                        var html = $(this).html().substring(0,limit);
                        $(this).html(html).append('...');
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
        <link rel="stylesheet" type="text/css" href="${wordPressUrl}/wp-content/themes/ala/css/speciesPage.css" media="screen" />
    </head>
    <body id="taxon">
        <div id="header" class="taxon">
            <c:choose>
            <c:when test="${not empty extendedTaxonConcept.taxonName && not empty extendedTaxonConcept.taxonName.nameComplete}">
	            <c:set var="sciNameFormatted">
	                <alatag:formatSciName name="${extendedTaxonConcept.taxonName.nameComplete}" rankId="${extendedTaxonConcept.taxonConcept.rankID}"/>
	            </c:set>
            </c:when>
            <c:otherwise>
	            <c:set var="sciNameFormatted">
	                <alatag:formatSciName name="${extendedTaxonConcept.taxonConcept.nameString}" rankId="${extendedTaxonConcept.taxonConcept.rankID}"/>
	            </c:set>           
            </c:otherwise>
            </c:choose>
            <c:set var="contributeURL" value="${biocacheUrl}share/sighting/${extendedTaxonConcept.taxonConcept.guid}"/>
            <div id="breadcrumb">
                <ul>
                    <li><a href="${wordPressUrl}">Home</a></li>
                    <li><a href="${pageContext.request.contextPath}/species/search">Species</a></li>
                    <li>${sciNameFormatted} <c:if test="${not empty extendedTaxonConcept.commonNames}">(${extendedTaxonConcept.commonNames[0].nameString})</c:if></li>
                </ul>
            </div>
            <div class="section full-width">
                <div class="container2"> 
                    <div class="container1"> 
                        <div class="hrgroup"> 
                            <h1>${sciNameFormatted} <span>${extendedTaxonConcept.taxonConcept.author}</span></h1>
                            <h2>
                                <c:choose>
                                    <c:when test="${extendedTaxonConcept.taxonConcept.rankID>5000}">
                                            ${extendedTaxonConcept.commonNames[0].nameString}
                                    </c:when>
                                    <c:otherwise>
                                            <c:forEach items="${extendedTaxonConcept.commonNames}" var="commonName">
                                                    ${commonName.nameString}
                                            </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </h2> 
                        </div> 
                        <div class="meta"> 
                            <h3>Rank</h3><p style="text-transform: capitalize;">${extendedTaxonConcept.taxonConcept.rankString}</p>
                            <h3>Name source</h3><p><a href="${extendedTaxonConcept.taxonConcept.infoSourceURL}" target="_blank" class="external">${extendedTaxonConcept.taxonConcept.infoSourceName}</a></p>
                            <h3>Data links</h3><p><a href="#lsidText" id="lsid" class="local" title="Life Science Identifier (pop-up)">LSID</a>
                                | <a href="${pageContext.request.contextPath}/species/${extendedTaxonConcept.taxonConcept.guid}.json" class="local" title="JSON web service">JSON</a>
                                <!-- | <a href="${pageContext.request.contextPath}/species/${extendedTaxonConcept.taxonConcept.guid}.xml" class="local" title="XML web service">XML</a> -->
                            </p>
                            <div style="display:none; text-align: left;">
                                <div id="lsidText" style="text-align: left;">
                                    <b><a href="http://lsids.sourceforge.net/" target="_blank">Life Science Identifier (LSID):</a></b>
                                    <p style="margin: 10px 0;"><a href="http://lsid.tdwg.org/summary/${extendedTaxonConcept.taxonConcept.guid}" target="_blank">${extendedTaxonConcept.taxonConcept.guid}</a></p>
                                    <p style="font-size: 12px;">LSIDs are persistent, location-independent,resource identifiers for uniquely naming biologically
                                        significant resources including species names, concepts, occurrences, genes or proteins,
                                        or data objects that encode information about them. To put it simply,
                                        LSIDs are a way to identify and locate pieces of biological information on the web. </p>
                                </div>
                            </div>
                        </div> 
                    </div> 
                </div>
            </div>
            <div id="taxacrumb">
                <ul>
                    <c:forEach items="${taxonHierarchy}" var="taxon">
                        <c:if test="${taxon.rankId % 1000 == 0}"><%-- Only display major ranks. NdR --%>
                            <li>
                                <c:if test="${taxon.guid != extendedTaxonConcept.taxonConcept.guid}">
                                    <a href="<c:url value='/species/${taxon.guid}'/>" title="${taxon.rank}">
                                </c:if>
                                <c:if test="${taxon.rankId>=6000}"><i></c:if>${taxon.name}<c:if test="${taxon.rankId>=6000}"></i></c:if>
                                <c:if test="${taxon.guid != extendedTaxonConcept.taxonConcept.guid}"></a>
                                </c:if>
                            </li>
                        </c:if>
                    </c:forEach>
                </ul>
            </div>
            <div id="nav-tabs">
                <ul>
                    <li><a href="#overview">Overview</a></li>
                    <li><a href="#gallery">Gallery</a></li>
                    <%--<li><a href="#identification">Identification</a></li>--%>
                    <li><a href="#names">Names</a></li>
                    <li><a href="#classification">Classification</a></li>
                    <li><a href="#records">Records</a></li>
                    <%--<li><a href="#biology">Biology</a></li>
                    <li><a href="#molecular">Molecular</a></li>--%>
                    <li><a href="#literature">Literature</a></li>
                </ul>
            </div>
        </div><!--close section_page-->
        <div id="overview">
            <div id="column-one">
                <div class="section">
                    <h2 style="text-transform: capitalize;">${extendedTaxonConcept.taxonConcept.rankString} overview</h2>
                    <div class="distroMap section no-margin">
                        <h3>Mapped occurrence records</h3>
                        <p>
                            <a href="${biocacheUrl}occurrences/searchByTaxon?q=${extendedTaxonConcept.taxonConcept.guid}">View occurrence records list</a>
                            | <a href="${spatialPortalUrl}?species_lsid=${extendedTaxonConcept.taxonConcept.guid}" title="View interactive map">View interactive map</a>
                        </p>
                        <div class="left">
                            <img src="${spatialPortalWMSUrl}ws/density/map?species_lsid=${extendedTaxonConcept.taxonConcept.guid}" class="distroImg" width="462" alt=""/>
                        </div>
                        <div class="right" style="width:149px; font-size: 90%">
                            <table>
                                <caption>Number of records</caption>
                                <tr>
                                    <td style="background-color:#ffff00;color:#ffff00;">yel</td>
                                    <td>1&ndash;9</td>
                                </tr>
                                <tr>
                                    <td style="background-color:#ffcc00;color:#ffcc00;">yel or</td>
                                    <td>10&ndash;49</td>
                                </tr>
                                <tr>
                                    <td style="background-color:#ff9900;color:#ff9900;">or</td>
                                    <td>50&ndash;99</td>
                                </tr>
                                <tr>
                                    <td style="background-color:#ff6600;color:#ff6600;">or red</td>
                                    <td>100&ndash;249</td>
                                </tr>
                                <tr>
                                    <td style="background-color:#ff3300;color:#ff3300;">lt red</td>
                                    <td>250&ndash;499</td>
                                </tr>
                                <tr>
                                    <td style="background-color:#cc0000;color:#cc0000;">red</td>
                                    <td>500+</td>
                                </tr>
                                <tr>
                            </table>
                        </div>
                    </div>
                    <c:set var="descriptionBlock">
                        <c:forEach var="textProperty" items="${textProperties}" varStatus="status">
                            <c:if test="${fn:endsWith(textProperty.name, 'hasDescriptiveText') && status.count < 3}">
                                <p>${textProperty.value} <cite>source: <a href="${textProperty.identifier}" target="_blank" title="${textProperty.title}">${textProperty.infoSourceName}</a></cite></p>
                            </c:if>
                        </c:forEach>
                    </c:set>
                    <c:if test="${not empty descriptionBlock}">
                        <h3>Description</h3>
                        ${descriptionBlock}
                    </c:if>
                    <c:if test="${not empty extendedTaxonConcept.identificationKeys}">
                        <h3>Identification Keys</h3>
                        <ul>
                            <c:forEach var="idKey" items="${extendedTaxonConcept.identificationKeys}">
                                <li>
                                    <a href="${idKey.url}" target="_blank">${idKey.title}</a>
                                    <c:if test="${not empty idKey.infoSourceURL}">(source: <a href="${idKey.infoSourceURL}" target="_blank">${idKey.infoSourceName}</a>)</c:if>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:if>
                    <h2>Online resources</h2>
                    <table cellpadding="0" cellspacing="0" id="onlineResources">
                        <colgroup style="width:50%;"></colgroup> 
                        <colgroup></colgroup> 
                        <tbody> 
                            <c:forEach var="entry" items="${infoSources}" varStatus="status">
                                <c:set var="infoSource" value="${entry.value}"/>
                                <tr class="border-top">
                                    <td><a href="${infoSource.infoSourceURL}" target="_blank" class="infosource">${infoSource.infoSourceName}</a><!--${status.count}--></td>
                                    <td class="small-font">
                                        <c:forEach items="${infoSource.sections}" var="section" varStatus="s">
                                            <fmt:message key="${section}"/><c:if test="${!s.last}">,</c:if>
                                        </c:forEach>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="2"><p><span class="truncate">${infoSource.text}</span> <a href="${infoSource.infoSourceURL}" target="_blank">more</a></p></td>
                                </tr>
                            </c:forEach>
                        </tbody> 
                    </table> 
                    
                    <c:if test="${empty textProperties}">
                        <div class="sorry sighting no-margin-top">
                            <div>
                                <h2>Sorry!</h2>
                                <h3><a href="#contributeOverlay" class="contributeLink">We know the name, but not much else. Can you help?
                                    <span><b>Share</b> sightings, photos and data for 
                                        <c:choose>
                                            <c:when test="${not empty extendedTaxonConcept.commonNames}">the <strong>${extendedTaxonConcept.commonNames[0].nameString}</strong></c:when>
                                            <c:otherwise><c:if test="${extendedTaxonConcept.taxonConcept.rankID <= 6000}">the ${extendedTaxonConcept.taxonConcept.rankString} </c:if><strong>${sciNameFormatted}</strong></c:otherwise>
                                        </c:choose>
                                    </span></a>
                                </h3>
                            </div>
                        </div> 
                    </c:if>
                </div>
            </div><!---->
            <div id="column-two">
                <div class="toggle section half-padding-bottom"> 
                    <div id="status"class="status">
                        <c:if test="${extendedTaxonConcept.taxonConcept.rankID >= 7000}">
                            <c:choose>
                                <c:when test="${extendedTaxonConcept.isAustralian}">
                                    <div>Australia<span class="iucn native">&nbsp;</span>Recorded In</div>
                                </c:when>
                                <c:otherwise>
                                    <div>Australia<span class="iucn nonnative">&nbsp;</span>Not Recorded In</div>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <c:forEach var="status" items="${extendedTaxonConcept.conservationStatuses}">
                            <c:if test="${fn:containsIgnoreCase(status.status,'extinct') || fn:containsIgnoreCase(status.status,'endangered') || fn:containsIgnoreCase(status.status,'vulnerable') || fn:containsIgnoreCase(status.status,'threatened') || fn:containsIgnoreCase(status.status,'concern') || fn:containsIgnoreCase(status.status,'deficient')}">
                                <div><fmt:message key="region.${status.region}"/><a href="${status.infoSourceURL}" title="${status.infoSourceName}" target="_blank">
                                    <c:choose>
                                        <c:when test="${fn:endsWith(status.status,'Extinct')}"><span class="iucn red"><!--EX--></span></c:when>
                                        <c:when test="${fn:containsIgnoreCase(status.status,'wild')}"><span class="iucn red"><!--EW--></span></c:when>
                                        <c:when test="${fn:containsIgnoreCase(status.status,'Critically')}"><span class="iucn yellow"><!--CR--></span></c:when>
                                        <c:when test="${fn:startsWith(status.status,'Endangered')}"><span class="iucn yellow"><!--EN--></span></c:when>
                                        <c:when test="${fn:containsIgnoreCase(status.status,'Vulnerable')}"><span class="iucn yellow"><!--VU--></span></c:when>
                                        <c:when test="${fn:containsIgnoreCase(status.status,'Near')}"><span class="iucn green"><!--NT--></span></c:when>
                                        <c:when test="${fn:containsIgnoreCase(status.status,'concern')}"><span class="iucn green"><!--LC--></span></c:when>
                                    </c:choose>
                                    ${status.rawStatus}</a>
                                </div>
                            </c:if>
                        </c:forEach>
                        
                        <c:forEach var="habitat" items="${extendedTaxonConcept.habitats}">
                            <c:set var="divMarine">
                                <div>Habitat <a href="${habitat.infoSourceURL}" title="${habitat.infoSourceName}" target="_blank"><span class="iucn marine">&nbsp;</span> Marine</a></div>
                            </c:set>
                            <c:set var="divTerrestrial">
                                <div>Habitat <a href="${habitat.infoSourceURL}" title="${habitat.infoSourceName}" target="_blank"><span class="iucn terrestrial">&nbsp;</span> Terrestrial</a></div>
                            </c:set>
                            <c:choose>
                                <c:when test="${habitat.status == 'M'}">${divMarine}</c:when>
                                <c:when test="${habitat.status == 'N'}">${divTerrestrial}</c:when>
                                <c:otherwise>${divMarine} ${divTerrestrial}</c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </div> 
                    <p class="trigger left no-padding-bottom" style="display: none"><a href="#">View all statuses</a></p>
                </div>
                <div id="images" class="section">
                    <ul>
                        <c:choose>
                            <c:when test="${not empty extendedTaxonConcept.taxonConcept.rankID && extendedTaxonConcept.taxonConcept.rankID < 7000}">
                                <c:set var="imageLimit" value="6"/>
                                <c:set var="imageSize" value="150"/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="imageLimit" value="1"/>
                                <c:set var="imageSize" value="314"/>
                            </c:otherwise>
                        </c:choose>
                        <c:forEach var="image" items="${extendedTaxonConcept.images}" varStatus="status">
                            <c:if test="${status.index < imageLimit}">
                                <li><a href="${status.index}" title=""><img src="${image.repoLocation}" style="max-width: ${imageSize}px" alt="" /></a></li>
                            </c:if>
                        </c:forEach>
                    </ul>
                </div>
                <c:if test="${not empty textProperties}">
                    <div class="section buttons sighting no-margin-top">
                        <div class="last">
                            <h3>
                                <a href="#contributeOverlay" class="contributeLink">Share <span>Sightings, photos and data for
                                    <c:choose>
                                        <c:when test="${not empty extendedTaxonConcept.commonNames}">
                                            the <strong>${extendedTaxonConcept.commonNames[0].nameString}</strong>
                                        </c:when>
                                        <c:otherwise>
                                            <c:if test="${extendedTaxonConcept.taxonConcept.rankID <= 6000}">the ${extendedTaxonConcept.taxonConcept.rankString} </c:if><strong>${extendedTaxonConcept.taxonConcept.nameString}</strong>
                                        </c:otherwise>
                                    </c:choose></span>
                                </a>
                            </h3>
                        </div>
                    </div>
                </c:if>
                <div class="section">
                </div><!--close tools-->
            </div><!--close -->
        </div><!--close overview-->
        <div id="gallery">
            <div id="column-one">
                <div class="section">
                    <h2>Images</h2>
                    <div id="imageGallery">
                    	<script type="text/javascript">
                    		function rankThisImage(guid, uri, infosourceId, documentId, positive, name){
                    			 var url = "${pageContext.request.contextPath}/rankTaxonImage?guid="+guid+"&uri="+uri+"&infosourceId="+infosourceId+"&positive="+positive+"&name="+name;
                    			 //alert(url);
                    			 $('.imageRank-'+documentId).html('Sending your ranking....');
				                 $.getJSON(url, function(data){
				                	 $('.imageRank-'+documentId).each(function(index) {
				                	 	//alert(this);
				                	    //alert(index);
								    	$(this).html('Thanks for your help!');
  			                	        //alert(index+ ' set' );
									  });
				                 	//$('.imageRank-'+documentId).html('Thanks for your help!');
				                 });
	                    		}
                    	</script>
                        <c:choose>
                            <c:when test="${not empty extendedTaxonConcept.images}">
                                <c:forEach var="image" items="${extendedTaxonConcept.images}" varStatus="status">
                                    <c:set var="thumbUri">${image.thumbnail}</c:set>
                                    <a class="thumbImage" rel="thumbs" title="${image.title}" href="${image.repoLocation}" id="thumb${status.index}"><img src="${thumbUri}" alt="${image.infoSourceName}" title="${imageTitle}" width="100px" height="100px" style="width:100px;height:100px;padding-right:3px;"/></a>
                                    <div id="thumbDiv${status.index}" style="display:none;">
                                        <c:if test="${not empty image.title}">
                                            ${image.title}<br/>
                                        </c:if>
                                        <c:if test="${not empty image.creator}">
                                            Image by: ${image.creator}<br/>
                                        </c:if>
                                        <c:if test="${not empty image.locality}">
                                            Locality: ${image.locality}<br/>
                                        </c:if>
                                        <c:if test="${not empty image.licence}">
                                            Licence: ${image.licence}<br/>
                                        </c:if>
                                        <c:if test="${not empty image.rights}">
                                            Rights: ${image.rights}<br/>
                                        </c:if>
                                        <c:set var="imageUri">
                                            <c:choose>
                                                <c:when test="${not empty image.isPartOf}">
                                                    ${image.isPartOf}
                                                </c:when>
                                                <c:when test="${not empty image.identifier}">
                                                    ${image.identifier}
                                                </c:when>
                                                <c:otherwise>
                                                    ${image.infoSourceURL}
                                                </c:otherwise>
                                            </c:choose>
                                        </c:set>
                                        <cite>Source: <a href="${imageUri}" target="_blank">${image.infoSourceName}</a></cite>
                                     	<p class="imageRank-${image.documentId}">
                                        <c:choose>
	                                        <c:when test="${fn:contains(rankedImageUris,image.identifier)}">
    	                                    	You have ranked this image as 
    	                                    		<c:if test="${!rankedImageUriMap[image.identifier]}">
    	                                    			NOT
    	                                    		</c:if>
  	                                    			representative of ${extendedTaxonConcept.taxonConcept.nameString}
        	                                </c:when>
            	                            <c:otherwise>
            	                            	Is this image representative of ${extendedTaxonConcept.taxonConcept.rankString} ?  
   	            	                           <a class="isrepresent" href="javascript:rankThisImage('${extendedTaxonConcept.taxonConcept.guid}','${image.identifier}','${image.infoSourceId}','${image.documentId}',true,'${extendedTaxonConcept.taxonConcept.nameString}');"> 
   	            	                           	  YES
   	            	                           </a>
   	            	                           	 |
   	            	                           <a class="isnotrepresent" href="javascript:rankThisImage('${extendedTaxonConcept.taxonConcept.guid}','${image.identifier}','${image.infoSourceId}','${image.documentId}',false,'${extendedTaxonConcept.taxonConcept.nameString}');"> 
   	            	                           	  NO
   	            	                           </a>
                            	            </c:otherwise>
                                	        </c:choose>   
                                	       </p> 
                                        </div>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                There are no images for this taxon.
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div><!---->
            <div id="column-two">
                <div class="section buttons sighting no-margin-top">
                    <div class="last">
                        <h3><a href="#contributeOverlay" class="contributeLink">Share <span>Sightings, photos and data for the
                        	<strong>
                        		<c:choose>
                        		<c:when test="${not empty extendedTaxonConcept.commonNames}">
                        			${extendedTaxonConcept.commonNames[0].nameString}
                        		</c:when>
                        		<c:otherwise>
                        			${extendedTaxonConcept.taxonConcept.nameString}
                        		</c:otherwise>
                        		</c:choose>
                        	</strong>
                        	</span>
                       	 </a>
                        </h3>
                    </div>
                </div>
                <div class="section">
                    <h2></h2>
                </div><!--close-->
            </div><!--close -->
        </div><!--close multimedia-->
        <%--<div id="identification">
            <div id="column-one">
                <div class="section">
                    <h2>Identification</h2>
                    <h3>Description</h3>
                    <c:forEach var="textProperty" items="${textProperties}" varStatus="status">
                        <c:set var="sectionName" value="${fn:substringAfter(textProperty.name, '#')}"/><!-- ${sectionName} -->
                        <c:if test="${fn:containsIgnoreCase(sectionName, 'descriptive')}">
                            <p>${textProperty.value}
                                <cite>source: <a href="${textProperty.identifier}" target="_blank" title="${textProperty.title}">${textProperty.infoSourceName}</a></cite>
                            </p>
                        </c:if>
                    </c:forEach>
                </div><!--close section-->
            </div><!---->
            <div id="column-two">
                <div class="section tools">
                    <h3 class="contribute">Contribute</h3>
                    <ul>
                        <li><a href="">Images</a></li>
                        <li><a href="">Data</a></li>
                        <li><a href="">Links</a></li>
                    </ul>
                </div><!--close tools-->
                <div class="section">
                    <h2></h2>
                </div><!--close-->
            </div><!--close -->
        </div><!--close identification-->--%>
        <div id="names">
            <div id="column-one">
                <div class="section">
                    <h2>Accepted Name</h2>
                    <p>${sciNameFormatted} ${extendedTaxonConcept.taxonConcept.author}
                        <cite>Source: <a href="${extendedTaxonConcept.taxonConcept.infoSourceURL}" target="blank">${extendedTaxonConcept.taxonConcept.infoSourceName}</a></cite>
                        <c:if test="${not empty extendedTaxonConcept.taxonName.publishedIn}"><cite>Published in: <a href="#">${extendedTaxonConcept.taxonName.publishedIn}</a></cite></c:if>
                    </p>
                    <c:if test="${not empty extendedTaxonConcept.synonyms}">
                        <h2>Synonyms</h2>
                    </c:if>
                    <c:forEach items="${extendedTaxonConcept.synonyms}" var="synonym">
                        <p><alatag:formatSciName name="${synonym.nameString}" rankId="${extendedTaxonConcept.taxonConcept.rankID}"/> ${synonym.author}
                            <c:choose>
                                <c:when test="${empty synonym.infoSourceURL}"><cite>Source: <a href="${extendedTaxonConcept.taxonConcept.infoSourceURL}" target="blank">${extendedTaxonConcept.taxonConcept.infoSourceName}</a></cite></c:when>
                                <c:otherwise><cite>Source: <a href="${synonym.infoSourceURL}" target="blank">${synonym.infoSourceName}</a></cite></c:otherwise>
                            </c:choose>
                            <c:if test="${not empty synonym.publishedIn}"><cite>Published in: <span class="publishedIn">${synonym.publishedIn}</span></cite></c:if>
                        </p>
                    </c:forEach>
                    <c:if test="${not empty extendedTaxonConcept.commonNames}">
                        <h2>Common Names</h2>
                    </c:if>
                    <c:forEach items="${sortCommonNameKeys}" var="nkey">
                    	<p>${nkey}
	                     	<c:forEach items="${sortCommonNameSources[nkey]}" var="commonName">
	                            <c:choose>
	                                <c:when test="${not empty commonName.identifier && not empty commonName.infoSourceName}"><cite>Source: <a href="${commonName.identifier}" target="blank">${commonName.infoSourceName}</a></cite></c:when>
	                                <c:otherwise><cite>Source: <a href="${commonName.infoSourceURL}" target="blank">${commonName.infoSourceName}</a></cite></c:otherwise>
	                            </c:choose>
	                            <c:if test="${not empty synonym.publishedIn}"><cite>Published in: <span class="publishedIn">${synonym.publishedIn}</span></cite></c:if>
	                     	</c:forEach>
                    	</p>
                    </c:forEach>                   
                </div>
            </div><!---->
            <div id="column-two">
                <div class="section">
                </div>
            </div><!--close -->
        </div><!--close names-->
        <div id="classification">
            <div id="column-one">
                <div class="section">
                    <h2>Scientific Classification</h2>
                    <div id="classificationList">
                    	<c:forEach items="${taxonHierarchy}" var="taxon">
                            <ul>
                               	<c:choose>
                                    <c:when test="${taxon.guid != extendedTaxonConcept.taxonConcept.guid}">
                                        <li>${taxon.rank}: <a href="<c:url value='/species/${taxon.guid}#classification'/>" title="${taxon.rank}">
                                            <alatag:formatSciName name="${taxon.name}" rankId="${taxon.rankId}"/>
                                            <c:if test="${not empty taxon.commonNameSingle && taxon.guid == extendedTaxonConcept.taxonConcept.guid}">
                                                (${taxon.commonNameSingle})
                                            </c:if>
                                        </a></li>
                                    </c:when>
                                    <c:otherwise>
                                        <li id="currentTaxonConcept">${taxon.rank}: <span><alatag:formatSciName name="${taxon.name}" rankId="${taxon.rankId}"/>
                                        <c:if test="${not empty taxon.commonNameSingle && taxon.guid == extendedTaxonConcept.taxonConcept.guid}">
                                                (${taxon.commonNameSingle})
                                            </c:if></span></li>
                                    </c:otherwise>
                                </c:choose>
                    	</c:forEach>
                        <ul>
                            <c:forEach items="${childConcepts}" var="child">
                                <li>${child.rank}:
                                	<a href="<c:url value='/species/${child.guid}#classification'/>">
                                	<c:if test="${child.rankId>=6000}"><i></c:if>
                                    	${child.name}
                                	<c:if test="${child.rankId>=6000}"></i></c:if>
                                    <c:if test="${not empty child.commonNameSingle}">
                                     	(${child.commonNameSingle})
                                    </c:if>
                                    </a>
                                </li>
                            </c:forEach>
                        </ul>
                        <c:forEach items="${taxonHierarchy}" var="taxon">
                            </ul>
                        </c:forEach>
                    </div>
                    <%-- <c:if test="${fn:length(extendedTaxonConcept.parentConcepts) > 0}">
                        <h5>Parent <c:if test="${fn:length(extendedTaxonConcept.parentConcepts) > 1}">Taxa</c:if>
                            <c:if test="${fn:length(extendedTaxonConcept.parentConcepts) < 2}">Taxon</c:if></h5>
                            <ul>
                                <c:forEach items="${extendedTaxonConcept.parentConcepts}" var="parent">
                                    <li><a href="<c:url value='/species/${parent.guid}#classification'/>">${parent.nameString}</a></li>
                                </c:forEach>
                            </ul>
                            <c:if test="${not empty parent.infoSourceName && not empty parent.infoSourceURL}">
                                <cite>Source: <a href="${parent.infoSourceURL}">${parent.infoSourceURL}</a></cite>
                            </c:if>
                    </c:if>
                    <c:if test="${fn:length(extendedTaxonConcept.childConcepts) > 0}">
                        <h5>Child <c:if test="${fn:length(extendedTaxonConcept.childConcepts) > 1}">Taxa</c:if>
                            <c:if test="${fn:length(extendedTaxonConcept.childConcepts) < 2}">Taxon</c:if></h5>
                            <ul>
                                <c:forEach items="${childConcepts}" var="child">
                                    <li><a href="<c:url value='/species/${child.guid}#classification'/>">
                                    	${child.name}
                                    	<c:if test="${not empty child.commonNameSingle}">
                                    	 (${child.commonNameSingle})
                                    	</c:if>
                                    	</a>
                                    </li>
                                </c:forEach>
                            </ul>
                            <c:if test="${not empty child.infoSourceName && not empty child.infoSourceURL}">
                                <cite>Source: <a href="${child.infoSourceURL}">${child.infoSourceURL}</a></cite>
                            </c:if>
                    </c:if> --%>
                </div>
            </div><!---->
            <div id="column-two">
                <div class="section">

                </div>
            </div><!--close -->
        </div><!--close classification-->
        <div id="records">
            <div id="column-one">
                <div class="section">
                    <h2>Occurrence Records</h2>
                    <div id="occurrenceRecords">
                        <p><a href="${biocacheUrl}occurrences/searchByTaxon?q=${extendedTaxonConcept.taxonConcept.guid}">View
                                list of all <span id="occurenceCount"></span> occurrence records for this taxon</a></p>
                        <div id="recordBreakdowns" style="display: block">
                        </div>

                        <%-- Distribution map images --%>
                        <c:if test="${not empty extendedTaxonConcept.distributionImages}">
                                <h2>Record maps from other sources</h2>
                                <c:forEach items="${extendedTaxonConcept.distributionImages}" var="distribImage">
                                                    <div class="recordMapOtherSource" style="display: block">
                                                            <c:set var="imageLink">${not empty distribImage.isPartOf ? distribImage.isPartOf : distribImage.infoSourceURL}</c:set>
                                                            <a href="${imageLink}">
                                                            <img src="${distribImage.repoLocation}"/>
                                                            </a>
                                                            <br/>
                                                            <cite>Source:
                                                                    <a href="${imageLink}" target="blank">${distribImage.infoSourceName}</a>
                                                            </cite>
                                </div>
                                </c:forEach>
                        </c:if>
                    </div>
                    <%--
                    <c:forEach var="regionType" items="${extendedTaxonConcept.regionTypes}">
                        <c:if test="${fn:containsIgnoreCase(regionType.regionType, 'state') || fn:containsIgnoreCase(regionType.regionType, 'territory')}">
                            <h4>${regionType.regionType}</h4>
                            <ul style="list-style-type: circle;">
                                <c:forEach var="region" items="${regionType.regions}">
                                    <li>${region.name}:
                                        <a href="${biocacheUrl}occurrences/searchByTaxon?q=${extendedTaxonConcept.taxonConcept.guid}&fq=state:${region.name}">${region.occurrences}</a>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:if>
                    </c:forEach>
                    <c:forEach var="regionType" items="${extendedTaxonConcept.regionTypes}">
                        <c:if test="${fn:containsIgnoreCase(regionType.regionType, 'ibra') || fn:containsIgnoreCase(regionType.regionType, 'imcra')}">
                            <h4>${regionType.regionType}</h4>
                            <ul style="list-style-type: circle;">
                                <c:forEach var="region" items="${regionType.regions}">
                                    <li>${region.name}: ${region.occurrences}</li>
                                </c:forEach>
                            </ul>
                        </c:if>
                    </c:forEach> --%>
                </div>
            </div><!---->
            <div id="column-two">
                <div class="section buttons sighting no-margin-top">
                    <div class="last">
                        <h3><a href="#contributeOverlay" class="contributeLink">Share <span>Sightings, photos and data for the
                                <strong>
                                    <c:choose>
                                        <c:when test="${not empty extendedTaxonConcept.commonNames}">
                                            ${extendedTaxonConcept.commonNames[0].nameString}
                                        </c:when>
                                        <c:otherwise>
                                            ${extendedTaxonConcept.taxonConcept.nameString}
                                        </c:otherwise>
                                    </c:choose>
                                </strong>
                            </span></a>
                        </h3>
                    </div>
                </div>
                <div class="section">
                    <div class="distroMap">
                        <h4>Map of Occurrence Records</h4>
                        <p>
                            <a href="${spatialPortalUrl}?species_lsid=${extendedTaxonConcept.taxonConcept.guid}" title="view in mapping tool" target="_blank">
                                <img src="${spatialPortalWMSUrl}ws/density/map?species_lsid=${extendedTaxonConcept.taxonConcept.guid}" class="distroImg" alt="" width="300" style="margin-bottom:-30px;"/></a><br/>
                            <a href="${spatialPortalUrl}?species_lsid=${extendedTaxonConcept.taxonConcept.guid}" title="view in mapping tool" target="_blank">Interactive version of this map</a>
                        </p>
                    </div>
                    <c:if test="${not empty extendedTaxonConcept.specimenHolding}">
                        <div class="section">
                            <h3>Specimen Holdings</h3>
                            <ul>
                                <c:forEach var="specimenHolding" items="${extendedTaxonConcept.specimenHolding}">
                                    <li><a href="${specimenHolding.url}" target="_blank">${specimenHolding.institutionName}&nbsp;:&nbsp;${specimenHolding.siteName}</a> (specimens:&nbsp;${specimenHolding.count})</li>
                                </c:forEach>
                            </ul>
                        </div>
                    </c:if>
                </div><!--close-->
            </div><!--close -->
        </div><!--close records-->
<%--        <div id="biology">
            <div id="column-one">
                <div class="section">
                    <h2>Biology</h2>
                    <p></p>
                </div>
            </div><!---->
            <div id="column-two">
                <div class="section tools">
                    <h3 class="contribute">Contribute</h3>
                    <ul>
                        <li><a href="">Images</a></li>
                        <li><a href="">Data</a></li>
                        <li><a href="">Links</a></li>
                    </ul>
                </div><!--close tools-->
                <div class="section">
                    <h2></h2>
                </div><!--close-->
            </div><!--close -->
        </div><!--close biology-->
        <div id="molecular">
            <div id="column-one">
                <div class="section">
                    <h2>Molecular</h2>
                    <p></p>
                </div>
            </div><!---->
            <div id="column-two">
                <div class="section tools">
                    <h3 class="contribute">Contribute</h3>
                    <ul>
                        <li><a href="">Images</a></li>
                        <li><a href="">Data</a></li>
                        <li><a href="">Links</a></li>
                    </ul>
                </div><!--close tools-->
                <div class="section">
                    <h2></h2>
                </div><!--close-->
            </div><!--close -->
        </div><!--close molecular-->
--%>
        <div id="literature">
            <div id="column-one" class="full-width">
                <div class="section">
                    <h2>Literature</h2>
                    <div id="literature">
                        <c:if test="${not empty extendedTaxonConcept.earliestReference || not empty extendedTaxonConcept.references}">
                            <table class="propertyTable" >
                                <tr>
                                    <th>Scientific&nbsp;Name</th>
                                    <th>Reference</th>
                                    <th>Volume</th>
                                    <th>Author</th>
                                    <th>Year</th>
                                    <th>Source</th>
                                </tr>
                                <c:if test="${not empty extendedTaxonConcept.earliestReference}">
                                    <tr class="earliestReference">
                                        <td>${extendedTaxonConcept.earliestReference.scientificName}</td>
                                        <td>${extendedTaxonConcept.earliestReference.title}
                                            <br/><span class="earliestReferenceLabel">(Earliest reference within BHL)</span>
                                        </td>
                                        <td>${extendedTaxonConcept.earliestReference.volume}</td>
                                        <td>${extendedTaxonConcept.earliestReference.authorship}</td>
                                        <td>${extendedTaxonConcept.earliestReference.year}</td>
                                        <td><a href="http://library.ala.org.au/page/${extendedTaxonConcept.earliestReference.pageIdentifiers[0]}" title="view original publication" target="_blank">Biodiversity Heritage Library</a></td>
                                    </tr>
                                </c:if>
                                <c:forEach items="${extendedTaxonConcept.references}" var="reference">
                                    <tr>
                                        <td>${reference.scientificName}</td>
                                        <td>
                                            <span class="title">${reference.title}</span>
                                        </td>
                                        <td>
                                            <span class="volume"><c:if test="${not empty reference.volume && reference.volume!='NULL'}">${reference.volume}</c:if></span><br/>
                                        </td>
                                        <td>
                                            <span class="authorship">${reference.authorship}</span>
                                        </td>
                                        <td>
                                            <span class="year">${reference.year}</span>
                                        </td>
                                        <td><a href="http://library.ala.org.au/page/${reference.pageIdentifiers[0]}" title="view original publication" target="_blank">Biodiversity Heritage Library</a></td>
                                    </tr>
                                </c:forEach>
                            </table>
                        </c:if>
                    </div>
                </div>
            </div><!---->
        </div><!--close references-->
        <div style="display: none;">
            <div id="contributeOverlay">
                <div class="section buttons"  style="text-align: left !important">
                    <h2>Share</h2>
                    <div class="sightings">
                        <h3><a href="${contributeURL}">Sightings
                                <span>Record sightings for
                                    <c:choose>
                                        <c:when test="${not empty extendedTaxonConcept.commonNames}">the <strong>${extendedTaxonConcept.commonNames[0].nameString}</strong></c:when>
                                        <c:otherwise><c:if test="${extendedTaxonConcept.taxonConcept.rankID <= 6000}">the ${extendedTaxonConcept.taxonConcept.rankString} </c:if><strong>${sciNameFormatted}</strong></c:otherwise>
                                    </c:choose>
                                </span></a></h3>
                    </div>
                    <div class="photos">
                        <h3><a href="${initParam.centralServer}/share/share-images/">Photos
                             <span>Upload your images</span></a></h3>
                    </div>
                    <div class="analogue-data">
                        <h3><a href="${initParam.centralServer}/share/share-analogue-data/">Non-digital data
                                <span>Share your paper-based notes, journals and references</span></a></h3>
                    </div>
                    <div class="digital-data last">
                        <h3><a href="${initParam.centralServer}/share/share-data/">Digital data
                                <span>Upload your spreadsheets, databases &amp; more</span></a></h3>
                    </div>
                </div>
                <br/>
                <p>And don't forget to send us your favourite <a href="${initParam.centralServer}/share/share-links/">links
                        to good <strong>web sites</strong>, <strong>ideas</strong> and <strong>information</strong></a>.</p>
            </div>
        </div>
    </body>
</html>