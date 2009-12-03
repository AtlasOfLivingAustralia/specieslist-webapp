<%--
    Document   : harvest-setup.jsp
    Created on : 14/07/2009, 11:46:39 AM
    Author     : oak021
--%>

<!DOCTYPE html PUBLIC
	"-//W3C//DTD XHTML 1.1 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
        <title>Harvest Page</title>
    </head>
    <body>
            <h1>Data Harvest</h1>
            <s:form action="harvest/go" method="GET" theme="simple">

            <h2>InfoSource:</h2>
               <s:select label="Select Source" name="infoSourceGUID" headerKey = "1"
                headerValue="--Please select --" list="infoSourceList"/>

               <s:submit value="Harvest"/>

            </s:form>
    </body>
</html>
