/***************************************************************************
 * Copyright (C) 2009 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ***************************************************************************/
package org.ala.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.ala.csvmapper.CsvMapper;
import org.ala.documentmapper.DocumentMapper;
import org.ala.documentmapper.MappingUtils;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Repository;
import org.ala.repository.Triple;
import org.ala.util.FileImportUtil;
import org.ala.util.MimeType;
import org.ala.util.Response;
import org.ala.util.WebUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import au.com.bytecode.opencsv.CSVReader;

/**
 * CSV file merger
 * 
 * @author Tommy Wang
 */
public class CsvMerger {

    /**
     * Main method for testing this particular Harvester
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        CsvMerger c = new CsvMerger();

        c.start(args);
    }	
    

    /**
     * @see org.ala.harvester.Harvester#start()
     */
    public void start(String[] args) throws Exception {

        String csvFile1 = args[0];
        String csvFile2 = args[1];
        String outputDir = args[2];
        int keyIdx1 = Integer.valueOf(args[3]);
        int keyIdx2 = Integer.valueOf(args[4]);
        
        mergeCsv(csvFile1, csvFile2, outputDir, keyIdx1, keyIdx2);

    }
    
    public static Writer getWriter(String dir) throws IOException {
        FileUtils.forceMkdir(new File(dir));
        File outputFile = new File(dir + File.separator + "output.csv");
        if(outputFile.exists())
            FileUtils.forceDelete(outputFile);
        return new FileWriter(outputFile);
    }
    
    private void mergeCsv(String input1, String input2, String outputDir, int keyIdx1, int keyIdx2) throws Exception {
        List<String[]> fieldsList1 = new ArrayList<String[]>();
        List<String[]> fieldsList2 = new ArrayList<String[]>();
        
        System.out.println("Merging...");
        
        Writer writer = getWriter(outputDir);
        
        CSVReader r1 = getCsvReader(input1);
        CSVReader r2 = getCsvReader(input2);
        String[] fields1 = null;
        String[] fields2 = null;
        
        fields1 = r1.readNext();
        fields2 = r2.readNext();
        
        if (fields1 != null && fields2 != null) {
            writeCsv(fields1, fields2, keyIdx2, writer);
        }
        
        while((fields1 = r1.readNext()) != null) {
            fieldsList1.add(fields1);
        }
        
        while ((fields2 = r2.readNext()) != null) {
            fieldsList2.add(fields2);
        }
        
        for (String[] fl1 : fieldsList1) {
            String key1 = fl1[keyIdx1];
            
            if (key1 != null && !"".equals(key1)) {
                for (String[] fl2 : fieldsList2) {
                    String key2 = fl2[keyIdx2];
                    
                    if (key1.equals(key2)) {
                        writeCsv(fl1, fl2, keyIdx2, writer);
                    }
                }
            }
        }
        writer.flush();
        writer.close();
    }
    
    private void writeCsv(String[] f1, String[] f2, int k2, Writer writer) throws IOException {
        boolean isFirst = true;
        
        for (String f : f1) {
            if (!isFirst) {
                writer.write(",");
            }
            writer.write("\"");
            writer.write(f);
            writer.write("\"");
            isFirst = false;
            writer.flush();
        }
        
        for (int i = 0; i < f2.length; i++) {
            if (i == k2) {
                continue;
            } else {
                writer.write(",");
                writer.write("\"");
                writer.write(f2[i]);
                writer.write("\"");
                writer.flush();
            }
        }
        
        writer.write("\n");
        
        writer.flush();
    }

    private CSVReader getCsvReader(String filePath) throws Exception {
        InputStream csvIS = new FileInputStream(filePath);
        Reader reader = new InputStreamReader(csvIS);

        CSVReader r = new CSVReader(reader,',','"');

        return r;

    }

    private static int getIdxForField(String[] fields, String header) {
        int i=0;
        for(String field: fields){
            if(field.trim().equalsIgnoreCase(header))
                return i;
            i++;
        }
        return -1;
    }

}
