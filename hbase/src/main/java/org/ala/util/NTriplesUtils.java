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
package org.ala.util;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.ala.model.Triple;

/**
 * Utilities for reading NTriples. Written primarly to read
 * the dbpedia export.
 * 
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class NTriplesUtils {

	static final Pattern tabPattern = Pattern.compile("\t");
	
	/**
	 * Reads from the reader and adds triples to array list
	 * @param reader
	 * @return
	 * @throws Exception
	 */
	public static List<Triple> readNTriples(Reader reader, boolean stopOnSubjectChange) throws Exception {
		BufferedReader br =  new BufferedReader(reader);
		List<Triple> triples = new ArrayList<Triple>();
		String line = "";
		String currentSubject = null;
		while((line = br.readLine())!=null){
			br.mark(1000); //FIXME arbitrarily chosen
			String[] triple = tabPattern.split(line);
			if(triple.length>=3){
				if(currentSubject==null || triple[0].equals(currentSubject)){
					currentSubject = triple[0];
					triples.add(new Triple(triple[0], triple[1], triple[2]));
				} else if(stopOnSubjectChange) {
					br.reset();
					break;
				}
			}
		}
		return triples;
	}
}
