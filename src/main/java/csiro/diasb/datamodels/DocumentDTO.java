package csiro.diasb.datamodels;

import java.util.LinkedHashMap;
import java.util.Map;
/**
 * A DTO for transferring details of a index document.
 * 
 * @author Dave Martin
 */
public class DocumentDTO {

	/** This is the name of the infosource e.g. PaDIL */
	private String infoSourceName;
	/** This is the url to the infosource e.g. http://www.padil.org.au */
	private String infoSourceUrl;
	/** This is the url to the document/htmlpage/image that supplied the information */
	private String sourceUrl;
	/** This is the title of the document/htmlpage/image that supplied the information */
	private String sourceTitle;
	/** The key/value map of properties */
	private Map<String, Object> propertyMap = new LinkedHashMap<String,Object>();
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
	 * @return the propertyMap
	 */
	public Map<String, Object> getPropertyMap() {
		return propertyMap;
	}
	/**
	 * @param propertyMap the propertyMap to set
	 */
	public void setPropertyMap(Map<String, Object> propertyMap) {
		this.propertyMap = propertyMap;
	}
}
