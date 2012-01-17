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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.ala.client.util.RestfulClient;
import org.ala.dao.InfoSourceDAO;
import org.ala.dao.SolrUtils;
import org.ala.dao.TaxonConceptDao;
import org.ala.model.Document;
import org.ala.model.InfoSource;
import org.ala.model.Triple;
import org.ala.repository.Predicates;
import org.ala.util.FileType;
import org.ala.util.RepositoryFileUtils;
import org.ala.util.SpringUtils;
import org.ala.util.TurtleUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
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
	protected HashMap<String, Integer> uidInfoSourceMap;

	@Inject
    protected InfoSourceDAO infoSourceDAO;
    @Inject
    protected RepositoryFileUtils repoFileUtils;
    @Inject
	protected SolrUtils solrUtils;
    private boolean statsOnly = false;
    private boolean reindex = false;
    private boolean gList = false;
	private FileOutputStream guidOut = null;

	int totalFilesRead = 0;
	int totalPropertiesSynced = 0;
	
	/**
	 * This takes a list of infosource ids...
	 * 
	 * Usage: -stats or -reindex or -gList and list of infosourceId
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
            if(args[0].equalsIgnoreCase("-stats")) {
                loader.statsOnly = true;
                args = (String[])ArrayUtils.subarray(args, 1, args.length);
            }
            if(args[0].equalsIgnoreCase("-reindex")) {
                loader.reindex = true;
                args = (String[])ArrayUtils.subarray(args, 1, args.length);
                logger.info("**** -reindex: " + loader.reindex);
            }
            if(args[0].equalsIgnoreCase("-gList")) {
                loader.gList = true;
                args = (String[])ArrayUtils.subarray(args, 1, args.length);
                logger.info("**** -gList: " + loader.gList);
            }
            if(args[0].equalsIgnoreCase("-biocache")) {
            	Hashtable<String, String> hashTable = new Hashtable<String, String>();
        		hashTable.put("accept", "application/json");
            	ObjectMapper mapper = new ObjectMapper();
        		mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            	RestfulClient restfulClient = new RestfulClient(0);
            	Object[] resp = restfulClient.restGet("http://biocache.ala.org.au/ws/occurrences/search?q=multimedia:Multimedia&facets=data_resource_uid&pageSize=0", hashTable);
        		if((Integer)resp[0] == HttpStatus.SC_OK){
        			String content = resp[1].toString();
        			if(content != null && content.length() > "[]".length()){
        				Map map = mapper.readValue(content, Map.class);
        				try{
        					List<java.util.LinkedHashMap<String,String>> list = ((List<java.util.LinkedHashMap<String,String>>)((java.util.LinkedHashMap)((java.util.ArrayList)map.get("facetResults")).get(0)).get("fieldResult"));
        					Set<String> arg = new LinkedHashSet<String>();
        					for(int i = 0; i < list.size(); i++){
        						java.util.LinkedHashMap<String,String> value = list.get(i);
        						String provider = (loader.getUidInfoSourceMap().get(value.get("label"))).toString();
        						if(provider != null){
        							arg.add(provider);
        						}
        					}
        					args = new String[]{};
        					args = arg.toArray(args);
        				}
        				catch(Exception e){
        					logger.error("ERROR: exit process....." + e);
        					System.exit(0);
        				}
        			}
        		} 
        		else {
        			logger.warn("Unable to process url: ");
        		}            	
            }            
        }
		int filesRead = loader.load(filePath, args); //FIX ME - move to config
    	long finish = System.currentTimeMillis();
    	logger.info(filesRead+" files scanned/loaded in: "+((finish-start)/60000)+" minutes "+((finish-start)/1000)+" seconds.");
    	System.exit(1);
	}

	public int load(String filePath, String[] repoDirs) throws Exception {
		return load(filePath, repoDirs, true);
	}
	
	/**
	 * Scan through the single directory, retrieve triples and
	 * add to taxon concepts. used by admin imageUpload
	 * 
	 * @param repoDir scan single directory
	 * @throws Exception
	 */
	 public int singleImageUploadLoad(String repoDir) throws Exception {
		// reset counts
        totalFilesRead = 0;
        totalPropertiesSynced = 0;
	        
		//start scan
		File dir = null;
		dir = new File(repoDir);
		logger.info("Processing directories..."+dir.getAbsolutePath());		
		
		if(dir.isDirectory()){
			File[] dirsToScan = {dir};
			scanDirectory(dirsToScan);			
		}
		logger.info("Files read: "+totalFilesRead+", files matched: "+totalPropertiesSynced);
		return totalFilesRead;
	}

		/**
		 * Scan through the repository, retrieve triples and
		 * add to taxon concepts
		 * 
		 * @param filePath Root directory of harvested repository
		 * @param repoDirs Optional array of Infosource directories to scan passed as program arguments
		 * @throws Exception
		 */
		 public int load(String filePath, String[] repoDirs, boolean allowStats) throws Exception {
			 FileOutputStream statsOut = null;
			 
			logger.info("Scanning directory: "+filePath);

	                //open the statistics file
			if(allowStats){
	                statsOut = FileUtils.openOutputStream(new File("/data/bie/bie_name_matching_stats_"+System.currentTimeMillis() + ".csv"));
	                statsOut.write("InfoSource ID, InfoSource Name, URL, ANBG matches, Other matches, Missing, Homonyms detected\n".getBytes());
			}

			if(gList){
				guidOut = FileUtils.openOutputStream(new File("/data/bie/repoLoader_guid_"+System.currentTimeMillis() + ".csv"));
			}
			
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
					if(allowStats){
	                                //report the stats
	                                if(org.apache.commons.lang.StringUtils.isNumeric(childFile.getName())){
	                                InfoSource infoSource = infoSourceMap.get(new Integer(childFile.getName()));
	                                taxonConceptDao.reportStats(statsOut, infoSource.getId() + ","+infoSource.getName() + "," + infoSource.getWebsiteUrl());
	                            }
					}
	                                
				}

			}

			logger.info("Files read: "+totalFilesRead+", files matched: "+totalPropertiesSynced);
			if(allowStats){
	                statsOut.flush();
	                statsOut.close();
			}
			if(reindex){
				solrUtils.getSolrServer().commit();
			}
			if(gList){
				guidOut.flush();
                guidOut.close();
			}
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
					String infoSourceUid = infoSourceDAO.getUidByInfosourceId(String.valueOf(infosourceId));
					//read the dublin core in the same directory - determine if its an image
					try {
	                    logger.info("Reading file: " + currentFile.getAbsolutePath());
						FileReader reader = new FileReader(currentFile);
	                    List<Triple> triples = TurtleUtils.readTurtle(reader);
	                    //close the reader
	                    reader.close();
	                    
	                    String currentSubject = null;
	                    List<Triple> splitBySubject = new ArrayList<Triple>();
	                    
	                    String guid = null;
	                    //iterate through triple, splitting the triples by subject
	                    for(Triple triple: triples){
	                    
							if (currentSubject == null) {
	                    		currentSubject = triple.subject;
							} else if (!currentSubject.equals(triple.subject)) {
	                    		//sync these triples
//								/data/bie/1036/23/235332/rdf
								
								guid = sync(currentFile, splitBySubject, infosourceId, infoSourceUid);
								if (guid != null && guid.trim().length() > 0) {
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
							guid = sync(currentFile, splitBySubject, infosourceId, infoSourceUid);
							if (guid != null && guid.trim().length() > 0) {
								propertiesSynced++;
							}
						}
						
						if(gList && guid != null){
							guidOut.write((guid + "\n").getBytes());
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
	private String sync(File currentFile, List<Triple> triples, String infosourceId, String infoSourceUid) throws Exception {
		
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
		
		
		
		if (infoSourceUid != null && !"".equals(infoSourceUid)) {
		    document.setInfoSourceUid(infoSourceUid);
		}
		
		Map<String, String> dc = readDcFileAsMap(currentFile);
		// Sync the triples and associated DC data
		logger.info("Attempting to sync triple where Scientific Name = " + getScientificName(triples));
		String guid = taxonConceptDao.syncTriples(document, triples, dc, statsOnly, reindex);
//		boolean success = taxonConceptDao.syncTriples(document, triples, dc, statsOnly);
		logger.info("Processed file: "+currentFile.getAbsolutePath() + ", Scientific Name = " + getScientificName(triples) + ", guid: "+guid);
		return guid;
	}

    /**
     * Initialise the info source map
     *
     * @return infoSourceMap
     */
    public void loadInfoSources() {
        this.infoSourceMap = new HashMap<Integer, InfoSource>();
        this.uidInfoSourceMap = new HashMap<String, Integer>();
        if (infoSourceDAO!=null) {
            List<Integer> allIds = infoSourceDAO.getIdsforAll();
            Map<String, String> allUids = infoSourceDAO.getInfosourceIdUidMap();
            for (Integer id : allIds) {            	
                infoSourceMap.put(id, infoSourceDAO.getById(id));
                if(allUids.get(id.toString()) != null && !"".equals(allUids.get(id.toString()))){
                	uidInfoSourceMap.put(allUids.get(id.toString()), id);
                }
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

    public HashMap<String, Integer> getUidInfoSourceMap() {
		return uidInfoSourceMap;
	}
}

