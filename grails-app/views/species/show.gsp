%{--
  - Copyright (C) 2012 Atlas of Living Australia
  - All Rights Reserved.
  -
  - The contents of this file are subject to the Mozilla Public
  - License Version 1.1 (the "License"); you may not use this file
  - except in compliance with the License. You may obtain a copy of
  - the License at http://www.mozilla.org/MPL/
  -
  - Software distributed under the License is distributed on an "AS
  - IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  - implied. See the License for the specific language governing
  - rights and limitations under the License.
  --}%
<%--
  Species "show" view
  User: nick
  Date: 17/05/12
  Time: 11:10 AM
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<g:set var="alaUrl" value="${grailsApplication.config.ala.baseURL}"/>
<g:set var="biocacheUrl" value="${grailsApplication.config.biocache.baseURL}"/>
<g:set var="speciesListUrl" value="${grailsApplication.config.speciesList.baseURL}"/>
<g:set var="spatialPortalUrl" value="${grailsApplication.config.spatial.baseURL}"/>
<g:set var="collectoryUrl" value="${grailsApplication.config.collectory.baseURL}"/>
<g:set var="citizenSciUrl" value="${grailsApplication.config.brds.guidUrl}"/>
<g:set var="guid" value="${tc?.previousGuid?:tc?.taxonConcept?.guid?:''}"/>
<g:set var="sciNameFormatted"><bie:formatSciName name="${tc?.taxonConcept?.nameString}" rankId="${tc?.taxonConcept?.rankID?:0}"/></g:set>
<g:set var="synonymsQuery"><g:each in="${tc?.synonyms}" var="synonym" status="i">\"${synonym.nameString}\"<g:if test="${i < tc.synonyms.size() - 1}"> OR </g:if></g:each></g:set>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />
    <title>${tc?.taxonConcept?.nameString} ${(tc?.commonNames) ? ' : ' + tc?.commonNames?.get(0)?.nameString : ''} | Atlas of Living Australia</title>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'species.css')}" type="text/css" media="screen" />
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'colorbox.css')}" type="text/css" media="screen" />
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'snazzy.css')}" type="text/css" media="screen" />
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery.fancybox.css')}" type="text/css" media="screen" />
    <script src="${resource(dir: 'js', file: 'jquery.tools.min.js')}"></script><!-- tabs, etc. -->
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.htmlClean.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.colorbox-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.fancybox.pack.js')}"></script>
    <script type="text/javascript" src="http://ajax.googleapis.com/jsapi"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.jsonp-2.3.1.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'charts2.js')}"></script>
    <script type="text/javascript">
        // load google charts api
        google.load("visualization", "1", {packages:["corechart"]});

        // global var to pass GSP vars into JS file
        SHOW_CONF = {
            biocacheUrl:    "${biocacheUrl}",
            collectoryUrl:  "${collectoryUrl}",
            guid:           "${guid}",
            scientificName: "${tc?.taxonConcept?.nameString?:''}",
            synonymsQuery:  "${synonymsQuery}",
            citizenSciUrl:  "${citizenSciUrl}",
            serverName:     "${grailsApplication.config.grails.serverURL}",
            bieUrl:         "${grailsApplication.config.bie.baseURL}",
            alertsUrl:      "${grailsApplication.config.alerts.baseUrl}",
            remoteUser:     "${request.remoteUser?:''}"
        }
    </script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'species.show.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'trove.js')}"></script>
</head>
<body class="species content">
    <header id="page-header">
        <div class="inner row-fluid">
            <div id="breadcrumb" class="span12">
                <ol class="breadcrumb">
                    <li><a href="${alaUrl}">Home</a> <span class=" icon icon-arrow-right"></span></li>
                    <li><a href="${alaUrl}/australias-species/">Australia&#39;s species</a> <span class=" icon icon-arrow-right"></span></li>
                    <li class="active"><bie:formatSciName name="${tc?.taxonConcept?.nameString}" rankId="${tc?.taxonConcept?.rankID?:0}"/></li>
                </ol>
            </div>
        </div>
        <hgroup class="row-fluid">
            <div class="span8">
                <h1><bie:formatSciName name="${tc?.taxonConcept?.nameString}" rankId="${tc?.taxonConcept?.rankID?:0}"/>
                    <span>${tc?.taxonConcept?.author?:""}</span></h1>
                <h2>${(tc?.commonNames) ? tc?.commonNames?.opt(0)?.nameString : '<br/>'}</h2>
            </div>
            <div class="span4" id="actionButtons">
                <a href="${citizenSciUrl}${guid}" class="btn btn-ala" title="Record a sighting">Record a sighting</a>
                <a id="alertsButton" class="btn btn-ala" href="#">Alerts <i class="icon-bell icon-white"></i></a>
            </div>
        </hgroup>
    </header>
    <div class="row-fluid">
        <div class="span3">
            <div class="well">
                <section class="meta">
                    <dl>
                        <dt>Name source</dt>
                        <dd><a href="${tc?.taxonConcept?.infoSourceURL}" target="_blank" class="external">${tc?.taxonConcept?.infoSourceName}</a></dd>
                        <dt>Rank</dt>
                        <dd style="text-transform: capitalize;">${tc?.taxonConcept?.rankString}</dd>
                        <dt>Data links</dt>
                        <dd><a href="#lsidText" id="lsid" class="btn btn-small" title="Life Science Identifier (pop-up)">LSID</a>
                            <a href="#dataLinksText" id="dataLinks" class="btn btn-small" title="JSON web service">JSON / <g:if test="${tc?.taxonConcept?.rankID?:1 % 1000 == 0}">WMS /</g:if>RDF</a>
                        </dd>
                        %{--<dt>Related pages</dt>--}%
                        %{--<dd><a class="button" href="${biocacheUrl}/occurrences/taxa/${guid}" title="View occurrence records for ${sciNameFormatted}">Species records</a></dd>--}%
                        %{--<dd><a class="button" href="${spatialPortalUrl}/?q=lsid:%22${guid}%22&cm=geospatial_kosher" title="GIS analysis of occurrence records for ${sciNameFormatted}">Spatial analysis</a></dd>--}%
                    </dl>
                    <div class="hide">
                        <div id="lsidText">
                            <h2><a href="http://lsids.sourceforge.net/" target="_blank" title="More information on LSIDs">Life Science Identifier (LSID):</a></h2>
                            <p><a href="http://lsid.tdwg.org/summary/${guid}" target="_blank" title="Original source for this LSID">${guid}</a></p>
                            <p>LSIDs are persistent, location-independent,resource identifiers for uniquely naming biologically significant resources including species names, concepts, occurrences, genes or proteins, or data objects that encode information about them. To put it simply, LSIDs are a way to identify and locate pieces of biological information on the web.</p>
                        </div>

                        <div id="dataLinksText" style="text-align: left;">
                            <h1>Data Links</h1>
                            <h2>JSON</h2>
                            <p>
                                For a JSON view of this data, click <a href="${grailsApplication.config.ala.bie.baseURL}/ws/species/${tc?.taxonConcept?.guid}.json">here</a>
                            </p>
                            <g:if test="${tc?.taxonConcept?.rankID?:1 % 1000 == 0}">
                            <h2>WMS</h2>
                            <p>
                                To use WMS services, copy and paste the following GetCapabilities URL into your
                                OGC client (e.g. <a href="http://udig.refractions.net">uDIG</a>,
                                    <a href="http://www.esri.com/software/arcgis">ESRI ArcGIS</a>) <br/>
                                <a href="http://biocache.ala.org.au/ws/ogc/ows?q=${tc?.taxonConcept?.rankString}:${tc?.taxonConcept?.nameString}">
                                    http://biocache.ala.org.au/ws/ogc/ows?q=${tc?.taxonConcept?.rankString}:${tc?.taxonConcept?.nameString}
                                </a><br/>
                                For higher taxa, this will give you a hierarchical listing of layers for each taxon.
                            </p>
                            </g:if>

                            <h2>RDF</h2>
                            <p>
                                To download an RDF/XML document for the concepts and names click
                                <a href="http://biodiversity.org.au/taxon/${tc?.taxonConcept?.nameString}.rdf">here</a><br/>
                                A JSON view of this information is here
                                <a href="http://biodiversity.org.au/taxon/${tc?.taxonConcept?.nameString}.json">here</a> <br/>
                                A html view of this information is here
                                <a href="http://biodiversity.org.au/taxon/${tc?.taxonConcept?.nameString}">here</a>
                            </p>

                            <h2>Further details</h2>
                            <p> For more details on occurrence webservices, <a href="http://biocache.ala.org.au/ws">click here</a><br/>
                                For more details on names webservices, click <a href="http://biodiversity.org.au">here</a>
                            </p>
                        </div>
                    </div>

                </section>
                <g:if test="${tc.habitats || tc?.taxonConcept?.rankID?:0 >= 7000 && tc.isAustralian}">
                    <section class="status">
                        <dl>
                            <dt>Species presence</dt>
                            <g:if test="${tc?.taxonConcept?.rankID?:0 >= 7000}">
                                <g:set var="isAussie" value=""/>
                                <g:if test="${isAustralian != null}">
                                    <g:set var="isAussie" value="${isAustralian}"/>
                                </g:if>
                                <g:else>
                                    <g:set var="isAussie" value="${tc.isAustralian}"/>
                                </g:else>
                                <g:if test="${isAussie}">
                                    <dd><span class="native">&nbsp;</span>Recorded In Australia</dd>
                                </g:if>
                                <g:else>
                                    <dd><span class="nonnative">&nbsp;</span>Not recorded In Australia</dd>
                                </g:else>
                            </g:if>
                            <g:each var="habitat" in="${tc.habitats}">
                                <g:set var="divMarine">
                                    <dd><span class="marine">&nbsp;</span>Marine Habitats</dd>
                                </g:set>
                                <g:set var="divTerrestrial">
                                    <dd><span class="terrestrial">&nbsp;</span>Terrestrial Habitats</dd>
                                </g:set>
                                <g:set var="divLimnetic">
                                    <dd><span class="terrestrial-aquatic">&nbsp;</span>Terrestrial Aquatic Habitats</dd>
                                </g:set>
                                <g:if test="${habitat.status == 'M'}">${divMarine}</g:if>
                                <g:elseif test="${habitat.status == 'N'}">${divTerrestrial}</g:elseif>
                                <g:elseif test="${habitat.status == 'Limnetic'}">${divLimnetic}</g:elseif>
                                <g:else>${divMarine} ${divTerrestrial}</g:else>
                            </g:each>
                        </dl>
                    </section>
                </g:if>
                <g:if test="${tc.conservationStatuses}">
                    <section class="status">
                        <dl>
                            <dt>Conservation status</dt>
                            <g:each var="status" in="${tc.conservationStatuses}">
                                <g:set var="regionCode" value="${status.region ?: "IUCN"}"/>
                                <dd>
                                    <a href="${grailsApplication.config.collectory.threatenedSpeciesCodesUrl}/${statusRegionMap.get(regionCode)}" title="Threatened Species Codes - details"
                                       onclick="window.open(this.href); return false;"><span class="iucn <bie:colourForStatus status="${status.status}"/>"><g:message
                                            code="region.${regionCode}"/></span>${status.rawStatus}</a>
                                </dd>
                            </g:each>
                        </dl>
                    </section>
                </g:if>
                <g:if test="${tc.categories}">
                    <section class="status">
                        <dl>
                            <dt>Categories</dt>
                            <dd>
                                <table class="status">
                                    <g:each var="category" in="${tc.categories}">
                                        <div>
                                            <g:set var="catURL" value="${category.identifier?:category.infoSourceURL}" />
                                            <tr>
                                                <g:if test="${category.stateProvince}">
                                                    <td><a href="${catURL}" title="Category details" onclick="window.open(this.href); return false;">
                                                        <span class="iucn category"><g:message
                                                                code="region.${category.stateProvince}"/></span></a></td></g:if>
                                                <td><a href="${catURL}" title="Category details" onclick="window.open(this.href); return false;">
                                                    ${category.category}
                                                </a></td>
                                            </tr>
                                        </div>
                                    </g:each>
                                </table>
                            </dd>
                        </dl>
                    </section>
                </g:if>
            </div>
        </div><!--col-narrow-->
        <div class="span9">
            <div class="tabbable tabs-belowZ">
                <ul class="nav nav-tabs">
                    <li class="active"><a id="t1" href="#overview" data-toggle="tab">Overview</a></li>
                    <g:if test="${tc.taxonConcept?.rankID?:0 >= 6000}"><li><a id="t2" href="#gallery" data-toggle="tab">Gallery</a></li></g:if>
                    <li><a id="t3" href="#names" data-toggle="tab">Names</a></li>
                    <li><a id="t4" href="#classification" data-toggle="tab">Classification</a></li>
                    <li><a id="t5" href="#records" data-toggle="tab">Records</a></li>
                    <li id="bhl"><a id="t6" href="#literature" data-toggle="tab">Literature</a></li>
                </ul>
            </div>
            <div class="tab-content ">
                <section  class="tab-pane active" id="overview">
                    <div class="four-column">
                        <section class="" id="divMap">
                            <div id="expertDistroDiv" style="display:none;margin-bottom: 10px;">
                                <h2>Compiled distribution map</h2>
                                <img id="distroMapImage" src="${resource(dir: 'images', file: 'noImage.jpg')}" class="distroImg" width="316" alt="occurrence map" onerror="this.style.display='none'"/>
                                <div class="mapAttribution">Compiled distribution map provided by <span id="dataResource">[data resource not known]</span></div>
                            </div>
                            <h2>Occurrence records map</h2>
                            <div class="bg-white">
                                <g:set var="spatialQuery" value="lsid:%22${guid}%22%20AND%20geospatial_kosher:true"/>
                                <img id="mapImage" src="http://biocache.ala.org.au/ws/density/map?q=${spatialQuery}" class="distroImg" width="316" alt="occurrence map" onerror="this.style.display='none'"/>
                                <img id="mapLegend" src="http://biocache.ala.org.au/ws/density/legend?q=${spatialQuery}" class="distroLegend" alt="map legend" onerror="this.style.display='none'"/>
                            </div>
                            <p>
                                <a class="button" href="${biocacheUrl}/occurrences/taxa/${guid}" title="View records list">View records list</a>
                                <a class="button" href="${spatialPortalUrl}/?q=lsid:%22${guid}%22&cm=geospatial_kosher" title="Map & analyse records">Map &amp; analyse records</a>
                            </p>
                        </section>
                        <section class="" id="overviewImage">
                            <ul class="overviewImages">
                                <g:if test="${extraImages}">
                                    <g:set var="imageSearchUrl" value="${createLink(controller:'image-search', action: 'showSpecies', params:[taxonRank: tc?.taxonConcept?.rankString, scientificName: tc?.taxonConcept?.nameString])}" />
                                    <li>
                                        <a href="${imageSearchUrl}" class="btn">
                                            View images of species for ${sciNameFormatted}</a>
                                    </li>
                                </g:if>
                                <g:if test="${tc.taxonConcept?.rankID && tc.taxonConcept?.rankID < 7000}">%{-- higher taxa show mulitple images --}%
                                    <g:set var="imageLimit" value="${3}"/>
                                    <g:set var="imageSize" value="150"/>
                                    <g:each in="${extraImages?.searchDTOList}" var="searchTaxon" status="status">
                                        <g:set var="imageSrc" value="${searchTaxon.smallImageUrl}"/>
                                        <g:if test="${status < imageLimit}">
                                            <li>
                                                <a href="${imageSearchUrl}" class="thumbImageBrowse" title="Browse images of species for ${sciNameFormatted}">
                                                    <img src="${searchTaxon.smallImageUrl?:searchTaxon.thumbnail}" class="overviewImage"  style="width:100%;max-width:314px;"/>
                                                </a>
                                            </li>
                                        </g:if>
                                    </g:each>
                                </g:if>
                                <g:else>
                                    <g:set var="imageSize" value="314"/>
                                    <g:set var="gotOne" value="${false}"/>
                                <%--Iterate over images and check image is not black listed--%>
                                    <g:each var="image" in="${tc.images}" status="status">
                                        <g:if test="${!image.isBlackListed && !gotOne}">
                                            <g:set var="gotOne" value="${true}"/>
                                            <g:set var="imageSrc" value="${image.smallImageUrl?:image.repoLocation?.replace('/raw.', '/smallRaw.')}"/>
                                            <li>
                                                <a href="${image.repoLocation}" id="thumb0" class="thumbImage"
                                                   title="Species representative photo"><img src="${image.smallImageUrl}"
                                                                                             class="overviewImage"
                                                                                             style="width:100%;max-width:${imageSize}px"
                                                                                             alt="representative image of taxa" /></a>
                                                <cite>
                                                    <g:if test="${image.licence}">
                                                        <br/>Source: ${image.infoSourceName}
                                                    </g:if>
                                                    <g:if test="${image.creator}">
                                                        <br/>Image by: <bie:lookupUserName id="${image.creator}"/>
                                                    </g:if>
                                                    <g:if test="${image.rights}">
                                                        <br/>Rights: ${image.rights}
                                                    </g:if>
                                                    <g:if test="${false && image.licence}">
                                                        <br/>Licence: ${image.licence}
                                                    </g:if>
                                                </cite>
                                            </li>
                                        </g:if>
                                    </g:each>
                                </g:else>
                            </ul>
                        </section>
                    </div>
                    <g:set var="descriptionBlock">
                        <g:set var="counter" value="${0}"/>
                        <g:each var="textProperty" in="${textProperties}" status="status">
                            <g:if test="${textProperty.name?.endsWith("hasDescriptiveText") && counter < 3 && textProperty.infoSourceId != '1051'}">
                                <g:set var="counter" value="${counter + 1}"/>
                                <p>${textProperty.value} <cite>source:
                                    <a href="${textProperty.identifier}" class="external" target="_blank" title="${textProperty.title}">${textProperty.infoSourceName}</a></cite>
                                </p>
                            </g:if>
                        </g:each>
                    </g:set>
                    <g:if test="${descriptionBlock?.trim().length() > 0}">
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
                        <section id="resources" class="clearfix">
                            <h2>Online resources</h2>
                            <dd>
                                <g:each var="is" in="${infoSources}" status="status">
                                %{--<g:set var="infoSource" value="${entry.value}"/>--}%<!--code>${is}</code-->
                                    <dt><a href="${is.value?.infoSourceURL}" target="_blank" class="infosource">${is.value?.infoSourceName}</a></dt>
                                    <dd>
                                        <g:each in="${is.value?.sections}" var="s" status="i">
                                            <g:set var="section"><g:message code="${s}"/></g:set>
                                            ${section}${section && i>1 && is.value?.sections?.size()>1 ?', ':''}
                                        </g:each>
                                        %{--${is.value?.sections.join(",")}--}%
                                    </dd>
                                </g:each>
                            </dd>
                        </section>
                    </g:if>
                    <g:elseif test="${infoSourceMap}">
                        <section id="resources" class="clearfix">
                            <h2>Online resources</h2>
                            <dl>
                                <g:each var="ism" in="${infoSourceMap}" status="status">
                                %{--<g:set var="infoSource" value="${entry.value}"/>--}%<!--code>${ism}</code-->
                                    <dt><a href="${ism.key}" target="_blank" class="infosource">${ism.value?.name}</a></dt>
                                    <dd>
                                        <g:set var="sections" value="${ism.value?.sections?.minus(["hasOccurrenceRowKey","hasImageLicenseInfo"])}"/>
                                        <g:each in="${sections}" var="s" status="i">
                                            <g:set var="section"><g:message code="${s}"/></g:set>
                                            ${section}${section && i < sections.size() - 1?', ':''}
                                        </g:each>
                                        %{--${is.value?.sections.join(",")}--}%
                                    </dd>
                                </g:each>
                            </dl>
                        </section>
                    </g:elseif>
                    <g:if test="${speciesList}">
                        <h2>Species Lists</h2>
                        <dl id="speciesLists">
                            <g:each in="${speciesList}" var="list">
                                <dt>
                                    <a href="${speciesListUrl}/speciesListItem/list/${list.dataResourceUid}">${list?.list?.listName}</a>
                                </dt>
                                <g:if test="${list.kvpValues}">
                                    <g:each in="${list.kvpValues}" var="kvp">
                                        <dd>
                                            <span>${kvp.key}:</span>
                                                ${kvp.vocabValue?:kvp.value}
                                        </dd>
                                    </g:each>
                                </g:if>
                            </g:each>
                        </dl>

                    </g:if>
                    </table>
                </section><!--#overview-->
                <g:if test="${tc.taxonConcept?.rankID?:0 >= 6000}">
                    <section class="tab-pane" id="gallery">
                        <g:if test="${tc.images}">
                            <h2>Images</h2>
                            <div id="imageGallery">
                                <g:each var="image" in="${tc.images}" status="status">
                                    <g:if test="${!image.isBlackListed}">
                                        <a class="thumbImage" rel="thumbs" title="${image.title?:''}" href="${image.largeImageUrl}"
                                           id="thumb${status}"><img src="${image.smallImageUrl}" alt="${image.infoSourceName}"
                                                                          alt="${image.title}" height="100px"
                                                                          style="height:100px;padding-right:3px;"/></a>
                                        <div id="thumbDiv${status}" style="display:none;">
                                            <g:if test="${image.title}">
                                                <span class="imageTitle">${image.title}</span><br/>
                                            </g:if>
                                            <g:if test="${image.creator}">
                                                <span class="imageMetadataField">Image by:</span>
                                                <bie:lookupUserName id="${image.creator}"/><br/>
                                            </g:if>
                                            <g:if test="${image.locality}">
                                                <span class="imageMetadataField">Locality:</span> ${image.locality}<br/>
                                            </g:if>
                                            <g:if test="${image.licence}">
                                                <span class="imageMetadataField">Licence:</span> ${image.licence}<br/>
                                            </g:if>
                                            <g:if test="${image.rights}">
                                                <span class="imageMetadataField">Rights:</span> ${image.rights}<br/>
                                            </g:if>
                                            <!-- Flickr images need to use identifier instead of isPartOf for the imageUri -->
                                            <g:set var="imageUri">
                                                <g:if test="${image.isPartOf && !image.identifier?.startsWith('http://www.flickr.com') }">
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
                                                <cite><span class="imageMetadataField">Source:</span> ${image.infoSourceName}</cite>
                                            </g:if>
                                            <g:elseif test="${image.infoSourceURL == 'http://www.elfram.com/'}">
                                                <cite><span class="imageMetadataField">Source:</span> <a href="${image.infoSourceURL}" target="_blank" class="external">${image.infoSourceName}</a></cite>
                                            </g:elseif>
                                            <g:else>
                                                <cite><span class="imageMetadataField">Source:</span> <a href="${imageUri}" target="_blank" class="external">${image.infoSourceName}</a></cite>
                                            </g:else>
                                            <g:if test="${image.occurrenceUid}">
                                                <p><a href="http://biocache.ala.org.au/occurrences/${image.occurrenceUid}" target="_blank">View more details for this image</a></p>
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
                                                                   href="#" onclick="rankThisImage('${guid}','${image.identifier}','${image.infoSourceId}','${image.documentId}',false,true,'${tc.taxonConcept.nameString}');return false;">
                                                                    YES
                                                                </a> |
                                                                <a class="isnotrepresent" href="#"
                                                                        onclick="rankThisImage('${guid}','${image.identifier}','${image.infoSourceId}','${image.documentId}',false,false,'${tc.taxonConcept.nameString}');return false;">
                                                                    NO
                                                                </a>
                                                                <g:if test="${isRoleAdmin}">
                                                                    <br/><a class="isnotrepresent" href="#"
                                                                            onclick="rankThisImage('${guid}','${image.identifier}','${image.infoSourceId}','${image.documentId}',true,false,'${tc.taxonConcept.nameString}');return false;">
                                                                        BlackList</a> |
                                                                    <a class="isnotrepresent" href="#" onClick="editThisImage('${guid}', '${image.identifier}');return false;">Edit</a>
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
                                    </g:if>
                                </g:each>
                            </div>
                        </g:if>
                        <g:elseif test="${tc.screenshotImages}">
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
                        </g:elseif>
                        <g:else>
                            <p>There are no images for this taxon</p>
                        </g:else>
                    </section><!--#gallery-->
                </g:if>
                <section class="tab-pane" id="names">
                    <h2>Names and sources</h2>
                    <table class="table table-condensed">
                        <thead>
                            <tr>
                                <th>Accepted name</th>
                                <th>Source</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>${sciNameFormatted} ${authorship}</td>
                                <td class="source">
                                    <ul><li><a href="${tc.taxonConcept.infoSourceURL}" target="_blank" class="external">${tc.taxonConcept.infoSourceName}</a></li></ul>
                                </td>
                            </tr>
                            <g:if test="${(tc.taxonName && tc.taxonName.publishedIn) || tc.taxonConcept.publishedIn}">
                                <tr class="cite">
                                    <td colspan="2">
                                        <cite>Published in: <a href="#" target="_blank" class="external">${tc.taxonName?.publishedIn?:tc.taxonConcept.publishedIn}</a></cite>
                                    </td>
                                </tr>
                            </g:if>
                        </tbody>
                    </table>
                    <g:if test="${tc.synonyms}">
                        <h2>Synonyms</h2>
                        <table class="outline table table-condensed">
                            <thead>
                                <tr>
                                    <th>Synonyms</th>
                                    <th>Source</th>
                                </tr>
                            </thead>
                            <tbody>
                    </g:if>
                    <g:each in="${tc.synonyms}" var="synonym">
                        <tr>
                            <td><bie:formatSciName name="${synonym.nameString}" rankId="${tc.taxonConcept?.rankID}"/> ${synonym.author}</td>
                            <td class="source">
                                <ul>
                                    <g:if test="${!synonym.infoSourceURL}"><li><a href="${tc.taxonConcept.infoSourceURL}" target="_blank" class="external">${tc.taxonConcept.infoSourceName}</a></li></g:if>
                                    <g:else><li><a href="${synonym.infoSourceURL}" target="_blank" class="external">${synonym.infoSourceName}</a></li></g:else>
                                </ul>
                            </td>
                        </tr>
                        <g:if test="${synonym.publishedIn || synonym.referencedIn}">
                            <tr class="cite">
                                <td colspan="2">
                                    <cite>Published in: <span class="publishedIn">${synonym.publishedIn?:synonym.referencedIn}</span></cite>
                                </td>
                            </tr>
                        </g:if>
                    </g:each>
                    <g:if test="${tc.synonyms}">
                        </tbody></table>
                    </g:if>
                    <g:if test="${tc.commonNames}">
                        <h2>Common Names</h2>
                        <table class="outline table table-condensed">
                            <thead>
                                <tr>
                                    <th>Common name</th>
                                    <th>Source</th>
                                </tr>
                            </thead>
                            <tbody>
                    </g:if>
                    <g:each in="${sortCommonNameSources}" var="cn">
                        <g:set var="cNames" value="${cn.value}" />
                        <%-- special treatment for <div> id and cookie name/value. matchup with Ranking Controller.rankTaxonCommonNameByUser --%>
                        <g:set var="nkey" value="${cn.key}" />
                        <g:set var="fName" value="${nkey?.trim()?.hashCode()}" />
                        <%-- javascript treatment: manual translate special charater, because string:encodeURL cannot handle non-english character --%>
                        <g:set var="enKey" value="${nkey?.encodeAsJavaScript()}" />
                        <tr>
                            <td>
                                ${nkey}
                                <g:if test="${!isReadOnly}">
                                    <g:if test="${!rankedImageUris?.contains(fName)}">
                                        <g:if test="${cNames}">
                                            <div id='cnRank-${fName}' class="rankCommonName">
                                                Is this a preferred common name for this ${tc.taxonConcept.rankString}?
                                                <a class="isrepresent" href="#" onclick="rankThisCommonName('${guid}','${fName}',false,true,'${enKey.trim()}');return false;">YES</a>
                                                |
                                                <a class="isnotrepresent" href="#" onclick="rankThisCommonName('${guid}','${fName}',false,false,'${enKey.trim()}');return false;">NO</a>
                                            </div>
                                        </g:if>
                                    </g:if>
                                </g:if>
                                <g:else>
                                    <div id='cnRank-${fName}' class="rankCommonName">Read Only Mode</div>
                                </g:else>
                            </td>
                            <td class="source">
                                <ul>
                                <g:each in="${sortCommonNameSources?.get(nkey)}" var="commonName">
                                    <li><a href="${commonName.infoSourceURL}" onclick="window.open(this.href); return false;">${commonName.infoSourceName}</a></li>
                                </g:each>
                                </ul>
                            </td>
                        </tr>
                    </g:each>
                    <g:if test="${tc.commonNames}">
                        </tbody></table>
                    </g:if>
                </section><!--#names-->
                <section class="tab-pane" id="classification">
                    <h2>Working classification</h2>
                    <div id="isAustralianSwitch"></div>
                        <g:each in="${taxonHierarchy}" var="taxon">
                            <!-- taxon = ${taxon} -->
                            <%-- Note: check for rankId is here due to some taxonHierarchy including taxa at higher rank than requested taxon (bug)--%>
                            <g:if test="${taxon.rankId?:0 <= tc.taxonConcept.rankID && taxon.guid != tc.taxonConcept.guid}">
                                <dl><dt>${taxon.rank}</dt>
                                    <dd><a href="${request?.contextPath}/species/${taxon.guid}#classification" title="${taxon.rank}">
                                        <bie:formatSciName name="${taxon.scientificName}" rankId="${taxon.rankId?:0}"/>
                                        <g:if test="${taxon.commonNameSingle}">: ${taxon.commonNameSingle}</g:if></a>
                                    </dd>
                            </g:if>
                            <g:elseif test="${taxon.guid == tc.taxonConcept.guid}">
                                <dl><dt id="currentTaxonConcept">${taxon.rank}</dt>
                                    <dd><span><bie:formatSciName name="${taxon.scientificName}" rankId="${taxon.rankId?:0}"/>
                                        <g:if test="${taxon.commonNameSingle}">: ${taxon.commonNameSingle}</g:if></span>
                                        <g:if test="${taxon.isAustralian || tc.isAustralian}">
                                            &nbsp;<span><img src="${grailsApplication.config.ala.baseURL}/wp-content/themes/ala2011/images/status_native-sm.png" alt="Recorded in Australia" title="Recorded in Australia" width="21" height="21"></span>
                                        </g:if>
                                    </dd>
                            </g:elseif>
                            <g:else><!-- Taxa ${taxon}) should not be here! --></g:else>
                        </g:each>
                        <dl class="childClassificationXXX">
                            <g:set var="currentRank" value=""/>
                            <g:each in="${childConcepts}" var="child" status="i">
                                <g:set var="currentRank" value="${child.rank}"/>
                                <dt>${child.rank}</dt>
                                <g:set var="taxonLabel"><bie:formatSciName name="${child.nameComplete ? child.nameComplete : child.name}"
                                           rankId="${child.rankId?:0}"/><g:if test="${child.commonNameSingle}">: ${child.commonNameSingle}</g:if></g:set>
                                <dd><a href="${request?.contextPath}/species/${child.guid}#classification">${taxonLabel.trim()}</a>&nbsp;
                                    <span>
                                        <g:if test="${child.isAustralian}">
                                            <img src="${grailsApplication.config.ala.baseURL}/wp-content/themes/ala2011/images/status_native-sm.png" alt="Recorded in Australia" title="Recorded in Australia" width="21" height="21">
                                        </g:if>
                                        <g:else>
                                            <g:if test="${child.guid?.startsWith('urn:lsid:catalogueoflife.org:taxon')}">
                                                <span class="inferredPlacement" title="Not recorded in Australia">[inferred placement]</span>
                                            </g:if>
                                            <g:else>
                                                <span class="inferredPlacement" title="Not recorded in Australia"></span>
                                            </g:else>
                                        </g:else>
                                    </span>
                                </dd>
                            </g:each>
                        </dl>
                        <g:each in="${taxonHierarchy}" var="taxon">
                            </dl>
                        </g:each>
                </section><!--classificatio-->
                <section class="tab-pane" id="records">
                    <h2>Occurrence records</h2>
                    <div id="occurrenceRecords">
                        <p><a href="${biocacheUrl}/occurrences/taxa/${guid}">View
                        list of all <span id="occurenceCount"></span> occurrence records for this taxon</a></p>
                        <div id="recordBreakdowns" style="display: block;">
                            <h2>Charts showing breakdown of occurrence records</h2>
                            <div id="chartsHint">Hint: click on chart elements to view that subset of records</div>
                        </div>
                    </div>

                    <%-- Distribution map images --%>
                    <g:if test="${tc.distributionImages}">
                        <h2>Record maps from other sources</h2>
                        <g:each in="${tc.distributionImages}" var="distribImage">
                            <div class="recordMapOtherSource" style="display: block">
                                <g:set var="imageLink">${distribImage.isPartOf ? distribImage.isPartOf : distribImage.infoSourceURL}</g:set>
                                <a href="${imageLink}">
                                    <img src="${distribImage.repoLocation}" alt="3rd party distribution map"/>
                                </a>
                                <br/>
                                <cite>Source:
                                    <a href="${imageLink}" onclick="window.open(this.href); return false;">${distribImage.infoSourceName}</a>
                                </cite>
                            </div>
                        </g:each>
                    </g:if>
                </section><!--#records-->
                <section class="tab-pane" id="literature">
                    <h2>Name references found in the Biodiversity Heritage Library</h2>
                    <div id="status-box" class="column-wrap" style="display: none;">
                        <div id="search-status" class="column-wrap" >
                            <span style="vertical-align: middle; ">
                                Searching, please wait...
                                <img src="${resource(dir: 'css/images', file: 'indicator.gif')}" alt="Searching" style="vertical-align: middle;"/>
                            </span>
                        </div>
                    </div>
                    <div id="results-home" class="column-wrap">
                        <div id="synonyms" style="display: none">
                        </div>
                        <div class="column-wrap" id="solr-results">
                        </div>
                    </div>

                    <div class="section" id="trove-container" style="padding-top:20px;">
                        <h2>Name references found in the TROVE - NLA </h2>
                        <div id="trove-results-home" class="column-wrap">
                        </div>
                        <input type="button" class="btn" id="previousTrove" value="Previous page"/>
                        <input type="button" class="btn" id="nextTrove" value="Next page"/>
                    </div>
                    <script type="text/javascript">
                        setupTrove('${tc?.taxonConcept?.nameString}','trove-container','trove-results-home','previousTrove','nextTrove');
                    </script>
                    <style type="text/css">
                    .trove-results-home .titleInfo { height:15px; }
                    </style>
            </section><!--#literature-->
            </div><!--tabs-panes-noborder-->
        </div><!--col-wide last-->
    </div><!--inner-->
</body>
</html>