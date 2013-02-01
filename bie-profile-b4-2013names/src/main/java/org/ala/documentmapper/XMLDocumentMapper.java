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

import java.io.StringReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.ala.model.Licence;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.input.DOMBuilder;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * A Document Mapper for mapping XML Documents. This is intended to be extended by 
 * DocumentMapper implementation that are processing XML or XHTML.
 * 
 * @author Dave Martin
 */
public abstract class XMLDocumentMapper implements DocumentMapper {

	protected Logger logger = Logger.getLogger(XMLDocumentMapper.class);

	/** The URI of the current document */
	protected String uri = null;

	/** Collection of XPaths where data will be extracted from for Dublin Core properties */
	private List<Mapping> dcMappingList = new ArrayList<Mapping>();
	
	/** Collection of XPaths where data will be extracted from for triples */
	private List<Mapping> tripleMappingList = new ArrayList<Mapping>();

	/** The default content type to add */
	protected String contentType = "text/xml";
	
	/** A map of licences */
	protected Map<String, Licence> licencesMap;
	
	/**
	 * @see org.ala.documentmapper.DocumentMapper#map(java.lang.String, byte[])
	 */
	public List<ParsedDocument> map(String uri, byte[] content) throws Exception {

		this.uri = uri;

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(false);

		InputSource is = new InputSource(new StringReader(new String(content, "UTF-8")));
		DocumentBuilder parser = dbFactory.newDocumentBuilder();
		Document document = null;
		
		try {
			document = parser.parse(is);
		} catch (Exception e) {
			logger.warn("Unable to process document. Message:"+e.getMessage(), e);
			return new ArrayList<ParsedDocument>();
		}

		ParsedDocument pd = new ParsedDocument();
		pd.setGuid(URLDecoder.decode(uri, "UTF-8"));
		pd.getDublinCore().put(Predicates.DC_IDENTIFIER.toString(), URLDecoder.decode(uri, "UTF-8"));
		pd.setContent(content);
		pd.setContentType(this.contentType);

		//map the dublin core properties
		doMapping(dcMappingList, document, pd, true);
		
		//map the triple properties		
		doMapping(tripleMappingList, document, pd, false);
		
		List<ParsedDocument> pds = new ArrayList<ParsedDocument>();
		pds.add(pd);

		extractProperties(pds, document);

		return pds;
	}

	/**
	 * Map the fields configured in the supplied <code>mappingList</code>.
	 * 
	 * @param mappingList
	 * @param document
	 * @param parsedDoc
	 * @param isDublinCore
	 */
	private void doMapping(List<Mapping> mappingList, Document document, ParsedDocument parsedDoc, boolean isDublinCore) {
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		// if (getNamespaceContext() != null)
		// xpath.setNamespaceContext(getNamespaceContext());
		for (Mapping mapping : mappingList) {
			
			if(mapping.mappingType==MappingType.XPATH){
				performXPathMapping(document, parsedDoc, isDublinCore, xpath, mapping);
			} else if(mapping.mappingType==MappingType.REGEX){
				performRegexMapping(document, parsedDoc, isDublinCore, mapping);
			}
		}
	}

	/**
	 * Map properties using a regular expression.
	 * 
	 * @param document
	 * @param parsedDoc
	 * @param isDublinCore
	 * @param mapping
	 */
	private void performRegexMapping(Document document,
			ParsedDocument parsedDoc, boolean isDublinCore, Mapping mapping) {
		// regex handler
		DOMBuilder builder = new DOMBuilder();
		XMLOutputter xml = new XMLOutputter();
		String docString = new String();
		
		try {
			docString = xml.outputString(builder.build(document));  
		} catch (Exception e) {
			
		}
		
		String value = new String();

		Pattern p = Pattern.compile(mapping.getQueryString());
		Matcher m = p.matcher(docString);

		int searchIdx = 0;

		while(m.find(searchIdx)){
			int endIdx = m.end();

			for (int i = 1 ; i <= m.groupCount(); i++) {
				value += " " + m.group(i);
			}

			createTriples(parsedDoc, isDublinCore, mapping, value);
			
			searchIdx = endIdx;
		}
	}

	/**
	 * Uses the supplied xpath to retrieve values
	 * 
	 * @param document
	 * @param xpathAsString
	 * @return
	 * @throws Exception
	 */
	protected List<String> getXPathValues(Document document, String xpathAsString) throws Exception {
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		List<String> extractedValues = new ArrayList<String>();
		NodeList nodes = (NodeList) xpath.evaluate(xpathAsString, document, XPathConstants.NODESET);

		for (int i = 0; i < nodes.getLength(); i++) {
			String value = extractValue(nodes.item(i));
			value = StringUtils.trimToNull(value);
			if(value!=null){
				extractedValues.add(value);
			}
		}
		return extractedValues;
	}
	
	/**
	 * Uses the supplied xpath to retrieve values
	 * 
	 * @param document
	 * @param xpathAsString
	 * @return
	 * @throws Exception
	 */
	protected String getXPathSingleValue(Document document, String xpathAsString) throws Exception {
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		NodeList nodes = null;
		
		try {
			nodes = (NodeList) xpath.evaluate(xpathAsString, document, XPathConstants.NODESET);
			
			for (int i = 0; i < nodes.getLength(); i++) {
				String value = extractValue(nodes.item(i));
				value = StringUtils.trimToNull(value);
				if(value!=null){
					return value;
				}
			}
		} catch (XPathException e) {
			String value = (String) xpath.evaluate(xpathAsString, document, XPathConstants.STRING);
			return value;
		}

		
		return null;
	}	
	
	
	/**
	 * Map the property using the xpath mapping supplied.
	 * 
	 * @param document
	 * @param parsedDoc
	 * @param isDublinCore
	 * @param xpath
	 * @param mapping
	 */
	private void performXPathMapping(Document document, ParsedDocument parsedDoc,
			boolean isDublinCore, XPath xpath, Mapping mapping) {
		try {
			
			NodeList nodes = (NodeList) xpath.evaluate(mapping.getQueryString(), document, XPathConstants.NODESET);

			for (int i = 0; i < nodes.getLength(); i++) {
				
				String value = extractValue(nodes.item(i));
				if(value!=null){

					if(mapping.isGuid){
						parsedDoc.setGuid(value);
					}
					//create the triples
					createTriples(parsedDoc, isDublinCore, mapping, value);
				}
			}
		} catch (XPathExpressionException e) {
			//throw new Exception("Failed to extract XPath property: "+ e.getMessage(), e);
			// To handle the XPATH expressions which don't represent a nodelist. 
			String value;
			try {
				value = trim(xpath.evaluate(mapping.getQueryString(), document));
				if (value != null || !"".equals(value)){
					value = value.replaceAll("[\\s&&[^ ]]{1,}", "");
					value = value.replaceAll("[ ]{2,}", " ");
					//create the triples
					createTriples(parsedDoc, isDublinCore, mapping, value);
				}
			} catch (XPathExpressionException e1) {
				logger.warn(e.getMessage(), e);
			}
		}
	}

	/**
	 * Create triples for this mapping, adding them to the triples or dublin core properties.
	 * 
	 * @param parsedDoc
	 * @param isDublinCore
	 * @param mapping
	 * @param value
	 */
	private void createTriples(ParsedDocument parsedDoc, boolean isDublinCore,
			Mapping mapping, String value) {
		
		value = StringUtils.trimToNull(value);
		
		if(value!=null){
			for (Predicates predicate : mapping.predicates) {
				if(isDublinCore){
					parsedDoc.getDublinCore().put(predicate.toString(), value);
				} else {
					Triple<String,String,String> triple = new Triple(mapping.subject,predicate.toString(), value);
					parsedDoc.getTriples().add(triple);
				}
			}
		}
	}

	/**
	 * Get the literal value for this predicate.
	 * 
	 * @param pds
	 * @param triplePredicateLocalPart
	 * @return
	 */
	public String getTripleObjectLiteral(List<ParsedDocument> pds,
			String triplePredicateLocalPart) {

		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();

		for (Triple<String,String,String> triple : triples) {
			if (triple.getPredicate().endsWith(triplePredicateLocalPart)) {
				return triple.getObject().toString();
			}
		}
		return null;
	}

	/**
	 * Retrieve a Node value and append to the supplied StringBuffer.
	 * 
	 * @param sb
	 * @param node
	 */
	private void getNodeValue(StringBuilder sb, Node node) {
		String value = node.getNodeValue();
		if (value != null && value.length() > 0) {
			sb.append(value);
		}

		// It seems Attribute Nodes - do this implicitly.
		if (node.getNodeType() != Node.ATTRIBUTE_NODE) {
			NodeList nodes = node.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				sb.append(" ");
				getNodeValue(sb, nodes.item(i));
			}
		}
	}

	/**
	 * If false only the value of the node selected by the XPath is extracted. If true, recursively the values of all children are added too.
	 */
	private boolean recursiveValueExtraction = false;

	/**
	 * @return the recursiveValueExtraction
	 */
	public boolean isRecursiveValueExtraction() {
		return recursiveValueExtraction;
	}

	/**
	 * Enables or disables recursive extraction of properties from a
	 * XPath node.
	 * <br />
	 *
	 * If enabled the value of the XPath node plus recursively the values of its
	 * children are extracted. If not enabled only the value of the selected node
	 * is extracted (as before). Disabled by default to be backwards compatible.
	 *
	 * @param recursiveValueExtraction the recursiveValueExtraction to set
	 */
	public void setRecursiveValueExtraction(boolean recursiveValueExtraction) {
		this.recursiveValueExtraction = recursiveValueExtraction;
	}

	/**
	 * To be overridden by subclasses, allow subclasses to add some functionality
	 * after the basic mapping has been completed.
	 * 
	 * @param pds
	 * @param xmlDocument
	 * @throws Exception
	 */
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {}
	
	/**
	 * Add a mapping for a dublin core property.
	 * 
	 * FIXME the subject isnt used downstream - should be removed
	 * 
	 * @param xpath
	 * @param targets
	 */
	protected void addDCMapping(String query, String subject, Predicates ... predicates) {
		dcMappingList.add(new Mapping(query, subject, predicates));
	}
	
	/**
	 * Add a mapping for a dublin core property.
	 * 
	 * @param xpath
	 * @param targets
	 */
	protected void addDCMapping(String query, MappingType mappingType, String subject, Predicates ... predicates) {
		dcMappingList.add(new Mapping(query, subject, predicates,mappingType));
	}
	
	/**
	 * Add a mapping for a triple.
	 * 
	 * @param xpath
	 * @param targets
	 */
	protected void addTripleMapping(String query, String subject, Predicates ... predicates) {
		tripleMappingList.add(new Mapping(query, subject, predicates));
	}	
	
	/**
	 * Add a mapping for a triple, supplying the mapping type (e.e. xpath or regex).
	 * 
	 * @param xpath
	 * @param targets
	 */
	protected void addTripleMapping(String query, MappingType mappingType, String subject, Predicates ... predicates) {
		tripleMappingList.add(new Mapping(query, subject, predicates, mappingType));
	}

	/**
	 * Extract the string value from a Node
	 * 
	 * @param node
	 * @return
	 */
	public String extractValue(org.w3c.dom.Node node){
		
		String value = null;
		
		if (isRecursiveValueExtraction()) {
			StringBuilder sb = new StringBuilder();
			getNodeValue(sb, node);
			value = trim(sb.toString());
		} else {
			value = trim(node.getNodeValue());
		}

		if (StringUtils.trimToNull(value) == null)
			return value;

		value = value.replaceAll("[\\s&&[^ ]]{1,}", "");
		value = value.replaceAll("[ ]{2,}", " ");
		
		return value;
	}
	
	/**
	 * Replaces all white space characters by a single space. Removes all leading and trailing white spaces.
	 * @param value The String to be trimmed.
	 * @return The trimmed String.
	 */
	private String trim(String value) {
		if (value != null) {
			String trimmed = value.replaceAll("[\\p{Z}\\s]+", " ");
			return trimmed.trim();
		}
		return null;
	}
	
	/**
	 * @param licencesMap the licencesMap to set
	 */
	public void setLicencesMap(Map<String, Licence> licencesMap) {
		this.licencesMap = licencesMap;
	}
}
