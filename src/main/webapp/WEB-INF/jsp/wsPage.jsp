<%@ page contentType="text/html" pageEncoding="UTF-8" %><%@ 
taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="pageName" content="home"/>
  		<link rel="stylesheet" href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/themes/base/jquery-ui.css" type="text/css" media="all" />
  		<script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery-ui-1.8.custom.min.js"></script>
        <title>BIE Webservices | Atlas of Living Australia</title>
    </head>
    <body>
        <style type="text/css">
            .code { font-family: courier new; }
            .webserviceList { margin-left:30px; }
            .paramList { margin-left:60px; }
            .examples  { margin-left:90px; }
            strong { font-weight:bold; }
        </style>

    	<div class="section">
        <h1> Web Services </h1>
		<br/>
        <p>
            These webservices provide taxon search capabilities and taxon profile information.
            <br/>
            Please send any bug reports, suggestions for improvements or new services to:
            <strong>developers 'AT' ala.org.au</strong>
        </p>
        <h3>Search</h3>
        <ul class="webserviceList">
            <li><strong>Full Text Search:</strong> /search.json or /search.xml - needs request param definition<br/>
            Performs a search across all objects, and selects to show the view for the closest match. Params:
                <ul class="paramList">
                    <li>q - the initial query e.g. <a href="${initParam.serverName}/search.json?q=Macropus">q=Macropus</a></li>
                    <li>fq - filters to be applied to the original query. e.g. &fq=idxType=TAXON will limit results to taxa only.</li>
                    <li>start - start offset for the results</li>
                    <li>pageSize - number record on single page</li>
                    <li>sort - sort field</li>
                    <li>dir - sort direction "asc" or "dec"</li>
                </ul>
                <p>Note: Image URLs can be resolved by replacing file paths /data/bie/ with http://bie.ala.org.au/repo/.</p>
            </li>
 
            <li><strong>Auto Complete Search:</strong> /search/auto.json* - needs request param definition<br/>
            Used to provide a list of scientific and common names that can be used to automatically complete a supplied partial name. Params:
                <ul class="paramList">
            	    <li>q - The value to auto complete e.g. <a href="${initParam.serverName}/search/auto.json?q=Mac">q=Mac</a></li>
            	    <li>geoOnly - When "true" only include results that have some geospatial occurrence records</li>
            	    <li>idxType - The index type to limit . Values include "TAXON", "REGION", "COLLECTION", "INSTITUTION", "DATASET"</li>
            	    <li>limit - The maximum number of results to return</li>
                </ul>
            </li>
            
            <li><strong>Download Taxa Search Results:</strong> /download -needs request param definition<br/>
            Downloads all the idxtype:TAXON results to file.
            	<ul class="paramList">
                    <li>q - the initial query e.g. <a href="${initParam.serverName}/search.json?q=Macropus">q=Macropus</a></li>
                    <li>fq - filters to be applied to the original query. e.g. &fq=idxType=TAXON will limit results to taxa only.</li>                                     
                    <li>sort - sort field</li>
                    <li>dir - sort direction "asc" or "dec"</li>
                    <li>fields - a comma separated list of SOLR fields to include in the download.  
                    Fields can be included in the download if they have been stored. See <a href="${initParam.serverName}/admin/indexFields">index fields</a> for more information</li>
                    <li>file - the name of file to create</li>
                </ul>
            </li>

            <li><strong>Bulk lookup:</strong> /species/bulklookup.json<br/>
                Used to provide a list of GUIDs.
                <ul class="paramList">
                    <li>HTTP POST a JSON body containing an array of GUIDs or names.</li>
                </ul>
            </li>
            <li><strong>Bulk lookup based on guids: </strong> /species/guids/bulklookup.json<br/>
            	A more efficient bulk lookup based on GUIDs.
            	<ul class="paramList">
                    <li>HTTP POST a JSON body containing an array of GUIDs.</li>
                </ul>
            </li>
        </ul>

        <h3>Species</h3>
        <ul class="webserviceList">
        	<li><strong>Show Species:</strong> /species/{guid}.json or /species/{guid}.jsonp or /species/{guid}.xml<br/>
        	Returns all available data for a given taxon concept. URI path must contain a valid identifier (usually an LSID).
        	</li>
            <li><strong>Show Short Species Info:</strong> /species/shortProfile/{guid}.json or /species/shortProfile/{guid}.xml <br/>
            Returns the abbreviated profile for a given taxon concepts.
                e.g. <a href="${initParam.serverName}/species/shortProfile/urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537.json">short profile for red kangaroo</a>
            </li>
            <li><strong>Show Species Info:</strong> /species/info/{guid}.json or /species/info/{guid}.xml<br/>
            Returns the profile for a given taxon concept.
                e.g. <a href="${initParam.serverName}/species/info/urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537.json">info for red kangaroo</a>
            </li>
            <li><strong>Show More Species Info:</strong> /species/moreInfo/{guid}.json or /species/moreInfo/{guid}.xml<br/>
            Returns the extended profile for a given taxon concept.
                e.g. <a href="${initParam.serverName}/species/moreInfo/urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537.json">info for red kangaroo</a>
            </li>
            <li><strong>Get Guid For Names:</strong> /ws/guid/{scientificName}<br/>
            Returns guid for a name.
            </li>
            <li><strong>Get Guids For Names:</strong> /ws/guid/batch - needs request param definition. Param:<br/>
            Intended for batch or bulk use – returns guids for a specified list of names.
                <ul class="paramList"><li>q - list of scientific names</li></ul>
            </li>
            <li><strong>Get Image</strong> /species/image/{imageType}/{guid:.+} - needs request param definition. <br/>
            	Get the repo location of a image according to guid. Param:
            	<ul><li>imageType - thumbnail OR small </li></ul>
                Examples, red kangaroo:
               <ul class="examples">
                   <li><a href="${initParam.serverName}/species/image/thumbnail/urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537">${initParam.serverName}/species/image/thumbnail/urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537</a></li>
                   <li><a href="${initParam.serverName}/species/image/small/urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537">${initParam.serverName}/species/image/small/urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537</a></li>
                   <li><a href="${initParam.serverName}/species/image/large/urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537">${initParam.serverName}/species/image/large/urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537</a></li>
               </ul>
            </li>
            <li><strong>Get Document Details:</strong> /species/document/{documentId}.json <br/>
            JSON web service (AJAX) to return details for a repository document</li>
            <li><strong>Get List Status:</strong> /species/status/{status}.json - needs request param definition. <br/>
            	Pest / Conservation status JSON. Params:
                <ul class="paramList">
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
                <ul class="paramList"><li>guid - list of guids</li></ul>
            </li>
            <li><strong>Get Synonym Names For Guid:</strong> /species/synonymsForGuid/{guid} <br/>
            return a list of synonyms for a GUID/LSID</li>
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

            <ul class="webserviceList">
        		<li><strong>Check for Read Only Mode:</strong><a href="javascript:callAdminWs('isReadOnly')">&nbsp;${pageContext.request.contextPath}/admin/isReadOnly;&nbsp;</a></li>
        		<li><strong>Reopen SOLR index:</strong> <a href="${initParam.serverName}/admin/reopenIndex">/admin/reopenIndex</a></li> </li>
        		<li><strong>View list of SOLR index fields:</strong> <a href="${initParam.serverName}/admin/indexFields">/admin/indexFields</a></li>
        	</ul>
        	
        	The remaining services in the section is support GET method and protected by CAS security framework. To consume these services must logon into CAS server.<br/>
        	All services will place the bie-webapp in read only mode until the process has been completed
            <ul class="webserviceList">
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