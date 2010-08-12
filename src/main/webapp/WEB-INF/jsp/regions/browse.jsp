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
  <title>Regions - States & Territories</title>
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
            <h1>States & Territories</h1>
            <br/>
            <ul>
           	<c:forEach items="${states}" var="region">
                <li><a href="${pageContext.request.contextPath}/regions/${region.guid}">${region.name}</a></li>           	
           	</c:forEach>
            </ul>
            
            <h1>Other regionalisations of Australia</h1>
            
            <table style="width:900px;">
            <tr>
            <td  style="width:300px;">
            <h2>Local Government Areas</h2>
            <ul>
           	<c:forEach items="${lga}" var="region">
                <li><a href="${pageContext.request.contextPath}/regions/${region.guid}">${region.name}</a></li>           	
           	</c:forEach>
            </ul>
            </td>
            <td  style="width:300px;">
            <h2>IBRA</h2>
            <ul>
           	<c:forEach items="${ibra}" var="region">
                <li><a href="${pageContext.request.contextPath}/regions/${region.guid}">${region.name}</a></li>           	
           	</c:forEach>
            </ul>
            </td>
            <td  style="width:300px;">
            <h2>IMCRA</h2>
            <ul>
           	<c:forEach items="${imcra}" var="region">
                <li><a href="${pageContext.request.contextPath}/regions/${region.guid}">${region.name}</a></li>           	
           	</c:forEach>
            </ul>
            </td>
            </tr>
            </table>
            
        </div>
    </div>
</body>
</html>
