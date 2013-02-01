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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
import org.ala.util.FileImportUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

/**
 * A Harvester class for Blue Tier 
 * 
 * @author Tommy Wang
 */
@Component("BluetierHarvester")
@Scope(BeanDefinition.SCOPE_PROTOTYPE) 
public class BlueTierHarvester implements Harvester {

    protected Logger logger = Logger.getLogger(BlueTierHarvester.class);

    //    protected String endpoint;
    //	private String eolGroupId;
    //	private String MorphbankRestBaseUrl;
    //	private String MorphbankApiKey;
    //	private int recordsPerPage;
    protected Repository repository;
    protected int timeGap = 0;
    //	private static final int RESULT_LIMIT = 6544;
    private static final int BLUE_TIER_INFOSOURCE_ID = 1084;
    protected String contentType = "text/xml";
    protected final String baseUrl = "http://www.bluetier.org/";
    protected final String[] endpoints = {
            //            "http://www.bluetier.org/images.htm",
            //            "http://www.bluetier.org/nature/ferns.htm",
            "http://www.bluetier.org/nature/fungi.htm"
            //            "http://www.bluetier.org/nature/mammals.htm",
            //            "http://www.bluetier.org/nature/birds.htm",
            //            "http://www.bluetier.org/nature/reptiles.htm"
    };

    protected final String[] exceptions = {
            "a-archeri",
            "g-hypnorum",
            "h-chromolinonae",
            "h-graminicolor",
            "lycoperdon1",
            "m-affixus",
            "m-epipterygia",
            "m-hepatochrous",
            "m-nargan",
            "m-toyer laricola",
            "m-viscidocruenta",
            "o-chromacea",
            "p-atromarginatus",
            "p-squarrosipes",
            "r-butyracea"
    };
    
    /**
     * Main method for testing this particular Harvester
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String[] locations = {"classpath*:spring.xml"};
        ApplicationContext context = new ClassPathXmlApplicationContext(locations);
        BlueTierHarvester h = new BlueTierHarvester();
        Repository r = (Repository) context.getBean("repository"); 
        h.setRepository(r);

        //set the connection params	
        h.start(BLUE_TIER_INFOSOURCE_ID); 
    }	

    /**
     * @see org.ala.harvester.Harvester#setConnectionParams(java.util.Map)
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
        processPages(endpoints, infosourceId);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void processPages(String[] urls, int infosourceId) throws Exception {
        Pattern speciesPattern = Pattern.compile("(?:<a href=\")" +
                "([a-z0-9\\-/\\.]*\\.JPG)" +
                "(?:\">[\\s]*<em>)" +
                "([A-Za-z ]*)" +
        "(?:</em>[\\s]*</a>)");

        Pattern speciesPattern2 = Pattern.compile("(?:<em>[\\s]*<a href=\")" +
                "([a-z0-9\\-/\\.]*\\.JPG)" +
                "(?:\">[\\s]*)" +
                "([A-Za-z ]*)" +
        "(?:[\\s]*</a>[\\s]*</em>)");

        Pattern speciesPattern3 = Pattern.compile("(?:<em>[\\s]*)" +
                "([a-zA-Z ]*)" +
                "(?:</em>[\\s]*\\-[\\s]*<a href=\")" +
                "([a-z0-9\\-/\\.]*\\.JPG)" +
        "(?:\">)");

        List fileList = FileImportUtil.readMetaFile(String.valueOf(BLUE_TIER_INFOSOURCE_ID));
        String[] fileArray = new String[fileList.size()];
        fileList.toArray(fileArray);

        for (String url : urls) {
            Map<String, String> urlNameMap = new HashMap<String, String>();
            String indexStr = getIndexPageStr(url);

            indexStr = indexStr.replaceAll("[\\s]{1,}", " ");

            urlNameMap.putAll(matchStr(speciesPattern, indexStr, 1, 2));
            urlNameMap.putAll(matchStr(speciesPattern2, indexStr, 1, 2));
            urlNameMap.putAll(matchStr(speciesPattern3, indexStr, 2, 1));

            Set<String> keySet = urlNameMap.keySet();

            Iterator<String> iterator = keySet.iterator();

            while (iterator.hasNext()) {
                String imageUrl = (String) iterator.next();
                String sciName = urlNameMap.get(imageUrl);                
                String identifier = url + "#" + sciName.replaceAll(" ", "");
                boolean isexception = false;
                boolean isListed = false;
                
                for (String exception : exceptions) {
                    if (imageUrl.contains(exception)) {
                        isexception = true;
                    }
                }
                
                
                for (String file : fileArray) {
                    System.out.println(file);
                    if (imageUrl.contains(file)) {
                        isListed = true;
                    }
                }
                
                System.out.println("id: " + imageUrl + ", isException: " + isexception + ", isListed: " + isListed);
                
                if (!isexception && isListed) {
                    storeImageDoc(identifier, imageUrl, sciName, infosourceId);
                }
            }


        }
    }

    private Map<String, String> matchStr(Pattern p, String str, int imageUrlPosition, int sciNamePosition) {
        Matcher m = p.matcher(str);
        Map<String, String> urlNameMap = new HashMap<String, String>();

        int searchIdx = 0;

        while(m.find(searchIdx)){
            int endIdx = m.end();
            //          String found = content.substring(startIdx, endIdx);
            String imageUrl = m.group(imageUrlPosition);
            String sciName = m.group(sciNamePosition);

            imageUrl = imageUrl.replaceFirst("\\.\\./", "");

            String generatedUrl = baseUrl + imageUrl;

//            System.out.println("URL:" + generatedUrl);
//            System.out.println("Name:" + sciName);

            //            storeImageDoc(generatedUrl, sciName);

            urlNameMap.put(generatedUrl, sciName);

            searchIdx = endIdx;
        }

        return urlNameMap;
    }

    private void storeImageDoc(String identifier, String imageUrl, String sciName, int infosourceId) throws Exception {
        ParsedDocument pd = new ParsedDocument();
        ParsedDocument imageDoc = null;

        if (sciName != null && !"".equals(sciName)) {
            List<Triple<String,String,String>> triples = pd.getTriples();
            Map<String, String> dcs = pd.getDublinCore();

            String subject = MappingUtils.getSubject();

            pd.setGuid(identifier);
            pd.setContent(getContent(identifier));
            pd.setContentType(contentType);

            dcs.put(Predicates.DC_TITLE.toString(), sciName);
            dcs.put(Predicates.DC_IDENTIFIER.toString(), identifier);
            dcs.put(Predicates.DC_LICENSE.toString(), "CC-BY");
            dcs.put(Predicates.COUNTRY.toString(), "Australia");

            if (sciName != null && !"".equals(sciName)) {
                triples.add(new Triple<String, String, String>(subject, Predicates.SCIENTIFIC_NAME.toString(), sciName));
            }
            if (imageUrl != null && !"".equals(imageUrl)) {
                triples.add(new Triple<String, String, String>(subject, Predicates.IMAGE_URL.toString(), imageUrl));
                imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
                imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), "Photo: T. Thekathyil, bluetier.org");
                imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), "T. Thekathyil");
            }
        }

        if (pd != null && pd.getGuid() != null) {
            debugParsedDoc(pd);
            this.repository.storeDocument(infosourceId, pd);
        }
        if (imageDoc != null) {
            debugParsedDoc(imageDoc);
            this.repository.storeDocument(infosourceId, imageDoc);
        }
    }

    private byte[] getContent(String url) throws Exception {
        String contentStr = null;

        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();
        // Create a method instance.
        GetMethod method = new GetMethod(url);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"UTF-8");
        method.getParams().setParameter(HttpMethodParams.HTTP_ELEMENT_CHARSET,"UTF-8");
        method.getParams().setParameter(HttpMethodParams.HTTP_URI_CHARSET,"UTF-8");

        try {
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                String errMsg = "HTTP GET to " + "`" + url + "`"
                + " returned non HTTP OK code.  " + "Returned code "
                + statusCode + " and message " + method.getStatusLine()
                + "\n";
                method.releaseConnection();
                logger.error(errMsg);
                throw new Exception(errMsg);
            }

            InputStream responseStream = method.getResponseBodyAsStream();

            contentStr = inputStream2String(responseStream);

        } catch (Exception domCreationErr) {
            throw new Exception(
                    domCreationErr);

        } finally {
            // Release the connection.
            method.releaseConnection();
        }

        return contentStr.getBytes();
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

    private String getIndexPageStr(String url) throws Exception {

        // Constructs the GET URL to search.

        // `woe_id` is Yahoo! Where On Earth ID.
        // Issue
        // http://api.Morphbank.com/services/rest/?method=Morphbank.places.find&api_key=08f5318120189e9d12669465c0113351&query=australia
        // to find Australia.
        // `woe_id` here is country level code, as opposed to continent code.

        String urlToSearch = url;

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
