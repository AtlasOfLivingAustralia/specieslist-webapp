<%--
  Species "show" view
  User: nick
  Date: 17/05/12
  Time: 11:10 AM
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.codehaus.groovy.grails.commons.ConfigurationHolder" %>
<g:set var="alaUrl" value="${ConfigurationHolder.config.ala.baseURL}"/>
<g:set var="biocacheUrl" value="${ConfigurationHolder.config.biocache.baseURL}"/>
<g:set var="spatialPortalUrl" value="${ConfigurationHolder.config.spatial.baseURL}"/>
<g:set var="sciNameFormatted"><bie:formatSciName name="${tc?.taxonConcept?.nameString}" rankId="${tc?.taxonConcept?.rankID?:0}"/></g:set>
<html>
<head>
    <meta name="layout" content="main" />
    <title>${tc?.taxonConcept?.nameString} : ${tc?.commonNames?.get(0)?.nameString} | Atlas of Living Australia</title>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'species.css')}" type="text/css" media="screen" />
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'colorbox.css')}" type="text/css" media="screen" />
    <script src="http://cdn.jquerytools.org/1.2.7/full/jquery.tools.min.js"></script><!-- tabs, etc. -->
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.colorbox-min.js')}"></script>
    <script type="text/javascript">
        $(document).ready(function(){
            // setup tabs
            $("ul.tabs").tabs("div.tabs-panes-noborder > section", { history: true, effect: 'fade' });
            // Gallery image popups using ColorBox
            $("a.thumbImage").colorbox({
                title: function() {
                    var titleBits = this.title.split("|");
                    return "<a href='"+titleBits[1]+"'>"+titleBits[0]+"</a>"; },
                opacity: 0.5,
                maxWidth: "80%",
                maxHeight: "80%",
                preloading: false,
                onComplete: function() {
                    $("#cboxTitle").html(""); // Clear default title div
                    var index = $(this).attr('id').replace("thumb",""); // get the imdex of this image
                    var titleHtml = $("div#thumbDiv"+index).html(); // use index to load meta data
                    //console.log("index", index, "titleHtml", titleHtml);
                    $("<div id='titleText'>"+titleHtml+"</div>").insertAfter("img.cboxPhoto");
                    $("div#titleText").css("padding-top","8px");
                    var cbox = $.fn.colorbox;
                    if ( cbox != undefined){
                        cbox.resize();
                    } else{
                        console.log("cboxis undefined 0: " + cbox);
                    }
                }
            });

            console.log("isRoleAdmin", "${isRoleAdmin}", "userName", "${userName}")
        });
    </script>
</head>
<body class="species">
    <header id="page-header">
        <div class="inner">
            <nav id="breadcrumb">
                <ol>
                    <li><a href="${alaUrl}">Home</a></li>
                    <li><a href="${alaUrl}/australias-species/">Australia&#8217;s species</a></li>
                    <li class="last"><bie:formatSciName name="${tc?.taxonConcept?.nameString}" rankId="${tc?.taxonConcept?.rankID?:0}"/></li>
                </ol>
            </nav>
            <hgroup>
                <h1><bie:formatSciName name="${tc?.taxonConcept?.nameString}" rankId="${tc?.taxonConcept?.rankID?:0}"/> <span>${tc?.taxonConcept?.author}</span></h1>
                <h2>${tc?.commonNames?.get(0)?.nameString}</h2>
            </hgroup>
        </div>
    </header>
    <div class="inner">
        <div class="col-narrow">
            <div class="boxed attached">
                <section class="meta">
                    <dl>
                        <dt>Name source</dt>
                        <dd><a href="${tc?.taxonConcept?.infoSourceURL}" target="_blank" class="external">${tc?.taxonConcept?.infoSourceName}</a></dd>
                        <dt>Rank</dt>
                        <dd style="text-transform: capitalize;">${tc?.taxonConcept?.rankString}</dd>
                        <dt>Data links</dt>
                        <dd><a href="#lsidText" id="lsid" class="button" title="Life Science Identifier (pop-up)">LSID</a></dd>
                        <dd><a href="/species/${tc?.taxonConcept?.guid}.json" class="button" title="JSON web service">JSON</a></dd>
                    </dl>
                    <div style="display:none;">
                        <div id="lsidText">
                            <h2><a href="http://lsids.sourceforge.net/" target="_blank">Life Science Identifier (LSID):</a></h2>
                            <p><a href="http://lsid.tdwg.org/summary/${tc?.taxonConcept?.guid}" target="_blank">${tc?.taxonConcept?.guid}</a></p>
                            <p>LSIDs are persistent, location-independent,resource identifiers for uniquely naming biologically significant resources including species names, concepts, occurrences, genes or proteins, or data objects that encode information about them. To put it simply, LSIDs are a way to identify and locate pieces of biological information on the web.</p>
                        </div>
                    </div>
                </section>
                <section class="status">
                    <h2>Species presence</h2>
                    <g:if test="${tc?.taxonConcept?.rankID >= 7000}">
                        <g:if test="${tc.isAustralian}">
                            <div><span class="native">&nbsp;</span>Recorded In Australia</div>
                        </g:if>
                        <g:else>
                            <div><span class="nonnative">&nbsp;</span>Not recorded In Australia</div>
                        </g:else>
                    </g:if>
                    <g:each var="habitat" in="${tc.habitats}">
                        <g:set var="divMarine">
                            <div><span class="marine">&nbsp;</span>Marine Habitats</div>
                        </g:set>
                        <g:set var="divTerrestrial">
                            <div><span class="terrestrial">&nbsp;</span>Terrestrial Habitats</div>
                        </g:set>
                        <g:if test="${habitat.status == 'M'}">${divMarine}</g:if>
                        <g:elseif test="${habitat.status == 'N'}">${divTerrestrial}</g:elseif>
                        <g:else>${divMarine} ${divTerrestrial}</g:else>
                    </g:each>
                </section>
                <section class="status">
                    <h2>Conservation status</h2>
                    <g:each var="status" in="${tc.conservationStatuses}">
                        <g:set var="regionCode" value="${status.region ?: "IUCN"}"/>
                        <div>
                            <a href="${ConfigurationHolder.config.collectory.threatenedSpeciesCodesUrl}/${statusRegionMap.get(regionCode)}" title="Threatened Species Codes - details"
                                onclick="window.open(this.href); return false;"><span class="iucn <bie:colourForStatus status="${status.status}"/>"><g:message
                                code="region.${regionCode}"/></span>${status.rawStatus}</a>
                        </div>
                    </g:each>
                    %{--<div><a href="http://www.ala.org.au/about/program-of-projects/sds/threatened-species-codes/#International" title="Threatened Species Codes - details" target="_blank"><span class="green">IUCN<!--LC--></span>Least Concern</a></div>--}%
                    %{--<div class="hide"><a href="http://www.ala.org.au/about/program-of-projects/sds/threatened-species-codes/#International" title="Threatened Species Codes - details" target="_blank"><span class="yellow">NT<!--LC--></span>Vulnerable</a></div>--}%
                    %{--<div class="hide"><a href="http://www.ala.org.au/about/program-of-projects/sds/threatened-species-codes/#International" title="Threatened Species Codes - details" target="_blank"><span class="red">WA<!--LC--></span>Extinct in the wild</a></div>--}%
                </section>
            </div>
        </div><!--col-narrow-->
        <div class="col-wide last">
            <ul class="tabs">
                <li><a id="t1" href="#overview">Overview</a></li>
                <li><a id="t2" href="#gallery">Gallery</a></li>
                <li><a id="t3" href="#names">Names</a></li>
                <li><a id="t4" href="#classification">Classification</a></li>
                <li><a id="t5" href="#records">Records</a></li>
                <li><a id="t6" href="#literature">Literature</a></li>
            </ul>
            <div class="tabs-panes-noborder">
                <section id="overview">
                    <div class="four-column">
                        <section class="double" id="divMap">
                            <h2>Mapped occurrence records</h2>
                            <div class="bg-white">
                                <img id="mapImage" src='http://biocache.ala.org.au/ws/density/map?q=lsid:"${tc.taxonConcept.guid}"' class="distroImg" width="316" alt="occurrence map" onerror="this.style.display='none'"/>
                                <img id="mapLegend" src='http://biocache.ala.org.au/ws/density/legend?q=lsid:"${tc.taxonConcept.guid}"' class="distroLegend" alt="map legend" onerror="this.style.display='none'"/>
                            </div>
                            <p><a class="button" href="${biocacheUrl}/occurrences/taxa/${tc.taxonConcept.guid}" title="View records list">View records list</a>
                                <a class="button" href="${spatialPortalUrl}/?q=lsid:${tc.taxonConcept.guid}" title="Map & analyse records">Map &amp; analyse records</a></p>
                        </section>
                        <section class="last">
                            <ul class="overviewImages">
                                <g:if test="${tc.taxonConcept?.rankID && tc.taxonConcept?.rankID < 7000}">%{-- higher taxa show mulitple images --}%
                                    <g:set var="imageLimit" value="6"/>
                                    <g:set var="imageSize" value="150"/>
                                    <g:each in="${extraImages}" var="searchTaxon" status="status">
                                        <g:set var="imageSrc" value="${searchTaxon.thumbnailUrl}"/>
                                        <g:if test="${status < imageLimit}">
                                            <li>
                                                <a id="popUp${status}" class="thumbImage1" href="${createLink(controller:'image-search', action: 'infoBox', params:[q: searchTaxon.guid])}" >
                                                    <img src="${searchTaxon.thumbnailUrl}" width="100px" height="100px" style="width:100px;height:100px;padding-right:3px;"/>
                                                </a>
                                            </li>
                                        </g:if>
                                    </g:each>
                                    <g:if test="${extraImages}">
                                        <li>
                                            <a href="#" onclick='javascript:window.location.href="${createLink(controller:'image-search', action: 'showSpecies', params:[taxonRank: tc?.taxonConcept?.rankString, scientificName: tc?.taxonConcept?.nameString])}${pageContext.request.contextPath}";'>
                                                View images of species for ${sciNameFormatted}</a>
                                        </li>
                                    </g:if>
                                </g:if>
                                <g:else>
                                    <g:set var="imageSize" value="314"/>
                                    <g:set var="image" value="${tc.images?.get(0)}"/>
                                    <g:set var="imageSrc" value="${image.repoLocation.replace('/raw.', '/smallRaw.')}"/>
                                    <li>
                                        <a href="0" title="Species representative photo"><img src="${imageSrc}" class="overviewImage" style="max-width: ${imageSize}px" alt="" /></a>
                                        <g:if test="${image.creator}">
                                            <cite>Image by: ${image.creator}
                                                <g:if test="${image.rights}">
                                                    <br/>Rights: ${image.rights}
                                                </g:if>
                                            %{--<br/><alatag:imageSourceURL image="${image}"/>--}%
                                            </cite>
                                        </g:if>
                                    </li>
                                </g:else>
                            </ul>
                            %{--<a href="0" title=""><img src="http://bie.ala.org.au/repo/1013/128/1284064/smallRaw.jpg" class="overviewImage" alt="" /></a>--}%
                            %{--<cite>Image by: Ian Sanderson--}%
                                %{--<br/>Rights: Attribution-NonCommercial License<br/>Source: --}%
                                %{--<a href="http://www.flickr.com/photos/iansand/3096843303/" target="_blank" onclick="javascript:window.location.href='http://www.flickr.com/photos/iansand/3096843303/';">Flickr EOL</a>--}%
                            %{--</cite>--}%
                            <a href="" class="button orange">Record a sighting</a>
                        </section>
                    </div>
                    <g:set var="descriptionBlock">
                        <g:set var="counter" value="${0}"/>
                        <g:each var="textProperty" in="${textProperties}" status="status">
                            <g:if test="${textProperty.name?.endsWith("hasDescriptiveText") && counter < 3 && textProperty.infoSourceId != 1051}">
                                <p>${textProperty.value} <cite>source:
                                    <a href="${textProperty.identifier}" class="external" target="_blank" title="${textProperty.title}">${textProperty.infoSourceName}</a></cite>
                                </p>
                                <g:set var="counter" value="${counter + 1}"/>
                            </g:if>
                        </g:each>
                    </g:set>
                    <g:if test="${descriptionBlock}">
                        <section class="clearfix">
                            <h2>Description</h2>
                            ${descriptionBlock}
                        </section>
                    </g:if>
                    <g:if test="${tc.identificationKeys}">
                        <h3>Identification Keys</h3>
                        <ul>
                            <g:each var="idKey" in="${tc.identificationKeys}">
                                <li>
                                    <a href="${idKey?.url}" target="_blank" class="external">${idKey?.title}</a>
                                    <g:if test="${idKey?.infoSourceURL}">(source: ${idKey?.infoSourceName})</g:if>
                                </li>
                            </g:each>
                        </ul>
                    </g:if>
                    <g:if test="${infoSources}">
                        <section id="resources">
                            <h2>Online resources</h2>
                            <ul>
                                <g:each var="is" in="${infoSources}" status="status">
                                %{--<g:set var="infoSource" value="${entry.value}"/>--}%<!--code>${is}</code-->
                                    <li><a href="${is.value?.infoSourceURL}" target="_blank" class="infosource">${is.value?.infoSourceName}</a>
                                        <ul>
                                            <li>
                                                <g:each in="${is.value?.sections}" var="s" status="i">
                                                    <g:set var="section"><g:message code="${s}"/></g:set>
                                                    ${section}${section && i < is.value?.sections.size() - 1?', ':''}
                                                </g:each>
                                                %{--${is.value?.sections.join(",")}--}%
                                            </li>
                                        </ul>
                                    </li>
                                </g:each>
                            </ul>
                        </section>
                    </g:if>
                    <g:elseif test="${infoSourceMap}">
                        <section id="resources">
                            <h2>Online resources</h2>
                            <ul>
                                <g:each var="ism" in="${infoSourceMap}" status="status">
                                %{--<g:set var="infoSource" value="${entry.value}"/>--}%<!--code>${ism}</code-->
                                    <li><a href="${ism.key}" target="_blank" class="infosource">${ism.value?.name}</a>
                                        <ul>
                                            <li>
                                                <g:each in="${ism.value?.sections}" var="s" status="i">
                                                    <g:set var="section"><g:message code="${s}"/></g:set>
                                                    ${section}${section && i < ism.value?.sections.size() - 1?', ':''}
                                                </g:each>
                                                %{--${is.value?.sections.join(",")}--}%
                                            </li>
                                        </ul>
                                    </li>
                                </g:each>
                            </ul>
                        </section>
                    </g:elseif>
                </section><!--#overview-->
                <section id="gallery">
                    <g:if test="${tc.images}">
                        <h2>Images</h2>
                        <div id="imageGallery">
                            <g:each var="image" in="${tc.images}" status="status">
                                <g:set var="imageUri">
                                    <g:if test="${image.repoId}">images/${image.repoId}.jpg</g:if>
                                    <g:elseif test="${false && image.documentId}">images/${image.documentId}.jpg</g:elseif>
                                    <g:else>${image.repoLocation}</g:else>
                                </g:set>
                                <a class="thumbImage" rel="thumbs" title="${image.title?:''}" href="${imageUri}"
                                   id="thumb${status}"><img src="${image.thumbnail}" alt="${image.infoSourceName}"
                                                                  alt="${image.title}" width="100px" height="100px"
                                                                  style="width:100px;height:100px;padding-right:3px;"/></a>
                                <div id="thumbDiv${status}" style="display:none;">
                                    <g:if test="${image.title}">
                                        ${image.title}<br/>
                                    </g:if>
                                    <g:if test="${image.creator}">
                                        Image by: ${image.creator}<br/>
                                    </g:if>
                                    <g:if test="${image.locality}">
                                        Locality: ${image.locality}<br/>
                                    </g:if>
                                    <g:if test="${image.licence}">
                                        Licence: ${image.licence}<br/>
                                    </g:if>
                                    <g:if test="${image.rights}">
                                        Rights: ${image.rights}<br/>
                                    </g:if>
                                    <g:set var="imageUri">
                                        <g:if test="${image.isPartOf && !image.occurrenceUid}">
                                            ${image.isPartOf}
                                        </g:if>
                                        <g:elseif test="${image.identifier}">
                                            ${image.identifier}
                                        </g:elseif>
                                        <g:else>
                                            ${image.infoSourceURL}
                                        </g:else>
                                    </g:set>
                                    <g:if test="${image.infoSourceURL == 'http://www.ala.org.au'}">
                                        <cite>Source: ${image.infoSourceName}</cite>
                                    </g:if>
                                    <g:elseif test="${image.infoSourceURL == 'http://www.elfram.com/'}">
                                        <cite>Source: <a href="${image.infoSourceURL}" target="_blank" class="external">${image.infoSourceName}</a></cite>
                                    </g:elseif>
                                    <g:else>
                                        <cite>Source: <a href="${imageUri}" target="_blank" class="external">${image.infoSourceName}</a></cite>
                                    </g:else>
                                    <g:if test="${image.occurrenceUid}">
                                        <a href="http://biocache.ala.org.au/occurrences/${image.occurrenceUid}" target="_blank">View more details for this image</a>
                                    </g:if>
                                    <g:if test="${!isReadOnly}">
                                        <p class="imageRank-${image.documentId}">
                                            %{--<cite>--}%
                                                <g:if test="${rankedImageUris?.contains(image.identifier)}">
                                                    You have ranked this image as
                                                    <g:if test="${!rankedImageUriMap[image.identifier]}">
                                                        NOT
                                                    </g:if>
                                                    representative of ${tc.taxonConcept.nameString}
                                                </g:if>
                                                <g:else>
                                                        Is this image representative of ${tc.taxonConcept.rankString}?
                                                        <a class="isrepresent"
                                                           href="javascript:rankThisImage('${tc.taxonConcept.guid}','${image.identifier}','${image.infoSourceId}','${image.documentId}',false,true,'${tc.taxonConcept.nameString}');">
                                                            YES
                                                        </a> |
                                                        <a class="isnotrepresent"
                                                           href="javascript:rankThisImage('${tc.taxonConcept.guid}','${image.identifier}','${image.infoSourceId}','${image.documentId}',false,false,'${tc.taxonConcept.nameString}');">
                                                            NO
                                                        </a>
                                                        <g:if test="${isRoleAdmin}">
                                                            <br/><a class="isnotrepresent"
                                                               href="javascript:rankThisImage('${tc.taxonConcept.guid}','${image.identifier}','${image.infoSourceId}','${image.documentId}',true,false,'${tc.taxonConcept.nameString}');">
                                                                BlackList</a> |
                                                            <a class="isnotrepresent" href="#"
                                                               onClick="editThisImage('${tc.taxonConcept.guid}', '${image.identifier}');
                                                               return false;">Edit</a>
                                                        </g:if>
                                                </g:else>
                                            %{--</cite>--}%
                                        </p>
                                    </g:if>
                                    <g:else>
                                        <p class="imageRank-${image.documentId}">
                                            <b>Read Only Mode</b>
                                        </p>
                                    </g:else>
                                </div>
                            </g:each>
                        </div>
                    </g:if>
                    <g:if test="${tc.screenshotImages}">
                        <h2 style="margin-top:20px;">Videos</h2>
                        <div id="videosGallery">
                            <g:each var="screenshot" in="${tc.screenshotImages}" status="status">
                                <g:set var="thumbUri">${screenshot.repoLocation}</g:set>
                                <g:set var="screenshotUri"><g:if
                                        test="${screenshot.identifier}">${screenshot.identifier}</g:if><g:elseif
                                        test="${screenshot.isPartOf}">${screenshot.isPartOf}</g:elseif><g:else>${screenshot.infoSourceURL}</g:else></g:set>
                                <table>
                                    <tr>
                                        <td>
                                            <a class="screenshotThumb" title="${screenshot.title}" href="${screenshotUri}" target="_blank"
                                               class="external"><img src="${thumbUri}" alt="${screenshot.infoSourceName}" title="${imageTitle}"
                                                                     width="120px" height="120px" style="width:120px;height:120px;padding-right:3px;"/></a>
                                        </td>
                                        <td>
                                            <g:if test="${screenshot.title}">
                                                ${screenshot.title}<br/>
                                            </g:if>
                                            <g:if test="${screenshot.creator}">
                                                Video by: ${screenshot.creator}<br/>
                                            </g:if>
                                            <g:if test="${screenshot.locality}">
                                                Locality: ${screenshot.locality}<br/>
                                            </g:if>
                                            <g:if test="${screenshot.licence}">
                                                Licence: ${screenshot.licence}<br/>
                                            </g:if>
                                            <g:if test="${screenshot.rights}">
                                                Rights: ${screenshot.rights}<br/>
                                            </g:if>
                                            Source: <a href="${screenshotUri}" target="_blank" class="external">${screenshot.infoSourceName}</a>
                                        </td>
                                    </tr>
                                </table>
                            </g:each>
                        </div>
                    </g:if>
                </section><!--#gallery-->
                <section id="names">
                    <h2>Names and sources</h2>
                </section><!--#names-->
                <section id="classification">
                    <h2>Scientific classification</h2>
                </section><!--classificatio-->
                <section id="records">
                    <h2>Occurrence records</h2>
                </section><!--#records-->
                <section id="literature">
                    <h2>Name references in literature</h2>
                </section><!--#literature-->
            </div><!--tabs-panes-noborder-->
        </div><!--col-wide last-->
    </div><!--inner-->
</body>
</html>