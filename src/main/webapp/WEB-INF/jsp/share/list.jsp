<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<c:set var="biocacheUrl">http://biocache.ala.org.au/</c:set>
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
       <a href="${initParam.centralServer}/contribute">Share</a>
       <span class="current">Record Sightings</span>
   	</div>
	<h1>Record Sightings - find a species</h1>
</div><!--close header-->


<div class="section">
<div id="focussedSearch">
	<h2>Find a species to record</h2>
	<div id="inpage_search">
		<form id="search-inpage" action="${pageContext.request.contextPath}/share/sighting" method="get" name="search-form">
		<label for="search">Search</label>
		<input type="text" class="filled" id="search" name="qs" placeholder="Find a species"  <c:if test="${not empty param['qs']}">value="${param['qs']}"</c:if>/>
		<input type="hidden" name="fq" value="idxtype:TAXON"/>
		<span class="search-button-wrapper"><input type="submit" class="search-button" alt="Search" value="Search"></span>
		</form>
	</div><!--close wrapper_search-->
</div><!-- focussedSearch -->
</div><!-- section -->

<div class="section">

<div class="solrResults" style="width:967px;">

<div class="results">

<c:forEach items="${results.results}" var="result">
  <h4> 
      <c:if test="${not empty result.thumbnail}"><a href="${biocacheUrl}/contribute/sighting/${result.guid}" class="occurrenceLink"><img class="alignright" src="${result.thumbnail}" width="85" height="85" alt="species image thumbnail"/></a></c:if>
      <c:if test="${empty result.thumbnail}"><div class="alignright" style="width:85px; height:40px;"></div></c:if>
      <span style="text-transform: capitalize; display: inline;">${result.rank}</span>:
      <alatag:formatSciName rankId="${result.rankId}" name="${result.name}" acceptedName="${result.acceptedConceptName}"/> ${result.author}
	  <c:if test="${not empty result.commonNameSingle}">&nbsp;&ndash;&nbsp; ${result.commonNameSingle}</c:if>
	  <span><a href="${biocacheUrl}/contribute/sighting/${result.guid}" class="occurrenceLink">Record a sighting</a></span>
  </h4>
  <p>
      <c:if test="${not empty result.commonNameSingle && result.commonNameSingle != result.commonName}">
          <span>${fn:substring(result.commonName, 0, 220)}<c:if test="${fn:length(result.commonName) > 220}">...</c:if></span>
      </c:if>
      <c:if test="${false && not empty result.highlight}">
          <span><b>...</b> ${result.highlight} <b>...</b></span>
      </c:if>
      <c:if test="${not empty result.kingdom}">
          <span><strong class="resultsLabel">Kingdom</strong>: ${result.kingdom}</span>
      </c:if>
      <!-- ${sectionText} -->
  </p>
</c:forEach>
    
</div><!-- results -->
<c:if test="${not empty searchResults}">
<div id="searchNavBar">
    <alatag:searchNavigationLinks totalRecords="${searchResults.totalRecords}" startIndex="${searchResults.startIndex}"
         lastPage="${lastPage}" pageSize="${searchResults.pageSize}"/>
</div>
</c:if>

</div><!-- solrResults -->

</div><!-- section -->
</body>
</html>
