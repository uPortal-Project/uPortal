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

import org.jasig.portal.PortalException;
import org.jasig.portal.utils.SAX2FilterImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

/**
 * <p>Replaces all relative URLs with absolute URLs.</p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public abstract class AbsoluteURLFilter extends SAX2FilterImpl {
  
  protected static String baseUrl = null;
  
  /**
   * A constructor which receives a ContentHandler to which
   * filtered SAX events are passed
   * @param handler the ContentHandler to which filtered SAX events are passed
   */
  protected AbsoluteURLFilter(ContentHandler handler) {
    super(handler);
  }
  
  /**
   * A factory method that uses a mime type to decide which kind of 
   * AbsoluteURLFilter to instantiate.  There are currently two types
   * of markup supported: XHTML and WML.
   * @param mimeType the mime type of the markup that this filter will apply to
   * @param baseUrl the base URL to be prepended to relative URL paths
   * @param handler the ContentHandler to which to pass along filtered SAX events
   * @return filter the AbsoluteURLFilter matching the mimeType
   */  
  public static final AbsoluteURLFilter newAbsoluteURLFilter(String mimeType, String baseUrl, ContentHandler handler) throws PortalException {
    AbsoluteURLFilter filter = null;
    AbsoluteURLFilter.baseUrl = baseUrl;
    
    if (mimeType != null) {
      if (mimeType.equals("text/html")) {
        filter = new XHTMLURLFilter(handler);
      } else if (mimeType.equals("text/vnd.wap.wml")) {
        filter = new WMLURLFilter(handler);
      } else {
        throw new PortalException("AbsoluteURLFilter.newAbsoluteURLFilter(): Unable to locate AbsoluteURLFilter for mime type '" + mimeType + "'");
      }
    } else {
      throw new PortalException("AbsoluteURLFilter.newAbsoluteURLFilter(): Unable to create AbsoluteURLFilter. Mime type is null.");
    }
    
    return filter;
  }
  
  /**
   * A helper method for derivitive classes to easily fix an attribute
   * that has a URL value
   * @param elementName the element name containing an attribute of name attName
   * @param attName the name of the attribute of elementName
   * @param qName the name of the current element
   * @param atts the attibutes of the current element
   * @param attsImpl the attributes implementation to contain the new attribute value
   */
  protected final void fixURL(String elementName, String attName, String qName, Attributes atts, AttributesImpl attsImpl) {
    if (qName.equals(elementName)) {
      String attValue = atts.getValue(attName);
      // Assume that if attribute value exists and doesn't contain "://",
      // then it is a relative URL and a base URL must be prepended to it
      if (attValue != null && attValue.indexOf("://") < 0) {
        attValue = baseUrl + attValue;
        int index = atts.getIndex(attName);
        attsImpl.setAttribute(index, atts.getURI(index), atts.getLocalName(index), attName, atts.getType(index), attValue);
      }      
    }
  }  
}
