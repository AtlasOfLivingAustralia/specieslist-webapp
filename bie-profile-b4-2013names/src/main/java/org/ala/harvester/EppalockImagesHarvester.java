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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
import org.ala.util.MimeType;
import org.ala.util.Response;
import org.ala.util.WebUtils;
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

import au.com.bytecode.opencsv.CSVReader;

/**
 * A Harvester class for EppalockImages
 * 
 * @author Tommy Wang
 */
@Component("EppalockImagesHarvester")
@Scope(BeanDefinition.SCOPE_PROTOTYPE) 
public class EppalockImagesHarvester implements Harvester {

    protected Logger logger = Logger.getLogger(EppalockImagesHarvester.class);

    //    protected String endpoint;
    //	private String eolGroupId;
    //	private String MorphbankRestBaseUrl;
    //	private String MorphbankApiKey;
    //	private int recordsPerPage;
    protected Repository repository;
    protected int timeGap = 0;
    //	private static final int RESULT_LIMIT = 6544;
    private static final int EPPALOCK_INFOSOURCE_ID = 1096;
    protected String contentType = "text/xml";
    private static final String BASE_IMAGE_URL = "http://www2.ala.org.au/datasets/EppalockImages/";
    private static final String IMAGE_APPENDIX = ".JPG";
    private static final String IMAGE_SPREAD_SHEET_PATH = "/data/EppalockImages.csv";
    //    protected final String baseUrl = "http://www.bluetier.org/";
    //    protected final String[] endpoints = {
    //            //            "http://www.bluetier.org/images.htm",
    //            //            "http://www.bluetier.org/nature/ferns.htm",
    //            "http://www.bluetier.org/nature/fungi.htm"
    //            //            "http://www.bluetier.org/nature/mammals.htm",
    //            //            "http://www.bluetier.org/nature/birds.htm",
    //            //            "http://www.bluetier.org/nature/reptiles.htm"
    //    };
    //
    //    protected final String[] exceptions = {
    //            "a-archeri",
    //            "g-hypnorum",
    //            "h-chromolinonae",
    //            "h-graminicolor",
    //            "lycoperdon1",
    //            "m-affixus",
    //            "m-epipterygia",
    //            "m-hepatochrous",
    //            "m-nargan",
    //            "m-toyer laricola",
    //            "m-viscidocruenta",
    //            "o-chromacea",
    //            "p-atromarginatus",
    //            "p-squarrosipes",
    //            "r-butyracea"
    //    };

    /**
     * Main method for testing this particular Harvester
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String[] locations = {"classpath*:spring.xml"};
        ApplicationContext context = new ClassPathXmlApplicationContext(locations);
        EppalockImagesHarvester h = new EppalockImagesHarvester();
        Repository r = (Repository) context.getBean("repository"); 
        h.setRepository(r);

        //set the connection params	
        h.start(EPPALOCK_INFOSOURCE_ID); 
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
        Map<String, String> scientificNameImageFileUrlMap = getScientificNameImageFileUrlMap(IMAGE_SPREAD_SHEET_PATH);

        processImages(scientificNameImageFileUrlMap, infosourceId);
    }

    private Map<String, String> getScientificNameImageFileUrlMap(String path) throws IOException {
        Map<String, String> scientificNameImageFileUrlMap = new HashMap<String, String>();

        InputStream csvIS = new FileInputStream(path);
        Reader reader = new InputStreamReader(csvIS);

        CSVReader r = new CSVReader(reader,',','"');
        String[] fields = r.readNext();
        int fileNameIdx = getIdxForField(fields, "Filename");
        int sciNameIdx = getIdxForField(fields, "Species");


        if(fileNameIdx<0){
            System.out.println("Unable to locate file names in meta file.");
            System.exit(1);
        }

        if(sciNameIdx<0){
            System.out.println("Unable to locate species names in meta file.");
            System.exit(1);
        }

        while((fields = r.readNext())!=null){

            // allow a gap between requests. This will stop us bombarding smaller sites.
            String fileName = fields[fileNameIdx].trim();
            String sciName = fields[sciNameIdx].trim();

            if (fileName != null && sciName != null && !"".equals(fileName) && !"".equals(sciName)) {
                if (fileName.contains(",")) {
                    String[] tmp = fileName.split(",");

                    for (String tmpFileName : tmp) {
//                        String tmpFileNameWithTN = BASE_IMAGE_URL + "tn_" + tmpFileName + IMAGE_APPENDIX;
//                        tmpFileName = BASE_IMAGE_URL + tmpFileName + IMAGE_APPENDIX;
                        scientificNameImageFileUrlMap.put(sciName, tmpFileName);
//                        scientificNameImageFileUrlMap.put(sciName, tmpFileNameWithTN);
                    }
                } else {
//                    String fileNameWithTN = BASE_IMAGE_URL + "tn_" + fileName + IMAGE_APPENDIX;
//                    fileName = BASE_IMAGE_URL + fileName + IMAGE_APPENDIX;
                    scientificNameImageFileUrlMap.put(sciName, fileName);
//                    scientificNameImageFileUrlMap.put(sciName, fileNameWithTN);
                }
            }
        }

        return scientificNameImageFileUrlMap;
    }

    private static int getIdxForField(String[] fields, String header) {
        int i=0;
        for(String field: fields){
            if(header.equalsIgnoreCase(field))
                return i;
            i++;
        }
        return -1;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void processImages(Map<String, String> scientificNameImageFileUrlMap, int infosourceId) throws Exception {

        Set<String> keySet = scientificNameImageFileUrlMap.keySet();
        Iterator<String> iterator = keySet.iterator();

        while (iterator.hasNext()) {
            String sciName = iterator.next();
            String imageFileUrl = scientificNameImageFileUrlMap.get(sciName);
//            System.out.println(sciName + "!!!!!" + imageFileUrl);
            
            String imageUrl1 = BASE_IMAGE_URL + imageFileUrl + IMAGE_APPENDIX;
            String imageUrl2 = BASE_IMAGE_URL + "tn_" + imageFileUrl + IMAGE_APPENDIX;
            storeImageDoc(imageUrl1, imageUrl1, sciName, infosourceId);
            storeImageDoc(imageUrl2, imageUrl2, sciName, infosourceId);
        }
    }

    private void storeImageDoc(String identifier, String imageUrl, String sciName, int infosourceId) throws Exception {

        Response response = null;

        try {
            response = WebUtils.getUrlContentAsBytes(imageUrl);
        } catch (Exception e) {
//            logger.warn("Invalid URL: " + imageUrl);
        }

        if (response != null) {

            ParsedDocument imageDoc = new ParsedDocument();
            imageDoc.setGuid(imageUrl);
            String contentType = response.getContentType();

            //check the content type - may have supplied HTML 404
            if(!MimeType.getImageMimeTypes().contains(contentType)){
                logger.warn("Unrecognised mime type for image: "+contentType+" for image URL "+imageUrl+". Returning null parsed document.");
            } else {

                imageDoc.setContentType(contentType);
                imageDoc.setContent(response.getResponseAsBytes());

                if (sciName != null && !"".equals(sciName)) {
                    String subject = MappingUtils.getSubject();

                    imageDoc.setGuid(identifier);
                    imageDoc.setContent(getContent(identifier));
                    imageDoc.setContentType(contentType);

                    imageDoc.getDublinCore().put(Predicates.DC_TITLE.toString(), sciName);
                    imageDoc.getDublinCore().put(Predicates.DC_IDENTIFIER.toString(), identifier);
                    imageDoc.getDublinCore().put(Predicates.DC_LICENSE.toString(), "CC-BY");
                    imageDoc.getDublinCore().put(Predicates.COUNTRY.toString(), "Australia");

                    imageDoc.getTriples().add(new Triple<String, String, String>(subject, Predicates.SCIENTIFIC_NAME.toString(), sciName));
                    if (imageUrl != null && !"".equals(imageUrl)) {
                        imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), "Steve Williams");
                        imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), "Steve Williams");
                    }
                }

                if (imageDoc != null) {
//                    debugParsedDoc(imageDoc);
                    System.out.println(imageUrl);
                    this.repository.storeDocument(infosourceId, imageDoc);
                }
            }
        }
    }

    private byte[] getContent(String url) throws Exception {
        Response response = WebUtils.getUrlContentAsBytes(url);

        return response.getResponseAsBytes();
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
