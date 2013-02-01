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
package org.ala.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlHead;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Reusable static utility methods for harvesters
 * 
 * @author Dave Martin
 */
public class WebUtils {

	protected static Logger logger = Logger.getLogger(WebUtils.class);
	
	/**
	 * Retrieve the HTML page as a string.
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String getHTMLPageAsXML(String url, boolean isJavaScriptEnabled) throws Exception {
	    WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3);

        // Disable Javascript as basically we don't need it.  
        webClient.setJavaScriptEnabled(isJavaScriptEnabled);

        URL targetPageUrl = new URL(url);
        WebRequestSettings reqSettings = new WebRequestSettings(targetPageUrl);
        reqSettings.setCharset("UTF-8");

        HtmlPage currentPage = null;

        try {
            currentPage = webClient.getPage(reqSettings);

            webClient.closeAllWindows();
            currentPage.cleanUp();
            HtmlElement idMetaTagElement = currentPage.createElement("meta");
            idMetaTagElement.setAttribute("name", "ALA.Guid");
            idMetaTagElement.setAttribute("scheme", "URL");
            idMetaTagElement.setAttribute("content", url);

            // this.currentPage.appendChild(idMetaTagElement);
            HtmlElement rootElement = currentPage.getDocumentElement();
            List<HtmlHead> headSection = (List<HtmlHead>) rootElement.getByXPath("//html/head");
            HtmlHead currentHtmlHead = headSection.get(0);
            currentHtmlHead.appendChild(idMetaTagElement);

            return currentPage.asXml();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        
        return null;
	}
	
	public static String getHTMLPageAsXML(String url) throws Exception {
	    return getHTMLPageAsXML(url, false);
	}

	/**
	 * Retrieve content as InputStream.
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static InputStream getUrlContent(String url) throws Exception {
		HttpClient httpClient = new HttpClient();
		GetMethod gm = new GetMethod(url);
		httpClient.executeMethod(gm);
		return gm.getResponseBodyAsStream();
	}

	/**
	 * Retrieve content as <code>Response</code> object.
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static Response getUrlContentAsBytes(String url) throws Exception {
		HttpClient httpClient = new HttpClient();
		GetMethod gm = new GetMethod(url);
		httpClient.executeMethod(gm);
		Response r = new Response();
		r.setResponseAsBytes(gm.getResponseBody());
		Header hdr = gm.getResponseHeader("Content-Type");

		if (hdr != null) {
			String contentType = hdr.getValue();
			if(contentType.contains(";")){
				contentType = contentType.substring(0,contentType.indexOf(";"));
				contentType = contentType.trim();
			}
			r.setContentType(contentType);
		}
		return r;
	}

	/**
	 * Retrieve PDF content as byte[]
	 * 
	 * @param urlStr
	 * @return
	 * @throws Exception
	 */
	public static String getTextFromPDFPage(String url, byte[] pdfByteContent) throws Exception {

		PDDocument pdDocument = PDDocument.load(new ByteArrayInputStream(pdfByteContent));

		PDFTextStripper pdfTextStripper = new PDFTextStripper();

		String documentStr = pdfTextStripper.getText(pdDocument);

		pdDocument.close();

		return documentStr;
	}	

	/**
	 * Retrieve content as String.
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String getUrlContentAsString(String url) throws Exception {
		HttpClient httpClient = new HttpClient();
		GetMethod gm = new GetMethod(url);
		gm.setFollowRedirects(true);
		httpClient.executeMethod(gm);
		// String requestCharset = gm.getRequestCharSet();
		String content = gm.getResponseBodyAsString();
		// content = new String(content.getBytes(requestCharset), "UTF-8");
		return content;
	}
}
