 /**
 * Copyright (c) 2001 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.channels;

import org.xml.sax.DocumentHandler;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.net.MalformedURLException;
import org.jasig.portal.*;
import org.jasig.portal.utils.XSLT;
import org.w3c.tidy.*;

/**
 * <p>A channel which transforms and interacts with dynamic XML or HTML.</p>
 *
 * <p>Static channel parameters to be supplied:</p>
 *  <ol>
 *  <li>"xmlUri" - a URI representing the source XML document
 *  <li>"sslUri" - a URI representing the corresponding .ssl (stylesheet list) file
 *  <li>"xslTitle" - a title representing the stylesheet (optional)
 *                  <i>If no title parameter is specified, a default
 *                  stylesheet will be chosen according to the media</i>
 *  <li>"xslUri" - a URI representing the stylesheet to use
 *                  <i>If <code>xslUri</code> is supplied, <code>sslUri</code>
 *                  and <code>xslTitle</code> will be ignored.
 *  <li>"passThrough" - indicates that RunTimeData is to be passed through.
 *                  <i>If <code>passThrough</code> is supplied, and not set
 *		    to "none", additional RunTimeData parameters and values
 *		    will be passed as request parameters to the
 *		    <code>xmlUri</code>.
 *  <li>"tidy" 	    - output from <code>xmlUri</code> will be passed though Jtidy
 * </ol>
 * <p>The static parameters above can be overridden by including
 * parameters of the same name (<code>xmlUri</code>, <code>sslUri</code>,
 * <code>xslTitle</code> and/or <code>xslUri</code> in the HttpRequest string.</p>
 * <p>This channel can be used for all XML formats including RSS.
 * Any other parameters passed to this channel via HttpRequest will get
 * passed in turn to the XSLT stylesheet as stylesheet parameters. They can be
 * read in the stylesheet as follows:
 * <code>&lt;xsl:param name="yourParamName"&gt;aDefaultValue&lt;/xsl:param&gt;</code></p>
 * @author Steve Toth, stoth@interactivebusiness.com
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @author Andrew Draskoy, andrew@mun.ca
 * @author Sarah Arnott, sarnott@mun.ca
 * @version $Revision$
 */
public class CWebProxy implements org.jasig.portal.IChannel
{
  protected String fullxmlUri;
  protected String xmlUri;
  protected String passThrough;
  protected String tidy;
  protected String sslUri;
  protected String xslTitle;
  protected String xslUri;
  protected ChannelRuntimeData runtimeData;
  protected String media;

  protected static String fs = File.separator;
  protected static String stylesheetDir = GenericPortalBean.getPortalBaseDir () + "webpages" + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal" + fs + "CWebProxy" + fs;

  public CWebProxy ()
  {
  }

  // Get channel parameters.
  public void setStaticData (ChannelStaticData sd)
  {
    try
    {
      this.xmlUri = sd.getParameter ("xml");
      this.sslUri = sd.getParameter ("ssl");
      this.fullxmlUri = sd.getParameter ("xml");
      this.passThrough = sd.getParameter ("passThrough");
      this.tidy = sd.getParameter ("tidy");
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }

  public void setRuntimeData (ChannelRuntimeData rd)
  {
    runtimeData = rd;

    String xmlUri = runtimeData.getParameter("xmlUri");

    if (xmlUri != null)
       this.xmlUri = xmlUri;

    String sslUri = runtimeData.getParameter("sslUri");

    if (sslUri != null)
       this.sslUri = sslUri;

    String xslTitle = runtimeData.getParameter("xslTitle");

    if (xslTitle != null)
       this.xslTitle = xslTitle;

    String xslUri = runtimeData.getParameter("xslUri");

    if (xslUri != null)
       this.xslUri = xslUri;

    String passThrough = runtimeData.getParameter("passThrough");

    if (passThrough != null)
       this.passThrough = passThrough;

    String tidy = runtimeData.getParameter("tidy");

    if (tidy != null)
       this.tidy = tidy;

    media = runtimeData.getMedia();

    if (this.passThrough != null )
      Logger.log (Logger.DEBUG, "CWebProxy: passThrough: " + this.passThrough);

    if ( this.passThrough != null &&
       !this.passThrough.equalsIgnoreCase("none") &&
         ( this.passThrough.equalsIgnoreCase("all") ||
           runtimeData.getParameter("inChannelLink") != null ) )
    {
      Logger.log (Logger.DEBUG, "CWebProxy: xmlUri is " + this.xmlUri);

      StringBuffer newXML = new StringBuffer().append(this.xmlUri);
      String appendchar = "?";

      String teststr = (String) runtimeData.getParameter("commentText");
      Logger.log (Logger.DEBUG, "CWebProxy: commentText is " + teststr);

      Enumeration e=runtimeData.getParameterNames ();
      if (e!=null)
        {
          while (e.hasMoreElements ())
            {
              String pName = (String) e.nextElement ();
              Logger.log (Logger.DEBUG, "CWebProxy: got reqparam " + pName);
              if ( !pName.equals("inChannelLink") )
              {
                newXML.append(appendchar);
                appendchar = "&";
                newXML.append(pName);
                newXML.append("=");
                newXML.append(URLEncoder.encode(runtimeData.getParameter(pName)));
              }
            }
        }
      fullxmlUri = newXML.toString();
      Logger.log (Logger.DEBUG, "CWebProxy: fullxmlUri now: " + fullxmlUri);
    }
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
    csb.setDefaultDetachWidth ("550");
    csb.setDefaultDetachHeight ("450");
    return csb;
  }

  public void renderXML (DocumentHandler out) throws PortalException
  {
    String xml;

    if (tidy != null && tidy.equalsIgnoreCase("on"))
    {
      try
      {
        xml = getTidyString (fullxmlUri);
      }
      catch (Exception e)
      {
        throw new ResourceMissingException (fullxmlUri, "", e.getMessage());
      }
    }
    else
    {
      try
      {
        xml = UtilitiesBean.getContentsAsString(fullxmlUri);
      }
      catch (Exception e)
      {
        throw new ResourceMissingException (fullxmlUri, "", e.getMessage());
      }
    }

    runtimeData.put("baseActionURL", runtimeData.getBaseActionURL());

    // possibly this should be a copy
    if (xmlUri != null)
      runtimeData.put("xmlUri", xmlUri);
    if (sslUri != null)
      runtimeData.put("sslUri", sslUri);
    if (xslTitle != null)
      runtimeData.put("xslTitle", xslTitle);
    if (xslUri != null)
      runtimeData.put("xslUri", xslUri);
    if (passThrough != null)
      runtimeData.put("passThrough", passThrough);
    if (tidy != null)
      runtimeData.put("tidy", tidy);

    try
    {
      if (xslUri != null)
        XSLT.transform(xml, new URL(xslUri), out, runtimeData);
      else
      {
        if (xslTitle != null)
          XSLT.transform(xml, new URL(sslUri), out, runtimeData, xslTitle, media);
        else
          XSLT.transform(xml, new URL(sslUri), out, runtimeData, media);
      }
    }
    catch (org.xml.sax.SAXException e)
    {
      throw new GeneralRenderingException("problem performing the transformation");
    } catch (IOException i) {
      StringWriter sw = new StringWriter();
      i.printStackTrace(new PrintWriter(sw));
      sw.flush();
      throw new GeneralRenderingException(sw.toString());
    }
  }

  /**
   * Get the contents of a URI as a String but send it through tidy first
   * @param uri the URI
   * @return the data pointed to by a URI
   */
  public static String getTidyString (String uri) throws IOException, MalformedURLException, PortalException
  {
    URL url = new URL (UtilitiesBean.fixURI(uri));
    Tidy tidy = new Tidy ();
    tidy.setXHTML (true);
    tidy.setDocType ("omit");
    tidy.setQuiet(true);
    tidy.setShowWarnings(false);
    tidy.setNumEntities(true);
    tidy.setMakeClean(true);
    tidy.setWord2000(true);
    tidy.setXmlSpace(true);
    if ( System.getProperty("os.name").indexOf("Windows") != -1 )
       tidy.setErrout( new PrintWriter ( new FileOutputStream (new File ("nul") ) ) );
    else
       tidy.setErrout( new PrintWriter ( new FileOutputStream (new File ("/dev/null") ) ) );
    ByteArrayOutputStream stream = new ByteArrayOutputStream (1024);

    tidy.parse (url.openStream(), new BufferedOutputStream (stream));
    if ( tidy.getParseErrors() > 0 )
      throw new GeneralRenderingException("Unable to convert input document to XHTML");
    return stream.toString ();

  }

}
