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

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Channel Rendering buffer allows portal to accumulate a list
 * of all channels that will have to be rendered. This is done
 * by accumulating layout content (full page content minus content
 * provided by the channels).
 * The entire document is accumulated in a buffer. Information about
 * channel elements is passed to a ChannelManager. Once the end of the
 * document is reached, the entire buffer is released to a provided
 * Document Handler.
 *
 * @author Peter Kharchenko
 * @version $Revision$
 */
public class ChannelRenderingBuffer extends SAX2BufferImpl
{
    
    private static final Log log = LogFactory.getLog(ChannelRenderingBuffer.class);
    
  protected ChannelManager cm;

  // information about the current channel
  private boolean insideChannelElement = false;
  private Hashtable params;
  private String channelClassName;
  private String channelSubscribeId;
  private String channelPublishId;
  private long timeOut;
    boolean ccaching;

  /**
   * Default constructor.
   * @param chanman the channel manager
   */
  public ChannelRenderingBuffer(ChannelManager chanman) {
    super();
    ccaching=false;
    this.cm = chanman;
    this.startBuffering();
    this.setContentHandler(null);
  }

  public ChannelRenderingBuffer(ContentHandler ch, ChannelManager chanman) {
    super(ch);
    ccaching=false;
    this.cm = chanman;
    this.startBuffering();
    this.setContentHandler(null);
  }

  public ChannelRenderingBuffer(XMLReader parent, ChannelManager chanman) {
    super(parent);
    ccaching=false;
    this.cm = chanman;
    this.startBuffering();
    this.setContentHandler(null);
  }

  public ChannelRenderingBuffer(ChannelManager chanman, boolean ccaching) {
    this(chanman);
    this.setCharacterCaching(ccaching);
  }

  public ChannelRenderingBuffer(ContentHandler ch, ChannelManager chanman, boolean ccaching) {
    this(ch,chanman);
    this.setCharacterCaching(ccaching);
  }
  public ChannelRenderingBuffer(XMLReader parent, ChannelManager chanman, boolean ccaching) {
    this(parent,chanman);
    this.setCharacterCaching(ccaching);
  }

    public void setCharacterCaching(boolean setting) {
        this.ccaching=setting;
    }

  public void startDocument() throws SAXException {
    insideChannelElement = false;
    super.startDocument ();
  }

  public void endDocument() throws SAXException {
    cm.commitToRenderingChannelSet();
    super.endDocument();
  }

  public void startElement(String url, String localName, String qName, Attributes atts) throws SAXException {
    if (!insideChannelElement) {
      // recognizing "channel"
      if (qName.equals("channel")) {
        insideChannelElement = true;

        // get class attribute
        channelClassName = atts.getValue("class");
        channelSubscribeId = atts.getValue("ID");
        channelPublishId = atts.getValue("chanID");
        timeOut = java.lang.Long.parseLong(atts.getValue("timeout"));
        params = new Hashtable();
      }
    }
    else if (qName.equals("parameter")) {
      params.put(atts.getValue("name"), atts.getValue("value"));
    }

    super.startElement(url,localName,qName,atts);
  }

  public void endElement(String url, String localName, String qName) throws SAXException {
    if (insideChannelElement) {
      if (qName.equals("channel")) {
          try {
              cm.startChannelRendering(channelSubscribeId);
          } catch (PortalException pe) {
              log.error("ChannelRenderingBuffer::endElement() : unable to start rendering channel! (channelSubscribeId=\""+channelSubscribeId+"\")");
          }
          insideChannelElement=false;
      }
    }
    super.endElement (url,localName,qName);
  }
}
