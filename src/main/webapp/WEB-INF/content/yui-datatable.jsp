<%@taglib prefix="s" uri="/struts-tags" %>
<s:set name="pageSize">15</s:set>
                  <script type="text/javascript">
                        var query, propertyName, facetQuery, myDataTable;

                        function loadDatatable(thisPropertyName, thisQuery, thisFacetQuery) {
                            // use global variables if not passed in as args
                            thisPropertyName = (!thisPropertyName) ? propertyName : thisPropertyName;
                            thisQuery = (!thisQuery) ? query : thisQuery;
                            thisFacetQuery = (!thisFacetQuery) ? "" : thisFacetQuery;

                            var formatTitleUrl = function(elCell, oRecord, oColumn, sData) {
                                var cellContent;
                                if (sData.length< 1) {
                                    cellContent = "[no title]";
                                //} else if (sData.length > 150) {
                                //    cellContent = sData.substring(0,150) + "...";
                                } else {
                                    cellContent = sData;
                                }
                                elCell.innerHTML = "<a href='" + "${pageContext.request.contextPath}/" + oRecord.getData("urlMapper") + "/" + oRecord.getData("pid") + "' title='view record details'>" + cellContent + "</a>";
                            };

                            myDataSource = new YAHOO.util.DataSource("${pageContext.request.contextPath}/ajax?propertyName="+thisPropertyName+"&query="+thisQuery+"&facetQuery="+thisFacetQuery+"&");
                            myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
                            myDataSource.responseSchema = {
                                resultsList: "records",
                                fields: ["pid","title","contentModel","contentModelInitial","contentModelLabel","urlMapper","rank", "rankId"],
                                metaFields: {
                                    totalRecords: "totalRecords",
                                    paginationRecordOffset : "startIndex",
                                    sortKey: "sort",
                                    sortDir: "dir"
                                }
                            };

                            // Column definitions
                            var myColumnDefs = [
                                //{key:"ContentModel", field:"contentModelLabel", label:"Type", sortable: true, minWidth: 100},
                                {key:"dc3.title", field:"title", sortable:true, label:"Title", formatter:formatTitleUrl}, // , formatter:formatTitleUrl
                                {key:"rdf.hasRankId", field:"rank", label:"Classification", sortable: true, minWidth: 100},
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
                            <%--YAHOO.example.XHR_JSON = function() {
                                var formatTitleUrl = function(elCell, oRecord, oColumn, sData) {
                                    var cellContent;
                                    if (sData.length< 1) {
                                        cellContent = "[no title]";
                                    } else if (sData.length > 150) {
                                        cellContent = sData.substring(0,150) + "...";
                                    } else {
                                        cellContent = sData;
                                    }
                                    elCell.innerHTML = "<a href='" + "${pageContext.request.contextPath}/" + oRecord.getData("urlMapper") + "/" + oRecord.getData("pid") + "' title='view record details'>" + cellContent + "</a>";
                                };

                                var myColumnDefs = [
                                    {key:"ContentModel", field:"contentModelLabel", label:"Type", sortable: true},
                                    {key:"dc3.title", field:"title", sortable:true, label:"Title", formatter:formatTitleUrl}
                                ];

                                var myDataSource = new YAHOO.util.DataSource("${pageContext.request.contextPath}/ajax?propertyName="+thisPropertyName+"&query="+thisQuery+"&facetQuery="+facetQuery+"&");
                                myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
                                myDataSource.responseSchema = {
                                    resultsList: "result",
                                    fields: ["pid","title","contentModel","contentModelInitial","contentModelLabel","urlMapper"],
                                    metaFields: {totalRecords: "totalRecords"}
                                };

                                var myPaginator = new YAHOO.widget.Paginator({
                                    rowsPerPage : 20,
                                    pageLinks   : 9,
                                    rowsPerPageOptions: [10,20,50,100],
                                    template : "<div style='float:right;'>{RowsPerPageDropdown}</div> {FirstPageLink} {PreviousPageLink} {PageLinks} {NextPageLink} {LastPageLink} {CurrentPageReport}", //"{CurrentPageReport} {PageLinks}",
                                    pageReportTemplate : "<strong>{totalRecords} records.</strong>"
                                });

                                // DataTable configuration
                                var myConfigs = {
                                    initialRequest:"sort=ContentModel&dir=asc&startIndex=0&results=20",
                                    dynamicData: true,
                                    sortedBy: {key:"ContentModel", dir:"asc"},
                                    paginator: myPaginator
                                };

                                myDataTable = new YAHOO.widget.DataTable("results", myColumnDefs, myDataSource, myConfigs); 

                                myDataTable.handleDataReturnPayload = function(oRequest, oResponse, oPayload) {
                                    oPayload.totalRecords = oResponse.meta.totalRecords;
                                    return oPayload;
                                };
                                
                                return {
                                    ds: myDataSource,
                                    dt: myDataTable
                                };
                            }();
                        }
--%>
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