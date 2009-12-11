<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib prefix="fn" uri="/WEB-INF/tld/fn.tld" %>

<c:choose>
<c:when test="${!param['sort']}">

	Number of results : ${fn:length(properties)}
	<hr/>    
	 <c:forEach items="${properties}" var="properties">
	 Infosource ID:${properties.infoSourceUrl}<br/>
	 Infosource Name:${properties.infoSourceName}<br/>
	 Source ID:${properties.sourceUrl}<br/>
	 Source Title:${properties.sourceTitle}<br/>
	 <table>
	  <c:forEach var="entry" items="${properties.propertyMap}">
	       <tr>
	         <td>${entry.key}</td>
	         <td>${entry.value}</td>
	       </tr>
	  </c:forEach>
	</table>        
	<hr/>    
	</c:forEach>

</c:when>
<c:when test="${param['sort'] && param['pretty']}">

 <c:forEach items="${orderedDocuments}" var="orderedDocument">
  
  <div style="border:1px solid gray; padding:10px; margin:10px;">  
    <h3>${orderedDocument.infoSourceName}</h3> 
    <h4><a href="${orderedDocument.sourceUrl}">${orderedDocument.sourceTitle}</a></h4>
    <c:forEach var="categorisedProperties" items="${orderedDocument.categorisedProperties}">
    
      <c:if test="${categorisedProperties.category.name!='Taxonomic'}">
	      <h5>${categorisedProperties.category.name}</h5>
	      <table>
	      <c:forEach var="entry" items="${categorisedProperties.propertyMap}">
	          <tr>
	            <td>${entry.key}</td>
	            <td>${entry.value}</td>
	          </tr>
	      </c:forEach>
	      </table>
      
      </c:if>        
    </c:forEach> 
  </div>
  
</c:forEach>

</c:when>
<c:otherwise>

 <c:forEach items="${orderedDocuments}" var="orderedDocument">
  
  <div style="border:1px solid gray; padding:10px; margin:10px;">  
    <table>
        <tr><td>Infosource Name:</td><td>${orderedDocument.infoSourceName}</td></tr>
        <tr><td>Infosource URL:</td><td>${orderedDocument.infoSourceUrl}</td></tr>
        <tr><td>Source ID:</td><td>${orderedDocument.sourceUrl}</td></tr>
        <tr><td>Source Title:</td><td>${orderedDocument.sourceTitle}</td></tr>
   </table>
	<c:forEach var="categorisedProperties" items="${orderedDocument.categorisedProperties}">
	  <h5>${categorisedProperties.category.name} - category rank: ${categorisedProperties.category.rank}</h5>
	  <table>
	  <c:forEach var="entry" items="${categorisedProperties.propertyMap}">
	      <tr>
	        <td>${entry.key}</td>
	        <td>${entry.value}</td>
	      </tr>
	  </c:forEach>
	  </table>  	  
	</c:forEach> 
  </div>
  
</c:forEach>

</c:otherwise>
</c:choose>