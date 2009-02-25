/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channels.webproxy;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Rewrites URLs for CWebProxy in a WML document.
 * @author Sarah Arnott, sarnott@mun.ca
 * @version $Revision$
 */
public class CWebProxyWMLURLFilter extends CWebProxyURLFilter
{

  /**
   * A constructor which receives a ContentHandler to which
   * filtered SAX events are passed.  
   * @param handler the ContentHandler to which filtered SAX events are passed
   */  
  public CWebProxyWMLURLFilter(ContentHandler handler) 
  {
    super(handler);
  }
  
  public void startElement (String uri, String localName, String qName,  Attributes atts) throws SAXException 
  {
    AttributesImpl attsImpl = new AttributesImpl(atts);

    // This is an initial guess at what needs to be fixed...more may be needed
    if (attsImpl.getIndex("href") != -1)
    {
      rewriteURL("a", "href", qName, atts, attsImpl);
      rewriteURL("go", "href", qName, atts, attsImpl);
    }

    super.startElement(uri, localName, qName, attsImpl);   
  }

}
