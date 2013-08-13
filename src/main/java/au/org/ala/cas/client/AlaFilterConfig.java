package au.org.ala.cas.client;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;

/**
 * A filterconfig wrapper that allows cas.properties to be provided as
 * configuration to jasig-cas filters.
 * 
 * @author Natasha Carter (natasha.carter@csiro.au)
 * 
 */
public class AlaFilterConfig implements FilterConfig {
    private final static Logger logger = Logger
            .getLogger(AlaFilterConfig.class);
    private FilterConfig embeddedFilterConfig;
    private ServletContext embeddedServletContext;
    private Properties casProperties;
    private java.util.List<String> whitelist;
    private Enumeration propertyEnumeration;
    private Enumeration filterEnumeration;

    public AlaFilterConfig(FilterConfig config) {
        this.embeddedFilterConfig = config;
        filterEnumeration = config.getInitParameterNames();
        // load the cas.properties file
        loadProperties();
        embeddedServletContext = new
        AlaServletContext(config.getServletContext(), casProperties);
        
        
        // try {
        // casProperties = new Properties();
        // InputStream inStream = Thread.currentThread()
        // .getContextClassLoader()
        // .getResourceAsStream("cas.properties");
        // //System.out.println("CAS INSTREAM:::: "+inStream.read() + " " +
        // inStream.toString());
        // casProperties.load(inStream);
        // if(casProperties.size()>0){
        // propertyEnumeration = casProperties.keys();
        // }
        // embeddedServletContext = new
        // AlaServletContext(config.getServletContext(), casProperties);
        //
        // } catch (Exception e) {
        // //Exception will be thrown if no configuration exists.
        // e.printStackTrace();
        // }
    }

    private void loadProperties() {
        /*
         * NC 2013-08-13 - We are changing the CAS configuration to be
         * configurable via the *-config.proeprties file. By convention we will
         * be using appname-config.properties files. These will be configurable
         * outside outside webapps provided the external directory is included
         * on the class path.
         * 
         * Sometimes the other properties that are in the properties file can prevent the 
         * authenticator from doing its job correctly. To counteract this we look for a  
         * casProperties value in the properties file to supply a comma separated list of supported
         * properties.
         */
        // attempt to locate the *-config.properties file
        java.net.URL[] urls = ((java.net.URLClassLoader) Thread.currentThread()
                .getContextClassLoader()).getURLs();
        for (java.net.URL url : urls) {
            //logger.debug("Handling " + url.getPath());
            // only want to load a single config file.
            if (casProperties == null) {
                logger.debug("Handling " + url.getPath());
                java.io.File f = new java.io.File(url.getPath());
                if (f.isDirectory()) {
                    // check to see if the *-config file exists.

                    String[] filenames = f.list(new java.io.FilenameFilter() {                        
                        public boolean accept(File dir, String name) {
                            return (name.endsWith("config.properties"));
                        }
                    });
                    logger.debug("Filenames " + filenames.length);
                    if (filenames.length > 0) {
                        // assume that the first one is the *-config.properties
                        // file to be used
                        logger.debug("Using config file " + filenames[0]);
                        InputStream in2 = Thread.currentThread()
                                .getContextClassLoader()
                                .getResourceAsStream(filenames[0]);
                        casProperties = new Properties();
                        try {
                            Properties p = new Properties();
                            p.load(in2);
                            //remove the properties that don't make up the cas configuration
                            if(p.containsKey("casProperties")){
                                whitelist = Arrays.asList(p.getProperty("casProperties").split(","));
                                for(String item: whitelist){
                                    Object value = p.getProperty(item);
                                    if(value != null){
                                        casProperties.put(item, p.getProperty(item));
                                    }
                                }
                            } else{
                                casProperties = p;
                            }
                        } catch (Exception e) {
                            logger.warn("Unable to load config properties in "
                                    + filenames[0], e);
                        }
                    }
                }
            }
        }
        if (casProperties == null) {
            casProperties = new Properties();
        }
        logger.info("The configProperties " + casProperties);
    }

    public String getFilterName() {
        return embeddedFilterConfig.getFilterName();
    }

    public ServletContext getServletContext() {
        return embeddedServletContext;
        // return embeddedFilterConfig.getServletContext();
    }

    public String getInitParameter(String name) {
        logger.debug("Extracting property from the ala filter " + name + " "
                + casProperties.getProperty(name));
        return casProperties.getProperty(name,
                embeddedFilterConfig.getInitParameter(name));
    }

    /**
     * This class acts as a Enumerator. It will enumerate over the
     * cas.properties first and then the embedded filter properties.
     */
    public Enumeration getInitParameterNames() {
        return new MultiSourceEnumeration(new Enumeration[] {
                casProperties.keys(),
                embeddedFilterConfig.getInitParameterNames() });

    }

    /**
     * An ala wrapper class to include properties file in the params.
     * 
     * @author car61w
     * 
     */
    private class AlaServletContext implements ServletContext {

        private ServletContext embeddedContext;
        private Properties casProperties;
        private Enumeration propertyEnumeration;
        private Enumeration contextEnumeration;

        AlaServletContext(ServletContext context, Properties properties) {
            this.embeddedContext = context;
            this.casProperties = properties;

        }

        public ServletContext getContext(String uripath) {
            return embeddedContext.getContext(uripath);
        }

        public int getMajorVersion() {
            return embeddedContext.getMajorVersion();
        }

        public int getMinorVersion() {
            return embeddedContext.getMinorVersion();
        }

        public String getMimeType(String file) {
            return embeddedContext.getMimeType(file);
        }

        public Set getResourcePaths(String path) {
            return embeddedContext.getResourcePaths(path);
        }

        public URL getResource(String path) throws MalformedURLException {
            return embeddedContext.getResource(path);
        }

        public InputStream getResourceAsStream(String path) {
            return embeddedContext.getResourceAsStream(path);
        }

        public RequestDispatcher getRequestDispatcher(String path) {
            return embeddedContext.getRequestDispatcher(path);
        }

        public RequestDispatcher getNamedDispatcher(String name) {
            return embeddedContext.getNamedDispatcher(name);
        }

        public Servlet getServlet(String name) throws ServletException {
            return embeddedContext.getServlet(name);
        }

        public Enumeration getServlets() {
            return embeddedContext.getServlets();
        }

        public Enumeration getServletNames() {
            return embeddedContext.getServletNames();
        }

        public void log(String msg) {
            embeddedContext.log(msg);
        }

        public void log(Exception exception, String msg) {
            embeddedContext.log(exception, msg);
        }

        public void log(String message, Throwable throwable) {
            embeddedContext.log(message, throwable);

        }

        public String getRealPath(String path) {
            return embeddedContext.getRealPath(path);
        }

        public String getServerInfo() {
            return embeddedContext.getServerInfo();
        }

        public String getInitParameter(String name) {
            logger.debug("Extracting property from the ala context " + name
                    + " " + casProperties.getProperty(name));
            return casProperties.getProperty(name,
                    embeddedContext.getInitParameter(name));
        }

        public Enumeration getInitParameterNames() {
            return new MultiSourceEnumeration(new Enumeration[] {
                    casProperties.keys(),
                    embeddedContext.getInitParameterNames() });
        }

        public Object getAttribute(String name) {
            return embeddedContext.getAttribute(name);
        }

        public Enumeration getAttributeNames() {
            return embeddedContext.getAttributeNames();
        }

        public void setAttribute(String name, Object object) {
            embeddedContext.setAttribute(name, object);
        }

        public void removeAttribute(String name) {
            embeddedContext.removeAttribute(name);
        }

        public String getServletContextName() {
            return embeddedContext.getServletContextName();
        }

    }

    /**
     * Enumerates of multiple source in the order specified in the sources
     * array.
     * 
     * @author Natasha Carter (natasha.carter@csiro.au)
     * 
     */
    public class MultiSourceEnumeration implements Enumeration {
        Enumeration[] sources;        

        MultiSourceEnumeration(Enumeration[] sources) {
            this.sources = sources;
        }

        /**
         * @returns true when at least one of the enumerators has more elements
         */
        public boolean hasMoreElements() {
            for (Enumeration source : sources) {
                if (source.hasMoreElements()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * @returns the next element in the collection. It will go through the
         *          enumerators in the order that they are supplied.
         */
        public Object nextElement() {
            for (Enumeration source : sources) {
                if (source.hasMoreElements()) {
                    return source.nextElement();
                }
            }
            return null;
        }

    }
}
