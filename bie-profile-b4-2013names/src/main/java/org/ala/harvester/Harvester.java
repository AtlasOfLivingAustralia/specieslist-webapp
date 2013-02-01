package org.ala.harvester;

import java.util.Map;

import org.ala.documentmapper.DocumentMapper;
import org.ala.repository.Repository;
/**
 * A harvester implementation is a protocol specific implementation
 * that knows how to process a series of documents using a 
 * protocol and document format. 
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public interface Harvester {

	/**
	 * Start the harvest for this infosource
	 * 
	 * @param infosourceId
	 * @throws Exception
	 */
	public void start(int infosourceId) throws Exception;
	
	/**
	 * Start the harvest for this infosource
	 * 
	 * @param infosourceId, timeGap
	 * @throws Exception
	 */
	public void start(int infosourceId, int timeGap) throws Exception;
	
	/**
	 * Set the required connection parameters
	 * 
	 * @param connectionParams
	 */
	public void setConnectionParams(Map<String,String> connectionParams);
	
	/**
	 * Set the document mapper to use for the processed documents
	 * 
	 * @param documentMapper
	 */
	public void setDocumentMapper(DocumentMapper documentMapper);

	/**
	 * Set the repository to use to store the harvested
	 * and parsed documents.
	 * 
	 * @param repository
	 */
	public void setRepository(Repository repository);
}
