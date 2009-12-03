<!DOCTYPE html PUBLIC
	"-//W3C//DTD XHTML 1.1 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
        <title>ALA Repository Datastream Display</title>
    </head>

    <body>
            <h1>Repository Object Datastream</h1>
            <h3><em>PID:</em> ${pid}</h3>
            <h3><em>Datastream ID:</em> ${selectedDSID}</h3>

            <s:property value="propertiesXml" />

    </body>    
</html>
