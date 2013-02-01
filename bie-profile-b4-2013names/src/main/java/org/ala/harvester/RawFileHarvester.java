/***************************************************************************
x * Copyright (C) 2009 Atlas of Living Australia
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
package org.ala.harvester;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.ala.documentmapper.DocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Repository;
import org.ala.repository.Triple;
import org.ala.util.FileType;
import org.ala.util.MimeType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;


/**
 * @author "Tommy Wang<tommy.wang@csiro.au>"
 */
@Component("rawFileHarvester")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RawFileHarvester implements Harvester {

	protected Logger logger = Logger.getLogger(RawFileHarvester.class);
	protected Repository repository;
	protected DocumentMapper documentMapper;
	//    protected Map<String, String> connectionParams;
	protected String defaultMimeType = MimeType.HTML.getMimeType();
	private Integer infoSourceId = null;
	private String rootDirectory = File.separator+"data"+File.separator+"bie";
	
	private static final String URL_PATTERN = "\\w\\d:#@%/;$()~_?\\+-=\\.\\&";

	private final List<String> TARGET_FILE_NAMES; 
		

	/**
	 * Contructor to set repository
	 *
	 * @param repository
	 */
	@Inject
	public RawFileHarvester(Repository repository) {
		this();
		this.repository = repository;
	}

	/**
	 * Default no-args contructor
	 */
	public RawFileHarvester() {
		TARGET_FILE_NAMES = new ArrayList<String>();
		TARGET_FILE_NAMES.add(FileType.RAW.getFilename()+".html");
		TARGET_FILE_NAMES.add(FileType.RAW.getFilename()+".xml");
	}

	/**
	 * Looks up the details of an infosource, retrieving the details of a 
	 * sitemap to use to crawl an external website. Also makes use of
	 * a mime type to help parse the document.
	 * 
	 * @see org.ala.harvester.Harvester#start(int)
	 */
	@Override
	public void start(int infoSId) throws Exception {

		this.infoSourceId = infoSId;

		try {
			String infoSourceDirLoc = null;
			//        	String mimeType = null;
			//        	String classPath = this.getClass().getClassLoader().getResource("").toString();
			//        	System.out.println("CCCCC" + classPath);
			infoSourceDirLoc = rootDirectory + File.separator + String.valueOf(this.infoSourceId);
			logger.info("File location: "+infoSourceDirLoc);

			File infoSourceDir = new File(infoSourceDirLoc);
			logger.info("Crawling dir: "+infoSourceDir.getAbsolutePath());
			List<File> rawFiles = getRawFileList(infoSourceDir);

			for (File rawFile : rawFiles) {
				try {
//					System.out.println(rawFile.getAbsolutePath());
					harvestDoc(infoSourceId, rawFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			logger.error("start() error: "+e.getMessage(), e);
			e.printStackTrace();
		}
		//		logger.info("CSV Harvester completed for infosource: "+infosourceId
		//				+". Request made: "+urlsHarvested
		//				+", Successfully processed: "+urlsProcessed
		//				+", Urls skipped: "+urlsSkipped);
	}


	private List<File> getRawFileList(File infoSourceDir) {
		List<File> rawFileList = new ArrayList<File>();

		if (infoSourceDir.isDirectory()) {
			logger.info("Start crawling " + TARGET_FILE_NAMES);
			searchForAllFileByName(rawFileList, infoSourceDir, TARGET_FILE_NAMES);
		}

		return rawFileList;
	}

	private void searchForAllFileByName(List<File> resultFileList, File rootDir, List<String> targetFileNames) {
		File[] files = rootDir.listFiles();
		for (File file : files) {
			//			System.out.println(file.getName());
			if (targetFileNames.contains(file.getName())) {
				resultFileList.add(file);
				//				System.out.println(file.getAbsolutePath());
			} else if(file.isDirectory()){
				searchForAllFileByName(resultFileList, file, targetFileNames);
			}
		}
	}


	/**
	 * Harvest a document and update the repository.
	 * 
	 * @param infosourceId
	 * @param url
	 * @param mimeType
	 * @throws Exception
	 */
	private void harvestDoc(int infosourceId, File rawFile) throws Exception {

		byte[] content = null;

		String path = rawFile.getAbsolutePath();
		String url = null;

		String contentAsString = FileUtils.readFileToString(rawFile);
		if(contentAsString!=null){
			url = getGuidFromRawFile(contentAsString);
			
			content = contentAsString.getBytes();
		} else {
			logger.info("NULL " + path);
		}
		//		}

		if(content!=null){
			//map the document 
			
			if (url == null) {
				logger.info("Null GUID");
			} else {
				logger.info("GUID: " + url);
			}
			
			List<ParsedDocument> pds = documentMapper.map(url, content);

			//store the results
			for(ParsedDocument pd: pds){
				repository.storeDocument(infosourceId, pd);
//				debugParsedDoc(pd);
			}
		} else {
			logger.warn("Unable to process url: "+url);
		}
	}
	
	private String getGuidFromRawFile(String str) {
		String guid = null;
		
		Pattern p = Pattern.compile(
				"(?:<meta scheme=\"URL\" content=\")" +
				"([" + URL_PATTERN + "]{1,})" +
//				"([.]{1,})" +
				"(?:\" name=\"ALA\\.Guid\")");
		
		Pattern p2 = Pattern.compile(
				"(?:<meta content=\")" +
				"([" + URL_PATTERN + "]{1,})" +
//				"([.]{1,})" +
				"(?:\" scheme=\"URL\" name=\"ALA\\.Guid\")");
		
		Matcher m = p.matcher(str);
		
		if (m.find(0)) {
			guid = m.group(1);
		} else {
			m = p2.matcher(str);
			
			if (m.find(0)) {
				guid = m.group(1);
			}
		}
		
		return guid;
	}


	private void debugParsedDoc(ParsedDocument parsedDoc){

		System.out.println("===============================================================================");

		System.out.println("GUID: "+parsedDoc.getGuid());
		System.out.println("Content-Type: "+parsedDoc.getContentType());

		Map<String,String> dublinCore = parsedDoc.getDublinCore();
		for(String key: dublinCore.keySet()){
			System.out.println("DC: "+key+"\t"+dublinCore.get(key));
		}

		List<Triple<String,String,String>> triples = parsedDoc.getTriples(); 
		for(Triple<String,String,String> triple: triples){
			System.out.println("RDF: "+triple.getSubject()+"\t"+triple.getPredicate()+"\t"+triple.getObject());
		}

		System.out.println("===============================================================================");
	}

	/**
	 * @param fields
	 * @param string
	 * @return
	 */
	private int getIdxForField(String[] fields, String string) {
		int i=0;
		for(String field: fields){
			if("URI".equalsIgnoreCase(field))
				return i;
			i++;
		}
		return -1;
	}

	/**
	 * Main method for testing this particular Harvester
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		if(args.length!=3){
			printUsage();
			System.exit(1);
		}

		String infosourceId = args[0];
		String siteMap = args[1];
		String documentMapperClass = args[2];

		String[] locations = {"classpath*:spring.xml"};
		ApplicationContext context = new ClassPathXmlApplicationContext(locations);
		RawFileHarvester h = (RawFileHarvester) context.getBean("rawFileHarvester"); 
		h.setDocumentMapper((DocumentMapper) Class.forName(documentMapperClass).newInstance());
		HashMap<String, String> connectParams = new HashMap<String, String>();
		connectParams.put("sitemap", siteMap);
		h.setConnectionParams(connectParams);
		h.start(Integer.parseInt(infosourceId));
	}

	private static void printUsage() {
		System.out.print("Usage: ");
		System.out.print("<infosourceId> <path-to-sitemap> <documentMapperClass>");
		System.out.println();
		System.out.println("infosourceId e.g. 1036");
		System.out.println("path-to-sitemap e.g. /data/bie/1036/siteMap.txt");
		System.out.println("documentMapperClass e.g. org.ala.documentmapper.WikipediaDocumentMapper");
	}

	@Override
	public void setConnectionParams(Map<String, String> connectionParams) {
		//        this.connectionParams = connectionParams;
	}

	@Override
	public void setDocumentMapper(DocumentMapper documentMapper) {
		this.documentMapper = documentMapper;
	}

	@Override
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	@Override
	public void start(int infosourceId, int timeGap) throws Exception {
		// TODO Auto-generated method stub

	}
}
