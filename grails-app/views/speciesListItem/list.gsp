<%@ page import="au.org.ala.specieslist.AuthService" %>
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
<g:set var="collectoryUrl" value="${grailsApplication.config.collectory.baseURL}" />
<g:set var="maxDownload" value="${grailsApplication.config.downloadLimit}" />
<g:set var="userCanEditPermissions" value="${
    params.action == 'listAuth' &&
    (speciesList.username == request.getUserPrincipal()?.attributes?.email || request.isUserInRole("ROLE_ADMIN"))
}" />
<g:set var="userCanEditData" value="${
    params.action == 'listAuth' &&
    (   speciesList.username == request.getUserPrincipal()?.attributes?.email ||
        request.isUserInRole("ROLE_ADMIN") ||
        request.getUserPrincipal()?.attributes?.email in speciesList.editors
    )
}" />
<html>
<head>
    %{--<gui:resources components="['dialog']"/>--}%
    <r:require modules="fancybox"/>
    <meta name="layout" content="main"/>
    <link rel="stylesheet" href="${resource(dir:'css',file:'scrollableTable.css')}"/>
    <script language="JavaScript" type="text/javascript" src="${resource(dir:'js',file:'facets.js')}"></script>
    <script language="JavaScript" type="text/javascript" src="${resource(dir:'js',file:'getQueryParam.js')}"></script>
    <script language="JavaScript" type="text/javascript" src="${resource(dir:'js',file:'jquery-ui-1.8.17.custom.min.js')}"></script>
    <script language="JavaScript" type="text/javascript" src="${resource(dir:'js',file:'jquery.doubleScroll.js')}"></script>
    <title>Species list items | Atlas of Living Australia</title>
    <style type="text/css">
    #buttonDiv {display: none;}
    #refine {display:none;}
    </style>

    <script type="text/javascript">
    function init(){
        document.getElementById("buttonDiv").style.display = "block";
        document.getElementById("refine").style.display = "block";
    }
    $(document).ready(function(){
        init();

        // download link
        $("#downloadLink").fancybox({
            'hideOnContentClick' : false,
            'hideOnOverlayClick': true,
            'showCloseButton': true,
            'titleShow' : false,
            'autoDimensions' : false,
            'width': 520,
            'height': 400,
            'padding': 10,
            'margin': 10,
            onCleanup: function() {
                $("label[for='reasonTypeId']").css("color","#444");
            }
        });

        // fancybox div for refining search with multiple facet values
        $(".multipleFacetsLink").fancybox({
            'hideOnContentClick' : false,
            'hideOnOverlayClick': true,
            'showCloseButton': true,
            'titleShow' : false,
            'transitionIn': 'elastic',
            'transitionOut': 'elastic',
            'speedIn': 400,
            'speedOut': 400,
            'scrolling': 'auto',
            'centerOnScroll': true,
            'autoDimensions' : false,
            'width': 560,
            'height': 560,
            'padding': 10,
            'margin': 10

        });

        // Add scroll bar to top and bottom of table
        $('.fwtable').doubleScroll();

        // Tooltip for link title
        $('#content a').tooltip({placement: "bottom", html: true, delay: 200});

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
                //console.log("data", data, "textStatus", textStatus,"jqXHR", jqXHR);
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
            //thisFormData.push({id: id});
            //console.log("thisFormData", id, thisFormData)
            $.post("${createLink(controller: "editor", action: 'createRecord')}", thisFormData, function(data, textStatus, jqXHR) {
                //console.log("data", data, "textStatus", textStatus,"jqXHR", jqXHR);
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

        //console.log("owner = ${speciesList.username}");
        //console.log("logged in user = ${request.getUserPrincipal()?.attributes?.email}");

        // Toggle display of list meta data editing
        $("#edit-meta-button").click(function(el) {
            el.preventDefault();
            toggleEditMeta(!$("#edit-meta-div").is(':visible'));
        });

        // submit edit meta data
        $("#edit-meta-submit").click(function(el) {
            el.preventDefault();
            var thisFormData = $(this).parents("form").serializeArray();
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

    }); // end document ready

    function toggleEditMeta(showHide) {
        $("#edit-meta-div").slideToggle(showHide);
        //$("#edit-meta-button").hide();
        $("#show-meta-dl").slideToggle(!showHide);
    }

//    function loadMultiFacets(facetName, displayName) {
//        console.log(facetName, displayName)
//        console.log("#div"+facetName,$("#div"+facetName).innerHTML)
//        $("div#dynamic").innerHTML=$("#div"+facetName).innerHTML;
//    }

    function downloadOccurrences(o){
        if(validateForm()){
            this.cancel();
            //downloadURL = $("#email").val();
            downloadURL = "${request.contextPath}/speciesList/occurrences/${params.id}${params.toQueryString()}&type=Download&email="+$("#email").val()+"&reasonTypeId="+$("#reasonTypeId").val()+"&file="+$("#filename").val();
            window.location =  downloadURL//"${request.contextPath}/speciesList/occurrences/${params.id}?type=Download&email=$('#email').val()&reasonTypeId=$(#reasonTypeId).val()&file=$('#filename').val()"
        }
    }
    function downloadFieldGuide(o){
        if(validateForm()){
            this.cancel();
            //alert(${params.toQueryString()})
            window.location = "${request.contextPath}/speciesList/fieldGuide/${params.id}${params.toQueryString()}"
        }

    }
    function downloadList(o){
         if(validateForm()){
             this.cancel();
             window.location = "${request.contextPath}/speciesListItem/downloadList/${params.id}${params.toQueryString()}&file="+$("#filename").val()
         }
    }
    function validateForm() {
        var isValid = false;
        var reasonId = $("#reasonTypeId option:selected").val();

        if (reasonId) {
            isValid = true;
        } else {
            $("#reasonTypeId").focus();
            $("label[for='reasonTypeId']").css("color","red");
            alert("Please select a \"download reason\" from the drop-down list");
        }

        return isValid;
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
    <r:layoutResources/>
</head>
<body class="yui-skin-sam species">
<r:layoutResources/>
<div id="content" >
    <header id="page-header">

        <div class="inner row-fluid">
            <div id="breadcrumb" class="span12">
                <ol class="breadcrumb">
                    %{--<li><a href="http://www.ala.org.au">Home</a> <span class=" icon icon-arrow-right"></span></li>--}%
                    <li><a href="${request.contextPath}/public/speciesLists">Species lists</a> <span class=" icon icon-arrow-right"></span></li>
                    <li class="active">${speciesList?.listName?:"Species list items"}</li>
                </ol>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span7">
                <h2>
                    Species List: <a href="${collectoryUrl}/public/show/${params.id}" title="view Date Resource page">${speciesList?.listName}</a>
                    <g:if test="${userCanEditPermissions}">
                        <a href="#" class="btn btn-small" data-remote="${createLink(controller: 'editor', action: 'editPermissions', id: params.id)}"
                            data-target="#modal" data-toggle="modal" style="margin:0 0 5px 12px;"><i class="icon-user "></i> Edit permissions</a>
                    </g:if>
                    <g:if test="${userCanEditData}">
                        <a href="#" class="btn btn-small" data-remote="${createLink(controller: 'editor', action: 'addRecordScreen', id: params.id)}"
                           data-target="#addRecord" data-toggle="modal" style="margin:0 0 5px 5px;"><i class="icon-plus-sign "></i> Add species</a>
                    </g:if>
                </h2>
            </div>
            <g:if test="${userCanEditPermissions}">
                <div class="modal hide fade" id="modal">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                        <h3 id="myModalLabel">Species list permissions</h3>
                    </div>
                    <div class="modal-body">
                        <p><img src="${resource(dir:'images',file:'spinner.gif')}" alt="spinner icon"/></p>
                    </div>
                    <div class="modal-footer">
                        <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
                        <button class="btn btn-primary" id="saveEditors">Save changes</button>
                    </div>
                </div>
            </g:if>
            <g:if test="${userCanEditData}">
                <div class="modal hide fade" id="addRecord">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                        <h3>Add record values</h3>
                    </div>
                    <div class="modal-body">
                        <p><img src="${resource(dir:'images',file:'spinner.gif')}" alt="spinner icon"/></p>
                    </div>
                    <div class="modal-footer">
                        <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
                        <button class="btn btn-primary" id="saveNewRecord" data-id="${speciesList.id}" data-modal="#addRecord">Save changes</button>
                    </div>
                </div>
            </g:if>
            <div class="span5 header-btns" id="buttonDiv">
                %{--<div id="buttonDiv" class="buttonDiv">--}%
                <a href="#download" class="btn btn-ala" title="View the download options for this species list." id="downloadLink">Download</a>
                <a class="btn btn-ala" title="View occurrences for up to ${maxDownload} species on the list"
                   href="${request.contextPath}/speciesList/occurrences/${params.id}${params.toQueryString()}&type=Search">View Occurrences</a>
            </div>  <!-- rightfloat -->

            <div style="display:none">
                <g:render template="/download"/>
            </div>
        </div><!--inner-->
    </header>
    <div class="well well-small" id="list-meta-data">
        <g:if test="${userCanEditPermissions}">
            <a href="#" class="btn btn-small" id="edit-meta-button"><i class="icon-pencil"></i> Edit</a>
        </g:if>
        <dl class="dl-horizontal" id="show-meta-dl">
            <dt>${message(code: 'speciesList.listName.label', default: 'List name')}</dt>
            <dd>${speciesList.listName?:'&nbsp;'}</dd>
            <dt>${message(code: 'speciesList.username.label', default: 'Owner')}</dt>
            <dd>${speciesList.fullName?:'&nbsp;'}</dd>
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
            <dd><g:formatDate format="yyyy-MM-dd" date="${speciesList.dateCreated?:0}"/><!-- ${speciesList.lastUpdated} --></dd>
            <dt>${message(code: 'speciesList.isPrivate.label', default: 'Is private')}</dt>
            <dd><g:formatBoolean boolean="${speciesList.isPrivate?:false}" /></dd>
        </dl>
        <g:if test="${userCanEditPermissions}">
            <div id="edit-meta-div" class="hide">
                <form class="form-horizontal" id="edit-meta-form">
                    <input type="hidden" name="id" value="${speciesList.id}" />
                    <div class="control-group">
                        <label class="control-label" for="listName">${message(code: 'speciesList.listName.label', default: 'List name')}</label>
                        <div class="controls">
                            <input type="text" name="listName" id="listName" class="input-xlarge" value="${speciesList.listName}" />
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="owner">${message(code: 'speciesList.username.label', default: 'Owner')}</label>
                        <div class="controls">
                            <select name="owner" id="owner" class="input-xlarge">
                                <g:each in="${users}" var="userId"><option value="${userId}" ${(speciesList.username == userId) ? 'selected="selected"':''}><sl:getFullNameForUserId userId="${userId}" /></option></g:each>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="listType">${message(code: 'speciesList.listType.label', default: 'List type')}</label>
                        <div class="controls">
                            <select name="listType" id="listType" class="input-xlarge">
                                <g:each in="${au.org.ala.specieslist.ListType.values()}" var="type"><option value="${type.name()}" ${(speciesList.listType == type) ? 'selected="selected"':''}>${type.displayValue}</option></g:each>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="description">${message(code: 'speciesList.description.label', default: 'Description')}</label>
                        <div class="controls">
                            <textarea rows="3" name="description" id="description" class="input-block-level">${speciesList.description}</textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="url">${message(code: 'speciesList.url.label', default: 'URL')}</label>
                        <div class="controls">
                            <input type="url" name="url" id="url" class="input-xlarge" value="${speciesList.url}" />
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="description">${message(code: 'speciesList.wkt.label', default: 'WKT vector')}</label>
                        <div class="controls">
                            <textarea rows="3" name="wkt" id="wkt" class="input-block-level">${speciesList.wkt}</textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="dateCreated">${message(code: 'speciesList.dateCreated.label', default: 'Date submitted')}</label>
                        <div class="controls">
                            <input type="date" name="dateCreated" id="dateCreated" data-date-format="yyyy-mm-dd" class="input-xlarge" value="<g:formatDate format="yyyy-MM-dd" date="${speciesList.dateCreated?:0}"/>" />
                            %{--<g:datePicker name="dateCreated" value="${speciesList.dateCreated}" precision="day" relativeYears="[-2..7]" class="input-small"/>--}%
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="isPrivate">${message(code: 'speciesList.isPrivate.label', default: 'Is private')}</label>
                        <div class="controls">
                            <input type="checkbox" id="isPrivate" name="isPrivate" class="input-xlarge" value="true" data-value="${speciesList.isPrivate}" ${(speciesList.isPrivate == true) ? 'checked="checked"':''} />
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <button type="submit" id="edit-meta-submit" class="btn btn-primary">Save</button>
                            <button class="btn" onclick="toggleEditMeta(false);return false;">Cancel</button>
                        </div>
                    </div>
                </form>
            </div>
        </g:if>
    </div>

<div class="inner row-fluid">
    <g:if test="${flash.message}">
        <div class="message alert alert-info"><b>Alert:</b> ${flash.message}</div>
    </g:if>

    <div class="span3 well" id="facets-column">
        <div class="boxedZ attachedZ">
                %{--<div id="customiseList" >--}%

                    %{--<a id="customiseListButton" class="buttonDiv">Customise</a>--}%
                %{--</div>--}%
                <section class="meta">
                    <div class="matchStats">
                        %{--<p>--}%
                            %{--<span class="count">${speciesList.firstName} ${speciesList.surname}</span>--}%
                            %{--Owner--}%
                        %{--</p>--}%
                        <p>
                            <span class="count">${totalCount}</span>
                            Number of Taxa
                        </p>
                        %{--<br/>--}%
                        <p>
                            <span class="count">${distinctCount}</span>
                            Distinct Species
                        </p>

                        <g:if test="${noMatchCount>0}">
                        %{--<br/>--}%
                            <p>
                                <span class="count">${noMatchCount}</span>
                                <a href="?fq=guid:null${queryParams}" title="View unrecognised taxa">Unrecognised Taxa </a>
                            </p>
                        </g:if>

                    </div>
                </section>
                <section class="refine" id="refine">
                    <g:if test="${facets.size()>0 || params.fq}">
                        <h4>Refine results</h4>
                        <g:set var="fqs" value="${params.list('fq')}" />
                        <g:if test="${fqs.size()>0&& fqs.get(0).length()>0}">
                            <div id="currentFilter">
                                <p>
                                    <span class="FieldName">Current Filters</span>
                                </p>
                                <div id="currentFilters" class="subnavlist">
                                    <ul>
                                        <g:each in="${fqs}" var="fq">
                                            <g:if test="${fq.length() >0}">
                                                <li>
                                                    <a href="#" class="removeLink " title="Uncheck (remove filter)" onclick="removeFacet('${fq}')"><i class="icon-check"></i></a>
                                                    ${fq.replaceFirst("kvp ","")}
                                                    %{--<a class="removeLink" onclick="removeFacet('family:ACANTHASPIDIIDAE'); return false;" href="#" oldtitle="remove filter" aria-describedby="ui-tooltip-1">X</a>--}%
                                                    %{--[<b><a href="#" class="removeLink" title="Remove Filter" onclick="removeFacet('${fq}')">X</a></b>]--}%
                                                </li>
                                            </g:if>
                                        </g:each>
                                    </ul>
                                </div>
                            </div>
                        </g:if>
                        <g:if test="${facets.containsKey("listProperties")}">
                            <g:each in="${facets.get("listProperties")}" var="value">
                                <p>
                                    <span class="FieldName">${value.getKey()}</span>
                                </p>
                                <div id="facet-${value.getKey()}" class="subnavlist">
                                    <ul>
                                        <g:set var="i" value="${0}" />
                                        <g:set var="values" value="${value.getValue()}" />
                                    %{--<g:each in="${value.getValue()}" var="arr">--}%
                                        <g:while test="${i < 4 && i<values.size()}">

                                            <g:set var="arr" value="${values.get(i)}" />
                                            <li>
                                                <a href="?fq=kvp ${arr[0]}:${arr[1]}${queryParams}">${arr[2]?:arr[1]}</a>  (${arr[3]})
                                            </li>
                                            <%i++%>
                                        </g:while>
                                        <g:if test="${values.size()>4}">
                                            <div class="showHide">
                                                <i class="icon icon-hand-right"></i> <a href="#div${value.getKey().replaceAll(" " ,"_")}" class="multipleFacetsLink" id="multi-${value.getKey()}"
                                                                                        title="See full list of values">choose more...</a>
                                                <div style="display:none">
                                                    <div id="div${value.getKey().replaceAll(" " ,"_")}">

                                                        %{--<a class="multipleFacetsLink" title="See more options." id="options${value.getKey()}">choose more...</a>--}%
                                                        %{--<gui:dialog--}%
                                                        %{--title="Refine List"--}%
                                                        %{--draggable="true"--}%
                                                        %{--id="dialog_${value.getKey().replaceAll(" " ,"_")}"--}%

                                                        %{--buttons="[--}%

                                                        %{--[text:'Close', handler: 'function() {this.cancel();}', isDefault: true]--}%
                                                        %{--]"--}%
                                                        %{--triggers="[show:[id:'options'+value.getKey(), on:'click']]"--}%
                                                        %{-->--}%

                                                        <h3>Refine your search</h3>
                                                        <table class='table table-striped compact scrollTable'>
                                                            <thead class='fixedHeader'>
                                                            <tr class='tableHead'>
                                                                <th>&nbsp;</th>
                                                                <th>${value.getKey()}</th>
                                                                <th>Count</th>
                                                            </tr>
                                                            </thead>
                                                            <tbody class='scrollContent'>
                                                            <g:each in="${value.getValue()}" var="arr">
                                                                <tr>
                                                                    <td>&nbsp</td>
                                                                    <td><a href="?fq=kvp ${arr[0]}:${arr[1]}${queryParams}">${arr[2]?:arr[1]} </a></td>
                                                                    <td>${arr[3]}</td>
                                                                </tr>
                                                            </g:each>
                                                            </tbody>
                                                        </table>
                                                        %{--</gui:dialog>--}%
                                                    </div>

                                                </div>
                                            </div><!-- invisible content div for facets -->
                                        </g:if>
                                    %{--</g:each>--}%
                                    </ul>
                                </div>

                            </g:each>
                            <div style="display:none"><!-- fancybox popup div -->
                                <div id="multipleFacets">
                                    <p>Refine your search</p>
                                    <div id="dynamic" class="tableContainer"></div>
                                    %{--<div id='submitFacets'>--}%
                                    %{--<input type='submit' class='submit' id="include" value="INCLUDE selected items in search"/>--}%
                                    %{--&nbsp;--}%
                                    %{--<input type='submit' class='submit' id="exclude" value="EXCLUDE selected items from search"/>--}%
                                    %{--</div>--}%
                                </div>
                            </div>
                        </g:if>
                    </g:if>

                </section>
            </div><!-- boxed attached -->
        </div> <!-- col narrow -->

        <div class="span9">
            <div class="">
                <section class="double">
                    <div class="fwtable table-bordered" style="overflow:auto;width:100%;">
                        <table class="tableList table table-bordered table-striped">
                            <thead>
                            <tr>
                                <g:if test="${userCanEditData}">
                                    <th>Action</th>
                                </g:if>
                                <th>Supplied Name</th>
                                <th>Scientific Name (matched)</th>
                                <th>Image</th>
                                <th>Author (matched)</th>
                                <th>Common Name (matched)</th>
                                <g:each in="${keys}" var="key">
                                    <th>${key}</th>
                                </g:each>
                            </tr>
                            </thead>
                            <tbody>
                            <g:each var="result" in="${results}" status="i">
                                <g:set var="recId" value="${result.id}"/>
                                <g:set var="bieSpecies" value="${bieItems?.get(result.guid)}"/>
                                <g:set var="bieTitle">species page for <i>${result.rawScientificName}</i></g:set>
                                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                                    <g:if test="${userCanEditData}">
                                        <td>
                                            <div class="btn-group btn-group">
                                                <a class="btn btn-small" href="#" title="edit" data-remote="${createLink(controller: 'editor', action: 'editRecordScreen', id: result.id)}"
                                                   data-target="#editRecord_${recId}" data-toggle="modal" ><i class="icon-pencil"></i></a>
                                                <a class="btn btn-small" href="#" title="delete" data-target="#deleteRecord_${recId}" data-toggle="modal"><i class="icon-trash"></i></a>
                                            </div>
                                            <div class="modal hide fade" id="editRecord_${recId}">
                                                <div class="modal-header">
                                                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                                                    <h3>Edit record values</h3>
                                                </div>
                                                <div class="modal-body">
                                                    <p><img src="${resource(dir:'images',file:'spinner.gif')}" alt="spinner icon"/></p>
                                                </div>
                                                <div class="modal-footer">
                                                    <button class="btn" data-dismiss="modal" aria-hidden="true">Cancel</button>
                                                    <button class="btn btn-primary saveRecord" data-modal="#editRecord_${recId}" data-id="${recId}">Save changes</button>
                                                </div>
                                            </div>
                                            <div class="modal hide fade" id="deleteRecord_${recId}">
                                                <div class="modal-header">
                                                    <h3>Are you sure you want to delete this species record?</h3>
                                                </div>
                                                <div class="modal-body">
                                                    <p>This will permanently delete the data for species <i>${result.rawScientificName}</i></p>
                                                </div>
                                                <div class="modal-footer">
                                                    <button class="btn" data-dismiss="modal" aria-hidden="true">Cancel</button>
                                                    <button class="btn btn-primary deleteSpecies" data-modal="#deleteRecord_${recId}" data-id="${recId}">Delete</button>
                                                </div>
                                            </div>
                                        </td>
                                    </g:if>
                                    <td>
                                        %{--[${fieldValue(bean: result, field: "itemOrder")}] --}%
                                        ${fieldValue(bean: result, field: "rawScientificName")}
                                        <g:if test="${result.guid == null}">
                                            <br/>(unmatched - try <a href="http://google.com/search?q=${fieldValue(bean: result, field: "rawScientificName").trim()}" target="google" clas="btn btn-primary btn-mini">Google</a>,
                                            <a href="http://biocache.ala.org.au/occurrences/search?q=${fieldValue(bean: result, field: "rawScientificName").trim()}" target="biocache" clas="btn btn-success btn-mini">Biocache</a>)
                                        </g:if>
                                    </td>
                                    <td><a href="${bieUrl}/species/${result.guid}" title="${bieTitle}">${bieSpecies?.get(2)}</a></td>
                                    <td id="img_${result.guid}"><a href="${bieUrl}/species/${result.guid}" title="${bieTitle}"><img src="${bieSpecies?.get(0)}" style="max-height:50px;max-width:100px;"/></a></td>
                                    <td>${bieSpecies?.get(3)}</td>
                                    <td id="cn_${result.guid}">${bieSpecies?.get(1)}</td>
                                    <g:each in="${keys}" var="key">
                                        <g:set var="kvp" value="${result.kvpValues.find {it.key == key}}" />
                                        <td>${kvp?.vocabValue?:kvp?.value?.trimLength(20)}</td>
                                    </g:each>
                                %{--<p>--}%
                                %{--${result.guid} ${result.rawScientificName}--}%
                                %{--</p>--}%
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </div>

                    <g:if test="${params.max<totalCount}">
                        <div class="searchWidgets">
                            Items per page:
                            <select id="maxItems" class="input-mini" onchange="reloadWithMax(this)">
                                <g:each in="${[10,25,50,100]}" var="max">
                                    <option ${(params.max == max)?'selected="selected"':''}>${max}</option>
                                </g:each>
                            </select>
                        </div>

                        <div class="pagination listPagination" id="searchNavBar">
                            <g:if test="${params.fq}">
                                <g:paginate total="${totalCount}" action="list" id="${params.id}" params="${[fq: params.fq]}"/>
                            </g:if>
                            <g:else>
                                <g:paginate total="${totalCount}" action="list" id="${params.id}" />
                            </g:else>
                        </div>
                    </g:if>
                </section>
            </div> <!-- tabs-panes-noborder -->
        </div> <!-- .span9 -->
        %{--</div> <!-- results -->--}%
    </div>
</div> <!-- content div -->
%{--<script type="text/javascript">--}%
    %{--function loadMultiFacets(facetName, displayName) {--}%
        %{--console.log(facetName, displayName)--}%
        %{--console.log("#div"+facetName,$("#div"+facetName),$("#div"+facetName).innerHTML)--}%
        %{--$("div#dynamic").innerHTML=$("#div"+facetName).innerHTML;--}%
    %{--}--}%
%{--</script>--}%
</body>
</html>