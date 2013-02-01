package org.ala.hbase;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import au.com.bytecode.opencsv.CSVReader;
import org.ala.client.util.RestfulClient;
import org.ala.dao.TaxonConceptDao;
import org.ala.model.Category;
import org.ala.model.CommonName;
import org.ala.util.SpringUtils;
import org.ala.util.WebUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.org.ala.data.model.LinnaeanRankClassification;

@Component("genericCSVLoader")
public abstract class GenericCSVLoader {

    @Inject
    protected TaxonConceptDao taxonConceptDao;
    protected RestfulClient restfulClient = new RestfulClient(0);
    protected static Logger logger = Logger.getLogger(GenericCSVLoader.class);
    protected Set<String> lsidsToReindex;
    protected FileOutputStream reindexOut;
    protected static final String reindexFile = "/data/bie-staging/profile/reindex.out";

    public abstract void processLine(Map resourceMap, String[] row, String[] header);

    public void load(String dataResource) {

        System.out.println("Starting to load...");
        Map<String, Object> colMap = getDetailsFromCollectory(dataResource);
        lsidsToReindex = new HashSet<String>();
        try {
            Map<String,String> connParams = (Map<String,String>) colMap.get("connectionParameters");
            String fileUrl = (String) connParams.get("url");
            System.out.println("using file: " + fileUrl);
            char separator = ((String) connParams.get("csv_delimiter")).charAt(0);
            InputStream csvData = WebUtils.getUrlContent(fileUrl);
            CSVReader reader = new CSVReader(new InputStreamReader(csvData), separator);
            reindexOut = FileUtils.openOutputStream(new File(reindexFile));
            String[] header = reader.readNext();
            System.out.println("The header " + header[0]);
            //to do map the header row to valid DWC field names.
            String[] line = reader.readNext();
            while (line!=null) {
                System.out.println(line[0] + " - " + line[1]);
                processLine(colMap, line, header);
                line = reader.readNext();
            }
            for (String lsid : lsidsToReindex){
                reindexOut.write((lsid + "\n").getBytes());
            }
            reindexOut.flush();
            reindexOut.close();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String getValue(String field, Map<String, String> map) {
        return StringUtils.stripToNull(map.get(field));
    }

    protected Map<String, Object> getDetailsFromCollectory(String druid) {
        String url = "http://collections.ala.org.au/ws/dataResource/" + druid + ".json";
        ObjectMapper mapper = new ObjectMapper();

        try {
            Object[] resp = restfulClient.restGet(url);
            if ((Integer) resp[0] == HttpStatus.SC_OK) {
                String content = resp[1].toString();
                System.out.println(content);
                Map<String, Object> values = mapper.readValue(resp[1].toString(), new TypeReference<Map<String, Object>>() {});
                return values;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Map<String, String> toMap(String[] keys, String[] values) {
        int keysSize = (keys != null) ? keys.length : 0;
        int valuesSize = (values != null) ? values.length : 0;

        if (keysSize == 0 && valuesSize == 0) {
            // return mutable map
            return new HashMap<String, String>();
        }

        if (keysSize != valuesSize) {
            logger.warn("Unable to load " + StringUtils.join(keys, ",") + " as it does not have the same rows as the header.");
            return null;
        }

        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < keysSize; i++) {
            map.put(keys[i], values[i]);
        }

        return map;
    }
}
