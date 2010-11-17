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
   
   <table class="taxonGroupElement">
   	<tr>
		<td class="taxonGroupCell">
			<span class="taxonGroupTitle">${taxonGroup}: ${taxaCount} (Number with images: ${taxonWithImagesCount})</span>
		</td>
		<td class="showHideCell">
			<button id="view${taxonGroup}List" class="downloadButton">Show/Hide</button>
		</td>	
		<td class="downloadCell">
			<button id="${idSuffix}DL" class="downloadButton">Download</button>
		</td>
	</tr>
   </table>
   
   <div id="${idSuffix}Gallery">
   
   <c:if test="${taxonWithImagesCount>24}">
   <div class="pagers">
    <span class="previousPage">
    <span class="pager">&#x276E;</span>
    <a id="${idSuffix}Previous" href="javascript:show${idSuffix}PreviousPage(this,'${pageContext.request.contextPath}/regions/taxa?regionType=${regionType}&regionName=${regionName}&higherTaxon=${higherTaxa}&rank=${rank}&withImages=true&limit=24');">Previous</a>
    </span>
    <span id="${idSuffix}Loading" class="loadingMessage">Loading...</span>
    <span class="nextPage">
    <a id="${idSuffix}Next"  href="javascript:show${idSuffix}NextPage(this,'${pageContext.request.contextPath}/regions/taxa?regionType=${regionType}&regionName=${regionName}&higherTaxon=${higherTaxa}&rank=${rank}&withImages=true&limit=24');">Next</a> 
    <span class="pager">&#x276F;</span>
    </span>
    </div>
    </c:if>
    
    <table id="${idSuffix}List" class="taxonList">
        
    </table>
   </div>

   <script type="text/javascript">

   var ${idSuffix}InitialLoad = false;
   
    $('#view${taxonGroup}List').click(function () {
		if(!${idSuffix}InitialLoad){
			${idSuffix}InitialLoad = true;		
            $('#${idSuffix}Loading').show();
            $.get("${pageContext.request.contextPath}/regions/taxa?regionType=${regionType}&regionName=${regionName}&higherTaxon=${higherTaxa}&rank=${rank}&withImages=true&limit=24&start=0", function(data) {
                $('#${idSuffix}List').html(data);
                $('#${idSuffix}Loading').hide();
            });
		}
        $('#${idSuffix}Gallery').toggle("slow");
    });
    $('#${idSuffix}Loading').hide();
    $('#${idSuffix}Gallery').hide();
    $('button#${idSuffix}DL').click(function (e) {
        var uri = "${pageContext.request.contextPath}/regions/${regionType}/${regionName}/download?title=${taxonGroup}List${regionAcronym}&higherTaxon=${higherTaxa}&rank=${rank}";
        window.location.replace(uri);
    });

    <c:if test="${taxonWithImagesCount>24}">
    var ${idSuffix}PageCounter = 0;
    var ${idSuffix}MaxPage = Math.ceil(${taxonWithImagesCount}/24);
    function show${idSuffix}NextPage(link, url){
        if(${idSuffix}PageCounter<${idSuffix}MaxPage-1){
        	${idSuffix}PageCounter = ${idSuffix}PageCounter+1;
            var start = ${idSuffix}PageCounter * 24;
            $('#${idSuffix}Loading').show();
            $.get(url+"&start="+start, function(data) {
                $('#${idSuffix}List').html(data);
                $('#${idSuffix}Loading').hide();
            });
        }
    }
    function show${idSuffix}PreviousPage(link, url){
        if(${idSuffix}PageCounter>0){
        	${idSuffix}PageCounter = ${idSuffix}PageCounter-1;
            var start = ${idSuffix}PageCounter * 24;
            $('#${idSuffix}Loading').show();
            $.get(url+"&start="+start, function(data) {
                $('#${idSuffix}List').html(data);
                $('#${idSuffix}Loading').hide();
            });
        }
    }
    </c:if>
   </script>
</li>
</c:if>