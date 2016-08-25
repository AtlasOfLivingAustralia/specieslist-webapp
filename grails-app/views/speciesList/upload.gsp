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
<title>Upload a list | Species lists | ${grailsApplication.config.skin.orgNameLong}</title>
<script type="text/javascript">
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
            reportError("<b>Error:</b> You must either upload a file <i>or</i> copy and paste the list into the provided field, not both.");
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
                contentType: !isFileUpload,
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
            isValid=true
        }
        else{
            $('#listTitle').focus();
            alert("You must supply a species list title");
        }
        if(isValid){

            if(typeId){
                isValid = true
            }
            else{
                isValid=false
                $("#listTypeId").focus();
                alert("You must supply a list type");
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
            //add the existing data resource uid if it is provided to handle a resubmit
            if("${resourceUid}")
                map['id'] = "${resourceUid}"
            //if the isBIE checkbox exists add the value
            if($('#isBIE').length>0){
                map['isBIE']=$('#isBIE').is(':checked');
            }
            //if the isSDS checkbox exists add the value
            if($('#isSDS').length>0){
                map['isSDS']=$('#isSDS').is(':checked');
                var ischecked=$('#isSDS').is(':checked');
                if(ischecked){
                    //add the SDS only properties
                    map['region'] = $('#sdsRegion').val();
                    map['authority'] = $('#authority').val();
                    map['category'] = $('#category').val();
                    map['generalisation'] = $('#generalisation').val();
                    map['sdsType'] = $('#sdsType').val();
                }
            }
            console.log("The map: ",map);
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
                contentType: !isFileUpload,
                data: data,
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

</script>
    <r:require modules="application, fileupload"/>
</head>

<body class="upload">
<div id="content" class="container">
    <header id="page-header">
        <div id="breadcrumb" >
            <ol class="breadcrumb">
                <li><a href="${request.contextPath}/public/speciesLists">Species lists</a> <span class="divider"><i class="fa fa-arrow-right"></i></span></li>
                <li class="current">Upload a list</li>
            </ol>
        </div>
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

            <p>A species list should consist of a list of scientific or common names and some optional associated properties. Provide input consisting of a header line and some rows of data and we will attempt to map your list to taxon names known to the ALA system. A species list can be uploaded either as a CSV file, or as copy and pasted text.</p>

            <div id="initialPaste">
                <h3>Option 1: Select a CSV file to upload here</h3>
                Please note that the file upload feature requires a modern browser (such as Chrome, Firefox, or Internet
                Explorer 10)

                <g:uploadForm name="csvUploadForm" id="csvUploadForm" action="parseData">
                    <div class="fileupload fileupload-new pull-left" data-provides="fileupload">
                        <div class="input-append">
                            <div class="uneditable-input span3">
                                <i class="icon-file fileupload-exists"></i>
                                <span class="fileupload-preview"></span>
                            </div>
                            <span class="btn btn-default btn-file">
                                <span class="fileupload-new">Select file</span>
                                <span class="fileupload-exists">Change</span>
                                <input type="file" name="csvFile" id="csvFileUpload"/>
                            </span>
                            <a href="#" class="btn fileupload-exists" data-dismiss="fileupload">Remove</a>
                        </div>
                    </div>
                </g:uploadForm>

                <div style="clear: both"></div>

                <g:submitButton id="checkData2" class="actionButton btn" name="checkData" value="Check Data"
                                onclick="javascript:parseColumns();"/>

                <h3>Option 2: Paste your species list here</h3>
                <p>To paste your data, click the rectangle below, and type <strong>control-V (Windows)</strong>
                    or <strong>command-V (Macintosh)</strong>.
                </p>

                <g:textArea
                        id="copyPasteData"
                        name="copyPasteData" rows="10" cols="120" style="width:100%;"
                        onkeyup="javascript:window.setTimeout('parseColumns()', 500, true);"></g:textArea>

                <g:submitButton id="checkData" class="actionButton btn" name="checkData" value="Check Data"
                                onclick="javascript:parseColumns();"/>
                <p id="processingInfo"></p>

            </div>

            <div id="recognisedData" tabindex="-1"></div>

            <!-- Moved the upload div to here so that the values can be remembered to support a reload of the species list-->

            <div id="uploadDiv">
                <h2>3. Upload Species List</h2>
                <p>Please supply a title for your list, and indicate the type of list you are uploading from the options provided.<br/>
                You can optionally supply a description, an external URL as a reference to the list and a geospatial bounds for the list (in WKT format).
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
                            <td><label for="listTypeId"><g:message code="upload.listtype.label" default="List Type*"/></label></td>
                            <td>
                                <select name="listTypeId" id="listTypeId">
                                    <option value="">-- select a type --</option>
                                    <g:each in="${au.org.ala.specieslist.ListType.values()}" var="type"><option value="${type.name()}" ${(list?.listType == type) ? 'selected="selected"':''}>${type.displayValue}</option></g:each>
                                </select>
                            </td>

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
                        </g:if>
                        <tr class="SDSOnly">
                            <td><label>Region</label></td>
                            <td>
                                <g:textField name="sdsRegion" style="width:99%" value="${list?.region}"/>
                            </td>
                        </tr>
                        <tr class="SDSOnly">
                            <td><label>Authority</label></td>
                            <td>
                                <g:textField name="authority" style="width:99%" value="${list?.authority}"/>
                            </td>
                        </tr>
                        <tr class="SDSOnly">
                            <td><label>Category</label></td>
                            <td>
                                <g:textField name="category" style="width:99%" value="${list?.category}"/>
                            </td>
                        </tr>
                        <tr class="SDSOnly">
                            <td><label>Generalisation</label></td>
                            <td>
                                <g:textField name="generalisation" style="width:99%" value="${list?.generalisation}"/>
                            </td>
                        </tr>
                        <tr class="SDSOnly">
                            <td><label>SDS Type</label></td>
                            <td>
                                <g:select from="['CONSERVATION', 'BIOSECURITY']" name="sdsType" style="width:99%" value="${list?.sdsType}" />
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label for="listDesc"><g:message code="upload.listdesc.label" default="Description"/></label>
                            </td>
                            <td>
                                <g:textArea cols="100" class="input-xxlarge" rows="5" name="listDesc">${list?.description}</g:textArea>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label for="listURL"><g:message code="upload.listlink.label" default="URL"/></label>
                            </td>
                            <td>
                                <g:textField name="listURL" class="input-xxlarge">${list?.url}</g:textField>
                            </td>

                        </tr>
                        <tr>
                            <td>
                                <label for="listWkt"><g:message code="upload.listWkt.label" default="Spatial bounds for data (WKT)"/></label>
                            </td>
                            <td>
                                <g:textArea cols="100" rows="5" class="input-xxlarge" name="listWkt">${list?.wkt}</g:textArea>
                            </td>

                        </tr>
                        </tbody>
                    </table>
                    <input id="uploadButton" class="datasetName actionButton btn btn-primary" type="button" value="Upload"
                           onclick="javascript:uploadSpeciesList();"/>
                </div>
            </div>

            <div id="uploadFeedback" style="clear:right;display:none;" class="alert alert-error">
                <button type="button" class="close" onclick="$(this).parent().hide()">×</button>
                <div></div>
            </div>
            <div id="uploadProgressBar">
            </div>
        </div>
    </div>
    <div id="statusMsgDiv">
        <div class="well">
            <h3><img src='${resource(dir:'images',file:'spinner.gif')}' id='spinner'/>&nbsp;&nbsp;<span>Uploading your list...</span></h3>
            <p>This will take a few moments depending on the size of the list.</p>
        </div>
    </div>
</div> <!-- content div -->
</body>
</html>