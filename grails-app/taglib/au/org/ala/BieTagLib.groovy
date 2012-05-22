package au.org.ala

class BieTagLib {
    static namespace = 'bie'     // namespace for headers and footers

    /**
     * Format a scientific name with appropriate italics depending on rank
     *
     * @attr name REQUIRED the scientific name
     * @attr rankId REQUIRED the rank id
     */
    def formatSciName = { attrs ->
        def rankId = attrs.rankId
        def name = attrs.name

        if (rankId >= 600) {
            out << "<i>" + name + "</i>"
        } else {
            out << name
        }
    }

    /**
     * Output the colour name for a given conservationstatus
     *
     * @attr status REQUIRED the conservation status
     */
    def colourForStatus = { attrs ->
//        <g:if test="${status.status ==~ /extinct$/}"><span class="iucn red"><g:message code="region.${regionCode}"/><!--EX--></span></g:if>
//        <g:elseif test="${status.status ==~ /(?i)wild/}"><span class="iucn red"><g:message code="region.${regionCode}"/><!--EW--></span></g:elseif>
//        <g:elseif test="${status.status ==~ /(?i)Critically/}"><span class="iucn yellow"><g:message code="region.${regionCode}"/><!--CR--></span></g:elseif>
//        <g:elseif test="${status.status ==~ /(?i)^Endangered/}"><span class="iucn yellow"><g:message code="region.${regionCode}"/><!--EN--></span></g:elseif>
//        <g:elseif test="${status.status ==~ /(?i)Vulnerable/}"><span class="iucn yellow"><g:message code="region.${regionCode}"/><!--VU--></span></g:elseif>
//        <g:elseif test="${status.status ==~ /(?i)Near/}"><span class="iucn green"><g:message code="region.${regionCode}"/><!--NT--></span></g:elseif>
//        <g:elseif test="${status.status ==~ /(?i)concern/}"><span class="iucn green"><g:message code="region.${regionCode}"/><!--LC--></span></g:elseif>
//        <g:else><span class="iucn green"><g:message code="region.${regionCode}"/><!--LC--></span></g:else>
        def status = attrs.status
        def colour

        switch ( status ) {
            case ~/extinct$/:
            case ~/(?i)wild/:
                colour = "red"
                break
            case ~/(?i)Critically/:
            case ~/(?i)^Endangered/:
            case ~/(?i)Vulnerable/:
                colour = "yellow"
                break
            case ~/(?i)Near/:
            case ~/(?i)concern/:
            default:
                colour = "green"
                break
        }

        out << colour
    }
}