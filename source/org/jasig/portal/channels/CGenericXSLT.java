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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.IMultithreadedCacheable;
import org.jasig.portal.IMultithreadedChannel;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.Version;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.LocalConnectionContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.DTDResolver;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;

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
 *  5) "cacheTimeout" - the amount of time (in seconds) that the contents of the
 *                  channel should be cached (optional).  If this parameter is left
 *                  out, a default timeout value will be used.
 *  6) "upc_localConnContext" - The class name of the ILocalConnectionContext 
 *                  implementation.
 *                  <i>Use when local data needs to be sent with the
 *                  request for the URL.</i>
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
 * can be used and is included with CGenericXSLT.  Just set the xml parameter to this
 * empty document.</p>
 * @author Steve Toth, stoth@interactivebusiness.com
 * @author Ken Weiner, kweiner@unicon.net
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a> (multithreading,caching)
 * @version $Revision$
 */
public class CGenericXSLT implements IMultithreadedChannel, IMultithreadedCacheable
{
    private static final Log log = LogFactory.getLog(CGenericXSLT.class);
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
    private Map params;
    private long cacheTimeout;
    private ChannelRuntimeData runtimeData;
    private LocalConnectionContext localConnContext;

    public CState()
    {
      xmlUri = sslUri = xslTitle = xslUri = null;
      params = new HashMap();
      cacheTimeout = PropertiesManager.getPropertyAsLong("org.jasig.portal.channels.CGenericXSLT.default_cache_timeout");
      runtimeData = null;
      localConnContext = null;
    }
    
    public String toString()
    {
       StringBuffer str = new StringBuffer();
       str.append("xmlUri = "+xmlUri+"\n");
       str.append("xslUri = "+xslUri+"\n");
       str.append("sslUri = "+sslUri+"\n");
       str.append("xslTitle = "+xslTitle+"\n");
       if (params != null) {
          str.append("params = "+params.toString()+"\n");
       }
       return str.toString();
    }
  }
  
  public CGenericXSLT()
  {
    stateTable = Collections.synchronizedMap(new HashMap());
  }

  public void setStaticData (ChannelStaticData sd, String uid) throws ResourceMissingException
  {
    CState state = new CState();
    state.xmlUri = sd.getParameter("xmlUri");
    state.sslUri = sd.getParameter("sslUri");
    state.xslTitle = sd.getParameter("xslTitle");
    state.xslUri = sd.getParameter("xslUri");

    String cacheTimeout = sd.getParameter("cacheTimeout");

    if (cacheTimeout != null)
      state.cacheTimeout = Long.parseLong(cacheTimeout);

    String connContext = sd.getParameter ("upc_localConnContext");
    if (connContext != null)
    {
      try
      {
        state.localConnContext = (LocalConnectionContext) Class.forName(connContext).newInstance();
        state.localConnContext.init(sd);
      }
      catch (Exception e)
      {
        log.error( "CGenericXSLT: Cannot initialize ILocalConnectionContext: " + e);
      }
    }

    stateTable.put(uid,state);
  }

  public void setRuntimeData (ChannelRuntimeData rd, String uid)
  {
    CState state = (CState)stateTable.get(uid);

    if (state == null)
      log.error("CGenericXSLT:setRuntimeData() : attempting to access a non-established channel! setStaticData() has never been called on the uid=\""+uid+"\"");
    else
    {
      // because of the portal rendering model, there is no reason to synchronize on state
      state.runtimeData=rd;
      String xmlUri = rd.getParameter("xmlUri");

      if (xmlUri != null)
        state.xmlUri = xmlUri;

      String sslUri = rd.getParameter("sslUri");

      if (sslUri != null)
        state.sslUri = sslUri;

      String xslTitle = rd.getParameter("xslTitle");

      if (xslTitle != null)
        state.xslTitle = xslTitle;

      String xslUri = rd.getParameter("xslUri");

      if (xslUri != null)
        state.xslUri = xslUri;

      // grab the parameters and stuff them all into the state object        
      Enumeration enum = rd.getParameterNames();
      while (enum.hasMoreElements()) {
         String n = (String)enum.nextElement();
         if (rd.getParameter(n) != null) {
            state.params.put(n,rd.getParameter(n));
         }
      }
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
      log.error("CGenericXSLT:getRuntimeProperties() : attempting to access a non-established channel! setStaticData() has never been called on the uid=\""+uid+"\"");
    }
    return rp;
  }

  public void renderXML(ContentHandler out,String uid) throws PortalException
  {
    CState state=(CState)stateTable.get(uid);

    if (state == null)
      log.error("CGenericXSLT:renderXML() : attempting to access a non-established channel! setStaticData() has never been called on the uid=\""+uid+"\"");
    else
    {
      log.debug("CGenericXSLT::renderXML() : state = " + state.toString() );

      String xml;
      Document xmlDoc;
      InputStream inputStream = null;

      try
      {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        DTDResolver dtdResolver = new DTDResolver();
        docBuilder.setEntityResolver(dtdResolver);

        URL url;
        if (state.localConnContext != null)
          url = ResourceLoader.getResourceAsURL(this.getClass(), state.localConnContext.getDescriptor(state.xmlUri, state.runtimeData));
        else
          url = ResourceLoader.getResourceAsURL(this.getClass(), state.xmlUri);

        URLConnection urlConnect = url.openConnection();

        if (state.localConnContext != null)
        {
          try
          {
            state.localConnContext.sendLocalData(urlConnect, state.runtimeData);
          }
          catch (Exception e)
          {
            log.error( "CGenericXSLT: Unable to send data through " + state.runtimeData.getParameter("upc_localConnContext") + ": " + e.getMessage());
          }
        }
        inputStream = urlConnect.getInputStream();
        xmlDoc = docBuilder.parse(inputStream);
      }
      catch (IOException ioe)
      {
        throw new ResourceMissingException (state.xmlUri, "", ioe.getMessage());
      }
      catch (Exception e)
      {
        throw new GeneralRenderingException("Problem parsing " + state.xmlUri + ": " + e);
      } finally {
        try {
          if (inputStream != null)
            inputStream.close();
        } catch (IOException ioe) {
          throw new PortalException("CGenericXSLT:renderXML():: could not close InputStream");
        }
      }

      state.runtimeData.put("baseActionURL", state.runtimeData.getBaseActionURL());
      state.runtimeData.put("isRenderingAsRoot", String.valueOf(state.runtimeData.isRenderingAsRoot()));
      
      // Add uPortal version string (used in footer channel)
      state.runtimeData.put("uP_productAndVersion", Version.getProductAndVersion());
      
      // OK, pass everything we got cached in params...
      if (state.params != null)
      {
         Iterator it = state.params.keySet().iterator();
         while (it.hasNext()) {
            String n = (String)it.next();
            if (state.params.get((Object)n) != null) {
               state.runtimeData.put(n,state.params.get((Object)n));
            }
         }
      }

      XSLT xslt = XSLT.getTransformer(this);
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
      log.error("CGenericXSLT:generateKey() : attempting to access a non-established channel! setStaticData() has never been called on the uid=\""+uid+"\"");
      return null;
    }
    else
    {
      ChannelCacheKey k = new ChannelCacheKey();
      k.setKey(this.getKey(state)+","+uid);
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
      log.error("CGenericXSLT:isCacheValid() : attempting to access a non-established channel! setStaticData() has never been called on the uid=\""+uid+"\"");
      return false;
    }
    else
      return (System.currentTimeMillis() - ((Long)validity).longValue() < state.cacheTimeout*1000);
  }

  private String getKey(CState state)
  {
    // Maybe not the best way to generate a key, but it seems to work.
    // If you know a better way, please change it!
    StringBuffer sbKey = new StringBuffer(1024);
    sbKey.append(systemCacheId).append(": ");
    sbKey.append("xmluri:").append(state.xmlUri).append(", ");
    sbKey.append("sslUri:").append(state.sslUri).append(", ");

    // xslUri may either be specified as a parameter to this channel or we will
    // get it by looking in the stylesheet list file
    String xslUriForKey = state.xslUri;
    try {
      if (xslUriForKey == null) {
        String sslUri = ResourceLoader.getResourceAsURLString(CGenericXSLT.class, state.sslUri);
        xslUriForKey = XSLT.getStylesheetURI(sslUri, state.runtimeData.getBrowserInfo());
      }
    } catch (Exception e) {
      xslUriForKey = "Not attainable: " + e;
    }

    sbKey.append("locales:").append(LocaleManager.stringValueOf(state.runtimeData.getLocales()));
    sbKey.append("xslUri:").append(xslUriForKey).append(", ");
    sbKey.append("cacheTimeout:").append(state.cacheTimeout).append(", ");
    sbKey.append("isRenderingAsRoot:").append(state.runtimeData.isRenderingAsRoot()).append(", ");

    // If a local connection context is configured, include its descriptor in the key
    if (state.localConnContext != null)
      sbKey.append("descriptor:").append(state.localConnContext.getDescriptor(state.xmlUri, state.runtimeData)).append(", ");    

    sbKey.append("params:").append(state.params.toString());
    return sbKey.toString();
  }
}
