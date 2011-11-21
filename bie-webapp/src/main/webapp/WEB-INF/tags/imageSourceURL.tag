<%@ include file="/common/taglibs.jsp" %>
<%@ attribute name="image" required="true" type="org.ala.model.Image" rtexprvalue="true" %>
<c:set var="imageUri">
    <c:choose>
        <c:when test="${not empty image.isPartOf}">
            ${image.isPartOf}
        </c:when>
        <c:when test="${not empty image.identifier}">
            ${image.identifier}
        </c:when>
        <c:otherwise>
            ${image.infoSourceURL}
        </c:otherwise>
    </c:choose>
</c:set>
<c:choose>
<c:when test="${image.infoSourceURL == 'http://www.ala.org.au'}">
    Source: ${image.infoSourceName}
 </c:when>
 <c:when test="${image.infoSourceURL == 'http://www.elfram.com/'}">
    Source: <a href="${image.infoSourceURL}" target="_blank" onclick="javascript:window.location.href='${image.infoSourceURL}';">${image.infoSourceName}</a>
 </c:when>
 <c:otherwise>
    Source: <a href="${imageUri}" target="_blank" onclick="javascript:window.location.href='${imageUri}';">${image.infoSourceName}</a>
 </c:otherwise>
</c:choose>