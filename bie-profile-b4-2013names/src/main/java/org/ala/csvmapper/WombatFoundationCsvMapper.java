package org.ala.csvmapper;

import java.util.HashMap;
import java.util.Map;

import org.ala.harvester.LocalCSVHarvester;

public class WombatFoundationCsvMapper implements CsvMapper{

    @Override
    public Map<String, String> getParams() {
        // TODO Auto-generated method stub
        Map<String, String> csvMap = new HashMap<String, String>();

        csvMap.put(FILE_NAME_HEADER, "FILE_NAME");
        csvMap.put(SCIENTIFIC_NAME_HEADER, "SPECIES");
        csvMap.put(BASE_URL, "http://www2.ala.org.au/datasets/wombat_foundation/");
        csvMap.put(IMAGE_MAPPING_PATH, "/data/mapping/wombat_foundation.csv");
        csvMap.put(LICENSE, "CC-BY-NC");
        csvMap.put(RIGHTS, "Copyright Fran McFadzen");
        csvMap.put(CREATOR, "Fran McFadzen");
        csvMap.put(DESCRIPTIVE_TEXT, "Description");
        csvMap.put(IS_PART_OF, "http://www.wombatfoundation.com.au/nhnw.htm");

        return csvMap;
    }

}
