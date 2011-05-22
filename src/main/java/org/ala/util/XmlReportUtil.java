package org.ala.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ala.dao.FulltextSearchDao;
import org.ala.dao.IndexedTypes;
import org.ala.dao.InfoSourceDAO;
import org.ala.dao.TaxonConceptDao;
import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.dto.SearchDTO;
import org.ala.dto.SearchResultsDTO;
import org.ala.model.CommonName;
import org.ala.model.ExtantStatus;
import org.ala.model.Habitat;
import org.ala.model.Image;
import org.ala.model.PestStatus;
import org.ala.model.TaxonConcept;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationContext;

public class XmlReportUtil {

    private FulltextSearchDao searchDao;
    private TaxonConceptDao taxonConceptDao;
    private InfoSourceDAO infosourceDao;

    /** The path to the repository */
    protected String repositoryPath = "/data/bie/";
    /** The URL to the repository */
    protected String repositoryUrl = "http://bie.ala.org.au/repo/";

    private final String XML_HEADER = "<?xml version='1.0' encoding='utf-8' ?>" +
    "<response  xmlns='http://www.eol.org/transfer/content/0.2'  xmlns:xsd='http://www.w3.org/2001/XMLSchema'  " +
    "xmlns:dc='http://purl.org/dc/elements/1.1/'  xmlns:dcterms='http://purl.org/dc/terms/'  xmlns:geo='http://www.w3.org/2003/01/geo/wgs84_pos#'  " +
    "xmlns:dwc='http://rs.tdwg.org/dwc/dwcore/'  xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'  " +
    "xsi:schemaLocation='http://www.eol.org/transfer/content/0.2 http://services.eol.org/schema/content_0_2.xsd'>";
    private final String XML_FOOTER = "</response>";

    private final static String UID = "dr390";

    private Map<String, String> uidInfosourceIdMap;

    private final String OUTPUT_DIR = "/data/xml/";

    public static void main(String[] args) {

        XmlReportUtil xmlReportUtil = new XmlReportUtil();
        try {
            xmlReportUtil.generateReport(UID);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }


    public void generateReport(String uid) throws Exception {
        ApplicationContext context = SpringUtils.getContext();
        List <ExtendedTaxonConceptDTO> etcList = new ArrayList<ExtendedTaxonConceptDTO>();

        searchDao = (FulltextSearchDao) context.getBean(FulltextSearchDao.class);
        taxonConceptDao = (TaxonConceptDao) context.getBean(TaxonConceptDao.class);
        infosourceDao = (InfoSourceDAO) context.getBean("infoSourceDAO");

        uidInfosourceIdMap = infosourceDao.getInfosourceIdUidMap();

        SearchResultsDTO<SearchDTO> stcs = searchDao.findByUid(IndexedTypes.TAXON, uid, null, 0, 5000, "score", "asc");
        if(stcs.getTotalRecords()>0){

            List<SearchDTO> stcsResults = stcs.getResults();
            //            System.out.println("Result number: " + stcs.getTotalRecords() + " " + stcsResults.size());

            for (SearchDTO st : stcsResults) {

                //                System.out.println(st.getGuid());

                ExtendedTaxonConceptDTO etc = taxonConceptDao.getExtendedTaxonConceptByGuid(st.getGuid());
                etcList.add(etc);

                //                System.out.println("SCI NAME:" + sciName);
            }
        }
        Writer writer = getWriter(uid);

        //        String xmlStr = buildXmlReport(etcList, uid).toString();

        //        System.out.println(xmlStr);

        buildXmlReport(etcList, uid, writer);

        writer.flush();
        writer.close();
    }

    public void buildXmlReport(List <ExtendedTaxonConceptDTO> etcList, String uid, Writer writer) throws IOException {

        //        StringBuffer xmlStrBuf = new StringBuffer();
        writer.write(XML_HEADER);

        for (ExtendedTaxonConceptDTO etc : etcList) {
            ExtendedTaxonConceptDTO dedicatedEtc = refineEtc(etc, uid);

            if (dedicatedEtc.getImages().size() > 0 || dedicatedEtc.getScreenshotImages().size() > 0) {
                writer.write("<taxon>");
                writer.write('\n');
                writer.write("<dc:identifier>" + dedicatedEtc.getTaxonConcept().getGuid() + "</dc:identifier>");
                writer.write('\n');
                writer.write("<dc:source>" + dedicatedEtc.getTaxonConcept().getIdentifier() + "</dc:source>");
                writer.write('\n');
                writer.write("<dwc:Kingdom>" + dedicatedEtc.getClassification().getKingdom() + "</dwc:Kingdom>");
                writer.write('\n');
                writer.write("<dwc:Class>" + dedicatedEtc.getClassification().getClazz() + "</dwc:Class>");
                writer.write('\n');
                writer.write("<dwc:Order>" + dedicatedEtc.getClassification().getOrder() + "</dwc:Order>");
                writer.write('\n');
                writer.write("<dwc:Family>" + dedicatedEtc.getClassification().getFamily() + "</dwc:Family>");
                writer.write('\n');
                writer.write("<dwc:ScientificName>" + dedicatedEtc.getClassification().getScientificName() + "</dwc:ScientificName>");

                if (dedicatedEtc.getCommonNames().size() > 0) {
                    for (CommonName commonName : dedicatedEtc.getCommonNames()) {
                        writer.write("<commonName xml:lang=\"en\">" + URLEncoder.encode(commonName.getNameString(), "UTF8").replaceAll("\\+", " ") + "</commonName>");
                        writer.write('\n');
                    }
                } else {
                    writer.write("<commonName xml:lang=\"en\"></commonName>");
                    writer.write('\n');
                }

                writer.write("<dcterms:created></dcterms:created>");
                writer.write('\n');
                writer.write("<dcterms:modified></dcterms:modified>");
                writer.write('\n');

                List<Image> imageObjects = dedicatedEtc.getImages();
                imageObjects.addAll(dedicatedEtc.getScreenshotImages());
                
                for (Image image : imageObjects) {
                    writer.write("<dataObject>");
                    writer.write('\n');
                    writer.write("<dc:identifier>" + image.getIdentifier() + "</dc:identifier>");
                    writer.write('\n');
                    writer.write("<dataType>http://purl.org/dc/dcmitype/StillImage</dataType>");
                    writer.write('\n');
                    writer.write("<mimeType>image/jpeg</mimeType>");
                    writer.write('\n');
                    writer.write("<agent role=\"photographer\" homepage=\"" + image.getInfoSourceURL() +"\">" + encode(image.getCreator() != null ? image.getCreator() : "") + "</agent>");
                    writer.write('\n');
                    writer.write("<agent role=\"author\" homepage=\"" + image.getInfoSourceURL() +"\">" + encode(image.getCreator() != null ? image.getCreator() : "") + "</agent>");
                    writer.write('\n');
                    writer.write("<dcterms:created></dcterms:created>");
                    writer.write('\n');
                    writer.write("<dc:title xml:lang=\"en\">" + encode(image.getTitle() != null ? image.getTitle() : "") + "</dc:title>");
                    writer.write('\n');
                    writer.write("<license>" + image.getLicence() + "</license>");
                    writer.write('\n');
                    writer.write("<dcterms:rightsHolder>" + encode(image.getCreator() != null ? image.getCreator() : "") + "</dcterms:rightsHolder>");
                    writer.write('\n');
                    writer.write("<audience>General public</audience>");
                    writer.write('\n');
                    writer.write("<audience>Children</audience>");
                    writer.write('\n');
                    writer.write("<dc:description xml:lang=\"en\">" + encode(image.getDescription() != null ? image.getDescription() : "") + "</dc:description>");
                    writer.write('\n');
                    writer.write("<mediaURL>" + image.getRepoLocation() + "</mediaURL>");
                    writer.write('\n');
                    writer.write("<thumbnailURL>" + image.getThumbnail().replaceAll("thumbnail", "smallRaw") + "</thumbnailURL>");
                    writer.write('\n');
                    writer.write("<location>" + encode(image.getLocality() != null ? image.getLocality() : "") + "</location>");
                    writer.write('\n');
                    writer.write("</dataObject>");
                    writer.write('\n');
                }

                writer.write("</taxon>");
                writer.write('\n');
            }
        }

        writer.write(XML_FOOTER);
        //        return xmlStrBuf;

    }

    private String encode(String str) {
        str = str.replaceAll("", "");
        return str.replaceAll("\\&", "&amp;");
    }

    private ExtendedTaxonConceptDTO refineEtc(ExtendedTaxonConceptDTO etc, String uid) {
        ExtendedTaxonConceptDTO dedicatedEtc = new ExtendedTaxonConceptDTO();
        boolean gotIdentifier = false;

        TaxonConcept taxonConcept = etc.getTaxonConcept();

        List<Image> images = new ArrayList<Image>();

        for (Image image : etc.getImages()) {
            if (uid.equals(uidInfosourceIdMap.get(image.getInfoSourceId()))) {
                images.add(image);
                if (image.getIdentifier() != null) {
                    taxonConcept.setIdentifier(image.getIdentifier());
                    gotIdentifier = true;
                }
            }
        }

        dedicatedEtc.setImages(images);

        List<Image> distributionImages = new ArrayList<Image>();

        for (Image distributionImage : etc.getDistributionImages()) {
            if (uid.equals(uidInfosourceIdMap.get(distributionImage.getInfoSourceId()))) {
                distributionImages.add(distributionImage);
                if (distributionImage.getIdentifier() != null && !gotIdentifier) {
                    taxonConcept.setIdentifier(distributionImage.getIdentifier());
                    gotIdentifier = true;
                }
            }
        }

        dedicatedEtc.setDistributionImages(distributionImages);

        List<Image> screenshotImages = new ArrayList<Image>();

        for (Image screenshotImage : etc.getScreenshotImages()) {
            if (uid.equals(uidInfosourceIdMap.get(screenshotImage.getInfoSourceId()))) {
                screenshotImages.add(screenshotImage);
                if (screenshotImage.getIdentifier() != null && !gotIdentifier) {
                    taxonConcept.setIdentifier(screenshotImage.getIdentifier());
                    gotIdentifier = true;
                }
            }
        }

        dedicatedEtc.setScreenshotImages(screenshotImages);

        List<TaxonConcept> synonyms = new ArrayList<TaxonConcept>();

        for (TaxonConcept synonym : etc.getSynonyms()) {
            if (uid.equals(uidInfosourceIdMap.get(synonym.getInfoSourceId()))) {
                synonyms.add(synonym);
                if (synonym.getIdentifier() != null && !gotIdentifier) {
                    taxonConcept.setIdentifier(synonym.getIdentifier());
                    gotIdentifier = true;
                }
            }
        }

        dedicatedEtc.setSynonyms(synonyms);

        List<CommonName> commonNames = new ArrayList<CommonName>();

        for (CommonName commonName : etc.getCommonNames()) {
            if (uid.equals(uidInfosourceIdMap.get(commonName.getInfoSourceId()))) {
                commonNames.add(commonName);
                if (commonName.getIdentifier() != null && !gotIdentifier) {
                    taxonConcept.setIdentifier(commonName.getIdentifier());
                    gotIdentifier = true;
                }
            }
        }

        dedicatedEtc.setCommonNames(commonNames);

        List<PestStatus> pestStatuses = new ArrayList<PestStatus>();

        for (PestStatus pestStatus : etc.getPestStatuses()) {
            if (uid.equals(uidInfosourceIdMap.get(pestStatus.getInfoSourceId()))) {
                pestStatuses.add(pestStatus);
                if (pestStatus.getIdentifier() != null && !gotIdentifier) {
                    taxonConcept.setIdentifier(pestStatus.getIdentifier());
                    gotIdentifier = true;
                }
            }
        }

        dedicatedEtc.setPestStatuses(pestStatuses);

        List<ExtantStatus> extantStatuses = new ArrayList<ExtantStatus>();

        for (ExtantStatus extantStatus : etc.getExtantStatuses()) {
            if (uid.equals(uidInfosourceIdMap.get(extantStatus.getInfoSourceId()))) {
                extantStatuses.add(extantStatus);
                if (extantStatus.getIdentifier() != null && !gotIdentifier) {
                    taxonConcept.setIdentifier(extantStatus.getIdentifier());
                    gotIdentifier = true;
                }
            }
        }

        dedicatedEtc.setExtantStatuses(extantStatuses);

        List<Habitat> habitats = new ArrayList<Habitat>();

        for (Habitat habitat : etc.getHabitats()) {
            if (uid.equals(uidInfosourceIdMap.get(habitat.getInfoSourceId()))) {
                habitats.add(habitat);
                if (habitat.getIdentifier() != null && !gotIdentifier) {
                    taxonConcept.setIdentifier(habitat.getIdentifier());
                    gotIdentifier = true;
                }
            }
        }

        dedicatedEtc.setHabitats(habitats);

        dedicatedEtc.setTaxonConcept(taxonConcept);
        dedicatedEtc.setTaxonName(etc.getTaxonName());
        dedicatedEtc.setClassification(etc.getClassification());
        dedicatedEtc.setIdentifiers(etc.getIdentifiers());

        dedicatedEtc = fixRepoUrls(dedicatedEtc);

        return dedicatedEtc;
    }

    /**
     * Fix the repository URLs
     * 
     * @param searchConceptDTO
     * @return
     */
    public ExtendedTaxonConceptDTO fixRepoUrls(ExtendedTaxonConceptDTO taxonConceptDTO){
        List<Image> images = taxonConceptDTO.getImages();
        if(images!=null){
            for(Image image: images){
                fixRepoUrls(image);
            }
        }
        images = taxonConceptDTO.getDistributionImages();
        if(images!=null){
            for(Image image: images){
                fixRepoUrls(image);
            }
        }
        images = taxonConceptDTO.getScreenshotImages();
        if(images!=null){
            for(Image image: images){
                fixRepoUrls(image);
            }
        }
        return taxonConceptDTO;
    }

    /**
     * Fix URLS for images.
     * 
     * @param image
     */
    public Image fixRepoUrls(Image image) {
        String imageLocation = image.getRepoLocation();

        if(imageLocation!=null && imageLocation.contains(repositoryPath)){
            imageLocation = fixSingleUrl(imageLocation);
            image.setRepoLocation(imageLocation);
        }

        int lastFileSep = imageLocation.lastIndexOf(File.separatorChar);
        if(lastFileSep < 0){
            lastFileSep = imageLocation.lastIndexOf("/");
        }
        String baseUrl = imageLocation.substring(0, lastFileSep+1);
        String fileName = imageLocation.substring(lastFileSep+1);
        String extension = FilenameUtils.getExtension(fileName);
        String thumbnail = baseUrl + "thumbnail"+ "." + extension;

        //set the thumbnail location and DC path
        image.setDcLocation(baseUrl + FileType.DC.getFilename());
        image.setThumbnail(thumbnail);
        return image;
    }

    /**
     * Fix the supplied URL
     * 
     * @param thumbnail
     * @return
     */
    public String fixSingleUrl(String thumbnail) {
        String url = thumbnail.replace(repositoryPath, repositoryUrl);
        return url;
    }

    public Writer getWriter(String uid) throws IOException {
        String dataDir = OUTPUT_DIR;
        FileUtils.forceMkdir(new File(dataDir));
        File outputFile = new File(dataDir + uid + ".xml");
        if(outputFile.exists())
            FileUtils.forceDelete(outputFile);
        return new FileWriter(outputFile);
    }
}
