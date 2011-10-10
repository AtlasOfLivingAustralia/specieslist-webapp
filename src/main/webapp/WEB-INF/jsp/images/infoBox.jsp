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

<style type="text/css">
    .multiImages:hover { cursor: pointer; }
</style>
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

function nextImageWithRoll(){
    if(imageIndex + 1 < noOfImages){
        imageIndex = imageIndex +1;
        $('#selectedImage').attr('src',imageArray[imageIndex]);
        $('#imageCounter').html(imageIndex+1)
    } else {
        imageIndex = 0
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

<h1 style="text-align: left; margin-bottom:10px; margin-left:15px; margin-top:5px;">
<a href="${pageContext.request.contextPath}/species/${extendedTaxonConcept.taxonConcept.guid}" style="text-decoration: none;">
<span ><i>${extendedTaxonConcept.taxonConcept.nameString}</i></span>
<c:if test="${not empty commonNames}"> |
${commonNames[0]}
    </c:if>
</a>
</h1>

<table style="border:0px;">
<tr>
<td style="vertical-align: top; width: 400px;">

<img id="selectedImage"
     class="<c:if test="${fn:length(extendedTaxonConcept.images) > 1}">multiImages</c:if>"
     src="${fn:replace(extendedTaxonConcept.images[0].repoLocation,'raw','smallRaw')}" style="max-width: 400px; max-height: 245px;" onclick="javascript:nextImageWithRoll();"/>
<br/>
<c:if test="${fn:length(extendedTaxonConcept.images) >1}">
    No. <span id="imageCounter">1</span> of ${fn:length(extendedTaxonConcept.images)} images
    <a href="javascript:previousImage();">Previous image</a> |
    <a href="javascript:nextImage();">Next image</a>
</c:if>
</td>
<td style="vertical-align: top; width: 300px;">
<img src="${spatialPortalMap.mapUrl}" alt="" style="max-width: 300px; max-height: 265px;"/>
</td>
</tr>
</table>

</body>
</html>
