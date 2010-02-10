package org.ala.util;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.ala.model.Triple;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.turtle.TurtleParser;

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
				Triple triple = new Triple(st.getSubject().stringValue(), st.getPredicate().stringValue(), st.getObject().stringValue());
				triples.add(triple);
			}
		});
		parser.setVerifyData(true);
		parser.parse(reader,defaultUriBase);
		return triples;
	}
}
