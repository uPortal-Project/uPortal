/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.webproxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.utils.SAX2FilterImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Rewrites URLs for CWebProxy in an XHTML document.
 * @author Sarah Arnott, sarnott@mun.ca
 * @version $Revision$
 */
public class CWebProxyXHTMLURLFilter extends CWebProxyURLFilter
{
    private static final Log log = LogFactory.getLog(CWebProxyXHTMLURLFilter.class);
  private SAX2BufferImpl buffer;
  private boolean insideForm = false;
  private boolean markedInputExists = false;
  private String actionURL = null; // either baseActionUrl or downloadActionUrl
  private String origActionValue = null;
  private String newActionValue = null;
  private String passThrough = "";

  /**
   * A constructor which receives a ContentHandler to which
   * filtered SAX events are passed.  
   * @param handler the ContentHandler to which filtered SAX events are passed
   */  
  public CWebProxyXHTMLURLFilter(ContentHandler handler) 
  {
    super(handler);
  }
  
  public void startElement (String uri, String localName, String qName,  Attributes atts) throws SAXException 
  {
    AttributesImpl attsImpl = new AttributesImpl(atts);

    if (attsImpl.getIndex("href") != -1)
    {
      String target = atts.getValue("target");
      // if target exists, do not go through channel
      if (target == null)
        rewriteURL("a", "href", qName, atts, attsImpl);
      if (target == null)
        rewriteURL("area", "href", qName, atts, attsImpl);
      rewriteURL("map", "href", qName, atts, attsImpl);
    }
    else if (qName.equals("form"))
    {
      String target = atts.getValue("target");
      if (target == null)
      {
        passThrough = (String)runtimeData.get("cw_passThrough");
        if (passThrough.equals("all") || passThrough.equals("marked")
                                      || passThrough.equals("application"))
        {
          insideForm = true;
       
          // determine original action value
          String attValue = atts.getValue("action");
          if (attValue != null)
          {
            origActionValue = attValue;
          }
          else // action attribute required
          {
            attsImpl.addAttribute(uri, localName, "action", "CDATA", "");
            origActionValue = "";
          }

          // set up a buffer for form elements
          buffer = new SAX2BufferImpl(this.contentHandler);
          buffer.clearBuffer();
          buffer.startBuffering();
        }
      }
    }
    else if (qName.equals("input"))
    {
      if (insideForm)
      {
        String name = atts.getValue("name");
        if (name != null)
        {
          if (name.equals("cw_download"))
            actionURL = atts.getValue("value");
          else if (passThrough.equals("marked") && name.equals("cw_inChannelLink"))
            markedInputExists = true;
        }
      }
    }

    if (insideForm)
      buffer.startElement(uri, localName, qName, attsImpl);
    else
      super.startElement(uri, localName, qName, attsImpl);
  }

  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    if (qName.equals("form") && insideForm)
    {
      try
      {
        initRewrite();
        insideForm = false;
        actionURL = null;
        buffer.endElement(uri, localName, qName);
        buffer.stopBuffering();
        buffer.outputBuffer(new FormFilter(this.contentHandler));
        buffer.clearBuffer();
      }
      catch (Exception e)
      {
          if (log.isDebugEnabled())
              log.debug("CWebProxyXHTMLFilter:: Exception: " + e);
      }
    }
    else if (insideForm)
      buffer.endElement(uri, localName, qName);
    else
      super.endElement(uri, localName, qName);
  }

  public void characters(char ch[], int start, int length) throws SAXException
  {
    if (insideForm)
      buffer.characters(ch, start, length);
    else
      super.characters(ch, start, length);
  }

  /**
   * Sets newActionValue for FormFilter to modify and adds an input 
   * element to buffer, if required.
   */
  private void initRewrite()
  {
    if (actionURL == null)
      actionURL = (String)runtimeData.get("baseActionURL");

    String query = getQueryString(origActionValue); 
    String base = getBase(origActionValue);

    String xmlUri = (String) runtimeData.get("cw_xml");

    if (passThrough.equals("all"))
    {
      newActionValue = actionURL + query;

      if (!(origActionValue.trim().equals("") || xmlUri.equals(base)))
        addInputToBuffer();
    }
    else if (passThrough.equals("marked") && markedInputExists)
    {
      newActionValue = actionURL + query;
      
      if (!(origActionValue.trim().equals("") || xmlUri.equals(base)))
        addInputToBuffer();
    }
    else if (passThrough.equals("application"))
    {
      if (origActionValue.trim().equals("") || xmlUri.equals(base))
        newActionValue = actionURL + query;
      else
        newActionValue = origActionValue;
    }
    else
      newActionValue = origActionValue;
  }

  /**
   * Adds an input element to the form to pass original action
   * attribute value to CWebProxy.
   */
  private void addInputToBuffer()
  {
    try
    {
      AttributesImpl atts = new AttributesImpl();
      atts.addAttribute("", "", "type", "CDATA", "hidden");
      atts.addAttribute("", "", "name", "CDATA", "cw_xml");
      atts.addAttribute("", "", "value", "CDATA", origActionValue);
      buffer.startElement("", "", "input", atts);
      buffer.endElement("", "", "input");
    }
    catch (SAXException e)
    {
        if (log.isInfoEnabled())
            log.info("CWebProxyXHTMLURLFilter::cannot add input element to buffer: " + e);
    }
  }

  /**
   * A helper class which rewrites the action attribute in a form
   * element.
   */
  private class FormFilter extends SAX2FilterImpl
  {
    public FormFilter(ContentHandler ch)
    {
      super(ch);
    }

    public void startElement(String uri, String localName, String qName,  Attributes atts) throws SAXException
    {
      AttributesImpl attsImpl = new AttributesImpl(atts);

      if (qName.equals("form"))
      {
        int index = attsImpl.getIndex("action");
        if (index != -1) // action att should always exist
        {
          attsImpl.setAttribute(index, attsImpl.getURI(index), attsImpl.getLocalName(index), "action", attsImpl.getType(index), newActionValue);
        }
      }
      super.startElement(uri, localName, qName, attsImpl);
    }
  } //end helper class

}
