<%-- 
    Document   : taxa-index
    Created on : 15/06/2009, 10:06:08 AM
    Author     : hwa002
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
        <title>Harvest Index Page</title>
    </head>
    <body>
            <h1>Available InfoSources</h1>
            <s:if test="%{searchResults.isEmpty()}"><h2>There are no Publications in the repository</h2></s:if>
            <s:else>

                 <!-- Dynamic table content. -->
                 <table class ="propertyTable">
                     <tr>
                         <th>Type</th>
                         <th>Title</th>

                     </tr>

                    <s:iterator value="searchResults">
                     <tr>
                         <td>
                             <s:property value="contentModelInitial"/>
                         </td>
                        <td>
                          <a href="./<s:property value="urlMapper"/>/<s:property value="pid"/>"><s:property value="title"/></a>
                        </td>
                     </tr>
                    </s:iterator>
                </table>
           </s:else>
    </body>
</html>
