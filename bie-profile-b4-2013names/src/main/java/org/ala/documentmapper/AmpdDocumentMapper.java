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
package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.MimeType;
import org.ala.util.Response;
import org.ala.util.WebUtils;
import org.w3c.dom.Document;

/**
 * Document mapper for Millipedes of Australia
 *
 * @author Tommy Wang (Tommy.Wang@csiro.au)
 */
public class AmpdDocumentMapper extends XMLDocumentMapper {

	/**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */
	public AmpdDocumentMapper() {
		
		setRecursiveValueExtraction(true);
		
		//set the content type this doc mapper handles
		this.contentType = MimeType.HTML.toString();
		
		//set an initial subject
		String subject = MappingUtils.getSubject();
		
		// subject, predicate namespace, predicate, 
		//addDCMapping("//a[@id=\"top\"]/text()", subject, Predicates.DC_TITLE);

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
        addDCMapping("//p/b/text()",subject, Predicates.DC_TITLE);
        addTripleMapping("//p[@class=\"indentsmall\"]/font[contains(.,\"Locality\")]/following-sibling::text()[1]", subject, Predicates.DISTRIBUTION_TEXT);
        addTripleMapping("//p[string-length(@class)=0]/b/text()", subject, Predicates.SCIENTIFIC_NAME);
        
//		
////		addTripleMapping("//p[b[contains(.,\"Attached Images\")]]/following::p[1]/a/attribute::href",
//		addTripleMapping("//img[@class=\"page\"]/attribute::src",
//				subject, Predicates.IMAGE_URL);
//		
//		addTripleMapping("//a[@class=\"mappopup\"]/span/img/attribute::src",
//                subject, Predicates.DIST_MAP_IMG_URL);
	}
	
	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
		throws Exception {
		
		String documentStr = new String(content);
		
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<i>[\\s&&[^ ]]{0,}", "");
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</i>[\\s&&[^ ]]{0,}", "");
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<em>[\\s&&[^ ]]{0,}", "");
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</em>[\\s&&[^ ]]{0,}", "");
		
		String[] tmp = uri.split("#");
		String anchor = null;		
//		
		if (tmp.length > 1) {
		    anchor = tmp[tmp.length - 1];
		}
//		
//		int bodyOpenIndex = documentStr.indexOf("<body>");
//		int bodyCloseIndex = documentStr.indexOf("</body>");
//		int anchorIndex = documentStr.indexOf("<a name=\"" + anchor + "\">");
//		
//		int tmpLastIndex = 0;
//		
//		while (true) {
//		    tmpLastIndex = documentStr.lastIndexOf("<a name =\"");
//		    
//		    if (tmpLastIndex != anchorIndex) {
//		        if (tmpLastIndex != anchorIndex) {
//                    
//                }
//		    } else {
//		        break;
//		    }
//		}
		
//		System.out.println(bodyOpenIndex + ": " + anchorIndex);
//		System.out.println(documentStr);
		
		tmp = documentStr.split("<hr width=\"75%\"/>");
		
		if (tmp.length > 1 && anchor != null) {
		    for (int i = 0; i < tmp.length - 1; i ++) {
		        if (tmp[i].contains("<a name=\"" + anchor + "\">")) {
//		            System.out.println("!!!!!!!!!!!!!" + tmp.length);
		            documentStr = tmp[0] + tmp[i]; 
		        }
		    }
		}
//		System.out.println(documentStr);
		
		if (!documentStr.contains("</html>")) {
		    documentStr += "</td></tr></tbody></table></body></html>";
		}
		
		content = documentStr.getBytes();
		
		return super.map(uri, content);
	}

	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		// extract details of images
		String source = "http://www.polydesmida.info/tasmanianmultipedes/";
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
		
		String titleStr = null; 
		String subject = MappingUtils.getSubject();
		
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			
			if(predicate.endsWith("hasDistributionText")) {
			    String currentObj = (String) triple.getObject();
                String newObj = currentObj.replaceFirst("\\. ", "");
                triple.setObject(newObj);
			}
		}

		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "Creative Commons-Attribution-NonCommercial");
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
//		triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), titleStr));
		
		//remove the triple from the triples
		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}
		
		//replace the list of triples
		pd.setTriples(triples);
	}
	
	private String cleanBrackets(String str) {
		str = str.replaceAll("\\(", "");
		str = str.replaceAll("\\)", "");
		
		if ("".equals(str.trim())) {
			return null;
		} else {
			return str.trim();
		}
		
	}
}
