<%--
    Document   : searchNavigationLinks.yag
    Created on : May 07, 2010, 9:36:39 AM
    Author     : "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
--%>
<%@ include file="/common/taglibs.jsp" %>
<%@ attribute name="totalRecords" required="true" type="java.lang.Long" %>
<%@ attribute name="startIndex" required="true" type="java.lang.Long" %>
<%@ attribute name="pageSize" required="true" type="java.lang.Long" %>
<%@ attribute name="lastPage" required="true" type="java.lang.Integer" %>
<%@ attribute name="maxPageLinks" required="false" type="java.lang.Integer" %>
<span id="navLinks">
    <c:if test="${empty maxPageLinks}"><c:set var="maxPageLinks" value="10"/></c:if>
    <fmt:formatNumber var="pageNumber" value="${(startIndex / pageSize) + 1}" pattern="0" />
    <c:set var="coreParams">?taxonRank=${param.taxonRank}&scientificName=${param.scientificName}</c:set>
    <c:set var="startPageLink">
        <c:choose>
            <c:when test="${pageNumber < 6 || lastPage < 10}">
                1
            </c:when>
            <c:when test="${(pageNumber + 4) < lastPage}"> <%-- ${(lastPage - pageNumber) < 5} --%>
                ${pageNumber - 4}
            </c:when>
            <c:otherwise>
                ${lastPage - 8}
            </c:otherwise>
        </c:choose>
    </c:set>
    <c:set var="endPageLink">
        <c:choose>
            <c:when test="${lastPage > (startPageLink + 8)}"> <%-- ${lastPage > 9} || ${(pageNumber < (lastPage - 4))} --%>
                ${startPageLink + 8}
            </c:when>
            <c:otherwise>
                ${lastPage}
            </c:otherwise>
        </c:choose>
    </c:set>
    <ul>
        <c:choose>
            <c:when test="${startIndex > 0}">
                <li id="prevPage"><a href="${coreParams}&start=${startIndex - pageSize}${hash}">&laquo; Previous</a></li>
            </c:when>
            <c:otherwise>
                <li id="prevPage">&laquo; Previous</li>
            </c:otherwise>
        </c:choose>
        <c:forEach var="pageLink" begin="${startPageLink}" end="${endPageLink}" step="1">
            <c:choose>
                <c:when test="${pageLink == pageNumber}"><li class="currentPage">${pageLink}</li></c:when>
                <c:otherwise><li><a href="${coreParams}&start=${(pageLink * pageSize) - pageSize}${hash}">${pageLink}</a></li></c:otherwise>
            </c:choose>
        </c:forEach>
        <c:choose>
            <c:when test="${!(pageNumber == lastPage)}">
                <li id="nextPage"><a href="${coreParams}&start=${startIndex + pageSize}${hash}">Next &raquo;</a></li>
            </c:when>
            <c:otherwise>
                <li id="nextPage">Next &raquo;</li>
            </c:otherwise>
        </c:choose>
    </ul>
</span>