<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta name="pageName" content="${statusType.value}"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <!-- Combo-handled YUI CSS files: -->
    <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?2.8.0r4/build/fonts/fonts-min.css&2.8.0r4/build/base/base-min.css&2.8.0r4/build/paginator/assets/skins/sam/paginator.css&2.8.0r4/build/datatable/assets/skins/sam/datatable.css">
    <!-- Combo-handled YUI JS files: -->
    <script type="text/javascript" src="http://yui.yahooapis.com/combo?2.8.0r4/build/yahoo/yahoo-debug.js&2.8.0r4/build/event/event-debug.js&2.8.0r4/build/connection/connection-debug.js&2.8.0r4/build/datasource/datasource-debug.js&2.8.0r4/build/dom/dom-debug.js&2.8.0r4/build/element/element-debug.js&2.8.0r4/build/paginator/paginator-debug.js&2.8.0r4/build/datatable/datatable-debug.js&2.8.0r4/build/history/history-debug.js&2.8.0r4/build/json/json-debug.js&2.8.0r4/build/logger/logger-debug.js"></script>
    <title>${statusType.displayName} List</title>
</head>
<body>
    <h1>${statusType.displayName} List</h1>
    <c:if test="${empty searchResults.taxonConcepts}"><p>Search for <span style="font-weight: bold"><c:out value="${query}"/></span> did not match any documents</p></c:if>
    <c:if test="${not empty searchResults.taxonConcepts}">
        <div id="facetBar">
            <c:if test="${fn:length(filterQuery) < 1}">
                <h4>Restrict by:</h4>
                <ul id="navlist">
                    <c:if test="${not empty query}"><c:set var="queryParam">q=<c:out value="${query}" escapeXml="true"/></c:set></c:if>
                    <c:forEach var="facetResult" items="${searchResults.facetResults}">
                        <c:if test="${fn:containsIgnoreCase(statusType.value, facetResult.fieldName) && fn:length(filterQuery) < 1}">
                            <li><span class="FieldName"><fmt:message key="facet.${facetResult.fieldName}"/></span></li>
                            <ul id="subnavlist">
                                <c:forEach var="fieldResult" items="${facetResult.fieldResult}">
                                    <%-- test to see if the current facet search is also a listed facet link --%>
                                    <c:if test="${!fn:containsIgnoreCase(filterQuery, fieldResult.label)}">
                                        <li id="subactive">
                                            <a href="?fq=${facetResult.fieldName}:${fieldResult.label}&${queryParam}">
                                                <fmt:message key="${fieldResult.label}"/> (${fieldResult.count})</a>
                                        </li>
                                    </c:if>
                                </c:forEach>
                            </ul>
                        </c:if>
                    </c:forEach>
                </ul>
            </c:if>
            <br/>
        <c:if test="${not empty filterQuery}">
            <div id="removeFacet">
                <h4>Displaying subset of results, restricted to: <span id="facetName">
                        <fmt:message key="${fn:substringAfter(filterQuery, ':')}"/></span></h4>
                <p>&bull; <a href="?<c:out value="${queryParam}"/>">Return to full result list</a></p>
            </div>
        </c:if>
        </div>
        <div id="content" class="yui-skin-sam">
            <div class="datatable-summary" style="width:640px;">Taxa with ${statusType.displayName}
                <c:if test="${fn:length(filterQuery) > 0}"> - ${fn:substringAfter(filterQuery, ':')}</c:if>: 
                <c:out value="${searchResults.totalRecords}"/> record<c:if test="${searchResults.totalRecords > 1}">s</c:if>
            </div>

            <iframe id="yui-history-iframe" src="<c:url value='/static/css/blank.html'/>"></iframe>
            <input id="yui-history-field" type="hidden">
            <div id="dt-pag-head"></div>
            <div id="results" style="/*width:100%;*/"></div>
            <div id="dt-pag-nav"></div>
            <c:set var="pageSize">20</c:set>
            <script type="text/javascript">
                var myDataTable;

                function loadDatatable() {

                    var formatTitleUrl = function(elCell, oRecord, oColumn, sData) {
                        var cellContent;
                        if (sData.length< 1) {
                            cellContent = "[no title]";
                        //} else if (sData.length > 150) {
                        //    cellContent = sData.substring(0,150) + "...";
                        } else {
                            cellContent = sData;
                        }
                        elCell.innerHTML = "<a href='" + "${pageContext.request.contextPath}/species/" + oRecord.getData("guid") + "' title='view record details'>" + cellContent + "</a>";
                    };

                    myDataSource = new YAHOO.util.DataSource("${pageContext.request.contextPath}/species/status/${statusType.value}.json?fq=${filterQuery}&");
                    myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
                    myDataSource.responseSchema = {
                        resultsList: "searchResultsDTO.taxonConcepts",
                        fields: ["guid","nameString","acceptedConceptName", "commonName","rank", "rankId", "${statusType.value}", "score"],

                        metaFields: {
                            totalRecords: "searchResultsDTO.totalRecords",
                            paginationRecordOffset : "searchResultsDTO.startIndex",
                            sortKey: "searchResultsDTO.sort",
                            sortDir: "searchResultsDTO.dir"
                        }
                    };

                    // Column definitions
                    var myColumnDefs = [
                        //{key:"ContentModel", field:"contentModelLabel", label:"Type", sortable: true, minWidth: 100},
                        {key:"scientificNameRaw", field:"nameString", sortable:true, label:"Scientific Name", formatter:formatTitleUrl}, //, width: 350 , formatter:formatTitleUrl
                        //{key:"acceptedConceptName", field:"acceptedConceptName", sortable:false, label:"Accepted Name"},
                        {key:"commonNameSort", field:"commonName", sortable:true, label:"Common Name"},
                        //{key:"rankId", field:"rank", label:"Rank", sortable: true}, // , minWidth: 100,width: 200
                        {key:"${statusType.value}", field:"${statusType.value}", sortable:false, label:"${statusType.displayName}"},
                        {key:"score", field:"score", hidden:true, maxAutoWidth: 0}
                    ];

                    // Create the Paginator
                    var myPaginator = new YAHOO.widget.Paginator({
                        <%--containers : ["dt-pag-nav"],
                        template : "{PreviousPageLink} {CurrentPageReport} {NextPageLink} {RowsPerPageDropdown}",
                        pageReportTemplate : "Showing items {startRecord} - {endRecord} of {totalRecords}",
                        rowsPerPageOptions : [10,25,50,100]--%>
                        rowsPerPage : ${pageSize},
                        pageLinks   : 10,
                        containers : "dt-pag-nav",
                        nextPageLinkLabel: "<span>Next Page</span>",
                        previousPageLinkLabel: "<span>Previous Page</span>",
                        //rowsPerPageOptions: [10,20,50,100],
                        template : "<div style='text-align:center;width:640px;'> {PreviousPageLink} {PageLinks} {NextPageLink} </div>", //"{CurrentPageReport} {PageLinks}",
                        pageReportTemplate : "<strong>{totalRecords} records.</strong>"
                    });
                    
                    var headPaginator = new YAHOO.widget.Paginator({
                        containers : "dt-pag-head",
                        pageReportTemplate : "Showing items {startRecord} - {endRecord} of {totalRecords}"
                    });

                    // DataTable configurations
                    var myConfig = {
                        paginator : myPaginator,
                        dynamicData : true,
                        //scrollable: true,
                        height: "400px",
                        initialLoad : false
                    };

                    // Instantiate DataTable
                    var myDataTable = new YAHOO.widget.DataTable("results", myColumnDefs, myDataSource, myConfig);
                    //var myDataTable = new YAHOO.widget.ScrollingDataTable("results", myColumnDefs, myDataSource, myConfig);

                    // Show loading message while page is being rendered
                    myDataTable.showTableMessage(myDataTable.get("MSG_LOADING"), YAHOO.widget.DataTable.CLASS_LOADING);

                    // Add the instances to the YAHOO.example namespace for inspection
                    YAHOO.example.BHMIntegration = {
                        myPaginator  : myPaginator,
                        myDataSource : myDataSource,
                        myDataTable  : myDataTable
                    };

                    // Integrate with Browser History Manager
                    var History = YAHOO.util.History;

                    // Define a custom function to route sorting through the Browser History Manager
                    var handleSorting = function (oColumn) {
                        // Calculate next sort direction for given Column
                        var sDir = this.getColumnSortDir(oColumn);

                        // The next state will reflect the new sort values
                        // while preserving existing pagination rows-per-page
                        // As a best practice, a new sort will reset to page 0
                        var newState = generateRequest(0, oColumn.key, sDir, this.get("paginator").getRowsPerPage());

                        // Pass the state along to the Browser History Manager
                        History.navigate("myDataTable", newState);
                    };
                    myDataTable.sortColumn = handleSorting;

                    // Define a custom function to route pagination through the Browser History Manager
                    var handlePagination = function(state) {
                        // The next state will reflect the new pagination values
                        // while preserving existing sort values
                        // Note that the sort direction needs to be converted from DataTable format to server value
                        var sortedBy  = this.get("sortedBy"),
                        newState = generateRequest(
                        state.recordOffset, sortedBy.key, sortedBy.dir, state.rowsPerPage
                    );

                        // Pass the state along to the Browser History Manager
                        History.navigate("myDataTable", newState);
                    };
                    // First we must unhook the built-in mechanism...
                    myPaginator.unsubscribe("changeRequest", myDataTable.onPaginatorChangeRequest);
                    // ...then we hook up our custom function
                    myPaginator.subscribe("changeRequest", handlePagination, myDataTable, true);

                    // Update payload data on the fly for tight integration with latest values from server
                    myDataTable.doBeforeLoadData = function(oRequest, oResponse, oPayload) {
                        var meta = oResponse.meta;
                        oPayload.totalRecords = meta.totalRecords || oPayload.totalRecords;
                        oPayload.pagination = {
                            rowsPerPage: meta.paginationRowsPerPage || ${pageSize},
                            recordOffset: meta.paginationRecordOffset || 0
                        };
                        oPayload.sortedBy = {
                            key: meta.sortKey || "score",
                            dir: (meta.sortDir) ? "yui-dt-" + meta.sortDir : "yui-dt-desc" // Convert from server value to DataTable format
                        };
                        return true;
                    };

                    // Returns a request string for consumption by the DataSource
                    var generateRequest = function(startIndex,sortKey,dir,results) {
                        startIndex = startIndex || 0;
                        sortKey   = sortKey || "score";
                        dir   = (dir) ? dir.substring(7) : "desc"; // Converts from DataTable format "yui-dt-[dir]" to server value "[dir]"
                        results   = results || ${pageSize};
                        return "results="+results+"&startIndex="+startIndex+"&sort="+sortKey+"&dir="+dir;
                    };

                    // Called by Browser History Manager to trigger a new state
                    var handleHistoryNavigation = function (request) {
                        // Sends a new request to the DataSource
                        myDataSource.sendRequest(request,{
                            success : myDataTable.onDataReturnSetRows,
                            failure : myDataTable.onDataReturnSetRows,
                            scope : myDataTable,
                            argument : {} // Pass in container for population at runtime via doBeforeLoadData
                        });
                    };

                    // Calculate the first request
                    var initialRequest = History.getBookmarkedState("myDataTable") || // Passed in via URL
                    generateRequest(); // Get default values

                    // Register the module
                    History.register("myDataTable", initialRequest, handleHistoryNavigation);

                    // Render the first view
                    History.onReady(function() {
                        // Current state after BHM is initialized is the source of truth for what state to render
                        var currentState = History.getCurrentState("myDataTable");
                        handleHistoryNavigation(currentState);
                    });

                    // Initialize the Browser History Manager.
                    YAHOO.util.History.initialize("yui-history-field", "yui-history-iframe");
                }

                function doFacetSearch(fieldName, label, displayLabel) {
                    var facetQuery = fieldName + ":" + label;
                    if (myDataTable) myDataTable.destroy();  // prevent async table creation corruption
                    loadDatatable(null, null, facetQuery);
                    $("#facetName").html(displayLabel);
                    $("#facetBar").hide();
                    $("#removeFacet").show();
                }

                function removeFacetSearch() {
                    loadDatatable(null, null, "");
                    $("#facetName").html("");
                    $('#facetBar').show();
                    $('#removeFacet').hide();
                }

            </script>
            <script type="text/javascript">
                loadDatatable();
            </script>
        </div>
    </c:if>
</body>
</html>
