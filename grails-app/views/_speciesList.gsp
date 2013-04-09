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
                <td>
                <g:set var="test" value="${[id:list.id]}" />
                    <a href="#" onclick="fancyConfirm('Are you sure that you would like to delete ${list.listName}',${list.id},'delete');return false;" id="delete_${list.id}" class="buttonDiv">Delete</a>
                </td>
                <td>
                    <a href="#" onclick="fancyConfirm('Are you sure that you would like to rematch ${list.listName}',${list.id},'rematch');return false;" id="rematch_${list.id}" class="buttonDiv">Rematch</a>
                </td>
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
