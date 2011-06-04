<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@
        include file="/common/taglibs.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta name="pageName" content="species"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>${param['q']} | Image Search | Atlas of Living Australia</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/screen.css" type="text/css"
          media="screen" charset="utf-8"/>
    <link type="text/css" media="screen" rel="stylesheet"
          href="${pageContext.request.contextPath}/static/css/colorbox.css"/>
    <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery.colorbox-min.js"></script>
    <script type="text/javascript">

        /*
         * OnLoad equivilent in JQuery
         */
        $(document).ready(function() {

            <c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '1024'}"><c:set var="noOfColumns" value="4"/></c:if>
            <c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '1440'}"><c:set var="noOfColumns" value="5"/></c:if>
            <c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '1680'}"><c:set var="noOfColumns" value="7"/></c:if>
            <c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '1900'}"><c:set var="noOfColumns" value="7"/></c:if>
            <c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '2200'}"><c:set var="noOfColumns" value="9"/></c:if>
            <c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '2500'}"><c:set var="noOfColumns" value="11"/></c:if>
            <c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '2900'}"><c:set var="noOfColumns" value="13"/></c:if>
            <c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '3201'}"><c:set var="noOfColumns" value="15"/></c:if>
            <c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '3500'}"><c:set var="noOfColumns" value="17"/></c:if>

            var screenWidth = 1024;
            if ($(window).width() >= 800 && $(window).width() < 1024) {
                screenWidth = 800;
            }
            if ($(window).width() >= 1024 && $(window).width() < 1280) {
                screenWidth = 1024;
            }
            if ($(window).width() >= 1280 && $(window).width() < 1440) {
                screenWidth = 1440;
            }
            if ($(window).width() >= 1440 && $(window).width() < 1680) {
                screenWidth = 1680;
            }
            if ($(window).width() >= 1680 && $(window).width() < 2201) {
                screenWidth = 1900; //alert("1280");
            }
            if ($(window).width() >= 2201 && $(window).width() < 2501) {
                screenWidth = 2200; //alert("1280");
            }
            if ($(window).width() >= 2501 && $(window).width() < 2901) {
                screenWidth = 2500; //alert("1280");
            }
            if ($(window).width() >= 2901 && $(window).width() < 3201) {
                screenWidth = 2900; //alert("1280");
            }
            if ($(window).width() >= 3201 && $(window).width() < 3501) {
                screenWidth = 3200; //alert("1280");
            }
            if ($(window).width() >= 3501) {
                screenWidth = 3500; //alert("1280");
            }

            //is there a screen res variable
            var screenWidthRequestParam = "${param['screenWidth']}";
            var queryString = "${pageContext.request.queryString}";
            if (screenWidthRequestParam == "") {
                if (queryString == "") {
                    window.location.replace(window.location.href + "?screenWidth=" + screenWidth);
                } else {
                    window.location.replace(window.location.href + "&screenWidth=" + screenWidth);
                }
            } else if (screenWidthRequestParam != screenWidth) {
                var url = window.location.href.replace("screenWidth=" + screenWidthRequestParam, "screenWidth=" + screenWidth)
                window.location.replace(url);
            }

            // Gallery image popups using ColorBox
            $("a.thumbImage").colorbox({
                        title: function() {
                            return "";
                        },
                        opacity: 0.5,
                        height: "600px",
                        width: "850px",
                        preloading: false,
                        'easingIn'   : 'easeOutQuad',
                        'easingOut'  : 'easeInQuad',
                        onComplete: function() {
                            //$("#cboxTitle").html(""); // Clear default title div
                            //var index = $(this).attr('id').replace("thumb",""); // get the imdex of this image
                            //var titleHtml = $("div#thumbDiv"+index).html(); // use index to load meta data
                            //$("<div id='titleText'>DAVE TITLE</div>").insertAfter("#cboxPhoto");
                            //$("div#titleText").css("padding-top","8px");
                            //$.fn.colorbox({height:"800px", width:"800px"})
                        }
                    });

        });

    </script>
</head>
<body>
<table>
    <tr>
        <td style="width:80px; padding-left:10px;">

            <h1><c:set var="baseUrl"
                       value="${pageContext.request.contextPath}/images/search/?${pageContext.request.queryString}"/></h1>

            <div id="facets" style="text-align:left;">
                <h2>Refine Results</h2>

                <h3>State/Territory</h3>
                <c:if test="${not empty param['state']}">
                    >> Selected state: ${param['state']}
                </c:if>
                <c:if test="${empty param['state']}">
                    <ul>
                        <li><a href="${baseUrl}&state=Australian Capital Territory">ACT</a></li>
                        <li><a href="${baseUrl}&state=New South Wales">NSW</a></li>
                        <li><a href="${baseUrl}&state=Northern Territory">NT</a></li>
                        <li><a href="${baseUrl}&state=Queensland">QLD</a></li>
                        <li><a href="${baseUrl}&state=South Australia">SA</a></li>
                        <li><a href="${baseUrl}&state=Tasmania">TAS</a></li>
                        <li><a href="${baseUrl}&state=Victoria">VIC</a></li>
                        <li><a href="${baseUrl}&state=Western Australia">WA</a></li>
                    </ul>
                </c:if>

                <h3>Species group</h3>
                <ul>
                    <li><a href="${baseUrl}&speciesGroup=Birds">Birds</a></li>
                    <li><a href="${baseUrl}&speciesGroup=Insects">Insects</a></li>
                    <li><a href="${baseUrl}&speciesGroup=Reptiles">Reptiles</a></li>
                    <li><a href="${baseUrl}&speciesGroup=Frogs">Frogs</a></li>
                    <li><a href="${baseUrl}&speciesGroup=Fish">Fish</a></li>
                    <li><a href="${baseUrl}&speciesGroup=Mammals">Mammals</a></li>
                    <li><a href="${baseUrl}&speciesGroup=Flowering plants">Flowering plants</a></li>
                    <li><a href="${baseUrl}&speciesGroup=Plants">Plants</a></li>
                </ul>

                <h3>Rank</h3>
                <c:if test="${not empty param['rank']}">
                    >> Selected state: ${param['rank']}
                </c:if>
                <c:if test="${empty param['rank']}">
                    <ul>
                        <li><a href="${baseUrl}&rank=genus">Genus</a></li>
                        <li><a href="${baseUrl}&rank=species">Species</a></li>
                        <li><a href="${baseUrl}&rank=subspecies">Subspecies</a></li>
                    </ul>
                </c:if>


            </div>


        </td>


        <td style="text-align: left;">

            <h3><strong>${results.totalRecords}</strong> taxa returned with images
                <c:if test="${not empty param['q']} or not empty ${param['fq']}">
                    for
                    <strong>${param['q']} ${param['fq']}</strong>
                </c:if>
            </h3>


            <table>
                <tr>
                    <c:set var="noOfColumns" value="4"/>

                    <c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '1024'}"><c:set
                            var="noOfColumns" value="4"/></c:if>
                    <c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '1440'}"><c:set
                            var="noOfColumns" value="5"/></c:if>
                    <c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '1680'}"><c:set
                            var="noOfColumns" value="7"/></c:if>
                    <c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '1900'}"><c:set
                            var="noOfColumns" value="7"/></c:if>
                    <c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '2200'}"><c:set
                            var="noOfColumns" value="9"/></c:if>
                    <c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '2500'}"><c:set
                            var="noOfColumns" value="11"/></c:if>
                    <c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '2900'}"><c:set
                            var="noOfColumns" value="13"/></c:if>
                    <c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '3200'}"><c:set
                            var="noOfColumns" value="15"/></c:if>
                    <c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '3500'}"><c:set
                            var="noOfColumns" value="17"/></c:if>

                    <c:forEach items="${results.results}" var="searchTaxon" varStatus="status">

                    <c:if test="${status.index % noOfColumns == 0 && status.index>0}">
                </tr>
                <tr>
                    </c:if>
                    <td>
                        <a class="thumbImage"
                           href="${pageContext.request.contextPath}/images/infoBox?q=${searchTaxon.guid}">
                            <img src="${fn:replace(searchTaxon.thumbnail, 'thumbnail', 'smallRaw' )}" width="175"
                                 style="border: 1px solid gray;"/>
                        </a>
                        <br/>
                        <c:if test="${not empty searchTaxon.commonNameSingle}">${searchTaxon.commonNameSingle}
                            <br/></c:if>
                        <alatag:formatSciName name="${searchTaxon.nameComplete}" rankId="${searchTaxon.rankId}"/>
                    </td>
                    </c:forEach>
                </tr>
            </table>
        </td>
    </tr>
</table>
</body>
</html>
