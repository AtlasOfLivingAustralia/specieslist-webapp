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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ala.documentmapper.DocumentMapper;
import org.ala.model.Document;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Repository;
import org.ala.repository.Triple;
import org.ala.util.MimeType;
import org.ala.util.Response;
import org.ala.util.WebUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Comma Separated Values (CSV) file format Harvester implementation
 *
 * FIXME We should be copying the additional properties across from the sitemap
 *
 * @author Dave Martin (David.Martin@csiro.au)
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
@Component("csvHarvester")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CSVHarvester implements Harvester {

	protected Logger logger = Logger.getLogger(CSVHarvester.class);
    protected Repository repository;
    protected DocumentMapper documentMapper;
    protected Map<String, String> connectionParams;
    protected String defaultMimeType = MimeType.HTML.getMimeType();
    protected int timeGap = 0;
    
    /**
     * Contructor to set repository
     *
     * @param repository
     */
    @Inject
    public CSVHarvester(Repository repository) {
        this.repository = repository;
    }

    /**
     * Default no-args contructor
     */
    public CSVHarvester() {}
    
    /**
     * Set a time gap between two look-ups
     */
    
    public void start(int infosourceId, int timeGap) throws Exception {
    	this.timeGap = timeGap;
    	start(infosourceId);
    }
    
    /**
     * Looks up the details of an infosource, retrieving the details of a 
     * sitemap to use to crawl an external website. Also makes use of
     * a mime type to help parse the document.
     * 
     * @see org.ala.harvester.Harvester#start(int)
     */
	@Override
	public void start(int infosourceId) throws Exception {
		
		int urlsProcessed = 0;
		int urlsHarvested = 0;
		int urlsSkipped = 0;
        try {
        	String siteMapUrl = null;
        	String mimeType = null;
        	
            if (connectionParams != null && connectionParams.containsKey("sitemap")) {
                siteMapUrl = connectionParams.get("sitemap");
                mimeType = connectionParams.get("mimeType");
                logger.info("Site map: "+siteMapUrl);
            }
            
            if(mimeType==null){
            	mimeType = defaultMimeType;
            }
            logger.info("Using mime type: "+mimeType);
            
            InputStream csvIS = null;
            // Read the sitemap file via HTTP get
            if(siteMapUrl.startsWith("http")){
	            HttpClient httpClient = new HttpClient();
	            GetMethod getMethod = new GetMethod(siteMapUrl);
	            httpClient.executeMethod(getMethod);
	            csvIS = getMethod.getResponseBodyAsStream();
	        } else {
	        	//read from file system
	        	csvIS = new FileInputStream(siteMapUrl);
	        }
            
            Reader reader = new InputStreamReader(csvIS, "UTF-8");
            
			CSVReader r = new CSVReader(reader,'\t','"');
			String[] fields = r.readNext();
			int idx = getIdxForField(fields, "URI");
			
			if(idx<0){
				logger.error("Unable to locate URI in site map.");
				System.exit(1);
			}
			
			while((fields = r.readNext())!=null){
				
				// allow a gap between requests. This will stop us bombarding smaller sites.
				Thread.sleep(timeGap);
				
				String url = fields[idx];
				
                try {
                	if(System.getProperty("skip.harvested")!=null){
                		//only harvest if not already in the system
                		Document doc = repository.getDocumentByGuid(url);
                		if(doc==null){
                			logger.debug("Harvesting ("+url+") ");
    	                	urlsHarvested++;
    						harvestDoc(infosourceId,url, mimeType);
                		} else {
                			logger.debug("Skipping ("+url+") ");
                			urlsSkipped++;
                		}
                	} else {
                		logger.debug("Harvesting URL: "+url);
	                	urlsHarvested++;
						harvestDoc(infosourceId,url, mimeType);
                	}
					urlsProcessed++;
                } catch (Exception ex) {
                    // Badly formed HTML documents will be caught here, skip to next URL
                    logger.error("Error parsing document ("+url+"): "+ex.getMessage(), ex);
                }
			}
		} catch (Exception e) {
			logger.error("start() error: "+e.getMessage(), e);
            e.printStackTrace();
        }
		logger.info("CSV Harvester completed for infosource: "+infosourceId
				+". Request made: "+urlsHarvested
				+", Successfully processed: "+urlsProcessed
				+", Urls skipped: "+urlsSkipped);
	}

	/**
	 * Harvest a document and update the repository.
	 * 
	 * @param infosourceId
	 * @param url
	 * @param mimeType
	 * @throws Exception
	 */
	private void harvestDoc(int infosourceId, String url, String mimeType) throws Exception {
		
		byte[] content = null;
		if (mimeType.equals(MimeType.PDF.getMimeType()) || mimeType.equals(MimeType.XML.getMimeType()) || infosourceId == 1051) {
			//get the PDF & XML as bytes
			 Response response = WebUtils.getUrlContentAsBytes(url);
			 content = response.getResponseAsBytes();
		} else if (infosourceId == 1095){
            String contentAsString = WebUtils.getHTMLPageAsXML(url, true);
            if(contentAsString!=null){
                content = contentAsString.getBytes();
            }
        } else{
			String contentAsString = WebUtils.getHTMLPageAsXML(url);
			if(contentAsString!=null){
				content = contentAsString.getBytes();
			}
		}
//		logger.info("harvesting");
		if(content!=null){
			//map the document 
			List<ParsedDocument> pds = documentMapper.map(url, content);
//			for(ParsedDocument pd: pds){
//			    debugParsedDoc(pd);
//			}
			Collections.sort(pds, new DocumentComparator());
			
			//store the results
			for(ParsedDocument pd: pds){
//				debugParsedDoc(pd);
				repository.storeDocument(infosourceId, pd);
				logger.debug("Parent guid for stored doc: " + pd.getParentGuid());
			}
		} else {
			logger.warn("Unable to process url: "+url);
		}
	}
	
	/**
	 * @param fields
	 * @param string
	 * @return
	 */
	private int getIdxForField(String[] fields, String string) {
		int i=0;
		for(String field: fields){
			if("URI".equalsIgnoreCase(field))
				return i;
			i++;
		}
		return -1;
	}

    /**
     * Main method for testing this particular Harvester
     *
     * @param args
     */
	public static void main(String[] args) throws Exception {
		
		if(args.length!=3){
			printUsage();
			System.exit(1);
		}
		
		String infosourceId = args[0];
		String siteMap = args[1];
		String documentMapperClass = args[2];
		
        String[] locations = {"classpath*:spring.xml"};
        ApplicationContext context = new ClassPathXmlApplicationContext(locations);
		CSVHarvester h = (CSVHarvester) context.getBean("csvHarvester"); 
        h.setDocumentMapper((DocumentMapper) Class.forName(documentMapperClass).newInstance());
        HashMap<String, String> connectParams = new HashMap<String, String>();
        connectParams.put("sitemap", siteMap);
        h.setConnectionParams(connectParams);
		h.start(Integer.parseInt(infosourceId));
	}

	private static void printUsage() {
		System.out.print("Usage: ");
		System.out.print("<infosourceId> <path-to-sitemap> <documentMapperClass>");
		System.out.println();
		System.out.println("infosourceId e.g. 1036");
		System.out.println("path-to-sitemap e.g. /data/bie/1036/siteMap.txt");
		System.out.println("documentMapperClass e.g. org.ala.documentmapper.WikipediaDocumentMapper");
	}

    @Override
    public void setConnectionParams(Map<String, String> connectionParams) {
        this.connectionParams = connectionParams;
    }

    @Override
    public void setDocumentMapper(DocumentMapper documentMapper) {
        this.documentMapper = documentMapper;
    }

    @Override
    public void setRepository(Repository repository) {
        this.repository = repository;
    }
    
    /**
     * Sort the 
     *
     */
    
    private class DocumentComparator implements Comparator {
    	public int compare(Object op1, Object op2) {
    		ParsedDocument pd1 = (ParsedDocument) op1;
    		ParsedDocument pd2 = (ParsedDocument) op2;
    		int rank1, rank2;    		
    		
    		if (pd1.getParentGuid()!=null && !"".equals(pd1.getParentGuid())) {
    			rank1 = 2;
    		} else {
    			rank1 = 1;
    		}
    		
    		if (pd2.getParentGuid()!=null && !"".equals(pd2.getParentGuid())) {
    			rank2 = 2;
    		} else {
    			rank2 = 1;
    		}
//    		System.out.println(rank1-rank2);
    		return rank1-rank2;
    	}
    }
    
    private void debugParsedDoc(ParsedDocument parsedDoc){

		System.out.println("===============================================================================");

		System.out.println("GUID: "+parsedDoc.getGuid());
		System.out.println("Content-Type: "+parsedDoc.getContentType());

		Map<String,String> dublinCore = parsedDoc.getDublinCore();
		for(String key: dublinCore.keySet()){
			System.out.println("DC: "+key+"\t"+dublinCore.get(key));
		}

		List<Triple<String,String,String>> triples = parsedDoc.getTriples(); 
		for(Triple<String,String,String> triple: triples){
			System.out.println("RDF: "+triple.getSubject()+"\t"+triple.getPredicate()+"\t"+triple.getObject());
		}

		System.out.println("===============================================================================");
	}
}
