/**
 * 
 */
package org.ala.harvester;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.ala.documentmapper.DocumentMapper;
import org.ala.documentmapper.FlickrDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Repository;
import org.ala.util.DOMUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

/**
 * A Harvester class for Flickr. 
 * 
 * This is a port from the Diasb codebase which is hideously over-engineered.
 * 
 * @author Dave Martin
 */
@Component("flickrHarvester")
@Scope(BeanDefinition.SCOPE_PROTOTYPE) 
public class FlickrHarvester implements Harvester {

	protected Logger logger = Logger.getLogger(FlickrHarvester.class);

	protected String endpoint;
	private String eolGroupId;
	private String flickrRestBaseUrl;
	private String flickrApiKey;
	private int recordsPerPage;
	protected DocumentMapper documentMapper;
	protected Repository repository;
	
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
        connectParams.put("eolGroupId", "806927@N20");
        connectParams.put("flickrRestBaseUrl", "http://api.flickr.com/services/rest");
        connectParams.put("flickrApiKey", "08f5318120189e9d12669465c0113351");
        connectParams.put("recordsPerPage", "50");
        
        h.setConnectionParams(connectParams);
		h.start(1013);
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

	/**
	 * @see
	 * org.ala.harvester.Harvester#setDocumentMapper(org.ala.documentmapper.
	 * DocumentMapper)
	 */
	@Override
	public void setDocumentMapper(DocumentMapper documentMapper) {
		this.documentMapper = documentMapper;
	}

	/**
	 * @see
	 * org.ala.harvester.Harvester#setRepository(org.ala.repository.Repository)
	 */
	@Override
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ala.harvester.Harvester#start()
	 */
	@Override
	public void start(int infosourceId) throws Exception {

		int currentPageNum = 1; // index starts from 1

		// TODO Auto-generated method stub
		while (true) {

			// Obtains the image listing on the page number specified.
			// Instance variable `currentResDom` will have new
			// DOM representation of the result.
			Document parsedDoc = getIndexPage(currentPageNum);

			if (!isDocExtractionSuccessful(parsedDoc)) {
				logger.error("Extracting page number " + currentPageNum
						+ " returned error.  Skipping to next page number.");
				currentPageNum++;
				continue;
			}

			// returns {currentPageNum, totalPages, actualRecordsPerPage}
			int[] counter = parseDataFragmentationInfo(parsedDoc);

			if (parsedDoc == null) {
				String errMsg = "DOM representation of image list XML has null reference.  "
						+ "Skipping to next page number.\n";
				logger.error(errMsg);
				currentPageNum++;
				if (currentPageNum > counter[1]) {
					break;
				}
				continue;
			}

			// Process for each photo found in current index.
			// In XPath array of elements starts with 1, as opposed to 0
			for (int tempCount = 1; tempCount <= counter[2]; tempCount++) {
				// catch error here and continue.
				try {
					processSingleImage(infosourceId, tempCount, parsedDoc);
				} catch (Exception err) {
					logger.error("!!ERROR encountered in processing image @ "
							+ "page: " + currentPageNum + " " + "photo: "
							+ tempCount + "  " + "Skippig to next image."
							+ "\n" + err.toString());
				}
			} // End of looping through images in index page.

			currentPageNum++;
			if (currentPageNum > counter[1]) {
				break;
			}

		} // End of infinite loop of parsing index page, then parsing individual
			// images.

	}

	private void processSingleImage(int infosourceId, int imageIndex, Document currentResDom)
			throws Exception {

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		// <rsp stat="ok">
		// <photos page="1" pages="1532" perpage="10" total="15314">
		// <photo id="id" owner="owner" secret="sec" server="s" farm="f"
		// title="title" ispublic="1" isfriend="0" isfamily="0"/>
		// </photos>
		// </rsp>

		String xPathToPhotoId = "/rsp/photos/photo[" + imageIndex + "]"
				+ "/@id";
		String photoId = null;
		try {
			photoId = (String) xpath.evaluate(xPathToPhotoId, currentResDom,
					XPathConstants.STRING);
		} catch (XPathExpressionException getPageFragmentationError) {
			String errMsg = "Failed to obtain Flickr's PhotoId";
			logger.error(errMsg);
			throw new Exception(errMsg, getPageFragmentationError);
		}

		final String flickrMethod = "flickr.photos.getInfo";

		// Calls the Flickr's Photo Info API to determine whether the photo
		// comes from Australia or not.
		String photoInfoFlickrUrl = this.flickrRestBaseUrl + "/" + "?method="
				+ flickrMethod + "&" + "api_key=" + this.flickrApiKey + "&"
				+ "photo_id=" + photoId;
		
//		System.out.println(photoInfoFlickrUrl);
		
		org.w3c.dom.Document photoInfoDom = null;

		// Create an instance of HttpClient.
		HttpClient client = new HttpClient();
		// Create a method instance.
		GetMethod method = new GetMethod(photoInfoFlickrUrl);

		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
				"UTF-8");
		method.getParams().setParameter(HttpMethodParams.HTTP_ELEMENT_CHARSET,
				"UTF-8");
		method.getParams().setParameter(HttpMethodParams.HTTP_URI_CHARSET,
				"UTF-8");

		logger.debug("Fetching info. for photo with ID " + photoId + " from "
				+ "`" + photoInfoFlickrUrl + "`" + "\n");

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
			throw new Exception(
					"Failed to create DOM representation of GET response.",
					domCreationErr);

		} finally {
			// Release the connection.
			method.releaseConnection();
		}

		// Check for Flickr's error.
		if (isDocExtractionSuccessful(photoInfoDom) == false) {
			throw new Exception(
					"Flickr error response for fetching single image information.");
		}

		// Determines whether photo has geo-coded tag from Australia.
		// If so, pass onto DocumentMapper.
		if (isPhotoFromAustralia(photoInfoDom)) {
			try {
				String document = (DOMUtils.domToString(photoInfoDom));
//				System.out.println(document);
				// FIXME flickr GUID ???
				List<ParsedDocument> parsedDocs = documentMapper.map("", document.getBytes());
				for(ParsedDocument parsedDoc : parsedDocs){
					this.repository.storeDocument(infosourceId, parsedDoc);
				}

			} catch (Exception docMapperErr) {
				// throw new Exception("Error from document mapper.",
				// docMapperErr);

				// Skipping over errors here and proceeding to next document.
				logger.error(
						"Document Mapper returned error.  Moving on to next record.  "
								+ docMapperErr.toString() + "\n", docMapperErr);
			}

		} // End of photo is from Australia.

		try {
			logger.debug("About to go to sleep after processing one photo.\n");
			Thread.sleep(1000);
		} catch (InterruptedException wakeUp) {
			// Ignore it and move on.
			logger.warn("Sleep after processing a single photo interrupted.");
		}

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
	private boolean isPhotoFromAustralia(org.w3c.dom.Document photoInfoXmlDom)
			throws Exception {

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		String xPathToCountry = "/rsp/photo/location/country/text()";
		String xPathToPhotoId = "/rsp/photo/@id";

		if (photoInfoXmlDom == null) {
			String errMsg = "DOM of Photo Info. XML has null reference.";
			logger.error(errMsg);
			throw new Exception(errMsg);
		}

		String photoId = null;
		try {
			photoId = (String) xpath.evaluate(xPathToPhotoId, photoInfoXmlDom,
					XPathConstants.STRING);
		} catch (XPathExpressionException getPageFragmentationError) {
			String errMsg = "Failed to obtain Flickr's PhotoId";
			logger.error(errMsg);
			throw new Exception(errMsg, getPageFragmentationError);
		}

		String photoCountry = null;
		try {
			photoCountry = (String) xpath.evaluate(xPathToCountry,
					photoInfoXmlDom, XPathConstants.STRING);
		} catch (XPathExpressionException getPageFragmentationError) {
			String errMsg = "Failed to obtain Flickr's Photo's country";
			logger.error(errMsg);
			throw new Exception(errMsg, getPageFragmentationError);
		}

		if (photoCountry == null) {
			String errMsg = "Flickr's Photo's country has null reference.";
			logger.error(errMsg);
			throw new Exception(errMsg);
		}

		if ("Australia".compareToIgnoreCase(photoCountry) == 0) {
			logger.debug("Photo with ID: " + photoId + " has country geo-tag "
					+ "`" + photoCountry + "`"
					+ "  Should be send to document mapper.\n");
			return true;
		} else {
			logger.debug("Photo with ID: " + photoId + " has country geo-tag "
					+ "`" + photoCountry + "`" + "\n");
			return false;
		}

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

		// <rsp stat="ok">
		// <photos page="1" pages="1532" perpage="10" total="15314">
		// </rsp>

		String xPathToCurrentPageNum = "/rsp/photos/@page";
		String xPathToTotalPages = "/rsp/photos/@pages";
		// String xPathToPerPage = "/rsp/photos/@perpage";
		// String xPathToTotolImages = "/rsp/photos/@total";

		String xPathToCountActualNumOfRecord = "count(/rsp/photos/photo)";

		try {
			int currentPageNum = Integer.parseInt((String) xpath
					.evaluate(xPathToCurrentPageNum, currentResDom,
							XPathConstants.STRING));

			int totalPages = Integer.parseInt((String) xpath.evaluate(
					xPathToTotalPages, currentResDom, XPathConstants.STRING));

			int actualRecordsPerPage = Integer.parseInt((String) xpath
					.evaluate(xPathToCountActualNumOfRecord, currentResDom,
							XPathConstants.STRING));

			logger.debug("Extracted and set current page number to "
					+ currentPageNum + "\n");
			logger.debug("Extracted and set total page number to " + totalPages
					+ "\n");
			logger.debug("Actual number of records returned is "
					+ actualRecordsPerPage + "\n");

			return new int[] { currentPageNum, totalPages, actualRecordsPerPage };

		} catch (XPathExpressionException getPageFragmentationError) {
			String errMsg = "Failed to obtain data fragmentation information from Flickr's REST response.";
			throw new Exception(errMsg, getPageFragmentationError);
		}

	} // End of `parseHarvestFragementData` method.

	private boolean isDocExtractionSuccessful(org.w3c.dom.Document resDom)
			throws Exception {

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

		if ("ok".equals(statusString)) {
			return true;
		}

		// Status is false.
		String xPathToErrCode = "/rsp/err/@code";
		String xPathToErrMsg = "/rsp/err/@msg";

		String flickrErrCode = null;
		String flickrErrMsg = null;
		try {
			flickrErrCode = (String) xpath.evaluate(xPathToErrCode, resDom, XPathConstants.STRING);
			flickrErrMsg = (String) xpath.evaluate(xPathToErrMsg, resDom, XPathConstants.STRING);
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

	private org.w3c.dom.Document getIndexPage(int pageNumber) throws Exception {

		final String flickrMethodUri = "flickr.photos.search";

		// Constructs the GET URL to search.

		// `woe_id` is Yahoo! Where On Earth ID.
		// Issue
		// http://api.flickr.com/services/rest/?method=flickr.places.find&api_key=08f5318120189e9d12669465c0113351&query=australia
		// to find Australia.
		// `woe_id` here is country level code, as opposed to continent code.

		String urlToSearch = this.flickrRestBaseUrl + "/" + "?method="
				+ flickrMethodUri + "&" + "content_type=1" + "&"
				+ "machine_tag_mode=any" + "&" + "group_id=" + this.eolGroupId
				+ "&" + "accuracy=3" + "&" + "privacy_filter=1" + "&"
				+ "has_geo=1" + "&" + "accuracy=3" + "&" + "woe_id=23424748"
				+ "&" + "api_key=" + this.flickrApiKey + "&" + "page="
				+ pageNumber + "&" + "per_page=" + this.recordsPerPage;

		/*
		 * // Default parameters if not supplied. if (this.flickrApiKeySupplied
		 * == false) { urlToSearch += "&" + "api_key=" + this.flickrApiKey; } if
		 * (this.currentPageNumSupplied == false) { urlToSearch += "&" + "page="
		 * + this.currentPageNum; } if (this.recordsPerPageSupplied == false) {
		 * urlToSearch += "&" + "per_page=" + this.recordsPerPage; }
		 */
		System.out.println(urlToSearch);
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
}
