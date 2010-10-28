<%@ include file="/common/taglibs.jsp" %><%@ 
attribute name="idSuffix" required="true" type="java.lang.String" rtexprvalue="true" %><%@ 
attribute name="taxonGroup" required="true" type="java.lang.String"  rtexprvalue="true" %><%@ 
attribute name="taxaCount" required="true" type="java.lang.Integer"  rtexprvalue="true" %><%@ 
attribute name="taxonWithImagesCount" required="true" type="java.lang.Integer"  rtexprvalue="true" %><%@ 
attribute name="taxaRecords" required="true" type="java.util.List"  rtexprvalue="true" %><%@ 
attribute name="regionType" required="true" type="java.lang.String"  rtexprvalue="true" %><%@ 
attribute name="regionName" required="true" type="java.lang.String"  rtexprvalue="true" %><%@ 
attribute name="regionAcronym" required="true" type="java.lang.String"  rtexprvalue="true" %><%@ 
attribute name="higherTaxa" required="true" type="java.lang.String"  rtexprvalue="true" %><%@ 
attribute name="rank" required="true" type="java.lang.String"  rtexprvalue="true" %>
<c:if test="${taxaCount>0}">
<li id="${idSuffix}Breakdown" class="taxonBreakdown">
   <span class="taxonGroupTitle">${taxonGroup}: ${taxaCount} (Number with images: ${taxonWithImagesCount})</span>
   <span class="taxonGroupActions">
       <button id="${idSuffix}DL" class="downloadButton">Download</button>
       <a href="#${taxonGroup}" id="view${taxonGroup}List">Show/Hide</a>
   </span>
   <div id="${idSuffix}Gallery">
   
   <c:if test="${taxonWithImagesCount>24}">
   <div class="pagers">
    <a id="${idSuffix}Previous" class="previousPage" href="javascript:show${idSuffix}PreviousPage(this,'${pageContext.request.contextPath}/regions/taxa?regionType=${regionType}&regionName=${regionName}&higherTaxon=${higherTaxa}&rank=${rank}&withImages=true&limit=24');">
        Previous
    </a>
    <a id="${idSuffix}Next" class="nextPage" href="javascript:show${idSuffix}NextPage(this,'${pageContext.request.contextPath}/regions/taxa?regionType=${regionType}&regionName=${regionName}&higherTaxon=${higherTaxa}&rank=${rank}&withImages=true&limit=24');">
        Next
    </a>
    </div>
    </c:if>
    <table id="${idSuffix}List" class="taxonList">
        <alatag:renderTaxaList taxonConcepts="${taxaRecords}"/>
    </table>
   </div>
   <c:if test="${taxonWithImagesCount>24}">
   <script type="text/javascript">
    $('#view${taxonGroup}List').click(function () {
        $('#${idSuffix}Gallery').toggle("slow");
    });
    $('#${idSuffix}Gallery').hide();
    $('button#${idSuffix}DL').click(function (e) {
        var uri = "${pageContext.request.contextPath}/regions/${regionType}/${regionName}/download?title=${taxonGroup}List${regionAcronym}&higherTaxon=${higherTaxa}&rank=${rank}";
        window.location.replace(uri);
    });
    var ${idSuffix}PageCounter = 0;
    var ${idSuffix}MaxPage = Math.ceil(${taxonWithImagesCount}/24);
    function show${idSuffix}NextPage(link, url){
        if(${idSuffix}PageCounter<${idSuffix}MaxPage-1){
        	${idSuffix}PageCounter = ${idSuffix}PageCounter+1;
            var start = ${idSuffix}PageCounter * 24;
            $.get(url+"&start="+start, function(data) {
                $('#${idSuffix}List').html(data);
            });
        }
    }
    function show${idSuffix}PreviousPage(link, url){
        if(${idSuffix}PageCounter>0){
        	${idSuffix}PageCounter = ${idSuffix}PageCounter-1;
            var start = ${idSuffix}PageCounter * 24;
            $.get(url+"&start="+start, function(data) {
                $('#${idSuffix}List').html(data);
            });
        }
    }
   </script>
   </c:if>
</li>
</c:if>