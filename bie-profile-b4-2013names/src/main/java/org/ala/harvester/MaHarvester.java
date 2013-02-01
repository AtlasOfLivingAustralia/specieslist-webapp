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
import java.util.List;
import java.util.Map;
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
 * A Harvester class for Mosquitoes of Australia. 
 * 
 * @author Tommy Wang
 */
@Component("MaHarvester")
@Scope(BeanDefinition.SCOPE_PROTOTYPE) 
public class MaHarvester implements Harvester {

    protected Logger logger = Logger.getLogger(MaHarvester.class);

    protected String endpoint;
    //	private String eolGroupId;
    //	private String MorphbankRestBaseUrl;
    //	private String MorphbankApiKey;
    //	private int recordsPerPage;
    protected Repository repository;
    protected int timeGap = 0;
    //	private static final int RESULT_LIMIT = 6544;
    private static final int MA_INFOSOURCE_ID = 1066;
    protected String contentType = "text/xml";
    protected final String baseUrl = "http://medent.usyd.edu.au/photos/";

    /**
     * Main method for testing this particular Harvester
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String[] locations = {"classpath*:spring.xml"};
        ApplicationContext context = new ClassPathXmlApplicationContext(locations);
        MaHarvester h = new MaHarvester();
        Repository r = (Repository) context.getBean("repository"); 
        h.setRepository(r);

        //set the connection params	
        Map<String, String> connectParams = new HashMap<String, String>();
        connectParams.put("endpoint", "http://medent.usyd.edu.au/photos/mosquitoesofaustralia.htm");

        h.setConnectionParams(connectParams);
        h.start(MA_INFOSOURCE_ID); 
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

        Pattern identifierPattern = Pattern.compile("(?:<a href=\")" +
                "([a-z#]{1,})" +
        "(?:\">)");

        Matcher m = identifierPattern.matcher(indexStr);

        List <String> identifiers = new ArrayList<String>();

        int speciesCounter = 0;
        int searchIdx = 0;
        // get all the family links
        while(m.find(searchIdx)){
            int endIdx = m.end();
            //          String found = content.substring(startIdx, endIdx);
            String url = m.group(1);
            String generatedUrl = this.endpoint + url;

            System.out.println("URL:" + generatedUrl);
            
            speciesCounter++;

            identifiers.add(generatedUrl);

            searchIdx = endIdx;
        }

        for (String identifier : identifiers) {
            processSingleImage(identifier, indexStr, infosourceId);
        }
        
        System.out.println(speciesCounter);

    }

    @SuppressWarnings("unchecked")
    public void processSingleImage(String identifier, String pageStr, int infosourceId) throws Exception {
        String[] bodyContentStrs = pageStr.split("<strong>The Mosquitoes</strong>");

        String anchor = identifier.split("#")[1];

//        System.out.println("anchor:" + anchor);

        Pattern distributionPattern = Pattern.compile("(?:\\()" +
                "([a-zA-Z \\,]{1,})" +
                "(?:\\))");

        Pattern imagePattern = Pattern.compile("(?:\\(<a href=\")" +
                "(mosqphotos/[a-zA-Z_]{1,}\\.jpg)" +
                "(?:\">Photo)");

        if (bodyContentStrs.length == 2) {
            pageStr = bodyContentStrs[1];

            //            String[] speciesStrs = pageStr.split("<a name=\"[a-z]{1,}\">");
            String[] speciesStrs = pageStr.split("<p>");
            int sciNameCounter = 0;

            if (speciesStrs.length > 1) {
                for (String speciesStr : speciesStrs) {
                    if (!speciesStr.contains("</body>") && speciesStr.contains("<a name=\"" + anchor + "\">")) {
                        //                        System.out.println(speciesStr);

                        String[] components = speciesStr.split("<[/a-z]{1,}>");

                        String sciName = null;
                        String description = null;
                        String photoUrl = null;
                        String distribution = null;

                        boolean isSciNameSet = false;
                        boolean isPhotoUrlSet = false;

                        ParsedDocument pd = new ParsedDocument();
                        ParsedDocument imageDoc = null;
                        
                        Matcher matcher = null;

                        if (components.length > 1) {
                            for (int i = 0; i < components.length; i++) {
                                if (!isSciNameSet && components[i] != null && components[i].length() > 0 && !components[i].contains("<")) {
                                    sciName = components[i];
                                    sciNameCounter ++;
                                    //                                    System.out.println("Scientific Name: " + sciName);
                                    isSciNameSet = true;
                                }
                                
                                components[i] = components[i].replaceAll("[\\s]{1,}", " ");
                                
                                System.out.println(components[i]);
                                if (isSciNameSet && components[i] != null && components[i].startsWith(":")) {
                                    

                                    matcher = distributionPattern.matcher(components[i]);

                                    if (matcher.find(0)) {
                                        distribution = matcher.group(1);
                                        components[i] = components[i].replaceFirst("\\([a-zA-Z \\,]{1,}\\)", "");

                                        //                                        System.out.println("Distribution: " + distribution);
                                    }

                                    matcher = imagePattern.matcher(components[i]);

                                    if (matcher.find(0)) {
                                        photoUrl = matcher.group(1);
                                        components[i] = components[i].replaceFirst("\\(<a href=\"mosqphotos/[a-zA-Z_]{1,}\\.jpg\">Photo", "");

                                        photoUrl = baseUrl + photoUrl;
                                        
                                        isPhotoUrlSet = true;

                                        //                                        System.out.println("Photo URL: " + photoUrl);
                                    }

                                    if (!components[i].contains("<") || !(components[i].contains("similar to") || components[i].contains("see &quot;"))) {
                                        description = components[i];

                                        description = description.replaceFirst(":", "").trim();
//                                        description = description.split("<")[0];
                                        if (description.lastIndexOf(".") > 0) {
                                            description = description.substring(0, description.lastIndexOf("."));
                                        }
                                        //                                        System.out.println("Description: " + description);
                                    }
                                }
                                
                                if (isSciNameSet && !isPhotoUrlSet && components[i] != null && components[i].contains("jpg")) {
                                    matcher = imagePattern.matcher(components[i]);
                                    
                                    if (matcher.find(0)) {
                                        photoUrl = matcher.group(1);
                                        photoUrl = baseUrl + photoUrl;
                                        
                                        isPhotoUrlSet = true;
                                    }
                                }
                            }
                        }

                        if (sciName != null && !"".equals(sciName)) {
                            List<Triple<String,String,String>> triples = pd.getTriples();
                            Map<String, String> dcs = pd.getDublinCore();

                            String subject = MappingUtils.getSubject();

                            pd.setGuid(identifier);
                            pd.setContent(getContent(identifier));
                            pd.setContentType(contentType);

                            dcs.put(Predicates.DC_TITLE.toString(), sciName);
                            dcs.put(Predicates.DC_IDENTIFIER.toString(), identifier);
                            dcs.put(Predicates.COUNTRY.toString(), "Australia");

                            if (sciName != null && !"".equals(sciName)) {
                                triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), sciName));
                            }
                            if (description != null && !"".equals(description)) {
                                triples.add(new Triple(subject, Predicates.DESCRIPTIVE_TEXT.toString(), description));
                            }
                            if (distribution != null && !"".equals(distribution)) {
                                triples.add(new Triple(subject, Predicates.DISTRIBUTION_TEXT.toString(), distribution));
                            }
                            if (photoUrl != null && !"".equals(photoUrl)) {
                                triples.add(new Triple(subject, Predicates.IMAGE_URL.toString(), photoUrl));
                                imageDoc = MappingUtils.retrieveImageDocument(pd,photoUrl);
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
                }
            }
            
//            System.out.println(sciNameCounter);
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
