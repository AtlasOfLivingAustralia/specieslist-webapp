<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>
<c:if test="${regionType.name != 'state'}">

<!-- Start of the Comparison Tool -->
<h2 id="comparisonToolHdr"><spring:message code="regiontype.${regionType.name}.compareTo"/><a name="compare">&nbsp;</a></h2>
<div id="comparisonTable">
    <form><!-- JQuery UI buttons -->
        <div id="compareRegions" class="regionSelect">
        	<select id="regionSelectInput" onchange="javascript:loadTaxaDiffForRegion('${geoRegion.name}', this,'${regionType.name}');" >
        		<c:forEach items="${otherRegions}" var="otherRegion">
        		<c:if test="${otherRegion.name!=geoRegion.name}">
        			<option value="${otherRegion.name}">${otherRegion.name}</option>
        		</c:if>
        		</c:forEach>
        	</select>
        </div>
        <div id="selectedGroup" class="groupSelect">
            <input type="radio" id="birds" name="radio2" onclick="javascript:setSelectedTaxa(this,'birds','Aves','class');" />
            <label for="birds">Birds</label>
            <input type="radio" id="fish" name="radio2" onclick="javascript:setSelectedTaxa(this,'fish','Myxini,Petromyzontida,Chondrichthyes,Sarcopterygii,Actinopterygii','class');" />
            <label for="fish">Fish</label>
            <input type="radio" id="frogs" name="radio2" onclick="javascript:setSelectedTaxa(this,'frogs','Amphibia','class');" />
            <label for="frogs">Frogs</label>
            <input type="radio" id="mammals" name="radio2" checked="checked" onclick="javascript:setSelectedTaxa(this,'mammals','Mammalia','class');" />
            <label for="mammals">Mammals</label>
            <input type="radio" id="reptiles" name="radio2" onclick="javascript:setSelectedTaxa(this,'reptiles','Reptilia','class');" />
            <label for="reptiles">Reptiles</label>
            <input type="radio" id="anthropods" name="radio2" onclick="javascript:setSelectedTaxa(this,'anthropods','Arthropoda','phylum');" />
            <label for="anthropods">Arthropods</label>
            <input type="radio" id="Molluscs" name="radio2" onclick="javascript:setSelectedTaxa(this,'Molluscs','Mollusca','phylum');" />
            <label for="Molluscs">Molluscs</label>       
        </div>
    </form><!-- END JQuery UI buttons -->

    <!-- Two panel display -->
    <table id="taxaDiffComparison">
       <tr>
         <td>
           <h6 id="taxaDiffCount" class="taxaDiffCount"></h6>
           <table id="taxaDiff"></table>
         </td>
         <td>
           <h6 id="taxaDiffCount2" class="taxaDiffCount"></h6>
           <table id="taxaDiff2"></table>
         </td>
       </tr>
    </table>

    <script type="text/javascript"><!--
    // currently selected grouping
    var selectedTaxaSimple = null;
    var selectedTaxa = null;
    var selectedTaxonRank = null;

    var geoRegion = null;
    var geoRegionType = null;
    var compareRegion = null;
    var compareRegionType = null;

    var currentNode = null;
    var initialised = false;

    initPage();

    /**
     * Initialise the page and tool.
     */
    function initPage(){

      selectedTaxaSimple = 'mammals';
      selectedTaxa = 'Mammalia';
      selectedTaxonRank = 'class';

      geoRegion = '${geoRegion.name}'; 
      compareRegion = '${otherRegions[0].name != geoRegion.name ? otherRegions[0].name : otherRegions[1].name}';
      geoRegionType = '${regionType.name}';

      loadTaxaDiff(geoRegion, compareRegion, geoRegionType);
    }

    /**
     * Switch currently selected taxa.
     */
    function setSelectedTaxa(currentNode, commonName, taxa, taxonRank){
      //$('#selectedGroup').children().removeClass("selectedCompareGroup");
      //currentNode.parentNode.className = 'selectedCompareGroup';
      selectedTaxaSimple = commonName;
      selectedTaxa = taxa;
      selectedTaxonRank = taxonRank;
      
      //reload the taxon comparator
      loadTaxaDiff(geoRegion, compareRegion, geoRegionType);
    }

    /**
     * Loads the taxo that are different between states.
     */
    function loadTaxaDiffForRegion(regionName, selectInput, regionType){
  	  compareRegion = selectInput.value;
  	  loadTaxaDiff(regionName, compareRegion, regionType);
    }
    
    /**
     * Loads the taxo that are different between states.
     */
    function loadTaxaDiff(regionName, altRegionName, regionType){

      if(initialised){
          $('#taxaDiff').hide('slow');
          $('#taxaDiff').empty();
      }

      var searchUrl = '${pageContext.request.contextPath}/regions/taxaDiff.json?regionType='+regionType+'&regionName='+regionName+'&altRegionType='+regionType+'&altRegionName='+altRegionName+'&higherTaxon='+selectedTaxa+'&rank='+selectedTaxonRank;
      $.getJSON(searchUrl, function(data) {
        //alert("totalRecords = "+data.searchResults.totalRecords+" | results.length = "+data.searchResults.results.length);
        for(var i=0; i<data.searchResults.results.length; i++){
          var tc = data.searchResults.results[i];
          var commonName = tc.commonNameSingle!=null ? tc.commonNameSingle : '';
          $('#taxaDiff').append('<tr><td><a href="${pageContext.request.contextPath}/species/'+tc.guid+'">'+tc.name+'</td><td>'+commonName+'</td></tr>');
        }
        $('#taxaDiffCount').html('<em>'+regionName+ '</em> has recorded <em>'+data.searchResults.totalRecords+' '+selectedTaxaSimple+'</em> not recorded in <em>'+altRegionName+'</em>');
        $('#taxaDiff').show('fast');
      });

      if(initialised){
          $('#taxaDiff2').hide();
          $('#taxaDiff2').empty();
      }

      var searchUrl = '${pageContext.request.contextPath}/regions/taxaDiff.json?regionType='+regionType+'&regionName='+altRegionName+'&altRegionType='+regionType+'&altRegionName='+regionName+'&higherTaxon='+selectedTaxa+'&rank='+selectedTaxonRank;
      $.getJSON(searchUrl, function(data) {
          for(var i=0; i<data.searchResults.results.length; i++){
            var tc = data.searchResults.results[i];
            var commonName = tc.commonNameSingle!=null ? tc.commonNameSingle : '';
            $('#taxaDiff2').append('<tr><td><a href="${pageContext.request.contextPath}/species/'+tc.guid+'">'+tc.name+'</td><td>'+commonName+'</td></tr>');
          }
          $('#taxaDiffCount2').html('<em>'+altRegionName+ '</em> has recorded <em>'+data.searchResults.totalRecords+' '+selectedTaxaSimple+'</em> not recorded in <em>'+regionName+'</em>');
          $('#taxaDiff2').show('fast');
          if(initialised) window.location.replace("#compare");
          initialised = true;
      });
    }
    --></script>
</div>
</c:if>
