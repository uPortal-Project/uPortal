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

package org.jasig.portal.xmlchannels;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import org.jasig.portal.*;
import org.apache.xalan.xslt.*;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;

import java.net.*;

/**
 * A channel which transforms XML for rendering in the portal.
 * Two parameters must be supplied:
 *
 *  1) a URI representing the source XML document
 *  2) a URI representing the corresponding .ssl (stylesheet list) file
 *
 * This channel can be used for all XML formats including RSS.
 * @author Steve Toth
 * @author Ken Weiner
 * @version $Revision$
 */
public class CGenericXSLT implements org.jasig.portal.IChannel
{
  protected String sXML;
  protected String sSSL;
  protected String sChannelTitle;
  protected StylesheetSet stylesheetSet;
  protected ChannelRuntimeData runtimeData;

  protected static String fs = File.separator;
  protected static String stylesheetDir = GenericPortalBean.getPortalBaseDir () + "webpages" + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal" + fs + "CGenericXSLT" + fs;
  protected static String sMediaProps = GenericPortalBean.getPortalBaseDir () + "properties" + fs + "media.properties";
  
  public CGenericXSLT ()
  {
  }

  // Get channel parameters.
  public void setStaticData (ChannelStaticData sd)
  {
    try
    {
      this.sChannelTitle = sd.getParameter ("name");
      this.sXML = fixURI (sd.getParameter ("xml"));
      this.sSSL = fixURI (sd.getParameter ("ssl"));
      
      stylesheetSet = new StylesheetSet (sSSL);
      stylesheetSet.setMediaProps (sMediaProps);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }

  public void setRuntimeData (ChannelRuntimeData rd)
  {
    runtimeData = rd;
  }

  public void receiveEvent (LayoutEvent ev)
  {
    // No events to process here
  }

  // Access channel runtime properties.
  public ChannelRuntimeProperties getRuntimeProperties ()
  {
    return new ChannelRuntimeProperties ();
  }

  // Set some channel properties.
  public ChannelSubscriptionProperties getSubscriptionProperties ()
  {
    ChannelSubscriptionProperties csb = new ChannelSubscriptionProperties ();
    csb.setName (sChannelTitle);
    csb.setDefaultDetachWidth ("550");
    csb.setDefaultDetachHeight ("450");
    return csb;
  }
  
  public void renderXML (DocumentHandler out)
  {
    try
    {
      if (stylesheetSet != null)
      {
        XSLTInputSource stylesheet = stylesheetSet.getStylesheet (runtimeData.getHttpRequest ());
        
        if (stylesheet != null)
        {
          XSLTProcessor processor = XSLTProcessorFactory.getProcessor ();
          processor.process (new XSLTInputSource (sXML), stylesheet, new XSLTResultTarget (out));
        }
      }
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, "Problem transforming " + sXML);
      Logger.log (Logger.ERROR, e);
    }
  }
  
  /**
   * Allows uri parameters to be entered in one
   * of 3 ways:
   * 1) http://...
   * 2) An absolute file system path optionally beginning with file://
   *    e.g. C:\WinNT\whatever.ssl or /usr/local/whatever.ssl
   *    or file://C:\WinNT\whatever.ssl
   * 3) A path relative to the portal base dir as determined from 
   *    GenericPortalBean.getPortalBaseDir()
   */
  private static String fixURI (String str)
  {
    // Windows fix
    char ch0 = str.charAt (0);
    char ch1 = str.charAt (1);

    if (str.indexOf ("://") == -1 && ch1 != ':')
    {
      // Relative path was specified, so prepend portal base dir
      str = "file:/" + GenericPortalBean.getPortalBaseDir () + str;
    }
    else if (str.startsWith ("file://"))
    {
      // Replace "file://" with "file:/"
      str = "file:/" + str.substring (7);
    }
    else if (ch1 == ':')
    {
      // If Windows full path, prepend with "file:/"
      str = "file:/" + str;
    }

    // Handle platform-dependent strings
    str = str.replace (java.io.File.separatorChar, '/');

    return str;
  }
}
