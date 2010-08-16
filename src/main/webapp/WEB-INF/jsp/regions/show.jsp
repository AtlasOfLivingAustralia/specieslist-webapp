<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta name="pageName" content="geoRegion"/>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery-ui-1.8.custom.min.js"></script>
  <link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/static/css/bie-theme/jquery-ui-1.8.custom.css" charset="utf-8">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/screen.css" type="text/css" media="screen" charset="utf-8"/>
  <title>Regions | ${geoRegion.regionTypeName} | ${geoRegion.name}</title>
  <script type="text/javascript">
        $(document).ready(function() {
            // JQuery UI buttons
            $(".downloadButton").button();
            $("#selectedGroup").buttonset();
            $("#compareRegions").buttonset();
        });
   </script>
</head>
<body>
    <div id="header">
        <div id="breadcrumb">
            <a href="http://test.ala.org.au">Home</a>
            <a href="http://test.ala.org.au/explore">Explore</a>
            <a href="${pageContext.request.contextPath}/regions/">Regions</a>
            <a href="${pageContext.request.contextPath}/regions/"><spring:message code="regiontype.${regionType.name}"/></a>
            <span class="current">${geoRegion.name}</span>
        </div>
        <h1>${geoRegion.name}</h1>
    </div><!--close header-->
    <div id="column-two">
    
   	<c:if test="${regionType.name == 'state'}">
        <div class="section">
            <img src="${pageContext.request.contextPath}/static/images/map_${geoRegion.name}.png" alt="map of ${geoRegion.name}" id="regionMap"/>
        </div><!--close section-->
    </c:if>
    
    </div><!--close column-one-->
    <div id="column-one">
            <table id="emblems">
               <c:if test="${not empty extendedGeoRegion.animalEmblem}">
                <tr>
                    <td>
                        <a id="animalEmblem" href="${pageContext.request.contextPath}/species/${extendedGeoRegion.animalEmblem.guid}">
                            <img src="${pageContext.request.contextPath}/static/images/noImage85.jpg" alt="${extendedGeoRegion.animalEmblem.nameString}"/>
                        </a>
                    </td>
                    <td>
                        <h3>Animal emblem</h3>
                        <div id="animalEmblemText">${extendedGeoRegion.animalEmblem.nameString}</div>
                    </td>
                </tr>
                </c:if>
               <c:if test="${not empty extendedGeoRegion.plantEmblem}">
                <tr>
                    <td>
                        <a id="plantEmblem" href="${pageContext.request.contextPath}/species/${extendedGeoRegion.plantEmblem.guid}">
                            <img src="${pageContext.request.contextPath}/static/images/noImage85.jpg" alt="${extendedGeoRegion.plantEmblem.nameString}"/>
                        </a>
                    </td>
                    <td>
                        <h3>Plant emblem</h3>
                        <div id="plantEmblemText">${extendedGeoRegion.plantEmblem.nameString}</div>
                    </td>
                </tr>
                </c:if>
                <c:if test="${not empty extendedGeoRegion.birdEmblem}">
                <tr>
                    <td>
                        <a id="birdEmblem" href="${pageContext.request.contextPath}/species/${extendedGeoRegion.birdEmblem.guid}">
                            <img src="${pageContext.request.contextPath}/static/images/noImage85.jpg" alt="${extendedGeoRegion.birdEmblem.nameString}"/>
                        </a>
                    </td>
                    <td>
                        <h3>Bird emblem</h3>
                        <div id="birdEmblemText">${extendedGeoRegion.birdEmblem.nameString}</div>
                    </td>
                </tr>
                </c:if>
                <c:if test="${not empty extendedGeoRegion.marineEmblem}">
                <tr>
                    <td>
                        <a id="marineEmblem" href="${pageContext.request.contextPath}/species/${extendedGeoRegion.marineEmblem.guid}">
                            <img src="${pageContext.request.contextPath}/static/images/noImage85.jpg" alt="${extendedGeoRegion.marineEmblem.nameString}"/>
                        </a>
                    </td>
                    <td>
                        <h3>Marine emblem</h3>
                        <div id="marineEmblemText">${extendedGeoRegion.marineEmblem.nameString}</div>
                    </td>
                </tr>
                </c:if>
            </table>

  <script type="text/javascript">
        
        <c:if test="${not empty extendedGeoRegion.animalEmblem}">
	        setupEmblem('${pageContext.request.contextPath}/species/moreInfo/${extendedGeoRegion.animalEmblem.guid}.json', 'animalEmblem', 'animalEmblemText');
        </c:if>
        <c:if test="${not empty extendedGeoRegion.plantEmblem}">
	        setupEmblem('${pageContext.request.contextPath}/species/moreInfo/${extendedGeoRegion.plantEmblem.guid}.json', 'plantEmblem', 'plantEmblemText');
	    </c:if>
        <c:if test="${not empty extendedGeoRegion.birdEmblem}">
	        setupEmblem('${pageContext.request.contextPath}/species/moreInfo/${extendedGeoRegion.birdEmblem.guid}.json', 'birdEmblem', 'birdEmblemText');        
        </c:if>
        <c:if test="${not empty extendedGeoRegion.marineEmblem}">
        	setupEmblem('${pageContext.request.contextPath}/species/moreInfo/${extendedGeoRegion.marineEmblem.guid}.json', 'marineEmblem', 'marineEmblemText');        
    	</c:if>
        
        function setupEmblem(searchUrl, imgTag, textTag){
          $.getJSON(searchUrl, function(data) {
        	if(data.images!=null && data.images.length>0){
	            $('#'+imgTag).html('<img src="'+data.images[0].thumbnail+'" class="emblemThumb" alt="'+data.taxonConcept.nameString+' image"/>');
	        }
            if(data.commonNames!=null && data.commonNames.length>0){
	            $('#'+textTag).html(data.commonNames[0].nameString+" (<i>"+data.taxonConcept.nameString+"</i>)");
	        } else {
		        $('#'+textTag).html("<i>"+data.taxonConcept.nameString+"</i>");
	        }
          });
        }
        
  </script>
  
    </div><!--close column-one-->
    <div id="column-one" class="full-width">
        <div class="section">
  <ul id="taxonGroups">

    <!-- BIRDS -->
    <li id="birdsBreakdown" class="taxonBreakdown">
        <c:choose>
            <c:when test="${birds.totalRecords>0}">
                <span class="taxonGroupTitle">Birds: ${birdCount} (Number with images: ${birds.totalRecords})</span>
                <span class="taxonGroupActions">
                    <button id="birdsDL" class="downloadButton">Download</button>
                    <a href="#Birds" id="viewBirdsList">Show/Hide<c:if test="${birds.totalRecords>25}"> (limited to 25)</c:if></a>
                </span>
                <table id="birdsList" class="taxonList">
                    <alatag:renderTaxaList taxonConcepts="${birds.results}"/>
                </table>
                <script type="text/javascript">
                    $('#viewBirdsList').click(function () {
                        $('#birdsList').toggle("slow");
                    });
                    $('#birdsList').hide();
                    $('button#birdsDL').click(function (e) {
                        var uri = "${pageContext.request.contextPath}/regions/${regionType.name}/${geoRegion.name}/download?title=BirdsList${geoRegion.acronym}&higherTaxon=Amphibia&rank=class";
                        window.location.replace(uri);
                    });
                </script>
            </c:when>
            <c:otherwise>
                <span class="taxonGroupTitle">Birds: ${birds.totalRecords}</span>
            </c:otherwise>
        </c:choose>
    </li>

    <!-- FISH -->
    <li id="fishBreakdown" class="taxonBreakdown">
        <c:choose>
            <c:when test="${fish.totalRecords>0}">
                <span class="taxonGroupTitle">Fish: ${fishCount} (Number with images:${fish.totalRecords})</span>
                <span class="taxonGroupActions">
                    <button id="fishDL" class="downloadButton">Download</button>
                    <a href="#Fish" id="viewFishList">Show/Hide<c:if test="${fish.totalRecords>25}"> (limited to 25)</c:if></a>
                </span>
                <table id="fishList" class="taxonList">
                    <alatag:renderTaxaList taxonConcepts="${fish.results}"/>
                </table>
                <script type="text/javascript">
                    $('#viewFishList').click(function () {
                        $('#fishList').toggle("slow");
                    });
                    $('#fishList').hide();
                    $('button#fishDL').click(function (e) {
                        var uri = "${pageContext.request.contextPath}/regions/${regionType.name}/${geoRegion.name}/download?title=FishList${geoRegion.acronym}&higherTaxon=Myxini,Petromyzontida,Chondrichthyes,Sarcopterygii,Actinopterygii&rank=class";
                        window.location.replace(uri);
                    });
                </script>
            </c:when>
            <c:otherwise>
                <span class="taxonGroupTitle">Fish: ${fish.totalRecords}</span>
            </c:otherwise>
        </c:choose>
    </li>

    <!-- FROGS -->
    <li id="frogsBreakdown" class="taxonBreakdown">
        <c:choose>
            <c:when test="${frogs.totalRecords>0}">
                <span class="taxonGroupTitle">Frogs: ${frogCount} (Number with images:${frogs.totalRecords})</span>
                <span class="taxonGroupActions">
                    <button id="frogsDL" class="downloadButton">Download</button>
                    <a href="#Frogs" id="viewFrogsList">Show/Hide<c:if test="${frogs.totalRecords>25}"> (limited to 25)</c:if></a>
                </span>
                <table id="frogsList" class="taxonList">
                    <alatag:renderTaxaList taxonConcepts="${frogs.results}"/>
                </table>
                <script type="text/javascript">
                    $('#viewFrogsList').click(function () {
                        $('#frogsList').toggle("slow");
                    });
                    $('#frogsList').hide();
                    $('button#frogsDL').click(function (e) {
                        var uri = "${pageContext.request.contextPath}/regions/${regionType.name}/${geoRegion.name}/download?title=FrogsList${geoRegion.acronym}&higherTaxon=Amphibia&rank=class";
                        window.location.replace(uri);
                    });
                </script>
            </c:when>
            <c:otherwise>
                <span class="taxonGroupTitle">Frogs: ${frogs.totalRecords}</span>
            </c:otherwise>
        </c:choose>
    </li>

    <!-- MAMMALS -->
    <li id="mammalsBreakdown" class="taxonBreakdown">
        <c:choose>
            <c:when test="${mammals.totalRecords>0}">
                <span class="taxonGroupTitle">Mammals: ${mammalCount} (Number with images:${mammals.totalRecords})</span>
                <span class="taxonGroupActions">
                    <button id="mammalsDL" class="downloadButton">Download</button>
                    <a href="#Mammals" id="viewMammalsList">Show/Hide<c:if test="${mammals.totalRecords>25}"> (limited to 25)</c:if></a>
                </span>
                <table id="mammalsList" class="taxonList">
                    <alatag:renderTaxaList taxonConcepts="${mammals.results}"/>
                </table>
                <script type="text/javascript">
                    $('#viewMammalsList').click(function () {
                        $('#mammalsList').toggle("slow");
                    });
                    $('#mammalsList').hide();
                    $('button#mammalsDL').click(function (e) {
                        var uri = "${pageContext.request.contextPath}/regions/${regionType.name}/${geoRegion.name}/download?title=MammalsList${geoRegion.acronym}&higherTaxon=Mammalia&rank=class";
                        window.location.replace(uri);
                    });
                </script>
            </c:when>
            <c:otherwise>
                <span class="taxonGroupTitle">Mammals: ${mammals.totalRecords}</span>
            </c:otherwise>
        </c:choose>
    </li>

    <!-- REPTILES -->
    <li id="reptilesBreakdown" class="taxonBreakdown">
        <c:choose>
            <c:when test="${reptiles.totalRecords>0}">
                <span class="taxonGroupTitle">Reptiles: ${reptileCount} (Number with images:${reptiles.totalRecords})</span>
                <span class="taxonGroupActions">
                    <button id="reptilesDL" class="downloadButton">Download</button>
                    <a href="#Reptiles" id="viewReptilesList">Show/Hide<c:if test="${reptiles.totalRecords>25}"> (limited to 25)</c:if></a>
                </span>
                <table id="reptilesList" class="taxonList">
                    <alatag:renderTaxaList taxonConcepts="${reptiles.results}"/>
                </table>
                <script type="text/javascript">
                    $('#viewReptilesList').click(function () {
                        $('#reptilesList').toggle("slow");
                    });
                    $('#reptilesList').hide();
                    $('button#reptilesDL').click(function (e) {
                        var uri = "${pageContext.request.contextPath}/regions/${regionType.name}/${geoRegion.name}/download?title=ReptilesList${geoRegion.acronym}&higherTaxon=Reptilia&rank=class";
                        window.location.replace(uri);
                    });
                </script>
            </c:when>
            <c:otherwise>
                <span class="taxonGroupTitle">Reptiles: ${reptiles.totalRecords}</span>
            </c:otherwise>
        </c:choose>
    </li>
    
    <!-- ARTHROPODS -->
    <li id="arthropodsBreakdown" class="taxonBreakdown">
        <c:choose>
            <c:when test="${arthropods.totalRecords>0}">
                <span class="taxonGroupTitle">Arthropods: ${arthropodCount} (Number with images:${arthropods.totalRecords})</span>
                <span class="taxonGroupActions">
                    <button id="arthropodsDL" class="downloadButton">Download</button>
                    <a href="#arthropods" id="viewArthropodsList">Show/Hide<c:if test="${arthropods.totalRecords>25}"> (limited to 25)</c:if></a>
                </span>
                <table id="arthropodsList" class="taxonList">
                    <alatag:renderTaxaList taxonConcepts="${arthropods.results}"/>
                </table>
                <script type="text/javascript">
                    $('#viewArthropodsList').click(function () {
                        $('#arthropodsList').toggle("slow");
                    });
                    $('#arthropodsList').hide();
                    $('button#arthropodsDL').click(function (e) {
                        var uri = "${pageContext.request.contextPath}/regions/${regionType.name}/${geoRegion.name}/download?title=arthropodsList${geoRegion.acronym}&higherTaxon=Arthropoda&rank=phylum";
                        window.location.replace(uri);
                    });
                </script>
            </c:when>
            <c:otherwise>
                <span class="taxonGroupTitle">Arthropods: ${arthropods.totalRecords}</span>
            </c:otherwise>
        </c:choose>
    </li>
    
    <!-- MOLLUSCS-->
    <li id="molluscsBreakdown" class="taxonBreakdown">
        <c:choose>
            <c:when test="${molluscs.totalRecords>0}">
                <span class="taxonGroupTitle">Molluscs: ${molluscCount} (Number with images:${molluscs.totalRecords})</span>
                <span class="taxonGroupActions">
                    <button id="molluscsDL" class="downloadButton">Download</button>
                    <a href="#molluscs" id="viewMolluscsList">Show/Hide<c:if test="${molluscs.totalRecords>25}"> (limited to 25)</c:if></a>
                </span>
                <table id="molluscsList" class="taxonList">
                    <alatag:renderTaxaList taxonConcepts="${molluscs.results}"/>
                </table>
                <script type="text/javascript">
                    $('#viewMolluscsList').click(function () {
                        $('#molluscsList').toggle("slow");
                    });
                    $('#molluscsList').hide();
                    $('button#molluscsDL').click(function (e) {
                        var uri = "${pageContext.request.contextPath}/regions/${regionType.name}/${geoRegion.name}/download?title=MolluscsList${geoRegion.acronym}&higherTaxon=Mollusca&rank=phylum";
                        window.location.replace(uri);
                    });
                </script>
            </c:when>
            <c:otherwise>
                <span class="taxonGroupTitle">Molluscs: ${molluscs.totalRecords}</span>
            </c:otherwise>
        </c:choose>
    </li>
    
    <!-- ANGIOSPERMS -->
    <li id="angiospermsBreakdown" class="taxonBreakdown">
        <c:choose>
            <c:when test="${angiosperms.totalRecords>0}">
                <span class="taxonGroupTitle">Flowering plants::${angiospermCount} (Number with images:${angiosperms.totalRecords})</span>
                <span class="taxonGroupActions">
                    <button id="angiospermsDL" class="downloadButton">Download</button>
                    <a href="#angiosperms" id="viewAngiospermsList">Show/Hide<c:if test="${angiosperms.totalRecords>25}"> (limited to 25)</c:if></a>
                </span>
                <table id="angiospermsList" class="taxonList">
                    <alatag:renderTaxaList taxonConcepts="${angiosperms.results}"/>
                </table>
                <script type="text/javascript">
                    $('#viewAngiospermsList').click(function () {
                        $('#angiospermsList').toggle("slow");
                    });
                    $('#angiospermsList').hide();
                    $('button#angiospermsDL').click(function (e) {
                        var uri = "${pageContext.request.contextPath}/regions/${regionType.name}/${geoRegion.name}/download?title=floweringPlantsList${geoRegion.acronym}&higherTaxon=Magnoliophyta&rank=phylum";
                        window.location.replace(uri);
                    });
                </script>
            </c:when>
            <c:otherwise>
                <span class="taxonGroupTitle">Flowering plants: ${angiosperms.totalRecords}</span>
            </c:otherwise>
        </c:choose>
    </li>
    
  </ul>

  <!-- Start of the Comparison Tool -->
  <h2 id="comparisonToolHdr">Compare biodiversity to other states and territories<a name="compare">&nbsp;</a></h2>
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
</div>
</div>
</body>
</html>