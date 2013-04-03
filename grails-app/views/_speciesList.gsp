 <!-- Template for diplaying a list of species list with or without a delete button -->
    <script type="text/javascript">

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
            //console.log(this)
            var listId = this.id.replace("dialog_","");
            var url = "${createLink(controller:'speciesList', action:'delete')}" + "/"+listId;
            //console.log("DELETE ITEMS",listId, url)
            $.post(url,
                    function(data){
                        //alert('Value returned from service: '  + data.uid);
                        window.location.reload()
                    } );
            this.cancel();

        }

        function fancyConfirm(msg,listId,action,callback){
            //alert("${request.contextPath}"+"/speciesList/"+action+ "/"+listId)
            jQuery.fancybox({
                'content':"<div style=\"margin:1px;width:240px;text-align:left;\">"+msg+"<div style=\"text-align:right;margin-top:10px;\"><input id=\"fancyConfirm_cancel\" type=\"button\" value=\"No\" class=\"actionButton btn btn-small\">&nbsp;<input id=\"fancyConfirm_ok\" type=\"button\" value=\"Yes\" class=\"actionButton btn btn-small\"></div></div>",
                'padding': 10,
                'margin': 20,
                onComplete : function() {
                    jQuery("#fancyConfirm_cancel").click(function() {
                        ret = false;
                        jQuery.fancybox.close();
                    });
                    jQuery("#fancyConfirm_ok").click(function() {
                        ret = true;
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
                            jQuery.fancybox.close();
                        });
                    })
                }

            })
        }

        function reloadWithMax(el) {
            var max = $(el).find(":selected").val();
            var params = {
                max: max,
                sort: "${params.sort}",
                order: "${params.order}",
                offset: "${params.offset?:0}"
            }
            var paramStr = jQuery.param(params);
            window.location.href = window.location.pathname + '?' + paramStr;
        }
    </script>
<div style="float: right;">
    Items per page:
    <select id="maxItems" class="input-mini" onchange="reloadWithMax(this)">
        <g:each in="${[10,25,50,100]}" var="max">
            <option ${(params.max == max)?'selected="selected"':''}>${max}</option>
        </g:each>
    </select>
</div>
 <div id="speciesList" class="speciesList">
    <table class="tableList table table-bordered table-striped" style="width:100%;">
        <colgroup>
            <col width="26%">
            <col width="24%">
            <col width="16%">
            <col width="12%">
            <col width="12%">
            %{--<col width="5%">--}%
        </colgroup>
    <thead>
    <tr>
        <g:sortableColumn property="listName" title="${message(code: 'speciesList.listName.label', default: 'List Name')}" />
        <g:sortableColumn property="listType" title="${message(code: 'speciesList.listType.label', default: 'List Type')}" />
        <g:sortableColumn property="username" title="${message(code: 'speciesList.username.label', default: 'Owner')}" />
        <g:sortableColumn property="dateCreated" title="${message(code: 'speciesList.name.dateCreated', default: 'Date Submitted')}" />
        <g:sortableColumn property="itemsCount" title="${message(code: 'speciesList.name.count', default: 'Item Count')}" />
        %{--<th><a href="${createLink(controller: params.controller, action: params.action, params:[sort:'listName', order: (params.order == "asc") ? "desc" : "asc"])}">List Name</a></th>
        <th><a href="${createLink(controller: params.controller, action: params.action, params:[sort:'listType', order: (params.order == "asc") ? "desc" : "asc"])}">List Type</a></th>
        <th><a href="${createLink(controller: params.controller, action: params.action, params:[sort:'username', order: (params.order == "asc") ? "desc" : "asc"])}">Owner</a></th>
        <th><a href="${createLink(controller: params.controller, action: params.action, params:[sort:'dateCreated', order: (params.order == "asc") ? "desc" : "asc"])}">Date Submitted</a></th>
        <th><a href="${createLink(controller: params.controller, action: params.action, params:[sort:'count', order: (params.order == "asc") ? "desc" : "asc"])}">Item Count</a></th>--}%
        <g:if test="${request.getUserPrincipal()}">
            <th colspan="2">Actions</th>
        </g:if>
    </tr>
    </thead>
    <tbody>
    <g:each in="${lists}" var="list" status="i">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
            <td><a href="${request.contextPath}/speciesListItem/list/${list.dataResourceUid}">${fieldValue(bean: list, field: "listName")}</a></td>
            <td>${list.listType?.getDisplayValue()}</td>
            %{--<td>${fieldValue(bean: list, field: "firstName")} ${fieldValue(bean: list, field: "surname")}</td>--}%
            <td>${list.fullName}</td>
            <td><g:formatDate format="yyyy-MM-dd" date="${list.dateCreated}"/></td>
            <td>${list.items.size()}</td>
            <g:if test="${list.username == request.getUserPrincipal()?.attributes?.email || request.isUserInRole("ROLE_ADMIN")}">
                %{--<td id="delete${list.id}">--}%
                    %{--Delete--}%
                    %{--<a class="buttonDiv" href="#deleteList?listid=${list.id}">Delete</a>--}%
                    %{--<span class="button orange">--}%
                        %{--<g:form>--}%
                            %{--<g:hiddenField name="id" value="${list.id}"/>--}%
                            %{--<g:actionSubmit class="button orange" action="deleteList" value="Remove List" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/>--}%
                        %{--</g:form>--}%
                    %{--</span>--}%
                %{--</td>--}%
                <td>
                <g:set var="test" value="${[id:list.id]}" />
                %{--<gui:dialog--}%
                        %{--title="Delete ${list.listName}"--}%
                        %{--draggable="true"--}%
                        %{--id="dialog_${list.id}"--}%
                        %{--params="${test}"--}%
                        %{--buttons="[--}%
                                %{--[text:'Yes', handler: 'deleteAction', isDefault: false],--}%
                                %{--[text:'No', handler: 'function() {this.cancel();}', isDefault: true]--}%
                        %{--]"--}%
                        %{--triggers="[show:[id:'delete_'+list.id, on:'click']]"--}%
                %{-->--}%
                    %{--Are you sure you would like to delete?--}%
                %{--</gui:dialog>--}%
                    <a href="#" onclick="fancyConfirm('Are you sure that you would like to delete ${list.listName}',${list.id},'delete');return false;" id="delete_${list.id}" class="buttonDiv">Delete</a>
                </td>
                <td>
                    <a href="#" onclick="fancyConfirm('Are you sure that you would like to rematch ${list.listName}',${list.id},'rematch');return false;" id="rematch_${list.id}" class="buttonDiv">Rematch</a>
                </td>
                %{--<gui:dialog--}%
                        %{--title='Delete ${list.listName}'--}%
                        %{--form='true'--}%
                        %{--action="delete"  controller="speciesList"--}%
                        %{--triggers="[show:[id:'delete' + list.id, on:'click']]"--}%
                        %{--buttons="[--}%
                                %{--[text:'Yes', handler: 'function() {this.submit();}', isDefault: true],--}%
                                %{--[text:'No', handler: 'function() {this.cancel();}', isDefault: false]--}%
                        %{--]">--}%
                    %{--Are you sure you would like to delete?--}%
                %{--</gui:dialog>--}%
            </g:if>
            %{--<g:else><td/></g:else>--}%
        </tr>
    </g:each>
    </tbody>
</table>
<g:if test="${params.max<total}">
    <div class="pagination" id="searchNavBar" data-total="${total}" data-max="${params.max}">
        <g:paginate total="${total}" />
    </div>
</g:if>
</div>
