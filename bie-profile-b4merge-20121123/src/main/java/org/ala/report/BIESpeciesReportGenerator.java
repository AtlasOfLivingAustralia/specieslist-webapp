package org.ala.report;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;

import org.ala.client.util.RestfulClient;
import org.ala.util.SpringUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.ApplicationContext;

import au.com.bytecode.opencsv.CSVWriter;
/**
 * A rough class that can be used to dump species information from BIE webservices. 
 * 
 * arg[0] = absolute file name to dump to
 * args[1] = the suffix to apply to the search.json websevice
 * args[2] = the index fields that need to be dumped 
 * 
 * @author Natasha Carter
 *
 */
public class BIESpeciesReportGenerator {
    public static final String BIE_WS = "http://bie.ala.org.au/search.json";
    public static void main(String[] args) throws Exception {
        if(args.length >2){
            //first arg is the file name
            CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(args[0])), '\t', '"');
            //second arg is query params
            RestfulClient restfulClient = new RestfulClient(0);
            System.out.println(URLEncoder.encode(args[1]));
            String baseURL = BIE_WS + args[1];
            ObjectMapper mapper = new ObjectMapper();
            Object[] resp = restfulClient.restGet(baseURL+"&pageSize=0");
            if((Integer)resp[0] == HttpStatus.SC_OK){
                String content1 = resp[1].toString();
                java.util.HashMap map1 = mapper.readValue(content1, new java.util.HashMap<String,Object>().getClass());
                java.util.Map sr1= (java.util.Map)map1.get("searchResults");
                    int totalRecs = (Integer)sr1.get("totalRecords");
                    System.out.println("Total records: " + totalRecs);
                    int pageSize =1000;
                    int offset =0;
                    String[] fields = args[2].split(",");
                    boolean isOK = true;
                    while(offset <totalRecs && isOK){
                        resp = restfulClient.restGet(baseURL+"&pageSize="+pageSize + "&start="+offset);
                    //third arg is a csv of the fields to dump
                    
                    
                    if((Integer)resp[0] == HttpStatus.SC_OK){
                        String content = resp[1].toString();
                        java.util.HashMap map = mapper.readValue(content, new java.util.HashMap<String,Object>().getClass());
                        //System.out.println(map);
                        System.out.println("Offset: "+ offset);
                        java.util.Map sr= (java.util.Map)map.get("searchResults");
                        java.util.List results = (java.util.List)sr.get("results");
                        //System.out.println(results);
                        for(Object item : results){
                            java.util.Map itemMap = (java.util.Map)item;
                            String[] row = new String[fields.length];
                            for(int i =0;i<fields.length;i++){
                                Object value = itemMap.get(fields[i]);
                                row[i] = value != null ? value.toString():"";
                               // System.out.print(itemMap.get(fields[i]) + "\t" );
                            }
                            csvWriter.writeNext(row);
                            //System.out.println();
                                
        //                        System.out.print(itemMap.get(field) + "\t" );
        //                    System.out.println();
                            //System.out.println(itemMap);
                        }
                        offset +=pageSize;
                    }
                    else
                        isOK = false;
                    }
            }
            csvWriter.flush();
            csvWriter.close();

        }
        else{
            System.out.println("Please provide args: <filename> <webservice suffix> <index fields to dump>");
        }
    }
}
