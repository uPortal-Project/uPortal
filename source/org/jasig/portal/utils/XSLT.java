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
 * via XSLT.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class XSLT
{
  private static final String mediaProps = UtilitiesBean.getPortalBaseDir() + "properties" + File.separator + "media.properties";

  /**
   * Transforms an xml document.
   * @param out a document handler
   * @param media
   * @param xml
   * @param sslUrl
   * @param stylesheetName
   */
  public static void transform (DocumentHandler out, String media, String xml, String sslUrl, String stylesheetName) throws org.xml.sax.SAXException
  {
    StylesheetSet set = new StylesheetSet(sslUrl);
    set.setMediaProps (mediaProps);
    XSLTInputSource xmlSource = new XSLTInputSource(new StringReader(xml));
    XSLTInputSource xslSource = set.getStylesheet(stylesheetName, media);
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
    processor.process (xmlSource, xslSource, xmlResult);
  }

  /**
   * Transforms an xml document. Accepts stylesheet parameters
   * (name, value pairs) stored in a Hashtable.
   * @param out a document handler
   * @param media
   * @param xml
   * @param sslUrl
   * @param stylesheetName
   * @param stylesheetParams
   */
  public static void transform (DocumentHandler out, String media, String xml, String sslUrl, String stylesheetName, Hashtable stylesheetParams) throws org.xml.sax.SAXException
  {
    StylesheetSet set = new StylesheetSet(sslUrl);
    set.setMediaProps (mediaProps);
    XSLTInputSource xmlSource = new XSLTInputSource(new StringReader(xml));
    XSLTInputSource xslSource = set.getStylesheet(stylesheetName, media);
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
    setStylesheetParams(processor, stylesheetParams);
    processor.process (xmlSource, xslSource, xmlResult);
  }

  private static void setStylesheetParams (XSLTProcessor processor, Hashtable stylesheetParams)
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
