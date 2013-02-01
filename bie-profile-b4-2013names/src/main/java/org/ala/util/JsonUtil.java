package org.ala.util;

/**
 *
 * @author Tommy Wang (tommy.wang@csiro.au)
 */


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//import org.apache.commons.dbcp.BasicDataSource;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;


public class JsonUtil {

    public static Map<String,String> getJsonValueMap(String url, List<String> attributeNameList) throws Exception {
        String jsonStr = WebUtils.getUrlContentAsString(url);
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
            
            for (String attributeName : attributeNameList) {
                if (jsonNode.get(attributeName) == null) {
                    continue;
                } else {
                    String value = jsonNode.get(attributeName).getTextValue();
                    jsonValuedMap.put(attributeName, value);
                }
            }
        }


        return jsonValuedMap;
    }
    
    public static Map<String, String> getNodeAttributes(String url, String nodeName) throws Exception {
        String jsonStr = WebUtils.getUrlContentAsString(url);
        Map<String,String> jsonValuedMap = new HashMap<String, String>();
        
        ObjectMapper om = new ObjectMapper();

        JsonNode root = om.readTree(jsonStr);
        
        JsonNode subNode = root.get(nodeName);
        
        if (subNode == null) {
            return null;
        } else {
            Iterator<String> iter = subNode.getFieldNames();
            while(iter.hasNext()){
                String fieldName = iter.next();
                String value = subNode.get(fieldName).getTextValue();
                System.out.println(fieldName + ":" + value);
                jsonValuedMap.put(fieldName, value);
            }
        }
        
        return jsonValuedMap;
    }
}
