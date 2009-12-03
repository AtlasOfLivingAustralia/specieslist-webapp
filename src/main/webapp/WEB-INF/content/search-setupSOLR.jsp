<!DOCTYPE html PUBLIC
	"-//W3C//DTD XHTML 1.1 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
 
<head>
    <title>SOLR Repository Search</title>
</head>

<body>
        <h1>SOLR Repository Search</h1>
        
        <s:form action="searchSOLR" namespace="search" method="GET" >
            <h2>Enter Query Details</h2>

             <s:textfield key="propertyValue"/>
             <s:combobox label="Property Name" name="propertyName" headerKey = "1"
                value="PID" list="solrFieldList"/>
           <!--  <s:combobox label="Select Content Model" name="contentModel" headerKey = "1"
                headerValue="--Please select --" list="contentModelList"/>
                -->
             <s:submit label="searchSOLR" name="searchSOLR"/>
         </s:form>
        <p>Note: apply Lucene query syntax. The query value applies
        to the property name (field) chosen and has no escaping or URI encoding applied.
        Wildcards are allowed, but you cannot use a * or ? symbol as the first character of a search.
        Sample query 1: ala\:* </p>
         
</body>
</html>