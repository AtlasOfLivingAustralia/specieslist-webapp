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
  <title>${geoRegion.name}  | ${geoRegion.regionTypeName} | Regions | Atlas of Living Australia</title>
  <script type="text/javascript">
        $(document).ready(function() {
            // JQuery UI buttons
            $(".downloadButton").button();
            $("#selectedGroup").buttonset();
            $("#compareRegions").buttonset();
        });
   </script>
   <style type="text/css">
    table { margin-bottom:0px; border-bottom: 0px; border-bottom-style: none;}
    .noUnderline { text-decoration: none; }
    .pagers { color: #01716E;}
    .nextPage { float:right; }
    .pagerButtons { padding-top:8px; }
    div.pagers { padding:15px 16px 4px 10px;}
    .loadingMessage { margin-left: 325px; }
  </style>
</head>
<body>
    <div id="header">
        <div id="breadcrumb">
            <a href="${initParam.centralServer}">Home</a>
            <a href="${initParam.centralServer}/explore">Explore</a>
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
                <tr>
               <c:if test="${not empty extendedGeoRegion.animalEmblem}">
                    <td>
                        <a id="animalEmblem" href="${pageContext.request.contextPath}/species/${extendedGeoRegion.animalEmblem.guid}">
                            <img src="${pageContext.request.contextPath}/static/images/noImage85.jpg" alt="${extendedGeoRegion.animalEmblem.nameString}"/>
                        </a>
                    </td>
                    <td>
                        <h3>Animal emblem</h3>
                        <div id="animalEmblemText">${extendedGeoRegion.animalEmblem.nameString}</div>
                    </td>
                </c:if>
               <c:if test="${not empty extendedGeoRegion.plantEmblem}">
                    <td>
                        <a id="plantEmblem" href="${pageContext.request.contextPath}/species/${extendedGeoRegion.plantEmblem.guid}">
                            <img src="${pageContext.request.contextPath}/static/images/noImage85.jpg" alt="${extendedGeoRegion.plantEmblem.nameString}"/>
                        </a>
                    </td>
                    <td>
                        <h3>Plant emblem</h3>
                        <div id="plantEmblemText">${extendedGeoRegion.plantEmblem.nameString}</div>
                    </td>
                </c:if>
                </tr>
                <tr>
                <c:if test="${not empty extendedGeoRegion.birdEmblem}">
                    <td>
                        <a id="birdEmblem" href="${pageContext.request.contextPath}/species/${extendedGeoRegion.birdEmblem.guid}">
                            <img src="${pageContext.request.contextPath}/static/images/noImage85.jpg" alt="${extendedGeoRegion.birdEmblem.nameString}"/>
                        </a>
                    </td>
                    <td>
                        <h3>Bird emblem</h3>
                        <div id="birdEmblemText">${extendedGeoRegion.birdEmblem.nameString}</div>
                    </td>
                </c:if>
                <c:if test="${not empty extendedGeoRegion.marineEmblem}">
                    <td>
                        <a id="marineEmblem" href="${pageContext.request.contextPath}/species/${extendedGeoRegion.marineEmblem.guid}">
                            <img src="${pageContext.request.contextPath}/static/images/noImage85.jpg" alt="${extendedGeoRegion.marineEmblem.nameString}"/>
                        </a>
                    </td>
                    <td>
                        <h3>Marine emblem</h3>
                        <div id="marineEmblemText">${extendedGeoRegion.marineEmblem.nameString}</div>
                    </td>
                </c:if>
                </tr>
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
    <alatag:regionBrowse 
        regionName="${geoRegion.name}"
        taxaCount="${birdCount}"
        rank="class"
        regionAcronym="${geoRegion.acronym}"
        idSuffix="birds"
        higherTaxa="Aves"
        taxonWithImagesCount="${birds.totalRecords}"
        regionType="${regionType.name}"
        taxonGroup="Birds"
        taxaRecords="${birds.results}"/>
    <!-- FISH -->
    <alatag:regionBrowse 
        regionName="${geoRegion.name}"
        taxaCount="${fishCount}"
        rank="class"
        regionAcronym="${geoRegion.acronym}"
        idSuffix="fish"
        higherTaxa="Myxini,Petromyzontida,Chondrichthyes,Sarcopterygii,Actinopterygii"
        taxonWithImagesCount="${fish.totalRecords}"
        regionType="${regionType.name}"
        taxonGroup="Fish"
        taxaRecords="${fish.results}"/>
    <!-- FROGS -->
    <alatag:regionBrowse 
        regionName="${geoRegion.name}"
        taxaCount="${frogCount}"
        rank="class"
        regionAcronym="${geoRegion.acronym}"
        idSuffix="frogs"
        higherTaxa="Amphibia"
        taxonWithImagesCount="${frogs.totalRecords}"
        regionType="${regionType.name}"
        taxonGroup="Frogs"
        taxaRecords="${frogs.results}"/>
    <!-- MAMMALS -->
    <alatag:regionBrowse 
        regionName="${geoRegion.name}"
        taxaCount="${mammalCount}"
        rank="class"
        regionAcronym="${geoRegion.acronym}"
        idSuffix="mammals"
        higherTaxa="Mammalia"
        taxonWithImagesCount="${mammals.totalRecords}"
        regionType="${regionType.name}"
        taxonGroup="Mammals"
        taxaRecords="${mammals.results}"/>
    <!-- REPTILES -->
    <alatag:regionBrowse 
        regionName="${geoRegion.name}"
        taxaCount="${reptileCount}"
        rank="class"
        regionAcronym="${geoRegion.acronym}"
        idSuffix="reptiles"
        higherTaxa="Reptilia"
        taxonWithImagesCount="${reptiles.totalRecords}"
        regionType="${regionType.name}"
        taxonGroup="Reptiles"
        taxaRecords="${reptiles.results}"/>
    <!-- ARTHROPODS -->
    <alatag:regionBrowse 
        regionName="${geoRegion.name}"
        taxaCount="${arthropodCount}"
        rank="phylum"
        regionAcronym="${geoRegion.acronym}"
        idSuffix="arthropods"
        higherTaxa="Arthropoda"
        taxonWithImagesCount="${arthropods.totalRecords}"
        regionType="${regionType.name}"
        taxonGroup="Arthropods"
        taxaRecords="${arthropods.results}"/>
    <!-- MOLLUSCS -->
    <alatag:regionBrowse 
        regionName="${geoRegion.name}"
        taxaCount="${molluscCount}"
        rank="phylum"
        regionAcronym="${geoRegion.acronym}"
        idSuffix="molluscs"
        higherTaxa="Mollusca"
        taxonWithImagesCount="${molluscs.totalRecords}"
        regionType="${regionType.name}"
        taxonGroup="Molluscs"
        taxaRecords="${molluscs.results}"/>
    <!-- ANGIOSPERMS -->
    <alatag:regionBrowse 
        regionName="${geoRegion.name}"
        taxaCount="${angiospermCount}"
        rank="phylum"
        regionAcronym="${geoRegion.acronym}"
        idSuffix="angiosperms"
        higherTaxa="Magnoliophyta"
        taxonWithImagesCount="${angiosperms.totalRecords}"
        regionType="${regionType.name}"
        taxonGroup="Angiosperms"
        taxaRecords="${angiosperms.results}"/>
  </ul>
</div>

<c:if test="${not empty param['showCompare']}">
	<jsp:include page="compare.jsp"/>
</c:if>

</div>
</body>
</html>