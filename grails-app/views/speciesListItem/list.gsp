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
<g:set var="bieUrl" value="${grailsApplication.config.bie.baseURL}"/>
<g:set var="collectoryUrl" value="${grailsApplication.config.collectory.baseURL}" />
<g:set var="maxDownload" value="${grailsApplication.config.downloadLimit}" />
<html>
<head>
    %{--<gui:resources components="['dialog']"/>--}%
    <r:require modules="fancybox"/>
    <meta name="layout" content="ala2"/>
    <link rel="stylesheet" href="${resource(dir:'css',file:'scrollableTable.css')}"/>
    <script language="JavaScript" type="text/javascript" src="${resource(dir:'js',file:'facets.js')}"></script>
    <script language="JavaScript" type="text/javascript" src="${resource(dir:'js',file:'getQueryParam.js')}"></script>
    <title>Species list items | Atlas of Living Australia</title>
    <style type="text/css">
    #buttonDiv {display: none;}
    #refine {display:none;}
    </style>

    <script type="text/javascript">
    function init(){
        document.getElementById("buttonDiv").style.display = "block";
        document.getElementById("refine").style.display = "block";
    }
    $(document).ready(function(){
        init();

        // download link
        $("#downloadLink").fancybox({
            'hideOnContentClick' : false,
            'hideOnOverlayClick': true,
            'showCloseButton': true,
            'titleShow' : false,
            'autoDimensions' : false,
            'width': 500,
            'height': 400,
            'padding': 10,
            'margin': 10,
            onCleanup: function() {
                $("label[for='reasonTypeId']").css("color","#444");
            }
        });

        // fancybox div for refining search with multiple facet values
        $(".multipleFacetsLink").fancybox({
            'hideOnContentClick' : false,
            'hideOnOverlayClick': true,
            'showCloseButton': true,
            'titleShow' : false,
            'transitionIn': 'elastic',
            'transitionOut': 'elastic',
            'speedIn': 400,
            'speedOut': 400,
            'scrolling': 'auto',
            'centerOnScroll': true,
            'autoDimensions' : false,
            'width': 560,
            'height': 560,
            'padding': 10,
            'margin': 10

        });


    });


//    function loadMultiFacets(facetName, displayName) {
//        console.log(facetName, displayName)
//        console.log("#div"+facetName,$("#div"+facetName).innerHTML)
//        $("div#dynamic").innerHTML=$("#div"+facetName).innerHTML;
//    }

    function downloadOccurrences(o){
        if(validateForm()){
            this.cancel();
            //downloadURL = $("#email").val();
            downloadURL = "${request.contextPath}/speciesList/occurrences/${params.id}${params.toQueryString()}&type=Download&email="+$("#email").val()+"&reasonTypeId="+$("#reasonTypeId").val()+"&file="+$("#filename").val();
            window.location =  downloadURL//"${request.contextPath}/speciesList/occurrences/${params.id}?type=Download&email=$('#email').val()&reasonTypeId=$(#reasonTypeId).val()&file=$('#filename').val()"
        }
    }
    function downloadFieldGuide(o){
        if(validateForm()){
            this.cancel();
            //alert(${params.toQueryString()})
            window.location = "${request.contextPath}/speciesList/fieldGuide/${params.id}${params.toQueryString()}"
        }

    }
    function downloadList(o){
         if(validateForm()){
             this.cancel();
             window.location = "${request.contextPath}/speciesListItem/downloadList/${params.id}${params.toQueryString()}&file="+$("#filename").val()
         }
    }
    function validateForm() {
        var isValid = false;
        var reasonId = $("#reasonTypeId option:selected").val();

        if (reasonId) {
            isValid = true;
        } else {
            $("#reasonTypeId").focus();
            $("label[for='reasonTypeId']").css("color","red");
            alert("Please select a \"download reason\" from the drop-down list");
        }

        return isValid;
    }
</script>
    <r:layoutResources/>
</head>
<body class="yui-skin-sam species">
<r:layoutResources/>
<div id="content" >
    <header id="page-header">
        <div class="inner">
            <nav id="breadcrumb">
                <ol>
                    <li><a href="http://www.ala.org.au">Home</a></li>
                    <li><a href="${request.contextPath}/public/speciesLists">Species lists</a></li>
                    <li class="last">Species list items</li>
                </ol>
            </nav>
            <hgroup class="leftfloat">
                <h1>Species List </h1>
                <h2><a href="${collectoryUrl}/public/show/${params.id}">${speciesList?.listName}</a></h2>
            </hgroup>
            <div class="rightfloat" id="buttonDiv">
                %{--<div id="buttonDiv" class="buttonDiv">--}%
                    <a href="#download" class="button orange" title="View the download options for this species list." id="downloadLink">Download</a>
                    <a class="button orange" title="View occurrences for up to ${maxDownload} species on the list"
                       href="${request.contextPath}/speciesList/occurrences/${params.id}${params.toQueryString()}&type=Search">View Occurrences</a>
                <div style="display:none">
                    <g:render template="/download"/>
                </div>

            </div>  <!-- rightfloat -->
        </div><!--inner-->
    </header>

    <div class="inner">

        <div class="col-narrow">
            <div class="boxed attached">
                %{--<div id="customiseList" >--}%

                    %{--<a id="customiseListButton" class="buttonDiv">Customise</a>--}%
                %{--</div>--}%
                <section class="meta">
                    <div class="matchStats">

                       <h3>
                           <span class="count">${totalCount}</span>
                           Number of Taxa
                       </h3>
                       <br/>
                       <h3>
                           <span class="count">${distinctCount}</span>
                           Distinct Species
                       </h3>

                       <g:if test="${noMatchCount>0}">
                           <br/>
                           <h3>
                               <span class="count">${noMatchCount}</span>
                            <a href="?fq=guid:null${queryParams}" title="View unrecognised taxa">Unrecognised Taxa </a>
                           </h3>
                       </g:if>

                   </div>
                </section>
                <section class="refine" id="refine">
                    <g:if test="${facets.size()>0 || params.fq}">
                        <h2>Refine results</h2>
                        <g:set var="fqs" value="${params.list('fq')}" />
                        <g:if test="${fqs.size()>0&& fqs.get(0).length()>0}">
                        <div id="currentFilter">
                            <h3>
                                <span class="FieldName">Current Filters</span>
                            </h3>
                        <div id="currentFilters" class="subnavlist">
                            <ul>
                                <g:each in="${fqs}" var="fq">
                                    <g:if test="${fq.length() >0}">
                                    <li>
                                        ${fq.replaceFirst("kvp ","")}
                                        %{--<a class="removeLink" onclick="removeFacet('family:ACANTHASPIDIIDAE'); return false;" href="#" oldtitle="remove filter" aria-describedby="ui-tooltip-1">X</a>--}%
                                        [<b><a class="removeLink" title="Remove Filter" onclick="removeFacet('${fq}')">X</a></b>]
                                    </li>
                                    </g:if>
                                </g:each>
                            </ul>
                         </div>
                        </div>
                        </g:if>
                        <g:if test="${facets.containsKey("listProperties")}">
                            <g:each in="${facets.get("listProperties")}" var="value">
                                <h3>
                                    <span class="FieldName">${value.getKey()}</span>
                                </h3>
                                <div id="facet-${value.getKey()}" class="subnavlist">
                                    <ul>
                                        <g:set var="i" value="${0}" />
                                        <g:set var="values" value="${value.getValue()}" />
                                        %{--<g:each in="${value.getValue()}" var="arr">--}%
                                        <g:while test="${i < 4 && i<values.size()}">

                                            <g:set var="arr" value="${values.get(i)}" />
                                            <li>
                                                <a href="?fq=kvp ${arr[0]}:${arr[1]}${queryParams}">${arr[2]?:arr[1]}</a>  (${arr[3]})
                                            </li>
                                            <%i++%>
                                        </g:while>
                                        <g:if test="${values.size()>4}">
                                            <div class="showHide">
                                                <a href="#div${value.getKey().replaceAll(" " ,"_")}" class="multipleFacetsLink" id="multi-${value.getKey()}"
                                                   title="See more options or refine with multiple values">choose more...</a>
                                                <div style="display:none">
                                                <div id="div${value.getKey().replaceAll(" " ,"_")}">

                                                %{--<a class="multipleFacetsLink" title="See more options." id="options${value.getKey()}">choose more...</a>--}%
                                                %{--<gui:dialog--}%
                                                        %{--title="Refine List"--}%
                                                        %{--draggable="true"--}%
                                                        %{--id="dialog_${value.getKey().replaceAll(" " ,"_")}"--}%

                                                        %{--buttons="[--}%

                                                                %{--[text:'Close', handler: 'function() {this.cancel();}', isDefault: true]--}%
                                                        %{--]"--}%
                                                        %{--triggers="[show:[id:'options'+value.getKey(), on:'click']]"--}%
                                                %{-->--}%

                                                    <h3>Refine your search</h3>
                                                    <table class='compact scrollTable'>
                                                        <thead class='fixedHeader'>
                                                            <tr class='tableHead'>
                                                                <th>&nbsp;</th>
                                                                <th>${value.getKey()}</th>
                                                                <th width=>Count</th>
                                                            </tr>
                                                        </thead>
                                                        <tbody class='scrollContent'>
                                                            <g:each in="${value.getValue()}" var="arr">
                                                            <tr>
                                                                <td>&nbsp</td>
                                                                <td><a href="?fq=kvp ${arr[0]}:${arr[1]}${queryParams}">${arr[2]?:arr[1]} </a></td>
                                                                <td >${arr[3]}</td>
                                                            </tr>
                                                            </g:each>
                                                        </tbody>
                                                    </table>
                                                %{--</gui:dialog>--}%
                                                    </div>

                                            </div>
                                               </div><!-- invisible content div for facets -->
                                        </g:if>
                                        %{--</g:each>--}%
                                        </ul>
                                    </div>

                            </g:each>
                            <div style="display:none"><!-- fancybox popup div -->
                                <div id="multipleFacets">
                                    <h3>Refine your search</h3>
                                    <div id="dynamic" class="tableContainer"></div>
                                    %{--<div id='submitFacets'>--}%
                                        %{--<input type='submit' class='submit' id="include" value="INCLUDE selected items in search"/>--}%
                                        %{--&nbsp;--}%
                                        %{--<input type='submit' class='submit' id="exclude" value="EXCLUDE selected items from search"/>--}%
                                    %{--</div>--}%
                                </div>
                            </div>
                        </g:if>
                    </g:if>

                </section>
            </div><!-- boxed attached -->
        </div> <!-- col narrow -->

        <div class="col-wide last">
           <div class="tabs-panes-noborder">
            <section class="double">
            <div class="fwtable">
            <table class="tableList">
                <thead>
                <tr>
                      <td>Scientific Name</td>
                      <td>Common Name</td>
                    <g:each in="${keys}" var="key">
                        <td>${key}</td>
                    </g:each>
                      <td>Image</td>

                </tr>
                </thead>
            <tbody>
                <g:each var="result" in="${results}" status="i">
                    <g:set var="bieSpecies" value="${bieItems?.get(result.guid)}"/>
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        <td>
                            <g:if test="${result.guid}">
                                <a href="${bieUrl}/species/${result.guid}">${fieldValue(bean: result, field: "rawScientificName")}</a>
                            </g:if>
                            <g:else>
                                ${fieldValue(bean: result, field: "rawScientificName")}
                            </g:else>
                            <g:if test="${result.guid == null}">
                                (unmatched)
                            </g:if>
                        </td>
                        <td id="cn_${result.guid}">${bieSpecies?.get(1)}</td>
                        <g:each in="${keys}" var="key">
                            <g:set var="kvp" value="${result.kvpValues.find {it.key == key}}" />
                            <td>${kvp?.vocabValue?:kvp?.value}</td>
                        </g:each>
                        <td id="img_${result.guid}"><a href="${bieUrl}/species/${result.guid}" ><img src="${bieSpecies?.get(0)}"/></a></td>

                      %{--<p>--}%
                          %{--${result.guid} ${result.rawScientificName}--}%
                      %{--</p>--}%
                    </tr>
                </g:each>
            </tbody>
        </table>
        </div>
        <g:if test="${params.max<totalCount}">
            <div class="pagination" id="searchNavBar">
                <g:if test="${params.fq}">
                    <g:paginate total="${totalCount}" action="list" id="${params.id}" params="${[fq: params.fq]}"/>
                 </g:if>
                <g:else>
                    <g:paginate total="${totalCount}" action="list" id="${params.id}" />
                </g:else>
            </div>
        </g:if>
        </section>
            </div> <!-- tabs-panes-noborder -->
            </div> <!-- "col-wide last" -->
        %{--</div> <!-- results -->--}%
    </div>
</div> <!-- content div -->
%{--<script type="text/javascript">--}%
    %{--function loadMultiFacets(facetName, displayName) {--}%
        %{--console.log(facetName, displayName)--}%
        %{--console.log("#div"+facetName,$("#div"+facetName),$("#div"+facetName).innerHTML)--}%
        %{--$("div#dynamic").innerHTML=$("#div"+facetName).innerHTML;--}%
    %{--}--}%
%{--</script>--}%
</body>
</html>