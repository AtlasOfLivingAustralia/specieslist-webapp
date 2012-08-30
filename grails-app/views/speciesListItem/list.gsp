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
    <gui:resources components="['dialog']"/>
    <meta name="layout" content="ala2"/>
    <script language="JavaScript" type="text/javascript" src="${resource(dir:'js',file:'facets.js')}"></script>
    <script language="JavaScript" type="text/javascript" src="${resource(dir:'js',file:'getQueryParam.js')}"></script>
    <title>ALA Species List Items</title>
    <style type="text/css">
    #buttonDiv {display: none;}
    </style>

<script type="text/javascript">
    function init(){
        document.getElementById("buttonDiv").style.display = "block";
    }
    $(document).ready(function(){ init(); });
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

</head>
<body class="yui-skin-sam">
<div id="content" class="species">
    <header id="page-header">
        <div class="inner">
            <nav id="breadcrumb">
                <ol>
                    <li><a href="http://www.ala.org.au">Home</a></li>
                    <li><a href="${request.contextPath}/public/speciesLists">All Species Lists</a></li>
                    <li class="last">Species List Items</li>
                </ol>
            </nav>
            <hgroup class="leftfloat">
                <h1>Species List <a href="${collectoryUrl}/public/show/${params.id}">${speciesList?.listName}</a></h1>
            </hgroup>
            <div class="rightfloat" id="buttonDiv">
                %{--<div id="buttonDiv" class="buttonDiv">--}%
                    <a class="button orange" title="View the download options for this species list." id="download">Download</a>
                    <a class="button orange" title="View occurrences for up to ${maxDownload} species on the list"
                       href="${request.contextPath}/speciesList/occurrences/${params.id}${params.toQueryString()}&type=Search">View Occurrences</a>

                    <gui:dialog
                            title="Download"
                            draggable="true"
                            id="downloadDialog"
                            width= "50em"
                            buttons="[
                                    [text:'Download Occurrence Records', handler: 'downloadOccurrences', isDefault: false],
                                    [text:'Download Species Field Guide', handler: 'downloadFieldGuide', isDefault: false],
                                    [text:'Download Species List', handler: 'downloadList', isDefault: false]
                            ]"
                            triggers="[show:[id:'download', on:'click']]"
                    >
                        <p id="termsOfUseDownload">
                            By downloading this content you are agreeing to use it in accordance with the Atlas of Living Australia
                            <a href="http://www.ala.org.au/about/terms-of-use/#TOUusingcontent">Terms of Use</a> and any Data Provider
                        Terms associated with the data download.
                            <br/><br/>
                            Please provide the following details before downloading (* required):
                        </p>
                        <fieldset>
                            <p><label for="email">Email</label>
                                <input type="text" name="email" id="email" value="${request.remoteUser}" size="30"  />
                            </p>
                            <p><label for="filename">File Name</label>
                                <input type="text" name="filename" id="filename" value="data" size="30"  />
                            </p>
                            <p><label for="reasonTypeId" style="vertical-align: top">Download Reason *</label>
                                <select name="reasonTypeId" id="reasonTypeId">
                                    <option value="">-- select a reason --</option>
                                    <g:each in="${downloadReasons}" var="reason">
                                        <option value="${reason.key}">${reason.value}</option>
                                    </g:each>
                                </select>
                            </p>
                        </fieldset>
                        Note: The field guide may take several minutes to prepare and download.<br/>
                        A maximum of ${maxDownload} species will be considered for each download.
                    </gui:dialog>
                %{--</div> <!-- button div -->--}%
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
                            Unrecognised Taxa
                           </h3>
                       </g:if>

                   </div>
                </section>
                <section class="refine">
                    <g:if test="${facets.size()>0 || params.fq}">
                        <h3>Refine Results</h3>
                        <g:if test="${params.list('fq').size()>0&& params.list('fq').get(0).length()>0}">
                        <div id="currentFilter">
                            <h4>
                                <span class="FieldName">Current Filters</span>
                            </h4>
                         <table>
                        <g:each in="${params.list('fq')}" var="fq">
                            <g:if test="${fq.length() >0}">
                            <tr>
                                <td>${fq}</td>
                                %{--<a class="removeLink" onclick="removeFacet('family:ACANTHASPIDIIDAE'); return false;" href="#" oldtitle="remove filter" aria-describedby="ui-tooltip-1">X</a>--}%
                                <td>  <b>[<a class="removeLink" title="Remove Filter" onclick="removeFacet('${fq}')">X</a>]</b></td>
                            </tr>
                            </g:if>
                        </g:each>
                         </table>
                        </div>
                        </g:if>
                        <g:if test="${facets.containsKey("listProperties")}">
                            <g:each in="${facets.get("listProperties")}" var="value">
                                <h4>
                                    <span class="FieldName">${value.getKey()}</span>
                                    <ul class="facets">
                                    <g:each in="${value.getValue()}" var="arr">
                                        <li>
                                        <a href="?fq=kvp ${arr[0]}:${arr[1]}${queryParams}">${arr[2]?:arr[1]} </a> (${arr[3]})
                                        </li>
                                    </g:each>
                                    </ul>
                                </h4>
                            </g:each>
                        </g:if>
                    </g:if>

                </section>
            </div><!-- boxed attached -->
        </div> <!-- col narrow -->

        <div class="col-wide last">
           <div class="tabs-panes-noborder">
            <section class="double">

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
</body>
</html>