<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ 
taglib
	prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@ 
taglib
	prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<html>
<head>
<title>${extendedTaxonConcept.taxonConcept.nameString}</title>
<link rel="stylesheet" href="/bie-hbase/css/default.css" />
</head>
<body>
<h1>${extendedTaxonConcept.taxonConcept.nameString} 
  - 
  <a href="taxon?debug=true&guid=${extendedTaxonConcept.taxonConcept.guid}">debug</a>
  <span style="float:right;"><a href ="/bie-hbase/">HOME</a></span>
</h1>
<table>
	<tr>
		<td>Author</td>
		<td>${extendedTaxonConcept.taxonConcept.author}</td>
	</tr>
	<tr>
		<td>Year</td>
		<td>${extendedTaxonConcept.taxonConcept.authorYear}</td>
	</tr>
	<tr>
		<td>Published</td>
		<td>${extendedTaxonConcept.taxonConcept.publishedIn}</td>
	</tr>
</table>

<c:if test="${not empty commonNames}">
  <h3>Common names</h3>
  <table>
    <c:forEach items="${extendedTaxonConcept.commonNames}" var="commonName">
      <tr>
        <td>Common name</td>
        <td>${commonName.nameString}</td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<h3>Parent concepts</h3>
<table>
	<c:forEach items="${extendedTaxonConcept.parentConcepts}" var="parentConcept">
		<tr>
			<td>Scientific name</td>
			<td><a href="taxon?guid=${parentConcept.guid}">${parentConcept.nameString}</a></td>
		</tr>
	</c:forEach>
</table>

<h3>Taxon name properties</h3>
<table>
	<tr>
		<td>Scientific name</td>
		<td>${extendedTaxonConcept.taxonName.nameComplete}</td>
	</tr>
	<tr>
		<td>Author</td>
		<td>${extendedTaxonConcept.taxonName.authorship}</td>
	</tr>
	<tr>
		<td>Rank</td>
		<td>${extendedTaxonConcept.taxonName.rankString}</td>
	</tr>
	<tr>
		<td>Typification</td>
		<td>${extendedTaxonConcept.taxonName.typificationString}</td>
	</tr>
</table>

<c:if test="${not empty extendedTaxonConcept.synonyms}">
	<h3>Synonyms (${fn:length(extendedTaxonConcept.synonyms)})</h3>
	<table>
		<c:forEach items="${extendedTaxonConcept.synonyms}" var="synonym">
			<tr>
				<td style="width: 30%;"><a href="taxon?guid=${synonym.guid}">${synonym.nameString}</a></td>
				<td>${synonym.publishedIn}</td>
			</tr>
		</c:forEach>
	</table>
</c:if>

<c:if test="${not empty extendedTaxonConcept.childConcepts}">
	<h3>Child concepts (${fn:length(extendedTaxonConcept.childConcepts)})</h3>
	<table>
		<c:forEach items="${extendedTaxonConcept.childConcepts}" var="childConcept">
			<tr>
				<td>Scientific name</td>
				<td><a href="taxon?guid=${childConcept.guid}">${childConcept.nameString}</a></td>
			</tr>
		</c:forEach>
	</table>
</c:if>


<c:if test="${not empty extendedTaxonConcept.images}">
  <h3>Images (${fn:length(extendedTaxonConcept.images)})</h3>
  <table>
    <c:forEach items="${extendedTaxonConcept.images}" var="image">
      <tr>
        <td><img src="${fn:replace(image.repoLocation, '/data/bie/', 'http://localhost/repository/')}"/></td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<c:if test="${not empty extendedTaxonConcept.pestStatuses}">
  <h3>Pest Status (${fn:length(extendedTaxonConcept.pestStatuses)})</h3>
  <table>
    <c:forEach items="${extendedTaxonConcept.pestStatuses}" var="status">
      <tr>
        <td>Status</td>
        <td>${status.status}</td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<c:if test="${not empty extendedTaxonConcept.conservationStatuses}">
  <h3>Conservation Status (${fn:length(extendedTaxonConcept.conservationStatuses)})</h3>
  <table>
    <c:forEach items="${extendedTaxonConcept.conservationStatuses}" var="status">
      <tr>
        <td>Status</td>
        <td>${status.status}</td>
      </tr>
    </c:forEach>
  </table>
</c:if>


</body>
</html>