<%-- 
    Document   : index
    Created on : 18/08/2009, 10:33:05 AM
    Author     : oak021
--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="pageName" content="home"/>
        <title>Atlas of Living Australia - Biodiversity Information Explorer</title>
    </head>
    <body>
        <p>Welcome to the Atlas of Living Australia <strong>Biodiversity Information Explorer</strong>.</p>
        <p>This is an expirental interface to the ALA Fedora Commons Repository. The following content
            types are browsable:</p>
        <ul>
            <li><a href="${pageContext.request.contextPath}/datastream">All (repository index)</a></li>
            <li><a href="${pageContext.request.contextPath}/taxa">Taxon Concepts</a></li>
            <li><a href="${pageContext.request.contextPath}/name">Taxon Names</a></li>
            <li><a href="${pageContext.request.contextPath}/pub">Publications</a></li>
            <li><a href="${pageContext.request.contextPath}/img">Images</a></li>
            <li><a href="${pageContext.request.contextPath}/html">HTML Pages</a></li>
        </ul>
    </body>
</html>
