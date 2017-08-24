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
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/public/speciesLists,Species lists"/>
    <meta name="breadcrumb" content="My species lists"/>
    <title>My Species lists | ${grailsApplication.config.skin.orgNameLong}</title>
    <style type="text/css">
    #speciesList {
        display: none;
    }
    </style>
    <asset:stylesheet src="fancybox.css"/>
</head>

<body class="yui-skin-sam nav-species">
<asset:script type="text/javascript">
    window.onload=init
    function init(){
        if(document.getElementById("speciesList") != null)
            document.getElementById("speciesList").style.display = "block";
    }
</asset:script>
<div id="content" class="row">
    <div class="col-md-12">
        <div class="row">
            <div class="col-md-8">
                <h1 class="subject-subtitle">My species lists</h1>
            </div>

            <div class="col-md-4">
                <g:link controller="speciesList" action="upload" class="btn btn-ala pull-right"
                        title="Add Species List">Upload a list</g:link>
            </div>
        </div><!--inner-->

        <div class="inner">
            <g:if test="${lists && total > 0}">
                <p>Below is a listing of species lists that you have provided. You can use these lists to work with parts of the Atlas.
                Click on the "delete" button next to a list to remove it from the Atlas.</p>
                <g:render template="/speciesList"/>
            </g:if>
            <g:else>
                <p>You do not have any available species lists.</p>
            </g:else>
        </div>
    </div>
</div> <!--content-->
<asset:javascript src="fancybox.js"/>
</body>
</html>