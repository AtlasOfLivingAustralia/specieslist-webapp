<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script language="JavaScript" type="text/javascript" src="${initParam.centralServer}/wp-content/themes/ala/scripts/jquery-1.4.3.min.js"></script>
</head>
<body>

<script>

var imageIndex = 0;
var noOfImages = ${fn:length(extendedTaxonConcept.images)};

var imageArray = Array(<c:forEach items="${extendedTaxonConcept.images}" var="image" varStatus="status"><c:if test="${status.index>0}">,</c:if>'${fn:replace(image.repoLocation,"raw","smallRaw")}'</c:forEach>)

function nextImage(){
    if(imageIndex + 1 < noOfImages){
        imageIndex = imageIndex +1;
        $('#selectedImage').attr('src',imageArray[imageIndex]);
        $('#imageCounter').html(imageIndex+1)
    }
}

function previousImage(){
	if(imageIndex>0){
	    imageIndex = imageIndex - 1;
	    $('#selectedImage').attr('src',imageArray[imageIndex]);
	    $('#imageCounter').html(imageIndex+1)
	}
}

</script>

<table style="border:0px;">
<tr>
<td style="vertical-align: top;">
<c:if test="${fn:length(extendedTaxonConcept.images) >1}">

No. <span id="imageCounter">1</span> of ${fn:length(extendedTaxonConcept.images)} images

<a href="javascript:previousImage();">Previous image</a> |
<a href="javascript:nextImage();">Next image</a>

</c:if>
<img id="selectedImage" src="${fn:replace(extendedTaxonConcept.images[0].repoLocation,'raw','smallRaw')}" style="max-width: 550px; max-height: 400px;"/>
<br/>



</td>
<td style="vertical-align: top;">
<img src="${spatialPortalMap.mapUrl}" class="distroImg" alt="" width="375" style="margin-bottom:-30px;"/>
</td>
</tr>
</table>
<p style="text-align: left; border-top: 1px solid gray; padding:10px; margin: 10px;">
Species page: 
<a href="${pageContext.request.contextPath}/species/${extendedTaxonConcept.taxonConcept.guid}">
${extendedTaxonConcept.taxonConcept.nameString}<br/>
</a>
<c:if test="${not empty commonNames}">
Common names:
<c:forEach items="${commonNames}" var="commonName" varStatus="status"><c:if test="${status.index>0}">, </c:if>${commonName}</c:forEach>
</c:if>
</p>
</body>
</html>
