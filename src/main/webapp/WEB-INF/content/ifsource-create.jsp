<!DOCTYPE html PUBLIC
	"-//W3C//DTD XHTML 1.1 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
 
<head>
    <title>Create New InfoSource</title>
</head>

<body>
        <h1>Add a new harvest source to the Repository</h1>
        
        <s:form action="ingest" method="GET" >
            <h2>Enter Source Details</h2>

             <s:textfield label="Identifier" name="guid" size="80" required="true"/>
             <s:textfield label="Label" name="description" size="80" required="true"/>
             <s:checkbox label="Is Authoritative" name="authoritative" required="true"/>
             <s:select label="Select Protocol" name="protocol" headerKey = "1"
                headerValue="--Please select --" list="protocolList" required="true"
                size="5" emptyOption="true"/>
             <s:textarea label="Protocol Parameters" name="protocolParam" rows="4" cols="80" required="true"/>
             <s:textfield label="Endpoint URI" name="endpoint" size="80" required="true"/>
             <s:select label="Select Content Model" name="destMetadataClass" headerKey = "1"
                headerValue="--Please select --" list="contentModelList" required="true"/>
             <s:select label="Select Mapper" name="documentMapper" headerKey = "1"
                headerValue="--Please select --" list="mapperList" required="true"/>
               
             <s:textfield label="source XML Schema" name="sourceXMLSchema" size="80" required="true"/>
             <s:submit name="Ingest"/>
         </s:form>  
    </div>
</body>
</html>
