<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta name="pageName" content="vocabularies"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Vocabularies</title>
</head>
<body>
    <h1>Vocabularies for <c:out value="${infoName}" /></h1>
    <c:if test="${not empty vocabulariesMap}">
        <h3>&bull; Term Mapping</h3>
        <table>
            <tr>
                <th>ID</th>
                <th>Raw Term</th>
                <th>Standard Term</th>
                <th>Predicate</th>
            </tr>
            <c:forEach var="vocaMap" items="${vocabulariesMap}">
                    <tr>
                        <td><c:out value="${vocaMap['source_id']}" /></td>
                        <td><c:out value="${vocaMap['source_term']}" /></td>
                        <td><c:out value="${vocaMap['target_term']}" /></td>
                        <td><c:out value="${vocaMap['predicate']}" /></td>
                    </tr>
            </c:forEach>
        </table>
    </c:if>
</body>
</html>
