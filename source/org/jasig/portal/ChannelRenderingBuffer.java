/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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

import org.xml.sax.DocumentHandler;
import org.xml.sax.SAXException;
import java.util.Hashtable;

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
public class ChannelRenderingBuffer extends SAXBufferImpl
{
  protected ChannelManager cm;

  // information about the current channel
  private boolean insideChannelElement = false;
  private Hashtable params;
  private String channelClassName;
  private String channelID;
  private long timeOut;

  /**
   * Default constructor.
   * @param handler output document handler
   * @param chanman channel manager
   */
  public ChannelRenderingBuffer (ChannelManager chanman)
  {
    super ();
    this.cm = chanman;
    this.startBuffering();
    this.setDocumentHandler(null);
  }

  public void startDocument () throws SAXException
  {
    insideChannelElement = false;
    super.startDocument ();
  }

  public void endDocument () throws SAXException
  {
    super.endDocument ();

    // buffer will be unplugged by the LayoutBean
    //    this.stopBuffering();
  }

  public void startElement (java.lang.String name, org.xml.sax.AttributeList atts) throws SAXException
  {
    if (!insideChannelElement)
    {
      // recognizing "channel"
      if (name.equals ("channel"))
      {
        insideChannelElement = true;
        
        // get class attribute
        channelClassName = atts.getValue ("class");
        channelID = atts.getValue ("ID");
        timeOut = java.lang.Long.parseLong (atts.getValue ("timeout"));
        params = new Hashtable ();
      }
    } 
    else if (name.equals ("parameter"))
    {
      params.put (atts.getValue ("name"), atts.getValue ("value"));
    }

    super.startElement (name,atts);
  }

  public void endElement (java.lang.String name) throws SAXException
  {
    if (insideChannelElement)
    {
      if (name.equals ("channel"))
      {
        cm.startChannelRendering (channelID, channelClassName, timeOut,params);
        insideChannelElement=false;
      }
    }
    super.endElement (name);
  }
}
