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
<html>
<head>
    <gui:resources components="['dialog']"/>
    <meta name="layout" content="ala2"/>
    <title>ALA Species List Items</title>
    %{--<script type="text/javascript">--}%
        %{--function updateColumns(){--}%
            %{--alert("Update columsn " + ${guids})--}%
            %{--$.ajaxSetup({--}%
                %{--scriptCharset: "utf-8",--}%
                %{--contentType: "text/html; charset=utf-8"--}%
            %{--});--}%
            %{--$.ajax({--}%
                %{--type: "POST",--}%
                %{--url: "speciesListItem/itemDetails",--}%
                %{--data: "Test",--}%
                %{--success: function(data){--}%
                    %{--for(var item in data){--}%
                        %{--if(data.hasOwnProperty(item))--}%
                        %{--{--}%
                            %{--var arr = data.get(item);--}%
                            %{--//now update the image and common name stuff--}%
                            %{--if(item[0]!= null && item[0].length>0){--}%

                            %{--}--}%
                            %{--if(item[1] != null && item[1].length>0){--}%
                                %{--$('#cn_'+item).html(item[1]);--}%
                            %{--}--}%

                        %{--}--}%
                    %{--}--}%
                %{--}--}%
            %{--});--}%
        %{--}--}%

        %{--function init(){--}%
            %{--console.log("Initialising list...")--}%
            %{--updateColumns();--}%
        %{--}--}%

        %{--$(document).ready(function(){--}%
            %{--init();--}%
        %{--});--}%

    %{--</script>--}%

<script type="text/javascript">
    function downloadOccurrences(o){
        if(validateForm()){
            this.cancel();
            //downloadURL = $("#email").val();
            downloadURL = "${request.contextPath}/speciesList/occurrences/${params.id}?type=Download&email="+$("#email").val()+"&reasonTypeId="+$("#reasonTypeId").val()+"&file="+$("#filename").val();
            window.location =  downloadURL//"${request.contextPath}/speciesList/occurrences/${params.id}?type=Download&email=$('#email').val()&reasonTypeId=$(#reasonTypeId).val()&file=$('#filename').val()"
        }
    }
    function downloadFieldGuide(o){
        if(validateForm()){
            this.cancel();
            window.location = "${request.contextPath}/speciesList/fieldGuide/${params.id}"
        }

    }
    function downloadList(o){
         if(validateForm()){
             this.cancel();
             window.location = "${request.contextPath}/speciesListItem/downloadList/${params.id}?file="+$("#filename").val()
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
            <h1>Species List <a href="${collectoryURL}/public/show/${params.id}">${params.id}</a></h1>
        </div><!--inner-->
    </header>

    <div class="inner">
        <div class="four-column">
            <section class="double">
                <div id="buttonDiv">
                <a class="button orange" id="download">Download</a>
                <a class="button orange" title="View Occurrence for this list"
                   href="${request.contextPath}/speciesList/occurrences/${params.id}?type=Search">View Occurrences</a>

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
                                <input type="text" name="email" id="email" value="natasha" size="30"  />
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
                        Note: The field guide may take several minutes to prepare and download.
                    </gui:dialog>
                </div>
            <table>
                <thead>
                <tr>
                      <td>Scientific Name</td>
                      <td>Vernacular Name</td>
                      <td>Image</td>
                      <g:each in="${keys}" var="key">
                          <td>${key}</td>
                      </g:each>
                </tr>
                </thead>
            <tbody>
                <g:each var="result" in="${results}" status="i">
                    <g:set var="bieSpecies" value="${bieItems?.get(result.guid)}"/>
                    <tr class="${result.guid == null? 'unmatched' :(i % 2) == 0 ? 'odd' : 'even'}">
                        <td>
                            <g:if test="${result.guid}">
                                <a href="${bieUrl}/species/${result.guid}">${fieldValue(bean: result, field: "rawScientificName")}</a>
                            </g:if>
                            <g:else>
                                ${fieldValue(bean: result, field: "rawScientificName")}
                            </g:else>
                        </td>
                        <td id="cn_${result.guid}">${bieSpecies?.get(1)}</td>
                        <td id="img_${result.guid}"><img src="${bieSpecies?.get(0)}"/></td>
                        <g:each in="${keys}" var="key">
                            <g:set var="kvp" value="${result.kvpValues.find {it.key == key}}" />
                            <td>${kvp?.vocabValue?:kvp?.value}</td>
                        </g:each>
                      %{--<p>--}%
                          %{--${result.guid} ${result.rawScientificName}--}%
                      %{--</p>--}%
                    </tr>
                </g:each>
            </tbody>
        </table>
        <g:if test="${params.max<totalCount}">
            <div class="pagination" id="searchNavBar">
                <g:paginate total="${totalCount}" action="list" id="${params.id}" />
            </div>
        </g:if>
        </section>
        <section class="last">
            <h3>Statistics</h3>
            Number of records in list: ${totalCount}<br>
            Number of distinct species: ${distinctCount}<br>
            Number of unknown species: ${noMatchCount} <br>
        </section>
            </div> <!-- four-column -->
        %{--</div> <!-- results -->--}%
    </div>
</div> <!-- content div -->
</body>
</html>