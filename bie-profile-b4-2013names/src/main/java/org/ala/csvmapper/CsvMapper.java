package org.ala.csvmapper;

import java.util.Map;

/*
 * CsvMapper is used to provide params for the LocalCSVHarvester. 
 */

public interface CsvMapper {

    public final static String FILE_NAME_HEADER = "fileName";
    public final static String FILE_PATH_HEADER = "filePath";
    public final static String SCIENTIFIC_NAME_HEADER = "scientificName";
    public final static String FAMILY_HEADER = "family";
    public final static String GENUS_HEADER = "genus";
    public final static String COUNTRY_HEADER = "country";
    public final static String CREATOR_HEADER = "creator";
    public final static String RIGHTS_HEADER = "rightsHeader";
    public final static String LICENSE_HEADER = "licence";
    public final static String COMMENT_HEADER = "comment";
    public final static String SYNONYM_HEADER = "synonym";
    public final static String DISTRIBUTION_HEADER = "distribution";
    public final static String HABITAT_HEADER = "habitat";
    public final static String DIET_HEADER = "diet";
    public final static String THREAT_HEADER = "threat";
    public final static String CONSERVATION_STATUS_HEADER = "conservationStatus";
    public final static String BASE_URL = "baseUrl";
    public final static String URL_APPENDIX = "urlAppendix";
    public final static String IMAGE_MAPPING_PATH = "mappingFilePath";
    public final static String LICENSE = "license";
    public final static String RIGHTS = "rights";
    public final static String CREATOR = "creator";
    public final static String DESCRIPTIVE_TEXT = "Description";
    public final static String IS_PART_OF = "IsPartOf";
    public final static String IDENTIFIER = "identifier";
    public final static String IS_PREFERRED = "isPreferred";

    /*
     * Get a hash map of all the required params
     */
    public Map<String, String> getParams();
}
