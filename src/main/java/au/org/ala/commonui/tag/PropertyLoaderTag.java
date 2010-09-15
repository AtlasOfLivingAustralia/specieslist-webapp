/***************************************************************************
 * Copyright (C) 2005 Global Biodiversity Information Facility Secretariat.
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ***************************************************************************/
package au.org.ala.commonui.tag;

import java.util.ResourceBundle;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

/**
 * Simple tag that writes out a property from a bundle.
 * 
 * @author dmartin
 */
public class PropertyLoaderTag extends TagSupport {

	private static final long serialVersionUID = -6406031197753714478L;
	protected static Logger logger = Logger.getLogger(PropertyLoaderTag.class);
	
	/** The bundle to lookup */
	protected String bundle;
	/** The property to retrieve */
	protected String property;
	
	/**
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		
		try {
			ResourceBundle rb = ResourceBundle.getBundle(bundle);			
			pageContext.getOut().print(rb.getString(property));
		} catch (Exception e) {
			//this could be expected behaviour for properties with a default
			logger.warn(e.getMessage(), e);
		}
		return super.doStartTag();
	}

	/**
	 * @param bundle the bundle to set
	 */
	public void setBundle(String bundle) {
		this.bundle = bundle;
	}

	/**
	 * @param property the property to set
	 */
	public void setProperty(String property) {
		this.property = property;
	}
}