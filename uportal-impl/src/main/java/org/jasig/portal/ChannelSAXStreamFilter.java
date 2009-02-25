/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

import org.jasig.portal.utils.SAX2FilterImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Filters out startDocument and endDocument from the
 * channel content.
 * This filter is invoked by the ChannelManager
 * prior to passing channel content to the ChannelIncorporationFilter.
 * @author Peter Kharchenko
 * @version $Revision$
 */
public class ChannelSAXStreamFilter extends SAX2FilterImpl
{
    // downward
  public ChannelSAXStreamFilter (ContentHandler handler) {
    super(handler);
  }

    // upward
  public ChannelSAXStreamFilter (XMLReader parent) {
    super(parent);
  }

  public void startDocument() throws SAXException {
  }

  public void endDocument() throws SAXException {
  }

  public void processingInstruction(String target, String data) throws SAXException {
  }
}
