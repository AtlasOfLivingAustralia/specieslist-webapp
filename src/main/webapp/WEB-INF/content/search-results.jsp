<!DOCTYPE html PUBLIC
	"-//W3C//DTD XHTML 1.1 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>List of Search Results</title>
    </head>

    <body>
            <h1>Fedora Search Results</h1>
            <s:if test="%{searchResults.isEmpty()}"><h2>There are no objects which match your criterion</h2></s:if>
            <s:else>
                <h3>The following items match your criterion, click for a detailed display:</h3>
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
                          <a href="${pageContext.request.contextPath}/<s:property value="urlMapper"/>/<s:property value="pid"/>"><s:property value="title"/></a>
                        </td>
                     </tr>
                    </s:iterator>
                </table>
           </s:else>
    </body>
</html>
