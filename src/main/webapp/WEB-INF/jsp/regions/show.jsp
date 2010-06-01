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
          <a href="${pageContext.request.contextPath}/regions/state/${geoRegion.name}/download?higherTaxon=Aves&rank=class">Download</a>
          <a href="#Birds" id="viewBirdsList">View<c:if test="${birds.totalRecords>100}"> (limited to 100)</c:if></a>
        </c:if>
      </span>
      <ul id="birdsList" class="taxonList">
        <alatag:renderTaxaList taxonConcepts="${birds.taxonConcepts}"/>
      </ul>
     </li>
     
    <!-- FISH -->
    <li id="fishBreakdown" class="taxonBreakdown">
      <span class="taxonGroupTitle">Fish: ${fish.totalRecords}</span>
      <span class="taxonGroupActions">
        <c:if test="${fish.totalRecords>0}">
          <a href="${pageContext.request.contextPath}/regions/state/${geoRegion.name}/download?higherTaxon=Myxini,Petromyzontida,Chondrichthyes,Sarcopterygii,Actinopterygii&rank=class">Download</a>
          <a href="#Fish" id="viewFishList">View<c:if test="${fish.totalRecords>=25}"> (limited to 25)</c:if></a>
        </c:if>
      </span>
      <ul id="fishList" class="taxonList">
        <alatag:renderTaxaList taxonConcepts="${fish.taxonConcepts}"/>
      </ul>
    </li>
    
    <!-- MAMMALS -->
    <li id="mammalsBreakdown" class="taxonBreakdown">
      <span class="taxonGroupTitle">Mammals: ${mammals.totalRecords}</span>
      <span class="taxonGroupActions">
        <c:if test="${mammals.totalRecords>0}">
          <a href="${pageContext.request.contextPath}/regions/state/${geoRegion.name}/download?higherTaxon=Mammalia&rank=class">Download</a>
          <a href="#Mammals" id="viewMammalsList">View<c:if test="${mammals.totalRecords>=25}"> (limited to 25)</c:if></a>
        </c:if>
      </span>
      <ul id="mammalsList" class="taxonList">
        <alatag:renderTaxaList taxonConcepts="${mammals.taxonConcepts}"/>
      </ul>
    </li>
    
    <!-- FROGS -->
    <li id="frogsBreakdown" class="taxonBreakdown">
      <span class="taxonGroupTitle">Frogs: ${frogs.totalRecords}</span>
      <span class="taxonGroupActions">
        <c:if test="${frogs.totalRecords>0}">
          <a href="${pageContext.request.contextPath}/regions/state/${geoRegion.name}/download?higherTaxon=Amphibia&rank=class">Download</a>
          <a href="#Frogs" id="viewFrogsList">View<c:if test="${frogs.totalRecords>=25}"> (limited to 25)</c:if></a>
        </c:if>
      </span>
      <ul id="frogsList" class="taxonList">
        <alatag:renderTaxaList taxonConcepts="${frogs.taxonConcepts}"/>
      </ul>
    </li>
    
    <!-- REPTILES -->
    <li id="reptilesBreakdown" class="taxonBreakdown">
      <span class="taxonGroupTitle">Reptiles: ${reptiles.totalRecords}</span>
      <span class="taxonGroupActions">
        <c:if test="${reptiles.totalRecords>0}">
          <a href="${pageContext.request.contextPath}/regions/state/${geoRegion.name}/download?higherTaxon=Reptilia&rank=class">Download</a>
          <a href="#Reptiles" id="viewReptilesList">View<c:if test="${reptiles.totalRecords>=25}"> (limited to 25)</c:if></a>
        </c:if>
      </span>
      <ul id="reptilesList" class="taxonList">
        <alatag:renderTaxaList taxonConcepts="${reptiles.taxonConcepts}"/>
      </ul>
    </li>
  </ul>
  
  <!-- Start of the Comparison Tool -->
  <h2 id="comparisonToolHdr">Compare biodiversity to other states and territories</h2>
  <div id="comparisonTable">
	  <ul id="selectedGroup" class="tabs groupSelect">
	    <li class="selectedCompareGroup"><a href="#" onclick="javascript:setSelectedTaxa(this,'mammals','Mammalia','class');">Mammals</a></li>
	    <li><a href="#" onclick="javascript:setSelectedTaxa(this,'birds','Aves','class');">Birds</a></li>
	    <li><a href="#" onclick="javascript:setSelectedTaxa(this,'fish','Myxini,Petromyzontida,Chondrichthyes,Sarcopterygii,Actinopterygii','class');">Fish</a></li>
	    <li><a href="#" onclick="javascript:setSelectedTaxa(this,'frogs','Amphibia','class');">Frogs</a></li>
	    <li><a href="#" onclick="javascript:setSelectedTaxa(this,'reptiles','Reptilia','class');">Reptiles</a></li>
	  </ul>
	  <ul id="compareRegions" class="tabs regionSelect">
	    <li><a id="Australian Capital Territory" href="#" onclick="javascript:loadTaxaDiff(this,'state','${geoRegion.name}','state','Australian Capital Territory');">ACT</a></li>
	    <li><a id="New South Wales" href="#" onclick="javascript:loadTaxaDiff(this,'state','${geoRegion.name}','state','New South Wales');">NSW</a></li>
	    <li><a id="Northern Territory" href="#" onclick="javascript:loadTaxaDiff(this,'state','${geoRegion.name}','state','Northern Territory');">NT</a></li>
	    <li><a id="Queensland" href="#" onclick="javascript:loadTaxaDiff(this,'state','${geoRegion.name}','state','Queensland');">QLD</a></li>
	    <li><a id="South Australia" href="#" onclick="javascript:loadTaxaDiff(this,'state','${geoRegion.name}','state','South Australia');">SA</a></li>
	    <li><a id="Tasmania" href="#" onclick="javascript:loadTaxaDiff(this,'state','${geoRegion.name}','state','Tasmania');">TAS</a></li>
	    <li><a id="Victoria" href="#" onclick="javascript:loadTaxaDiff(this,'state','${geoRegion.name}','state','Victoria');">VIC</a></li>
	  </ul>
	  <h6 id="taxaDiffCount"></h6>
	  <ul id="taxaDiff"></ul>
  </div>

<script type="text/javascript"><!--

  // currently selected grouping
  var selectedTaxaSimple = 'mammals';
  var selectedTaxa = 'Mammalia';
  var selectedTaxonRank = 'class';

  var geoRegion = '${geoRegion.name}';
	var geoRegionType = 'state'; //FIXME hardcoded state for now
	var compareRegion = '${geoRegion.name !="Victoria" ? "Victoria" : "Tasmania"}';
	var compareRegionType = 'state';
	
  initPage();

  //default to Victoria for now
  var currentNode = document.getElementById(compareRegion);
  loadTaxaDiff(currentNode, geoRegionType, geoRegion, compareRegionType, compareRegion);

  /**
   * Initialise the page and tool.
   */
  function initPage(){
	  //hide lists initially
	  $('#viewBirdsList').click(function () {
	      $('#birdsList').toggle("slow");
	  });
	  $('#birdsList').hide();

	  $('#viewMammalsList').click(function () {
	    $('#mammalsList').toggle("slow");
	  });
	  $('#mammalsList').hide();
	
	  $('#viewFishList').click(function () {
		    $('#fishList').toggle("slow");
		});
		$('#fishList').hide();
	
	  $('#viewFrogsList').click(function () {
	      $('#frogsList').toggle("slow");
	  });
	  $('#frogsList').hide();
	
	  $('#viewReptilesList').click(function () {
	      $('#reptilesList').toggle("slow");
	  });
	  $('#reptilesList').hide();
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

	  $('#taxaDiff').hide('slow');
	  $('#taxaDiff').empty();
	  
	  var searchUrl = '${pageContext.request.contextPath}/regions/taxaDiff.json?regionType='+regionType+'&regionName='+regionName+'&altRegionType='+altRegionType+'&altRegionName='+altRegionName+'&higherTaxon='+selectedTaxa+'&rank='+selectedTaxonRank;
	  $.getJSON(searchUrl, function(data) {   
		  for(var i=0; i<data.searchResults.taxonConcepts.length; i++){
			  var tc = data.searchResults.taxonConcepts[i];
			  var nameString  = tc.nameString;
			  if(tc.commonName!=null){
				  nameString = nameString + ' (' + tc.commonName + ')';
			  }
			  $('#taxaDiff').append('<li><a href="${pageContext.request.contextPath}/species/'+tc.guid+'">'+nameString+'</a></li>');
		  }
		  $('#taxaDiffCount').html(regionName+ ' has recorded '+data.searchResults.totalRecords+' '+selectedTaxaSimple+' not recorded in '+altRegionName);
		  $('#taxaDiff').show('slow');
		}); 
  }
</script>
</body>

</html>
