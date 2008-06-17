/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Replaces all relative URLs with absolute URLs in
 * an XHTML document
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class XHTMLURLFilter extends AbsoluteURLFilter {
  
  /**
   * A constructor which receives a ContentHandler to which
   * filtered SAX events are passed
   * @param handler the ContentHandler to which filtered SAX events are passed
   */  
  protected XHTMLURLFilter(ContentHandler handler) {
    super(handler);
  }
  
  public void startElement (String uri, String localName, String qName,  Attributes atts) throws SAXException {
    AttributesImpl attsImpl = new AttributesImpl(atts);

    if (qName.equals("base"))
    {
      super.setBaseUrl(atts.getValue("href"));
      return;  // do not include base element in channel output
    }
    else if (atts.getValue("src") != null)
    {
      fixURL("img", "src", qName, atts, attsImpl);
      fixURL("input", "src", qName, atts, attsImpl);
      fixURL("script", "src", qName, atts, attsImpl);
      fixURL("frame", "src", qName, atts, attsImpl);
      fixURL("iframe", "src", qName, atts, attsImpl);
    }
    else if (atts.getValue("href") != null)
    {
      fixURL("a", "href", qName, atts, attsImpl);
      fixURL("area", "href", qName, atts, attsImpl);
      fixURL("map", "href", qName, atts, attsImpl);
      fixURL("link", "href", qName, atts, attsImpl);
    }
    else if (atts.getValue("action") != null)
    {
      fixURL("form", "action", qName, atts, attsImpl);
    }
    super.startElement(uri, localName, qName, attsImpl);   
  }

  public void endElement (String uri, String localName, String qName) throws SAXException {
    if (qName.equals("base"))
      return; // do not include base element in channel output
    else
      super.endElement(uri, localName, qName);
  }  
}
