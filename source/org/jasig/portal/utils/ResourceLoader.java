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
import java.net.URLDecoder;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.car.CarResources;
import org.jasig.portal.properties.PropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    
  private static DocumentBuilderFactory f;
  static {
    f = DocumentBuilderFactory.newInstance();
    f.setNamespaceAware(true);
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
  public static URL getResourceAsURL(Class requestingClass, String resource) throws ResourceMissingException {
    URL resourceURL = null;
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
        throw new ResourceMissingException(resource, resourceRelativeToClasspath, "Resource not found in classpath: " + resourceRelativeToClasspath);
      }
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
  public static String getResourceAsURLString(Class requestingClass, String resource) throws ResourceMissingException {
    return getResourceAsURL(requestingClass, resource).toString();
  }

  /**
   * Returns the requested resource as a File.
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource to load
   * @return the requested resource as a File
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static File getResourceAsFile(Class requestingClass, String resource) throws ResourceMissingException {
    return new File(getResourceAsFileString(requestingClass, resource));
  }

  /**
   * Returns the requested resource as a File string.
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource to load
   * @return the requested resource as a File string
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static String getResourceAsFileString(Class requestingClass, String resource) throws ResourceMissingException {
    return URLDecoder.decode(getResourceAsURL(requestingClass, resource).getFile());
  }

  /**
   * Returns the requested resource as a stream.
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource to load
   * @return the requested resource as a stream
   * @throws org.jasig.portal.ResourceMissingException
   * @throws java.io.IOException
   */
  public static InputStream getResourceAsStream(Class requestingClass, String resource) throws ResourceMissingException, IOException {
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
  public static InputSource getResourceAsSAXInputSource(Class requestingClass, String resource) throws ResourceMissingException, IOException {
    return new InputSource(getResourceAsURL(requestingClass, resource).openStream());
  }

  /**
   * Get the contents of a URL as an XML Document
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource whose contents to load
   * @return the actual contents of the resource as an XML Document
   * @throws org.jasig.portal.ResourceMissingException
   * @throws java.io.IOException
   * @throws javax.xml.parsers.ParserConfigurationException
   * @throws org.xml.sax.SAXException
   */
  public static Document getResourceAsDocument (Class requestingClass, String resource) throws ResourceMissingException, IOException, ParserConfigurationException, SAXException {
    Document document = null;
    InputStream inputStream = null;
    try {
      inputStream = getResourceAsStream(requestingClass, resource);
      document = f.newDocumentBuilder().parse(inputStream);  	
    } finally {
      if (inputStream != null)
        inputStream.close();  		
    }
    return document;
  }

  /**
   * Get the contents of a URL as a java.util.Properties object
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource whose contents to load
   * @return the actual contents of the resource as a Properties object
   * @throws org.jasig.portal.ResourceMissingException
   * @throws java.io.IOException
   */
  public static Properties getResourceAsProperties (Class requestingClass, String resource) throws ResourceMissingException, IOException {
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
  public static String getResourceAsString (Class requestingClass, String resource) throws ResourceMissingException, IOException {
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
