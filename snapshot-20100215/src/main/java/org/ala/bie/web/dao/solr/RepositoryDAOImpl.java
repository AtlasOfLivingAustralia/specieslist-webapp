/* *************************************************************************
 *  Copyright (C) 2009 Atlas of Living Australia
 *  All Rights Reserved.
 *
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/

package org.ala.bie.web.dao.solr;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.stereotype.Component;

import org.ala.bie.web.dao.RepositoryDAO;
import org.ala.bie.web.dto.CategorisedProperties;
import org.ala.bie.web.dto.Category;
import org.ala.bie.web.dto.DocumentDTO;
import org.ala.bie.web.dto.FacetResultDTO;
import org.ala.bie.web.dto.FieldResultDTO;
import org.ala.bie.web.dto.HtmlPageDTO;
import org.ala.bie.web.dto.ImageDTO;
import org.ala.bie.web.dto.OrderedDocumentDTO;
import org.ala.bie.web.dto.OrderedPropertyDTO;
import org.ala.bie.web.dto.SearchResultDTO;
import org.ala.bie.web.dto.SolrResultsDTO;
import org.ala.bie.web.dto.SourceDTO;
import org.ala.bie.web.dto.TaxonConceptDTO;
import org.ala.bie.web.dto.TaxonNameDTO;

/**
 * SOLR implementation of RepositoryDAO
 *
 * @see org.ala.bie.web.dao.RepositoryDAO
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
@Component("solrDAO")
public class RepositoryDAOImpl implements RepositoryDAO {

    /** log4 j logger */
    private static final Logger logger = Logger.getLogger(RepositoryDAOImpl.class);
    /** SOLR server instance */
    private CommonsHttpSolrServer server = null;
    /** URL of the SOLR servlet */
    private String solrUrl = "http://localhost:8080/solr";  // overridden from bie.properties
    /** Timeout in milliseconds for SOLR HTTP request */
    private Integer TIMEOUT_IN_MSEC = 5000;

    /**
     * Constructor - set the server field
     */
    public void FedoraDAOImpl() {
        initSolrServer();
    }

    /**
     * @see org.ala.bie.web.dao.RepositoryDAO#getDocumentsForName(java.lang.String)
     */
    @Override
    public List<DocumentDTO> getDocumentsForName(List<String> scientificNames) {

        List<DocumentDTO> propertiesList = new ArrayList<DocumentDTO>();
        try {
            SolrDocumentList solrDocumentList = doListQuery(scientificNames, "rdf.hasScientificName", null);
            logger.debug("######### " + solrDocumentList.size() + " documents returned.");
            Iterator iter = solrDocumentList.iterator();
            while (iter.hasNext()) {
                SolrDocument solrDocument = (SolrDocument) iter.next();

                Map<String, Object> fieldMap = solrDocument.getFieldValueMap();
                DocumentDTO propertiesDTO = new DocumentDTO();
                Set<String> keys = fieldMap.keySet();
                for (String key : keys) {
                    logger.debug("######### key: " + key + ", value: " + fieldMap.get(key));
                    propertiesDTO.getPropertyMap().put(key, fieldMap.get(key));
                }
                logger.debug("######### Source: " + solrDocument.getFieldValue("dc.source"));
                propertiesDTO.setInfoSourceName((String) fieldMap.get("dc.source"));
                propertiesDTO.setPropertyMap(sortByKey(fieldMap));
                propertiesList.add(propertiesDTO);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return propertiesList;
    }

    /**
     * @see org.ala.bie.web.dao.RepositoryDAO#getOrderedPropertiesForName(java.util.List)
     */
    @Override
    public List<OrderedPropertyDTO> getOrderedPropertiesForName(
            List<String> scientificNames) {
        List<OrderedPropertyDTO> orderedPropertyList = new ArrayList<OrderedPropertyDTO>();

        try {
            SolrDocumentList solrDocumentList = doListQuery(scientificNames, "rdf.hasScientificName", null);
            logger.info("######### " + solrDocumentList.size() + " documents returned.");
            Iterator iter = solrDocumentList.iterator();

            //iterate through each SOLR document and create an OrderedDocumentDTO
            while (iter.hasNext()) {

                SolrDocument solrDocument = (SolrDocument) iter.next();
                Map<String, Object> fieldMap = solrDocument.getFieldValueMap();

                Set<String> retrievedKeys = fieldMap.keySet();
                //FIXME
                String contentModel = (String) fieldMap.get("rdf.hasModel");
                if (contentModel != null && contentModel.endsWith("ImageContentModel")) {
                    //dont add these properties to result set
                    continue;
                }

                //for each key, find the correct category, and create a CategorisedProperties instance
                for (String propertyName : retrievedKeys) {

                    Category category = Category.getCategoryForProperty(propertyName);
                    if (category != null) {

                        logger.info("Category:" + category.getName() + ",  key: '" + propertyName + "', value:" + fieldMap.get(propertyName));

                        Object propertyValue = fieldMap.get(propertyName);

                        SourceDTO sourceDTO = new SourceDTO();

                        sourceDTO.setInfoSourceUrl(resolveSingleValue(solrDocument.getFieldValue("dc.source")));

                        //a display name for the infosource
                        sourceDTO.setInfoSourceName(resolveSingleValue(fieldMap.get("dc.publisher")));

                        //the URL of the web page
                        sourceDTO.setSourceUrl(resolveSingleValue(fieldMap.get("dc.identifier")));

                        //the title of the web page
                        sourceDTO.setSourceTitle(resolveSingleValue(fieldMap.get("dc.title")));

                        if (propertyValue instanceof List) {
                            List<Object> propertyValues = (List) propertyValue;
                            for (Object singleValue : propertyValues) {
                                addOrderedProperty(orderedPropertyList,
                                        propertyName, category, propertyValue,
                                        sourceDTO);
                            }
                        } else {
                            addOrderedProperty(orderedPropertyList,
                                    propertyName, category, propertyValue,
                                    sourceDTO);
                        }
                    } else {
                        logger.info("No category found for key: '" + propertyName + "', value:" + fieldMap.get(propertyName));
                    }
                }
            }

            //order properties by category and by order internal to category
            Collections.sort(orderedPropertyList, new Comparator<OrderedPropertyDTO>() {

                @Override
                public int compare(OrderedPropertyDTO o1, OrderedPropertyDTO o2) {
                    if (o1.getCategory().equals(o2.getCategory())) {
                        Category category = o1.getCategory();
                        return category.getIndexInCategory(o1.getPropertyName()) - category.getIndexInCategory(o2.getPropertyName());
                    } else {
                        return o1.getCategory().getRank() - o2.getCategory().getRank();
                    }
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return orderedPropertyList;
    }

    /**
     * Add the property to the list or find an existing property with the same value and
     * update the list of sources.
     *
     * @param orderedPropertyList
     * @param propertyName
     * @param category
     * @param propertyValue
     * @param sourceDTO
     */
    private void addOrderedProperty(List<OrderedPropertyDTO> orderedPropertyList, String propertyName,
            Category category, Object propertyValue, SourceDTO sourceDTO) {
        //check for empty strings
        String propValue = StringUtils.trimToNull(propertyValue.toString());
        if (propValue != null) {
            OrderedPropertyDTO orderedPropertyDTO = retrieveProperty(orderedPropertyList, propertyName, propValue);
            if (orderedPropertyDTO == null) {
                //not retrieved, so create a new one
                orderedPropertyDTO = createObjectProperty(sourceDTO, category, propertyName, propValue);
                orderedPropertyList.add(orderedPropertyDTO);
            } else {
                orderedPropertyDTO.getSources().add(sourceDTO);
            }
        }
    }

    /**
     * Retrieves the property from the list if it exists
     *
     * @param orderedPropertyList
     * @param propertyName
     * @param propValue
     * @return
     */
    private OrderedPropertyDTO retrieveProperty(
            List<OrderedPropertyDTO> orderedPropertyList, String propertyName,
            String propValue) {

        for (OrderedPropertyDTO orderedProperty : orderedPropertyList) {
            if (orderedProperty.getPropertyName().equalsIgnoreCase(propertyName)
                    && orderedProperty.getPropertyValue().equalsIgnoreCase(propValue)) {
                return orderedProperty;
            }
        }
        return null;
    }

    /**
     * Create a Ordered Property.
     *
     * @param solrDocument
     * @param fieldMap
     * @param category
     * @param propertyName
     * @param propertyValue
     * @return
     */
    private OrderedPropertyDTO createObjectProperty(SourceDTO sourceDTO, Category category, String propertyName,
            Object propertyValue) {
        //create property
        OrderedPropertyDTO orderedPropertyDTO = new OrderedPropertyDTO();

        //the ID of the infosource

        orderedPropertyDTO.getSources().add(sourceDTO);
        orderedPropertyDTO.setCategory(category);
        orderedPropertyDTO.setPropertyName(propertyName);
        orderedPropertyDTO.setPropertyValue(propertyValue.toString());

        return orderedPropertyDTO;
    }

    /**
     * Resolve a single value.
     *
     * @param fieldValue
     * @return
     */
    private String resolveSingleValue(Object fieldValue) {
        if (fieldValue instanceof List) {
            List<Object> values = (List) fieldValue;
            if (!values.isEmpty()) {
                return values.get(0).toString();
            } else {
                return null;
            }
        }
        if (fieldValue != null) {
            return fieldValue.toString();
        }
        return null;
    }

    /**
     * @see org.ala.bie.web.dao.RepositoryDAO#getDocumentsForName(java.lang.String)
     */
    @Override
    public List<OrderedDocumentDTO> getOrderedDocumentsForName(List<String> scientificNames) {

        List<OrderedDocumentDTO> orderedDocumentList = new ArrayList<OrderedDocumentDTO>();
        try {
            SolrDocumentList solrDocumentList = doListQuery(scientificNames, "rdf.hasScientificName", null);
            logger.info("######### " + solrDocumentList.size() + " documents returned.");
            Iterator iter = solrDocumentList.iterator();

            //iterate through each SOLR document and create an OrderedDocumentDTO
            while (iter.hasNext()) {
                SolrDocument solrDocument = (SolrDocument) iter.next();

                Map<String, Object> fieldMap = solrDocument.getFieldValueMap();
                OrderedDocumentDTO orderedDocument = new OrderedDocumentDTO();
                Set<String> retrievedKeys = fieldMap.keySet();

                Map<String, CategorisedProperties> catPropMap = new HashMap<String, CategorisedProperties>();

                //for each key, find the correct category, and create a CategorisedProperties instance
                for (String key : retrievedKeys) {

                    Category category = Category.getCategoryForProperty(key);
                    if (category != null) {
                        logger.info("Category:" + category.getName() + ",  key: '" + key + "', value:" + fieldMap.get(key));

                        CategorisedProperties categorisedProperties = catPropMap.get(category.getName());

                        if (categorisedProperties == null) {
                            categorisedProperties = new CategorisedProperties();
                            categorisedProperties.setCategory(category);
                            categorisedProperties.getPropertyMap().put(key, (String) fieldMap.get(key));
                            catPropMap.put(category.getName(), categorisedProperties);
                        } else {
                            categorisedProperties.getPropertyMap().put(key, (String) fieldMap.get(key));
                        }
                    } else {
                        logger.info("No category found for key: '" + key + "', value:" + fieldMap.get(key));
                    }

                    if ("rdf.hasModel".equals(key)) {
                        String model = (String) fieldMap.get(key);
                        orderedDocument.setDocumentType(model);
                    }
                }

                //order categories by rank
                Collection<CategorisedProperties> catProperties = catPropMap.values();

                List<CategorisedProperties> catPropertiesList = new ArrayList<CategorisedProperties>();
                catPropertiesList.addAll(catProperties);
                Collections.sort(catPropertiesList, new Comparator<CategorisedProperties>() {

                    @Override
                    public int compare(CategorisedProperties o1,
                            CategorisedProperties o2) {
                        return o1.getCategory().getRank() - o2.getCategory().getRank();
                    }
                });

                //order the properties, split by category. order by the order specified in category definition
                for (CategorisedProperties categorisedProperties : catPropertiesList) {
                    //sort the properties in each category
                    final Category category = categorisedProperties.getCategory();
                    Map<String, String> propertyMap = categorisedProperties.getPropertyMap();

                    //create a sorted list of the keys
                    List<String> orderedKeys = new ArrayList<String>();
                    orderedKeys.addAll(propertyMap.keySet());
                    Collections.sort(orderedKeys, new Comparator<String>() {

                        @Override
                        public int compare(String o1, String o2) {
                            return category.getIndexInCategory(o1) - category.getIndexInCategory(o2);
                        }
                    });

                    //create a ordered list of keys
                    LinkedHashMap<String, String> orderedProps = new LinkedHashMap<String, String>();
                    for (String orderedKey : orderedKeys) {
                        orderedProps.put(orderedKey, propertyMap.get(orderedKey));
                    }

                    //replace existing map with the ordered version
                    categorisedProperties.setPropertyMap(orderedProps);
                }

                logger.info("DC Source: " + solrDocument.getFieldValue("dc.source"));

                //the ID of the infosource
                orderedDocument.setInfoSourceUrl((String) fieldMap.get("dc.source"));

                //a display name for the infosource
                orderedDocument.setInfoSourceName((String) fieldMap.get("dc.publisher"));

                //the URL of the web page
                orderedDocument.setSourceUrl((String) fieldMap.get("dc.identifier"));

                //the title of the web page
                orderedDocument.setSourceTitle((String) fieldMap.get("dc.title"));

                orderedDocument.setCategorisedProperties(catPropertiesList);

                if (orderedDocument.getDocumentType() == null || !orderedDocument.getDocumentType().endsWith("ImageContentModel")) {
                    orderedDocumentList.add(orderedDocument);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return orderedDocumentList;
    }

    static Map sortByKey(Map<String, Object> map) {
        List<String> list = new LinkedList(map.keySet());
        Collections.sort(list);
        // logger.info(list);
        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            String key = (String) it.next();
            result.put(key, map.get(key));
        }
        return result;
    }

    /**
     * For a given list of TN guids, query SOLR and return a list of TaxonNameDTOs
     *
     * @param taxonNameIds
     * @return list of TaxonNameDTOs
     */
    @Override
    public List<TaxonNameDTO> getTaxonNamesForUrns(List<String> taxonNameIds) {
        List<TaxonNameDTO> tns = new ArrayList<TaxonNameDTO>();
        SolrDocumentList sdl = null;
        try {
            sdl = doListQuery(taxonNameIds, "dc.identifier", "ContentModel:ala.TaxonNameContentModel");
        } catch (SolrServerException ex) {
            logger.warn("Problem communicating with SOLR server." + ex.getMessage(), ex);
            return null;
        }

        for (SolrDocument doc : sdl) {
            String contentModel = (String) doc.getFirstValue("ContentModel");

            if (contentModel.equalsIgnoreCase("ala.TaxonNameContentModel")) {
                // Only want to request these values if the returned document is the expected type
                String pid = (String) doc.getFirstValue("PID");
                String title = (String) doc.getFirstValue("dc.title");
                String name = (String) doc.getFirstValue("rdf.hasNameComplete");
                String rank = (String) doc.getFirstValue("Rank");
                String guid = (String) doc.getFirstValue("rdf.hasGuid");
                List sourceList = (List) doc.getFieldValues("rdf.sameAs");
                String source = (String) sourceList.get(0); // TODO: Fix this code to check all values and pick out the one we want
                // populate bean with variables
                TaxonNameDTO tn = new TaxonNameDTO(pid, title, name, rank, guid, source);
                // Add the bean to the list
                tns.add(tn);
            }
        }

        return tns;
    }

    /**
     * Return a list of ImageDTOs for a given list of scientific names by querying
     * SOLR index.
     *
     * @param scientificNames the list of scientific names used to serach against
     * @return imageDTOs the list of ImageDTO objects populated with reults from SOLR search
     */
    @Override
    public List<ImageDTO> getImagesForScientificNames(List<String> scientificNames) {
        List<ImageDTO> imageDTOs = new ArrayList<ImageDTO>();
        SolrDocumentList sdl = null;
        try {
            sdl = doListQuery(scientificNames, "rdf.hasScientificName", "ContentModel:ala.ImageContentModel");
        } catch (SolrServerException ex) {
            logger.warn("Problem communicating with SOLR server." + ex.getMessage(), ex);
            return null;
        }

        for (SolrDocument doc : sdl) {
            String contentModel = (String) doc.getFirstValue("ContentModel");

            if (contentModel.equalsIgnoreCase("ala.ImageContentModel")) {
                // Only want to request these values if the returned document is the expected type
                ImageDTO image = new ImageDTO();
                image.setPid((String) doc.getFirstValue("PID"));
                image.setGuid((String) doc.getFirstValue("rdf.hasGuid"));
                image.setTitle((String) doc.getFirstValue("dc.title"));
                image.setDescription((String) doc.getFirstValue("dc.description"));
                image.setCountry((String) doc.getFirstValue("Country"));
                image.setRegion((String) doc.getFirstValue("Region"));
                image.setLatitude((String) doc.getFirstValue("rdf.latitude"));
                image.setLongitude((String) doc.getFirstValue("rdf.longitude"));
                image.setPhotoPage((String) doc.getFirstValue("rdf.hasPhotoPage"));
                image.setPhotoSourceUrl((String) doc.getFirstValue("rdf.hasPhotoSourceUrl"));
                image.setScientificName((String) doc.getFirstValue("rdf.hasScientificName"));// TODO: store list of scientificNames in imageDTO
                image.setSource((String) doc.getFirstValue("rdf.hasGuid"));
                // Add the bean to the list
                imageDTOs.add(image);
            }
        }

        return imageDTOs;
    }

    /**
     * Return a list of ImageDTOs for a given list of scientific names by querying
     * SOLR index.
     *
     * @param scientificNames the list of scientific names used to serach against
     * @return imageDTOs the list of ImageDTO objects populated with reults from SOLR search
     */
    @Override
    public List<HtmlPageDTO> getHtmlPagesForScientificNames(List<String> scientificNames) {
        List<HtmlPageDTO> htmlPageDTOs = new ArrayList<HtmlPageDTO>();
        SolrDocumentList sdl = null;
        try {
            sdl = doListQuery(scientificNames, "rdf.hasScientificName", "ContentModel:ala.HtmlPageContentModel");
        } catch (SolrServerException ex) {
            logger.warn("Problem communicating with SOLR server." + ex.getMessage(), ex);
            return null;
        }

        for (SolrDocument doc : sdl) {
            String contentModel = (String) doc.getFirstValue("ContentModel");

            if (contentModel.equalsIgnoreCase("ala.HtmlPageContentModel")) {
                // Only want to request these values if the returned document is the expected type
                HtmlPageDTO htmlPage = new HtmlPageDTO();
                htmlPage.setPid((String) doc.getFirstValue("PID"));
                htmlPage.setGuid((String) doc.getFirstValue("rdf.hasGuid"));
                htmlPage.setTitle((String) doc.getFirstValue("dc.title"));
                htmlPage.setGuid((String) doc.getFirstValue("rdf.hasScientificName"));
                htmlPage.setUrl((String) doc.getFirstValue("rdf.hasURL"));
                htmlPage.setSource((String) doc.getFirstValue("fgs.label"));
                // optional fields stored in HashMap
                Map fieldMap = doc.getFieldValueMap();
                HashMap<String, String> rdfProperties = new HashMap<String, String>();

                for (Object keyObj : fieldMap.keySet()) {
                    // Cast key to String
                    try {
                        String key = (String) keyObj;

                        if (key.startsWith("rdf")) {
                            String value = (String) fieldMap.get(keyObj);
                            rdfProperties.put(key, value);
                        }
                    } catch (Exception e) {
                        logger.error("Error parsing SOLR values: " + e.getMessage());
                    }
                }

                htmlPage.setRdfProperties(rdfProperties);

                // Add the bean to the list
                htmlPageDTOs.add(htmlPage);
            }
        }

        return htmlPageDTOs;
    }

    /**
     * @see org.ala.bie.web.dao.RepositoryDAO#getTaxonConceptForIdentifier(java.lang.String)
     *
     *
     * @param identifier the identifier to search with
     * @return tc the TaxonConceptDTO to return
     */
    @Override
    public TaxonConceptDTO getTaxonConceptForIdentifier(String identifier) {
        TaxonConceptDTO tc = null;
        try {
            String safeQuery = ClientUtils.escapeQueryChars(identifier);
            SolrQuery searchQuery = new SolrQuery(); // handle better?
            searchQuery.setQuery("dc.identifier:" + safeQuery);
            // do the Solr search
            if (server == null) {
                this.initSolrServer();
            }
            QueryResponse qr = null;
            qr = server.query(searchQuery); // can throw exception
            SolrDocumentList sdl = qr.getResults();
            logger.debug("SOLR request URL: "+qr.getRequestUrl());

            if (!sdl.isEmpty()) {
                SolrDocument doc = sdl.get(0); // assume 1 result
                String pid = (String) doc.getFirstValue("PID");
                tc = new TaxonConceptDTO(pid);
                tc.setGuid((String) doc.getFirstValue("rdf.hasGuid"));
                tc.setTitle((String) doc.getFirstValue("dc.title"));
                tc.setScientificName((String) doc.getFirstValue("dc.title")); // ideally should be rdf.hasScientificName
                tc.setRank((String) doc.getFirstValue("rdf.hasRank"));
                tc.setSource((String) doc.getFirstValue("fgs.label"));
                tc.setTaxonNameGuid((String) doc.getFirstValue("rdf.hasTaxonName"));
                List<String> childTaxaList = (List) doc.getFieldValues("rdf.hasIsParentTaxonOf");
                tc.setChildTaxa(childTaxaList);
                List<String> parentTaxaList = (List) doc.getFieldValues("rdf.hasIsChildTaxonOf");
                tc.setParentTaxa(parentTaxaList);
            }
        } catch (SolrServerException ex) {
            logger.warn("Problem communicating with SOLR server. " + ex.getMessage(), ex);
        }
        return tc;
    }

    /**
     * @see org.ala.bie.web.dao.RepositoryDAO#getFullTextSearchResults(java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String)
     *
     * TODO: Simplify this method and possibly reduce the DTO dependencies as the current code has been
     *       imported mostly "as is" from the BIE Struts webapp 
     *       @see csiro.diasb.fedora.SolrSearch.java#parseSolrResults(org.apache.solr.client.solrj.response.QueryResponse))
     *
     * @param filterQuery 
     * @return sr the SearchResultDTO to return
     */
    @Override
    public SolrResultsDTO getFullTextSearchResults(String query, String filterQuery, Integer startIndex,
            Integer pageSize, String sortField, String sortDirection) {
        SolrResultsDTO solrResult = new SolrResultsDTO();
        logger.info("Params = query: "+query+", filterQuery: "+filterQuery+", startIndex: "+startIndex+
                ", pageSize: "+pageSize+", sortField: "+sortField+", sortDirection: "+sortDirection);
        try {
            SolrQuery solrQuery = initFacetQuery();
            // set the query
            StringBuffer queryString = new StringBuffer(ClientUtils.escapeQueryChars(query));
            queryString.append(" AND rdf.hasModel:ala.TaxonConceptContentModel");
            solrQuery.setQuery(queryString.toString());
            // set the facet query if set
            if (filterQuery!=null && !filterQuery.isEmpty()) {
                // pull apart fq. E.g. Rank:species and then sanitize the string parts
                // so that special characters are escaped apporpriately
                String[] parts = filterQuery.split(":");
                String prefix = ClientUtils.escapeQueryChars(parts[0]);
                String suffix = ClientUtils.escapeQueryChars(parts[1]);
                solrQuery.addFilterQuery(prefix+":"+suffix); // solrQuery.addFacetQuery(facetQuery)
            }
            // Set the other params
            solrQuery.setRows(pageSize);
            solrQuery.setStart(startIndex);
            solrQuery.setSortField(sortField, ORDER.valueOf(sortDirection));
            // do the Solr search
            if (server == null) {
                this.initSolrServer();
            }
            QueryResponse qr = server.query(solrQuery); // can throw exception
            SolrDocumentList sdl = qr.getResults();
            List<FacetField> facets = qr.getFacetFields();

            Collection<SearchResultDTO> results = new ArrayList<SearchResultDTO>();
            Collection<FacetResultDTO> facetResults = new ArrayList<FacetResultDTO>();
            
            solrResult.setStartIndex(sdl.getStart());
            solrResult.setTotalRecords(sdl.getNumFound());
            solrResult.setPageSize(pageSize);
            solrResult.setSort(sortField);
            solrResult.setDir(sortDirection);
            solrResult.setStatus("OK");
            
            // populate SOLR search results
            if (!sdl.isEmpty()) {
                for (SolrDocument doc : sdl) {
                    String pid = (String) doc.getFirstValue("PID");
                    String title = (String) doc.getFirstValue("dc.title");
                    String rank = (String) doc.getFirstValue("Rank");
                    String rankId = (String) doc.getFirstValue("rdf.hasRankId");
                    SearchResultDTO sr = new SearchResultDTO(pid, title, rank, rankId);
                    sr.setGuid((String) doc.getFirstValue("rdf.hasGuid"));
                    sr.setContentModel((String) doc.getFirstValue("rdf.hasModel"));
                    // Highlighting
                    Map<String, List<String>> hlItem = qr.getHighlighting().get(pid);
                    if (hlItem != null && !hlItem.isEmpty())
                        sr.setHighLights(hlItem.entrySet());
                    results.add(sr);
                }
            }

            solrResult.setSearchResults(results);

            // populate SOLR facet results
            if (facets != null) {
                for (FacetField facet : facets) {
                    List<FacetField.Count> facetEntries = facet.getValues();

                    if ((facetEntries != null) && (facetEntries.size() > 0)) {
                        ArrayList<FieldResultDTO> r = new ArrayList<FieldResultDTO>();
                        for (FacetField.Count fcount : facetEntries) {
                            String msg = fcount.getName() + ": " + fcount.getCount();
                            //logger.trace(fcount.getName() + ": " + fcount.getCount());
                            r.add(new FieldResultDTO(fcount.getName(),fcount.getCount()));
                        }
                        FacetResultDTO fr = new FacetResultDTO(facet.getName(),r);
                        facetResults.add(fr);
                    }
                }
            }

            solrResult.setFacetResults(facetResults);
            // The query result is stored in its original format so that all the information returned is available later on if needed
            solrResult.setQr(qr);

        } catch (SolrServerException ex) {
            logger.warn("Problem communicating with SOLR server. " + ex.getMessage(), ex);
            solrResult.setStatus("ERROR"); // TODO also set a message field on this bean with the error message(?)
        }
        return solrResult;
    }

    /**
     * Helper method to format SOLR query (Boolean expression) and perform the SOLR search
     *
     * @param fieldValues
     * @param fieldName
     * @param contentModelFilter
     * @return SolrDocumentList
     * @throws SolrServerException
     */
    protected SolrDocumentList doListQuery(List<String> fieldValues, String fieldName, String contentModelFilter)
            throws SolrServerException {

        StringBuffer query = new StringBuffer();
        Iterator it = fieldValues.iterator();

        while (it.hasNext()) {
            String safeQueryStr = ClientUtils.escapeQueryChars(it.next().toString());
            query.append(fieldName);
            query.append(":");
            query.append(safeQueryStr);
            if (it.hasNext()) {
                query.append(" OR ");
            }
        }

        if (contentModelFilter != null && !contentModelFilter.isEmpty()) {
            // add optional contentModel filter, which requires parentheses around existing query
            // so that we get the expected precedence for Boolean expression
            query.insert(0, "(");
            query.append(") AND " + contentModelFilter);
        }

        SolrQuery searchQuery = new SolrQuery();  // handle better?
        searchQuery.setQuery(query.toString());
        logger.info("SOLR query: " + query.toString());
        // do the Solr search
        if (server == null) {
            this.initSolrServer();
        }
        QueryResponse qr = server.query(searchQuery); // can throw exception

        return qr.getResults();
    }

    /**
     * Helper method to create SolrQuery object and add facet settings
     * 
     * @return solrQuery the SolrQuery
     */
    protected SolrQuery initFacetQuery() {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQueryType("standard");
        solrQuery.setFacet(true);
        //addFacetField("ContentModel");
        solrQuery.addFacetField("Rank");
        solrQuery.addFacetField("PublicationType");
        solrQuery.addFacetField("CollectionID");
        solrQuery.addFacetField("GeographicRegionID");
        solrQuery.setFacetMinCount(1);
        solrQuery.addSortField("dc2.title", SolrQuery.ORDER.asc);
        solrQuery.setRows(10);
        solrQuery.setStart(0);

        //add highlights
        solrQuery.setHighlight(true);
        solrQuery.addHighlightField("text_all");
        solrQuery.addHighlightField("dc.title");
        solrQuery.addHighlightField("dc.description");
        solrQuery.addHighlightField("rdf.hasRank");
        solrQuery.addHighlightField("rdf.hasRegion");
        solrQuery.addHighlightField("rdf.hasCountry");
        solrQuery.addHighlightField("rdf.hasContentModel");
        // Note that this wildcard highlight field doesn't seem to work, although the SOLR documentation suggests that it should
        solrQuery.addHighlightField("rdf.has*");
        return solrQuery;
    }

    /**
     * returns a pointer to the SOLR server singleton stored in fedoraAPI
     * @return
     */
    public CommonsHttpSolrServer initSolrServer() {
        if (this.server == null & solrUrl != null) {
            try {
                this.server = new CommonsHttpSolrServer(solrUrl);
                this.server.setSoTimeout(TIMEOUT_IN_MSEC);
            } catch (MalformedURLException e) {
                logger.error(e.getLocalizedMessage());
            }
        }

        return this.server;
    }

    /**
     * @see csiro.diasb.dao.RepositoryDAO#getServerUrl()
     *
     * @return
     */
    @Override
    public String getServerUrl() {
        return solrUrl;
    }

    public CommonsHttpSolrServer getServer() {
        return server;
    }

    public void setServer(CommonsHttpSolrServer server) {
        this.server = server;
    }

    public String getSolrUrl() {
        return solrUrl;
    }

    public void setSolrUrl(String solrUrl) {
        this.solrUrl = solrUrl;
    }

    public Integer getTIMEOUT_IN_MSEC() {
        return TIMEOUT_IN_MSEC;
    }

    public void setTIMEOUT_IN_MSEC(Integer TIMEOUT_IN_MSEC) {
        this.TIMEOUT_IN_MSEC = TIMEOUT_IN_MSEC;
    }
}
