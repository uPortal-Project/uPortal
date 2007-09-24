/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import org.jasig.portal.utils.SAX2FilterImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A filter that incorporates content rendered by the channels in to
 * the main transformation stream.
 * 
 * TODO: there are enhancements in CharacterCachingChannelIncorporationFilter as
 * of uPortal 2.5.1 to support dynamic channel titles.  Do those enhancements need
 * to be copied here?  Or is the functionality of this class duplicated by a
 * degenerate (non-caching) configuration of CharacterCachingChannelIncorporationFilter, 
 * such that it would be a worthwhile reduction of code duplication to drop this 
 * class?
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version $Revision$
 */
public class ChannelIncorporationFilter extends SAX2FilterImpl {

  // keep track if we are "in" the <channel> element
  private boolean insideChannelElement = false;
  ChannelManager cm;

  // information about the current channel
  private String channelSubscribeId;

    // constructors

    // bare
    public ChannelIncorporationFilter(ChannelManager chanm) {
        this.cm=chanm;
    }

    // upward chaining
    public ChannelIncorporationFilter(XMLReader parent, ChannelManager chanm) {
        super(parent);
        this.cm=chanm;
    }
    // downward chaining
    public ChannelIncorporationFilter (ContentHandler handler, ChannelManager chanm) {
        super (handler);
        this.cm = chanm;
    }

    public void startElement (String uri, String localName, String qName,  Attributes atts) throws SAXException {
        if (!insideChannelElement) {
            // recognizing "channel"
            if (qName.equals ("channel")) {
                insideChannelElement = true;

                // get class attribute
                channelSubscribeId = atts.getValue ("ID");
            } else {
                super.startElement (uri,localName,qName,atts);
            }
        }
    }

    public void endElement (String uri, String localName, String qName) throws SAXException {
        if (insideChannelElement) {
            if (qName.equals ("channel")) {
                if (this.getContentHandler() != null) {
                    cm.outputChannel(channelSubscribeId,this.getContentHandler());
                    insideChannelElement = false;
                }
            }
        } else { 
            super.endElement (uri,localName,qName);
        }
    }
}
