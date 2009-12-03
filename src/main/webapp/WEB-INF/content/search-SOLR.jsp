<!DOCTYPE html PUBLIC
	"-//W3C//DTD XHTML 1.1 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
 
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"></html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html">
        <!-- Combo-handled YUI CSS files: -->
        <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?2.8.0r4/build/fonts/fonts-min.css&2.8.0r4/build/base/base-min.css&2.8.0r4/build/paginator/assets/skins/sam/paginator.css&2.8.0r4/build/datatable/assets/skins/sam/datatable.css">
        <!-- Combo-handled YUI JS files: -->
        <script type="text/javascript" src="http://yui.yahooapis.com/combo?2.8.0r4/build/yahoo/yahoo-debug.js&2.8.0r4/build/event/event-debug.js&2.8.0r4/build/connection/connection-debug.js&2.8.0r4/build/datasource/datasource-debug.js&2.8.0r4/build/dom/dom-debug.js&2.8.0r4/build/element/element-debug.js&2.8.0r4/build/paginator/paginator-debug.js&2.8.0r4/build/datatable/datatable-debug.js&2.8.0r4/build/history/history-debug.js&2.8.0r4/build/json/json-debug.js&2.8.0r4/build/logger/logger-debug.js"></script>
        <script type="text/javascript">
            var contentModelMap = {}; // store the display name for document types (facets)
            // private method for UTF-8 encoding
            function utf8_encode (string) {
		string = string.replace(/\r\n/g,"\n");
		var utftext = "";

		for (var n = 0; n < string.length; n++) {

			var c = string.charCodeAt(n);

			if (c < 128) {
				utftext += String.fromCharCode(c);
			}
			else if((c > 127) && (c < 2048)) {
				utftext += String.fromCharCode((c >> 6) | 192);
				utftext += String.fromCharCode((c & 63) | 128);
			}
			else {
				utftext += String.fromCharCode((c >> 12) | 224);
				utftext += String.fromCharCode(((c >> 6) & 63) | 128);
				utftext += String.fromCharCode((c & 63) | 128);
			}

		}

		return utftext;
                //return string;
            }

        </script>
        <title>ALA Repository Search</title>
    </head>

    <body>
        <h1>Repository Search</h1>
        <div id="facetBar">
        <s:if test="%{!solrResults.facetResults.isEmpty()}">
          
            <h4>Refine your results:</h4>
            <ul id="navlist">
                <s:iterator value="solrResults.facetResults">
                    <li><span class="FieldName"><s:text name="facet.%{fieldName}"/></span></li>

                    <ul id="subnavlist">
                        <s:iterator value="fieldResult">
                            <li id="subactive">
                                <script type="text/javascript">
                                    // Add the document type's display names to a JS Map (used for catching bookmarked state)
                                    contentModelMap['<s:property value="label"/>'] = '<s:text name="fedora.%{label}"/>';
                                </script>
                                <a href="#<s:property value="fieldName"/>:<s:property value="label"/>" 
                                   onClick="doFacetSearch('<s:property value="fieldName"/>','<s:property value="label"/>','<s:text name="fedora.%{label}"/>')">
                                   <s:text name="fedora.%{label}"/> (<s:property value="count"/>)</a>
                               <%--<a href="${pageContext.request.contextPath}/search/fq=<s:property value="fieldName"/>:<s:property value="label"/>">
                                 <s:property value="label"/>:(<s:property value="count"/>)</a></li>--%>
                            </li>
                        </s:iterator>
                     </ul>
                 </s:iterator>
             </ul>           
         </s:if>
         <s:if test="%{!facetConstraints.isEmpty()}">
           <ul><s:form action="removeConstraint" namespace="search" method="GET" theme="simple">
            <li><span class="FieldName">Current Constraints (Click to Remove)</span></li>
            <ul id="subnavlist"><s:iterator value="facetConstraints">
                <a href="${pageContext.request.contextPath}/search/rfq=<s:property />">
                        <s:property/> (X)</a>
            </s:iterator>
            </ul>
            <s:submit value="RemoveAll"/>
          </s:form> </ul>
         </s:if>
        </div>
        <div id="removeFacet">
            <h4>Displaying subset of results, restricted to: <span id="facetName"></span></h4>
            <ul>
                <li><a href="#" onClick="removeFacetSearch();">Return to full result list (<s:property value="solrResults.nResults"/>)</a></li>
            </ul>
        </div>
        
        <div id="content" class="yui-skin-sam">
          <s:if test="%{solrResults.searchResults.isEmpty()}"><h2>${responseMessage}</h2></s:if>
          <s:elseif test="%{false}">
             <table>
               <tr><th><h4>
                   <s:if test="%{currentPage>0}">
                    <a  href="${pageContext.request.contextPath}/search/getPrevPage">
                        <img border="0" height="20" width="22" src="<s:url value="/images/leftArrow.gif"/>" alt="Previous Page" />
                    </a> </s:if>
                   <s:if test="%{currentPage<(maxPages-1)}">
                    <a href="${pageContext.request.contextPath}/search/getNextPage">
                        <img border="0" height="20" width="22" src="<s:url value="/images/rightArrow.gif"/>" alt="Next Page" />
                    </a> </s:if>
                   <s:property value="solrResults.nResults"/> results. Page   <s:property value="%{currentPage+1}"/> of <s:property value="%{maxPages}"/>
               </h4></th></tr>

            </table>
             <!-- Dynamic table content. -->
             <table class ="resultTable">
                 <tr>
                     <th>#</th>
                     <th>Type</th>
                     <th>Title</th>
                 </tr>
                 <s:iterator value="solrResults.searchResults" status="iStatus">
                   <tr>
                       <td>
                           <s:property value="%{#iStatus.count+currentPage*solrResults.resultsPerPage}"/>
                       </td>
                     <td>
                         <s:property value="contentModelInitial"/>
                     </td>
                     <td>
                        <ul style="list-style-type:none">
                            <li> <a href="${pageContext.request.contextPath}/<s:property value="urlMapper"/>/<s:property value="pid"/>"><s:property value="title"/></a></li>
                            <s:if test="%{!highLights.isEmpty()}"> <li>${highLights}> </li></s:if>

                        </ul>
                    </td>
                 </tr>
                </s:iterator>
                </table>
            </s:elseif>
            <s:else>
                <div class="datatable-summary">Total records: <s:property value="solrResults.nResults"/></div>
                <iframe id="yui-history-iframe" src='<s:url value="/css/blank.html"/>'></iframe>
                <input id="yui-history-field" type="hidden">
                <div id="dt-pag-head"></div>
                <div id="results"></div>
                <div id="dt-pag-nav"></div>
                <jsp:include page="yui-datatable.jsp"/>
                <script type="text/javascript">
                    propertyName = "all_text";
                    query = escape(this.utf8_encode($("#searchSOLR_propertyValue").val()));
                    var fq = "${id}";
                    var ids = fq.split("=");
                    facetQuery = ids[1];
                    loadDatatable(propertyName, query, facetQuery);
                </script>
            </s:else>
        </div>
    </body>
</html>
