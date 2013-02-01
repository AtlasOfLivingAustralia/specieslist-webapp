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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.ala.documentmapper.DocumentMapper;
import org.ala.documentmapper.MappingUtils;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Repository;
import org.ala.repository.Triple;
import org.ala.util.Response;
import org.ala.util.WebUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * A Harvester class for Western Australian Insect Study Society 
 * 
 * @author Tommy Wang
 */
@Component("WaissHarvester")
@Scope(BeanDefinition.SCOPE_PROTOTYPE) 
public class WaissHarvester implements Harvester {

    protected Logger logger = Logger.getLogger(MaHarvester.class);

    protected String endpoint;
    //	private String eolGroupId;
    //	private String MorphbankRestBaseUrl;
    //	private String MorphbankApiKey;
    //	private int recordsPerPage;
    protected Repository repository;
    protected int timeGap = 0;
    //	private static final int RESULT_LIMIT = 6544;
    private static final int WAISS_INFOSOURCE_ID = 1110;
    protected String contentType = "image/jpeg";
    protected final String baseUrl = "http://www.museum.wa.gov.au/waiss/pages/image.htm";

    /**
     * Main method for testing this particular Harvester
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String[] locations = {"classpath*:spring.xml"};
        ApplicationContext context = new ClassPathXmlApplicationContext(locations);
        WaissHarvester h = new WaissHarvester();
        Repository r = (Repository) context.getBean("repository"); 
        h.setRepository(r);

        //set the connection params	
        Map<String, String> connectParams = new HashMap<String, String>();
        connectParams.put("endpoint", "http://www.museum.wa.gov.au/waiss/pages/image.htm");

        h.setConnectionParams(connectParams);
        h.start(WAISS_INFOSOURCE_ID); 
    }	

    /**
     * @see org.ala.harvester.Harvester#setConnectionParams(java.util.Map)
     */
    @Override
    public void setConnectionParams(Map<String, String> connectionParams) {
        this.endpoint = connectionParams.get("endpoint");
    }


    @Override
    public void start(int infosourceId, int timeGap) throws Exception {
        this.timeGap = timeGap;
        start(infosourceId);
    }

    /**
     * @see org.ala.harvester.Harvester#start()
     */
    @SuppressWarnings("unchecked")
    @Override
    public void start(int infosourceId) throws Exception {

        // TODO Auto-generated method stub
        Thread.sleep(timeGap);

        // Obtains the image listing on the page number specified.
        // Instance variable `currentResDom` will have new
        // DOM representation of the result.
        String indexStr = getIndexPageStr();
//        String xpathToImageUrls = "//div[@align='center']/a[@target='_blank']/@href";
//        
//        Document document = getDocument(indexStr.getBytes());
//        XPathFactory factory = XPathFactory.newInstance();
//        XPath xpath = factory.newXPath();
//        
//        NodeList nodes = (NodeList) xpath.evaluate(xpathToImageUrls, document, XPathConstants.NODESET);
//        
//        for (int i = 0; i < nodes.getLength(); i++) {
//            String imageUrl = (nodes.item(i)).getNodeValue();
//            imageUrl = StringUtils.trimToNull(imageUrl);
//            
//            if (imageUrl != null && !"".equals(imageUrl)) {
//                imageUrl = "http://www.museum.wa.gov.au/waiss/pages/" + imageUrl;
//                System.out.println(imageUrl);
//            }
//        }
        
        Pattern speciesPattern = Pattern.compile("(?:<a href=\")" +
                "(images/[a-zA-Z0-9\\.]*)" +
                "(?:\" target=\"_blank\"><img src=\")" +
                "(?:\\.\\./a_data/[a-zA-Z0-9_]{1,}\\.jpg)" +
                "(?:\" width=\"[0-9]{1,}\" height=\"[0-9]{1,}\" border=\"[0-9]{1,}\"></a></div></td>[\\s]{0,}<td[ width=\"422]*>[\\s]*[<div align=\"center\">]*<span class=\"style4\"><strong>)" +
                "([a-zA-Z \\-']*)" +
                "(?:</strong>[ ]*\\([ ]*<em>)" +
        "([a-zA-Z ]*)");
        
        Pattern creatorPattern = Pattern.compile("(?:Photo: )" +
        		"([a-zA-Z ]{1,})");

        Matcher m = speciesPattern.matcher(indexStr);

        String[] creatorArray  = new String[9];
        
        Matcher m2 = creatorPattern.matcher(indexStr);
        
        int searchIdx2 = 0;
        int creatorCounter = 0;
        
        while(m2.find(searchIdx2)){
            int endIdx2 = m2.end();
            
            String creator = m2.group(1);
            
            if (creator != null && !"".equals(creator)) {
                creatorArray[creatorCounter] = creator;
                creatorCounter ++;
            }
            
            searchIdx2 = endIdx2;
        }

        int speciesCounter = 0;
        int searchIdx = 0;
        // get all the family links
        while(m.find(searchIdx)){
            int endIdx = m.end();
            //          String found = content.substring(startIdx, endIdx);
            String url = "http://www.museum.wa.gov.au/waiss/pages/" + m.group(1);
            String commonName = m.group(2);
            String sciName = m.group(3);
            //            String generatedUrl = this.endpoint + url;
            //
            System.out.println("URL:" + url);
            System.out.println("Common Name: " + commonName);
            System.out.println("Sci Name:" + sciName);
            //            
            processSingleImage(url.trim(), commonName.trim(), sciName.trim(), infosourceId, creatorArray[speciesCounter]);
            speciesCounter++;
            //
            //            identifiers.add(generatedUrl);

            searchIdx = endIdx;
        }

        
        
        System.out.println(speciesCounter);

    }

    private Document getDocument(byte[] content) throws ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(false);
        
        InputSource is = new InputSource(new StringReader(new String(content)));
        DocumentBuilder parser = dbFactory.newDocumentBuilder();
        Document document = null;
        
        try {
            document = parser.parse(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return document;
    }
    
    @SuppressWarnings("unchecked")
    public void processSingleImage(String url, String commonName, String scientificName, int infosourceId, String creator) throws Exception {
        ParsedDocument imageDoc = new ParsedDocument();

        if (scientificName != null && !"".equals(scientificName)) {
            List<Triple<String,String,String>> triples = imageDoc.getTriples();
            Map<String, String> dcs = imageDoc.getDublinCore();

            String subject = MappingUtils.getSubject();
            Response response = WebUtils.getUrlContentAsBytes(url);

            imageDoc.setGuid(url);
            imageDoc.setContent(response.getResponseAsBytes());
            imageDoc.setContentType(response.getContentType());

            dcs.put(Predicates.DC_TITLE.toString(), scientificName);
            dcs.put(Predicates.DC_IDENTIFIER.toString(), "http://www.museum.wa.gov.au/waiss/pages/image.htm");
            dcs.put(Predicates.DC_CREATOR.toString(), creator);
            dcs.put(Predicates.DC_LICENSE.toString(), "CC BY-NC");
            dcs.put(Predicates.DC_RIGHTS.toString(), "Copyright by " + creator);
            dcs.put(Predicates.COUNTRY.toString(), "Australia");
            dcs.put(Predicates.DC_IS_PART_OF.toString(), "http://www.museum.wa.gov.au/waiss/pages/image.htm");
            
            if (scientificName != null && !"".equals(scientificName)) {
                triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), scientificName));
            }

            if (commonName != null && !"".equals(commonName)) {
                triples.add(new Triple(subject, Predicates.COMMON_NAME.toString(), commonName));
            }
        }

        if (imageDoc != null) {
            debugParsedDoc(imageDoc);
            this.repository.storeDocument(infosourceId, imageDoc);
        }
    }

    private String inputStream2String(InputStream is) throws IOException{
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null){
            buffer.append(line);
        }
        return buffer.toString();
    }

    private String getIndexPageStr() throws Exception {

        // Constructs the GET URL to search.

        // `woe_id` is Yahoo! Where On Earth ID.
        // Issue
        // http://api.Morphbank.com/services/rest/?method=Morphbank.places.find&api_key=08f5318120189e9d12669465c0113351&query=australia
        // to find Australia.
        // `woe_id` here is country level code, as opposed to continent code.

        String urlToSearch = this.endpoint;

        System.out.println("Search URL: "+urlToSearch);

        /*
         * // Default parameters if not supplied. if (this.MorphbankApiKeySupplied
         * == false) { urlToSearch += "&" + "api_key=" + this.MorphbankApiKey; } if
         * (this.currentPageNumSupplied == false) { urlToSearch += "&" + "page="
         * + this.currentPageNum; } if (this.recordsPerPageSupplied == false) {
         * urlToSearch += "&" + "per_page=" + this.recordsPerPage; }
         */
        //		System.out.println(urlToSearch);
        logger.debug("URL to search is: " + "`" + urlToSearch + "`" + "\n");

        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();

        // Create a method instance.
        GetMethod method = new GetMethod(urlToSearch);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"UTF-8");
        method.getParams().setParameter(HttpMethodParams.HTTP_ELEMENT_CHARSET,"UTF-8");
        method.getParams().setParameter(HttpMethodParams.HTTP_URI_CHARSET,"UTF-8");

        try {
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                String errMsg = "HTTP GET to " + "`" + urlToSearch + "`"
                + " returned non HTTP OK code.  Returned code "
                + statusCode + " and message " + method.getStatusLine()
                + "\n";
                method.releaseConnection();
                throw new Exception(errMsg);
            }

            //			InputStream responseStream = method.getResponseBodyAsStream();
            //
            //			// Instantiates a DOM builder to create a DOM of the response.
            //			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //			DocumentBuilder builder = factory.newDocumentBuilder();

            // return a parsed Document
            return method.getResponseBodyAsString();

        } catch (Exception httpErr) {
            String errMsg = "HTTP GET to `" + urlToSearch
            + "` returned HTTP error.";
            throw new Exception(errMsg, httpErr);
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
    } // End of `getIndexPage` method.

    public void debugParsedDoc(ParsedDocument parsedDoc){

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

    /**
     * @see
     * org.ala.harvester.Harvester#setRepository(org.ala.repository.Repository)
     */
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void setDocumentMapper(DocumentMapper documentMapper) {
        // TODO Auto-generated method stub

    }
}
