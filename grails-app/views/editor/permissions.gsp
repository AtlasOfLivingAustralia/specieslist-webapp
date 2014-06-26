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
  <title>Edit List Permissions</title>
</head>
<body>
<g:if test="${flash.message}">
    <div class="message alert alert-info"><b>Alert:</b> ${flash.message}</div>
</g:if>
<div>This form allows the list owner (or administrator) to add/remove users as <strong>editors</strong> of this list. This
in turn, allows those <strong>editors</strong> to edit and delete entries in this list.</div>
<div>&nbsp;</div>
<form class="form-inline" id="userEditForm">
    <label class="control-label" for="search">User's email address </label>
    <input id="search" type="text" class="input-xlarge" data-provide="typeahead" placeholder="Enter user's email address" autocomplete="off">
    <button type="submit" class="btn">Add</button>
</form>
<table id="userTable" class="table table-bordered" style="margin-top: 10px;">
    <thead>
        <tr><th>Name</th><th>Email</th><th>Role</th><th>Action</th></tr>
    </thead>
    <tbody>
        <tr>
            <td>${speciesList.firstName} ${speciesList.surname}</td>
            <td>${speciesList.username}</td>
            <td>owner</td>
            <td></td>
        </tr>
        <g:set var="removeLink"><a href='#' onclick='removeRow(this)'>remove</a></g:set>
        <g:each in="${speciesList.editors}" var="editor">
            <tr class='editor'>
                <td>${mapOfUserNamesById[editor]}</td>
                <td class='userId'>${editor}</td>
                <td>editor</td>
                <td>${removeLink}</td>
            </tr>
        </g:each>
    </tbody>
</table>
<script type="text/javascript">

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
        $("#userEditForm").submit(function() {
            var userId = $("#search").val().trim();
            var url = "${g.createLink(controller:'webService', action:'checkEmailExists')}?email=" + userId;
            $.getJSON(url, function(data) {
                if (data && data.userId) {
                    console.log('data', data);
                    $("#userTable tbody").append("<tr class='editor'><td>"+ data.displayName +"</td><td class='userId'>"+data.userName+"</td><td>editor</td><td>${removeLink}</td></tr>");
                    $("#search").val("");
                } else {
                    alert("The user id " + userId + " was not found");
                }
            }).fail(function(jqxhr, textStatus, error) {
                alert('Error checking email address: ' + textStatus + ', ' + error);
            }).always(function() {
                //$('#gallerySpinner').hide();
            });
            return false;
        });

        /**
         * Save changes button on modal div (in calling page)
         */
        $("#saveEditors").click(function(el) {
            el.preventDefault();
            var editors = [];
            $("#userTable tr.editor").each(function() {
                editors.push($(this).find("td.userId").html());
            });
            //console.log("editors", editors);
            var params = {
                id: "${params.id}",
                editors: editors
            };
            $.post("${createLink(action: 'updateEditors')}", params, function(data, textStatus, jqXHR) {
                //console.log("data", data, "textStatus", textStatus,"jqXHR", jqXHR);
                alert("Editors were successfully saved");
                $('#modal').modal('hide');
                window.location.reload(true);
            }).error(function(jqXHR, textStatus, error) {
                alert("An error occurred: " + error + " - " + jqXHR.responseText);
            });
        });
    });
</script>
</body>
</html>