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
 * a WML document
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class WMLURLFilter extends AbsoluteURLFilter {
  
  /**
   * A constructor which receives a ContentHandler to which
   * filtered SAX events are passed
   * @param handler the ContentHandler to which filtered SAX events are passed
   */  
  protected WMLURLFilter(ContentHandler handler) {
    super(handler);
  }
  
  public void startElement (String uri, String localName, String qName,  Attributes atts) throws SAXException {
    AttributesImpl attsImpl = new AttributesImpl(atts);
    // This is an initial guess at what needs to be fixed...more may be needed!
    fixURL("img", "src", qName, atts, attsImpl);
    fixURL("a", "href", qName, atts, attsImpl);
    fixURL("go", "href", qName, atts, attsImpl);
    super.startElement(uri, localName, qName, attsImpl);   
  }
}
