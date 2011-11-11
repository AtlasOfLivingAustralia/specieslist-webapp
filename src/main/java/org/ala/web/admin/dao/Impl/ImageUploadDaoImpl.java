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

package org.ala.web.admin.dao.Impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.MimetypesFileTypeMap;
import javax.inject.Inject;
import org.ala.web.admin.dao.ImageUploadDao;
import org.ala.dao.TaxonConceptDao;
import org.ala.documentmapper.MappingUtils;
import org.ala.hbase.RepoDataLoader;
import org.ala.model.Document;
import org.ala.web.admin.model.UploadItem;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Repository;
import org.ala.repository.Triple;
import org.ala.util.MimeType;
import org.ala.util.RepositoryFileUtils;
import org.ala.util.TurtleUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * store image into repository.
 * 
 * @author mok011
 *
 */

@Component("imageUploadDao")
public class ImageUploadDaoImpl implements ImageUploadDao{

	@Inject
	protected Repository repository;
	@Inject
	protected TaxonConceptDao taxonConceptDao;
    @Inject
    protected RepoDataLoader repoDataLoader;
    @Inject
    protected RepositoryFileUtils repoFileUtils;
    
	protected static final String ALA_UPLOADS = "ALA website image uploads";
    protected static final String ALA_URL = "http://www.ala.org.au";
    protected static final String BIE_URL = "http://bie.ala.org.au";
	private static Logger logger = Logger.getLogger(ImageUploadDaoImpl.class);
	
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public Document storeDocument(int infoSourceId, UploadItem uploadItem) throws Exception {
		Document doc = null;
		
		if(uploadItem == null || infoSourceId < 1){
			return doc;
		}
		ParsedDocument parsedDoc = new ParsedDocument();
		
		String guid = null;
		if(uploadItem.getGuid() != null && uploadItem.getGuid().length() > 0){
			guid = uploadItem.getGuid();
		}
		else{
			//String guid = taxonConceptDao.findLsidByName(uploadItem.getScientificName().trim());
			guid = taxonConceptDao.findLsidByName(uploadItem.getScientificName().trim(), uploadItem.getRank().trim().toLowerCase());
		}
        String imageGuid = BIE_URL + "/uploads/" + guid + "^:^" +System.currentTimeMillis(); // unique id for uploaded image
        
		if(guid != null && !guid.isEmpty()){		
            logger.debug("GUID = " + guid);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new java.util.Date();
            // Set Dublin Core properties
			parsedDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), uploadItem.getUserName());
            parsedDoc.getDublinCore().put(Predicates.DC_MODIFIED.toString(), dateFormat.format(date));
			parsedDoc.getDublinCore().put(Predicates.DC_LICENSE.toString(), uploadItem.getLicence());
            parsedDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), uploadItem.getAttribn());
			parsedDoc.getDublinCore().put(Predicates.DC_TITLE.toString(), uploadItem.getTitle());
			parsedDoc.getDublinCore().put(Predicates.DC_DESCRIPTION.toString(), uploadItem.getDescription());
            parsedDoc.getDublinCore().put(Predicates.DC_SOURCE.toString(), ALA_URL);
            parsedDoc.getDublinCore().put(Predicates.DC_PUBLISHER.toString(), ALA_UPLOADS);
            // Set DwC triples
			parsedDoc.getTriples().add(new Triple(MappingUtils.getSubject(),Predicates.COMMON_NAME.toString(), uploadItem.getCommonName().trim()));
			parsedDoc.getTriples().add(new Triple(MappingUtils.getSubject(),Predicates.SCIENTIFIC_NAME.toString(), uploadItem.getScientificName().trim()));
			
			if (uploadItem.getFileData() != null) {
				//if same guid will write to same folder, added timestamp to alter file to different folder.
				parsedDoc.setGuid(imageGuid);
	            parsedDoc.getDublinCore().put(Predicates.DC_FORMAT.toString(), MimeType.getFileExtension(uploadItem.getFileData().getOriginalFilename()));
	            parsedDoc.getDublinCore().put(Predicates.DC_IDENTIFIER.toString(), imageGuid); // made-up URI (TODO implement in bie-webapp) 
	            parsedDoc.setContent(getUploadFileByte(uploadItem.getFileData().getInputStream()));
                parsedDoc.setContentType(new MimetypesFileTypeMap().getContentType(uploadItem.getFileData().getOriginalFilename())); // gets mimetype from file extention
                doc = repository.storeDocument(infoSourceId, parsedDoc);
                
                if (doc != null) {
                    logger.debug("Image document stored: " + doc.toString());
                    // Add to Cassandra data store 
//                  addImageToCassandra(infoSourceId);                    
                    addImageToCassandra(doc);
                } else {
                    // Unlikely to get triggered
                    throw new Exception("Failed to create document (storeDocument returned null)");
                }
			} else {
				logger.debug("uploadItem.getFileData() was null");
                for(FileItem fi : uploadItem.getFiles()){
					//if same guid will write to same folder, added timestamp to alter file to different folder.
                	imageGuid = BIE_URL + "/uploads/" + guid + System.currentTimeMillis();
					parsedDoc.setGuid(imageGuid);
		            parsedDoc.getDublinCore().put(Predicates.DC_FORMAT.toString(), fi.getContentType());
		            parsedDoc.getDublinCore().put(Predicates.DC_IDENTIFIER.toString(), imageGuid); // made-up URI (TODO implement in bie-webapp) 
					parsedDoc.setContent(getUploadFileByte(fi.getInputStream()));
					parsedDoc.setContentType(fi.getContentType()); // gets mimetype from file extention
					doc = repository.storeDocument(infoSourceId, parsedDoc);
                    
                    if (doc != null) {
                        logger.debug("Image document stored: " + doc.toString());
                        // Add to Cassandra data store 
//                        addImageToCassandra(infoSourceId);
                        addImageToCassandra(doc);
                    }
				}
			}
		} else {
            String msg = "Failed upload for unknown scientific name: " + uploadItem.getScientificName().trim();
            logger.warn(msg);
            throw new Exception(msg);
        }
		return doc;
	}
	
	private byte[] getUploadFileByte(InputStream istream){
		byte[] farray = null;
		
		if(istream != null){
			try{				
				byte[] buffer = new byte[8196];
				ByteArrayOutputStream ostream = new ByteArrayOutputStream();
				while (istream.read(buffer) != -1) {
					ostream.write(buffer);
				}
				farray = ostream.toByteArray();
				istream.close();
				ostream.close();
			}
			catch (Exception e) {
				logger.warn("getUploadFileByte error: " + e.getMessage(), e);
			}
		}
		return farray;
	}

    /**
     * Add image to Cassandra via RepoDataLoader
     * 
     * @param doc
     * @throws Exception
     */
	/*
    private void addImageToCassandra(int infosourceId) throws Exception {
        // init repoDataLoader
        repoDataLoader.loadInfoSources();
        String[] repoPath = { Integer.toString(infosourceId) };
        if (repoDataLoader.load(RepoDataLoader.getRepositoryDir(), repoPath, false) < 1) {
            logger.error("Failed to load object into Cassandra");
        }
    }
    */
	
    private void addImageToCassandra(Document doc) throws Exception {
        // init repoDataLoader
        repoDataLoader.loadInfoSources();
//        String repoPath = Integer.toString(infosourceId);       
        if (repoDataLoader.singleImageUploadLoad(doc.getFilePath()) < 1) {
            logger.error("Failed to load object into Cassandra");
        }
    }    
    
    public List<Triple<String, String, String>> readRdfFile(Document doc){
    	List<Triple<String, String, String>> triples = null;
    	FileReader reader = null;
    	
    	if(doc == null){
        	return null;
        }
    	
    	
    	//get RDF file content and populate uploadItem.
		try {
			String repoLocation = doc.getFilePath();
		    File rdfFile = new File(repoLocation + "/rdf");
			reader = new FileReader(rdfFile);
			List<org.ala.model.Triple> rdf = TurtleUtils.readTurtle(reader);
			if(rdf != null){
				triples = new ArrayList<Triple<String, String, String>>();
				for(org.ala.model.Triple triple : rdf){
					triples.add(new Triple(MappingUtils.getSubject(),triple.predicate, triple.object));
				}
			}
		} 
		catch (Exception e) {
			logger.error(e.toString());
		}
		finally{
			try {
				if(reader != null) reader.close();
			} 
			catch (IOException e) {
				logger.error(e.toString());
			}
	    }     		        
    	return triples;
    }
    
    private List<Triple<String, String, String>> updateTriple(List<Triple<String, String, String>> from, List<Triple<String, String, String>> to){
    	List<Triple<String, String, String>> list = null;
    	Map<String, Triple<String, String, String>> map = new Hashtable<String, Triple<String, String, String>>();
    	
    	for(Triple<String, String, String> triple : to){
    		map.put(triple.getPredicate(), triple);
    	}
    	
    	for(Triple<String, String, String> triple: from){
    		if(map.containsKey(triple.getPredicate())){
    			map.remove(triple.getPredicate());
    		}
    		map.put(triple.getPredicate(), triple);
    	}
    	
    	Collection<Triple<String, String, String>> values = map.values();
    	if(values != null){
    		list = new ArrayList<Triple<String, String, String>>(values);
    	}
    	return list;
    }
    
    public Map<String,String> readDcFile(Document doc){
    	File dcFile = null;
    	List<String[]> dcContents = null;
    	Map<String,String> dcProperties = null;
        if(doc == null){
        	return null;
        }
    	    	
    	//get RDF file content and populate uploadItem.
		try {
			String repoLocation = doc.getFilePath();
			dcFile = new File(repoLocation + "/dc");
			
	    	//get dc file content and populate uploadItem.
			dcContents = new ArrayList<String[]>();			
	        dcContents = repoFileUtils.readRepositoryFile(dcFile);  
	        if(dcContents != null){
	        	dcProperties = new LinkedHashMap<String, String>();
	        	for(String[] values : dcContents){
	        		dcProperties.put(values[0], values[1]);
	        	}
	        }
		} 
		catch (Exception e) {
			logger.error(e.toString());
		}
		return dcProperties;
    }
    
    private Map<String,String> updateDcFile(Map<String,String> from, Map<String,String> to){
    	Map<String,String> dcProperties = new LinkedHashMap<String, String>();
    	
    	Set<String> keys = to.keySet();
    	Iterator<String> itr = keys.iterator();
    	while(itr.hasNext()){
    		String key = itr.next();
    		dcProperties.put(key, to.get(key));
    	}
    	
    	keys = from.keySet();
    	itr = keys.iterator();
    	while(itr.hasNext()){
    		String key = itr.next();
    		if(dcProperties.containsKey(key)){
    			dcProperties.remove(key);
    		}
    		dcProperties.put(key, from.get(key));
    	}    	
    	return dcProperties;    	
    }
    
    public boolean updateDocument(Document doc, UploadItem uploadItem) throws Exception {
    	boolean ok = false;
    	
    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new java.util.Date();
        Map<String,String> dcProperties = new LinkedHashMap<String, String>();
        List<Triple<String, String, String>> triples = new ArrayList<Triple<String, String, String>>();
		if(uploadItem != null && uploadItem.getDocumentId() != null){
			
			int docId = Integer.valueOf(uploadItem.getDocumentId());
			dcProperties.put(Predicates.DC_CREATOR.toString(), uploadItem.getUserName());
			dcProperties.put(Predicates.DC_MODIFIED.toString(), dateFormat.format(date));
			dcProperties.put(Predicates.DC_LICENSE.toString(), uploadItem.getLicence());
			dcProperties.put(Predicates.DC_TITLE.toString(), uploadItem.getTitle());
			dcProperties.put(Predicates.DC_DESCRIPTION.toString(), uploadItem.getDescription());
			dcProperties.put(Predicates.DC_RIGHTS.toString(), uploadItem.getAttribn());
			
			triples.add(new Triple(MappingUtils.getSubject(),Predicates.COMMON_NAME.toString(), uploadItem.getCommonName().trim()));
			triples.add(new Triple(MappingUtils.getSubject(),Predicates.SCIENTIFIC_NAME.toString(), uploadItem.getScientificName().trim()));
			
			List<Triple<String, String, String>> rdf = readRdfFile(doc);
			rdf = updateTriple(triples, rdf);
			repository.storeRDF(docId, rdf);
			
			Map<String,String> dc = readDcFile(doc);
			dc = updateDcFile(dcProperties, dc);
			repository.storeDublinCore(docId, dc);
			
			addImageToCassandra(doc);
			ok = true;
		}		
		return ok;    	
    }
}
