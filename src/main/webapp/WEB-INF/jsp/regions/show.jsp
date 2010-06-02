<%@ page contentType="text/html" pageEncoding="UTF-8" %><%@ 
taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ 
taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%@ 
taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%@ 
taglib tagdir="/WEB-INF/tags" prefix="alatag" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta name="pageName" content="geoRegion"/>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Regions - ${geoRegion.regionTypeName} - ${geoRegion.name}</title>
</head>
<body>
  <h1><a href="${pageContext.request.contextPath}/regions/">Regions</a> &gt; ${geoRegion.regionTypeName} &gt; Explore ${geoRegion.name}</h1>
  <ul id="taxonGroups">
  
    <!-- BIRDS -->
    <li id="birdsBreakdown" class="taxonBreakdown">
      <span class="taxonGroupTitle">Birds: ${birds.totalRecords}</span>
      <span class="taxonGroupActions">
        <c:if test="${birds.totalRecords>0}">
          <a href="${pageContext.request.contextPath}/regions/state/${geoRegion.name}/download?title=BirdsList${geoRegion.acronym}&higherTaxon=Aves&rank=class">Download</a>
          <a href="#Birds" id="viewBirdsList">View<c:if test="${birds.totalRecords>25}"> (limited to 25)</c:if></a>
        </c:if>
      </span>
      <table id="birdsList" class="taxonList">
        <alatag:renderTaxaList taxonConcepts="${birds.taxonConcepts}"/>
      </table>
      <script type="text/javascript">
        $('#viewBirdsList').click(function () {
          $('#birdsList').toggle("slow");
        });
        $('#birdsList').hide();
      </script>
     </li>
     
    <!-- FISH -->
    <li id="fishBreakdown" class="taxonBreakdown">
      <span class="taxonGroupTitle">Fish: ${fish.totalRecords}</span>
      <span class="taxonGroupActions">
        <c:if test="${fish.totalRecords>0}">
          <a href="${pageContext.request.contextPath}/regions/state/${geoRegion.name}/download?title=FishList${geoRegion.acronym}&higherTaxon=Myxini,Petromyzontida,Chondrichthyes,Sarcopterygii,Actinopterygii&rank=class">Download</a>
          <a href="#Fish" id="viewFishList">View<c:if test="${fish.totalRecords>=25}"> (limited to 25)</c:if></a>
        </c:if>
      </span>
      <table id="fishList" class="taxonList">
        <alatag:renderTaxaList taxonConcepts="${fish.taxonConcepts}"/>
      </table>
      <script type="text/javascript">
        $('#viewFishList').click(function () {
          $('#fishList').toggle("slow");
        });
        $('#fishList').hide();
      </script>
    </li>
    
    <!-- FROGS -->
    <li id="frogsBreakdown" class="taxonBreakdown">
      <span class="taxonGroupTitle">Frogs: ${frogs.totalRecords}</span>
      <span class="taxonGroupActions">
        <c:if test="${frogs.totalRecords>0}">
          <a href="${pageContext.request.contextPath}/regions/state/${geoRegion.name}/download?title=FrogsList${geoRegion.acronym}&higherTaxon=Amphibia&rank=class">Download</a>
          <a href="#Frogs" id="viewFrogsList">View<c:if test="${frogs.totalRecords>=25}"> (limited to 25)</c:if></a>
        </c:if>
      </span>
      <table id="frogsList" class="taxonList">
        <alatag:renderTaxaList taxonConcepts="${frogs.taxonConcepts}"/>
      </table>
      <script type="text/javascript">
        $('#viewFrogsList').click(function () {
          $('#frogsList').toggle("slow");
        });
        $('#frogsList').hide();
      </script>
    </li>
    
    <!-- MAMMALS -->
    <li id="mammalsBreakdown" class="taxonBreakdown">
      <span class="taxonGroupTitle">Mammals: ${mammals.totalRecords}</span>
      <span class="taxonGroupActions">
        <c:if test="${mammals.totalRecords>0}">
          <a href="${pageContext.request.contextPath}/regions/state/${geoRegion.name}/download?title=MammalsList${geoRegion.acronym}&higherTaxon=Mammalia&rank=class">Download</a>
          <a href="#Mammals" id="viewMammalsList">View<c:if test="${mammals.totalRecords>=25}"> (limited to 25)</c:if></a>
        </c:if>
      </span>
      <table id="mammalsList" class="taxonList">
        <alatag:renderTaxaList taxonConcepts="${mammals.taxonConcepts}"/>
      </table>
      <script type="text/javascript">
        $('#viewMammalsList').click(function () {
          $('#mammalsList').toggle("slow");
        });
        $('#mammalsList').hide();
      </script>
    </li>
    
    <!-- REPTILES -->
    <li id="reptilesBreakdown" class="taxonBreakdown">
      <span class="taxonGroupTitle">Reptiles: ${reptiles.totalRecords}</span>
      <span class="taxonGroupActions">
        <c:if test="${reptiles.totalRecords>0}">
          <a href="${pageContext.request.contextPath}/regions/state/${geoRegion.name}/download?title=ReptilesList${geoRegion.acronym}&higherTaxon=Reptilia&rank=class">Download</a>
          <a href="#Reptiles" id="viewReptilesList">View<c:if test="${reptiles.totalRecords>=25}"> (limited to 25)</c:if></a>
        </c:if>
      </span>
      <table id="reptilesList" class="taxonList">
        <alatag:renderTaxaList taxonConcepts="${reptiles.taxonConcepts}"/>
      </table>
      <script type="text/javascript">
        $('#viewReptilesList').click(function () {
          $('#reptilesList').toggle("slow");
        });
        $('#reptilesList').hide();
      </script>
    </li>
  </ul>
  
  <!-- Start of the Comparison Tool -->
  <h2 id="comparisonToolHdr">Compare biodiversity to other states and territories</h2>
  <div id="comparisonTable">
	  <ul id="selectedGroup" class="groupSelect">
	    <li><a href="#" onclick="javascript:setSelectedTaxa(this,'birds','Aves','class');">Birds</a></li>
	    <li><a href="#" onclick="javascript:setSelectedTaxa(this,'fish','Myxini,Petromyzontida,Chondrichthyes,Sarcopterygii,Actinopterygii','class');">Fish</a></li>
	    <li><a href="#" onclick="javascript:setSelectedTaxa(this,'frogs','Amphibia','class');">Frogs</a></li>
	    <li class="selectedCompareGroup"><a href="#" onclick="javascript:setSelectedTaxa(this,'mammals','Mammalia','class');">Mammals</a></li>
	    <li><a href="#" onclick="javascript:setSelectedTaxa(this,'reptiles','Reptilia','class');">Reptiles</a></li>
	  </ul>
	  <ul id="compareRegions" class="regionSelect">
	    <li><a id="Australian Capital Territory" href="#" onclick="javascript:loadTaxaDiff(this,'state','${geoRegion.name}','state','Australian Capital Territory');">ACT</a></li>
	    <li><a id="New South Wales" href="#" onclick="javascript:loadTaxaDiff(this,'state','${geoRegion.name}','state','New South Wales');">NSW</a></li>
	    <li><a id="Northern Territory" href="#" onclick="javascript:loadTaxaDiff(this,'state','${geoRegion.name}','state','Northern Territory');">NT</a></li>
	    <li><a id="Queensland" href="#" onclick="javascript:loadTaxaDiff(this,'state','${geoRegion.name}','state','Queensland');">QLD</a></li>
	    <li><a id="South Australia" href="#" onclick="javascript:loadTaxaDiff(this,'state','${geoRegion.name}','state','South Australia');">SA</a></li>
	    <li><a id="Tasmania" href="#" onclick="javascript:loadTaxaDiff(this,'state','${geoRegion.name}','state','Tasmania');">TAS</a></li>
	    <li><a id="Victoria" href="#" onclick="javascript:loadTaxaDiff(this,'state','${geoRegion.name}','state','Victoria');">VIC</a></li>
	  </ul>
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
      <script type="text/javascript">
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
        geoRegionType = 'state'; //FIXME hardcoded state for now
        compareRegion = '${geoRegion.name !="Victoria" ? "Victoria" : "Tasmania"}';
        compareRegionType = 'state';
          
        //default to Victoria for now
        currentNode = document.getElementById(compareRegion);
        loadTaxaDiff(currentNode, geoRegionType, geoRegion, compareRegionType, compareRegion);
      }

      /**
       * Switch currently selected taxa.
       */
      function setSelectedTaxa(currentNode, commonName, taxa, taxonRank){
        $('#selectedGroup').children().removeClass("selectedCompareGroup");
        currentNode.parentNode.className = 'selectedCompareGroup';
        selectedTaxaSimple = commonName;
        selectedTaxa = taxa;
        selectedTaxonRank = taxonRank;
        //reload the taxon comparator
        loadTaxaDiff(currentNode, geoRegionType, geoRegion, compareRegionType, compareRegion);
      }

      /**
       * Loads the taxo that are different between states.
       */
      function loadTaxaDiff(currentNode, regionType, regionName, altRegionType, altRegionName){

        $('#compareRegions').children().removeClass("selectedCompareGroup");
        currentNode.parentNode.className = 'selectedCompareGroup';

        if(initialised){
            $('#taxaDiff').hide('slow');
        	  $('#taxaDiff').empty();
        }
        
        var searchUrl = '${pageContext.request.contextPath}/regions/taxaDiff.json?regionType='+regionType+'&regionName='+regionName+'&altRegionType='+altRegionType+'&altRegionName='+altRegionName+'&higherTaxon='+selectedTaxa+'&rank='+selectedTaxonRank;
        $.getJSON(searchUrl, function(data) { 
          for(var i=0; i<data.searchResults.taxonConcepts.length; i++){
            var tc = data.searchResults.taxonConcepts[i];
            var commonName = tc.commonName!=null ? tc.commonName : ''; 
            $('#taxaDiff').append('<tr><td><a href="${pageContext.request.contextPath}/species/'+tc.guid+'">'+tc.nameString+'</td><td>'+commonName+'</td></tr>');
          }
          $('#taxaDiffCount').html('<em>'+regionName+ '</em> has recorded <em>'+data.searchResults.totalRecords+' '+selectedTaxaSimple+'</em> not recorded in <em>'+altRegionName+'</em>');
          $('#taxaDiff').show('slow');
        }); 

        if(initialised){
        	 $('#taxaDiff2').hide('slow');
           $('#taxaDiff2').empty();
        }
        
        var searchUrl = '${pageContext.request.contextPath}/regions/taxaDiff.json?regionType='+altRegionType+'&regionName='+altRegionName+'&altRegionType='+regionType+'&altRegionName='+regionName+'&higherTaxon='+selectedTaxa+'&rank='+selectedTaxonRank;
        $.getJSON(searchUrl, function(data) {
            for(var i=0; i<data.searchResults.taxonConcepts.length; i++){
              var tc = data.searchResults.taxonConcepts[i];
              var commonName = tc.commonName!=null ? tc.commonName : ''; 
              $('#taxaDiff2').append('<tr><td><a href="${pageContext.request.contextPath}/species/'+tc.guid+'">'+tc.nameString+'</td><td>'+commonName+'</td></tr>');
            }
            $('#taxaDiffCount2').html('<em>'+altRegionName+ '</em> has recorded <em>'+data.searchResults.totalRecords+' '+selectedTaxaSimple+'</em> not recorded in <em>'+regionName+'</em>');
            $('#taxaDiff2').show('slow');
        }); 

        initialised = true;
      }
      </script>
  </div>

<script type="text/javascript"><!--


</script>
</body>

</html>
