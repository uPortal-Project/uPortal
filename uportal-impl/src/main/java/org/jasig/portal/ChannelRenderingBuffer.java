/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.xml.sax.Attributes;
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

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final ChannelManager cm;
    private final boolean ccaching;

    // information about the current channel
    private boolean insideChannelElement = false;
    private Hashtable params;
    private String channelClassName;
    private String channelSubscribeId;
    private String channelPublishId;
    private long timeOut;
    
  public ChannelRenderingBuffer(XMLReader parent, ChannelManager chanman, boolean ccaching, HttpServletRequest request, HttpServletResponse response) {
      super(parent);
      this.cm = chanman;
      this.ccaching = ccaching;
      this.request = request;
      this.response = response;
      
      this.setContentHandler(null);
      this.startBuffering();
  }
  
  public ChannelRenderingBuffer(ChannelManager chanman, boolean ccaching, HttpServletRequest request, HttpServletResponse response) {
      this.cm = chanman;
      this.ccaching = ccaching;
      this.request = request;
      this.response = response;
      
      this.setContentHandler(null);
      this.startBuffering();
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
              cm.startChannelRendering(this.request, this.response, this.channelSubscribeId);
          } catch (PortalException pe) {
              log.error("ChannelRenderingBuffer::endElement() : unable to start rendering channel! (channelSubscribeId=\""+channelSubscribeId+"\")");
          }
          insideChannelElement=false;
      }
    }
    super.endElement (url,localName,qName);
  }
}
