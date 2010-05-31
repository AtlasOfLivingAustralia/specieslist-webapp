<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta name="pageName" content="geoRegion"/>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Regions - ${geoRegion.regionTypeName} - ${geoRegion.name}</title>
</head>
<body>
  <h1>
    <a href="${pageContext.request.contextPath}/regions/">Regions</a> 
      &gt; ${geoRegion.regionTypeName} 
      &gt; ${geoRegion.name}
  </h1>
  <ul id="taxonGroups">
    <li id="birdsBreakdown">
      <span class="taxonGroupTitle">Birds: ${birdCount}</span>
      <span class="taxonGroupActions">Download list</span>
     </li>
    <li id="fishBreakdown">
      <span class="taxonGroupTitle">Fish: ${fishCount}</span>
      <span class="taxonGroupActions">Download list</span>
    </li>
    <li id="mammalsBreakdown">
      <span class="taxonGroupTitle">Mammals: ${mammals.totalRecords}</span>
      <span class="taxonGroupActions">
        <a href="${pageContext.request.contextPath}/regions/state/${geoRegion.name}/download?higherTaxon=Mammalia&rank=class">Download</a>
        <a href="#Mammals" id="viewMammalsList">View<c:if test="${mammals.totalRecords>100}"> (limited to 100)</c:if></a>
      </span>
      <ul id="mammalsList" class="taxonList">
        <c:forEach items="${mammals.taxonConcepts}" var="taxonConcept">
          <li>${taxonConcept.nameString} 
              <c:if test="${not empty taxonConcept.commonName}">(${taxonConcept.commonName})</c:if>
          </li>
        </c:forEach>
      </ul>
    </li>
    <li id="frogsBreakdown">
      <span class="taxonGroupTitle">Frogs: ${frogCount}</span>
      <span class="taxonGroupActions">Download list</span>
    </li>
    <li id="reptilesBreakdown">
      <span class="taxonGroupTitle">Reptiles: ${reptileCount}</span>
      <span class="taxonGroupActions">Download list</span>
    </li>
  </ul>
  
  <h2 id="comparisonToolHdr">Comparison tool</h2>
  <div id="comparisonTable">
	  <ul id="selectedGroup" class="tabs groupSelect">
	    <li class="selectedCompareGroup" onclick="javascript:setSelectedTaxa(this,'mammals','Mammalia','class');">Mammals</li>
	    <li>Birds</li>
	    <li>Fish</li>
	    <li>Frogs</li>
	    <li>Reptiles</li>
	  </ul>
	  
	  <ul id="compareRegions" class="tabs regionSelect">
	    <li><a href="javascript:loadTaxaDiff('state','${geoRegion.name}','state','Australian Capital Territory');">ACT</a></li>
	    <li><a href="javascript:loadTaxaDiff('state','${geoRegion.name}','state','New South Wales');">NSW</a></li>
	    <li><a href="javascript:loadTaxaDiff('state','${geoRegion.name}','state','Northern Territory');">NT</a></li>
	    <li><a href="javascript:loadTaxaDiff('state','${geoRegion.name}','state','Queensland');">QLD</a></li>
	    <li><a href="javascript:loadTaxaDiff('state','${geoRegion.name}','state','South Australia');">SA</a></li>
	    <li><a href="javascript:loadTaxaDiff('state','${geoRegion.name}','state','Tasmania');">TAS</a></li>
	    <li><a href="javascript:loadTaxaDiff('state','${geoRegion.name}','state','Victoria');">VIC</a></li>
	  </ul>
	  <h6 id="taxaDiffCount"></h6>
	  <ul id="taxaDiff"></ul>
  </div>
  
<script type="text/javascript"><!--

  $('#viewMammalsList').click(function () {
    $('#mammalsList').toggle("slow");
  });
  $('#mammalsList').hide();

  var selectedTaxaSimple = "mammals";
  var selectedTaxa = "Mammalia";
  var selectedTaxonRank = "class";

  function setSelectedTaxa(currentNode, commonName, taxa, taxonRank){
	  selectedTaxaSimple = commonName;
	  selectedTaxa = taxa;
	  selectedTaxonRank = taxonRank;
  }

  
  /**
   * Loads the taxo that are different between states.
   */
  function loadTaxaDiff(regionType, regionName, altRegionType, altRegionName){

	  $('#taxaDiff').hide("slow");
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
		  $('#taxaDiffCount').html(regionName+ ' has recorded '+data.searchResults.taxonConcepts.length+' '+selectedTaxaSimple+' not recorded in '+altRegionName);
		  $('#taxaDiff').show("slow");
		}); 
  }
</script>
</body>

</html>
