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
import java.io.FileFilter;
import java.io.FileOutputStream;
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
import org.ala.util.FileType;
import org.ala.util.RepositoryFileUtils;
import org.ala.util.SpringUtils;
import org.ala.util.TurtleUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Data Loader that scans through a BIE repository, find triples
 * and adds them to concepts held in the profiler. 
 * 
 * @author Dave Martin
 */
@Component
public class RepoDataLoader {

	protected static Logger logger = Logger.getLogger(RepoDataLoader.class);
	
	protected static String repositoryDir = "/data/bie";
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	protected Map<Integer, InfoSource> infoSourceMap;
    @Inject
    protected InfoSourceDAO infoSourceDAO;
    @Inject
    protected RepositoryFileUtils repoFileUtils;
    private boolean statsOnly = false;
    private  FileOutputStream statsOut;

	int totalFilesRead = 0;
	int totalPropertiesSynced = 0;
	
	/**
	 * This takes a list of infosource ids...
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		//RepoDataLoader loader = new RepoDataLoader();
        ApplicationContext context = SpringUtils.getContext();
        RepoDataLoader loader = (RepoDataLoader) context.getBean(RepoDataLoader.class);
		long start = System.currentTimeMillis();
        loader.loadInfoSources();
        String filePath = repositoryDir;
        if(args.length>0){
            if(args[0].equalsIgnoreCase("-stats"));
            loader.statsOnly = true;
            args = (String[])ArrayUtils.subarray(args, 1, args.length-1);
        }
		int filesRead = loader.load(filePath, args); //FIX ME - move to config
    	long finish = System.currentTimeMillis();
    	logger.info(filesRead+" files scanned/loaded in: "+((finish-start)/60000)+" minutes "+((finish-start)/1000)+" seconds.");
    	System.exit(1);
	}

	/**
	 * Scan through the repository, retrieve triples and
	 * add to taxon concepts
	 * 
	 * @param filePath Root directory of harvested repository
	 * @param repoDirs Optional array of Infosource directories to scan passed as program arguments
	 * @throws Exception
	 */
	 public int load(String filePath, String[] repoDirs) throws Exception {
		logger.info("Scanning directory: "+filePath);

                //open the statistics file
                statsOut = FileUtils.openOutputStream(new File("/data/bie/bie_name_matching_stats_"+System.currentTimeMillis() + ".csv"));
                statsOut.write("InfoSource ID, InfoSource Name, URL, ANBG matches, Other matches, Missing, Homonyms detected\n".getBytes());

		// reset counts
        totalFilesRead = 0;
        totalPropertiesSynced = 0;
        
		//start scan
		File file = new File(filePath);
		File [] dirs = null;

		// See if array of infosource directories passed as program arguments
		if (repoDirs.length > 0) {
			dirs = new File [repoDirs.length];
			for (int i = 0; i < repoDirs.length; i++) {
				dirs[i] = new File(file.getAbsolutePath() + File.separator + repoDirs[i]);
				logger.info("Processing directories..."+dirs[i].getAbsolutePath());
			}
		} else {
			//list immediate directories - this will give the 
			logger.info("Listing all directories...");
			dirs = file.listFiles();
		}
		
		//go through each infosource directory
		for(File childFile: dirs){
			logger.info("Listing directories for infosource directory: "+childFile.getAbsolutePath());
			
			if(childFile.isDirectory()){
                            taxonConceptDao.resetStats();
				//  takes us to /data/bie/<infosource-id>/<section-id>
				logger.info("Listing directories for the section: "+childFile.getAbsolutePath());
				File[] infosourceSection = childFile.listFiles();
				for(File sectionDirectory: infosourceSection){
					//this will list all the files in the
					if(sectionDirectory.isDirectory()){
						File[] dirsToScan = sectionDirectory.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
						scanDirectory(dirsToScan);
					}
				}
                                //report the stats
                                if(org.apache.commons.lang.StringUtils.isNumeric(childFile.getName())){
                                InfoSource infoSource = infoSourceMap.get(new Integer(childFile.getName()));
                                taxonConceptDao.reportStats(statsOut, infoSource.getId() + ","+infoSource.getName() + "," + infoSource.getWebsiteUrl());
                            }
                                
			}

		}

		logger.info("Files read: "+totalFilesRead+", files matched: "+totalPropertiesSynced);
                statsOut.flush();
                statsOut.close();
		return totalFilesRead;
	}

	/**
	 * Retrieve the scientific name from the list of triples.
	 * 
	 * @param triples
	 * @return scientific name if found, null otherwise
	 */
	private String getScientificName(List<Triple> triples) {
        for (Triple triple: triples) {
        	if (triple.predicate.equalsIgnoreCase(Predicates.SCIENTIFIC_NAME.toString())) {
        		return triple.object.toString();
        	}
        }
		return null;
	}

	/**
	 * Scan through the supplied directories.
	 * 
	 * @param dirs
	 */
	public void scanDirectory(File[] dirs){
		
		int filesRead = 0;
		int propertiesSynced = 0;
		
		for (File currentDir : dirs) {
			logger.info("Reading directory: " + currentDir.getAbsolutePath());
			Iterator<File> fileIterator = FileUtils.iterateFiles(currentDir, null, true);
			while (fileIterator.hasNext()) {
				File currentFile = fileIterator.next();
				if (currentFile.getName().equals(FileType.RDF.toString())) {
					filesRead++;
					String infosourceId = currentFile.getParentFile().getParentFile().getParentFile().getName();
					//read the dublin core in the same directory - determine if its an image
					try {
	                    logger.info("Reading file: " + currentFile.getAbsolutePath());
						FileReader reader = new FileReader(currentFile);
	                    List<Triple> triples = TurtleUtils.readTurtle(reader);
	                    //close the reader
	                    reader.close();
	                    
	                    String currentSubject = null;
	                    List<Triple> splitBySubject = new ArrayList<Triple>();
	                    
	                    //iterate through triple, splitting the triples by subject
	                    for(Triple triple: triples){
	                    
							if (currentSubject == null) {
	                    		currentSubject = triple.subject;
							} else if (!currentSubject.equals(triple.subject)) {
	                    		//sync these triples
//								/data/bie/1036/23/235332/rdf
								
								boolean success = sync(currentFile, splitBySubject, infosourceId);
								if (success) {
									propertiesSynced++;
								}
	    	                    //clear list
	    	                    splitBySubject.clear();
	    	                    currentSubject = triple.subject;
	                    	}
	                    	splitBySubject.add(triple);
	                    }
	
	                    //sort out the buffer
						if (!splitBySubject.isEmpty()) {
							boolean success = sync(currentFile, splitBySubject, infosourceId);
							if (success) {
								propertiesSynced++;
							}
						}
	                    
	                } catch (Exception e) {
	                    logger.error("Error reading triples from file: '"+currentFile.getAbsolutePath() +"', "+e.getMessage(), e);
	                }
				}
			}
			logger.info("InfosourceId: " + currentDir.getName() + " - Files read: " + filesRead + ", files matched: " + propertiesSynced);
			totalFilesRead += filesRead;
			totalPropertiesSynced += propertiesSynced;
		}
	}
	
	
	/**
	 * Synchronize triples to database.
	 * 
	 * @param currentFile
	 * @param triples
	 * @throws Exception
	 */
	private boolean sync(File currentFile, List<Triple> triples, String infosourceId) throws Exception {
		
		String documentId = currentFile.getParentFile().getName();
		// Read dublin core
		// Added info source data to the Document via info source Map
		InfoSource infoSource = infoSourceMap.get(new Integer(infosourceId));
		Document document = readDcFile(currentFile);
		document.setId(Integer.parseInt(documentId));
		document.setInfoSourceId(infoSource.getId());
		document.setInfoSourceName(infoSource.getName());
		document.setInfoSourceUri(infoSource.getWebsiteUrl());
		document.setFilePath(currentFile.getParentFile().getAbsolutePath());
		Map<String, String> dc = readDcFileAsMap(currentFile);
		// Sync the triples and associated DC data
		logger.info("Attempting to sync triple where Scientific Name = " + getScientificName(triples));
		boolean success = taxonConceptDao.syncTriples(document, triples, dc, statsOnly);
		logger.info("Processed file: "+currentFile.getAbsolutePath() + ", Scientific Name = " + getScientificName(triples) + ", success: "+success);
		return success;
	}

    /**
     * Initialise the info source map
     *
     * @return infoSourceMap
     */
    public void loadInfoSources() {
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
     * @return doc the Document to return
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
                } else if (line[0].equalsIgnoreCase(Predicates.DC_FORMAT.toString())) {
                    doc.setMimeType(line[1]);
                }
            }
        } catch (Exception ex) {
            logger.error("Cannot open dc file: "+dcFileName+" - "+ex.getMessage());
        }
        return doc;
    }

    /**
     * Read dc file and populate a Document with values from file
     *
     * @param currentFile
     * @return doc the Docuement to return
     */
    private Map<String,String> readDcFileAsMap(File currentFile) {
        String rdfFileName = currentFile.getAbsolutePath();
        String dcFileName = rdfFileName.replaceFirst("rdf", "dc");
        File dcfile = new File(dcFileName);
        Map<String,String> dc = null;
        try {
        	dc = repoFileUtils.readDcFileAsMap(dcfile);
        } catch (Exception ex) {
            logger.error("Cannot open dc file: "+dcFileName+" - "+ex.getMessage());
        }
        return dc;
    }
    
    /**
	 * @param taxonConceptDao the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}

    public static String getRepositoryDir() {
        return repositoryDir;
    }
}
