<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta name="pageName" content="geoRegion"/>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Regions - ${geoRegion.regionTypeName} - ${geoRegion.name}</title>
</head>
<body>
  <h1>${geoRegion.name}</h1>
  <ul>
    <li>Birds - Number recorded in ${geoRegion.name}: ${birdCount} - Download list</li>
    <li>Fish - Number recorded in ${geoRegion.name}: ${fishCount} - Download list</li>
    <li>
      Mammals 
      - Number recorded in ${geoRegion.name}: ${mammals.totalRecords} 
      - <a href="${pageContext.request.contextPath}/regions/state/${geoRegion.name}/download?higherTaxon=Mammalia&rank=class">Download</a>
      - <a href="#Mammals" id="viewMammalsList">View<c:if test="${mammals.totalRecords>100}">(limited to 100)</c:if></a>
      <ul id="mammalsList">
        <c:forEach items="${mammals.taxonConcepts}" var="taxonConcept">
          <li>${taxonConcept.nameString} 
              <c:if test="${not empty taxonConcept.commonName}">(${taxonConcept.commonName})</c:if>
          </li>
        </c:forEach>
      </ul>
    </li>
    <li>Frogs - Number recorded in ${geoRegion.name}: ${frogCount} - Download list</li>
    <li>Reptiles - Number recorded in ${geoRegion.name}: ${reptileCount} - Download list</li>
  </ul>
  
  <h3>Comparison tool</h3>
  
  
  
  
<script type="text/javascript">
  $('#mammalsList').hide();
  $('#viewMammalsList').click(function () {
    $('#mammalsList').toggle("slow");
  }); 
</script>  
</body>

</html>
