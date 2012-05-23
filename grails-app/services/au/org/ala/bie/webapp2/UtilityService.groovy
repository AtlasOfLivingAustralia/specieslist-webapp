package au.org.ala.bie.webapp2

import org.codehaus.groovy.grails.commons.ConfigurationHolder

class UtilityService {
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

        return textProperties;
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
        return text;
    }
}
