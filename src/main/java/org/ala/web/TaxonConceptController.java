/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 *
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/
package org.ala.web;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedImageAdapter;

import org.ala.dao.DocumentDAO;
import org.ala.dao.InfoSourceDAO;
import org.ala.dao.TaxonConceptDao;
import org.ala.dao.TaxonConceptDaoImplSolr;
import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.dto.SearchResultsDTO;
import org.ala.dto.SearchTaxonConceptDTO;
import org.ala.model.Document;
import org.ala.model.InfoSource;
import org.ala.repository.Predicates;
import org.ala.util.MimeType;
import org.ala.util.RepositoryFileUtils;
import org.ala.util.StatusType;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Main controller for the BIE site
 *
 * TODO: If this class gets too big or complex then split into mulitple Controllers.
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
@Controller("taxonConceptController")
public class TaxonConceptController {

	/** Logger initialisation */
    private final static Logger logger = Logger.getLogger(TaxonConceptController.class);
    /** DAO bean for access to taxon concepts */
    @Inject
    private TaxonConceptDao taxonConceptDao;
    /** DAO bean for access to repository document table */
    @Inject
    private DocumentDAO documentDAO;
    /** DAO bean for SOLR search queries */
    @Inject
    private TaxonConceptDaoImplSolr tcDaoSolr;
    /** DAO bean for access to info sources */
    @Inject
    private InfoSourceDAO infoSourceDAO;
    /** Name of view for site home page */
    private String HOME_PAGE = "homePage";
    /** Name of view for search page */
    private final String SPECIES_SEARCH = "speciesSearchForm";
    /** Name of view for list of taxa */
    private final String SPECIES_LIST = "speciesList";
    /** Name of view for a single taxon */
    private final String SPECIES_SHOW = "speciesShow";
    /** Name of view for list of pest/conservation status */
    private final String STATUS_LIST = "statusList";
    /** Name of view for list of datasets */
    private final String DATASET_LIST = "datasetList";
    
    /**
	 * Custom handler for the welcome view.
	 * <p>
	 * Note that this handler relies on the RequestToViewNameTranslator to
	 * determine the logical view name based on the request URL: "/welcome.do"
	 * -&gt; "welcome".
     *
     * @return viewname to render
     */
	@RequestMapping("/")
	public String homePageHandler() {
		return HOME_PAGE;
	}

    /**
     * Default method for Controller
     *
     * @return mav
     */
    @RequestMapping(value = "/species", method = RequestMethod.GET)
    public ModelAndView listSpecies() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(SPECIES_LIST);
        mav.addObject("message", "Results list for search goes here. (TODO)");
        return mav;
    }

    /**
     * Map to a /search URI - perform a full-text SOLR search
     * Note: adding .json to URL will result in JSON output and
     * adding .xml will result in XML output.
     *
     * @param query
     * @param filterQuery
     * @param startIndex
     * @param pageSize
     * @param sortField
     * @param sortDirection
     * @param model
     * @return view name
     */
    @RequestMapping(value = "/species/search*", method = RequestMethod.GET)
    public String solrSearchSpecies(
            @RequestParam(value="q", required=false) String query,
            @RequestParam(value="fq", required=false) String filterQuery,
            @RequestParam(value="startIndex", required=false, defaultValue="0") Integer startIndex,
            @RequestParam(value="results", required=false, defaultValue ="10") Integer pageSize,
            @RequestParam(value="sort", required=false, defaultValue="score") String sortField,
            @RequestParam(value="dir", required=false, defaultValue ="asc") String sortDirection,
            @RequestParam(value="title", required=false, defaultValue ="Species Search Results") String title,
            Model model) throws Exception {
        if (query == null) {
            return SPECIES_SEARCH;
        }

        String queryJsEscaped = StringEscapeUtils.escapeJavaScript(query);
        model.addAttribute("query", query);
        model.addAttribute("queryJsEscaped", queryJsEscaped);
        model.addAttribute("title", StringEscapeUtils.escapeJavaScript(title));
        String filterQueryChecked = (filterQuery == null) ? "" : filterQuery;
        model.addAttribute("facetQuery", filterQueryChecked);

        //TaxonConceptDaoImplSolr tcDao = new TaxonConceptDaoImplSolr();

        SearchResultsDTO searchResults = tcDaoSolr.findByScientificName(query, filterQuery, startIndex, pageSize, sortField, sortDirection);
        model.addAttribute("searchResults", searchResults);
        logger.debug("query = "+query);

        if (searchResults.getTaxonConcepts().size() == 1) {
            List taxonConcepts = (List) searchResults.getTaxonConcepts();
            SearchTaxonConceptDTO res = (SearchTaxonConceptDTO) taxonConcepts.get(0);
            String guid = res.getGuid();
            //return "redirect:/species/" + guid;
        }

        return SPECIES_LIST;
    }

    /**
     * Map to a /{guid} URI.
     * E.g. /species/urn:lsid:biodiversity.org.au:afd.taxon:a402d4c8-db51-4ad9-a72a-0e912ae7bc9a
     * 
     * @param guid
     * @param model
     * @return view name
     */ 
    @RequestMapping(value = "/species/{guid}", method = RequestMethod.GET)
    public String showSpecies(@PathVariable("guid") String guid, Model model) throws Exception {
        logger.debug("Retrieving concept with guid: "+guid+".");
        model.addAttribute("extendedTaxonConcept", taxonConceptDao.getExtendedTaxonConceptByGuid(guid));
        return SPECIES_SHOW;
    }

    /**
     * JSON output for TC guid
     *
     * @param guid
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/species/{guid}.json", method = RequestMethod.GET)
    public ExtendedTaxonConceptDTO showSpeciesJson(@PathVariable("guid") String guid) throws Exception {
        logger.info("Retrieving concept with guid: "+guid);
        return taxonConceptDao.getExtendedTaxonConceptByGuid(guid);
    }

    /**
     * JSON web service (AJAX) to return details for a repository document
     *
     * @param documentId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/species/document/{documentId}.json", method = RequestMethod.GET)
    public Document getDocumentDetails(@PathVariable("documentId") int documentId) throws Exception {
        Document doc = documentDAO.getById(documentId);

        if (doc != null) {
            // augment data with title from reading dc file
            String fileName = doc.getFilePath()+"/dc";
            RepositoryFileUtils repoUtils = new RepositoryFileUtils();
            List<String[]> lines = repoUtils.readRepositoryFile(fileName);
            //System.err.println("docId:"+documentId+"|filename:"+fileName);
            for (String[] line : lines) {
                // get the dc.title value
                if (line[0].endsWith(Predicates.DC_TITLE.getLocalPart())) {
                    doc.setTitle(line[1]);
                } else if (line[0].endsWith(Predicates.DC_IDENTIFIER.getLocalPart())) {
                    doc.setIdentifier(line[1]);
                }
            }
        }

        return doc;
    }

    /**
     *
     * @param documentId
     * @param scale
     * @param square
     * @param outputStream
     * @throws IOException
     */
    @RequestMapping(value="/species/images/{documentId}.jpg", method = RequestMethod.GET)
	public void thumbnailHandler(@PathVariable("documentId") int documentId, 
            @RequestParam(value="scale", required=false, defaultValue ="100") Integer scale,
            @RequestParam(value="square", required=false, defaultValue ="true") Boolean square,
            OutputStream outputStream) throws IOException {
		Document doc = documentDAO.getById(documentId);

        if (doc != null) {
            // augment data with title from reading dc file
            String fileName = doc.getFilePath()+"/raw"+MimeType.getForMimeType(doc.getMimeType()).getFileExtension();
            BufferedImage original = ImageIO.read(new File(fileName));

            //BufferedImage scaled = jaiScaleImage(original, scale, square,
            //    Interpolation.getInstance(Interpolation.INTERP_BICUBIC_2));

            BufferedImage scaled = awtScaleImage(original, scale, Image.SCALE_SMOOTH);

            if (square) {
                scaled = cropImage(scaled, scale);
            }
            
            ImageIO.write(scaled, "jpg", outputStream);
        }

	}

    /**
     * Pest / Conservation status list
     *
     * @param statusStr
     * @param filterQuery 
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/species/status/{status}", method = RequestMethod.GET)
    public String listStatus(
            @PathVariable("status") String statusStr,
            @RequestParam(value="fq", required=false) String filterQuery,
            Model model) throws Exception {
        StatusType statusType = StatusType.getForStatusType(statusStr);
        if (statusType==null) {
            return "redirect:/error.jsp";
        }
        model.addAttribute("statusType", statusType);
        model.addAttribute("filterQuery", filterQuery);
        SearchResultsDTO searchResults = tcDaoSolr.findAllByStatus(statusType, filterQuery,  0, 10, "score", "asc");// findByScientificName(query, startIndex, pageSize, sortField, sortDirection);
        model.addAttribute("searchResults", searchResults);
        return STATUS_LIST;
    }

    /**
     * Pest / Conservation status JSON (for yui datatable)
     *
     * @param statusStr
     * @param filterQuery 
     * @param startIndex
     * @param pageSize
     * @param sortField
     * @param sortDirection
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/species/status/{status}.json", method = RequestMethod.GET)
    public SearchResultsDTO listStatusJson(@PathVariable("status") String statusStr,
            @RequestParam(value="fq", required=false) String filterQuery,
            @RequestParam(value="startIndex", required=false, defaultValue="0") Integer startIndex,
            @RequestParam(value="results", required=false, defaultValue ="10") Integer pageSize,
            @RequestParam(value="sort", required=false, defaultValue="score") String sortField,
            @RequestParam(value="dir", required=false, defaultValue ="asc") String sortDirection,
            Model model) throws Exception {

        StatusType statusType = StatusType.getForStatusType(statusStr);
        SearchResultsDTO searchResults = null;

        if (statusType!=null) {
            searchResults = tcDaoSolr.findAllByStatus(statusType, filterQuery, startIndex, pageSize, sortField, sortDirection);// findByScientificName(query, startIndex, pageSize, sortField, sortDirection);
        }
        
        return searchResults;
    }

    /**
     * List of data sets
     *
     * @param model
     * @return view name
     */
    @RequestMapping(value = "/species/datasets", method = RequestMethod.GET)
    public String listDatasets (Model model)  {
        List<InfoSource> infoSources = infoSourceDAO.getAllByDatasetType();
        model.addAttribute("infoSources", infoSources);

        return DATASET_LIST;
    }

    /**
     * Thumbnail image generator, taken from
     * http://www.hanhuy.com/pfn/java-image-thumbnail-comparison;jsessionid=ED2CFDFF9B3A32CB89F1A15656902B44?page=2
     *
     * @param image
     * @param maxSize
     * @param interp
     * @return
     */
    protected BufferedImage jaiScaleImage(BufferedImage image, int maxSize, boolean square, Interpolation interp) {
        //System.out.println("JAI Scaling image to: " + maxSize);
        PlanarImage pi = new RenderedImageAdapter(image);
        int w = pi.getWidth();
        int h = pi.getHeight();
        float ratio = 0f;
        float scaleFactor = 1.0f;
        float cropBy = 0f; //maxSize / 2;
        float cropX = 0f;
        float cropY = 0f;

        if (w > h) {
            scaleFactor = ((float) maxSize / (float) w);
            int tmp = (int) (maxSize / ((float) w / (float) h));
            cropBy = (float) tmp; // forces rounding down of float
            int croptTmp = (int) ((float) (w * scaleFactor) - cropBy) / 2;
            cropX = (float) croptTmp;
            System.out.println("cropBy: "+cropBy+"|"+w+"|"+cropX);
        } else {
            scaleFactor = ((float) maxSize / (float) h);
            int tmp = (int) (maxSize / ((float) h / (float) w));
            cropBy = (float) tmp; // forces rounding down of float
            int croptTmp = (int) ((float) (h * scaleFactor) - cropBy) / 2;
            cropY = (float) croptTmp;
        }

        // scale image
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(pi);
        pb.add(scaleFactor);
        pb.add(scaleFactor);
        pb.add(0f);
        pb.add(0f);
        pb.add(interp);
        pi = JAI.create("scale", pb);
        
        if (square) {
            // crop image to a square
            int w1 = pi.getWidth();
            int h1 = pi.getHeight();
            ParameterBlockJAI params = new ParameterBlockJAI("crop");
            params.addSource(pi);
            params.setParameter("x", cropX);
            params.setParameter("y", cropY); // new Integer(pi.getMinY()).floatValue()
            params.setParameter("width", cropBy);
            params.setParameter("height", cropBy);
            pi = JAI.create("crop",params);
        }
        
        return pi.getAsBufferedImage();
    }

    /**
     * Thumbnail image generator, taken from
     * http://www.hanhuy.com/pfn/java-image-thumbnail-comparison;jsessionid=ED2CFDFF9B3A32CB89F1A15656902B44?page=2
     *
     * @param image
     * @param maxSize
     * @param hint
     * @return
     */
    BufferedImage awtScaleImage(BufferedImage image, int maxSize, int hint) {
        //System.out.println("AWT Scaling image to: " + maxSize);
        int w = image.getWidth();
        int h = image.getHeight();
        float scaleFactor = 1.0f;
        
        if (w > h) {
            scaleFactor = ((float) maxSize / (float) h);
        } else {
            scaleFactor = ((float) maxSize / (float) w);
        }
        
        w = (int)(w * scaleFactor);
        h = (int)(h * scaleFactor);
        // since this code can run both headless and in a graphics context
        // we will just create a standard rgb image here and take the
        // performance hit in a non-compatible image format if any
        Image i = image.getScaledInstance(w, h, hint);
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.drawImage(i, null, null);
        g.dispose();
        i.flush();
        return image;
    }

    /**
     * Crop image to a square (Flickr-style)
     *
     * @param scaled
     * @param scale
     * @return
     */
    private BufferedImage cropImage(BufferedImage scaled, Integer scale) {
        // crop image to a square
        PlanarImage pi = new RenderedImageAdapter(scaled);
        int w = pi.getWidth();
        int h = pi.getHeight();
        float ratio = 0f;
        float scaleFactor = 1.0f;
        float cropBy = 0f; 
        float cropX = 0f;
        float cropY = 0f;
        if (w > h) {
            scaleFactor = ((float) scale / (float) w);
            int tmp = (int) (scale / ((float) w / (float) h));
            cropBy = (float) tmp; // forces rounding down of float
            int croptTmp = (int) ((float) (w * scaleFactor) - cropBy) / 2;
            cropX = (float) croptTmp;
            System.out.println("cropBy: " + cropBy + "|" + w + "|" + cropX);
        } else {
            scaleFactor = ((float) scale / (float) h);
            int tmp = (int) (scale / ((float) h / (float) w));
            cropBy = (float) tmp; // forces rounding down of float
            int croptTmp = (int) ((float) (h * scaleFactor) - cropBy) / 2;
            cropY = (float) croptTmp;
        }
        ParameterBlockJAI params = new ParameterBlockJAI("crop");
        params.addSource(pi);
        params.setParameter("x", cropX);
        params.setParameter("y", cropY); // new Integer(pi.getMinY()).floatValue()
        params.setParameter("width", (float) scale);
        params.setParameter("height", (float) scale);
        pi = JAI.create("crop", params);
        BufferedImage scaled2 = pi.getAsBufferedImage();

//        ParameterBlock pb = new ParameterBlock();
//        pb.addSource(pi);
//        pb.add(cropX);
//        pb.add(cropY);
//        pb.add((float) scale);
//        pb.add((float) scale);
//        //pb.add(interp);
//        pi = JAI.create("crop", pb);
//        BufferedImage scaled2 = pi.getAsBufferedImage();
        return scaled2;
    }

	/**
	 * @param taxonConceptDao the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}
}
