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
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.SAX2FilterImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A tool for managing a collection of stylesheets.
 * StylesheetSet allows you to instansiate a list
 * of stylesheets in memory and select one according
 * to the request/title/media parameters.
 * @author Peter Kharchenko
 * @version $Revision$
 */
public class StylesheetSet extends SAX2FilterImpl {
    
    private static final Log log = LogFactory.getLog(StylesheetSet.class);
    
  // Default URI for the media properties file
  protected static final String m_defaultMediaPropsUri = "/properties/media.properties";
  protected static Hashtable m_mediaPropsCache = new Hashtable(5);
  protected String m_myMediaPropsUri = m_defaultMediaPropsUri;
  protected Hashtable title_table;


    public StylesheetSet() {
        title_table = new Hashtable();
    }

    /**
     * Create a SAX filter that will pick up stylesheet bindings in a document that's processed through this filter.
     *
     * @param dt a <code>ContentHandler</code> of the downstream SAX listener..
     */
    public StylesheetSet(ContentHandler dt) {
        super(dt);
    }

    /**
     * Creates a new <code>StylesheetSet</code> instance given a .ssl file URI.
     *
     * @param uri a <code>String</code> value
     * @exception PortalException if an error occurs
     */
    public StylesheetSet(String uri) throws PortalException {
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            StylesheetSet dummy = new StylesheetSet();
            reader.setContentHandler((ContentHandler)dummy);
            URL url = null;
            try {
                url = new URL(uri);
                reader.parse(url.toString());
                this.title_table = dummy.getTitleTable();
            } catch (IOException ioe) {
                throw new ResourceMissingException(url.toString(),"XSLT stylesheet","StylesheetSet(uri) : Unable to read stylesheet set from the specified location. Please check the URL.");
            } catch (SAXException se) {
                throw new GeneralRenderingException("StylesheetSet(uri) : Unable to parse stylesheet set (.ssl) file. URL=\""+url+"\", exception message: "+se.getMessage());
            }
        } catch (SAXException se) {
            // Log the exception
            log.error( se);
            throw new GeneralRenderingException("StylesheetSet(uri) : Unable to instantiate SAX Reader. Please check your library installation.");
        }
    }


    /**
     * Obtain a stylesheet transform source
     *
     * @param title a <code>String</code> value
     * @return a <code>Source</code> for a given stylesheet
     */
  public Source getStylesheet(String title) {
    Hashtable media_table = (Hashtable)title_table.get(title);
    if (media_table == null) {
        return  null;
    }
    StylesheetDescription sd = null;
    if (media_table.isEmpty()) {
        return  null;
    }
    for (Enumeration e = media_table.elements(); e.hasMoreElements();) {
      if (sd == null) {
        sd = (StylesheetDescription)e.nextElement();
      } else {
        StylesheetDescription tsd = (StylesheetDescription)e.nextElement();
        if (!tsd.getAlternate())
          sd = tsd;
      }
    }
    // after all this mess we should have a valid sd
    return  (new StreamSource(sd.getURI()));
  }

    /**
     * Obtains a default stylesheet.
     *
     * @return a <code>Source</code> for a default stylesheet.
     */
  public Source getStylesheet() {
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
          }  else {
            StylesheetDescription tsd = (StylesheetDescription)f.nextElement();
            if (!tsd.getAlternate()) {
                sd = tsd;
            }
          }
          if (!sd.getAlternate()) {
              break;
          }
        }
      }
    }
    return  (new StreamSource(sd.getURI()));
  }


    /**
     * Obtain a stylesheet.
     *
     * @param title stylesheet title
     * @param media stylesheet media
     * @return a <code>Source</code> for the stylesheet.
     */
  public Source getStylesheet(String title, String media) {
    Hashtable media_table = (Hashtable)title_table.get(title);
    if (media_table == null) {
        return  null;
    }
    StylesheetDescription sd = (StylesheetDescription)media_table.get(media);
    if (sd == null) {
      Enumeration sls = media_table.elements();
      if (sls.hasMoreElements()) {
          sd = (StylesheetDescription)sls.nextElement();
      }
    }
    if (sd == null) {
        return  null;
    }
    return (new StreamSource(sd.getURI()));
  }


    /**
     * Obtain a stylesheet
     *
     * @param title stylesheet title
     * @param bi current <code>BrowserInfo</code> value
     * @return a <code>Source</code> for the stylesheet
     * @exception PortalException if an error occurs
     */
  public Source getStylesheet(String title, BrowserInfo bi) throws PortalException {
    String media = getMedia(bi);
    Hashtable media_table = (Hashtable)title_table.get(title);
    if (media_table == null) {
        return  null;
    }

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
  public String getStylesheetURI(String media) throws GeneralRenderingException {
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
     * Obtain a matching stylesheet.
     *
     * @param req current request value.
     * @return a <code>String</code> stylesheet URI
     * @exception PortalException if an error occurs
     */
  public String getStylesheetURI(HttpServletRequest req) throws PortalException {
    return  (getStylesheetURI(getMedia(req)));
  }


    /**
     * Obtain a matching stylesheet URI
     *
     * @param bi a <code>BrowserInfo</code> value
     * @return a <code>String</code> styleshet
     * @exception PortalException if an error occurs
     */
    public String getStylesheetURI(BrowserInfo bi) throws PortalException {
        return  getStylesheetURI(getMedia(bi));
    }


    /**
     * Obtain stylesheet URI
     *
     * @param title stylesheet title
     * @param req a <code>HttpServletRequest</code> value
     * @return a <code>String</code> stylesheet URI
     * @exception PortalException if an error occurs
     */
    public String getStylesheetURI(String title, HttpServletRequest req) throws PortalException {
        return  getStylesheetURI(title, getMedia(req));
    }

    /**
     * Describe <code>getStylesheetURI</code> method here.
     *
     * @param title a stylesheet title
     * @param bi a <code>BrowserInfo</code> value
     * @return a <code>String</code> stylesheet URI
     * @exception PortalException if an error occurs
     */
    public String getStylesheetURI(String title, BrowserInfo bi) throws PortalException {
        return  getStylesheetURI(title, getMedia(bi));
    }


    /**
     * Obtain a stylesheet URI
     *
     * @param title stylesheet title
     * @param media media value
     * @return a <code>String</code> stylesheet URI
     * @exception GeneralRenderingException if an error occurs
     */
  public String getStylesheetURI(String title, String media) throws GeneralRenderingException {
    if (title != null) {
      Hashtable media_table = (Hashtable)title_table.get(title);
      if (media_table == null) {
        return  null;
      }
      log.debug("media=\"" + media + "\"");
      StylesheetDescription sd = (StylesheetDescription)media_table.get(media);
      if (sd == null) {
        Enumeration sls = media_table.elements();
        if (sls.hasMoreElements()) {
            sd = (StylesheetDescription)sls.nextElement();
        }
      }
      if (sd == null) {
        return  null;
      }
      return  sd.getURI();
    } else {
      return  getStylesheetURI(media);
    }
  }


  protected StylesheetDescription getStylesheetDescription(String media) throws GeneralRenderingException {
    if (media == null) {
        log.error( "StylesheetSet::getStylesheetDescription() : media argument is null");
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
      } else {
        Enumeration sls = media_table.elements();
        if (sls.hasMoreElements()) {
          sd = (StylesheetDescription)sls.nextElement();
        }
      }
    }
    return  sd;
  }


    /**
     * Obtain a stylesheet source.
     *
     * @param title stylesheet title
     * @param req current request
     * @return a <code>Source</code> for the stylesheet.
     * @exception PortalException if an error occurs
     */
    public Source getStylesheet(String title, HttpServletRequest req) throws PortalException {
        //	log.debug("getStylesheet(title,req) : Looking up the media name for "+req.getHeader("User-Agent")+" : media=\""+getMedia(req)+"\"");
        return  getStylesheet(title, getMedia(req));
    }

    /**
     * Obtain a stylesheet source.
     *
     * @param req an <code>HttpServletRequest</code> value
     * @return a <code>Source</code> for the stylesheet
     * @exception PortalException if an error occurs
     */
    public Source getStylesheet(HttpServletRequest req) throws PortalException {
        StylesheetDescription sd = getStylesheetDescription(getMedia(req));
        if (sd != null) {
            return  new StreamSource(sd.getURI());
        }
        else {
            return  null;
        }
    }


    /**
     * Obtain a stylesheet for a given media.
     *
     * @param media desired media
     * @return a <code>Source</code> for the stylesheet.
     * @exception GeneralRenderingException if an error occurs
     */
  public Source getStylesheetByMedia(String media) throws GeneralRenderingException {
    //	log.debug("getStylesheet(req) : Looking up the media name for "+req.getHeader("User-Agent")+" : media=\""+getMedia(req)+"\"");
    StylesheetDescription sd = getStylesheetDescription(media);
    if (sd != null) {
      return  new StreamSource(sd.getURI());
    } else {
      return  (null);
    }
  }


    /**
     * Add a stylesheet to the list.
     *
     * @param sd a <code>StylesheetDescription</code> value
     */
   public void addStyleSheet(StylesheetDescription sd) {
    // see if the title is already in the hashtable
    Hashtable media_table = (Hashtable)title_table.get(sd.getTitle());
    if (media_table == null) {
      media_table = new Hashtable();
      media_table.put(sd.getMedia(), sd);
      title_table.put(sd.getTitle(), media_table);
    } else {
      media_table.put(sd.getMedia(), sd);
    }
  }

  /**
   * Fills StylesheetSet by accepting SAX events
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

  protected OrderedProps getMediaProps() throws PortalException {
    // Check to see if the media properties are in the cache
    if(m_mediaPropsCache.containsKey(m_myMediaPropsUri))
    {
      return((OrderedProps)m_mediaPropsCache.get(m_myMediaPropsUri));
    }
    else
    {
      // Try to load the media properties
      setMediaProps(m_myMediaPropsUri);
    }

    // Try to return them from the cache again
    return((OrderedProps)m_mediaPropsCache.get(m_myMediaPropsUri));
  }


    /**
     * Set the location of the media properties object.
     *
     * @param uri a <code>String</code> value
     * @exception PortalException if an error occurs
     */
    public void setMediaProps(String uri) throws PortalException {
    if (uri == null)
    {
      // Use the default URI
      uri = m_defaultMediaPropsUri;
    }
    else
    {
      // Fix up the provided URI
      uri = ResourceLoader.getResourceAsURLString(this.getClass(), uri);

      // Cache the URI of the media props that this instance will use
      m_myMediaPropsUri = uri;
    }

    // Check to see if we've already cached these properties
    if(m_mediaPropsCache.containsKey(uri))
    {
      return;
    }

    try
    {
      // Create a URL from the given URI
      URL url = new URL(uri);
      if (url != null)
      {
        // Put the loaded media properties in the cache
        InputStream in = url.openStream();
        try {
          m_mediaPropsCache.put(uri, new OrderedProps(in));
        } finally {
          in.close();
        }
      }
      else
      {
        throw new ResourceMissingException(uri, "The media.properties file", "Unable to understand the media.properties URI");
      }
    }
    catch (IOException ioe)
    {
      throw new ResourceMissingException(uri, "The media.properties file ", ioe.getMessage());
    }
  }

    protected  Hashtable getTitleTable () {
        return  title_table;
    }


  protected String getMedia (HttpServletRequest req) throws PortalException
  {
    String ua=req.getHeader("User-Agent");
    if(ua==null || ua.equals("")) {
      ua=MediaManager.NULL_USER_AGENT;
    }

    return(getMediaProps().getValue(ua));
  }


  protected String getMedia (BrowserInfo bi) throws PortalException {
    return(getMediaProps().getValue(bi.getUserAgent()));
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
                input.close();
    }

    /**
     * Iterates through the Key list and returns the first value for whose
     * key the given string contains.  Returns "unknown" if no key is contained
     * in the string.
     * @param s String being searched for a key.
     * @return Value for key found in string, otherwise "unknown"
     */
    String getValue (String s) {
        if (s == null) {
            return  null;
        }
      int i, j = attVec.size();
      for (i = 0; i < j; i++) {
        String temp[] = (String[])attVec.elementAt(i);
        if (s.indexOf(temp[0]) > -1) {
            return  temp[1];
        }
      }
      return  "unknown";
    }
  }
}



