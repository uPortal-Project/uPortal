/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
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
