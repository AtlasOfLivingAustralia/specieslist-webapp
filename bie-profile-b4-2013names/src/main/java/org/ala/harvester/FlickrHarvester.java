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
package org.ala.harvester;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.ala.documentmapper.DocumentMapper;
import org.ala.documentmapper.FlickrDocumentMapper;
import org.ala.model.Licence;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Repository;
import org.ala.repository.Triple;
import org.ala.util.DOMUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * A Harvester class for Flickr. 
 * 
 * 
 * 
 * @author Dave Martin
 */
@Component("flickrHarvester")
@Scope(BeanDefinition.SCOPE_PROTOTYPE) 
public class FlickrHarvester implements Harvester {

	protected Logger logger = Logger.getLogger(FlickrHarvester.class);

	protected String endpoint;
	private String eolGroupId;
	private String userId;
    private String flickrRestBaseUrl;
	private String flickrApiKey;
	private int recordsPerPage;
	protected DocumentMapper documentMapper;
	protected Repository repository;
	protected int timeGap = 0;

	/**
	 * Main method for testing this particular Harvester
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String[] locations = {"classpath*:spring.xml"};
		ApplicationContext context = new ClassPathXmlApplicationContext(locations);
		FlickrHarvester h = new FlickrHarvester();
		Repository r = (Repository) context.getBean("repository"); 
		h.setDocumentMapper(new FlickrDocumentMapper());
		h.setRepository(r);

		//set the connection params	
		Map<String, String> connectParams = new HashMap<String, String>();
		connectParams.put("endpoint", "http://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=08f5318120189e9d12669465c0113351&page=1");
//		connectParams.put("eolGroupId", "806927@N20");
		connectParams.put("eolGroupId", "22545712@N05");
		connectParams.put("flickrRestBaseUrl", "http://api.flickr.com/services/rest");
		connectParams.put("flickrApiKey", "08f5318120189e9d12669465c0113351");
		connectParams.put("recordsPerPage", "50");

		h.setConnectionParams(connectParams);
		h.start(1106); //1013 is the ID for the data source flickr
	}	

	/**
	 * @see org.ala.harvester.Harvester#setConnectionParams(java.util.Map)
	 */
	@Override
	public void setConnectionParams(Map<String, String> connectionParams) {
		this.endpoint = connectionParams.get("endpoint");
		this.eolGroupId = connectionParams.get("eolGroupId");
		this.flickrRestBaseUrl = connectionParams.get("flickrRestBaseUrl");
		this.flickrApiKey = connectionParams.get("flickrApiKey");
		this.recordsPerPage = Integer.parseInt(connectionParams.get("recordsPerPage"));
	}

	@Override
	public void start(int infosourceId, int timeGap) throws Exception {
		this.timeGap = timeGap;
		start(infosourceId);
	}

	/**
	 * @see org.ala.harvester.Harvester#start()
	 */
	@Override
	public void start(int infosourceId) throws Exception {

		
		//get licences maps
		Map<String, Licence> licences = getLicencesMap();
		documentMapper.setLicencesMap(licences);
		
		int totalIndexed = 0;
		Date endDate = new Date();
		Date finalStartDate = DateUtils.parseDate("2004-01-01", new String[]{"yyyy-MM-dd"});
		if(System.getProperty("startDate")!=null){
			endDate = DateUtils.parseDate(System.getProperty("startDate"), new String[]{"yyyy-MM-dd"});
		}
		if(System.getProperty("endDate")!=null){
			finalStartDate = DateUtils.parseDate(System.getProperty("endDate"), new String[]{"yyyy-MM-dd"});
		}
		
		Date startDate = DateUtils.addDays(endDate, -1);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		// page through the images month-by-month
		while(startDate.after(finalStartDate)){
			logger.info("Harvesting time period: "+df.format(startDate)+" to "+df.format(endDate));			
			totalIndexed+=indexTimePeriod(infosourceId, endDate, startDate);
			endDate = startDate;
			startDate = DateUtils.addDays(endDate, -1);
		}
		logger.info("Total harvested: "+totalIndexed);
	}

	public int indexTimePeriod(int infosourceId, Date endDate, Date startDate) {
		int currentPageNum = 1; // index starts from 1
		int totalPages = -1;
		int imagesIndexed = 0;
		// TODO Auto-generated method stub
		while (totalPages==-1 || totalPages>=currentPageNum) {

			// Obtains the image listing on the page number specified.
			// Instance variable `currentResDom` will have new
			// DOM representation of the result.
			try {
				Document parsedDoc = getIndexPage(currentPageNum, startDate, endDate);
	
				if (isDocExtractionSuccessful(parsedDoc)) {

					// returns {currentPageNum, totalPages, actualRecordsPerPage}
					int[] counter = parseDataFragmentationInfo(parsedDoc);
					totalPages = counter[1];
					int photosInPage = counter[2];
					logger.info("Photos in result set: "+photosInPage);
					// Process for each photo found in current index.
					// In XPath array of elements starts with 1, as opposed to 0
					for (int tempCount = 1; tempCount <= photosInPage; tempCount++) {
						// catch error here and continue.
						try {
							boolean success = processSingleImage(infosourceId, tempCount, parsedDoc);
							if(success) imagesIndexed++;
						} catch (Exception err) {
							logger.error("!!ERROR encountered in processing image @ " + "page: " + currentPageNum + " " + "photo: "+ tempCount + " Skipping to next image. " + err.toString(), err);
						}
					} // End of looping through images in index page.
					
				} else {
					logger.error("Extracting page number " + currentPageNum+ " returned error.  Skipping to next page number.");
				}
	
				currentPageNum++;
				
			} catch (Exception e){
				logger.error("Extracting page number " + currentPageNum + " returned error.  Skipping to next page number.", e);
			}
		} // End of infinite loop of parsing index page, then parsing individual images.
		return imagesIndexed;
	}

	/**
	 * Retrieves a map of licences.
	 * 
	 * @return
	 */
	private Map<String, Licence> getLicencesMap() throws Exception {

		final String flickrMethodUri = "flickr.photos.licenses.getInfo";
		String urlToSearch = this.flickrRestBaseUrl + "/" 
		+ "?method=" + flickrMethodUri 
		+ "&api_key=" + this.flickrApiKey; 

		logger.info(urlToSearch);
		logger.debug("URL to search is: " + "`" + urlToSearch + "`" + "\n");

		// Create an instance of HttpClient.
		HttpClient client = new HttpClient();

		// Create a method instance.
		GetMethod method = new GetMethod(urlToSearch);

		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"UTF-8");
		method.getParams().setParameter(HttpMethodParams.HTTP_ELEMENT_CHARSET,"UTF-8");
		method.getParams().setParameter(HttpMethodParams.HTTP_URI_CHARSET,"UTF-8");

		try {
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				String errMsg = "HTTP GET to " + "`" + urlToSearch + "`"
				+ " returned non HTTP OK code.  Returned code "
				+ statusCode + " and message " + method.getStatusLine()
				+ "\n";
				method.releaseConnection();
				throw new Exception(errMsg);
			}
			
			//parse the response
			InputStream responseStream = method.getResponseBodyAsStream();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc =  builder.parse(responseStream);
			XPathFactory xfactory = XPathFactory.newInstance();
			XPath xpath = xfactory.newXPath();

			XPathExpression xe = xpath.compile("/rsp/licenses/license");
			NodeList nodeSet = (NodeList) xe.evaluate(doc, XPathConstants.NODESET);

			Map<String,Licence> licencesMap = new HashMap<String,Licence>();

			for(int i=0; i<nodeSet.getLength();i++){
				NamedNodeMap map = nodeSet.item(i).getAttributes();
				String id = map.getNamedItem("id").getNodeValue();
				Licence licence = new Licence();
				licence.setName(map.getNamedItem("name").getNodeValue());
				licence.setUrl(map.getNamedItem("url").getNodeValue());
				licencesMap.put(id, licence);
			}
			return licencesMap;

		} catch (Exception httpErr) {
			String errMsg = "HTTP GET to `" + urlToSearch + "` returned HTTP error.";
			throw new Exception(errMsg, httpErr);
		} finally {
			// Release the connection.
			method.releaseConnection();
		}
	}

	/**
	 * Process a single image, do the document mapping etc
	 * 
	 * @param infosourceId
	 * @param imageIndex
	 * @param currentResDom
	 * @throws Exception
	 */
	private boolean processSingleImage(int infosourceId, int imageIndex, Document currentResDom) throws Exception {

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		String xPathToPhotoId = "/rsp/photos/photo[" + imageIndex + "]/@id";
		String photoId  = (String) xpath.evaluate(xPathToPhotoId, currentResDom,XPathConstants.STRING);
		logger.info("Handling photo ID: "+photoId);

		final String flickrMethod = "flickr.photos.getInfo";

		// Calls the Flickr's Photo Info API to determine whether the photo
		// comes from Australia or not.
		String photoInfoFlickrUrl = this.flickrRestBaseUrl + "/" + "?method="
		+ flickrMethod + "&" + "api_key=" + this.flickrApiKey + "&"
		+ "photo_id=" + photoId;

		System.out.println("PHOTO URL:" + photoInfoFlickrUrl);

		org.w3c.dom.Document photoInfoDom = null;
		
		// Create an instance of HttpClient.
		HttpClient client = new HttpClient();
		// Create a method instance.
		GetMethod method = new GetMethod(photoInfoFlickrUrl);

		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"UTF-8");
		method.getParams().setParameter(HttpMethodParams.HTTP_ELEMENT_CHARSET,"UTF-8");
		method.getParams().setParameter(HttpMethodParams.HTTP_URI_CHARSET,"UTF-8");

		logger.debug("Fetching info. for photo with ID " + photoId + " from "+ "`" + photoInfoFlickrUrl + "`" + "\n");

		try {
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				String errMsg = "HTTP GET to " + "`" + photoInfoFlickrUrl + "`"
				+ " returned non HTTP OK code.  " + "Returned code "
				+ statusCode + " and message " + method.getStatusLine()
				+ "\n";
				method.releaseConnection();
				logger.error(errMsg);
				throw new Exception(errMsg);
			}

			InputStream responseStream = method.getResponseBodyAsStream();

			// Instantiates a DOM builder to create a DOM of the response.
			DocumentBuilderFactory domFactory = DocumentBuilderFactory
			.newInstance();
			DocumentBuilder domBuilder = domFactory.newDocumentBuilder();

			photoInfoDom = domBuilder.parse(responseStream);

		} catch (Exception domCreationErr) {
			throw new Exception("Failed to create DOM representation of GET response.", domCreationErr);

		} finally {
			// Release the connection.
			method.releaseConnection();
		}

		// Check for Flickr's error.
		if (!isDocExtractionSuccessful(photoInfoDom)) {
			throw new Exception("Flickr error response for fetching single image information.");
		}
		
		if(System.getProperty("overwrite")!=null && "false".equals(System.getProperty("overwrite"))){
			String photoPageUrl = (String) xpath.evaluate("/rsp/photo/urls/url[@type=\"photopage\"]/text()", photoInfoDom,
						XPathConstants.STRING);
			
			logger.debug("photo page URL: "+photoPageUrl);
			org.ala.model.Document doc = this.repository.getDocumentByGuid(photoPageUrl);
			if(doc!=null){
				logger.debug("Document with URI already harvested. Skipping: "+photoPageUrl);
				return true;
			}
		}

		// Determines whether photo has geo-coded tag from Australia.
		// If so, pass onto DocumentMapper.
		if (isPhotoFromAustralia(photoInfoDom)) {
			
			try {
				String document = (DOMUtils.domToString(photoInfoDom));
				// FIXME flickr GUID ???
				List<ParsedDocument> parsedDocs = documentMapper.map("", document.getBytes());
				for(ParsedDocument parsedDoc : parsedDocs){
					this.repository.storeDocument(infosourceId, parsedDoc);
					debugParsedDoc(parsedDoc);
				}
				return false;
			} catch (Exception docMapperErr) {
				// Skipping over errors here and proceeding to next document.
				logger.error( "Problem processing image. "
						+ docMapperErr.toString()
						+", Problem processing: "+photoInfoFlickrUrl, 
						docMapperErr);
			}
		} else {
			logger.debug("Photo is unAustralian: "+ photoInfoFlickrUrl);
		}
		
		return false;
	} // End of `processSingleImage` method.

	/**
	 * Determines whether a Flickr photo has geo-coded location with Australia
	 * as the country. <br />
	 * 
	 * XPath used to extract this information is
	 * <code>/rsp/photo/location/country/text()</code> <br />
	 * 
	 * Non case-sensitive String comparison is performed.
	 * 
	 * @param photoInfoXmlDom
	 *            DOM representation of XML result from calling
	 *            <code>flickr.photos.search</code> Flickr method.
	 * 
	 * @return <code>true</code> if photo has geo-coded location for Australia,
	 *         <code>false</code> otherwise.
	 * 
	 * @throws csiro.diasb.protocolhandlers.Exception
	 *             On error.
	 * 
	 * @since v0.4
	 */
	private boolean isPhotoFromAustralia(org.w3c.dom.Document photoInfoXmlDom) throws Exception {
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		if (photoInfoXmlDom == null) {
			String errMsg = "DOM of Photo Info. XML has null reference.";
			logger.error(errMsg);
			throw new Exception(errMsg);
		}
		
		String photoTitle = (String) xpath.evaluate("/rsp/photo/title/text()", photoInfoXmlDom, XPathConstants.STRING);
		String photoDescription = (String) xpath.evaluate("/rsp/photo/description/text()", photoInfoXmlDom, XPathConstants.STRING);
		String photoCountry = (String) xpath.evaluate("/rsp/photo/location/country/text()", photoInfoXmlDom, XPathConstants.STRING);
		
		//check the machine tags
		String xPathToTags = "/rsp/photo/tags/tag/text()";
		NodeList nl = (NodeList) xpath.evaluate(xPathToTags, photoInfoXmlDom, XPathConstants.NODESET);
		for(int i=0; i<nl.getLength(); i++){
			String content = nl.item(i).getNodeValue();
			if(content!=null){
				content = content.toLowerCase();
				if(content.contains("australia")){
					return true;
				}
			}
		}
		
		if ("australia".compareToIgnoreCase(photoCountry) == 0
				|| (photoTitle!=null && photoTitle.toLowerCase().contains("australia"))
				|| (photoDescription!=null && photoDescription.toLowerCase().contains("australia"))
		) {
			return true;
		} 
		
		return false;
	} // End of `isPhotoFromAustralia` method.

	/**
	 * Parses the XML listing of images to obtain data necessary for future data
	 * extraction. Specifically, the current page number, current images per
	 * page and total number of pages.
	 * 
	 * @since v0.4
	 */
	private int[] parseDataFragmentationInfo(Document currentResDom)
	throws Exception {

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		try {
			int currentPageNum = Integer.parseInt((String) xpath.evaluate("/rsp/photos/@page", currentResDom, XPathConstants.STRING));
			int totalPages = Integer.parseInt((String) xpath.evaluate("/rsp/photos/@pages", currentResDom, XPathConstants.STRING));
			int actualRecordsPerPage = Integer.parseInt((String) xpath.evaluate("count(/rsp/photos/photo)", currentResDom, XPathConstants.STRING));

			logger.debug("Extracted and set current page number to "+ currentPageNum);
			logger.debug("Extracted and set total page number to " + totalPages);
			logger.debug("Actual number of records returned is "+ actualRecordsPerPage);

			return new int[] { currentPageNum, totalPages, actualRecordsPerPage };

		} catch (XPathExpressionException getPageFragmentationError) {
			String errMsg = "Failed to obtain data fragmentation information from Flickr's REST response.";
			throw new Exception(errMsg, getPageFragmentationError);
		}

	} // End of `parseHarvestFragementData` method.

	private boolean isDocExtractionSuccessful(org.w3c.dom.Document resDom) throws Exception {

		if(resDom==null){
			return false;
		}
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		// <rsp stat="fail">
		// <err code="[error-code]" msg="[error-message]" />
		// </rsp>

		String xPathToStatus = "/rsp/@stat";

		String statusString = null;
		try {
			statusString = (String) xpath.evaluate(xPathToStatus, resDom,
					XPathConstants.STRING);
		} catch (XPathExpressionException getStatusStringErr) {
			String errMsg = "Failed to obtain Flickr REST response's status string.";
			logger.error(errMsg);
			throw new Exception(errMsg, getStatusStringErr);
		}
		logger.debug("Response status: "+statusString);
		if ("ok".equals(statusString)) {
			return true;
		} else {
			logger.error("Error response status: "+statusString);
		}

		// Status is false.
		String flickrErrCode = null;
		String flickrErrMsg = null;
		try {
			flickrErrCode = (String) xpath.evaluate("/rsp/err/@code", resDom, XPathConstants.STRING);
			flickrErrMsg = (String) xpath.evaluate("/rsp/err/@msg", resDom, XPathConstants.STRING);
		} catch (XPathExpressionException getErrDetailsErr) {
			String errMsg = "Failed to obtain Flickr REST response's error code and message.";
			logger.error(errMsg);
			throw new Exception(errMsg, getErrDetailsErr);
		}

		String errMsg = "Flickr REST response returned error.  Code: "
			+ flickrErrCode + " " + "Message: " + "`" + flickrErrMsg + "`"
			+ "\n";
		logger.error(errMsg);

		return false;
	} // End of `isDocExtractionSuccessful` successful.

	private org.w3c.dom.Document getIndexPage(int pageNumber, Date startDate, Date endDate) throws Exception {

		final String flickrMethodUri = "flickr.photos.search";

		// Constructs the GET URL to search.

		// `woe_id` is Yahoo! Where On Earth ID.
		// Issue
		// http://api.flickr.com/services/rest/?method=flickr.places.find&api_key=08f5318120189e9d12669465c0113351&query=australia
		// to find Australia.
		// `woe_id` here is country level code, as opposed to continent code.

		
		SimpleDateFormat mysqlDateTime = new SimpleDateFormat("yyyy-MM-dd");
		
		String minUpdateDate = mysqlDateTime.format(startDate);
		String maxUpdateDate = mysqlDateTime.format(endDate);
		
		String urlToSearch = this.flickrRestBaseUrl + "/" 
		+ "?method=" + flickrMethodUri 
		+ "&content_type=1" 
//		+ "&sort=date-posted-asc"
//		+ "&machine_tag_mode=any" 
//		+ "&group_id=" + this.eolGroupId
		+ "&user_id=" + this.eolGroupId
//		+ "&accuracy=3" 
		+ "&privacy_filter=1" 
//		+ "&machine_tags=%22geo:country=Australia%22"
//		+ "&machine_tags=%22taxonomy:binomial=Pogona%20barbata%22"
//		+ "&tags=geo:country=Australia&country=Australia"
//		+ "&has_geo=1" 
//		+ "&accuracy=3" 
//		+ "&woe_id=23424748"
		// MYSQL date time
		+ "&min_upload_date=" + minUpdateDate     //startDate
		+ "&max_upload_date=" + maxUpdateDate	  //endDate
		+ "&api_key=" + this.flickrApiKey 
		+ "&page=" + pageNumber 
		+ "&per_page=" + this.recordsPerPage;
		
//		String urlToSearch = 
//		"http://api.flickr.com/services/rest/?method=flickr.photos.search" +
//		"&api_key=08f5318120189e9d12669465c0113351" +
//		"&page=1" +
//		"&per_page=50" +
//		"&machine_tag_mode=any" +
//		"&content_type=1" +
//		"&group_id=806927@N20&privacy_filter=1" +
//		"&machine_tags=%22taxonomy:binomial=Pogona%20barbata%22";
		
		logger.info("Search URL: "+urlToSearch);
		
		/*
		 * // Default parameters if not supplied. if (this.flickrApiKeySupplied
		 * == false) { urlToSearch += "&" + "api_key=" + this.flickrApiKey; } if
		 * (this.currentPageNumSupplied == false) { urlToSearch += "&" + "page="
		 * + this.currentPageNum; } if (this.recordsPerPageSupplied == false) {
		 * urlToSearch += "&" + "per_page=" + this.recordsPerPage; }
		 */
		//		logger.info(urlToSearch);
		logger.debug("URL to search is: " + "`" + urlToSearch + "`" + "\n");

		// Create an instance of HttpClient.
		HttpClient client = new HttpClient();

		// Create a method instance.
		GetMethod method = new GetMethod(urlToSearch);

		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"UTF-8");
		method.getParams().setParameter(HttpMethodParams.HTTP_ELEMENT_CHARSET,"UTF-8");
		method.getParams().setParameter(HttpMethodParams.HTTP_URI_CHARSET,"UTF-8");

		try {
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				String errMsg = "HTTP GET to " + "`" + urlToSearch + "`"
				+ " returned non HTTP OK code.  Returned code "
				+ statusCode + " and message " + method.getStatusLine()
				+ "\n";
				method.releaseConnection();
				throw new Exception(errMsg);
			}

			InputStream responseStream = method.getResponseBodyAsStream();

			// Instantiates a DOM builder to create a DOM of the response.
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			// return a parsed Document
			return builder.parse(responseStream);

		} catch (Exception httpErr) {
			String errMsg = "HTTP GET to `" + urlToSearch
			+ "` returned HTTP error.";
			throw new Exception(errMsg, httpErr);
		} finally {
			// Release the connection.
			method.releaseConnection();
		}
	} // End of `getIndexPage` method.

	public void debugParsedDoc(ParsedDocument parsedDoc){

		logger.debug("===============================================================================");

		logger.debug("GUID: "+parsedDoc.getGuid());
		logger.debug("Content-Type: "+parsedDoc.getContentType());

		Map<String,String> dublinCore = parsedDoc.getDublinCore();
		for(String key: dublinCore.keySet()){
			logger.debug("DC: "+key+"\t"+dublinCore.get(key));
		}

		List<Triple<String,String,String>> triples = parsedDoc.getTriples(); 
		for(Triple<String,String,String> triple: triples){
			logger.debug("RDF: "+triple.getSubject()+"\t"+triple.getPredicate()+"\t"+triple.getObject());
		}

		logger.debug("===============================================================================");
	}

	/**
	 * @see
	 * org.ala.harvester.Harvester#setDocumentMapper(org.ala.documentmapper.
	 * DocumentMapper)
	 */
	public void setDocumentMapper(DocumentMapper documentMapper) {
		this.documentMapper =  documentMapper;
	}

	/**
	 * @see
	 * org.ala.harvester.Harvester#setRepository(org.ala.repository.Repository)
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}
	
}
