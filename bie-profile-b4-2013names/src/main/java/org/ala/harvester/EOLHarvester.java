package org.ala.harvester;

import org.ala.client.util.RestfulClient;
import org.ala.documentmapper.DocumentMapper;
import org.ala.documentmapper.EolDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Repository;
import org.ala.util.DebugUtils;
import org.ala.util.Response;
import org.ala.util.WebUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.com.bytecode.opencsv.CSVReader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class EOLHarvester implements Harvester {
	protected Logger logger = Logger.getLogger(EOLHarvester.class);
    protected Repository repository;
    protected DocumentMapper documentMapper;
    protected Map<String, String> connectionParams;
	//create restful client with no connection timeout.
	protected RestfulClient restfulClient = new RestfulClient(0);
    protected Map<String, String> hashTable;
    protected ObjectMapper mapper;

    /**
     * Main method for testing this particular Harvester
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String[] locations = {"classpath*:spring.xml"};
        ApplicationContext context = new ClassPathXmlApplicationContext(locations);
        EOLHarvester h = new EOLHarvester();
        Repository r = (Repository) context.getBean("repository");
        h.setDocumentMapper(new EolDocumentMapper());
        h.setRepository(r);
        h.start(1051);
    }        
 
	public EOLHarvester(){
		hashTable = new Hashtable<String, String>();
		hashTable.put("accept", "application/json");

		mapper = new ObjectMapper();
		mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);    							
	}

    private List<Integer> getEolIds(String sciName){
    	List<Integer> idList = new ArrayList<Integer>();
    	Map dynaBean = null;
    	String content = "";
		Object[] resp;
		
		if(sciName == null || sciName.trim().length() == 0){
			return idList;
		}
		
		try {
	    	String eolSearchUrl = "http://www.eol.org/api/search/" + URLEncoder.encode(sciName.trim(), "UTF-8") + ".json";
//	    	String eolSearchUrl = "http://www.eol.org/api/search/" + URLEncoder.encode("Eurytoma helena", "utf-8") + ".json";

			resp = restfulClient.restGet(eolSearchUrl , hashTable);			
			if((Integer) resp[0] == HttpStatus.SC_OK){
				content = resp[1].toString();
				if(content != null && content.length() > "[]".length()){
					dynaBean = mapper.readValue(content, Map.class);
					if(dynaBean != null){
						Object o = dynaBean.get("results");
						List results = null;
						if(o instanceof List){
							results = (List)o;
						}
						else{
							results = new ArrayList();
						}
						
						for(int i = 0; i < results.size(); i++){
							Object result = results.get(i);
							if(result instanceof Map){
								Map entry = (Map)result;
								try{
									Integer id = new Integer(entry.get("id").toString());
									idList.add(id);
								}
								catch(Exception e){
									logger.error(e);
								}								
							}
						}
					}
				}
			} 
			else {
				logger.warn("Unable to process url: " + eolSearchUrl);
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
    	return idList;    	
    }
    	
    @Override
    public void start(int infosourceId) throws Exception {
        this.start(infosourceId,0);
    }

    @Override
    public void start(int infosourceId, int timeGap) throws Exception {
    	InputStream csvIS = null; 
    	String csvUrl = "harvest-seed.txt";
    	HttpClient httpClient = new HttpClient();
    	CSVReader r = null;
    	
    	// get list of scienific name from ws or csv file
        if(csvUrl.startsWith("http")){            
            GetMethod getMethod = new GetMethod(csvUrl);
            httpClient.executeMethod(getMethod);
            csvIS = getMethod.getResponseBodyAsStream();
        } else {
        	//read from file system
            csvIS = this.getClass().getClassLoader().getResourceAsStream(csvUrl);

        	//csvIS = new FileInputStream(csvUrl);
        }
        Reader reader = new InputStreamReader(csvIS);
        r = new CSVReader(reader,';');
		String[] fields = null;

    	try{	        	     
			//traverse list of scientific name
			while((fields = r.readNext()) !=null){
				String sciName = fields[0];
				// get ids from eol
				List<Integer> ids = getEolIds(sciName);
				logger.debug("*** EOL id lookup, sciName: " + sciName + ", id: " + (ids.size() > 0?ids.get(0):-1));
				//used first id only
				//for(int i = 0; i < ids.size(); i++){
				for(int i = 0; i < ids.size(); i = ids.size()){
					// get eol content
		        	String eolPageurl = "http://www.eol.org/api/pages/1.0/"+ ids.get(i) +"?common_names=1&details=1&images=75&subjects=all&text=2";	        	
		            try {
		                Response response = WebUtils.getUrlContentAsBytes(eolPageurl);
		                byte[] responseXML = response.getResponseAsBytes();
		                //get the rank
		                String guid = "http://www.eol.org/pages/" + ids.get(i);
		                if(!new String(responseXML).contains("Unknown identifier")){
		                    List<ParsedDocument> docs = this.documentMapper.map(guid, responseXML);
		                    DebugUtils.debugParsedDocs(docs);
		                    for(ParsedDocument doc: docs){
		                        repository.storeDocument(infosourceId, doc);
		                    }	                    
		                }
		            } catch (Exception e) {
		            	logger.error("Cannot harvest " + eolPageurl);                
		                continue;
		            }
		            Thread.sleep(timeGap);		            
				}				
	        }
    	}
    	catch(Exception ex){
    		logger.error(ex);
    	}    	
    	finally{
    		if(r != null){
    			r.close();
    		}
    	}
    }

    @Override
    public void setConnectionParams(Map<String, String> connectionParams) {
        this.connectionParams = connectionParams;
    }

    @Override
    public void setDocumentMapper(DocumentMapper documentMapper) {
        this.documentMapper =  documentMapper;
    }

    @Override
    public void setRepository(Repository repository) {
        this.repository = repository;
    }      
}


