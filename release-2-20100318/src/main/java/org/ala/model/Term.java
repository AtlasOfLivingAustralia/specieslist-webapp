package org.ala.model;

/**
 * Model object for a Term. A vocabulary consists of multiple terms
 * and a fixed predicate.
 * 
 * @author Tommy Wang (tommy.wang@csiro.au)
 */
public class Term {
	
	protected int id;
	protected int vocabularyId;
	protected String termString;
	protected String predicate;
	
	/**
	 * @return id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return termString
	 */
	public String getTermString() {
		return termString;
	}
	/**
	 * @param termString
	 */
	public void setTermString(String termString) {
		this.termString = termString;
	}
	/**
	 * @return predicate
	 */
	public String getPredicate() {
		return predicate;
	}
	/**
	 * @param predicate
	 */
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	/**
	 * @return vocabularyId
	 */
	public int getVocabularyId() {
		return vocabularyId;
	}
	/**
	 * @param vocabularyId
	 */
	public void setVocabularyId(int vocabularyId) {
		this.vocabularyId = vocabularyId;
	}
}
