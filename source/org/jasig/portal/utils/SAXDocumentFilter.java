/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Filters out startDocument and endDocument from the
 * channel content.
 * @author Peter Kharchenko
 * @version $Revision$
 */
public class SAXDocumentFilter extends SAX2FilterImpl
{
    // downward
  public SAXDocumentFilter (ContentHandler handler) {
    super(handler);
  }

    // upward
  public SAXDocumentFilter (XMLReader parent) {
    super(parent);
  }

  public void startDocument() throws SAXException {
  }

  public void endDocument() throws SAXException {
  }

}
