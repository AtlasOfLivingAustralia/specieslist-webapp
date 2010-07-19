<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta name="pageName" content="species" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>ALA Biodiversity Information Explorer: ${extendedTaxonConcept.taxonConcept.nameString}</title>
        <script language="JavaScript" type="text/javascript" src="http://test.ala.org.au/wp-content/themes/ala/scripts/jquery.jcarousel.min.js"></script>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/static/js/jquery-fancybox/jquery.fancybox-1.3.1.css" media="screen" />
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery-fancybox/jquery.fancybox-1.3.1.pack.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery.colorbox.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery.easing.1.3.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery-galleryview-1.1/jquery.galleryview-1.1.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery-galleryview-1.1/jquery.timers-1.1.2.js"></script>
        <script type="text/javascript">
            /**
             * Dena's carousel callback
             */
            function mycarousel_initCallback(carousel) {
                jQuery('.jcarousel-control a').bind('click', function() {
                    carousel.scroll(jQuery.jcarousel.intval(jQuery(this).text()));
                    carousel.startAuto(0);
                    return false;
                });
                // Disable autoscrolling if the user clicks the prev or next button.
                carousel.buttonNext.bind('click', function() {
                    carousel.startAuto(0);
                });

                carousel.buttonPrev.bind('click', function() {
                    carousel.startAuto(0);
                });

                // Pause autoscrolling if the user moves with the cursor over the clip.
                carousel.clip.hover(function() {
                    carousel.stopAuto();
                }, function() {
                    carousel.startAuto();
                });
            };

            var solrServer = "${solrServerUrl}"; //

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

                // Dena's tabs implementaation
                $('#nav-tabs > ul').tabs();

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

                // images in overview tabbed should take you to Multimedia tab and display the image
                $("#images ul a").click(function(e) {
                    e.preventDefault(); //Cancel the link behavior
                    $('#nav-tabs > ul').tabs( "select" , 1 );
                    var thumbId = "thumb" + $(this).attr('href');
                    $("a#"+thumbId).click();
                });

                // Check for valid distribution map img URLs
                $('.distroImg').load(function() {
                    $('.distroMap').show();
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
        <link type="text/css" media="screen" rel="stylesheet" href="${pageContext.request.contextPath}/static/css/colorbox.css" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/static/css/speciesPage.css" media="screen" />
    </head>
    <body id="page-36" class="page page-id-36 page-parent page-template page-template-default two-column-right">
        <div id="header" class="taxon">
            <c:set var="taxonConceptTitle">
                <c:choose>
                    <c:when test="${fn:length(taxonNames) > 0}">${taxonNames[0].nameComplete}</c:when>
                    <c:otherwise>${taxonConcept.title}</c:otherwise>
                </c:choose>
            </c:set>
            <c:set var="taxonConceptRank">
                <c:choose>
                    <c:when test="${not empty extendedTaxonConcept.taxonConcept}">${extendedTaxonConcept.taxonConcept.rankString}</c:when>
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
            <div id="breadcrumb">
                <ul>
                    <li><a href="/">Home</a></li>
                    <li><a href="${pageContext.request.contextPath}/species/search">Species</a></li>
                    <li>${sciNameFormatted}</li>
                </ul>
            </div>
            <div class="section full-width">
                <div class="hrgroup col-8">
                    <h1 class="family">${fn:replace(extendedTaxonConcept.taxonConcept.nameString, extendedTaxonConcept.taxonName.nameComplete, sciNameFormatted)}</h1>
                    <h2>${extendedTaxonConcept.commonNames[0].nameString}</h2>
                </div>
                <div class=" col-4">
                    <h3 id="rank">${extendedTaxonConcept.taxonConcept.rankString} profile</h3>
                    <cite>source: <a href="${extendedTaxonConcept.taxonConcept.infoSourceURL}" target="_blank">${extendedTaxonConcept.taxonConcept.infoSourceName}</a></cite>
                    <cite><a href="#lsidText" id="lsid" class="local" title="Life Science Identifier (pop-up)">LSID</a>
                        | <a href="${pageContext.request.contextPath}/species/${extendedTaxonConcept.taxonConcept.guid}.json" class="local" title="JSON web service">JSON</a>
                        <!-- | <a href="${pageContext.request.contextPath}/species/${extendedTaxonConcept.taxonConcept.guid}.xml" class="local" title="XML web service">XML</a> -->
                    </cite>
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
            <div id="breadcrumb" class="taxaCrumb">
                <ul>
                    <c:set var="classfn" value="${extendedTaxonConcept.classification}"/>
                    <c:set var="rankId" value="${classfn.rankId}"/>
                    <c:if test="${rankId >= 1000}">
                        <li>
                            <c:choose>
                                <c:when test="${classfn.rankId == 1000}">${classfn.scientificName}</c:when>
                                <c:when test="${not empty classfn.kingdom}"><a href="${classfn.kingdomGuid}" title="kingdom">${classfn.kingdom}</a></c:when>
                            </c:choose>
                        </li>
                    </c:if>
                    <c:if test="${rankId >= 2000}">
                        <li>
                            <c:choose>
                                <c:when test="${classfn.rankId == 2000}">${classfn.scientificName}</c:when>
                                <c:when test="${not empty classfn.phylum}"><a href="${classfn.phylumGuid}" title="phylum">${classfn.phylum}</a></c:when>
                            </c:choose>
                        </li>
                    </c:if>
                    <c:if test="${rankId >= 3000}">
                        <li>
                            <c:choose>
                                <c:when test="${classfn.rankId == 3000}">${classfn.scientificName}</c:when>
                                <c:when test="${not empty classfn.clazz}"><a href="${classfn.clazzGuid}" title="class">${classfn.clazz}</a></c:when>
                            </c:choose>
                        </li>
                    </c:if>
                    <c:if test="${rankId >= 4000}">
                        <li>
                            <c:choose>
                                <c:when test="${classfn.rankId == 4000}">${classfn.scientificName}</c:when>
                                <c:when test="${not empty classfn.order }"><a href="${classfn.orderGuid}" title="order">${classfn.order}</a></c:when>
                            </c:choose>
                        </li>
                    </c:if>
                    <c:if test="${rankId >= 5000}">
                        <li>
                            <c:choose>
                                <c:when test="${classfn.rankId == 5000}">${classfn.scientificName}</c:when>
                                <c:when test="${not empty classfn.familyGuid}"><a href="${classfn.familyGuid}" title="family">${classfn.family}</a></c:when>
                            </c:choose>
                        </li>
                    </c:if>
                    <c:if test="${rankId >= 6000}">
                        <li>
                            <c:choose>
                                <c:when test="${classfn.rankId == 6000}">${classfn.scientificName}</c:when>
                                <c:when test="${not empty classfn.genus}"><a href="${classfn.genusGuid}" title="genus">${classfn.genus}</a></c:when>
                            </c:choose>
                        </li>
                    </c:if>
                    <c:if test="${rankId >= 7000}">
                        <li>
                            <c:choose>
                                <c:when test="${classfn.rankId == 7000}">${classfn.scientificName}</c:when>
                                <c:when test="${not empty classfn.species}"><a href="${classfn.speciesGuid}" title="species">${classfn.species}</a></c:when>
                            </c:choose>
                        </li>
                    </c:if>
                    <c:if test="${rankId >= 8000}">
                        <li>
                            <c:choose>
                                <c:when test="${classfn.rankId == 8000}">${classfn.subspecies}</c:when>
                                <c:when test="${not empty classfn.subspecies}"><a href="${classfn.subspeciesGuid}" title="subspecies">${classfn.subspecies}</a></c:when>
                            </c:choose>
                        </li>
                    </c:if>
                </ul>
            </div>
            <div id="nav-tabs">
                <ul>
                    <li><a href="#overview">Overview</a></li>
                    <li><a href="#gallery">Gallery</a></li>
                    <li><a href="#identification">Identification</a></li>
                    <li><a href="#names">Names</a></li>
                    <li><a href="#records">Records</a></li>
<%--
                    <li><a href="#biology">Biology</a></li>
                    <li><a href="#molecular">Molecular</a></li>
--%>
                    <li><a href="#references">References</a></li>
                </ul>
            </div>
        </div><!--close section_page-->
        <div id="overview">
            <div id="column-one">
                <div class="section">
                    <c:forEach var="textProperty" items="${textProperties}" varStatus="status">
                        <c:if test="${status.index == 0 || !fn:contains(textProperty.name, textProperties[(status.index -1)].name)}">
                            <!-- Only show heading for first occurrence -->
                            <h2><fmt:message key="${fn:substringAfter(textProperty.name, '#')}"/></h2>
                        </c:if>
                        <p>
                            ${textProperty.value}
                            <cite>source: <a href="${textProperty.identifier}" target="_blank" title="${textProperty.title}">${textProperty.infoSourceName}</a></cite>
                        </p>
                    </c:forEach>
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
                <div id="images" class="section">
                    <ul>
                        <c:forEach var="image" items="${extendedTaxonConcept.images}" varStatus="status">
                            <c:if test="${status.index < 6}">
                                <li><a href="${status.index}" title=""><img src="${image.repoLocation}" width="150" alt="" /></a></li>
                            </c:if>
                        </c:forEach>
                    </ul>
                </div>
                <div class="section">
                    <c:if test="${not empty extendedTaxonConcept.conservationStatuses}"><h3>Conservation Status</h3></c:if>
                    <c:forEach var="status" items="${extendedTaxonConcept.conservationStatuses}">
                        <c:if test="${fn:containsIgnoreCase(status.status,'extinct') || fn:containsIgnoreCase(status.status,'endangered') || fn:containsIgnoreCase(status.status,'vulnerable') || fn:containsIgnoreCase(status.status,'threatened') || fn:containsIgnoreCase(status.status,'concern') || fn:containsIgnoreCase(status.status,'deficient')}">
                            <ul class="iucn">
                                <li <c:if test="${fn:endsWith(status.status,'Extinct')}">class="green"</c:if>><abbr title="Extinct">ex</abbr></li>
                                <li <c:if test="${fn:containsIgnoreCase(status.status,'wild')}">class="green"</c:if>><abbr title="Extinct in the wild">ew</abbr></li>
                                <li <c:if test="${fn:containsIgnoreCase(status.status,'Critically')}">class="green"</c:if>><abbr title="Critically endangered">cr</abbr></li>
                                <li <c:if test="${fn:startsWith(status.status,'Endangered')}">class="green"</c:if>><abbr title="Endangered">en</abbr></li>
                                <li <c:if test="${fn:containsIgnoreCase(status.status,'Vulnerable')}">class="green"</c:if>><abbr title="Vulnerable">vu</abbr></li>
                                <li <c:if test="${fn:containsIgnoreCase(status.status,'Near')}">class="green"</c:if>><abbr title="Near threatened">nt</abbr></li>
                                <li <c:if test="${fn:containsIgnoreCase(status.status,'concern')}">class="green"</c:if>><abbr title="Least concern">lc</abbr></li>
                            </ul>
                        </c:if>
                        <p>${status.status}<cite>source: <a href="${status.identifier}" target="_blank" title="${status.infoSourceName}">${status.infoSourceName}</a></cite></p>
                    </c:forEach>
                    <c:if test="${not empty extendedTaxonConcept.pestStatuses}"><h3>Pest Status</h3></c:if>
                    <c:forEach var="status" items="${extendedTaxonConcept.pestStatuses}">
                        <p>${status.status}
                            <cite>source: <a href="${status.identifier}" target="_blank" title="${status.infoSourceName}">${status.infoSourceName}</a></cite>
                        </p>
                    </c:forEach>
                    <c:if test="${not empty extendedTaxonConcept.extantStatusus}"><h3>Extant Status</h3></c:if>
                    <c:forEach var="status" items="${extendedTaxonConcept.extantStatusus}">
                        <p><fmt:message key="status.${status.status}"/>
                            <cite>source: <a href="${status.infoSourceURL}" target="_blank" title="${status.infoSourceName}">${status.infoSourceName}</a></cite>
                        </p>
                    </c:forEach>
                    <c:if test="${not empty extendedTaxonConcept.habitats}"><h3>Habitat Status</h3></c:if>
                    <c:forEach var="status" items="${extendedTaxonConcept.habitats}">
                        <c:set var="sourceUrl">
                            <c:choose>
                                <c:when test="${not empty status.identifier}">${status.identifier}</c:when>
                                <c:otherwise>${status.infoSourceURL}</c:otherwise>
                            </c:choose>
                        </c:set>
                        <p><fmt:message key="habitat.${status.status}"/>
                            <cite>source: <a href="${sourceUrl}" target="_blank" title="${status.infoSourceName}">${status.infoSourceName}</a></cite>
                        </p>
                    </c:forEach>
                    <div class="distroMap" style="display:none;">
                        <h3>Distribution Map</h3>
                        <p>
                            <a href="http://spatial.ala.org.au/webportal/?species_lsid=${extendedTaxonConcept.taxonConcept.guid}" title="view in mapping tool">
                                <img src="http://spatial.ala.org.au/alaspatial/ws/density/map?species_lsid=${extendedTaxonConcept.taxonConcept.guid}" id="distroImg" alt="" width="300" style="margin-bottom:-30px;"/></a>
                            <a href="http://spatial.ala.org.au/webportal/?species_lsid=${extendedTaxonConcept.taxonConcept.guid}" title="view in mapping tool">Interactive version of this map</a>
                        </p>
                    </div>
                </div><!--close news-->
                <div class="section" id="infoSourceList">
                    <h3>Resources contributing to this page</h3>
                    <ul>
                        <c:forEach var="infoSource" items="${infoSources}">
                            <c:if test="${not empty infoSource.infoSourceURL && not empty infoSource.infoSourceName}">
                                <li><a href="${infoSource.infoSourceURL}">${infoSource.infoSourceName}</a><!--${infoSource.infoSourceId}--></li>
                            </c:if>
                        </c:forEach>
                    </ul>
                </div><!--close tools-->
            </div><!--close -->
        </div><!--close overview-->
        <div id="gallery">
            <div id="column-one">
                <div class="section">
                    <h2>Gallery</h2>
                    <h3>Images</h3>
                    <div id="imageGallery">
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
                                        <c:if test="${not empty image.licence}">
                                            Image licence: ${image.licence}<br/>
                                        </c:if>
                                        <c:if test="${not empty image.rights}">
                                            Image rights: ${image.rights}<br/>
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
                                        <cite>Source: <a href="${imageUri}" target="_blank">${image.infoSourceName}</a></cite></div>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                There are no images for this taxa.
                            </c:otherwise>
                        </c:choose>
                    </div>
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
        </div><!--close multimedia-->
        <div id="identification">
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
        </div><!--close identification-->
        <div id="names">
            <div id="column-one">
                <div class="section">
                    <h2>Names</h2>
                    <p><b>Accepted Name:</b> <alatag:formatSciName name="${extendedTaxonConcept.taxonConcept.nameString}" rankId="${extendedTaxonConcept.taxonConcept.rankID}"/>${extendedTaxonConcept.taxonConcept.author}
                        <cite>Source: <a href="${extendedTaxonConcept.taxonConcept.infoSourceURL}" target="blank">${extendedTaxonConcept.taxonConcept.infoSourceName}</a></cite>
                        <c:if test="${not empty extendedTaxonConcept.taxonName.publishedIn}"><cite>Published in: <a href="#">${extendedTaxonConcept.taxonName.publishedIn}</a></cite></c:if>
                    </p>
                    <c:forEach items="${extendedTaxonConcept.synonyms}" var="synonym">
                        <p><b>Synonym:</b><alatag:formatSciName name="${synonym.nameString}" rankId="${extendedTaxonConcept.taxonConcept.rankID}"/> ${synonym.author}
                            <c:choose>
                                <c:when test="${empty synonym.infoSourceURL}"><cite>Source: <a href="${extendedTaxonConcept.taxonConcept.infoSourceURL}" target="blank">${extendedTaxonConcept.taxonConcept.infoSourceName}</a></cite></c:when>
                                <c:otherwise><cite>Source: <a href="${synonym.infoSourceURL}" target="blank">${synonym.infoSourceName}</a></cite></c:otherwise>
                            </c:choose>
                            <c:if test="${not empty synonym.publishedIn}"><cite>Published in: <a href="#">${synonym.publishedIn}</a></cite></c:if>
                        </p>
                    </c:forEach>
                    <c:forEach items="${extendedTaxonConcept.commonNames}" var="commonName">
                        <p><b>Common Name:</b> ${commonName.nameString}
                            <c:choose>
                                <c:when test="${empty commonName.infoSourceURL}"><cite>Source: <a href="${extendedTaxonConcept.taxonConcept.infoSourceURL}" target="blank">${extendedTaxonConcept.taxonConcept.infoSourceName}</a></cite></c:when>
                                <c:otherwise><cite>Source: <a href="${commonName.infoSourceURL}" target="blank">${commonName.infoSourceName}</a></cite></c:otherwise>
                            </c:choose>
                            <c:if test="${not empty synonym.publishedIn}"><cite>Published in: <a href="#">${synonym.publishedIn}</a></cite></c:if>
                        </p>
                    </c:forEach>
                </div>
            </div><!---->
            <div id="column-two">
                <div class="section">
                    <h2>Parent &amp; Child Taxa</h2>
                    <c:if test="${fn:length(extendedTaxonConcept.parentConcepts) > 0}">
                        <h5>Parent <c:if test="${fn:length(extendedTaxonConcept.parentConcepts) > 1}">Taxa</c:if>
                            <c:if test="${fn:length(extendedTaxonConcept.parentConcepts) < 2}">Taxon</c:if></h5>
                            <ul>
                                <c:forEach items="${extendedTaxonConcept.parentConcepts}" var="parent">
                                    <li><a href="<c:url value='/species/${parent.guid}'/>">${parent.nameString}</a></li>
                                </c:forEach>
                            </ul>
                            <c:if test="${not empty parent.infoSourceName && not empty parent.infoSourceURL}">
                                <cite>Source: <a href="${parent.infoSourceURL}">${parent.infoSourceURL}</a></cite>
                            </c:if>
                    </c:if>
                    <c:if test="${fn:length(extendedTaxonConcept.childConcepts) > 0}">
                        <h5>Child <c:if test="${fn:length(extendedTaxonConcept.childConcepts) > 1}">Taxa</c:if>
                            <c:if test="${fn:length(extendedTaxonConcept.childConcepts) < 2}">Taxon</c:if>:</h5>
                            <ul>
                                <c:forEach items="${extendedTaxonConcept.childConcepts}" var="child">
                                    <li><a href="<c:url value='/species/${child.guid}'/>">${child.nameString}</a></li>
                                </c:forEach>
                            </ul>
                            <c:if test="${not empty child.infoSourceName && not empty child.infoSourceURL}">
                                <cite>Source: <a href="${child.infoSourceURL}">${child.infoSourceURL}</a></cite>
                            </c:if>
                    </c:if>
                </div>
            </div><!--close -->
        </div><!--close names-->
        <div id="records">
            <div id="column-one">
                <div class="section">
                    <h2>Records</h2>
                    <p><a href="http://biocache.ala.org.au/occurrences/searchByTaxon?q=${extendedTaxonConcept.taxonConcept.guid}">View all occurrence records for this taxon</a></p>
                    <p><a href="http://spatial.ala.org.au/webportal/?species_lsid=${extendedTaxonConcept.taxonConcept.guid}">View all map of records for this taxon</a></p>
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
                    <div class="distroMap" style="display:none;">
                        <h3>Distribution Map</h3>
                        <p>
                            <a href="http://spatial.ala.org.au/webportal/?species_lsid=${extendedTaxonConcept.taxonConcept.guid}" title="view in mapping tool">
                                <img src="http://spatial.ala.org.au/alaspatial/ws/density/map?species_lsid=${extendedTaxonConcept.taxonConcept.guid}" id="distroImg" alt="" width="300" style="margin-bottom:-30px;"/></a>
                            <a href="http://spatial.ala.org.au/webportal/?species_lsid=${extendedTaxonConcept.taxonConcept.guid}" title="view in mapping tool">Interactive version of this map</a>
                        </p>
                    </div>
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
        <div id="references">
            <div id="column-one" class="full-width">
                <div class="section">
                    <h2>References</h2>
                    <div id="literature">
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
                    </div>
                </div>
            </div><!---->
        </div><!--close references-->
    </body>
</html>