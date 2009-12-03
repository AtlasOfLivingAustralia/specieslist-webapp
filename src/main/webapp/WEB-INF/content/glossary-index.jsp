<%-- 
    Document   : glossary
    Created on : 15/09/2009, 9:00:33 AM
    Author     : oak021
--%>

<!DOCTYPE html PUBLIC
	"-//W3C//DTD XHTML 1.1 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
    <title>Prefix Glossary</title>
</head>
<body>
        <h1>Prefix Glossary</h1>
                   
       <dl>
            <s:iterator value="namespaceMap">
                <dt> <s:property value="%{key}"/> </dt>
                <dd> <s:property value="%{value}"/> </dd>
            </s:iterator>
       </dl>
    </body>
</html>
