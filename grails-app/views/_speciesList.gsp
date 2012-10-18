 <!-- Template for diplaying a list of species list with or without a delete button -->
<div id="speciesList" class="speciesList">
    <script type="text/javascript">
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

        function fancyConfirm(msg,listId,callback){
            jQuery.fancybox({
                'content':"<div style=\"margin:1px;width:240px;text-align:left;\">"+msg+"<div style=\"text-align:right;margin-top:10px;\"><input id=\"fancyConfirm_cancel\" style=\"margin:3px;padding:0px;\" type=\"button\" value=\"No\"><input id=\"fancyConfirm_ok\" style=\"margin:3px;padding:0px;\" type=\"button\" value=\"Yes\"></div></div>",
                onComplete : function() {
                    jQuery("#fancyConfirm_cancel").click(function() {
                        ret = false;
                        jQuery.fancybox.close();
                    })
                    jQuery("#fancyConfirm_ok").click(function() {
                        ret = true;
                        jQuery.fancybox.close();
                        var url = "${createLink(controller:'speciesList', action:'delete')}" + "/"+listId;
                        //console.log("DELETE ITEMS",listId, url)
                        $.post(url,
                                function(data){
                                    //alert('Value returned from service: '  + data.uid);
                                    window.location.reload()
                                } );
                    })
                }

            })
        }
    </script>
    <table class="tableList">
        <colgroup>
            <col width="22%">
            <col width="19%">
            <col width="16%">
            <col width="12%">
            <col width="16%">
            <col width="5%">
        </colgroup>
    <thead>
    <tr>
        <td>List Name</td>
        <td>List Type</td>
        <td>Owner</td>
        <td>Date Submitted</td>
        <td>Item Count</td>
        <td/>
    </tr>
    </thead>
    <tbody>
    <g:each in="${lists}" var="list" status="i">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
            <td><a href="${request.contextPath}/speciesListItem/list/${list.dataResourceUid}">${fieldValue(bean: list, field: "listName")}</a></td>
            <td>${list.listType?.getDisplayValue()}</td>
            <td>${fieldValue(bean: list, field: "firstName")} ${fieldValue(bean: list, field: "surname")}</td>
            <td><g:formatDate format="yyyy-MM-dd" date="${list.dateCreated}"/></td>
            <td>${list.items.size()}</td>
            <g:if test="${list.username = request.getUserPrincipal()?.attributes?.email}">
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
                    <a href="#" onclick="fancyConfirm('Are you sure that you would like to delete ${list.listName}',${list.id});return false;" id="delete_${list.id}" class="buttonDiv">Delete</a>
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
            <g:else><td/></g:else>
        </tr>
    </g:each>
    </tbody>
</table>
<g:if test="${params.max<total}">
    <div class="pagination">
        <g:paginate total="${total}" action="showList"  />
    </div>
</g:if>
</div>
