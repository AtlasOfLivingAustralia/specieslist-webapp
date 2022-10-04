<%--
  Created by IntelliJ IDEA.
  User: dos009
  Date: 21/03/13
  Time: 9:01 AM
  To change this template use File | Settings | File Templates.
--%>
<%@page defaultCodec="none" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>${message(code:'permissions.lists.edit.title', default:'Edit List Permissions')}</title>
</head>
<body>
<g:if test="${flash.message}">
    <div class="message alert alert-info"><b>${message(code:'generic.lists.button.alert.label', default:'Alert')}:</b> ${flash.message}</div>
</g:if>
<div>${message(code:'permissions.lists.text', default:'This form allows the list owner (or administrator) to add/remove users as <strong>editors</strong> of this list. This in turn, allows those <strong>editors</strong> to edit and delete entries in this list.')}</div>
<br>
<div>${message(code:'permissions.lists.warning', default:'Please note that if the entered email does not match an existing ALA user, it will not be added to list permissions on Save.')}</div>
<div>&nbsp;</div>
<form id="userEditForm" class="form-horizontal">
    <div class="form-group">
        <label class="control-label col-md-4" for="search">${message(code:'permissions.lists.email.label', default:'User\'s email address')} </label>
        <div class="col-md-6">
            <div class="input-group">
                <input id="search" type="text" class="form-control" data-provide="typeahead" placeholder="${message(code:'permissions.lists.email.enter', default:'Enter user\'s email address')}" autocomplete="off">
                <div class="input-group-btn">
                    <button type="submit" class="btn btn-default">${message(code:'permissions.lists.button01', default:'Add')}</button>
                </div>
            </div>
        </div>
    </div>
</form>
<table id="userTable" class="table table-bordered" style="margin-top: 10px;">
    <thead>
        <tr><th>${message(code:'permissions.lists.tableheader.email', default:'Email')}</th><th>${message(code:'permissions.lists.tableheader.role', default:'Role')}</th><th>${message(code:'permissions.lists.tableheader.action', default:'Action')}</th></tr>
    </thead>
    <tbody>
        <tr>
            <td>${speciesList.username}</td>
            <td>${message(code:'permissions.lists.tablecolumn.owner', default:'owner')}</td>
            <td></td>
        </tr>
        <g:set var="removeLink"><a href='#' onclick='removeRow(this)'>${message('permissions.lists.action.text', default:'remove')}</a></g:set>
        %{--<g:each in="${speciesList.editors}" var="editor">--}%
        <g:each in="${editorsWithDetails}" var="editor">
            <tr class='editor'>
                <td class='userEmail' data-useremail='${editor.userName}'>${editor.userName}</td>
                <td>${message(code:'permissions.lists.tablecolumn.editor', default:'editor')}</td>
                <td>${removeLink}</td>
            </tr>
        </g:each>
    </tbody>
</table>
<asset:script type="text/javascript" asset-defer="">

    /**
    * Delete a row from the table
    */
    function removeRow(link) {
        $(link).parent().parent().remove();
    }

    $(document).ready(function() {

        /**
         * Add button for user id input - adds ID to the table
         */
        $("#userEditForm").submit(function(el) {
            el.preventDefault();
            var userEmail = $("#search").val().trim();
            $("#userTable tbody").append("<tr class='editor'><td class='userEmail' data-userEmail='"+userEmail+"'>"+userEmail+"</td><td>Pending Editor</td><td>${removeLink}</td></tr>");
            $("#search").val("");
            return true;
        });

        /**
         * Save changes button on modal div (in calling page)
         */
        $("#saveEditors").click(function(el) {
            el.preventDefault();
            var editors = [];
            $("#userTable tr.editor").each(function() {
                editors.push($(this).find("td.userEmail").data('useremail'));
            });
            //console.log("editors", editors);
            var params = {
                id: "${params.id}",
                editors: editors
            };

            $.post("${createLink(action: 'updateEditors')}", params, function(data, textStatus, jqXHR) {
                //console.log("data", data, "textStatus", textStatus,"jqXHR", jqXHR);
                alert('Editors were successfully saved');
                $('#modal').modal('hide');
                window.location.reload(true);
            }).error(function(jqXHR, textStatus, error) {
                alert("An error occurred: " + error + " - " + jqXHR.responseText);
            });
        });
    });
</asset:script>
</body>
</html>