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
  Created by IntelliJ IDEA.
  User: nick
  Date: 12/06/12
  Time: 4:50 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <title>Image browser</title>
    <meta name="layout" content="main" />
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'colorbox.css')}" type="text/css" media="screen" />
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.colorbox-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.tools.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.inview.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.livequery.min.js')}"></script>
    <script type="text/javascript">
        /**
         * OnLoad equavilent in JQuery
         */
        $(document).ready(function() {
            //currentPage = 1;
            //lastPage = 0;
            imageLoad();

            // trigger more images to load when bottom of page comes "in view"
            $("#loadMoreTrigger").live("inview", function(event, visible, visiblePartX, visiblePartY) {
                if (visible) {
                    //console.log("currentPage", currentPage);
                    imageLoad();
                }

            });

            // tooltips for images using livequery
//            $("a.thumbImage").livequery(function() {
//                $(this).tooltip({
//                    effect: "slide",
//                    offset: [-15, 0],
//                    direction: "down",
//                    position: "bottom center"
//                });
//            });
        });

        var prevPage = 0;
        var currentPage = 0;
        var lastPage, noOfColumns;
        var pageSize = 20; // was: no of columns * 12 in bie code

        function imageLoad() {
            $('#divPostsLoader').html('<img src="${resource(dir: "images", file:"spinner.gif")}">');

            //send a query to server side to present new content
            $.ajax({
                type: "GET",
                url: "${grailsApplication.config.bie.baseURL}/image-search/showSpecies.json?taxonRank=${params['taxonRank']}&scientificName=${params['scientificName']}&start=" + (currentPage * pageSize) + "&pageSize=" + pageSize,
                contentType: "application/json; charset=utf-8",
                dataType: "jsonp",
                success: function (data) {
                    if (data) {
                        //addTable(data);
                        $("#totalImageCount").text(numberWithCommas(data.totalRecords));
                        addImages(data);
                        currentPage = currentPage + 1;
                        $('#divPostsLoader').empty();
                    }
                }
            })
        };

        function addImages(data) {
            $.each(data.results, function(i, el) {
                var scientificName = (el.nameComplete) ? "<i>" + el.nameComplete + "</i>" : "";
                var commonName = (el.commonNameSingle) ? el.commonNameSingle + "<br/> " : "";
                var imageUrl = el.thumbnail;
                var titleText = $("<div/>" + commonName.replace("<br/>"," - ") + scientificName).text();
                if (imageUrl) {
                    imageUrl = imageUrl.replace('thumbnail', 'smallRaw');
                }
                var content = '<div class="imgContainer"><a href="${grailsApplication.config.grails.serverURL}/species/' + el.guid;
                content +=  '" class="thumbImage" title="' + titleText + '">';
                content += '<img src="' + imageUrl + '" class="searchImage" style="max-height:150px;"/><br/>';
                content += commonName + scientificName + '</a></div>';
                $("#imageResults").append(content);
            });
            // add delay for trigger div
            $("#loadMoreTrigger").delay(500).show();
        }

        function numberWithCommas(x) {
            return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
        }

        function htmlDecode(input){
            var e = document.createElement('div');
            e.innerHTML = input;
            return e.childNodes.length === 0 ? "" : e.childNodes[0].nodeValue;
        }

    </script>
    <style type="text/css">
    .thumbImage {

    }

    #imageResults {
        margin-bottom: 30px;
    }

    .tooltip {
        display:none;
        background-color:#ffa;
        border:1px solid #cc9;
        padding:3px 6px;
        font-size:13px;
        text-align: center;
        -moz-box-shadow: 2px 2px 11px #666;
        -webkit-box-shadow: 2px 2px 11px #666;
    }

    hgroup h1 {
        text-align: left;
        margin-top: 10px;
    }

    hgroup h1 a {
        text-decoration: none;
    }

    .imgContainer {
        display: inline-block;
        margin-right: 8px;
        text-align: center;
        line-height: 1.3em;
        background-color: #DDD;
        /*color: #DDD;*/
        padding: 5px;
        margin-bottom: 8px;
    }
    .imgContainer a:link {
        text-decoration: none;
        /*color: #DDD;*/
    }

    </style>
</head>
<body class="fluid">
    <header id="page-header">
        <div class="inner">
            <nav id="breadcrumb">
                <ol>
                    <li><a href="${alaUrl}">Home</a></li>
                    <li><a href="${alaUrl}/australias-species/">Australia&#39;s species</a></li>
                    <li class="last">Image browser for ${msg}</li>
                </ol>
            </nav>
            <hgroup>
                <h1>Images of <b id="totalImageCount">...</b> species from ${params.taxonRank}:
                    <a href="${grailsApplication.config.grails.serverURL}/species/${params.scientificName}" title="More information on this ${params.taxonRank}">${params.scientificName}</a></h1>
            </hgroup>
        </div>
    </header>
    <div class="inner">
        <div id="imageResults">
            <!-- image objects get inserted here by JS -->
        </div>

        <div id="divPostsLoader" style="margin-left:auto;margin-right:auto; width:120px;"></div>

        <div id="loadMoreTrigger" style="display: none;"></div>
    </div>
</body>
</html>