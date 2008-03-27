/* Copyright 2002, 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.webproxy;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.ICacheable;
import org.jasig.portal.IChannel;
import org.jasig.portal.IMimeResponse;
import org.jasig.portal.MediaManager;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.LocalConnectionContext;
import org.jasig.portal.utils.AbsoluteURLFilter;
import org.jasig.portal.utils.CookieCutter;
import org.jasig.portal.utils.DTDResolver;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.utils.cache.CacheFactory;
import org.jasig.portal.utils.cache.CacheFactoryLocator;
import org.jasig.portal.utils.uri.BlockedUriException;
import org.jasig.portal.utils.uri.IUriScrutinizer;
import org.jasig.portal.utils.uri.PrefixUriScrutinizer;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.ContentHandler;

/**
 * <p>A channel which transforms and interacts with dynamic XML or HTML.
 *    See docs/website/developers/channel_docs/reference/CwebProxy.html
 *    for full documentation.
 * </p>
 *
 *<p>Static and Runtime Channel Parameters:
 *   These parameters can be configured both as ChannelStaticData parameters and as
 *   ChannelRuntimeData parameters.
 *   These static parameters can be updated by equivalent
 *    Runtime parameters.  Caching parameters can also be changed temporarily.
 *    Cache defaults and IPerson restrictions are loaded first from properties,
 *    and overridden by static data if there.
 *</p>
 *<ol>
 *  <li>"cw_xml" - a URI for the source XML document.  By default, must be an
 *      http:// or https:// URI, though this requirement is configurable via
 *      ChannelStaticData parameters.
 *  <li>"cw_xslTitle" - a title representing the stylesheet (optional)
 *                  <i>If no title parameter is specified, a default
 *                  stylesheet will be chosen according to the media</i>
 *  <li>"cw_cacheDefaultMode" - Default caching mode.
 *          <i>May be <code>none</code> (normally don't cache), or
 *          <code>all</code> (cache everything).</i>
 *  <li>"cw_cacheDefaultTimeout" - Default timeout in seconds.
 *  <li>"cw_cacheMode" - override default for this request only.
 *          <i>Primarily intended as a runtime parameter, but can
 *              used statically to override the first instance.</i>
 *  <li>"cw_cacheTimeout" - override default for this request only.
 *          <i>Primarily intended as a runtime parameter, but can
 *              be used statically to override the first instance.</i>
 *</ol>
 *
 * <p>Static Channel Parameters:
      These parameters can be configured only as ChannelStaticData parameters.
      They can no longer (as of uPortal 2.5.1) be changed at runtime.  This closes
      some serious security vulnerabilities wherein the Adversary would manipulate
      these parameters at runtime to access resources on the local filesystem.
 * </p>
 * <ol>
 *  <li>"cw_ssl" - a URI specifying the corresponding .ssl (stylesheet list) file
 *  <li>"cw_xsl" - a URI specifying the stylesheet to use
 *                  <i>If <code>cw_xsl</code> is supplied, <code>cw_ssl</code>
 *                  and <code>cw_xslTitle</code> will be ignored.</i>
 *  <li>"cw_passThrough" - indicates how RunTimeData is to be passed through.
 *                  <i>If <code>cw_passThrough</code> is supplied, and not set
 *          to "all" or "application", additional RunTimeData
 *          parameters not starting with "cw_" or "upc_" will be
 *          passed as request parameters to the XML URI.  If
 *          <code>cw_passThrough</code> is set to "marked", this will
 *          happen only if there is also a RunTimeData parameter of
 *          <code>cw_inChannelLink</code>.  "application" is intended
 *          to keep application-specific links in the channel, while
 *          "all" should keep all links in the channel.  This
 *          distinction is handled entirely in the URL Filters.</i>
 *  <li>"cw_tidy" - output from <code>xmlUri</code> will be passed though Jtidy
 *  <li>"cw_info" - a URI to be called for the <code>info</code> event.
 *  <li>"cw_help" - a URI to be called for the <code>help</code> event.
 *  <li>"cw_edit" - a URI to be called for the <code>edit</code> event.
 *  <li>"cw_person" - IPerson attributes to pass.
 *          <i>A comma-separated list of IPerson attributes to
 *          pass to the back end application.  The static data
 *          value will be passed on </i>all<i> requests except some
 *          refresh requests.</i>
 *  <li>"cw_personAllow" - Restrict IPerson attribute passing to this list.
 *          <i>A comma-separated list of IPerson attributes that
 *          may be passed via cw_person.  An empty or non-existent
 *          value means use the default value from the corresponding
 *          property.  The special value "*" means all attributes
 *          are allowed.  The value "!*" means none are allowed.
 *          Static data only.</i>
 *  <li>"upc_localConnContext" - LocalConnectionContext implementation class.
 *                  <i>The name of a class to use when data sent to the
 *                  backend application needs to be modified or added
 *                  to suit local needs.  Static data only.</i>
 *  <li>"cw_allow_uri_prefixes" - permitted URI prefixes.
 *         <i>Optional static data only parameter specifying allowable prefixes
 *            for URIs accessed by this channel.  This channel will only use
 *            URIs with these prefixes for obtaining XML and XSLT for use in
 *            rendering.  Whitespace delimit allowed prefixes.  Effectively
 *            defaults to "http:// https://"; do not allow "file:/" lightly.</i>
 *       </li>
 *  <li>"cw_block_uri_prefixes" - blocked URI prefixes.
 *         <i>Optional static data only parameter further restricting which
 *            URIs this channel will use to obtain XML and XSLT.  This channel
 *            will not use URIs matching prefixes specified in this whitespace-
 *            delimited parameter.  Effectively defaults to "".</i>
 *  </li>
 *  <li>"cw_restrict_xmlUri_inStaticData" - Optional parameter specifying whether
 *                  the xmlUri should be restricted according to the allow and
 *                  deny prefix rules above as presented in ChannelStaticData
 *                  or just as presented in ChannelRuntimeData.  "true" means
 *                  both ChannelStaticData and ChannelRuntimeData will be restricted.
 *                  Any other value or the parameter not being present means
 *                  only ChannelRuntimeData will be restricted.  It is important
 *                  to set this value to true when using subscribe-time
 *                  channel parameter configuration of the xmlUri.
 *  </li>
 *  <li>"cw_cacheGlobalMode" - should content be cached globally
 *          <i>May be <code>false</code> (normally don't globally cache), or
 *          <code>true</code> (cache everything).</i>
 *  </li>
 * </ol>
 * <p>Runtime Channel Parameters:</p>
 *    The following parameters are runtime-only.
 * </p>
 * <ol>
 *  <li>"cw_reset" - an instruction to return to reset internal variables.
 *         <i>The value <code>return</code> resets <code>cw_xml</code>
 *         to its last value before changed by button events.  The
 *         value "reset" returns all variables to the static data
 *         values.</i>
 *  <li>"cw_download" - use download worker for this link or form
 *                 <i>any link or form that contains this parameter will be
 *                 handled by the download worker, if the pass-through mode
 *                 is set to rewrite the link or form.  This allows downloads
 *                 from the proxied site to be delivered via the portal,
 *                 primarily useful if the download requires verification
 *                 of a session referenced by a proxied cookie</i>
 *
 * </ol>
 * <p>This channel can be used for all XML formats with appropriate stylesheets.
 *    All static data parameters as well as additional runtime data parameters
 *    passed to this channel via HttpRequest will in turn be passed on to the
 *    XSLT stylesheet as stylesheet parameters.  They can be read in the
 *    stylesheet as follows:
 *    <code>&lt;xsl:param
 *    name="yourParamName"&gt;aDefaultValue&lt;/xsl:param&gt;</code>
 * </p>
 * @author Andrew Draskoy, andrew@mun.ca
 * @author Sarah Arnott, sarnott@mun.ca
 * @version $Revision$
 */
public class CWebProxy implements IChannel, ICacheable, IMimeResponse

{
    private static final Log log = LogFactory.getLog(CWebProxy.class);
    private static final Log accessLog = LogFactory.getLog(CWebProxy.class+".access");

    /**
     * The name of the optional ChannelStaticData parameter the value of
     * which will be a String containing a whitespace-delimited list of allowable
     * URI prefixes for URIs from which the configured CWebProxy instance will
     * obtain XML and XSLT.  Defaults to allowing all URIs of the http://
     * and https:// methods.
     */
    public static final String ALLOW_URI_PREFIXES_PARAM = "cw_allow_uri_prefixes";

    /**
     * The name of the optional ChannelStaticData parameter the value of which
     * will be a String containing a whitespace-delimited list of explicitly
     * blocked URI prefixes for URIs from which the configured CWebProxy
     * instance will not obtain XML and XSLT.  URIs matching these prefixes
     * will not be used even if they also match an allow prefix specified in
     * ALLOW_URI_PREFIXES_PARAM.
     */
    public static final String BLOCK_URI_PREFIXES_PARAM = "cw_block_uri_prefixes";

    /**
     * The name of the optional ChannelStaticData parameter the value of which
     * will be the String "true" to convey that the xmlUri should be restricted
     * as received from both ChannelStaticData and CHannelRuntimeData and
     * any other value to convey that it should be restricted only as received
     * from ChannelRuntimeData.
     *
     * CWebProxy should be configured to restrict xmlUri from ChannelStaticData
     * in the case where the xmlUri is a subscribe-time configurable parameter.
     */
    public static final String RESTRICT_STATIC_XMLURI_PREFIXES_PARAM = "cw_restrict_xmlUri_inStaticData";

    private static final MediaManager MEDIAMANAGER = MediaManager.getMediaManager();
  // to prepend to the system-wide cache key
  static final String systemCacheId="org.jasig.portal.channels.webproxy.CWebProxy";
  static PrintWriter devNull;

  ChannelState chanState;

  // the content cache shared by ALL users
  private static Map<Serializable, Object> contentCache;

  static {

	  try {
      devNull = getErrout();
    } catch (FileNotFoundException fnfe) {
      /* Ignore */
    }

    // retrieve our content cache. this cache will contain
    // tidied xml (as a String) and parsed XML (Document) Objects.
    // it is a global cache, with content shared between all
    // users who use this channel instance.
	contentCache = CacheFactoryLocator.getCacheFactory().getCache(CacheFactory.CONTENT_CACHE);

  }

  /**
   * All state variables are stored in this inner class.
   * It would probably be an improvement to extract this inner class into its own
   * fully fledged class in the cwebproxy package, thereby enforcing that its
   * properties are only accessed via getter and setter methods that would
   * need to be added.
   */
  private class ChannelState
  {
	private String publishId;
    private IPerson iperson;
    private String person;
    private String personAllow;
    private HashSet personAllow_set;
    private String fullxmlUri;

    /**
     * URI of the source of XML this CWebProxy instance will render in response
     * to a recent button press (channel control button, e.g. "help").
     */
    private String buttonxmlUri;

    /**
     * URI of the source of XML this CWebProxy instance will render.
     * Do not set this field directly.  Instead, access it via the setter
     * method.
     */
    private String xmlUri;
    private String key;
    private String passThrough;
    private String tidy;

    /**
     * URI of the stylesheet selector this channel will use to select its
     * XSLT.
     */
    private String sslUri;
    private String xslTitle;

    /**
     * URI of the XSLT this channel will use to select its XSLT.
     */
    private String xslUri;

    /**
     * URI of the XML this channel will use to render its info mode.
     */
    private String infoUri;

    /**
     * URI of the XML this channel will use to render its help mode.
     */
    private String helpUri;

    /**
     * URI of the XML this channel will use to render its edit mode.
     */
    private String editUri;

    private String cacheDefaultMode;
    private String cacheMode;
    private String reqParameters;
    private long cacheDefaultTimeout;
    private long cacheTimeout;
    private ChannelRuntimeData runtimeData;
    private CookieCutter cookieCutter;
    private URLConnection connHolder;
    private LocalConnectionContext localConnContext;
    private int refresh;

    private boolean cacheGlobalMode;

    //  last time the content was loaded (instance shared by ALL users)
    private long cacheContentLoaded;

    /**
     * The default scrutinizer is one that allows only http: and https: URIs.
     * Non-null.  Constructor enforces initialization to a non-null.
     */
    private final IUriScrutinizer uriScrutinizer;

    public ChannelState (IUriScrutinizer uriScrutinizerArg)
    {
        if (uriScrutinizerArg == null) {
            throw new IllegalArgumentException("Cannot instantiate CWebProxy ChannelState with a null URI srutinizer.");
        }
        this.uriScrutinizer = uriScrutinizerArg;

      fullxmlUri = buttonxmlUri = xmlUri = key = passThrough = sslUri = null;
      xslTitle = xslUri = infoUri = helpUri = editUri = tidy = null;
      cacheMode = null;
      iperson = null;
      publishId = null;
      refresh = -1;
      cacheTimeout = cacheDefaultTimeout = PropertiesManager.getPropertyAsLong("org.jasig.portal.channels.webproxy.CWebProxy.cache_default_timeout");
      cacheMode = cacheDefaultMode = PropertiesManager.getProperty("org.jasig.portal.channels.webproxy.CWebProxy.cache_default_mode");
      personAllow = PropertiesManager.getProperty("org.jasig.portal.channels.webproxy.CWebProxy.person_allow");
      cacheGlobalMode = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.channels.webproxy.CWebProxy.cache_global_mode", false);
      runtimeData = null;
      cookieCutter = new CookieCutter();
      localConnContext = null;
    }

    /**
     * Set the xmlUri channel state property, applying URI acceptance logic
     * before accepting the parameter.
     * @param uriArg URI of XML source, or null
     * @throws IllegalArgumentException if the uriArg is not in URI syntax or is
     * a non-URI classpath-relative path which doesn't map to an actually existing resource.
     * @throws BlockedUriException if the URI is unacceptable for reasons of policy
     * @since uPortal 2.5.1
     */
    public void setXmlUri(String uriArg) {
        if (uriArg != null && !"".equals(uriArg)) {
            // resolve partial relative path fragments per ResourceLoader
            try {
                URL resourceUrl = ResourceLoader.getResourceAsURL(this.getClass(), uriArg);
                uriArg = resourceUrl.toExternalForm();
            } catch (ResourceMissingException rme) {
                IllegalArgumentException iae = new IllegalArgumentException("Resource [" + uriArg + "] missing.");
                iae.initCause(rme);
                throw iae;
            }

            try {
                this.uriScrutinizer.scrutinize(new URI(uriArg));
                this.xmlUri = uriArg;
            } catch (URISyntaxException e) {
                IllegalArgumentException iae = new IllegalArgumentException("Value [" + uriArg
                        + "]had bad URI syntax.");
                iae.initCause(e);
                throw iae;
            }
        }
        // if uriArg was null do nothing.

    }

    /**
     * Storing cache data is straigt-forward. We rely on the cache implementation
     * to remove data as it ages AND only allow a maximum amount of data into
     * the structure.
     *
     * @param key the URL key String
     * @param data our cache data as a String or Document Object
     */
    public synchronized void setCacheContent(String key, Object data) {
    	contentCache.put(key, data);
    	// we substract 2 seconds to the cache content loaded flag on purpose
    	// this is due to the framework running in a different thread and
    	// setting the validity of the content to a different time that
    	// what we have. This ensures us that we will function properly
    	cacheContentLoaded = System.currentTimeMillis() - 2000;
    }

    public synchronized Object getCacheContent(String key) {
    	return (contentCache.get(key));
    }

    public synchronized long getCacheContentLoaded() {
    	return (cacheContentLoaded);
    }

  }

  public CWebProxy () {}

  /**
   * Passes ChannelStaticData to the channel.
   * This is done during channel instantiation time.
   * see org.jasig.portal.ChannelStaticData
   * @param sd channel static data
   * @see ChannelStaticData
   */
  public void setStaticData (ChannelStaticData sd)
      throws PortalException {

    // detect static data configuration for URI scrutinizer

    String allowUriPrefixesParam =
        sd.getParameter(ALLOW_URI_PREFIXES_PARAM);
    String denyUriPrefixesParam =
        sd.getParameter(BLOCK_URI_PREFIXES_PARAM);


    // store the scrutinizer into channel state
    IUriScrutinizer uriScrutinizer =
        PrefixUriScrutinizer.instanceFromParameters(allowUriPrefixesParam, denyUriPrefixesParam);
    ChannelState state = new ChannelState(uriScrutinizer);

    // determine whether we should restrict what URIs we accept as the xmlUri from
    // ChannelStaticData
    String scrutinizeXmlUriAsStaticDataString = sd.getParameter(RESTRICT_STATIC_XMLURI_PREFIXES_PARAM);
    boolean scrutinizeXmlUriAsStaticData = "true".equals(scrutinizeXmlUriAsStaticDataString);

    String xmlUriParam = sd.getParameter("cw_xml");
    if (scrutinizeXmlUriAsStaticData) {
        // apply configured xmlUri restrictions
        state.setXmlUri(xmlUriParam);
    } else {
        // set the field directly to avoid applying xmlUri restrictions
        state.xmlUri = xmlUriParam;
    }

    state.iperson = sd.getPerson();
    state.publishId = sd.getChannelPublishId();
    state.person = sd.getParameter("cw_person");
    String personAllow = sd.getParameter ("cw_personAllow");
    if ( personAllow != null && (!personAllow.trim().equals("")))
      state.personAllow = personAllow;
    // state.personAllow could have been set by a property or static data
    if ( state.personAllow != null && (!state.personAllow.trim().equals("!*")) )
    {
      state.personAllow_set = new HashSet();
      StringTokenizer st = new StringTokenizer(state.personAllow,",");
      if (st != null)
      {
        while ( st.hasMoreElements () ) {
          String pName = st.nextToken();
          if (pName!=null) {
            pName = pName.trim();
            if (!pName.equals(""))
              state.personAllow_set.add(pName);
          }
        }
      }
    }


    state.sslUri = sd.getParameter("cw_ssl");
    state.xslTitle = sd.getParameter ("cw_xslTitle");
    state.xslUri = sd.getParameter("cw_xsl");
    state.fullxmlUri = sd.getParameter("cw_xml");

    state.passThrough = sd.getParameter ("cw_passThrough");
    state.tidy = sd.getParameter ("cw_tidy");

    state.infoUri = sd.getParameter("cw_info");
    state.helpUri = sd.getParameter("cw_help");
    state.editUri = sd.getParameter ("cw_edit");

    state.key = state.xmlUri;

    String cacheMode = sd.getParameter ("cw_cacheDefaultMode");
    if (cacheMode != null && !cacheMode.trim().equals(""))
      state.cacheDefaultMode = cacheMode;
    cacheMode = sd.getParameter ("cw_cacheMode");
    if (cacheMode != null && !cacheMode.trim().equals(""))
      state.cacheMode = cacheMode;
    else
      state.cacheMode = state.cacheDefaultMode;

    String cacheTimeout = sd.getParameter("cw_cacheDefaultTimeout");
    if (cacheTimeout != null && !cacheTimeout.trim().equals(""))
      state.cacheDefaultTimeout = Long.parseLong(cacheTimeout);
    cacheTimeout = sd.getParameter("cw_cacheTimeout");
    if (cacheTimeout != null && !cacheTimeout.trim().equals(""))
      state.cacheTimeout = Long.parseLong(cacheTimeout);
    else
      state.cacheTimeout = state.cacheDefaultTimeout;

    cacheMode = sd.getParameter("cw_cacheGlobalMode");
    if (cacheMode != null) {
    	cacheMode = cacheMode.trim().toLowerCase();
    	if ("true".equals(cacheMode)) {
    		state.cacheGlobalMode = true;
    	} else if ("false".equals(cacheMode)) {
    		state.cacheGlobalMode = false;
    	} else {
    		if (log.isWarnEnabled()) {
    			log.warn("Invalid channel [" + sd.getChannelPublishId() + ":"
    					+ state.fullxmlUri + "] parameter cw_cacheGlobalMode value ["
    					+ cacheMode + "]. Valid values are true|false. Defaulting to ["
    					+ state.cacheGlobalMode + "].");
    		}
    	}
    }

    String connContext = sd.getParameter ("upc_localConnContext");
    if (connContext != null && !connContext.trim().equals(""))
    {
      try
      {
        state.localConnContext = (LocalConnectionContext) Class.forName(connContext).newInstance();
        state.localConnContext.init(sd);
      }
      catch (Exception e)
      {
        log.error( "CWebProxy: Cannot initialize LocalConnectionContext: ", e);
      }
    }

    chanState = state;
  }

  /**
   * Passes ChannelRuntimeData to the channel.
   * This function is called prior to the renderXML() call.
   * @param rd channel runtime data
   * @see ChannelRuntimeData
   */
  public void setRuntimeData (ChannelRuntimeData rd)
  {
     ChannelState state = chanState;
     if (state == null){
       log.debug("CWebProxy:setRuntimeData() : no entry in state");
     }else{
       state.runtimeData = rd;
       if ( rd.isEmpty() && (state.refresh != -1) ) {
         // A refresh-- State remains the same.
         if ( state.buttonxmlUri != null ) {
           state.key = state.buttonxmlUri;
           state.fullxmlUri = state.buttonxmlUri;
           state.refresh = 0;
         } else {
           if ( state.refresh == 0 )
             state.key = state.fullxmlUri;
           state.fullxmlUri = state.xmlUri;
           state.refresh = 1;
         }
       } else {

       state.refresh = 0;

       String xmlUri = state.runtimeData.getParameter("cw_xml");
       if (xmlUri != null) {
         state.setXmlUri(xmlUri);
         // don't need an explicit reset if a new URI is provided.
         state.buttonxmlUri = null;
       }

       // prior to uPortal 2.5.1, CWebProxy allowed several other parameters
       // to  be set via channel runtime data.  These features were removed
       // to close security vulnerabilities.  Some (very few) uPortal deployments
       // will need to add these features back in or otherwise address them.

       String xslTitle = state.runtimeData.getParameter("cw_xslTitle");
       if (xslTitle != null)
          state.xslTitle = xslTitle;


       String passThrough = state.runtimeData.getParameter("cw_passThrough");
       if (passThrough != null)
          state.passThrough = passThrough;

       String cacheTimeout = state.runtimeData.getParameter("cw_cacheDefaultTimeout");
       if (cacheTimeout != null)
          state.cacheDefaultTimeout = Long.parseLong(cacheTimeout);

       cacheTimeout = state.runtimeData.getParameter("cw_cacheTimeout");
       if (cacheTimeout != null)
          state.cacheTimeout = Long.parseLong(cacheTimeout);
       else
          state.cacheTimeout = state.cacheDefaultTimeout;

       String cacheDefaultMode = state.runtimeData.getParameter("cw_cacheDefaultMode");
       if (cacheDefaultMode != null) {
          state.cacheDefaultMode = cacheDefaultMode;
       }

       String cacheMode = state.runtimeData.getParameter("cw_cacheMode");
       if (cacheMode != null) {
          state.cacheMode = cacheMode;
       } else
          state.cacheMode = state.cacheDefaultMode;

       // reset is a one-time thing.
       String reset = state.runtimeData.getParameter("cw_reset");
       if (reset != null) {
          if (reset.equalsIgnoreCase("return")) {
             state.buttonxmlUri = null;
          }
       }

       if ( state.buttonxmlUri != null )
           state.fullxmlUri = state.buttonxmlUri;
       else
       {
         //log.debug("CWebProxy: xmlUri is " + state.xmlUri);

         // pass IPerson atts independent of the value of cw_passThrough
         StringBuffer newXML = new StringBuffer();
         String appendchar = "";

         // here add in attributes according to cw_person
         if (state.person != null && state.personAllow_set != null)
         {
           StringTokenizer st = new StringTokenizer(state.person,",");
           if (st != null)
           {
             while (st.hasMoreElements ())
             {
               String pName = st.nextToken();
               if ((pName!=null)&&(!pName.trim().equals("")))
               {
                 if ( state.personAllow.trim().equals("*") ||
                      state.personAllow_set.contains(pName) )
                 {
                   newXML.append(appendchar);
                   appendchar = "&";
                   newXML.append(pName);
                   newXML.append("=");
                   // note, this only gets the first one if it's a
                   // java.util.Vector.  Should check
                   String pVal = (String)state.iperson.getAttribute(pName);
                   if (pVal != null)
                    try {
                        newXML.append(URLEncoder.encode(pVal,"UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                 }
                 else {
                     if (log.isInfoEnabled())
                         log.info(
                                 "CWebProxy: request to pass " + pName + " denied.");
                 }
               }
             }
           }
         }
         // end cw_person code

         // Is this a case where we need to pass request parameters to the xmlURI?
         if ( state.passThrough != null &&
            !state.passThrough.equalsIgnoreCase("none") &&
              ( state.passThrough.equalsIgnoreCase("all") ||
                state.passThrough.equalsIgnoreCase("application") ||
                rd.getParameter("cw_inChannelLink") != null ) )
         {
           // keyword and parameter processing
           // NOTE: if both exist, only keywords are appended
           String keywords = rd.getKeywords();
           if (keywords != null)
           {
             if (appendchar.equals("&"))
               newXML.append("&keywords=" + keywords);
             else
               newXML.append(keywords);
           }
           else
           {
             // want all runtime parameters not specific to WebProxy
             Enumeration e=rd.getParameterNames ();
             if (e!=null)
             {
               while (e.hasMoreElements ())
               {
                 String pName = (String) e.nextElement ();
                 if ( !pName.startsWith("cw_") && !pName.startsWith("upc_")
                                               && !pName.trim().equals(""))
                 {
                   String[] value_array = rd.getParameterValues(pName);
                   int i = 0;
                   while ( i < value_array.length )
                   {
                     newXML.append(appendchar);
                     appendchar = "&";
                     newXML.append(pName);
                     newXML.append("=");
                     try {
                        newXML.append(URLEncoder.encode(value_array[i++].trim(),"UTF-8"));
                    } catch (UnsupportedEncodingException e1) {
                        throw new RuntimeException(e1);
                    }
                   }
                 }
               }
             }
           }
         }

         state.reqParameters = newXML.toString();
         state.fullxmlUri = state.xmlUri;
         if (!state.runtimeData.getHttpRequestMethod().equals("POST"))
         {
           if ((state.reqParameters!=null) && (!state.reqParameters.trim().equals("")))
           {
             appendchar = (state.xmlUri.indexOf('?') == -1) ? "?" : "&";
             state.fullxmlUri = state.fullxmlUri+appendchar+state.reqParameters;
           }
           state.reqParameters = null;
         }

         //log.debug("CWebProxy: fullxmlUri now: " + state.fullxmlUri);
       }

       // set key for cache based on request parameters
       // NOTE: POST requests are not idempotent and therefore are not
       // retrievable from the cache
       if (!state.runtimeData.getHttpRequestMethod().equals("POST"))
         state.key = state.fullxmlUri;
       else //generate a unique string as key
         state.key = String.valueOf((new Date()).getTime());

      }
    }
  }

  /**
   * Process portal events.  Currently supported events are
   * EDIT_BUTTON_EVENT, HELP_BUTTON_EVENT, ABOUT_BUTTON_EVENT,
   * and SESSION_DONE.  The button events work by changing the xmlUri.
   * The new Uri's content should contain a link that will refer back
   * to the old one at the end of its task.
   * @param ev the event
   */
  public void receiveEvent (PortalEvent ev)
  {
    ChannelState state = chanState;
    if (state == null){
        log.debug("CWebProxy:receiveEvent() : no entry in state");
    }else {
      int evnum = ev.getEventNumber();

      switch (evnum)
      {
        case PortalEvent.EDIT_BUTTON_EVENT:
          if (state.editUri != null)
            state.buttonxmlUri = state.editUri;
          break;
        case PortalEvent.HELP_BUTTON_EVENT:
          if (state.helpUri != null)
            state.buttonxmlUri = state.helpUri;
          break;
        case PortalEvent.ABOUT_BUTTON_EVENT:
          if (state.infoUri != null)
            state.buttonxmlUri = state.infoUri;
          break;
        default:
          break;
      }
    }
  }

  /**
   * Acquires ChannelRuntimeProperites from the channel.
   * This function may be called by the portal framework throughout the session.
   * @see ChannelRuntimeProperties
   */
  public ChannelRuntimeProperties getRuntimeProperties ()
  {
    ChannelRuntimeProperties rp=new ChannelRuntimeProperties();

    // determine if such channel is registered
    if (chanState == null)
    {
      rp.setWillRender(false);
      log.debug("CWebProxy:getRuntimeProperties() : no entry in state");
    }
    return rp;
  }

  /**
   * Ask channel to render its content.
   * @param out the SAX ContentHandler to output content to
   */
  public void renderXML (ContentHandler out) throws PortalException
  {
    ChannelState state=chanState;
    if (state == null){
      log.debug("CWebProxy:renderXML() : no entry in state");
    }else{
      Document xml = null;
      String tidiedXml = null;

      // Optimized; cache retrieval of content between users
      Object o = null;

      // Global caching (i.e., cacheit) is calculated for each request.
      // For testing speed, we utilize OR checks; if any of the conditions
      // results in "TRUE", then we do NOT want to cache.
      //
      // state.cacheGlobalMode == false: the cacheGlobalMode can have
      // a default value defined in portal properties for all CWebProxy channels.
      // In addition, each channel can have a value defined. Should global
      // caching be performed for this channel?
      //
      // "none".equalsIgnoreCase(state.cacheMode): the cacheMode defines
      // framework caching. Should this content be cached by the framework
      // for this user?
      //
      // "post".equalsIgnoreCase(state.runtimeData.getHttpRequestMethod()):
      // is this an HTTP POST?
      //
      // state.fullxmlUri.indexOf('?') >= 0: does the URL include parameters?
      //
      // state.localConnContext != null: is a local connection
      // context is defined?
      boolean cacheit = !(state.cacheGlobalMode == false
    		  		|| "none".equalsIgnoreCase(state.cacheMode)
    		  		|| "post".equalsIgnoreCase(state.runtimeData.getHttpRequestMethod())
    		  		|| state.fullxmlUri.indexOf('?') >= 0
    		  		|| state.localConnContext != null);

      if (cacheit) {
    	  o = state.getCacheContent(state.fullxmlUri);
    	  if (log.isInfoEnabled()) {
    		  log.info("Global cache hit [" + (o != null) + "] for channel: " + state.fullxmlUri);
    	  }
      } else {
    	  if (log.isDebugEnabled()) {
    		  log.debug("Global cache not enabled for channel: " + state.fullxmlUri);
    	  }
      }

      if (o == null) {
    	  try {
    		  if (state.tidy != null && state.tidy.equals("on")) {
    			  tidiedXml = getTidiedXml(state.fullxmlUri, state);
    			  o = tidiedXml;
    		  } else {
    			  xml = getXml(state.fullxmlUri, state);
    			  o = xml;
    		  }
    	  } catch (Exception e) {
    		  throw new GeneralRenderingException ("Problem retrieving contents of " + state.fullxmlUri + ".  Please restart channel. ", e, false, true);
    	  }
    	  if (cacheit) {
    		  if (log.isInfoEnabled()) {
    			  log.info("Global cache store for channel: " + state.fullxmlUri);
    		  }
    		  state.setCacheContent(state.fullxmlUri, o);
    	  }
      } else {
    	  if (o instanceof String) {
    		  tidiedXml = (String) o;
    	  } else {
    		  xml = (Document) o;
    	  }
      }

      state.runtimeData.put("baseActionURL", state.runtimeData.getBaseActionURL());
      state.runtimeData.put("downloadActionURL", state.runtimeData.getBaseWorkerURL("download"));

      // Runtime data parameters are handed to the stylesheet.
      // Add any static data parameters so it gets a full set of variables.
      // We may wish to remove this feature since we don't need it for
      // the default stylesheets now.
      if (state.xmlUri != null)
        state.runtimeData.put("cw_xml", state.xmlUri);
      if (state.sslUri != null)
        state.runtimeData.put("cw_ssl", state.sslUri);
      if (state.xslTitle != null)
        state.runtimeData.put("cw_xslTitle", state.xslTitle);
      if (state.xslUri != null)
        state.runtimeData.put("cw_xsl", state.xslUri);
      if (state.passThrough != null)
        state.runtimeData.put("cw_passThrough", state.passThrough);
      if (state.tidy != null)
        state.runtimeData.put("cw_tidy", state.tidy);
      if (state.infoUri != null)
        state.runtimeData.put("cw_info", state.infoUri);
      if (state.helpUri != null)
        state.runtimeData.put("cw_help", state.helpUri);
      if (state.editUri != null)
        state.runtimeData.put("cw_edit", state.editUri);
      if (state.person != null)
        state.runtimeData.put("cw_person", state.person);
      if (state.personAllow != null)
        state.runtimeData.put("cw_personAllow", state.personAllow);

      XSLT xslt = XSLT.getTransformer(this, state.runtimeData.getLocales());
      if (tidiedXml != null)
        xslt.setXML(tidiedXml);
      else
        xslt.setXML(xml);
      if (state.xslUri != null && (!state.xslUri.trim().equals("")))
        xslt.setXSL(state.xslUri);
      else
        xslt.setXSL(state.sslUri, state.xslTitle, state.runtimeData.getBrowserInfo());

      // Determine mime type
      MediaManager mm = MEDIAMANAGER;
      String media = mm.getMedia(state.runtimeData.getBrowserInfo());
      String mimeType = mm.getReturnMimeType(media);
      if (MediaManager.UNKNOWN.equals(mimeType)) {
        String accept = state.runtimeData.getBrowserInfo().getHeader("accept");
        if (accept != null && accept.indexOf("text/html") != -1) {
          mimeType = "text/html";
        }
      }


      CWebProxyURLFilter filter2 = CWebProxyURLFilter.newCWebProxyURLFilter(mimeType, state.runtimeData, out);
      if (state.xmlUri.indexOf("://") != -1) {
	      AbsoluteURLFilter filter1 = AbsoluteURLFilter.newAbsoluteURLFilter(mimeType, state.xmlUri, filter2);
	      xslt.setTarget(filter1);
      } else {
	      xslt.setTarget(filter2);    	  
      }
      
      xslt.setStylesheetParameters(state.runtimeData);
      xslt.transform();
    }
  }

  /**
   * Get the contents of a URI as a Document object.  This is used if tidy
   * is not set or equals 'off'.
   * Also includes support for cookies.
   * @param uri the URI
   * @return the data pointed to by a URI as a Document object
   */
  private Document getXml(String uri, ChannelState state) throws Exception
  {
	long start = System.currentTimeMillis();
	int status = 0;

	Document doc = null;
    try {
    	URLConnection urlConnect = getConnection(uri, state);
		if (urlConnect instanceof HttpURLConnection){
			status = ((HttpURLConnection)urlConnect).getResponseCode();
		}
	    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	    docBuilderFactory.setNamespaceAware(false);
	    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	    DTDResolver dtdResolver = new DTDResolver();
	    docBuilder.setEntityResolver(dtdResolver);
	    InputStream is = null;
	    try{
		    is = urlConnect.getInputStream();
	    	doc = docBuilder.parse(is);
	    }finally{
	    	try{
	    		is.close();
	    	}catch(Exception e){
	    		// ignore
	    	}
	    }

    } finally {
        long elapsedTimeMillis = System.currentTimeMillis()-start;
        logAccess(uri, state, status, elapsedTimeMillis);
    }
    return doc;
  }

	// Optimized; share the tidy object in this thread
	private static final ThreadLocal perThreadTidy = new ThreadLocal() {
		protected Object initialValue() {
			Tidy tidy = new Tidy ();
			tidy.setXHTML (true);
			tidy.setDocType ("omit");
			tidy.setQuiet(true);
			tidy.setShowWarnings(false);
			tidy.setNumEntities(true);
			tidy.setWord2000(true);

			tidy.setErrout(devNull);
			return (tidy);
		}
	};

  private void logAccess(String uri, ChannelState state, int status, long elapsedTimeMillis) {
	  // trim off request parameters
	  int index = uri.indexOf("?");
	  if (index >= 0){
		  uri = uri.substring(0,index);
	  }
	  // trim off extra spaces and replace needed spaces with %20
	  uri = uri.trim().replaceAll(" ", "%20");
	  accessLog.info("logAccess: " +state.publishId+" "+state.iperson.getAttribute(IPerson.USERNAME)+" "
			  +uri+" "+status+" "+elapsedTimeMillis/1000F);
  }

  /**
    * Get the contents of a URI as a String but send it through tidy first.
    * Also includes support for cookies.
    * @param uri the URI
    * @return the data pointed to by a URI as a String
    */
  private String getTidiedXml(String uri, ChannelState state) throws Exception
  {
    long start = System.currentTimeMillis();
    String tidiedXml = null;
    int status = 0;

    try {
		URLConnection urlConnect = getConnection(uri, state);
    	if (urlConnect instanceof HttpURLConnection){
    		status = ((HttpURLConnection)urlConnect).getResponseCode();
    	}

		// get character encoding from Content-Type header
		String encoding = null;
		String ct = urlConnect.getContentType();
		int i;
		if (ct!=null && (i=ct.indexOf("charset="))!=-1)
		{
		  encoding = ct.substring(i+8).trim();
		  if ((i=encoding.indexOf(";"))!=-1)
		    encoding = encoding.substring(0,i).trim();
		  if (encoding.indexOf("\"")!=-1)
		    encoding = encoding.substring(1,encoding.length()+1);
		}

		Tidy tidy = (Tidy) perThreadTidy.get();

		// If charset is specified in header, set JTidy's
		// character encoding  to either UTF-8, ISO-8859-1
		// or ISO-2022 accordingly (NOTE that these are
		// the only character encoding sets that are supported in
		// JTidy).  If character encoding is not specified,
		// UTF-8 is the default.
		if (encoding != null)
		{
		  if (encoding.toLowerCase().equals("iso-8859-1"))
		    tidy.setCharEncoding(org.w3c.tidy.Configuration.LATIN1);
		  else if (encoding.toLowerCase().equals("iso-2022-jp"))
		    tidy.setCharEncoding(org.w3c.tidy.Configuration.ISO2022);
		  else
		    tidy.setCharEncoding(org.w3c.tidy.Configuration.UTF8);
		}
		else
		{
		  tidy.setCharEncoding(org.w3c.tidy.Configuration.UTF8);
		}

//		tidy.setErrout(devNull);

		ByteArrayOutputStream stream = new ByteArrayOutputStream (1024);
		BufferedOutputStream out = new BufferedOutputStream (stream);

		tidy.parse (urlConnect.getInputStream(), out);
		tidiedXml = stream.toString();
		stream.close();
		out.close();

		if ( tidy.getParseErrors() > 0 )
		  throw new GeneralRenderingException("Unable to convert input document to XHTML");
    }finally{
        long elapsedTimeMillis = System.currentTimeMillis()-start;
        logAccess(uri, state, status, elapsedTimeMillis);
    }
    return tidiedXml;
  }

  private URLConnection getConnection(String uri, ChannelState state) throws Exception
  {
      // before making the connection, ensure all spaces in the URI are encoded
      // (Note that URLEncoder.encode(String uri) cannot be used because
      // this method encodes everything, including forward slashes and
      // forward slashes are used for determining if the URL is
      // relative or absolute)

      uri = uri.trim().replaceAll(" ", "%20");

      URL url;
      if (state.localConnContext != null)
        url = ResourceLoader.getResourceAsURL(this.getClass(), state.localConnContext.getDescriptor(uri, state.runtimeData));
      else
        url = ResourceLoader.getResourceAsURL(this.getClass(), uri);

      // get info from url for cookies
      String domain = url.getHost().trim();
      String path = url.getPath();
      if ( path.indexOf("/") != -1 )
      {
        if (path.lastIndexOf("/") != 0)
          path = path.substring(0, path.lastIndexOf("/"));
      }
      String port = Integer.toString(url.getPort());

      //get connection
      URLConnection urlConnect = url.openConnection();
      String protocol = url.getProtocol();

      if (protocol.equals("http") || protocol.equals("https"))
      {
        if (domain != null && path != null)
        {
          //prepare the connection by setting properties and sending data
          HttpURLConnection httpUrlConnect = (HttpURLConnection) urlConnect;
          httpUrlConnect.setInstanceFollowRedirects(false);
          //send any cookie headers to proxied application
          if(state.cookieCutter.cookiesExist())
            state.cookieCutter.sendCookieHeader(httpUrlConnect, domain, path, port);
          //set connection properties if request method was post
          if (state.runtimeData.getHttpRequestMethod().equals("POST"))
          {
            if ((state.reqParameters!=null) && (!state.reqParameters.trim().equals("")))
            {
              httpUrlConnect.setRequestMethod("POST");
              httpUrlConnect.setAllowUserInteraction(false);
              httpUrlConnect.setDoOutput(true);
            }
          }

          //send local data, if required
          //can call getOutputStream in sendLocalData (ie. to send post params)
          //(getOutputStream can be called twice on an HttpURLConnection)
          if (state.localConnContext != null)
          {
            try
            {
              state.localConnContext.sendLocalData(httpUrlConnect, state.runtimeData);
            }
            catch (Exception e)
            {
              log.error( "CWebProxy: Unable to send data through " + state.runtimeData.getParameter("upc_localConnContext"), e);
            }
          }

          //send the request parameters by post, if required
          //at this point, set or send methods cannot be called on the connection
          //object (they must be called before sendLocalData)
          if (state.runtimeData.getHttpRequestMethod().equals("POST")){
            if ((state.reqParameters!=null) && (!state.reqParameters.trim().equals(""))){
              PrintWriter post = new PrintWriter(httpUrlConnect.getOutputStream());
              post.print(state.reqParameters);
              post.flush();
              post.close();
              state.reqParameters=null;
            }
          }

          //receive cookie headers
          state.cookieCutter.storeCookieHeader(httpUrlConnect, domain, path, port);

          int status = httpUrlConnect.getResponseCode();
          String location = httpUrlConnect.getHeaderField("Location");
          switch (status)
          {
            case HttpURLConnection.HTTP_NOT_FOUND:
              throw new ResourceMissingException
                (httpUrlConnect.getURL().toExternalForm(),
                  "", "HTTP Status-Code 404: Not Found");
            case HttpURLConnection.HTTP_FORBIDDEN:
              throw new ResourceMissingException
                (httpUrlConnect.getURL().toExternalForm(),
                  "", "HTTP Status-Code 403: Forbidden");
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
              throw new ResourceMissingException
                (httpUrlConnect.getURL().toExternalForm(),
                  "", "HTTP Status-Code 500: Internal Server Error");
            case HttpURLConnection.HTTP_NO_CONTENT:
              throw new ResourceMissingException
                (httpUrlConnect.getURL().toExternalForm(),
                  "", "HTTP Status-Code 204: No Content");
            /*
             * Note: these cases apply to http status codes 302 and 303
             * this will handle automatic redirection to a new GET URL
             */
            case HttpURLConnection.HTTP_MOVED_TEMP:
              httpUrlConnect.disconnect();
              httpUrlConnect = (HttpURLConnection) getConnection(location,state);
              break;
            case HttpURLConnection.HTTP_SEE_OTHER:
              httpUrlConnect.disconnect();
              httpUrlConnect = (HttpURLConnection) getConnection(location,state);
              break;
            /*
             * Note: this cases apply to http status code 301
             * it will handle the automatic redirection of GET requests.
             * The spec calls for a POST redirect to be verified manually by the user
             * Rather than bypass this security restriction, we will throw an exception
             */
            case HttpURLConnection.HTTP_MOVED_PERM:
              if (state.runtimeData.getHttpRequestMethod().equals("GET")){
                httpUrlConnect.disconnect();
                httpUrlConnect = (HttpURLConnection) getConnection(location,state);
              }
              else {
                throw new ResourceMissingException
                  (httpUrlConnect.getURL().toExternalForm(),
                    "", "HTTP Status-Code 301: POST Redirection currently not supported");
              }
              break;
            default:
              break;
          }

          return (URLConnection) httpUrlConnect;
        }
      }
      return urlConnect;
  }

  public ChannelCacheKey generateKey()
  {
    ChannelState state = chanState;

    if (state == null)
    {
      log.debug("CWebProxy:generateKey() : no entry in state");
      return null;
    }

    if ( state.cacheMode.equalsIgnoreCase("none") )
      return null;
    // else if http see first if caching is on or off.  if it's on,
    // store the validity time in the state, cache it, and further
    // resolve later with isValid.
    // check cache-control, no-cache, must-revalidate, max-age,
    // Date & Expires, expiry in past
    // for 1.0 check pragma for no-cache
    // add a warning to docs about not a full http 1.1 impl.

    ChannelCacheKey k = new ChannelCacheKey();
    StringBuffer sbKey = new StringBuffer(1024);

    // Only INSTANCE scope is currently supported.
    k.setKeyScope(ChannelCacheKey.INSTANCE_KEY_SCOPE);

    sbKey.append("sslUri:").append(state.sslUri).append(", ");

    // xslUri may either be specified as a parameter to this channel
    // or we will get it by looking in the stylesheet list file
    String xslUriForKey = state.xslUri;
    try {
      if (xslUriForKey == null) {
        String sslUri = ResourceLoader.getResourceAsURLString(this.getClass(), state.sslUri);
        xslUriForKey = XSLT.getStylesheetURI(sslUri, state.runtimeData.getBrowserInfo());
      }
    } catch (Exception e) {
      xslUriForKey = "Not attainable: " + e;
    }

    sbKey.append("xslUri:").append(xslUriForKey).append(", ");
    sbKey.append("key:").append(state.key).append(", ");
    sbKey.append("passThrough:").append(state.passThrough).append(", ");
    sbKey.append("tidy:").append(state.tidy).append(", ");
    sbKey.append("person:").append(state.person);
    k.setKey(sbKey.toString());
    k.setKeyValidity(new Long(System.currentTimeMillis()));
    //log.debug("CWebProxy:generateKey("
    //      + uid + ") : cachekey=\"" + sbKey.toString() + "\"");
    return k;
  }

  static PrintWriter getErrout() throws FileNotFoundException
  {
    if (System.getProperty("os.name").indexOf("Windows") != -1)
      return new PrintWriter(new FileOutputStream("nul"));
    else
      return new PrintWriter(new FileOutputStream("/dev/null"));
  }

  public boolean isCacheValid(Object validity)
  {
    if (!(validity instanceof Long))
      return false;

    ChannelState state = chanState;

    if (state == null)
    {
      log.debug("CWebProxy:isCacheValid() : no entry in state");
      return false;
    } else if (System.currentTimeMillis() - ((Long)validity).longValue() > state.cacheTimeout*1000) {
    	if (log.isInfoEnabled()) {
    		log.info("Framework cache timeout: " + state.fullxmlUri);
    	}
    	return false;
    } else if (state.getCacheContentLoaded() > ((Long)validity).longValue()) {
    	if (log.isInfoEnabled()) {
    		log.info("Global cache timeout:"
    				+ " >cacheContentLoaded: " + state.getCacheContentLoaded()
    				+ " >validity: " + validity
    				+ " >currentTime: " + System.currentTimeMillis()
    				+ " >url: " + state.fullxmlUri);
    	}
    	return false;
    }

    return true;

  }

  public String getContentType() {
    return chanState.connHolder.getContentType();
  }

  public InputStream getInputStream() throws IOException {
    InputStream rs = chanState.connHolder.getInputStream();
    chanState.connHolder = null;
    return rs;
  }

  public void downloadData(OutputStream out) throws IOException {
    throw(new IOException("CWebProxy: downloadData method not supported - use getInputStream only"));
  }

  public String getName() {
    return "proxyDL";
  }

  public Map getHeaders() {
    ChannelState state = chanState;
    try {
      state.connHolder= getConnection(state.fullxmlUri, state);
    }
    catch (Exception e){
      log.error(e, e);
    }
    Map rhdrs = new HashMap();
    int i = 0;
    while (state.connHolder.getHeaderFieldKey(i) != null){
      rhdrs.put(state.connHolder.getHeaderFieldKey(i),state.connHolder.getHeaderField(i));
      i++;
    }
    return rhdrs;
  }

  public void reportDownloadError(Exception e) {
    // We really should report this to the user somehow??
    log.error(e.getMessage(), e);
  }


}
