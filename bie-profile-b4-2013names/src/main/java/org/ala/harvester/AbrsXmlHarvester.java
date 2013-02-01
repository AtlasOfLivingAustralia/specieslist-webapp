package org.ala.harvester;

import java.io.StringReader;
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
import org.ala.util.Response;
import org.ala.util.WebUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Harvester for SpeciesBank - ABRS xml
 * 
 * @author Tommy Wang (tommy.wang@csiro.au)
 */
public class AbrsXmlHarvester implements Harvester {

    protected Repository repository;    
    private final String NAMES_LIST_URL_BASE = "http://www.anbg.gov.au/abrs/online-resources/flora/nameslist.xsql?pnid=";
    private final String DISPLAY_URL_BASE = "http://www.anbg.gov.au/abrs/online-resources/flora/stddisplay.xsql?pnid=";
    private final String IMAGE_URL_BASE = "http://www.anbg.gov.au/abrs/online-resources/flora/images/vol";
    private final String DISPLAY_URL_APPENDIX = "&xml-stylesheet=none"; 
    private final String PLANTAE_PNID = "40155";
    private final String CONTENT_TYPE = "text/xml";
    private final static int ABRS_INFOSOURCE_ID = 1001;

    public static void main(String[] args) throws Exception{
        String[] locations = {"classpath*:spring.xml"};
        ApplicationContext context = new ClassPathXmlApplicationContext(locations);
        AbrsXmlHarvester h = new AbrsXmlHarvester();
        Repository r = (Repository) context.getBean("repository"); 
        h.setRepository(r);

        //set the connection params 
        Map<String, String> connectParams = new HashMap<String, String>();

        h.setConnectionParams(connectParams);
        h.start(ABRS_INFOSOURCE_ID); 
    }

    @Override
    public void start(int infosourceId) throws Exception {
        this.start(infosourceId, 0);

    }

    @Override
    public void start(int infosourceId, int timeGap) throws Exception {
        Map<String, String> pnidSciNameMap = new HashMap<String, String>();
        processImages(PLANTAE_PNID, pnidSciNameMap);
    }

    private Document getIndexPage(String pnid) throws Exception {
        String urlToSearch = DISPLAY_URL_BASE + pnid + DISPLAY_URL_APPENDIX;

        System.out.println("Processing URL: "+urlToSearch);

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
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void processSingleImage(int infosourceId, Document currentResDom) throws Exception {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        ParsedDocument pd = new ParsedDocument();
        ParsedDocument imageDoc = null;

        String subject = MappingUtils.getSubject();

        String xPathToIdentifier = "/page/request/parameters/pnid/text()";
        String xPathToScientificName = "/page/ROWSET/ROW/TAXON_NAME/text()";
        String xPathToCommonName = "/page/ROWSET/ROW/COMMONNAME/text()";
        String xPathToImageUrl = "/page/ROWSET/ROW/IMAGES/IMAGES_ITEM/IMGURL/text()";
        String xPathToCreator = "/page/ROWSET/ROW/AUTHOR/text()";
        String xPathToFamily = "/page/ROWSET/ROW/FAMILY/text()";
        String xPathToGenus = "/page/ROWSET/ROW/GENUS/text()";
        String xPathToDescription = "/page/ROWSET/ROW/DESCRIPTION/text()";
        String xPathToHabitat = "/page/ROWSET/ROW/DESCRIPTION/text()";
        String xPathToDistribution = "/page/ROWSET/ROW/DISTRIBUTIONS/DISTRIBUTIONS_ITEM/DIST_TEXT/text()";

        String identifier = null;
        String scientificName = null;
        String commonName = null;
        String imageUrl = null;
        String creator = null;
        String family = null;
        String genus = null;
        String description = null;
        String habitat = null;
        String distribution = null;

        try {
            identifier = (String) xpath.evaluate(xPathToIdentifier, currentResDom,
                    XPathConstants.STRING);
            scientificName = (String) xpath.evaluate(xPathToScientificName, currentResDom,
                    XPathConstants.STRING);
            commonName = (String) xpath.evaluate(xPathToCommonName, currentResDom,
                    XPathConstants.STRING);
            imageUrl = (String) xpath.evaluate(xPathToImageUrl, currentResDom,
                    XPathConstants.STRING);
            creator = (String) xpath.evaluate(xPathToCreator, currentResDom,
                    XPathConstants.STRING);
            family = (String) xpath.evaluate(xPathToFamily, currentResDom,
                    XPathConstants.STRING);
            genus = (String) xpath.evaluate(xPathToGenus, currentResDom,
                    XPathConstants.STRING);
            description = (String) xpath.evaluate(xPathToDescription, currentResDom,
                    XPathConstants.STRING);
            habitat = (String) xpath.evaluate(xPathToHabitat, currentResDom,
                    XPathConstants.STRING);
            distribution = (String) xpath.evaluate(xPathToDistribution, currentResDom,
                    XPathConstants.STRING);

        } catch (XPathExpressionException getPageFragmentationError) {
            String errMsg = "Failed to obtain Morphbank's Detail Page Url";
            System.out.println(errMsg);
            throw new Exception(errMsg, getPageFragmentationError);
        }

        //      System.out.println("Index: " + imageIndex);

        identifier = DISPLAY_URL_BASE + identifier;
        //        System.out.println("URL:" + identifier);

        List<Triple<String,String,String>> triples = pd.getTriples();
        Map<String, String> dcs = pd.getDublinCore();

        pd.setGuid(identifier);
        pd.setContent(getContent(identifier));
        pd.setContentType(CONTENT_TYPE);

        dcs.put(Predicates.DC_TITLE.toString(), scientificName);
        dcs.put(Predicates.DC_IDENTIFIER.toString(), identifier);
        if (creator != null && !"".equals(creator)) {
            dcs.put(Predicates.DC_CREATOR.toString(), creator);
        }

        triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), scientificName));

        if (commonName != null && !"".equals(commonName)) {
            triples.add(new Triple(subject, Predicates.COMMON_NAME.toString(), commonName));
        }
        if (family != null && !"".equals(family)) {
            triples.add(new Triple(subject, Predicates.FAMILY.toString(), family));
        }
        if (genus != null && !"".equals(genus)) {
            triples.add(new Triple(subject, Predicates.GENUS.toString(), genus));
        }
        if (description != null && !"".equals(description)) {
            triples.add(new Triple(subject, Predicates.DESCRIPTIVE_TEXT.toString(), description));
        }
        if (habitat != null && !"".equals(habitat)) {
            triples.add(new Triple(subject, Predicates.HABITAT_TEXT.toString(), habitat));
        }
        if (distribution != null && !"".equals(distribution)) {
            triples.add(new Triple(subject, Predicates.DISTRIBUTION_TEXT.toString(), distribution));
        }


        if (imageUrl != null && !"".equals(imageUrl)) {
            Pattern imageVolNumPattern = Pattern.compile("(?:v)([0-9]*)");
            Matcher m = imageVolNumPattern.matcher(imageUrl);
            String volNum = null;
            if (m.find()) {
                volNum = m.group(1);
            }

            if (volNum != null && !"".equals(volNum)) {
                String imageType = (String) xpath.evaluate("/page/ROWSET/ROW/IMAGES/IMAGES_ITEM/IMGURL[contains(.," + imageUrl + ")]/preceding-sibling::IMGKIND[1]/text()", currentResDom,
                        XPathConstants.STRING);

                imageUrl = IMAGE_URL_BASE + volNum + "/" + imageUrl;

                System.out.println("!!!!" + imageUrl + " of type: " + imageType);
                imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);

                if (imageDoc != null) {
                    if ("map".equalsIgnoreCase(imageType)) {
                        List<Triple<String,String,String>> imageDocTriples = imageDoc.getTriples();
                        imageDocTriples.add(new Triple(subject,Predicates.DIST_MAP_IMG_URL.toString(), imageDoc.getGuid()));
                        imageDoc.setTriples(imageDocTriples);
                    }

                    debugParsedDoc(imageDoc);
                }
            }
        }

        debugParsedDoc(pd);

        if (pd != null) {
            this.repository.storeDocument(infosourceId, pd);
        }
        if (imageDoc != null) {
            this.repository.storeDocument(infosourceId, imageDoc);
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

    private byte[] getContent(String url) throws Exception {
        Response response = WebUtils.getUrlContentAsBytes(url);

        return response.getResponseAsBytes();
    }

    private Map<String, String> processImages(String pnid, Map<String, String> pnidSciNameMap) throws Exception {
        String indexPage = NAMES_LIST_URL_BASE + pnid;
        String kingdomIndex = WebUtils.getUrlContentAsString(indexPage);

        kingdomIndex = kingdomIndex.replaceAll("\\&nbsp;", " ");

        Pattern p = Pattern.compile(
                "(?:<a href=\"stddisplay\\.xsql\\?pnid=)" +
                "([0-9]{1,})" +
                "(?:\">)" +
                "([a-zA-Z ]{1,})" +
        "(?:</a>)");

        Matcher m = p.matcher(kingdomIndex);    

        int searchIdx = 0;



        // Get Phylum links     
        while(m.find(searchIdx)){
            int endIdx = m.end();
            //          String found = content.substring(startIdx, endIdx);
            String subpnid = m.group(1);
            String sciName = m.group(2);

            if (!mapContainsElement(pnidSciNameMap, subpnid)) {
                Document parsedDoc = getIndexPage(subpnid);

                processSingleImage(ABRS_INFOSOURCE_ID, parsedDoc);
                //                System.out.println("PNID:" + subpnid);
                //                System.out.println("NAME:" + sciName);
                pnidSciNameMap.put(subpnid, sciName);
                pnidSciNameMap.putAll(processImages(subpnid, pnidSciNameMap));

            }
            searchIdx = endIdx;
        }
        return pnidSciNameMap;
    }

    private boolean mapContainsElement(Map<String, String> map, String key) {
        if (map.get(key) != null) {
            return true;
        }

        return false;
    }

    @Override
    public void setConnectionParams(Map<String, String> connectionParams) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDocumentMapper(DocumentMapper documentMapper) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRepository(Repository repository) {
        this.repository = repository;

    }
}
