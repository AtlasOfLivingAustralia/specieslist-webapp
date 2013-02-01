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

import java.util.TreeSet;

import org.ala.model.Rank;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.gbif.ecat.model.ParsedName;
import org.gbif.ecat.parser.NameParser;
import org.gbif.ecat.parser.UnparsableException;
/**
 * Reusable methods for lucene searching or index creation.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class LuceneUtils {
    public static final String SCI_NAME = "scientificName";
    public static final String SCI_NAME_RAW = "scientificNameRaw";
    public static final String SCI_NAME_TEXT = "scientificNameText";
    private final static Logger logger = Logger.getLogger(LuceneUtils.class);
	/**
	 * Adds a scientific name to the lucene index in multiple forms to increase
	 * chances of matches
	 * 
	 * @param doc
     * @param scientificName
     * @param taxonRank
	 */
	public static void addScientificNameToIndex(Document doc, String scientificName, String taxonRank){
		
		NameParser nameParser = new NameParser();
        Integer rankId = -1;
		
        if (taxonRank!=null) {
           Rank rank = Rank.getForField(taxonRank.toLowerCase());
           if(rank!=null){     
             rankId = rank.getId();
           } else {
             logger.warn("Unknown rank string: " + taxonRank);
           }
        }
		//remove the subgenus
		String normalized = "";
		boolean useNormalised = true;
		
		if(scientificName!=null){
			normalized = scientificName.replaceFirst("\\([A-Za-z]{1,}\\) ", "");
		}
		TreeSet<String> sciNames = new TreeSet<String>();
		try{
		ParsedName parsedName = nameParser.parse(normalized);
        // store scientific name values in a set before adding to Lucene so we don't get duplicates
        

    	if(parsedName!=null){
    		if(parsedName.isBinomial()){
    		    useNormalised = false;
    			//add multiple versions
                sciNames.add(parsedName.buildName(true, false, false,false, true, true, false, false, false, false).toLowerCase());
                sciNames.add(parsedName.buildName(true, false, true, false, true, true, false, false, false, false).toLowerCase());
    		}

            //add lowercased version
            sciNames.add(parsedName.canonicalName().toLowerCase());
    	}
		}
    	catch(UnparsableException e){
    	    
    	}
            // add to Lucene
            for (String sciName : sciNames) {
                doc.add(new Field(SCI_NAME, sciName, Store.YES, Index.NOT_ANALYZED_NO_NORMS));
            }

            Float boost = 1f;
            
            if(rankId!=null){
	            if (rankId == 6000) {
    	            // genus higher than species so it appears first
        	        boost = 3f;
            	} else if (rankId == 7000) {
                	// species higher than subspecies so it appears first
	                boost = 2f;
    	        }
    	    }

            Field f = new Field(SCI_NAME_TEXT, StringUtils.join(sciNames, " "), Store.YES, Index.ANALYZED);
            f.setBoost(boost);
            doc.add(f);
            //doc.add(new Field(SCI_NAME_TEXT, StringUtils.join(sciNames, " "), Store.YES, Index.ANALYZED_NO_NORMS));
            if(useNormalised){
    		//add lowercased version if name parser failed			    		
	    	doc.add(new Field(SCI_NAME, normalized.toLowerCase(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
            doc.add(new Field(SCI_NAME_TEXT, normalized.toLowerCase(), Store.YES, Index.ANALYZED));
            
            }
    	
    	if(scientificName!=null){
    		doc.add(new Field(SCI_NAME_RAW, scientificName, Store.YES, Index.NOT_ANALYZED_NO_NORMS));
    	}
	}
}
