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
import java.io.BufferedInputStream;
import java.util.*;

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
import org.jasig.portal.i18n.LocaleAwareXSLT;
import org.jasig.portal.services.LogService;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This utility provides methods for transforming XML documents
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
 * 
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class XSLT {
  // These flags should be set to true for production to
  // ensure that pre-compiled stylesheets and stylesheet sets are cached.
  protected static boolean stylesheetRootCacheEnabled = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.utils.XSLT.stylesheet_root_caching");
  protected static boolean stylesheetSetCacheEnabled = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.utils.XSLT.stylesheet_set_caching");
  protected static final String mediaProps = "/properties/media.properties";
  protected static final Hashtable stylesheetRootCache = new Hashtable(); // Consider changing to org.jasig.portal.utils.SmartCache
  protected static final Hashtable stylesheetSetCache = new Hashtable();  // Consider changing to org.jasig.portal.utils.SmartCache

  private static SAXTransformerFactory saxTFactory = null;

  protected Object caller = null;
  protected Source xmlSource;
  protected Result xmlResult;
  protected HashMap stylesheetParams;
  protected String xslURI;
  protected ResourceBundle l18n;


  /**
   * Constructs an XSLT object. This contructor should
   * be declared protected, but it will remain public for a while
   * until most client code is changed to use the getTransformer()
   * methods.  <strong>Please avoid using this constructor!</strong>
   * @param instance the client of this utility
   */
  public XSLT (Object instance) {
    this.stylesheetParams = new HashMap();
    this.caller = instance;
  }
  
  /**
   * Factory method that produces an XSLT transformer utility.
   * @param instance the client of this utility
   * @return a transformer utility
   * @since uPortal 2.2
   */
  public static XSLT getTransformer(Object instance) {
      return new XSLT(instance);
  }

  /**
   * Factory method that produces an XSLT transformer utility
   * with a capability of choosing a stylesheet depending on a
   * list of locales.
   * @param instance the client of this utility
   * @return a locale-aware transformer utility
   * @since uPortal 2.2
   */
  public static XSLT getTransformer(Object instance, Locale[] locales) {
      return new LocaleAwareXSLT(instance, locales);
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
   * @param is an input stream to the serialized xml source
   */
  public void setXML(java.io.InputStream is) {
    xmlSource = new StreamSource(is);
  }

  /**
   * Configures the xml source.
   * @param file a File object representing the xml source
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
   * @param name the name of the stylesheet parameter
   * @param value the value of the stylesheet parameter
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
      Transformer trans;
      if(l18n == null){
          trans = getTransformer(this.xslURI);
      }
      else{
          trans = getTransformer(this.xslURI,l18n);
      }
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

    public void setResourceBundle(ResourceBundle bundle){
        this.l18n=bundle;
    }

    /**
       * This method caches compiled stylesheet objects, keyed by the stylesheet's URI and locale.
       * @param stylesheetURI the URI of the XSLT stylesheet
       * @param l18n the localized strings to add to the xsl
       * @return the StlyesheetRoot object
       * @throws SAXException
       */
    public static Templates getTemplates(String stylesheetURI, ResourceBundle l18n) throws SAXException, PortalException, TransformerConfigurationException {
        String lookup = new StringBuffer(stylesheetURI).append(l18n.getLocale().toString()).toString();
        Templates temp = (Templates)stylesheetRootCache.get(lookup);
        if(temp == null) {
            Document xsl = null;
            try {
                xsl = DocumentFactory.getDocumentFromStream(
                                    new BufferedInputStream(ResourceLoader.getResourceAsStream(DocumentFactory.class, stylesheetURI),2048));
            }
            catch(IOException e) {
                throw new ResourceMissingException(stylesheetURI, "Stylesheet", "Unable to read stylesheet from the specified location. Please check the stylesheet URL");
            }
            addLocalization(xsl, l18n);
            Source src = new DOMSource(xsl);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            temp = tFactory.newTemplates(src);
            if(stylesheetRootCacheEnabled) {
                stylesheetRootCache.put(lookup, temp);
            }
        }
        return temp;
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
   * This method returns a localized Transformer for a given stylesheet.
   * @param stylesheetURI the URI of the XSLT stylesheet
   * @return <code>Transformer</code>
   * @throws SAXException
   */
  public static Transformer getTransformer(String stylesheetURI, ResourceBundle l18n) throws SAXException, PortalException {
    Transformer t = null;
    try {
      t = getTemplates(stylesheetURI,l18n).newTransformer();
    } catch (TransformerConfigurationException tce) {
      LogService.log(LogService.ERROR,"XSLT::getTransformer() : TRAX transformer is misconfigured : "+tce.getMessage());
    }
    return t;
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
   * This method returns a localized TransformerHandler for a given stylesheet.
   * @param stylesheetURI the URI of the XSLT stylesheet
   * @param locales the list of locales
   * @param caller the calling class
   * @return <code>Transformer</code>
   * @throws SAXException
   */
  public static TransformerHandler getTransformerHandler(String stylesheetURI, Locale[] locales, Object caller) throws SAXException, PortalException {
      TransformerHandler th = null;
      try {
        String localizedStylesheetURI = LocaleAwareXSLT.getLocaleAwareXslUri(stylesheetURI, locales, caller);
        th = getSAXTFactory().newTransformerHandler(getTemplates(localizedStylesheetURI));
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

   /**
   * Writes a set of key/value pairs from a resourcebundle as global variables
   * in an xsl stylesheet
   *
   * @param xsl the xsl stylesheet as a DOM document
   * @param localization the resource bundle of key/value pairs to be written to xsl variables
   */
   protected static void addLocalization(Document xsl, ResourceBundle localization) {
        ArrayList keys = new ArrayList();
        Enumeration en = localization.getKeys();
        while(en.hasMoreElements()){
            keys.add(en.nextElement());
        }
        //String test = "test";
        Element root = xsl.getDocumentElement();
        Node ft = root.getFirstChild();
        boolean foundFT = false;
        NodeList nl = root.getChildNodes();
        for(int i=0;i<nl.getLength();i++){
            Node n = nl.item(i);
            if(n.getNodeType() == Node.ELEMENT_NODE){
                Element e = (Element)n;
                //System.out.println("Checking Element "+e.getNamespaceURI()+":"+e.getLocalName());
                if(!foundFT && e.getNamespaceURI().equals("http://www.w3.org/1999/XSL/Transform")
                    && e.getLocalName().equals("template")){
                    //System.out.println("found first template in position "+i);
                    ft = n;
                    foundFT = true;
                }
                if(e.getNamespaceURI().equals("http://www.w3.org/1999/XSL/Transform")
                    && e.getLocalName().equals("variable")){
                    String name = e.getAttribute("name");
                    //System.out.println(name+" = "+e.getAttribute("select"));
                    //test = e.getAttribute("select");
                    if(keys.contains(name)){
                        e.removeAttribute("select");
                        if (e.hasChildNodes()){
                            NodeList cl = e.getChildNodes();
                            for(int j=cl.getLength()-1;j>=0;j--){
                                e.removeChild(cl.item(j));
                            }
                        }
                       e.setAttribute("select","'"+escape(localization.getString(name))+"'");
                        keys.remove(name);
                    }
                }
            }
        }

        for(int z=0;z<keys.size();z++){
            String k = (String)keys.get(z);
            String v = escape(localization.getString(k));
            Element e = xsl.createElementNS("http://www.w3.org/1999/XSL/Transform","xsl:variable");
            e.setAttribute("name",k);
            e.setAttribute("select","'"+v+"'");
            //System.out.println(e.getAttribute("select"));
            root.insertBefore(e,ft);
        }

    }
    
  /**
   * Escape problem characters which will be inserted into XSL
   *
   * @param s the string to escape
   */
    protected static String escape(String s){
        // for initial implementation, just look for single quote
        s = CommonUtils.replaceText(s,"'","\u2019");
        return s;
    }
}

