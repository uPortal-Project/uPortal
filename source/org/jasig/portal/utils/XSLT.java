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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal.utils;

import  org.jasig.portal.*;
import  org.apache.xalan.xslt.*;
import  org.apache.xerces.parsers.SAXParser;
import  java.io.File;
import  java.io.StringReader;
import  java.io.IOException;
import  java.io.StringWriter;
import  java.util.Hashtable;
import  java.util.Enumeration;
import  java.net.URL;
import  org.xml.sax.DocumentHandler;
import  org.xml.sax.SAXException;
import  org.w3c.dom.Document;


/**
 * <p>This utility provides methods for transforming XML documents
 * via XSLT. It takes advantage of Xalan's ability to pre-compile
 * stylehseets into StylesheetRoot objects.  The first time a transform
 * is requested, a stylesheet is compiled and cached.</p>
 * <p>None of the method signatures in this class should contain
 * classes specific to a particular XSLT engine, e.g. Xalan, or
 * XML parser, e.g. Xerces.</p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class XSLT {
  // cacheEnabled flag should be set to true for production to
  // ensure that pre-compiled stylesheets are cached
  // I'm hoping that this setting can come from some globally-set
  // property.  I'll leave this for later.
  // Until then, it'll stay checked in set to false so that
  // developers can simply reload the page to see the effect of
  // a modified XSLT stylesheet
  private static boolean cacheEnabled = false;
  private static final String mediaProps = UtilitiesBean.getPortalBaseDir() + "properties" + File.separator + "media.properties";
  private static Hashtable stylesheetRootCache = new Hashtable();

  /**
   * Performs an XSL transformation. Accepts stylesheet parameters
   * (key, value pairs) stored in a Hashtable.
   * @param xml a string representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a document handler
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   * @param stylesheetTitle the title that identifies the stylesheet in the stylesheet list file (.ssl), <code>null</code> if no title
   * @param media the media type
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static void transform (String xml, URL sslUri, DocumentHandler out, Hashtable stylesheetParams, String stylesheetTitle,
      String media) throws SAXException, IOException, ResourceMissingException, GeneralRenderingException {
    XSLTInputSource xmlSource = new XSLTInputSource(new StringReader(xml));
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    StylesheetSet set = new StylesheetSet(sslUri.toExternalForm());
    set.setMediaProps(mediaProps);
    XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
    StylesheetRoot stylesheetRoot = getStylesheetRoot(set.getStylesheetURI(stylesheetTitle, media));
    processor.reset();
    setStylesheetParams(processor, stylesheetParams);
    stylesheetRoot.process(processor, xmlSource, xmlResult);
  }

  /**
   * Performs an XSL transformation. Accepts stylesheet parameters
   * (key, value pairs) stored in a Hashtable.
   * @param xml a string representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a StringWriter
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   * @param stylesheetTitle the title that identifies the stylesheet in the stylesheet list file (.ssl), <code>null</code> if no title
   * @param media the media type
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static void transform (String xml, URL sslUri, StringWriter out, Hashtable stylesheetParams, String stylesheetTitle,
      String media) throws SAXException, IOException, ResourceMissingException, GeneralRenderingException {
    XSLTInputSource xmlSource = new XSLTInputSource(new StringReader(xml));
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    StylesheetSet set = new StylesheetSet(sslUri.toExternalForm());
    set.setMediaProps(mediaProps);
    XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
    StylesheetRoot stylesheetRoot = getStylesheetRoot(set.getStylesheetURI(stylesheetTitle, media));
    processor.reset();
    setStylesheetParams(processor, stylesheetParams);
    // Make sure to generate XML in order to cache it
    stylesheetRoot.setOutputMethod("xml");
    // Process the XML/XSLT and store the result in the StringWriter
    stylesheetRoot.process(processor, xmlSource, xmlResult);
  }

  /**
   * Performs an XSL transformation.
   * @param xml a string representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a document handler
   * @param stylesheetTitle the title that identifies the stylsheet in the stylesheet list file (.ssl)
   * @param media the media type
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static void transform (String xml, URL sslUri, DocumentHandler out, String stylesheetTitle, String media) throws SAXException,
      IOException, ResourceMissingException, GeneralRenderingException {
    transform(xml, sslUri, out, (Hashtable)null, stylesheetTitle, media);
  }

  /**
   * Performs an XSL transformation. Accepts stylesheet parameters
   * (key, value pairs) stored in a Hashtable.
   * @param xml a string representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a document handler
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   * @param media the media type
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static void transform (String xml, URL sslUri, DocumentHandler out, Hashtable stylesheetParams, String media) throws SAXException,
      IOException, ResourceMissingException, GeneralRenderingException {
    transform(xml, sslUri, out, stylesheetParams, (String)null, media);
  }

  /**
   * Performs an XSL transformation.
   * @param xml a string representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a document handler
   * @param media the media type
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static void transform (String xml, URL sslUri, DocumentHandler out, String media) throws SAXException, IOException,
      ResourceMissingException, GeneralRenderingException {
    transform(xml, sslUri, out, (Hashtable)null, (String)null, media);
  }

  /**
   * Performs an XSL transformation. Accepts stylesheet parameters
   * (key, value pairs) stored in a Hashtable.
   * @param xmlDoc a DOM object representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a document handler
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   * @param stylesheetTitle the title that identifies the stylesheet in the stylesheet list file (.ssl), <code>null</code> if no title
   * @param media the media type
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static void transform (Document xmlDoc, URL sslUri, DocumentHandler out, Hashtable stylesheetParams, String stylesheetTitle,
      String media) throws SAXException, IOException, ResourceMissingException, GeneralRenderingException {
    XSLTInputSource xmlSource = new XSLTInputSource(xmlDoc);
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    StylesheetSet set = new StylesheetSet(sslUri.toExternalForm());
    set.setMediaProps(mediaProps);
    XSLTProcessor processor = XSLTProcessorFactory.getProcessor(new org.apache.xalan.xpath.xdom.XercesLiaison());
    StylesheetRoot stylesheetRoot = getStylesheetRoot(set.getStylesheetURI(stylesheetTitle, media));
    processor.reset();
    setStylesheetParams(processor, stylesheetParams);
    stylesheetRoot.process(processor, xmlSource, xmlResult);
  }

  /**
   * Performs an XSL transformation. Accepts stylesheet parameters
   * (key, value pairs) stored in a Hashtable.
   * @param xmlDoc a DOM object representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a string writer
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   * @param stylesheetTitle the title that identifies the stylesheet in the stylesheet list file (.ssl), <code>null</code> if no title
   * @param media the media type
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static void transform (Document xmlDoc, URL sslUri, StringWriter out, Hashtable stylesheetParams, String stylesheetTitle,
      String media) throws SAXException, IOException, ResourceMissingException, GeneralRenderingException {
    XSLTInputSource xmlSource = new XSLTInputSource(xmlDoc);
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    StylesheetSet set = new StylesheetSet(sslUri.toExternalForm());
    set.setMediaProps(mediaProps);
    XSLTProcessor processor = XSLTProcessorFactory.getProcessor(new org.apache.xalan.xpath.xdom.XercesLiaison());
    StylesheetRoot stylesheetRoot = getStylesheetRoot(set.getStylesheetURI(stylesheetTitle, media));
    processor.reset();
    setStylesheetParams(processor, stylesheetParams);
    stylesheetRoot.process(processor, xmlSource, xmlResult);
  }

  /**
   * Performs an XSL transformation.
   * @param xmlDoc a DOM object representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a document handler
   * @param stylesheetTitle the title that identifies the stylsheet in the stylesheet list file (.ssl)
   * @param media the media type
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static void transform (Document xmlDoc, URL sslUri, DocumentHandler out, String stylesheetTitle, String media) throws SAXException,
      IOException, ResourceMissingException, GeneralRenderingException {
    transform(xmlDoc, sslUri, out, (Hashtable)null, stylesheetTitle, media);
  }

  /**
   * Performs an XSL transformation. Accepts stylesheet parameters
   * (key, value pairs) stored in a Hashtable.
   * @param xmlDoc a DOM object representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a document handler
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   * @param media the media type
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static void transform (Document xmlDoc, URL sslUri, DocumentHandler out, Hashtable stylesheetParams, String media) throws SAXException,
      IOException, ResourceMissingException, GeneralRenderingException {
    transform(xmlDoc, sslUri, out, stylesheetParams, (String)null, media);
  }

  /**
   * Performs an XSL transformation. Accepts stylesheet parameters
   * (key, value pairs) stored in a Hashtable.
   * @param xmlDoc a DOM object representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a StringWriter
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   * @param media the media type
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static void transform (Document xmlDoc, URL sslUri, StringWriter out, Hashtable stylesheetParams, String media) throws SAXException,
      IOException, ResourceMissingException, GeneralRenderingException {
    transform(xmlDoc, sslUri, out, stylesheetParams, (String)null, media);
  }

  /**
   * Performs an XSL transformation.
   * @param xmlDoc a DOM object representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a document handler
   * @param media the media type
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static void transform (Document xmlDoc, URL sslUri, DocumentHandler out, String media) throws SAXException, IOException,
      ResourceMissingException, GeneralRenderingException {
    transform(xmlDoc, sslUri, out, (Hashtable)null, (String)null, media);
  }

  /**/
  /**
   * Performs an XSL transformation. Accepts stylesheet parameters
   * (key, value pairs) stored in a Hashtable.
   * @param xml a string representing the xml document
   * @param xslUri the URI of the stylesheet file (.xsl)
   * @param out a document handler
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static void transform (String xml, URL xslUri, DocumentHandler out, Hashtable stylesheetParams) throws SAXException,
      IOException, ResourceMissingException {
    XSLTInputSource xmlSource = new XSLTInputSource(new StringReader(xml));
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
    StylesheetRoot stylesheetRoot = getStylesheetRoot(xslUri.toExternalForm());
    processor.reset();
    setStylesheetParams(processor, stylesheetParams);
    stylesheetRoot.process(processor, xmlSource, xmlResult);
  }

  /**
   * Performs an XSL transformation.
   * @param xml a string representing the xml document
   * @param xslUri the URI of the stylesheet file
   * @param out a document handler
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static void transform (String xml, URL xslUri, DocumentHandler out) throws SAXException, IOException, ResourceMissingException {
    transform(xml, xslUri, out, (Hashtable)null);
  }

  /**
   * Performs an XSL transformation. Accepts stylesheet parameters
   * (key, value pairs) stored in a Hashtable.
   * @param xmlDoc a DOM object representing the xml document
   * @param xslUri the URI of the stylesheet file (.xsl)
   * @param out a document handler
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static void transform (Document xmlDoc, URL xslUri, DocumentHandler out, Hashtable stylesheetParams) throws SAXException,
      IOException, ResourceMissingException {
    XSLTInputSource xmlSource = new XSLTInputSource(xmlDoc);
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    XSLTProcessor processor = XSLTProcessorFactory.getProcessor(new org.apache.xalan.xpath.xdom.XercesLiaison());
    StylesheetRoot stylesheetRoot = getStylesheetRoot(xslUri.toExternalForm());
    processor.reset();
    setStylesheetParams(processor, stylesheetParams);
    stylesheetRoot.process(processor, xmlSource, xmlResult);
  }

  /**
   * Performs an XSL transformation. Accepts stylesheet parameters
   * (key, value pairs) stored in a Hashtable.
   * @param xmlDoc a DOM object representing the xml document
   * @param xslUri the URI of the stylesheet file (.xsl)
   * @param out a string writer
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static void transform (Document xmlDoc, URL xslUri, StringWriter out, Hashtable stylesheetParams) throws SAXException,
      IOException, ResourceMissingException {
    XSLTInputSource xmlSource = new XSLTInputSource(xmlDoc);
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    XSLTProcessor processor = XSLTProcessorFactory.getProcessor(new org.apache.xalan.xpath.xdom.XercesLiaison());
    StylesheetRoot stylesheetRoot = getStylesheetRoot(xslUri.toExternalForm());
    processor.reset();
    setStylesheetParams(processor, stylesheetParams);
    stylesheetRoot.process(processor, xmlSource, xmlResult);
  }

  /**
   * Performs an XSL transformation. Accepts stylesheet parameters
   * (key, value pairs) stored in a Hashtable.
   * @param xmlDoc a DOM object representing the xml document
   * @param xslUri the URI of the stylesheet file (.xsl)
   * @param out a document handler
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.ResourceMissingException
   */
  public static void transform (Document xmlDoc, URL xslUri, DocumentHandler out) throws SAXException, IOException, ResourceMissingException {
    transform(xmlDoc, xslUri, out, (Hashtable)null);
  }

  /**
   * put your documentation comment here
   * @param xmlDoc
   * @param stylesheetSet
   * @param out
   * @param stylesheetParams
   * @param stylesheetTitle
   * @param media
   * @exception SAXException, IOException, ResourceMissingException, GeneralRenderingException
   */
  public static void transform (Document xmlDoc, StylesheetSet stylesheetSet, DocumentHandler out, Hashtable stylesheetParams,
      String stylesheetTitle, String media) throws SAXException, IOException, ResourceMissingException, GeneralRenderingException {
    // Create the input source for the input xml
    XSLTInputSource xmlSource = new XSLTInputSource(xmlDoc);
    // Create the result target for the DocumentHandler
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    // Get an instance of the XSLT processor
    XSLTProcessor processor = XSLTProcessorFactory.getProcessor(new org.apache.xalan.xpath.xdom.XercesLiaison());
    // Reset the processor
    processor.reset();
    // Get the XSL stylesheet location
    StylesheetRoot stylesheetRoot = getStylesheetRoot(stylesheetSet.getStylesheetURI(stylesheetTitle, media));
    // Set the parameters for the transformation
    setStylesheetParams(processor, stylesheetParams);
    // Perform the transformation
    stylesheetRoot.process(processor, xmlSource, xmlResult);
  }

  /**
   * put your documentation comment here
   * @param processor
   * @param stylesheetParams
   */
  private static void setStylesheetParams (XSLTProcessor processor, Hashtable stylesheetParams) {
    if (stylesheetParams != null) {
      Enumeration e = stylesheetParams.keys();
      while (e.hasMoreElements()) {
        String key = (String)e.nextElement();
        Object o = stylesheetParams.get(key);
        if (o instanceof String) {
          processor.setStylesheetParam(key, processor.createXString((String)o));
        }
        else if (o.getClass().getName().equals("[Ljava.lang.String;")) {
          // This situation occurs for some requests from cell phones
          String[] sa = (String[])o;
          processor.setStylesheetParam(key, processor.createXString(sa[0]));
        }
        else if (o instanceof Boolean) {
          processor.setStylesheetParam(key, processor.createXBoolean(((Boolean)o).booleanValue()));
        }
        else if (o instanceof Double) {
          processor.setStylesheetParam(key, processor.createXNumber(((Double)o).doubleValue()));
        }
      }
    }
  }

  /**
   * This method caches compiled stylesheet objects, keyed by the stylesheet's URI.
   * @param stylesheetURI the URI of the XSLT stylesheet
   * @return the StlyesheetRoot object
   * @exception SAXException, ResourceMissingException
   */
  public static StylesheetRoot getStylesheetRoot (String stylesheetURI) throws SAXException, ResourceMissingException {
    // First, check the cache...
    StylesheetRoot stylesheetRoot = (StylesheetRoot)stylesheetRootCache.get(stylesheetURI);
    if (stylesheetRoot == null) {
      // Get the StylesheetRoot and cache it
      XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
      stylesheetRoot = processor.processStylesheet(stylesheetURI);
      if (cacheEnabled) {
        stylesheetRootCache.put(stylesheetURI, stylesheetRoot);
        Logger.log(Logger.INFO, "Caching StylesheetRoot for: " + stylesheetURI);
      }
    }
    return  stylesheetRoot;
  }
}



