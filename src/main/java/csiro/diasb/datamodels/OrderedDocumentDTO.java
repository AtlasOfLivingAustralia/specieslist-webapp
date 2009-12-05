package csiro.diasb.datamodels;

import java.util.Collection;


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
	
	public String getInfoSourceName() {
		return infoSourceName;
	}
	public void setInfoSourceName(String infoSourceName) {
		this.infoSourceName = infoSourceName;
	}
	public String getInfoSourceUrl() {
		return infoSourceUrl;
	}
	public void setInfoSourceUrl(String infoSourceUrl) {
		this.infoSourceUrl = infoSourceUrl;
	}
	public String getSourceUrl() {
		return sourceUrl;
	}
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
	public Collection<CategorisedProperties> getCategorisedProperties() {
		return categorisedProperties;
	}
	public void setCategorisedProperties(
			Collection<CategorisedProperties> categorisedProperties) {
		this.categorisedProperties = categorisedProperties;
	}
	public String getSourceTitle() {
		return sourceTitle;
	}
	public void setSourceTitle(String sourceTitle) {
		this.sourceTitle = sourceTitle;
	}
}
