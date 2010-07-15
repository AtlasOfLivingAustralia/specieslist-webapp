<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta name="pageName" content="species"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery-ui-1.8.custom.min.js"></script>
    <link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/static/css/bie-theme/jquery-ui-1.8.custom.css" charset="utf-8">
    <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery.cookie.js"></script>
    <script type="text/javascript">
        $(document).ready(function() {
            var facetLinksSize = $("ul#subnavlist li").size();
            if (facetLinksSize == 0) {
                // Hide an empty facet link list
                $("#facetBar > h4").hide();
                $("#facetBar #navlist").hide();
            }

            /* Accordion widget */
            var icons = {
                header: "ui-icon-circle-arrow-e",
                headerSelected: "ui-icon-circle-arrow-s"
            };
            $("#accordion").accordion({
                icons: icons,
                collapsible: true,
                autoHeight: false
            });
            // more/fewer search option links
            $("#refineMore a").click(function(e) {
                e.preventDefault();
                $("#accordion").show();
                $("#refineLess").show();
                $("#refineMore").hide();
                $.cookie("bie-refine", "show");
            });
            $("#refineLess a").click(function(e) {
                e.preventDefault();
                $("#accordion").hide();
                $("#refineLess").hide();
                $("#refineMore").show();
                $.cookie("bie-refine", "hide");
            });
            // use cookie to remeber state of the facet links
            var refineState = $.cookie("bie-refine");
            if (refineState == "show") {
                $("#refineMore a").click();
            }
            // listeners for sort widgets
            $("select#sort").change(function() {
                var val = $("option:selected", this).val();
                reloadWithParam('sort',val);
            });
            $("select#dir").change(function() {
                var val = $("option:selected", this).val();
                reloadWithParam('dir',val);
            });
        });

        // jQuery getQueryParam Plugin 1.0.0 (20100429)
        // By John Terenzio | http://plugins.jquery.com/project/getqueryparam | MIT License
        // Adapted by Nick dos Remedios to handle multiple params with same name - return a list
        (function ($) {
            // jQuery method, this will work like PHP's $_GET[]
            $.getQueryParam = function (param) {
                // get the pairs of params fist
                var pairs = location.search.substring(1).split('&');
                var values = [];
                // now iterate each pair
                for (var i = 0; i < pairs.length; i++) {
                    var params = pairs[i].split('=');
                    if (params[0] == param) {
                        // if the param doesn't have a value, like ?photos&videos, then return an empty srting
                        //return params[1] || '';
                        values.push(params[1]);
                    }
                }

                if (values.length > 0) {
                    return values;
                } else {
                    //otherwise return undefined to signify that the param does not exist
                    return undefined;
                }

            };
        })(jQuery);

        function removeFacet(facet) {
            var q = $.getQueryParam('q'); //$.query.get('q')[0];
            var fqList = $.getQueryParam('fq'); //$.query.get('fq');
            var paramList = [];
            if (q != null) {
                paramList.push("q=" + q);
            }
            //alert("this.facet = "+facet+"; fqList = "+fqList.join('|'));

            if (fqList instanceof Array) {
                //alert("fqList is an array");
                for (var i in fqList) {
                    //alert("i == "+i+"| fq = "+fqList[i]);
                    if (decodeURI(fqList[i]) == facet) {
                        //alert("removing fq: "+fqList[i]);
                        fqList.splice(fqList.indexOf(fqList[i]),1);
                    }
                }
            } else {
                //alert("fqList is NOT an array");
                if (decodeURI(fqList) == facet) {
                    fqList = null;
                }
            }
            //alert("(post) fqList = "+fqList.join('|'));
            if (fqList != null) {
                paramList.push("fq=" + fqList.join("&fq="));
            }

            window.location.replace(window.location.pathname + '?' + paramList.join('&'));
        }

        /**
         * Catch sort drop-down and build GET URL manually
         */
        function reloadWithParam(paramName, paramValue) {
            var paramList = [];
            var q = $.getQueryParam('q'); //$.query.get('q')[0];
            var fqList = $.getQueryParam('fq'); //$.query.get('fq');
            var sort = $.getQueryParam('sort');
            var dir = $.getQueryParam('dir');
            // add query param
            if (q != null) {
                paramList.push("q=" + q);
            }
            // add filter query param
            if (fqList != null) {
                paramList.push("fq=" + fqList.join("&fq="));
            }
            // add sort param if already set
            if (paramName != 'sort' && sort != null) {
                paramList.push('sort' + "=" + sort);
            }
            
            if (paramName != null && paramValue != null) {
                if (paramName == 'sort') {
                    paramList.push(paramName + "=" +paramValue);
                } else if (paramName == 'dir'){// && !(sort == null || sort == 'score')) {//moved to SearchController
                    paramList.push(paramName + "=" +paramValue);
                }
            }

            //alert("params = "+paramList.join("&"));
            //alert("url = "+window.location.pathname);
            window.location.replace(window.location.pathname + '?' + paramList.join('&'));
        }

    </script>
    <title>Species Search - ${query}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/screen.css" type="text/css" media="screen" charset="utf-8"/>
</head>
<body>
<div id="decoratorBody">
    <c:set var="pageTitle">
        <c:if test="${not empty title}">${title}</c:if>
        <c:if test="${empty title}">Search Results</c:if>
    </c:set>
    <h1>${pageTitle}</h1>
    <c:if test="${empty searchResults.results}"><p>Search for <span style="font-weight: bold"><c:out value="${query}"/></span> did not match any documents</p></c:if>
    <c:if test="${not empty searchResults.results}">

        <div id="searchResults">
            <fmt:formatNumber var="currentPage" value="${(searchResults.startIndex / searchResults.pageSize) + 1}" pattern="0"/>
            <div class="solrResults">
                <div id="sortWidget">
                    <div id="searchTerms">
                        <div class="queryTermBox">
                            Search: <a href="?q=${queryJsEscaped}">${queryJsEscaped}</a><a name="searchResults">&nbsp;</a>
                        </div>
                        <c:forEach var="filter" items="${paramValues.fq}">
                            <c:set var="fqField" value="${fn:substringBefore(filter, ':')}"/>
                            <c:set var="fqValue" value="${fn:substringAfter(filter, ':')}"/>
                            <c:if test="${not empty fqValue}">
                                <div class="facetTermBox">
                                    <!-- <b class="facetTermDivider ui-icon ui-icon-triangle-1-e">&nbsp;</b>-->
                                    <span class="facetFieldName"><fmt:message key="facet.${fqField}"/>:</span>
                                    ${fn:replace(fqValue,'-01-01T12:00:00Z','')} <a href="#" onClick="removeFacet('${filter}'); return false;" class="facetCloseLink ui-icon ui-icon-closethick" title="Remove this restriction">&nbsp;</a>
                                </div>
                            </c:if>
                        </c:forEach>
                    </div>
                    <div id="resultsStats">
                        Page ${currentPage} of <fmt:formatNumber value="${lastPage}" pattern="#,###,###"/> (search returned <fmt:formatNumber value="${searchResults.totalRecords}" pattern="#,###,###"/> results)
                    </div>
                    sort by
                    <select id="sort" name="sort">
                        <option value="score" <c:if test="${param.sort eq 'score'}">selected</c:if>>best match</option>
                        <option value="scientificNameRaw" <c:if test="${param.sort eq 'scientificNameRaw'}">selected</c:if>>scientific name</option>
                        <!--                            <option value="rank">rank</option>-->
                        <option value="commonNameSort" <c:if test="${param.sort eq 'commonNameSort'}">selected</c:if>>common name</option>
                        <option value="rank" <c:if test="${param.sort eq 'rank'}">selected</c:if>>taxon rank</option>
                    </select>
                    sort order
                    <select id="dir" name="dir">
                        <option value="asc" <c:if test="${param.dir eq 'asc'}">selected</c:if>>normal</option>
                        <option value="desc" <c:if test="${param.dir eq 'desc'}">selected</c:if>>reverse</option>
                    </select>
                </div>
                <table>
                    <thead>
                        <tr>
                            <th>Scientific&nbsp;Name</th>
                            <th>Common&nbsp;Name</th>
                            <th>Taxon&nbsp;Rank</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="taxonConcept" items="${searchResults.results}">
                            <tr>
                                <td id="col1"><a href="${pageContext.request.contextPath}/species/${taxonConcept.guid}?conceptName=${taxonConcept.name}" class="occurrenceLink"><alatag:formatSciName rankId="${taxonConcept.rankId}" name="${taxonConcept.name}" acceptedName="${taxonConcept.acceptedConceptName}"/></a></td>
                                <td id="col2">${fn:substring(taxonConcept.commonName, 0, 250)}</td>
                                <td id="col3">${taxonConcept.rank}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                <div id="searchNavBar">
                    <alatag:searchNavigationLinks totalRecords="${searchResults.totalRecords}" startIndex="${searchResults.startIndex}"
                         lastPage="${lastPage}" pageSize="${searchResults.pageSize}"/>
                </div>
            </div>
            <div id="facets">
                <c:if test="${not empty TAXON or not empty REGION or not empty INSTITUTION or not empty COLLECTION or not empty DATAPROVIDER or not empty DATASET}">
                    <c:set var="taxon" scope="session"><c:out value="${TAXON}"/></c:set>
                    <c:set var="region" scope="session"><c:out value="${REGION}"/></c:set>
                    <c:set var="institution" scope="session"><c:out value="${INSTITUTION}"/></c:set>
                    <c:set var="collection" scope="session"><c:out value="${COLLECTION}"/></c:set>
                    <c:set var="dataprovider" scope="session"><c:out value="${DATAPROVIDER}"/></c:set>
                    <c:set var="dataset" scope="session"><c:out value="${DATASET}"/></c:set>
                </c:if>
                <div id="searchTypes">
                    <ul>
                        <li><a href="#">Site Pages</a></li>
                        <li class="active">Species</li>
                        <c:if test="${not empty region}">
                            <li><a href="${pageContext.request.contextPath}/regions/search?q=${param['q']}">Regions</a></li>
                        </c:if>
<!--                        <li><a href="/biocache-webapp/occurrences/search?q=${param['q']}"><strike>Occurrence Records</strike></a></li>-->
                        <c:if test="${not empty institution}">
                            <li><a href="${pageContext.request.contextPath}/institutions/search?q=${param['q']}">Institutions</a></li>
                        </c:if>
                        <c:if test="${not empty collection}">
                            <li><a href="${pageContext.request.contextPath}/collections/search?q=${param['q']}">Collections</a></li>
                        </c:if>
                        <c:if test="${not empty dataprovider}">
                            <li><a href="${pageContext.request.contextPath}/dataproviders/search?q=${param['q']}">Data Providers</a></li>
                        </c:if>
                        <c:if test="${not empty dataset}">
                            <li><a href="${pageContext.request.contextPath}/datasets/search?q=${param['q']}">Data Sets</a></li>
                        </c:if>
                    </ul>
                </div>
                <div id="refineMore"><a href="#">More Search Options</a></div>
                <div id="accordion">
                    <c:if test="${not empty query}">
                        <c:set var="queryParam">q=<c:out value="${query}" escapeXml="true"/><c:if test="${not empty param.fq}">&fq=${fn:join(paramValues.fq, "&fq=")}</c:if></c:set>
                    </c:if>
                    <c:forEach var="facetResult" items="${searchResults.facetResults}">
                        <c:if test="${!fn:containsIgnoreCase(facetQuery, facetResult.fieldResult[0].label) && !fn:containsIgnoreCase(facetResult.fieldName, 'idxtype')}">
                            <h3><a href="#"><span class="FieldName"><fmt:message key="facet.${facetResult.fieldName}"/></span></a></h3>
                            <div id="subnavlist">
                                <ul>
                                    <c:set var="lastElement" value="${facetResult.fieldResult[fn:length(facetResult.fieldResult)-1]}"/>
                                    <c:if test="${lastElement.label eq 'before'}">
                                        <li><c:set var="firstYear" value="${fn:substring(facetResult.fieldResult[0].label, 0, 4)}"/>
                                            <a href="?${queryParam}&fq=${facetResult.fieldName}:[* TO ${facetResult.fieldResult[0].label}]">Before ${firstYear}</a>
                                            (<fmt:formatNumber value="${lastElement.count}" pattern="#,###,###"/>)
                                        </li>
                                    </c:if>
                                    <c:forEach var="fieldResult" items="${facetResult.fieldResult}" varStatus="vs">
                                        <c:set var="dateRangeTo"><c:choose><c:when test="${vs.last}">*</c:when><c:otherwise>${facetResult.fieldResult[vs.count].label}</c:otherwise></c:choose></c:set>
                                        <c:choose>
                                            <c:when test="${fn:containsIgnoreCase(facetResult.fieldName, 'occurrence_date') && fn:endsWith(fieldResult.label, 'Z')}">
                                                <li><c:set var="startYear" value="${fn:substring(fieldResult.label, 0, 4)}"/>
                                                    <a href="?${queryParam}&fq=${facetResult.fieldName}:[${fieldResult.label} TO ${dateRangeTo}]">${startYear} - ${startYear + 10}</a>
                                                    (<fmt:formatNumber value="${fieldResult.count}" pattern="#,###,###"/>)</li>
                                            </c:when>
                                            <c:when test="${fn:endsWith(fieldResult.label, 'before')}"><%-- skip --%></c:when>
                                            <c:otherwise>
                                                <li><a href="?${queryParam}&fq=${facetResult.fieldName}:${fieldResult.label}"><fmt:message key="${fieldResult.label}"/></a>
                                                (<fmt:formatNumber value="${fieldResult.count}" pattern="#,###,###"/>)</li>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </ul>
                            </div>
                        </c:if>
                    </c:forEach>
                </div>
                <div id="refineLess"><a href="#">Fewer Search Options</a></div>
            </div>
        </div>
    </c:if>
</div>
</body>
</html>
