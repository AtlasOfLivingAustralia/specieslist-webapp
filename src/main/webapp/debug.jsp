<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ 
taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ 
taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<html>
<head>
  <title>Debug page for ${param['guid']}</title>
  <link rel="stylesheet" href="/bie-hbase/css/default.css" />
<head>
<body>
<h1> ${param['guid']}</h1>
<table>
<c:forEach items="${properties}" var="entry">
  <tr>
    <td>${entry.key}</td>
    <td>
      <c:if test="${fn:endsWith(entry.key,'guid')}">
        <a href="taxon?guid=${entry.value}">
      </c:if>  
      ${entry.value}
      <c:if test="${fn:endsWith('guid', entry.key)}">
        </a>
      </c:if>  
    </td>  
  </tr>  
</c:forEach>
</table>
</body>
<html>