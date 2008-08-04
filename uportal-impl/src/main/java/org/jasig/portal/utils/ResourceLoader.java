/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.car.CarResources;
import org.jasig.portal.properties.PropertiesManager;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <p>This utility provides methods for accessing resources.
 * The methods generally use the classpath to find the resource
 * if the requested URL isn't already specified as a fully-qualified
 * URL string.</p>
 * <p>The methods of this class sort of replace the old UtiltiesBean.fixURI() method.</p>
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @since uPortal 2.0
 */

public class ResourceLoader {

    private static final Log log = LogFactory.getLog(ResourceLoader.class);

  private static DocumentBuilderFactory validatingDocumentBuilderFactory;

  private static DocumentBuilderFactory nonValidatingDocumentBuilderFactory;
  
  private static Map<Tuple<Class<?>, String>, URL> resourceUrlCache;
  private static Map<Tuple<Class<?>, String>, ResourceMissingException> resourceUrlNotFoundCache;

  static {
    validatingDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
    nonValidatingDocumentBuilderFactory = DocumentBuilderFactory.newInstance();

    validatingDocumentBuilderFactory.setValidating(true);
    nonValidatingDocumentBuilderFactory.setValidating(false);


    validatingDocumentBuilderFactory.setNamespaceAware(true);
    nonValidatingDocumentBuilderFactory.setNamespaceAware(true);

    try{
      String handler = PropertiesManager.getProperty("org.jasig.portal.utils.ResourceLoader.HttpsHandler");
      if ((System.getProperty("java.protocol.handler.pkgs") != null) &&
        !(System.getProperty("java.protocol.handler.pkgs").equals(""))){
        handler = handler+"|"+System.getProperty("java.protocol.handler.pkgs");
      }
      System.setProperty("java.protocol.handler.pkgs",handler);
    }
    catch(Exception e){
      log.error("Unable to set HTTPS Protocol handler", e);
    }
  }
  
    /**
     * @return the resourceUrlCache
     */
    public Map<Tuple<Class<?>, String>, URL> getResourceUrlCache() {
        return resourceUrlCache;
    }
    /**
     * @param resourceUrlCache the resourceUrlCache to set
     */
    public void setResourceUrlCache(Map<Tuple<Class<?>, String>, URL> resourceUrlCache) {
        ResourceLoader.resourceUrlCache = resourceUrlCache;
    }
  
    /**
     * @return the resourceUrlNotFoundCache
     */
    public Map<Tuple<Class<?>, String>, ResourceMissingException> getResourceUrlNotFoundCache() {
        return resourceUrlNotFoundCache;
    }
    /**
     * @param resourceUrlNotFoundCache the resourceUrlNotFoundCache to set
     */
    public void setResourceUrlNotFoundCache(Map<Tuple<Class<?>, String>, ResourceMissingException> resourceUrlNotFoundCache) {
        ResourceLoader.resourceUrlNotFoundCache = resourceUrlNotFoundCache;
    }
    
  /**
   * Finds a resource with a given name.  This is a convenience method for accessing a resource
   * from a channel or from the uPortal framework.  If a well-formed URL is passed in,
   * this method will use that URL unchanged to find the resource.
   * If the URL is not well-formed, this method will look for
   * the desired resource relative to the classpath.
   * If the resource name starts with "/", it is unchanged. Otherwise, the package name
   * of the requesting class is prepended to the resource name.
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource to load
   * @return a URL identifying the requested resource
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static URL getResourceAsURL(Class<?> requestingClass, String resource) throws ResourceMissingException {
    final Tuple<Class<?>, String> cacheKey = new Tuple<Class<?>, String>(requestingClass, resource);
     
    //Look for a cached URL 
    final Map<Tuple<Class<?>, String>, URL> resourceUrlCache = ResourceLoader.resourceUrlCache;
    URL resourceURL = resourceUrlCache != null ? resourceUrlCache.get(cacheKey) : null;
    if (resourceURL != null) {
        return resourceURL;
    }
    
    //Look for a failed lookup
    final Map<Tuple<Class<?>, String>, ResourceMissingException> resourceUrlNotFoundCache = ResourceLoader.resourceUrlNotFoundCache;
    ResourceMissingException exception = resourceUrlNotFoundCache != null ? resourceUrlNotFoundCache.get(cacheKey) : null;
    if (exception != null) {
        throw new ResourceMissingException(exception);
    }
    
    try {
      resourceURL = new URL(resource);
    } catch (MalformedURLException murle) {
      // URL is invalid, now try to load from classpath
      resourceURL = requestingClass.getResource(resource);

      if ( resourceURL == null )
      {
          // try in a car file
          resourceURL = CarResources.getInstance()
              .getClassLoader().getResource( resource );
      }

      if (resourceURL == null) {
        String resourceRelativeToClasspath = null;
        if (resource.startsWith("/"))
          resourceRelativeToClasspath = resource;
        else
          resourceRelativeToClasspath = '/' + requestingClass.getPackage().getName().replace('.', '/') + '/' + resource;
          exception = new ResourceMissingException(resource, resourceRelativeToClasspath, "Resource not found in classpath: " + resourceRelativeToClasspath);
          if (resourceUrlNotFoundCache != null) {
              resourceUrlNotFoundCache.put(cacheKey, exception);
          }
          throw new ResourceMissingException(exception);
      }
    }
    
    if (resourceUrlCache != null) {
        resourceUrlCache.put(cacheKey, resourceURL);
    }
    return resourceURL;
  }

  /**
   * Returns the requested resource as a URL string.
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource to load
   * @return the requested resource as a URL string
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static String getResourceAsURLString(Class<?> requestingClass, String resource) throws ResourceMissingException {
	  return getResourceAsURL(requestingClass, resource).toString();
	}
	
  public static long getResourceLastModified(Class<?> requestingClass, String resource) {
      final URL contentUrl = getResourceAsURL(requestingClass, resource);
      final String contentFileName = contentUrl.getFile();
      
      //Use JAR modified time if the resource is in a .jar file denoted by ! delimeter
      final int delimIndex = contentFileName.indexOf('!');
      
      final File contentFile;
      if (delimIndex > 0) {
          contentFile = new File(contentFileName.substring(0, delimIndex));
      }
      else {
          contentFile = new File(contentFileName);
      }
      
      return contentFile.lastModified();
  }

  /**
   * Returns the requested resource as a stream.
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource to load
   * @return the requested resource as a stream
   * @throws org.jasig.portal.ResourceMissingException
   * @throws java.io.IOException
   */
  public static InputStream getResourceAsStream(Class<?> requestingClass, String resource) throws ResourceMissingException, IOException {
    return getResourceAsURL(requestingClass, resource).openStream();
  }

  /**
   * Returns the requested resource as a SAX input source.
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource to load
   * @return the requested resource as a SAX input source
   * @throws org.jasig.portal.ResourceMissingException
   * @throws java.io.IOException
   */
  public static InputSource getResourceAsSAXInputSource(Class<?> requestingClass, String resource) throws ResourceMissingException, IOException {
      URL url = getResourceAsURL(requestingClass, resource);
      InputSource source = new InputSource(url.openStream());
      source.setPublicId(url.toExternalForm());
    return source;
  }

  /**
   * Get the contents of a URL as an XML Document
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource whose contents to load
   * @param validate boolean. True if the document builder factory should validate, false otherwise.
   * @return the actual contents of the resource as an XML Document
   * @throws org.jasig.portal.ResourceMissingException
   * @throws java.io.IOException
   * @throws javax.xml.parsers.ParserConfigurationException
   * @throws org.xml.sax.SAXException
   */
  public static Document getResourceAsDocument (Class<?> requestingClass, String resource, boolean validate)
      throws ResourceMissingException, IOException, ParserConfigurationException, SAXException {
    Document document = null;
    InputStream inputStream = null;

    try {

    	DocumentBuilderFactory factoryToUse = null;

      if (validate) {
    	  factoryToUse = ResourceLoader.validatingDocumentBuilderFactory;
      } else {
    	factoryToUse = ResourceLoader.nonValidatingDocumentBuilderFactory;
      }
      inputStream = getResourceAsStream(requestingClass, resource);
      DocumentBuilder db = factoryToUse.newDocumentBuilder();

      db.setEntityResolver(new DTDResolver());
      db.setErrorHandler(new SAXErrorHandler("ResourceLoader.getResourceAsDocument(" + resource + ")"));
      document = db.parse(inputStream);
    } finally {
      if (inputStream != null)
        inputStream.close();
    }
    return document;
  }

  /**
   * Get the contents of a URL as an XML Document, first trying to read the Document with validation turned on,
   * and falling back to reading it with validation turned off.
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource whose contents to load
   * @return the actual contents of the resource as an XML Document
   * @throws org.jasig.portal.ResourceMissingException
   * @throws java.io.IOException
   * @throws javax.xml.parsers.ParserConfigurationException
   * @throws org.xml.sax.SAXException
   */
  public static Document getResourceAsDocument (Class<?> requestingClass, String resource)
      throws ResourceMissingException, IOException, ParserConfigurationException, SAXException {

	  // Default is non-validating...
	  return getResourceAsDocument(requestingClass, resource, false);

  }



  /**
   * Get the contents of a URL as a java.util.Properties object
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource whose contents to load
   * @return the actual contents of the resource as a Properties object
   * @throws org.jasig.portal.ResourceMissingException
   * @throws java.io.IOException
   */
  public static Properties getResourceAsProperties (Class<?> requestingClass, String resource) throws ResourceMissingException, IOException {
    InputStream inputStream = null;
    Properties props = null;
    try {
      inputStream = getResourceAsStream(requestingClass, resource);
      props = new Properties();
      props.load(inputStream);
    } finally {
      if(inputStream != null)
        inputStream.close();
    }
    return props;
  }

  /**
   * Get the contents of a URL as a String
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource whose contents to load
   * @return the actual contents of the resource as a String
   * @throws org.jasig.portal.ResourceMissingException
   * @throws java.io.IOException
   */
  public static String getResourceAsString (Class<?> requestingClass, String resource) throws ResourceMissingException, IOException {
    String line = null;
    BufferedReader in = null;
    StringBuffer sbText = null;
    try {
      in = new BufferedReader (new InputStreamReader(getResourceAsStream(requestingClass, resource)));
      sbText = new StringBuffer (1024);
      while ((line = in.readLine()) != null)
        sbText.append (line).append ("\n");
    } finally {
      if(in != null )
        in.close();
    }
    return sbText.toString ();
  }
}
