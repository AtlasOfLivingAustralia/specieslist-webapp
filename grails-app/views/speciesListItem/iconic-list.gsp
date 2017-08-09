<%@ page import="grails.converters.JSON" %>
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
<!doctype html>
<g:set var="bieUrl" value="${grailsApplication.config.bie.baseURL?:'http://bie.ala.org.au'}"/>
<g:set var="collectoryUrl" value="${grailsApplication.config.collectory.baseURL}" />
<g:set var="maxDownload" value="${grailsApplication.config.downloadLimit}" />

<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="Australia&apos;s species"/>
    <script language="JavaScript" type="text/javascript" src="${asset.assetPath(src:'facets.js')}"></script>
    <script language="JavaScript" type="text/javascript" src="${asset.assetPath(src:'getQueryParam.js')}"></script>
    <script language="JavaScript" type="text/javascript" src="${asset.assetPath(src:'jquery-ui-1.8.17.custom.min.js')}"></script>
    <script language="JavaScript" type="text/javascript" src="${asset.assetPath(src:'jquery.doubleScroll.js')}"></script>
    <title>Species list items | ${grailsApplication.config.skin.orgNameLong}</title>
    <style type="text/css">
        #buttonDiv {display: none;}
        #refine {display:none;}
        .bs-docs-sidenav {
            width: 228px;
            margin: 15px 0 0;
            padding: 0;
            background-color: #fff;
            -webkit-border-radius: 6px;
            -moz-border-radius: 6px;
            border-radius: 6px;
            -webkit-box-shadow: 0 1px 4px rgba(0,0,0,.065);
            -moz-box-shadow: 0 1px 4px rgba(0,0,0,.065);
            box-shadow: 0 1px 4px rgba(0,0,0,.065);
        }
        .bs-docs-sidenav > li > a {
            display: block;
            width: 190px \9;
            margin: 0 0 -1px;
            padding: 8px 14px;
            border: 1px solid #e5e5e5;
        }
        .bs-docs-sidenav > li:first-child > a {
            -webkit-border-radius: 6px 6px 0 0;
            -moz-border-radius: 6px 6px 0 0;
            border-radius: 6px 6px 0 0;
        }
        .bs-docs-sidenav > li:last-child > a {
            -webkit-border-radius: 0 0 6px 6px;
            -moz-border-radius: 0 0 6px 6px;
            border-radius: 0 0 6px 6px;
        }
        .bs-docs-sidenav > .active > a {
            /*position: relative;*/
            /*z-index: 2;*/
            padding: 9px 15px;
            border: 0;
            text-shadow: 0 1px 0 rgba(0,0,0,.15);
            -webkit-box-shadow: inset 1px 0 0 rgba(0,0,0,.1), inset -1px 0 0 rgba(0,0,0,.1);
            -moz-box-shadow: inset 1px 0 0 rgba(0,0,0,.1), inset -1px 0 0 rgba(0,0,0,.1);
            box-shadow: inset 1px 0 0 rgba(0,0,0,.1), inset -1px 0 0 rgba(0,0,0,.1);
        }
        /* Chevrons */
        .bs-docs-sidenav .glyphicon-chevron-right {
            float: right;
            margin-top: 2px;
            margin-right: -6px;
            opacity: .25;
        }
        .bs-docs-sidenav > li > a:hover {
            background-color: #f5f5f5;
        }
        .bs-docs-sidenav a:hover .glyphicon-chevron-right {
            opacity: .5;
        }
        .bs-docs-sidenav .active .glyphicon-chevron-right,
        .bs-docs-sidenav .active a:hover .glyphicon-chevron-right {
            opacity: 1;
        }
        .bs-docs-sidenav.affix {
            top: 50px;
        }
        .bs-docs-sidenav.affix-bottomX {
            position: absolute;
            top: auto;
            bottom: 270px;
        }

        .general-search {
            width: 500px;
        }

        /* Responsive
        -------------------------------------------------- */

        /* Desktop large
        ------------------------- */
        @media (min-width: 1200px) {
            .bs-docs-container {
                max-width: 970px;
            }
            .bs-docs-sidenav {
                width: 258px;
            }
            .bs-docs-sidenav > li > a {
                width: 230px \9; /* Override the previous IE8-9 hack */
            }
            .general-search {
                width: 500px;
            }
        }

        /* Desktop
        ------------------------- */
        @media (max-width: 980px) {
            /* When affixed, space properly */
            .bs-docs-sidenav {
                top: 0;
                width: 215px;
                margin-top: 30px;
                margin-right: 0;
            }
            .general-search {
                width: 400px;
            }
        }

        /* Tablet to desktop
        ------------------------- */
        @media (min-width: 768px) and (max-width: 979px) {

            /* Adjust sidenav width */
            .bs-docs-sidenav {
                width: 160px;
                margin-top: 20px;
            }
            .bs-docs-sidenav.affix {
                top: 0;
            }
            .general-search {
                width: 300px;
            }
        }

        /* Tablet
        ------------------------- */
        @media (max-width: 767px) {
            /* Sidenav */
            .bs-docs-sidenav {
                width: auto;
                margin-bottom: 20px;
            }
            .bs-docs-sidenav.affix {
                position: static;
                width: auto;
                top: 0;
            }
            .input-group .general-search {
                width: 100%;
            }
        }

        .imgCon img {
            height: 140px;
        }

        #content p.lead {
            font-size: 16px;
            margin-bottom: 10px;
        }

    </style>

    <asset:script type="text/javascript">
        function reloadWithMax(el) {
            var max = $(el).find(":selected").val();
            var params = {
                max: max,
                sort: "${params.sort}",
                order: "${params.order}",
                offset: "${params.offset?:0}",
                fq: "${params.fq?:'kvp group:Birds'}"
            }
            var paramStr = jQuery.param(params);
            window.location.href =  '?' + paramStr;
        }
    </asset:script>

</head>
<body class="yui-skin-sam nav-species">
<div id="content" class="container">

    <div class="inner row">
        <div class="col-md-12">
            <h2 class="subject-title">Australian iconic species</h2>
            <p class="lead">
                Below is a listing of some of Australia's most recognisable species. As well as the listing
                below you can <a href="http://bie.ala.org.au/search?q=&fq=idxtype:%22TAXON%22" class="">search
                over 100,000 species within the ALA</a>.
            </p>
        </div>
    </div>
    <g:if test="${flash.message}">
        <div class="inner row">
            <div class="message alert alert-info"><b>Alert:</b> ${flash.message}</div>
        <div>
    </g:if>
    <div class="inner row">
        <div class="col-md-3">
            <ul id="groupsNav" class="nav bs-docs-sidenav ">
                <g:set var="fqs" value="${params.list('fq')}" />
                <g:each in="${facets.get("listProperties")}" var="group">
                    <g:if test="${group.getKey() == 'group'}">
                        <g:each in="${group.getValue().sort{it[1]}}" var="arr" status="i">
                            <g:set var="active" value="${fqs.any{ it.contains(arr[1])} ? "active" : ""}"/>
                            <li class="${active}"><a href="?fq=kvp ${arr[0]}:${arr[1]}">${arr[1]} (${arr[3]})<i class="glyphicon glyphicon-chevron-right"></i> </a></li>
                        </g:each>
                    </g:if>
                </g:each>
            </ul>
        </div> <!-- /col-md-3 -->
        <div class="col-md-9">
            <div id="gridView" class="">
                <g:each var="result" in="${results}" status="i">
                    <g:set var="recId" value="${result.id}"/>
                    <g:set var="bieSpecies" value="${bieItems?.get(result.guid)}"/>
                    <g:set var="bieTitle">species page for <i>${result.rawScientificName}</i></g:set>
                    <div class="imgCon">
                        <a class="thumbImage viewRecordButton" rel="thumbs" title="click to view detailed page" href="${bieUrl}/species/${result.guid?:bieSpecies?.get(2)}"
                                    data-id="${recId}"><img src="${bieSpecies?.get(0)?:asset.assetPath(src:'infobox_info_icon.png" style="opacity:0.5')}" alt="thumbnail species image"/>
                            </a>
                            <g:if test="${true}">
                                <g:set var="displayName">
                                    <g:if test="${bieSpecies?.get(1)}">${bieSpecies?.get(1)}</g:if>
                                    <g:else>
                                        <i><g:if test="${result.guid == null}">
                                            ${fieldValue(bean: result, field: "rawScientificName")}
                                        </g:if>
                                        <g:else>
                                            ${bieSpecies?.get(2)}
                                        </g:else></i>
                                    </g:else>
                                </g:set>
                                <div class="meta brief">
                                    ${displayName}
                                </div>
                            </g:if>
                        </a>
                    </div>
                </g:each>
            </div><!-- /#iconView -->
            <g:if test="${params.max<totalCount}">
                <div class="searchWidgets">
                    Items per page:
                    <select id="maxItems" onchange="reloadWithMax(this)">
                        <g:each in="${[10,25,50,100]}" var="max">
                            <option ${(params.max == max)?'selected="selected"':''}>${max}</option>
                        </g:each>
                    </select>
                </div>

                <div class="pagination listPagination" id="searchNavBar">
                    <g:if test="${params.fq}">
                        <g:paginate total="${totalCount}" action="iconicSpecies" id="${params.id}" params="${[fq: params.fq]}"/>
                    </g:if>
                    <g:else>
                        <g:paginate total="${totalCount}" action="iconicSpecies" id="${params.id}" />
                    </g:else>
                </div>
            </g:if>
        </div> <!-- .col-md-9 -->
        %{--</div> <!-- results -->--}%
    </div>
</div> <!-- content div -->
<asset:javascript src="amplify.js"/>
</body>
</html>