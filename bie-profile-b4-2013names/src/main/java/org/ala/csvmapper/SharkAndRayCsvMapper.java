package org.ala.csvmapper;

import java.util.HashMap;
import java.util.Map;

import org.ala.harvester.LocalCSVHarvester;

public class SharkAndRayCsvMapper implements CsvMapper{
    
    @Override
    public Map<String, String> getParams() {
        // TODO Auto-generated method stub
        Map<String, String> csvMap = new HashMap<String, String>();

        csvMap.put(FILE_NAME_HEADER, "Filename");
        csvMap.put(SCIENTIFIC_NAME_HEADER, "AFD Species name");
//        csvMap.put(LocalCSVHarvester.SCIENTIFIC_NAME_HEADER, "Family");
        csvMap.put(FAMILY_HEADER, "Family");
        csvMap.put(COUNTRY_HEADER, "Country of origin");
        csvMap.put(CREATOR_HEADER, "Copyright");
        csvMap.put(RIGHTS_HEADER, "Copyright");
        csvMap.put(COMMENT_HEADER, "Additional comment (to load)");
        csvMap.put(BASE_URL, "http://www2.ala.org.au/datasets/sharkandray/");
        csvMap.put(URL_APPENDIX, "#Genus");
        csvMap.put(IMAGE_MAPPING_PATH, "/data/mapping/shark_and_ray.csv");
        csvMap.put(LICENSE, "Creative Common Attribution 3.0 Australia");
//        csvMap.put(LocalCSVHarvester.RIGHTS, "Bougher, N.L. (2009). Fungi of the Perth Region and Beyond. Western Australian Naturalists Club (Inc.), Perth, Western ");
//        csvMap.put(LocalCSVHarvester.CREATOR, "Bougher, N.L.");
        csvMap.put(IS_PART_OF, "http://www.ala.org.au/");
        csvMap.put(IDENTIFIER, "http://www.ala.org.au/");
        csvMap.put(IS_PREFERRED, "isPreferred");

        return csvMap;
    }
}


/*
Filename,
Species name to upload,
Comment on name,
Fam#,
Family,
Attribution for ALA,
CAAB number,
Region of origin,
Creative Commons,
IP attribution,
Image quality

 */
