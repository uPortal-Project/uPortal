/**
 * Copyright (c) 2001 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.utils;

import org.jasig.portal.*;
import org.apache.xalan.xslt.*;
import java.io.File;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Enumeration;
import org.xml.sax.DocumentHandler;

/**
 * This utility provides methods for transforming XML documents
 * via XSLT. It takes advantage of Xalan's ability to pre-compile
 * stylehseets into StylesheetRoot objects.  The first time a transform
 * is requested, a stylesheet is compiled and cached.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class XSLT
{
  private static final String mediaProps = UtilitiesBean.getPortalBaseDir() + "properties" + File.separator + "media.properties";
  private static Hashtable stylesheetRootCache = new Hashtable();

  /**
   * Performs an XSL transformation.
   * @param out a document handler
   * @param media the media type
   * @param xml a string representing the xml document
   * @param sslUrl the URL of the stylesheet list file (.ssl)
   * @param stylesheetTitle the title that identifies the stylsheet in the stylesheet list file (.ssl)
   */
  public static void transform (DocumentHandler out, String media, String xml, String sslUrl, String stylesheetTitle) throws org.xml.sax.SAXException, java.io.IOException
  {
    transform(out, media, xml, sslUrl, stylesheetTitle, null);
  }

  /**
   * Performs an XSL transformation.
   * @param out a document handler
   * @param media the media type
   * @param xml a string representing the xml document
   * @param sslUrl the URL of the stylesheet list file (.ssl)
   */
  public static void transform (DocumentHandler out, String media, String xml, String sslUrl) throws org.xml.sax.SAXException, java.io.IOException
  {
    transform(out, media, xml, sslUrl, null, null);
  }

  /**
   * Performs an XSL transformation. Accepts stylesheet parameters
   * (key, value pairs) stored in a Hashtable.
   * @param out a document handler
   * @param media the media type
   * @param xml a string representing the xml document
   * @param sslUrl the URL of the stylesheet list file (.ssl)
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   */
  public static void transform (DocumentHandler out, String media, String xml, String sslUrl, Hashtable stylesheetParams) throws org.xml.sax.SAXException, java.io.IOException
  {
    transform(out, media, xml, sslUrl, null, stylesheetParams);
  }

  /**
   * Performs an XSL transformation. Accepts stylesheet parameters
   * (key, value pairs) stored in a Hashtable.
   * @param out a document handler
   * @param media the media type
   * @param xml a string representing the xml document
   * @param sslUrl the URL of the stylesheet list file (.ssl)
   * @param stylesheetTitle the title that identifies the stylesheet in the stylesheet list file (.ssl), <code>null</code> if no title
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   */
  public static void transform (DocumentHandler out, String media, String xml, String sslUrl, String stylesheetTitle, Hashtable stylesheetParams) throws org.xml.sax.SAXException, java.io.IOException
  {
    XSLTInputSource xmlSource = new XSLTInputSource(new StringReader(xml));
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    StylesheetSet set = new StylesheetSet(sslUrl);
    set.setMediaProps (mediaProps);

    XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
    StylesheetRoot stylesheetRoot = getStylesheetRoot(set.getStylesheetURI(stylesheetTitle, media));
    processor.reset();
    setStylesheetParams(processor, stylesheetParams);
    stylesheetRoot.process(processor, xmlSource, xmlResult);
  }

  private static void setStylesheetParams (XSLTProcessor processor, Hashtable stylesheetParams)
  {
    if (stylesheetParams != null)
    {
      Enumeration e = stylesheetParams.keys();

      while (e.hasMoreElements())
      {
        String key = (String)e.nextElement();
        Object o = stylesheetParams.get(key);

        if (o instanceof String)
          processor.setStylesheetParam(key, processor.createXString((String)o));
        else if (o instanceof Boolean)
          processor.setStylesheetParam(key, processor.createXBoolean(((Boolean)o).booleanValue()));
        else if (o instanceof Double)
          processor.setStylesheetParam(key, processor.createXNumber(((Double)o).doubleValue()));
      }
    }
  }

  private static StylesheetRoot getStylesheetRoot (String stylesheetURI) throws org.xml.sax.SAXException
  {
    // First, check the cache...
    StylesheetRoot stylesheetRoot = (StylesheetRoot)stylesheetRootCache.get(stylesheetURI);

    if (stylesheetRoot == null)
    {
      // Get the StylesheetRoot and cache it
      XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
      stylesheetRoot = processor.processStylesheet(stylesheetURI);
      stylesheetRootCache.put(stylesheetURI, stylesheetRoot);
      Logger.log(Logger.INFO, "Caching StylesheetRoot for: " + stylesheetURI);
    }

    return stylesheetRoot;
  }
}
