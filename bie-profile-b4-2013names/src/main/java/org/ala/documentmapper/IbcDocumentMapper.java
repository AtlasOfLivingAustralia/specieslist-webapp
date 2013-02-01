package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

public class IbcDocumentMapper extends XMLDocumentMapper {

	public IbcDocumentMapper() {
		
		String subject = MappingUtils.getSubject();

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", subject, Predicates.DC_IDENTIFIER);

		addDCMapping("//title/text()", subject, Predicates.DC_TITLE);

		addTripleMapping("//div[@class=\"status-and-map-species\"]/ul/li/text()", 
				subject, Predicates.CONSERVATION_STATUS);

		addTripleMapping("//div[@id=\"species\"]/div[1]/div[1]/h4[1]/a/text()", 
				subject, Predicates.FAMILY);

		addTripleMapping("//div[@id=\"species\"]/div[1]/div[1]/h4[1]/a/text()", 
				subject, Predicates.FAMILY_COMMON_NAME);
		
		addTripleMapping("//div[@id=\"species\"]/div[@class=\"intro\"]/div[@class=\"info\"]/h1/i/text()", 
				subject, Predicates.SCIENTIFIC_NAME);

		/*
		 * It is hard to get just the English common name as the French, German and Spanish versions are in the same node
		 * as the English one. 
		 */

//		addTripleMapping("//div[@id=\"species\"]/div[1]/div[1]/h1[1]/text()|//p[@class=\"other-languages\"]/text()", 
//				subject, Predicates.COMMON_NAME);

		addTripleMapping("//div[@class=\"view-content view-content-pictures-in-species\"]/ul[1]/li/a[1]/img[1]/attribute::src", 
				subject, Predicates.IMAGE_PAGE_URL);

		addTripleMapping("//div[@class=\"view-content view-content-videos-in-species\"]/ul[1]/li/a[1]/attribute::href", 
				subject, Predicates.VIDEO_PAGE_URL);
	}
	
	@Override
    public List<ParsedDocument> map(String uri, byte[] content)
    throws Exception {

        String documentStr = new String(content);

        documentStr = documentStr.replaceAll("\\]\\]>", "");
        documentStr = documentStr.replaceAll("<!\\[CDATA\\[", "");
        //      documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</i>[\\s&&[^ ]]{0,}", "");
        //      documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<em>[\\s&&[^ ]]{0,}", "");
        //      documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</em>[\\s&&[^ ]]{0,}", "");

        //      System.out.println(documentStr);

        content = documentStr.getBytes();

        return super.map(uri, content);
    }

	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@SuppressWarnings("unchecked")
    @Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {

		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
//		Map<String, String> dublinCore = pd.getDublinCore();
//		
//		String pageTitle = dublinCore.get(Predicates.DC_TITLE.toString());
//		//extract the scientific name
//		
//		Pattern p = Pattern.compile("*\\(("+)
//		
//		

		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
		List<String> commonNameList = new ArrayList<String>();
		
		String subject = triples.get(0).getSubject();

//		pd.getDublinCore().put(Predicates.DC_CREATOR.toString(), " Internet Bird Collection");
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "http://ibc.lynxeds.com/content/about-us");		
		
		String source = "http://ibc.lynxeds.com";

		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasCommonName")) {
				String currentCommonName = ((String)triple.getObject()).trim();
				String formattedCommonName = cleanBrackets(currentCommonName);

				if (!formattedCommonName.equals("")){
					triple.setObject(formattedCommonName);
				} else {
					tmpTriple.add(triple);
				}
				
				if (formattedCommonName.contains(",")) {
				    String[] tmpCommonNames = formattedCommonName.split(",");
				    tmpTriple.add(triple);
				    
				    for (String commonName : tmpCommonNames) {
//				        triples.add(new Triple(subject, Predicates.COMMON_NAME.toString(), commonName.trim()));
				        commonNameList.add(commonName.trim());
				    }
				}

			} else if(predicate.endsWith("title")) {
				String currentObj = ((String)triple.getObject()).trim();
				String newObj = currentObj.split("\\|")[0];
				if(! "".equals(newObj)) {
					triple.setObject(newObj);
				} else {
					tmpTriple.add(triple);
				}

			} else if(predicate.endsWith("hasFamilyCommonName")) {
				String currentObj = ((String)triple.getObject()).trim();
				String newObj = currentObj.split(" ")[0];
				newObj = cleanBrackets(newObj);
				if(! "".equals(newObj)) {
					triple.setObject(newObj);
				} else {
					tmpTriple.add(triple);
				}
			} else if(predicate.endsWith("hasFamily")) {
				String currentObj = ((String)triple.getObject()).trim();
				String newObj = currentObj.split(" ")[1];
				if(! "".equals(newObj)) {
					triple.setObject(cleanBrackets(newObj));
				} else {
					tmpTriple.add(triple);
				}
			} else if(predicate.endsWith("hasImagePageUrl")) {
				String imageUrl = (String) triple.getObject();
				//imageUrl = source + imageUrl;
				
				// Get the link for large images
				imageUrl = imageUrl.replaceAll("thumb", "node");
				triple.setObject(imageUrl);
				
				//retrieve the image and create new parsed document
				//Cannot do this as the image links contain special characters which is not accepted by apache.commons.httpclient
				try {
					ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
					if(imageDoc!=null){
//						pds.add(imageDoc);
					}
					tmpTriple.add(triple);
				} catch (Exception e){
					logger.error("Problem downloading image from: "+imageUrl+ ",  "+ e.getMessage(), e);
				}
			} else if(predicate.endsWith("hasVideoPageUrl")) {
				String videoUrl = (String) triple.getObject();
				
				String imageUrl = getXPathSingleValue(xmlDocument, "//a[@href=\"" + videoUrl + "\"]/img/@src");
				String creator = getXPathSingleValue(xmlDocument, "//a[@href=\"" + videoUrl + "\"]/following-sibling::span[@class=\"user\"]//text()");
	            System.out.println(imageUrl);
	            
	            String right = creator;
	            
	            imageUrl = imageUrl.replaceAll("48x36", "116x93");
	            
	            videoUrl = source + videoUrl;
                triple.setObject(videoUrl);

	            ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
	            if(imageDoc!=null){
	                imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), creator.trim());

	                imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), right.trim());
	                
	                imageDoc.setScreenShot(imageUrl);
	                
	                imageDoc.getDublinCore().put(Predicates.DC_IDENTIFIER.toString(), videoUrl);
	                
	                imageDoc.getTriples().add(new Triple(subject, Predicates.VIDEO_PAGE_URL.toString(), videoUrl));
	                pds.add(imageDoc);
	            }
				
				
				
			}
		}

		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
		
		if (commonNameList.size() > 0) {
		    for (String commonName : commonNameList) {
		        triples.add(new Triple(subject, Predicates.COMMON_NAME.toString(), commonName));
		    }
		}
		
		//remove the triple from the triples
		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}

		//replace the list of triples
		pd.setTriples(triples);
	}

	// Clean the ( and ) symbols in a String
	private String cleanBrackets(String result) {
		if(StringUtils.isNotEmpty(result)){
			result = result.replaceAll("\\(", "");
			result = result.replaceAll("\\)", "");
			result = result.replaceAll(";", "");
			result.trim();
		}
		return result;
	}
}
