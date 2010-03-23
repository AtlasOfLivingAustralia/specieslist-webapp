/**
 * Copyright (c) CSIRO Australia, 2009
 * All rights reserved.
 *
 * Original Author: Tommy Wang
 * Last Modified By: $LastChangedBy$
 * Last Modified Info: $Id$
 */

package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.repository.Namespaces;
import org.w3c.dom.Document;

/**
 * Document Mapper for Australian Insect Common Names (AICN).
 *
 * @author Tommy Wang
 */
public class AicnDocumentMapper extends XMLDocumentMapper {

	private static final org.apache.log4j.Logger classLogger =
			org.apache.log4j.Logger.getLogger(AicnDocumentMapper.class);

	public AicnDocumentMapper() {
		
		String subject = MappingUtils.getSubject();

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", subject, Predicates.DC_IDENTIFIER);
		addDCMapping("//title/text()",subject, Predicates.DC_TITLE);
		addTripleMapping("//html/body/table/tbody[1]/tr[1]/td[2]/div[2]/table[1]/tbody[1]/tr[4]/td[2]/font/text()", subject, Predicates.COMMON_NAME);

/*
		addDefaultSubjectMapping("//html/body/table/tbody[1]/tbody[1]/tr[1]/td[2]/div[2]/table[1]/tbody[1]/tbody[1]/tr[3]/td[1]/img/attribute::src", 
				FedoraConstants.AICN_NAMESPACE, "hasImageUrl", false);
*/
		addTripleMapping("//html/body/table/tbody[1]/tr[1]/td[2]/div[2]/table[1]/tbody[1]/tr[3]/td[1]/img/attribute::src",
				subject, Predicates.IMAGE_URL);
//				Namespaces.ALA, "tempImageUrl", false);

		addTripleMapping("//html/body/table/tbody[1]/tr[1]/td[2]/div[2]/table[1]/tbody[1]/tr[9]/td[2]/b/font/text()", 
				subject, Predicates.PEST_STATUS);
		
		addTripleMapping("//html/body/table/tbody[1]/tr[1]/td[2]/div[2]/table[1]/tbody[1]/tr[10]/td[2]/a/font/text()", 
				subject, Predicates.PHYLUM);
		
		addTripleMapping("//html/body/table/tbody[1]/tr[1]/td[2]/div[2]/table[1]/tbody[1]/tr[11]/td[2]/a/font/text()", 
				subject, Predicates.CLASS);
		
		addTripleMapping("//html/body/table/tbody[1]/tr[1]/td[2]/div[2]/table[1]/tbody[1]/tr[12]/td[2]/a/font/text()", 
				subject, Predicates.ORDER);
		
		addTripleMapping("//html/body/table/tbody[1]/tr[1]/td[2]/div[2]/table[1]/tbody[1]/tr[13]/td[3]/a/font/text()", 
				subject, Predicates.FAMILY);

		addTripleMapping("//i[1]/text()",
				subject, Predicates.SCIENTIFIC_NAME);
//				Namespaces.ALA, "tempBinomialName", false);

		addTripleMapping("//i[2]/text()",
				subject, Predicates.INFRA_SPECIFIC_EPITHET);
//				Namespaces.ALA, "tempInfraSpecificEpithet", false);

		// //html/body/table/tbody[1]/tr[1]/td[2]/div[2]/table[1]/tbody[1]/tbody[1]/tr[1]/td[2]/font/a/i/text()
		// Name. 1 item for bionomial.  2 items for trinomial.  Get marker from next.

		// //html/body/table/tbody[1]/tr[1]/td[2]/div[2]/table[1]/tbody[1]/tbody[1]/tr[1]/td[2]/font/a/text()
		// 1 item = author. 2 items = 1st item, marker, 2nd item, author

	} // End of constructor.
	
	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {

		//extract the pest status
		extractConservationStatus(pds);
		
		reformatImageUrl(pds);

		generateTitle(pds);
		
		// Generates science name.
		generateScientificName(pds);

		// Generates name parts.
		// generateNamePartsFromSciName();

		//dealWithImages(pds);

	} // End of `postProcessProperties` method.

	private void extractConservationStatus(List<ParsedDocument> pds) {
		String subject = MappingUtils.getSubject();
		
		String htmlContent = new String(pds.get(0).getContent());
		
//		Pattern nativePattern = Pattern.compile(">[ \n\t\\s]*Native[ \n\t\\s]*<");
//		Pattern exoticPattern = Pattern.compile("<font size=2 face=\"Arial\">Exotic</font>");
//		Pattern bioAgentPattern = Pattern.compile("<font size=2 face=\"Arial\">Biological Control Agent</font>");
		
		if(htmlContent.contains("Native")){
			pds.get(0).getTriples().add(new Triple(subject, Predicates.PEST_STATUS.toString(), "Native"));
		} else if(htmlContent.contains("Exotic")){
			pds.get(0).getTriples().add(new Triple(subject, Predicates.PEST_STATUS.toString(), "Exotic"));
		} else if(htmlContent.contains("Biological Control Agent")){
			pds.get(0).getTriples().add(new Triple(subject, Predicates.PEST_STATUS.toString(), "Biological Control Agent"));
		}
	}

	/**
	 * Reformats and creates the <code>dc:title</code> property.
	 */
	private void generateTitle(List<ParsedDocument> pds) {

		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("title")) {
				String title = (String) triple.getObject();
				
				Pattern removeNumberingPrefixRegex = Pattern.compile("\\d\\.\\s");
				Matcher removeNumberingMatcher =
						removeNumberingPrefixRegex.matcher(title);
				boolean foundNumberingPrefix = removeNumberingMatcher.find();
				
				if (foundNumberingPrefix) {
					String[] titleParts = removeNumberingPrefixRegex.split(title);
					
					triple.setObject(titleParts[1].trim());
					break;
				}
			}
		}
	
	} // End of `generateTitle` method.


	/**
	 * Extracts from the title property, the scientific name.
	 * <br />
	 *
	 * The expected format is (including space):
	 * <ul>
	 * <li><code>genus species author</code></li>
	 * <li><code>genus species (author)</code></li>
	 * <li><code>genus species subspecies author</code></li>
	 * <li><code>genus species subspecies (author)</code></lis>
	 * </ul>
	 *
	 * <br />
	 * Extracts and possibly create name parts properties from scientific
	 * name.  The name parts are from {@link http://rs.tdwg.org/ontology/voc/TaxonName TDWG's Taxon Name}.
	 * Specifically, the
	 * {@link http://rs.tdwg.org/ontology/voc/TaxonName#genusPart genusPart} and
	 * {@link http://rs.tdwg.org/ontology/voc/TaxonName#specificEpithet specificEpithet} for
	 * bionomial (2 parts) names.  In addition,
	 * {@link http://rs.tdwg.org/ontology/voc/TaxonName#infraspecificEpithet infraspecificEpithet}
	 * name part for trinomial (3 parts) names.
	 * <br />
	 * 
	 */
	private void generateScientificName(List<ParsedDocument> pds) {
        
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
        String currentTitle = getTripleObjectLiteral(pds, "title");
        Triple<String,String,String> tmpTriple = null;
        
		if (currentTitle == null) {
			AicnDocumentMapper.classLogger.warn(
					"Cannot parse `title` property for scientific name extraction " +
					" for data from " +
					"`" + getCurrentUrl(pds) + "`");
			return;
		}

		boolean isTrinomialName = false;

		String tempBinomialName = getTripleObjectLiteral(pds,Predicates.SCIENTIFIC_NAME.getLocalPart());
		

		if (tempBinomialName == null) {
			AicnDocumentMapper.classLogger.warn(
					"`tempBinomialName` property for scientific name extraction " +
					" has null reference for data from " +
                    "`" + getCurrentUrl(pds) + "`");
			return;
		}

		// Simple binomial name.
		String subject = triples.get(0).getSubject();
		
		triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), tempBinomialName));
//		triples.add(new Triple(subject, new QName(Namespaces.TWDG_TAXONNAME, "nameComplete").toString(), tempBinomialName));
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
		

		// Splits binomial name into genus and species parts based on space
		// character.
		String[] binomialParts = tempBinomialName.split("\\s+");

		if (binomialParts.length < 2) {
			AicnDocumentMapper.classLogger.warn(
					"Bionomial name cannot be splitted " +
					"for data from " +
					"`" + getCurrentUrl(pds) + "`");
			return;
		}

		if (binomialParts[0] == null) {
			AicnDocumentMapper.classLogger.warn(
					"`genusPart` property for scientific name" +
					" has null reference for data from " +
					"`" + getCurrentUrl(pds) + "`");
		} else {
			
			triples.add(new Triple(subject, Predicates.GENUS.toString(), binomialParts[0].trim()));
		}

		if (binomialParts[1] == null) {
			AicnDocumentMapper.classLogger.warn(
					"`species` property for scientific name" +
					" has null reference for data from " +
					"`" + getCurrentUrl(pds) + "`");
		} else {
			triples.add(new Triple(subject, Predicates.SPECIFIC_EPITHET.toString(), binomialParts[1].trim()));
		}

		String tempInfraSpecificEpithet = getTripleObjectLiteral(pds, Predicates.INFRA_SPECIFIC_EPITHET.getLocalPart());
        
		for (Triple triple : triples) {
			if (((String)triple.getPredicate()).endsWith(Predicates.INFRA_SPECIFIC_EPITHET.getLocalPart())) {
				tmpTriple = triple;
			}
		}
		triples.remove(tmpTriple);
		
		if (tempInfraSpecificEpithet == null) {
			isTrinomialName = false;
			return;
		}

		AicnDocumentMapper.classLogger.warn(
				"Possible trinomial name found in data from " +
				"`" + getCurrentUrl(pds) + "`");

		// Make sure the infraspecific epithet is one word, else
		// it is some other text that is not infraspecific epithet
		String[] infraspecificEpithetParts = tempInfraSpecificEpithet.split("\\s+");

		if (infraspecificEpithetParts.length != 0) {
			isTrinomialName = true;
		} else {
			isTrinomialName = false;
		}

		// Trinomial name
		if (isTrinomialName) {
			// Assumes dc:title is in the format of
			// <genus> <species> <marker> <infraspecifc epithet> <author>
			// We want the <marker>
			// Split via <infraspecific epithet>, then again by
			// <genus> <species> combo.
			// Then remove leading and trailing spaces.

			String[] contentAfterMarker = currentTitle.split(
					tempBinomialName);

/*
			int index = 0;
			System.out.println("<< After splitting with bionimal name");
			while (index < contentAfterMarker.length) {
				System.out.println("<<" + contentAfterMarker[index]);
				index++;
			}
*/

			if (contentAfterMarker.length < 2) {
				AicnDocumentMapper.classLogger.warn(
						"dc:title failed to be splitted using infraspecifc epithet " +
						"for data from " +
						"`" + getCurrentUrl(pds) + "`");
				return;
			}

			tempInfraSpecificEpithet = tempInfraSpecificEpithet.trim();
			String[] rankParts = contentAfterMarker[1].split(tempInfraSpecificEpithet);

/*
			index = 0;
			System.out.println("<< After splitting with infraspecific epithet");
			while (index < rankParts.length) {
				System.out.println(">>" + rankParts[index]);
				index++;
			}
*/

			if (rankParts.length < 2) {
				AicnDocumentMapper.classLogger.warn(
						"1st half of dc:title failed to be splitted using bionimal name" +
						"for data from " +
						"`" + getCurrentUrl(pds) + "`");
				return;
			}
			
			for (Triple triple : triples) {
				if (((String)triple.getPredicate()).endsWith("hasScientificName") || ((String)triple.getPredicate()).endsWith("nameComplete")) {
					tmpTriple = triple;
				}
			}
			triples.remove(tmpTriple);
			
			String sciName = tempBinomialName + " " + rankParts[0].trim() + " " +
					tempInfraSpecificEpithet;
			
			triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), sciName));
//			triples.add(new Triple(subject, new QName(Namespaces.ALA, "nameComplete").toString(), sciName));
			triples.add(new Triple(subject, Predicates.INFRA_SPECIFIC_EPITHET.toString(), tempInfraSpecificEpithet));
			
			


		} // End of processing trinomial name.

	} // End of `generateScientificName` method.
	
	/**
	 * Extracts and possibly create name parts properties from scientific
	 * name.  The name parts are from {@link http://rs.tdwg.org/ontology/voc/TaxonName TDWG's Taxon Name}.
	 * Specifically, the
	 * {@link http://rs.tdwg.org/ontology/voc/TaxonName#genusPart genusPart} and 
	 * {@link http://rs.tdwg.org/ontology/voc/TaxonName#specificEpithet specificEpithet} for
	 * bionomial (2 parts) names.  In addition,
	 * {@link http://rs.tdwg.org/ontology/voc/TaxonName#infraspecificEpithet infraspecificEpithet}
	 * name part for trinomial (3 parts) names.
	 * <br />
	 *
	 * Assumes the {@link #extractScientificNameFromTitle() } method has
	 * been called previously
	 */
	private void generateNamePartsFromSciName(List<ParsedDocument> pds) {
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		final String sciName = getTripleObjectLiteral(pds, "nameComplete");
		String subject = triples.get(0).getSubject();
		
		if (sciName == null) {
			AicnDocumentMapper.classLogger.warn(
					"Cannot parse `nameComplete` property for name parts extraction " +
					" for data from " +
					"`" + getCurrentUrl(pds) + "`");
			return;
		}

		// `sciName` should either be in bionomial form (2 parts) or
		// trinomial form (3 parts).  Determine this and insert appropriate
		// TDWG properties: `genusPart` `specificEpithetPart` and
		// assume `infraspecificEpithet` (rank below species).
		// See
		// http://rs.tdwg.org/ontology/voc/TaxonName
		String[] sciNameParts = sciName.split("\\s+");

		// Prepeare a hash set structure to add new properties.
		
		if (sciNameParts.length == 2) {
			// Bionomial name.
			String genusPart = sciNameParts[0];
			String specificEpithet = sciNameParts[1];

			// Make sure these things are not null before submission.
			// If they are null, ignore.
			if ( (genusPart != null) && (specificEpithet != null)) {
				// Prepeare the TDWG's `genusPart` property.
				triples.add(new Triple(subject, Predicates.GENUS.toString(), genusPart));
				triples.add(new Triple(subject, Predicates.SPECIFIC_EPITHET.toString(), specificEpithet));

/*
				toAdd.add(new RDFProperty(
						FedoraConstants.TWDG_TAXONNAME_NAMESPACE,
						"genusPart", genusPart, true));

				// Prepeare the TDWG's `specificEpithet` property.
				toAdd.add(new RDFProperty(
						FedoraConstants.TWDG_TAXONNAME_NAMESPACE,
						"specificEpithet", specificEpithet, true));
*/
			} else {
				AicnDocumentMapper.classLogger.warn(
						"A part of the binomial name has null reference or is empty " +
						" for data from URL " +
						"`" + getCurrentUrl(pds) + "`");
			}

		} else if (sciNameParts.length == 3) {
			// Trinomial name.
			String genusPart = sciNameParts[0];
			String specificEpithet = sciNameParts[1];
			String infraspecificEpithet = sciNameParts[2];

			// Make sure these things are not null before submission.
			// If they are null, ignore.

			if ( (genusPart != null) && (specificEpithet != null) && (infraspecificEpithet != null)) {
				// Prepeare the TDWG's `genusPart` property.
/*
				toAdd.add(new RDFProperty(
						FedoraConstants.TWDG_TAXONNAME_NAMESPACE,
						"genusPart", genusPart, true));

				// Prepeare the TDWG's `specificEpithet` property.
				toAdd.add(new RDFProperty(
						FedoraConstants.TWDG_TAXONNAME_NAMESPACE,
						"specificEpithet", specificEpithet, true));

				// Prepeare the TDWG's `infraspecificEpithet` property.
				toAdd.add(new RDFProperty(
						FedoraConstants.TWDG_TAXONNAME_NAMESPACE,
						"infraspecificEpithet", infraspecificEpithet, true));
*/
			} else {
				AicnDocumentMapper.classLogger.warn(
						"A part of the trinomial name has null reference or is empty " +
						" for data from URL " +
						"`" + getCurrentUrl(pds) + "`");
			}

		} // End of parsing for genus and species part of names.

		// Adds the prepeared hash set to property list.
/*
		if (!toAdd.isEmpty()) {
			SortedSet<RDFProperty> defProps =
					getProperties(RDFProperty.DEFAULT_SUBJECT);
			defProps.addAll(toAdd);
		}
*/
	} // End of `generateNamePartsFromSciName` method.

	/**
	 * If the current data source has images, then create new digital objects
	 * in content repository for them.
	 * <br />
	 *
	 * Requires {@link #generateScientificName() } method to be
	 * called.
	 *
	 */
	/*private void dealWithImages(List<ParsedDocument> pds) {
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		String imageUrl = getTripleObjectLiteral(pds, "hasImageUrl");
		String subject = triples.get(0).getSubject();

		if (imageUrl == null) {
			return;
		}

		AicnDocumentMapper.classLogger.debug(
				"Image found for data from " +
				"`" + getCurrentUrl(pds) + "`" +
				"with URL " +
				"`" + imageUrl + "`");

		String imageObjectId = addImageWithScientificName(imageUrl,
				"Australian Insect Common Names (AICN)",
				getTripleObjectLiteral(pds, "nameComplete"));

		if (imageObjectId == null) {
			AicnDocumentMapper.classLogger.warn(
					"Failed to create digital object for image " +
					"with URL " +
					"`" + imageUrl + "`" +
					" for data from " +
					"`" + getCurrentUrl(pds) + "`");
		}

	} */ // End of `dealWithImages` method.

	/**
	 * Reformats the `hasImageUrl` property from relative path to
	 * fully qualified path.
	 * @throws Exception 
	 */
	private void reformatImageUrl(List<ParsedDocument> pds) throws Exception {
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		String subject = triples.get(0).getSubject();
		final String aicnBaseUrl = "http://www.ento.csiro.au/aicn/";
		// Obtains the extracted image URL, if any.
		String imageRelativeUrl = getTripleObjectLiteral(pds, "hasImageUrl");
		//System.out.println(imageRelativeUrl);
		if (imageRelativeUrl == null) {
			return;
		}
		
		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();	
		
		for (Triple triple : triples) {
			if (((String)triple.getPredicate()).endsWith("hasImageUrl")) {
				tmpTriple.add(triple);
			}
		}
		//remove the triple from the triples
		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}

		// The relative URL is in the format of
		// ../images/file.jpeg
		// Remove the relative paths.
		String[] relativeUrlParts = imageRelativeUrl.split("/");

		String imageUrl = aicnBaseUrl +
				relativeUrlParts[1] + "/" + relativeUrlParts[2];
		
		//triples.add(new Triple(subject,Predicates.IMAGE_URL.toString(), imageUrl));
		
		//retrieve the image and create new parsed document
		ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
		if(imageDoc!=null){
			pds.add(imageDoc);
		}
		
		//triples.add(new Triple(subject, new QName(TripleConstants.ALA_NAMESPACE, "hasImageUrl").toString(), imageUrl));
		

	} // End of `reformatImageUrl` method.

    /**
     * Parses the property list to obtain the URL of the current document.
     * Useful for inserting this in debugging and log messages.
     *
     * @return URL of the current document being parsed.  <code>null</code>
     * if property does not exists.
     */
    private String getCurrentUrl(List<ParsedDocument> pds) {
        return getTripleObjectLiteral(pds, "hasURL");
        
    } // End of `getCurrentUrl` method.

	/**
	 * Lists out the current properties on the standard output.
	 * Usefule for debugging.
	 */
//	private void showProperties() {
//		
//		SortedSet<RDFProperty> props = this.getProperties(RDFProperty.DEFAULT_SUBJECT);
//		System.out.print(props);
//		Iterator<RDFProperty> iter = props.iterator();
//		while (iter.hasNext()) {
//			System.out.println(">>> " + iter.next().toString());
//		}
//		System.out.println(">>>\n");
//		
//	} // End of `showProperties` method.

} // End of class.
