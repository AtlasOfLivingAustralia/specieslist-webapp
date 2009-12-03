<!DOCTYPE html PUBLIC
	"-//W3C//DTD XHTML 1.1 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
 
<head>
    <title>Search the Fedora Repository</title>
</head>

<body>
        <h1>Search the Fedora Repository by DC Field</h1>
        
        <s:form action="search/searchFields" method="GET" >
            <h3>Enter search term (pid field is assumed)</h3>

             <s:textfield label="Value" name="propertyValue" size="60" required="true"/>
             
             <s:select label="Extra Search Fields" name="selectedDCFields" headerKey = "1"
                value="selectedDCFields" list="DCStreamFieldList" size="10" multiple="true"/>
 
             <s:submit label="searchFields" name="searchFields"/>
         </s:form>
         
</body>
</html>