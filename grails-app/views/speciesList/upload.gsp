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
    <meta name="layout" content="ala2"/>
    <title>BIE Species List Upload</title>
    <script type="text/javascript">
        function init(){
            //hide('manualMapping')
            reset();
        }

        function reset(){
            $('#recognisedDataDiv').hide();
            $('#uploadDiv').hide()
            $('#statusMsgDiv').hide()
        }

        function parseColumns(){
            if($('#copyPasteData').val().trim() == ""){
                reset();
            } else {
                console.log($('#copyPasteData').val())
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
                        $('#recognisedData').html(data)
                        $('#uploadDiv').show()
                        $('#listvocab').hide()
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

        function uploadSpeciesList(){
            if($('#listTitle').val().length > 0){
            var map =getVocabularies();
            map['headers'] = getColumnHeaders();
            map['speciesListName'] = $('#listTitle').val();
            map['description'] = $('#listDesc').val();
            map['listUrl'] = $('#listURL').val()
            map['rawData']  =$('#copyPasteData').val()
            //console.log($.param(map))
            //console.log("The map: ",map)
            $('#recognisedDataDiv').hide();
            $('#uploadDiv').hide()
            $('#statusMsgDiv').show()
            var url = "${createLink(controller:'speciesList', action:'uploadList')}";
            $.ajax({
                type: "POST",
                url: url,
                dataType:"json",
                data: JSON.stringify(map),//.val().substring(0,$('#copyPasteData').val().indexOf('\n')),
                success: function(response){
                    console.log(response, response.url)

                    window.location.href = response.url;
                },
                error: function(xhr, textStatus, errorThrown) {
                    console.log('Error!  Status = ' ,xhr.status, textStatus, errorThrown);
                }

        });
            }
            else{
                $('#listTitle').focus()
                alert("You must supply a species list title")
            }
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
                var value = ""
                var h3value = "vocab_"+$(vdiv).find('h3:first').text()

                //console.log("tbody",$("table[for='"+header3.for+"']"))
                $(vdiv).find('table').find('tbody').find('tr').each(function(index2,vrow){

                    if(value.length>0)
                        value = value +","

                    var vkey = $(vrow).children().eq(0).text()

                    var vvalue= $(vrow).children().eq(1).children().eq(0).val()
                    if(vvalue.length>0)
                        value = value + vkey +":"+vvalue
                })

                vocabMap[h3value] = value
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

                $("h3[for='"+input.id+"']").html($(input).val()) ;
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
            <nav id="breadcrumb">
                <ol>
                    <li><a href="http://www.ala.org.au">Home</a></li>
                    <li><a href="${request.contextPath}/public/speciesLists">Species Lists</a></li>
                    <li class="last">Upload New List</li>
                </ol>
            </nav>
            <hgroup>
            <h1>Species List Upload</h1>
            </hgroup>
        </div><!--inner-->
    </header>
    <div class="inner">
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
                        name="copyPasteData" rows="15" cols="120"
                        onkeyup="javascript:window.setTimeout('parseColumns()', 500, true);"></g:textArea>
                <g:submitButton id="checkData" class="actionButton" name="checkData" value="Check Data"
                                onclick="javascript:parseColumns();"/>
                <p id="processingInfo"></p>

            </div>
            <div id="recognisedDataDiv">
                <h2>2. Check our initial interpretation</h2>

                <p>Adjust headings that have been incorrectly matched using the text boxes.


                <div id="recognisedData"></div>

            </div>
            <div id="uploadDiv">
                <h2>3. Upload Species List</h2>
                Please supply a title for your list.  You can optionally supply a description and external URL as a reference to the list.
                <div id="processSampleUpload">
                    <p style="padding-bottom:0px;">
                        <table>
                            <tbody>
                                <tr>
                                    <td>
                                        <label for="listTitle"><g:message code="upload.listname.label" default="Title*" /></label>
                                    </td>
                                    <td>
                                        <g:textField name="listTitle" style="width:99%"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <label for="listDesc"><g:message code="upload.listdesc.label" default="Description" /></label>
                                    </td>
                                    <td>
                                        <g:textArea cols="100" rows="5" name="listDesc" />
                                    </td>

                                </tr>
                                <tr>
                                    <td>
                                        <label for="listURL"><g:message code="upload.listlink.label" default="URL" /></label>
                                    </td>
                                    <td>
                                        <g:textField name="listURL" style="width:99%" />
                                    </td>

                                </tr>


                            </tbody>
                        </table>
                        %{--<label for="speciesListName" class="datasetName"><strong>Your species list name</strong></label>--}%
                        %{--<input id="speciesListName" class="datasetName" name="datasetName" type="text" value="My test species list" style="width:350px; margin-bottom:5px;"/>--}%
                        <input id="uploadButton" class="datasetName" type="button" value="Upload" onclick="javascript:uploadSpeciesList();"/>
                    <div id="uploadFeedback" style="clear:right;">
                    </div>
                    <div id="uploadProgressBar">
                    </div>
                </p>
                </div>
            </div>
            <div id="statusMsgDiv">
                <h3>Uploading your list...</h3>
            </div>

            %{--<g:uploadForm action="submitList">--}%
                %{--<table>--}%
                    %{--<tbody>--}%
                        %{--<tr>--}%
                            %{--<td>Species List Title:</td>--}%
                            %{--<td>--}%
                                %{--<g:textField name="speciesListTitle" value="" />--}%
                            %{--</td>--}%
                        %{--</tr>--}%
                        %{--<tr>--}%
                            %{--<td>File to upload:</td>--}%
                            %{--<td><input type="file" name="species_list" /> </td>--}%
                        %{--</tr>--}%
                        %{--<tr>--}%
                             %{--<td>Header Row indicates mapping:</td>--}%
                             %{--<td>--}%
                                 %{--<g:checkBox name="rowIndicatesMapping" value="true" onclick="updateCustom(this.checked);" />--}%
                             %{--</td>--}%
                        %{--</tr>--}%
                        %{--<input type="submit" value="Upload List" />--}%
                    %{--</tbody>--}%
                %{--</table>--}%
                %{--<div id="manualMapping" visibility="hidden">--}%
                    %{--<table>--}%
                        %{--<tr>--}%
                            %{--<td>Columns</td>--}%
                            %{--<td>--}%
                                %{--<g:textField name="column0" value="scientific name" default="scientific name" />--}%
                            %{--</td>--}%
                            %{--<td>--}%
                                %{----}%
                            %{--</td>--}%
                        %{--</tr>--}%
                        %{--<tr><td/><td>--}%
                            %{--<g:textField name="column1" value=""  />--}%
                        %{--</td>--}%
                            %{--<td><g:textField name="vocab1"/></td>--}%
                        %{--</tr>--}%
                    %{--</table>--}%
                %{--</div>--}%
            %{--</g:uploadForm>--}%
        </div>
    </div>
</div> <!-- content div -->
</body>
</html>