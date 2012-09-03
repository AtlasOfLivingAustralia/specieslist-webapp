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
    <gui:resources components="['dialog']"/>
    <meta name="layout" content="ala2"/>
    <title>Species List</title>
    <style type="text/css">
        #speciesList {display: none;}
    </style>
</head>

<body class="yui-skin-sam species">
<script type="text/javascript">
    window.onload=init
    function init(){
        if(document.getElementById("speciesList") != null)
            document.getElementById("speciesList").style.display = "block";
    }
</script>
<div id="content">
    <header id="page-header">
        <div class="inner">
            <nav id="breadcrumb">
                <ol>
                    <li><a href="http://www.ala.org.au">Home</a></li>
                    <li><a href="${request.contextPath}/public/speciesLists">All Species Lists</a></li>
                    <li class="last">${request.getUserPrincipal()?.attributes?.firstname} ${request.getUserPrincipal()?.attributes?.lastname} Species Lists</li>
                </ol>
            </nav>

            <hgroup class="leftfloat">
                <h1>Species Lists</h1>
            </hgroup>
            <div class="rightfloat">
                <a class="button orange" title="Add Species List" href="${request.contextPath}/speciesList/upload">Add Species List</a>
            </div>
        </div><!--inner-->
    </header>

    <div class="inner">
        <div id="section" class="col-wide">

            <g:if test="${lists && total > 0}">
                <p>Below is a listing of species lists that you have provided. You can use these lists to work with parts of the Atlas. Click on the "delete" button next a list to remove it from the Atlas.</p>
                <g:render template="/speciesList"/>
            </g:if>
            <g:else>
                <p>You do not have any available species lists.</p>
            </g:else>
        </div>
    </div>
</div> <!--content-->
</body>
</html>