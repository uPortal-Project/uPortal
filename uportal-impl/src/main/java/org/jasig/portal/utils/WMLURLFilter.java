/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
