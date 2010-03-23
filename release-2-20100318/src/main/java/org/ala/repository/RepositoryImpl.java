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
package org.ala.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ala.dao.DocumentDAO;
import org.ala.model.Document;
import org.ala.util.FileType;
import org.ala.util.MimeType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.springframework.stereotype.Component;

/**
 * A simple implementation of a repository that knows how to store
 * documents on the file system and maintain any housekeeping
 * information. 
 * 
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Component("repository") // bean id
public class RepositoryImpl implements Repository{

	protected Logger log = Logger.getLogger(RepositoryImpl.class);
	
	/** The directory root */
	protected String cacheDirectoryRoot = "/data/bie"; // look for override in properties file

	protected String stagingRoot = "/data/bie-staging";
	
	protected String baseDirectory;
	/** Max file limit. Typically should be 32,000 on linux */
	public static int MAX_FILES_PER_DIRECTORY = 10000;
    /** Document DAO to be injected */
	@Inject
    protected DocumentDAO documentDao;
	
//	protected boolean useTurtle = false;
	
	/**
	 * Initialize the file cache. This class has some state to reduce
	 * counting the number of files in the current directory.
	 * 
	 * @throws IOException
	 */
	public RepositoryImpl() throws IOException {
		this(null);
	}

    /**
	 * Initialise repository with subroot.
	 * 
	 * @param directorySubRoot
	 * @throws IOException
	 */
	public RepositoryImpl(String directorySubRoot) throws IOException {
		this(null, directorySubRoot);
	}
    
    /**
	 * Initialise repository with root and subroot.
	 * 
	 * @param directorySubRoot
	 * @throws IOException
	 */
    public RepositoryImpl(String directoryRoot, String directorySubRoot) throws IOException {
		if (directoryRoot != null) {
            this.cacheDirectoryRoot = directoryRoot;
        } 
        //initialise directory structure
		if(directorySubRoot!=null){
			this.baseDirectory = cacheDirectoryRoot+directorySubRoot;
		} else {
			this.baseDirectory = cacheDirectoryRoot;
		}
		log.info("Initialising repository.... baseDirectory = "+baseDirectory);
		//create /data/bie
		File repositoryRoot = new File(this.baseDirectory);
		if(!repositoryRoot.exists()){
			FileUtils.forceMkdir(repositoryRoot);
			log.info("Repository root created at: "+repositoryRoot.getAbsolutePath());
		}
	}
	
    /**
     * @see org.ala.repository.Repository#getDocumentByGuid(java.lang.String)
     */
	@Override
	public Document getDocumentByGuid(String guid) throws Exception {
		return documentDao.getByUri(guid);
	}
	
	/**
	 * @see org.ala.repository.Repository#getDocumentOutputStream(int, java.lang.String, java.lang.String)
	 */
	@Override
	public DocumentOutputStream getDocumentOutputStream(int infoSourceId, String guid, String mimeType) throws Exception {
		
    	if(StringUtils.trimToNull(guid)==null){
    		throw new IllegalArgumentException("Supplied GUID is empty or null. A stored document must have a non-null identifier.");
    	}
    	
		Document doc = documentDao.getByUri(guid);
		File file = null;
		
		if(doc==null){
			
			doc = new Document();
			doc.setInfoSourceId(infoSourceId);
			doc.setUri(guid);
			doc.setMimeType(mimeType);

			//store in database
			documentDao.save(doc);
			
			//update the filepath
			File directory = getDirectoryForNewDoc(infoSourceId, doc.getId());
			file = getRawOutputFile(directory, mimeType);
			doc.setFilePath(directory.getAbsolutePath());
			documentDao.update(doc);
			doc = documentDao.getByUri(guid);
			
		} else {
			//overwrite...
			documentDao.update(doc);
			File directory = new File(doc.getFilePath());
			file = getRawOutputFile(directory, mimeType);
		}

		//set up the output stream
		DocumentOutputStream dos = new DocumentOutputStream();
		dos.setId(doc.getId());
		dos.setInfoSourceId(infoSourceId);
		dos.setOutputStream(new FileOutputStream(file));
		
		return dos;
	}

	/**
	 * @see org.ala.repository.Repository#getRDFOutputStream(int)
	 */
	@Override
	public DocumentOutputStream getRDFOutputStream(int documentId)
			throws Exception {
		File file = getRDFOutputFile(documentId);
		FileOutputStream fOut = new FileOutputStream(file);
		
		DocumentOutputStream dos = new DocumentOutputStream();
		dos.setId(documentId);
//		dos.infoSourceId
		dos.setOutputStream(fOut);
		return dos;
	}
	
	/**
	 * Save some dublin core information for a document.
	 * 
	 * TODO 
	 * 
     * @param dcProperties
     * @throws IOException
	 */
    @Override
	public void storeDublinCore(int documentId, Map<String,String> dcProperties) throws IOException {
		// add DC properties to an ordered Map to then serialise into JSON
        Map<String,String> dcMap = new LinkedHashMap<String,String>();
		Iterator<String> keys = dcProperties.keySet().iterator();

        while(keys.hasNext()){
			String key = keys.next();
            dcMap.put(key, dcProperties.get(key));
		}
		
		//add source
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // ISO format
        dcMap.put(Predicates.DC_MODIFIED.toString(), sdf.format(now)); // "modified"

        //write out as JSON
		ObjectMapper o = new ObjectMapper();
		String dublinCore = o.writeValueAsString(dcMap);
		log.debug("DC json: "+dublinCore);

		//write to file
		storeMetadata(documentId, dublinCore.getBytes(), FileType.DC.toString());
	}
	
	/**
	 * Store this metadata in a file in the saem directory as the document
	 * with the supplied document id.
	 * 
	 * @param documentId
	 * @param content
	 * @param metadataFileName
	 * @throws IOException
	 */
	public void storeMetadata(int documentId, byte[] content, String metadataFileName) throws IOException {
		
		Document doc = documentDao.getById(documentId);
		String directory = doc.getFilePath();
		
		String fullFilePath = directory + File.separator + metadataFileName;
		File file = new File(fullFilePath);
		if(file.exists()){
			FileUtils.forceDelete(file);
		}

		FileUtils.writeByteArrayToFile(file, content);
	}
	
	/**
	 * @see org.ala.repository.Repository#storeDocument(int, org.ala.repository.ParsedDocument)
	 */
	public Document storeDocument(int infoSourceId, ParsedDocument parsedDocument) throws Exception {
		
		//defensively...
		if(parsedDocument==null){
			return null;
		}
		
		// store the original document
		Document doc = storeDocument(infoSourceId, parsedDocument.getGuid(), parsedDocument.getContent(), parsedDocument.getContentType(), null);
		// store triples
		// TODO add any additional triples from the sitemap
		storeRDF(doc.getId(), parsedDocument.getTriples());
//		// retrieve the Map of DC properties
        Map<String,String> dcProperties = parsedDocument.getDublinCore();
        
        if (doc.getInfoSourceName()!=null && doc.getInfoSourceUri()!=null) {
            // Add dc:publisher & dc:source to properties
            dcProperties.put(Predicates.DC_PUBLISHER.toString(), doc.getInfoSourceName()); // "dc:publisher"
            dcProperties.put(Predicates.DC_SOURCE.toString(), doc.getInfoSourceUri()); // "dc:source"
        }
        // store dublin core properties
		storeDublinCore(doc.getId(), dcProperties);
		return doc;
	}	
	
	/**
	 * Store the supplied file.
	 * 
	 * @param content
	 * @throws IOException
	 */
    @Override
	public Document storeDocument(int infoSourceId, String guid, byte[] content, String mimeType, Integer parentDocumentId) throws IOException {
		
    	if(StringUtils.trimToNull(guid)==null){
    		throw new IllegalArgumentException("Supplied GUID is empty or null. A stored document must have a non-null identifier.");
    	}
    	
		Document doc = documentDao.getByUri(guid);
		
		if(doc==null){
			
			doc = new Document();
			doc.setInfoSourceId(infoSourceId);
			doc.setUri(guid);
			doc.setMimeType(mimeType);
			doc.setParentDocumentId(parentDocumentId);

			//store in database
			documentDao.save(doc);
			
			//update the filepath
			File directory = getDirectoryForNewDoc(infoSourceId, doc.getId());
			saveRawContent(directory, content, mimeType);
			doc.setFilePath(directory.getAbsolutePath());
			documentDao.update(doc);
			
		} else {
			//overwrite...
			documentDao.update(doc);
			File directory = new File(doc.getFilePath());
			saveRawContent(directory, content, mimeType);
		}

        // Refresh document values (infosource name & uri) by DB lookup
        return documentDao.getByUri(guid);
	}

    /**
     * Store the raw byte content for this document.
     * 
     * @param directory
     * @param content
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
	private File saveRawContent(File directory, byte[] content, String contentType)
			throws UnsupportedEncodingException, IOException {
		
		File file = getRawOutputFile(directory, contentType);
		FileUtils.writeByteArrayToFile(file, content);
		
		return file;
	}

	/**
	 * Get an output file to write the raw content to for this document.
	 * 
	 * @param directory
	 * @param contentType
	 * @return
	 * @throws IOException
	 */
	private File getRawOutputFile(File directory, String contentType)
			throws IOException {
		if(!directory.exists()){
			FileUtils.forceMkdir(directory);
		}
		
		//store the raw file
		File file = new File(directory.getAbsolutePath()
				+File.separator
				+FileType.RAW
				+MimeType.getFileExtension(contentType));
		if(file.exists()){
			FileUtils.forceDelete(file);
		}
		file.createNewFile();
		return file;
	}

	/**
	 * Retrieve the next available directory for this infosource.
	 * 
	 * @param infoSourceId
	 * @return
	 * @throws IOException
	 */
	private File getDirectoryForNewDoc(int infoSourceId, int documentId) throws IOException {
		File directory = new File(cacheDirectoryRoot
				+ File.separator
				+ infoSourceId
				+ File.separator
				+ documentId/MAX_FILES_PER_DIRECTORY
				+ File.separator
				+ documentId);
		return directory;
	}

	/**
	 * Store these triples in rdf/xml or n3 or N-Triple format.
	 * 
     * @param triples 
     * @see ala.repository.Repository#storeRDF(int, java.util.List)
	 */
	@Override
	public void storeRDF(int documentId, List<Triple<String, String, String>> triples) throws Exception {
		
		File file = getRDFOutputFile(documentId);
		FileWriter fw = new FileWriter(file);
		
//		if(useTurtle){
			serialiseAsTurtle(fw, triples);
//		} else {
//			serialiseAsTab(fw, triples);
//		}
		
		fw.flush();
		fw.close();
	}

	/**
	 * Get the RDF output file for this document.
	 * 
	 * @param documentId
	 * @return
	 * @throws IOException
	 */
	private File getRDFOutputFile(int documentId) throws IOException {
		Document doc = documentDao.getById(documentId);
		String filePath = doc.getFilePath();
		File directory = new File(filePath);
		if(!directory.exists()){
			FileUtils.forceMkdir(directory);
		}
		
		//store the raw file
		File file = new File(directory.getAbsolutePath()+File.separator+FileType.RDF);
		if(file.exists()){
			FileUtils.forceDelete(file);
		}
		
		file.createNewFile();
		return file;
	}

	/**
	 * Serialise the triples as subject \t predicate \t object
	 * Similar to NTriples.
	 * 
	 * @param writer
	 * @param triples
	 * @throws Exception
	 */
	private void serialiseAsTab(Writer writer, List<Triple<String, String, String>> triples) throws Exception {
		for(Triple<String, String, String> triple: triples){
			writer.write(triple.subject);
			writer.write('\t');
			writer.write(triple.predicate);
			writer.write('\t');
			writer.write(triple.object.toString());
			writer.write('\n');
		}
	}	
	
	/**
	 * Serialise the triples in turtle format.
	 * 
	 * See http://www.w3.org/TeamSubmission/turtle/
	 * 
	 * @param writer
	 * @param triples
	 * @throws RDFHandlerException
	 */
	private void serialiseAsTurtle(Writer writer, List<Triple<String, String, String>> triples)
			throws RDFHandlerException {
		final RDFWriter rdfWriter = new TurtleWriter(writer);
		rdfWriter.startRDF();
		for(Triple<String, String, String> triple: triples){
			rdfWriter.handleStatement(new StatementImpl(new BNodeImpl(triple.subject), new URIImpl(triple.predicate), new LiteralImpl(triple.object.toString())));
		}
		rdfWriter.endRDF();
	}

	/**
	 * @param cacheDirectoryRoot the cacheDirectoryRoot to set
	 */
	public void setCacheDirectoryRoot(String cacheDirectoryRoot) {
		this.cacheDirectoryRoot = cacheDirectoryRoot;
	}

	/**
	 * @param baseDirectory the baseDirectory to set
	 */
	public void setBaseDirectory(String baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	/**
	 * @param documentDao the documentDao to set
	 */
	public void setDocumentDao(DocumentDAO documentDao) {
		this.documentDao = documentDao;
	}
//
//	/**
//	 * @return the useTurtle
//	 */
//	public boolean isUseTurtle() {
//		return useTurtle;
//	}
//
//	/**
//	 * @param useTurtle the useTurtle to set
//	 */
//	public void setUseTurtle(boolean useTurtle) {
//		this.useTurtle = useTurtle;
//	}

	/**
	 * @return the stagingRoot
	 */
	public String getStagingRoot() {
		return stagingRoot;
	}

	/**
	 * @param stagingRoot the stagingRoot to set
	 */
	public void setStagingRoot(String stagingRoot) {
		this.stagingRoot = stagingRoot;
	}

	/**
	 * @return the cacheDirectoryRoot
	 */
	public String getCacheDirectoryRoot() {
		return cacheDirectoryRoot;
	}
}