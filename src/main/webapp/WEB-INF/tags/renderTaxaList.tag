<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@
taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%@
taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%@
taglib prefix="spring" uri="http://www.springframework.org/tags" %><%@ 
taglib tagdir="/WEB-INF/tags" prefix="alatag" %>
<%@ attribute name="taxonConcepts" required="true" type="java.util.List" rtexprvalue="true"  %>
<c:forEach items="${taxonConcepts}" var="taxonConcept">
 <li class="taxonConceptResult">
      <c:if test="${not empty taxonConcept.thumbnail}">
        <img src="http://${pageContext.request.serverName}:80${fn:replace(taxonConcept.thumbnail,'/data/bie','/repository')}"/>
      </c:if> 
      <a href="${pageContext.request.contextPath}/species/${taxonConcept.guid}">
        <c:choose>
          <c:when test="${not empty taxonConcept.commonName}">
            <span class="commonName">${taxonConcept.commonName}</span>
            <span class="scientificName">(${taxonConcept.nameString})</span>
          </c:when>
          <c:otherwise>
           <span class="scientificName">${taxonConcept.nameString}</span> 
          </c:otherwise>
        </c:choose>
     </a>
 </li>
</c:forEach>
