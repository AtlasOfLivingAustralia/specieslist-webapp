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
            .fwtable {
                overflow-y: hidden; overflow-x: auto; width: 100%;
            }
        </style>

        <h1></h1>
        <g:if test="${error}">
            <div class="message alert alert-danger">${error}</div>
        </g:if>
        <g:else>
            <div id="recognisedDataDiv">
                <h2>${message(code:'upload.lists.checkdata.header', default:'2. Check our initial interpretation')}</h2>
                <g:if test="${!nameFound}">
                    <div class="alert alert-danger">
                        <strong>${message(code:'upload.lists.checkdata.map.warning', default:'Warning')}</strong> ${message(code:'upload.lists.checkdata.map.warning.message01', default:'No species name column could be found, please add one. Any of the following column names will do:')}
                        <g:each in="${nameColumns}" var="nc" status="i">
                            <g:if test="${i == nameColumns.size() - 1}">, or </g:if><g:elseif test="${i != 0}">, </g:elseif>“${nc}”</g:each>
                    </div>
                </g:if>

                <p>${message(code:'upload.lists.checkdata.text01', default:'Adjust headings that have been incorrectly matched using the text boxes.')}</p>

                <div class="fwtable well">
                    <table id="initialParse" class="table table-striped table-bordered">
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
                    ${message(code:'upload.lists.checkdata.text02', default:'We have detected species properties within the list.')}<br/>
                    ${message(code:'upload.lists.checkdata.text03', default:'It is possible to map your properties to a controlled vocabulary.')}<br/>
                    ${message(code:'upload.lists.checkdata.text04', default:'This step is <strong>optional</strong>.')}

                    <input id="viewVocabButton" class="datasetName actionButton btn btn-default" type="button"
                           value="${message(code:'upload.lists.checkdata.map.button', default:'Click here to map...')}" onclick="javascript:viewVocab();"/>
                </p>

                <div class="allVocabs well" id="listvocab">

                    <div class="pull-right"><button class="btn btn-default" onclick="javascript:hideVocab();">${message(code:'upload.lists.checkdata.map.text03', default:'Close')}</button></div>

                    <g:each in="${listProperties.keySet()}" var="key">
                        <div class="vocabDiv">
                            <h3 class="vocabHeader" for="Head_${key}">${key}</h3>

                            <div class="fhtable">
                                <table class="vocabularyTable table table-condensed" id="Voc_${key}" for="Head_${key}">
                                    <thead>
                                    <th class="parse">${message(code:'upload.lists.checkdata.map.text01', default:'Value')}</th>
                                    <th class="parse">${message(code:'upload.lists.checkdata.map.text02', default:'Maps To')}</th>
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

                    <div class="pull-right"><button class="btn btn-default" onclick="javascript:hideVocab();">${message(code:'upload.lists.checkdata.map.text03', default:'Close')}</button></div>
                </div><!-- #listvocab -->
            </g:if>
        </g:else>
    </div>
</div>