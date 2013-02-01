/***************************************************************************
 * Copyright (C) 2009 Atlas of Living Australia
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
 ***************************************************************************/
package org.ala.harvester;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.inject.Inject;

import org.ala.client.util.RestfulClient;
import org.ala.dao.InfoSourceDAO;
import org.ala.documentmapper.DocumentMapper;
import org.ala.documentmapper.MappingUtils;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Repository;
import org.ala.repository.Triple;
import org.ala.util.Response;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
/**
 * A Harvester class for Biocache 
 */
@Component("biocacheHarvester")
public class BiocacheHarvester implements Harvester {
    protected Logger logger = Logger.getLogger(BiocacheHarvester.class);

    protected Repository repository;
    protected int timeGap = 0;
    @Inject
    protected InfoSourceDAO infoSourceDAO;

    protected Map<String, String> hashTable;
    protected ObjectMapper mapper;
	//create restful client with no connection timeout.
	protected RestfulClient restfulClient = new RestfulClient(0);
	protected HttpClient httpClient;
	
	public static final String DATA_RESOURCE_URL ="http://biocache.ala.org.au/ws/occurrences/search?q=multimedia:Image&facets=data_resource_uid&pageSize=0";
	public static final String COLLECTORY_URL = "http://collections.ala.org.au/ws/dataResource/";
	public static final String BIOCACHE_SEARCH_URL = "http://biocache.ala.org.au/ws/occurrences/search?q=*:*&fq=multimedia:Image&facet=off&fl=taxon_name,raw_taxon_name,occurrence_id,image_url,id,row_key,data_resource_uid,collector,all_image_url&pageSize=100";
	public static final String BIOCACHE_OCCURRENCE_URL = "http://biocache.ala.org.au/occurrences/";
	public static final String BIOCACHE_MEDIA_URL = "http://biocache.ala.org.au/biocache-media/";
	
	private int ctr = 0;
	private Date startDate = null;
	private Date endDate = null;
	
    public BiocacheHarvester(){
    	int timeout = 3000; //msec
    	
		hashTable = new Hashtable<String, String>();
		hashTable.put("accept", "application/json");

		mapper = new ObjectMapper();
		mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);    	
		
		HttpConnectionManagerParams params;
        MultiThreadedHttpConnectionManager m_connectionmgr = new MultiThreadedHttpConnectionManager();
        params=new HttpConnectionManagerParams();
        params.setConnectionTimeout(timeout);
        params.setSoTimeout(timeout);
        params.setDefaultMaxConnectionsPerHost(100);
        params.setMaxTotalConnections(3000);
        m_connectionmgr.setParams(params);
        httpClient = new HttpClient(m_connectionmgr);		
    }
    
    private DynaBean getJsonDynaBean(String url) throws Exception {
    	DynaBean dynaBean = null;
    	String content = "";
		Object[] resp = restfulClient.restGet(url, hashTable);
		if((Integer)resp[0] == HttpStatus.SC_OK){
			content = resp[1].toString();
			if(content != null && content.length() > "[]".length()){
				dynaBean = mapper.readValue(content, DynaBean.class);
			}
		} 
		else {
			logger.warn("Unable to process url: " + url);
		}
    	return dynaBean;
    }

    private byte[] getContent(String url) throws Exception {
		GetMethod gm = new GetMethod(url);
		httpClient.executeMethod(gm);
		Response r = new Response();
		r.setResponseAsBytes(gm.getResponseBody());
		Header hdr = gm.getResponseHeader("Content-Type");

		if (hdr != null) {
			String contentType = hdr.getValue();
			if(contentType.contains(";")){
				contentType = contentType.substring(0,contentType.indexOf(";"));
				contentType = contentType.trim();
			}
			r.setContentType(contentType);
		}
    	return r.getResponseAsBytes();
    }

    private void processJsonDynaBean(DynaBean dynaBean) throws Exception {
    	List<String> imageUrls = null;
    	String identifier = null;
    	String mediaURL = "/data/biocache-media/";
    	
    	if(dynaBean != null && dynaBean.getOther().get("occurrences") != null){
    		List occurrences = (List)dynaBean.getOther().get("occurrences");
    		for(int i = 0; i < occurrences.size(); i++){
    			try{
    				logger.debug("\n ------------------- \n*** record ctr: " + (ctr++) + "\n -------------------");
    				ParsedDocument imageDoc = new ParsedDocument();

    	            List<Triple<String,String,String>> triples = imageDoc.getTriples();
    	            Map<String, String> dcs = imageDoc.getDublinCore();
    	            
	    			Map<String,Object> occurrence = (Map<String,Object>)occurrences.get(i);
	    			imageUrls = (List<String>)occurrence.get("images");
	    			//load all the images
	    			for(String imageUrl : imageUrls){
    	    			if(imageUrl.length() > 4){
    	    				imageDoc.setContentType(new MimetypesFileTypeMap().getContentType(imageUrl));
        				}	    			
    
    	    			if(imageUrl != null && imageUrl.trim().startsWith(mediaURL)){
    	    				imageUrl = imageUrl.trim().replaceFirst(mediaURL, BIOCACHE_MEDIA_URL);
    	    				String occurrenceID = (String)occurrence.get("occurrenceID");
    	    				if(occurrenceID != null && occurrenceID.trim().startsWith("http://www.flickr.com")){
    	    					identifier = occurrenceID.trim();	    					
    	    				}
    	    				else{
    	    					identifier = imageUrl;
    	    				}	    				
    	    			}
    	    			else{
    	    				identifier = imageUrl;
    	    			}	    			
    	    			logger.debug("imageUrl: " + imageUrl + " ,occurrence: " + occurrence);
    	    			
    	    			imageDoc.setGuid(identifier);
    	    			imageDoc.setContent(getContent(imageUrl));
    	    			
        	            dcs.put(Predicates.DC_TITLE.toString(), (String)occurrence.get("scientificName"));
        	            dcs.put(Predicates.DC_IDENTIFIER.toString(), identifier);
        	            dcs.put(Predicates.DC_LICENSE.toString(), "CC-BY");
        	            dcs.put(Predicates.COUNTRY.toString(), "Australia");
        	            dcs.put(Predicates.DC_IS_PART_OF.toString(), imageUrl);
                        String collector = (String) occurrence.get("collector");
                        if(collector!=null){
                            dcs.put(Predicates.DC_CREATOR.toString(), collector);
                        }
        	                	            
        	            String subject = MappingUtils.getSubject();
        	            String sname = (String)occurrence.get("scientificName");
        	            if(sname == null){
        	            	sname = (String)occurrence.get("raw_scientificName");
        	            }
        	            
        	            triples.add(new Triple<String, String, String>(subject, Predicates.SCIENTIFIC_NAME.toString(), sname));
    	                triples.add(new Triple<String, String, String>(subject, Predicates.IMAGE_URL.toString(), imageUrl));
        	            //bie image link back to biocache occurrence
        	            triples.add(new Triple<String, String, String>(subject, Predicates.OCCURRENCE_UID.toString(), (String)occurrence.get("uuid")));
        	            triples.add(new Triple<String, String, String>(subject, Predicates.OCCURRENCE_ROW_KEY.toString(), (String)occurrence.get("rowKey")));
        	            
        	            String dataResourceUid = (String)occurrence.get("dataResourceUid");
        	            
        	            if (imageDoc != null && imageDoc.getGuid() != null) {
        	                debugParsedDoc(imageDoc);
        	                this.repository.storeDocument(dataResourceUid, imageDoc);
        	            }
	    			}
    			}
    			catch(Exception ex){
    				//do nothing...continue on next
    				logger.error("processJsonDynaBean: " + ex);
    			}    			
    		}
    	}
    }
    
	/**
	 * data load process
	 * @throws Exception 
	 */
	private void load() throws Exception{
    	int ctr = 0;
    	String dateRangeQuery = formatDateRangeQuery();
		logger.debug("Date query: " + dateRangeQuery);
    	DynaBean dynaBean = this.getJsonDynaBean(DATA_RESOURCE_URL + '&' + dateRangeQuery);    	
    	if("OK".equalsIgnoreCase(dynaBean.status)){
    		ctr = dynaBean.totalRecords;
    		List<Map<String,String>> list = ((List<Map<String,String>>)((Map)((List)dynaBean.get("facetResults")).get(0)).get("fieldResult"));
    		addMissingInfosources(list);
	    	for(int i = 0; i <= ctr/100; i++){
	    		try {
	    			String url = BIOCACHE_SEARCH_URL + "&start=" + (i * 100);
		    		if(dateRangeQuery != null){
		    			url = url + "&" + dateRangeQuery;
		    		}
		    		logger.debug("*** loop url: " + url);
		    		dynaBean = this.getJsonDynaBean(url);
		    		processJsonDynaBean(dynaBean);
	    		} catch(Exception ex) {
	    			//log it and continue to next
	    			logger.error(ex.getMessage(), ex);
	    		}
	    	}    	
    	}    	
	}
	
	String formatDateRangeQuery(){
		if(this.endDate != null){
			SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			StringBuffer sb = new StringBuffer("fq=first_loaded_date:%5B");
			sb.append(sfd.format(this.getStartDate()));
			sb.append("%20TO%20");
			sb.append(sfd.format(this.getEndDate()));
			sb.append("%5D");
			return sb.toString();
		} else {
			return null;
		}
	}
	
	/**
	 * Dynamically add the missing infosources so we don't miss harvesting images for new data resources.
	 * @param dataResourceFacet
	 */
	private void addMissingInfosources(List<Map<String,String>> dataResourceFacet) {
	    Map<String,String> uidsToIds = infoSourceDAO.getUidInfosourceIdMap();
	    ObjectMapper mapper = new ObjectMapper();
	    for(Map<String,String> dataResource:dataResourceFacet){
	        String dr = dataResource.get("label");
	        if(!uidsToIds.containsKey(dr)){
	            logger.warn("Dynamically adding an infosource for " + dr);
	            try{
	            Object[] resp = restfulClient.restGet(COLLECTORY_URL+dr);
	            Map map = mapper.readValue(resp[1].toString(),Map.class);
	            Object url = map.get("websiteUrl");
	            if(url == null) 
	                url = map.get("alaPublicUrl");
	            infoSourceDAO.addInfosource(dr, map.get("name").toString(),url.toString());
	            }
	            catch(Exception e){
	                logger.error("Unable to add missing infosource "+dataResource, e);	                
	            }
	        }
	    }
	}
	
    /**
     * @see au.org.ala.bie.harvester.Harvester#setConnectionParams(java.util.Map)
     */
    @Override
    public void setConnectionParams(Map<String, String> connectionParams) {
    }

    @Override
    public void start(int infosourceId, int timeGap) throws Exception {
        this.timeGap = timeGap;
        start(infosourceId);
    }

    /**
     * @see au.org.ala.bie.harvester.Harvester#start()
     */
    @SuppressWarnings("unchecked")
    @Override
    public void start(int infosourceId) throws Exception {
        Thread.sleep(timeGap);
        load();
    }

    public void debugParsedDoc(ParsedDocument parsedDoc){

        logger.debug("===============================================================================");

        logger.debug("GUID: "+parsedDoc.getGuid());
        logger.debug("Content-Type: "+parsedDoc.getContentType());

        Map<String,String> dublinCore = parsedDoc.getDublinCore();
        for(String key: dublinCore.keySet()){
            logger.debug("DC: "+key+"\t"+dublinCore.get(key));
        }

        List<Triple<String,String,String>> triples = parsedDoc.getTriples(); 
        for(Triple<String,String,String> triple: triples){
            logger.debug("RDF: "+triple.getSubject()+"\t"+triple.getPredicate()+"\t"+triple.getObject());
        }

        logger.debug("===============================================================================");
    }

    /**
     * @see
     * au.org.ala.bie.harvester.Harvester#setRepository(au.org.ala.bie.repository.Repository)
     */
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void setDocumentMapper(DocumentMapper documentMapper) {
        // TODO Auto-generated method stub
    }
    
    // ==================<main>============
    /**
     * Main method for testing this particular Harvester
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String[] locations = {"classpath*:spring.xml"};
        ApplicationContext context = new ClassPathXmlApplicationContext(locations);
        BiocacheHarvester h = (BiocacheHarvester) context.getBean(BiocacheHarvester.class);
        Repository r = (Repository) context.getBean("repository"); 
        h.setRepository(r);
        
        if(args.length==1){
        	h.setEndDate(new Date());
        	if("-lastYear".equals(args[0])){
            	h.setStartDate(DateUtils.addYears(h.getEndDate(), -1));                	
        	} else if("-lastMonth".equals(args[0])){
            	h.setStartDate(DateUtils.addMonths(h.getEndDate(), -1));        			
        	} else if("-lastWeek".equals(args[0])){
        		h.setStartDate(DateUtils.addWeeks(h.getEndDate(), -1));        	
        	} else {
        	  //attempt to parse the date down to an acceptable date
        	  try{
        	      Date startDate =DateUtils.parseDate(args[0], new String[]{"yyyy-MM-dd"});
        	      h.setStartDate(startDate);
        	  }
        	  catch(java.text.ParseException e){
        	      h.setStartDate(DateUtils.addDays(h.getEndDate(), -1));
        	  }
        	}
        }
        h.start(-1);
    }	

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}    
}


//================================================
class DynaBean
{
    // Two mandatory properties
    protected String status;
    protected String query;
    protected int startIndex;
	protected int totalRecords;

    // and then "other" stuff:
    protected Map<String,Object> other = new HashMap<String,Object>();

	// Could alternatively add setters, but since these are mandatory
    @JsonCreator
    public DynaBean(@JsonProperty("status") String status, @JsonProperty("query") String query, @JsonProperty("startIndex") int startIndex, @JsonProperty("totalRecords") int totalRecords)
    {
        this.status = status;
        this.query = query;
        this.startIndex = startIndex;
        this.totalRecords = totalRecords;
    }

    public String getStatus() {
		return status;
	}

	public String getQuery() {
		return query;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getTotalRecords() {
		return totalRecords;
	}

    public Object get(String name) {
        return other.get(name);
    }

    public Map<String, Object> getOther() {
		return other;
	}
    
    // "any getter" needed for serialization    
    @JsonAnyGetter
    public Map<String,Object> any() {
        return other;
    }

    @JsonAnySetter
    public void set(String name, Object value) {
        other.put(name, value);
    }
}    

