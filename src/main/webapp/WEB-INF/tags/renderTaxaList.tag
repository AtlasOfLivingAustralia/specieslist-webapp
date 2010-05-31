<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@
taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%@
taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%@
taglib prefix="spring" uri="http://www.springframework.org/tags" %><%@ 
taglib tagdir="/WEB-INF/tags" prefix="alatag" %>
<%@ attribute name="taxonConcepts" required="true" type="java.util.List" rtexprvalue="true"  %>
<c:forEach items="${taxonConcepts}" var="taxonConcept">
 <li>
      <a href="${pageContext.request.contextPath}/species/${taxonConcept.guid}">
       <span class="scientificName">${taxonConcept.nameString}</span> 
       <c:if test="${not empty taxonConcept.commonName}">(${taxonConcept.commonName})</c:if>
     </a>
 </li>
</c:forEach>
