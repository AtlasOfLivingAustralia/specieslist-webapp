<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>
<c:set var="googleKey" scope="request"><ala:propertyLoader bundle="bie-webapp" property="googleKey"/></c:set>
<c:set var="flashDir" value="${pageContext.request.contextPath}/static/flash"/>
<c:set var="assets" value="${flashDir}/Assets"/>
<c:set var="width" value="800"/>
<c:set var="height" value="600"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta name="pageName" content="geoRegion"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery.qtip-1.0.0.min.js"></script>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/static/flash/history/history.css" />
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/static/js/jquery-fancybox/jquery.fancybox-1.3.4.css" media="screen" />
    <link rel="stylesheet" type="text/css" href="${initParam.centralServer}/wp-content/themes/ala/css/bie.css" media="screen" />
    <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery-fancybox/jquery.fancybox-1.3.4.pack.js"></script>
    <script src="${pageContext.request.contextPath}/static/flash/swfobject.js" language="javascript"></script>
    <script src="${pageContext.request.contextPath}/static/flash/history/history.js" language="javascript"></script>
    <title>Regions | Atlas of Living Australia</title>
    <script type="text/javascript">
        /*
         * Body OnLoad equivilent in JQuery
         */
        $(document).ready(function() {
            // Tool tip for headings
            $('#regionalisations h2 span').qtip({ style: { name: 'cream', tip: true } });
            
            // Make regionBrowser flash map appear in popup div
            $("a#showRegionBrowser").fancybox({
                'hideOnContentClick' : false,
                'titleShow' : false,
                'autoDimensions' : false,
                'width' : ${width + 8},
                'height' : ${height + 8}
            });
            
            //if (swfobject.getFlashPlayerVersion().major >= 9) {
                // Flash is detected
                //alert("Flash is right version: " + swfobject.getFlashPlayerVersion().major);
                
            //} else {
                // No Flash
                //alert("Flash not present or not right version");
                //$('#regionBrowserLink').hide();
            //}
            
        }); // End document.ready
        
        
        var flashvars = {};
        var params = {
            allowFullScreen: "true",
            flashvars: "googleKey=${googleKey}&flashDir=${flashDir}&shpFile=${assets}/states_ordered.shp.zip&dbfFile=${assets}/states_ordered.dbf.zip&baseRedirectUrl=&regionType=states_ordered&baseRedirectUrl=http%3A%2F%2Fbie.ala.org.au%2Fregions%2Faus_states%2F"
        };

        swfobject.embedSWF("${flashDir}/RegionBrowser.swf", "regionBrowserDiv", "${width}", "${height}", 
                "9.0.0","${flashDir}/expressInstall.swf", flashvars, params, null, flashCheck);
        
        /**
         * Callback to report if Flash loaded
         */
        function flashCheck(e) {
            if (e.success) {
                // alert('flash embedded successfully: ' + e.success); 
                $('div#regionBrowserLinks').show();
            }
        }
    </script>
</head>
<body>
    <div id="header">
        <div id="breadcrumb">
            <a href="${initParam.centralServer}">Home</a>
            <a href="${initParam.centralServer}/explore">Explore</a>
            Regions
        </div>
        <h1>States &amp; Territories</h1>
    </div><!--close header-->
    <div id="column-one" class="full-width">
        <div class="section">
            <div style="width: 600px;">
                <div id="regionBrowserLinks" style="float: right; display: none;">
                    <a href="#regionBrowserDiv" id="showRegionBrowser" class="plain" title="View interactive map of Australian Regions">
                        <img src="${pageContext.request.contextPath}/static/images/regionBrowserThumbnail.jpg" alt="region browser thumbnail">
                        <br/>
                        View interactive map of Australian Regions
                    </a>
                </div>
                <div id="states">
                    <ul>
                        <c:forEach items="${states}" var="region">
                            <li><a href="${pageContext.request.contextPath}/regions/${region.guid}">${region.name}</a></li>           	
                        </c:forEach>
                    </ul>
                </div>
            </div>
            <div style="clear: both">&nbsp;</div>
            <h1>Other Regionalisations of Australia</h1>
            <table style="width:100%" id="regionalisations">
                <tr>
                    <td style="width:300px;">
                        <h2><span title="Local Government Areas">LGA</span></h2>
                        <ul>
                            <c:forEach items="${lga}" var="region">
                                <li><a href="${pageContext.request.contextPath}/regions/${region.guid}">${region.name}</a></li>           	
                            </c:forEach>
                        </ul>
                    </td>
                    <td style="width:300px;">
                        <h2><span title="Interim Biogeographic Regionalisation of Australia">IBRA</span></h2>
                        <ul>
                            <c:forEach items="${ibra}" var="region">
                                <li><a href="${pageContext.request.contextPath}/regions/${region.guid}">${region.name}</a></li>           	
                            </c:forEach>
                        </ul>
                    </td>
                    <td style="width:300px;">
                        <h2><span title="Integrated Marine and Coastal Regionalisation of Australia">IMCRA</span></h2>
                        <ul>
                            <c:forEach items="${imcra}" var="region">
                                <li><a href="${pageContext.request.contextPath}/regions/${region.guid}">${region.name}</a></li>           	
                            </c:forEach>
                        </ul>
                    </td>
                </tr>
            </table>
            <div style="display: none;">
                <div id="regionBrowserDiv" style="width: ${width}; height: ${height}">
                    Requires Flash
                </div>
            </div>
        </div>
    </div>
</body>
</html>
