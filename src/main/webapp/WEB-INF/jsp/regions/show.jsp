<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta name="pageName" content="geoRegion"/>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/screen.css" type="text/css" media="screen" charset="utf-8"/>
  <title>Regions - ${geoRegion.regionTypeName} - ${geoRegion.name}</title>
</head>
<body>
    <div id="header"> 
        <div id="breadcrumb">
            <a href="http://test.ala.org.au">Home</a> 
            <a href="http://test.ala.org.au/explore">Explore</a> 
            <a href="${pageContext.request.contextPath}/regions/">Regions</a> 
            <span class="current">${geoRegion.name}</span>
        </div>
        <h1>${geoRegion.name}</h1>
    </div><!--close header-->
    <div id="column-two">
        <div class="section">
            <img src="${pageContext.request.contextPath}/static/images/map_${geoRegion.name}.png" alt="map of ${geoRegion.name}"/>
        </div><!--close section-->
    </div><!--close column-one-->
    <div id="column-one">
        <div class="section">
    <table id="emblems">
  	<tr>
		<td>Animal emblem</td>
		<td>
  			<a id="animalEmblem" href="${pageContext.request.contextPath}/species/${extendedGeoRegion.animalEmblem.guid}">
  				${extendedGeoRegion.animalEmblem.nameString}
	  		</a>
	  	</td>
	  	<td id="animalEmblemText">
		  	${extendedGeoRegion.animalEmblem.nameString}
	  	</td>
  	</tr>
  	<tr>
  		<td>Plant emblem</td> 
  		<td>
  			<a id="plantEmblem" href="${pageContext.request.contextPath}/species/${extendedGeoRegion.plantEmblem.guid}">
  				${extendedGeoRegion.plantEmblem.nameString}
  			</a>
  		</td>
	  	<td id="plantEmblemText">
		  	${extendedGeoRegion.plantEmblem.nameString}
	  	</td>
  	</tr>
  	<tr>
  		<td>Bird emblem</td> 
  		<td>
  			<a id="birdEmblem" href="${pageContext.request.contextPath}/species/${extendedGeoRegion.birdEmblem.guid}">
  				${extendedGeoRegion.birdEmblem.nameString}
  			</a>
  		</td>
	  	<td id="birdEmblemText">
		  	${extendedGeoRegion.birdEmblem.nameString}
	  	</td>
  	</tr>
  </table>
  
  <script type="text/javascript">
        var searchUrl = '${pageContext.request.contextPath}/species/info/${extendedGeoRegion.animalEmblem.guid}.json';
        $.getJSON(searchUrl, function(data) {
            $('#animalEmblem').html('<img src="'+data.taxonConcept.thumbnail+'"/>');
            $('#animalEmblemText').html(data.taxonConcept.commonNameSingle+" (<i>"+data.taxonConcept.name+"</i>)");
        });  
        searchUrl = '${pageContext.request.contextPath}/species/info/${extendedGeoRegion.plantEmblem.guid}.json';
        $.getJSON(searchUrl, function(data) {
            $('#plantEmblem').html('<img src="'+data.taxonConcept.thumbnail+'"/>');
            $('#plantEmblemText').html(data.taxonConcept.commonNameSingle+" (<i>"+data.taxonConcept.name+"</i>)");
        });  
        searchUrl = '${pageContext.request.contextPath}/species/info/${extendedGeoRegion.birdEmblem.guid}.json';
        $.getJSON(searchUrl, function(data) {
            $('#birdEmblem').html('<img src="'+data.taxonConcept.thumbnail+'"/>');
            $('#birdEmblemText').html(data.taxonConcept.commonNameSingle+" (<i>"+data.taxonConcept.name+"</i>)");
        });  
  </script>
  </div><!--close section-->
    </div><!--close column-one-->
    <div id="column-one" class="full-width">
        <div class="section">
  <ul id="taxonGroups">
  
    <!-- BIRDS -->
    <li id="birdsBreakdown" class="taxonBreakdown">
      <span class="taxonGroupTitle">Birds: ${birds.totalRecords}</span>
      <span class="taxonGroupActions">
        <c:if test="${birds.totalRecords>0}">
          <a href="${pageContext.request.contextPath}/regions/state/${geoRegion.name}/download?title=BirdsList${geoRegion.acronym}&higherTaxon=Aves&rank=class">Download</a>
          <a href="#Birds" id="viewBirdsList">Show/Hide<c:if test="${birds.totalRecords>25}"> (limited to 25)</c:if></a>
        </c:if>
      </span>
      <table id="birdsList" class="taxonList">
        <alatag:renderTaxaList taxonConcepts="${birds.results}"/>
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
          <a href="#Fish" id="viewFishList">Show/Hide<c:if test="${fish.totalRecords>=25}"> (limited to 25)</c:if></a>
        </c:if>
      </span>
      <table id="fishList" class="taxonList">
        <alatag:renderTaxaList taxonConcepts="${fish.results}"/>
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
          <a href="#Frogs" id="viewFrogsList">Show/Hide<c:if test="${frogs.totalRecords>=25}"> (limited to 25)</c:if></a>
        </c:if>
      </span>
      <table id="frogsList" class="taxonList">
        <alatag:renderTaxaList taxonConcepts="${frogs.results}"/>
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
          <a href="#Mammals" id="viewMammalsList">Show/Hide<c:if test="${mammals.totalRecords>=25}"> (limited to 25)</c:if></a>
        </c:if>
      </span>
      <table id="mammalsList" class="taxonList">
        <alatag:renderTaxaList taxonConcepts="${mammals.results}"/>
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
          <a href="#Reptiles" id="viewReptilesList">Show/Hide<c:if test="${reptiles.totalRecords>=25}"> (limited to 25)</c:if></a>
        </c:if>
      </span>
      <table id="reptilesList" class="taxonList">
        <alatag:renderTaxaList taxonConcepts="${reptiles.results}"/>
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
          for(var i=0; i<data.searchResults.results.length; i++){
            var tc = data.searchResults.results[i];
            var commonName = tc.commonNameSingle!=null ? tc.commonNameSingle : ''; 
            $('#taxaDiff').append('<tr><td><a href="${pageContext.request.contextPath}/species/'+tc.guid+'">'+tc.name+'</td><td>'+commonName+'</td></tr>');
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
            for(var i=0; i<data.searchResults.results.length; i++){
              var tc = data.searchResults.results[i];
              var commonName = tc.commonNameSingle!=null ? tc.commonNameSingle : ''; 
              $('#taxaDiff2').append('<tr><td><a href="${pageContext.request.contextPath}/species/'+tc.guid+'">'+tc.name+'</td><td>'+commonName+'</td></tr>');
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
</div>
    </div>
</body>

</html>
