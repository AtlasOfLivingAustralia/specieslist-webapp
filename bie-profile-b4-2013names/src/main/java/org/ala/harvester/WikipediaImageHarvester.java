package org.ala.harvester;

import au.org.ala.checklist.lucene.CBIndexSearch;
import au.org.ala.checklist.lucene.HomonymException;
import au.org.ala.checklist.lucene.model.NameSearchResult;
import au.org.ala.data.util.RankType;
import org.ala.documentmapper.DocumentMapper;
import org.ala.documentmapper.MappingUtils;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Repository;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.compress.compressors.bzip2.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class WikipediaImageHarvester implements Harvester {

    protected String dbpediaImagesFile = "http://downloads.dbpedia.org/current/en/images_en.nt.bz2";
    protected Repository repository;
    protected String contentType = "text/xml";
    protected String wikipediaBaseUrl = "http://en.wikipedia.org";
    protected boolean downloaded = true;
//    protected final int INFOSOURCE_ID = 1013;

    public static void main(String[] args) throws Exception {
        String[] locations = {"classpath*:spring.xml"};
        
        ApplicationContext context = new ClassPathXmlApplicationContext(locations);
        WikipediaImageHarvester h = new WikipediaImageHarvester();
        Repository r = (Repository) context.getBean("repository"); 
        h.setRepository(r);
        if (args.length == 1 && "--download".equals(args[0])) {
            h.setDownloaded(false);
        }
        h.start(1036);
    }

    

    @Override
    public void start(int infosourceId) throws Exception {
        start(infosourceId,1036);
    }

    @Override
    public void start(int infosourceId, int timeGap) throws Exception {

        //name index
        CBIndexSearch nameIndex = new CBIndexSearch("/data/lucene/namematching");

        //download the images file from DBPedia
        if (!downloaded) {
            System.out.println("Downloading NT triple dump from DBPedia..." + dbpediaImagesFile);
            File bzipFile = new File("/data/images_en.nt.bz2");
            FileUtils.copyURLToFile(new URL(dbpediaImagesFile), bzipFile);
            System.out.println("Downloaded.");

            //decompress
            System.out.println("Decompressing.....");
            FileInputStream in = new FileInputStream(bzipFile);
            FileOutputStream out = new FileOutputStream("/data/images_en.nt");
            BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
            final byte[] buffer = new byte[1048576];
            int n = 0;
            while (-1 != (n = bzIn.read(buffer))) {
                out.write(buffer, 0, n);
            }
            out.close();
            bzIn.close();
            System.out.println("Decompressed.");
        }

        //iterate through each line
        BufferedReader reader = new BufferedReader(new FileReader("/data/images_en.nt"));
        String line = reader.readLine();
        while(line != null){
            //            System.out.println("LINE: " + line);

            if(line.contains("depiction")){
                String[] parts = line.split(" ");

                String dbpediaUrl = parts[0].replaceAll(">","").replaceAll("<","");
                String resourceName = parts[0].substring(parts[0].lastIndexOf('/') + 1);
                String nameToMatch = resourceName.replace(">","").replaceAll("_", " ").trim();
                // println(nameToMatch)

                try {

                    //name must be a bionomial or trinomial
                    if (nameToMatch.contains("-") || nameToMatch.contains(" ")){

                        //only match things that look like binomials or trinomials
                        NameSearchResult nsr = null;

                        try {
                            nameIndex.searchForRecord(nameToMatch, null);
                        } catch (HomonymException he) {

                        }
                        if (nsr == null){
                            //search for common name
                            nsr = nameIndex.searchForCommonName(nameToMatch);
                        }

                        if (nsr != null
                                && (RankType.SPECIES.equals(nsr.getRank()) || RankType.SUBSPECIES.equals(nsr.getRank()))
                                && nsr.getLsid() != null
                                && nsr.getLsid().contains("biodiversity.org.au")
                                && nsr.getRankClassification().getScientificName() !=null
                                && nsr.getRankClassification().getScientificName().contains(" ")
                        ){

                            //validate the match
                            String dbpediaPage = WebUtils.getUrlContentAsString(dbpediaUrl);
                            if (dbpediaPage.contains("http://dbpedia.org/ontology/genus")
                                    || dbpediaPage.contains("http://dbpedia.org/ontology/species")
                                    || dbpediaPage.contains("http://dbpedia.org/property/genus")
                                    || dbpediaPage.contains("http://dbpedia.org/property/species")
                                    || dbpediaPage.contains("http://dbpedia.org/property/binomial")
                                    || dbpediaPage.contains("http://dbpedia.org/ontology/phylum")){
                                System.out.println("URL: " + dbpediaUrl + ", matched string: " + nameToMatch + ", to " + nsr.getRank().toString() + ": " + nsr.getRankClassification().getScientificName());

                                //TODO
                                //download image full res image
                                //download wikipedia page for image e.g. http://en.wikipedia.org/wiki/File:Kangur.rudy.drs.jpg
                                //retrieve creator, rights, licence, date
                                //save to repository
                                String wikiPageUrl = getWikiPageUrl(dbpediaPage);
                                
                                if (wikiPageUrl != null && !"".equals(wikiPageUrl)) {
                                    List<String> imagePageUrlList = getImagePageUrlList(wikiPageUrl);
                                    harvestImagePages(imagePageUrlList, nsr, infosourceId);
                                }

                            } else {
                                System.out.println("False positive for " + "http://en.wikipedia.org/wiki/" + resourceName);
                            }
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

            line = reader.readLine();
        }

        System.out.println("Finished.");
    }

    protected String getWikiPageUrl(String str) {
        Pattern p = Pattern.compile("(http://en.wikipedia.org/wiki/[0-9a-zA-Z_\\-]*)");

        Matcher m = p.matcher(str);

        if (m.find(0)) {
            String wikiPageUrl = m.group(1);
            
            return wikiPageUrl;
        }
        
        return null;
    }

    protected List<String> getImagePageUrlList(String wikiPageUrl) throws Exception {
        System.out.println(wikiPageUrl);
        Document wikiPageDoc = parseUrlToDocument(wikiPageUrl);

        String xpathToImagePageUrls = "//a[@class='image']/@href";

        List<String> imagePageUrlList = getXPathValues(wikiPageDoc, xpathToImagePageUrls);

        return imagePageUrlList;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void harvestImagePages(List<String> imagePageUrlList, NameSearchResult nsr, int infosourceId) throws Exception {
        String subject = MappingUtils.getSubject();
        

        String xpathToCreator = "//table[@class='wikitable filehistory']/tbody[1]/tr[2]/td[5]/text()";
        String xpathToLicense = "//span[@class='licensetpl_long']/text()";
        String xpathToDate = "//td[@class='filehistory-selected']/a[1]/text()";
        String xpathToImageUrl = "//div[@class='fullImageLink']/a/img/@src";
        

        for (String imagePageUrl : imagePageUrlList) {
            imagePageUrl = wikipediaBaseUrl + imagePageUrl;

            System.out.println("Image Page url: " + imagePageUrl);

            Document imagePageDoc = parseUrlToDocument(imagePageUrl);
            
            String imageUrl = getSingleXPathValue(imagePageDoc, xpathToImageUrl);
            String date = getSingleXPathValue(imagePageDoc, xpathToDate);
            String license = getSingleXPathValue(imagePageDoc, xpathToLicense);
            String creator = getSingleXPathValue(imagePageDoc, xpathToCreator);
            String rights = "Copyright by " + creator.trim();
            
            ParsedDocument pd = new ParsedDocument();
            ParsedDocument imageDoc = new ParsedDocument();
            
            List<Triple<String,String,String>> triples = pd.getTriples();
            Map<String, String> dcs = pd.getDublinCore();
            
            pd.setGuid(imagePageUrl);
            pd.setContent(getContent(imagePageUrl));
            pd.setContentType(contentType);
            
            if (imageUrl != null && imageUrl.startsWith("//")) {
                imageUrl = "http:" + imageUrl;
            }
            
            triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), nsr.getRankClassification().getScientificName()));
            triples.add(new Triple(subject, Predicates.IMAGE_URL.toString(), imageUrl));
            
            dcs.put(Predicates.DC_TITLE.toString(), nsr.getRankClassification().getScientificName());
            dcs.put(Predicates.DC_IDENTIFIER.toString(), imagePageUrl.trim());
            dcs.put(Predicates.DC_MODIFIED.toString(), date.trim());
            dcs.put(Predicates.DC_LICENSE.toString(), license.trim());
            dcs.put(Predicates.DC_CREATOR.toString(), creator.trim());
            dcs.put(Predicates.DC_RIGHTS.toString(), rights.trim());
            
            if (imageUrl != null && !"".equals(imageUrl)) {
                imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
                debugParsedDoc(imageDoc);
            }

            debugParsedDoc(pd);
            
            if (pd != null) {
                this.repository.storeDocument(infosourceId, pd);
            }
            if (imageDoc != null) {
                this.repository.storeDocument(infosourceId, imageDoc);
            }
        }
    }

    protected List<String> getXPathValues(Document document, String xpathAsString) throws Exception {

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        List<String> extractedValues = new ArrayList<String>();
        NodeList nodes = (NodeList) xpath.evaluate(xpathAsString, document, XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); i++) {
            String value = (nodes.item(i)).getNodeValue().trim();
            value = StringUtils.trimToNull(value);
            if(value!=null){
                extractedValues.add(value);
            }
        }
        return extractedValues;
    }

    private byte[] getContent(String url) throws Exception {
        String contentStr = null;

        contentStr = WebUtils.getHTMLPageAsXML(url);
        //        System.out.println(contentStr);

        return contentStr.getBytes();
    }

    private String getSingleXPathValue(Document currentResDom, String xpathAsString) throws Exception {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        return (String) xpath.evaluate(xpathAsString, currentResDom,
                XPathConstants.STRING);
    }

    private org.w3c.dom.Document parseUrlToDocument(String urlToSearch) throws Exception {
        try {

            String inputStr = WebUtils.getHTMLPageAsXML(urlToSearch);
            //            inputStr = inputStr.replaceAll("[^\\x00-\\x7f]*", "");

            InputSource is = new InputSource(new StringReader(new String(inputStr)));
            // Instantiates a DOM builder to create a DOM of the response.
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // return a parsed Document
            return builder.parse(is);

        } catch (Exception httpErr) {
            httpErr.printStackTrace();
        } 

        return null;
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

    @Override
    public void setConnectionParams(Map<String, String> connectionParams) {}

    @Override
    public void setDocumentMapper(DocumentMapper documentMapper) {}

    @Override
    public void setRepository(Repository repository) {
        this.repository = repository;
    }
    
    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }
}
