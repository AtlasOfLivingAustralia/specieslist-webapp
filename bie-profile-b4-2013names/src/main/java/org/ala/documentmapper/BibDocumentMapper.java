package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.w3c.dom.Document;

// Document mapper for Birds in Backyards (Australian Museum/BA) 
public class BibDocumentMapper extends XMLDocumentMapper {

	public BibDocumentMapper() {

		setRecursiveValueExtraction(true);

		/*
		 *  It's very likely that some of the pages may be badly structured. For example, the sample page we use doesn't have a <p> tag
		 *  for the habitat text whereas the other pages do. Therefore, sometimes the xPath expressions below may get wrong mappings. I 
		 *  haven't worked out a method to work around this issue as the <p> tag can be missing for every paragraph in the content div. 
		 */

		String subject = MappingUtils.getSubject();

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", 
				subject, Predicates.DC_IDENTIFIER);

		addDCMapping("//h2/text()", subject, Predicates.DC_TITLE);
		
//		addTripleMapping("//p[@class=\"featureimage\"]/text()", subject, Predicates.TMP);

		addTripleMapping("//div[@id=\"content\"]/p[contains(.,\"Scientific name:\")]/em/text()", 
				subject, Predicates.SCIENTIFIC_NAME);

		addTripleMapping("//div[@id=\"content\"]/p[contains(.,\"Family:\")]/text()", 
				subject, Predicates.FAMILY);

		addTripleMapping("//div[@id=\"content\"]/p[contains(.,\"Order:\")]/text()", 
				subject, Predicates.ORDER);

		addTripleMapping("//h5[contains(.,\"Distribution\")]/following-sibling::p[1]", 
				subject, Predicates.DISTRIBUTION_TEXT);

		addTripleMapping("//h5[contains(.,\"Habitat\")]/following-sibling::p[1]", 
				subject, Predicates.HABITAT_TEXT);

		addTripleMapping("//h5[contains(.,\"Similar species\")]/following-sibling::p[1]", 
				subject, Predicates.SIMILAR_SPECIES);

		addTripleMapping("//h5[contains(.,\"Breeding\")]/following-sibling::p[1]", 
				subject, Predicates.REPRODUCTION_TEXT);

		addTripleMapping("//h5[contains(.,\"Feeding\")]/following-sibling::p[1]", 
				subject, Predicates.DIET_TEXT);

		addTripleMapping("//p[@class=\"featureimage\"]/img/attribute::src", 
				subject, Predicates.IMAGE_URL);
	}

	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {

		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();

		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
		List<String> imageUrls = new ArrayList<String>();

		String subject = triples.get(0).getSubject();
		
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "http://birdsinbackyards.net/about/legal.cfm");
		
		String source = "http://www.birdsinbackyards.net";
		
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasFamily")) {
				String currentObj = ((String)triple.getObject()).trim();
				String newObj = null;

				if(currentObj.contains("Family:")) {
					String[] tmp = currentObj.split(":");
					if (tmp.length > 1) {
						newObj = tmp[1].trim();
					}
					triple.setObject(newObj);
				} else {
					tmpTriple.add(triple);
				}

			} else if(predicate.endsWith("hasOrder")) {
				String currentObj = ((String)triple.getObject()).trim();
				String newObj = null;

				if(currentObj.contains("Order:")) {
					String[] tmp = currentObj.split(":");
					if (tmp.length > 1) {
						newObj = tmp[1].trim();
						triple.setObject(newObj);
					} else {
						tmpTriple.add(triple);
					}
				} else {
					tmpTriple.add(triple);
				}

			} else if(predicate.endsWith("hasImageUrl")) {
				
							
				String imageUrl = (String) triple.getObject();
//				imageUrl = source + imageUrl;
				imageUrls.add(imageUrl);

				//if image URL contains "map"
				
				//retrieve the image and create new parsed document
				//				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
				//				if(imageDoc!=null){
				//					pds.add(imageDoc);
				//				}

				tmpTriple.add(triple);
			} 
//			else if(predicate.endsWith("tmp")) {
//				String currentObj = ((String)triple.getObject()).trim();
//				
//				if (currentObj.toLowerCase().contains("photo")) {
//				
//					String creator = currentObj.replaceAll("Photo:", "");
//					String right = null;
//					
//					if (creator.contains("©")) {
//					    right = creator.split("©")[1];
//					    creator = creator.split("©")[0];
//					}
//					
//					if (right != null) {
//					    pd.getDublinCore().put(Predicates.DC_RIGHTS.toString(), creator.trim());
//					}
//					
//					pd.getDublinCore().put(Predicates.DC_CREATOR.toString(), creator.trim());
//				}
//				
//				tmpTriple.add(triple);
//			}
		}

		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));

		//remove the triple from the triples
		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}

		//replace the list of triples
		pd.setTriples(triples);

		for (String imageUrl : imageUrls) {
		    
		    String tmp = getXPathSingleValue(xmlDocument, "//p[@class=\"featureimage\"]/img[@src=\"" + imageUrl + "\"]/following-sibling::text()[2]");
		    System.out.println(tmp);
		    
		    String right = null;
		    String creator = null;
		    
		    if (tmp != null && tmp.contains("©")) {
		        if (!tmp.trim().endsWith("©")) {
		            right = tmp.split("©")[1];
		        }
		        creator = tmp.split("©")[0].replaceAll("Photo:", "").trim();
		    }
		    System.out.println(right);
		    System.out.println(creator);
		    imageUrl = source + imageUrl;
		    
			ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
			if(imageDoc!=null){
			    
				if(imageUrl.contains("/maps/")){
					Triple triple = imageDoc.getTriples().get(0);
					imageDoc.getTriples().add(new Triple(triple.getSubject(), Predicates.DIST_MAP_IMG_URL.toString(), imageUrl));
				}
				
				if (creator != null && !"".equals(creator)) {
				    imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), creator.trim());
				}
				
				if (right != null && !"".equals(right)) {
				    if (imageUrl.contains("405132_brownsonglark.jpg")) {
				        System.out.println(imageDoc.getGuid() + "::" + right);
				    }
				    if (!right.contains("Australian Museum") || creator.contains("Richard Major") || creator.contains("Greg Gowing")) {
				        imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), right.trim());
				    } else {
				        continue;
				    }
				}
//				if (imageUrl.contains("405132_brownsonglark.jpg")) {
//                    System.out.println(imageDoc.getGuid());
//                }
				
				pds.add(imageDoc);
			}
		}
	}
}
