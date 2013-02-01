/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 *
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/

package org.ala.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ala.repository.Triple;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Component;

/**
 * Read a DC or RDF file to produce a List of String[]'s
 * (based on CvsReader API)
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
@Component("repositoryFileUtils")
public class RepositoryFileUtils {
    private static final Logger logger = Logger.getLogger(RepositoryFileUtils.class.getName());

    /**
     * For a given (absolute) file name string, read the file and return as a list
     * of String[] arrays (2 column for dc files and 3 columns for rdf files)
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    public List<String[]> readRepositoryFile(String fileName) throws Exception {
        List<String[]> lines = new ArrayList<String[]>();
        File file = new File(fileName);
        if (file.canRead()) {
            lines = readRepositoryFile(file);
        }
        return lines;
    }

    /**
     * For a given java.io.File, read the file and return as a list
     * of String[] arrays (2 column for dc files and 3 columns for rdf files)
     *
     * @param file
     * @return
     * @throws Exception
     */
    public List<String[]> readRepositoryFile(File file) throws Exception {
        String fileName = file.getName();
        List<String[]> lines = new ArrayList<String[]>();

        if (fileName.matches(FileType.DC.getFilename())) {
            lines = readDcFile(file);
        } else if (fileName.matches(FileType.RDF.getFilename())) {
            lines = readRdfFile(file);
        }

        return lines;
    }

     /**
     * Parse JSON encoded text file
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private List<String[]> readDcFile(File file) throws Exception {
        Map<String,String> jsonMap = readDcFileAsMap(file);
        return deserialiseJsonMap(jsonMap);
    }
    
    /**
     * Parse JSON encoded text file
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static Map<String,String> readDcFileAsMap(File file) throws Exception {
        String jsonString = FileUtils.readFileToString(file);
        ObjectMapper o = new ObjectMapper();
        Map<String,String> jsonMap = o.readValue(jsonString, new TypeReference<Map<String,String>>(){});
        return jsonMap;
    }

    /**
     * Parse turle format RDF file with TurtleUtils
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws Exception
     */
    private List<String[]> readRdfFile(File file) throws FileNotFoundException, Exception {
        List<Triple<String,String,String>> triples = TurtleUtils.readTurtle(new FileReader(file), false);
        return deserialiseTriples(triples);
    }

    /**
     * Deserialise list of Triples into a list of String arrays
     *
     * @param triples
     * @return array of Strings
     */
    private List<String[]> deserialiseTriples(List<Triple<String,String,String>> triples) {
        List<String[]> lines =  new ArrayList<String[]>();

        for (Triple<String,String,String> t : triples) {
            String[] fields = {t.getSubject(),  t.getPredicate(), t.getObject()};
            lines.add(fields);
            logger.debug("triple: "+StringUtils.join(fields, " | "));
        }

        return lines;
    }

    /**
     * Deserialise Map<String,String> into a list of String arrays
     * 
     * @param jsonMap
     * @return array of Strings
     */
    private List<String[]> deserialiseJsonMap(Map<String, String> jsonMap) {
        List<String[]> lines =  new ArrayList<String[]>();
        Iterator it = jsonMap.entrySet().iterator();
        
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            if(logger.isTraceEnabled()){
            	logger.trace("deserialiseJsonMap: "+ pairs.getKey() + " = " + pairs.getValue());
            }
            String[] fields = {(String) pairs.getKey(), (String) pairs.getValue()};
            lines.add(fields);
        }

        return lines;
    }
}
