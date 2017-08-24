<%@page defaultCodec="html" %>
<!-- Template for diplaying a list of species list with or without a delete button -->
<asset:script type="text/javascript">

    $(document).ready(function(){
        // make table header cells clickable
        $("table .sortable").each(function(i){
            var href = $(this).find("a").attr("href");
            $(this).css("cursor", "pointer");
            $(this).click(function(){
                window.location.href = href;
            });
        });

    });

    function deleteAction(){
        var listId = this.id.replace("dialog_","");
        var url = "${createLink(controller:'speciesList', action:'delete')}" + "/"+listId;
        $.post(url,
                function(data){
                    window.location.reload()
                } );
        this.cancel();

    }

    function fancyConfirm(msg,listId,action,callback){
        //alert("${request.contextPath}"+"/speciesList/"+action+ "/"+listId)
        jQuery.fancybox({
            'content':"<div style=\"padding:20px;width:400px;text-align:center;\">"+msg+"<div style=\"text-align:center;margin-top:10px;\"><input id=\"fancyConfirm_cancel\" type=\"button\" value=\"No\" class=\"actionButton btn btn-default btn-sm\">&nbsp;<input id=\"fancyConfirm_ok\" type=\"button\" value=\"Yes\" class=\"actionButton btn btn-default btn-sm\"><img src='${asset.assetPath(src:'spinner.gif')}' id='spinner'/></div></div>",
            'padding': 0,
            'margin': 0,
            'width' : 'auto',
            'height': 'auto',
            onComplete : function() {
                jQuery("#fancyConfirm_cancel").click(function() {
                    ret = false;
                    jQuery.fancybox.close();
                });
                jQuery("#fancyConfirm_ok").click(function() {
                    ret = true;
                    $("img#spinner").show(); // show spinning gif
                    $("#fancyConfirm_ok").attr("disabled","disabled"); // disable "Yes" button while processing
                    //jQuery.fancybox.close();
                    var url = "${request.contextPath}"+"/speciesList/"+action+ "/"+listId;
                    //console.log("Dialog ACTION ITEMS",listId, url)
                    $.post(url, function(data){
                        //alert('Value returned from service: '  + data.uid);
                        alert(action + ' was successful');
                        window.location.reload()
                    }).error(function(jqXHR, textStatus, error) {
                        alert("An error occurred: " + error + " - " + jqXHR.responseText);
                    }).complete(function() {
                        $("img#spinner").hide();
                        $("#fancyConfirm_ok").removeAttr("disabled");
                        jQuery.fancybox.close();
                    });
                })
            }
        })
    }

    function reloadWithMax(el) {
        var max = $(el).find(":selected").val();
        //collect all the params that are applicable for the a page resizing
        var paramStr = "${raw(params.findAll {key, value -> key != 'max' && key != 'offset' && key != 'controller' && key != 'action' && !!value}.collect { it }.join('&'))}" + "&max="+max
        //alert(paramStr)
        window.location.href = window.location.pathname + '?' + paramStr;
    }
</asset:script>
<div class="row">
    <div class="col-md-5">
    <form class="listSearchForm">
        <div class="input-group" id="searchLists">
            <input id="appendedInputButton" class="form-control" name="q" type="text" value="${params.q}"
                   placeholder="Search in list name, description or owner">

            <div class="input-group-btn">
                <button class="btn btn-default" type="submit">Search</button>
            </div>
        </div>
    </form>
    </div>
<div class="col-md-3">
    <form class="listSearchForm">
        <g:if test="${params.q}">
            <button class="btn btn-primary" type="submit">Clear search</button>
        </g:if>
    </form>
</div>
    <div class="col-md-4">
        <div class="form-group pull-right">
            <label class="control-label">Items per page:</label>
            <select id="maxItems" onchange="reloadWithMax(this)">
                <g:each in="${[10,25,50,100]}" var="max">
                    <option ${(params.max == max)?'selected="selected"':''}>${max}</option>
                </g:each>
            </select>
        </div>
    </div>
</div>
<div id="speciesList" class="speciesList clearfix">
    <table class="table table-bordered table-striped">

        <thead>
        <tr>
            <g:sortableColumn property="listName" params="${[q:params.q]}"
                              title="${message(code: 'speciesList.listName.label', default: 'List Name')}"/>
            <g:sortableColumn property="listType" params="${[q:params.q]}"
                              title="${message(code: 'speciesList.listType.label', default: 'List Type')}"/>
            <g:if test="${request.isUserInRole("ROLE_ADMIN")}">
                <g:sortableColumn property="isBIE" params="${[q:params.q]}"
                                  title="${message(code: 'speciesList.isBIE.label', default: 'Included in BIE')}"/>
                <g:sortableColumn property="isSDS" params="${[q:params.q]}"
                                  title="${message(code: 'speciesList.isSDS.label', default: 'Part of the SDS')}"/>
            </g:if>
            <g:sortableColumn property="isAuthoritative" params="${[q:params.q]}"
                              title="${message(code: 'speciesList.isAuthoritative.label', default: 'Authoritative')}"/>
            <g:sortableColumn property="isInvasive" params="${[q:params.q]}"
                              title="${message(code: 'speciesList.isInvasive.label', default: 'Invasive')}"/>
            <g:sortableColumn property="isThreatened" params="${[q:params.q]}"
                              title="${message(code: 'speciesList.isThreatened.label', default: 'Threatened')}"/>
            <g:sortableColumn property="ownerFullName" params="${[q:params.q]}"
                              title="${message(code: 'speciesList.username.label', default: 'Owner')}"/>
            <g:sortableColumn property="dateCreated" params="${[q:params.q]}"
                              title="${message(code: 'speciesList.name.dateCreated', default: 'Date Submitted')}"/>
            <g:sortableColumn property="lastUpdated" params="${[q:params.q]}"
                              title="${message(code: 'speciesList.name.lastUpdated', default: 'Date Updated')}"/>
            <g:sortableColumn property="itemsCount" params="${[q:params.q]}"
                              title="${message(code: 'speciesList.name.count', default: 'Item Count')}"/>
            <g:if test="${request.getUserPrincipal()}">
                <th colspan="3">Actions</th>
            </g:if>
        </tr>
        </thead>
        <tbody>
        <g:each in="${lists}" var="list" status="i">
            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                <td><a href="${request.contextPath}/speciesListItem/list/${list.dataResourceUid}">${fieldValue(bean: list, field: "listName")}</a>
                </td>
                <td>${list.listType?.getDisplayValue()}</td>
                <g:if test="${request.isUserInRole("ROLE_ADMIN")}">
                    <td><g:formatBoolean boolean="${list.isBIE ?: false}" true="Yes" false="No"/></td>
                    <td><g:formatBoolean boolean="${list.isSDS ?: false}" true="Yes" false="No"/></td>
                </g:if>
                <td><g:formatBoolean boolean="${list.isAuthoritative ?: false}" true="Yes" false="No"/></td>
                <td><g:formatBoolean boolean="${list.isInvasive ?: false}" true="Yes" false="No"/></td>
                <td><g:formatBoolean boolean="${list.isThreatened ?: false}" true="Yes" false="No"/></td>
            %{--<td>${fieldValue(bean: list, field: "firstName")} ${fieldValue(bean: list, field: "surname")}</td>--}%
                <td>${list.ownerFullName}</td>
                <td><g:formatDate format="yyyy-MM-dd" date="${list.dateCreated}"/></td>
                <td><g:formatDate format="yyyy-MM-dd" date="${list.lastUpdated}"/></td>
                <td>${list.itemsCount}</td>
                <g:if test="${list.username == request.getUserPrincipal()?.attributes?.email || request.isUserInRole("ROLE_ADMIN")}">
                    <td>
                        <g:set var="test" value="${[id: list.id]}"/>
                        <a href="#"
                           onclick="fancyConfirm('Are you sure that you would like to delete ${list.listName.encodeAsHTML()}', ${list.id}, 'delete');
                           return false;" id="delete_${list.id}" class="buttonDiv">Delete</a>
                    </td>
                    <td>
                        <a href="#"
                           onclick="fancyConfirm('Are you sure that you would like to rematch ${list.listName.encodeAsHTML()}', ${list.id}, 'rematch');
                           return false;" id="rematch_${list.id}" class="buttonDiv">Rematch</a>
                    </td>
                    <td>
                        <a href="${request.contextPath}/speciesList/upload/${list.dataResourceUid}"
                           class="buttonDiv">Reload</a>
                    </td>
                </g:if>
            %{--<g:else><td/></g:else>--}%
            </tr>
        </g:each>
        </tbody>
    </table>
    <g:if test="${params.max < total}">
        <div class="pagination" id="searchNavBar" data-total="${total}" data-max="${params.max}">
            <hf:paginate total="${total}" params="${params}"/>
        </div>
    </g:if>
</div>
