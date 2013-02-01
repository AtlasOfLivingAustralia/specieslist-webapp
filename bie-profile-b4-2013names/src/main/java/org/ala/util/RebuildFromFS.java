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
package org.ala.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import org.ala.dao.DocumentDAO;
import org.ala.model.Document;
import org.ala.repository.Predicates;
import org.ala.repository.Repository;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Utility to rebuild entries in "document" table if DB
 * and Filesystem happen to become out of sync.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Component("rebuilder")
public class RebuildFromFS {

	protected Logger logger = Logger.getLogger(RebuildFromFS.class);
	
	@Inject
	RepositoryFileUtils repositoryFileUtils;
	
	@Inject
	Repository repository;
	
	@Inject
	DocumentDAO documentDao;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring.xml");
		RebuildFromFS r = (RebuildFromFS) context.getBean("rebuilder");
		if(args.length==0){
			r.rebuildFS("/data/bie", "/data/bie-new");
			r.rebuildDatabase("/data/bie");
		}
		if(args.length==1 || "fs".equals(args[0])){
			r.rebuildFS("/data/bie", "/data/bie-new");
		}
		if(args.length==2 || "db".equals(args[0])){
			r.rebuildDatabase(args[1]);
		}
	}

	/**
	 * 
	 * @param directoryRoot
	 * @param newDirectoryRoot
	 * @throws Exception
	 */
	private void rebuildFS(String directoryRoot, String newDirectoryRoot) throws Exception {

		int fileIndex = 1;
		File oldDirectory = new File(directoryRoot);
		
		//this gives the directory per infosource
		File[] dirs = oldDirectory.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
 
		//iterate through each infosource directory
		for (File infosourceDir : dirs) {
			
			System.out.println("Reading directory: " + infosourceDir.getAbsolutePath());
			
			String infosource = infosourceDir.getName();
			
			File[] subDirectories = infosourceDir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
			
			for(File subDirectory : subDirectories){
				
				File[] directories = subDirectory.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
				
				String subDirectoryPath = Integer.toString(fileIndex / 10000);
				
				//move each directory
				FileUtils.forceMkdir(new File(directoryRoot + File.separator + infosource + File.separator + subDirectoryPath));
				
				for(File directory : directories){
					
					FileUtils.moveDirectory(directory, new File(newDirectoryRoot + File.separator + infosource + File.separator + subDirectoryPath + File.separator + fileIndex));
					fileIndex++;
				}
			}
		}
		
		//remove the temporary dir
		FileUtils.forceDelete(new File(directoryRoot));
		FileUtils.moveDirectory(new File(newDirectoryRoot), new File(directoryRoot));
	}

	/**
	 * Rebuild the house keeping database from the filesystem.
	 * 
	 * @param directoryRoot
	 */
	private void rebuildDatabase(String directoryRoot) {
		File file = new File(directoryRoot);
		
		File[] dirs = file.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
 
		for (File currentDir : dirs) {
			System.out.println("Reading directory: " + currentDir.getAbsolutePath());
			Iterator<File> fileIterator = FileUtils.iterateFiles(currentDir, null, true);
			while (fileIterator.hasNext()) {
				File currentFile = fileIterator.next();
				if(currentFile.getName().equals(FileType.DC.toString())){
					//lets make a record
					
					try {
						Map<String,String> properties = repositoryFileUtils.readDcFileAsMap(currentFile);
						
						Document document = new Document();
						document.setFilePath(currentFile.getParentFile().getAbsolutePath());
						Date lastModified = new Date(currentFile.lastModified());
						document.setInfoSourceId(Integer.parseInt(currentFile.getParentFile().getParentFile().getParentFile().getName()));
						document.setCreated(lastModified);
						document.setModified(lastModified);
						document.setUri(properties.get(Predicates.DC_IDENTIFIER.toString()));
						document.setMimeType(properties.get(Predicates.DC_FORMAT.toString()));
						
						String parentGuid = properties.get(Predicates.DC_IS_PART_OF.toString());
						if(parentGuid!=null){
							Document parentDocument = documentDao.getByUri(parentGuid);
							if(parentDocument!=null){
								document.setParentDocumentId(parentDocument.getId());
							} else {
								logger.warn("Unable to find doc for parent guid: "+parentGuid);
							}
						}
						
						int documentId = Integer.parseInt(currentFile.getParentFile().getName());
						document.setId(documentId);
						Document doc = documentDao.getByUri(document.getUri());
						if(doc==null){
							documentDao.save(document);
						} else {
							documentDao.update(document);
						}
					} catch (Exception e){
						System.err.println("Problem with "+currentFile.getAbsolutePath()+", error:"+e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * @param repositoryFileUtils the repositoryFileUtils to set
	 */
	public void setRepositoryFileUtils(RepositoryFileUtils repositoryFileUtils) {
		this.repositoryFileUtils = repositoryFileUtils;
	}

	/**
	 * @param repository the repository to set
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/**
	 * @param documentDao the documentDao to set
	 */
	public void setDocumentDao(DocumentDAO documentDao) {
		this.documentDao = documentDao;
	}
}
