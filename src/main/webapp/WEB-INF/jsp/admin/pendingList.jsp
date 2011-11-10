<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<c:set var="biocacheUrl">http://biocache.ala.org.au</c:set>
<html>
<head>
    <meta name="pageName" content="species"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Find a species | Share a sighting | Atlas of Living Australia</title>
    <link rel="stylesheet" href="${initParam.centralServer}/wp-content/themes/ala/css/bie.css" type="text/css" media="screen" charset="utf-8"/>
</head>
<body>

<div id="header">
    <div id="breadcrumb">
       <a href="${initParam.centralServer}">Home</a>
       <a href="${initParam.centralServer}/share">Share</a>
       <span class="current">Record Sightings</span>
   	</div>
	<h1>Photos Pending List</h1>
</div><!--close header-->

<div class="section">

<div class="solrResults" style="width:967px;">

<div class="results">
<script>
function editThisImage(guid, uri){
	var url = "${pageContext.request.contextPath}/admin/edit?guid="+guid+"&uri="+uri;
	window.open(url);
}

function popupImage(urlImage){
	var url = urlImage.replace('thumbnail', 'raw');
	window.open(url);
}

</script>
<c:forEach items="${searchResults.results}" var="result">
  <h4> 
      <c:if test="${not empty result.imageUrl}"><a href="#" onclick="popupImage('${result.imageUrl}')"><img class="alignright" src="${result.imageUrl}" width="85" height="85" alt="species image thumbnail"/></a></c:if>
      <c:if test="${empty result.imageUrl}"><div class="alignright" style="width:85px; height:40px;"></div></c:if>
      <span style="text-transform: capitalize; display: inline;"><c:if test="${empty result.rank}">Species</c:if><c:if test="${not empty result.rank}">${result.rank}</c:if></span>:
      <c:if test="${not empty result.scientificName}">&nbsp;&nbsp; ${result.scientificName}</c:if>
	  <c:if test="${not empty result.commonName}">&nbsp;&ndash;&nbsp; ${result.commonName}</c:if>
	  <span><a href="#" onclick="editThisImage('${result.guid}', '<string:encodeUrl>${result.uri}</string:encodeUrl>')" class="occurrenceLink">Edit Image</a></span>
  </h4>
  <p>
  </p>
</c:forEach>
    
</div><!-- results -->
<c:if test="${not empty searchResults}">
<div id="searchNavBar">
    <alatag:searchNavigationLinks totalRecords="${totalRecords}" startIndex="${searchResults.startIndex}"
         lastPage="${lastPage}" pageSize="${searchResults.pageSize}"/>
</div>
</c:if>

</div><!-- solrResults -->

</div><!-- section -->
</body>
</html>
