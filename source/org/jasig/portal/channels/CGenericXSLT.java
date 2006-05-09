/* Copyright 2001, 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.jasig.portal.utils.uri.BlockedUriException;
import org.jasig.portal.utils.uri.IUriScrutinizer;
import org.jasig.portal.utils.uri.PrefixUriScrutinizer;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;

/**
 * <p>A channel which transforms XML for rendering in the portal.</p>
 *
 * <p>Static channel parameters to be supplied:
 * <ol>
 *  <li> "xmlUri" - a URI representing the source XML document</li>
 *  <li> "sslUri" - a URI representing the corresponding .ssl (stylesheet list) file</li>
 *  <li> "xslTitle" - a title representing the stylesheet (optional)
 *                  <i>If no title parameter is specified, a default
 *                  stylesheet will be chosen according to the media</i>
 *  </li>
 *  <li> "xslUri" - a URI representing the stylesheet to use
 *                  <i>If <code>xslUri</code> is supplied, <code>sslUri</code>
 *                  and <code>xslTitle</code> will be ignored.</i>
 *  </li>
 *  <li> "cacheTimeout" - the amount of time (in seconds) that the contents of the
 *                  channel should be cached (optional).  If this parameter is left
 *                  out, a default timeout value will be used.
 *  </li>          
 *  <li> "upc_localConnContext" - The class name of the ILocalConnectionContext 
 *                  implementation.
 *                  <i>Use when local data needs to be sent with the
 *                  request for the URL.</i>
 *  </li>
 *  <li> "upc_allow_xmlUri_prefixes" - Optional parameter specifying as a whitespace
 *                  delimited String the allowable xmlUri prefixes.  
 *                  <i>Defaults to "http:// https://"</i>
 *  </li>
 *  <li> "upc_deny_xmlUri_prefixes" - Optional parameter specifying as a whitespace
 *                  delimited String URI prefixes that should block a URI 
 *                  as xmlUri even if it matched one of the allow prefixes.
 *                  <i>Defaults to ""</i>
 *  </li>
 *  <li> "restrict_xmlUri_inStaticData" - Optional parameter specifying whether 
 *                  the xmlUri should be restricted according to the allow and
 *                  deny prefix rules above as presented in ChannelStaticData
 *                  or just as presented in ChannelRuntimeData.  "true" means
 *                  both ChannelStaticData and ChannelRuntimeData will be restricted.
 *                  Any other value or the parameter not being present means 
 *                  only ChannelRuntimeData will be restricted.  It is important
 *                  to set this value to true when using subscribe-time
 *                  channel parameter configuration of the xmlUri.
 * </p>
 * <p>The xmlUri and xslTitle static parameters above can be overridden by including
 * parameters of the same name (<code>xmlUri</code> and/or <code>xslTitle</code>) 
 * in the HttpRequest string.  Prior to uPortal 2.5.1 sslUri and xslUri could also
 * be overridden -- these features have been removed to improve the security of
 * CGenericXSLT instances. </p>
 * <p>
 * Additionally, as of uPortal 2.5.1, the xmlUri must match an allowed URI prefix.
 * By default http:// and https:// URIs are allowed.  If you are using the 
 * empty document or another XML file from the classpath or from the filesystem,
 * you will need to allow a prefix to or the full path of that resource.
 * </p>
 * <p>This channel can be used for all XML formats including RSS.
 * Any other parameters passed to this channel via HttpRequest will get
 * passed in turn to the XSLT stylesheet as stylesheet parameters. They can be
 * read in the stylesheet as follows:
 * <code>&lt;xsl:param name="yourParamName"&gt;aDefaultValue&lt;/xsl:param&gt;</code></p>
 * <p>CGenericXSLT is also useful for channels that have no dynamic data.  In these types
 * of channels, all the markup comes from the XSLT stylesheets.  An empty XML document
 * can be used and is included with CGenericXSLT.  Just set the xml parameter to this
 * empty document and allow the path to the empty document.</p>
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
    /**
     * URI from which this channel will obtain XML to render.
     * Do not set this field directly.  Instead call the setter method, which
     * provides argument checking.
     */
    private String xmlUri;
    private String sslUri;
    private String xslTitle;
    private String xslUri;
    private Map params;
    private long cacheTimeout;
    private ChannelRuntimeData runtimeData;
    private LocalConnectionContext localConnContext;

    private final IUriScrutinizer uriScrutinizer;
    
    public CState(final IUriScrutinizer uriScrutinizerArg)
    {
        if (uriScrutinizerArg == null) {
            throw new IllegalArgumentException("CGenericXSLT channel state requires a non-null IUriScrutinizer");
        }
        this.uriScrutinizer = uriScrutinizerArg;
        
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
    
    /**
     * Set the URI or resource-relative-path of the XML this CGenericXSLT should
     * render.
     * @param xmlUriArg URI or local resource path to the XML this channel should render.
     * @throws IllegalArgumentException if xmlUriArg specifies a missing resource
     * or if the URI has bad syntax
     * @throws BlockedUriException if the xmlUriArg is blocked for policy reasons
     */
    public void setXmlUri(String xmlUriArg) {
        URL url = null;
        try {
            url = ResourceLoader.getResourceAsURL(this.getClass(), xmlUriArg);
        } catch (ResourceMissingException e) {
            IllegalArgumentException iae = new IllegalArgumentException("Resource [" + xmlUriArg + "] missing.");
            iae.initCause(e);
            throw iae;
        }
        
        String urlString = url.toExternalForm();
        try {
            this.uriScrutinizer.scrutinize(new URI(urlString));
        }catch (URISyntaxException e1) {
        	IllegalArgumentException iae = new IllegalArgumentException("xmlUri [" + xmlUriArg + "] resolved to a URI with bad syntax.");
            iae.initCause(e1);
            throw iae;
        }
        
        this.xmlUri = xmlUriArg;
    }
  }
  
  public CGenericXSLT()
  {
    stateTable = Collections.synchronizedMap(new HashMap());
  }

  public void setStaticData (ChannelStaticData sd, String uid) throws ResourceMissingException
  {
      
      String allowXmlUriPrefixesParam = 
          sd.getParameter("upc_allow_xmlUri_prefixes");
      String denyXmlUriPrefixesParam = 
          sd.getParameter("upc_deny_xmlUri_prefixes");
      
      IUriScrutinizer uriScrutinizer = 
          PrefixUriScrutinizer.instanceFromParameters(allowXmlUriPrefixesParam, denyXmlUriPrefixesParam);
      
    CState state = new CState(uriScrutinizer);
    
    // determine whether we should restrict what URIs we accept as the xmlUri from
    // ChannelStaticData
    String scrutinizeXmlUriAsStaticDataString = sd.getParameter("restrict_xmlUri_inStaticData");
    boolean scrutinizeXmlUriAsStaticData = "true".equals(scrutinizeXmlUriAsStaticDataString);
    
    String xmlUriParam = sd.getParameter("xmlUri");
    if (scrutinizeXmlUriAsStaticData) {
        // apply configured xmlUri restrictions
        state.setXmlUri(xmlUriParam);
    } else {
        // set the field directly to avoid applying xmlUri restrictions
        state.xmlUri = xmlUriParam;
    }
    
    
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
        log.error( "CGenericXSLT: Cannot initialize ILocalConnectionContext: ", e);
      }
    }
    state.params.putAll(sd);
    
    stateTable.put(uid,state);
    
  }

  public void setRuntimeData (ChannelRuntimeData rd, String uid)
  {
    CState state = (CState)stateTable.get(uid);

    if (state == null){
      log.debug("CGenericXSLT:setRuntimeData() : no entry in state for uid=\""+uid+"\"");
    }
    else
    {
      // because of the portal rendering model, there is no reason to synchronize on state
      state.runtimeData=rd;
      String xmlUri = rd.getParameter("xmlUri");

      if (xmlUri != null)
        state.setXmlUri(xmlUri);

      // prior to uPortal 2.5.1 sslUri was configurable via ChannelRuntimeProperties
      // this feature has been removed to improve security of CGenericXSLT instances.

      String xslTitle = rd.getParameter("xslTitle");

      if (xslTitle != null)
        state.xslTitle = xslTitle;

      // grab the parameters and stuff them all into the state object        
      Enumeration enum1 = rd.getParameterNames();
      while (enum1.hasMoreElements()) {
         String n = (String)enum1.nextElement();
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
      log.debug("CGenericXSLT:getRuntimeProperties() : no entry in state for uid=\""+uid+"\"");
    }
    return rp;
  }

  public void renderXML(ContentHandler out,String uid) throws PortalException
  {
    CState state=(CState)stateTable.get(uid);

    if (state == null){
        log.debug("CGenericXSLT:renderXML() : no entry in state for uid=\""+uid+"\"");
    }else{
      if (log.isDebugEnabled())
          log.debug("CGenericXSLT::renderXML() : state = " + state );
      
      // OK, pass everything we got cached in params...
      if (state.params != null) {
          Iterator it = state.params.keySet().iterator();
          while (it.hasNext()) {
              String n = (String) it.next();
              if (state.params.get((Object) n) != null) {
                  state.runtimeData.put(n, state.params.get((Object) n));
              }
          }
      }

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
            log.error( "CGenericXSLT: Unable to send data through " + state.runtimeData.getParameter("upc_localConnContext"), e);
          }
        }
        inputStream = urlConnect.getInputStream();
        xmlDoc = docBuilder.parse(inputStream);
      }
      catch (IOException ioe)
      {
        throw new ResourceMissingException (state.xmlUri, "", ioe);
      }
      catch (Exception e)
      {
        throw new GeneralRenderingException("Problem parsing " + state.xmlUri + ": " , e);
      } finally {
        try {
          if (inputStream != null)
            inputStream.close();
        } catch (IOException ioe) {
          throw new PortalException("CGenericXSLT:renderXML():: could not close InputStream",ioe);
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
      log.debug("CGenericXSLT:generateKey() : no entry in state for uid=\""+uid+"\"");
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
      log.debug("CGenericXSLT:isCacheValid() : no entry in state for uid=\""+uid+"\"");
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
      log.error(e,e);
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
