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
        /**
         * OnLoad equavilent in JQuery
         */
        $(document).ready(function() {

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
            $("a.thumbImage").colorbox({
                title: function() {
                    return "";
                },
                opacity: 0.5,
                height: "420px",
                width: "700px",
                preloading: false,
                'easingIn'   : 'easeOutQuad',
                'easingOut'  : 'easeInQuad',
                onComplete: function() {
                }
            });
        });

        var currentPage = 1;

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

<table style="width:100%; cell-padding:0; border:0px;">
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

</body>
</html>
