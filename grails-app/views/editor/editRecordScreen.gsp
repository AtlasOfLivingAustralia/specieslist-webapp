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
    <div class="message alert alert-danger"><b>${message(code:'generic.lists.button.alert.label', defauilt:'Alert')}:</b> ${flash.message}</div>
</g:if>
<g:if test="${record}">
    <div class="hide">
        <dl class="dl-horizontal">
            <dt>${message(code:'public.lists.items.header01', default:'Supplied Name')}</dt><dd>${record.rawScientificName}</dd>
            <dt>${message(code:'public.lists.items.header02', default:'Scientific Name (matched)')}</dt><dd>${record.matchedName}</dd>
        </dl>
    </div>
    <form class="editRecordForm" id="editForm_${record.id}" data-id="${record.id?:record.mylist?.id}">
        <input type="hidden" name="id" value="${record.id?:record.mylist?.id}"/>
        <table id="editRecordTable" class="table table-bordered table-condensed">
            <thead>
            <tr>
                <th style="width: 20%">${message(code:'public.lists.addspecies.table.col01', default:'Field')}</th>
                <th style="width: 60%">${message(code:'public.lists.addspecies.table.col02', default:'Value')}</th>
                <th style="width: 20%">${message(code:'public.lists.addspecies.table.col03', default:'Vocab')}</th>
            </tr>
            </thead>
            <tbody>
            <tr class=''>
                <td class="dataField">${message(code:'public.lists.items.header01', default:'Supplied Name')}</td>
                <td class='dataValue'><input name="rawScientificName" id="rawScientificName" type="text" class="form-control" value="${record?.rawScientificName?.trim()}"/></td>
                <td>&nbsp;</td>
            </tr>
            %{--<g:each in="${record.kvpValues}" var="field">--}%
            <g:each in="${KVPKeys}" var="key" status="i">
                <g:set var="hasVocab" value="${keyVocabs}"/>
                <g:set var="fieldSet" value="${record.kvpValues.findAll { it.key == key } as List}"/>
                <g:set var="field" value="${fieldSet[0]}"/>
                <tr class='${field}'>
                    <td class="dataField">
                        ${key}
                        <input type="hidden" name="itemOrder_${key}" value="${kvpOrder[i]?:0}"/>
                    </td>
                    <td class='dataValue'>
                        <g:if test="${field?.value?.size() > 50}">
                            <textarea name="${key}" class="form-control" rows="2" ${(hasVocab)?"readonly='readonly'":""}>${field?.value}</textarea>
                        </g:if>
                        <g:else>
                            <input type="text" name="${key}" class="form-control" value="${field?.value}" ${(hasVocab)?"readonly='readonly'":""}/>
                        </g:else>
                    </td>
                    <td>
                        <g:if test="${hasVocab}">%{-- select onChange detected in JS block below --}%
                            <select name="vocab_${key}" class="form-control vocabDropDown" data-key="${key}">
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

<asset:script type="text/javascript">

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
</asset:script>
</body>
</html>