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
<%@ page contentType="text/html;charset=UTF-8" %>
<g:set var="alaUrl" value="${grailsApplication.config.ala.baseURL}"/>
<g:set var="biocacheUrl" value="${grailsApplication.config.biocache.baseURL}"/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />
    <title>${query} | Search | <g:message code="site.title"/></title>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'bie.search.css')}" type="text/css" media="screen" />
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.sortElemets.js')}"></script>
    <script type="text/javascript">
        // global var to pass GSP vars into JS file
        SEARCH_CONF = {
            query: "${query}",
            serverName: "${grailsApplication.config.grails.serverURL}",
            bieUrl: "${grailsApplication.config.bie.baseURL}",
            biocacheUrl: "${grailsApplication.config.biocache.baseURL}",
            bhlUrl: "${grailsApplication.config.bhl.baseURL}"
        }
    </script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'search.js')}"></script>
</head>
<body class="species">
    <header id="page-header">
        <div class="inner">
            <nav id="breadcrumb">
                <ol>
                    <li><a href="${alaUrl}">Home</a></li>
                    <li class="last">Search the Atlas</li>
                </ol>
            </nav>
            <hgroup>
                <g:if test="${searchResults.totalRecords}">
                    <h1>Search for <b>${query.replaceFirst(/^\*$/, "[all records]")}</b> returned <g:formatNumber number="${searchResults.totalRecords}" type="number"/> results</h1>
                </g:if>
                <g:else>
                    <h1 style="font-size: 1.8em;">Search for <b>${query}</b> did not match any documents</h1><br/>
                </g:else>
            </hgroup>
        </div>
    </header>
    <g:if test="${searchResults.totalRecords}">
        <g:set var="paramsValues" value="${[:]}"/>
        <div class="inner">
            <div class="col-narrow">
                <div class="boxed attached">
                    <div class="facetLinks">
                        <h2>Refine results</h2>
                        <div id="accordion">
                            <g:if test="${query && filterQuery}">
                                <g:set var="queryParam">q=${query.encodeAsHTML()}<g:if test="${!filterQuery.isEmpty()}">&fq=${filterQuery?.join("&fq=")}</g:if></g:set>
                            </g:if>
                            <g:else>
                                <g:set var="queryParam">q=${query.encodeAsHTML()}<g:if test="${params.fq}">&fq=${fqList?.join("&fq=")}</g:if></g:set>
                            </g:else>
                            <g:if test="${searchResults.query}">
                                <g:set var="downloadParams">q=${searchResults.query?.encodeAsHTML()}<g:if test="${params.fq}">&fq=${params.list("fq")?.join("&fq=")?.trim()}</g:if></g:set>
                            </g:if>
                            <%-- is this init search? then add fq parameter in href --%>
                            <g:if test="${isAustralian}">
                                <g:set var="appendQueryParam" value="&fq=australian_s:recorded" />
                            </g:if>
                            <g:else>
                                <g:set var="appendQueryParam" value="" />
                            </g:else>
                            <g:if test="${facetMap}">
                                <h3><span class="FieldName">Current Filters</span></h3>
                                <div class="subnavlist" id="currentFilters">
                                    <ul>
                                        <g:each var="item" in="${facetMap}">
                                            <li>
                                                <g:set var="closeLink">&nbsp;[<b><a href="#" onClick="javascript:removeFacet('${item.key}:${item.value}'); return true;" style="text-decoration: none" title="remove">X</a></b>]</g:set>
                                                <g:if test="${item.key?.contains("uid")}">
                                                    <g:set var="resourceType">${item.value}_resourceType</g:set>
                                                    ${collectionsMap?.get(resourceType)}:<b>&nbsp;${collectionsMap?.get(item.value)}</b>${closeLink}
                                                </g:if>
                                                <g:else>
                                                    <g:message code="facet.${item.key}"/>: <b><g:message code="${item.key}.${item.value}" default="${item.value}"/></b>${closeLink}
                                                </g:else>
                                            </li>
                                        </g:each>
                                    </ul>
                                </div>
                            </g:if>
                            <g:each var="facetResult" in="${searchResults.facetResults?:[]}">
                                <g:if test="${!facetMap?.get(facetResult.fieldName) && !filterQuery?.contains(facetResult.fieldResult?.opt(0)?.label) && !facetResult.fieldName?.contains('idxtype1')}">
                                    <h3><span class="FieldName"><g:message code="facet.${facetResult.fieldName}"/></span></h3>
                                    <div class="subnavlist" id="facet-${facetResult.fieldName}">
                                        <ul>
                                            <g:set var="lastElement" value="${facetResult.fieldResult?.get(facetResult.fieldResult.length()-1)}"/>
                                            <g:if test="${lastElement.label == 'before'}">
                                                <li><g:set var="firstYear" value="${facetResult.fieldResult?.opt(0)?.label.substring(0, 4)}"/>
                                                    <a href="?${queryParam}${appendQueryParam}&fq=${facetResult.fieldName}:[* TO ${facetResult.fieldResult.opt(0)?.label}]">Before ${firstYear}</a>
                                                    (<g:formatNumber number="${lastElement.count}" type="number"/>)
                                                </li>
                                            </g:if>
                                            <g:each var="fieldResult" in="${facetResult.fieldResult}" status="vs">
                                                <g:set var="dateRangeTo"><g:if test="${vs == lastElement}">*</g:if><g:else>${facetResult.fieldResult[vs+1]?.label}</g:else></g:set>
                                                <g:if test="${facetResult.fieldName?.contains("occurrence_date") && fieldResult.label?.endsWith("Z")}">
                                                    <li><g:set var="startYear" value="${fieldResult.label?.substring(0, 4)}"/>
                                                        <a href="?${queryParam}${appendQueryParam}&fq=${facetResult.fieldName}:[${fieldResult.label} TO ${dateRangeTo}]">${startYear} - ${startYear + 10}</a>
                                                        (<g:formatNumber number="${fieldResult.count}" type="number"/>)</li>
                                                </g:if>
                                                <g:elseif test="${fieldResult.label?.endsWith("before")}"><%-- skip --%></g:elseif>
                                                <g:elseif test="${fieldResult.label?.isEmpty()}">
                                                    %{--<li><a href="?${queryParam}${appendQueryParam}&fq=${facetResult.fieldName}:[* TO %22%22]"><g:message code="${facetResult.fieldName}.${fieldResult.label}" default="${fieldResult.label?:"[empty]"}"/></a>--}%
                                                        %{--(<g:formatNumber number="${fieldResult.count}" type="number"/>)--}%
                                                    %{--</li>--}%
                                                </g:elseif>
                                                <g:else>
                                                    <li><a href="?${queryParam}${appendQueryParam}&fq=${facetResult.fieldName}:${fieldResult.label}"><g:message code="${facetResult.fieldName}.${fieldResult.label}" default="${fieldResult.label?:"[unknown]"}"/></a>
                                                        (<g:formatNumber number="${fieldResult.count}" type="number"/>)
                                                    </li>
                                                </g:else>
                                            </g:each>
                                        </ul>
                                    </div>
                                </g:if>
                            </g:each>
                        </div>
                    </div><!--end #facets-->
                </div><!--end .boxed-->
            </div><!--end .col-narrow-->
            <div class="col-wide last">
                <div class="solrResults">
                    <div id="dropdowns">
                        <g:if test="${idxTypes.contains("TAXON")}">
                            <g:set var="downloadUrl" value="${grailsApplication.config.bie.baseURL}/ws/download/?${downloadParams}${appendQueryParam}&sort=${searchResults.sort}&dir=${searchResults.dir}"/>
                            <input type="button" onclick="window.location='${downloadUrl}'" value="Download" title="Download a list of taxa for your search" style="float:left;"/>
                            %{--<div id="downloads" class="buttonDiv" style="">--}%
                                %{--<a href="${grailsApplication.config.bie.baseURL}/download/?${downloadParams}${appendQueryParam}&sort=${searchResults.sort}&dir=${searchResults.dir}" id="downloadLink" title="Download taxa results for your search">Download</a>--}%
                            %{--</div>--}%
                        </g:if>
                        <div id="sortWidget">
                            <label for="per-page">Results per page</label>
                            <select id="per-page" name="per-page">
                                <option value="10" ${(params.pageSize == '10') ? "selected=\"selected\"" : ""}>10</option>
                                <option value="20" ${(params.pageSize == '20') ? "selected=\"selected\"" : ""}>20</option>
                                <option value="50" ${(params.pageSize == '50') ? "selected=\"selected\"" : ""}>50</option>
                                <option value="100" ${(params.pageSize == '100') ? "selected=\"selected\"" : ""} >100</option>
                            </select>
                            Sort by
                            <select id="sort" name="sort">
                                <option value="score" ${(params.sort == 'score') ? "selected=\"selected\"" : ""}>best match</option>
                                <option value="commonNameSingle" ${(params.sort == 'commonNameSingle') ? "selected=\"selected\"" : ""}>common name</option>
                                <option value="rank" ${(params.sort == 'rank') ? "selected=\"selected\"" : ""}>taxon rank</option>
                            </select>
                            Sort order
                            <select id="dir" name="dir">
                                <option value="asc" ${(params.dir == 'asc') ? "selected=\"selected\"" : ""}>normal</option>
                                <option value="desc" ${(params.dir == 'desc') ? "selected=\"selected\"" : ""}>reverse</option>
                            </select>
                            <input type="hidden" value="${pageTitle}" name="title"/>
                        </div><!--sortWidget-->
                    </div><!--drop downs-->
                    <div class="results">
                        <g:each var="result" in="${searchResults.results}">
                            <g:set var="sectionText"><g:if test="${!facetMap.idxtype}"><span><b>Section:</b> <g:message code="idxType.${result.idxType}"/></span></g:if></g:set>
                                <g:if test="${result.has("idxType") && result.idxType == 'TAXON'}">
                                    <h4>
                                        <g:set var="speciesPageLink">${request.contextPath}/species/${result.linkIdentifier?:result.guid}</g:set>
                                        <g:if test="${result.smallImageUrl}">
                                            <a href="${speciesPageLink}" class="occurrenceLink"><img class="alignright" src="${result.smallImageUrl}" style="max-height: 150px; max-width: 300px;" alt="species image thumbnail"/></a>
                                        </g:if>
                                        <g:else><div class="alignright" style="width:85px; height:40px;"></div></g:else>
                                        <g:if test="${result.rank}"><span style="text-transform: capitalize; display: inline;">${result.rank}</span>:</g:if>
                                        <g:if test="${result.linkIdentifier}">
                                            <a href="${request.contextPath}/species/${result.linkIdentifier}" class="occurrenceLink"><bie:formatSciName rankId="${result.rankId}" name="${(result.nameComplete) ? result.nameComplete : result.name}" acceptedName="${result.acceptedConceptName}"/> ${result.author?:''}</a>
                                        </g:if>
                                        <g:if test="${!result.linkIdentifier}">
                                            <a href="${request.contextPath}/species/${result.guid}" class="occurrenceLink"><bie:formatSciName rankId="${result.rankId}" name="${(result.nameComplete) ? result.nameComplete : result.name}" acceptedName="${result.acceptedConceptName}"/> ${result.author?:''}</a>
                                        </g:if>
                                        <g:if test="${result.acceptedConceptName}">
                                            accepted name: ${result.acceptedConceptName}
                                        </g:if>
                                        <g:if test="${result.commonNameSingle}"><span class="commonNameSummary">&nbsp;&ndash;&nbsp; ${result.commonNameSingle}</span></g:if>
                                    </h4>
                                    <p>
                                        <g:if test="${result.commonNameSingle && result.commonNameSingle != result.commonName}">
                                            <span>${result.commonName?:"".substring(0, 220)}<g:if test="${result.commonName?.length()?:0 > 220}">...</g:if></span>
                                        </g:if>
                                        <g:if test="${false && result.highlight}">
                                            <span><b>...</b> ${result.highlight} <b>...</b></span>
                                        </g:if>
                                        <g:if test="${result.kingdom}">
                                            <span><strong class="resultsLabel">Kingdom</strong>: ${result.kingdom}</span>
                                        </g:if>
                                        <g:if test="${result.rankId && result.rankId > 5000}">
                                            <span class="recordSighting" style="display:inline;">
                                                <a href="http://cs.ala.org.au/bdrs-ala/bdrs/user/atlas.htm?surveyId=1&guid=${result.guid}">Record a sighting/share a photo</a>
                                            </span>&nbsp;
                                            <g:if test="${result?.occCount?:0 > 0}">
                                                <span class="recordSighting" style="display:inline;"><a href="http://biocache.ala.org.au/occurrences/taxa/${result.guid}">Occurrences:
                                                    <g:formatNumber number="${result.occCount}" type="number"/></a></span>
                                            </g:if>
                                        </g:if>
                                        <g:if test="${result.rankId && result.rankId < 7000}">
                                            &nbsp;<span style="display:inline;"><a href="${createLink(controller:'image-search', action: 'showSpecies', params:[taxonRank: result.rank, scientificName: (result.nameComplete) ? result.nameComplete : result.name])}">View images of species</a></span>
                                        </g:if>
                                    <!-- ${sectionText} -->
                                    </p>
                                    <div class="resultSeparator">&nbsp;</div>
                                </g:if>
                                <g:elseif test="${result.has("regionTypeName") && result.get("regionTypeName")}">
                                    <h4><g:message code="idxType.${result.idxType}"/>:
                                        <a href="${result.guid}">${result.name}</a></h4>
                                    <p>
                                        <span>Region type: ${result.regionTypeName}</span>
                                        <!-- ${sectionText} -->
                                    </p>
                                </g:elseif>
                                <g:elseif test="${result.has("institutionName") && result.get("institutionName")}">
                                    <h4><g:message code="idxType.${result.idxType}"/>:
                                        <a href="${result.guid}">${result.name}</a></h4>
                                    <p>
                                        <span>${result.institutionName}</span>
                                        <!-- ${sectionText} -->
                                    </p>
                                </g:elseif>
                                <g:elseif test="${result.has("acronym") && result.get("acronym")}">
                                    <h4><g:message code="idxType.${result.idxType}"/>:
                                        <a href="${result.guid}">${result.name}</a></h4>
                                    <p>
                                        <span>${result.acronym}</span>
                                        <!-- ${sectionText} -->
                                    </p>
                                </g:elseif>
                                <g:elseif test="${result.has("description") && result.get("description")}">
                                    <h4><g:message code="idxType.${result.idxType}"/>:
                                        <a href="${result.guid}">${result.name}</a></h4>
                                    <p>
                                        <span>${result.description}</span>
                                        <!-- ${sectionText} -->
                                    </p>
                                </g:elseif>
                                <g:elseif test="${result.has("highlight") && result.get("highlight")}">
                                    <h4><g:message code="idxType.${result.idxType}"/>:
                                        <a href="${result.guid}">${result.name}</a></h4>
                                    <p>
                                        <span>${result.highlight}</span>
                                        <!-- ${sectionText} -->
                                    </p>
                                </g:elseif>
                                <g:else>
                                    <h4><g:message code="idxType.${result.idxType}"/>: <a href="${result.guid}">${result.name}</a></h4>
                                    <p><!-- ${sectionText} --></p>
                                </g:else>
                        </g:each>
                    </div><!--close results-->
                    <g:if test="${searchResults && searchResults.totalRecords > searchResults.pageSize}">
                        <div id="searchNavBar">
                            <bie:searchNavigationLinks totalRecords="${searchResults?.totalRecords}" startIndex="${searchResults?.startIndex}"
                                      lastPage="${lastPage?:-1}" pageSize="${searchResults?.pageSize}"/>
                        </div>
                    </g:if>
                </div><!--solrResults-->
            </div><!--end .col-wide last-->
        </div><!--end .inner-->
    </g:if>
</body>
</html>