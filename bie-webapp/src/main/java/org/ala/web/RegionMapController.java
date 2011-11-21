package org.ala.web;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Controller("regionMapController")
public class RegionMapController {
//
//    @RequestMapping(value = "/map/sld", method = RequestMethod.GET)
//    public String map(
//            @RequestParam(value="guid", required=true) String regionType,
//            @RequestParam(value="rt", required=true) List<String> regions,
//            Model model) throws Exception {
//        return "sld";
//    }
	
	
	
	
    @RequestMapping(value = "/map/sld", method = RequestMethod.GET)
    public String makeSld(
            @RequestParam(value="rt", required=true) String regionType,
            @RequestParam(value="r", required=true) List<String> regions,
            Model model) throws Exception {
//
//    	/*
//|    1 | State                       | region.type.state     | ala:as                 | ADMIN_NAME         |
//|    2 | Territory                   | region.type.territory | ala:as                 | ADMIN_NAME         |
//|    3 | Borough                     | region.type.territory | ala:gadm               | NAME_2             |
//|    4 | City                        | region.type.territory | ala:gadm               | NAME_2             |
//|    5 | Community gov. council      | region.type.territory | ala:gadm               | NAME_2             |
//|    6 | District council            | region.type.territory | ala:gadm               | NAME_2             |
//|    7 | Municipality                | region.type.territory | ala:gadm               | NAME_2             |
//|    8 | Rural city                  | region.type.territory | ala:gadm               | NAME_2             |
//|    9 | Shire                       | region.type.territory | ala:gadm               | NAME_2             |
//|   10 | Territory                   | region.type.territory | ala:gadm               | NAME_2             |
//|   11 | Town                        | region.type.territory | ala:gadm               | NAME_2             |
//| 2000 | IBRA BioRegion              | region.type.territory | ala:ibra               | REG_NAME           |
//| 3001 | IMCRA Cold Temperate Waters | region.type.territory | ala:imcra              | PB_NAME            |
//| 3002 | IMCRA Subtropical Waters    | region.type.territory | ala:imcra              | PB_NAME            |
//| 3003 | IMCRA Transitional Waters   | region.type.territory | ala:imcra              | PB_NAME            |
//| 3004 | IMCRA Tropical Waters       | region.type.territory | ala:imcra              | PB_NAME            |
//| 3005 | Warm Temperate Waters       | region.type.territory | ala:imcra              | PB_NAME            |
//| 5000 | River Basin                 | region.type.territory | geoscience:riverbasins | RNAME              |
//
//    	 * 
//    	 * 
//    	 */
    	String namedLayer = "ala:as";
    	String layerParam = "ADMIN_NAME";
    	
    	if("States".equals(regionType) || "Territory".equals(regionType)){
    		namedLayer = "ala:as";
    		layerParam = "ADMIN_NAME";
    	} else if("Borough".equals(regionType) 
    			|| "City".equals(regionType)
    			|| "Community gov. council".equals(regionType)
    			|| "District council".equals(regionType)
    			|| "Municipality".equals(regionType)
    			|| "Rural city".equals(regionType)
    			|| "Shire".equals(regionType)
    			|| "Territory".equals(regionType)
    			|| "Town".equals(regionType)
    		){
    		namedLayer = "ala:gadm";
    	} else if("IBRA BioRegion".equals(regionType)){
    		namedLayer = "ala:ibra";
    	} else if("IMCRA Cold Temperate Waters".equals(regionType)
    			|| "IMCRA Subtropical Waters".equals(regionType)
    			|| "IMCRA Transitional Waters".equals(regionType)
    			|| "IMCRA Tropical Waters".equals(regionType)
    			|| "Warm Temperate Waters".equals(regionType)
    		){
    		namedLayer = "ala:imcra";
    	} else if("River Basin".equals(regionType)){
    		namedLayer = "geoscience:riverbasins";
    	}
    	
    	model.addAttribute("regions", regions);
    	model.addAttribute("namedLayer", namedLayer);
    	model.addAttribute("layerParam", layerParam);
        return "sld";
    }
    
    @RequestMapping(value="/map/map.json*", method = RequestMethod.GET)
    public void getJson(           
            @RequestParam(value="guid", defaultValue ="", required=true) String guid,            
            HttpServletResponse response) throws Exception {
    	String jsonString = PageUtils.getUrlContentAsJsonString(PageUtils.SPATIAL_JSON_URL + guid);
    	response.setContentType("application/json;charset=UTF-8");
    	response.setStatus(200);
    	PrintWriter out = response.getWriter();    	
    	out.write(jsonString);
    	out.flush();
    	out.close();
    	response.flushBuffer();
    }
}
