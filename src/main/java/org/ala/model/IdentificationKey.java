package org.ala.model;

public class IdentificationKey extends AttributableObject {
	private String id;
	private String title;
	private String url;
	private String description;
	private String publisher;
	private int publishedyear;
	private String taxonomicscope;
	private String geographicscope;
	private String keytype;
	private String accessibility;
	private String vocabulary;
	private String technicalskills;
	private String imagery;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public int getPublishedyear() {
		return publishedyear;
	}

	public void setPublishedyear(int publishedyear) {
		this.publishedyear = publishedyear;
	}

	public String getTaxonomicscope() {
		return taxonomicscope;
	}

	public void setTaxonomicscope(String taxonomicscope) {
		this.taxonomicscope = taxonomicscope;
	}

	public String getGeographicscope() {
		return geographicscope;
	}

	public void setGeographicscope(String geographicscope) {
		this.geographicscope = geographicscope;
	}

	public String getKeytype() {
		return keytype;
	}

	public void setKeytype(String keytype) {
		this.keytype = keytype;
	}

	public String getAccessibility() {
		return accessibility;
	}

	public void setAccessibility(String accessibility) {
		this.accessibility = accessibility;
	}

	public String getVocabulary() {
		return vocabulary;
	}

	public void setVocabulary(String vocabulary) {
		this.vocabulary = vocabulary;
	}

	public String getTechnicalskills() {
		return technicalskills;
	}

	public void setTechnicalskills(String technicalskills) {
		this.technicalskills = technicalskills;
	}

	public String getImagery() {
		return imagery;
	}

	public void setImagery(String imagery) {
		this.imagery = imagery;
	}
}
