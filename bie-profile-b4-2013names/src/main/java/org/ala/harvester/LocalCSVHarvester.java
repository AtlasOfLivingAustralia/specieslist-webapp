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

import static org.ala.csvmapper.CsvMapper.*;
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

import org.ala.csvmapper.CsvMapper;
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
import org.apache.commons.lang.StringUtils;
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
@Component("LocalCSVHarvester")
@Scope(BeanDefinition.SCOPE_PROTOTYPE) 
public class LocalCSVHarvester implements Harvester {

    protected Logger logger = Logger.getLogger(LocalCSVHarvester.class);

    protected Repository repository;
    protected int timeGap = 0;
    //	private static final int RESULT_LIMIT = 6544;
    protected String contentType = "text/xml";
//    private static final String BASE_IMAGE_URL = "http://www2.ala.org.au/datasets/ScottSisters/";
//    private static final String IMAGE_NAME_MAPPING_PATH = "/data/scott_image_name_mapping.csv";
    

    private Map<String, String> paramsMap;

    /**
     * Main method for testing this particular Harvester
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String[] locations = {"classpath*:spring.xml"};
        ApplicationContext context = new ClassPathXmlApplicationContext(locations);
        LocalCSVHarvester h = new LocalCSVHarvester();
        Repository r = (Repository) context.getBean("repository"); 
        h.setRepository(r);
        int infosourceId = 0;
        String csvMapper = null;
        
        if (args.length == 2) {
            try {
                infosourceId = Integer.valueOf(args[0]);
            } catch (NumberFormatException nfe) {
                System.out.println("Invalid infosource ID!");
                System.exit(1);
            }
            
            csvMapper = args[1];
        } else {
            System.out.println("Usage: LocalCSVHarvester INFOSOURCE_ID CSV_MAPPER_NAME");
            System.exit(1);
        }
        
        
        CsvMapper csvMapperClass = (CsvMapper) Class.forName(csvMapper).newInstance();
//        displayMapContent(csvMapperClass.getParams());
        
        h.setConnectionParams(csvMapperClass.getParams());
        
        //set the connection params	
        h.start(infosourceId);
        
        
    }	
    
    private static void displayMapContent(Map<String, String> map) {
        Set<String> keyset = map.keySet();
        Iterator<String> iterator = keyset.iterator();
        
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = map.get(key);
            System.out.println("Key: " + key + ", value: " + value);
        }
    }

    /**
     * @see org.ala.harvester.Harvester#setConnectionParams(java.util.Map)
     */
    @Override
    public void setConnectionParams(Map<String, String> connectionParams) {
        paramsMap = connectionParams;
    }

    @Override
    public void start(int infosourceId, int timeGap) throws Exception {
        this.timeGap = timeGap;
        start(infosourceId);
    }

    /**
     * @see org.ala.harvester.Harvester#start(int)
     */
    @Override
    public void start(int infosourceId) throws Exception {

        // TODO Auto-generated method stub
        Thread.sleep(timeGap);

        // Obtains the image listing on the page number specified.
        // Instance variable `currentResDom` will have new

        List<ImageObj> imageObjList = getImageObjList(paramsMap.get(IMAGE_MAPPING_PATH));
        storeImageDoc(imageObjList, infosourceId);
    }
    
    private List<ImageObj> getImageObjList(String imageNameMappingPath) throws Exception {
        List<ImageObj> imageObjList = new ArrayList<ImageObj>();

        CSVReader r = getCsvReader(imageNameMappingPath);
        String[] fields = r.readNext();
        int fileNameIdx = getIdxForField(fields, paramsMap.get(FILE_NAME_HEADER));
        int filePathIdx = getIdxForField(fields, paramsMap.get(FILE_PATH_HEADER));
        int sciNameIdx = getIdxForField(fields, paramsMap.get(SCIENTIFIC_NAME_HEADER));
        int descriptionIdx = getIdxForField(fields, paramsMap.get(DESCRIPTIVE_TEXT));
        int familyIdx = getIdxForField(fields, paramsMap.get(FAMILY_HEADER));
        int genusIdx = getIdxForField(fields, paramsMap.get(GENUS_HEADER));
        int countryIdx = getIdxForField(fields, paramsMap.get(COUNTRY_HEADER));
        int creatorIdx = getIdxForField(fields, paramsMap.get(CREATOR_HEADER));
        int rightsIdx = getIdxForField(fields, paramsMap.get(RIGHTS_HEADER));
        int licenceIdx = getIdxForField(fields, paramsMap.get(LICENSE_HEADER));
        int commentIdx = getIdxForField(fields, paramsMap.get(COMMENT_HEADER));
        int synonymIdx = getIdxForField(fields, paramsMap.get(SYNONYM_HEADER));
        int distributionIdx = getIdxForField(fields, paramsMap.get(DISTRIBUTION_HEADER));
        int habitatIdx = getIdxForField(fields, paramsMap.get(HABITAT_HEADER));
        int dietIdx = getIdxForField(fields, paramsMap.get(DIET_HEADER));
        int threatIdx = getIdxForField(fields, paramsMap.get(THREAT_HEADER));
        int conservationStatusIdx = getIdxForField(fields, paramsMap.get(CONSERVATION_STATUS_HEADER));

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
            
            if (filePathIdx != -1) {
                String filePath = fields[filePathIdx].trim(); 
                fileName = filePath + fileName;
            }
            
//            String genus = fields[genusIdx].trim();
//            String species = fields[speciesIdx].trim();
//            String subspecies = fields[subspeciesIdx].trim();
//            String family = fields[familyIdx].trim();
//            String commonName = fields[commonNameIdx].trim();

            if (fileName != null && sciName != null && !"".equals(fileName) && !"".equals(sciName)) {
                ImageObj imageObj = new ImageObj();

                if (paramsMap.get(URL_APPENDIX) != null && !"".equals(paramsMap.get(URL_APPENDIX))) {
                    imageObj.setGuid(paramsMap.get(BASE_URL) + fileName + paramsMap.get(URL_APPENDIX));
                } else {
                    imageObj.setGuid(paramsMap.get(BASE_URL) + fileName);
                }
                imageObj.setFileName(fileName);
                imageObj.setScientificName(sciName);
                if (descriptionIdx != -1) {
                    imageObj.setDescription(fields[descriptionIdx].trim());
                }
                if (familyIdx != -1) {
                    imageObj.setFamily(fields[familyIdx].trim());
                }
                if (genusIdx != -1) {
                    imageObj.setGenus(fields[genusIdx].trim());
                }
                if (countryIdx != -1) {
                    imageObj.setCountry(fields[countryIdx].trim());
                }
                if (creatorIdx != -1) {
                    imageObj.setCreator(fields[creatorIdx].trim());
                }
                if (rightsIdx != -1) {
                    imageObj.setRights(fields[rightsIdx].trim());
                }
                if (commentIdx != -1) {
                    imageObj.setComments(fields[commentIdx].trim());
                }
                if (licenceIdx != -1) {
                    imageObj.setLicence(fields[licenceIdx].trim());
                }
                if (synonymIdx != -1) {
                    imageObj.setSynonym(fields[synonymIdx].trim());
                }
                if (distributionIdx != -1) {
                    imageObj.setDistribution(fields[distributionIdx].trim());
                }
                if (habitatIdx != -1) {
                    imageObj.setHabitat(fields[habitatIdx].trim());
                }
                if (dietIdx != -1) {
                    imageObj.setDiet(fields[dietIdx].trim());
                }
                if (threatIdx != -1) {
                    imageObj.setThreat(fields[threatIdx].trim());
                }
                if (conservationStatusIdx != -1) {
                    imageObj.setConservationStatus(fields[conservationStatusIdx].trim());
                }
                
//                System.out.println("COMMENT: " + imageObj.getComments());
                
//                imageObj.setGenus(genus);
//                imageObj.setSpecies(species);
//                imageObj.setSubspecies(subspecies);
//                imageObj.setFamily(family);
//                imageObj.setCommonName(commonName);

                imageObjList.add(imageObj);
            }
        }

        return imageObjList;
    }

    private CSVReader getCsvReader(String filePath) throws Exception {
        Reader reader = null;
        if(filePath.startsWith("http://")){
            HttpClient c = new HttpClient();
            GetMethod g = new GetMethod(filePath);
            g.setRequestHeader("Accept-Charset","utf-8");
            g.setRequestHeader("Accept","text/plain");
            int status = c.executeMethod(g);
            reader = new InputStreamReader(g.getResponseBodyAsStream(), "UTF-8");
        } else {
            reader = new InputStreamReader(new FileInputStream(filePath), "UTF-8");
        }
        return new CSVReader(reader,',','"');
    }

    private static int getIdxForField(String[] fields, String header) {
        int i=0;
        for(String field: fields){
            if(field.trim().equalsIgnoreCase(header))
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
                response = WebUtils.getUrlContentAsBytes(imageObj.getGuid().replaceAll(" ", "%20"));
            } catch (Exception e) {
                e.printStackTrace();
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
                       
                        if (paramsMap.get(IDENTIFIER) != null && !"".equals(paramsMap.get(IDENTIFIER))) {
                            imageDoc.getDublinCore().put(Predicates.DC_IDENTIFIER.toString(), paramsMap.get(IDENTIFIER) + "#" + imageObj.getScientificName());
                        } else {
                            imageDoc.getDublinCore().put(Predicates.DC_IDENTIFIER.toString(), imageObj.getGuid());
                        }

                        if(imageObj.getLicence() != null){
                            imageDoc.getDublinCore().put(Predicates.DC_LICENSE.toString(), imageObj.getLicence());
                        } else {
                            imageDoc.getDublinCore().put(Predicates.DC_LICENSE.toString(), paramsMap.get(LICENSE));
                        }

                        if (StringUtils.trimToNull(imageObj.getCreator()) == null || "".equals(imageObj.getCreator())) {
                            imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), paramsMap.get(CREATOR));
                        } else {
                            imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), imageObj.getCreator());
                        }
                       
                        if (imageObj.getRights() == null || "".equals(imageObj.getRights())) {                       
                            imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), paramsMap.get(RIGHTS));
                        } else {
                            String rights = imageObj.getRights();
                            if (imageObj.getComments() != null && !"".equals(imageObj.getComments())) {
                                rights = rights + ", " + imageObj.getComments();
                            }
                            
                            String paramRights = paramsMap.get(RIGHTS);
                            
                            if (paramRights != null && !"".equals(paramRights)) {
                                rights = rights + ", " + paramRights;
                            }
                            
                            imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), rights);
                        }
                        
                        if (imageObj.getCountry() == null || "".equals(imageObj.getCountry())) {
                            imageDoc.getDublinCore().put(Predicates.COUNTRY.toString(), "Australia");
                        } else {
                            if (!"".equals(imageObj.getCountry().replaceAll("\\?", "").trim())) {
                                imageDoc.getDublinCore().put(Predicates.COUNTRY.toString(), imageObj.getCountry());
                            }
                        }
                        
                        if (paramsMap.get(IS_PART_OF) != null && !"".equals(paramsMap.get(IS_PART_OF))) {
                            imageDoc.getDublinCore().put(Predicates.DC_IS_PART_OF.toString(), paramsMap.get(IS_PART_OF));
                        }
                        

                        List<Triple<String,String,String>> triples = imageDoc.getTriples();
                        triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), imageObj.getScientificName()));
                        
                        if (imageObj.getDescription() != null && !"".equals(imageObj.getDescription())) {
                            triples.add(new Triple(subject, Predicates.DESCRIPTIVE_TEXT.toString(), imageObj.getDescription()));
                        }
                        if (imageObj.getSynonym() != null && !"".equals(imageObj.getSynonym())) {
                            triples.add(new Triple(subject, Predicates.SYNONYM.toString(), imageObj.getSynonym()));
                        }
                        if (imageObj.getDistribution() != null && !"".equals(imageObj.getDistribution())) {
                            triples.add(new Triple(subject, Predicates.DISTRIBUTION_TEXT.toString(), imageObj.getDistribution()));
                        }
                        if (imageObj.getHabitat() != null && !"".equals(imageObj.getHabitat())) {
                            triples.add(new Triple(subject, Predicates.HABITAT_TEXT.toString(), imageObj.getHabitat()));
                        }
                        if (imageObj.getDiet() != null && !"".equals(imageObj.getDiet())) {
                            triples.add(new Triple(subject, Predicates.DIET_TEXT.toString(), imageObj.getDiet()));
                        }
                        if (imageObj.getThreat() != null && !"".equals(imageObj.getThreat())) {
                            triples.add(new Triple(subject, Predicates.THREATS_TEXT.toString(), imageObj.getThreat()));
                        }
                        if (imageObj.getConservationStatus() != null && !"".equals(imageObj.getConservationStatus())) {
                            triples.add(new Triple(subject, Predicates.CONSERVATION_STATUS.toString(), imageObj.getConservationStatus()));
                        }
                        if (imageObj.getFamily() != null && !"".equals(imageObj.getFamily())) {
                            triples.add(new Triple(subject, Predicates.FAMILY.toString(), imageObj.getFamily()));
                        }
                        if (imageObj.getGenus() != null && !"".equals(imageObj.getGenus())) {
                            triples.add(new Triple(subject, Predicates.GENUS.toString(), imageObj.getGenus()));
                        }
                        if (paramsMap.get(IS_PREFERRED) != null && !"".equals(paramsMap.get(IS_PREFERRED))) {
                            triples.add(new Triple(subject, Predicates.PREFERRED_IMAGE.toString(), paramsMap.get(IS_PREFERRED)));
                        }
                        imageDoc.setTriples(triples);
                    }

                    if (imageDoc != null) {
                        debugParsedDoc(imageDoc);
                        try {
                            this.repository.storeDocument(infosourceId, imageDoc);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
    public void setDocumentMapper(DocumentMapper documentMapper) {}

    private class ImageObj {
        private String guid;
        private String fileName;
        private String scientificName;
        private String genus;
        private String species;
        private String subspecies;
        private String family;
        private String commonName;
        private String description;
        private String creator;
        private String rights;
        private String licence;
        private String comments;
        private String isPartOf;
        private String country;
        private String synonym;
        private String distribution;
        private String habitat;
        private String diet;
        private String threat;
        private String conservationStatus;

        public String getLicence() {
            return licence;
        }

        public void setLicence(String licence) {
            this.licence = licence;
        }

        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
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
        public void setCountry(String country) {
            this.country = country;
        }
        public String getCountry() {
            return country;
        }
        public void setRights(String rights) {
            this.rights = rights;
        }
        public String getRights() {
            return rights;
        }
        public void setComments(String comments) {
            this.comments = comments;
        }
        public String getComments() {
            return comments;
        }
        public void setConservationStatus(String conservationStatus) {
            this.conservationStatus = conservationStatus;
        }
        public String getConservationStatus() {
            return conservationStatus;
        }
        public void setThreat(String threat) {
            this.threat = threat;
        }
        public String getThreat() {
            return threat;
        }
        public void setDiet(String diet) {
            this.diet = diet;
        }
        public String getDiet() {
            return diet;
        }
        public void setHabitat(String habitat) {
            this.habitat = habitat;
        }
        public String getHabitat() {
            return habitat;
        }
        public void setDistribution(String distribution) {
            this.distribution = distribution;
        }
        public String getDistribution() {
            return distribution;
        }
        public void setSynonym(String synonym) {
            this.synonym = synonym;
        }
        public String getSynonym() {
            return synonym;
        }
    }
}
