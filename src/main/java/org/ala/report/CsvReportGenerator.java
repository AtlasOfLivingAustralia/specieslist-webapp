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
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;

import org.ala.dao.StoreHelper;
import org.ala.dao.TaxonConceptDao;
import org.ala.model.CommonName;
import org.ala.model.ConservationStatus;
import org.ala.model.SimpleProperty;
import org.ala.repository.Predicates;
import org.ala.util.SpringUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import au.org.ala.checklist.lucene.CBIndexSearch;
import au.org.ala.checklist.lucene.SearchResultException;

/**
 * Generate CSV format report.
 * 
 * @author MOK011
 *
 */

@Component
public class CsvReportGenerator {
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
		CsvReportGenerator reportGen = (CsvReportGenerator) context
				.getBean(CsvReportGenerator.class);
		reportGen.runReport(args[0], args[1]);
		System.exit(0);
	}

	public CsvReportGenerator() {
	}

	public void runReport(String inputFile, String outputPath) throws Exception {
		long start = System.currentTimeMillis();
		System.out.println("Report process is started.....");
		DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmm");
		String fileName = dateFormat.format( new java.util.Date());
		
//		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outputPath + "/report_" + fileName + ".csv"),"8859_1");		
//		CSVWriter writer = new CSVWriter(osw, ',');
		CSVWriter writer = new CSVWriter(new FileWriter(outputPath + "/report_" + fileName + ".csv"), ',');		
		CSVReader reader = new CSVReader(new FileReader(inputFile));
		
		String[] nextLine;


		String scientificName = null;
		String commonName = null;
		String description = null;
		String status = null;
		String galleryLink = null;
		String overviewLink = null;
		String imageLink = null;

		csvWriteHeader(writer);
		while ((nextLine = reader.readNext()) != null) {
			scientificName = nextLine[0];
			String guid = getGuid(scientificName);
			if (guid == null) {
				break;
			}

			status = getStatus(guid);
			commonName = getCommonName(guid);
			description = getDescription(guid);
			galleryLink = getGalleryLink(guid);
			overviewLink = getOverviewLink(guid);
			imageLink = getImageLink(guid, outputPath, fileName + "_" + (ctr++) + ".jpg");

			csvWriteLine(writer, scientificName, commonName, description,
					status, galleryLink, overviewLink, imageLink);
		}
		writer.close();
		System.out.println("Total time taken: "
				+ (System.currentTimeMillis() - start));

	}
		
	private String getGalleryLink(String guid) {
		if (guid == null) {
			return "";
		}
		return "http://bie.ala.org.au/species/" + guid + "#gallery";
	}

	private String getOverviewLink(String guid) {
		if (guid == null) {
			return "";
		}
		return "http://bie.ala.org.au/species/" + guid + "#overview";
	}

	private String getImageLink(String guid, String dir,String fileName) throws FileNotFoundException {
		GetMethod method = null;
		FileOutputStream fos = null;
		
		if (guid == null) {
			return "";
		}

		try {
			HttpClient client = new HttpClient();

			method = new GetMethod(" http://spatial.ala.org.au/alaspatial/ws/density/map?species_lsid=" + guid);
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				logger.debug("Method failed: " + method.getStatusLine());
			}

			// Read the response body.
			byte[] responseBody = method.getResponseBody();
			if(responseBody.length > 0){
				fos = new FileOutputStream(dir + "/" + fileName);
				fos.write(responseBody);		
				fos.close();
			}
		} 
		catch (Exception e) {
			logger.debug("ERROR...Distribution image not found: " + guid);
		} 
		finally {
			// Release the connection.
			method.releaseConnection();
		}
		return fileName;
	}

	private String getStatus(String guid) {
		StringBuffer sb = new StringBuffer();

		if (guid == null) {
			return "";
		}

		try {
			List<ConservationStatus> l = taxonConceptDao
					.getConservationStatuses(guid);
			for (int i = 0; i < l.size(); i++) {
				sb.append(l.get(i).getStatus());
				if (l.size() > (i + 1)) {
					sb.append("; ");
				}
			}
		} catch (Exception e) {
			logger.debug("ERROR...Status not found: " + guid);
		}
		return sb.toString();
	}

	private String getDescription(String guid) {
		StringBuffer sb = new StringBuffer();

		if (guid == null) {
			return "";
		}

		try {
			List<SimpleProperty> l = taxonConceptDao.getTextPropertiesFor(guid);			
			for (int i = 0; i < l.size(); i++) {
				if(l.get(i).getName().endsWith(Predicates.DESCRIPTIVE_TEXT.getLocalPart())){
					sb.append("[ " + l.get(i).getValue() + " ];");
				}
			}
			//remove last ';' mark
			if(sb.length() > 0){
				sb.deleteCharAt(sb.length() - 1);
			}
		} catch (Exception e) {
			logger.debug("ERROR...Description not found: " + guid);
		}
		return sb.toString();
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

	private String getCommonName(String guid) {
		StringBuffer sb = new StringBuffer();

		if (guid == null) {
			return "";
		}

		try {
			List<CommonName> lcn = taxonConceptDao.getCommonNamesFor(guid);
			for (int i = 0; i < lcn.size(); i++) {
				sb.append(lcn.get(i).getNameString());
				if (lcn.size() > (i + 1)) {
					sb.append("; ");
				}
			}
		} catch (Exception e) {
			logger.debug("ERROR...Common Name not found: " + guid);
		}
		return sb.toString();
	}

	private void csvWriteHeader(CSVWriter writer) {
		String[] outLine = { "Scientific Name", "Common Name", "Description",
				"Conservation Status", "Gallery Page", "Overview Page",
				"Image Page" };

		if (writer != null) {
			writer.writeNext(outLine);
		}
	}

	private void csvWriteLine(CSVWriter writer, String scientificName,
			String commonName, String description, String status,
			String galleryLink, String overviewLink, String imageLink) {
		String[] outLine = new String[7];

		if (writer != null) {
			outLine[0] = scientificName;
			outLine[1] = commonName;
			outLine[2] = description;
			outLine[3] = status;
			outLine[4] = galleryLink;
			outLine[5] = overviewLink;
			outLine[6] = imageLink;

			writer.writeNext(outLine);
		}
	}

	/**
	 * @param taxonConceptDao
	 *            the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}
}
