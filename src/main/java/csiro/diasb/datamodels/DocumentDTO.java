package csiro.diasb.datamodels;

import java.util.LinkedHashMap;
import java.util.Map;

public class DocumentDTO {

	/** This is the name of the infosource e.g. PaDIL */
	private String infoSourceName;
	/** This is the url to the infosource e.g. http://www.padil.org.au */
	private String infoSourceUrl;
	/** This is the url to the document/htmlpage/image that supplied the information */
	private String sourceUrl;
	/** This is the title of the document/htmlpage/image that supplied the information */
	private String sourceTitle;
	
	private Map<String, Object> propertyMap = new LinkedHashMap<String,Object>();
	
	public String getInfoSourceName() {
		return infoSourceName;
	}
	public void setInfoSourceName(String infoSourceName) {
		this.infoSourceName = infoSourceName;
	}
	public Map<String, Object> getPropertyMap() {
		return propertyMap;
	}
	public void setPropertyMap(Map<String, Object> propertyMap) {
		this.propertyMap = propertyMap;
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
	public String getSourceTitle() {
		return sourceTitle;
	}
	public void setSourceTitle(String sourceTitle) {
		this.sourceTitle = sourceTitle;
	}
}
