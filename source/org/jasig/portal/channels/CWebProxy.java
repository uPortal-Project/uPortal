/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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

import org.xml.sax.ContentHandler;
import java.util.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.text.ParseException;
import javax.servlet.http.Cookie;
import javax.xml.parsers.*;
import org.w3c.tidy.*;
import org.w3c.dom.Document;
import org.jasig.portal.*;
import org.jasig.portal.utils.*;
import org.jasig.portal.services.LogService;
import org.jasig.portal.security.IPerson;

/**
 * <p>A channel which transforms and interacts with dynamic XML or HTML.
 *    See http://www.mun.ca/cc/portal/cw/ for full documentation.
 *    This version introduces experimental features, which may or may
 *    not survive to the next release.  Default values are backwards
 *    compatible with uPortal version 2.0.1.  Only defaults have been
 *    fully tested.</p>
 *
 * <p>Static Channel Parameters:</p>
 * <ol>
 *  <li>"cw_xml" - a URI for the source XML document
 *  <li>"cw_ssl" - a URI for the corresponding .ssl (stylesheet list) file
 *  <li>"cw_xslTitle" - a title representing the stylesheet (optional)
 *                  <i>If no title parameter is specified, a default
 *                  stylesheet will be chosen according to the media</i>
 *  <li>"cw_xsl" - a URI for the stylesheet to use
 *                  <i>If <code>cw_xsl</code> is supplied, <code>cw_ssl</code>
 *                  and <code>cw_xslTitle</code> will be ignored.
 *  <li>"cw_passThrough" - indicates how RunTimeData is to be passed through.
 *                  <i>If <code>cw_passThrough</code> is supplied, and not set
 *		    to "all" or "application", additional RunTimeData
 *		    parameters not starting with "cw_" will be passed as
 *		    request parameters to the XML URI.  If
 *		    <code>cw_passThrough</code> is set to "marked", this will
 *		    happen only if there is also a RunTimeData parameter of
 *		    <code>cw_inChannelLink</code>.  "application" is intended
 *		    to keep application-specific links in the channel, while
 *		    "all" should keep all links in the channel.  This
 *		    distinction is handled entirely in the stylesheets.
 *  <li>"cw_tidy" - output from <code>xmlUri</code> will be passed though Jtidy
 *  <li>"cw_info" - a URI to be called for the <code>info</code> event.
 *  <li>"cw_help" - a URI to be called for the <code>help</code> event.
 *  <li>"cw_edit" - a URI to be called for the <code>edit</code> event.
 *  <li>"cw_cacheDefaultTimeout" - Default timeout in seconds.
 *  <li>"cw_cacheDefaultScope" - Default cache scope.  <i>May be
 *		    <code>system</code> (one copy for all users), or
 *		    <code>user</code> (one copy per user), or
 *		    <code>instance</code> (cache for this channel instance
 *		    only).</i>
 *  <li>"cw_cacheDefaultMode" - Default caching mode.
 *		    <i>May be <code>none</code> (normally don't cache),
 *		    <code>http</code> (follow http caching directives), or
 *		    <code>all</code> (cache everything).  Http is not
 *		    currently implemented.</i>
 *  <li>"cw_cacheTimeout" - override default for this request only.
 *		    <i>Primarily intended as a runtime parameter, but can
 *	            user statically to override the first instance.</i>
 *  <li>"cw_cacheScope" - override default for this request only.
 *		    <i>Primarily intended as a runtime parameter, but can
 *	            user statically to override the first instance.</i>
 *  <li>"cw_cacheMode" - override default for this request only.
 *		    <i>Primarily intended as a runtime parameter, but can
 *	            user statically to override the first instance.</i>
 *  <li>"cw_person" - IPerson attributes to pass.
 *		    <i>A comma-separated list of IPerson attributes to
 *		    pass to the back end application.</i>
 * </ol>
 * <p>Runtime Channel Parameters:</p>
 *    The static parameters above can be updated by equivalent Runtime
 *    parameters.  Caching parameters can also be changed temporarily.
 *    Cache scope and mode can only be made more restrictive, not less.
 *    The following parameter is runtime-only.
 * </p>
 * <ol>
 *  <li>"cw_reset" - an instruction to return to reset internal variables.
 *		   The value <code>return</code> resets <code>cw_xml</code>
 *		   to its last value before changed by button events.  The
 *		   value "reset" returns all variables to the static data
 *		   values.  Runtime data parameter only.
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
public class CWebProxy implements IMultithreadedChannel, IMultithreadedCacheable, IMultithreadedMimeResponse

{
  Map stateTable;
  // to prepend to the system-wide cache key
  static final String systemCacheId="org.jasig.portal.channels.CWebProxy";

  // All state variables now stored here
  private class ChannelState
  {
    private int id;
    private IPerson iperson;
    private String person;
    private String fullxmlUri;
    private String buttonxmlUri;
    private String xmlUri;
    private String passThrough;
    private String tidy;
    private String sslUri;
    private String xslTitle;
    private String xslUri;
    private String infoUri;
    private String helpUri;
    private String editUri;
    private String cacheDefaultScope;
    private String cacheScope;
    private String cacheDefaultMode;
    private String cacheMode;
    private String reqParameters;
    private long cacheDefaultTimeout;
    private long cacheTimeout;
    private ChannelRuntimeData runtimeData;
    private Vector cookies;
    private boolean supportSetCookie2;
    private URLConnection connHolder;

    public ChannelState ()
    {
      fullxmlUri = buttonxmlUri = xmlUri = passThrough = sslUri = null;
      xslTitle = xslUri = infoUri = helpUri = editUri = tidy = null;
      id = 0;
      cacheMode = cacheScope = null;
      iperson = null;
      cacheTimeout = cacheDefaultTimeout = PropertiesManager.getPropertyAsLong("org.jasig.portal.channels.CWebProxy.cache_default_timeout");
      cacheDefaultMode = PropertiesManager.getProperty("org.jasig.portal.channels.CWebProxy.cache_default_mode");
      cacheDefaultScope = PropertiesManager.getProperty("org.jasig.portal.channels.CWebProxy.cache_default_scope");
      runtimeData = null;
      cookies = new Vector();
      supportSetCookie2 = false;
    }
  }

  public CWebProxy ()
  {
    stateTable = Collections.synchronizedMap(new HashMap());
  }

  /**
   * Passes ChannelStaticData to the channel.
   * This is done during channel instantiation time.
   * see org.jasig.portal.ChannelStaticData
   * @param sd channel static data
   * @see ChannelStaticData
   */
  public void setStaticData (ChannelStaticData sd, String uid) throws ResourceMissingException
  {
    ChannelState state = new ChannelState();

    state.id = sd.getPerson().getID();
    state.iperson = sd.getPerson();
    state.person = sd.getParameter("cw_person");
    state.xmlUri = sd.getParameter ("cw_xml");
    state.sslUri = sd.getParameter ("cw_ssl");
    state.fullxmlUri = sd.getParameter ("cw_xml");
    state.passThrough = sd.getParameter ("cw_passThrough");
    state.tidy = sd.getParameter ("cw_tidy");
    state.infoUri = sd.getParameter ("cw_info");
    state.helpUri = sd.getParameter ("cw_help");
    state.editUri = sd.getParameter ("cw_edit");
    String cacheScope = sd.getParameter ("cw_cacheDefaultScope");
    if (cacheScope != null)
      state.cacheDefaultScope = cacheScope;
    cacheScope = sd.getParameter ("cw_cacheScope");
    if (cacheScope != null)
      state.cacheScope = cacheScope;
    String cacheMode = sd.getParameter ("cw_cacheDefaultMode");
    if (cacheMode != null)
      state.cacheDefaultMode = cacheMode;
    cacheMode = sd.getParameter ("cw_cacheMode");
    if (cacheMode != null)
      state.cacheMode = cacheMode;
    String cacheTimeout = sd.getParameter("cw_cacheDefaultTimeout");
    if (cacheTimeout != null)
      state.cacheDefaultTimeout = Long.parseLong(cacheTimeout);
    cacheTimeout = sd.getParameter("cw_cacheTimeout");
    if (cacheTimeout != null)
      state.cacheTimeout = Long.parseLong(cacheTimeout);

    stateTable.put(uid,state);
  }

  /**
   * Passes ChannelRuntimeData to the channel.
   * This function is called prior to the renderXML() call.
   * @param rd channel runtime data
   * @see ChannelRuntimeData
   */
  public void setRuntimeData (ChannelRuntimeData rd, String uid)
  {
     ChannelState state = (ChannelState)stateTable.get(uid);
     if (state == null)
       LogService.instance().log(LogService.ERROR,"CWebProxy:setRuntimeData() : attempting to access a non-established channel! setStaticData() hasn't been called on uid=\""+uid+"\"");
     else
     {
       state.runtimeData = rd;

       String xmlUri = state.runtimeData.getParameter("cw_xml");
       if (xmlUri != null) {
         state.xmlUri = xmlUri;
         // don't need an explicit reset if a new URI is provided.
         state.buttonxmlUri = null;
       }

       String sslUri = state.runtimeData.getParameter("cw_ssl");
       if (sslUri != null)
          state.sslUri = sslUri;

       String xslTitle = state.runtimeData.getParameter("cw_xslTitle");
       if (xslTitle != null)
          state.xslTitle = xslTitle;

       String xslUri = state.runtimeData.getParameter("cw_xsl");
       if (xslUri != null)
          state.xslUri = xslUri;

       String passThrough = state.runtimeData.getParameter("cw_passThrough");
       if (passThrough != null)
          state.passThrough = passThrough;
   
       String tidy = state.runtimeData.getParameter("cw_tidy");
       if (tidy != null)
          state.tidy = tidy;

       String infoUri = state.runtimeData.getParameter("cw_info");
       if (infoUri != null)
          state.infoUri = infoUri;

       String editUri = state.runtimeData.getParameter("cw_edit");
       if (editUri != null)
          state.editUri = editUri;

       String helpUri = state.runtimeData.getParameter("cw_help");
       if (helpUri != null)
          state.helpUri = helpUri;

       // need a way to see if cacheScope, cacheMode, cacheTimeout were
       // set in static data if this is the first time.

       String cacheTimeout = state.runtimeData.getParameter("cw_cacheDefaultTimeout");
       if (cacheTimeout != null)
          state.cacheDefaultTimeout = Long.parseLong(cacheTimeout);

       cacheTimeout = state.runtimeData.getParameter("cw_cacheTimeout");
       if (cacheTimeout != null)
          state.cacheTimeout = Long.parseLong(cacheTimeout);
       else
          state.cacheTimeout = state.cacheDefaultTimeout;

       String cacheDefaultScope = state.runtimeData.getParameter("cw_cacheDefaultScope");
       if (cacheDefaultScope != null) {
          // PSEUDO see if it's a reduction fine, otherwise log error 
          state.cacheDefaultScope = cacheDefaultScope;
       }

       String cacheScope = state.runtimeData.getParameter("cw_cacheScope");
       if (cacheScope != null) {
          // PSEUDO see if it's a reduction fine, otherwise a problem
	  // for now all instance -> user
          if ( state.cacheDefaultScope.equalsIgnoreCase("system") )
             state.cacheScope = cacheScope;
	  else {
             state.cacheScope = state.cacheDefaultScope;
             LogService.instance().log(LogService.INFO,
	       "CWebProxy:setRuntimeData() : ignoring illegal scope reduction from "
	       + state.cacheDefaultScope + " to " + cacheScope);
	  }
       } else
          state.cacheScope = state.cacheDefaultScope;

       LogService.instance().log(LogService.DEBUG, "CWebProxy setRuntimeData(): state.cacheDefaultMode was " + state.cacheDefaultMode);
       String cacheDefaultMode = state.runtimeData.getParameter("cw_cacheDefaultMode");
       LogService.instance().log(LogService.DEBUG, "CWebProxy setRuntimeData(): cw_cacheDefaultMode is " + cacheDefaultMode);
       if (cacheDefaultMode != null) {
          // maybe don't allow if scope is system?
          state.cacheDefaultMode = cacheDefaultMode;
       }
       LogService.instance().log(LogService.DEBUG, "CWebProxy setRuntimeData(): state.cacheDefaultMode is now " + state.cacheDefaultMode);

       LogService.instance().log(LogService.DEBUG, "CWebProxy setRuntimeData(): state.cacheMode was " + state.cacheMode);
       String cacheMode = state.runtimeData.getParameter("cw_cacheMode");
       LogService.instance().log(LogService.DEBUG, "CWebProxy setRuntimeData(): cw_cacheMode is " + cacheMode);
       if (cacheMode != null) {
          // maybe don't allow if scope is system?
          state.cacheMode = cacheMode;
       } else
          state.cacheMode = state.cacheDefaultMode;
       LogService.instance().log(LogService.DEBUG, "CWebProxy setRuntimeData(): state.cacheMode is now " + state.cacheMode);

       // reset is a one-time thing.
       String reset = state.runtimeData.getParameter("cw_reset");
       if (reset != null) {
          if (reset.equalsIgnoreCase("return")) {
             state.buttonxmlUri = null;
          }
          // else if (reset.equalsIgnoreCase("reset")) {
          //  call setStaticData with our cached copy.
          // }
       }

       if ( state.buttonxmlUri != null )
           state.fullxmlUri = state.buttonxmlUri;
       else {
         //if (this.passThrough != null )
         //  LogService.instance().log(LogService.DEBUG, "CWebProxy: passThrough: "+this.passThrough);

         // Is this a case where we need to pass request parameters to the xmlURI?
         if ( state.passThrough != null &&
            !state.passThrough.equalsIgnoreCase("none") &&
              ( state.passThrough.equalsIgnoreCase("all") ||
                state.passThrough.equalsIgnoreCase("application") ||
                rd.getParameter("cw_inChannelLink") != null ) )
           {
             LogService.instance().log(LogService.DEBUG, "CWebProxy: xmlUri is " + state.xmlUri);

             StringBuffer newXML = new StringBuffer();
             String appendchar = "";

             // want all runtime parameters not specific to WebProxy
             Enumeration e=rd.getParameterNames ();
             if (e!=null)
               {
                 while (e.hasMoreElements ())
                   {
                     String pName = (String) e.nextElement ();
                     if ( !pName.startsWith("cw_") && !pName.trim().equals("")) {
		       String[] value_array = rd.getParameterValues(pName);
		       if ( value_array == null || value_array.length == 0 ) {
			   // keyword-style parameter
			   newXML.append(appendchar);
			   appendchar = "&";
			   newXML.append(pName);
			} else {
			  int i = 0;
			  while ( i < value_array.length ) {
LogService.instance().log(LogService.DEBUG, "CWebProxy: ANDREW adding runtime parameter: " + pName);
                            newXML.append(appendchar);
                            appendchar = "&";
                            newXML.append(pName);
                            newXML.append("=");
		            newXML.append(URLEncoder.encode(value_array[i++]));
		          }
			}

                     }
                   }
               }
	     // here add in attributes according to cw_person
	     if (state.person != null) {
               StringTokenizer st = new StringTokenizer(state.person,",");
               if (st != null)
                 {
                   while (st.hasMoreElements ())
                     {
                       String pName = st.nextToken();
                       if ((pName!=null)&&(!pName.trim().equals(""))){
LogService.instance().log(LogService.DEBUG, "CWebProxy: ANDREW adding person attribute: " + pName);
                         newXML.append(appendchar);
                         appendchar = "&";
                         newXML.append(pName);
                         newXML.append("=");
                         // note, this only gets the first one if it's a
                         // java.util.Vector.  Should check
                         String pVal = (String)state.iperson.getAttribute(pName);
                         if (pVal != null)
                           newXML.append(URLEncoder.encode(pVal));
                       }
                     }
                 }
	       }
	     // end new cw_person code

             // to add: if not already set, make a copy of sd for
	     // the "reset" command
             state.reqParameters = newXML.toString();
             state.fullxmlUri = state.xmlUri;
             if (!state.runtimeData.getHttpRequestMethod().equals("POST")){
                if ((state.reqParameters!=null) && (!state.reqParameters.trim().equals(""))){
                  appendchar = (state.xmlUri.indexOf('?') == -1) ? "?" : "&";
                  // BUG 772 - this doesn't seem to catch all cases.
                  state.fullxmlUri = state.fullxmlUri+appendchar+state.reqParameters;
                }
                state.reqParameters = null;
             }
             LogService.instance().log(LogService.DEBUG, "CWebProxy: fullxmlUri now: " + state.fullxmlUri);
          }
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
  public void receiveEvent (PortalEvent ev, String uid)
  {
    ChannelState state = (ChannelState)stateTable.get(uid);
    if (state == null)
       LogService.instance().log(LogService.ERROR,"CWebProxy:receiveEvent() : attempting to access a non-established channel! setStaticData() hasn't been called on uid=\""+uid+"\"");
    else {
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
        case PortalEvent.SESSION_DONE:
	  stateTable.remove(uid);
          break;
        // case PortalEvent.UNSUBSCRIBE: // remove db entry for channel
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
  public ChannelRuntimeProperties getRuntimeProperties (String uid)
  {
    ChannelRuntimeProperties rp=new ChannelRuntimeProperties();

    // determine if such channel is registered
    if (stateTable.get(uid) == null)
    {
      rp.setWillRender(false);
      LogService.instance().log(LogService.ERROR,"CWebProxy:getRuntimeProperties() : attempting to access a non-established channel! setStaticData() hasn't been called on uid=\""+uid+"\"");
    }
    return rp;
  }

  /**
   * Ask channel to render its content.
   * @param out the SAX ContentHandler to output content to
   */
  public void renderXML (ContentHandler out, String uid) throws PortalException
  {
    ChannelState state=(ChannelState)stateTable.get(uid);
    if (state == null)
      LogService.instance().log(LogService.ERROR,"CWebProxy:renderXML() : attempting to access a non-established channel! setStaticData() hasn't been called on uid=\""+uid+"\"");
    else
      {
      String xml = null;
      Document xmlDoc = null;

      try
      {
        if (state.tidy != null && state.tidy.equals("on"))
          xml = getXmlString (state.fullxmlUri, state);
	else
	  xmlDoc = getXmlDocument (state.fullxmlUri, state);
      }
      catch (Exception e)
      {
        throw new ResourceMissingException (state.fullxmlUri, "", e.getMessage());
      }

      state.runtimeData.put("baseActionURL", state.runtimeData.getBaseActionURL());
      state.runtimeData.put("downloadActionURL", state.runtimeData.getBaseWorkerURL("download"));

      // Runtime data parameters are handed to the stylesheet.
      // Add any static data parameters so it gets a full set of variables.
      // Possibly this should be a copy.
      if (state.xmlUri != null)
        state.runtimeData.put("cw_xml", state.xmlUri);
      if (state.sslUri != null)
        state.runtimeData.put("cs_ssl", state.sslUri);
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

      XSLT xslt = new XSLT(this);
      if (xmlDoc != null)
        xslt.setXML(xmlDoc);
      else
        xslt.setXML(xml);
      if (state.xslUri != null)
        xslt.setXSL(state.xslUri);
      else
        xslt.setXSL(state.sslUri, state.xslTitle, state.runtimeData.getBrowserInfo());
      xslt.setTarget(out);
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
  private Document getXmlDocument(String uri, ChannelState state) throws Exception
  {
    URLConnection urlConnect = getConnection(uri, state);

    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setNamespaceAware(false);
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    DTDResolver dtdResolver = new DTDResolver();
    docBuilder.setEntityResolver(dtdResolver);

    return  docBuilder.parse(urlConnect.getInputStream());
  }

  /**
   * Get the contents of a URI as a String but send it through tidy first.
   * Also includes support for cookies.
   * @param uri the URI
   * @return the data pointed to by a URI as a String
   */
  private String getXmlString (String uri, ChannelState state) throws Exception
  {
    URLConnection urlConnect = getConnection(uri, state);

    String xml;
    if ( (state.tidy != null) && (state.tidy.equalsIgnoreCase("on")) )
    {
      Tidy tidy = new Tidy ();
      tidy.setXHTML (true);
      tidy.setDocType ("omit");
      tidy.setQuiet(true);
      tidy.setShowWarnings(false);
      tidy.setNumEntities(true);
      tidy.setWord2000(true);
      if ( System.getProperty("os.name").indexOf("Windows") != -1 )
         tidy.setErrout( new PrintWriter ( new FileOutputStream (new File ("nul") ) ) );
      else
         tidy.setErrout( new PrintWriter ( new FileOutputStream (new File ("/dev/null") ) ) );
      ByteArrayOutputStream stream = new ByteArrayOutputStream (1024);

      tidy.parse (urlConnect.getInputStream(), new BufferedOutputStream (stream));
      if ( tidy.getParseErrors() > 0 )
        throw new GeneralRenderingException("Unable to convert input document to XHTML");
      xml = stream.toString();
    }
    else
    {
      String line = null;
      BufferedReader in = new BufferedReader(new InputStreamReader(urlConnect.getInputStream()));
      StringBuffer sbText = new StringBuffer (1024);

      while ((line = in.readLine()) != null)
        sbText.append (line).append ("\n");

      xml = sbText.toString ();
    }

    return xml;
  }
  
  private URLConnection getConnection(String uri, ChannelState state) throws Exception{
      URL url = ResourceLoader.getResourceAsURL(this.getClass(), uri);
      String domain = url.getHost().trim();
      String path = url.getPath();
      if ( path.indexOf("/") != -1 )
      {
        if (path.lastIndexOf("/") != 0)
          path = path.substring(0, path.lastIndexOf("/"));
      }
      String port = Integer.toString(url.getPort());
      URLConnection urlConnect = url.openConnection();
      String protocol = url.getProtocol();
  
      if (protocol.equals("http") || protocol.equals("https"))
      {
        //HttpURLConnection httpUrlConnect = (HttpURLConnection) urlConnect;
        if (domain != null && path != null)
          urlConnect = (URLConnection) sendAndStoreCookies(((HttpURLConnection) urlConnect), domain, path, port, state);
        //urlConnect = (URLConnection) httpUrlConnect;
      }
      return urlConnect;
  }

   /**
    * Sends any cookies in the cookie vector as a request header and stores
    * any incoming cookies in the cookie vector (according to rfc 2109,
    * 2965 &amp; netscape)
    *
    * @param httpUrlConnect The HttpURLConnection handling the URL connection
    * @param domain The domain value for the Cookie to be sent
    * @param path The path value for the Cookie to be sent
    * @param port The port value for the Cookie to be sent
    */
  private HttpURLConnection sendAndStoreCookies(HttpURLConnection httpUrlConnect, String domain, String path, String port, ChannelState state) throws Exception
  {
    httpUrlConnect.setInstanceFollowRedirects(false);
    // send appropriate cookies to origin server from cookie vector
    if (state.cookies.size() > 0)
      sendCookieHeader(httpUrlConnect, domain, path, port, state.cookies);

    // added 5/13/2002 by ASV - print post data
    if (state.runtimeData.getHttpRequestMethod().equals("POST")){
        if ((state.reqParameters!=null) && (!state.reqParameters.trim().equals(""))){
          httpUrlConnect.setRequestMethod("POST");
          httpUrlConnect.setAllowUserInteraction(false);
          httpUrlConnect.setDoOutput(true);
          PrintWriter post = new PrintWriter(httpUrlConnect.getOutputStream());
          post.print(state.reqParameters);
          post.flush();
          post.close(); 
          state.reqParameters=null;
        }
    }


    // store any cookies sent by the channel in the cookie vector
    int index = 1;
    String header;
    while ( (header=httpUrlConnect.getHeaderFieldKey(index)) != null )
    {
       if (state.supportSetCookie2)
       {
         if (header.equalsIgnoreCase("set-cookie2"))
            processSetCookie2Header(httpUrlConnect.getHeaderField(index), domain, path, port, state.cookies);
       }
       else
       {
         if (header.equalsIgnoreCase("set-cookie2"))
         {
           state.supportSetCookie2 = true;
           processSetCookie2Header(httpUrlConnect.getHeaderField(index), domain, path, port, state.cookies);
         }
         else if (header.equalsIgnoreCase("set-cookie"))
         {
           processSetCookieHeader(httpUrlConnect.getHeaderField(index), domain, path, port, state.cookies);
         }
       }
       index++;
    }
    
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
    return httpUrlConnect;
  }

  /**
   * Sends a cookie header to origin server according to the Netscape
   * specification.
   *
   * @param httpUrlConnect The HttpURLConnection handling this URL connection
   * @param domain The domain value of the cookie
   * @param path The path value of the cookie
   * @param port The port value of the cookie
   */
  private void sendCookieHeader(HttpURLConnection httpUrlConnect, String domain, String path, String port, Vector cookies)
  {
     Vector cookiesToSend = new Vector();
     WebProxyCookie cookie;
     String cport = "";
     boolean portOk = true;
     for (int index=0; index<cookies.size(); index++)
     {
        cookie = (WebProxyCookie) cookies.elementAt(index);
        boolean isExpired;
        Date current = new Date();
        Date cookieExpiryDate = cookie.getExpiryDate();
        if (cookieExpiryDate != null)
           isExpired = cookieExpiryDate.before(current);
        else
           isExpired = false;
        if (cookie.isPortSet())
        {
           cport = cookie.getPort();
           portOk = false;
        }
        if ( !cport.equals("") )
        {
           if (cport.indexOf(port) != -1)
             portOk = true;
        }
        if ( domain.endsWith(cookie.getDomain()) && path.startsWith(cookie.getPath()) && portOk && !isExpired )
          cookiesToSend.addElement(cookie);
     }
     if (cookiesToSend.size()>0)
     {
       //put the cookies in the correct order to send to origin server
       Vector cookiesInOrder= new Vector();
       WebProxyCookie c1;
       WebProxyCookie c2;
       boolean flag;
       outerloop:
       for (int i=0; i<cookiesToSend.size(); i++)
       {
         c1 = (WebProxyCookie) cookiesToSend.elementAt(i);
         flag = false;
         if (cookiesInOrder.size()==0)
           cookiesInOrder.addElement(c1);
         else
         {
           for (int index=0; index<cookiesInOrder.size(); index++)
           {
             c2 = (WebProxyCookie) cookiesInOrder.elementAt(index);
             if ( c1.getPath().length() >= c2.getPath().length() )
             {
               cookiesInOrder.insertElementAt(c1, index);
               flag = true;
               continue outerloop;
             }
           }
           if (!flag)
             cookiesInOrder.addElement(c1);
         }
       }
       //send the cookie header
       // **NOTE** This is NOT the syntax of the cookie header according
       // to rfc 2965. Tested under Apache's Tomcat, the servlet engine
       // treats the cookie attributes as separate cookies.
       // This is the syntax according to the Netscape Cookie Specification.
       String headerValue = "";
       WebProxyCookie c;
       for (int i=0; i<cookiesInOrder.size(); i++)
       {
         c = (WebProxyCookie) cookiesInOrder.elementAt(i);
         if (i == 0)
           headerValue = c.getName() + "=" +c.getValue();
         else
           headerValue = headerValue + "; " + c.getName() + "=" +c.getValue();
       }
       if ( !headerValue.equals("") )
       {
         httpUrlConnect.setRequestProperty("Cookie", headerValue);
       }
     }
  }

  /**
   * Processes the Cookie2 header.
   *
   * @param headerVal The value of the header
   * @param domain The domain value of the cookie
   * @param path The path value of the cookie
   * @param port The port value of the cookie
   */
  private void processSetCookie2Header (String headerVal, String domain, String path, String port, Vector cookies)
  {
     StringTokenizer headerValue = new StringTokenizer(headerVal, ",");
     StringTokenizer cookieValue;
     WebProxyCookie cookie;
     String token;
     while (headerValue.hasMoreTokens())
     {
       cookieValue = new StringTokenizer(headerValue.nextToken(), ";");
       token = cookieValue.nextToken();
       if ( token.indexOf("=") != -1)
       {
          cookie = new WebProxyCookie ( token.substring(0, token.indexOf("=")),
                                token.substring(token.indexOf("=")+1).trim() );
       }
       else
       {
          LogService.instance().log(LogService.DEBUG, "CWebProxy: Invalid Header: \"Set-Cookie2:"+headerVal+"\"");
          cookie = null;
       }
       // set max-age, path and domain of cookie
       if (cookie != null)
       {
          boolean ageSet = false;
          boolean domainSet = false;
          boolean pathSet = false;
          boolean portSet = false;
          while( cookieValue.hasMoreTokens() )
          {
            token = cookieValue.nextToken();
            if ( (!ageSet && (token.indexOf("=")!=-1)) && token.substring(0, token.indexOf("=")).trim().equalsIgnoreCase("max-age") )
            {
               cookie.setMaxAge(Integer.parseInt(token.substring(token.indexOf("=")+1).trim()) );
               ageSet = true;
            }
            if ( (!domainSet && (token.indexOf("=")!=-1)) && token.substring(0, token.indexOf("=")).trim().equalsIgnoreCase("domain") )
            {
               cookie.setDomain(token.substring(token.indexOf("=")+1).trim());
               domainSet = true;
               cookie.domainIsSet();
            }
            if ( (!pathSet && (token.indexOf("=")!=-1)) && token.substring(0, token.indexOf("=")).trim().equalsIgnoreCase("path") )
            {
               cookie.setPath(token.substring(token.indexOf("=")+1).trim());
               pathSet = true;
               cookie.pathIsSet();
            }
            if ( !portSet && ((token.indexOf("Port") != -1 || token.indexOf("PORT") != -1)
                                                           || token.indexOf("port") != -1) )
            {
               if (token.indexOf("=")==-1)
                 cookie.setPort(port);
               else
                 cookie.setPort(token.substring(token.indexOf("=")+1).trim());
               portSet = true;
               cookie.portIsSet();
            }
          }
          if (!domainSet)
          {
             cookie.setDomain(domain);
          }
          if (!pathSet)
          {
             cookie.setPath(path);
          }
          // set the version attribute
          cookie.setVersion(1);
          // checks to see if this cookie should replace one already stored
          for (int index = 0; index < cookies.size(); index++)
          {
             WebProxyCookie old = (WebProxyCookie) cookies.elementAt(index);
             if ( cookie.getName().equals(old.getName()) )
             {
                String newPath = cookie.getPath();
                String newDomain = cookie.getDomain();
                String oldPath = old.getPath();
                String oldDomain = old.getDomain();
                if (newDomain.equalsIgnoreCase(oldDomain) && newPath.equals(oldPath))
                     cookies.removeElement(old);
             }
          }
          // handles the max-age cookie attribute (according to rfc 2965)
          int expires = cookie.getMaxAge();
          if (expires < 0)
          {
            // cookie persists until browser shutdown so add cookie to
            // cookie vector
            cookies.addElement(cookie);
          }
          else if (expires == 0)
          {
            // cookie is to be discarded immediately, do not store
          }
          else
          {
            // add the cookie to the cookie vector and then
            // set the expiry date for the cookie
            Date d = new Date();
            cookie.setExpiryDate(new Date((long) d.getTime()+(expires*1000)) );
            cookies.addElement(cookie);
          }
      }
    }
  }

  /**
   * Processes the Cookie header.
   *
   * @param headerVal The value of the header
   * @param domain The domain value of the cookie
   * @param path The path value of the cookie
   * @param port The port value of the cookie
   */
  private void processSetCookieHeader (String headerVal, String domain, String path, String port, Vector cookies)
  throws ParseException
  {
     StringTokenizer cookieValue;
     String token;
     WebProxyCookie cookie;
     if ( ( (headerVal.indexOf("Expires=") != -1)
              || (headerVal.indexOf("expires=") != -1) )
              || (headerVal.indexOf("EXPIRES=") != -1) )
     {
       // there is only one cookie (old netscape spec)
       cookieValue = new StringTokenizer(headerVal, ";");
       token = cookieValue.nextToken();
       if ( token.indexOf("=") != -1)
       {
          cookie = new WebProxyCookie ( token.substring(0, token.indexOf("=")), token.substring(token.indexOf("=")+1).trim() );
       }
       else
       {
          LogService.instance().log(LogService.DEBUG, "CWebProxy: Invalid Header: \"Set-Cookie:"+headerVal+"\"");
          cookie = null;
       }
       // set max-age, path and domain of cookie
       if (cookie != null)
       {
         boolean ageSet = false;
         boolean domainSet = false;
         boolean pathSet = false;
         while( cookieValue.hasMoreTokens() )
         {
           token = cookieValue.nextToken();
           if ( (!ageSet && (token.indexOf("=")!=-1)) && token.substring(0, token.indexOf("=")).trim().equalsIgnoreCase("expires") )
           {
              SimpleDateFormat f = new SimpleDateFormat("EEE, d-MMM-yyyy HH:mm:ss z", Locale.ENGLISH);
	      f.setTimeZone(TimeZone.getTimeZone("GMT"));
              f.setLenient(true);
              Date date = f.parse( token.substring(token.indexOf("=")+1).trim());
              Date current = new Date();
              if (date!=null)
              {
                //set max-age for cookie
                long l;
                if (date.before(current))
                   //accounts for the case where max age is 0 and cookie
                   //should be discarded immediately
                   l = 0;
                else
                   l = date.getTime() - current.getTime();
                int exp = (int) l / 1000;
                cookie.setMaxAge(exp);
                ageSet = true;
              }
           }
           if ( (!domainSet && (token.indexOf("=")!=-1)) && token.substring(0, token.indexOf("=")).trim().equalsIgnoreCase("domain") )
           {
              cookie.setDomain(token.substring(token.indexOf("=")+1).trim());
              domainSet = true;
           }
           if ( (!pathSet && (token.indexOf("=")!=-1)) && token.substring(0, token.indexOf("=")).trim().equalsIgnoreCase("path") )
           {
              cookie.setPath(token.substring(token.indexOf("=")+1).trim());
              pathSet = true;
           }
         }
         if (!domainSet)
         {
            cookie.setDomain(domain);
         }
         if (!pathSet)
         {
            cookie.setPath(path);
         }
         // sets the version attribute of the cookie
         cookie.setVersion(0);
         // checks to see if this cookie should replace one already stored
         for (int index = 0; index < cookies.size(); index++)
         {
            WebProxyCookie old = (WebProxyCookie) cookies.elementAt(index);
            if ( cookie.getName().equals(old.getName()) )
            {
               String newPath = cookie.getPath();
               String newDomain = cookie.getDomain();
               String oldPath = old.getPath();
               String oldDomain = old.getDomain();
               if ( newDomain.equalsIgnoreCase(oldDomain) && newPath.equals(oldPath) )
                  cookies.removeElement(old);
            }
         }
         // handles the max-age cookie attribute (according to rfc 2965)
         int expires = cookie.getMaxAge();
         if (expires < 0)
         {
           // cookie persists until browser shutdown so add cookie to
           // cookie vector
           cookies.addElement(cookie);
         }
         else if (expires == 0)
         {
           // cookie is to be discarded immediately, do not store
         }
         else
         {
           // add the cookie to the cookie vector and then
           // set the expiry date for the cookie
           Date d = new Date();
           cookie.setExpiryDate( new Date((long)d.getTime()+(expires*1000) ) );
           cookies.addElement(cookie);
         }
       }
     }
     else
     {
       // can treat according to RCF 2965
       processSetCookie2Header(headerVal, domain, path, port, cookies);
     }
  }

  /**
   * This class is used by CWebProxy to store cookie information.
   * WebProxyCookie extends javax.servlet.http.Cookie
   * and contains methods to query the cookie's attribute status.
   *
   */
  private class WebProxyCookie extends Cookie
  {

     protected String port = null;
     protected boolean pathSet = false;
     protected boolean domainSet = false;
     protected boolean portSet = false;
     protected Date expiryDate = null;

     public WebProxyCookie(String name, String value)
     {
       super(name, value);
     }

     public void setExpiryDate(Date expiryDate)
     {
       this.expiryDate = expiryDate;
     }

     public Date getExpiryDate()
     {
       return expiryDate;
     }

     public String getPath()
     {
       String path = super.getPath();
       if (path.startsWith("\"") && path.endsWith("\""))
         path = path.substring(1, path.length()-1);
       return path;
     }

     public String getValue()
     {
       String value = super.getValue();
       if (value.startsWith("\"") && value.endsWith("\""))
         value = value.substring(1, value.length()-1);
       return value;
     }

     public void pathIsSet()
     {
       pathSet = true;
     }

     public void domainIsSet()
     {
       domainSet = true;
     }

     public void portIsSet()
     {
       portSet = true;
     }

     public void setPort(String port)
     {
       this.port = port;
     }

     public String getPort()
     {
       return port;
     }

     public boolean isPathSet()
     {
       return pathSet;
     }

     public boolean isDomainSet()
     {
       return domainSet;
     }

     public boolean isPortSet()
     {
       return portSet;
     }
  }

  public ChannelCacheKey generateKey(String uid)
  {
    ChannelState state = (ChannelState)stateTable.get(uid);

    if (state == null)
    {
      LogService.instance().log(LogService.ERROR,"CWebProxy:generateKey() : attempting to access a non-established channel! setStaticData() hasn't been called on uid=\""+uid+"\"");
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

    if ( state.cacheScope.equalsIgnoreCase("instance") ) {
      k.setKeyScope(ChannelCacheKey.INSTANCE_KEY_SCOPE);
    } else {
      k.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
      sbKey.append(systemCacheId).append(": ");
      if ( state.cacheScope.equalsIgnoreCase("user") ) {
        sbKey.append("userId:").append(state.id).append(", ");
      }
    }
    // Later:
    // if scope==guest, do same as user, but use GUEST instead if isGuest()
    // Scope descending order: system, guest, user, instance.

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
    sbKey.append("fullxmlUri:").append(state.fullxmlUri).append(", ");
    sbKey.append("passThrough:").append(state.passThrough).append(", ");
    sbKey.append("tidy:").append(state.tidy);
    k.setKey(sbKey.toString());
    k.setKeyValidity(new Long(System.currentTimeMillis()));
    return k;
  }

  public boolean isCacheValid(Object validity,String uid)
  {
    if (!(validity instanceof Long))
      return false;

    ChannelState state = (ChannelState)stateTable.get(uid);

    if (state == null)
    {
      LogService.instance().log(LogService.ERROR,"CWebProxy:isCacheValid() : attempting to access a non-established channel! setStaticData() hasn't been called on uid=\""+uid+"\"");
      return false;
    }
    else
    return (System.currentTimeMillis() - ((Long)validity).longValue() < state.cacheTimeout*1000);
  }
  
  public String getContentType(String uid) {
    ChannelState state = (ChannelState)stateTable.get(uid);
    return state.connHolder.getContentType();
  }
  
  public InputStream getInputStream(String uid) throws IOException {
    ChannelState state = (ChannelState)stateTable.get(uid);
    InputStream rs = state.connHolder.getInputStream();
    state.connHolder = null;
    return rs;
  }
  
  public void downloadData(OutputStream out,String uid) throws IOException {
    throw(new IOException("CWebProxy: donloadData method not supported - use getInputStream only"));
  }
  
  public String getName(String uid) {
    ChannelState state = (ChannelState)stateTable.get(uid);
    return "proxyDL";
  }
  
  public Map getHeaders(String uid) {
    ChannelState state = (ChannelState)stateTable.get(uid);
    try {
      state.connHolder= getConnection(state.fullxmlUri, state);
    }
    catch (Exception e){
      LogService.instance().log(LogService.ERROR,e);
    }
    Map rhdrs = new HashMap();
    int i = 0;
    while (state.connHolder.getHeaderFieldKey(i) != null){
      rhdrs.put(state.connHolder.getHeaderFieldKey(i),state.connHolder.getHeaderField(i));
      i++;
    }
    return rhdrs;
  }
  
}

/*
 * Developer's notes for convenience.  Will be deleted later.
 * Cache control parameters:
 *  Static params
 *    cw_cacheDefaultTimeout	timeout in seconds.
 *    cw_cacheDefaultScope	"system" - one copy for all users
 *				"guest" - one copy for guest, others by user
 *				"user" - one copy per user
 *    				"instance" - cache for this instance only
 *    cw_cacheDefaultMode		"none" - normally don't cache
 *    				"init" - only cache initial view
 *    				"http" - follow http caching directives
 *				"all" - why not?  cache everything.
 *  Runtime only params:
 *    cw_cacheTimeout		override default for this request only
 *    cw_cacheScope		override default for this request only
 *    cw_cacheMode		override default for this request only
 *
 * Note: all static parameters can be replaced via equivalent runtime.
 *
 * The Scope can only be reduced, never increased.
 */
/*
 * NOTE could cw_person be multi-valued instead of comma-sep?
 *      cw_restrict should work the same way.
 * NOTE Does IPerson contain multiple instances of attributes?
 * cw_restrict - a list of runtime parameters that cannot be changed.
 *               possibly allow multi-values, with params. indicating
 *               the param can only be changed to that?
 *	       - can we encode the scope restrictions with this
 *	         as a default?
 */
