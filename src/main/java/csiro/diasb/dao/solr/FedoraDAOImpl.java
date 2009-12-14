/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: dos009 $
 * @version $Id:  $
 */
package csiro.diasb.dao.solr;

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


import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import csiro.diasb.dao.FedoraDAO;
import csiro.diasb.datamodels.CategorisedProperties;
import csiro.diasb.datamodels.Category;
import csiro.diasb.datamodels.DocumentDTO;
import csiro.diasb.datamodels.HtmlPageDTO;
import csiro.diasb.datamodels.ImageDTO;
import csiro.diasb.datamodels.OrderedDocumentDTO;
import csiro.diasb.datamodels.OrderedPropertyDTO;
import csiro.diasb.datamodels.TaxonConceptDTO;
import csiro.diasb.datamodels.TaxonNameDTO;

/**
 * Return a list of TaxonNameDTO's for a given list of taxon name identifiers (urn*).
 * Note: the imput list of identifiers are NOT PIDs but the original identifiers from AFD, etc.
 * 
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class FedoraDAOImpl implements FedoraDAO {
	
    /** log4 j logger */
    private static final Logger logger = Logger.getLogger(FedoraDAOImpl.class);
    /** SOLR server instance */
    private CommonsHttpSolrServer server = null;
    /** URL of the SOLR servlet */
    protected String solrUrl = "http://diasbdev1-cbr.vm.csiro.au:8080/solr";  // http://localhost:8080/solr
//    protected String solrUrl = "http://localhost:8080/solr";  // http://localhost:8080/solr

    /**
     * Constructor - set the server field
     */
    public void FedoraDAOImpl() {
        initSolrServer();
    }
    
    /**
     * @see csiro.diasb.dao.FedoraDAO#getDocumentsForName(java.lang.String)
     */
    @Override
	public List<DocumentDTO> getDocumentsForName(List<String> scientificNames) {
    	
    	List<DocumentDTO> propertiesList = new ArrayList<DocumentDTO>();
    	try {
    		SolrDocumentList solrDocumentList = doListQuery(scientificNames, "rdf.hasScientificName", null);
    		logger.debug("######### "+solrDocumentList.size()+ " documents returned.");
    		Iterator iter = solrDocumentList.iterator();
    		while(iter.hasNext()){
    			SolrDocument solrDocument = (SolrDocument) iter.next();
    			
    			Map<String, Object> fieldMap = solrDocument.getFieldValueMap();
    			DocumentDTO propertiesDTO = new DocumentDTO();
    			Set<String> keys = fieldMap.keySet();
    			for(String key: keys){
    				logger.debug("######### key: "+key+", value: "+fieldMap.get(key));   
    				propertiesDTO.getPropertyMap().put(key, fieldMap.get(key));
    			}
    			logger.debug("######### Source: "+solrDocument.getFieldValue("dc.source"));    			
    			propertiesDTO.setInfoSourceName((String) fieldMap.get("dc.source"));
    			propertiesDTO.setPropertyMap(sortByKey(fieldMap));
    			propertiesList.add(propertiesDTO);
    		}
    	} catch (Exception e){
    		logger.error(e.getMessage(),e);
    	}
		return propertiesList;
	}
    
    /**
     * @see csiro.diasb.dao.FedoraDAO#getOrderedPropertiesForName(java.util.List)
     */
	@Override
	public List<OrderedPropertyDTO> getOrderedPropertiesForName(
			List<String> scientificNames) {
    	List<OrderedPropertyDTO> orderedPropertyList = new ArrayList<OrderedPropertyDTO>();
    	
    	try {
    		SolrDocumentList solrDocumentList = doListQuery(scientificNames, "rdf.hasScientificName", null);
    		logger.info("######### "+solrDocumentList.size()+ " documents returned.");
    		Iterator iter = solrDocumentList.iterator();
    		
    		//iterate through each SOLR document and create an OrderedDocumentDTO 
    		while(iter.hasNext()){
    			
    			SolrDocument solrDocument = (SolrDocument) iter.next();
    			Map<String, Object> fieldMap = solrDocument.getFieldValueMap();
    			
    			Set<String> retrievedKeys = fieldMap.keySet();
    			//FIXME 
				String contentModel = (String) fieldMap.get("rdf.hasModel");
				if(contentModel!=null && contentModel.endsWith("ImageContentModel")){
					//dont add these properties to result set
					continue;
				}
    			
    			//for each key, find the correct category, and create a CategorisedProperties instance
    			for(String propertyName: retrievedKeys){
    				
    			    Category category = Category.getCategoryForProperty(propertyName);
    				if(category!=null){
    					
	    				logger.info("Category:"+category.getName()+",  key: '"+propertyName+"', value:"+fieldMap.get(propertyName));
    					
	    				Object propertyValue =  fieldMap.get(propertyName);
	    				
	    				if(propertyValue instanceof List){
	    					
	    					List<Object> propertyValues = (List) propertyValue;
	    					for(Object singleValue: propertyValues){
		    					OrderedPropertyDTO orderedPropertyDTO = createObjectProperty(
										solrDocument, fieldMap, category, propertyName,
										singleValue);
		    	    			
		    	    			orderedPropertyList.add(orderedPropertyDTO);
	    					}
	    					
	    				} else {

	    					OrderedPropertyDTO orderedPropertyDTO = createObjectProperty(
									solrDocument, fieldMap, category,propertyName,
									propertyValue);
	    	    			
	    	    			orderedPropertyList.add(orderedPropertyDTO);
	    				}
    				} else {
    					logger.info("No category found for key: '"+propertyName+"', value:"+fieldMap.get(propertyName));	
    				}
       			}
    		}
    		
    		//order properties by category and by order internal to category
    		Collections.sort(orderedPropertyList, new Comparator<OrderedPropertyDTO>() {
				@Override
				public int compare(OrderedPropertyDTO o1, OrderedPropertyDTO o2) {
					if(o1.getCategory().equals(o2.getCategory())){
						Category category = o1.getCategory();
						return category.getIndexInCategory(o1.getPropertyName()) - category.getIndexInCategory(o2.getPropertyName());
					} else {
						return o1.getCategory().getRank() - o2.getCategory().getRank();
					}
				}
			});
    	} catch (Exception e){
    		logger.error(e.getMessage(),e);
    	}
		return orderedPropertyList;
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
	private OrderedPropertyDTO createObjectProperty(SolrDocument solrDocument,
			Map<String, Object> fieldMap, Category category, String propertyName,
			Object propertyValue) {
		//create property
		OrderedPropertyDTO orderedPropertyDTO = new OrderedPropertyDTO();
		
		//the ID of the infosource    			
		
		orderedPropertyDTO.setInfoSourceUrl(resolveSingleValue(solrDocument.getFieldValue("dc.source")));    			
		
		//a display name for the infosource    			
		orderedPropertyDTO.setInfoSourceName(resolveSingleValue(fieldMap.get("dc.publisher")));

		//the URL of the web page
		orderedPropertyDTO.setSourceUrl(resolveSingleValue(fieldMap.get("dc.identifier")));
		
		//the title of the web page
		orderedPropertyDTO.setSourceTitle(resolveSingleValue(fieldMap.get("dc.title")));
		
		orderedPropertyDTO.setCategory(category);
		
		orderedPropertyDTO.setPropertyName(propertyName);
		
		//call toString on object
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
    	if(fieldValue instanceof List){
    		List<Object> values = (List)fieldValue;
    		if(!values.isEmpty()){
    			return values.get(0).toString();
    		} else {
    			return null;
    		}
    	}if(fieldValue!=null){
    		return fieldValue.toString();
    	}
    	return null;
	}

	/**
     * @see csiro.diasb.dao.FedoraDAO#getDocumentsForName(java.lang.String)
     */
    @Override
	public List<OrderedDocumentDTO> getOrderedDocumentsForName(List<String> scientificNames) {
    	
    	List<OrderedDocumentDTO> orderedDocumentList = new ArrayList<OrderedDocumentDTO>();
    	try {
    		SolrDocumentList solrDocumentList = doListQuery(scientificNames, "rdf.hasScientificName", null);
    		logger.info("######### "+solrDocumentList.size()+ " documents returned.");
    		Iterator iter = solrDocumentList.iterator();
    		
    		//iterate through each SOLR document and create an OrderedDocumentDTO 
    		while(iter.hasNext()){
    			SolrDocument solrDocument = (SolrDocument) iter.next();
    			
    			Map<String, Object> fieldMap = solrDocument.getFieldValueMap();
    			OrderedDocumentDTO orderedDocument = new OrderedDocumentDTO();
    			Set<String> retrievedKeys = fieldMap.keySet();
    			
    			Map<String, CategorisedProperties> catPropMap = new HashMap<String, CategorisedProperties>();
    			
    			//for each key, find the correct category, and create a CategorisedProperties instance
    			for(String key: retrievedKeys){
    				
    			    Category category = Category.getCategoryForProperty(key);
    				if(category!=null){
	    				logger.info("Category:"+category.getName()+",  key: '"+key+"', value:"+fieldMap.get(key));
	    				
	    				CategorisedProperties categorisedProperties = catPropMap.get(category.getName());
	    				
	    				if(categorisedProperties==null){
	    					categorisedProperties = new CategorisedProperties();
	    					categorisedProperties.setCategory(category);
	    					categorisedProperties.getPropertyMap().put(key, (String) fieldMap.get(key));
	    					catPropMap.put(category.getName(), categorisedProperties);
	    				} else {
	    					categorisedProperties.getPropertyMap().put(key, (String) fieldMap.get(key));
	    				}
    				} else {
    					logger.info("No category found for key: '"+key+"', value:"+fieldMap.get(key));	
    				}
    				
    				if("rdf.hasModel".equals(key)){
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
						return o1.getCategory().getRank()-o2.getCategory().getRank();
					}
				});
    			
    			//order the properties, split by category. order by the order specified in category definition
    			for(CategorisedProperties categorisedProperties: catPropertiesList){
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
    				LinkedHashMap<String, String> orderedProps = new LinkedHashMap<String,String>();
    				for(String orderedKey: orderedKeys){
    					orderedProps.put(orderedKey, propertyMap.get(orderedKey));
    				}
    				
    				//replace existing map with the ordered version
    				categorisedProperties.setPropertyMap(orderedProps);
    			}
    			
    			logger.info("DC Source: "+solrDocument.getFieldValue("dc.source"));    			
    			
    			//the ID of the infosource    			
    			orderedDocument.setInfoSourceUrl((String) fieldMap.get("dc.source"));
    			
    			//a display name for the infosource    			
    			orderedDocument.setInfoSourceName((String) fieldMap.get("dc.publisher"));

    			//the URL of the web page
    			orderedDocument.setSourceUrl((String) fieldMap.get("dc.identifier"));
    			
    			//the title of the web page
    			orderedDocument.setSourceTitle((String) fieldMap.get("dc.title"));
    			
    			orderedDocument.setCategorisedProperties(catPropertiesList);
    			
    			if(orderedDocument.getDocumentType()!=null && !orderedDocument.getDocumentType().endsWith("ImageContentModel")){
    				orderedDocumentList.add(orderedDocument);
    			}
    		}
    	} catch (Exception e){
    		logger.error(e.getMessage(),e);
    	}
		return orderedDocumentList;
	}    
    
    
    static Map sortByKey(Map<String,Object> map) {
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
            sdl = doListQuery(taxonNameIds, "dc.identifier",  "ContentModel:ala.TaxonNameContentModel");
        } catch (SolrServerException ex) {
            logger.warn("Problem communicating with SOLR server." + ex.getMessage(), ex);
            return null;
        }

        for (SolrDocument doc : sdl) {
            String contentModel = (String) doc.getFieldValue("ContentModel");
            
            if (contentModel.equalsIgnoreCase("ala.TaxonNameContentModel")) {
                // Only want to request these values if the returned document is the expected type
                String pid = (String) doc.getFieldValue("PID");
                String title = (String) doc.getFieldValue("dc.title");
                String name = (String) doc.getFieldValue("rdf.hasNameComplete");
                String rank = (String) doc.getFieldValue("Rank");
                String guid = (String) doc.getFieldValue("rdf.hasGuid");
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
        List <ImageDTO> imageDTOs = new ArrayList<ImageDTO>();
        SolrDocumentList sdl = null;
        try {
            sdl = doListQuery(scientificNames, "rdf.hasScientificName", "ContentModel:ala.ImageContentModel");
        } catch (SolrServerException ex) {
            logger.warn("Problem communicating with SOLR server." + ex.getMessage(), ex);
            return null;
        }

        for (SolrDocument doc : sdl) {
            String contentModel = (String) doc.getFieldValue("ContentModel");

            if (contentModel.equalsIgnoreCase("ala.ImageContentModel")) {
                // Only want to request these values if the returned document is the expected type
                ImageDTO image = new ImageDTO();
                image.setPid((String) doc.getFieldValue("PID"));
                image.setGuid((String) doc.getFieldValue("rdf.hasGuid"));
                image.setTitle((String) doc.getFieldValue("dc.title"));
                image.setDescription((String) doc.getFieldValue("dc.description"));
                image.setCountry((String) doc.getFieldValue("Country"));
                image.setRegion((String) doc.getFieldValue("Region"));
                image.setLatitude((String) doc.getFieldValue("rdf.latitude"));
                image.setLongitude((String) doc.getFieldValue("rdf.longitude"));
                image.setPhotoPage((String) doc.getFieldValue("rdf.hasPhotoPage"));
                image.setPhotoSourceUrl((String) doc.getFieldValue("rdf.hasPhotoSourceUrl"));
                image.setScientificName((String) doc.getFieldValue("rdf.hasScientificName"));
                image.setSource((String) doc.getFieldValue("rdf.hasGuid"));
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
    public List<HtmlPageDTO> getHtmlPagesForScientificNames(List<String> scientificNames) {
        List <HtmlPageDTO> htmlPageDTOs = new ArrayList<HtmlPageDTO>();
        SolrDocumentList sdl = null;
        try {
            sdl = doListQuery(scientificNames, "rdf.hasScientificName", "ContentModel:ala.HtmlPageContentModel");
        } catch (SolrServerException ex) {
            logger.warn("Problem communicating with SOLR server." + ex.getMessage(), ex);
            return null;
        }

        for (SolrDocument doc : sdl) {
            String contentModel = (String) doc.getFieldValue("ContentModel");

            if (contentModel.equalsIgnoreCase("ala.HtmlPageContentModel")) {
                // Only want to request these values if the returned document is the expected type
                HtmlPageDTO htmlPage = new HtmlPageDTO();
                htmlPage.setPid(resolveSingleMultiValue(doc,"PID"));
                htmlPage.setGuid(resolveSingleMultiValue(doc,"rdf.hasGuid"));
                htmlPage.setTitle(resolveSingleMultiValue(doc,"dc.title"));
                htmlPage.setGuid(resolveSingleMultiValue(doc,"rdf.hasScientificName"));
                htmlPage.setUrl(resolveSingleMultiValue(doc,"rdf.hasURL"));
                htmlPage.setSource(resolveSingleMultiValue(doc,"fgs.label"));
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

	private String resolveSingleMultiValue(SolrDocument doc, String fieldName) {
		Object guid = doc.getFieldValue("rdf.hasGuid");
		if(guid instanceof String){
			return (String) guid;
		} else if(guid instanceof List){
			List<String> guidList = (List)guid;
			if(guidList.isEmpty()){
				String firstGuid = guidList.get(0);
				return (String) firstGuid;
			}
		}
		return null;
	}

    /**
     * @see csiro.diasb.dao.FedoraDAO#getTaxonConceptForIdentifier(java.lang.String)
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
            if (server == null) this.initSolrServer();
            QueryResponse qr = null;
            qr = server.query(searchQuery); // can throw exception
            SolrDocumentList sdl = qr.getResults();

            if (!sdl.isEmpty()) {
                SolrDocument doc = sdl.get(0); // assume 1 result
                String pid = (String) doc.getFieldValue("PID");
                tc = new TaxonConceptDTO(pid);
                tc.setGuid((String) doc.getFieldValue("rdf.hasGuid"));
                tc.setTitle((String) doc.getFieldValue("dc.title"));
                tc.setScientificName((String) doc.getFieldValue("dc.title")); // ideally should be rdf.hasScientificName
                tc.setRank((String) doc.getFieldValue("rdf.hasRank"));
                tc.setSource((String) doc.getFieldValue("fgs.label"));
                tc.setTaxonNameGuid((String) doc.getFieldValue("rdf.hasTaxonName"));
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
        	//String queryValue = it.next().toString();
            query.append(fieldName);
            query.append(":");
            query.append(safeQueryStr);
            //query.append(queryValue);
            if (it.hasNext()) {
                query.append(" OR ");
            }
        }

        if (contentModelFilter!=null && !contentModelFilter.isEmpty()) {
            // add optional contentModel filter, which requires parentheses around existing query
            // so that we get the expected precedence for Boolean expression
            query.insert(0,"(");
            query.append(") AND " + contentModelFilter);
        }

        SolrQuery searchQuery = new SolrQuery();  // handle better?
        searchQuery.setQuery(query.toString());
        logger.info("SOLR query: " + query.toString());
        // do the Solr search
        if (server == null){
        	this.initSolrServer();
        }
        QueryResponse qr = server.query( searchQuery ); // can throw exception
        
        return qr.getResults();
    }

    /**
     * returns a pointer to the SOLR server singleton stored in fedoraAPI
     * @return
     */
    public CommonsHttpSolrServer initSolrServer() {
        if (this.server == null & solrUrl != null) {
            try {
                this.server = new CommonsHttpSolrServer(solrUrl);
            } catch (MalformedURLException e) {
                logger.error(e.getLocalizedMessage());
            }
        }

        return this.server;
    }
    
    /**
     * @see csiro.diasb.dao.FedoraDAO#getServerUrl()
     *
     * @return
     */
    public String getServerUrl() {
        return solrUrl;
    }
    
    public CommonsHttpSolrServer getServer() {
        return server;
    }

    public void setServer(CommonsHttpSolrServer server) {
        this.server = server;
    }

    public  String getSolrUrl() {
        return solrUrl;
    }

    public  void setSolrUrl(String solrUrl) {
        this.solrUrl = solrUrl;
    }
}
