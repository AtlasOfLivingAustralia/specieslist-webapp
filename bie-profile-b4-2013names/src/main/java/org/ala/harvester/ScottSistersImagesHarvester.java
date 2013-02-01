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
import java.io.FileNotFoundException;
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
@Component("ScottSistersImagesHarvester")
@Scope(BeanDefinition.SCOPE_PROTOTYPE) 
public class ScottSistersImagesHarvester implements Harvester {

    protected Logger logger = Logger.getLogger(ScottSistersImagesHarvester.class);

    protected Repository repository;
    protected int timeGap = 0;
    //	private static final int RESULT_LIMIT = 6544;
    private static final int SCOTT_SISTERS_INFOSOURCE_ID = 1098;
    protected String contentType = "text/xml";
    private static final String BASE_IMAGE_URL = "http://www2.ala.org.au/datasets/ScottSisters/";
    private static final String IMAGE_INFO_PATH = "/data/scott_image_info.csv";
    private static final String IMAGE_NAME_MAPPING_PATH = "/data/scott_image_name_mapping.csv";
    private static final String DEFAULT_IMAGE_SOURCE = "http://www.australianmuseum.net.au/Beauty-from-Nature-art-of-the-Scott-Sisters/";

    /**
     * Main method for testing this particular Harvester
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String[] locations = {"classpath*:spring.xml"};
        ApplicationContext context = new ClassPathXmlApplicationContext(locations);
        ScottSistersImagesHarvester h = new ScottSistersImagesHarvester();
        Repository r = (Repository) context.getBean("repository"); 
        h.setRepository(r);

        //set the connection params	
        h.start(SCOTT_SISTERS_INFOSOURCE_ID); 
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
    @Override
    public void start(int infosourceId) throws Exception {

        // TODO Auto-generated method stub
        Thread.sleep(timeGap);

        // Obtains the image listing on the page number specified.
        // Instance variable `currentResDom` will have new
        // DOM representation of the result.
        List<ImageObj> imageObjList = getImageObjList(IMAGE_NAME_MAPPING_PATH, IMAGE_INFO_PATH);

        storeImageDoc(imageObjList, infosourceId);
    }

    private List<ImageObj> getImageObjList(String imageNameMappingPath, String imageInfoPath) throws Exception {
        List<ImageObj> imageObjList = new ArrayList<ImageObj>();

        CSVReader r = getCsvReader(imageNameMappingPath);
        String[] fields = r.readNext();
        int fileNameIdx = getIdxForField(fields, "Image file name");
        int sciNameIdx = getIdxForField(fields, "Current scientific name");
        int genusIdx = getIdxForField(fields, "Genus");
        int speciesIdx = getIdxForField(fields, "specific");
        int subspeciesIdx = getIdxForField(fields, "subspecies");
        int familyIdx = getIdxForField(fields, "Family");
        int commonNameIdx = getIdxForField(fields, "Common name");

        if(fileNameIdx<0){
            System.out.println("Unable to locate file names in file " + imageNameMappingPath);
            System.exit(1);
        }

        if(sciNameIdx<0){
            System.out.println("Unable to locate species names in file " + imageNameMappingPath);
            System.exit(1);
        }

        while((fields = r.readNext())!=null){

            // allow a gap between requests. This will stop us bombarding smaller sites.
            String fileName = fields[fileNameIdx].trim();
            String sciName = fields[sciNameIdx].trim();
            String genus = fields[genusIdx].trim();
            String species = fields[speciesIdx].trim();
            String subspecies = fields[subspeciesIdx].trim();
            String family = fields[familyIdx].trim();
            String commonName = fields[commonNameIdx].trim();
            
            if (family.contains(":")) {
                family = family.split(":")[0];
            }

            if (fileName != null && sciName != null && !"".equals(fileName) && !"".equals(sciName)) {
                ImageObj imageObj = new ImageObj();

                imageObj.setGuid(BASE_IMAGE_URL + fileName + "#" + sciName);
                imageObj.setFileName(fileName);
                imageObj.setScientificName(sciName);
                imageObj.setGenus(genus);
                imageObj.setSpecies(species);
                imageObj.setSubspecies(subspecies);
                imageObj.setFamily(family);
                imageObj.setCommonName(commonName);

                imageObjList.add(imageObj);
            }
        }

        r = getCsvReader(imageInfoPath);
        fields = r.readNext();

        int creatorIdx = getIdxForField(fields, "Artist");
        int fileNameIdx2 = getIdxForField(fields, "Image file name");
        int isPartOfIdx = getIdxForField(fields, "AM web page");
//        int commonNameIdx = getIdxForField(fields, "Common Name");

        if(fileNameIdx2<0){
            System.out.println("Unable to locate file names in file " + imageInfoPath);
            System.exit(1);
        }

        while((fields = r.readNext())!=null){

            // allow a gap between requests. This will stop us bombarding smaller sites.
            String fileName = fields[fileNameIdx2].trim();
            String creator = fields[creatorIdx].trim();
            String isPartOf = fields[isPartOfIdx].trim();
            

            if (fileName != null && !"".equals(fileName)) {

                for (ImageObj imageObj : imageObjList) {
                    if (imageObj.getFileName().equals(fileName)) {
                        imageObj.setCreator(creator);

                        if (isPartOf != null && !"".equals(isPartOf)) {
                            imageObj.setIsPartOf(isPartOf);
                        } else {
                            imageObj.setIsPartOf(DEFAULT_IMAGE_SOURCE);
                        }
                    }
                }

            }
        }

        return imageObjList;
    }

    private CSVReader getCsvReader(String filePath) throws Exception {
        InputStream csvIS = new FileInputStream(filePath);
        Reader reader = new InputStreamReader(csvIS);

        CSVReader r = new CSVReader(reader,',','"');

        return r;

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
    private void storeImageDoc(List<ImageObj> imageObjList, int infosourceId) throws Exception {

        for (ImageObj imageObj : imageObjList) {
            Response response = null;

            try {
                response = WebUtils.getUrlContentAsBytes(imageObj.getGuid());
            } catch (Exception e) {
                //            logger.warn("Invalid URL: " + imageUrl);
            }

            if (response != null) {

                ParsedDocument imageDoc = new ParsedDocument();
                imageDoc.setGuid(imageObj.getGuid());
                String contentType = response.getContentType();

                //check the content type - may have supplied HTML 404
                if(!MimeType.getImageMimeTypes().contains(contentType)){
                    logger.warn("Unrecognised mime type for image: "+contentType+" for image URL "+imageObj.getGuid()+". Returning null parsed document.");
                } else {

                    imageDoc.setContentType(contentType);
                    imageDoc.setContent(response.getResponseAsBytes());

                    if (imageObj.getScientificName() != null && !"".equals(imageObj.getScientificName())) {
                        String subject = MappingUtils.getSubject();

                        imageDoc.getDublinCore().put(Predicates.DC_TITLE.toString(), imageObj.getScientificName());
                        imageDoc.getDublinCore().put(Predicates.DC_IDENTIFIER.toString(), imageObj.getIsPartOf());
                        imageDoc.getDublinCore().put(Predicates.DC_LICENSE.toString(), "http://australianmuseum.net.au/copyright");
                        imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), "Copyright Australian Museum");
                        imageDoc.getDublinCore().put(Predicates.COUNTRY.toString(), "Australia");
                        imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), imageObj.getCreator());
                        imageDoc.getDublinCore().put(Predicates.DC_IS_PART_OF.toString(), imageObj.getIsPartOf());
                        
                        List<Triple<String,String,String>> triples = imageDoc.getTriples();
                        triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), imageObj.getScientificName()));
                        triples.add(new Triple(subject, Predicates.GENUS.toString(), imageObj.getGenus()));
                        triples.add(new Triple(subject, Predicates.SPECIES.toString(), imageObj.getSpecies()));
                        triples.add(new Triple(subject, Predicates.FAMILY.toString(), imageObj.getFamily()));
                        
                        if (!imageObj.getCommonName().contains(" or ")) {
                            triples.add(new Triple(subject, Predicates.COMMON_NAME.toString(), imageObj.getCommonName()));
                        } else {
                            String[] tmp = imageObj.getCommonName().split(" or ");
                            for (String cn : tmp) {
                                triples.add(new Triple(subject, Predicates.COMMON_NAME.toString(), cn.trim())); 
                            }
                        }
                        if (imageObj.getSubspecies() != null && !"".equals(imageObj.getSubspecies())) {
                            triples.add(new Triple(subject, Predicates.SUBSPECIES.toString(), imageObj.getSubspecies()));
                        }
                    }

                    if (imageDoc != null) {
                        //                    debugParsedDoc(imageDoc);
                        System.out.println(imageObj.getGuid());
                        this.repository.storeDocument(infosourceId, imageDoc);
                    }
                }
            }
        }
    }

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

    private class ImageObj {
        private String guid;
        private String fileName;
        private String scientificName;
        private String genus;
        private String species;
        private String subspecies;
        private String family;
        private String commonName;
        private String creator;
        private String isPartOf;

        public String getFileName() {
            return fileName;
        }
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
        public String getScientificName() {
            return scientificName;
        }
        public void setScientificName(String scientificName) {
            this.scientificName = scientificName;
        }
        public String getGenus() {
            return genus;
        }
        public void setGenus(String genus) {
            this.genus = genus;
        }
        public String getSpecies() {
            return species;
        }
        public void setSpecies(String species) {
            this.species = species;
        }
        public String getSubspecies() {
            return subspecies;
        }
        public void setSubspecies(String subspecies) {
            this.subspecies = subspecies;
        }
        public String getFamily() {
            return family;
        }
        public void setFamily(String family) {
            this.family = family;
        }
        public String getCommonName() {
            return commonName;
        }
        public void setCommonName(String commonName) {
            this.commonName = commonName;
        }
        public String getCreator() {
            return creator;
        }
        public void setCreator(String creator) {
            this.creator = creator;
        }
        public String getIsPartOf() {
            return isPartOf;
        }
        public void setIsPartOf(String isPartOf) {
            this.isPartOf = isPartOf;
        }
        public void setGuid(String guid) {
            this.guid = guid;
        }
        public String getGuid() {
            return guid;
        }
    }
}
