/***************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
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
package org.ala.hbase;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ala.dao.InfoSourceDAO;
import org.ala.dao.TaxonConceptDao;
import org.ala.model.Document;
import org.ala.model.InfoSource;
import org.ala.model.Triple;
import org.ala.repository.Predicates;
import org.ala.util.RepositoryFileUtils;
import org.ala.util.TurtleUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Data Loader that scans through a BIE repository, find triples
 * and adds them to concepts held in the profiler. 
 * 
 * @author Dave Martin
 */
@Component
public class RepoDataLoader {

	protected static String repositoryDir = "/data/bie";
	protected boolean useTurtle = true;
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	protected Map<Integer, InfoSource> infoSourceMap;
    @Inject
    protected InfoSourceDAO infoSourceDAO;
    @Inject
    RepositoryFileUtils repoFileUtils;
	protected static Logger logger = Logger.getLogger(RepoDataLoader.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		//RepoDataLoader loader = new RepoDataLoader();
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-profiler.xml", "classpath:spring.xml"});
        RepoDataLoader loader = (RepoDataLoader) context.getBean(RepoDataLoader.class);
		long start = System.currentTimeMillis();
        loader.loadInfoSources();
		int filesRead = loader.load(repositoryDir); //FIX ME - move to config
    	long finish = System.currentTimeMillis();
    	System.out.println(filesRead+" files scanned/loaded in: "+((finish-start)/60000)+" minutes "+((finish-start)/1000)+" seconds.");
	}

	/**
	 * Scan through the repository, retrieve triples and
	 * add to taxon concepts
	 * 
	 * @param filePath
	 * @throws Exception
	 */
	private int load(String filePath) throws Exception {
		logger.info("Scanning directory: "+filePath);
		
		int filesRead = 0;
		
		//start scan
		File file = new File(filePath);
		Iterator<File> fileIterator = FileUtils.iterateFiles(file, null, true);
		while(fileIterator.hasNext()){
			File currentFile = fileIterator.next();
			if(currentFile.getName().equals("rdf")){
				filesRead++;
				
				//read the dublin core in the same directory - determine if its an image
				try {
                    FileReader reader = new FileReader(currentFile);
                    List<Triple> triples = TurtleUtils.readTurtle(reader);
                    
                    String currentSubject = null;
                    List<Triple> splitBySubject = new ArrayList<Triple>();
                    
                    for(Triple triple: triples){
                    
                    	if(currentSubject==null){
                    		currentSubject = triple.subject;
                    	} else if(!currentSubject.equals(triple.subject)){
                    		//sync these triples
    	                    String infosourceId = currentFile.getParentFile().getParentFile().getParentFile().getName();
    	                    String documentId = currentFile.getParentFile().getName();
                            
    	                    //retrieve the publisher and source from the infosource
    	                    InfoSource infoSource = infoSourceMap.get(new Integer(infosourceId));
    	                    String dcSource = infoSource.getWebsiteUrl();
    	                    String dcPublisher = infoSource.getName();
    	                    Document document = new Document();
                            document.setInfoSourceId(new Integer(infosourceId));
                            document.setInfoSourceName(dcPublisher);
                            document.setInfoSourceUri(dcSource);
    	                    //sync the triples to BIE profiles
    	                    taxonConceptDao.syncTriples(infosourceId, documentId, document, splitBySubject, currentFile.getParentFile().getAbsolutePath());
    	                    
    	                    //clear list
    	                    splitBySubject.clear();
    	                    splitBySubject.add(triple);
    	                    currentSubject = triple.subject;
                    	}
                    	
                    	splitBySubject.add(triple);
                    }
                    
                    String infosourceId = currentFile.getParentFile().getParentFile().getParentFile().getName();
                    String documentId = currentFile.getParentFile().getName();
                    // Read dublin core
                    Document document = readDcFile(currentFile);
                    // Added info source data to the Document via info source Map
                    InfoSource infoSource = infoSourceMap.get(new Integer(infosourceId));
                    document.setInfoSourceId(infoSource.getId());
                    document.setInfoSourceName(infoSource.getName());
                    document.setInfoSourceUri(infoSource.getWebsiteUrl());
                    // Sync the triples and associated DC data
                    taxonConceptDao.syncTriples(infosourceId, documentId, document, triples, currentFile.getParentFile().getAbsolutePath());

                } catch (Exception e) {
                    logger.error("Error reading triples from file: '"+currentFile.getName() +"', "+e.getMessage(), e);
                }
                logger.debug("finished syncing file: "+currentFile.getAbsolutePath());
			}
		}
		return filesRead;
	}

    /**
     * Initialise the info source map
     */
    protected void loadInfoSources() {
        this.infoSourceMap = new HashMap<Integer, InfoSource>();
        if (infoSourceDAO!=null) {
            List<Integer> allIds = infoSourceDAO.getIdsforAll();
            for (Integer id : allIds) {
                infoSourceMap.put(id, infoSourceDAO.getById(id));
            }
        }
        logger.info("loaded infoSource map: "+infoSourceMap.size());
    }

    /**
     * Read dc file and populate a Document with values from file
     *
     * @param currentFile
     * @return doc the Docuement to return
     */
    private Document readDcFile(File currentFile) {
        Document doc = new Document();
        String rdfFileName = currentFile.getAbsolutePath();
        String dcFileName = rdfFileName.replaceFirst("rdf", "dc");
        File dcfile = new File(dcFileName);
        List<String[]> dcContents = new ArrayList<String[]>();
        
        try {
            dcContents = repoFileUtils.readRepositoryFile(dcfile);

            for (String[] line : dcContents) {
                // expect 2 element String array (key, value)
                if (line[0].equalsIgnoreCase(Predicates.DC_IDENTIFIER.toString())) {
                    doc.setIdentifier(line[1]);
                } else if (line[0].equalsIgnoreCase(Predicates.DC_TITLE.toString())) {
                    doc.setTitle(line[1]);
                }
            }
        } catch (Exception ex) {
            logger.error("Cannot open dc file: "+dcFileName+" - "+ex.getMessage());
        }

        return doc;
    }

    /**
	 * @param taxonConceptDao the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}


}
