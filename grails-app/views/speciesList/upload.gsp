%{--<%@ page import="au.org.ala.names.ws.api.SearchStyle" %>--}%
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
<html>
<head>
<meta name="layout" content="${grailsApplication.config.skin.layout}"/>
<meta name="breadcrumb" content="Upload a list"/>
<meta name="breadcrumbParent" content="${request.contextPath}/,Species lists"/>
<title>${message(code:'generic.lists.button.uploadList.label', default:'Upload a list')} | ${message(code:'public.lists.header', default:'Species lists')} | ${grailsApplication.config.skin.orgNameLong}</title>

<asset:stylesheet src="fileupload.css"/>
<asset:script type="text/javascript">
    function init(){
        reset();
    }

    function reset(){
        $('#recognisedDataDiv').hide();
        if("${list}")
            $('#uploadDiv').show();
        else
            $('#uploadDiv').hide();
        $('#statusMsgDiv').hide();
        $('#uploadmsg').hide();
        refreshSDSRows();
    }

    function refreshSDSRows(){
        var ischecked=$('#isSDS').is(':checked');
        var rows = $('table.listDetailTable tr');
        if(ischecked) {
            rows.filter('.SDSOnly').show();
        } else {
            rows.filter('.SDSOnly').hide();
        }
    }

    function parseColumns(){
        if ($('#copyPasteData').val().trim() == "" && $('#csvFileUpload').val().trim() == "") {
            reset();
        } else if ($('#copyPasteData').val().trim() != "" && $('#csvFileUpload').val().trim() != "") {
            reportError("${message(code:'upload.lists.info', default:'<b>Error:</b> You must either upload a file <i>or</i> copy and paste the list into the provided field, not both.')}");
        } else {
            //console.log($('#copyPasteData').val())
            $.ajaxSetup({
                scriptCharset: "utf-8",
                contentType: "text/html; charset=utf-8"
            });
            var url = "${createLink(controller:'speciesList', action:'parseData')}";
            var isFileUpload = $('#csvFileUpload').val().trim() != "";
            $.ajax({
                type: "POST",
                url: url,
                processData: !isFileUpload,
                contentType: (isFileUpload ? false : ""),
                data: isFileUpload ? new FormData(document.forms.namedItem("csvUploadForm")) : $('#copyPasteData').val(),
                success: function(data) {
                    $('#recognisedDataDiv').show();
                    $('#recognisedData').html(data);
                    if (isFileUpload) $('#recognisedData input:first').focus();
                    $('#uploadDiv').show();
                    $('#listvocab').hide();
                },
                error: function(jqXHR, textStatus, error) {
                    //console.log("jqXHR", jqXHR);
                    var ExtractedErrorMsg = $(jqXHR.responseText).find(".error-details").clone().wrap('<p>').parent().html(); // hack to get outerHtml
                    reportError("<b>Error:</b> " + error + " (" + jqXHR.status + ")<br/><code style='background-color:inherit;'>" + ExtractedErrorMsg + "</code>");
                }
            });
        }
    }

    function updateCustom(checked){
        if (checked) {
            hide('manualMapping');
        } else {
            show('manualMapping');
        }
    }
    function hide(obj)
    {
        obj1 = document.getElementById(obj);
        obj1.style.visibility = 'hidden';
    }
    function show(obj)
    {
        obj1 = document.getElementById(obj);
        obj1.style.visibility = 'visible';
    }

    function viewVocab(){
        $('#listvocab').show();
        $('#viewVocabButton').hide();
    }

    function hideVocab(){
        $('#listvocab').hide();
        $('#viewVocabButton').show();
    }

    function validateForm(){
        var isValid = false;
        var typeId = $("#listTypeId option:selected").val();
        if($('#listTitle').val().length > 0){
            isValid = true
        }
        else{
            $('#listTitle').focus();
            alert("${message(code:'upload.lists.uploadprocess.missingfield.message01', default:'You must supply a species list title')}");
        }
        if(isValid){
            if(typeId){
                if ('LOCAL_LIST' == typeId && $('#listWkt').val().length == 0){
                    isValid = false;
                    $('#listWkt').focus();
                    alert("You must supply a spatial bounds");
                }
                else{
                    isValid = true
                }
            }
            else{
                isValid = false
                $("#listTypeId").focus();
                alert("${message(code:'upload.lists.uploadprocess.missingfield.message02', default:'You must supply a list type')}");
            }
        }
        return isValid;
    }

    function reportError(error){
        $('#statusMsgDiv').hide();
        $('#uploadFeedback div').html(error);
        $('#uploadFeedback').show();
    }

    function uploadSpeciesList(){
        if(validateForm()){
            var isFileUpload = $('#csvFileUpload').val().trim() != "";

            var map = getVocabularies();
            map['headers'] = getColumnHeaders();
            map['speciesListName'] = $('#listTitle').val();
            map['description'] = $('#listDesc').val();
            map['listUrl'] = $('#listURL').val();
            map['listWkt'] = $('#listWkt').val();
            if (!isFileUpload) {
                map['rawData']  =$('#copyPasteData').val();
            }
            map['listType'] =$('#listTypeId').val();
            map['isPrivate']=$('#isPrivate').is(':checked');
            //add the existing data resource uid if it is provided to handle a resubmit
            if("${resourceUid}")
                map['id'] = "${resourceUid}"
            //if the isBIE checkbox exists add the value
            if($('#isBIE').length>0){
                map['isBIE']=$('#isBIE').is(':checked');
            }
            //admin values
            if ($('#isAuthoritative').length>0) {
                map['isAuthoritative']=$('#isAuthoritative').is(':checked');
            }
            if ($('#isThreatened').length>0) {
                map['isThreatened']=$('#isThreatened').is(':checked');
            }
            if ($('#isInvasive').length>0) {
                map['isInvasive']=$('#isInvasive').is(':checked');
            }
            if ($('#sdsRegion').length>0) {
                map['region'] = $('#sdsRegion').val();
            }
            //if the isSDS checkbox exists add the value
            if($('#isSDS').length>0){
                map['isSDS']=$('#isSDS').is(':checked');
                var ischecked=$('#isSDS').is(':checked');
                if(ischecked){
                    //add the SDS only properties
                    //SDS region is also part of admin values
                    map['authority'] = $('#authority').val();
                    map['category'] = $('#category').val();
                    map['generalisation'] = $('#generalisation').val();
                    map['sdsType'] = $('#sdsType').val();
                }
            }
            map['looseSearch'] = $('#looseSearch').val();
            // map['searchStyle'] = $('#searchStyle').val();
            //console.log("The map: ",map);
            $('#recognisedDataDiv').hide();
            $('#uploadDiv').hide();
            $('#statusMsgDiv').show();
            var url = "${createLink(controller:'speciesList', action:'uploadList')}";

            var data
            if (isFileUpload) {
                data = new FormData(document.forms.namedItem("csvUploadForm"))
                data.append("formParams", JSON.stringify(map))
            }
            else {
                data = JSON.stringify(map)
            }
            $.ajax({
                type: "POST",
                url: url,
                processData: !isFileUpload,
                contentType: (isFileUpload ? false : ""),
                data: data,
                timeout: 1800000,
                success: function(response){
                    //console.log(response, response.url)
                    if(response.url != null && response.error == null) {
                        window.location.href = response.url;
                    } else {
                        reportError(response.error)
                    }

                },
                error: function(xhr, textStatus, errorThrown) {
                    //console.log('Error!  Status = ' ,xhr.status, textStatus, errorThrown, xhr.responseText);
                    reportError("Error: " +errorThrown);
                }

            });
        }
    }

    function getVocabularies(){
        var potentialVocabH3s = $('div.vocabDiv');
        var vocabMap = {};
        $.each(potentialVocabH3s, function(index,vdiv){
            var value = "";
            var h3value = "vocab_"+$(vdiv).find('h3:first').text();

            $(vdiv).find('table').find('tbody').find('tr').each(function(index2,vrow){

                if(value.length>0)
                    value = value +",";

                var vkey = $(vrow).children().eq(0).text();

                var vvalue= $(vrow).children().eq(1).children().eq(0).val();
                if(vvalue.length>0)
                    value = value + vkey +":"+vvalue;
            })

            vocabMap[h3value] = value;
        })
        return vocabMap;
    }

    function getColumnHeaders(){

        var columnHeaderInputs = $('input.columnHeaderInput');
        var columnHeadersCSV = "";
        var i = 0;
        $.each(columnHeaderInputs, function(index, input){
            if(index>0){
                columnHeadersCSV = columnHeadersCSV + ",";
            }
            columnHeadersCSV = columnHeadersCSV + input.value;
            i++;
        });

        return columnHeadersCSV;
    }

    function updateH3(column){
        var columnHeaderInputs = $('input.columnHeaderInput');
        $.each(columnHeaderInputs, function(index, input){
            $("h3[for='"+input.id+"']").html($(input).val());
        })
    }

    //setup the page
    $(document).ready(function(){
        init();
        $("#isSDS").change(function(){
            refreshSDSRows();
      });
    });

</asset:script>
</head>

<body class="upload">

</form>
<div id="content" class="container">
    <header id="page-header">
        <hgroup>
            <g:if test="${list}">
                <h1><g:message code="upload.heading.hasList" default="Upload a list"/></h1>
            </g:if>
            <g:else>
                <h1><g:message code="upload.heading" default="Upload a list"/></h1>
            </g:else>
        </hgroup>
    </header>
    <div>
        <div class="message alert alert-info" id="uploadmsg" style="clear:right;">${flash.message}</div>
        <div id="section" class="col-wide">


            <g:if test="${resourceUid}">
                <div class="message alert alert-info"><g:message code="upload.instructions.hasList" default="Upload a list"/></div>
            </g:if>

            <p>${message(code:'upload.lists.header01.text01',  args:[grailsApplication.config.skin.orgNameShort], default:'A species list should consist of a list of scientific or common names and some optional associated properties. Provide input consisting of a header line and some rows of data and we will attempt to map your list to taxon names known to the {0} system. A species list can be uploaded either as a CSV file, or as copy and pasted text.')}</p>
            <div id="initialPaste">
                <h3>${message(code:'upload.lists.subheader.text01', default:'Option 1: Select a CSV file to upload here')}</h3>
                ${message(code:'upload.lists.subheader.des01', default:'Please note that the file upload feature requires a modern browser (such as Chrome, Firefox, or Internet Explorer 10)')}

                <g:uploadForm name="csvUploadForm" id="csvUploadForm" action="parseData">
                    <div class="fileupload fileupload-new pull-left" data-provides="fileupload">
                        <div class="btn-group" role="group">
                            <button class="btn btn-default disabled fileupload-exists">
                                <i class="glyphicon glyphicon-file fileupload-exists"></i>
                                <span class="fileupload-preview"></span>
                            </button>

                            <span class="btn btn-default btn-file">
                                <span class="fileupload-new">${message(code:'upload.lists.button01', default:'Select file')}</span>
                                <span class="fileupload-exists">${message(code:'upload.lists.button03', default:'Change')}</span>
                                <input type="file" name="csvFile" id="csvFileUpload"/>
                            </span>
                            <button href="#" class="btn btn-default fileupload-exists" data-dismiss="fileupload">${message(code:'upload.lists.button04', default:'Remove')}</button>
                        </div>
                    </div>

                </g:uploadForm>

                <div style="clear: both"></div>

                <g:submitButton id="checkData2" class="actionButton btn btn-default" name="checkData" value="${message(code:'upload.lists.button02', default:'Check Data')}"
                                onclick="javascript:parseColumns();"/>

                <h3>${message(code:'upload.lists.subheader.text02', default:'Option 2: Paste your species list here')}</h3>
                <p>${message(code:'upload.lists.subheader.des02', default:'To paste your data, click the rectangle below, and type <strong>control-V (Windows)</strong> or <strong>command-V (Macintosh)')}'</strong>.
                </p>

                <g:textArea
                        id="copyPasteData"
                        name="copyPasteData" rows="10" cols="120" style="width:100%;"
                        onkeyup="javascript:window.setTimeout('parseColumns()', 500, true);"></g:textArea>

                <g:submitButton id="checkData" class="actionButton btn btn-default" name="checkData" value="${message(code:'upload.lists.button02', default:'Check Data')}"
                                onclick="javascript:parseColumns();"/>
                <p id="processingInfo"></p>

            </div>

            <div id="recognisedData" tabindex="-1"></div>

            <!-- Moved the upload div to here so that the values can be remembered to support a reload of the species list-->

            <div id="uploadDiv">
                <h2>${message(code:'upload.lists.uploadinfo.header', default:'3. Upload Species List')}</h2>
                <p>${message(code:'upload.lists.uploadinfo.text01', default:'Please supply a title for your list, and indicate the type of list you are uploading from the options provided.')}<br/>
                    ${message(code:'upload.lists.uploadinfo.text02', default:'You can optionally supply a description, an external URL as a reference to the list and a geospatial bounds for the list (in WKT format).')}
                </p>
                <div id="processSampleUpload" class="well">
                    <table class="listDetailTable table table-condensed borderless">
                        <tbody>
                        <tr>
                            <td>
                                <label for="listTitle"><g:message code="upload.listname.label" default="Title*"/></label>
                            </td>
                            <td>
                                <g:textField name="listTitle" style="width:99%" value="${list?.listName}"/>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="listTypeId"><g:message code="upload.lists.field.listType.label" default="List Type*"/></label></td>
                            <td>
                                <select name="listTypeId" id="listTypeId">
                                    <option value="">${message(code:'upload.lists.field.listType.select', default:'-- select a type --')}</option>
                                    <g:each in="${au.org.ala.specieslist.ListType.values()}" var="type"><option value="${type.name()}" ${(list?.listType == type) ? 'selected="selected"':''}>${message(code:type.i18nValue, default:type.displayValue)}</option></g:each>
                                </select>
                            </td>

                        </tr>
                        <tr>
                            <td><label for="isPrivate"><g:message code= "speciesList.isPrivate.label" default= "Is private in species list"/></label> </td>
                            <td><g:checkBox name="isPrivate" id="isPrivate" checked="${list?.isPrivate}"/></td>
                        </tr>
                        <g:if test="${request.isUserInRole("ROLE_ADMIN")}">
                            <tr>
                                <td><label for="isBIE"><g:message code= "speciesList.isBIE.label" default= "Included in species pages"/></label> </td>
                                <td><g:checkBox name="isBIE" id="isBIE" checked="${list?.isBIE}"/></td>
                            </tr>
                            <tr>
                                <td><label for="isSDS"><g:message code= "speciesList.isSDS.label" default= "Part of the Sensitive Data Service"/></label> </td>
                                <td><g:checkBox name="isSDS" id="isSDS" checked="${list?.isSDS}"/></td>
                            </tr>
                            <tr>
                                <td><label for="isAuthoritative"><g:message code= "speciesList.isAuthoritative.label" default= "Authoritative"/></label> </td>
                                <td><g:checkBox name="isAuthoritative" id="isAuthoritative" checked="${list?.isAuthoritative}"/></td>
                            </tr>
                            <tr>
                                <td><label for="isThreatened"><g:message code= "speciesList.isThreatened.label" default= "Threatened"/></label> </td>
                                <td><g:checkBox name="isThreatened" id="isThreatened" checked="${list?.isThreatened}"/></td>
                            </tr>
                            <tr>
                                <td><label for="isInvasive"><g:message code= "speciesList.isInvasive.label" default= "Invasive"/></label> </td>
                                <td><g:checkBox name="isInvasive" id="isInvasive" checked="${list?.isInvasive}"/></td>
                            </tr>
                            <td><label>${message(code:'speciesList.region.label', deafult:'Region')}</label></td>
                            <td>
                                <g:textField name="sdsRegion" style="width:99%" value="${list?.region}"/>
                            </td>
                        </g:if>
                        <tr class="SDSOnly">
                            <td><label>${message(code:'speciesList.authority.label', deafult:'Authority')}</label></td>
                            <td>
                                <g:textField name="authority" style="width:99%" value="${list?.authority}"/>
                            </td>
                        </tr>
                        <tr class="SDSOnly">
                            <td><label>${message(code:'speciesList.category.label', default:'Category')}</label></td>
                            <td>
                                <g:textField name="category" style="width:99%" value="${list?.category}"/>
                            </td>
                        </tr>
                        <tr class="SDSOnly">
                            <td><label>${message(code:'speciesList.generalisation.label', default:'Generalisation')}</label></td>
                            <td>
                                <g:textField name="generalisation" style="width:99%" value="${list?.generalisation}"/>
                            </td>
                        </tr>
                        <tr class="SDSOnly">
                            <td><label>${message(code:'speciesList.sdsType.label', default:'SDS Type')}</label></td>
                            <td>
                                <g:select from="['CONSERVATION', 'BIOSECURITY']" name="sdsType" style="width:99%" value="${list?.sdsType}" />
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label for="listDesc"><g:message code="upload.listdesc.label" default="Description"/></label>
                            </td>
                            <td>
                                <g:textArea cols="100" class="full-width" rows="5" name="listDesc">${list?.description}</g:textArea>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label for="listURL"><g:message code="upload.listlink.label" default="URL"/></label>
                            </td>
                            <td>
                                <g:textField name="listURL" class="full-width">${list?.url}</g:textField>
                            </td>

                        </tr>
                        <tr>
                            <td>
                                <label for="listWkt"><g:message code="upload.lists.field.wkt.label" default="Spatial bounds for data (WKT)"/></label>
                            </td>
                            <td>
                                <g:textArea cols="100" rows="5" class="full-width" name="listWkt">${list?.wkt}</g:textArea>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="looseSearch">${message(code:'speciesList.looseSearch.label', default:'Loose Search')}</label></td>
                            <td>
                                <g:select noSelection="${['':'--']}" from="[true, false]" name="looseSearch" style="width:99%" value="${list?.looseSearch}" />
                            </td>
                        </tr>
%{--                        <tr>--}%
%{--                            <td><label for="searchStyle">${message(code:'speciesList.searchStyle.label', default:'Search Style')}</label></td>--}%
%{--                            <td>--}%
%{--                                <g:select name="searchStyle" noSelection="${['':'--']}" from="${SearchStyle.values()}" style="width:99%" value="${list?.searchStyle}" />--}%
%{--                            </td>--}%
%{--                        </tr>--}%
                        </tbody>
                    </table>
                    <input id="uploadButton" class="datasetName actionButton btn btn-primary" type="button" value="${message(code:'upload.heading',default:'Upload')}"
                           onclick="javascript:uploadSpeciesList();"/>
                </div>
            </div>

            <div id="uploadFeedback" style="clear:right;display:none;" class="alert alert-danger">
                <button type="button" class="close" onclick="$(this).parent().hide()">×</button>
                <div></div>
            </div>
            <div id="uploadProgressBar">
            </div>
        </div>
    </div>
    <div id="statusMsgDiv">
        <div class="well">
            <h3><img src='${asset.assetPath(src:'spinner.gif')}' id='spinner'/>&nbsp;&nbsp;<span>${message(code:'upload.lists.uploadprocess.header', default:'Uploading your list...')}</span></h3>
            <p>${message(code:'upload.lists.uploadprocess.text', default:'This will take a few moments depending on the size of the list.')}</p>
        </div>
    </div>
</div> <!-- content div -->
<asset:javascript src="fileupload.js"/>
</body>
</html>
