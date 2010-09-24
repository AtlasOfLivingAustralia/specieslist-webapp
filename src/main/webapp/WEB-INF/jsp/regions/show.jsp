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
	            $('#'+textTag).html("<i>"+data.taxonConcept.nameString+"</i><br/>"+data.commonNames[0].nameString);
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
    <c:if test="${birdCount>0}">
    <li id="birdsBreakdown" class="taxonBreakdown">
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
               var uri = "${pageContext.request.contextPath}/regions/${regionType.name}/${geoRegion.name}/download?title=BirdsList${geoRegion.acronym}&higherTaxon=Aves&rank=class";
               window.location.replace(uri);
           });
       </script>
    </li>
    </c:if>

    <!-- FISH -->
    <c:if test="${fishCount>0}">
    <li id="fishBreakdown" class="taxonBreakdown">
       <span class="taxonGroupTitle">Fish: ${fishCount} (Number with images:${fish.totalRecords})</span>
       <span class="taxonGroupActions">
           <button id="fishDL" class="downloadButton">Download</button>
           <c:if test="${not empty fish.totalRecords}">
           	<a href="#Fish" id="viewFishList">Show/Hide<c:if test="${fish.totalRecords>25}"> (limited to 25)</c:if></a>
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
           $('button#fishDL').click(function (e) {
               var uri = "${pageContext.request.contextPath}/regions/${regionType.name}/${geoRegion.name}/download?title=FishList${geoRegion.acronym}&higherTaxon=Myxini,Petromyzontida,Chondrichthyes,Sarcopterygii,Actinopterygii&rank=class";
               window.location.replace(uri);
           });
       </script>
    </li>
	</c:if>
	
    <!-- FROGS -->
    <c:if test="${frogCount>0}">
    <li id="frogsBreakdown" class="taxonBreakdown">
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
    </li>
    </c:if>

    <!-- MAMMALS -->
    <c:if test="${mammalCount>0}">
    <li id="mammalsBreakdown" class="taxonBreakdown">
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
    </li>
    </c:if>

    <!-- REPTILES -->
    <c:if test="${reptileCount>0}">
    <li id="reptilesBreakdown" class="taxonBreakdown">
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
    </li>
    </c:if>
    
    <!-- ARTHROPODS -->
    <c:if test="${arthropodCount>0}">
    <li id="arthropodsBreakdown" class="taxonBreakdown">
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
               var uri = "${pageContext.request.contextPath}/regions/${regionType.name}/${geoRegion.name}/download?title=ArthropodsList${geoRegion.acronym}&higherTaxon=Arthropoda&rank=phylum";
               window.location.replace(uri);
           });
       </script>
    </li>
    </c:if>
    
    <!-- MOLLUSCS-->
    <c:if test="${molluscCount>0}">
    <li id="molluscsBreakdown" class="taxonBreakdown">
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
    </li>
    </c:if>
    
    <!-- ANGIOSPERMS -->
    <c:if test="${angiospermCount>0}">
    <li id="angiospermsBreakdown" class="taxonBreakdown">
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
                        var uri = "${pageContext.request.contextPath}/regions/${regionType.name}/${geoRegion.name}/download?title=FloweringPlantsList${geoRegion.acronym}&higherTaxon=Magnoliophyta&rank=phylum";
                        window.location.replace(uri);
                    });
                </script>
    </li>
    </c:if>
  </ul>
</div>

<c:if test="${not empty param['showCompare']}">
	<jsp:include page="compare.jsp"/>
</c:if>

</div>
</body>
</html>