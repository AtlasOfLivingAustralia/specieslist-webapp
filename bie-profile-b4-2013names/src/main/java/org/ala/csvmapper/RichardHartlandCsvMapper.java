package org.ala.csvmapper;

import java.util.HashMap;
import java.util.Map;

import org.ala.harvester.LocalCSVHarvester;

public class RichardHartlandCsvMapper implements CsvMapper{

    @Override
    public Map<String, String> getParams() {
        // TODO Auto-generated method stub
        Map<String, String> csvMap = new HashMap<String, String>();

        csvMap.put(FILE_NAME_HEADER, "FILE");
        csvMap.put(FILE_PATH_HEADER, "PATH");
        csvMap.put(SCIENTIFIC_NAME_HEADER, "SPECIES");
        csvMap.put(BASE_URL, "http://www2.ala.org.au/datasets/dr534/");
        csvMap.put(IMAGE_MAPPING_PATH, "/data/mapping/dr534.csv");
        csvMap.put(LICENSE, "CC BY - Creative Commons Attribution 3.0");
        csvMap.put(RIGHTS, "Copyright Richard Hartland");
        csvMap.put(CREATOR, "Richard Hartland");

        return csvMap;
    }

}
