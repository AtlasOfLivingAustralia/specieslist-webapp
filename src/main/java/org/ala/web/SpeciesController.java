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

import atg.taglib.json.util.JSONObject;
import java.io.IOException;
import java.io.OutputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ala.dao.DocumentDAO;
import org.ala.dao.FulltextSearchDao;
import org.ala.dao.IndexedTypes;
import org.ala.dao.TaxonConceptDao;
import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.dto.SearchDTO;
import org.ala.dto.SearchResultsDTO;
import org.ala.dto.SearchTaxonConceptDTO;
import org.ala.model.AttributableObject;
import org.ala.model.CommonName;
import org.ala.model.ConservationStatus;
import org.ala.model.Document;
import org.ala.model.ExtantStatus;
import org.ala.model.Habitat;
import org.ala.model.Image;
import org.ala.model.PestStatus;
import org.ala.model.Reference;
import org.ala.model.SimpleProperty;
import org.ala.model.TaxonConcept;
import org.ala.repository.Predicates;
import org.ala.util.ImageUtils;
import org.ala.util.MimeType;
import org.ala.util.RepositoryFileUtils;
import org.ala.util.StatusType;
import org.ala.util.WebUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Main controller for the BIE site
 *
 * TODO: If this class gets too big or complex then split into multiple Controllers.
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
@Controller("speciesController")
public class SpeciesController {

	/** Logger initialisation */
	private final static Logger logger = Logger.getLogger(SpeciesController.class);
	/** DAO bean for access to taxon concepts */
	@Inject
	private TaxonConceptDao taxonConceptDao;
	/** DAO bean for access to repository document table */
	@Inject
	private DocumentDAO documentDAO;
	/** DAO bean for SOLR search queries */
	@Inject
	private FulltextSearchDao searchDao;
	/** Name of view for site home page */
	private String HOME_PAGE = "homePage";
	/** Name of view for a single taxon */
	private final String SPECIES_SHOW = "species/show";
	/** Name of view for a single taxon */
	private final String SPECIES_SHOW_BRIEF = "species/showBrief";	
    /** Name of view for a taxon error page */
	private final String SPECIES_ERROR = "species/error";
	/** Name of view for list of pest/conservation status */
	private final String STATUS_LIST = "species/statusList";
	@Inject
	protected RepoUrlUtils repoUrlUtils;
    /** The set of data sources that will not be truncated **/
    protected Set<String> nonTruncatedSources = new java.util.HashSet<String>();
    /** The set of data sources that have low priority (ie displayed at the end of the list or removed if other sources available) **/
    protected Set<String> lowPrioritySources = new java.util.HashSet<String>();
    /** The URI for JSON data for static occurrence map */
    private final String SPATIAL_JSON_URL = "http://spatial.ala.org.au/alaspatial/ws/density/map?species_lsid=";
    /** Max width to Truncate text to */
    private int TEXT_MAX_WIDTH = 200;

    public SpeciesController(){
        nonTruncatedSources.add("http://www.environment.gov.au/biodiversity/abrs/online-resources/flora/main/index.html");
        lowPrioritySources.add("http://www.ozanimals.com/");
        lowPrioritySources.add("http://en.wikipedia.org/");
        //lowPrioritySources.add("http://plantnet.rbgsyd.nsw.gov.au/floraonline.htm");
    }
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
	 * Map to a /{guid} URI.
	 * E.g. /species/urn:lsid:biodiversity.org.au:afd.taxon:a402d4c8-db51-4ad9-a72a-0e912ae7bc9a
	 * 
	 * @param guid
	 * @param model
	 * @return view name
	 * @throws Exception
	 */ 
	@RequestMapping(value = "/species/charts/{guid:.+}*", method = RequestMethod.GET)
	public String showChartInfo(@PathVariable("guid") String guid,
            HttpServletResponse response) throws Exception {
		String contentAsString = WebUtils.getUrlContentAsString("http://biocache.ala.org.au/occurrences/searchByTaxon.json?q="+guid);
		response.setContentType("application/json");
		response.getWriter().write(contentAsString);
		return null;
	}

	private List<Image> removeBlackListedImageItem(List<Image> images){
		List<Image> il = new ArrayList<Image>();
		
		for(Image image : images){
			if(!image.getIsBlackListed()){
				il.add(image);
			}
		}
		return il;
	}
	
    /**
     * Get the list of collections, institutes, data resources and data providers that have specimens for the supplied taxon concept guid
     * Wrapper around the biocache service: http://biocache.ala.org.au/occurrences/sourceByTaxon
     * @param guid
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/species/source/{guid:.+}*", method = RequestMethod.GET)
    public void showSourceInfo(@PathVariable("guid") String guid,
            HttpServletResponse response) throws Exception {
        String contentAsString = WebUtils.getUrlContentAsString("http://biocache.ala.org.au/occurrences/sourceByTaxon/" +guid +".json?fq=basis_of_record:specimen");
        response.setContentType("application/json");
        response.getWriter().write(contentAsString);
    }

	/**
	 * Map to a /{guid} URI.
	 * E.g. /species/urn:lsid:biodiversity.org.au:afd.taxon:a402d4c8-db51-4ad9-a72a-0e912ae7bc9a
	 * 
	 * @param guid
	 * @param model
	 * @return view name
	 * @throws Exception
	 */ 
	@RequestMapping(value = "/species/{guid:.+}", method = RequestMethod.GET)
	public String showSpecies(
            @PathVariable("guid") String guid,
            @RequestParam(value="conceptName", defaultValue ="", required=false) String conceptName,
            HttpServletRequest request,
            Model model) throws Exception {
		logger.debug("Displaying page for: " + guid +" .....");
        ExtendedTaxonConceptDTO etc = taxonConceptDao.getExtendedTaxonConceptByGuid(guid);
        //remove blackListed image...
        etc.setImages(removeBlackListedImageItem(etc.getImages()));        
        if (request.getRemoteUser() != null && request.isUserInRole("ROLE_ADMIN")) {
        	model.addAttribute("isRoleAdmin", true);
        }
        else{
        	model.addAttribute("isRoleAdmin", false);
        }
        
        if (etc.getTaxonConcept() == null || etc.getTaxonConcept().getGuid() == null) {
            model.addAttribute("errorMessage", "The requested taxon was not found: "+conceptName+" ("+ guid+")");
            return SPECIES_ERROR;
        }

        //construct the scientific name and authorship
        String[] sciAndAuthor = getScientificNameAndAuthorship(etc);
        model.addAttribute("scientificName", sciAndAuthor[0]);
        model.addAttribute("authorship", sciAndAuthor[1]);
        
        //common name with many infosources
        List<CommonName> names = fixCommonNames(etc.getCommonNames()); // remove duplicate names
        Map<String, List<CommonName>> namesMap = sortCommonNameSources(names);
        String[] keyArray = namesMap.keySet().toArray(new String[0]);
        Arrays.sort(keyArray, String.CASE_INSENSITIVE_ORDER);
        model.addAttribute("sortCommonNameSources", namesMap);
        model.addAttribute("sortCommonNameKeys", keyArray);
        
        etc.setCommonNames(names); 
        etc.setImages(addImageDocIds(etc.getImages()));
        model.addAttribute("extendedTaxonConcept", repoUrlUtils.fixRepoUrls(etc));
		model.addAttribute("commonNames", getCommonNamesString(etc));
		model.addAttribute("textProperties", filterSimpleProperties(etc));
        model.addAttribute("infoSources", getInfoSource(etc));
        
        //retrieve cookies indicating user has ranked
        List<StoredRanking> srs = RankingCookieUtils.getRankedImageUris(request.getCookies(), guid);
        //create a list of URLs
        List<String> rankedUris = new ArrayList<String>();
        Map<String, Boolean> rankingMap = new HashMap<String, Boolean>();
        for(StoredRanking sr : srs){
        	rankedUris.add(sr.getUri());
        	rankingMap.put(sr.getUri(), sr.isPositive());
        }

        TaxonConcept tc = etc.getTaxonConcept();
        
        //load the hierarchy
        if(tc.getLeft()!=null){
        	SearchResultsDTO<SearchTaxonConceptDTO> taxonHierarchy = searchDao.getClassificationByLeftNS(tc.getLeft());
        	model.addAttribute("taxonHierarchy", taxonHierarchy.getResults());
        }
        
        //load child concept using search indexes
        List<SearchTaxonConceptDTO> childConcepts = searchDao.getChildConceptsParentId(Integer.toString(tc.getId()));
        //Reorder the children concepts so that ordering is based on rank followed by name
        //TODO: Currently this is being performed here instead of in the DAO incase somewhere relies on the default order.  We may need to move this
        Collections.sort(childConcepts, new TaxonRankNameComparator());

        model.addAttribute("childConcepts", childConcepts);
        
        //create a map
        model.addAttribute("rankedImageUris", rankedUris);
        model.addAttribute("rankedImageUriMap", rankingMap);
        // add map for conservation status regions to sections in the WP page describing them (http://test.ala.org.au/threatened-species-codes/#International)
        model.addAttribute("statusRegionMap", statusRegionMap());
        // get static occurrence map from spatial portal via JSON lookup
        model.addAttribute("spatialPortalMap", getSpatialPortalMap(etc.getTaxonConcept().getGuid()));
        logger.debug("Returning page view for: " + guid +" .....");
		return SPECIES_SHOW;
	}
	
	/**
	 * FIXME it should be possible to factor this out at some point
	 * 
	 * @param etc
	 * @return
	 */
	private String[] getScientificNameAndAuthorship(ExtendedTaxonConceptDTO etc) {
		
		String[] parts = new String[2];
		//for AFD, and CoL this is fine
		parts[0] = etc.getTaxonConcept().getNameString();
		parts[1] = etc.getTaxonConcept().getAuthor();
		
		//for APNI, APC...
		if(etc.getTaxonConcept().getGuid()!=null && etc.getTaxonConcept().getGuid().contains(":apni.taxon:") && etc.getTaxonName()!=null){
			parts[0] = etc.getTaxonName().getNameComplete();
			parts[1] = etc.getTaxonName().getAuthorship();
		}
		return parts;
	}
	/**
	 * Map to a /{guid}.json or /{guid}.xml URI.
	 * E.g. /species/urn:lsid:biodiversity.org.au:afd.taxon:a402d4c8-db51-4ad9-a72a-0e912ae7bc9a
	 * 
	 * @param guid
	 * @param model
	 * @return view name
	 * @throws Exception
	 */ 
	@RequestMapping(value = {"/species/info/{guid}.json","/species/info/{guid}.xml"}, method = RequestMethod.GET)
	public SearchResultsDTO showInfoSpecies(
            @PathVariable("guid") String guid,
            @RequestParam(value="conceptName", defaultValue ="", required=false) String conceptName,
            Model model) throws Exception {
		
		SearchResultsDTO<SearchDTO> stcs = searchDao.findByName(IndexedTypes.TAXON, guid, null, 0, 1, "score", "asc");
        if(stcs.getTotalRecords()>0){
        	SearchTaxonConceptDTO st = (SearchTaxonConceptDTO) stcs.getResults().get(0);
        	model.addAttribute("taxonConcept", repoUrlUtils.fixRepoUrls(st));
        }
		return stcs;
	}
	
	/**
	 * Map to a /{guid}.json or /{guid}.xml URI.
	 * E.g. /species/urn:lsid:biodiversity.org.au:afd.taxon:a402d4c8-db51-4ad9-a72a-0e912ae7bc9a
	 * 
	 * @param guid
	 * @param model
	 * @return view name
	 * @throws Exception
	 */ 
	@RequestMapping(value = {"/species/moreInfo/{guid}.json","/species/moreInfo/{guid}.xml"}, method = RequestMethod.GET)
	public String showMoreInfoSpecies(
            @PathVariable("guid") String guid,
            @RequestParam(value="conceptName", defaultValue ="", required=false) String conceptName,
            Model model) throws Exception {
		guid = taxonConceptDao.getPreferredGuid(guid);
       	model.addAttribute("taxonConcept", taxonConceptDao.getByGuid(guid));
       	model.addAttribute("commonNames", taxonConceptDao.getCommonNamesFor(guid));
       	model.addAttribute("images", repoUrlUtils.fixRepoUrls(taxonConceptDao.getImages(guid)));
		return SPECIES_SHOW_BRIEF;
	}
	
	/**
	 * JSON output for TC guid
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = {"/species/{guid}.json","/species/{guid}.xml"}, method = RequestMethod.GET)
	public ExtendedTaxonConceptDTO showSpeciesJson(@PathVariable("guid") String guid) throws Exception {
		logger.info("Retrieving concept with guid: "+guid);
		return repoUrlUtils.fixRepoUrls(taxonConceptDao.getExtendedTaxonConceptByGuid(guid));
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
	 * Dynamically generate a scaled and/or square image
     * 
	 * @param documentId
	 * @param scale
	 * @param square
	 * @param outputStream
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value="/species/images/{documentId}.jpg", method = RequestMethod.GET)
	public void thumbnailHandler(@PathVariable("documentId") int documentId, 
			@RequestParam(value="scale", required=false, defaultValue ="1000") Integer scale,
			@RequestParam(value="square", required=false, defaultValue ="false") Boolean square,
			OutputStream outputStream,
			HttpServletResponse response) throws IOException {
		logger.debug("Requested image " + documentId + ".jpg ");
		Document doc = documentDAO.getById(documentId);
		if (doc != null) {
			// augment data with title from reading dc file
			MimeType mt = MimeType.getForMimeType(doc.getMimeType());
			String fileName = doc.getFilePath()+"/raw"+mt.getFileExtension();
            logger.debug("filename = "+fileName);
            ImageUtils iu = new ImageUtils();
            try {
            	logger.debug("Loading with image utils...");
                iu.load(fileName); // problem with Jetty 7.0.1
                logger.debug("Loaded");
                // create a square image (crops)
                if (square) {
                	logger.debug("Producing square images...");
                    iu.square();
                }
                // scale if original is bigger than "scale" pixels
                boolean success = iu.smoothThumbnail(scale);
                logger.debug("Thumbnailing success..."+success);
                if(success){
                    logger.debug("Thumbnailing success...");
	                response.setContentType(mt.getMimeType());
	                ImageIO.write(iu.getModifiedImage(), mt.name(), outputStream);
                } else {
                	logger.debug("Fixing URL for file name: " +fileName+", "+repoUrlUtils);
                    String url = repoUrlUtils.fixSingleUrl(fileName);
                    logger.debug("Small image detected, redirecting to source: " +url);
                    response.sendRedirect(repoUrlUtils.fixSingleUrl(url));
                    return;
                }
            } catch (Exception ex) {
            	logger.error("Problem loading image with JAI: " + ex.getMessage(), ex);
                String url = repoUrlUtils.fixSingleUrl(fileName);
                logger.warn("Redirecting to: "+url);
                response.sendRedirect(repoUrlUtils.fixSingleUrl(url));
                return;
            }
		} else {
			logger.error("Requested image " + documentId + ".jpg was not found");
            response.sendError(response.SC_NOT_FOUND, "Requested image " + documentId + ".jpg was not found");
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
		SearchResultsDTO searchResults = searchDao.findAllByStatus(statusType, filterQuery,  0, 10, "score", "asc");// findByScientificName(query, startIndex, pageSize, sortField, sortDirection);
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
			searchResults = searchDao.findAllByStatus(statusType, filterQuery, startIndex, pageSize, sortField, sortDirection);// findByScientificName(query, startIndex, pageSize, sortField, sortDirection);
		}

		return searchResults;
	}


	/**
	 * Utility to pull out common names and remove duplicates, returning a string
	 *
	 * @param etc
	 * @return
	 */
	private String getCommonNamesString(ExtendedTaxonConceptDTO etc) {
		HashMap<String, String> cnMap = new HashMap<String, String>();

		for (CommonName cn : etc.getCommonNames()) {
			String lcName = cn.getNameString().toLowerCase().trim();

			if (!cnMap.containsKey(lcName)) {
				cnMap.put(lcName, cn.getNameString());
			}
		}

		return StringUtils.join(cnMap.values(), ", ");
	}

	/**
	 * Filter a list of SimpleProperty objects so that the resulting list only
	 * contains objects with a name ending in "Text". E.g. "hasDescriptionText".
	 *
	 * @param etc
	 * @return
	 */
	 private List<SimpleProperty> filterSimpleProperties(ExtendedTaxonConceptDTO etc) {
        List<SimpleProperty> simpleProperties = etc.getSimpleProperties();
        List<SimpleProperty> textProperties = new ArrayList<SimpleProperty>();

        //we only want the list to store the first type for each source
        //HashSet<String> processedProperties = new HashSet<String>();
        Hashtable<String, SimpleProperty> processProperties = new Hashtable<String, SimpleProperty>();
        for (SimpleProperty sp : simpleProperties) {
            String thisProperty = sp.getName() + sp.getInfoSourceName();
            if ((sp.getName().endsWith("Text") || sp.getName().endsWith("hasPopulateEstimate"))) {
                //attempt to find an existing processed property
                SimpleProperty existing = processProperties.get(thisProperty);
                if (existing != null) {
                    //separate paragraphs using br's instead of p so that the citation is aligned correctly
                    existing.setValue(existing.getValue() + "<br><br>" + sp.getValue());
                } else {
                    processProperties.put(thisProperty, sp);
                }
            }
        }
        simpleProperties = Collections.list(processProperties.elements());
        //sort the simple properties based on low priority and non-truncated infosources
        Collections.sort(simpleProperties, new SimplePropertyComparator());
        for (SimpleProperty sp : simpleProperties) {
            if (!nonTruncatedSources.contains(sp.getInfoSourceURL())) {
                sp.setValue(truncateTextBySentence(sp.getValue(), 300));
            }
            textProperties.add(sp);
        }

        return textProperties;
    }

    /**
     * Truncates the text at a sentence break after min length
     * @param text
     * @param min
     * @return
     */
     private String truncateTextBySentence(String text, int min) {
        try {
            if (text != null && text.length() > min) {
                java.text.BreakIterator bi = java.text.BreakIterator.getSentenceInstance();
                bi.setText(text);
                int finalIndex = bi.following(min);
                return text.substring(0, finalIndex) + "...";
            }
        } catch (Exception e) {
            logger.debug("Unable to truncate " + text, e);
        }
        return text;
    }

    /**
     * Truncates the text at a word break after min length
     *
     * @param text
     * @param min
     * @return
     */
     private String truncateTextByWord(String text, int min) {
        try {
            if (text != null && text.length() > min) {
                BreakIterator bi = BreakIterator.getWordInstance();
                bi.setText(text);
                int finalIndex = bi.following(min);
                String truncatedText = text.substring(0, finalIndex) + "...";
                logger.debug("truncate at position: " + finalIndex + " | min: " + min);
                logger.debug("truncated text = " + truncatedText);
                return truncatedText;
            }
        } catch (Exception e) {
            logger.warn("Unable to truncate " + text, e);
        }
        return text;
    }

	/**
	 * @param taxonConceptDao the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}

	/**
	 * @param repoUrlUtils the repoUrlUtils to set
	 */
	public void setRepoUrlUtils(RepoUrlUtils repoUrlUtils) {
		this.repoUrlUtils = repoUrlUtils;
	}

    public Set<String> getNonTruncatedSources() {
        return nonTruncatedSources;
    }

    public void setNonTruncatedSources(Set<String> nonTruncatedSources) {
        logger.debug("Setting the non truncated sources");
        this.nonTruncatedSources = nonTruncatedSources;
    }

    public Set<String> getLowPrioritySources() {
        return lowPrioritySources;
    }

    public void setLowPrioritySources(Set<String> lowPrioritySources) {
        logger.debug("setting the low priority sources");
        this.lowPrioritySources = lowPrioritySources;
    }

    /**
     * Fix for some info sources where multiple common names are produced.
     * Remove duplicates but assumes input List is ordered so that dupes are sequential.
     * 
     * @param commonNames
     * @return commonNames
     */
    private List<CommonName> fixCommonNames(List<CommonName> commonNames) {
        List<CommonName> newNames = new ArrayList<CommonName>();
        if(commonNames!=null && commonNames.size()>0){
        	newNames.add(commonNames.get(0));
        }
        
        for (int i = 1; i < commonNames.size(); i++) {
            CommonName thisCn = commonNames.get(i);
            
            String commonName1 = StringUtils.trimToNull(thisCn.getNameString());
            String infosource1 = StringUtils.trimToNull(thisCn.getInfoSourceName());
            
            String commonName2 = StringUtils.trimToNull(commonNames.get(i-1).getNameString());
            String infosource2 = StringUtils.trimToNull(commonNames.get(i-1).getInfoSourceName());
            
            if (commonName1!=null && commonName1.equalsIgnoreCase(commonName2) 
            		&& infosource1!=null && infosource1.equalsIgnoreCase(infosource2)) {
                logger.debug("Duplicate commonNames detected: "+thisCn);
            } else {
                newNames.add(commonNames.get(i));
            }
        }        
        return newNames;
    }

    /**
     * key-pair-value for commonNames & list of infosource
     * 
     * @param names
     * @return
     */
    private Map<String, List<CommonName>> sortCommonNameSources(List<CommonName> names){
    	String pattern = "[^a-zA-Z0-9]";
    	CommonName prevName = null;
    	Map<String, List<CommonName>> map = new Hashtable<String, List<CommonName>>();
    	List<CommonName> list = new ArrayList<CommonName>();
    	
    	//made a copy of names, so sorting doesn't effect original list order ....
    	List<CommonName> newArrayList = (List<CommonName>)((ArrayList<CommonName>)names).clone();
    	Collections.sort(newArrayList, new CommonNameComparator());
    	Iterator<CommonName> it = newArrayList.iterator();
    	if(it.hasNext()){
    		prevName = it.next();
    		list.add(prevName);
    	}
    	
    	// group the name with infosource, compare the name with alphabet & number only.
    	while(it.hasNext()){
    		CommonName curName = it.next();
    		if(prevName.getNameString().replaceAll(pattern, "").equalsIgnoreCase(
    				curName.getNameString().replaceAll(pattern, ""))){
    			list.add(curName);
    		}
    		else{
    			map.put(prevName.getNameString(), list);
    			
    			list = new ArrayList<CommonName>();
    			list.add(curName);
    			prevName = curName;    			
    		}
    	}
    	if(prevName != null){
    		map.put(prevName.getNameString(), list);
    	}
    	return map;
    }

    /**
     * Create a list of unique infoSources to display on Overview page.
     * 
     * @param etc
     * @return
     */
    private Map<String, InfoSourceDTO> getInfoSource(ExtendedTaxonConceptDTO etc) {
        Map<String, InfoSourceDTO> infoSourceMap = new TreeMap<String, InfoSourceDTO>();

        // Look in each property of the ExtendedTaxonConceptDTO
        if (etc.getTaxonConcept() != null) {
            String text = "Name: " + etc.getTaxonConcept().getNameString() + " " + etc.getTaxonConcept().getAuthor();
            extractInfoSources(etc.getTaxonConcept(), infoSourceMap, text, "Names");
        }
        if (etc.getTaxonName() != null) {
            extractInfoSources(etc.getTaxonName(), infoSourceMap, "Name: " + etc.getTaxonName().getNameComplete(), "Names");
        }
        if (etc.getCommonNames() != null) {
            for (CommonName cn : etc.getCommonNames()) {
                extractInfoSources(cn, infoSourceMap, "Common Name: " + cn.getNameString(), "Names");
            }
        }
        if (etc.getSimpleProperties() != null) {
            for (SimpleProperty sp : etc.getSimpleProperties()) {
                String label = StringUtils.substringAfter(sp.getName(), "#");
                extractInfoSources(sp, infoSourceMap, sp.getValue(), label);
            }
        }
        if (etc.getPestStatuses() != null) {
            for (PestStatus ps : etc.getPestStatuses()) {
                extractInfoSources(ps, infoSourceMap, "Status: " + ps.getStatus(), "Status");
            }
        }
        if (etc.getConservationStatuses() != null) {
            for (ConservationStatus cs : etc.getConservationStatuses()) {
                extractInfoSources(cs, infoSourceMap, "Status: " + cs.getStatus(), "Status");
            }
        }
        if (etc.getExtantStatuses() != null) {
            for (ExtantStatus es : etc.getExtantStatuses()) {
                extractInfoSources(es, infoSourceMap, "Status: " + es.getStatusAsString(), "Status");
            }
        }
        if (etc.getHabitats() != null) {
            for (Habitat hb : etc.getHabitats()) {
                extractInfoSources(hb, infoSourceMap, "Status: " + hb.getStatusAsString(), "Status");
            }
        }
        if (etc.getImages() != null) {
            for (Image img : etc.getImages()) {
                StringBuilder text = new StringBuilder("Image from " + img.getInfoSourceName());
                if (img.getCreator() != null) {
                    text.append("(by ").append(img.getCreator()).append(")");
                }
                if (img.getIsPartOf() != null) {
                    extractInfoSources(img, infoSourceMap, text.toString(), "Images", img.getIsPartOf());
                } else {
                    extractInfoSources(img, infoSourceMap, text.toString(), "Images");
                }
            }
        }
        if (etc.getDistributionImages() != null) {
            for (Image img : etc.getDistributionImages()) {
                StringBuilder text = new StringBuilder("Distribution image from " + img.getInfoSourceName());
                if (img.getCreator() != null) {
                    text.append("(by ").append(img.getCreator()).append(")");
                }
                if (img.getIsPartOf() != null) {
                    extractInfoSources(img, infoSourceMap, text.toString(), "Images", img.getIsPartOf());
                } else {
                    extractInfoSources(img, infoSourceMap, text.toString(), "Images");
                }
            }
        }
        if (etc.getPublicationReference() != null) {
            for (Reference ref : etc.getPublicationReference()) {
                extractInfoSources(ref, infoSourceMap, "Reference: " + ref.getTitle(), "Publication");
            }
        }

        return infoSourceMap;
    }

    /**
     * Extract Info Source information from various AttributableObject's to create a Map of
     * Info Sources for display on web page.
     *
     * @param ao
     * @param infoSourceMap
     * @param text
     * @param section
     */
    private void extractInfoSources(AttributableObject ao, Map<String, InfoSourceDTO> infoSourceMap, String text, String section) {
        extractInfoSources(ao, infoSourceMap, text, section, ao.getIdentifier()); // ao.getInfoSourceURL()
    }

    /**
     * Extract Info Source information from various AttributableObject's to create a Map of
     * Info Sources for display on web page. Takes custom identifier text property.
     *
     * @param ao
     * @param infoSourceMap
     * @param text
     * @param section
     * @param identifier
     */
    private void extractInfoSources(AttributableObject ao, Map<String, InfoSourceDTO> infoSourceMap, String text, String section, String identifier) {
        if (ao != null && ao.getInfoSourceName() != null) {
            String infoSourceName = ao.getInfoSourceName();

            if (infoSourceMap.containsKey(infoSourceName)) {
                // key exists so add to existing object
                InfoSourceDTO is = infoSourceMap.get(infoSourceName);
                if (is.getText() == null || is.getText().isEmpty()) {
                    is.setText(truncateTextByWord(text, TEXT_MAX_WIDTH)); // truncate text
                }
                if (!section.isEmpty()) {
                    Set<String> sections = null;
                    if (is.getSections() != null) {
                        sections = new LinkedHashSet<String>(is.getSections());
                    } else {
                        sections = new LinkedHashSet<String>();
                    }
                    sections.add(section);
                    is.setSections(sections);
                }
            } else {
                // no key so create one
                InfoSourceDTO is = new InfoSourceDTO();
                is.setInfoSourceName(infoSourceName);
                //String identifier = ao.getIdentifier();
                if (identifier != null && !identifier.isEmpty()) {
                    is.setIdentifier(identifier);
                }
                is.setInfoSourceURL(ao.getInfoSourceURL());
                is.setInfoSourceId(ao.getInfoSourceId());
                is.setText(truncateTextByWord(text, TEXT_MAX_WIDTH)); // truncate text
                if (!section.isEmpty()) {
                    Set<String> sections = new LinkedHashSet<String>();
                    sections.add(section);
                    is.setSections(sections);
                }
                infoSourceMap.put(infoSourceName, is);
            }
        }
    }

    /**
     * Populate a return a Map of regions names to regions names section in the WP
     * page http://test.ala.org.au/threatened-species-codes
     *
     * @return regions - the regions Map
     */
    private Map<String, String> statusRegionMap() {
        Map regions = new HashMap<String, String>();
        regions.put("IUCN", "International");
        regions.put("Australia", "Australia-Wide");
        regions.put("Australian Capital Territory", "Australian-Capital-Territory");
        regions.put("New South Wales", "New-South-Wales");
        regions.put("Northern Territory", "Northern-Territory");
        regions.put("Queensland", "Queensland");
        regions.put("South Australia", "South-Australia");
        regions.put("Tasmania", "Tasmania");
        regions.put("Victoria", "Victoria");
        regions.put("Western Australia", "Western-Australia");
        return regions;
    }

    /**
     * Perform JSON service lookup on Spatial Portal for occurrence map Url, etc
     * for a given GUID.
     *
     * @param guid
     * @return
     */
    private Map<String, String> getSpatialPortalMap(String guid) {
        Map<String, String> mapData = new HashMap<String, String>();
        try {
            String jsonString = getUrlContentAsJsonString(SPATIAL_JSON_URL + guid);
            JSONObject jsonObj = new JSONObject(jsonString);
            String mapUrl = jsonObj.getString("mapUrl");
            String legendUrl = jsonObj.getString("legendUrl");
            String type = jsonObj.getString("type");
            mapData.put("mapUrl", mapUrl);
            mapData.put("legendUrl", legendUrl);
            mapData.put("type", type);
        } catch (Exception ex) {
            logger.error("JSON Lookup for Spatial Portal distro map failed. "+ex.getMessage(), ex);
            mapData.put("error", ex.getLocalizedMessage());
        }
        return mapData;
    }


    /**
     * Retrieve content as String. With HTTP header accept: "application/json".
     *
     * @param url
     * @return
     * @throws Exception
     */
    public String getUrlContentAsJsonString(String url) throws Exception {
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setSoTimeout(1500);
        GetMethod gm = new GetMethod(url);
        gm.setRequestHeader("accept", "application/json"); // needed for spatial portal JSON web services
        gm.setFollowRedirects(true);
        httpClient.executeMethod(gm);
        String content = gm.getResponseBodyAsString();
        return content;
    }

    /**
     * Extract the Image repository Id from the Image and set the repoId field
     *
     * @param images
     * @return
     */
    private List<Image> addImageDocIds(List<Image> images) {
    	String repoIdStr = "";
    	
        // Extract the repository document id from repoLocation field
        // E.g. Http://bie.ala.org.au/repo/1040/38/388624/Raw.jpg -> 388624
        for (Image img : images) {
        	String[] paths = StringUtils.split(img.getRepoLocation(), "/");
            // unix format file path
            if(paths.length >= 2){
            	repoIdStr = paths[paths.length - 2]; // get path before filename
            }
            else{
            	// windows format file path
            	paths = StringUtils.split(img.getRepoLocation(), "\\");
            	if(paths.length >= 2){
            		repoIdStr = paths[paths.length - 2]; // get path before filename
            	}
            	else{
                	continue;
                }
            }
            
            if (repoIdStr != null && !repoIdStr.isEmpty()) {
                Integer docId = Integer.parseInt(repoIdStr);
                logger.debug("setting docId = "+docId);
                img.setRepoId(docId);
            }
        }
        return images;
    }
    
    protected class TaxonRankNameComparator implements Comparator<SearchTaxonConceptDTO>{
    	@Override
    	public int compare(SearchTaxonConceptDTO t1, SearchTaxonConceptDTO t2) {
    		if(t1!= null && t1 != null){
    			if(t1.getRankId() != t2.getRankId()){
    				return t1.getRankId() -t2.getRankId();
    			}
    			else{
    				return t1.compareTo(t2);
    			}
    		}
    	return 0;
    	}
 	}

    protected class CommonNameComparator implements Comparator<CommonName>{
    	@Override
        public int compare(CommonName o1, CommonName o2) {
    		return o1.getNameString().compareToIgnoreCase(o2.getNameString());
    	}
    }
    
    /**
     * Comparator to order the Simple Properties based on their natural ordering
     * and low and high priority info sources.
     */
    protected class SimplePropertyComparator implements Comparator<SimpleProperty>{

        @Override
        public int compare(SimpleProperty o1, SimpleProperty o2) {
            int value = o1.compareTo(o2);
            
            if(value ==0){
                //we want the low priority items to appear at the end of the list
                boolean low1 = lowPrioritySources.contains(o1.getInfoSourceURL());
                boolean low2 = lowPrioritySources.contains(o2.getInfoSourceURL());
                if(low1 &&low2) return 0;
                if(low1) return 1;
                if(low2) return -1;
                //we want the non-truncated infosources to appear at the top of the list
                boolean hi1 = nonTruncatedSources.contains(o1.getInfoSourceURL());
                boolean hi2 = nonTruncatedSources.contains(o2.getInfoSourceURL());
                if(hi1&&hi2) return 0;
                if(hi1) return -1;
                if(hi2) return 1;
            }
            return value;
        }

    }
}
