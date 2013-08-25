package au.org.ala.bie.webapp2

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.apache.commons.lang.StringUtils
import grails.converters.JSON

class UtilityService {

    def grailsApplication
    def webService
    /**
     * Get a map of region names to collectory codes
     *
     * @return regions map
     */
    def getStatusRegionCodes = {
        // copied from Spring webapp
        Map regions = new HashMap<String, String>();
        regions.put("IUCN", "dr657");
        regions.put("Australia", "dr656");
        regions.put("Australian Capital Territory", "dr649");
        regions.put("New South Wales", "dr650");
        regions.put("Northern Territory", "dr651");
        regions.put("Queensland", "dr652");
        regions.put("South Australia", "dr653");
        regions.put("Tasmania", "dr654");
        regions.put("Victoria", "dr655");
        regions.put("Western Australia", "dr467");
        return regions;
    }

   /**
    * Filter a list of SimpleProperty objects so that the resulting list only
    * contains objects with a name ending in "Text". E.g. "hasDescriptionText".
    *
    * @param etc
    * @return
    */
    def filterSimpleProperties(etc) {
        def simpleProperties = etc.simpleProperties
        def textProperties = []
        def nonTruncatedSources = ConfigurationHolder.config.nonTruncatedSources
        //we only want the list to store the first type for each source
        Hashtable<String, Object> processProperties = new Hashtable<String, Object>();
        simpleProperties.each { sp ->
            def thisPropertyStr = sp.name + sp.infoSourceName

            if (sp.name?.endsWith("Text") || sp.name?.endsWith("hasPopulateEstimate")) {
                def existing = processProperties.get(thisPropertyStr)

                if (existing) {
                    //separate paragraphs using br's instead of p so that the citation is aligned correctly
                    // TODO: not sure this is doing anything (taken from legacy app)
                    existing.value = existing.value + "<br><br>" + sp.value;
                } else {
                    processProperties.put(thisPropertyStr, sp);
                }
            }
        }

        def processPropertiesList = Collections.list(processProperties.elements())
        processPropertiesList.each { sp ->
            if (!nonTruncatedSources.contains(sp.infoSourceURL)) {
                sp.value = truncateTextBySentence(sp.value, 300)
            }
            textProperties.add(sp)
        }
        textProperties;
    }

    /**
     * Truncates the text at a sentence break after min length
     * @param text
     * @param min
     * @return
     */
    def truncateTextBySentence(String text, int min) {
        try {
            if (text != null && text.length() > min) {
                java.text.BreakIterator bi = java.text.BreakIterator.getSentenceInstance();
                bi.setText(text);
                int finalIndex = bi.following(min);
                return text.substring(0, finalIndex) + "...";
            }
        } catch (Exception e) {
            log.debug("Unable to truncate " + text, e);
        }
        text;
    }

    def getInfoSourcesForTc(etc) {
        def infoSourceMap = new TreeMap() // so it keeps its sorted order
        def selectedSections = []
        //log.debug "etc = " + etc.simpleProperties
        selectedSections.addAll(etc.simpleProperties)

        selectedSections.each {
            def identifier = it.infoSourceURL
            def name = it.infoSourceName
            def property = (it.name?.startsWith("http://ala.org.au/ontology/ALA")) ? it.name?.replaceFirst(/^.*#/, "") : null

            if (identifier && name && property) {
                if (infoSourceMap.containsKey(identifier)) {
                    def mapValue = infoSourceMap.get(identifier)
                    def newValues = mapValue.sections as Set
                    newValues.add(property)
                    infoSourceMap.put(identifier, [name: name, sections: newValues])
                } else {
                    infoSourceMap.put(identifier, [name: name, sections: [property]])
                }
            }
        }

        if(log.debugEnabled) log.debug "1. infoSourceMap = " + infoSourceMap
        infoSourceMap = infoSourceMap.sort {a, b -> a.value.name <=> b.value.name}
        if(log.debugEnabled) log.debug "2. infoSourceMap = " + infoSourceMap

        infoSourceMap
    }

    def unDuplicateNames(names) {
        def namesSet = []

        names.eachWithIndex { it, i ->
            if (names.length() == 1 || normaliseString(it.nameString) != normaliseString(names[i - 1]?.nameString)
                    || it.infoSourceName != names[i - 1]?.infoSourceName) {
                namesSet.add(it)
            } else {
                log.debug i + " dupe not added: "  + normaliseString(it.nameString) + "=" + it.infoSourceName + " | " +
                        normaliseString(names[i - 1]?.nameString) + "=" + names[i - 1]?.infoSourceName
            }
        }
        log.debug "namesSet: ${namesSet}"
        namesSet
    }

    /**
     * Group names which are equivalent into a map with a list of their name objects
     *
     * @param names
     * @return
     */
    def getNamesAsSortedMap(commonNames) {
        def sortedNames = unDuplicateNames(commonNames)
        def names2 = new ArrayList(sortedNames) // take a copy
        def namesMap = [:] as LinkedHashMap // Map of String, List<CommonNames>

        names2.eachWithIndex { name, i ->
            def nameKey = name.nameString.trim()
            def tempGroupedNames = [name]

            if (name.nameString?.replaceAll(/[^a-zA-Z0-9]/, "").trim().toLowerCase() ==  names2[i - 1]?.nameString?.replaceAll(/[^a-zA-Z0-9]/, "").trim().toLowerCase()) {
                // existing name (allowing for slight differences in ws & punctuation, etc
                nameKey = names2[i - 1].nameString.trim()

                if (namesMap.containsKey(nameKey)) {
                    tempGroupedNames.addAll(namesMap[nameKey])
                }
            }

            namesMap.put(nameKey, tempGroupedNames)
        }

        namesMap
    }

    def normaliseString(input) {
        input.replaceAll(/([.,-]*)?([\\s]*)?/, "").trim().toLowerCase()
    }

    def addFacetMap(List list) {
        def facetMap = [:]
        list.each {
            if (it.contains(":")) {
                String[] fqBits = StringUtils.split(it, ":", 2);
                facetMap.put(fqBits[0], fqBits[-1]?:"");
            }
        }
        facetMap
    }

    def addFqUidMap(List fqs){
        //TODO have an expiring cache of collectory items so that we don't have to lookup the details every time.
        def map = [:]
        try{
            fqs.each {
                if(it.contains("uid:")){
                    String[] fqBits = StringUtils.split(it, ":", 2)
                    if(fqBits != null && fqBits.length > 1 && !"".equals(fqBits[1])){
                        String uid = fqBits[1]
                        String type = (uid.startsWith("dr")) ? "dataResource" :
                                      (uid.startsWith("dp")) ? "dataProvider" :
                                      (uid.startsWith("co")) ? "collection" :
                                      (uid.startsWith("in")) ? "institution" : null
                        if(type != null){
                            String url = grailsApplication.config.collectory.baseURL+"/ws/"+type+"/"+uid
                            def json = webService.get(url)
                            Map wsmap =JSON.parse(json)
                            map.putAt(uid, wsmap.get("name"))
                            map.put(uid+"_resourceType", wsmap.get("resourceType"))
                        }
                    }
                }
            }
        } catch(Exception e){
            log.error("Unable to get collectory information.",e)
        }
        if(map.size()>0){
            log.debug("Collectory UID map for filters " + map)
        }

        map
    }

    def getIdxtypes(facetResults) {
        def idxtypes = [] as Set

        facetResults.each { facet ->
            if (facet.fieldName == "idxtype") {
                facet.fieldResult.each { field ->
                    idxtypes.add(field.label)
                }
            }
        }
        idxtypes
    }

    def getJsonMimeType(params) {
        (params.callback) ? "text/javascript" : "application/json"
    }
}
