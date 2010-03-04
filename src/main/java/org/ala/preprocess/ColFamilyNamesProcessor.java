package org.ala.preprocess;

import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

import javax.inject.Inject;

import org.ala.dao.InfoSourceDAO;
import org.ala.documentmapper.MappingUtils;
import org.ala.model.InfoSource;
import org.ala.repository.DocumentOutputStream;
import org.ala.repository.Predicates;
import org.ala.repository.Repository;
import org.openrdf.model.BNode;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.com.bytecode.opencsv.CSVReader;
/**
 * This class converts a dump file of scientific family names
 * to family common names taken from Catalogue of Life 2009.
 *
 * This class creates a dublin core file and rdf turtle file
 * of this information so that it can be imported into the BIE profiles.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class ColFamilyNamesProcessor {

	@Inject
	protected Repository repository;
	
	@Inject
	protected InfoSourceDAO infoSourceDAO;
	
	public static void main (String[] args) throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				new String[]{"classpath*:spring-profiler.xml", "classpath:spring.xml"});
		ColFamilyNamesProcessor p = new ColFamilyNamesProcessor();
		p.setInfoSourceDAO((InfoSourceDAO) context.getBean("infoSourceDAO"));
		p.setRepository((Repository) context.getBean("repository"));
		p.process();
	}

	private void process() throws Exception {
		
		//preprocess the tab file into turtle format
		Reader r = new FileReader("/data/bie-staging/col/familyCommonNames.txt");
		
		InfoSource infoSource = infoSourceDAO.getByUri("http://www.catalogueoflife.org/");
		
		DocumentOutputStream dos = repository.getDocumentOutputStream(infoSource.getId(), 
				"http://www.catalogueoflife.org/familyCommonNames", "text/plain");
		
		//write the file to RAW file in the repository
		OutputStreamWriter w = new OutputStreamWriter(dos.getOutputStream());
		
		//read into buffer
		char[] buff = new char[1000];
		int read = 0;
		while((read = r.read(buff))>0){
			w.write(buff, 0, read);
		}
		w.flush();
		w.close();
		
		//reset the reader so it can be read again
		r = new FileReader("/data/bie-staging/col/familyCommonNames.txt");
		
		//write the triples out
		DocumentOutputStream rdfDos = repository.getRDFOutputStream(dos.getId());
		
		//read file, creating the turtle
		CSVReader csvr = new CSVReader(r, '\t');
		String[] fields = null;
		
		final RDFWriter rdfWriter = new TurtleWriter(new OutputStreamWriter(rdfDos.getOutputStream()));
		rdfWriter.startRDF();
		
		String subject = MappingUtils.getSubject();
		while((fields = csvr.readNext())!=null){
			if(fields.length==2){
				BNode bnode = new BNodeImpl(subject);
				rdfWriter.handleStatement(new StatementImpl(bnode, 
						new URIImpl(Predicates.SCIENTIFIC_NAME.toString()), 
						new LiteralImpl(fields[0])));
				
				rdfWriter.handleStatement(new StatementImpl(bnode, 
						new URIImpl(Predicates.FAMILY.toString()), 
						new LiteralImpl(fields[0])));
				
				rdfWriter.handleStatement(new StatementImpl(bnode, 
						new URIImpl(Predicates.COMMON_NAME.toString()), 
						new LiteralImpl(fields[1])));
			}
			subject = MappingUtils.getNextSubject(subject);
		}
		rdfWriter.endRDF();
		
		//tied up the output stream
		rdfDos.getOutputStream().flush();
		rdfDos.getOutputStream().close();
	}

	/**
	 * @param repository the repository to set
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/**
	 * @param infoSourceDAO the infoSourceDAO to set
	 */
	public void setInfoSourceDAO(InfoSourceDAO infoSourceDAO) {
		this.infoSourceDAO = infoSourceDAO;
	}
}
