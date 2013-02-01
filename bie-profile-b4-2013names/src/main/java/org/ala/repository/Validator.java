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
package org.ala.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.DateValidator;
import org.apache.log4j.Logger;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import au.com.bytecode.opencsv.CSVReader;
import javax.inject.Inject;

import org.ala.util.FileType;
import org.ala.util.MimeType;
import org.ala.util.RepositoryFileUtils;


/**
 * Validate the "dc" and "rdf" files produced by the 
 * {@see org.ala.repository.RepositoryImpl RepositoryImpl} class.
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
@Component("validator")
public class Validator {
    private static final Logger logger = Logger.getLogger(Validator.class.getName());
    private String rootDirectory = File.separator+"data"+File.separator+"bie"; // can be overridden in overrides.properties
    private final char DELIMITER = '\t';
    private Integer infoSourceId = null;
    private boolean useTurtle = true;  // can be overridden in overrides.properties
    @Inject
    private RepositoryFileUtils rfu;

    /**
     * Constructor to set infoSourceId
     *
     * @param isid the info source id to set
     */
    public Validator(Integer isid) {
        this.infoSourceId = isid;
    }

    /**
     * Constructor
     */
    public Validator() {}

    /**
     * Main method
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring.xml");
        Validator validator = (Validator) context.getBean("validator");
        Integer id = 1008;
        if (args.length > 0) {
            id = Integer.parseInt(args[0]);
        } 
        validator.setInfoSourceId(id);
        validator.findAndValidateFiles();
    }

    /**
     * File system crawler - looks for files of interest and triggers
     * validator methods.
     *
     * @throws Exception
     */
    public void findAndValidateFiles() throws Exception {
        IOFileFilter fileFilter = new IOFileFilter() {
            // AIC implementation of interface
            @Override
            public boolean accept(File file) {
                validateFile(file);
                return false;
            }
            @Override
            public boolean accept(File dir, String name) {
                return true;
            }
        };

        IOFileFilter dirFilter = new IOFileFilter() {
            // AIC implementation of interface
            @Override
            public boolean accept(File file) {
                //logger.debug("isId = "+infoSourceId+"|dir = "+file.getParent()+"|name = "+file.getName()+
                //      "|"+rootDirectory+File.separator+infoSourceId+File.separator);
                if (infoSourceId !=null && (rootDirectory.equals(file.getParent()) && file.getName().equals(infoSourceId.toString()) ) ) {
                    logger.info("Crawling dir: "+file.getAbsolutePath());
                    return true;
                } else if (infoSourceId !=null && file.getParent().startsWith(rootDirectory+File.separator+infoSourceId)) {
                    logger.debug("crawling...");
                    return true;
                } else if (infoSourceId !=null) {
                    return false;
                } else {
                    return true;
                }
            }
            @Override
            public boolean accept(File dir, String name) {
               return true;
            }
        };

        logger.info("Validating repository files...");
        FileUtils.listFiles(new File(rootDirectory), fileFilter, dirFilter);
        logger.info("Validating completed.");
    }

    /**
     * Validate a file
     *
     * @param file the file to validate
     */
    protected void validateFile(File file) {
        String fileName = file.getName();

        try {
            if (fileName.matches(FileType.DC.getFilename())) {
                validateDcFile(file);
            } else if (fileName.matches(FileType.RDF.getFilename())) {
                validateRdfFile(file);
            }
        } catch (MalformedURLException ex) {
            // URI
            logger.error("Invalid URI: "+ex.getMessage()+" - "+file.getAbsolutePath());
        } catch (IOException ex) {
            // File access errors
            logger.error("Failed to open file ("+file.getAbsolutePath()+"): "+ex.getMessage());
        } catch (NoSuchFieldError ex) {
            // Missing fields
            logger.error("Missing field: "+ex.getMessage()+" - "+file.getAbsolutePath());
        } catch (IllegalArgumentException ex) {
            // thrown by Assert class
            logger.error("Invalid data: "+ex.getMessage()+" - "+file.getAbsolutePath());
        } catch (Exception ex) {
            // Anything else
            logger.error("General Exception: "+ex.getMessage()+" - "+file.getAbsolutePath());
        }

    }

    /**
     * Validate a DC file
     *
     * @param file
     * @throws MalformedURLException
     * @throws IllegalArgumentException
     * @throws NoSuchFieldError
     * @throws Exception
     */
    protected void validateDcFile(File file) throws MalformedURLException,
            IllegalArgumentException, NoSuchFieldError, Exception {

        //CSVReader reader = new CSVReader(new FileReader(file), DELIMITER);
        //List<String[]> lines = reader.readAll();
        //validateDcFile(lines);
        //RepositoryFileUtils rfu = new RepositoryFileUtils();
        validateDcFile(rfu.readRepositoryFile(file));
    }

    /**
     * Validate a DC file (parsed into list of String[])
     *
     * @param lines 
     * @throws MalformedURLException
     * @throws IllegalArgumentException
     * @throws NoSuchFieldError
     * @throws Exception
     */
    protected void validateDcFile(List<String[]> lines) throws MalformedURLException,
            IllegalArgumentException, NoSuchFieldError, Exception {
        // initialise requiredDcFields
        ArrayList<String> requiredDcFields = new ArrayList<String>();
        requiredDcFields.add(Field.IDENTIFIER.name); // alt value: Predicates.DC_IDENTIFIER.getLocalPart()
        requiredDcFields.add(Field.FORMAT.name);
        requiredDcFields.add(Field.MODIFIED.name);
        //requiredDcFields.add(Field.URI.name);

        for (String[] data : lines) {
            logger.debug("DC entries (" + data.length + ") = " + StringUtils.join(data, "|"));
            // Check for expected number of tab fields
            Assert.isTrue(data.length == FileType.DC.getFieldCount(),
                    "Entry not expected size of " + FileType.DC.getFieldCount() + 
                    ", got " + data.length +" - " +  StringUtils.join(data, "|"));
            
            if (data[0].endsWith(Field.FORMAT.name)) {
                // Check "format" field
                requiredDcFields.remove(Field.FORMAT.name);
                Assert.isTrue(MimeType.getAllMimeTypes().contains(data[1]),
                        Field.FORMAT.name+" does not contain an accepted value: " + data[1] + " - " +
                        StringUtils.join(MimeType.getAllMimeTypes(), "|"));
            } else if (data[0].endsWith(Field.IDENTIFIER.name)) {
                // Check "identifier" field
                requiredDcFields.remove(Field.IDENTIFIER.name);
                Assert.isTrue(data[1].length() > 0, Field.IDENTIFIER.name+" is empty");
            } else if (data[0].endsWith(Field.MODIFIED.name)) {
                // Check "modified" date field
                requiredDcFields.remove(Field.MODIFIED.name);
                Assert.isTrue(data[1].length() > 0, Field.MODIFIED.name+" date is empty");
                DateValidator validator = DateValidator.getInstance();
                if (!validator.isValid(data[1], "yyyy-MM-dd", true)) {
                    throw new IllegalArgumentException(Field.MODIFIED.name+" date is not a valid date: " + data[1]);
                }
            } else if (data[0].endsWith(Field.URI.name)) {
                // Check "URI" field
                requiredDcFields.remove(Field.URI.name);
                new URL(data[1]);  // throws MalformedURLException if not valid URL
            }
        }

        if (!requiredDcFields.isEmpty()) {
            throw new NoSuchFieldError("Required fields not found: " + StringUtils.join(requiredDcFields, ", "));
        }
    }

    /**
     * Validate a RDF file 
     *
     * @param file
     * @throws IllegalArgumentException
     * @throws NoSuchFieldError
     * @throws FileNotFoundException
     * @throws IOException
     * @throws RDFParseException
     * @throws RDFHandlerException
     * @throws Exception
     */
    protected void validateRdfFile(File file) throws IllegalArgumentException, NoSuchFieldError,
            FileNotFoundException, IOException, RDFParseException, RDFHandlerException, Exception {
        
        List<String[]> lines = null;
        
        if (useTurtle) {
            // parse turtle file
            //List<Triple> triples = TurtleUtils.readTurtle(new FileReader(file), false);
            //lines = deserialiseTriples(triples);
            //RepositoryFileUtils rfu = new RepositoryFileUtils();
            lines = rfu.readRepositoryFile(file);
        } else {
            // parse CSV file
            CSVReader reader = new CSVReader(new FileReader(file), DELIMITER);
            lines = reader.readAll();
        }
        
        validateRdfFile(lines);
    }

    /**
     * Validate a DC file (parsed into list of String[])
     *
     * @param lines
     * @throws IllegalArgumentException
     * @throws NoSuchFieldError
     * @throws Exception
     */
    protected void validateRdfFile(List<String[]> lines) throws IllegalArgumentException,
            NoSuchFieldError, Exception {
        // initialise requiredRdfFields
        ArrayList<String> requiredRdfFields = new ArrayList<String>();
        requiredRdfFields.add(Field.SCI_NAME.name);

        for (String[] data : lines) {
            logger.debug("RDF entries (" + data.length + ") = " + StringUtils.join(data, "|"));
            Assert.isTrue(data.length == FileType.RDF.getFieldCount(),
                    "RDF Entry not expected size of " + FileType.RDF.getFieldCount() +
                    ", got " + data.length +" - " + StringUtils.join(data, "|"));
            
            if (data[1].endsWith(Field.SCI_NAME.name)) {
                // Check hasScientificName
                requiredRdfFields.remove(Field.SCI_NAME.name);
                Assert.isTrue(data[2].length() > 0, Field.SCI_NAME.name+" is empty");
            }
        }
    
        if (!requiredRdfFields.isEmpty()) {
            throw new NoSuchFieldError("Required fields not found: " + StringUtils.join(requiredRdfFields, ", "));
        }
    }

    /**
     * Deserialise list of Triples into a list of String arrays
     *
     * @param triples
     * @return array of Strings
     */
    private List<String[]> deserialiseTriples(List<Triple> triples) {
        List<String[]> lines =  new ArrayList<String[]>();

        for (Triple t : triples) {
            String[] fields = {(String) t.getSubject(), (String) t.getPredicate(), (String) t.getObject()};
            lines.add(fields);
            logger.debug("triple: "+StringUtils.join(fields, " | "));
        }

        return lines;
    }

    public Integer getInfoSourceId() {
        return infoSourceId;
    }

    public void setInfoSourceId(Integer infoSourceId) {
        this.infoSourceId = infoSourceId;
    }

    /**
     * An (inner) Enum of fields (RDF predicates)
     *
     * <p>TODO: Replace references to this class with the {@see org.ala.repository.Predicates Predicates enum},
     * which is currently missing some of the DC fields needed here.</p>
     */
    protected enum Field {
        IDENTIFIER("identifier"),
        TITLE("title"),
        FORMAT("format"),
        URI("URI"),
        MODIFIED("modified"),
        SCI_NAME("hasScientificName");

        private final String name;

        private Field(String theName) {
            this.name = theName;
        }

        public String getName() {
            return this.name;
        }
    }

    public boolean isUseTurtle() {
        return useTurtle;
    }

    public void setUseTurtle(boolean useTurtle) {
        this.useTurtle = useTurtle;
    }
}
