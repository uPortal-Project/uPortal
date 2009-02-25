/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
