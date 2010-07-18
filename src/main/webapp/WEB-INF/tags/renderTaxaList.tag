<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@
taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%@
taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%@
taglib prefix="spring" uri="http://www.springframework.org/tags" %><%@ 
taglib tagdir="/WEB-INF/tags" prefix="alatag" %>
<%@ attribute name="taxonConcepts" required="true" type="java.util.List" rtexprvalue="true"  %>
<tr class="taxonConceptResult">
<c:forEach items="${taxonConcepts}" var="taxonConcept" varStatus="loopStatus">

  <c:if test="${loopStatus.index>0 && loopStatus.index % 8 == 0}">
    </tr>
    <tr class="taxonConceptResult">
  </c:if>
  
  <td>
      <a href="${pageContext.request.contextPath}/species/${taxonConcept.guid}">
        <c:choose>
        <c:when test="${not empty taxonConcept.thumbnail}">
          <img src="${taxonConcept.thumbnail}"/>
        </c:when>
        <c:otherwise>
          <img src="${pageContext.request.contextPath}/static/images/noImage100.jpg"/>
        </c:otherwise>
        </c:choose>
      </a>
      <br/>
     <span class="scientificName"><a href="${pageContext.request.contextPath}/species/${taxonConcept.guid}">${taxonConcept.name}</a></span>
     <br/>
     <span class="commonName">${taxonConcept.commonNameSingle}</span>
  </td>

</c:forEach>
</tr>
