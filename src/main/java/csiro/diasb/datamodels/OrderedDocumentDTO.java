package csiro.diasb.datamodels;

import java.util.Collection;

/**
 * An ordered document has its properties split and ordered
 * into categories.
 * 
 * @author Dave Martin
 */
public class OrderedDocumentDTO {

	/** This is the name of the infosource e.g. PaDIL */
	private String infoSourceName;
	/** This is the url to the infosource e.g. http://www.padil.org.au */
	private String infoSourceUrl;
	/** This is the url to the document/htmlpage/image that supplied the information */
	private String sourceUrl;
	/** This is the title of the document/htmlpage/image that supplied the information */
	private String sourceTitle;
	/** This is the organised properties that have been derived from the page */
	private Collection<CategorisedProperties> categorisedProperties;
	/**
	 * @return the infoSourceName
	 */
	public String getInfoSourceName() {
		return infoSourceName;
	}
	/**
	 * @param infoSourceName the infoSourceName to set
	 */
	public void setInfoSourceName(String infoSourceName) {
		this.infoSourceName = infoSourceName;
	}
	/**
	 * @return the infoSourceUrl
	 */
	public String getInfoSourceUrl() {
		return infoSourceUrl;
	}
	/**
	 * @param infoSourceUrl the infoSourceUrl to set
	 */
	public void setInfoSourceUrl(String infoSourceUrl) {
		this.infoSourceUrl = infoSourceUrl;
	}
	/**
	 * @return the sourceUrl
	 */
	public String getSourceUrl() {
		return sourceUrl;
	}
	/**
	 * @param sourceUrl the sourceUrl to set
	 */
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
	/**
	 * @return the sourceTitle
	 */
	public String getSourceTitle() {
		return sourceTitle;
	}
	/**
	 * @param sourceTitle the sourceTitle to set
	 */
	public void setSourceTitle(String sourceTitle) {
		this.sourceTitle = sourceTitle;
	}
	/**
	 * @return the categorisedProperties
	 */
	public Collection<CategorisedProperties> getCategorisedProperties() {
		return categorisedProperties;
	}
	/**
	 * @param categorisedProperties the categorisedProperties to set
	 */
	public void setCategorisedProperties(
			Collection<CategorisedProperties> categorisedProperties) {
		this.categorisedProperties = categorisedProperties;
	}
}
