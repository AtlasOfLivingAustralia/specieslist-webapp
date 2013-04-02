<%--
  Created by IntelliJ IDEA.
  User: dos009
  Date: 21/03/13
  Time: 9:01 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page import="au.org.ala.specieslist.SpeciesListKVP" contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>Edit Record Values</title>
</head>
<body>
<g:if test="${flash.message}">
    <div class="message alert alert-error"><b>Alert:</b> ${flash.message}</div>
</g:if>
<g:if test="${record}">
    <div class="hide">
        <dl class="dl-horizontal">
            <dt>Supplied Name</dt><dd>${record.rawScientificName}</dd>
            <dt>Scientific Name (matched)</dt><dd>${record.matchedName}</dd>
        </dl>
    </div>
    <form class="editRecordForm" id="editForm_${record.id}" data-id="${record.id?:record.list?.id}">
        <input type="hidden" name="id" value="${record.id?:record.list?.id}"/>
        <table id="editRecordTable" class="table table-bordered table-condensed">
            <thead>
            <tr>
                <th style="width: 20%">Field</th>
                <th style="width: 60%">Value</th>
                <th style="width: 20%">Vocab</th>
            </tr>
            </thead>
            <tbody>
            <tr class=''>
                <td class="dataField">Supplied Name</td>
                <td class='dataValue'><input name="rawScientificName" id="rawScientificName" type="text" class="input-block-level" value="${record?.rawScientificName?.trim()}"/></td>
                <td>&nbsp;</td>
            </tr>
            %{--<g:each in="${record.kvpValues}" var="field">--}%
            <g:each in="${KVPKeys}" var="key">
                <g:set var="hasVocab" value="${keyVocabs}"/>
                <g:set var="fieldSet" value="${record.kvpValues.findAll { it.key == key } as List}"/>
                <g:set var="field" value="${fieldSet[0]}"/>
                <tr class='${field}'>
                    <td class="dataField">${key}</td>
                    <td class='dataValue'>
                        <g:if test="${field?.value?.size() > 50}">
                            <textarea name="${key}" class="input-block-level" rows="2" ${(hasVocab)?"readonly='readonly'":""}>${field?.value}</textarea>
                        </g:if>
                        <g:else>
                            <input type="text" name="${key}" class="input-block-level" value="${field?.value}" ${(hasVocab)?"readonly='readonly'":""}/>
                        </g:else>
                    </td>
                    <td>
                        <g:if test="${hasVocab}">%{-- select onChange detected in JS block below --}%
                            <select name="vocab_${key}" class="input-block-level vocabDropDown" data-key="${key}">
                                <g:each in="${keyVocabs[key]}" var="vocab">
                                    <option ${(field?.vocabValue == vocab) ? "selected='selected'": ""} data-value="${kvpMap[key]?.findAll{it.vocabValue == vocab}[0]?.value}">${vocab}</option>
                                </g:each>
                            </select>
                        </g:if>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </form>
</g:if>

<script type="text/javascript">

    $(document).ready(function() {
        // change raw KVP value if vocab drop down is changed
        $("select.vocabDropDown").change(function() {
            var key = $(this).data("key");
            var thisFormId = $(this).closest("form").attr("id");
            var value = $(this).find(":selected").data("value");
            //console.log("value", value, "input", thisFormId);
            $("#" + thisFormId).find(":input[name='"+key+"']").val(value);
        });
    });
</script>
</body>
</html>