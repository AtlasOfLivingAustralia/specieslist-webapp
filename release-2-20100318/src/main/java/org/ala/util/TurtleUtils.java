/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 * 
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 * 
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/
package org.ala.util;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.ala.repository.Triple;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.turtle.TurtleParser;

/**
 * Helper class to handle turtle RDF files
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class TurtleUtils {

	public static final String defaultUriBase = "http://ala.org.au/ontology/ALA#";

	/**
	 * Read the triples from turtle. At this stage these triple *should* be flat in
	 * nature i.e. no nested triples.
	 *
	 * @param reader
	 * @param stopOnSubjectChange
	 * @return
	 * @throws Exception
	 */
	public static List<Triple> readTurtle(Reader reader, boolean stopOnSubjectChange) throws Exception {

		final List<Triple> triples = new ArrayList<Triple>();

		 final TurtleParser parser = new TurtleParser();

		 parser.setRDFHandler(new RDFHandlerBase(){

			@Override
			public void handleStatement(Statement st)
					throws RDFHandlerException {
//				System.out.println(st.getSubject()+"\t"+st.getPredicate()+"\t"+st.getObject());
//				super.handleStatement(st);
				Triple triple = new Triple(st.getSubject().stringValue(), st.getPredicate().stringValue(), st.getObject().stringValue());
				triples.add(triple);
                //System.out.println(triple);
			}

		 });
		 parser.setVerifyData(true);
		 parser.parse(reader,defaultUriBase);
		 return triples;
	}
}