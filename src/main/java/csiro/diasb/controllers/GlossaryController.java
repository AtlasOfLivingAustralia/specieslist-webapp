/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package csiro.diasb.controllers;

import com.opensymphony.xwork2.ActionSupport;
import java.util.HashMap;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;

/**
 *
 * @author oak021
 */
public class GlossaryController extends ActionSupport {

    private HashMap<String, String> namespaceMap =
            new HashMap<String, String>(1);
   
    /**
     * Shows a list of the current namespaces
     * @return
     */
    public HttpHeaders index() {
        init();
        return new DefaultHttpHeaders("index").disableCaching();
    }
    public HttpHeaders show() {        
        init();
        return new DefaultHttpHeaders("index").disableCaching();
    }
    private void init()
    {
        namespaceMap.put("TaxonRank", "http://rs.tdwg.org/ontology/voc/TaxonRank");
        namespaceMap.put("TaxonName", "http://rs.tdwg.org/ontology/voc/TaxonName");
        namespaceMap.put("TaxonConcept", "http://rs.tdwg.org/ontology/voc/TaxonConcept");
        namespaceMap.put("PublicationCitation","http://rs.tdwg.org/ontology/voc/PublicationCitation" );
        namespaceMap.put("Common","http://rs.tdwg.org/ontology/voc/Common" );
        namespaceMap.put("TaxonOccurrence", "http://rs.tdwg.org/ontology/voc/TaxonOccurrence");
        namespaceMap.put("TDWGPublication", "http://lsid.tdwg.org/urn:lsid:biodiversity.org.au:afd.publication");
        namespaceMap.put("BDPublication", "http://biodiversity.org.au/afd.publication");
    }
     public HashMap<String, String> getNamespaceMap() {
        return namespaceMap;
    }
    public void setNamespaceMap(HashMap<String, String> namespaceMap) {
        this.namespaceMap = namespaceMap;
    }
}
