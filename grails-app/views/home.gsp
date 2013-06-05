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

<%--
  Alternative index page (replaces generated Grails version)
  User: nick
  Date: 25/06/12
  Time: 10:01 AM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />
    <title>Biodiversity Information Explorer | Atlas of Living Australia</title>
    <style type="text/css" media="screen">
        #inpage_search {
            float:left;
            position:relative;
            width:503px;
            height:33px;
            margin:9px 0 40px 17px;
            background-color:#fff;
            border:1px solid #d1d1d1;
            -moz-border-radius: 7px; /* FF1+ */
            -webkit-border-radius: 7px; /* Saf3+, Chrome */
            border-radius: 7px; /* Opera 10.5, IE 9 */
            overflow:hidden;
            z-index:1;
        }

        #inpage_search {
            margin:0 250px 5px 0;
        }
        form#search-form label,
        form#search-inpage label,
        form#search-portal label {
            display:none;
        }

        form#search-form input.empty, form#search-form input.filled,
        form#search-inpage input.empty, form#search-inpage input.filled,
        form#search-portal input.empty, form#search-portal input.filled  {
            border-color:transparent;
            width:435px;
            height:25px;
            margin:2px 0 2px 5px;
            outline:none;
            font: bold 1.3em/25px Helvetica, sans-serif;
            color:#df4a21; /*#666;*/
            float:left;
        }
        form#search-form .search-button-wrapper,
        form#search-inpage .search-button-wrapper,
        form#search-portal .search-button-wrapper {
            background: #d1d1d1 url(${resource(dir: 'images', file: 'button_search.png')}) 3px 3px no-repeat;
            width:34px;
            height:33px;
            display:block;
            float:right;
            -moz-border-radius:0 7px 7px 0; /* FF1+ */
            -webkit-border-radius:0 7px 7px 0; /* Saf3+, Chrome */
            border-radius: 0 7px 7px 0; /* Opera 10.5, IE 9 */
        }
        form#search-form .search-button,
        form#search-inpage .search-button,
        form#search-portal .search-button{
            background:transparent;
            border:none;
            outline:none;
            width:34px;
            padding:33px 0 0 0;
            cursor:pointer;
        }
        form#search-form .search-button-wrapper:hover,
        form#search-inpage .search-button-wrapper:hover,
        form#search-portal .search-button-wrapper:hover{
            /*background-position: 0 -33px;*/
            background-color: grey;
        }

        form#search-form input.filled,
        form#log-in input.filled,
        form#search-inpage input.filled,
        form#search-portal input.filled {
            color:#000;
        }
    </style>
</head>
<body class="species">
    <header id="page-header">
        <div class="inner row-fluid">
            <nav id="breadcrumb" class="span12">
                <ol class="breadcrumb">
                    <li><a href="${alaUrl}">Home</a> <span class=" icon icon-arrow-right"></span></li>
                    <li><a href="${alaUrl}/australias-species/">Australia&#39;s species</a> <span class=" icon icon-arrow-right"></span></li>
                    <li class="active">Biodiversity Information Explorer (BIE)</li>
                </ol>
            </nav>
            <hgroup>
                <h1>Biodiversity Information Explorer</h1>
            </hgroup>
        </div>
    </header>
    <div class="inner">
        <div class="section">
            <p>
                Welcome to the Atlas of Living Australia <strong>Biodiversity Information Explorer (BIE)</strong><br/>
                For a listing of webservices for the BIE, <a href="${grailsApplication.config.bie.baseURL}/ws"><strong>click here</strong></a>.</p>
            <h2 style="">Free text search for taxa</h2>
            <div class="section" style="margin-bottom:60px">
                <div>
                    <form id="search-inpage" action="search" method="get" name="search-form">
                        <div class="input-append">
                            <input id="search" class="span4" name="q" type="text" placeholder="Search the Atlas" autocomplete="off">
                            <input type="submit" class="btn" alt="Search" value="Search">
                        </div>
                        %{--<label for="search">Search</label>--}%
                        %{--<input type="text" class="filled ac_input" id="search" name="q" placeholder="Search the Atlas" autocomplete="off">--}%
                        %{--<span class="search-button-wrapper"><input type="submit" class="search-button" alt="Search" value="Search"></span>--}%
                    </form>
                </div>
            </div>
        </div>
    </div><!--end .inner-->
</body>
</html>