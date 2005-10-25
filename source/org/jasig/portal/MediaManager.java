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
 */


package  org.jasig.portal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.serialize.BaseMarkupSerializer;
import org.jasig.portal.serialize.CachingHTMLSerializer;
import org.jasig.portal.serialize.CachingXHTMLSerializer;
import org.jasig.portal.serialize.OutputFormat;
import org.jasig.portal.serialize.XMLSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


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
    
    private static final Log log = LogFactory.getLog(MediaManager.class);
    
  protected OrderedProps mediaProps = null;
  protected OrderedProps mimeProps = null;
  protected OrderedProps serializerProps = null;
  private static boolean outputIndenting = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.MediaManager.output_indenting");
  private static boolean omitDoctype = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.MediaManager.omit_doctype");

  private static final String mediaPropsUrl = MediaManager.class.getResource("/properties/media.properties").toString();
  private static final String mimePropsUrl = MediaManager.class.getResource("/properties/mime.properties").toString();
  private static final String serializerPropsUrl = MediaManager.class.getResource("/properties/serializer.properties").toString();
  
  private static final MediaManager MEDIAMANAGER = new MediaManager(mediaPropsUrl, mimePropsUrl, serializerPropsUrl);
  /**
   * A user agent string to use when the user-agent header value itself is null.
   *
   */
  public static final String NULL_USER_AGENT="null";
  public static final String UNKNOWN = "unknown";

  /**
   * Constructs a MediaManager
   */
  private MediaManager () {
  }

  /**
   * 7/25/05 - UP-1181 - change MediaManager into a singleton
   * 
   * Returns the default MediaManager singleton
   */
  public static MediaManager getMediaManager() {
      return (MEDIAMANAGER);
  }
  
  /**
   * Constructor that initializes all of the property tables.
   * This is equivalent to running a base constructor and
   * setMediaProps(), setMimeProps() and setSerializerProps() afterwards.
   *
   * @param mediaPropsFile location of the media properties file
   * @param mimePropsFile location of the mime properties file
   * @param serializerPropsFile location of the serializer properties file
   */
  private MediaManager (String mediaPropsFile, String mimePropsFile, String serializerPropsFile) {
    setMediaProps(mediaPropsFile);
    setMimeProps(mimePropsFile);
    setSerializerProps(serializerPropsFile);
  }

  /**
   * Initializes media properties table.
   * @param uri location of the media properties file, complete with the filename
   */
  public void setMediaProps (String uri) {
    URL url = null;
    try {
      if (uri == null)
        url = this.getClass().getResource("/properties/media.properties");
      else
        url = new URL(uri);
      if (url != null) {
        InputStream in = url.openStream();
        try {
          mediaProps = new OrderedProps(in);
        } finally {
          in.close();
        }
      }
    } catch (IOException ioe) {
      log.error( "MediaManager::setMediaProps : Exception occurred while loading media properties file: " +
          uri + ". " + ioe);
    }
  }

  /**
   * Initializes mime properties table.
   * @param uri location of the mime properties file, complete with the filename
   */
  public void setMimeProps (String uri) {
    URL url = null;
    try {
      if (uri == null)
        url = this.getClass().getResource("/properties/mime.properties");
      else
        url = new URL(uri);
      if (url != null) {
        InputStream in = url.openStream();
        try {
          mimeProps = new OrderedProps(in);
        } finally {
          in.close();
        }
      }
    } catch (IOException ioe) {
      log.error( "MediaManager::setMimeProps : Exception occurred while loading mime properties file: " +
          uri + ". " + ioe);
    }
  }

  /**
   * Initializes serializer properties table.
   * @param uri location of the serializer properties file, complete with the filename
   */
  public void setSerializerProps (String uri) {
    URL url = null;
    try {
      if (uri == null)
        url = this.getClass().getResource("/properties/serializer.properties");
      else
        url = new URL(uri);
      if (url != null) {
        InputStream in = url.openStream();
        try {
          serializerProps = new OrderedProps(in);
        } finally {
          in.close();
        }
      }
    } catch (IOException ioe) {
      log.error( "MediaManager::setSerializerProps : Exception occurred while loading serializer properties file: " +
          uri + ". " + ioe);
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
        String ua=req.getHeader("User-Agent");
        if(ua==null || ua.equals("")) {
            ua=NULL_USER_AGENT;
        }
      return  mediaProps.getValue(ua);
    }
    return  (String)null;
  }

  /**
   * Determines a media name from the browser info object.
   * @param bi the browser info
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
    String mimeType = this.getReturnMimeType(this.getMedia(req));
    if (UNKNOWN.equals(mimeType)) {
      String accepts = req.getHeader("accept");
      if (accepts != null && accepts.indexOf("text/html") != -1) {
        mimeType = "text/html";
      }
    }
    return mimeType;
  }

  /**
   * Determines a mime name from a media type name.
   * @param mediaType the media type name
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
   * Determines and configures a serialzier that is proper
   * for the specified media type.
   * "serializer.properties" file contains mapping of media
   * names to serializer names.
   * Prior to using a serializer returned by this function,
   * make sure to set it up by calling asContentHandler(),
   * asDocumentHandler() or asDOMSerializer().
   *
   * @param mediaType media name
   * @param out output writer
   * @return the serializer
   */
  public BaseMarkupSerializer getSerializer (String mediaType, java.io.Writer out) {
    // I don't like this function, here's why :
    //   1.  I would like to make it read all preferences
    //      from some kind of a .properties file, just like
    //      mime and media functions do. The problem with doing
    //      it so is that the often serializer needs additional
    //      parameters passed to it during the initialization time.
    //       For example, OutputFormat object constructor parameters
    //      are very important for WML, and there's no way to store
    //      such information in a simple properties file.
    //       So the end result is that in order to support another
    //      mark up language, one would need to edit this function
    //      and recompile the code.
    //
    //   2.  It shouldn't be the "mediaType" passed as parameter, but
    //      the mime type. Unfortunately, there are differences in the
    //      markup rules inside a particular mime type.
    //      (i.e. netscape vs. AvantGo)
    //
    // please imporve on this if you can.
    String serializerName = null;
    if (serializerProps == null) {
      this.setSerializerProps((String)null);
    }
    if (serializerProps != null) {
      serializerName = serializerProps.getValue(mediaType);
    }
    if (serializerName != null) {
      return getSerializerByName(serializerName, out);
    }
    else {
      log.error( "MediaManager::getSerializer() : Unable to initialize serializerProperties. Returning a null serializer object");
      return  null;
    }
  }

  /**
   * Gets a serializer by name which writes to the provided OutputStream
   * @param serializerName
   * @param out
   * @return the serializer
   */
  public BaseMarkupSerializer getSerializerByName (String serializerName, java.io.OutputStream out) throws UnsupportedEncodingException {
      return getSerializerByName(serializerName, new OutputStreamWriter(out,"UTF-8"));
  }

  /**
   * Gets a serializer by name which writes to the provided Writer
   * @param serializerName
   * @param out
   * @return the serializer
   */
  public BaseMarkupSerializer getSerializerByName (String serializerName, java.io.Writer out) {
    if (serializerName != null && serializerName.equals("WML")) {
      OutputFormat frmt = new OutputFormat("wml", "UTF-8", true);
      frmt.setDoctype("-//WAPFORUM//DTD WML 1.1//EN", "http://www.wapforum.org/DTD/wml_1.1.xml");
      return  new XMLSerializer(out, frmt);
    } /* else if (serializerName != null && serializerName.equals("PalmHTML")) {
      OutputFormat frmt = new OutputFormat("HTML", "UTF-8", true);
      return  new PalmHTMLSerializer(out, frmt);
      } */ else if (serializerName != null && serializerName.equals("XML")) {
      OutputFormat frmt = new OutputFormat("XML", "UTF-8", true);
      return  new XMLSerializer(out, frmt);
    } else if (serializerName != null && serializerName.equals("XHTML")) {
      OutputFormat frmt = new OutputFormat("XHTML", "UTF-8", true);
      frmt.setPreserveSpace(true);
      frmt.setIndenting(outputIndenting);
      frmt.setDoctype("-//W3C//DTD XHTML 1.0 Transitional//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
      frmt.setOmitDocumentType(omitDoctype);
      return  new CachingXHTMLSerializer(out, frmt);
    } else {
      // default case is HTML, such as that for netscape and explorer
      OutputFormat frmt = new OutputFormat("HTML", "UTF-8", true);
      frmt.setPreserveSpace(true);
      frmt.setIndenting(outputIndenting);
      frmt.setDoctype("-//W3C//DTD HTML 4.01 Transitional//EN", "http://www.w3.org/TR/1999/REC-html401-19991224/loose.dtd");
      frmt.setOmitDocumentType(omitDoctype);
      return  new CachingHTMLSerializer(out, frmt);
    }
  }

  /**
   * Another version of getSerializer() with OutputStream as one of the parameters.
   * @param mediaType media type string
   * @param out output stream
   * @return the markup serializer
   */
  public BaseMarkupSerializer getSerializer (String mediaType, java.io.OutputStream out) throws UnsupportedEncodingException {
    return getSerializer(mediaType, new OutputStreamWriter(out,"UTF-8"));
  }

  /**
   * Automatically determines the media type from the request object,
   * @param req the request object
   * @param out the output writer object
   * @return the markup serializer
   */
  public BaseMarkupSerializer getSerializer (HttpServletRequest req, java.io.Writer out) {
    if (mediaProps == null) {
      this.setMediaProps((String)null);
    }
    if (mediaProps != null) {
        String ua=req.getHeader("User-Agent");
        if(ua==null || ua.equals("")) {
            ua=NULL_USER_AGENT;
        }
        return  getSerializer(mediaProps.getValue(ua), out);
    }
    else {
      log.error( "MediaManager::getSerializer() : Unable to initialize mediaProperties. Returning a null serializer object");
      return  null;
    }
  }

  /**
   * Automatically determines the media type from the request object,
   * @param req the request object
   * @param out the output stream object
   * @return the markup serializer
   */
  public BaseMarkupSerializer getSerializer (HttpServletRequest req, java.io.OutputStream out) throws UnsupportedEncodingException {
    return getSerializer(req, new OutputStreamWriter(out,"UTF-8"));
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
      BufferedReader input = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
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
                input.close();
    }

    /**
     * Iterates through the Key list and returns the first value for whose
     * key the given string contains.  Returns "unknown" if no key is contained
     * in the string.
     * @param s String being searched for a key.
     * @return Value for key found in string, otherwise "unknown"
     */
    String getValue(String s) {
      int i, j = attVec.size();
      for (i = 0; i < j; i++) {
        String temp[] = (String[])attVec.elementAt(i);
        if (s.indexOf(temp[0]) > -1) {
          return  temp[1];
        }
      }
      return  UNKNOWN;
    }

    String getDefaultValue() {
      return  ((String[])attVec.elementAt(0))[1];
    }
  }
}



