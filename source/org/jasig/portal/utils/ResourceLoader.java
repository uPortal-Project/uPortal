/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or withoutu
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package  org.jasig.portal.utils;

import org.jasig.portal.ResourceMissingException;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.MalformedURLException;
import org.xml.sax.InputSource;


/**
 * <p>This utility provides methods for accessing resources.
 * The methods generally use the classpath to find the resource
 * if the requested URL isn't already specified as a fully-qualified
 * URL string.</p>
 * <p>The methods of this class sort of replace the old UtiltiesBean.fixURI() method.</p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 * @since uPortal 2.0
 */

public class ResourceLoader {

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
  public static URL getResourceAsURL(Class aClass, String resource) throws ResourceMissingException {
    URL resourceURL = null;
    try {
      resourceURL = new URL(resource);
    } catch (MalformedURLException murle) {
      // URL is invalid, now try to load from classpath
      resourceURL = aClass.getResource(resource);
      if (resourceURL == null)
        throw new ResourceMissingException(resource, resource, "Resource not found in classpath");
    }
    return resourceURL;
  }

  /**
   * Returns the requested resource as a URL string.
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource to load
   * @return the requested URL as a string
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static String getResourceAsURLString(Class requestingClass, String resource) throws ResourceMissingException {
    return getResourceAsURL(requestingClass, resource).toString();
  }

  /**
   * Returns the requested resource as a stream.
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource to load
   * @return the requested URL as a stream
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
   * @return the requested URL as a SAX input source
   * @throws org.jasig.portal.ResourceMissingException
   * @throws java.io.IOException
   */
  public static InputSource getResourceAsSAXInputSource(Class requestingClass, String resource) throws ResourceMissingException, IOException {
    return new InputSource(getResourceAsURL(requestingClass, resource).openStream());
  }

  /**
   * Get the contents of a URL as a String
   * @param requestingClass the java.lang.Class object of the class that is attempting to load the resource
   * @param resource a String describing the full or partial URL of the resource whose contents to load
   * @return the data pointed to by a URI
   * @throws org.jasig.portal.ResourceMissingException
   * @throws java.io.IOException
   */
  public static String getResourceAsString (Class requestingClass, String uri) throws ResourceMissingException, IOException {
    String line = null;
    BufferedReader in = new BufferedReader (new InputStreamReader(getResourceAsStream(requestingClass, uri)));
    StringBuffer sbText = new StringBuffer (1024);
    while ((line = in.readLine()) != null)
      sbText.append (line).append ("\n");
    return sbText.toString ();
  }
}



