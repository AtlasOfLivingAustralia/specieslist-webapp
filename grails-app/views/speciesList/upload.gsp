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
<meta name="layout" content="main"/>
<title>Upload a list | Species lists | Atlas of Living Australia</title>
<script type="text/javascript">
    function init(){
        //hide('manualMapping')
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
            //$("#SDSOnly").fadeIn(200);
            rows.filter('.SDSOnly').show();


        } else {
            //$("#SDSOnly").fadeOut(200);
            rows.filter('.SDSOnly').hide();
        }
    }

    function parseColumns(){
        if($('#copyPasteData').val().trim() == ""){
            reset();
        } else {
            //console.log($('#copyPasteData').val())
            $.ajaxSetup({
                scriptCharset: "utf-8",
                contentType: "text/html; charset=utf-8"
            });
            var url = "${createLink(controller:'speciesList', action:'parseData')}";
            $.ajax({
                type: "POST",
                url: url,
                data: $('#copyPasteData').val(),//.val().substring(0,$('#copyPasteData').val().indexOf('\n')),
                success: function(data){
                    $('#recognisedDataDiv').show();
                    $('#recognisedData').html(data);
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
//            alert("Here... " + checked)
        if (checked)
        {
            hide('manualMapping');
        }
        else{
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
            var map =getVocabularies();
            map['headers'] = getColumnHeaders();
            map['speciesListName'] = $('#listTitle').val();
            map['description'] = $('#listDesc').val();
            map['listUrl'] = $('#listURL').val();
            map['listWkt'] = $('#listWkt').val();
            map['rawData']  =$('#copyPasteData').val();
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
            //console.log($.param(map))
            console.log("The map: ",map)
            $('#recognisedDataDiv').hide();
            $('#uploadDiv').hide();
            $('#statusMsgDiv').show();
            var url = "${createLink(controller:'speciesList', action:'uploadList')}";
            $.ajax({
                type: "POST",
                url: url,
                dataType:"json",
                data: JSON.stringify(map),//.val().substring(0,$('#copyPasteData').val().indexOf('\n')),
                success: function(response){
                    //console.log(response, response.url)
                    if(response.url != null)
                        window.location.href = response.url;

                },
                error: function(xhr, textStatus, errorThrown) {
                    //console.log('Error!  Status = ' ,xhr.status, textStatus, errorThrown, xhr.responseText);
                    reportError("Error: " +errorThrown);
                }

            });
        }
//            else{
//                $('#listTitle').focus()
//                alert("You must supply a species list title")
//            }
        //dataType: "json",

//            $.post(url, $.param(map),
//                    function(data){
//                        //alert('Value returned from service: '  + data.uid);
//                        console.log("DATA" ,data)
//                    } );


    }

    function getVocabularies(){
        var potentialVocabH3s = $('div.vocabDiv');
        var vocabMap = {};
        $.each(potentialVocabH3s, function(index,vdiv){
            var value = "";
            var h3value = "vocab_"+$(vdiv).find('h3:first').text();

            //console.log("tbody",$("table[for='"+header3.for+"']"))
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
        //console.log("vocabMap: ",vocabMap)
        return vocabMap

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
        //console.log("H3: " , column)
        //$("h3[for='"+column.id+"']").html(column.value)
        var columnHeaderInputs = $('input.columnHeaderInput');
        var test =""
        $.each(columnHeaderInputs, function(index, input){
            //console.log("updateTables", index, input.id);

            $("h3[for='"+input.id+"']").html($(input).val());
            //console.log("h3 for",$("h3[for='"+input.id+"']"));
        })
    }

    //        //setup the page
    $(document).ready(function(){

        init();

        $("#isSDS").change(function(){
            refreshSDSRows();
      });

    });

</script>
</head>

<body class="species">
<div id="content">
    <header id="page-header">
        <div class="inner">
            <div class="inner row-fluid">
                <div id="breadcrumb" class="span12">
                    <ol class="breadcrumb">
                        %{--<li><a href="http://www.ala.org.au">Home</a> <span class=" icon icon-arrow-right"></span></li>--}%
                        <li><a href="${request.contextPath}/public/speciesLists">Species lists</a> <span class=" icon icon-arrow-right"></span></li>
                        <li class="last">Upload a list</li>
                    </ol>
                </div>
            </div>
            <hgroup>
                <g:if test="${list}">
                    <h1><g:message code="upload.heading.hasList" default="Upload a list"/></h1>
                </g:if>
                <g:else>
                    <h1><g:message code="upload.heading" default="Upload a list"/></h1>
                </g:else>
            </hgroup>
        </div><!--inner-->
    </header>
    <div class="inner">
        <div class="message alert alert-info" id="uploadmsg" style="clear:right;">${flash.message}</div>
        <div id="section" class="col-wide">

            <g:if test="${resourceUid}">
                <div class="message alert alert-info"><g:message code="upload.instructions.hasList" default="Upload a list"/></div>
            </g:if>

            A species list can consist of a list of scientific or common names and optionally associated properties. When
            a CSV list is supplied we will attempt to use the first line to determine mappings.

            <div id="initialPaste">
                <h2>1. Paste your species list here</h2>
                <p>To paste your data, click the rectangle below, and type <strong>control-V (Windows)</strong>
                    or <strong>command-V (Macintosh)</strong>.
                </p>

                <g:textArea
                        id="copyPasteData"
                        name="copyPasteData" rows="15" cols="120" style="width:100%;"
                        onkeyup="javascript:window.setTimeout('parseColumns()', 500, true);"></g:textArea>
                <g:submitButton id="checkData" class="actionButton btn" name="checkData" value="Check Data"
                                onclick="javascript:parseColumns();"/>
                <p id="processingInfo"></p>

            </div>

            <div id="recognisedData"></div>

            <!-- Moved the upload div to here so that the values can be remembered to support a reload of the species list-->

            <div id="uploadDiv">
                <h2>3. Upload Species List</h2>
                Please supply a title for your list.  You can optionally supply a description, an external URL as a reference to the list and a geospatial bounds for the list (in WKT format).
                <div id="processSampleUpload">
                    %{--<p style="padding-bottom:0px;">--}%
                    <table class="listDetailTable">
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
                                <td><label for="isBIE"><g:message code= "speciesList.isBIE.label" default= "Included in BIE"/></label> </td>
                                <td><g:checkBox name="isBIE" id="isBIE" checked="${list?.isBIE}"/></td>
                            </tr>
                            <tr>
                                <td><label for="isSDS"><g:message code= "speciesList.isSDS.label" default= "Part of the SDS"/></label> </td>
                                <td><g:checkBox name="isSDS" id="isSDS" checked="${list?.isSDS}"/></td>
                            </tr>
                        </g:if>
                         <tr class="SDSOnly" >
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
                                <g:textField name="sdsType" style="width:99%" value="${list?.sdsType}"/>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label for="listDesc"><g:message code="upload.listdesc.label" default="Description"/></label>
                            </td>
                            <td>
                                <g:textArea cols="100" rows="5" name="listDesc">${list?.description}</g:textArea>

                            </td>

                        </tr>
                        <tr>
                            <td>
                                <label for="listURL"><g:message code="upload.listlink.label" default="URL"/></label>
                            </td>
                            <td>
                                <g:textField name="listURL" style="width:99%">${list?.url}</g:textField>
                            </td>

                        </tr>
                        <tr>
                            <td>
                                <label for="listWkt"><g:message code="upload.listWkt.label" default="Spatial bounds for data (WKT)"/></label>
                            </td>
                            <td>
                                <g:textArea cols="100" rows="5" name="listWkt">${list?.wkt}</g:textArea>
                            </td>

                        </tr>
                        </tbody>
                    </table>
                    %{--<label for="speciesListName" class="datasetName"><strong>Your species list name</strong></label>--}%
                    %{--<input id="speciesListName" class="datasetName" name="datasetName" type="text" value="My test species list" style="width:350px; margin-bottom:5px;"/>--}%
                    <input id="uploadButton" class="datasetName actionButton btn" type="button" value="Upload"
                           onclick="javascript:uploadSpeciesList();"/>
                    %{--</p>--}%
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
        <h3>Uploading your list...</h3>
    </div>
</div> <!-- content div -->
</body>
</html>