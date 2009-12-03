<%-- 
    Document   : ifsource-ingest
    Created on : 10/07/2009, 4:21:57 PM
    Author     : oak021
--%>

<!DOCTYPE html PUBLIC
	"-//W3C//DTD XHTML 1.1 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Ingesting Info Source</title>
        <title>s:property value="message" </title>
    </head>
    <body>
            <h1>New info source  added to system</h1>
            <h3><s:property value ="pid"/></h3>

            <a href="../datastream/${pid}">View repository Object</a>
            <a href="../ifsource/create">Create a new info source</a>
            <a href="../harvest">Harvest Data</a>
    </body>
</html>
