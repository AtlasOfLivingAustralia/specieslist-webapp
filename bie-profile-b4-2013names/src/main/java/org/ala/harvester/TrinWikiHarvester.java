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

import org.ala.client.util.RestfulClient;
import org.ala.documentmapper.DocumentMapper;
import org.ala.documentmapper.MappingUtils;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Repository;
import org.ala.repository.Triple;
import org.ala.util.JsonUtil;
import org.ala.util.WebUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * A Harvester class for Trin Wiki. 
 * 
 * @author Tommy Wang
 */
@Component("TrinWikiHarvester")
@Scope(BeanDefinition.SCOPE_PROTOTYPE) 
public class TrinWikiHarvester implements Harvester {

    protected Logger logger = Logger.getLogger(TrinWikiHarvester.class);

    protected String endpoint;
    //  private String eolGroupId;
    //  private String MorphbankRestBaseUrl;
    //  private String MorphbankApiKey;
    //  private int recordsPerPage;
    protected Repository repository;
    protected int timeGap = 0;
    private static final int TRIN_WIKI_INFOSOURCE_ID = 1119;
    protected RestfulClient restfulClient = new RestfulClient(0);
    protected String contentType = "text/xml";
    protected static String jsonStr = "{\"_raw_text\":\"%META:TOPICINFO{_authorWikiName=\\\"AdminUser\\\" author=\\\"BaseUserMapping_333\\\" date=\\\"1326240804\\\" format=\\\"1.1\\\" version=\\\"1\\\"}%\\n\\n%META:FORM{name=\\\"TaxonProfile/Definitions.SeaSlug_Species_Form\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_LegacyID\\\" attributes=\\\"H\\\" title=\\\"Legacy ID\\\" type=\\\"label\\\" value=\\\"2041\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_Species\\\" size=\\\"80\\\" title=\\\"Species\\\" type=\\\"text\\\" value=\\\"Acanthodoris brunnea\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_GenusPart\\\" attributes=\\\"H\\\" title=\\\"Genus part\\\" type=\\\"dynamiclabel+onsave\\\" value=\\\"Acanthodoris\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_GenusAbbr\\\" attributes=\\\"H\\\" title=\\\"Genus abbreviation (Eg. 'H')\\\" type=\\\"input\\\" value=\\\"A\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_SpeciesPart\\\" attributes=\\\"H\\\" title=\\\"Species part\\\" type=\\\"dynamiclabel+onsave\\\" value=\\\"brunnea\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_Author\\\" size=\\\"80\\\" title=\\\"Author\\\" type=\\\"text\\\" value=\\\"MacFarland, 1905\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_Redetermination\\\" size=\\\"5\\\" title=\\\"The genus was redetermined\\\" type=\\\"text\\\" value=\\\"FALSE\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_NameUncertain\\\" size=\\\"80\\\" title=\\\"Is there debate or uncertainty about the name\\\" type=\\\"text\\\" value=\\\"FALSE\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_Distribution\\\" title=\\\"Distribution\\\" type=\\\"text\\\" value=\\\"\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_Caption\\\" size=\\\"80\\\" title=\\\"Photo Caption\\\" type=\\\"richtext\\\" value=\\\"<p>10m of water on the sand near the Monterey Breakwater, California, USA. Approx 20mm long.<br>Photo: Sami Laine</p>\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_BodyText\\\" size=\\\"80\\\" title=\\\"Profile body text\\\" type=\\\"richtext\\\" value=\\\"<p>The mantle is brown with blotches and spots of white and black. Much of the mantle is covered in high pointed translucent papilla which are greyish basally and whitish in the upper half. Dave Behrens in [[Taxon::Brbehre1][Pacific Coast Nudibranchs]] notes that one of its most distinctive characteristics is the pungent odour of cedar it gives off when handled. it feeds on bryozoans, and grows to approx 20mm long.</p>%0a%0a<ul>%0a<li>%0a<div>Behrens, D., 1991 [[Taxon::Brbehre1][Pacific Coast Nudibranchs]].</div>%0a</li>%0a%0a<li>%0a<div>MacFarland, F.M. (1905) A preliminary account of the Dorididae from Monterey Bay, California. <em>Proceedings of the Biological Society of Washington 18</em>: 35-54.</div>%0a</li>%0a%0a<li>%0a<div>MacFarland, F.M. (1905) Opisthobranchiate mollusca from Monterey Bay, California, and vicinity. <em>Bulletin of the Bureau of Fisheries, 25</em>: 109-151.</div>%0a</li>%0a</ul>%0a\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_CiteSwitch\\\" attributes=\\\"H\\\" title=\\\"Cite switch\\\" type=\\\"checkbox\\\" value=\\\"TRUE\\\" values=\\\"1\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_ImageList\\\" size=\\\"80\\\" title=\\\"Topic list of related Images\\\" type=\\\"text\\\" value=\\\"M8068a\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_RelatedTaxaText\\\" title=\\\"Related taxa link text\\\" type=\\\"text\\\" value=\\\"\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_RelatedTaxa\\\" title=\\\"Related taxa\\\" type=\\\"topic+list\\\" value=\\\"\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_Order\\\" size=\\\"80\\\" title=\\\"Order\\\" type=\\\"text\\\" value=\\\"NUDIBRANCHIA\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_Suborder\\\" size=\\\"80\\\" title=\\\"Suborder\\\" type=\\\"text\\\" value=\\\"DORIDINA\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_Superfamily\\\" size=\\\"80\\\" title=\\\"Superfamily\\\" type=\\\"text\\\" value=\\\"ANADORIDOIDEA\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_Family\\\" size=\\\"80\\\" title=\\\"Family\\\" type=\\\"text\\\" value=\\\"Onchidorididae\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_Subfamily\\\" size=\\\"80\\\" title=\\\"Subfamily\\\" type=\\\"text\\\" value=\\\"\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_Phylum\\\" size=\\\"80\\\" title=\\\"Phylum\\\" type=\\\"text\\\" value=\\\"\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_Subphylum\\\" size=\\\"80\\\" title=\\\"Phylum\\\" type=\\\"text\\\" value=\\\"\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_Class\\\" size=\\\"80\\\" title=\\\"Class\\\" type=\\\"text\\\" value=\\\"\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_LegacyBase\\\" attributes=\\\"H\\\" title=\\\"Legacy base\\\" type=\\\"label\\\" value=\\\"acanbrun\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_CiteName\\\" size=\\\"80\\\" title=\\\"Citation name\\\" type=\\\"text\\\" value=\\\"Rudman, W.B.\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_LegacyCiteDate\\\" attributes=\\\"H\\\" title=\\\"Legacy citedate\\\" type=\\\"label\\\" value=\\\"2002-10-29\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_Display\\\" size=\\\"80\\\" title=\\\"Display this species profile or not\\\" type=\\\"text\\\" value=\\\"TRUE\\\"}%\\n%META:FIELD{name=\\\"SeaSlug_LegacyUID\\\" attributes=\\\"H\\\" title=\\\"Legacy UID\\\" type=\\\"label\\\" value=\\\"351260A0-C4F8-7BE9-A3BD19F8867C4B05\\\"}%\\n\",\"_text\":\"\",\"_web\":\"Marine/SeaSlugs/Taxa\",\"_getRev1Info\":{\"rev1info\":{\"date\":1326240804,\"version\":1,\"author\":\"BaseUserMapping_333\"}},\"FILEATTACHMENT\":[],\"_loadedRev\":\"1\",\"TOPICINFO\":[{\"_authorWikiName\":\"AdminUser\",\"format\":\"1.1\",\"date\":1326240804,\"version\":1,\"rev\":1,\"author\":\"BaseUserMapping_333\"}],\"FIELD\":[{\"value\":\"2041\",\"name\":\"SeaSlug_LegacyID\",\"title\":\"Legacy ID\",\"type\":\"label\",\"attributes\":\"H\"},{\"value\":\"Acanthodoris brunnea\",\"name\":\"SeaSlug_Species\",\"title\":\"Species\",\"type\":\"text\",\"size\":\"80\"},{\"value\":\"Acanthodoris\",\"name\":\"SeaSlug_GenusPart\",\"title\":\"Genus part\",\"type\":\"dynamiclabel+onsave\",\"attributes\":\"H\"},{\"value\":\"A\",\"name\":\"SeaSlug_GenusAbbr\",\"title\":\"Genus abbreviation (Eg. 'H')\",\"type\":\"input\",\"attributes\":\"H\"},{\"value\":\"brunnea\",\"name\":\"SeaSlug_SpeciesPart\",\"title\":\"Species part\",\"type\":\"dynamiclabel+onsave\",\"attributes\":\"H\"},{\"value\":\"MacFarland, 1905\",\"name\":\"SeaSlug_Author\",\"title\":\"Author\",\"type\":\"text\",\"size\":\"80\"},{\"value\":\"FALSE\",\"name\":\"SeaSlug_Redetermination\",\"title\":\"The genus was redetermined\",\"type\":\"text\",\"size\":\"5\"},{\"value\":\"FALSE\",\"name\":\"SeaSlug_NameUncertain\",\"title\":\"Is there debate or uncertainty about the name\",\"type\":\"text\",\"size\":\"80\"},{\"value\":\"\",\"name\":\"SeaSlug_Distribution\",\"title\":\"Distribution\",\"type\":\"text\"},{\"value\":\"<p>10m of water on the sand near the Monterey Breakwater, California, USA. Approx 20mm long.<br>Photo: Sami Laine</p>\",\"name\":\"SeaSlug_Caption\",\"title\":\"Photo Caption\",\"type\":\"richtext\",\"size\":\"80\"},{\"value\":\"<p>The mantle is brown with blotches and spots of white and black. Much of the mantle is covered in high pointed translucent papilla which are greyish basally and whitish in the upper half. Dave Behrens in [[Taxon::Brbehre1][Pacific Coast Nudibranchs]] notes that one of its most distinctive characteristics is the pungent odour of cedar it gives off when handled. it feeds on bryozoans, and grows to approx 20mm long.</p>\\n\\n<ul>\\n<li>\\n<div>Behrens, D., 1991 [[Taxon::Brbehre1][Pacific Coast Nudibranchs]].</div>\\n</li>\\n\\n<li>\\n<div>MacFarland, F.M. (1905) A preliminary account of the Dorididae from Monterey Bay, California. <em>Proceedings of the Biological Society of Washington 18</em>: 35-54.</div>\\n</li>\\n\\n<li>\\n<div>MacFarland, F.M. (1905) Opisthobranchiate mollusca from Monterey Bay, California, and vicinity. <em>Bulletin of the Bureau of Fisheries, 25</em>: 109-151.</div>\\n</li>\\n</ul>\\n\",\"name\":\"SeaSlug_BodyText\",\"title\":\"Profile body text\",\"type\":\"richtext\",\"size\":\"80\"},{\"value\":\"TRUE\",\"name\":\"SeaSlug_CiteSwitch\",\"title\":\"Cite switch\",\"type\":\"checkbox\",\"values\":\"1\",\"attributes\":\"H\"},{\"value\":\"M8068a\",\"name\":\"SeaSlug_ImageList\",\"title\":\"Topic list of related Images\",\"type\":\"text\",\"size\":\"80\"},{\"value\":\"\",\"name\":\"SeaSlug_RelatedTaxaText\",\"title\":\"Related taxa link text\",\"type\":\"text\"},{\"value\":\"\",\"name\":\"SeaSlug_RelatedTaxa\",\"title\":\"Related taxa\",\"type\":\"topic+list\"},{\"value\":\"NUDIBRANCHIA\",\"name\":\"SeaSlug_Order\",\"title\":\"Order\",\"type\":\"text\",\"size\":\"80\"},{\"value\":\"DORIDINA\",\"name\":\"SeaSlug_Suborder\",\"title\":\"Suborder\",\"type\":\"text\",\"size\":\"80\"},{\"value\":\"ANADORIDOIDEA\",\"name\":\"SeaSlug_Superfamily\",\"title\":\"Superfamily\",\"type\":\"text\",\"size\":\"80\"},{\"value\":\"Onchidorididae\",\"name\":\"SeaSlug_Family\",\"title\":\"Family\",\"type\":\"text\",\"size\":\"80\"},{\"value\":\"\",\"name\":\"SeaSlug_Subfamily\",\"title\":\"Subfamily\",\"type\":\"text\",\"size\":\"80\"},{\"value\":\"\",\"name\":\"SeaSlug_Phylum\",\"title\":\"Phylum\",\"type\":\"text\",\"size\":\"80\"},{\"value\":\"\",\"name\":\"SeaSlug_Subphylum\",\"title\":\"Phylum\",\"type\":\"text\",\"size\":\"80\"},{\"value\":\"\",\"name\":\"SeaSlug_Class\",\"title\":\"Class\",\"type\":\"text\",\"size\":\"80\"},{\"value\":\"acanbrun\",\"name\":\"SeaSlug_LegacyBase\",\"title\":\"Legacy base\",\"type\":\"label\",\"attributes\":\"H\"},{\"value\":\"Rudman, W.B.\",\"name\":\"SeaSlug_CiteName\",\"title\":\"Citation name\",\"type\":\"text\",\"size\":\"80\"},{\"value\":\"2002-10-29\",\"name\":\"SeaSlug_LegacyCiteDate\",\"title\":\"Legacy citedate\",\"type\":\"label\",\"attributes\":\"H\"},{\"value\":\"TRUE\",\"name\":\"SeaSlug_Display\",\"title\":\"Display this species profile or not\",\"type\":\"text\",\"size\":\"80\"},{\"value\":\"351260A0-C4F8-7BE9-A3BD19F8867C4B05\",\"name\":\"SeaSlug_LegacyUID\",\"title\":\"Legacy UID\",\"type\":\"label\",\"attributes\":\"H\"}],\"_indices\":{\"FIELD\":{\"SeaSlug_LegacyID\":0,\"SeaSlug_Display\":26,\"SeaSlug_LegacyCiteDate\":25,\"SeaSlug_LegacyUID\":27,\"SeaSlug_GenusAbbr\":3,\"SeaSlug_CiteName\":24,\"SeaSlug_Caption\":9,\"SeaSlug_Subphylum\":21,\"SeaSlug_Species\":1,\"SeaSlug_ImageList\":12,\"SeaSlug_NameUncertain\":7,\"SeaSlug_RelatedTaxa\":14,\"SeaSlug_Family\":18,\"SeaSlug_BodyText\":10,\"SeaSlug_SpeciesPart\":4,\"SeaSlug_Redetermination\":6,\"SeaSlug_LegacyBase\":23,\"SeaSlug_Class\":22,\"SeaSlug_Subfamily\":19,\"SeaSlug_GenusPart\":2,\"SeaSlug_Author\":5,\"SeaSlug_Suborder\":16,\"SeaSlug_RelatedTaxaText\":13,\"SeaSlug_Order\":15,\"SeaSlug_Superfamily\":17,\"SeaSlug_Distribution\":8,\"SeaSlug_Phylum\":20,\"SeaSlug_CiteSwitch\":11},\"CREATEINFO\":{},\"FORM\":{\"TaxonProfile/Definitions.SeaSlug_Species_Form\":0},\"TOPICINFO\":{}},\"FORM\":[{\"name\":\"TaxonProfile/Definitions.SeaSlug_Species_Form\"}],\"_latestIsLoaded\":1,\"_topic\":\"Acanbrun\"}";

    /**
     * Main method for testing this particular Harvester
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String[] locations = {"classpath*:spring.xml"};
        ApplicationContext context = new ClassPathXmlApplicationContext(locations);
        TrinWikiHarvester h = new TrinWikiHarvester();
        Repository r = (Repository) context.getBean("repository"); 
        h.setRepository(r);

        //set the connection params 
        Map<String, String> connectParams = new HashMap<String, String>();

        //        connectParams.put("endpoint", "https://wiki.trin.org.au/bin/query/Marine/SeaSlugs/Taxa/Acanbrun/topic.json");
        connectParams.put("endpoint", jsonStr);

        h.setConnectionParams(connectParams);
        h.start(TRIN_WIKI_INFOSOURCE_ID); 
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
    @Override
    public void start(int infosourceId) throws Exception {

        // TODO Auto-generated method stub
        Thread.sleep(timeGap);
        System.out.println("Infosource ID: " + infosourceId);

        // Obtains the image listing on the page number specified.
        // Instance variable `currentResDom` will have new
        // DOM representation of the result.
        List<String> attributeNameList = new ArrayList<String>();

        attributeNameList.add("Species");
        attributeNameList.add("Genus part");
        attributeNameList.add("Author");
        attributeNameList.add("Distribution");
        attributeNameList.add("Profile body text");
        attributeNameList.add("Order");
        attributeNameList.add("Suborder");
        attributeNameList.add("Superfamily");
        attributeNameList.add("Family");
        attributeNameList.add("Subfamily");
        attributeNameList.add("Phylum");
        attributeNameList.add("Class");
        attributeNameList.add("Display this species profile or not");

        Map<String,String> valueMap = getJsonValueMap(this.endpoint, attributeNameList);

        displayMap(valueMap);
        
        if (valueMap != null && "TRUE".equals(valueMap.get("Display this species profile or not"))) {
            processSingleImage(infosourceId, valueMap);
        }

        //        System.out.println(valueMap.get("FIELD"));
        //        System.out.println(valueMap.get("FIELD"));
        //        Document parsedDoc = getIndexPage();
        //
        //        if (parsedDoc == null) {
        //            String errMsg = "DOM representation of image list XML has null reference.  ";
        //            logger.error(errMsg);
        //        }
        //
        //        String xpathToCount = "count(//root/item)";
        //        int resultNum = getCount(parsedDoc, xpathToCount);
        //
        //        //                      System.out.println(resultNum);
        //
        //        if (resultNum > 0) {
        //            for (int counter = 1; counter <= resultNum; counter++) {
        //                try {
        //                    processSingleImage(infosourceId, counter, parsedDoc);
        //                } catch (Exception e) {
        //                    e.printStackTrace();
        //                }
        //            }
        //        }
    }

    private void displayMap(Map<String, String> map) {
        Set<String> keySet = map.keySet();
        Iterator<String> iter = keySet.iterator();

        while (iter.hasNext()) {
            String key = iter.next();
            String value = map.get(key);

            System.out.println("key: " + key + ", value: " + value);

        }
    }


    public static Map<String,String> getJsonValueMap(String url, List<String> attributeNameList) throws Exception {
        String jsonStr = null;
        try {
            jsonStr = WebUtils.getUrlContentAsString(url);
        } catch (Exception e) {
            jsonStr = url;
        }
        Map<String,String> jsonValuedMap = new HashMap<String, String>();

        //        if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
        //            jsonStr = jsonStr.substring(1,jsonStr.length()-1);
        //        }

        //        System.out.println(jsonStr);

        ObjectMapper om = new ObjectMapper();

        JsonNode root = om.readTree(jsonStr);
        Iterator<JsonNode> iter = root.getElements();
        while(iter.hasNext()){
            JsonNode jsonNode = iter.next();

            Iterator<JsonNode> subNodeIter = jsonNode.getElements();

            while (subNodeIter.hasNext()) {    
                JsonNode subNode = subNodeIter.next();

                //                System.out.println(subNode.getTextValue());

                //                Iterator<String> fieldNameIter = subNode.getFieldNames();
                if (subNode != null && subNode.get("value")!=null && subNode.get("title") != null) {
                    String value = subNode.get("value").getTextValue();

                    String title = subNode.get("title").getTextValue();



                    for (String attributeName : attributeNameList) {
                        if (title != null && title.equals(attributeName)) {
                            if (value != null && !"".equals(value)) {
                                jsonValuedMap.put(attributeName, value);
                            }
                        } 
                    }
                }
            }
        }


        return jsonValuedMap;
    }

    private int getCount(Document currentResDom, String xpathToCount) throws Exception {
        int resultNum = 0;

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        resultNum = Integer.valueOf((String) xpath.evaluate(xpathToCount, currentResDom,
                XPathConstants.STRING));

        return resultNum;
    }

    /**
     * Process a single image, do the document mapping etc
     * 
     * @param infosourceId
     * @param imageIndex
     * @param currentResDom
     * @throws Exception
     */
    private void processSingleImage(int infosourceId, Map<String, String> valueMap) throws Exception {

        ParsedDocument pd = new ParsedDocument();

        String subject = MappingUtils.getSubject();


        //        String xPathToImageUrl = "/response/object[" + imageIndex + "]/thumbUrl/text()";
        //        String xPathToLicense = "/response/object[" + imageIndex + "]/copyrightText/text()";
        //        String xPathToSpecificEpithet = "/response/object[" + imageIndex + "]/SpecificEpithet/text()";
        //        String xPathToCountry = "/response/object[" + imageIndex + "]/Country/text()";
        //        String xPathToLocality = "/response/object[" + imageIndex + "]/Locality/text()";

        String scientificName = valueMap.get("Species");
        String phylum = valueMap.get("Phylum");
        String order = valueMap.get("Order");
        String genus = valueMap.get("Genus part");
        String author = valueMap.get("Author");
        String distribution = valueMap.get("Distribution");
        String description = valueMap.get("Profile body text");
        String suborder = valueMap.get("Suborder");
        String superfamily = valueMap.get("Superfamily");
        String family = valueMap.get("Family");
        String subfamily = valueMap.get("Subfamily");
        String klass = valueMap.get("Class");
        
        List<Triple<String,String,String>> triples = pd.getTriples();
        Map<String, String> dcs = pd.getDublinCore();

        pd.setGuid(this.endpoint);
        pd.setContent(this.endpoint.getBytes());
        pd.setContentType(contentType);

        dcs.put(Predicates.DC_TITLE.toString(), scientificName);
        dcs.put(Predicates.DC_IDENTIFIER.toString(), this.endpoint);
        //        dcs.put(Predicates.DC_LICENSE.toString(), "Creative Commons Attribution-Non Commercial 3.0 Australia License, http://creativecommons.org/licenses/by-nc/3.0/au/deed.en");
        //        dcs.put(Predicates.DC_CREATOR.toString(), license);
        dcs.put(Predicates.COUNTRY.toString(), "Australia");

        triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), scientificName));
        triples.add(new Triple(subject, Predicates.PHYLUM.toString(), phylum));
        triples.add(new Triple(subject, Predicates.ORDER.toString(), order));
        triples.add(new Triple(subject, Predicates.GENUS.toString(), genus));
        triples.add(new Triple(subject, Predicates.AUTHOR.toString(), author));
        triples.add(new Triple(subject, Predicates.DISTRIBUTION_TEXT.toString(), distribution));
        if (description != null && !"".equals(description)) {
            description = description.replaceAll("<[a-zA-Z/]{1,}>", "");
            description = description.replaceAll("[\\s]{2,}", " ");
            
            triples.add(new Triple(subject, Predicates.DESCRIPTIVE_TEXT.toString(), description));
        }
        triples.add(new Triple(subject, Predicates.SUBORDER.toString(), suborder));
        triples.add(new Triple(subject, Predicates.SUPERFAMILY.toString(), superfamily));
        triples.add(new Triple(subject, Predicates.FAMILY.toString(), family));
        triples.add(new Triple(subject, Predicates.SUBFAMILY.toString(), subfamily));
        triples.add(new Triple(subject, Predicates.CLASS.toString(), klass));

        if (pd != null) {
//            this.repository.storeDocument(infosourceId, pd);
            debugParsedDoc(pd);
        }

//        String xpathToImageCount = "count(//root/item[" + imageIndex + "]/medium/media)";    
//        int imageCount = getCount(currentResDom, xpathToImageCount);
//
//        System.out.println("item: " + imageIndex + ", counts: " + imageCount);
//
//        for (int imgCounter = 1; imgCounter <= imageCount; imgCounter ++) {
//            String xPathToImageUrl = "//root/item[" + imageIndex + "]/medium/media[" + imgCounter + "]/filename/text()";
//            String xPathToImageLicense = "//root/item[" + imageIndex + "]/medium/media[" + imgCounter + "]/rights/text()";
//            String xPathToImageCreator = "//root/item[" + imageIndex + "]/medium/media[" + imgCounter + "]/acknowledgement/text()";
//            String xPathToImageType = "//root/item[" + imageIndex + "]/medium/media[" + imgCounter + "]/type/text()";
//
//            String imageUrl = null;
//            String license = null;
//            String creator = null;
//            String type = null;
//
//            try {
//                imageUrl = (String) xpath.evaluate(xPathToImageUrl, currentResDom,
//                        XPathConstants.STRING);
//                license = (String) xpath.evaluate(xPathToImageLicense, currentResDom,
//                        XPathConstants.STRING);
//                creator = (String) xpath.evaluate(xPathToImageCreator, currentResDom,
//                        XPathConstants.STRING);
//                type = (String) xpath.evaluate(xPathToImageType, currentResDom,
//                        XPathConstants.STRING);
//
//            } catch (XPathExpressionException getPageFragmentationError) {
//                String errMsg = "Failed to obtain Image details";
//                logger.error(errMsg);
//                throw new Exception(errMsg, getPageFragmentationError);
//            }
//
//            if (license != null && license.contains("CC BY") && imageUrl != null && !"".equals(imageUrl) && (type != null && (!type.contains("Thumb") && !type.contains("List")))) {
//                ParsedDocument imageDoc = new ParsedDocument();
//                System.out.println(type);
//                imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
//
//                if (imageDoc != null) {
//                    imageDoc.getDublinCore().put(Predicates.DC_LICENSE.toString(), license);
//                    if (creator != null && !"".equals(creator)){
//                        imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), creator);
//                        imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), creator);
//                        debugParsedDoc(imageDoc);
//
//                        this.repository.storeDocument(infosourceId, imageDoc);
//                    }
//                }
//            }


//        }

    } // End of `processSingleImage` method.

    private byte[] getContent(String url) throws Exception {
        String contentStr = null;

        contentStr = WebUtils.getHTMLPageAsXML(url);
        //        System.out.println(contentStr);

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

    private org.w3c.dom.Document getIndexPage() throws Exception {

        final String MorphbankMethodUri = "Morphbank.photos.search";

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
        //      System.out.println(urlToSearch);
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

            String inputStr = method.getResponseBodyAsString();
            //            inputStr = inputStr.replaceAll("[^\\x00-\\x7f]*", "");
            inputStr = inputStr.replaceAll("/dwcg:VerbatimLongitude>", "</dwcg:VerbatimLongitude>");
            inputStr = inputStr.replaceAll("/dwcg:VerbatimLatitude>", "</dwcg:VerbatimLatitude>");
            inputStr = inputStr.replaceAll("<</", "</");

            //            Pattern p = Pattern.compile("[^<]{1}/[a-zA-Z]{1,}:[a-zA-Z]{1,}>");
            //            
            //            Matcher m = p.matcher(inputStr);
            //            
            //            int searchIdx = 0;
            //            
            //            while (m.find(searchIdx)) {
            //                int endIdx = m.end();
            //                
            //                
            //                
            //                searchIdx = endIdx;
            //            }

            //            System.out.println(inputStr);
            InputSource is = new InputSource(new StringReader(new String(inputStr)));
            // Instantiates a DOM builder to create a DOM of the response.
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // return a parsed Document
            return builder.parse(is);

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
