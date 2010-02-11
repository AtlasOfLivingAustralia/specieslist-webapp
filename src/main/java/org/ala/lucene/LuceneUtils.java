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
package org.ala.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.gbif.ecat.model.ParsedName;
import org.gbif.ecat.parser.NameParser;
/**
 * Reusable methods for lucene searching or index creation.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class LuceneUtils {

	/**
	 * Adds a scientific name to the lucene index in multiple forms to increase
	 * chances of matches
	 * 
	 * @param doc
	 * @param scientificName
	 */
	public static void addScientificNameToIndex(Document doc, String scientificName){
		
		NameParser nameParser = new NameParser();
		
		//remove the subgenus
		String normalized = "";
		
		if(scientificName!=null){
			normalized = scientificName.replaceFirst("\\([A-Za-z]{1,}\\) ", "");
		}
		ParsedName parsedName = nameParser.parseIgnoreAuthors(normalized);
    	if(parsedName!=null){
    		if(parsedName.isBinomial()){
    			//add multiple versions
    			doc.add(new Field("scientificName", parsedName.buildAbbreviatedCanonicalName().toLowerCase(), Store.YES, Index.ANALYZED));
    			doc.add(new Field("scientificName", parsedName.buildAbbreviatedFullName().toLowerCase(), Store.YES, Index.ANALYZED));
    		}
    		//add lowercased version
    		doc.add(new Field("scientificName", parsedName.buildCanonicalName().toLowerCase(), Store.YES, Index.ANALYZED));
    	} else {
    		//add lowercased version if name parser failed			    		
	    	doc.add(new Field("scientificName", normalized.toLowerCase(), Store.YES, Index.ANALYZED));
    	}
    	
    	if(scientificName!=null){
    		doc.add(new Field("scientificNameRaw", scientificName, Store.YES, Index.NO));
    	}
	}
}
