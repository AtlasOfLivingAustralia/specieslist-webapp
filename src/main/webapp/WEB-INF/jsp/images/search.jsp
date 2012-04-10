<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta name="pageName" content="species"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Image search | Atlas of Living Australia</title>
    <link rel="stylesheet" href="${initParam.centralServer}/wp-content/themes/ala/css/bie.css" type="text/css" media="screen" charset="utf-8"/>    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/screen.css" type="text/css"
          media="screen" charset="utf-8"/>
    <link type="text/css" media="screen" rel="stylesheet"
          href="${pageContext.request.contextPath}/static/css/colorbox.css"/>
    <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery.colorbox-min.js"></script>
    <script type="text/javascript">
	    var prevPage = 0;
	    var currentPage = 1;
    	var lastPage=${results.totalRecords/pageSize};

        function imageLoad() {
            $('#divPostsLoader').html('<img src="${pageContext.request.contextPath}/static/images/ajax-loader.gif">'); 

            //send a query to server side to present new content 
            $.ajax({ 
                type: "POST", 
                url: "./showSpecies.json?taxonRank=${param['taxonRank']}&scientificName=${param['scientificName']}&start=" + (currentPage * ${pageSize}) + "&pageSize=" + ${pageSize}, 
                contentType: "application/json; charset=utf-8", 
                dataType: "json", 
                success: function (data) { 
                    if (data != "") {                    	
                    	//addRow(data);
                    	addTable(data);
                    	currentPage = currentPage + 1;
                    } 
                    $('#divPostsLoader').empty(); 
                } 

            }) 
        }; 

<%--        
    	function addRow(data) {
    		var td1 = '<td style="width:${maxWidthImages}px">';
    		var td2 = '</td>';
    		var tr = '';
    		var href2 = '</a>';
    		var images = ''
    		var j = 0;
    		for(i = 0; i < data.results.length; i++){  
    			var href1 = '<a class="thumbImage" href="${pageContext.request.contextPath}/image-search/infoBox?q=' +  data.results[i].guid + '">';
    			var imageUrl = data.results[i].thumbnail;
    			if(imageUrl != null){
    				imageUrl = imageUrl.replace('thumbnail', 'smallRaw');
    			}
    			
    			var image = href1 + '<img src=' + imageUrl + ' class="searchImage" style="max-width:${maxWidthImages}px; max-height:150px;"/>' + href2 + '<br/>';
    			var name = data.results[i].commonNameSingle;
    			if(name != '' && name != null){
    				image = image + name + '<br/>';
    			}
    			
    			if(data.results[i].nameComplete != null && data.results[i].nameComplete != ''){
    				image += '<i>' + data.results[i].nameComplete + '</i>';
    			}
    			images = images + td1 + image + td2;
    			
    			j = i + 1;
				if((j % ${noOfColumns} == 0 && i > 0) || (j == data.results.length)){
    				tr = '<tr>' + images + '</tr>';
        			$('#imageTable0 tr:last').after(tr);
        			images = '';
    			}
    		}
    		// reload cbox handler
    		loadCbox();
    	}    
--%>

       	function addTable(data) {
       		var tbl1 = '<table id="imageTable' + currentPage + '" style="width:100%; cell-padding:0; border:0px;">';
       		var tbl2 = '</table>';
    		var td1 = '<td style="width:${maxWidthImages}px">';
    		var td2 = '</td>';
    		var tr = '';
    		var href2 = '</a>';
    		var images = '';
    		var tbl = '';
    		var j = 0;
    		for(i = 0; i < data.results.length; i++){  
    			var href1 = '<a class="thumbImage" href="${pageContext.request.contextPath}/image-search/infoBox?q=' +  data.results[i].guid + '">';
    			var imageUrl = data.results[i].thumbnail;
    			if(imageUrl != null){
    				imageUrl = imageUrl.replace('thumbnail', 'smallRaw');
    			}
    			
    			var image = href1 + '<img src=' + imageUrl + ' class="searchImage" style="max-width:${maxWidthImages}px; max-height:150px;"/>' + href2 + '<br/>';
    			var name = data.results[i].commonNameSingle;
    			if(name != '' && name != null){
    				image = image + name + '<br/>';
    			}
    			
    			if(data.results[i].nameComplete != null && data.results[i].nameComplete != ''){
    				image += '<i>' + data.results[i].nameComplete + '</i>';
    			}
    			images = images + td1 + image + td2;
    			
    			j = i + 1;
				if((j % ${noOfColumns} == 0 && i > 0) || (j == data.results.length)){
    				tr = '<tr>' + images + '</tr>';
        			//$('#imageTable:last').after(tr);
        			tbl = tbl + tr;
        			images = '';
    			}
    		}
    		$('#imageTable' + (currentPage - 1) + ':last').after(tbl1 + tbl + tbl2);
    		// reload cbox handler
    		loadCbox();
    	}    
    	
    	function loadCbox() {
    		// Gallery image popups using ColorBox
            $("a.thumbImage").colorbox({
                title: function() {
                    return "";
                },
                opacity: 0.5,
                height: "600px",
                width: "700px",
                preloading: false,
                'easingIn'   : 'easeOutQuad',
                'easingOut'  : 'easeInQuad',
                onComplete: function() {
                }
            });
    	}
    	
        /**
         * OnLoad equavilent in JQuery
         */
        $(document).ready(function() {
        	currentPage = 1;
        	lastPage=${results.totalRecords/pageSize};
        	
            //alert('Starting - checing screen res');
            //is there a screen res variable
            var screenWidth = $(window).width();
            var screenWidthRequestParam = "${param['sw']}";
            var queryString = "${pageContext.request.queryString}";
            if (screenWidthRequestParam.length == 0) {
                if (queryString.length == 0) {
                    window.location.replace(window.location.href + "?sw=" + screenWidth);
                } else {
                    window.location.replace(window.location.href + "&sw=" + screenWidth);
                }
            } else if (screenWidthRequestParam != screenWidth) {
                var url = window.location.href.replace("sw=" + screenWidthRequestParam, "sw=" + screenWidth)
                window.location.replace(url);
            } else {
                //alert('do nothing');
            }

            // Gallery image popups using ColorBox
            // init loadup first page images handler.
            loadCbox();              
        });
               
        //When scroll down, the scroller is at the bottom with the function below and fire the load function 
        $(window).scroll(function () { 
            if ($(window).scrollTop() == $(document).height() - $(window).height()) { 
            	//console.log("**** currentPage !!!! " + currentPage + ', lastPage: ' + lastPage);
            	if(lastPage > currentPage){            		
            		// prevent double request
            		if($('#divPostsLoader').html() == ''){
            			//console.log("**** imageLoad !!!! " + currentPage);
                    	imageLoad();
            		}
                }
             } 
        });             

        $(window).unload(function() {
        	scrollTo(0,0);
        }); 

<c:if test="${(fn:contains(header['User-Agent'],'iPad') || fn:contains(header['User-Agent'],'Android')) && results.totalRecords/pageSize > 1}">        
        function geNext(){
        	if(lastPage > currentPage){            		
        		// prevent double request
        		if($('#divPostsLoader').html() == ''){
        			//console.log("**** imageLoad !!!! " + currentPage);
                	imageLoad();
        		}
            }
           	if(lastPage < currentPage + 1){
        		$('#moreButton').hide(); 
        	}        	      	
        }
</c:if>          
    </script>
    <style type="text/css">
        .searchImage {
            background-color: white;
            /*
            -moz-border-radius: 7px 7px 7px 7px;
            -webkit-border-radius: 7px 7px 7px 7px;
            border-radius: 7px 7px 7px 7px;
            */
        }

        #loadMoreLink {
            border: 1px solid gray;
            background-color: #FCFCFC;
            padding:20px;
            margin-bottom: 30px;
        }

        #headingBar  {
            margin-left:0px;
            padding-left:10px;
        }

        #headingBar h1 {
            text-align: left;
        }

        #headingBar h1 a { text-decoration: none;}

    </style>
</head>
<body>


<div id="headingBar">
    <h1>
        Images of ${results.totalRecords} species from ${param['taxonRank']}
        <a href="${pageContext.request.contextPath}/species/${param['scientificName']}">
        ${param['scientificName']}</a>
     </h1>
</div>

<div id="imageResults">

<table id="imageTable0" style="width:100%; cell-padding:0; border:0px;">
    <tr>
        <c:forEach items="${results.results}" var="searchTaxon" varStatus="status">
        <c:if test="${status.index % noOfColumns == 0 && status.index>0}"></tr>
    <tr></c:if>
        <td style="width:${maxWidthImages}px">
            <a class="thumbImage"
               href="${pageContext.request.contextPath}/image-search/infoBox?q=${searchTaxon.guid}">
                <img src="${fn:replace(searchTaxon.thumbnail, 'thumbnail', 'smallRaw' )}"
                     class="searchImage"
                     style="max-width:${maxWidthImages}px; max-height:150px;"/>
            </a>
            <br/>
            <c:if test="${not empty searchTaxon.commonNameSingle}">${searchTaxon.commonNameSingle}<br/></c:if>
            <alatag:formatSciName name="${searchTaxon.nameComplete}" rankId="${searchTaxon.rankId}"/>
        </td>
        </c:forEach>

        <!-- padding -->
        <c:forEach begin="0" end="${fn:length(results.results) % noOfColumns}">
            <td style="width:${maxWidthImages}px">&nbsp;</td>
        </c:forEach>
    </tr>
</table>

</div>

<div style="margin-left:auto;margin-right:auto; width:120px;"  id="divPostsLoader"></div> 

<c:if test="${(fn:contains(header['User-Agent'],'iPad') || fn:contains(header['User-Agent'],'Android')) && results.totalRecords/pageSize > 1}">

<div style="margin-left:auto;margin-right:auto; width:120px;"  id="moreLink"> 
<table style="margin-left:auto;margin-right:auto;width:100%; cell-padding:0; border:0px;">
	<tr>
		<td>
			<input id="moreButton" style="height: 40px; width: 100px" type="button" value="More" onClick="javascript:geNext();return false;">
		</td>
	</tr>
</table> 
</div>
</c:if>

<!-- 
<table style="width:100%; cell-padding:0; border:0px;">
	<tr>
		<td>
			<c:if test="${not empty results && results.totalRecords > pageSize}">
				<div id="searchNavBar">
				    <alatag:imageNavigationLinks totalRecords="${results.totalRecords}" startIndex="${results.startIndex}"
				         lastPage="${results.totalRecords/pageSize}" pageSize="${pageSize}"/>
				</div>
			</c:if> 
		</td>
	</tr>
</table>
-->

</body>
</html>
