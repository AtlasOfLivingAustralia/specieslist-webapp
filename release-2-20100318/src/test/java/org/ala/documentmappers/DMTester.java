package org.ala.documentmappers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.ala.documentmapper.*;
import org.ala.repository.ParsedDocument;
import org.ala.util.Response;
import org.ala.util.WebUtils;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;


public class DMTester {
	
	private final String sitemapUrl = "http://www2.ala.org.au/sitemaps/sab/siteMap.txt";
	private DocumentMapper documentMapper = new SabDocumentMapper();
	
	@Test
	public void testDocumentMapper() throws Exception {
		
		String sitemapContent;
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3);
		webClient.setJavaScriptEnabled(false);
		URL sitemapPage = null;
		// get all the page links from a sitemap		
		try {
			sitemapPage = new URL(sitemapUrl);
			WebRequestSettings reqSettings = new WebRequestSettings(sitemapPage);
			TextPage page = webClient.getPage(reqSettings);
			page.cleanUp();
			sitemapContent = page.getContent();
			//System.out.println(sitemapContent);
			
		} catch (MalformedURLException urlErr) {
			throw new Exception("Supplied species page URL is malformed: "+urlErr.getMessage(), urlErr);
		} catch (FailingHttpStatusCodeException e) {
			throw new Exception(e.getMessage(), e);
		} catch (IOException e) {
			throw new Exception(e.getMessage(), e);
		}
		
		String[] pageLinks = sitemapContent.split("\\n");
		
		// clean up the page link formats
		for (int i = 1; i < pageLinks.length; i++) {
			pageLinks[i] = pageLinks[i].split("\\t")[0].trim();
			pageLinks[i] = pageLinks[i].replaceAll("\"", "");
			//System.out.println(pageLinks[i]);
		}
		
		// iterate through every page link and get all the triples
		for (int i = 1; i < pageLinks.length; i++) {
			try {
				Response response = WebUtils.getUrlContentAsBytes(pageLinks[i]);
				byte[] contentBytes = response.getResponseAsBytes();
				List<ParsedDocument> parsedDocs = documentMapper.map(pageLinks[i], contentBytes);
				
				for(ParsedDocument pd : parsedDocs){
					DebugUtils.debugParsedDoc(pd);
				}
				
//			} catch (MalformedURLException urlErr) {
//				throw new Exception("Supplied species page URL is malformed: "+urlErr.getMessage(), urlErr);
//			} catch (FailingHttpStatusCodeException e) {
//				throw new Exception(e.getMessage(), e);
//			} catch (IOException e) {
//				throw new Exception(e.getMessage(), e);
//			
			} catch (Exception e) {
				
			}
		}
	}
}
