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

import org.jasig.portal.services.LogService;
import org.jasig.portal.StylesheetSet;
import org.jasig.portal.UtilitiesBean;
import org.jasig.portal.PortalException;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.BrowserInfo;
import org.apache.xalan.xslt.*;
import org.apache.xerces.parsers.SAXParser;
import java.io.File;
import java.io.StringReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Enumeration;
import java.net.URL;
import org.xml.sax.DocumentHandler;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * <p>This utility provides methods for transforming XML documents
 * via XSLT. It takes advantage of Xalan's ability to pre-compile
 * stylehseets into StylesheetRoot objects.  The first time a transform
 * is requested, a stylesheet is compiled and cached.</p>
 * <p>None of the method signatures in this class should contain
 * classes specific to a particular XSLT engine, e.g. Xalan, or
 * XML parser, e.g. Xerces.</p>
 * <p>Typical usage:</p>
 * <p><code>
 * XSLT xslt = new XSLT();
 * xslt.setXML("/portal/docs/myXMLDoc.xml");
 * xslt.setSSL("/portal/stylesheets/myChannel.ssl", "aTitle", runtimeData.getBrowserInfo());
 * xslt.setTarget(out);
 * xslt.setStylesheetParameter("param1Name", "param1Value");
 * xslt.setStylesheetParameter("param2Name", "param2Value");
 * xslt.transform();
 * </code></p>
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
  private static final Hashtable stylesheetRootCache = new Hashtable(); // Consider changing to org.jasig.portal.utils.SmartCache
  private static final Hashtable stylesheetSetCache = new Hashtable();  // Consider changing to org.jasig.portal.utils.SmartCache

  private XSLTProcessor processor;
  private XSLTInputSource xmlInputSource;
  private XSLTInputSource xslInputSource;
  private XSLTResultTarget resultTarget;
  private HashMap stylesheetParams;
  
  /**
   * Constructs an XSLT object. 
   */  
  public XSLT () {
    processor = XSLTProcessorFactory.getProcessor(new org.apache.xalan.xpath.xdom.XercesLiaison());
    xmlInputSource = new XSLTInputSource();
    xslInputSource = new XSLTInputSource();
    resultTarget = new XSLTResultTarget();
    stylesheetParams = new HashMap();
  }

  /**
   * Configures the xml source.
   * @param xml a string representing the xml document
   */  
  public void setXML(String xml) {
    xmlInputSource.setCharacterStream(new StringReader(xml));
  }
  
  /**
   * Configures the xml source.
   * @param xml a node representing the xml document
   */  
  public void setXML(Node xml) {
    xmlInputSource.setNode(xml);
  }

  /**
   * Configures the xsl source.
   * @param xslUri the URL of an XSLT stylesheet
   */    
  public void setXSL(String xslUri) {
    xslInputSource.setSystemId(xslUri);
  }
  
  /**
   * Configures the xsl source by choosing the appropriate stylesheet from
   * the provided stylesheet list file.
   * @param sslUri the URL of the stylesheet list file
   * @param stylesheetTitle the title of a stylesheet within the stylesheet list file
   * @param browserInfo the browser info object
   * @throws org.jasig.portal.PortalException
   */  
  public void setSSL(String sslUri, String stylesheetTitle, BrowserInfo browserInfo) throws PortalException {
    StylesheetSet set = getStylesheetSet(sslUri);
    set.setMediaProps(mediaProps);
    String xslUri = set.getStylesheetURI(stylesheetTitle, browserInfo);
    setXSL(xslUri);    
  }
  
  /**
   * Configures the xsl source by choosing the appropriate stylesheet from
   * the provided stylesheet list file.
   * @param sslUri the URL of the stylesheet list file
   * @param browserInfo the browser info object
   * @throws org.jasig.portal.PortalException
   */  
  public void setSSL(String sslUri, BrowserInfo browserInfo) throws PortalException {
    setSSL(sslUri, (String)null, browserInfo);
  }
  
  /**
   * Configures the xslt target.
   * @param documentHandler the document handler
   */  
  public void setTarget(DocumentHandler documentHandler) {
    resultTarget.setDocumentHandler(documentHandler);
  }
  
  /**
   * Configures the xslt target.
   * @param fileName a file name
   */  
  public void setTarget(String fileName) {
    resultTarget.setFileName(fileName);
  }
  
  /**
   * Sets all the stylesheet parameters at once.
   * @param stylesheetParameters a Hashtable of stylesheet parameters
   */  
  public void setStylesheetParameters(Hashtable stylesheetParameters) {
    stylesheetParams.putAll(stylesheetParameters);
  }

  /**
   * Sets all the stylesheet parameters at once.
   * @param stylesheetParameters a HashMap of stylesheet parameters
   */  
  public void setStylesheetParameters(HashMap stylesheetParameters) {
    stylesheetParams = stylesheetParameters;
  }  
  
  /**
   * Sets all the stylesheet parameters at once.
   * @param stylesheetParameters a Hashtable of stylesheet parameters
   */  
  public void setStylesheetParameter(String name, String value) {
    stylesheetParams.put(name, value);
  }
  
  /**
   * Performs a transformation.  Assumes that the XML, XSL, and result targets
   * have already been set.
   * @throws org.jasig.portal.PortalException
   */  
  public void transform() throws PortalException {
    try {
      StylesheetRoot stylesheetRoot = getStylesheetRoot(xslInputSource.getSystemId());
      processor.reset();
      setStylesheetParams(processor, stylesheetParams);
      stylesheetRoot.process(processor, xmlInputSource, resultTarget);
    } catch (Exception e) {
      throw new GeneralRenderingException(e.getMessage());
    }
  }
  
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
   * @throws org.jasig.portal.PortalException
   * @deprecated replaced by {@link #transform(String, URL, DocumentHandler, Hashtable, String, BrowserInfo)}
   */
  public static void transform (String xml, URL sslUri, DocumentHandler out, Hashtable stylesheetParams, String stylesheetTitle, String media) throws SAXException, IOException, PortalException {
    XSLTInputSource xmlSource = new XSLTInputSource(new StringReader(xml));
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    StylesheetSet set = getStylesheetSet(sslUri.toExternalForm());
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
   * @param out a document handler
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   * @param stylesheetTitle the title that identifies the stylesheet in the stylesheet list file (.ssl), <code>null</code> if no title
   * @param browserInfo the browser information
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.PortalException
   */
  public static void transform (String xml, URL sslUri, DocumentHandler out, Hashtable stylesheetParams, String stylesheetTitle, BrowserInfo browserInfo) throws SAXException, IOException, PortalException {
    XSLTInputSource xmlSource = new XSLTInputSource(new StringReader(xml));
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    StylesheetSet set = getStylesheetSet(sslUri.toExternalForm());
    set.setMediaProps(mediaProps);
    XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
    StylesheetRoot stylesheetRoot = getStylesheetRoot(set.getStylesheetURI(stylesheetTitle, browserInfo));
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
   * @throws org.jasig.portal.PortalException
   * @deprecated replaced by {@link #transform(String, URL, StringWriter, Hashtable, String, BrowserInfo)}
   */
  public static void transform (String xml, URL sslUri, StringWriter out, Hashtable stylesheetParams, String stylesheetTitle, String media) throws SAXException, IOException, PortalException {
    XSLTInputSource xmlSource = new XSLTInputSource(new StringReader(xml));
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    StylesheetSet set = getStylesheetSet(sslUri.toExternalForm());
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
   * Performs an XSL transformation. Accepts stylesheet parameters
   * (key, value pairs) stored in a Hashtable.
   * @param xml a string representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a StringWriter
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   * @param stylesheetTitle the title that identifies the stylesheet in the stylesheet list file (.ssl), <code>null</code> if no title
   * @param browserInfo the browser information
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.PortalException
   * @deprecated the preferred way to use org.jasig.portal.utils.XSLT is to instatiate it, call its set methods and then {@link #transform()}
   */
  public static void transform (String xml, URL sslUri, StringWriter out, Hashtable stylesheetParams, String stylesheetTitle, BrowserInfo browserInfo) throws SAXException, IOException, PortalException {
    XSLTInputSource xmlSource = new XSLTInputSource(new StringReader(xml));
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    StylesheetSet set = getStylesheetSet(sslUri.toExternalForm());
    set.setMediaProps(mediaProps);
    XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
    StylesheetRoot stylesheetRoot = getStylesheetRoot(set.getStylesheetURI(stylesheetTitle, browserInfo));
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
   * @throws org.jasig.portal.PortalException
   * @deprecated replaced by {@link #transform(String, URL, DocumentHandler, String, String)}
   */
  public static void transform (String xml, URL sslUri, DocumentHandler out, String stylesheetTitle, String media) throws SAXException, IOException, PortalException {
    transform(xml, sslUri, out, (Hashtable)null, stylesheetTitle, media);
  }

  /**
   * Performs an XSL transformation.
   * @param xml a string representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a document handler
   * @param stylesheetTitle the title that identifies the stylsheet in the stylesheet list file (.ssl)
   * @param browserInfo the browser information
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.PortalException
   * @deprecated the preferred way to use org.jasig.portal.utils.XSLT is to instatiate it, call its set methods and then {@link #transform()}
   */
  public static void transform (String xml, URL sslUri, DocumentHandler out, String stylesheetTitle, BrowserInfo browserInfo) throws SAXException, IOException, PortalException {
    transform(xml, sslUri, out, (Hashtable)null, stylesheetTitle, browserInfo);
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
   * @throws org.jasig.portal.PortalException
   * @deprecated replaced by {@link #transform(String, URL, DocumentHandler, Hashtable, String)}
   */
  public static void transform (String xml, URL sslUri, DocumentHandler out, Hashtable stylesheetParams, String media) throws SAXException, IOException, PortalException {
    transform(xml, sslUri, out, stylesheetParams, (String)null, media);
  }

  /**
   * Performs an XSL transformation. Accepts stylesheet parameters
   * (key, value pairs) stored in a Hashtable.
   * @param xml a string representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a document handler
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   * @param browserInfo the browser information
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.PortalException
   * @deprecated the preferred way to use org.jasig.portal.utils.XSLT is to instatiate it, call its set methods and then {@link #transform()}
   */
  public static void transform (String xml, URL sslUri, DocumentHandler out, Hashtable stylesheetParams, BrowserInfo browserInfo) throws SAXException, IOException, PortalException {
    transform(xml, sslUri, out, stylesheetParams, (String)null, browserInfo);
  }  
  
  /**
   * Performs an XSL transformation.
   * @param xml a string representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a document handler
   * @param media the media type
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.PortalException
   * @deprecated replaced by {@link #transform(String, URL, DocumentHandler, String)}
   */
  public static void transform (String xml, URL sslUri, DocumentHandler out, String media) throws SAXException, IOException, PortalException {
    transform(xml, sslUri, out, (Hashtable)null, (String)null, media);
  }

  /**
   * Performs an XSL transformation.
   * @param xml a string representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a document handler
   * @param browserInfo the browser information
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.PortalException
   * @deprecated the preferred way to use org.jasig.portal.utils.XSLT is to instatiate it, call its set methods and then {@link #transform()}
   */
  public static void transform (String xml, URL sslUri, DocumentHandler out, BrowserInfo browserInfo) throws SAXException, IOException, PortalException {
    transform(xml, sslUri, out, (Hashtable)null, (String)null, browserInfo);
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
   * @throws org.jasig.portal.PortalException
   * @deprecated replaced by {@link #transform(Document, URL, DocumentHandler, Hashtable, String, String)}
   */
  public static void transform (Document xmlDoc, URL sslUri, DocumentHandler out, Hashtable stylesheetParams, String stylesheetTitle, String media) throws SAXException, IOException, PortalException {
    XSLTInputSource xmlSource = new XSLTInputSource(xmlDoc);
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    StylesheetSet set = getStylesheetSet(sslUri.toExternalForm());
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
   * @param out a document handler
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   * @param stylesheetTitle the title that identifies the stylesheet in the stylesheet list file (.ssl), <code>null</code> if no title
   * @param browserInfo the browser information
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.PortalException
   * @deprecated the preferred way to use org.jasig.portal.utils.XSLT is to instatiate it, call its set methods and then {@link #transform()}
   */
  public static void transform (Document xmlDoc, URL sslUri, DocumentHandler out, Hashtable stylesheetParams, String stylesheetTitle, BrowserInfo browserInfo) throws SAXException, IOException, PortalException {
    XSLTInputSource xmlSource = new XSLTInputSource(xmlDoc);
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    StylesheetSet set = getStylesheetSet(sslUri.toExternalForm());
    set.setMediaProps(mediaProps);
    XSLTProcessor processor = XSLTProcessorFactory.getProcessor(new org.apache.xalan.xpath.xdom.XercesLiaison());
    StylesheetRoot stylesheetRoot = getStylesheetRoot(set.getStylesheetURI(stylesheetTitle, browserInfo));
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
   * @throws org.jasig.portal.PortalException
   * @deprecated replaced by {@link #transform(Document, URL, StringWriter, Hashtable, String, String)}
   */
  public static void transform (Document xmlDoc, URL sslUri, StringWriter out, Hashtable stylesheetParams, String stylesheetTitle, String media) throws SAXException, IOException, PortalException {
    XSLTInputSource xmlSource = new XSLTInputSource(xmlDoc);
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    StylesheetSet set = getStylesheetSet(sslUri.toExternalForm());
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
   * @param browserInfo the browser information
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.PortalException
   * @deprecated the preferred way to use org.jasig.portal.utils.XSLT is to instatiate it, call its set methods and then {@link #transform()}
   */
  public static void transform (Document xmlDoc, URL sslUri, StringWriter out, Hashtable stylesheetParams, String stylesheetTitle, BrowserInfo browserInfo) throws SAXException, IOException, PortalException {
    XSLTInputSource xmlSource = new XSLTInputSource(xmlDoc);
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);
    StylesheetSet set = getStylesheetSet(sslUri.toExternalForm());
    set.setMediaProps(mediaProps);
    XSLTProcessor processor = XSLTProcessorFactory.getProcessor(new org.apache.xalan.xpath.xdom.XercesLiaison());
    StylesheetRoot stylesheetRoot = getStylesheetRoot(set.getStylesheetURI(stylesheetTitle, browserInfo));
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
   * @throws org.jasig.portal.PortalException
   * @deprecated replaced by {@link #transform(Document, URL, DocumentHandler, String, String)}
   */
  public static void transform (Document xmlDoc, URL sslUri, DocumentHandler out, String stylesheetTitle, String media) throws SAXException, IOException, PortalException {
    transform(xmlDoc, sslUri, out, (Hashtable)null, stylesheetTitle, media);
  }

  /**
   * Performs an XSL transformation.
   * @param xmlDoc a DOM object representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a document handler
   * @param stylesheetTitle the title that identifies the stylsheet in the stylesheet list file (.ssl)
   * @param browserInfo the browser information
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.PortalException
   * @deprecated the preferred way to use org.jasig.portal.utils.XSLT is to instatiate it, call its set methods and then {@link #transform()}
   */
  public static void transform (Document xmlDoc, URL sslUri, DocumentHandler out, String stylesheetTitle, BrowserInfo browserInfo) throws SAXException, IOException, PortalException {
    transform(xmlDoc, sslUri, out, (Hashtable)null, stylesheetTitle, browserInfo);
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
   * @throws org.jasig.portal.PortalException
   * @deprecated replaced by {@link #transform(Document, URL, DocumentHandler, Hashtable, String)}
   */
  public static void transform (Document xmlDoc, URL sslUri, DocumentHandler out, Hashtable stylesheetParams, String media) throws SAXException, IOException, PortalException {
    transform(xmlDoc, sslUri, out, stylesheetParams, (String)null, media);
  }

  /**
   * Performs an XSL transformation. Accepts stylesheet parameters
   * (key, value pairs) stored in a Hashtable.
   * @param xmlDoc a DOM object representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a document handler
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   * @param browserInfo the browser information
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.PortalException
   * @deprecated the preferred way to use org.jasig.portal.utils.XSLT is to instatiate it, call its set methods and then {@link #transform()}
   */
  public static void transform (Document xmlDoc, URL sslUri, DocumentHandler out, Hashtable stylesheetParams, BrowserInfo browserInfo) throws SAXException, IOException, PortalException {
    transform(xmlDoc, sslUri, out, stylesheetParams, (String)null, browserInfo);
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
   * @throws org.jasig.portal.PortalException
   * @deprecated replaced by {@link #transform(Document, URL, StringWriter, Hashtable, String)}
   */
  public static void transform (Document xmlDoc, URL sslUri, StringWriter out, Hashtable stylesheetParams, String media) throws SAXException, IOException, PortalException {
    transform(xmlDoc, sslUri, out, stylesheetParams, (String)null, media);
  }

  /**
   * Performs an XSL transformation. Accepts stylesheet parameters
   * (key, value pairs) stored in a Hashtable.
   * @param xmlDoc a DOM object representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a StringWriter
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   * @param browserInfo the browser information
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.PortalException
   * @deprecated the preferred way to use org.jasig.portal.utils.XSLT is to instatiate it, call its set methods and then {@link #transform()}
   */
  public static void transform (Document xmlDoc, URL sslUri, StringWriter out, Hashtable stylesheetParams, BrowserInfo browserInfo) throws SAXException, IOException, PortalException {
    transform(xmlDoc, sslUri, out, stylesheetParams, (String)null, browserInfo);
  }  
  
  /**
   * Performs an XSL transformation.
   * @param xmlDoc a DOM object representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a document handler
   * @param media the media type
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.PortalException
   * @deprecated replaced by {@link #transform(Document, URL, DocumentHandler, String)}
   */
  public static void transform (Document xmlDoc, URL sslUri, DocumentHandler out, String media) throws SAXException, IOException, PortalException {
    transform(xmlDoc, sslUri, out, (Hashtable)null, (String)null, media);
  }

  /**
   * Performs an XSL transformation.
   * @param xmlDoc a DOM object representing the xml document
   * @param sslUri the URI of the stylesheet list file (.ssl)
   * @param out a document handler
   * @param browserInfo the browser information
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   * @throws org.jasig.portal.PortalException
   * @deprecated the preferred way to use org.jasig.portal.utils.XSLT is to instatiate it, call its set methods and then {@link #transform()}
   */
  public static void transform (Document xmlDoc, URL sslUri, DocumentHandler out, BrowserInfo browserInfo) throws SAXException, IOException, PortalException {
    transform(xmlDoc, sslUri, out, (Hashtable)null, (String)null, browserInfo);
  }  
  
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
   * @deprecated the preferred way to use org.jasig.portal.utils.XSLT is to instatiate it, call its set methods and then {@link #transform()}
   */
  public static void transform (String xml, URL xslUri, DocumentHandler out, Hashtable stylesheetParams) throws SAXException, IOException, ResourceMissingException {
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
   * @deprecated the preferred way to use org.jasig.portal.utils.XSLT is to instatiate it, call its set methods and then {@link #transform()}
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
   * @deprecated the preferred way to use org.jasig.portal.utils.XSLT is to instatiate it, call its set methods and then {@link #transform()}
   */
  public static void transform (Document xmlDoc, URL xslUri, DocumentHandler out, Hashtable stylesheetParams) throws SAXException, IOException, ResourceMissingException {
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
   * @deprecated the preferred way to use org.jasig.portal.utils.XSLT is to instatiate it, call its set methods and then {@link #transform()}
   */
  public static void transform (Document xmlDoc, URL xslUri, StringWriter out, Hashtable stylesheetParams) throws SAXException, IOException, ResourceMissingException {
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
   * @deprecated the preferred way to use org.jasig.portal.utils.XSLT is to instatiate it, call its set methods and then {@link #transform()}
   */
  public static void transform (Document xmlDoc, URL xslUri, DocumentHandler out) throws SAXException, IOException, ResourceMissingException {
    transform(xmlDoc, xslUri, out, (Hashtable)null);
  }
  
  /**
   * Extracts name/value pairs from a Hashtable and uses them to create stylesheet parameters
   * @param processor the XSLT processor
   * @param stylesheetParams name/value pairs used as stylesheet parameters
   * @deprecated replaced by {@link #setStylesheetParams(XSLTProcessor, HashMap)}
   */
  private static void setStylesheetParams (XSLTProcessor processor, Hashtable stylesheetParams) {
    if (stylesheetParams != null) {
      HashMap stylesheetParamsHashMap = new HashMap();
      stylesheetParamsHashMap.putAll(stylesheetParams);
      setStylesheetParams(processor, stylesheetParamsHashMap);
    }
  }

  /**
   * Extracts name/value pairs from a Hashtable and uses them to create stylesheet parameters
   * @param processor the XSLT processor
   * @param stylesheetParams name/value pairs used as stylesheet parameters
   */
  private static void setStylesheetParams (XSLTProcessor processor, HashMap stylesheetParams) {
    if (stylesheetParams != null) {      
      Iterator iterator = stylesheetParams.keySet().iterator();
      while (iterator.hasNext()) {
        String key = (String)iterator.next();
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
   * @throws SAXException
   * @throws ResourceMissingException
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
        LogService.instance().log(LogService.INFO, "Caching StylesheetRoot for: " + stylesheetURI);
      }
    }
    return  stylesheetRoot;
  }
  
  /**
   * This method caches compiled stylesheet set objects, keyed by the stylesheet list's URI.
   * @param stylesheetListURI the URI of the XSLT stylesheet list file (.ssl)
   * @return the StlyesheetSet object
   * @throws PortalException
   */
  public static StylesheetSet getStylesheetSet (String stylesheetListURI) throws PortalException {
    // First, check the cache...
    StylesheetSet stylesheetSet = (StylesheetSet)stylesheetSetCache.get(stylesheetListURI);
    if (stylesheetSet == null) {
      // Get the StylesheetSet and cache it
      stylesheetSet = new StylesheetSet(stylesheetListURI);
      if (cacheEnabled) {
        stylesheetSetCache.put(stylesheetListURI, stylesheetSet);
        LogService.instance().log(LogService.INFO, "Caching StylesheetSet for: " + stylesheetListURI);
      }
    }
    return  stylesheetSet;
  }  
  
  /**
   * Returns a stylesheet URI.
   * @param sslUri the stylesheet list file URI
   * @param browserInfo the browser information
   * @return the stylesheet URI as a string
   * @throws org.jasig.portal.PortalException
   */
  public static String getStylesheetURI (String sslUri, BrowserInfo browserInfo) throws PortalException {
    StylesheetSet set = getStylesheetSet(sslUri);
    String xslUri = set.getStylesheetURI(browserInfo);
    return  xslUri;
  }  
}



