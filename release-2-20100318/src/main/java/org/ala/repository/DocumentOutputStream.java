package org.ala.repository;

import java.io.OutputStream;

/**
 * A Document output stream is intended to be used by classes
 * which need to populate
 * 
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class DocumentOutputStream {

	protected int id;
	
	/** The infosource id for this document */
	protected int infoSourceId;
	
	protected OutputStream outputStream;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the infoSourceId
	 */
	public int getInfoSourceId() {
		return infoSourceId;
	}

	/**
	 * @param infoSourceId the infoSourceId to set
	 */
	public void setInfoSourceId(int infoSourceId) {
		this.infoSourceId = infoSourceId;
	}

	/**
	 * @return the outputStream
	 */
	public OutputStream getOutputStream() {
		return outputStream;
	}

	/**
	 * @param outputStream the outputStream to set
	 */
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
}
