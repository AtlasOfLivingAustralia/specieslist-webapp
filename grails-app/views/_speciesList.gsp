<%@page defaultCodec="html" %>
<!-- Template for displaying a list of species list with or without a delete button -->
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
        jQuery.fancybox.open("<div style=\"padding:20px;width:400px;text-align:center;\">"+msg+"<div style=\"text-align:center;margin-top:10px;\"><input id=\"fancyConfirm_cancel\" type=\"button\" value=\"No\" class=\"actionButton btn btn-default btn-sm\">&nbsp;<input id=\"fancyConfirm_ok\" type=\"button\" value=\"Yes\" class=\"actionButton btn btn-default btn-sm\"><img src='${asset.assetPath(src:'spinner.gif')}' id='spinner'/></div></div>", {
            'padding': 0,
            'margin': 0,
            'width' : 'auto',
            'height': 'auto',
            afterShow : function() {
                jQuery("#fancyConfirm_cancel").click(function() {
                    ret = false;
                    jQuery.fancybox.close();
                });
                jQuery("#fancyConfirm_ok").click(function() {
                    ret = true;
                    $("img#spinner").show(); // show spinning gif
                    $("#fancyConfirm_ok").attr("disabled","disabled"); // disable "Yes" button while processing

                    var url = "${request.contextPath}"+"/speciesList/"+action+ "/"+listId;
                    //console.log("Dialog ACTION ITEMS",listId, url)
                    $.post(url, function(data){
                        //alert('Value returned from service: '  + data.uid);
                        alert('${message(code:'admin.lists.actions.button.message.ok', default:'Action was successful!')}');
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

<!-- Search panel -->
<g:set var="showActions" value="${source != 'public'}"/>

<div id="top-search-panel" class="row">
    <div class="col-md-5">
        <form class="listSearchForm">
            <div class="input-group" id="searchLists">
                <input id="appendedInputButton"
                       class="form-control"
                       name="q" type="text"
                       value="${params.q}"
                       placeholder="${message(code:'public.lists.search.text', default:'Search in list name, description or owner')}">
                <div class="input-group-btn">
                    <button class="btn btn-primary" type="submit">${message(code:'generic.lists.button.search.label', default:'Search')}</button>
                </div>
            </div>
        </form>
    </div>
    <div class="col-md-3">
        <form class="listSearchForm">
            <g:if test="${params.q}">
                <button class="btn btn-default" type="submit">${message(code:'public.lists.search.clear', default:'Clear search')}</button>
            </g:if>
        </form>
    </div>
    <div class="col-md-4">
        <div class="form-group pull-right">
            <label class="control-label">${message(code:'public.lists.search.options',default:'Items per page')}:</label>
            <select id="maxItems" class="form-control" onchange="reloadWithMax(this)">
                <g:each in="${[10,25,50,100,1000]}" var="max">
                    <option ${(params.max == max)?'selected="selected"':''}>${max}</option>
                </g:each>
            </select>
        </div>
    </div>
</div>
<!-- Search panel end -->

<!-- Search results -->
<div id="search-results" class="row">
    <div id="listFacets" class="col-md-2 well">

        <g:if test="${selectedFacets}">
            <h3>${message(code:'public.lists.search.filter.selected', default:'Selected filters')}</h3>
            <ul class="facets list-unstyled">
            <g:each in="${selectedFacets}" var="selectedFacet">
                <li>
                    <a href="${sl.selectedFacetLink([filter:selectedFacet.query])}" title="${message(code:'public.lists.search.filter.remove', default:'Click to remove this filter')}">
                    <span class="fa fa-check-square-o">&nbsp;</span>
                    <g:message code="${selectedFacet.facet.label}" default="${selectedFacet.facet.label}"/>
                    </a>
                </li>
            </g:each>
            </ul>
        </g:if>

        <h3>${message(code:'public.lists.types.headline', default:'Refine lists')}</h3>
        <ul class="facets list-unstyled">
            <g:each in="${typeFacets}" var="facet">
                <li>
                    <a href="${sl.facetLink([filter:facet.query]) }" title="${message(code:facet.tooltip)}">
                        <span class="fa fa-square-o">&nbsp;</span>
                        <span class="facet-item">
                            <g:message code="${facet.label}" default="${facet.label}"/>
                            <span class="facetCount"> (${facet.count})</span>
                        </span>
                    </a>
                </li>
            </g:each>
        </ul>
        <br>
        <ul class="facets list-unstyled">
            <g:each in="${tagFacets}" var="facet">
                <li>
                    <a href="${sl.facetLink([filter:facet.query]) }" title="${message(code:facet.tooltip)}">
                        <span class="fa fa-square-o">&nbsp;</span>
                        <span class="facet-item">
                            <g:message code="${facet.label}" default="${facet.label}"/>
                            <span class="facetCount"> (${facet.count})</span>
                        </span>
                    </a>
                </li>
            </g:each>
        </ul>
    </div>
    <div id="speciesList" class="col-md-10">
        <table class="table table-bordered table-striped">
            <thead>
            <tr>
                <g:sortableColumn property="listName" params="${params}"
                                  title="${message(code: 'speciesList.listName.label', default: 'List Name')}"/>
                <g:sortableColumn property="listType" params="${params}"
                                  title="${message(code: 'speciesList.listType.label', default: 'List Type')}"/>
                <g:if test="${request.isUserInRole("ROLE_ADMIN")}">
                    <g:sortableColumn property="isBIE" params="${params}"
                                      title="${message(code: 'speciesList.isBIE.label', default: 'Included in BIE')}"/>
                    <g:sortableColumn property="isSDS" params="${params}"
                                      title="${message(code: 'speciesList.isSDS.label', default: 'Part of the SDS')}"/>
                    <g:sortableColumn property="isPrivate" params="${params}"
                                      title="${message(code: 'speciesList.isPrivate.label', default: 'Private')}"/>
                </g:if>
                <g:sortableColumn property="ownerFullName" params="${params}"
                                  title="${message(code: 'speciesList.username.label', default: 'Owner')}"/>
                <g:sortableColumn property="dateCreated" params="${params}"
                                  title="${message(code: 'speciesList.name.dateCreated', default: 'Date Submitted')}"/>
                <g:sortableColumn property="lastUpdated" params="${params}"
                                  title="${message(code: 'speciesList.name.lastUpdated', default: 'Date Updated')}"/>
                <g:sortableColumn property="itemsCount" params="${params}"
                                  title="${message(code: 'speciesList.name.count', default: 'Item Count')}"/>
                <g:if test="${showActions && request.getUserPrincipal()}">
                    <th colspan="3">${message(code:'public.lists.view.table.action.label', default:'Action')}</th>
                </g:if>
            </tr>
            </thead>
            <tbody>
            <g:each in="${lists}" var="list" status="i">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                    <td>
                        <a href="${request.contextPath}/speciesListItem/list/${list.dataResourceUid}">${fieldValue(bean: list, field: "listName")}
                        </a>
                    </td>
                    <td>${list.listType?.getDisplayValue()}</td>
                    <g:if test="${request.isUserInRole("ROLE_ADMIN")}">
                        <td><g:formatBoolean boolean="${list.isBIE ?: false}" true="Yes" false="No"/></td>
                        <td><g:formatBoolean boolean="${list.isSDS ?: false}" true="Yes" false="No"/></td>
                        <td><g:formatBoolean boolean="${list.isPrivate ?: false}" true="Yes" false="No"/></td>
                    </g:if>
                    <td>${list.ownerFullName}</td>
                    <td><g:formatDate format="yyyy-MM-dd" date="${list.dateCreated}"/></td>
                    <td><g:formatDate format="yyyy-MM-dd" date="${list.lastUpdated}"/></td>
                    <td>${list.itemsCount}</td>
                    <g:if test="${showActions && (list.username == request.getUserPrincipal()?.attributes?.email || request.isUserInRole("ROLE_ADMIN"))}">
                        <td>
                            <g:set var="test" value="${[id: list.id]}"/>
                            <a href="#"
                               onclick="fancyConfirm('${message(code:"admin.lists.actions.button.delete.messages", default:"Are you sure that you would like to delete?")} ${list.listName.encodeAsHTML()}', ${list.id}, 'delete');
                               return false;" id="delete_${list.id}" class="btn btn-sm btn-primary">${message(code:"admin.lists.actions.button.delete.label", default:"Delete")}</a>
                        </td>
                        <td>
                            <a href="#"
                               onclick="fancyConfirm('${message(code:"admin.lists.actions.button.rematch.messages", default:"Are you sure that you would like to rematch?")} ${list.listName.encodeAsHTML()}', ${list.id}, 'rematch');
                               return false;" id="rematch_${list.id}" class="btn btn-sm btn-default">${message(code:"admin.lists.actions.button.rematch.label", default:"Reload")}</a>
                        </td>
                        <td>
                            <a href="${request.contextPath}/speciesList/upload/${list.dataResourceUid}"
                               class="btn btn-sm btn-default">${message(code:"admin.lists.actions.button.reload.label", default:"Reload")}</a>
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
</div>
