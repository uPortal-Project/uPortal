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
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.xml.sax.ext.LexicalHandler;
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

public class ChannelIncorporationFilter extends SAX2FilterImpl
{
  // keep track if we are "in" the <channel> element
  private boolean insideChannelElement = false;
  ChannelManager cm;

  // information about the current channel
  private Hashtable params;
  private String channelClassName;
  private String channelID;
  private String channelPublishID;
  private long timeOut;

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

  public void startElement (String uri, String localName, String qName,  Attributes atts) throws SAXException
  {
    if (!insideChannelElement) {
      // recognizing "channel"
      if (qName.equals ("channel")) {
        insideChannelElement = true;

        // get class attribute
        channelClassName = atts.getValue ("class");
        channelID = atts.getValue ("ID");
        channelPublishID = atts.getValue ("chanID");
        timeOut = java.lang.Long.parseLong (atts.getValue ("timeout"));
        params = new Hashtable ();
      } else {
        super.startElement (uri,localName,qName,atts);
      }
    } else if (qName.equals ("parameter")) {
      params.put (atts.getValue ("name"), atts.getValue ("value"));
    }
  }

  public void endElement (String uri, String localName, String qName) throws SAXException
  {
    if (insideChannelElement) {
      if (qName.equals ("channel")) {
        if (this.getContentHandler() != null) {
            cm.outputChannel (channelID, this.channelPublishID, this.getContentHandler(),this.channelClassName,this.timeOut,this.params);
            insideChannelElement = false;
        }
      }
    } else { 
        super.endElement (uri,localName,qName);
    }
  }
}
