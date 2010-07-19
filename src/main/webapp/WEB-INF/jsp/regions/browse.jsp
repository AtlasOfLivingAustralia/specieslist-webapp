<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta name="pageName" content="geoRegion"/>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/screen.css" type="text/css" media="screen" charset="utf-8"/>
  <title>Regions - States</title>
</head>
<body>
    <div id="header">
        <div id="breadcrumb">
            <a href="http://test.ala.org.au">Home</a>
            <a href="http://test.ala.org.au/explore">Explore</a>
            Regions
        </div>
        <h1>${geoRegion.name}</h1>
    </div><!--close header-->
    <div id="column-one">
        <div class="section">
            <h1>States</h1>
            <br/>
            <ul>
                <li><a href="${pageContext.request.contextPath}/regions/aus_states/Australian Capital Territory">Australian Capital Territory</a></li>
                <li><a href="${pageContext.request.contextPath}/regions/aus_states/New South Wales">New South Wales</a></li>
                <li><a href="${pageContext.request.contextPath}/regions/aus_states/Victoria">Victoria</a></li>
                <li><a href="${pageContext.request.contextPath}/regions/aus_states/Western Australia">Western Australia</a></li>
                <li><a href="${pageContext.request.contextPath}/regions/aus_states/Tasmania">Tasmania</a></li>
                <li><a href="${pageContext.request.contextPath}/regions/aus_states/Northern Territory">Northern Territory</a></li>
                <li><a href="${pageContext.request.contextPath}/regions/aus_states/Queensland">Queensland</a></li>
                <li><a href="${pageContext.request.contextPath}/regions/aus_states/South Australia">South Australia</a></li>
            </ul>
        </div>
    </div>
</body>
</html>
