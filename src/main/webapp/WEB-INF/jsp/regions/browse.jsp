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
    <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/picnet.table.filter.min.js"></script>
    <script src="${pageContext.request.contextPath}/static/flash/swfobject.js" language="javascript"></script>
    <script src="${pageContext.request.contextPath}/static/flash/history/history.js" language="javascript"></script>
    <title>Regions | Atlas of Living Australia</title>
    <script type="text/javascript">
        /*
         * Body OnLoad equivilent in JQuery
         */
        $(document).ready(function() {
            // Tool tip for headings
            //$('#regionalisations h3 span').qtip({ style: { name: 'cream', tip: true } });
            
            // Make regionBrowser flash map appear in popup div
            $("a#showRegionBrowser").fancybox({
                'hideOnContentClick' : false,
                'titleShow' : false,
                'autoDimensions' : false,
                'width' : ${width + 8},
                'height' : ${height + 8}
            });
            
            // TableFilter plugin
            var options = {
                additionalFilterTriggers: [$('#quickfind')],
                clearFiltersControls: [$('#cleanfilters')]
            };
            $('table#lga').tableFilter(options);
            $('table#ibra').tableFilter(options);
            $('table#imcra').tableFilter(options);
            
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
        <h1>Regions of Australia</h1>
    </div><!--close header-->
    <div id="column-one" class="full-width">
        <div class="section">
            <h2>States &amp; Territories</h2>
            <div style="width: 600px;">
                <div id="regionBrowserLinks" style="float: right; display: none;">
                    <a href="#regionBrowserDiv" id="showRegionBrowser" class="plain" title="View interactive map of Australian Regions">
                        <img src="${pageContext.request.contextPath}/static/images/regionBrowserThumbnail.jpg" alt="region browser thumbnail">
                        <br/>
                        View interactive map of Australian Regions
                    </a>
                </div>
                <div id="states" style="padding-top: 10px;">
                    <ul>
                        <c:forEach items="${states}" var="region">
                            <li><a href="${pageContext.request.contextPath}/regions/${region.guid}">${region.name}</a></li>           	
                        </c:forEach>
                    </ul>
                </div>
            </div>
            <div style="clear: both">&nbsp;</div>
            <h2>Other Regionalisations of Australia</h2>
            <div id="quickFindDiv">
                Quick Find: <input type="text" id="quickfind" size="40"/>
                &nbsp;&nbsp;<a id="cleanfilters" href="#">Clear Filters</a>
            </div>
            <table style="width:100%" id="regionalisations">
                <thead>
                    <tr>
                        <th><h3><span title="Local Government Areas">Local Government Areas (LGA)</span></h3></th>
                        <th><h3><span title="Interim Biogeographic Regionalisation of Australia">Interim Biogeographic Regionalisation of Australia (IBRA)</span></h3></th>
                        <th><h3><span title="Integrated Marine and Coastal Regionalisation of Australia">Integrated Marine and Coastal Regionalisation of Australia (IMCRA)</span></h3></th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td style="width:300px;">
                            <table width="100%" border="0" id="lga">
                                <c:forEach items="${lga}" var="region">
                                    <tr>
                                        <td><a href="${pageContext.request.contextPath}/regions/${region.guid}">${region.name}</a></td>
                                    </tr>
                                </c:forEach>
                            </table>
                        </td>
                        <td style="width:300px;">
                            <table width="100%" border="0" id="ibra">
                                <c:forEach items="${ibra}" var="region">
                                    <tr>
                                        <td><a href="${pageContext.request.contextPath}/regions/${region.guid}">${region.name}</a></td>
                                    </tr>
                                </c:forEach>
                            </table>
                        </td>
                        <td style="width:300px;">
                            <table width="100%" border="0" id="imcra">
                                <c:forEach items="${imcra}" var="region">
                                    <tr>
                                        <td><a href="${pageContext.request.contextPath}/regions/${region.guid}">${region.name}</a></td>
                                    </tr>
                                </c:forEach>
                            </table>
                        </td>
                    </tr>
                </tbody>
            </table>
                
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
