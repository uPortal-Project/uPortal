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

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

public class ChannelIncorporationFilter extends SAXFilterImpl
{
  // keep track if we are "in" the <channel> element
  private boolean insideChannelElement = false;
  ChannelManager cm;

  // information about the current channel
  private String channelID;

  // public functions

  public ChannelIncorporationFilter (DocumentHandler handler, ChannelManager chanm)
  {
    super (handler);
    this.cm = chanm;
  }

  public void startElement (java.lang.String name, org.xml.sax.AttributeList atts) throws SAXException
  {
    if (!insideChannelElement)
    {
      // recognizing "channel"
      if (name.equals ("channel"))
      {
        insideChannelElement = true;
        channelID = atts.getValue ("ID");
      }
      else
        super.startElement (name, atts);
    }
  }

  public void endElement (java.lang.String name) throws SAXException
  {
    if (insideChannelElement)
    {
      if (name.equals ("channel"))
      {
        if (super.outDocumentHandler != null)
        {
          cm.outputChannel (channelID, this.getDocumentHandler ());
          insideChannelElement = false;
        }
      }
    }
    else super.endElement (name);
  }
}
