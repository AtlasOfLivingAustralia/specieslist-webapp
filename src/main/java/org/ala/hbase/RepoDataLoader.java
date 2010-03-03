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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ala.dao.InfoSourceDAO;
import org.ala.dao.TaxonConceptDao;
import org.ala.model.InfoSource;
import org.ala.model.Triple;
import org.ala.util.NTriplesUtils;
import org.ala.util.TurtleUtils;
import org.apache.commons.io.FileUtils;
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
    protected Map<Integer, InfoSource> infoSourceMap;
    @Inject
    protected InfoSourceDAO infoSourceDAO;
	
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
		System.out.println("Scanning directory: "+filePath);
		
		//initialise DAO 
		TaxonConceptDao tcDao = new TaxonConceptDao();

        //load a cahce of infsourceid->name map
		
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
                    List<Triple> triples = null;
                    if (useTurtle) {
                        triples = TurtleUtils.readTurtle(reader, true);
                    } else {
                        triples = NTriplesUtils.readNTriples(reader, true);
                    }
                    //sync these triples
//				/../../infosource-id/document-id div 1000/document-id/rdf
                    String infosourceId = currentFile.getParentFile().getParentFile().getParentFile().getName();
                    String documentId = currentFile.getParentFile().getName();

                    //read dublin core

                    //read dc:source (infosource URL), dc:publisher (infosource name), document ID, infsource ID
                    InfoSource infoSource = infoSourceMap.get(Integer.getInteger(infosourceId));
                    String dcSource = infoSource.getWebsiteUrl();
                    String dcPublisher = infoSource.getName();
                    tcDao.syncTriples(infosourceId, documentId, dcSource, dcPublisher, triples, currentFile.getParentFile().getAbsolutePath());
                    //tcDao.syncTriples(infosourceId, documentId, triples, currentFile.getParentFile().getAbsolutePath());
                } catch (Exception exception) {
                    System.out.println("Error reading triple: "+exception.getMessage());
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
    protected Map<Integer, InfoSource> loadInfoSources() {
        Map<Integer, InfoSource> infoSourceMap = new HashMap<Integer, InfoSource>();

        if (infoSourceDAO!=null) {
            List<Integer> allIds = infoSourceDAO.getIdsforAll();
            
            for (Integer id : allIds) {
                infoSourceMap.put(id, infoSourceDAO.getById(id));
            }
        }
        System.out.println("loaded infoSource map: "+infoSourceMap.size());
        return infoSourceMap;
    }
}
