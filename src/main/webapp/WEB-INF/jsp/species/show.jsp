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

                $("a#lsid").fancybox({
                    'hideOnContentClick' : false,
                    'titleShow' : false,
                    'autoDimensions' : false,
                    'width' : 600,
                    'height' : 180
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

                // Dena's tabs implementaation
                $('#nav-tabs > ul').tabs();

                // Display full image when thumbnails are clicked
                function formatTitle(title, currentArray, currentIndex, currentOpts) {
                    return '<div id="tip7-title"><span></span>' +
                        (title && title.length ? '<b>' + title + '</b>' : '' ) + '<br/>Image ' + (currentIndex + 1) + ' of ' + currentArray.length + '</div>';
                }

                $("a.thumbImageOFF").fancybox({
                    'hideOnContentClick' : false,
                    'titlePosition' : 'inside',
                    'titleFormat' : formatTitle,
                    'titleShow' : true
                });
                
                $("a.thumbImage").colorbox({
                    title: function() {
                        var titleBits = this.title.split("|");
                        return "<a href='"+titleBits[1]+"'>"+titleBits[0]+"</a>"; },
                    opacity: 0.5,
                    maxWidth: "80%",
                    onComplete: function() {
                        $("#cboxTitle").html("");
                        //link = $.fn.colorbox.element();
                        //title = $(link).attr("title");
                        var titleBits = this.title.split("|");
                        var titleText = "<cite>Source: <a href='"+titleBits[1]+"' target='_blank'>"+titleBits[0]+"</a></cite>";
                        $("<div>"+titleText+"</div>").insertAfter("#cboxPhoto");
                        $.fn.colorbox.resize(); }
                });

                // images in overview tabbed should take you to Multimedia tab
                $("#images ul a").click(function(e) {
                    e.preventDefault(); //Cancel the link behavior
                    //window.location.replace("#multimedia");
                    //window.location.href = window.location.href;
                    //$(".ui-tabs-nav a").attr("href","#multimedia").click();
                    $('#nav-tabs > ul').tabs( "select" , 1 )
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
                            <div id="breadcrumb">
                                <ul>
<!--                                    <li><a href="">Animalia</a></li>
                                    <li><a href="">Arthropoda</a></li>
                                    <li><a href="">Uniramia</a></li>
                                    <li><a href="">Insecta</a></li>
                                    <li><a href="">Coleoptera</a></li>
                                    <li><a href="">Polyphaga</a></li>
                                    <li>Coccinellidae</li>-->
                                    <c:set var="classfn" value="${extendedTaxonConcept.classification}"/>
                                    <c:set var="rankId" value="${classfn.rankId}"/> <!-- rankID: ${rankId} -->
                                    <c:if test="${rankId >= 1000}">
                                        <li>
                                            <c:if test="${not empty classfn.kingdom}"><a href="${classfn.kingdomGuid}">${classfn.kingdom}</a></c:if>
                                            <c:if test="${empty classfn.kingdom && classfn.rankId == 1000}">${classfn.scientificName}</c:if>
                                        </li>
                                    </c:if>
                                    <c:if test="${rankId >= 2000}">
                                        <li>
                                            <c:if test="${not empty classfn.phylum}"><a href="${classfn.phylumGuid}">${classfn.phylum}</a></c:if>
                                            <c:if test="${empty classfn.phylum && classfn.rankId == 2000}">${classfn.scientificName}</c:if>
                                        </li>
                                    </c:if>
                                    <c:if test="${rankId >= 3000}">
                                        <li>
                                            <c:if test="${not empty classfn.clazz}"><a href="${classfn.clazzGuid}">${classfn.clazz}</a></c:if>
                                            <c:if test="${empty classfn.clazz && classfn.rankId == 3000}">${classfn.scientificName}</c:if>
                                        </li>
                                    </c:if>
                                    <c:if test="${rankId >= 4000}">
                                        <li>
                                            <c:if test="${not empty classfn.order}"><a href="${classfn.orderGuid}">${classfn.order}</a></c:if>
                                            <c:if test="${empty classfn.order && classfn.rankId == 4000}">${classfn.scientificName}</c:if>
                                        </li>
                                    </c:if>
                                    <c:if test="${rankId >= 5000}">
                                        <li>
                                            <c:if test="${not empty classfn.family}"><a href="${classfn.familyGuid}">${classfn.family}</a></c:if>
                                            <c:if test="${empty classfn.family && classfn.rankId == 5000}">${classfn.scientificName}</c:if>
                                        </li>
                                    </c:if>
                                    <c:if test="${rankId >= 6000}">
                                        <li>
                                            <c:if test="${not empty classfn.genus}"><a href="${classfn.genusGuid}">${classfn.genus}</a></c:if>
                                            <c:if test="${empty classfn.genus && classfn.rankId == 6000}">${classfn.scientificName}</c:if>
                                        </li>
                                    </c:if>
                                    <c:if test="${rankId >= 7000}">
                                        <li>
                                            <c:if test="${not empty classfn.species}"><a href="${classfn.speciesGuid}">${classfn.species}</a></c:if>
                                            <c:if test="${empty classfn.species && classfn.rankId == 7000}">${classfn.scientificName}</c:if>
                                        </li>
                                    </c:if>
                                    <c:if test="${rankId >= 8000}">
                                        <li>
                                            <c:if test="${not empty classfn.subspecies}"><a href="${classfn.subspeciesGuid}">${classfn.subspecies}</a></c:if>
                                            <c:if test="${empty classfn.subspecies && classfn.rankId == 8000}"><a href="">${classfn.scientificName}</a></c:if>
                                        </li>
                                    </c:if>
                                </ul>
                            </div>
                            <div class="section full-width">
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
                                <div class="hrgroup col-9">
                                    <h1 class="family">${fn:replace(extendedTaxonConcept.taxonConcept.nameString, extendedTaxonConcept.taxonName.nameComplete, sciNameFormatted)}</h1>
                                    <h2>${extendedTaxonConcept.commonNames[0].nameString}</h2>
                                </div>
                                <div class="aside col-3">
                                    <cite>source: <a href="${extendedTaxonConcept.taxonConcept.infoSourceURL}" target="_blank">${extendedTaxonConcept.taxonConcept.infoSourceName}</a></cite>
                                    <cite><a href="#lsidText" id="lsid" class="local" title="Life Science Identifier (pop-up)">LSID</a> | <a href="${pageContext.request.contextPath}/species/${extendedTaxonConcept.taxonConcept.guid}.json" class="local" title="JSON web service">JSON</a></cite>
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
                            <div id="nav-tabs">
                                <ul>
                                    <li><a href="#overview">Overview</a></li>
                                    <li><a href="#gallery">Gallery</a></li>
                                    <li><a href="#identification">Identification</a></li>
                                    <li><a href="#names">Names</a></li>
                                    <li><a href="#records">Records</a></li>
                                    <li><a href="#biology">Biology</a></li>
                                    <li><a href="#molecular">Molecular</a></li>
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
                                                <li><a href="" title=""><img src="http://${pageContext.request.serverName}:80${fn:replace(image.repoLocation, "/data/bie", "/repository")}" width="150" alt="" /></a></li>
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
                                        <p><fmt:message key="habitat.${status.status}"/>
                                            <cite>source: <a href="${status.identifier}" target="_blank" title="${status.infoSourceName}">${status.infoSourceName}</a></cite>
                                        </p>
                                    </c:forEach>
                                    <h3>Distribution Map</h3>
                                    <p>
                                        <a href="http://spatial-dev.ala.org.au/webportal/?species_lsid=${extendedTaxonConcept.taxonConcept.guid}" title="view in mapping tool">
                                            <img src="http://spatial.ala.org.au/alaspatial/ws/density/map?species_lsid=${extendedTaxonConcept.taxonConcept.guid}" alt="" width="300" style="margin-bottom:-30px;"/></a>
                                        <a href="http://spatial-dev.ala.org.au/webportal/?species_lsid=${extendedTaxonConcept.taxonConcept.guid}" title="view in mapping tool">Interactive version of this map</a>
                                    </p>
                                </div><!--close news-->
                                <div class="section tools">
                                    <h3><a href="">Experts</a></h3>
                                    <ul>
                                        <li><a href="">Jane Doe</a></li>
                                        <li><a href="">Jack Black</a></li>
                                        <li><a href="">Edmund Carry</a></li>
                                    </ul>
                                    <h3><a href="">Related Projects</a></h3>
                                    <ul>
                                        <li><a href="" class="external">ACT Kangaroo cull</a></li>
                                        <li><a href="" class="external">DEWHA ABC Study</a></li>
                                    </ul>
                                    <h3><a href="">Related websites</a></h3>
                                    <ul>
                                        <li><a href="" class="external">ACT Kangaroo cull</a></li>
                                        <li><a href="" class="external">DEWHA ABC Study</a></li>
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
                                        <c:forEach var="image" items="${extendedTaxonConcept.images}" varStatus="status">
                                            <c:set var="thumbUri">http://${pageContext.request.serverName}:80${fn:replace(fn:replace(image.repoLocation, "/data/bie", "/repository"),"raw","thumbnail")}</c:set>
                                            <c:set var="imageTitle">${image.infoSourceName} | ${image.infoSourceURL} </c:set>
                                            <a class="thumbImage" rel="thumbs" title="${imageTitle}" href="http://${pageContext.request.serverName}:80${fn:replace(image.repoLocation, "/data/bie", "/repository")}"><img src="${thumbUri}" alt="${image.infoSourceName}" title="${imageTitle}" width="100px" height="100px" style="width:100px;height:100px;padding-right:3px;"/></a>
                                        </c:forEach>
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

    <!--                                    <h3>Description</h3>
                                        <p>Broadly ovate, moderately to strongly convex beetles with pseudotrimerous tarsi (segment 3 reduced, 2 strongly lobed beneath it). Antennae short, weakly clubbed; apical maxillary palp segment almost always large and securiform; femoral lines on first ventrite and sometimes metasternum. Many larger species glabrous and brightly patterned with red or yellow and black or blue; most smaller species more uniformly coloured and finely pubescent.</p>
                                        <p>Larvae elongate, oblong or occasionally broadly ovate and slightly to strongly flattened, usually with transverse row of 6 tubercles or prominences on most abdominal segments and often covered with waxy exudate; some forms are spinose above and may be aposematically coloured like many adults. Antennae very short; mandibles often with sub-basal lobe which probably represents a reduced mola; mala obtuse and usually with stylus  (specialised, setiferous process); there are paired glandular openings on the thorax and abdomen; tibia usually withs 2 to several expanded setae extending beneath tarsungulus. Pupa obtect and partly enclosed within larval skin, which is attached to substrate by anal end.<cite>source: <a href="http://anic.ento.csiro.au/insectfamilies/biota_details.aspx?OrderID=25407&BiotaID=26494&PageID=families">Australian Insect Families</a></cite></p>
                                        <h3>Identification Keys</h3>
                                        <ul>
                                            <li><a class="external" href="http://anic.ento.csiro.au/insectfamilies/key_Coleoptera.aspx?OrderID=25407&PageID=identify">Key to distinguish Coccinellidae from other groups in Coleoptera</a></li>
                                            <li><a class="external" href="http://www.ento.csiro.au/biology/ladybirds/lucid/lucidKey.html">Key to identify genera of Australian Coccinellidae</a></li>
                                        </ul>-->
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
                                    
                                    <div id="names" style="display:none;">
                                        <table class="propertyTable">
                                            <tr>
                                                <th width="">Type</th>
                                                <th width="">Name</th>
                                                <th width="">Source</th>
                                            </tr>
                                            <tr>
                                                <td class="propertyNames">Accepted Name</td>
                                                <td>${extendedTaxonConcept.taxonConcept.nameString}</td>
                                                <td><c:if test="${not empty extendedTaxonConcept.taxonName.publishedIn}">Published in: ${extendedTaxonConcept.taxonName.publishedIn}</c:if></td>
                                            </tr>
                                            <c:forEach items="${extendedTaxonConcept.synonyms}" var="synonym">
                                                <tr>
                                                    <td class="propertyNames">Synonym</td>
                                                    <td>${synonym.nameString}</td>
                                                    <td><c:if test="${not empty synonym.publishedIn}">Published in: ${synonym.publishedIn}</c:if></td>
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
                        </div><!--close names-->
                        <div id="records">
                            <div id="column-one">
                                <div class="section">
                                    <h2>Records</h2>
                                    <p><a href="/biocache-webapp/occurrences/searchByTaxon?q=${extendedTaxonConcept.taxonConcept.guid}">View all occurrence records for this taxon</a></p>
                                    <p><a href="http://spatial-dev.ala.org.au/webportal/?species_lsid=${extendedTaxonConcept.taxonConcept.guid}">View all map of records for this taxon</a></p>
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
                                    <h3>Distribution Map</h3>
                                    <p>
                                        <a href="http://spatial-dev.ala.org.au/webportal/?species_lsid=${extendedTaxonConcept.taxonConcept.guid}" title="view in mapping tool"><img src="http://spatial.ala.org.au/alaspatial/ws/density/map?species_lsid=${extendedTaxonConcept.taxonConcept.guid}" alt="" width="300"/></a>
                                        <a href="http://spatial-dev.ala.org.au/webportal/?species_lsid=${extendedTaxonConcept.taxonConcept.guid}" title="view in mapping tool">Interactive version of this map</a>
                                    </p>
                                </div><!--close-->
                            </div><!--close -->
                        </div><!--close records-->
                        <div id="biology">
                            <div id="column-one">
                                <div class="section">
                                    <h2>Biology</h2>
                                    <p>[TODO]</p>
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
                                    <p>[TODO]</p>
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
                        <div id="references">
                            <div id="column-one" class="full-width">
                                <div class="section">
                                    <h2>References</h2>
                                    <div id="literature">
                                    <table class="propertyTable" >
                                        <tr>
                                            <th>Scientific Name</th>
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


<!--                                    <ul class="references">
                                        <li>Agassiz, J.L.R. 1846. <em>[1842-1847]. Nomenclatoris Zoologici, continens nomina systematica generum Animalium tam viventium quam fossilium, secundum ordinem alphabeticum disposita, adjectis auctoribus, libris in quibus reperiuntor, anno editionis, etymologia, et familis, ad quas pertinent, in variis classibus. (Lepidoptera)</em>. Soloduri : Jent et Gassmann Fasc. 9&ndash;10 [1846]; (Index universalis). 12 [1847] viii+393 pp.</li>
                                        <li>Bielawski, R. 1959. Coccinellidae (Coleopt.) von Sumba, Sumbawa, Flores, Timor und Bali. <em>Verhandlungen der Naturforschenden Gesellschaft in Basel</em> <strong>69</strong>(2): 145&ndash;166</li>
                                        <li>Bielawski, R. 1973. Rhyzobiini, Stethorini, Scymnini et Pharini (Coleoptera, Coccinellidae) de Nouvelle Cal&eacute;donie. <em>Annales Zoologici Warszawa</em> <strong>30</strong>(14): 387&ndash;409</li>
                                        <li>Blackburn, T. 1889. Further notes on Australian Coleoptera, with descriptions of new species VI. <em>Transactions of the Royal Society of South Australia</em> <strong>11</strong>: 132&ndash;148</li>
                                        <li>Blackburn, T. 1889. Further notes on Australian Coleoptera, with descriptions of new species. <em>Proceedings of the Linnean Society of New South Wales</em> 2 <strong>3</strong>: 1387&ndash;1506</li>
                                        <li>Blackburn, T. 1889. Further notes on Australian Coleoptera, with descriptions of new species. <em>Transactions of the Royal Society of South Australia</em> <strong>11</strong>: 175&ndash;214</li>
                                        <li>Blackburn, T. 1890. Further notes on Australian Coleoptera, with descriptions of new species. Part V. <em>Proceedings of the Linnean Society of New South Wales</em> <strong>4</strong>(1889): 1247&ndash;1276</li>
                                        <li>Blackburn, T. 1891. Further notes on Australian Coleoptera, with descriptions of new genera and species. IX. <em>Transactions of the Royal Society of South Australia</em> <strong>14</strong>: 65&ndash;153</li>
                                        <li>Blackburn, T. 1892. Further notes on Australian Coleoptera, with descriptions of new genera and species. XI. <em>Transactions of the Royal Society of South Australia</em> <strong>15</strong>(1): 20&ndash;73</li>
                                        <li>Blackburn, T. 1892. Further notes on Australian Coleoptera, with descriptions of new genera and species. XII. <em>Transactions of the Royal Society of South Australia</em> <strong>15</strong>(2): 207&ndash;261</li>
                                        <li>Blackburn, T. 1894. Further notes on Australian Coleoptera, with descriptions of new genera and species. XVI. <em>Transactions of the Royal Society of South Australia</em> <strong>18</strong>: 59&ndash;240</li>
                                        <li>Blackburn, T. 1895. Further notes on Australian Coleoptera, with descriptions of new genera and species. XVIII. <em>Transactions of the Royal Society of South Australia</em> <strong>19</strong>: 201&ndash;258</li>
                                        <li>Blackburn, T. 1896. Further notes on Australian Coleoptera, with descriptions of new genera and species. XIX. <em>Transactions of the Royal Society of South Australia</em> <strong>20</strong>: 35&ndash;109</li>
                                        <li>Blackburn, T. 1900. Further notes on Australian Coleoptera, with descriptions of new genera and species. XXVI. <em>Transactions of the Royal Society of South Australia</em> <strong>24</strong>: 35&ndash;68</li>
                                        <li>Blaisdell, F.E. 1892. A new species of Coleoptera from California. <em>Entomological News</em> <strong>3</strong>: 51</li>
                                        <li>Boheman, C.H. 1859. <em>Coleoptera. Species novas descripsit. Kongliga Svenska Fregatten Eugenies Resa omkring jorden under bef‰l af C.A. Virgin Ahren 1851-1853. Vetenskapliga iakttagelser pÂ H.M. Konung Oscar Den F&ouml;rstes befallning utgifna af K. Svenska Vetenkaps Akademien, Zoologi III, Insekter</em>. Stockholm : P.A. Norstedt & Sˆner Vol. 2 113&ndash;217 pp.</li>
                                        <li>Boisduval, J.A. 1835. <em>Voyage de DÈcouvertes de l'<em>Astrolabe</em> exÈcutÈ par ordre du Roi, pendant les annÈes 1826ñ1827ñ1828ñ1829, sous le commandement de M.J. Dumont d'Urville. Faune Entomologique de l'OcÈan Pacifique, avec l'illustration des insectes nouveaux recueillis pendant le voyage. 2me Partie. Col&eacute;opt&egrave;res et autres Ordres</em>. Paris : J. Tastu vii 716 pp.</li>
                                        <li>Boisduval, J.B.A.D. de 1832. <em>Voyage de DÈcouvertes de l'<i>Astrolabe</i> exÈcutÈ par Ordre du Roi, Pendant les AnnÈes 1826ñ1827ñ1828ñ1829, sous le Commandement de M.J. Dumont D'Urville. Faune entomologique de l'OcÈan Pacifique, avec l'illustration des insectes nouveaux recueillis pendant le Voyage. Part 1. LÈpidoptËres</em>. Paris : J. Tastu iv, 267 pp. [publication date: Sherborn, C.D. & Woodward, B.B. 1901. Dates of publication of the zoological and botanical portions of some French voyages. Dumont d'Urville's <i>Voyage de l'Astrolabe</I>. <em>Annals and Magazine of Natural History</em> 7 <strong>8</strong>: 333 [333]]</li>
                                        <li>Britton, E.B. & Lee, B. 1972. <i>Stethorus loxtoni</i> sp. n. (Coleoptera: Coccinellidae) a newly-discovered predator of the two-spotted mite. <em>Journal of the Australian Entomological Society</em> <strong>11</strong>: 55-60</li>
                                        <li>Casey, T.L. 1899. A revision of the American Coccinellidae. <em>Journal of the New York Entomological Society</em> <strong>7</strong>(2): 71-169</li>
                                        <li>Chapin, E.A. 1926. On some Coccinellidae of the tribe Telsimini, with descriptions of new species. <em>Actes des Colloques Insectes Sociaux</em> <strong>39</strong>: 129-134</li>
                                        <li>Chapin, E.A. 1940. New genera and species of lady-beetles related to <i>Serangium </i>Blackburn (Coleoptera: Coccinellidae). <em>Journal of the Washington Academy of Sciences</em> <strong>30</strong>: 263-272</li>
                                        <li>Chapin, E.A. 1965. The genera of the Chilocorini (Coleoptera, Coccinellidae). <em>Bulletin of the Museum of Comparative Zoology, Harvard</em> <strong>133</strong>(4): 227-271</li>
                                        <li>Chapuis, M.F. 1876. Famille des …rotyliens, des Endomychides et des Coccinellides. pp. 424 pp. <em>in</em> Lacordaire, T. & Chapuis, F. (eds). <em>Histoire Naturelle des Insectes. Genera des ColÈoptËres ou exposÈ mÈthodique et critique de tous les genres proposÈs jusqu'ici dans cet ordre d'insectes</em>. Paris : Roret Vol. 12.</li>
                                        <li>Chazeau, J. 1981. Description du genre <i>Paraphellus </i>Ètabli pour <i>Paraphellus pacificus</I>, nouvelle espËce de Coccinellidae des Iles Fidji (Coleoptera). <em>Revue FranÁaise d'Entomologie</em> NS <strong>3</strong>: 119-122</li>
                                        <li>Chazeau, J. 1984. <i>Telsimia </i>de Nouvelle-GuinÈe (Col. Coccinellidae). <em>Bulletin de la SociÈtÈ Entomologique de France</em> <strong>89</strong>: 2-9</li>
                                        <li>Chazeau, J. in Chazeau, J., …tienne, J. & F¸rsch, H. 1974. Les Coccinellidae de l'Œle de la RÈunion (Insecta Coleoptera). <em>Bulletin du Museum d'Histoire Naturelle. Paris</em> 3 <strong>210</strong>(Zoology 140): 265-297</li>
                                        <li>Chevrolat, L.A.A. in Dejean, P.F.M.A. 1836. <i>Catalogue des ColÈoptËres de la collection de M. le Comte Dejean</I>. [3 edition, Fascicle 5]. xiv 503 pp.</li>
                                        <li>Coquillett, D.W. 1893. Report on some of beneficial and injurious insects of California. <em>United States Department of Agriculture Entomology Bulletin</em> <strong>30</strong>: 9-33</li>
                                        <li>Crotch, G.R. 1871. <em>List of Coccinellidae</em>. Cambridge : Printed by the author 8 pp.</li>
                                        <li>Crotch, G.R. 1874. <em>A Revision of the Coleopterous Family Coccinellidae</em>. London : E.W. Janson xv + 311 pp.</li>
                                        <li>Dieke, G.H. 1947. Ladybeetles of the genus <i>Epilachna </I>(sens. lat.) in Asia, Europe and Australia. <em>Smithsonian Miscellaneous Collections</em> <strong>106</strong>(15): 1-183, 27 pls</li>
                                        <li>Erichson, W.F. 1842. Beitrag zur Insecten-Fauna von Vandiemensland, mit besonderer Ber¸cksichtung der geographischen Verbreitung der Insecten. <em>Archiv f¸r Naturgeschichte. Berlin</em> <strong>8</strong>: 83-287, pls IV, V</li>
                                        <li>Fabricius, J.C. 1775. <em>Systema Entomologiae, sistens Insectorum Classes, Ordines, Genera, Species, adiectis Synonymis, Locis, Descriptionibus, Observationibus</em>. Flensburgi & Lipsiae : Officina Libraria Kortii xxvii 832 pp.</li>
                                        <li>Fabricius, J.C. 1781. <em>Species Insectorum, exhibentes corum differentias specificas, synonyma auctorum, loca natalia, metamorphosin adjectis observationibus, descriptionibus</em>. Hamburgi et Kilonii : Carol. Ernest. Bohnii Vol. 1 viii 552 pp.</li>
                                        <li>Fabricius, J.C. 1787. <em>Mantissa Insectorum sistens eorum species nuper detectas adiectis characteribus genericus, differentiis specificis, emendationibus, observationibus</em>. Hafniae : Christ. Gottl. Proft. Vol. 1 xx 348 pp.</li>
                                        <li>Fauvel, M.A. 1862. ColÈoptËres de la Nouvelle-CalÈdonie, recueillis par M. E. DÈplanche, chirurgien de la marine impÈriale (1858-59-60). <em>Bulletin de la SociÈtÈ LinnÈenne de Normandie</em> <strong>7</strong>(1): 3-68, 3 pls</li>
                                        <li>F¸rsch, H. 1964. Neue Gesichtpunkte zur Beurteilung des Gattungsnamens <i>Micraspis </i>Dejean (Col. Coccinellidae). <em>Nachrichenblatt der Bayerischen Entomologie</em> <strong>13</strong>: 70-72</li>
                                        <li>F¸rsch, H. 1985. Die afrikanischen Sukunahikonini und Microweiseini mit Diskussion ¸ber alle Gattungen (Col. Cocc.). <em>Deutsche Entomologische Zeitschrift</em> N.F. <strong>32</strong>(4-5): 279-295</li>
                                        <li>F¸rsch, H. 1987. Ubersicht uber die Genera und Subgenera der Scymnini mit besonderer Berucksichtigung der Westpalaearktis (Insecta, Coleoptera, Coccinellidae). <em>Entomologische Abhandlungen. Staatliches Museum f¸r Tierkunde Dresden</em> <strong>51</strong>(4): 57-74</li>
                                        <li>Gadeau de Kerville, H. 1884. Descriptions de quelques espËces nouvelles de la famille des Coccinellidae. <em>Annales de la SociÈtÈ Entomologique de France</em> 6e <strong>4</strong>: 69-72 pl. 4</li>
                                        <li>Goeze, J.A.E. 1777. <em>Entomologische Beitr‰ge zu des Ritter LinnÈ zwˆlften Ausgabe des Natursystems</em>. Leipzig : Weidmann Vol. 1 xvi 736 pp.</li>
                                        <li>GuÈrin-MÈneville, F.E. 1835. <i>Iconographie du RÈgne Animal de G. Cuvier, ou reprÈsentations d'apres de l'une espËces les plus remarquables, et souvent non encore figurÈes, de chaque genre d'animaux</I>. Avec un texte descriptif mis au courant de la science. Ouvrage pouvant servir d'atlas a tous les traitÈs de zoologie. Vol. 2, Insectes (Plates 1829-37, text 1844). Paris : J.B. Balliere, Libraire de l'AcadÈmie Royale de MÈdicine. 576 pp.</li>
                                        <li>Gyllenhal in Schˆnherr, C.J. 1808. <em>Synonyma Insectorum, oder: Versuch einer Synonymie aller bisher bekannten Insecten; nach Fabricii Systema Eleutheratorum &c. geordnet. <i>Erster Band. Eleutherata oder K‰fer. Zweiter Theil.</I></em>. Stockholm : C.F. Marquard x + 424 pp. 1 pl.</li>
                                        <li>Hales, D.F. 1977. <i>Coleophora veranioides</i> Blackburn: a variety of <i>Coleophora inaequalis</i> (F.) (Coleoptera: Coccinellidae). <em>Australian Entomological Magazine</em> <strong>4</strong>: 55-56</li>
                                        <li>Herbst, J.F.W. 1786. Erste Mantisse zum Verzeichniﬂ der ersten Klasse meiner Insektensammlung. <em>Archiv der Insectengeschichte</em> <strong>6</strong>: 153-182</li>
                                        <li>Houston, K.J. 1980. A revision of the Australian species of <i>Stethorus </i>Weise (Coleoptera: Coccinellidae). <em>Journal of the Australian Entomological Society</em> <strong>19</strong>: 81-91</li>
                                        <li>Iablokoff-Khnzorian, S.M. 1972. Novye vidy zhestokrylykh-kokcinellid iz SSSR (Coleoptera, Coccinellidae) [in Russian]. <em>Doklady Akademii Nauk Armyanoskoi SSSR</em> <strong>55</strong>(2): 116-122</li>
                                        <li>Iablokoff-Khnzorian, S.M. 1979. Genera der pal‰arktischen Coccinellini (Coleoptera, Coccinellidae). <em>Entomologische Bl‰tter f¸r Biologie und Systematik der K‰fer</em> <strong>75</strong>: 37-75</li>
                                        <li>Iablokoff-Khnzorian, S.M. 1982. <em>Les coccinelles. ColeoptËres-Coccinellidae</em>. Paris : SociÈtÈ nouvelle des editions BoubÈe 568 pp.</li>
                                        <li>Iablokoff-Khnzorian, S.M. 1984. Notes sur la tribu des Coccinellini (Coleoptera, Coccinellidae). <em>Nouvelle Revue d'Entomologie</em> NS <strong>1</strong>(2): 203ñ222</li>
                                        <li>Iablokoff-Khnzorian, S.M. 1984. Synopsis von zwei Marienk‰fergattungen aus der australischen Region (Coleoptera, Coccinellidae). <em>Entomologische Bl‰tter f¸r Biologie und Systematik der K‰fer</em> <strong>80</strong>(2/3): 107-122</li>
                                        <li>Jadwiszczak, A. S. & Wegrzynowicz, P. 2003. <em>World Catalogue of Coccinellidae Part I ó Epilachninae</em>. Olsztyn : Mantis 264 pp.</li>
                                        <li>Kamiya, H. 1960. A new tribe of Coccinellidae (Coleoptera). <em>Konty˚</em> <strong>28</strong>(1): 22-26 pl. 3</li>
                                        <li>Kamiya, H. 1961. A revision of the tribe Scymnini from Japan and the Loochos (Coleoptera: Coccinellidae). Part I. Genera<i> Clitostethus, Stethorus </i>and <i>Scymnus </I>(except subgenus <i>Pullus</I>)<I>.</I>. <em>Journal of the Faculty of Agriculture, Kyushu University</em> <strong>11</strong>(3): 275-301</li>
                                        <li>Kapur, A.P. 1948. On the Old World species of the genus <i>Stethorus </i>Weise (Coleoptera, Coccinellidae). <em>Bulletin of Entomological Research</em> <strong>39</strong>: 297-320</li>
                                        <li>Koebele, A. 1890. Report of the fluted scale of the orange and its natural enemies in Australia. <em>Bulletin of the United States Bureau of Entomology</em> <strong>21</strong>: 9-32</li>
                                        <li>Korschefsky, R. 1931. Pars 118: Coccinellidae. I. pp. 1ñ224 <em>in</em> Junk, W. & Schenkling, S. (eds). <em>Coleopterorum Catalogus</em>. Berlin : W. Junk.</li>
                                        <li>Korschefsky, R. 1932. Pars 120: Coccinellidae. II. pp. 225ñ659 <em>in</em> Junk, W. & Schenkling, S. (eds). <em>Coleopterorum Catalogus</em>. Berlin : W. Junk.</li>
                                        <li>Korschefsky, R. 1933. Synonymische und andere Bemerkungen ¸ber Crotch'sche Coccinelliden-Typen. <em>Stylops</em> <strong>2</strong>: 236-237, 7 figs</li>
                                        <li>Korschefsky, R. 1934. Prof. Dr. E. Handschin, Studienreise auf den Sundainseln und Nordaustralien, 1930-32. 3. Drei neue Coccinelliden der Indo-malayischen und Papuanischen Region. <em>Mitteilungen der Schweizerischen Entomologischen Gesellschaft / Bulletin of the SociÈtÈ Entomologique Suisse</em> <strong>16</strong>(2): 107-109</li>
                                        <li>Korschefsky, R. 1944. Neue altweltliche Coccinelliden (Coleoptera: Coccinellidae). <em>Arbeiten ¸ber Morphologische und Taxonomische Entomologie aus Berlin-Dahlem</em> <strong>11</strong>(1): 47-56</li>
                                        <li>Kugelann, J.G. 1794. Verzeichniss der in einigen Gegenden Preussens bis jetzt entdeckten K‰fer-Arten, nebst kurzen Nachrichten von denselben. <em>Neuestes Magazin f¸r die Liebhaber der Entomologie (Schneider)</em> <strong>1</strong>(5): 513-582</li>
                                        <li>Lea, A.M. 1902. Descriptions of new species of Australian Coleoptera. <em>Proceedings of the Linnean Society of New South Wales</em> <strong>1901</strong>: 481-513</li>
                                        <li>Lea, A.M. 1908. The Coleoptera of King Island, Bass Strait. <em>Proceedings of the Royal Society of Victoria</em> <strong>20</strong>: 143-207</li>
                                        <li>Lea, A.M. 1914. Insecta. <em>Transactions of the Royal Society of South Australia</em> <strong>38</strong>: 448-454</li>
                                        <li>Lea, A.M. 1925. Descriptions of new species of Australian Coleoptera. Part XVIII. <em>Proceedings of the Linnean Society of New South Wales</em> <strong>1</strong>(4): 414-431</li>
                                        <li>Lea, A.M. 1926. On some Australian Coleoptera collected by Charles Darwin during the Voyage of the "Beagle". <em>Journal and Proceedings of the Entomological Society of London</em> <strong>1926</strong>(2): 279-288</li>
                                        <li>Lea, A.M. 1929. Notes on some miscellaneous Coleoptera, with descriptions of new species. Part VII. <em>Transactions of the Royal Society of South Australia</em> <strong>53</strong>: 203-244</li>
                                        <li>Leach, W.E. 1815. Entomology. pp. 57ñ172 <em>in</em> Brewster (ed.). <em>The Edinburgh Encyclopedia</em>. Edinburgh : William Blackburn Vol. 9(1).</li>
                                        <li>Leng, C.W. 1920. <em>Catalogue of the Coleoptera of America, North of Mexico</em>. Mount Vernon, New York : John D. Sherman 470 pp.</li>
                                        <li>Li, C.S. 1993. Review of the Australian Epilachninae (Coleoptera: Coccinellidae). <em>Journal of the Australian Entomological Society</em> <strong>32</strong>(3): 209-224</li>
                                        <li>Li, C.S. in Li, C.S. & Cook, E.F. 1961. The Epilachninae of Taiwan (Col.: Coccinellidae). <em>Pacific Insects</em> <strong>3</strong>(1): 31ñ91</li>
                                        <li>Linnaeus, C. 1758. <em>Systema Naturae per Regna tria Naturae, secundem Classes, Ordines, Genera, Species, cum Characteribus, Differentis, Synonymis, Locis. Tom.1, Editio decima, reformata</em>. Holmiae : Laurentii Salvii 824 pp.</li>
                                        <li>MacLeay, W.S. in King, P.P. (ed.) 1826. <em>Narrative survey of the intertropical and western coasts of Australia performed between the years 1818 and 1822</em>. London : Murray Vol. 2.</li>
                                        <li>Montrouzier, P. 1857. Essai sur la fauna de l'Óle de Woodlark. <em>Annales de la SociÈtÈ d'Agriculture de Lyon</em> <strong>8</strong>(1856): 1ñ226</li>
                                        <li>Montrouzier, X. 1855. Essai sur la Faune l'Óle de Woodlark ou Moiou. <em>Annales des Sciences Physiques et Naturalles díAgriculture et díIndustrie</em> 2 <strong>7</strong>: 1-114</li>
                                        <li>Montrouzier, X. 1861. Essai sur la faune entomologique de la Nouvelle-CalÈdonie (Balade) ColÈoptËres (Fin). <em>Annales de la SociÈtÈ Entomologique de France</em> 4 <strong>1</strong>: 265-306</li>
                                        <li>Mulsant, M.E. 1846. <em>Histoire Naturelle des ColÈoptËres de France. Sulcicolles-SÈcuripalpes</em>. Paris : Maison xxiv + 26 pp. + 280 pp. + 1 pl.</li>
                                        <li>Mulsant, M.E. 1850. Species des ColÈoptËres TrimËres SÈcuripalpes. <i>Annales des Sciencies Physiques et Naturelles, d'Agriculture et d'Industrie, publiÈes par la SociÈtÈ nationale d'Agriculture, etc., de Lyon</I>, DeuxiËme SÈrie, 2: xv + 1-1104 pp (part 1 pp. 1-450; part 2 pp. 451-1104).</li>
                                        <li>Mulsant, M.E. 1853. SupplÈment a la Monographie des ColÈoptËres TrimerËs SÈcuripapes. <em>Annales de la SociÈtÈ LinnÈenne de Lyon (Nouvelle SÈrie)</em> 2 <strong>2</strong>(1852-1853): 129-333</li>
                                        <li>Mulsant, M.E. 1866. <em>Monographie des Coccinellides</em>. Paris : Savy et Deyrolle 292 pp.</li>
                                        <li>Oke, C.G. 1951. The Coleoptera of the Russell Grimwade Expedition. <em>Memoirs of the National Museum of Victoria</em> <strong>17</strong>: 19-25</li>
                                        <li>Oliff, S. 1895. Entomological notes ó a new friendly ladybird. <em>Agricultural Gazette of New South Wales</em> <strong>6</strong>: 30-31</li>
                                        <li>Pang, X-f. & Mao, J. 1975. Important natural enemies of the tetranychid mites ó <i>Stethorus </i>Weise. <em>Acta Entomologica Sinica</em> <strong>18</strong>: 418-424</li>
                                        <li>Poorani, J., Slipinski, A. & Booth, R.G. 2008. A revision of the genus <em>Synona</em> Pope, 1989 (Coleoptera: Coccinellidae: Coccinellini). <em>Annals of Zoology</em> <strong>58</strong>(3): 529-594</li>
                                        <li>Pope, R.D. 1954. Coleoptera: Coccinellidae from the Monte Bello Islands, 1952. <em>Proceedings of the Zoological Society of London</em> <strong>165</strong>(1): 127</li>
                                        <li>Pope, R.D. 1962. A review of the Pharini (Coleoptera: Coccinellidae). <em>Annals and Magazine of Natural History</em> 13 <strong>4</strong>: 627-640</li>
                                        <li>Pope, R.D. 1981. 'Rhyzobius ventralis' (Coleoptera: Coccinellidae), its constituent species, and their taxonomic and historical roles in biological control. <em>Bulletin of Entomological Research</em> <strong>71</strong>: 19-31</li>
                                        <li>Pope, R.D. & Lawrence, J.F. 1990. A review of <i>Scymnodes </i>Blackburn, with the description of a new Australian species and its larva (Coleoptera: Coccinellidae). <em>Systematic Entomology</em> <strong>15</strong>: 241-252</li>
                                        <li>Pope, R.D. [1988] 1989. A revision of the Australian Coccinellidae (Coleoptera). Part 1. Subfamily Coccinellinae. <em>Invertebrate Taxonomy</em> <strong>2</strong>: 633ñ735</li>
                                        <li>Redtenbacher, L. 1844. <em>Tetamen dispositionis generum et specierum Coleopterorum Pseudotrimeorum</em>. Vindobonae : Archiducatus Austriae 32 pp.</li>
                                        <li>Richards, A.M. 1983. The <i>Epilachna vigintioctopunctata</i> complex (Coleoptera: Coccinellidae). <em>International Journal of Entomology</em> <strong>25</strong>(1): 11-41</li>
                                        <li>Sasaji, H. 1971. <em>Coccinellidae (Insecta: Coleoptera). Fauna Japonica</em>. Tokyo : Academic Press of Japan 16 pls, 345 pp.</li>
                                        <li>Schˆnherr, C.J. 1808. <em>Synonyma Insectorum, oder: Versuch einer Synonymie aller bisher bekannten Insecten; nach Fabricii Systema Eleutheratorum &c. geordnet<I>. Erster Band. Eleutherata oder K‰fer. Zweiter Theil</I></em>. Stockholm : C.F. Marquard x + 424 pp. 1 pl.</li>
                                        <li>Sharp, D. 1889. Two new species of <i>Scymnus</I>. <em>Insect Life (Washington DC)</em> <strong>1</strong>: 364-365</li>
                                        <li>Sicard, A. 1928. Description de quelques espËces nouvelles de Coccinellides. <em>Annals and Magazine of Natural History</em> 10 <strong>1</strong>: 299-301</li>
                                        <li>SlipiÒski, A. & Dolambi, F. 2007. Revision of the Australian Coccinellidae (Coleoptera). Part 7. Genus <i>Bucolus </i>Mulsant. <em>Annales Zoologici Warszawa</em> <strong>57</strong>(4): 763ñ781</li>
                                        <li>Stephens, J.F. 1829. <em>A systematic catalogue of British Insects<I>: being an attempt to arrange all the hitherto discovered indigenous insects in accordance with their natural affinities. Containing also the references to every English writer on entomology, and to the principal foreign authors. With all the published British genera to the present time. Part 1</I></em>. London : Baldwin & Cradock xxxiv + 416 pp.</li>
                                        <li>Thunberg, C.O. 1781. Dissertatio Entomologica Novas Insectorum Species, sistens cujus partem primam, Cons. Exper. Facul. Med. Upsal., publice ventilandam exhibent praeses Carol. P. Thunberg, et respondens Samuel Nicol. Casstrˆm. Joh. Edman. Upsaliae. 28 pp.</li>
                                        <li>Timberlake, P.H. 1943. The Coccinellidae or ladybeetles of the Koebele collection ó part I. <em>Hawiian Planters' Record, Honolulu, Hawaii</em> <strong>47</strong>(1): 7-67</li>
                                        <li>Weise, J. 1885. Beschreibung einiger Coccinelliden. <em>Entomologische Zeitschrift</em> <strong>46</strong>: 227-241</li>
                                        <li>Weise, J. 1885. <em>Coccinellidae. Bestimmungstabellen der europ‰ischen Coleopteren. II. Auflage mit Ber¸cksichtung der Arten aus dem Nordlichen Asien</em>. Mˆdling 83 pp.</li>
                                        <li>Weise, J. 1892. Kleine Beitr‰ge zur Coccinelliden-Fauna Ost-Afrika's. <em>Deutsche Entomologische Zeitschrift</em> <strong>1892</strong>(1): 15-16</li>
                                        <li>Weise, J. 1895. Insectes du Bengale. 36e mÈmoire. Coccinellidae. <em>Annales de la SociÈtÈ Entomologique de Belgique</em> <strong>39</strong>: 151-157</li>
                                        <li>Weise, J. 1895. Neue Coccinelliden, sowie bemerkungen zu bekannten Arten. <em>Annales de la SociÈtÈ Entomologique de Belgique</em> <strong>39</strong>: 120-146</li>
                                        <li>Weise, J. 1895. ‹ber die mit <i>Novius </i>Muls. verwandten Gattungen. <em>Annales de la SociÈtÈ Entomologique de Belgique</em> <strong>39</strong>: 147-150</li>
                                        <li>Weise, J. 1897. Coccinellen aus Ostafrika (Usambara). <em>Deutsche Entomologische Zeitschrift</em> <strong>1897</strong>(2): 289-304</li>
                                        <li>Weise, J. 1898. Ueber bekannte und neue Coccinelliden. <em>Archiv f¸r Naturgeschichte. Berlin</em> <strong>64/1</strong>(2): 225-238</li>
                                        <li>Weise, J. 1901. Beschreibungen africanischer Chrysomeliden nebst synonymischen Bemerkungen. <em>Deutsche Entomologische Zeitschrift</em> <strong>1900</strong>: 446-459</li>
                                        <li>Weise, J. 1901. Coccinelliden aus Ceylon. <em>Deutsche Entomologische Zeitschrift</em> <strong>1900</strong>(2): 417-445</li>
                                        <li>Weise, J. 1902. Coccinelliden aus der Sammlung des Ungarischen National-Museums. <em>TermÈszetrajzi F¸zetek</em> <strong>25</strong>: 489-520</li>
                                        <li>Weise, J. 1908. <em>Die Fauna S¸dwest-Australiens<I>. Ergebnisse der Hamburger s¸dwest-australischen Forschungsreise 1905. Herausgegeben von Prof. Dr. W. Michaelsen und Dr. R. Hartmeyer. Chrysomelidae und Coccinellidae</I></em>. Jena : G. Fisher Vol. II(I) 13 pp.</li>
                                        <li>Weise, J. 1916. Results of Dr. E. Mjˆbergís Swedish Scientific Expedition to Australia 1910-1913. 11. Chrysomeliden und Coccinelliden aus West-Australien. <em>Arkiv fˆr Zoologi</em> <strong>10</strong>(20): 1-51, 1 pl.</li>
                                        <li>Weise, J. 1918. Chrysomeliden und Coccinelliden aus Nord-Neu-Guinea, gesammeld von Dr. P.N. van Kampen und K. Gjellerup, in den Jahren 1910 und 1911. <em>Tijdschrift voor Entomologie</em> <strong>60</strong>(1917): 192-224</li>
                                        <li>Weise, J. 1923. Results of Dr. E. Mjˆbergís Swedish Scientific Expedition to Australia 1910-1913. 31. Chrysomeliden und Coccinelliden aus Queensland. <em>Arkiv fˆr Zoologi</em> <strong>15</strong>(12): 1-150</li>
                                        <li>Weise, J. 1927. Uber bekannte und neue Chrysomeliden und Coccinelliden aus dem Reichsmuseum zu Stockholm. <em>Deutsche Entomologische Zeitschrift</em> <strong>18A</strong>(34): 1-34</li>
                                        <li>Weise, J. 1929. Westindische Chrysomeliden und Coccinelliden. <em>Zoologische Jahrb¸cher</em> <strong>16</strong>: 11-34</li>
                                        <li>Slipinski, A. 2004. Revision of the Australian Coccinellidae (Coleoptera). Part 2. Tribe Sticholotidini. <em>Annales Zoologici Warszawa</em> <strong>54</strong>(2): 389-402</li>
                                        <li>Slipinski, A. 2007. <em>Australian Ladybird Beetles (Coleoptera: Coccinellidae), Their biology and classification</em>. Canberra, ACT : Australian Biological Resources Study xviii 286 pp.</li>
                                        <li>Slipinski, A. & Burckhardt, D. 2006. Revision of the Australian Coccinellidae (Coleoptera). Part 5. Tribe Serangiini. <em>Annales Zoologici Warszawa</em> <strong>56</strong>(1): 37-58</li>
                                        <li>Slipinski, A. & Giorgi, J.A.. 2006. Revision of the Australian Coccinellidae (Coleoptera). Part 6. Tribe Chilocorini. <em>Annales Zoologici Warszawa</em> <strong>56</strong>(2): 265-304</li>
                                        <li>Slipinski, A. & Tomaszewska, K.W. 2005. Revision of the Australian Coccinellidae (Coleoptera). Part 5. Tribe Sukunahikonini. <em>Australian Journal of Entomology</em> <strong>44</strong>: 369-384</li>
                                        <li>Slipinski, A., Pang, H. & Pope, R.D. 2005. Revision of the Australian Coccinellidae (Coleoptera). Part 4. Tribe Telsimini. <em>Annales Zoologici Warszawa</em> <strong>55</strong>(2): 243-269</li>
                                    </ul>-->
                                </div>
                            </div><!---->
                        </div><!--close references-->
    </body>
</html>