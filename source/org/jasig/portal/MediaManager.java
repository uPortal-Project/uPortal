/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal;

import  javax.servlet.*;
import  javax.servlet.http.*;
import  java.io.*;
import  java.util.*;
import  java.net.*;


/**
 * A tool for managing various media properties.
 * Given a request object, MediaManager determines
 * a client browser type (media). MediaManager also
 * provides information on the mime type that generated
 * response should carry.
 * @author Peter Kharchenko
 * @version $Revision$
 */
public class MediaManager {
  protected OrderedProps mediaProps = null;
  protected OrderedProps mimeProps = null;
  protected OrderedProps serializerProps = null;

  /**
   * Constructor that initializes all of the property tables.
   * This is equivalent to running a base constructor and
   * setMediaProps(), setMimeProps() and setSerializerProps() afterwards.
   *
   * @param mediaPropsFile location of the media properties file
   * @param mimePropsFile location of the mime properties file
   * @param serializerPropsFile location of the serializer properties file
   */
  public MediaManager (String mediaPropsFile, String mimePropsFile, String serializerPropsFile) {
    setMediaProps(mediaPropsFile);
    setMimeProps(mimePropsFile);
    setSerializerProps(serializerPropsFile);
  }

  /**
   * Initializes media properties table.
   * @param uri location of the media properties file, complete with the filename
   */
  public void setMediaProps (String uri) {
    if (uri == null) {
      uri = "file://" + GenericPortalBean.getPortalBaseDir() + "properties" + File.separator + "media.properties";
    }
    uri = UtilitiesBean.fixURI(uri);
    try {
      URL url = expandSystemId(uri);
      Logger.log(Logger.DEBUG, "MediaManager::setMediaProps() uri=" + uri + " URL=" + url);
      if (url != null) {
        mediaProps = new OrderedProps(url.openStream());
      }
    } catch (IOException ioe1) {
      Logger.log(Logger.ERROR, "MediaManager::setMediaProps : Exception occurred while loading media properties file: " + 
          uri + ". " + ioe1);
    }
  }

  /**
   * Initializes mime properties table.
   * @param uri location of the mime properties file, complete with the filename
   */
  public void setMimeProps (String uri) {
    if (uri == null) {
      uri = "file://" + GenericPortalBean.getPortalBaseDir() + "properties" + File.separator + "mime.properties";
    }
    uri = UtilitiesBean.fixURI(uri);
    try {
      URL url = expandSystemId(uri);
      if (url != null) {
        mimeProps = new OrderedProps(url.openStream());
      }
    } catch (IOException ioe1) {
      Logger.log(Logger.ERROR, "MediaManager::setMimeProps : Exception occurred while loading mime properties file: " + 
          uri + ". " + ioe1);
    }
  }

  /**
   * Initializes serializer properties table.
   * @param uri location of the serializer properties file, complete with the filename
   */
  public void setSerializerProps (String uri) {
    if (uri == null) {
      uri = "file://" + GenericPortalBean.getPortalBaseDir() + "properties" + File.separator + "serializer.properties";
    }
    uri = UtilitiesBean.fixURI(uri);
    try {
      URL url = expandSystemId(uri);
      if (url != null) {
        serializerProps = new OrderedProps(url.openStream());
      }
    } catch (IOException ioe1) {
      Logger.log(Logger.ERROR, "MediaManager::setSerializerProps : Exception occurred while loading serializer properties file: "
          + uri + ". " + ioe1);
    }
  }

  /**
   * Determines a media name from the request object.
   * @param req the request object
   * @return media name
   */
  public String getMedia (HttpServletRequest req) {
    if (mediaProps == null) {
      this.setMediaProps((String)null);
    }
    if (mediaProps != null) {
      return  mediaProps.getValue(req.getHeader("User-Agent"));
    }
    return  (String)null;
  }

  /**
   * Determines a media name from the request object.
   * @param req the request object
   * @return media name
   */
  public String getMedia (BrowserInfo bi) {
    if (mediaProps == null) {
      this.setMediaProps((String)null);
    }
    if (mediaProps != null) {
      return  mediaProps.getValue(bi.getUserAgent());
    }
    return  (String)null;
  }

  /**
   * Return a default media type.
   * The default media type is the first
   * media listed in the media.properties file
   * @return default media name
   */
  public String getDefaultMedia () {
    if (mediaProps == null) {
      this.setMediaProps((String)null);
    }
    if (mediaProps != null) {
      return  mediaProps.getDefaultValue();
    }
    return  (String)null;
  }

  /**
   * Determines a mime name from the request object.
   * @param req the request object
   * @return mime type string
   */
  public String getReturnMimeType (HttpServletRequest req) {
    return  (getReturnMimeType(getMedia(req)));
  }

  /**
   * Determines a mime name from a media name.
   * @param media name
   * @return mime type string
   */
  public String getReturnMimeType (String mediaType) {
    if (mimeProps == null) {
      this.setMimeProps((String)null);
    }
    if (mimeProps != null) {
      return  mimeProps.getValue(mediaType);
    } 
    else {
      return  null;
    }
  }

  /**
   * put your documentation comment here
   * @param systemId
   * @return 
   */
  private URL expandSystemId (String systemId) {
    String id = systemId;
    // check for bad parameters id
    if (id == null || id.length() == 0) {
      return  (null);
    }
    // if id already expanded, return
    try {
      URL url = new URL(id);
      if (url != null) {
        return  (url);
      }
    } catch (MalformedURLException e) {
      // continue on...
      Logger.log(Logger.WARN, "MediaManager.expandSystemId(): Threw exception! These ain't cheap!");
    }
    // normalize id
    id = fixURI(id);
    // normalize base
    URL base = null;
    URL url = null;
    try {
      String dir;
      try {
        dir = fixURI(System.getProperty("user.dir"));
      } catch (SecurityException se) {
        dir = "";
      }
      if (!dir.endsWith("/")) {
        dir = dir + "/";
      }
      base = new URL("file", "", dir);
      // expand id
      url = new URL(base, id);
    } catch (Exception e) {
      // let it go through
      Logger.log(Logger.WARN, "MediaManager.expandSystemId(): Threw exception! These ain't cheap!");
    }
    return  url;
  }

  /**
   * FROM XALAN
   * Fixes a platform dependent filename to standard URI form.
   *
   * @param str The string to fix.
   *
   * @return Returns the fixed URI string.
   */
  private static String fixURI (String str) {
    // handle platform dependent strings
    str = str.replace(java.io.File.separatorChar, '/');
    // Windows fix
    if (str.length() >= 2) {
      char ch1 = str.charAt(1);
      if (ch1 == ':') {
        char ch0 = Character.toUpperCase(str.charAt(0));
        if (ch0 >= 'A' && ch0 <= 'Z') {
          str = "/" + str;
        }
      }
    }
    return  str;
  }

  /**
   *  COPIED FROM XALAN SOURCE
   *  Stores the keys and values from a file (similar to a properties file) and
   *  can return the first value which has a key contained in its string.
   *  File can have comment lines starting with '#" and for each line the entries are
   *  separated by tabs and '=' char.
   */
  class OrderedProps {
    /**
     * Stores the Key and Values as an array of Strings
     */
    private Vector attVec = new Vector(15);

    /**
     * Constructor.
     * @param inputStream Stream containing the properties file.
     * @exception IOException Thrown if unable to read from stream
     */
    OrderedProps (InputStream inputStream) throws IOException
    {
      BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
      String currentLine, Key = null;
      StringTokenizer currentTokens;
      while ((currentLine = input.readLine()) != null) {
        currentTokens = new StringTokenizer(currentLine, "=\t\r\n");
        if (currentTokens.hasMoreTokens()) {
          Key = currentTokens.nextToken().trim();
        }
        if ((Key != null) && !Key.startsWith("#") && currentTokens.hasMoreTokens()) {
          String temp[] = new String[2];
          temp[0] = Key;
          temp[1] = currentTokens.nextToken().trim();
          attVec.addElement(temp);
        }
      }
    }

    /**
     * Iterates through the Key list and returns the first value for whose
     * key the given string contains.  Returns "unknown" if no key is contained
     * in the string.
     * @param s String being searched for a key.
     * @return Value for key found in string, otherwise "unknown"
     */
    String getValue (String s) {
      int j = attVec.size();
      for (int i = 0; i < j; i++) {
        String temp[] = (String[])attVec.elementAt(i);
        if (s.indexOf(temp[0]) > -1) {
          return  (temp[1]);
        }
      }
      return  ("unknown");
    }

    /**
     * put your documentation comment here
     * @return 
     */
    String getDefaultValue () {
      return  ((String[])attVec.elementAt(0))[1];
    }
  }
}



