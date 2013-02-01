package org.ala.documentmapper;

import org.ala.model.Licence;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.util.MimeType;
import org.apache.commons.digester.Digester;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

public class AbrsFloraOfOzOnlineXMLDocumentMapper implements DocumentMapper {

    @Override
    public List<ParsedDocument> map(String guid, byte[] content) throws Exception {
        try {


         Digester digester = new Digester();
         digester.setValidating(false);

         digester.addObjectCreate("ROW", FoAReference.class );
         digester.addBeanPropertySetter("ROW/SOURCE", "source" );
//         digester.addBeanPropertySetter("response/request/value", value" );
//         digester.addSetNext( "response/request", "setRequest" );
//
//         digester.addBeanPropertySetter( "response/matches", "matches" );

//         Reader reader = new StringReader(
//                 "<?xml version='1.0' encoding='UTF-8'?>" +
//                 "<response>" +
//                     "<request><name>books</name><value>xml</value></request>" +
//                     "<matches>20</matches>" +
//                 "</response>");
//         Response response = (Response)digester.parse( reader );
//
//         System.out.println( response.toString() );

      } catch( Exception exc ) {
         exc.printStackTrace();
      }
      
      return null;
    }

    @Override
    public void setLicencesMap(Map<String, Licence> licencesMap) {}

    public class FoAReference {

        String source;
        String contributor;
        String description;
        String scientificName;

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getContributor() {
            return contributor;
        }

        public void setContributor(String contributor) {
            this.contributor = contributor;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getScientificName() {
            return scientificName;
        }

        public void setScientificName(String scientificName) {
            this.scientificName = scientificName;
        }
    }
}


