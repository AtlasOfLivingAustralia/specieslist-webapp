package org.ala.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ala.dao.InfoSourceDAOImpl;
import org.ala.model.InfoSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class InfosourceUtil {
    
    private static final String jsonUrl = "http://collections.ala.org.au/ws/dataResource.json";
    
    public static void main(String[] args) {
        ApplicationContext context = SpringUtils.getContext();
        
        BasicDataSource dataSource = (BasicDataSource) context.getBean("dataSource");
        InfoSourceDAOImpl infoSourceDAOImpl = new InfoSourceDAOImpl(dataSource);
        
        Map<String, String> nameUidMap = null;
        
        try {
            nameUidMap = getNameUidMap(jsonUrl);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        displayNameUidMap(nameUidMap);
        
        insertUidByName(nameUidMap, infoSourceDAOImpl);
        
//        deserialiseJsonMap(jsonMap);
        
    }
    
    public static Map<String,String> getNameUidMap(String url) throws Exception {
        String jsonStr = WebUtils.getUrlContentAsString(url);
        Map<String,String> nameUidMap = new HashMap<String, String>();
        
//        if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
//            jsonStr = jsonStr.substring(1,jsonStr.length()-1);
//        }
        
//        System.out.println(jsonStr);
        
        ObjectMapper om = new ObjectMapper();
        
        JsonNode root = om.readTree(jsonStr);
        Iterator<JsonNode> iter = root.getElements();
        while(iter.hasNext()){
            JsonNode jsonNode = iter.next();
            String name = jsonNode.get("name").getTextValue();
            String uid = jsonNode.get("uid").getTextValue();
            
            nameUidMap.put(name, uid);
        }
        
        
        return nameUidMap;
    }
    
    public static void displayNameUidMap(Map<String, String> jsonMap) {
        if (jsonMap != null) {
            Set<String> keySet = jsonMap.keySet();
            
            Iterator<String> iter = keySet.iterator();
            
            while (iter.hasNext()) {
                String key = (String) iter.next();
                
                System.out.println(key + ":" + jsonMap.get(key));
            }
        } else {
            System.out.println("JSON Map is null!");
        }
    }
    
    public static void insertUidByName(Map<String, String> nameUidMap, InfoSourceDAOImpl infoSourceDAOImpl) {
        if (nameUidMap != null) {
            Set<String> keySet = nameUidMap.keySet();
            
            Iterator<String> iter = keySet.iterator();
            
            while (iter.hasNext()) {
                String name = (String) iter.next();
                String uid = nameUidMap.get(name);
                
                infoSourceDAOImpl.insertUidByName(name, uid);
            }
        } else {
            System.out.println("Map is null!");
        }
    }
    
}
