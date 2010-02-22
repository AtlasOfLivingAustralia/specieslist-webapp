<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta name="pageName" content="species"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Species Search</title>
</head>
<body>
    <div id="searchBox">
        <h3>Search for Species and Taxa</h3>
        <%--<s:form action="/species/" namespace="search" method="GET" theme="simple">--%>
        <form method="GET">
            <input name="q" size="30"/>
            <input type="submit" value="Search"/>
        <%--</s:form>--%>
        </form>
        <p>
        <p>Examples:<br/><a href='${pageContext.request.contextPath}/species/search?q="Pogona+barbata"'>&quot;Pogona barbata&quot;</a><br/>
            <a href='${pageContext.request.contextPath}/species/search?q="Hypertropha+chlaenota"'>&quot;Hypertropha chlaenota&quot;</a><br/>
            <a href='${pageContext.request.contextPath}/species/search?q="Argyropelecus+gigas"'>&quot;Argyropelecus gigas&quot;</a><br/>
            <a href='${pageContext.request.contextPath}/species/search?q="Podargus+strigoides"'>&quot;Podargus strigoides&quot;</a><br/>
            <a href='${pageContext.request.contextPath}/species/search?q="Glossopsitta+concinna"'>&quot;Glossopsitta concinna&quot;</a><br/>
            <a href='${pageContext.request.contextPath}/species/search?q="Hippotion+scrofa"'>&quot;Hippotion scrofa&quot;</a><br/>
            <a href='${pageContext.request.contextPath}/species/search?q=Asplenium+australasicum'>&quot;Asplenium australasicum&quot;</a><br/>
            <a href='${pageContext.request.contextPath}/species/search?q=Malacorhynchus+membranaceus'>&quot;Malacorhynchus membranaceus&quot;</a><br/>
        </p>
    </div>
    <script type="text/javascript">
        $("#menuSearch").hide();
    </script>
</body>
</html>
