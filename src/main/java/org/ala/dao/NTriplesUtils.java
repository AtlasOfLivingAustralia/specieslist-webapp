package org.ala.dao;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.ala.model.Triple;

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
