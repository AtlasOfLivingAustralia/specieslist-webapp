<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib prefix="fn" uri="/WEB-INF/tld/fn.tld" %>

<style>
table.datasheet { border:1px solid #CCCCCC; border-collapse: collapse; background-color: #fffae4; padding: 5px; text-align:left; }
table.datasheet thead { padding-top: 3px; border:0;}
table.datasheet th { border-right: 1px solid gray; border-top:0; color: gray; padding:3px 5px 3px 5px; }
table.datasheet td { border-right: 1px solid gray; border-top: 1px solid gray; text-align:left; padding:3px 5px 3px 5px; vertical-align:center; }

</style>


<c:choose>

<c:when test="${param['propertiesOnly']}">

    Number of properties : ${fn:length(orderedProperties)}
    <table class="datasheet">    
     <thead style="font-style: bold;">
        <th>Property name</th>
        <th>Property value</th>
        <th>Category</th>      
        <th>Source/th>
     </thead>    
     <c:forEach items="${orderedProperties}" var="orderedProperty">
     <tr style="border: 1px solid gray;border-collapse: collapse; ">
        <td>
            <s:set var="propertyName">${orderedProperty.propertyName}</s:set>
            <s:text name="%{propertyName}"/>
        </td>
        <td>${orderedProperty.propertyValue}</td>
        <td>${orderedProperty.category.name}</td>
        <td>
            <c:forEach items="${orderedProperty.sources}" var="source">
                ${source.infoSourceName}<br/>
                ${source.sourceTitle}
            </c:forEach>
        </td>
     </tr>
    </c:forEach>
    </table>
</c:when>


<c:when test="${!param['sort']}">

	Number of results : ${fn:length(documents)}
	<hr/>    
	 <c:forEach items="${documents}" var="document">
	 Infosource ID:${document.infoSourceUrl}<br/>
	 Infosource Name:${document.infoSourceName}<br/>
	 Source ID:${document.sourceUrl}<br/>
	 Source Title:${document.sourceTitle}<br/>
	 <table>
	  <c:forEach var="entry" items="${document.propertyMap}">
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