<%@ page contentType="text/html" pageEncoding="UTF-8" %><%@ 
taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="pageName" content="home"/>
  		<link rel="stylesheet" href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/themes/base/jquery-ui.css" type="text/css" media="all" />
  		<script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery-ui-1.8.custom.min.js"></script>
        <title>Bie | Atlas of Living Australia</title>
    </head>
    <body>
    	<div class="section">
        <h1> Web Services </h1>
		<br/>
        <h3>Search</h3>
        <ul>
            <li><strong>Full Text Search:</strong> /search.json or /search.xml - needs request param definition<br/>
            Performs a search across all objects, and selects to show the view for the closest match. Params:
            <ul>
            	<li>q - the initial query</li>
            	<li>fq - filters to be applied to the original query</li>
            	<li>start - start index</li>
            	<li>pageSize - number record on single page</li>
            	<li>sort - sort field</li>
            	<li>dir - sort direction [asc/dec]</li>
            	<li>title - title</li>
            </ul>
            </li>
 
            <li><strong>Auto Complete Search:</strong> /search/auto.json* - needs request param definition<br/>
            Used to provide a list of scientific and common names that can be used to automatically complete a supplied partial name. Params:
            <ul>
            	<li>q - The value to auto complete</li>
            	<li>geoOnly - When true only include results that have some geospatial occurrence records</li>
            	<li>idxType - The index type to limit see bie-hbase/src/main/java/org/ala/dao/IndexedTypes</li>
            	<li>limit - The maximum number of results to return</li>
            </ul>
            </li>            
        </ul>

        <h3>Species</h3>
        <ul>
        	<li><strong>Show Species:</strong> /species/{guid}.json or /species/{guid}.jsonp or /species/{guid}.xml<br/>
        	Returns all available data for a given taxon concept. URI path must contain a valid identifier (usually an LSID).
        	</li>
            <li><strong>Show Short Species Info:</strong> /species/shortProfile/{guid}.json or /species/shortProfile/{guid}.xml <br/>
            Returns the abbreviated profile for a given taxon concepts.
            </li>
            <li><strong>Show Species Info:</strong> /species/info/{guid}.json or /species/info/{guid}.xml<br/>
            Returns the profile for a given taxon concept.
            </li>
            <li><strong>Show More Species Info:</strong> /species/moreInfo/{guid}.json or /species/moreInfo/{guid}.xml<br/>
            Returns the extended profile for a given taxon concept.
            </li>
            <li><strong>Show Chart Info:</strong> /species/charts/{guid:.+}*.<br/>
            Returns the occurrence info for a given taxon concept.
            </li>
            <li><strong>Show Source Info:</strong> /species/source/{guid:.+}*<br/>
            Get the list of collections, institutes, data resources and data providers that have specimens for the supplied taxon concept guid
            </li>
            <li><strong>Get Guid For Names:</strong> /ws/guid/{scientificName}<br/>
            Returns guid for a name.
            </li>
            <li><strong>Get Guids For Names:</strong> /ws/guid/batch - needs request param definition. Param:<br/>
            Intended for batch or bulk use – returns guids for a specified list of names.
            	<ul><li>q - list of scientific names</li></ul>
            </li>
            <li><strong>Get Image Repo Location</strong> /species/image/{imageType}/{guid:.+} - needs request param definition. <br/>
            	Get the repo location of a image according to guid. Param:
            	<ul><li>imageType - thumbnail OR small </li></ul>
            </li>
            <li><strong>Get Document Details:</strong> /species/document/{documentId}.json <br/>
            JSON web service (AJAX) to return details for a repository document</li>
            <li><strong>Get List Status:</strong> /species/status/{status}.json - needs request param definition. <br/>
            	Pest / Conservation status JSON. Params:
	 			<ul>
	            	<li>status - status type </li>
	            	<li>fq - filters to be applied to the original query</li>
	            	<li>startIndex - start index</li>
	            	<li>results - number record on single page</li>
	            	<li>sort - sort field</li>
	            	<li>dir - sort direction [asc/dec]</li>
	            </ul>           
            </li>
            <li><strong>Get Names For Guids:</strong> /species/namesFromGuids.json <br/>
            	return a list of scientific names for an input list of GUIDs. Param:
            	<ul><li>guid - list of guids</li></ul>
            </li>
            <li><strong>Get Synonym Names For Guid:</strong> /species/synonymsForGuid/{guid} <br/>
            return a list of synonyms for a GUID/LSID</li>
        </ul>

        <h3>GeoSpatial</h3>
        <ul>
        	<li><strong>Get Species Location Map Info</strong> /map/map.json* - needs request param definition:<br/>
        	Returns urls for map images 
            	<ul><li>guid - guid </li></ul>
            </li>
<!--            
            <li><strong>Download Species List:</strong> /regions/{regionType}/{regionName}/download* - needs request param definition. <br/>
            	Download a list of species within a higher taxon group, that have occurred within a region. Params:
	 			<ul>
	            	<li>regionType - region type </li>
	            	<li>regionName - region name</li>
	            	<li>higherTaxon - higher taxon</li>
	            	<li>rank - rank</li>
	            	<li>title - download title</li>
	            </ul>           
            </li>
-->                        
        </ul>
        
        <h3>Administration</h3>
        
<div style="display:none" id="dialog-modal" title="Process dialog">
	<p>Process in progress. <br/>Please wait..........</p>
</div>
                    	<script type="text/javascript">
                    		function callAdminWs(wsName) {
                                var url = "${pageContext.request.contextPath}/admin/" + wsName;
                        		$( "#dialog:ui-dialog" ).dialog( "destroy" );
                        		
                        		$( "#dialog-modal" ).dialog({
                        			height: 140,
                        			modal: true
                        		});

				                var jqxhr = $.getJSON(url, function(data){
				                	
				                	$( "#dialog-modal" ).dialog("close");
                                    alert("response Text: " + jqxhr.responseText + " (" + jqxhr.status + ")");
									
				                }).error(function(jqxhr) {
				                	$( "#dialog-modal" ).dialog("close");
                                    // catch ajax errors (requiers JQuery 1.5+) - usually 500 error
                                    if(jqxhr.responseText != null && jqxhr.responseText.length > 0){
                                    	alert('response Text: ' + jqxhr.responseText + " (" + jqxhr.status + ")");
				                	}
                                });

	                        }
                    	</script>
                    	                    	
	        <ul>
        		<li><strong>Check for Read Only Mode:</strong><a href="javascript:callAdminWs('isReadOnly')">&nbsp;${pageContext.request.contextPath}/admin/isReadOnly;&nbsp;</a></li>
        	</ul>
        	
        	The remaining services in the section is support GET method and protected by CAS security framework. To consume these services must logon into CAS server.<br/>
        	All services will place the bie-webapp in read only mode until the process has been completed
        	<ul>
	        	<li><strong>Optimise Index: </strong>/admin/optimise - optimise embedded solr index.</strong>
	        	</li>
	        	<li><strong>Reload All Ranks: </strong>/admin/reloadAllRanks - reapply all rankings from 'rk' to 'tc' table.
				</li>
				<li><strong>Reload Fish Common Name Default Value: </strong>/admin/loadCaab - reload default common name rank value into 'rk' table.
				</li>  
	        	<li><strong>Reload Collections: </strong>/admin/reloadCollections - reload the collections into Solr index.</strong>
	        	</li>
	        	<li><strong>Reload Institutions: </strong>/admin/reloadreloadInstitutions - reload the institutions into Solr index.
				</li>
				<li><strong>Reload Data Providers: </strong>/admin/reloadDataProviders - reload the data providers into Solr index.
				</li>  
				<li><strong>Reload Data Resources: </strong>/admin/reloadDataResources - reload the datasets into Solr index.
				</li>        					      					      					      	
        	</ul>       
    </body>
</html>