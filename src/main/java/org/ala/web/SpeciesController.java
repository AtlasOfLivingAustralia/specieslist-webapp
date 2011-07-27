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

import java.io.IOException;
import java.io.OutputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ala.client.appender.RestLevel;
import org.ala.client.model.LogEventType;
import org.ala.client.model.LogEventVO;
import org.ala.dao.DocumentDAO;
import org.ala.dao.FulltextSearchDao;
import org.ala.dao.IndexedTypes;
import org.ala.dao.InfoSourceDAO;
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
import org.ala.model.InfoSource;
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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.org.ala.data.model.LinnaeanRankClassification;

/**
 * Main controller for the BIE site
 *
 * TODO: If this class gets too big or complex then split into multiple Controllers.
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
@Controller(value="speciesController")
public class SpeciesController {

    /** Logger initialisation */
    private final static Logger logger = Logger.getLogger(SpeciesController.class);
    /** DAO bean for access to taxon concepts */
    @Inject
    private TaxonConceptDao taxonConceptDao;
    /** DAO bean for access to repository document table */
    @Inject
    private DocumentDAO documentDAO;
    @Inject
    protected InfoSourceDAO infoSourceDAO;
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

    public static final String COL_HOME = "http://www.catalogueoflife.org/";
    public static final String APNI_HOME = "http://www.anbg.gov.au/apni/";
    public static final String APC_HOME = "http://www.anbg.gov.au/chah/apc/";
    public static final String AFD_HOME = "http://www.environment.gov.au/biodiversity/abrs/online-resources/fauna/afd/home";

    /** Max width to Truncate text to */
    private int TEXT_MAX_WIDTH = 200;

    InfoSource afd;
    InfoSource apni;
    InfoSource col;


    public SpeciesController(){
        nonTruncatedSources.add("http://www.environment.gov.au/biodiversity/abrs/online-resources/flora/main/index.html");
        lowPrioritySources.add("http://www.ozanimals.com/");
        lowPrioritySources.add("http://en.wikipedia.org/");
        //lowPrioritySources.add("http://plantnet.rbgsyd.nsw.gov.au/floraonline.htm");
    }

    @PostConstruct
    public void init(){
        afd = infoSourceDAO.getByUri(AFD_HOME);
        apni = infoSourceDAO.getByUri(APNI_HOME);
        col = infoSourceDAO.getByUri(COL_HOME);
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
     * Map to a /{guid}.json or /{guid}.xml URI.
     * E.g. /species/urn:lsid:biodiversity.org.au:afd.taxon:a402d4c8-db51-4ad9-a72a-0e912ae7bc9a
     *
     * @param guid
     * @param model
     * @return view name
     * @throws Exception
     */
    @RequestMapping(value = {"/species/shortProfile/{guid}.json","/species/shortProfile/{guid}.xml"}, method = RequestMethod.GET)
    public void showInfoSpecies(
            @PathVariable("guid") String guid, Model model) throws Exception {

        ExtendedTaxonConceptDTO edto = taxonConceptDao.getExtendedTaxonConceptByGuid(guid);

        if(edto!=null){

            repoUrlUtils.fixRepoUrls(edto);

            model.addAttribute("scientificName", edto.getTaxonConcept().getNameString());

            model.addAttribute("year", edto.getTaxonConcept().getAuthorYear()); //to be added - currently blank
            model.addAttribute("scientificNameAuthorship", edto.getTaxonConcept().getAuthor());
            model.addAttribute("rank", edto.getTaxonConcept().getRankString());
            model.addAttribute("rankID", edto.getTaxonConcept().getRankID());

            if(edto.getTaxonName()!=null){
                model.addAttribute("author", edto.getTaxonName().getAuthorship());
            } else {
                model.addAttribute("author", edto.getTaxonConcept().getAuthor());
            }

            if(edto.getClassification()!=null){
                model.addAttribute("family", edto.getClassification().getFamily());
                model.addAttribute("kingdom", edto.getClassification().getKingdom());
            }
            model.addAttribute("year", edto.getTaxonConcept().getAuthorYear());

            if(!edto.getCommonNames().isEmpty()){
                CommonName cn = edto.getCommonNames().get(0);
                model.addAttribute("commonName", cn.getNameString());
                model.addAttribute("commonNameGUID", cn.getGuid());
            }
            if(!edto.getImages().isEmpty()){
                Image image = edto.getImages().get(0);
                model.addAttribute("imageURL", image.getRepoLocation());
                model.addAttribute("thumbnail", image.getThumbnail());
            }
        }
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
     * Retrieve a list of identifiers for each of the names supplied.
     * 
     * @param scientificNames
     * @param request
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/ws/guid/batch", method = RequestMethod.GET)
    public @ResponseBody Map<String,List<GuidLookupDTO>> getGuidForNames(
            @RequestParam("q") List<String> scientificNames,
            HttpServletRequest request,
            Model model) throws Exception {
        Map<String,List<GuidLookupDTO>> nameMaps = new LinkedHashMap<String,List<GuidLookupDTO>>();
        for(String name: scientificNames){
            nameMaps.put(name, findGuids(name));
        }
        return nameMaps;
    }

    /**
     * Map to a /{guid} URI.
     * E.g. /species/urn:lsid:biodiversity.org.au:afd.taxon:a402d4c8-db51-4ad9-a72a-0e912ae7bc9a
     * 
     * @param model
     * @return view name
     * @throws Exception
     */ 
    @RequestMapping(value = "/ws/guid/{scientificName}", method = RequestMethod.GET)
    public @ResponseBody List<GuidLookupDTO> getGuidForName(@PathVariable("scientificName") String scientificName,HttpServletRequest request,
            Model model) throws Exception {
        return findGuids(scientificName);
    }

    private List<GuidLookupDTO> findGuids(String scientificName)
    throws Exception {
        String lsid = getLsidByNameAndKingdom(scientificName);
        List<GuidLookupDTO> guids = new ArrayList<GuidLookupDTO>();

        if(lsid != null && lsid.length() > 0){
            ExtendedTaxonConceptDTO etc = taxonConceptDao.getExtendedTaxonConceptByGuid(lsid);
            if (etc.getTaxonConcept() != null && etc.getTaxonConcept().getGuid() != null) {

                //FIXME - this should come straight from BIE with a the attribution
                //coming from the BIE. The identifier should be a separate model object
                //which extends AttributableObject
                TaxonConcept tc = etc.getTaxonConcept();

                GuidLookupDTO preferredGuid = new GuidLookupDTO();
                preferredGuid.setIdentifier(tc.getGuid());
                preferredGuid.setInfoSourceId(tc.getInfoSourceId());
                preferredGuid.setInfoSourceURL(tc.getInfoSourceURL());
                preferredGuid.setInfoSourceName(tc.getInfoSourceName());
                preferredGuid.setName(tc.getNameString());
                guids.add(preferredGuid);

                //add identifiers
                for(String identifier: etc.getIdentifiers()){
                    GuidLookupDTO g = new GuidLookupDTO();
                    g.setIdentifier(identifier);
                    g.setName(tc.getNameString());
                    //FIXME to be removed
                    if(identifier.contains(":apni.")){
                        g.setInfoSourceId(Integer.toString(apni.getId()));
                        g.setInfoSourceURL(apni.getWebsiteUrl());
                        g.setInfoSourceName(apni.getName());
                        guids.add(g);
                    } else if(identifier.contains(":afd.")){
                        g.setInfoSourceId(Integer.toString(afd.getId()));
                        g.setInfoSourceURL(afd.getWebsiteUrl());
                        g.setInfoSourceName(afd.getName());
                        guids.add(g);
                    } else if(identifier.contains(":catalogueoflife.")){
                        g.setInfoSourceId(Integer.toString(col.getId()));
                        g.setInfoSourceURL(col.getWebsiteUrl());
                        g.setInfoSourceName(col.getName());
                        guids.add(g);
                    }
                }
            }
        }
        return guids;
    }

    /**
     * Get the repo location of a image according to {guid} .
     * E.g. /species/image/urn:lsid:biodiversity.org.au:afd.taxon:a402d4c8-db51-4ad9-a72a-0e912ae7bc9a
     * 
     * @param model
     * @return view name
     * @throws Exception
     */ 
    @RequestMapping(value = "/species/image/{imageType}/{guid:.+}", method = RequestMethod.GET)
    public String showImages(
            @PathVariable("guid") String guidParam,
            @PathVariable("imageType") String imageType,
            @RequestParam(value="conceptName", defaultValue ="", required=false) String conceptName,
            HttpServletRequest request,
            Model model) throws Exception {
        String guid = guidParam;
        logger.info("Displaying image for: " + guid +" .....");

        SearchResultsDTO<SearchDTO> stcs = searchDao.findByName(IndexedTypes.TAXON, guid, null, 0, 1, "score", "asc");
        if(stcs.getTotalRecords()>0){
            SearchTaxonConceptDTO st = (SearchTaxonConceptDTO) stcs.getResults().get(0);
            repoUrlUtils.fixRepoUrls(st);
//            logger.info(st.getImage());
            if ("thumbnail".equals(imageType) && st.getThumbnail() != null && !"".equals(st.getThumbnail())) {
                return "redirect:" + st.getThumbnail();
            } else if ("small".equals(imageType) && st.getImage() != null && !"".equals(st.getImage())) {
                return "redirect:" + st.getImage().replaceAll("raw", "smallRaw");
            }
        }

        return "redirect:/images/noImage85.jpg"; // no image 
    }



    /**
     * Map to a /{guid} URI.
     * E.g. /species/urn:lsid:biodiversity.org.au:afd.taxon:a402d4c8-db51-4ad9-a72a-0e912ae7bc9a
     * 
     * @param model
     * @return view name
     * @throws Exception
     */ 
    @RequestMapping(value = "/species/{guid:.+}", method = RequestMethod.GET)
    public String showSpecies(
            @PathVariable("guid") String guidParam,
            @RequestParam(value="conceptName", defaultValue ="", required=false) String conceptName,
            HttpServletRequest request,
            Model model) throws Exception {
        String guid = guidParam;
        long startTime = System.currentTimeMillis();
        long sTime = System.currentTimeMillis();
        
        logger.debug("Displaying page for: " + guid +" .....");
        ExtendedTaxonConceptDTO etc = null;
        StringBuffer sb = new StringBuffer();
       
        if(guid == null || guid.length() < 3){
        	//no match for the parameter, redirect to search page.
            return "redirect:/search?q=" + extractScientificName(guid);
        }
        
        if (guid.matches("(urn\\:lsid[a-zA-Z\\-0-9\\:\\.]*)") || guid.matches("([0-9]*)")) {   
        	if(guid.matches("([0-9]*)") && guid.length() < 8){
        		//no match for the parameter, redirect to search page.
                return "redirect:/search?q=" + extractScientificName(guid);
        	}
        	etc = taxonConceptDao.getExtendedTaxonConceptByGuid(guid);
        }
        else{
        	// guid == sciName and kingkom?
        	String lsid = getLsidByNameAndKingdom(guid);
        	if(lsid != null && lsid.length() > 0){
                etc = taxonConceptDao.getExtendedTaxonConceptByGuid(lsid);
                if (etc.getTaxonConcept() != null && etc.getTaxonConcept().getGuid() != null) {
                    guid = lsid;
                }
        	}   
                
                
        	//if etc is empty then guid == sciName and kingkom?
//	        if(etc == null || etc.getTaxonConcept() == null || etc.getTaxonConcept().getGuid() == null){
//	            String lsid = getLsidByNameAndKingdom(guid);
//	            if(lsid != null && lsid.length() > 0){
//	                etc = taxonConceptDao.getExtendedTaxonConceptByGuid(lsid);
//	                if (etc.getTaxonConcept() != null && etc.getTaxonConcept().getGuid() != null) {
//	                    guid = lsid;
//	                }
//	            }
//	            else{
//	                //no match for the parameter, redirect to search page.
//	                return "redirect:/search?q=" + extractScientificName(guid);
//	            }
//	        }
        }

        if(etc == null || etc.getTaxonConcept() == null || etc.getTaxonConcept().getGuid() == null){
        	//no match for the parameter, redirect to search page.
            return "redirect:/search?q=" + extractScientificName(guid);
        }        
        sb.append("get ETC:" + (System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();
        
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
        List<CommonName> names = PageUtils.fixCommonNames(etc.getCommonNames()); // remove duplicate names
        Map<String, List<CommonName>> namesMap = PageUtils.sortCommonNameSources(names);
        String[] keyArray = PageUtils.commonNameRankingOrderKey(namesMap.keySet(), names);
        //        String[] keyArray = namesMap.keySet().toArray(new String[0]);
        //        Arrays.sort(keyArray, String.CASE_INSENSITIVE_ORDER);
        model.addAttribute("sortCommonNameSources", namesMap);
        model.addAttribute("sortCommonNameKeys", keyArray);

        etc.setCommonNames(names); 
        etc.setImages(addImageDocIds(etc.getImages()));
        sb.append(", get Common Name:" + (System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();
        
        //        etc.setScreenshotImages(addImageDocIds(etc.getScreenshotImages()));
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
        sb.append(", get ranking:" + (System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();
        
        TaxonConcept tc = etc.getTaxonConcept();

        //load the hierarchy
        if(tc.getLeft()!=null){
            SearchResultsDTO<SearchTaxonConceptDTO> taxonHierarchy = searchDao.getClassificationByLeftNS(tc.getLeft());
            model.addAttribute("taxonHierarchy", taxonHierarchy.getResults());
        }
        sb.append(", get hierarchy:" + (System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();
        
        //load child concept using search indexes
        List<SearchTaxonConceptDTO> childConcepts = searchDao.getChildConceptsParentId(Integer.toString(tc.getId()));
        //Reorder the children concepts so that ordering is based on rank followed by name
        //TODO: Currently this is being performed here instead of in the DAO incase somewhere relies on the default order.  We may need to move this
        Collections.sort(childConcepts, new TaxonRankNameComparator());
        
        model.addAttribute("childConcepts", childConcepts);
        sb.append(", get child concept:" + (System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();
        
        //create a map
        model.addAttribute("rankedImageUris", rankedUris);
        model.addAttribute("rankedImageUriMap", rankingMap);
        // add map for conservation status regions to sections in the WP page describing them (http://test.ala.org.au/threatened-species-codes/#International)
        model.addAttribute("statusRegionMap", statusRegionMap());
        // get static occurrence map from spatial portal via JSON lookup
//        model.addAttribute("spatialPortalMap", PageUtils.getSpatialPortalMap(etc.getTaxonConcept().getGuid()));
        sb.append(", get map:" + (System.currentTimeMillis() - startTime));
        model.addAttribute("executeTime", sb.toString() + ", total: " + (System.currentTimeMillis() - sTime));
        
        //send message to logger service
        String userAgent = request.getHeader("User-Agent");
        if(userAgent == null || !userAgent.toLowerCase().contains("googlebot")){
	        Map<String, Integer> imap = createImageSourceMap(etc);
	        if(imap.size() > 0){
		        LogEventVO vo = new LogEventVO(LogEventType.IMAGE_VIEWED, "", "species image view", request.getLocalAddr(), imap);
		        logger.log(RestLevel.REMOTE, vo);
	        }
        }
        logger.debug("Returning page view for: " + guid +" .....");
        return SPECIES_SHOW;
    }

    private Map<String, Integer> createImageSourceMap(ExtendedTaxonConceptDTO etc){
    	Map<String, Integer> recordCounts = new Hashtable<String, Integer>();
    	if(etc != null && etc.getImages() != null){
    		for(Image image: etc.getImages()){
    			String uid = image.getInfoSourceUid();
    			if(uid != null && !"".equals(uid)){
    				if(recordCounts.containsKey(uid)){
    					Integer value = recordCounts.get(uid);
    					recordCounts.put(uid, value + 1);
    				}
    				else{
    					recordCounts.put(uid, 1);
    				}
    			}
    		}
    	}
    	return recordCounts;
    }
    
    private String extractScientificName(String parameter){
        String name = null;

        int i = parameter.indexOf('(');
        if(i >= 0){
            name = parameter.substring(0, i);
        }
        else{
            name = parameter;
        }
        name = name.replaceAll("_", " ");
        name = name.replaceAll("\\+", " ");
        name = name.trim();

        return name;
    }

    private String extractKingdom(String parameter){
        String kingdom = null;

        int i = parameter.indexOf('(');
        int j = parameter.indexOf(')');
        if(i >= 0 && j >= 0 && j > i){
            kingdom = parameter.substring(i + 1, j);
            kingdom = kingdom.trim();
        }
        return kingdom;
    }

    private String getLsidByNameAndKingdom(String parameter){
        String lsid = null;
        String name = null;
        String kingdom = null;

        name = extractScientificName(parameter);
        kingdom = extractKingdom(parameter);
        if(kingdom != null){
            LinnaeanRankClassification cl = new LinnaeanRankClassification(kingdom, null);
            cl.setScientificName(name);
            lsid = taxonConceptDao.findLsidByName(cl.getScientificName(), cl, null);
        }

        if(lsid == null || lsid.length() < 1){
            lsid = taxonConceptDao.findLSIDByCommonName(name);
        }

        if(lsid == null || lsid.length() < 1){
            lsid = taxonConceptDao.findLSIDByConcatName(name);
        }

        if(lsid == null || lsid.length() < 1){
            //			if(name != null && !name.toLowerCase().startsWith("australia")){
            lsid = taxonConceptDao.findLsidByName(name);
            //			}
        }
        return lsid;
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
    @RequestMapping(value = {"/species/{guid}.json","/species/{guid}.jsonp"}, method = RequestMethod.GET)
    public @ResponseBody ExtendedTaxonConceptDTO showSpeciesJson(@PathVariable("guid") String guid) throws Exception {
        logger.info("Retrieving concept JSON with guid: "+guid);
        return findConceptByNameOrGuid(guid);
    }

    /**
     * JSON output (via request header) for TC guid
     *
     * @param guid
     * @return
     * @throws Exception
     */
    //@todo firefox request accept header included application/json field. firefox not working properly in species page.

    //    @RequestMapping(value="/species/{guid:.+}", method=RequestMethod.GET, headers="Accept=application/json")
    //    public @ResponseBody ExtendedTaxonConceptDTO showSpeciesJsonAcceptHeader(@PathVariable("guid") String guid) throws Exception {
    //		logger.info("Retrieving concept JSON with guid: "+guid);
    //		return findConceptByNameOrGuid(guid);
    //	}

    /**
     * XML output for TC guid
     *
     * @param guid
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/species/{guid}.xml", method = RequestMethod.GET)
    public ExtendedTaxonConceptDTO showSpeciesXml(@PathVariable("guid") String guid) throws Exception {
        logger.info("Retrieving concept XML with guid: "+guid);
        return findConceptByNameOrGuid(guid);
    }

    private ExtendedTaxonConceptDTO findConceptByNameOrGuid(String guid) throws Exception {
        ExtendedTaxonConceptDTO etc = taxonConceptDao.getExtendedTaxonConceptByGuid(guid);
        if(etc==null || etc.getTaxonConcept()==null || etc.getTaxonConcept().getNameString()==null){
            String lsid = getLsidByNameAndKingdom(guid);
            if(lsid != null && lsid.length() > 0){
                etc = taxonConceptDao.getExtendedTaxonConceptByGuid(lsid);
                return repoUrlUtils.fixRepoUrls(etc);
            }
        } else {
            return repoUrlUtils.fixRepoUrls(taxonConceptDao.getExtendedTaxonConceptByGuid(guid));
        }
        return null;
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
     * JSON web service to return a list of scientific names for an input list of GUIDs
     * 
     * @param guids
     * @return names
     */
    @RequestMapping(value = "/species/namesFromGuids.json", method = RequestMethod.GET)
    public @ResponseBody List<String> getNamesForGuids(@RequestParam(value="guid", required=true) String[] guids) {
        List<String> names = new ArrayList<String>();
        
        for (String guid : guids) {
            String name = null;
            try {
                TaxonConcept tc = taxonConceptDao.getByGuid(guid);
                name = tc.getNameString();
            } catch (Exception ex) {
                logger.warn("No TN found for guid: " + guid, ex);
            }
            
            names.add(name); // note: will add null if no name is found as we want size of output list to be same as input array
        }
        
        return names;
    }
    
    /**
     * JSON web service to return a list of synonyms for a GUID/LSID.
     * Note: accepted taxonConcept is included as first element of output list.
     * 
     * @param guid
     * @return synonyms
     */
    @RequestMapping(value = "/species/synonymsForGuid/{guid}", method = RequestMethod.GET)
    public @ResponseBody  List<Map<String, String>> getNamesForGuids(@PathVariable("guid") String guid) {
        List<Map<String, String>> synonyms = new ArrayList<Map<String, String>>();
        
        try {
            List<TaxonConcept> tcs = taxonConceptDao.getSynonymsFor(guid);
            TaxonConcept accepted = taxonConceptDao.getByGuid(guid);
            tcs.add(0, accepted);
            
            for (TaxonConcept tc : tcs) {
                Map<String, String> syn = new HashMap<String, String>();
                String name = tc.getNameString();
                String author = (tc.getAuthor() != null) ? tc.getAuthor() : "";
                String nameComplete = name;
                // incosistent format in nameString - some include author and others don't...
                if (!author.isEmpty() && StringUtils.contains(name, author)) {
                    nameComplete = name;
                    name = StringUtils.remove(name, author).trim();
                } else if (!author.isEmpty()) {
                    nameComplete = name + " " + author;
                } 
                
                //syn.put(tc.getGuid(), nameAuthor);
                syn.put("guid", tc.getGuid());
                syn.put("nameComplete", nameComplete);
                syn.put("name", name);
                syn.put("author", author);
                synonyms.add(syn);
            }
            
        } catch (Exception ex) {
            logger.warn("No TNs found for guid: " + guid, ex);
        }
        
        return synonyms;
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
        if (etc.getScreenshotImages() != null) {
            for (Image img : etc.getScreenshotImages()) {
                StringBuilder text = new StringBuilder("Screenshot image from " + img.getInfoSourceName());

                //System.out.println(img.getRepoLocation());
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
            //            System.out.println(img.getRepoLocation());

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

    /**
     * @param infoSourceDAO the infoSourceDAO to set
     */
    public void setInfoSourceDAO(InfoSourceDAO infoSourceDAO) {
        this.infoSourceDAO = infoSourceDAO;
    }
}
