<!DOCTYPE html PUBLIC
	"-//W3C//DTD XHTML 1.1 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>

<html>
    <head>
        <meta name="pageName" content="datastream"/>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>List of Available Datastreams</title>
    </head>

    <body>
            <h1>ALA Repository Object</h1>
            <h3>PID ${pid}</h3>
            <h3>Choose which datastream to display:</h3>
            <!-- Dynamic table content. -->
            <table class =" propertyTable">
               <th>Datastream ID</th>
               <s:iterator value="dsID">
                 <tr>                                                         
                    <td>
                      <a href="/fedora/get/${pid}/<s:property/>"><s:property/></a>
                    </td>
                 </tr>
               </s:iterator>
           </table>
           <h3>
             <a href="/fedora/get/${pid}">Fedora Default Dissemination</a>
           </h3>
    </body>
</html>
