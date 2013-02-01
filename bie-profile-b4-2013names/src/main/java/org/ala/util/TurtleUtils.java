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

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.ala.model.Triple;
import org.apache.log4j.Logger;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.turtle.TurtleParser;

/**
 * Utilities to read turtle files.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class TurtleUtils {

	public static final String defaultUriBase = "http://ala.org.au/ontology/ALA#";
	
	protected static Logger logger = Logger.getLogger(TurtleUtils.class);
	
	/**
	 * Read the triples from turtle. At this stage these triple *should* be flat in
	 * nature i.e. no nested triples.
	 * 
	 * The Triples returned are immutable.
	 * 
	 * @param reader
	 * @return
	 * @throws Exception
	 */
	public static List<Triple> readTurtle(Reader reader) throws Exception {
		
		final List<Triple> triples = new ArrayList<Triple>();
		final TurtleParser parser = new TurtleParser();
		 
		parser.setRDFHandler(new RDFHandlerBase(){
			
			@Override
			public void handleStatement(Statement st)
					throws RDFHandlerException {
				Triple triple = new Triple(st.getSubject().stringValue(), st.getPredicate().stringValue(), st.getObject().stringValue());
				if(logger.isTraceEnabled()){
					logger.trace(st.getSubject().stringValue()+ "\t"+  st.getPredicate().stringValue() + "\t"+  st.getObject().stringValue());
				}
				triples.add(triple);
			}
		});
		
		parser.setVerifyData(true);
		parser.parse(reader,defaultUriBase);
		return triples;
	}
	
	public static List<org.ala.repository.Triple<String,String,String>> readTurtle(Reader reader, boolean stopOnSubjectChange) throws Exception {

		final List<org.ala.repository.Triple<String,String,String>> triples = new ArrayList<org.ala.repository.Triple<String,String,String>>();

		 final TurtleParser parser = new TurtleParser();

		 parser.setRDFHandler(new RDFHandlerBase(){

			@Override
			public void handleStatement(Statement st)
					throws RDFHandlerException {
//				System.out.println(st.getSubject()+"\t"+st.getPredicate()+"\t"+st.getObject());
//				super.handleStatement(st);
				org.ala.repository.Triple<String,String,String> triple = new org.ala.repository.Triple<String,String,String>(st.getSubject().stringValue(), st.getPredicate().stringValue(), st.getObject().stringValue());
				triples.add(triple);
                //System.out.println(triple);
			}

		 });
		 parser.setVerifyData(true);
		 parser.parse(reader,defaultUriBase);
		 return triples;
	}
	

	
}
