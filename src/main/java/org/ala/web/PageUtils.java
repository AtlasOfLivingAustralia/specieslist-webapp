package org.ala.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ala.model.CommonName;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import atg.taglib.json.util.JSONObject;

public class PageUtils {
	protected static Logger logger = Logger.getLogger(PageUtils.class);
	
    /** The URI for JSON data for static occurrence map */
    public static final String SPATIAL_JSON_URL = "http://spatial.ala.org.au/alaspatial/ws/density/map?species_lsid=";
	

    /**
     * Fix for some info sources where multiple common names are produced.
     * Remove duplicates but assumes input List is ordered so that dupes are sequential.
     * 
     * @param commonNames
     * @return commonNames
     */
    public static List<CommonName> fixCommonNames(List<CommonName> commonNames) {
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
            
            if (commonName1!=null && normaliseCommonName(commonName1).equalsIgnoreCase(normaliseCommonName(commonName2)) 
            		&& infosource1!=null && infosource1.equalsIgnoreCase(infosource2)) {
                logger.debug("Duplicate commonNames detected: "+thisCn);
            } else {
                newNames.add(commonNames.get(i));
            }
        }        
        return newNames;
    }
    
    private static String normaliseCommonName(String commonName) {
		return commonName.replaceAll("([.,-]*)?([\\s]*)?", "").trim().toLowerCase();
	}

    /**
     * key-pair-value for commonNames & list of infosource
     * 
     * @param names
     * @return
     */
    public static Map<String, List<CommonName>> sortCommonNameSources(List<CommonName> names){
    	String pattern = "[^a-zA-Z0-9]";
    	CommonName prevName = null;
    	Map<String, List<CommonName>> map = new Hashtable<String, List<CommonName>>();
    	List<CommonName> list = new ArrayList<CommonName>();
    	
    	//made a copy of names, so sorting doesn't effect original list order ....
    	List<CommonName> newArrayList = (List<CommonName>)((ArrayList<CommonName>)names).clone();
    	Collections.sort(newArrayList, new Comparator<CommonName>(){
        	@Override
            public int compare(CommonName o1, CommonName o2) {
        		return o1.getNameString().compareToIgnoreCase(o2.getNameString());
        	}
    	});
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
     * Perform JSON service lookup on Spatial Portal for occurrence map Url, etc
     * for a given GUID.
     *
     * @param guid
     * @return
     */
    public static Map<String, String> getSpatialPortalMap(String guid) {
        Map<String, String> mapData = new HashMap<String, String>();
        try {
            String jsonString = PageUtils.getUrlContentAsJsonString(SPATIAL_JSON_URL + guid);
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
    public static String getUrlContentAsJsonString(String url) throws Exception {
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setSoTimeout(1500);
        GetMethod gm = new GetMethod(url);
        gm.setRequestHeader("accept", "application/json"); // needed for spatial portal JSON web services
        gm.setFollowRedirects(true);
        httpClient.executeMethod(gm);
        String content = gm.getResponseBodyAsString();
        return content;
    }

	public static List<String> dedup(List<CommonName> commonNames) {
		
		List<String> names = new ArrayList<String>();
		Set<String> namesNormalised = new HashSet<String>();
		for(CommonName commonName: commonNames){
			String[] parts = commonName.getNameString().split(",");
			for(String part: parts){
				String normalised = normaliseCommonName(part);
				System.out.println("Normalised: "+normalised);
				if(!namesNormalised.contains(normalised)){
					names.add(part);
					namesNormalised.add(normalised);
				}
			}
		}
		return names;
	}
}
