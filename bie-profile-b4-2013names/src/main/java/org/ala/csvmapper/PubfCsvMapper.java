package org.ala.csvmapper;

import java.util.HashMap;
import java.util.Map;

import org.ala.harvester.LocalCSVHarvester;

public class PubfCsvMapper implements CsvMapper{
    
    @Override
    public Map<String, String> getParams() {
        // TODO Auto-generated method stub
        Map<String, String> csvMap = new HashMap<String, String>();

        csvMap.put(FILE_NAME_HEADER, "FILE_NAME");
        csvMap.put(SCIENTIFIC_NAME_HEADER, "SPECIES");
        csvMap.put(BASE_URL, "http://www2.ala.org.au/datasets/pubf/");
        csvMap.put(IMAGE_MAPPING_PATH, "/data/mapping/perth_urban_bushland_fungi.csv");
        csvMap.put(LICENSE, "CC BY-NC Attribution-Non-Commercial 3.0 Australia");
        csvMap.put(RIGHTS, "Bougher, N.L. (2009). Fungi of the Perth Region and Beyond. Western Australian Naturalists Club (Inc.), Perth, Western ");
        csvMap.put(CREATOR, "Bougher, N.L.");
        csvMap.put(IS_PART_OF, "http://www.fungiperth.org.au/");
        csvMap.put(IDENTIFIER, "http://www.fungiperth.org.au/");

        return csvMap;
    }
}
