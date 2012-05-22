package au.org.ala.bie.webapp2

class LookupService {

    def getStatusRegionCodes() {
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
}
