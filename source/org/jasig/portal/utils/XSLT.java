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

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jasig.portal.BrowserInfo;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.PortalException;
import org.jasig.portal.PropertiesManager;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.StylesheetSet;
import org.jasig.portal.services.LogService;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * <p>This utility provides methods for transforming XML documents
 * via XSLT. It takes advantage of Xalan's ability to pre-compile
 * stylehseets into StylesheetRoot objects.  The first time a transform
 * is requested, a stylesheet is compiled and cached.</p>
 * <p>None of the method signatures in this class should contain
 * classes specific to a particular XSLT engine, e.g. Xalan, or
 * XML parser, e.g. Xerces.</p>
 * <p>The constructor for XSLT takes an instance of whatever class is requesting
 * the transformation.  XSLT uses this instance to locate resources relative
 * to the classpath.</p>
 * <p>Typical usage:</p>
 * <p><code><pre>
 * XSLT xslt = new XSLT(this);
 * xslt.setXML("myXMLDoc.xml");
 * xslt.setSSL("myChannel.ssl", "aTitle", runtimeData.getBrowserInfo());
 * xslt.setTarget(out);
 * xslt.setStylesheetParameter("param1Name", "param1Value");
 * xslt.setStylesheetParameter("param2Name", "param2Value");
 * xslt.transform();
 * </pre></code></p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */

public class XSLT {
  // These flags should be set to true for production to
  // ensure that pre-compiled stylesheets and stylesheet sets are cached.
  private static boolean stylesheetRootCacheEnabled = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.utils.XSLT.stylesheet_root_caching");
  private static boolean stylesheetSetCacheEnabled = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.utils.XSLT.stylesheet_set_caching");
  private static final String mediaProps = "/properties/media.properties";
  private static final Hashtable stylesheetRootCache = new Hashtable(); // Consider changing to org.jasig.portal.utils.SmartCache
  private static final Hashtable stylesheetSetCache = new Hashtable();  // Consider changing to org.jasig.portal.utils.SmartCache

  private static SAXTransformerFactory saxTFactory = null;

  private Object caller = null;
  private Source xmlSource;
  private Result xmlResult;
  private HashMap stylesheetParams;
  private String xslURI;


  /**
   * Constructs an XSLT object.
   */
  public XSLT (Object instance) {
    this.stylesheetParams = new HashMap();
    this.caller = instance;
  }

  public static SAXTransformerFactory getSAXTFactory() {
    if (saxTFactory == null) {
      // attempt to instantiate a sax transformer factory
      TransformerFactory tFactory = TransformerFactory.newInstance();
      if (tFactory instanceof SAXTransformerFactory) {
        saxTFactory = ((SAXTransformerFactory)tFactory);
      }
    }
    if (saxTFactory == null) {
      LogService.log(LogService.ERROR, "XSLT() : unable to instantiate SAX transformer ! Please make sure the TRAX implementation you're using supports SAX Transformers");
    }
    return saxTFactory;
  }

  /**
   * Configures the xml source.
   * @param xml a string representing the xml document
   */
  public void setXML(String xml) {
    xmlSource = new StreamSource(new StringReader(xml));
  }

  /**
   * Configures the xml source.
   * @param xml a node representing the xml document
   */
  public void setXML(Node xml) {
    xmlSource = new DOMSource(xml);
  }

  /**
   * Configures the xml source.
   * @param xml an input stream to the serialized xml source
   */
  public void setXML(java.io.InputStream is) {
    xmlSource = new StreamSource(is);
  }

  /**
   * Configures the xml source.
   * @param xml a File object representing the xml source
   */
  public void setXML(java.io.File file) {
    xmlSource = new StreamSource(file);
  }

  /**
   * Configures the xsl source.
   * @param xslUri the URL of an XSLT stylesheet
   */
  public void setXSL(String xslUri) throws PortalException {
    this.xslURI = ResourceLoader.getResourceAsURLString(caller.getClass(), xslUri);
  }

  /**
   * Configures the xsl source by choosing the appropriate stylesheet from
   * the provided stylesheet list file.
   * @param sslUri the URL of the stylesheet list file
   * @param stylesheetTitle the title of a stylesheet within the stylesheet list file
   * @param browserInfo the browser info object
   * @throws org.jasig.portal.PortalException
   */
  public void setXSL(String sslUri, String stylesheetTitle, BrowserInfo browserInfo) throws PortalException {
    StylesheetSet set = getStylesheetSet(ResourceLoader.getResourceAsURLString(caller.getClass(), sslUri));
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
  public void setXSL(String sslUri, BrowserInfo browserInfo) throws PortalException {
    setXSL(sslUri, (String)null, browserInfo);
  }

  /**
   * Configures the xslt target.
   * @param contentHandler the content handler
   */
  public void setTarget(ContentHandler contentHandler) {
    xmlResult=new SAXResult(contentHandler);
  }

  /**
   * Configures the xslt target.
   * @param os output stream
   */
  public void setTarget(java.io.OutputStream os) {
    xmlResult = new StreamResult(os);
  }

  /**
   * Configures the xslt target.
   * @param node target node
   */
  public void setTarget(org.w3c.dom.Node node) {
    xmlResult = new DOMResult(node);
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
      Transformer trans = getTransformer(this.xslURI);
      setStylesheetParams(trans, stylesheetParams);
      trans.transform(xmlSource, xmlResult);
    } catch (PortalException pe) {
      throw pe;
    } catch (Exception e) {
      throw new PortalException(e);
    }
  }


  /**
   * Performs an XSL transformation. Accepts stylesheet parameters
   * (key, value pairs) stored in a Hashtable.
   * @param xmlSource the source to be transformedn
   * @param xmlResult the result to be populated
   * @param stylesheetParams a Hashtable of key/value pairs or <code>null</code> if no parameters
   * @param xslURI the uri of the stylesheet to be used
   * @throws org.jasig.portal.PortalException if something goes wrong
   */
  public static void transform(Source xmlSource, Result xmlResult, Hashtable stylesheetParams, String xslURI) throws PortalException {
    try {
      Transformer trans = getTransformer(xslURI);
      setStylesheetParams(trans, stylesheetParams);
      trans.transform(xmlSource,xmlResult);
    } catch (PortalException pe) {
      throw pe;
    } catch (Exception e) {
      throw new PortalException(e);
    }
  }

  /**
   * Extracts name/value pairs from a Hashtable and uses them to create stylesheet parameters
   * @param processor the XSLT processor
   * @param stylesheetParams name/value pairs used as stylesheet parameters
   * @deprecated replaced by {@link #setStylesheetParams(XSLTProcessor, HashMap)}
   */
  private static void setStylesheetParams(Transformer transformer, Hashtable stylesheetParams) {
    if (stylesheetParams != null) {
      HashMap stylesheetParamsHashMap = new HashMap();
      stylesheetParamsHashMap.putAll(stylesheetParams);
      setStylesheetParams(transformer, stylesheetParamsHashMap);
    }
  }

  /**
   * Extracts name/value pairs from a Hashtable and uses them to create stylesheet parameters
   * @param processor the XSLT processor
   * @param stylesheetParams name/value pairs used as stylesheet parameters
   */
  private static void setStylesheetParams(Transformer transformer, HashMap stylesheetParams) {
    if (stylesheetParams != null) {
      Iterator iterator = stylesheetParams.keySet().iterator();
      while (iterator.hasNext()) {
        String key = (String)iterator.next();
        Object o = stylesheetParams.get(key);
        if (o.getClass().getName().equals("[Ljava.lang.String;")) {
          // This situation occurs for some requests from cell phones
          o = ((String[])o)[0];
        }
        transformer.setParameter(key,o);
      }
    }
  }

  /**
   * This method caches compiled stylesheet objects, keyed by the stylesheet's URI.
   * @param stylesheetURI the URI of the XSLT stylesheet
   * @return the StlyesheetRoot object
   * @throws SAXException
   */
  public static Templates getTemplates(String stylesheetURI) throws SAXException, PortalException {
    // First, check the cache...
    Templates temp = (Templates)stylesheetRootCache.get(stylesheetURI);
    if (temp == null) {
      // Get the Templates and cache them
      try
      {
        TemplatesHandler thand = getSAXTFactory().newTemplatesHandler();
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setContentHandler(thand);
        reader.parse(stylesheetURI);
        temp = thand.getTemplates();
        if (stylesheetRootCacheEnabled) {
          stylesheetRootCache.put(stylesheetURI, temp);
          LogService.log(LogService.INFO, "Caching templates for: " + stylesheetURI);
        }
      } catch (IOException ioe) {
        throw new ResourceMissingException(stylesheetURI, "Stylesheet", "Unable to read stylesheet from the specified location. Please check the stylesheet URL");
      } catch (TransformerConfigurationException tce) {
        LogService.log(LogService.ERROR, "XSLT::getTemplates() : unable to obtain TemplatesHandler due to TRAX misconfiguration!");
        throw new GeneralRenderingException("XSLT: current TRAX configuration does not allow for TemplateHandlers. Please reconfigure/reinstall your TRAX implementation.");
      } catch (SAXParseException px) {
        throw new GeneralRenderingException("XSLT:getTemplates(): SAXParseExeption: " +
        px.getMessage() + " line:" + px.getLineNumber() + " col:"+px.getColumnNumber());
      } catch (SAXException sx) {
        // Catch the sax exception so we can report line number info
        if ( null != sx.getException() && (sx.getException() instanceof TransformerException)) {
          TransformerException trx = (TransformerException)sx.getException();
          throw new GeneralRenderingException("XSLT.getTemplates():" + trx.getMessageAndLocation());
        }
        throw sx;
      }
    }
    return temp;
  }


  /**
   * This method returns a Transformer for a given stylesheet.
   * @param stylesheetURI the URI of the XSLT stylesheet
   * @return <code>Transformer</code>
   * @throws SAXException
   */
  public static Transformer getTransformer(String stylesheetURI) throws SAXException, PortalException {
    Transformer t = null;
    try {
      t = getTemplates(stylesheetURI).newTransformer();
    } catch (TransformerConfigurationException tce) {
      LogService.log(LogService.ERROR,"XSLT::getTransformer() : TRAX transformer is misconfigured : "+tce.getMessage());
    }
    return t;
  }

  /**
   * This method returns a TransformerHandler for a given stylesheet.
   * @param stylesheetURI the URI of the XSLT stylesheet
   * @return <code>Transformer</code>
   * @throws SAXException
   */
  public static TransformerHandler getTransformerHandler(String stylesheetURI) throws SAXException, PortalException {
      TransformerHandler th = null;
      try {
        th = getSAXTFactory().newTransformerHandler(getTemplates(stylesheetURI));
      } catch (TransformerConfigurationException tce) {
        LogService.log(LogService.ERROR,"XSLT::getTransformerHandler() : TRAX transformer is misconfigured : "+tce.getMessage());
      }
      return th;
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
      if (stylesheetSetCacheEnabled) {
        stylesheetSetCache.put(stylesheetListURI, stylesheetSet);
        LogService.log(LogService.INFO, "Caching StylesheetSet for: " + stylesheetListURI);
      }
    }
    return stylesheetSet;
  }

  /**
   * Returns a stylesheet URI exactly as it appears in a stylesheet list file.
   * @param sslUri the stylesheet list file URI
   * @param browserInfo the browser information
   * @return the stylesheet URI as a string
   * @throws org.jasig.portal.PortalException
   */
  public static String getStylesheetURI (String sslUri, BrowserInfo browserInfo) throws PortalException {
    StylesheetSet set = getStylesheetSet(sslUri);
    String xslUri = set.getStylesheetURI(browserInfo);
    return xslUri;
  }

  /**
   * Returns a stylesheet URI exactly as it appears in a stylesheet list file.
   * @param sslUri the stylesheet list file URI
   * @param title the stylesheet title
   * @param browserInfo the browser information
   * @return the stylesheet URI as a string
   * @throws org.jasig.portal.PortalException
   */
  public static String getStylesheetURI (String sslUri, String title, BrowserInfo browserInfo) throws PortalException {
    StylesheetSet set = getStylesheetSet(sslUri);
    String xslUri = set.getStylesheetURI(title, browserInfo);
    return xslUri;
  }
}



