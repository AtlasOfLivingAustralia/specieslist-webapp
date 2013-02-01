package org.ala.csvmapper;

import java.util.HashMap;
import java.util.Map;

public class ANFCCsvMapper implements CsvMapper{
    
    @Override
    public Map<String, String> getParams() {
        Map<String, String> csvMap = new HashMap<String, String>();
        csvMap.put(FILE_NAME_HEADER, "Filename");
        csvMap.put(SCIENTIFIC_NAME_HEADER, "Species name to upload");
        csvMap.put(FAMILY_HEADER, "Family");
        csvMap.put(COUNTRY_HEADER, "Region of origin");
        csvMap.put(CREATOR_HEADER, "Attribution for ALA");
        csvMap.put(RIGHTS_HEADER, "Attribution for ALA");
        csvMap.put(LICENSE_HEADER, "Creative Commons");
        csvMap.put(IS_PART_OF, "http://www.ala.org.au/");
        csvMap.put(IDENTIFIER, "http://www.ala.org.au/");
        csvMap.put(IS_PREFERRED, "isPreferred");
        csvMap.put(BASE_URL, "http://www2.ala.org.au/datasets/profile/dr660/");
        csvMap.put(IMAGE_MAPPING_PATH, "http://www2.ala.org.au/datasets/profile/dr660/full-clean.csv");
        return csvMap;
    }
}