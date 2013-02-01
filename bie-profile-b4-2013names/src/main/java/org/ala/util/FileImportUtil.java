package org.ala.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

public class FileImportUtil {

    private static final String BASE_DIR = "/data/bie-staging/image-imports/";
    private static final String DEFAULT_META_FILE_NAME = "meta.csv";


    public static void main(String[] args) {

    }

    public static List<String> readMetaFile(String infosourceId) throws IOException {
        return readMetaFile(infosourceId, DEFAULT_META_FILE_NAME);
    }

    public static List<String> readMetaFile(String infosourceId, String metaFileName) throws IOException {
        List<String> fileNameList = new ArrayList<String>();

        if (metaFileName.endsWith(".csv")) {
            fileNameList = getFileNameListFromCsv(infosourceId, metaFileName);
        } else if (metaFileName.endsWith(".xls")) {
            fileNameList = getFileNameListFromXls(infosourceId, metaFileName);
        } else {
            System.out.println("Unrecognized meta file format: " + metaFileName);
        }

        return fileNameList;
    }

    private static List<String> getFileNameListFromXls(String infosourceId, String metaFileName) throws IOException {
        List<String> fileNameList = new ArrayList<String>();
        
        WordAndExcelReader.convertXlsToCsv(BASE_DIR, metaFileName);
        fileNameList = getFileNameListFromCsv(infosourceId, metaFileName);
        
        return fileNameList;
        
    }
    
    private static List<String> getFileNameListFromCsv(String infosourceId, String metaFileName) throws IOException {
        List<String> fileNameList = new ArrayList<String>();
        
        InputStream csvIS = new FileInputStream(BASE_DIR + infosourceId + File.separator + metaFileName);
        Reader reader = new InputStreamReader(csvIS);

        CSVReader r = new CSVReader(reader,',','"');
        String[] fields = r.readNext();
        int idx = getIdxForField(fields, "FileName");

        if(idx<0){
            System.out.println("Unable to locate file names in meta file.");
            System.exit(1);
        }

        while((fields = r.readNext())!=null){

            // allow a gap between requests. This will stop us bombarding smaller sites.
            String fileName = fields[idx].trim();
//            System.out.println("File Name: " + fileName);
            fileName = fileName.replaceAll("\"", "");
            fileNameList.add(fileName);
        }
        
        return fileNameList;
    }
    
    /**
     * @param fields
     * @param string
     * @return
     */
    private static int getIdxForField(String[] fields, String header) {
        int i=0;
        for(String field: fields){
            if(header.equalsIgnoreCase(field))
                return i;
            i++;
        }
        return -1;
    }

}
