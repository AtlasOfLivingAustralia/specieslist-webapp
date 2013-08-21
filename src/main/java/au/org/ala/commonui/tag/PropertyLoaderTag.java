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

import java.util.Properties;
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
	/** Whether or not to check the initParams from the web.xml */
	protected Boolean checkInit=false;
	/** Whether or not to check for tghe properties that were supplied in the fileProperties JNDI entry */
	protected Boolean checkSupplied=false; 
	
	/** Initialise properties that will be used when no bundle name is provided */	
	private static Properties props = new Properties();
	static{
	    //initialise properties based on 
	    //System.out.println("INITIALISING THE PRPERTIES FOR THE TAG lib");
        try{
            javax.naming.Context ctx = new javax.naming.InitialContext();
            String filename =(String)ctx.lookup("java:comp/env/configPropFile");            
            props.load(new java.io.FileInputStream(new java.io.File(filename)));
           // System.out.println("THE PROPS IN TAG: " + props);
        } catch(Exception e){
            //don't do anything obviously can't find the value
        }
	}
	
	/**
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		logger.debug("Writing tag " + bundle + " " + property + " " +checkInit);
		boolean found =false;
		
		if(!checkSupplied || props.size()<1){
    		try {
    			ResourceBundle rb = ResourceBundle.getBundle(bundle);			
    			pageContext.getOut().print(rb.getString(property));
    			found=true;
    		} catch (Exception e) {
    			//this could be expected behaviour for properties with a default
    			logger.warn(e.getMessage(), e);
    			
    		}
		} else{
		    //check to see if it is in the properties file that was supplied in the JNDI
		    String value = props.getProperty(property);
		    if(value != null){
		        try{
		        pageContext.getOut().print(value);
		        found =true;
		        } catch(Exception e){}
		    }
		}
		if(!found && checkInit){
		    
                //NC: 2013-08-13: now check to see if the property is provided in the web.xml initParams.
                String value = pageContext.getServletContext().getInitParameter(property);
                logger.info("Getting the init value " + property + " : " + value);
                if(value != null){
                    try{
                        pageContext.getOut().print(value);
                    } catch(Exception e2){
                        logger.warn(e2.getMessage(), e2);
                    }
                }
            
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


    /**
     * @param checkInitParams the checkInit to set
     */
    public void setCheckInit(Boolean checkInit) {
        this.checkInit = checkInit;
    }

    /**
     * @param checkSupplied the checkSupplied to set
     */
    public void setCheckSupplied(Boolean checkSupplied) {
        this.checkSupplied = checkSupplied;
    }
    
    
	
}