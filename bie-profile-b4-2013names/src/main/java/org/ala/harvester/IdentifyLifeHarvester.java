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

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.ala.client.util.RestfulClient;
import org.ala.documentmapper.DocumentMapper;
import org.ala.documentmapper.IdentifyLifeDocumentMapper;
import org.ala.model.IdentifyLifeVO;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Repository;
import org.ala.util.MimeType;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * A Harvester class for IdentifyLife
 * 
 * @author MOK011
 *
 */

@Component("identifyLifeHarvester")
@Scope(BeanDefinition.SCOPE_PROTOTYPE) 
public class IdentifyLifeHarvester implements Harvester{
	protected Logger logger = Logger.getLogger(this.getClass());
	@Inject
    protected Repository repository;
	//create restful client with no connection timeout.
	protected RestfulClient restfulClient = new RestfulClient(0);
	
    protected DocumentMapper documentMapper;
    protected Map<String, String> connectionParams;
    protected String defaultMimeType = MimeType.HTML.getMimeType();
    protected int timeGap = 0;
	    
    public static final String SEARCH_END_POINT_ATTR_NAME = "searchEndpoint";
    public static final String KEY_END_POINT_ATTR_NAME = "keyEndpoint";
    public static final String KEY_FIELD_ATTR_NAME = "keyField";
    public static final String SEARCH_FIELD_TAXO_SCOPE = "taxonomicscope";
    public static final String SEARCH_FIELD_GEOG_SCOPE = "GeographicScope";       
    public static final String OUTPUT_FILENAME = "/data/bie-staging/identifylife/data.csv";
    
    public static enum IDLIFE_IDX {ID, TITLE, URL, DESCRIPTION, PUBLISHER, PUBLISHYEAR, TAXONOMICSCOPE,
		GEOGRAPHICSCOPE, KEYTYPE, ACCESSIBILITY, VOCABULARY, TECHNICALSKILLS, IMAGERY}    
	/**
	 * Harvest a document and update the repository.
	 * 
	 * @param infosourceId
	 * @param url
	 * @throws Exception
	 */
	private void harvestDoc(int infosourceId, String url) throws Exception {		
		byte[] content = null;
		
		System.out.println("******** request: " + url);
		Object[] resp = restfulClient.restGet(url);
		if((Integer)resp[0] == HttpStatus.SC_OK){
			content = resp[1].toString().getBytes("UTF-8");
		}
				
		if(content!= null){
			List<String> ids = new ArrayList<String>();
			String keyUrl = connectionParams.get(KEY_END_POINT_ATTR_NAME);
			
			// Instantiates a DOM builder to create a DOM of the response.
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			// return a parsed Document
			Document doc =  builder.parse(new ByteArrayInputStream(content));

			XPathFactory xfactory = XPathFactory.newInstance();
			XPath xpath = xfactory.newXPath();

			XPathExpression xe = xpath.compile("//keys/key[@id]");
			NodeList nodeSet = (NodeList) xe.evaluate(doc, XPathConstants.NODESET);
			for(int i=0; i<nodeSet.getLength();i++){
				NamedNodeMap map = nodeSet.item(i).getAttributes();
				ids.add(map.getNamedItem("id").getNodeValue());
			}
			
			for(int i = 0; i < ids.size(); i++){
				Object[] res = restfulClient.restGet(keyUrl + "/" + ids.get(i));
				if((Integer)res[0] == HttpStatus.SC_OK){
					//map the document 
					List<ParsedDocument> pds = documentMapper.map(keyUrl + ids.get(i), res[1].toString().getBytes());
					
					//store the results
					for(ParsedDocument pd: pds){
						//debugParsedDoc(pd);
						repository.storeDocument(infosourceId, pd);
						logger.debug("Parent guid for stored doc: " + pd.getParentGuid());
					}
				}
			}
		} 
		else {
			logger.warn("Unable to process url: " + url);
		}
	}
    
	/**
	 * Harvest a document and update the repository.
	 * 
	 * @param infosourceId
	 * @param url
	 * @throws Exception
	 */
	private void harvestJsonDoc(int infosourceId, String url, CSVWriter writer) throws Exception {		
		String content = "";
		ObjectMapper mapper = new ObjectMapper();
		mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		Map<String, String> hashTable = new Hashtable<String, String>();
		hashTable.put("accept", "application/json");
		
		System.out.println("******** request: " + url);
		Object[] resp = restfulClient.restGet(url, hashTable);
		if((Integer)resp[0] == HttpStatus.SC_OK){
			content = resp[1].toString();
			if(content != null && content.length() > "[]".length()){
				List<IdentifyLifeVO> objectList = mapper.readValue(content, TypeFactory.collectionType(ArrayList.class, IdentifyLifeVO.class));
				for(IdentifyLifeVO vo : objectList){
					csvWriteLine(writer, vo);
				}
			}
		} 
		else {
			logger.warn("Unable to process url: " + url);
		}
	}
	
	private void csvWriteLine(CSVWriter writer, IdentifyLifeVO vo) {
		String[] outLine = new String[13];

		if (writer != null && vo != null) {
			outLine[IDLIFE_IDX.ID.ordinal()] = vo.getId();
			outLine[IDLIFE_IDX.TITLE.ordinal()] = vo.getTitle();
			outLine[IDLIFE_IDX.URL.ordinal()] = vo.getUrl();
			outLine[IDLIFE_IDX.DESCRIPTION.ordinal()] = vo.getDescription();
			outLine[IDLIFE_IDX.PUBLISHER.ordinal()] = vo.getPublisher();
			outLine[IDLIFE_IDX.PUBLISHYEAR.ordinal()] = "" + vo.getPublishedyear();
			outLine[IDLIFE_IDX.TAXONOMICSCOPE.ordinal()] = vo.getTaxonomicscope();
			outLine[IDLIFE_IDX.GEOGRAPHICSCOPE.ordinal()] = vo.getGeographicscope();
			outLine[IDLIFE_IDX.KEYTYPE.ordinal()] = vo.getKeytype();
			outLine[IDLIFE_IDX.ACCESSIBILITY.ordinal()] = vo.getAccessibility();
			outLine[IDLIFE_IDX.VOCABULARY.ordinal()] = vo.getVocabulary();
			outLine[IDLIFE_IDX.TECHNICALSKILLS.ordinal()] = vo.getTechnicalskills();
			outLine[IDLIFE_IDX.IMAGERY.ordinal()] = vo.getImagery();

			writer.writeNext(outLine);
		}
	}
	
	
    @Override
	public void start(int infosourceId) throws Exception {
		CSVWriter writer = new CSVWriter(new FileWriter(OUTPUT_FILENAME), ',');

    	String url = connectionParams.get(SEARCH_END_POINT_ATTR_NAME) + "?" + connectionParams.get(KEY_FIELD_ATTR_NAME) + "=";
		// build request parameter from AA% to ZZ% 
    	// Taxonomic & Geographic Scope are both wildcard capable searches using '%' wildcard character 
    	for(char i = 65; i < (65 + 26); i++){
    		for(char j = 65; j < (65 + 26); j++){
//				harvestDoc(infosourceId, url + i + j + "%");
    			harvestJsonDoc(infosourceId, url + i + j + "%", writer);
				Thread.sleep(timeGap);
    		}
		}
    	writer.close();
	}

	@Override
	public void start(int infosourceId, int timeGap) throws Exception {
		this.timeGap = timeGap;
		start(infosourceId);		
	}

	@Override
	public void setConnectionParams(Map<String, String> connectionParams) {
		this.connectionParams = connectionParams;	
	}

	@Override
	public void setDocumentMapper(DocumentMapper documentMapper) {
		this.documentMapper =  documentMapper;	
	}

	@Override
	public void setRepository(Repository repository) {
		this.repository = repository;
	}
	
	//==================<Main>=========================
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if(args.length != 1){
			System.out.print("Usage: ");
			System.out.print("<infosourceId>");
			System.out.println();
			System.out.println("infosourceId e.g. 1036");			
			System.exit(1);
		}		
		
		String[] locations = {"classpath*:spring.xml"};
		ApplicationContext context = new ClassPathXmlApplicationContext(locations);
		IdentifyLifeHarvester identifyLifeHarvester = context.getBean(IdentifyLifeHarvester.class);
		HashMap<String, String> connectParams = new HashMap<String, String>();
        connectParams.put(SEARCH_END_POINT_ATTR_NAME, "http://www.identifylife.org:8001/Keys/Search");
        connectParams.put(KEY_END_POINT_ATTR_NAME, "http://www.identifylife.org:8001/Keys");
        connectParams.put(KEY_FIELD_ATTR_NAME, SEARCH_FIELD_TAXO_SCOPE);
        identifyLifeHarvester.setConnectionParams(connectParams);
        identifyLifeHarvester.setDocumentMapper(new IdentifyLifeDocumentMapper());
        identifyLifeHarvester.start(Integer.parseInt(args[0]), 500);
		System.exit(0);
	}	
}

//================================================


