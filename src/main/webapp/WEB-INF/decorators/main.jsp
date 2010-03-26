<%--
    Document   : main.jsp (sitemesh decorator file)
    Created on : 18/09/2009, 13:57
    Author     : dos009
--%>
<%@taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator" %><%@
    taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page" %><%@
    taglib prefix="spring" uri="http://www.springframework.org/tags" %><%@
    taglib prefix="form" uri="http://www.springframework.org/tags/form" %><%@
    taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
    <head>
        <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js" type="text/javascript"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery-autocomplete/jquery.autocomplete.js"></script>
        <link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/static/js/jquery-autocomplete/jquery.autocomplete.css" charset="utf-8">
        <title><decorator:title default="ALA Biodiversity Harvester" /></title>
        <decorator:head />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/screen.css" type="text/css" media="screen" charset="utf-8">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/print.css" type="text/css" media="print" charset="utf-8">
        <!--[if IE]><link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/lib/ie.css" type="text/css" media="screen, projection" /><![endif]-->
        <script type="text/javascript">
            $(document).ready(function() {
                $("#q").autocomplete({ url: "${pageContext.request.contextPath}/species/terms", matchSubset: 0, minChars: 3});
            });
        </script>
    </head>
    <body>
        <div id="page">
            <div id="header">
                <div id="menuSearch">
                    <form action="${pageContext.request.contextPath}/species/search" method="GET" autocomplete="off">
                        <input name="q" id="q" <c:if test="${not empty query}">value="<c:out value="${query}" />"</c:if> type="text" size="25"/>
                        <input type="hidden" name="title"/>
                        <input type="submit" value="Search"/>
                    </form>
                </div>
                <div id="headerLogo"> 
                    <a href="${pageContext.request.contextPath}/"><img src="${pageContext.request.contextPath}/static/images/ALA-logo-50px.gif" alt="ALA Logo" id="headerLogo" border="0"/></a>
                </div>
                <h1 id="headerTitle">Biodiversity Information Explorer</h1>
            </div>
            <div id="topmenu">
                <c:set var="pageName"><decorator:getProperty property="meta.pageName"/></c:set>
                <ul class="tabs">
                    <li class="<c:if test="${pageName == 'home'}">active</c:if> first"><a href="${pageContext.request.contextPath}/">Home</a></li>
                    <li class="<c:if test="${pageName == 'species'}">active</c:if>"><a href="${pageContext.request.contextPath}/species/search">Explore Species</a></li>
                    <!-- <li class="<c:if test="${pageName == 'maps'}">active</c:if> last"><a href="http://ec2-174-129-53-185.compute-1.amazonaws.com/webportal/">Explore maps</a></li> -->
                    <li class="<c:if test="${pageName == 'datasets'}">active</c:if> "><a href="${pageContext.request.contextPath}/species/datasets">Datasets</a></li>
                    <li class="<c:if test="${pageName == 'conservationStatus'}">active</c:if> "><a href="${pageContext.request.contextPath}/species/status/conservationStatus">Conservation Status List</a></li>
                    <li class="<c:if test="${pageName == 'pestStatus'}">active</c:if> last"><a href="${pageContext.request.contextPath}/species/status/pestStatus">Pest Status List</a></li>
                </ul>                
            </div>
            <div id="body" class="yui-skin-sam">
                <decorator:body />
            </div>
            <div id="footer" class="span-24 prepend-top append-bottom last">
                <div id="copy">&nbsp;&copy;2010 <a href="mailto:info@ala.org.au">Atlas of Living Australia</a></div>
                <ul class="tabs">
                    <li><a href="${pageContext.request.contextPath}/about">About the ALA</a></li>
                    <li><a href="${pageContext.request.contextPath}/contact">Contact Us</a></li>
                </ul>
            </div>
        </div>
    </body>
</html>