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
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Species lists | Atlas of Living Australia</title>
</head>
<body class="">
<div id="content" class="container">
    <header id="page-header">
        <div class="inner row-fluid" style="display: block;">
            <div id="breadcrumb" class="span12">
                <ol class="breadcrumb">
                    <li><a href="http://www.ala.org.au">Home</a> <span class="divider"><i class="fa fa-arrow-right"></i></span></li>
                    <li><a class="current" href="${request.contextPath}/admin/speciesLists">Species lists</a></li>
                </ol>
            </div>
        </div>
        <div class="row-fluid">
            <hgroup class="span8">
                <h1>Species lists</h1>
            </hgroup>
            <div class="span4 header-btns">
                <span class="pull-right">
                    <a class="btn btn-ala" title="Add Species List" href="${request.contextPath}/speciesList/upload">Upload a list</a>
                    <a class="btn btn-ala" title="My Lists" href="${request.contextPath}/speciesList/list">My Lists</a>
                </span>
            </div>
        </div><!--.row-fluid-->

    </header>
    <div class="inner row-fluid" id="public-specieslist">
        <g:if test="${flash.message}">
            <div class="message alert alert-info">
                <button type="button" class="close" onclick="$(this).parent().hide()">×</button>
                <b>Alert:</b> ${flash.message}
            </div>
        </g:if>
            <p>
                This tool allows you to upload a list of species, and work with that list within the Atlas.
                <br/>
                Click "Upload a list" to upload your own list of taxa.
            </p>
        <g:if test="${lists && total>0}">
            <p>
                Below is a listing of user provided species lists. You can use these lists to work
                with parts of the Atlas.
            </p>
            <form class="listSearchForm" >
                <div class="input-append" id="searchLists">
                    <input class="span4" id="appendedInputButton" name="q" type="text" value="${params.q}" placeholder="Search in list name, description or owner">
                    <button class="btn" type="submit">Search</button>
                </div>
            </form>
            <form class="listSearchForm" >
                <g:if test="${params.q}">
                %{--<input type="hidden" name="q" value=""/>--}%
                    <button class="btn btn-primary" type="submit">Clear search</button>
                </g:if>
            </form>
            <g:render template="/speciesList"/>
        </g:if>
        <g:elseif test="${params.q}">
            <form class="listSearchForm" >
                <p>No Species Lists found for: <b>${params.q}</b></p>
                <button class="btn btn-primary" type="submit">Clear search</button>
            </form>
        </g:elseif>
        <g:else>
            <p>There are no Species Lists available</p>
        </g:else>
    </div>
</div> <!-- content div -->
</body>
</html>