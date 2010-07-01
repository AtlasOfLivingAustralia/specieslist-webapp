<%@ include file="/common/taglibs.jsp" %>
<%@ attribute name="rankId" required="true" type="java.lang.String" %>
<%@ attribute name="name" required="true" type="java.lang.String" %>
<%@ attribute name="acceptedName" required="false" type="java.lang.String" %>
<!-- rankId = ${rankId} -->
<c:choose>
    <c:when test="${rankId >= 6000}">
        <i>${name}</i>
    </c:when>
    <c:otherwise>
        ${name}
    </c:otherwise>
</c:choose>
<c:if test="${not empty acceptedName}">
    <c:choose>
        <c:when test="${rankId >= 6000}">
            (accepted name: <i>${acceptedName}</i>)
        </c:when>
        <c:otherwise>
            (accepted name: ${acceptedName})
        </c:otherwise>
    </c:choose>
</c:if>