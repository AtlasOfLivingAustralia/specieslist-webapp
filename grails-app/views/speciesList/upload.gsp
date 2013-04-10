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
        $('#uploadDiv').hide();
        $('#statusMsgDiv').hide();
        $('#uploadmsg').hide();
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
    $(document).ready(function(){ init(); });
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
                <h1>Upload a list</h1>
            </hgroup>
        </div><!--inner-->
    </header>
    <div class="inner">
        <div class="message alert alert-info" id="uploadmsg" style="clear:right;">${flash.message}</div>
        <div id="section" class="col-wide">
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