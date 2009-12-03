<!DOCTYPE html PUBLIC
	"-//W3C//DTD XHTML 1.1 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
 
<head>
    <title>RI Repository Search</title>
</head>

<body>
        <h1>Resource Index Repository Search</h1>
        
        <s:form action="searchRI" namespace="search" method="GET" >
            <h2>Enter Query Details</h2>

             <s:textfield label="Value" name="propertyValue" size="40"/>
             <s:combobox label="Property Name" name="propertyName" headerKey = "1"
                value="identifier" list="propertyList"/>
             <s:submit value="Find"/>
         </s:form>
         
</body>
</html>