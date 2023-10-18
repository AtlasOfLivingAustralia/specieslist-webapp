/*
 * Copyright (C) 2022 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */
package au.org.ala.specieslist

import au.ala.org.ws.security.RequireApiKey
import au.org.ala.plugins.openapi.Path
import au.org.ala.web.UserDetails
import au.org.ala.ws.service.WebService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.opencsv.CSVWriter
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.apache.http.HttpStatus

import static io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY

/**
 * Provides all the webservices to be used from other sources eg the BIE
 */
class WebServiceController {

    def helperService
    def authService
    def localAuthService
    def queryService
    def apiKeyService

    def index() {}

    @Operation(
        method = "GET",
        tags = "List Items",
        operationId = "Get distinct values for a field in species list item ",
        summary = "Get distinct values for a field in species list item ",
        description = "Get a list distinct values for a field in species list item ",
        parameters = [
            @Parameter(name = "field",
                in = PATH,
                description = "The field e.g.(kingdom, matchedName, rawScientificName etc.) to get distinct values for across all species list items",
                schema = @Schema(implementation = String),
                required = true),
        ],
        responses = [
            @ApiResponse(
                description = "List of distinct values for a specified species list item field",
                responseCode = "200",
                content = [
                    @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = String))
                    )
                ],
                headers = [
                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "String"))
                ]
            )
        ]
    )
    @Path("/ws/speciesListItems/distinct/{field}")
    def getDistinctValues() {
        def field = params.field

        def props = [fetch: [mylist: 'join']]
        log.debug("Distinct values " + field + " " + params)

        // fetch lists that this user has access to view
        def hidePrivateLists = grailsApplication.config.getProperty('publicview.hidePrivateLists', Boolean, false)
        def list = queryService.visibleLists(true, hidePrivateLists)

        def results = queryService.getFilterListItemResult(props, params, null, list, field)
        render results as JSON
    }

    /**
     *
     * @return
     *
     * View Access controlled by WebServiceInterceptor
     */
    @Operation(
        method = "GET",
        tags = "List Items",
        operationId = "Get guid(s) contained in a species list",
        summary = "Get  guid(s) contained in a species list",
        description = "Get a list of guid(s) for taxa/species list items contained in a species list ",
        parameters = [
            @Parameter(name = "druid",
                in = PATH,
                description = "The data resource id to identify a list",
                schema = @Schema(implementation = String),
                required = true),
        ],
        responses = [
            @ApiResponse(
                description = "Species List item guid(s)",
                responseCode = "200",
                content = [
                    @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = String))
                    )
                ],
                headers = [
                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "String"))
                ]
            )
        ]
    )
    @Path("/ws/speciesList/{druid}/taxa")
    def getTaxaOnList() {
        def druid = params.druid
        def results = SpeciesListItem.executeQuery("select guid from SpeciesListItem where dataResourceUid=:dataResourceUid", [dataResourceUid: druid])
        render results as JSON
    }


    @Operation(
        method = "GET",
        tags = "List Items",
        operationId = "Get species list items details for specified guid(s)",
        summary = "Get species list items details for specified guid(s)",
        description = "Get details of species list items i.e species for a list of guid(s)",
        parameters = [
            // the "required" attribute is overridden to true when the parameter type is PATH.
            @Parameter(name = "guid",
                in = PATH,
                description = "A comma separated list of guid(s) to identify a species list item i.e. species.",
                schema = @Schema(implementation = String),
                required = true),
            @Parameter(name = "dr",
                in = QUERY,
                description = "A comma separated list of data resource ids to identify species lists to return matching list items from",
                schema = @Schema(implementation = String),
                required = false),
            @Parameter(name = "isBIE",
                in = QUERY,
                description = "The boolean value to specify whether the request is from the BIE",
                schema = @Schema(implementation = Boolean),
                required = false),
        ],
        responses = [
            @ApiResponse(
                description = "Species List item(s) details",
                responseCode = "200",
                content = [
                    @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = GetListItemsForSpeciesResponse))
                    )
                ],
                headers = [
                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "String"))
                ]
            )
        ]
    )
    @Path("/ws/species/{guid}")
    def getListItemsForSpecies() {
        def guid = params.guid.replaceFirst("https:/", "https://")
        def lists = params.dr?.split(",")
        def queryParams = params.subMap(["max","sort","offset"])
        def filters = params.findAll{it -> !["controller","action","max","sort","offset","guid", "dr"].contains(it.key)}
        def results = queryService.getListForSpecies(guid, params.isBIE, lists, queryParams, filters )

        // fetch lists that this user has access to view
        def hidePrivateLists = grailsApplication.config.getProperty('publicview.hidePrivateLists', Boolean, false)
        def permittedPrivateLists = queryService.visibleLists(false, hidePrivateLists)

        def listOfRecordMaps = results.findResults {
            // don't output private lists
            (!it.mylist.isPrivate) || permittedPrivateLists.contains(it.dataResourceUid) ?
            [
                dataResourceUid: it.dataResourceUid,
                guid           : it.guid,
                list           : [
                    username: it.mylist.username,
                    listName: it.mylist.listName,
                    sds     : it.mylist.isSDS ?: false,
                    isBIE   : it.mylist.isBIE ?: false
                ],
                kvpValues      : it.kvpValues.collect { kvp ->
                    [
                        key       : kvp.key,
                        value     : kvp.value,
                        vocabValue: kvp.vocabValue
                    ]
                }
            ] : null
        }
        render listOfRecordMaps as JSON
    }

    /**
     *   Returns either a JSON list of species lists or a specific species list
     *
     * @param druid - the data resource uid for the list to return  (optional)
     * @param splist - optional instance (added by the beforeInterceptor)
     *
     * View Access controlled by WebServiceInterceptor
     */
    @Operation(
        method = "GET",
        tags = "Lists",
        operationId = "Get species list(s) detail",
        summary = "Get species list(s) detail",
        description = "Get details of species lists or a specific list",
        parameters = [
            // the "required" attribute is overridden to true when the parameter type is PATH.
            @Parameter(name = "druid",
                in = PATH,
                description = "The data resource id to identify a list. This parameter is required for requesting a species list but optional for requesting species lists ",
                schema = @Schema(implementation = String),
                required = false),
            @Parameter(name = "sort",
                in = QUERY,
                description = "The field  on which to sort the returned results",
                schema = @Schema(implementation = String),
                required = false),
            @Parameter(name = "order",
                description = "The order to return the results in i.e asc or desc",
                schema = @Schema(implementation = Integer),
                required = false),
            @Parameter(name = "max",
                in = QUERY,
                description = "The number of records to return",
                schema = @Schema(implementation = Integer),
                required = false),
            @Parameter(name = "offset",
                in = QUERY,
                description = "The records offset, to enable paging",
                schema = @Schema(implementation = Integer),
                required = false)
        ],
        responses = [
            @ApiResponse(
                description = "Species List(s)",
                responseCode = "200",
                content = [
                    @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = GetListsResponse)
                    )
                ],
                headers = [
                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "String"))
                ]
            )
        ]
    )
    @Path("/ws/speciesList/{druid}?")
    def getListDetails() {
        log.debug("params" + params)
        if (params.splist) {
            def sl = params.splist
            log.debug("The speciesList: " + sl)
            def retValue = [
                dataResourceUid: sl.dataResourceUid,
                listName       : sl.listName,
                dateCreated    : sl.dateCreated,
                lastUpdated    : sl.lastUpdated,
                lastUploaded   : sl.lastUploaded,
                lastMatched    : sl.lastMatched,
                username       : sl.username,
                fullName       : sl.getFullName(),
                itemCount      : sl.itemsCount,//SpeciesListItem.countByList(sl)
                isAuthoritative: (sl.isAuthoritative ?: false),
                isInvasive     : (sl.isInvasive ?: false),
                isThreatened   : (sl.isThreatened ?: false),
                looseSearch    : sl.looseSearch/*,
                searchStyle    : sl.searchStyle?.toString()*/
            ]
            if (sl.listType) {
                retValue["listType"] = sl?.listType?.toString()
            }
            log.debug(" The retvalue: " + retValue)
            render retValue as JSON
        } else {
            //we need to return a summary of all lists
            //allowing for customisation in sort order and paging
            params.fetch = [items: 'lazy']
            if (params.sort)
                params.user = null
            if (!params.user)
                params.sort = params.sort ?: "listName"
            if (params.sort == "count") params.sort = "itemsCount"
            params.order = params.order ?: "asc"

            //AC 20141218: Previous behaviour was ignoring custom filter code in queryService.getFilterListResult when params.user
            //parameter was present and params.sort was absent. Moved special case sorting when params.user is present
            //and params.sort is absent into queryService.getFilterListResults so the custom filter code will always be applied.

            def allLists = queryService.getFilterListResult(params, false)
            def listCounts = allLists.totalCount
            def retValue = [listCount: listCounts, sort: params.sort, order: params.order, max: params.max, offset: params.offset,
                            lists    : allLists.collect {
                                [
                                 dataResourceUid: it.dataResourceUid,
                                 listName       : it.listName,
                                 listType       : it?.listType?.toString(),
                                 dateCreated    : it.dateCreated,
                                 lastUpdated    : it.lastUpdated,
                                 lastUploaded   : it.lastUploaded,
                                 lastMatched    : it.lastMatched,
                                 username       : it.username,
                                 fullName       : it.getFullName(),
                                 itemCount      : it.itemsCount,
                                 region         : it.region,
                                 category       : it.category,
                                 generalisation : it.generalisation,
                                 authority      : it.authority,
                                 sdsType        : it.sdsType,
                                 isAuthoritative: it.isAuthoritative ?: false,
                                 isInvasive     : it.isInvasive ?: false,
                                 isThreatened   : it.isThreatened ?: false,
                                 looseSearch    : it.looseSearch,
                                 //searchStyle    : it.searchStyle?.toString(),
                                 wkt            : it.wkt
                                ]
                            }]
            render retValue as JSON
        }
    }

    /**
     *   Returns either a JSON list of species lists or a specific species list
     *
     * @param druid - the data resource uid for the list to return  (optional)
     * @param splist - optional instance (added by the beforeInterceptor)
     *
     * View Access controlled by WebServiceInterceptor
     */
    @Operation(
            method = "GET",
            tags = "Lists",
            operationId = "Get species list(s) detail",
            summary = "Get species list(s) detail",
            description = "Get details of species lists or a specific list",
            parameters = [
                    // the "required" attribute is overridden to true when the parameter type is PATH.
                    @Parameter(name = "druid",
                            in = PATH,
                            description = "The data resource id to identify a list. This parameter is required for requesting a species list but optional for requesting species lists ",
                            schema = @Schema(implementation = String),
                            required = false),
                    @Parameter(name = "sort",
                            in = QUERY,
                            description = "The field  on which to sort the returned results",
                            schema = @Schema(implementation = String),
                            required = false),
                    @Parameter(name = "order",
                            description = "The order to return the results in i.e asc or desc",
                            schema = @Schema(implementation = Integer),
                            required = false),
                    @Parameter(name = "max",
                            in = QUERY,
                            description = "The number of records to return",
                            schema = @Schema(implementation = Integer),
                            required = false),
                    @Parameter(name = "offset",
                            in = QUERY,
                            description = "The records offset, to enable paging",
                            schema = @Schema(implementation = Integer),
                            required = false)
            ],
            responses = [
                    @ApiResponse(
                            description = "Species List(s)",
                            responseCode = "200",
                            content = [
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = GetListsResponse)
                                    )
                            ],
                            headers = [
                                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "String")),
                                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "String")),
                                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "String"))
                            ]
                    )
            ]
    )
    @RequireApiKey(scopes = ['ala/internal'])
    @Path("/ws/speciesListInternal/{druid}?")
    def getListDetailsInternal() {
        // set splist manually because splist is set in the WebServiceInterceptor that is not triggered by this function
        params.splist = SpeciesList.findByDataResourceUid(params.druid)

        getListDetails()
    }

    /**
     * Returns a summary list of items that form part of the supplied species list.
     *
     * View Access controlled by WebServiceInterceptor
     */
    @Operation(
        method = "GET",
        tags = "List Items",
        operationId = "Get species list(s) item details",
        summary = "Get species list(s) item details",
        description = "Get details of individual items i.e. species for specified species list(s)",
        parameters = [
            // the "required" attribute is overridden to true when the parameter type is PATH.
            @Parameter(name = "druid",
                in = PATH,
                description = "The data resource id or comma separated data resource ids  to identify list(s) to return list items for e.g. '/ws/speciesListItems/dr123,dr781,dr332'",
                schema = @Schema(implementation = String),
                required = true),
            @Parameter(name = "q",
                in = QUERY,
                description = "Optional query string to search common name, supplied name and scientific name in the lists specified by the 'druid' e.g. 'Eurystomus orientalis'",
                schema = @Schema(implementation = String),
                required = false),
            @Parameter(name = "nonulls",
                in = QUERY,
                description = "The value to specify whether to include or exclude species list item with null value for species guid",
                schema = @Schema(implementation = Boolean),
                required = false),
            @Parameter(name = "sort",
                in = QUERY,
                description = "The field  on which to sort the returned results. Default is 'itemOrder'",
                schema = @Schema(implementation = String),
                required = false),
            @Parameter(name = "order",
                description = "The order to return the results in i.e asc or desc",
                schema = @Schema(implementation = Integer),
                required = false),
            @Parameter(name = "max",
                in = QUERY,
                description = "The number of records to return",
                schema = @Schema(implementation = Integer),
                required = false),
            @Parameter(name = "offset",
                in = QUERY,
                description = "The records offset, to enable paging",
                schema = @Schema(implementation = Integer),
                required = false),
            @Parameter(name = "includeKVP",
                in = QUERY,
                description = "The value to specify whether to include KVP (key value pairs) values in the returned list item ",
                schema = @Schema(implementation = Boolean),
                required = false)

        ],
        responses = [
            @ApiResponse(
                description = "Species list item(s)",
                responseCode = "200",
                content = [
                    @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = GetListItemsResponse))
                    )
                ],
                headers = [
                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "String"))
                ]
            )
        ]
    )
    @Path("/ws/speciesListItems/{druid}")
    def getListItemDetails() {
        if (params.druid) {
            // This method supports a comma separated list of druid
            List druid = params.druid.split(',')
            params.sort = params.sort ?: "itemOrder" // default to order the items were imported in
            params.max = params.max ?: 400 // set default to 400 to prevent api gateway content size limit block
            def list

            if (!params.q) {
                list = params.nonulls ?
                    SpeciesListItem.findAllByDataResourceUidInListAndGuidIsNotNull(druid, params)
                    : SpeciesListItem.findAllByDataResourceUidInList(druid, params)
            } else {
                // if query parameter is passed, search in common name, supplied name and scientific name
                String query = "%${params.q}%"
                def criteria = SpeciesListItem.createCriteria()
                if (params.nonulls) {
                    // search among SpeciesListItem that has matched ALA taxonomy
                    list = criteria {
                        isNotNull("guid")
                        inList("dataResourceUid", druid)
                        or {
                            ilike("commonName", query)
                            ilike("matchedName", query)
                            ilike("rawScientificName", query)
                        }
                    }
                } else {
                    // search all SpeciesListItem
                    list = criteria {
                        inList("dataResourceUid", druid)
                        or {
                            ilike("commonName", query)
                            ilike("matchedName", query)
                            ilike("rawScientificName", query)
                        }
                    }
                }
            }

            List newList
            if (params.includeKVP?.toBoolean()) {
                newList = list.collect({
                    [id       : it.id, name: it.rawScientificName, commonName: it.commonName, scientificName: it.matchedName, lsid: it.guid, dataResourceUid: it.dataResourceUid,
                     kvpValues: it.kvpValues.collect({ [key: it.key, value: it.value] })]
                })
            } else {
                newList = list.collect { [id: it.id, name: it.rawScientificName, commonName: it.commonName, scientificName: it.matchedName, lsid: it.guid, dataResourceUid: it.dataResourceUid] }
            }
            render newList as JSON
        } else {
            render status: HttpStatus.SC_BAD_REQUEST, text: "druid parameter is required"
        }
    }

    /**
     * Returns a summary list of items that form part of the supplied species list.
     *
     * View Access controlled by WebServiceInterceptor
     */
    @Operation(
            method = "GET",
            tags = "List Items",
            operationId = "Get species list(s) item details",
            summary = "Get species list(s) item details",
            description = "Get details of individual items i.e. species for specified species list(s)",
            parameters = [
                    // the "required" attribute is overridden to true when the parameter type is PATH.
                    @Parameter(name = "druid",
                            in = PATH,
                            description = "The data resource id or comma separated data resource ids  to identify list(s) to return list items for e.g. '/ws/speciesListItems/dr123,dr781,dr332'",
                            schema = @Schema(implementation = String),
                            required = true),
                    @Parameter(name = "q",
                            in = QUERY,
                            description = "Optional query string to search common name, supplied name and scientific name in the lists specified by the 'druid' e.g. 'Eurystomus orientalis'",
                            schema = @Schema(implementation = String),
                            required = false),
                    @Parameter(name = "nonulls",
                            in = QUERY,
                            description = "The value to specify whether to include or exclude species list item with null value for species guid",
                            schema = @Schema(implementation = Boolean),
                            required = false),
                    @Parameter(name = "sort",
                            in = QUERY,
                            description = "The field  on which to sort the returned results. Default is 'itemOrder'",
                            schema = @Schema(implementation = String),
                            required = false),
                    @Parameter(name = "order",
                            description = "The order to return the results in i.e asc or desc",
                            schema = @Schema(implementation = Integer),
                            required = false),
                    @Parameter(name = "max",
                            in = QUERY,
                            description = "The number of records to return",
                            schema = @Schema(implementation = Integer),
                            required = false),
                    @Parameter(name = "offset",
                            in = QUERY,
                            description = "The records offset, to enable paging",
                            schema = @Schema(implementation = Integer),
                            required = false),
                    @Parameter(name = "includeKVP",
                            in = QUERY,
                            description = "The value to specify whether to include KVP (key value pairs) values in the returned list item ",
                            schema = @Schema(implementation = Boolean),
                            required = false)

            ],
            responses = [
                    @ApiResponse(
                            description = "Species list item(s)",
                            responseCode = "200",
                            content = [
                                    @Content(
                                            mediaType = "application/json",
                                            array = @ArraySchema(schema = @Schema(implementation = GetListItemsResponse))
                                    )
                            ],
                            headers = [
                                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "String")),
                                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "String")),
                                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "String"))
                            ]
                    )
            ]
    )
    @RequireApiKey(scopes = ['ala/internal'])
    @Path("/ws/speciesListItemsInternal/{druid}")
    def getListItemDetailsInternal() {
        getListItemDetails()
    }

    /**
     * Returns a summary list of items that form part of the supplied species list.
     *
     * View Access controlled by WebServiceInterceptor
     */
    @Operation(
        method = "GET",
        tags = "List Items",
        operationId = "Get species list(s) item details",
        summary = "Get species list(s) item details",
        description = "Get details of individual items i.e. species for specified species list(s) filter-able by specified fields",
        parameters = [
            // the "required" attribute is overridden to true when the parameter type is PATH.
            @Parameter(name = "druid",
                in = QUERY,
                description = "The data resource id or comma separated data resource ids  to identify list(s) to return list items for e.g. '/ws/speciesListItems/dr123,dr781,dr332'",
                schema = @Schema(implementation = String),
                required = true),
            @Parameter(name = "q",
                in = QUERY,
                description = "Optional query string to search common name, supplied name and scientific name in the lists specified by the 'druid' e.g. 'Eurystomus orientalis'",
                schema = @Schema(implementation = String),
                required = false),
            @Parameter(name = "fields",
                in = QUERY,
                description = "Used together with 'q', this specifies the field or fields  in list item and list item key value pairs (KVP) to apply the search query `q` against e.g. fields=commonName,group&q=bird",
                schema = @Schema(implementation = String),
                required = true),
            @Parameter(name = "nonulls",
                in = QUERY,
                description = "The value to specify whether to include or exclude species list item with null value for species guid",
                schema = @Schema(implementation = Boolean),
                required = false),
            @Parameter(name = "sort",
                in = QUERY,
                description = "The field  on which to sort the returned results. Default is 'itemOrder'",
                schema = @Schema(implementation = String),
                required = false),
            @Parameter(name = "order",
                description = "The order to return the results in i.e asc or desc",
                schema = @Schema(implementation = Integer),
                required = false),
            @Parameter(name = "max",
                in = QUERY,
                description = "The number of records to return",
                schema = @Schema(implementation = Integer),
                required = false),
            @Parameter(name = "offset",
                in = QUERY,
                description = "The records offset, to enable paging",
                schema = @Schema(implementation = Integer),
                required = false),
            @Parameter(name = "includeKVP",
                in = QUERY,
                description = "The value to specify whether to include KVP (key value pairs) values in the returned list item ",
                schema = @Schema(implementation = Boolean),
                required = false)

        ],
        responses = [
            @ApiResponse(
                description = "Species list item(s)",
                responseCode = "200",
                content = [
                    @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = GetListItemsResponse))
                    )
                ],
                headers = [
                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "String"))
                ]
            )
        ]
    )
    @Path("/ws/queryListItemOrKVP")
    def queryListItemOrKVP() {
        if (params.druid && params.fields) {
            List druid = params.druid.split(',')
            List listItemFields = ['rawScientificName', 'matchedName', 'commonName']
            List fields = params.fields.split(',')
            List speciesListItemFields = listItemFields.intersect(fields), kvpFields = fields - speciesListItemFields
            params.sort = params.sort ?: "itemOrder" // default to order the items were imported in
            def list
            if (!params.q) {
                list = params.nonulls ?
                    SpeciesListItem.findAllByDataResourceUidInListAndGuidIsNotNull(druid, params)
                    : SpeciesListItem.findAllByDataResourceUidInList(druid, params)
            } else {
                // if query parameter is passed, search in common name, supplied name and scientific name
                String query = "%${params.q}%"
                def criteria = SpeciesListItem.createCriteria()
                list = criteria.listDistinct {
                    inList("dataResourceUid", druid)
                    or {
                        speciesListItemFields.each { field ->
                            ilike(field, query)
                        }

                        if (kvpFields) {
                            kvpValues {
                                inList("key", kvpFields)
                                ilike("value", query)
                            }
                        }
                    }
                }
            }

            List newList
            if (params.includeKVP?.toBoolean()) {
                newList = list.collect({
                    [id       : it.id, rawScientificName: it.rawScientificName, commonName: it.commonName, matchedName: it.matchedName, lsid: it.guid, dataResourceUid: it.dataResourceUid,
                     kvpValues: it.kvpValues.collect({ [key: it.key, value: it.value] })]
                })
            } else {
                newList = list.collect { [id: it.id, rawScientificName: it.rawScientificName, commonName: it.commonName, matchedName: it.matchedName, lsid: it.guid, dataResourceUid: it.dataResourceUid] }
            }
            render newList as JSON
        } else {
            render status: HttpStatus.SC_BAD_REQUEST, text: "druid and fields parameters are required"
        }
    }

    /**
     *
     * @return
     *
     * View Access controlled by WebServiceInterceptor
     */
    @Operation(
        method = "GET",
        tags = "List Items",
        operationId = "Get all KVPs in a species list",
        summary = "Get all KVPs in species list",
        description = "Get all KVPs within a species list item for a specified species list",
        parameters = [
            @Parameter(name = "druid",
                in = PATH,
                description = "The data resource id or comma separated data resource ids to identify a species list",
                schema = @Schema(implementation = String),
                required = true),
        ],
        responses = [
            @ApiResponse(
                description = "List of of all available KVPs and the scientific name",
                responseCode = "200",
                content = [
                    @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = ListItemKVPResponse))
                    )
                ],
                headers = [
                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "String"))
                ]
            )
        ]
    )

    @Path("/ws/findSpeciesByName/")
    def findSpeciesByName() {
        def name = params.name
        def id = params.id
        def result = [:] as HashMap

        if (name && id) {
           def species = SpeciesListItem.findByRawScientificNameAndId(name.trim(), id)
            // default JSON converter does 'shallow' search, it won't convert the child table : matchedSpecies
            result = species.toMap()
        }
        render result as JSON
    }

    @Path("/ws/speciesListItemKvp/{druid}")
    def getSpeciesListItemKvp() {
        def speciesListDruid = params.druid

        List newList = []
        if (speciesListDruid) {
            def speciesList = SpeciesListItem.findAllByDataResourceUid(speciesListDruid)
            if (speciesList.size() > 0) {
                speciesList.each({
                    def scientificName = it.rawScientificName
                    if (it.kvpValues) {
                        Map kvps = new HashMap();
                        it.kvpValues.each {
                            if (kvps.containsKey(it.key)) {
                                def val = kvps.get(it.key) + "|" + it.value
                                kvps.put(it.key, val)
                            } else {
                                kvps.put(it.key, it.value)
                            }
                        }
                        newList.push(name: scientificName, kvps: kvps)
                    }
                })
            }
        }
        render newList as JSON

    }

    /**
     * Saves the details of the species list when no druid is provided in the JSON body
     * a new list is inserted.  Inserting a new list will fail if there are no items to be
     * included on the list.
     *
     * Two JSON structures are supported:
     *
     * - v1 (unstructured list items): {"listName": "list1",  "listType": "TEST", "listItems": "item1,item2,item3"}
     * - v2 (structured list items with KVP): { "listName": "list1", "listType": "TEST", "listItems": [ { "itemName":
     * "item1", "kvpValues": [ { "key": "key1", "value": "value1" }, { "key": "key2", "value": "value2" } ] } ] }
     *
     * Edit Access controlled by WebServiceInterceptor
     */
    @Operation(
        method = "POST",
        tags = "Lists",
        operationId = "Add or replace a species list",
        summary = "Add  or replace a species list",
        description = "Add new species list or replace an existing one. When no druid is provided in the JSON body, a new list will be created. Providing a druid in the path will attempt to update and existing list",
        requestBody = @RequestBody(
            description = "The JSON object containing new species list. Two JSON structures are supported: - v1 (unstructured list items): {\"listName\": \"list1\",  \"listType\": \"TEST\", \"listItems\": \"item1,item2,item3\"}, \n- v2 (structured list items with KVP): { \"listName\": \"list1\", \"listType\": \"TEST\", \"listItems\": [ { \"itemName\": \"item1\", \"kvpValues\": [ { \"key\": \"key1\", \"value\": \"value1\" }, { \"key\": \"key2\", \"value\": \"value2\" } ] } ] }",
            required = true,
            content = @Content(
                mediaType = 'application/json',
                schema = @Schema(implementation = FilterListsBody)
            )
        ),
        parameters = [
            @Parameter(name = "druid",
                in = PATH,
                description = "The data resource id to identify an existing list",
                schema = @Schema(implementation = String),
                required = true),
            @Parameter(name = "X-ALA-userId",  description="The user id to save the list against", in = HEADER, schema = @Schema(implementation = String), required = true)
        ],
        responses = [
            @ApiResponse(
                description = "List of druid which match the supplied filter",
                responseCode = "201",
                content = [
                    @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = SaveListResponse)
                    )
                ],
                headers = [
                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "String"))
                ]
            )
        ],
        security = [@SecurityRequirement(name = 'openIdConnect')]
    )
    @Path("/ws/speciesListPost/{druid}?")
    @RequireApiKey
    def saveList() {
        log.debug("Saving a user list")
        //create a new list
        try {
            def jsonBody = request.JSON
            def userCookie = null

            if (request.cookies) {
                userCookie = request.cookies.find { it.name == 'ALA-Auth' }
            }

            def userId = request.getHeader(WebService.DEFAULT_AUTH_HEADER)

            UserDetails user = null

            if (userId) {
                user = authService.getUserForUserId(userId)
            } else if (userCookie) {
                String username = java.net.URLDecoder.decode(userCookie.getValue(), 'utf-8')
                //test to see that the user is valid
                user = authService.getUserForEmailAddress(username)
            }

            boolean replaceList = true //default behaviour
            if (user) {
                if (jsonBody.listItems && jsonBody.listName) {
                    jsonBody.username = user.userName
                    log.warn(jsonBody?.toString())
                    def druid = params.druid

                    // This is passed in from web service call to make sure it doesn't replace existing list
                    if (jsonBody.replaceList == Boolean.FALSE) {
                        replaceList = jsonBody.replaceList
                    }

                    if (!druid) {
                        def drURL = helperService.addDataResourceForList([name: jsonBody.listName, username: user.userName])

                        if (drURL) {
                            druid = drURL.toString().substring(drURL.lastIndexOf('/') + 1)
                        } else {
                            badRequest "Unable to generate collectory entry."
                        }
                    }

                    def result = helperService.loadSpeciesListFromJSON(jsonBody, druid, replaceList)
                    created druid, result.speciesGuids
                } else {
                    badRequest "Missing compulsory mandatory properties."
                }
            } else {
                badRequest "Supplied username is invalid"
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e)
            render(status: 400, text: "Unable to parse JSON body. " + e.getMessage())
        }
    }

    private def created = { uid, guids ->
        response.addHeader 'druid', uid
        response.status = 201
        def outputMap = [status: 200, message: 'added species list', druid: uid, data: guids]
        render outputMap as JSON
    }

    private def badRequest = { text ->
        render(status: 400, text: text)
    }

    private def notFound = { text ->
        render(status: 404, text: text)
    }

    /**
     *
     * @return
     *
     * Edit Access controlled by WebServiceInterceptor
     */
    @Operation(
        method = "GET",
        tags = "List Items",
        operationId = "Mark species list items as published",
        summary = "Mark species list items as published",
        description = "Mark all species list items under thea specified species list as published",
        parameters = [
            @Parameter(name = "druid",
                in = PATH,
                description = "The data resource id to identify a list",
                schema = @Schema(implementation = String),
                required = true),
        ],
        responses = [
            @ApiResponse(
                description = "A message stating the operation has been successful",
                responseCode = "200",
                content = [
                    @Content(
                        mediaType = "text/html",
                        schema = @Schema(implementation = String)
                    )
                ],
                headers = [
                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "String"))
                ]
            )
        ]
    )
    @Path("ws/speciesList/publish/{druid}")
    @Transactional
    def markAsPublished() {
        //marks the supplied data resource as published
        if (params.druid) {
            SpeciesListItem.executeUpdate("update SpeciesListItem set isPublished=true where dataResourceUid='" + params.druid + "'")
            render "Species list " + params.druid + " has been published"
        } else {
            render(view: '../error', model: [message: "No data resource has been supplied"])
        }
    }

    /**
     *
     * TODO: Fix or remove this method. What uses this?
     *
     * @return
     */
    @Operation(
        method = "GET",
        tags = "List Items",
        operationId = "Get all the unpublished list items in batches of 100",
        summary = "Get all the unpublished list items in batches of 100",
        description = "Get all the unpublished list items in batches of 100",
        responses = [
            @ApiResponse(
                description = "A CSV text content with a list of unpublished list items",
                responseCode = "200",
                content = [
                    @Content(
                        schema = @Schema(implementation = String)
                    )
                ],
                headers = [
                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "String"))
                ]
            )
        ]
    )
    @Path("/ws/speciesList/unpublished")
    def getBieUpdates() {
        //retrieve all the unpublished list items in batches of 100
        def max = 100
        def offset = 0
        def hasMore = true
        def out = response.outputStream
        //use a criteria so that paging can be supported.
        def criteria = SpeciesListItem.createCriteria()
        while (hasMore) {
            def results = criteria.list([sort: "guid", max: max, offset: offset, fetch: [kvpValues: 'join']]) {
                isNull("isPublished")
                //order("guid")
            }
            //def results =SpeciesListItem.findAllByIsPublishedIsNull([sort:"guid",max: max, offset:offset,fetch:[kvpValues: 'join']])
            results.each {
                //each of the results are rendered as CSV
                def sb = '' << ''
                if (it.kvpValues) {
                    it.kvpValues.each { kvp ->
                        sb << toCsv(it.guid) << ',' << toCsv(it.dataResourceUid) << ',' << toCsv(kvp.key) << ',' << toCsv(kvp.value) << ',' << toCsv(kvp.vocabValue) << '\n'
                    }
                } else {
                    sb << toCsv(it.guid) << ',' << toCsv(it.dataResourceUid) << ',,,\n'
                }
                out.print(sb.toString())
            }
            offset += max
            hasMore = offset < results.getTotalCount()
        }
        out.close()
    }

    private def toCsv(value) {
        if (!value) return ""
        return '"' + value.replaceAll('"', '~"') + '"'
    }


    /**
     * Lists all unique keys from the key value pairs of all records owned by the requested data resource(s)
     *
     * @param druid one or more DR UIDs (comma-separated if there are more than 1)
     * @return JSON list of unique key names
     */
    @Operation(
        method = "GET",
        tags = "keys",
        operationId = "Get a list of keys in a species list",
        summary = "Get a list of keys in a species listt",
        description = "Get a list of keys in KVP available for a species list",
        parameters = [
            // the "required" attribute is overridden to true when the parameter type is PATH.
            @Parameter(name = "druid",
                in = QUERY,
                description = "The data resource id or comma separated data resource ids to identify list(s) to return the keys for",
                schema = @Schema(implementation = String),
                required = true),
        ],
        responses = [
            @ApiResponse(
                description = "List of keys present within a species list",
                responseCode = "200",
                content = [
                    @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = String))
                    )
                ],
                headers = [
                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "String"))
                ]
            )
        ]
    )
    @Path("/ws/speciesListItems/keys")
    def listKeys() {
        if (!params.druid) {
            response.status = HttpStatus.SC_BAD_REQUEST
            response.sendError(HttpStatus.SC_BAD_REQUEST, "Must provide a comma-separated list of druid value(s).")
        } else {
            List<String> druids = params.druid.split(",")

            List<String> filteredDruids = druids.findAll {isViewable(SpeciesList.findByDataResourceUid(it)) }

            def kvps = SpeciesListKVP.withCriteria {
                'in'("dataResourceUid", filteredDruids)

                projections {
                    distinct("key")
                }

                order("key")
            }

            render kvps as JSON
        }
    }

    /**
     * Lists common keys from a list of data resource ids
     *
     * @param druid one or more DR UIDs (comma-separated if there are more than 1)
     * @return JSON list of unique key names
     *
     * @example
     * if two lists have the following columns
     * list1 = ['rawScientificName', 'matchedName', 'commonName', 'colour', 'shape']
     * list1 = ['rawScientificName', 'matchedName', 'commonName', 'colour']
     * this will return
     * ['rawScientificName', 'matchedName', 'commonName', 'colour']
     */
    @Operation(
        method = "GET",
        tags = "keys",
        operationId = "Get a list of common keys in species lists ",
        summary = "Get a list of  common keys in species lists ",
        description = "Get a list of keys from KVP common across a list multiple species lists ",
        parameters = [
            @Parameter(name = "druid",
                in = QUERY,
                description = "Comma separated data resource ids to identify lists",
                schema = @Schema(implementation = String),
                required = true),
        ],
        responses = [
            @ApiResponse(
                description = "List of common keys present across multiple species lists",
                responseCode = "200",
                content = [
                    @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = String))
                    )
                ],
                headers = [
                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "String"))
                ]
            )
        ]
    )
    @Path("/ws/listCommonKeys")
    def listCommonKeys() {
        if (!params.druid) {
            response.status = HttpStatus.SC_BAD_REQUEST
            response.sendError(HttpStatus.SC_BAD_REQUEST, "Must provide a comma-separated list of druid value(s).")
        } else {
            List<String> druids = params.druid.split(",")

            List<String> filteredDruids = druids.findAll { isViewable(SpeciesList.findByDataResourceUid(it)) }

            Set intersection = new HashSet();

            filteredDruids?.each { druid ->
                def kvps = SpeciesListKVP.withCriteria {
                    'in'("dataResourceUid", [druid])

                    projections {
                        distinct("key")
                    }

                    order("key")
                }

                if (intersection.isEmpty()) {
                    intersection.addAll(kvps)
                } else {
                    intersection = intersection.intersect(kvps)
                }
            }

            render intersection as JSON
        }
    }


    /**
     * Finds species list items that contains specific keys
     *
     * @param druid one or more DR UIDs (comma-separated if there are more than 1). Mandatory.
     * @param keys one or more KVP keys (comma-separated if there are more than 1). Mandatory.
     * @param format either 'json' or 'csv'. Optional - defaults to json. Controls the output format.
     *
     * @return if format = json, {scientificName1: [{key: key1, value: value1}, ...], scientificName2: [{key: key1, value: value1}, ...]}. If format = csv, returns a CSV download with columns [ScientificName,Key1,Key2...].
     */
    @Operation(
        method = "GET",
        tags = "keys",
        operationId = "Finds species list items that contains specific keys ",
        summary = "Finds species list items that contains specific keys ",
        description = "Finds species list items that contains specific keys in their KVP",
        parameters = [
            @Parameter(name = "druid",
                in = QUERY,
                description = "A list of comma separated data resource ids to identify lists by",
                schema = @Schema(implementation = String),
                required = true),
            @Parameter(name = "keys",
                in = QUERY,
                description = "A list of comma separated keys to search species list items by",
                schema = @Schema(implementation = String),
                required = true),
        ],
        responses = [
            @ApiResponse(
                description = "Species list item names and their matched KVP",
                responseCode = "200",
                content = [
                    @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ListItemsByKeysResponse)
                    )
                ],
                headers = [
                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "String"))
                ]
            )
        ]
    )
    @Path("/ws/speciesListItems/byKeys")
    def listItemsByKeys() {
        if (!params.druid || !params.keys) {
            response.status = HttpStatus.SC_BAD_REQUEST
            response.sendError(HttpStatus.SC_BAD_REQUEST, "Must provide a comma-separated list of druid value(s) and a comma-separated list of key(s). Parameter 'format' is optional ('json' or 'csv').")
        } else {
            List<String> druids = params.druid.split(",")
            List<String> keys = params.keys.split(",")

            List<String> filteredDruids = druids.findAll { isViewable(SpeciesList.findByDataResourceUid(it)) }

            def listItems = SpeciesListItem.withCriteria {
                projections {
                    property "rawScientificName"
                    kvpValues {
                        property "key"
                        property "value"
                    }
                }

                'in'("dataResourceUid", filteredDruids)

                kvpValues {
                    'in'("key", keys)
                }

                order("rawScientificName")
            }

            Map<String, List> results = [:]

            listItems.each {
                if (!results.containsKey(it[0])) {
                    results[it[0]] = []
                }
                results[it[0]] << [key: it[1], value: it[2]]
            }

            if (!params.format || params.format.toLowerCase() == "json") {
                render results as JSON
            } else if (params.format.toLowerCase() == "csv") {
                StringWriter out = new StringWriter();
                CSVWriter csv = new CSVWriter(out)

                csv.writeNext((["ScientificName"] << keys.sort()).flatten() as String[])

                results.each { key, value ->
                    List line = []

                    line << key
                    results[key].each {
                        line << it.value
                    }

                    csv.writeNext(line as String[])
                }

                render(contentType: 'text/csv', text: out.toString())
            }
        }
    }

    @Operation(
        method = "POST",
        tags = "Lists",
        operationId = "Filter lists based on specified attributes",
        summary = "Filter lists based on specified attributes",
        description = "Search and filter lists based on specified attributes i.e. scientificNames and drIds",
        requestBody = @RequestBody(
            description = "The JSON object with filter attributes",
            required = true,
            content = @Content(
                mediaType = 'application/json',
                schema = @Schema(implementation = FilterListsBody)
            )
        ),
        responses = [
            @ApiResponse(
                description = "List of druid which match the supplied filter",
                responseCode = "200",
                content = [
                    @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = String))
                    )
                ],
                headers = [
                    @Header(name = 'Access-Control-Allow-Headers', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Methods', description = "CORS header", schema = @Schema(type = "String")),
                    @Header(name = 'Access-Control-Allow-Origin', description = "CORS header", schema = @Schema(type = "String"))
                ]
            )
        ]
    )
    @Path("/ws/speciesList/filter")
    def filterLists() {
        def json = request.getJSON()
        if (!json.scientificNames) {
            response.status = HttpStatus.SC_BAD_REQUEST
            response.sendError(HttpStatus.SC_BAD_REQUEST, "Must provide a JSON body with a mandatory list of scientific names to filter on. An optional list of data resource ids (drIds) can also be provided.")
        } else {

            // fetch lists that this user has access to view
            def hidePrivateLists = grailsApplication.config.getProperty('publicview.hidePrivateLists', Boolean, false)
            def list = queryService.visibleLists(true, hidePrivateLists)

            def filteredDrIds = json.drIds ? json.drIds.findAll { list.contains(it) } : list

            List<String> results = queryService.filterLists(json.scientificNames, filteredDrIds)

            render results as JSON
        }
    }

    @JsonIgnoreProperties('metaClass')
    static class SuccessResponse {
        boolean success = true
    }

    @JsonIgnoreProperties('metaClass')
    static class ListsReturnValue {
        String authority
        String category
        String dataResourceUid
        String dateCreated
        String fullName
        String generalisation
        Boolean isAuthoritative
        Boolean isInvasive
        Boolean isThreatened
        Integer itemCount
        String astUpdated
        String listName
        String listType
        String region
        String sdsType
        String username

    }

    @JsonIgnoreProperties('metaClass')
    static class GetListsResponse {
        Integer listCount
        List<ListsReturnValue> lists
        Integer max
        Integer offset
        String order
        String sort

    }

    @JsonIgnoreProperties('metaClass')
    static class GetListItemsResponse {
        String commonName
        String dataResourceUid
        Integer id
        List<HashMap<String, String>> kvpValues
        String lsid
        String name
        String scientificName
    }

    @JsonIgnoreProperties('metaClass')
    static class ListItemsByKeysResponse {
        HashMap<String, List<HashMap<String, String>>> value
    }

    @JsonIgnoreProperties('metaClass')
    static class FilterListsBody {
        List<String> scientificNames
        List<String> drIds
    }

    @JsonIgnoreProperties('metaClass')
    static class SpeciesListItemBody {
        String guid
        String rawScientificName
        HashMap<String, String> extra
    }

    @JsonIgnoreProperties('metaClass')
    static class GetListItemsForSpeciesResponse {
        String dataResourceUid
        String guid
        List<HashMap<String, String>> kvpValues
        HashMap<String, String> list
    }

    @JsonIgnoreProperties('metaClass')
    static class ListItemKVPResponse {
        List<HashMap<String, String>> kvps
        String name
    }

    @JsonIgnoreProperties('metaClass')
    static class SaveListResponse {
        Integer status
        String message
        String driod
        List<String> guid
    }

    /**
     * Check if list is public OR private and user is either owner, admin or on the specieslist's editors list.
     */
    private boolean isViewable(SpeciesList sl) {
        def isAllowed = false
        def loggedInUser = authService?.userId
        def hidePrivateLists = grailsApplication.config.getProperty('publicview.hidePrivateLists', Boolean, false)

        log.debug "Checking isCurrentUserEditorForList: loggedInUser = " + loggedInUser
        if (!sl) {
            log.debug "speciesList is null"
            isAllowed = false // saves repeating this check in subsequent else if
        } else if (!sl.isPrivate || !hidePrivateLists) {
            isAllowed = true
        } else if (sl.userId == loggedInUser) {
            log.debug "user is owner"
            isAllowed = true
        } else if (localAuthService.isAdmin()) {
            log.debug "user is ADMIN"
            isAllowed = true
        } else if (sl.editors.any { it == loggedInUser}) {
            log.debug "user is in editors list: " + sl.editors.join("|")
            isAllowed = true
        }

        log.debug "isAllowed = " + isAllowed
        return isAllowed
    }

    @Operation(
            method = "POST",
            tags = "createSpeciesListItem",
            operationId = "Create a species list item for a species list",
            summary = "Create a species list item for a species list",
            description = "Create a species list item for a species list",
            parameters = [
                    // the "required" attribute is overridden to true when the parameter type is PATH.
                    @Parameter(name = "druid",
                            in = QUERY,
                            description = "The data resource id",
                            schema = @Schema(implementation = String),
                            required = true)
            ],
            requestBody = @RequestBody(
                    description = "The JSON object containing new species list item guid, rawScientificName and extra fields and values.",
                    required = true,
                    content = @Content(
                            mediaType = 'application/json',
                            schema = @Schema(implementation = SpeciesListItemBody)
                    )
            ),
            responses = [
                    @ApiResponse(
                            responseCode = "200"
                    )
            ]
    )
    @Path("/ws/createItem")
    @RequireApiKey(scopes = ['ala/internal'])
    def createItem() {
        def list = SpeciesList.findByDataResourceUid(params.druid)

        if (list) {
            def json = request.JSON
            json.each { it ->
                params[it.key] = it.value
            }

            def response = helperService.createRecord(params)
            render(text: response.text, status: response.status)
        } else {
            render(status: 404)
        }
    }

    @Operation(
            method = "GET",
            tags = "deleteSpeciesListItem",
            operationId = "Delete a species list item for a species list",
            summary = "Delete a species list item for a species list",
            description = "Delete a species list item for a species list",
            parameters = [
                    // the "required" attribute is overridden to true when the parameter type is PATH.
                    @Parameter(name = "druid",
                            in = QUERY,
                            description = "The data resource id",
                            schema = @Schema(implementation = String),
                            required = true),
                    @Parameter(name = "guid",
                            in = QUERY,
                            description = "The taxon id",
                            schema = @Schema(implementation = String),
                            required = true)
            ],
            responses = [
                    @ApiResponse(
                            responseCode = "200"
                    )
            ]
    )
    @RequireApiKey(scopes = ['ala/internal'])
    @Transactional
    def deleteItem() {
        def sli = SpeciesListItem.findByDataResourceUidAndGuid(params.druid, params.guid)

        if (sli) {
            // remove attached KVP records
            // two step process to avoid java.util.ConcurrentModificationException
            def kvpRemoveList = [] as Set
            sli.kvpValues.each {
                kvpRemoveList.add(it)
            }
            kvpRemoveList.each {
                sli.removeFromKvpValues(it)
            }

            try {
                sli.delete(flush: true)
                render(text: message(code:'public.lists.view.table.delete.messages', default:'Record successfully deleted'), status: 200)

                sli.mylist.lastUploaded = new Date()
                sli.mylist.save()

            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                def message = "Could not delete SpeciesListItem: ${sli.rawScientificName}"
                //redirect(action: "show", id: p.id)
                render(text: message, status: 500)
            }
        } else {
            def message = "${message(code: 'default.not.found.message', args: [message(code: 'speciesListItem.label', default: 'Species List Item'), params.id])}"
            render(text: message, status: 404)
        }
    }


    /**
     * rematch existing SpeciesListItem
     */
    def rematchSpecies() {
        def result = helperService.rematchSpecies("developer", params.matchAll)
        def resp = result.toMap()
        render resp as JSON
    }

    def rematchStatus() {
        def result = helperService.queryRematchingProcess()
        def resp = result
        render resp as JSON
    }

    def handleException(final Exception e ) {
        log.error(e.message)
        return {error: e.message}
    }
}
