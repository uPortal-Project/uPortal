/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Replaces all relative URLs with absolute URLs in
 * an XHTML document
 * @author Ken Weiner, kweiner@interactivebusiness.com
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
