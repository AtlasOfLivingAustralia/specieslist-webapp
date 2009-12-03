<%--
    Document   : image-show
    Created on : 21/08/2009, 11:15:08 AM
    Author     : oak021
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <meta name="pageName" content="image" />
        <title>ALA Repository Image Display</title>
    </head>
    <body>
        <h1>${title}</h1>

        <h2>Type of Object: Image<br /></h2>
        <p>
            <img src="${photoSourceUrl}" width="100"  alt="Source Image" >
        <table>            
            <tr>
                <td>Object Identifier (Guid)</td>
                <td>${guid}</td>
            </tr>
        </table>
    </p>
    <h2>Properties:</h2>

    <table class ="propertyTable">
        <!-- Table headings. -->
        <tr>
            <th>Property</th>
            <th>Value</th>
            <th>Harvested</th>
            <th>Source</th>
        </tr>

        <!-- Dynamic table content. -->
        <s:iterator value="objProperties">
            <tr>
                <td><s:property value="relationship" /></td>
                <td>${value}</td>
                <td>
                    <s:property value="harvested" />
                </td>
                <td>
                    <a href="/fedora/get/${pid}/<s:property value="sourceDSID"/>"><s:property value="dataSource" /></a>
                </td>
            </tr>
        </s:iterator>
    </table>
    <br />

</body>
</html>
