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

import org.jasig.portal.services.LogService;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.net.URL;
import java.net.MalformedURLException;
import org.xml.sax.helpers.*;

import org.jasig.portal.utils.SAX2FilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * A tool for managing a collection of stylesheets.
 * StylesheetSet allows you to instansiate a list
 * of stylesheets in memory and select one according
 * to the request/title/media parameters.
 * @author Peter Kharchenko
 * @version $Revision$
 */
public class StylesheetSet extends SAX2FilterImpl {
  protected Hashtable title_table;
  protected OrderedProps props = null;

  /**
   * put your documentation comment here
   */
  public StylesheetSet () {
    title_table = new Hashtable();
  }

  /**
   * put your documentation comment here
   * @param   ContentHandler dt
   */
  public StylesheetSet (ContentHandler dt) {
      super(dt);
  }

  /**
   * put your documentation comment here
   * @param   String uri
   */
  public StylesheetSet (String uri) throws PortalException {
    try {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        StylesheetSet dummy = new StylesheetSet();
        reader.setContentHandler((ContentHandler)dummy);
        URL url = expandSystemId(uri);
        try {
            reader.parse(url.toString());
            this.title_table = dummy.getTitleTable();
        } catch (IOException ioe) {
            throw new ResourceMissingException(url.toString(),"XSLT stylesheet","StylesheetSet(uri) : Unable to read stylesheet set from the specified location. Please check the URL.");
        } catch (SAXException se) {
            throw new GeneralRenderingException("StylesheetSet(uri) : Unable to parse stylesheet set (.ssl) file. URL=\""+url+"\", exception message: "+se.getMessage());
        }
    } catch (SAXException se) {
        se.printStackTrace();
        throw new GeneralRenderingException("StylesheetSet(uri) : Unable to instantiate SAX Reader. Please check your library installation.");
    }
  }

  /**
   * put your documentation comment here
   * @param title
   * @return 
   */
  public Source getStylesheet (String title) {
    Hashtable media_table = (Hashtable)title_table.get(title);
    if (media_table == null)
      return  null;
    StylesheetDescription sd = null;
    if (media_table.isEmpty())
      return  null;
    for (Enumeration e = media_table.elements(); e.hasMoreElements();) {
      if (sd == null) {
        sd = (StylesheetDescription)e.nextElement();
      } 
      else {
        StylesheetDescription tsd = (StylesheetDescription)e.nextElement();
        if (!tsd.getAlternate())
          sd = tsd;
      }
    }
    // after all this mess we should have a valid sd
    return  (new StreamSource(sd.getURI()));
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public Source getStylesheet () {
    // this is painful ... browse through all possible
    // browse through all titles to find a non-alternate
    // stylesheet
    StylesheetDescription sd = null;
    for (Enumeration e = title_table.elements(); e.hasMoreElements();) {
      Hashtable media_table = (Hashtable)e.nextElement();
      if (!media_table.isEmpty()) {
        for (Enumeration f = media_table.elements(); f.hasMoreElements();) {
          if (sd == null) {
            sd = (StylesheetDescription)f.nextElement();
          } 
          else {
            StylesheetDescription tsd = (StylesheetDescription)f.nextElement();
            if (!tsd.getAlternate())
              sd = tsd;
          }
          if (!sd.getAlternate())
            break;
        }
      }
    }
    return  (new StreamSource(sd.getURI()));
  }

  /**
   * put your documentation comment here
   * @param title
   * @param media
   * @return 
   */
  public Source getStylesheet (String title, String media) {
    Hashtable media_table = (Hashtable)title_table.get(title);
    if (media_table == null)
      return  null;
    StylesheetDescription sd = (StylesheetDescription)media_table.get(media);
    if (sd == null) {
      Enumeration sls = media_table.elements();
      if (sls.hasMoreElements())
        sd = (StylesheetDescription)sls.nextElement();
    }
    if (sd == null)
      return  null;
    return  (new StreamSource(sd.getURI()));
  }

  /**
   * put your documentation comment here
   * @param title
   * @param media
   * @return 
   */
  public Source getStylesheet (String title, BrowserInfo bi) throws PortalException {
    String media = getMedia(bi);
    Hashtable media_table = (Hashtable)title_table.get(title);
    if (media_table == null)
      return  null;
    StylesheetDescription sd = (StylesheetDescription)media_table.get(media);
    if (sd == null) {
      Enumeration sls = media_table.elements();
      if (sls.hasMoreElements())
        sd = (StylesheetDescription)sls.nextElement();
    }
    if (sd == null)
      return  null;
    return  (new StreamSource(sd.getURI()));
  }

  /**
   * Returns the URI of the stylesheet matching the media
   * @param media
   * @return the stylesheet URI
   */
  public String getStylesheetURI (String media) throws GeneralRenderingException {
    if (media == null) {
      throw  (new GeneralRenderingException("StylesheetSet.getStylesheetURI(): Media argument cannot be null"));
    }
    String ssURI = null;
    StylesheetDescription sd = getStylesheetDescription(media);
    if (sd != null) {
      ssURI = sd.getURI();
    }
    return  ssURI;
  }

  /**
   * put your documentation comment here
   * @param req
   * @return 
   */
  public String getStylesheetURI (HttpServletRequest req) throws PortalException {
    return  (getStylesheetURI(getMedia(req)));
  }

  /**
   * put your documentation comment here
   * @param bi
   * @return 
   */
  public String getStylesheetURI (BrowserInfo bi) throws PortalException {
    return  getStylesheetURI(getMedia(bi));
  }

  /**
   * put your documentation comment here
   * @param title
   * @param req
   * @return 
   */
  public String getStylesheetURI (String title, HttpServletRequest req) throws PortalException {
    return  getStylesheetURI(title, getMedia(req));
  }

  /**
   * put your documentation comment here
   * @param title
   * @param bi
   * @return 
   */
  public String getStylesheetURI (String title, BrowserInfo bi) throws PortalException {
    return  getStylesheetURI(title, getMedia(bi));
  }

  /**
   * Returns the URI of the stylesheet matching the title and media
   * @param title
   * @param media
   * @return the stylesheet URI
   */
  public String getStylesheetURI (String title, String media) throws GeneralRenderingException {
    if (title != null) {
      Hashtable media_table = (Hashtable)title_table.get(title);
      if (media_table == null) {
        return  null;
      }
      LogService.instance().log(LogService.DEBUG, "media=\"" + media + "\"");
      StylesheetDescription sd = (StylesheetDescription)media_table.get(media);
      if (sd == null) {
        Enumeration sls = media_table.elements();
        if (sls.hasMoreElements())
          sd = (StylesheetDescription)sls.nextElement();
      }
      if (sd == null) {
        return  null;
      }
      return  sd.getURI();
    } 
    else {
      return  getStylesheetURI(media);
    }
  }

  /**
   * put your documentation comment here
   * @param media
   * @return 
   */
  protected StylesheetDescription getStylesheetDescription (String media) throws GeneralRenderingException {
    if (media == null) {
      LogService.instance().log(LogService.ERROR, "StylesheetSet::getStylesheetDescription() : media argument is null");
      throw  (new GeneralRenderingException("StylesheetSet.getStylesheetDescription(): Null media argument passed in"));
    }
    // search for a non-alternate stylesheet for a particular media
    StylesheetDescription sd = null;
    for (Enumeration e = title_table.elements(); e.hasMoreElements();) {
      Hashtable media_table = (Hashtable)e.nextElement();
      StylesheetDescription tsd = (StylesheetDescription)media_table.get(media);
      if (tsd != null) {
        if (sd == null) {
          sd = tsd;
        }
        if (!tsd.getAlternate()) {
          sd = tsd;
          break;
        }
      } 
      else {
        Enumeration sls = media_table.elements();
        if (sls.hasMoreElements()) {
          sd = (StylesheetDescription)sls.nextElement();
        }
      }
    }
    return  sd;
  }

  /**
   * put your documentation comment here
   * @param title
   * @param req
   * @return 
   */
  public Source getStylesheet (String title, HttpServletRequest req) throws PortalException {
    //	LogService.instance().log(LogService.DEBUG,"getStylesheet(title,req) : Looking up the media name for "+req.getHeader("User-Agent")+" : media=\""+getMedia(req)+"\"");
    return  getStylesheet(title, getMedia(req));
  }

  /**
   * put your documentation comment here
   * @param req
   * @return 
   */
  public Source getStylesheet (HttpServletRequest req) throws PortalException {
    StylesheetDescription sd = getStylesheetDescription(getMedia(req));
    if (sd != null) {
      return  new StreamSource(sd.getURI());
    } 
    else {
      return  null;
    }
  }

  /**
   * put your documentation comment here
   * @param media
   * @return 
   */
  public Source getStylesheetByMedia (String media) throws GeneralRenderingException {
    //	LogService.instance().log(LogService.DEBUG,"getStylesheet(req) : Looking up the media name for "+req.getHeader("User-Agent")+" : media=\""+getMedia(req)+"\"");
    StylesheetDescription sd = getStylesheetDescription(media);
    if (sd != null) {
      return  new StreamSource(sd.getURI());
    } 
    else {
      return  (null);
    }
  }

  /**
   * put your documentation comment here
   * @param sd
   */
  public void addStyleSheet (StylesheetDescription sd) {
    // see if the title is already in the hashtable
    Hashtable media_table = (Hashtable)title_table.get(sd.getTitle());
    if (media_table == null) {
      media_table = new Hashtable();
      media_table.put(sd.getMedia(), sd);
      title_table.put(sd.getTitle(), media_table);
    } 
    else {
      media_table.put(sd.getMedia(), sd);
    }
  }

  /**
   * put your documentation comment here
   * @param target
   * @param data
   * @exception SAXException
   */
  public void processingInstruction(String target, String data) throws SAXException {
    if (target.equals("xml-stylesheet")) {
      StylesheetDescription sd = new StylesheetDescription(data);
      this.addStyleSheet(sd);
    }
    // pass on the stylesheet instruction
    if (this.getContentHandler() != null) {
      this.getContentHandler().processingInstruction(target, data);
    }
  }

  /**
   * put your documentation comment here
   * @param uri
   */
  public void setMediaProps (String uri) throws PortalException {
    if (uri == null) {
      uri = "properties/media.properties";
    }
    uri = UtilitiesBean.fixURI(uri);
    try {
      // Create a URL from the given URI
      URL url = expandSystemId(uri);
      if (url != null) {
        props = new OrderedProps(url.openStream());
      } 
      else {
        throw new ResourceMissingException(uri, "The media.properties file", "Unable to understand the media.properties URI");
      }
    } catch (IOException ioe) {
        throw new ResourceMissingException(uri, "The media.properties file", ioe.getMessage());
    }
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public Hashtable getTitleTable () {
    return  title_table;
  }

  /**
   * put your documentation comment here
   * @param req
   * @return 
   */
  protected String getMedia (HttpServletRequest req) throws PortalException {
    // Try to set the 
    if (props == null) {
      this.setMediaProps(null);
    }
    if (props != null) {
      return  props.getValue(req.getHeader("User-Agent"));
    } 
    else {
      return  (null);
    }
  }

  /**
   * put your documentation comment here
   * @param bi
   * @return 
   */
  protected String getMedia (BrowserInfo bi) throws PortalException {
    if (props == null)
      this.setMediaProps((String)null);
    if (props != null)
      return  props.getValue(bi.getUserAgent());
    return  (String)null;
  }

  /**
   * put your documentation comment here
   * @param systemId
   * @return 
   */
  private URL expandSystemId (String systemId) {
    String id = systemId;
    // check for bad parameters id
    if (id == null || id.length() == 0)
      return  null;
    // if id already expanded, return
    try {
      URL url = new URL(id);
      if (url != null)
        return  url;
    } catch (MalformedURLException e) {
    // continue on...
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
        if (currentTokens.hasMoreTokens())
          Key = currentTokens.nextToken().trim();
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
      if (s == null)
        return  null;
      int i, j = attVec.size();
      for (i = 0; i < j; i++) {
        String temp[] = (String[])attVec.elementAt(i);
        if (s.indexOf(temp[0]) > -1)
          return  temp[1];
      }
      return  "unknown";
    }
  }
}



