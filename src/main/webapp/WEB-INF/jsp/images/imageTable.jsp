<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<table>
<tr>
<c:forEach items="${results.results}" var="searchTaxon" varStatus="status">

<c:set var="noOfColumns" value="4"/>

<c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '1024'}"><c:set var="noOfColumns" value="4"/></c:if>
<c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '1440'}"><c:set var="noOfColumns" value="5"/></c:if>
<c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '1680'}"><c:set var="noOfColumns" value="7"/></c:if>
<c:if test="${not empty param['screenWidth'] && param['screenWidth'] == '1900'}"><c:set var="noOfColumns" value="7"/></c:if>

<c:if test="${status.index % noOfColumns == 0 && status.index>0}">
</tr><tr>
</c:if>
<td>
 <a class="thumbImage" href="${pageContext.request.contextPath}/images/infoBox?q=${searchTaxon.guid}">
    <img src="${fn:replace(searchTaxon.thumbnail, 'thumbnail', 'smallRaw' )}" width="175" style="border: 1px solid gray;"/>
</a>
<br/>
<c:if test="${not empty searchTaxon.commonNameSingle}">${searchTaxon.commonNameSingle} <br/></c:if>
<alatag:formatSciName name="${searchTaxon.nameComplete}" rankId="${searchTaxon.rankId}"/>
</td>
</c:forEach>
</tr>
</table>

