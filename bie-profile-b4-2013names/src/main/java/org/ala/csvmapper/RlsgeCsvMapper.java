package org.ala.csvmapper;

import java.util.HashMap;
import java.util.Map;

import org.ala.harvester.LocalCSVHarvester;

public class RlsgeCsvMapper implements CsvMapper{
    
    @Override
    public Map<String, String> getParams() {
        // TODO Auto-generated method stub
        Map<String, String> csvMap = new HashMap<String, String>();

        csvMap.put(FILE_NAME_HEADER, "Filename");
        csvMap.put(SCIENTIFIC_NAME_HEADER, "Scientific name");
        csvMap.put(FAMILY_HEADER, "Family");
        csvMap.put(GENUS_HEADER, "Genus");
        csvMap.put(CREATOR_HEADER, "photographer");
        csvMap.put(RIGHTS_HEADER, "photographer");
        csvMap.put(SYNONYM_HEADER, "Synonyms");
        csvMap.put(DISTRIBUTION_HEADER, "Distribution");
        csvMap.put(HABITAT_HEADER, "Habitat");
        csvMap.put(DIET_HEADER, "Diet");
        csvMap.put(THREAT_HEADER, "Threats");
        csvMap.put(CONSERVATION_STATUS_HEADER, "Threat status");
        csvMap.put(DESCRIPTIVE_TEXT, "Description");
        csvMap.put(BASE_URL, "http://www2.ala.org.au/datasets/dr665/");
        csvMap.put(IMAGE_MAPPING_PATH, "/data/mapping/dr665.csv");
        csvMap.put(LICENSE, "CC BY-NC Attribution-Non-Commercial 3.0 Australia");
        csvMap.put(RIGHTS, "www.reeflifesurvey.com");
        csvMap.put(IS_PART_OF, "http://www.reeflifesurvey.com");
        csvMap.put(IDENTIFIER, "http://www.reeflifesurvey.com");

        return csvMap;
    }
}
