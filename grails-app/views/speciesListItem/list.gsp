%{--
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
  --}%
<!doctype html>
<g:set var="bieUrl" value="${grailsApplication.config.bie.baseURL}"/>
<g:set var="collectoryUrl" value="${grailsApplication.config.collectory.baseURL}"/>
<g:set var="maxDownload" value="${grailsApplication.config.downloadLimit}"/>
<g:set var="userCanEditPermissions" value="${(speciesList.username == request.getUserPrincipal()?.attributes?.email || request.isUserInRole("ROLE_ADMIN"))}"/>
<g:set var="userCanEditData" value="${(speciesList.username == request.getUserPrincipal()?.attributes?.email ||
            request.isUserInRole("ROLE_ADMIN") ||
            request.getUserPrincipal()?.attributes?.userid in speciesList.editors
    )
}"/>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumb" content="${speciesList?.listName}"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/,Species lists"/>
    <script language="JavaScript" type="text/javascript" src="${asset.assetPath(src: 'facets.js')}"></script>
    <script language="JavaScript" type="text/javascript" src="${asset.assetPath(src: 'getQueryParam.js')}"></script>
    <script language="JavaScript" type="text/javascript"
            src="${asset.assetPath(src: 'jquery.doubleScroll.js')}"></script>
    <title>${message(code:'generic.specieslist.title', default:'Species list items')} | ${grailsApplication.config.skin.orgNameLong}</title>
    <asset:stylesheet src="fancybox.css"/>
    <style type="text/css">
    #buttonDiv, #refine, .not-visible {
        display: none;
    }

    .action-btn-group-width {
        width: 100px;
    }
    </style>

    <asset:script type="text/javascript" asset-defer="">
        function init(){
            document.getElementById("buttonDiv").style.display = "block";
            document.getElementById("refine").style.display = "block";
        }
        $(document).ready(function(){
            init();

            // in mobile view toggle display of facets
            $('#toggleFacetDisplay').click(function() {
                $(this).find('i').toggleClass('glyphicon-chevron-right glyphicon-chevron-down');
                if ($('#accordion').is(':visible')) {
                    $('#accordion').removeClass('overrideHide');
                } else {
                    $('#accordion').addClass('overrideHide');
                }
            });

            function loadGridOrList() {
                var storedView = amplify.store('view-state');
                var hash = location.hash ? location.hash : "";

                if (hash == '#grid') {
                    enableGrid()
                } else if (hash == '#list') {
                    enableList();
                } else if (hash.indexOf("#") != -1) {
                    if (storedView == '#grid') enableGrid();
                    if (storedView == '#list') enableList();
                    getAndViewRecordId(hash);
                    hash = storedView;
                } else if (storedView) {
                    // no hash but stored value - use this
                    location.hash = storedView;
                    hash = storedView;
                } else {
                    //if nothing else selected, default to list
                    enableList();
                }

                // store current hash (or previous view) in local storage (for pagination links)
                amplify.store('view-state', hash);


                // Add scroll bar to top and bottom of table
                //Moving this here as the display of top scroll bar depends on the table content width
                // and that will only be known after table data is loaded.
                $('.fwtable').doubleScroll();

            };

            // register handler for hashchange event
            window.onhashchange = loadGridOrList;

            // Since the event is only triggered when the hash changes, we need to trigger
            // the event now, to handle the hash the page may have loaded with.
            loadGridOrList();

            // download link
            $("#downloadLink").fancybox({
                'hideOnContentClick' : false,
                'hideOnOverlayClick': true,
                'showCloseButton': true,
                'titleShow' : false,
                'autoDimensions' : false,
                'width': 600,
                'height': 400,
                'padding': 20,
                'margin': 0,
                afterClose: function() {
                    $("label[for='reasonTypeId']").css("color","#444");
                }
            });

            // Tooltip for link title
            $('#content a').not('.thumbImage').tooltip({placement: "bottom", html: true, delay: 200, container: "body"});

            // submit edit record changes via POST
            $("button.saveRecord").click(function() {
                var id = $(this).data("id");
                var modal = $(this).data("modal");
                var thisFormData = $("form#editForm_" + id).serializeArray();

                if (!$("form#editForm_" + id).find("#rawScientificName").val()) {
                    alert("Required field: supplied name cannot be blank");
                    return false;
                }

                $.post("${createLink(controller: "editor", action: 'editRecord')}", thisFormData, function(data, textStatus, jqXHR) {
                    $(modal).modal('hide');
                    alert(jqXHR.responseText);
                    window.location.reload(true);
                }).error(function(jqXHR, textStatus, error) {
                    alert("An error occurred: " + error + " - " + jqXHR.responseText);
                    $(modal).modal('hide');
                });
            });

            // create record via POST
            $("button#saveNewRecord").click(function() {
                var id = $(this).data("id");
                var modal = $(this).data("modal");
                var thisFormData = $("form#editForm_").serializeArray();

                if (!$("form#editForm_").find("#rawScientificName").val()) {
                    alert("Required field: supplied name cannot be blank");
                    return false;
                }
                $.post("${createLink(controller: "editor", action: 'createRecord')}", thisFormData, function(data, textStatus, jqXHR) {
                    $(modal).modal('hide');
                    alert(jqXHR.responseText);
                    window.location.reload(true);
                }).error(function(jqXHR, textStatus, error) {
                    alert("An error occurred: " + error + " - " + jqXHR.responseText);
                    $(modal).modal('hide');
                });
            });

            // submit delete record via GET
            $("button.deleteSpecies").click(function() {
                var id = $(this).data("id");
                var modal = $(this).data("modal");

                $.get("${createLink(controller: "editor", action: 'deleteRecord')}", {id: id}, function(data, textStatus, jqXHR) {
                    $(modal).modal('hide');
                    //console.log("data", data, "textStatus", textStatus,"jqXHR", jqXHR);
                    alert(jqXHR.responseText + " - reloading page...");
                    window.location.reload(true);
                    //$('#modal').modal('hide');
                }).error(function(jqXHR, textStatus, error) {
                    alert("An error occurred: " + error + " - " + jqXHR.responseText);
                    $(modal).modal('hide');
                });
            });

            // Toggle display of list meta data editing
            $("#edit-meta-button").click(function(el) {
                el.preventDefault();
                toggleEditMeta(!$("#edit-meta-div").is(':visible'));
            });

            // submit edit meta data
            $("#edit-meta-submit").click(function(el) {
                el.preventDefault();
                var $form = $(this).parents("form");
                var thisFormData = $($form).serializeArray();
                // serializeArray ignores unchecked checkboxes so explicitly send data for these
                thisFormData = thisFormData.concat(
                    $($form).find('input[type=checkbox]:not(:checked)').map(
                        function() {
                            return {"name": this.name, "value": false}
                        }
                    ).get()
                );

                //console.log("thisFormData", thisFormData);

                $.post("${createLink(controller: "editor", action: 'editSpeciesList')}", thisFormData, function(data, textStatus, jqXHR) {
                    //console.log("data", data, "textStatus", textStatus,"jqXHR", jqXHR);
                    alert(jqXHR.responseText);
                    window.location.reload(true);
                }).error(function(jqXHR, textStatus, error) {
                    alert("An error occurred: " + error + " - " + jqXHR.responseText);
                    //$(modal).modal('hide');
                });
            });

            // toggle display of list info box
            $("#toggleListInfo").click(function(el) {
                el.preventDefault();
                $("#list-meta-data").slideToggle(!$("#list-meta-data").is(':visible'))
            });

            // catch click ion view record button (on each row)
            // extract values from main table and display in table inside modal popup
            $("a.viewRecordButton").click(function(el) {
                el.preventDefault();
                var recordId = $(this).data("id");
                viewRecordForId(recordId);
            });

            // mouse over affect on thumbnail images
            $('.imgCon').on('hover', function() {
                $(this).find('.brief, .detail').toggleClass('hide');
            });

        }); // end document ready

        function getAndViewRecordId(hash) {
            var prefix = "row_";
            var h = decodeURIComponent(hash.substring(1)).replace("+", " ");
            var d = $("tr[id^=" + prefix + "] > td.matchedName");
            var e = $("tr[id^=" + prefix + "] > td.rawScientificName");
            var data = d.add(e);
            $(data).each(function(i, el) {
                // Handle case insensitively: http://stackoverflow.com/a/2140644/2495717
                var hashVal = h.toLocaleUpperCase();
                var cell = $(el).text().trim().toLocaleUpperCase();
                if (hashVal === cell) {
                    var id = $(el).parent().attr("id").substring(prefix.length);
                    viewRecordForId(id);
                    return false;
                }
            });
        }

        function enableGrid() {
            $('#listView').slideUp();
            $('#gridView').slideDown();
            $('#listItemView .grid').addClass('disabled');
            $('#listItemView .list').removeClass('disabled');
            $('#viewRecord').modal("hide");
        }

        function enableList() {
            $('#gridView').slideUp();
            $('#listView').slideDown();
            $('#listItemView .list').addClass('disabled');
            $('#listItemView .grid').removeClass('disabled');
            $('#viewRecord').modal("hide");
        }

        function toggleEditMeta(showHide) {
            $("#edit-meta-div").slideToggle(showHide);
            //$("#edit-meta-button").hide();
            $("#show-meta-dl").slideToggle(!showHide);
        }

        function viewRecordForId(recordId) {
            // read header values from the table
            var headerRow = $("table#speciesListTable > thead th").not(".action");
            var headers = [];
            $(headerRow).each(function(i, el) {
                headers.push($(this).text());
            });
            // read species row values from the table
            var valueTds = $("tr#row_" + recordId + " > td").not(".action");
            var values = [];
            $(valueTds).each(function(i, el) {
                var val = $(this).html();
                if ($.type(val) === "string") {
                    val = $.trim(val);
                }
                values.push(val);
            });
            $("#viewRecord p.spinner").hide();
            $("#viewRecord tbody").html(""); // clear values
            $.each(headers, function(i, el) {
                var row = "<tr><td>"+el+"</td><td>"+values[i]+"</td></tr>";
                $("#viewRecord tbody").append(row);
            });
            $("#viewRecord table").show();
            $('#viewRecord').modal("show");
        }

        function reloadWithMax(el) {
            var max = $(el).find(":selected").val();
            var params = {
                fq: [ "${(fqs ? fqs.join("\", \"") : "")}" ],
                max: max,
                sort: "${params.sort}",
                order: "${params.order}",
                offset: "${params.offset ?: 0}",
                q: "${params.q}",
                id: "${params.id}"
            };
            var paramStr = jQuery.param(params, true);
            window.location.href = window.location.pathname + '?' + paramStr;
        }

        function resetSearch() {
            document.getElementById("searchInputButton").value = '';
        }

    </asset:script>
</head>

<body class="yui-skin-sam nav-species">
<div id="content" class="container-fluid">
    <header id="page-header">
        <div class="row">
            <div class="col-md-6">
                <div class="row">
                    <h2 class="subject-subtitle">
                        ${message(code:'view.lists.header', default:'Species List')}: <a href="${collectoryUrl}/public/show/${params.id}"
                                         title="${message(code:'view.lists.dataresource.tooltip', default:'view Date Resource page')}">${speciesList?.listName}</a>
                        &nbsp;&nbsp;
                        <div class="btn-group btn-group" id="listActionButtons">
                            <a href="#" id="toggleListInfo" class="btn btn-default btn-sm"><i
                                    class="glyphicon glyphicon-info-sign "></i> ${message(code:'public.lists.view.page.button01', default: 'List info')} </a>
                            <g:if test="${userCanEditPermissions}">
                                <a href="#" class="btn btn-default btn-sm" data-target="#modal" data-toggle="modal"><i
                                        class="glyphicon glyphicon-user "></i>  ${message(code:'public.lists.view.page.button07', default: 'Edit permissions')}</a>
                            </g:if>
                            <g:if test="${userCanEditData}">
                                <a href="#" class="btn btn-default btn-sm" data-target="#addRecord" data-toggle="modal"><i
                                        class="glyphicon glyphicon-plus-sign "></i> ${message(code:'public.lists.view.page.button08', default: 'Add species')}</a>
                            </g:if>
                        </div>
                    </h2>
                    <g:if test="${userCanEditPermissions}">
                        <div class="modal fade" id="modal" tabindex="-1" role="dialog">
                            <div class="modal-dialog" role="document">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>

                                        <h3 id="myModalLabel">${message(code:'public.lists.view.page.button09', default: 'Species list permissions')}</h3>
                                    </div>

                                    <div class="modal-body">
                                        <g:include controller="editor" action="editPermissions"
                                                   params="${[id: params.id]}"></g:include>
                                    </div>

                                    <div class="modal-footer">
                                        <button class="btn btn-default" data-dismiss="modal" aria-hidden="true">${message(code:'data-dismiss-close', default: 'Close')}</button>
                                        <button class="btn btn-primary" id="saveEditors">${message(code:'data-save', default: 'Save changes')}</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </g:if>
                    <g:if test="${userCanEditData}">
                        <div class="modal fade" id="addRecord" role="dialog">
                            <div class="modal-dialog" role="document">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>

                                        <h3>${message(code: 'public.lists.items.add', default: 'Add record values')}</h3>
                                    </div>

                                    <div class="modal-body">
                                        <g:include controller="editor" action="addRecordScreen"
                                                   params="${[id: params.id, action: 'addRecordScreen']}"></g:include>
                                    </div>

                                    <div class="modal-footer">
                                        <button class="btn btn-default" data-dismiss="modal" aria-hidden="true">${message(code:'data-dismiss-close', default: 'Close')}</button>
                                        <button class="btn btn-primary" id="saveNewRecord" data-id="${speciesList.id}"
                                                data-modal="#addRecord">${message(code:'data-save', default: 'Save changes')}</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </g:if>
                </div>
            </div>
            <div class="col-md-6" id="buttonDiv">
                <div class="row">
                    <div class="col-md-12">
                        <div class="pull-right margin-top-10">
                            <a href="#download" class="btn btn-primary" title="${message(code:'view.lists.dataresource.downloadoptions.tooltip', default:'View the download options for this species list.')}"
                               id="downloadLink">${message(code:'generic.lists.button.download.label', default: 'Download')}</a>

                            <a class="btn btn-primary" title="${message(code:'generic.lists.button.occurrences.tooltip_1', deafult:'View occurrences for up to')} ${maxDownload} ${message(code:'generic.lists.button.occurrences.tooltip_2', deafult:'species on the list')}"
                               href="${request.contextPath}/speciesList/occurrences/${params.id}${params.toQueryString()}&type=Search">${message(code:'generic.lists.button.occurrences.label', default: 'View occurrence records')}</a>

                            <a href="${request.contextPath}/speciesList/spatialPortal/${params.id}${params.toQueryString()}&type=Search"
                               class="btn btn-primary" title="${message(code:'generic.lists.button.spatial.tooltip', default:'View the spatial portal.')} ">${message(code:'generic.lists.button.spatial.label', default:'View in spatial portal')}</a>
                        </div> <!-- rightfloat -->
                    </div>
                </div>
            </div>

            <div style="display:none">
                <g:render template="/download"/>
            </div>
        </div><!--inner-->
    </header>

    <div class="alert alert-info not-visible" id="list-meta-data">
        <button type="button" class="close" onclick="$(this).parent().slideUp()">&times;</button>
        <g:if test="${userCanEditPermissions}">
            <a href="#" class="btn btn-default btn-sm" id="edit-meta-button"><i
                    class="glyphicon glyphicon-pencil"></i> ${message(code:'view.lists.listinfo.edit.button.label', default:'Edit')}</a>
        </g:if>
        <dl class="dl-horizontal" id="show-meta-dl">
            <dt>${message(code: 'speciesList.listName.label', default: 'List name')}</dt>
            <dd>${speciesList.listName ?: '&nbsp;'}</dd>
            <dt>${message(code: 'speciesList.username.label', default: 'Owner')}</dt>
            <dd>${speciesList.fullName ?: speciesList.username ?: '&nbsp;'}</dd>
            <dt>${message(code: 'speciesList.listType.label', default: 'List type')}</dt>
            <dd>${speciesList.listType?.displayValue}</dd>
            <g:if test="${speciesList.description}">
                <dt>${message(code: 'speciesList.description.label', default: 'Description')}</dt>
                <dd>${speciesList.description}</dd>
            </g:if>
            <g:if test="${speciesList.url}">
                <dt>${message(code: 'speciesList.url.label', default: 'URL')}</dt>
                <dd><a href="${speciesList.url}" target="_blank">${speciesList.url}</a></dd>
            </g:if>
            <g:if test="${speciesList.wkt}">
                <dt>${message(code: 'speciesList.wkt.label', default: 'WKT vector')}</dt>
                <dd>${speciesList.wkt}</dd>
            </g:if>
            <dt>${message(code: 'speciesList.dateCreated.label', default: 'Date submitted')}</dt>
            <dd><g:formatDate format="yyyy-MM-dd"
                              date="${speciesList.dateCreated ?: 0}"/><!-- ${speciesList.lastUpdated} --></dd>
            <dt>${message(code: 'speciesList.lastUpdated.label', default: 'Date updated')}</dt>
            <dd><g:formatDate format="yyyy-MM-dd"
                              date="${speciesList.lastUpdated ?: 0}"/></dd>
            <dt>${message(code: 'speciesList.isPrivate.label', default: 'Is private')}</dt>
            <dd><g:formatBoolean boolean="${speciesList.isPrivate ?: false}" true="Yes" false="No"/></dd>
            <dt>${message(code: 'speciesList.isBIE.label', default: 'Included in BIE')}</dt>
            <dd><g:formatBoolean boolean="${speciesList.isBIE ?: false}" true="Yes" false="No"/></dd>
            <dt>${message(code: 'speciesList.isAuthoritative.label', default: 'Authoritative')}</dt>
            <dd><g:formatBoolean boolean="${speciesList.isAuthoritative ?: false}" true="Yes" false="No"/></dd>
            <dt>${message(code: 'speciesList.isInvasive.label', default: 'Invasive')}</dt>
            <dd><g:formatBoolean boolean="${speciesList.isInvasive ?: false}" true="Yes" false="No"/></dd>
            <dt>${message(code: 'speciesList.isThreatened.label', default: 'Threatened')}</dt>
            <dd><g:formatBoolean boolean="${speciesList.isThreatened ?: false}" true="Yes" false="No"/></dd>
            <dt>${message(code: 'speciesList.isSDS.label', default: 'Part of the SDS')}</dt>
            <dd><g:formatBoolean boolean="${speciesList.isSDS ?: false}" true="Yes" false="No"/></dd>
            <dt>${message(code: 'speciesList.region.label', default: 'Region')}</dt>
            <dd>${speciesList.region ?: 'Not provided'}</dd>
            <g:if test="${speciesList.isSDS}">
                <g:if test="${speciesList.authority}">
                    <dt>${message(code: 'speciesList.authority.label', default: 'SDS Authority')}</dt>
                    <dd>${speciesList.authority}</dd>
                </g:if>
                <g:if test="${speciesList.category}">
                    <dt>${message(code: 'speciesList.category.label', default: 'SDS Category')}</dt>
                    <dd>${speciesList.category}</dd>
                </g:if>
                <g:if test="${speciesList.generalisation}">
                    <dt>${message(code: 'speciesList.generalisation.label', default: 'SDS Coordinate Generalisation')}</dt>
                    <dd>${speciesList.generalisation}</dd>
                </g:if>
                <g:if test="${speciesList.sdsType}">
                    <dt>${message(code: 'speciesList.sdsType.label', default: 'SDS Type')}</dt>
                    <dd>${speciesList.sdsType}</dd>
                </g:if>
            </g:if>
            <g:if test="${speciesList.editors}">
                <dt>${message(code: 'speciesList.editors.label', default: 'List editors')}</dt>
                <dd>${speciesList.editors.collect { sl.getFullNameForUserId(userId: it) }?.join(", ")}</dd>
            </g:if>
            <dt>${message(code: 'speciesList.metadata.label', default: 'Metadata link')}</dt>
            <dd><a href="${grailsApplication.config.collectory.baseURL}/public/show/${speciesList.dataResourceUid}">${grailsApplication.config.collectory.baseURL}/public/show/${speciesList.dataResourceUid}</a>
            </dd>
        </dl>
        <g:if test="${userCanEditPermissions}">
            <div id="edit-meta-div" class="not-visible">
                <form class="form-horizontal" id="edit-meta-form">
                    <input type="hidden" name="id" value="${speciesList.id}"/>

                    <div class="form-group">
                        <label class="control-label col-md-2"
                               for="listName">${message(code: 'speciesList.listName.label', default: 'List name')}</label>

                        <div class="col-md-10">
                            <input type="text" name="listName" id="listName" class="form-control full-width" value="${speciesList.listName}"/>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2"
                               for="owner">${message(code: 'speciesList.username.label', default: 'Owner')}</label>

                        <div class="col-md-10">
                            <select name="owner" id="owner" class="form-control full-width">
                                <g:each in="${users}" var="userId"><option
                                        value="${userId}" ${(speciesList.username == userId) ? 'selected="selected"' : ''}><sl:getFullNameForUserId
                                            userId="${userId}"/></option></g:each>
                            </select>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2"
                               for="listType">${message(code: 'speciesList.listType.label', default: 'List type')}</label>

                        <div class="col-md-10">
                            <select name="listType" id="listType" class="form-control full-width">
                                <g:each in="${au.org.ala.specieslist.ListType.values()}" var="type"><option
                                        value="${type.name()}" ${(speciesList.listType == type) ? 'selected="selected"' : ''}>${type.displayValue}</option></g:each>
                            </select>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2"
                               for="description">${message(code: 'speciesList.description.label', default: 'Description')}</label>

                        <div class="col-md-10">
                            <textarea rows="3" name="description" id="description"
                                      class="form-control">${speciesList.description}</textarea>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2"
                               for="url">${message(code: 'speciesList.url.label', default: 'URL')}</label>

                        <div class="col-md-10">
                            <input type="url" name="url" id="url" class="form-control full-width" value="${speciesList.url}"/>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2"
                               for="description">${message(code: 'speciesList.wkt.label', default: 'WKT vector')}</label>

                        <div class="col-md-10">
                            <textarea rows="3" name="wkt" id="wkt" class="form-control full-width">${speciesList.wkt}</textarea>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2"
                               for="dateCreated">${message(code: 'speciesList.dateCreated.label', default: 'Date submitted')}</label>

                        <div class="col-md-10">
                            <input type="date" name="dateCreated" id="dateCreated" data-date-format="yyyy-mm-dd"
                                   class="form-control full-width"
                                   value="<g:formatDate format="yyyy-MM-dd" date="${speciesList.dateCreated ?: 0}"/>"/>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2"
                               for="isPrivate">${message(code: 'speciesList.isPrivate.label', default: 'Is private')}</label>

                        <div class="col-md-10">
                            <input type="checkbox" id="isPrivate" name="isPrivate" value="true"
                                   data-value="${speciesList.isPrivate}" ${(speciesList.isPrivate == true) ? 'checked="checked"' : ''}/>
                        </div>
                    </div>
                    <g:if test="${request.isUserInRole("ROLE_ADMIN")}">
                        <div class="form-group">
                            <label class="control-label col-md-2"
                                   for="isBIE">${message(code: 'speciesList.isBIE.label', default: 'Included in BIE')}</label>

                            <div class="col-md-10">
                                <input type="checkbox" id="isBIE" name="isBIE" value="true"
                                       data-value="${speciesList.isBIE}" ${(speciesList.isBIE == true) ? 'checked="checked"' : ''}/>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="control-label col-md-2"
                                   for="isAuthoritative">${message(code: 'speciesList.isAuthoritative.label', default: 'Authoritative')}</label>

                            <div class="col-md-10">
                                <input type="checkbox" id="isAuthoritative" name="isAuthoritative"
                                       value="true"
                                       data-value="${speciesList.isAuthoritative}" ${(speciesList.isAuthoritative == true) ? 'checked="checked"' : ''}/>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="control-label col-md-2"
                                   for="isInvasive">${message(code: 'speciesList.isInvasive.label', default: 'Invasive')}</label>

                            <div class="col-md-10">
                                <input type="checkbox" id="isInvasive" name="isInvasive" value="true"
                                       data-value="${speciesList.isInvasive}" ${(speciesList.isInvasive == true) ? 'checked="checked"' : ''}/>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="control-label col-md-2"
                                   for="isThreatened">${message(code: 'speciesList.isThreatened.label', default: 'Threatened')}</label>

                            <div class="col-md-10">
                                <input type="checkbox" id="isThreatened" name="isThreatened"
                                       value="true"
                                       data-value="${speciesList.isThreatened}" ${(speciesList.isThreatened == true) ? 'checked="checked"' : ''}/>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="control-label col-md-2"
                                   for="isSDS">${message(code: 'speciesList.isSDS.label', default: 'Part of the SDS')}</label>

                            <div class="col-md-10">
                                <input type="checkbox" id="isSDS" name="isSDS" value="true"
                                       data-value="${speciesList.isSDS}" ${(speciesList.isSDS == true) ? 'checked="checked"' : ''}/>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="control-label col-md-2"
                                   for="region">${message(code: 'speciesList.region.label', default: 'Region')}</label>

                            <div class="col-md-10">
                                <input type="text" name="region" id="region" class="form-control full-width"
                                       value="${speciesList.region}"/>
                            </div>
                        </div>
                        <g:if test="${speciesList.isSDS}">
                            <div class="form-group">
                                <label class="control-label col-md-2"
                                       for="authority">${message(code: 'speciesList.authority.label', default: 'SDS Authority')}</label>

                                <div class="col-md-10">
                                    <input type="text" name="authority" id="authority" class="form-control full-width"
                                           value="${speciesList.authority}"/>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="control-label col-md-2"
                                       for="category">${message(code: 'speciesList.category.label', default: 'SDS Category')}</label>

                                <div class="col-md-10">
                                    <input type="text" name="category" id="category" class="form-control full-width"
                                           value="${speciesList.category}"/>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="control-label col-md-2"
                                       for="generalisation">${message(code: 'speciesList.generalisation.label', default: 'SDS Generalisation')}</label>

                                <div class="col-md-10">
                                    <input type="text" name="generalisation" id="generalisation" class="form-control full-width"
                                           value="${speciesList.generalisation}"/>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="control-label col-md-2"
                                       for="sdsType">${message(code: 'speciesList.sdsType.label', default: 'SDS Type')}</label>

                                <div class="col-md-10">
                                    <input type="text" name="sdsType" id="sdsType" class="form-control full-width"
                                           value="${speciesList.sdsType}"/>
                                </div>
                            </div>
                        </g:if>
                    </g:if>
                    <div class="form-group">
                        <div class="col-md-offset-2 col-md-10">
                            <button type="submit" id="edit-meta-submit" class="btn btn-primary">${message(code:'generic.lists.button.save.label', default:'Save')}</button>
                            <button class="btn btn-default" onclick="toggleEditMeta(false);
                            return false;">${message(code:'generic.lists.button.cancel.label', default:'Cancel')}</button>
                        </div>
                    </div>
                </form>
            </div>
        </g:if>
    </div>

    <g:if test="${flash.message}">
        <div class="inner row">
            <div class="message alert alert-info"><b>${message(code:'generic.lists.button.alert.label', default:'Alert')}:</b> ${flash.message}</div>
        </div>
    </g:if>

    <div class="inner row">
        <div class="col-md-2 well" id="facets-column">
            <div class="boxedZ attachedZ">
                <section class="meta">
                    <div class="matchStats">
                        <p>
                            <span class="count">${totalCount}</span>
                                ${message(code:'public.lists.facets.matchstats01', default:'Number of Taxa')}
                        </p>

                        <p>
                            <span class="count">${distinctCount}</span>
                                ${message(code:'public.lists.facets.matchstats02', default:'Distinct Species')}
                        </p>
                        <g:if test="${hasUnrecognised && noMatchCount != totalCount}">
                            <p>
                                <span class="count">${noMatchCount}</span>
                                <g:link action="list" id="${params.id}" title="${message(code:'public.lists.facets.matchstats03.tooltip', default:'View unrecognised taxa')}"
                                        params="${[fq: sl.buildFqList(fqs: fqs, fq: "guid:null"), max: params.max]}">${message(code:'public.lists.facets.matchstats03', default:'Unrecognised Taxa')}</g:link>
                            </p>
                        </g:if>
                    </div>
                </section>
                <section class="refine" id="refine">
                    <g:if test="${facets.size() > 0 || params.fq}">
                        <h4 class="hidden-xs">${message(code:'public.lists.facets.refine.header',default:'Refine results')}</h4>
                        <h4 class="visible-xs">
                            <a href="#" id="toggleFacetDisplay"><i class="glyphicon glyphicon-chevron-right"
                                                                   id="facetIcon"></i>
                                ${message(code:'public.lists.facets.refine.header',default:'Refine results')}</a>
                        </h4>

                        <div class="hidden-xs" id="accordion">
                            <g:set var="fqs" value="${params.list('fq')}"/>
                            <g:if test="${fqs.size() > 0 && fqs.get(0).length() > 0}">
                                <div id="currentFilter">
                                    <p>
                                        <span class="FieldName">${message(code:'public.lists.facets.refine.subheader', default:'Current Filters')}</span>
                                    </p>

                                    <div id="currentFilters" class="subnavlist">
                                        <ul>
                                            <g:each in="${fqs}" var="fq">
                                                <g:if test="${fq.length() > 0}">
                                                    <li>
                                                        <g:link action="list" id="${params.id}"
                                                                params="${[fq: sl.excludedFqList(fqs: fqs, fq: fq), max: params.max]}"
                                                                class="removeLink" title="${message(code:'public.lists.facets.refine.tooltip',default:'Uncheck (remove filter)')}"><i
                                                                class="glyphicon glyphicon-check"></i></g:link>
                                                        <g:if test="${fq.startsWith("Search-")}">
                                                            <g:message code="facet.${fq.replaceFirst("Search- ", "")}"
                                                                       default="${fq.replaceFirst("Search-", "")}"/>
                                                        </g:if>
                                                        <g:else>
                                                            <g:message code="facet.${fq.replaceFirst("kvp ", "")}"
                                                                       default="${fq.replaceFirst("kvp ", "")}"/>
                                                        </g:else>
                                                    </li>
                                                </g:if>
                                            </g:each>
                                        </ul>
                                    </div>
                                </div>
                            </g:if>

                            <g:each in="${facets}" var="entry">
                                <g:if test="${entry.key == "listProperties"}">
                                    <g:each in="${facets.get("listProperties")}" var="value">
                                        <g:render template="facet"
                                                  model="${[key: value.getKey(), values: value.getValue(), isProperty: true]}"/>
                                    </g:each>
                                    <div style="display:none"><!-- fancybox popup div -->
                                        <div id="multipleFacets">
                                            <p>${message(code:'public.lists.facets.refine.box.header', default:'Refine your search')}</p>

                                            <div id="dynamic" class="tableContainer"></div>
                                        </div>
                                    </div>
                                </g:if>
                                <g:else>
                                    <g:render template="facet"
                                              model="${[key: entry.key, values: entry.value, isProperty: false]}"/>
                                </g:else>
                            </g:each>
                        </div>
                    </g:if>
                </section>
            </div><!-- boxed attached -->
        </div> <!-- col narrow -->
        <div class="col-md-10">
        <div class="row">
            <div class="col-md-6">
                <div id="listItemView" class="btn-group">
                    <a class="btn btn-default btn-sm list disabled" title="${message(code:'public.lists.view.page.tooltip02', default:'View as detailed list')} " href="#list"><i
                            class="glyphicon glyphicon-th-list"></i> ${message(code:'public.lists.view.page.button02', default:'list')}</a>
                    <a class="btn btn-default btn-sm grid" title="${message(code:'public.lists.view.page.tooltip03', default:'View as thumbnail image grid')}" href="#grid"><i
                            class="glyphicon glyphicon-th"></i> ${message(code:'public.lists.view.page.button03', default:'grid')}</a>
                </div>
            </div>
            <div class="col-md-6">
                <div id="searchView" class="searchItemForm">
                    <g:form class="searchItemForm" controller="speciesListItem" action="list">
                        <input type="hidden" name="id" value="${speciesList.dataResourceUid}"/>

                        <div class="input-group" id="searchListItem">
                            <input class="form-control" id="searchInputButton" name="q" type="text" value="${params.q}"
                                   placeholder="${message(code:'public.lists.view.search.text', default:'Search by Supplied Name')}">

                            <div class="input-group-btn">
                                <button class="btn btn-default" type="submit">${message(code:'generic.lists.button.search.label', default:'Search')}</button>
                                <g:if test="${params.q}">
                                    <button class="btn btn-primary" onclick="resetSearch()">${message(code:'generic.lists.button.clearSearch.label', default:'Clear search')}</button>
                                </g:if>
                            </div>
                        </div>
                    </g:form>
                </div>
            </div>
        </div>

            <div id="gridView" class="not-visible">
                <g:each var="result" in="${results}" status="i">
                    <g:set var="recId" value="${result.id}"/>
                    <g:set var="bieTitle">${message(code:'public.lists.view.table.tooltip03', default:'species page for ')}<i>${result.rawScientificName}</i></g:set>
                    <div class="imgCon">
                        <a class="thumbImage viewRecordButton" rel="thumbs" title="${message(code:'public.lists.view.table.tooltip02', default:'click to view details')}" href="#viewRecord"
                                    data-id="${recId}"><img
                        src="${raw(result.imageUrl ?: asset.assetPath(src: 'infobox_info_icon.png\" style=\"opacity:0.5'))}"
                        alt="thumbnail species image"/>
                    </a>
                    <g:if test="${true}">
                        <g:set var="displayName">
                            <i><g:if test="${result.guid == null}">
                                ${fieldValue(bean: result, field: "rawScientificName")}
                            </g:if>
                                <g:else>
                                    ${result.matchedName}
                                </g:else></i>
                        </g:set>
                        <div class="meta brief">
                            ${raw(displayName)}
                        </div>

                        <div class="meta detail hide">
                            ${raw(displayName)}
                            <g:if test="${result.author}">${result.author}</g:if>
                            <g:if test="${result.commonName}"><br>${result.commonName}</g:if>
                            <div class="pull-right" style="display:inline-block; padding: 5px;">
                                <a href="#viewRecord" class="viewRecordButton" title="${message(code:'public.lists.view.table.tooltip01', default:'view record')}" data-id="${recId}"><i
                                        class="glyphicon glyphicon-info-sign glyphicon-white"></i></a>&nbsp;
                                <g:if test="${userCanEditData}">
                                    <a href="#" title="${message(code:'public.lists.view.table.tooltip04', default:'edit')}"
                                       data-remote="${createLink(controller: 'editor', action: 'editRecordScreen', id: result.id)}"
                                       data-target="#editRecord_${recId}" data-toggle="modal"><i
                                            class="glyphicon glyphicon-pencil glyphicon-white"></i></a>&nbsp;
                                    <a href="#" title="${message(code:'public.lists.view.table.tooltip05', default:'delete')}" data-target="#deleteRecord_${recId}"
                                       data-toggle="modal"><i class="glyphicon glyphicon-trash glyphicon-white"></i>
                                    </a>&nbsp;
                                </g:if>
                            </div>
                        </div>
                    </g:if>
                    </a>
                </div>

                </g:each>
            </div><!-- /#iconView -->
            <div id="listView" class="not-visible">
                <section class="double">
                    <div class="fwtable table-bordered" style="overflow:auto;width:100%;">
                        <table class="tableList table table-bordered table-striped" id="speciesListTable">
                            <thead>
                            <tr>
                                <th class="action">${message(code:'public.lists.view.table.action.label', default:'Action')}</th>
                                <g:sortableColumn property="rawScientificName" title="${message(code:'public.lists.items.header01', default:'Supplied Name')}"
                                                  params="${[fq: fqs]}"></g:sortableColumn>
                                <g:sortableColumn property="matchedName" title="${message(code:'public.lists.items.header02', default:'Scientific Name (matched)')}"
                                                  params="${[fq: fqs]}"></g:sortableColumn>
                                <th>${message(code:'public.lists.items.header05', default:'Image')}</th>
                                <g:sortableColumn property="author" title="${message(code:'public.lists.items.header03', default:'Author (matched)')}"
                                                  params="${[fq: fqs]}"></g:sortableColumn>
                                <g:sortableColumn property="commonName" title="${message(code:'public.lists.items.header04', default:'Common Name (matched)')}"
                                                  params="${[fq: fqs]}"></g:sortableColumn>
                                <g:each in="${keys}" var="key">
                                    <th>${key}</th>
                                </g:each>
                            </tr>
                            </thead>
                            <tbody>
                            <g:each var="result" in="${results}" status="i">
                                <g:set var="recId" value="${result.id}"/>
                                <g:set var="bieTitle">${message(code:'public.lists.view.table.tooltip03', default:'species page for')} <i>${result.rawScientificName}</i></g:set>
                                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}" id="row_${recId}">
                                    <td class="action">
                                        <div class="btn-group action-btn-group-width" role="group">
                                            <a class="btn btn-default btn-xs viewRecordButton" href="#viewRecord"
                                               title="${message(code:'public.lists.view.table.tooltip01', default:'view record')}" data-id="${recId}"><i
                                                    class="glyphicon glyphicon-info-sign"></i></a>
                                            <g:if test="${userCanEditData}">
                                                <a class="btn btn-default btn-xs" href="#" title="${message(code:'public.lists.view.table.tooltip04', default:'edit')}"
                                                   data-remote-url="${createLink(controller: 'editor', action: 'editRecordScreen', id: result.id)}"
                                                   data-target="#editRecord_${recId}" data-toggle="modal"><i
                                                        class="glyphicon glyphicon-pencil"></i></a>
                                                <a class="btn btn-default btn-xs" href="#" title="${message(code:'public.lists.view.table.tooltip05', default:'delete')}"
                                                   data-target="#deleteRecord_${recId}" data-toggle="modal"><i
                                                        class="glyphicon glyphicon-trash"></i></a>
                                            </g:if>
                                        </div>
                                    </td>
                                    <td class="rawScientificName">
                                        ${fieldValue(bean: result, field: "rawScientificName")}
                                        <g:if test="${result.guid == null}">
                                            <br/>(unmatched - try <a
                                                href="http://google.com/search?q=${fieldValue(bean: result, field: "rawScientificName").trim()}"
                                                target="google" class="btn btn-primary btn-xs">Google</a>,
                                            <a href="${grailsApplication.config.biocache.baseURL}/occurrences/search?q=${fieldValue(bean: result, field: "rawScientificName").trim()}"
                                               target="biocache" class="btn btn-success btn-xs">${message(code:'generic.lists.button.Occurrences.label', default:'Occurrences')}</a>)
                                        </g:if>
                                    </td>
                                    <td class="matchedName">
                                        <g:if test="${result.guid}">
                                            <a href="${bieUrl}/species/${result.guid}"
                                               title="${bieTitle}">${result.matchedName}</a>
                                        </g:if>
                                        <g:else>
                                            ${result.matchedName}
                                        </g:else>
                                    </td>
                                    <td id="img_${result.guid}">
                                        <g:if test="${result.imageUrl}">
                                            <a href="${bieUrl}/species/${result.guid}" title="${bieTitle}"><img
                                                    style="max-width: 400px;" src="${result.imageUrl}"
                                                    class="smallSpeciesImage"/></a>
                                        </g:if>
                                    </td>
                                    <td>${result.author}</td>
                                    <td id="cn_${result.guid}">${result.commonName}</td>
                                    <g:each in="${keys}" var="key">
                                        <g:set var="kvp" value="${result.kvpValues.find { it.key == key }}"/>
                                        <g:set var="val" value="${kvp?.vocabValue ?: kvp?.value}"/>
                                        <td class="kvp ${val?.length() > 35 ? 'scrollWidth' : ''}"><div>${val}</div>
                                        </td>
                                    </g:each>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </div>
                </section>
            </div> <!-- /#listView -->
            <div class="searchWidgets">
                ${message(code:'generic.lists.ItemsPerPage', default:'Items per page:')}
                <select id="maxItems" onchange="reloadWithMax(this)">
                    <g:each in="${[10, 25, 50, 100]}" var="max">
                        <option ${(params.max == max) ? 'selected="selected"' : ''}>${max}</option>
                    </g:each>
                </select>
            </div>

            <div class="pagination listPagination" id="searchNavBar">
                <g:if test="${params.fq}">
                    <hf:paginate total="${totalCount}" action="list" id="${params.id}" params="${[fq: params.fq]}"/>
                </g:if>
                <g:else>
                    <hf:paginate total="${totalCount}" action="list" id="${params.id}"/>
                </g:else>
            </div>
        %{-- Output the BS modal divs (hidden until called) --}%
            <g:each var="result" in="${results}" status="i">
                <g:set var="recId" value="${result.id}"/>
                <div class="modal fade" id="viewRecord" role="dialog">
                    <div class="modal-dialog" role="document">
                        <div class="modal-content">
                            <div class="modal-header">
                                <button type="button" class="close" onclick="$('#viewRecord .modal-body').scrollTop(0);"
                                        data-dismiss="modal" aria-hidden="true">×</button>

                                <h3>${message(code:'public.lists.view.action.header', default:'View record details')}</h3>
                            </div>

                            <div class="modal-body">
                                <p class="spinner"><img src="${asset.assetPath(src: 'spinner.gif')}"
                                                        alt="spinner icon"/></p>
                                <table class="table table-bordered table-condensed table-striped">
                                    <thead><th>${message(code:'public.lists.view.action.col01', default:'Field')}</th><th>${message(code:'public.lists.view.action.col02', default:'Value')}</th></thead>
                                    <tbody></tbody>
                                </table>
                            </div>

                            <div class="modal-footer">
                                <button class="btn btn-primary hide" data-id="${recId}">${message(code:'generic.lists.button.Previous.label', default:'Previous')}</button>
                                <button class="btn btn-primary hide" data-id="${recId}">${message(code:'generic.lists.button.Next.label', default:'Next')}</button>
                                <button class="btn btn-default" onclick="$('#viewRecord .modal-body').scrollTop(0);"
                                        data-dismiss="modal" aria-hidden="true">${message(code:'data-dismiss-close', default: 'Close')}</button>
                            </div>
                        </div>
                    </div>
                </div>
                <g:if test="${userCanEditData}">
                    <div class="modal fade editRecords" id="editRecord_${recId}" tabindex="-1" role="dialog">
                        <div class="modal-dialog" role="document">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <button type="button" class="close" data-dismiss="modal"
                                            aria-hidden="true">×</button>

                                    <h3>${message(code:'public.lists.view.table.edit.header', default:'Edit record values')}</h3>
                                </div>

                                <div class="modal-body">
                                    <p><img src="${asset.assetPath(src: 'spinner.gif')}" alt="spinner icon"/></p>
                                </div>

                                <div class="modal-footer">
                                    <button class="btn btn-default" data-dismiss="modal"
                                            aria-hidden="true">${message(code:'generic.lists.button.cancel.label', default:'Cancel')}</button>
                                    <button class="btn btn-primary saveRecord" data-modal="#editRecord_${recId}"
                                            data-id="${recId}">${message(code:'data-save', default: 'Save changes')}</button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="modal fade" id="deleteRecord_${recId}" tabindex="-1" role="dialog">
                        <div class="modal-dialog" role="document">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h3>${message(code:'public.lists.view.table.delete.header', default:'Are you sure you want to delete this species record?')}</h3>
                                </div>

                                <div class="modal-body">
                                    <p>${message(code:'public.lists.view.table.delete.text', default:'This will permanently delete the data for species')} <i>${result.rawScientificName}</i>
                                    </p>
                                </div>

                                <div class="modal-footer">
                                    <button class="btn btn-default" data-dismiss="modal"
                                            aria-hidden="true">${message(code:'generic.lists.button.cancel.label', default:'Cancel')}</button>
                                    <button class="btn btn-primary deleteSpecies" data-modal="#deleteRecord_${recId}"
                                            data-id="${recId}">${message(code:'admin.lists.actions.button.delete.label', default:'Delete')}</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </g:if>
            </g:each>
        </div> <!-- .col-md-9 -->
    </div>
</div> <!-- content div -->
<asset:javascript src="fancybox.js" asset-defer=""/>
<asset:javascript src="amplify.js" asset-defer=""/>
<asset:script type="text/javascript" asset-defer="">

    $(document).ready(function(){
        // make table header cells clickable
        $("table .sortable").each(function(i){
            var href = $(this).find("a").attr("href");
            $(this).css("cursor", "pointer");
            $(this).click(function(){
                window.location.href = href;
            });
        });

        $('.editRecords').on('show.bs.modal', function(e) {
            var $this = $(this);
            var data = $this.data();
            if(data["bs.modal"].options.remoteUrl){
                $.ajax(data["bs.modal"].options.remoteUrl,{
                    success: function(htmlContent) {
                        $this.find('.modal-body').html(htmlContent);
                    },
                    error: function() {
                        $this.find('.modal-body').html("An error occurred while loading data.");
                    }
                })
            }
        });
    });
</asset:script>
</body>
</html>