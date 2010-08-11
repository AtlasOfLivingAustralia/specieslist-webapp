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
package org.ala.report;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;
import org.ala.model.Image;
import org.ala.dao.StoreHelper;
import org.ala.dao.TaxonConceptDao;
import org.ala.util.SpringUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import au.org.ala.checklist.lucene.CBIndexSearch;
import au.org.ala.checklist.lucene.SearchResultException;

/**
 * Generate CSV format image report.
 * 
 * @author MOK011
 *
 */

@Component 
public class CsvImageReportGenerator {
	protected Logger logger = Logger.getLogger(this.getClass());

	@Inject
	protected TaxonConceptDao taxonConceptDao;
	@Inject
	protected StoreHelper storeHelper;
	@Inject
	protected CBIndexSearch searcher;

	private int ctr = 1;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Input File Name && Output Directory....");
			System.exit(0);
		}
		ApplicationContext context = SpringUtils.getContext();
		CsvImageReportGenerator reportGen = (CsvImageReportGenerator) context.getBean(CsvImageReportGenerator.class);
		reportGen.runReport(args[0], args[1]);
		System.exit(0);
	}

	public CsvImageReportGenerator() {
	}

	public void runReport(String inputFile, String outputPath) throws Exception {
		long start = System.currentTimeMillis();
		System.out.println("Report process is started.....");
		DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmm");
		String fileName = dateFormat.format( new java.util.Date());
		
		CSVWriter writer = new CSVWriter(new FileWriter(outputPath + "/report_" + fileName + ".csv"), ',');		
		CSVReader reader = new CSVReader(new FileReader(inputFile));
		
		String[] nextLine;


		String scientificName = null;

		csvWriteHeader(writer);
		while ((nextLine = reader.readNext()) != null) {
			scientificName = nextLine[0];
			String guid = getGuid(scientificName);
			if (guid == null) {
				break;
			}
			String name = fileName + "_" + (ctr++);
			int imageCtr = getImageFromRepo(guid, outputPath, name);
			for(int i = 0; i < imageCtr; i++){
				csvWriteLine(writer, scientificName, name + "_" + i + ".jpg");
			}
			System.out.println("scientificName: " + scientificName);
		}
		writer.close();
		System.out.println("Total time taken (sec): "	+ ((System.currentTimeMillis() - start)/1000));

	}
		
	private String getGuid(String scientificName) {
		String guid = null;

		try {
			if (searcher == null || scientificName == null
					|| scientificName.length() == 0) {
				return guid;
			}
			guid = searcher.searchForLSID(scientificName);
		} catch (SearchResultException e) {
			logger.debug("ERROR...GUID not found: " + scientificName);
		}
		return guid;
	}

	private void csvWriteHeader(CSVWriter writer) {
		String[] outLine = { "Scientific Name", "Image File" };

		if (writer != null) {
			writer.writeNext(outLine);
		}
	}

	private void csvWriteLine(CSVWriter writer, String scientificName, String imageLink) {
		String[] outLine = new String[7];

		if (writer != null) {
			outLine[0] = scientificName;
			outLine[1] = imageLink;

			writer.writeNext(outLine);
		}
	}

	private int getImageFromRepo(String guid, String dir, String fileName) throws FileNotFoundException {
		GetMethod method = null;
		FileOutputStream fos = null;
		String prefix = "/data/bie/";
		int i = 0;
		
		if (guid == null) {
			return i;
		}

		try {
			List<Image> l = taxonConceptDao.getImages(guid);
			for(i = 0; i < l.size(); i++){
				Image image = l.get(i);
				String link = image.getRepoLocation();
				String url = "";
				if(link != null && link.startsWith(prefix)){					
					url = "http://bie.ala.org.au/repo/" + link.substring(prefix.length());
				
					HttpClient client = new HttpClient();
		
					method = new GetMethod(url);
					// Execute the method.
					int statusCode = client.executeMethod(method);
		
					if (statusCode != HttpStatus.SC_OK) {
						logger.debug("Method failed: " + method.getStatusLine());
					}
		
					// Read the response body.
					InputStream is = method.getResponseBodyAsStream();
					byte[] responseBody = IOUtils.toByteArray(is);
					if(responseBody.length > 0){
						fos = new FileOutputStream(dir + "/" + fileName + "_" + i + ".jpg");
						fos.write(responseBody);		
						fos.close();
					}
				}
				else{
					logger.debug("ERROR...image not found: " + guid + " getRepoLocation():" + link);
				}
			}
		} 
		catch (Exception e) {
			logger.debug("ERROR...image not found: " + guid);
		} 
		finally {
			// Release the connection.
			if(method != null){
				method.releaseConnection();
			}
		}
		return i;
	}
	
	/**
	 * @param taxonConceptDao
	 *            the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}
}
