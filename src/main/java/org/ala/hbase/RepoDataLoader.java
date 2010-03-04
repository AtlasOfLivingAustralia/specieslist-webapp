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
import org.ala.model.InfoSource;
import org.ala.model.Triple;
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
	protected TaxonConceptDao taxonConceptDao = null;
	protected Map<Integer, InfoSource> infoSourceMap;
    @Inject
    protected InfoSourceDAO infoSourceDAO;
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
    	                    
    	                    //sync the triples to BIE profiles
    	                    taxonConceptDao.syncTriples(infosourceId, documentId, dcSource, dcPublisher, splitBySubject, currentFile.getParentFile().getAbsolutePath());
    	                    
    	                    //clear list
    	                    splitBySubject.clear();
    	                    splitBySubject.add(triple);
    	                    currentSubject = triple.subject;
                    	}
                    	
                    	splitBySubject.add(triple);
                    }
                } catch (Exception e) {
                    logger.error("Error reading triples from file: '"+currentFile.getName() +"', "+e.getMessage(), e);
                }
			}
		}
		return filesRead;
	}

    /**
     * Initialise the info source map
     *
     * @return infoSourceMap
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
	 * @param taxonConceptDao the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}
}
