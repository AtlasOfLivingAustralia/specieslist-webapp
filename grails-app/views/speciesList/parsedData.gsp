<%--
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
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<div id="parsedData" xmlns="http://www.w3.org/1999/html">
    <div id="tabulatedData">
        <style type="text/css">
            table {
                border-collapse: collapse;
                margin-bottom: 0px;
            }

            /*th { font-size: 12px; border-collapse: collapse; border: 1px solid #000000; padding:2px; background-color: #000000; color: #ffffff;}*/
            /*td { font-size: 11px; border-collapse: collapse; border: 1px solid #000000; padding: 2px;}*/
        </style>

        <h1></h1>
        <g:if test="${error}">
            <div class="message alert alert-error">${error}</div>
        </g:if>
        <g:else>
            <div id="recognisedDataDiv">
                <h2>2. Check our initial interpretation</h2>

                <p>Adjust headings that have been incorrectly matched using the text boxes.

                <div class="fwtable">
                    <table id="initialParse">
                        <thead>
                        <g:if test="${columnHeaders}">
                            <g:each in="${columnHeaders}" var="hdr">
                                <th class="parse">
                                    <input id="Head_${hdr}" class="columnHeaderInput" type="text" value="${hdr}"
                                           style="${hdr.startsWith("UNKNOWN") ? 'background-color: #E9AB17;' : ''}"
                                           onkeyup="javascript:window.setTimeout('updateH3(this.id)', 500, true);"/>
                                </th>
                            </g:each>
                        </g:if>
                        </thead>
                        <tbody>
                        <g:each in="${dataRows}" var="row">
                            <tr>
                                <g:each in="${row}" var="value">
                                    <td class="parse">${value}</td>
                                </g:each>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div><!-- #recognisedDataDiv -->
            <g:if test="${listProperties}">
                <p>
                    We have detected species properties within the list. It is possible to map your properties to a controlled vocabulary.
                    <input id="viewVocabButton" class="datasetName actionButton btn" type="button"
                           value="Click here to map..." onclick="javascript:viewVocab();"/>
                </p>

                <div class="allVocabs" id="listvocab">
                    <g:each in="${listProperties.keySet()}" var="key">
                        <div class="vocabDiv">
                            <h3 class="vocabHeader" for="Head_${key}">${key}</h3>

                            <div class="fhtable">
                                <table class="vocabularyTable" id="Voc_${key}" for="Head_${key}">
                                    <thead>
                                    <th class="parse">Value</th>
                                    <th class="parse">Maps To</th>
                                    </thead>
                                    <tbody class="vocabBody">
                                    <g:each in="${listProperties.get(key)}" var="rawKeyVal">
                                        <tr>
                                            <td class="parse">${rawKeyVal}</td>
                                            <td class="parse"><input class="vocabHeader_${key}" type="text" value=""/></td>
                                        </tr>
                                    </g:each>
                                    </tbody>
                                </table>
                            </div> <!-- fhtable -->
                        </div><!-- #vocabDiv -->
                    </g:each>
                </div><!-- #listvocab -->
            </g:if>

            %{--<div id="recognisedData"></div>--}%

            %{--</div>--}%
            %{--<div id="uploadDiv">--}%
                %{--<h2>3. Upload Species List</h2>--}%
                %{--Please supply a title for your list.  You can optionally supply a description, an external URL as a reference to the list and a geospatial bounds for the list (in WKT format).--}%
                %{--<div id="processSampleUpload">--}%
                    %{--<p style="padding-bottom:0px;">--}%
                    %{--<table>--}%
                    %{--<tbody>--}%
                    %{--<tr>--}%
                       %{--<td>--}%
                           %{--<label for="listTitle"><g:message code="upload.listname.label" default="Title*"/></label>--}%
                                                    %{--</td>--}%
                        %{--<td>--}%
                            %{--<g:textField name="listTitle" style="width:99%"/>--}%
                        %{--</td>--}%
                        %{--</tr>--}%
                        %{--<tr>--}%
                            %{--<td><label for="listTypeId"><g:message code="upload.listtype.label" default="List Type*"/></label></td>--}%
                            %{--<td>--}%
                                %{--<select name="listTypeId" id="listTypeId">--}%
                                    %{--<option value="">-- select a type --</option>--}%
                                    %{--<g:each in="${listTypes}" var="listType">--}%
                                        %{--<option value="${listType}">${listType.getDisplayValue()}</option>--}%
                                    %{--</g:each>--}%
                                %{--</select>--}%
                            %{--</td>--}%

                        %{--</tr>--}%
                        %{--<g:if test="${request.isUserInRole("ROLE_ADMIN")}">--}%
                            %{--<tr>--}%
                                %{--<td><label for="isBIE"><g:message code= "speciesList.isBIE.label" default= "Included in BIE"/></label> </td>--}%
                                %{--<td><g:checkBox name="isBIE" id="isBIE"/></td>--}%
                            %{--</tr>--}%
                            %{--<tr>--}%
                                %{--<td><label for="isSDS"><g:message code= "speciesList.isSDS.label" default= "Part of the SDS"/></label> </td>--}%
                                %{--<td><g:checkBox name="isSDS" id="isSDS"/></td>--}%
                            %{--</tr>--}%
                        %{--</g:if>--}%
                        %{--<tr>--}%
                            %{--<td>--}%
                                %{--<label for="listDesc"><g:message code="upload.listdesc.label" default="Description"/></label>--}%
                            %{--</td>--}%
                            %{--<td>--}%
                                %{--<g:textArea cols="100" rows="5" name="listDesc"/>--}%
                            %{--</td>--}%

                        %{--</tr>--}%
                        %{--<tr>--}%
                            %{--<td>--}%
                                %{--<label for="listURL"><g:message code="upload.listlink.label" default="URL"/></label>--}%
                            %{--</td>--}%
                            %{--<td>--}%
                                %{--<g:textField name="listURL" style="width:99%"/>--}%
                            %{--</td>--}%

                        %{--</tr>--}%
                        %{--<tr>--}%
                            %{--<td>--}%
                                %{--<label for="listWkt"><g:message code="upload.listWkt.label" default="Spatial bounds for data (WKT)"/></label>--}%
                            %{--</td>--}%
                            %{--<td>--}%
                                %{--<g:textArea cols="100" rows="5" name="listWkt"/>--}%
                            %{--</td>--}%

                        %{--</tr>--}%
                        %{--</tbody>--}%
                        %{--</table>--}%
                    %{--<label for="speciesListName" class="datasetName"><strong>Your species list name</strong></label>--}%
                    %{--<input id="speciesListName" class="datasetName" name="datasetName" type="text" value="My test species list" style="width:350px; margin-bottom:5px;"/>--}%
                        %{--<input id="uploadButton" class="datasetName actionButton btn" type="button" value="Upload"--}%
                               %{--onclick="javascript:uploadSpeciesList();"/>--}%
                    %{--</p>--}%
                %{--</div>--}%
            %{--</div>--}%
        </g:else>
    </div>
</div>