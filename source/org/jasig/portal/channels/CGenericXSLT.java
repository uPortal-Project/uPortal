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

package org.jasig.portal.channels;

import org.jasig.portal.*;
import org.jasig.portal.utils.XSLT;

import org.jasig.portal.services.LogService;
import org.jasig.portal.helpers.SAXHelper;
import org.xml.sax.DocumentHandler;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Enumeration;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * <p>A channel which transforms XML for rendering in the portal.</p>
 *
 * <p>Static channel parameters to be supplied:
 *
 *  1) "xmlUri" - a URI representing the source XML document
 *  2) "sslUri" - a URI representing the corresponding .ssl (stylesheet list) file
 *  3) "xslTitle" - a title representing the stylesheet (optional)
 *                  <i>If no title parameter is specified, a default
 *                  stylesheet will be chosen according to the media</i>
 *  4) "xslUri" - a URI representing the stylesheet to use
 *                  <i>If <code>xslUri</code> is supplied, <code>sslUri</code>
 *                  and <code>xslTitle</code> will be ignored.
 * </p>
 * <p>The static parameters above can be overridden by including
 * parameters of the same name (<code>xmlUri</code>, <code>sslUri</code>,
 * <code>xslTitle</code> and/or <code>xslUri</code> in the HttpRequest string.</p>
 * <p>This channel can be used for all XML formats including RSS.
 * Any other parameters passed to this channel via HttpRequest will get
 * passed in turn to the XSLT stylesheet as stylesheet parameters. They can be
 * read in the stylesheet as follows:
 * <code>&lt;xsl:param name="yourParamName"&gt;aDefaultValue&lt;/xsl:param&gt;</code></p>
 * <p>CGenericXSLT is also useful for channels that have no dynamic data.  In these types
 * of channels, all the markup comes from the XSLT stylesheets.  An empty XML document
 * can be used and is included with CGenericXSLT.  Just set the xml parameter</p>
 * @author Steve Toth, stoth@interactivebusiness.com
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a> (multithreading,caching)
 * @version $Revision$
 */
public class CGenericXSLT implements IMultithreadedChannel, IMultithreadedCacheable
{
  // state table
  Map stateTable;
  static final String systemCacheId="org.jasig.portal.channels.CGenericXSLT"; // to prepend to the system-wide cache key

  // state class
  private class CState 
  {
    private String xmlUri;
    private String sslUri;
    private String xslTitle;
    private String xslUri;
    private ChannelRuntimeData runtimeData;

    public CState() 
    {
      xmlUri = sslUri = xslTitle = xslUri = null;
      runtimeData=null;
    }
  }

  public CGenericXSLT() 
  {
    stateTable = Collections.synchronizedMap(new HashMap());
  }

  public void setStaticData (ChannelStaticData sd, String uid) 
  {
    CState state = new CState();
    state.xmlUri = sd.getParameter("xmlUri");
    state.sslUri = sd.getParameter("sslUri");
    
    if (state.sslUri != null)
      state.sslUri = UtilitiesBean.fixURI(state.sslUri);
	  
    state.xslTitle = sd.getParameter("xslTitle");
    state.xslUri = sd.getParameter("xslUri");
    stateTable.put(uid,state);
  }
    
  public void setRuntimeData (ChannelRuntimeData rd, String uid) 
  {
    CState state = (CState)stateTable.get(uid);
	  
    if (state == null) 
      LogService.instance().log(LogService.ERROR,"CGenericXSLT:setRuntimeData() : attempting to access a non-established channel! setStaticData() has never been called on the uid=\""+uid+"\"");
    else 
    {
      // because of the portal rendering model, there is no reason to synchronize on state
      state.runtimeData=rd;
      String xmlUri = rd.getParameter("xmlUri");
    
      if (xmlUri != null) 
        state.xmlUri = xmlUri;
	    
      String sslUri = rd.getParameter("sslUri");
    
      if (sslUri != null)
        state.sslUri = UtilitiesBean.fixURI(sslUri);

      String xslTitle = rd.getParameter("xslTitle");
  
      if (xslTitle != null)
        state.xslTitle = xslTitle;
      
      String xslUri = rd.getParameter("xslUri");
	    
      if (xslUri != null)
        state.xslUri = xslUri;
    }
  }

  public void receiveEvent (PortalEvent ev, String uid) 
  {
      if (ev.getEventNumber() == PortalEvent.SESSION_DONE) 
      {
          // clean up
          stateTable.remove(uid);
      }
  }
    
  public ChannelRuntimeProperties getRuntimeProperties (String uid)  
  {
    ChannelRuntimeProperties rp=new ChannelRuntimeProperties();
    
    // determine if such channel is registered
    if (stateTable.get(uid) == null) 
    {
      rp.setWillRender(false);
      LogService.instance().log(LogService.ERROR,"CGenericXSLT:getRuntimeProperties() : attempting to access a non-established channel! setStaticData() has never been called on the uid=\""+uid+"\"");
    }
    return rp;
  }

  public void renderXML(DocumentHandler out,String uid) throws PortalException  
  {
    CState state=(CState)stateTable.get(uid);
	  
    if (state == null) 
      LogService.instance().log(LogService.ERROR,"CGenericXSLT:renderXML() : attempting to access a non-established channel! setStaticData() has never been called on the uid=\""+uid+"\"");
    else 
    {
      String xml;
      Document xmlDoc;

      try 
      {
        org.apache.xerces.parsers.DOMParser domParser = new org.apache.xerces.parsers.DOMParser();
        org.jasig.portal.utils.DTDResolver dtdResolver = new org.jasig.portal.utils.DTDResolver();
        domParser.setEntityResolver(dtdResolver);
        domParser.parse(UtilitiesBean.fixURI(state.xmlUri));
        xmlDoc = domParser.getDocument();
      }
      catch (IOException e) 
      {
        throw new ResourceMissingException (state.xmlUri, "", e.getMessage());
      }
      catch (SAXException se) 
      {
        throw new GeneralRenderingException("Problem parsing " + state.xmlUri + ": " + se);
      }

      state.runtimeData.put("baseActionURL", state.runtimeData.getBaseActionURL());
	    
      XSLT xslt = new XSLT();
      xslt.setXML(xmlDoc);
      if (state.xslUri != null)
        xslt.setXSL(state.xslUri);
      else 
        xslt.setXSL(state.sslUri, state.xslTitle, state.runtimeData.getBrowserInfo());
      xslt.setTarget(out);
      xslt.setStylesheetParameters(state.runtimeData);
      xslt.transform();
    }
  }

  public ChannelCacheKey generateKey(String uid) 
  {
    CState state = (CState)stateTable.get(uid);
	
    if (state == null) 
    {
      LogService.instance().log(LogService.ERROR,"CGenericXSLT:generateKey() : attempting to access a non-established channel! setStaticData() has never been called on the uid=\""+uid+"\"");
      return null;
    } 
    else 
    {
      ChannelCacheKey k = new ChannelCacheKey();
      k.setKey(this.getKey(state));
      k.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
      k.setKeyValidity(new Long(System.currentTimeMillis()));
      return k;
    }
  }

  public boolean isCacheValid(Object validity,String uid) 
  {
    if (!(validity instanceof Long)) 
      return false;
	  
    CState state = (CState)stateTable.get(uid);
	
    if (state == null) 
    {
      LogService.instance().log(LogService.ERROR,"CGenericXSLT:isCacheValid() : attempting to access a non-established channel! setStaticData() has never been called on the uid=\""+uid+"\"");
      return false;
    } 
    else 
      return (System.currentTimeMillis() - ((Long)validity).longValue() < 15*60*1000);
  }

  private static String getKey(CState state)
  {
    // Maybe not the best way to generate a key, but it seems to work.
    // If you know a better way, please change it!
    StringBuffer sbKey = new StringBuffer(1024);
    sbKey.append(systemCacheId).append(": ");
    sbKey.append("xmluri:").append(state.xmlUri).append(", ");
    sbKey.append("sslUri:").append(state.sslUri).append(", ");
    
    // xslUri may either be specified as a parameter to this channel or we will
    // get it by looking in the stylesheet list file
    String xslUriForKey = null;
    try {
      xslUriForKey = state.xslUri != null ? state.xslUri : XSLT.getStylesheetURI(state.sslUri, state.runtimeData.getBrowserInfo());
    } catch (PortalException pe) {
      xslUriForKey = "Not attainable!";
    }
    
    sbKey.append("xslUri:").append(xslUriForKey).append(", ");
    sbKey.append("params:").append(state.runtimeData.toString());
    return sbKey.toString();
  }
}
