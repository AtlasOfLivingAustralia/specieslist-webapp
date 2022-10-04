package au.org.ala.specieslist

import au.org.ala.names.ws.api.NameSearch
import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import org.apache.commons.lang3.StringUtils


/**
 * A service that matches column names from/to KVP values
 */
class ColumnMatchingService implements GrailsConfigurationAware {
    ColumnMatcher speciesNameMatcher = new ColumnMatcher('rawScientificName', 'name')
    ColumnMatcher authorMatcher = new ColumnMatcher('author', 'author')
    ColumnMatcher commonNameMatcher = new ColumnMatcher('commonName', 'commonName')
    ColumnMatcher ambiguousNameMatcher = new ColumnMatcher('ambiguousName', null)
    ColumnMatcher kingdomMatcher = new ColumnMatcher('kingdom', 'kingdom')
    ColumnMatcher phylumMatcher = new ColumnMatcher('phylum', 'phylum')
    ColumnMatcher classMatcher = new ColumnMatcher('class', 'class')
    ColumnMatcher orderMatcher = new ColumnMatcher('order', 'order')
    ColumnMatcher familyMatcher = new ColumnMatcher('family', 'family')
    ColumnMatcher genusMatcher = new ColumnMatcher('genus', 'genus')
    ColumnMatcher rankMatcher = new ColumnMatcher('rank', 'rank')
    boolean loose = false

    @Override
    void setConfiguration(Config configuration) {
        this.speciesNameMatcher = new ColumnMatcher('rawScientificName', configuration.getProperty("speciesNameColumns"))
        this.authorMatcher = new ColumnMatcher('author', configuration.getProperty("authorColumns"))
        this.commonNameMatcher = new ColumnMatcher('commonName', configuration.getProperty("commonNameColumns"))
        this.ambiguousNameMatcher = new ColumnMatcher('ambiguousName', configuration.getProperty("ambiguousNameColumns"))
        this.kingdomMatcher = new ColumnMatcher('kingdom', configuration.getProperty("kingdomColumns"))
        this.phylumMatcher = new ColumnMatcher('phylum', configuration.getProperty("phylumColumns"))
        this.classMatcher = new ColumnMatcher('class', configuration.getProperty("classColumns"))
        this.orderMatcher = new ColumnMatcher('order', configuration.getProperty("orderColumns"))
        this.familyMatcher = new ColumnMatcher('family', configuration.getProperty("familyColumns"))
        this.genusMatcher = new ColumnMatcher('genus', configuration.getProperty("genusColumns"))
        this.rankMatcher = new ColumnMatcher('rank', configuration.getProperty("rankColumns"))
        this.loose = configuration.getProperty("namematching.loose", Boolean.class, false)
    }

    /**
     * Build a name search for a species list item.
     * <p>
     * The scientific name comes from the raw scientifc name.
     * All other values come from additional KVP values.
     * </p>
     *
     * @param sli The species list item
     * @return The name search
     */
    NameSearch buildSearch(SpeciesListItem sli) {
        return NameSearch.builder()
                .scientificName(sli.rawScientificName)
                .scientificNameAuthorship(this.authorMatcher.get(sli))
                .kingdom(this.kingdomMatcher.get(sli))
                .phylum(this.phylumMatcher.get(sli))
                .clazz(this.classMatcher.get(sli))
                .order(this.orderMatcher.get(sli))
                .family(this.familyMatcher.get(sli))
                .genus(this.genusMatcher.get(sli))
                .rank(this.rankMatcher.get(sli))
                .vernacularName(this.commonNameMatcher.get(sli))
                .loose(this.loose)
                .build();
    }

    /**
     * determines what the header should be based on the data supplied
     * @param header
     */
    def parseData(String[] header) {
        def hasName = false
        def unknowni = 1
        def headerResponse = header.collect {
            if (findAcceptedLsidByScientificName(it)) {
                hasName = true
                "scientific name"
            } else if (findAcceptedLsidByCommonName(it)) {
                hasName = true
                "vernacular name"
            } else {
                "UNKNOWN" + (unknowni++)
            }
        }
        [header: headerResponse, nameFound: hasName]
    }

    /**
     * Check find suitable names from a header
     *
     * @param header The header
     * 
     * @return The results
     */
    def parseHeader(String[] header) {
        //first step check to see if scientificname or common name is provided as a header
        def hasName = false;
        def headerResponse = header.collect {
            def search = canonicalName(it)
            if (this.speciesNameMatcher.hasName(search)) {
                hasName = true
                "scientific name"
            } else if (this.commonNameMatcher.hasName(search)) {
                hasName = true
                "vernacular name"
            } else if (this.ambiguousNameMatcher.hasName(search)) {
                hasName = true
                "ambiguous name"
            } else {
                it
            }
        }

        headerResponse = parseHeadersCamelCase(headerResponse)

        if (hasName)
            [header: headerResponse, nameFound: hasName]
        else
            null
    }

    // specieslist-webapp#50
    protected parseHeadersCamelCase(List header) {
        def ret = []
        header.each { String it ->
            StringBuilder word = new StringBuilder()
            if (Character.isUpperCase(it.codePointAt(0))) {
                for (int i = 0; i < it.size(); i++) {
                    if (Character.isUpperCase(it[i] as char) && i != 0) {
                        word << " "
                    }
                    word << it[i]
                }

                ret << word.toString()
            } else {
                ret << it
            }
        }
        ret
    }

    int getSpeciesIndex(Object[] header) {
        int idx = header.findIndexOf { this.speciesNameMatcher.hasName(this.canonicalName(it)) }
        if (idx < 0)
            idx = header.findIndexOf { this.ambiguousNameMatcher.hasName(this.canonicalName(it)) }
        if (idx < 0)
            idx = header.findIndexOf { this.commonNameMatcher.hasName(this.canonicalName(it)) }
        return idx
    }

    /**
     * Build a map locating various terms in an array for the various columns.
     *
     * @param header The header
     * @return The column-name - column index map
     */
    Map getTermAndIndex(Object[] header){
        Map termMap = new HashMap<String, Integer>();
        this.speciesNameMatcher.locate(header, termMap)
        this.commonNameMatcher.locate(header, termMap)
        this.kingdomMatcher.locate(header, termMap)
        this.phylumMatcher.locate(header, termMap)
        this.classMatcher.locate(header, termMap)
        this.orderMatcher.locate(header, termMap)
        this.familyMatcher.locate(header, termMap)
        this.genusMatcher.locate(header, termMap)
        this.rankMatcher.locate(header, termMap)
        return termMap
    }

    protected String canonicalName(String name) {
        return name.toLowerCase().replaceAll('\\s', '')
    }

    class ColumnMatcher {
        String column;
        String[] names
        Set<String> matches
        
        ColumnMatcher(String column, String names) {
            this.column = column;
            this.names = names?.split(',') ?: []
            this.matches = this.names.inject([], { v, s -> v.add(canonicalName(s)); v}) as Set
        }

        /**
         * Get a KVP value from an item, based on possible column names.
         *
         * @param sli The species list item
         * @return The first matching value, or null for not found
         */
        String get(SpeciesListItem sli) {
            for (String name : this.names) {
                SpeciesListKVP kvp = sli.kvpValues.find { it.key == name }
                String value = kvp ? StringUtils.trimToNull(kvp.value) : null
                if (value)
                    return value
            }
            return null
        }

        /**
         * See if we have a specific name.
         * @param name A pre-canonicalised name
         * @return True if this matches
         */
        boolean hasName(String name) {
            name = canonicalName(name)
            return this.matches.contains(name)
        }

        int locate(Object[] header, Map map) {
            int idx = header.findIndexOf { hasName(it.toString()) }
            if (idx != -1) {
                map.put(this.column, idx)
            }
            return idx
        }
    }
}