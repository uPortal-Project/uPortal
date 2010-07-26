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

package org.jasig.portal;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.rendering.PortletExecutionManager;
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
 * @deprecated IChannel rendering code will be replaced with portlet specific rendering code in a future release
 */
@Deprecated
public class ChannelRenderingBuffer extends SAX2BufferImpl
{
    
    private static final Log log = LogFactory.getLog(ChannelRenderingBuffer.class);

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final PortletExecutionManager portletExecutionManager;
    private final boolean ccaching;

    // information about the current channel
    private boolean insideChannelElement = false;
    private Hashtable params;
    private String channelClassName;
    private String channelSubscribeId;
    private String channelPublishId;
    private long timeOut;
    
  public ChannelRenderingBuffer(XMLReader parent, PortletExecutionManager portletExecutionManager, boolean ccaching, HttpServletRequest request, HttpServletResponse response) {
      super(parent);
      this.portletExecutionManager = portletExecutionManager;
      this.ccaching = ccaching;
      this.request = request;
      this.response = response;
      
      this.setContentHandler(null);
      this.startBuffering();
  }
  
  public ChannelRenderingBuffer(PortletExecutionManager portletExecutionManager, boolean ccaching, HttpServletRequest request, HttpServletResponse response) {
      this.portletExecutionManager = portletExecutionManager;
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
              this.portletExecutionManager.startPortletRender(this.channelSubscribeId, this.request, this.response);
          } catch (PortalException pe) {
              log.error("ChannelRenderingBuffer::endElement() : unable to start rendering channel! (channelSubscribeId=\""+channelSubscribeId+"\")");
          }
          insideChannelElement=false;
      }
    }
    super.endElement (url,localName,qName);
  }
}
