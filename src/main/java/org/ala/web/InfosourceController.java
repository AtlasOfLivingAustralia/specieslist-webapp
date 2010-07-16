package org.ala.web;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.ala.dao.FulltextSearchDao;
import org.ala.dao.InfoSourceDAO;
import org.ala.dao.VocabularyDAO;
import org.ala.lucene.Autocompleter;
import org.ala.model.InfoSource;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
/**
 * A controller for showing information for infosources.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Controller("infosourceController")
public class InfosourceController {
	
	/** Logger initialisation */
	private final static Logger logger = Logger.getLogger(InfosourceController.class);
	/** DAO bean for access to info sources */
	@Inject
	private InfoSourceDAO infoSourceDAO;
	/** DAO bean for vocabularies */
	@Inject
	private VocabularyDAO vocabularyDAO;
	/** DAO bean for SOLR search queries */
	@Inject
	private FulltextSearchDao searchDao;
	/** Name of view for list of datasets */
	private final String DATASET_LIST = "species/datasetList";
	/** Name of view for list of vocabularies */
	private final String VOCABULARIES_LIST = "species/vocabularies";
	
	protected String dataProviderCounts = "http://biocache.ala.org.au/data_providers/counts.json";
	
	/**
	 * List of data sets.
	 * 
	 *
	 * @param model
	 * @return view name
	 */
	@RequestMapping(value = "/contributors", method = RequestMethod.GET)
	public String listContributors (Model model) throws Exception  {
		List<InfoSource> infoSources = infoSourceDAO.getAllByDatasetType();
		List<Integer> infoSourceIDWithVocabulariesMapList = new ArrayList<Integer>();
		for (InfoSource infoSource : infoSources) {
			List<Map<String,Object>> vocabulariesMap = vocabularyDAO.getTermsByInfosourceId(infoSource.getId());
			
			if (vocabulariesMap != null && vocabulariesMap.size() != 0) {
				infoSourceIDWithVocabulariesMapList.add(infoSource.getId());
			}
		}
		
		List<InfoSource> occurrenceInfoSources = retrieveOccurrenceInfoSources();
        model.addAttribute("occurrenceInfoSources", occurrenceInfoSources);
		model.addAttribute("infoSources", infoSources);
		model.addAttribute("infoSourceIDWithVocabulariesMapList", infoSourceIDWithVocabulariesMapList);
		Map<String, Long> countsMap = searchDao.getAllDatasetCounts();
		model.addAttribute("countsMap", countsMap);

		return DATASET_LIST;
	}

	/**
	 * Load occurrence counts from biocache in a fault tolerant manner.
	 * 
	 * @return
	 * @throws Exception
	 */
	private List<InfoSource> retrieveOccurrenceInfoSources() throws Exception {
		//call out to biocache to retrieve list of specimen and occurrence datasets / providers
		//http://biocache.ala.org.au/data_providers/counts.json
        String jsonObject = getUrlContentAsString(dataProviderCounts);
        List<InfoSource> occurrenceInfoSources = new ArrayList<InfoSource>();
		try {
			if(jsonObject!=null){
				ObjectMapper om = new ObjectMapper();
				JsonNode root = om.readTree(jsonObject);
				JsonNode dataProviders = root.get("dataProviders");
				Iterator<JsonNode> iter = dataProviders.getElements();
				while(iter.hasNext()){
					JsonNode jsonNode = iter.next();
					String id = jsonNode.get("id").getTextValue();
					String name = jsonNode.get("name").getTextValue();
					int count = jsonNode.get("count").getIntValue();
					InfoSource i = new InfoSource();
					i.setId(Integer.parseInt(id));
					i.setName(name);
					i.setDocumentCount(count);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return occurrenceInfoSources;
	}

	/**
	 * List of vocabularies for a given Info Source Id
	 *
	 * @param model
	 * @return view name
	 */
	@RequestMapping(value = "/species/vocabularies/{infosourceId}", method = RequestMethod.GET)
	public String listVocabularies (@PathVariable("infosourceId") String infoSourceId, Model model) throws Exception {
		model.addAttribute("infoSource", infoSourceId);
		
        int infoId = Integer.parseInt(infoSourceId);

        logger.debug(vocabularyDAO.getPreferredTermsFor(infoId, "" ,  ""));

        List<Map<String,Object>> vocabulariesMap = vocabularyDAO.getTermsByInfosourceId(infoId);

        model.addAttribute("vocabulariesMap", vocabulariesMap);

        InfoSource infoSource = infoSourceDAO.getById(infoId);
        String infoSourceName = infoSource.getName();
        logger.debug("Infosource Name:" + infoSourceName);
        model.addAttribute("infoName", infoSourceName);

		return VOCABULARIES_LIST;
	}

	/**
	 * Autocomplete AJAX service for JQuery-autocomplete
	 *
	 * @param query
	 * @param response
	 */
	@RequestMapping(value = "/species/terms", method = RequestMethod.GET)
	public void listTerms(
			@RequestParam(value="term", required=false) String query,
			HttpServletResponse response) {

		try {
			OutputStreamWriter os = new OutputStreamWriter(response.getOutputStream());
			Autocompleter ac = new Autocompleter();
			List<String> terms = new ArrayList<String>();
			terms = ac.suggestTermsFor(query.toLowerCase().trim(), 10);
			// create JSON string using Jackson
			ObjectMapper o = new ObjectMapper();
			String json = o.writeValueAsString(terms);
			response.setContentType("application/json");
			os.write(json);
			os.close();
		} catch (IOException ex) {
			logger.error("Problem running Autocompleter: "+ex.getMessage(), ex);
		}

		return;
	}

	/**
	 * Retrieve content as String.
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String getUrlContentAsString(String url) throws Exception {
		HttpClient httpClient = new HttpClient();
		GetMethod gm = new GetMethod(url);
		gm.setFollowRedirects(true);
		httpClient.executeMethod(gm);
		// String requestCharset = gm.getRequestCharSet();
		String content = gm.getResponseBodyAsString();
		// content = new String(content.getBytes(requestCharset), "UTF-8");
		return content;
	}
	
	/**
	 * @param infoSourceDAO the infoSourceDAO to set
	 */
	public void setInfoSourceDAO(InfoSourceDAO infoSourceDAO) {
		this.infoSourceDAO = infoSourceDAO;
	}

	/**
	 * @param vocabularyDAO the vocabularyDAO to set
	 */
	public void setVocabularyDAO(VocabularyDAO vocabularyDAO) {
		this.vocabularyDAO = vocabularyDAO;
	}

	/**
	 * @param searchDao the searchDao to set
	 */
	public void setSearchDao(FulltextSearchDao searchDao) {
		this.searchDao = searchDao;
	}
}
