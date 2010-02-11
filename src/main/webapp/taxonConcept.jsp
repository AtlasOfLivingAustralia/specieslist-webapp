<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ 
taglib
	prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@ 
taglib
	prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<html>
<head>
<title>${taxonConcept.nameString}</title>
<link rel="stylesheet" href="/bie-hbase/css/default.css" />
</head>
<body>
<h1>${taxonConcept.nameString} 
  - 
  <a href="taxon?debug=true&guid=${taxonConcept.guid}">debug</a>
  <span style="float:right;"><a href ="/bie-hbase/">HOME</a></span>
</h1>
<table>
	<tr>
		<td>Author</td>
		<td>${taxonConcept.author}</td>
	</tr>
	<tr>
		<td>Year</td>
		<td>${taxonConcept.authorYear}</td>
	</tr>
	<tr>
		<td>Published</td>
		<td>${taxonConcept.publishedIn}</td>
	</tr>
</table>

<c:if test="${not empty commonNames}">
  <h3>Common names</h3>
  <table>
    <c:forEach items="${commonNames}" var="commonName">
      <tr>
        <td>Common name</td>
        <td>${commonName.nameString}</td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<h3>Parent concepts</h3>
<table>
	<c:forEach items="${parentConcepts}" var="parentConcept">
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
		<td>${taxonName.nameComplete}</td>
	</tr>
	<tr>
		<td>Author</td>
		<td>${taxonName.authorship}</td>
	</tr>
	<tr>
		<td>Rank</td>
		<td>${taxonName.rankString}</td>
	</tr>
	<tr>
		<td>Typification</td>
		<td>${taxonName.typificationString}</td>
	</tr>
</table>

<c:if test="${not empty synonyms}">
	<h3>Synonyms</h3>
	<table>
		<c:forEach items="${synonyms}" var="synonym">
			<tr>
				<td>Scientific name</td>
				<td><a href="taxon?guid=${synonym.guid}">${synonym.nameString}</a></td>
			</tr>
		</c:forEach>
	</table>
</c:if>

<c:if test="${not empty childConcepts}">
	<h3>Child concepts</h3>
	<table>
		<c:forEach items="${childConcepts}" var="childConcept">
			<tr>
				<td>Scientific name</td>
				<td><a href="taxon?guid=${childConcept.guid}">${childConcept.nameString}</a></td>
			</tr>
		</c:forEach>
	</table>
</c:if>


<c:if test="${not empty images}">
  <h3>Images</h3>
  <table>
    <c:forEach items="${images}" var="image">
      <tr>
        <td><img src="${fn:replace(image.repoLocation, '/data/bie/', 'http://localhost/repository/')}"/></td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<c:if test="${not empty pestStatuses}">
  <h3>Pest Status</h3>
  <table>
    <c:forEach items="${pestStatuses}" var="status">
      <tr>
        <td>Status</td>
        <td>${status.status}</td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<c:if test="${not empty conservationStatuses}">
  <h3>Conservation Status</h3>
  <table>
    <c:forEach items="${conservationStatuses}" var="status">
      <tr>
        <td>Status</td>
        <td>${status.status}</td>
      </tr>
    </c:forEach>
  </table>
</c:if>


</body>
</html>