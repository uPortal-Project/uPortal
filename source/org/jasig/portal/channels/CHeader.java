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

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NotContextException;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ICacheable;
import org.jasig.portal.PortalException;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;


/**
 * This channel provides content for a page header.  It is indended
 * to be included in a layout folder of type "header".  Most stylesheets
 * will render the content of such header channels consistently on every
 * page.
 * @author Peter Kharchenko, pkharchenko@interactivebusiness.com
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @version $Revision$
 */
public class CHeader extends BaseChannel implements ICacheable {
  
  private static final String sslLocation = "CHeader/CHeader.ssl";

  /**
   * Checks user permissions to see if the user is authorized to publish channels
   * @return true if user can publish
   */
  private boolean canUserPublish() {
    boolean canPublish = false;    
    try {
	    // Let the authorization service decide:
	    canPublish = staticData.getAuthorizationPrincipal().canPublish();      
    } catch (Exception e) {
      LogService.log(LogService.ERROR, e);
      // Deny the user publish access if anything went wrong
    }
    return canPublish;
  }

  /**
   * Gets the current date/time with specified format
   * @param format the format string
   * @return a formatted date and time string
   */
  public static String getDate(String format) {
    try {
      // Format the current time.
      java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(format);
      java.util.Date currentTime = new java.util.Date();
      return formatter.format(currentTime);
    }
    catch (Exception e) {
      LogService.log(LogService.ERROR, e);
    }

    return "&nbsp;";
  }

  /**
   * Returns the DOM object associated with the user
   * @return DOM object associated with the user
   */
  private Document getUserXML() {
    // Get the fullname of the current user
    String fullName = (String)staticData.getPerson().getFullName();
    // Get a new DOM instance
    Document doc = DocumentFactory.getNewDocument();
    // Create <header> element
    Element headerEl = doc.createElement("header");
    // Create <full-name> element under <header>
    Element fullNameEl = doc.createElement("full-name");
    fullNameEl.appendChild(doc.createTextNode(fullName));
    headerEl.appendChild(fullNameEl);
    // Create <timestamp-long> element under <header>
    Element timeStampLongEl = doc.createElement("timestamp-long");
    timeStampLongEl.appendChild(doc.createTextNode(getDate("EEEE, MMM d, yyyy 'at' hh:mm a")));
    headerEl.appendChild(timeStampLongEl);
    // Create <timestamp-short> element under <header>
    Element timeStampShortEl = doc.createElement("timestamp-short");
    timeStampShortEl.appendChild(doc.createTextNode(getDate("M.d.y h:mm a")));
    headerEl.appendChild(timeStampShortEl);
    // Don't render the publish, subscribe, user preferences links if it's the guest user
    if (staticData.getPerson().getSecurityContext().isAuthenticated()) {
      Context globalIDContext = null;
      try {
        // Get the context that holds the global IDs for this user
        globalIDContext = (Context)staticData.getJNDIContext().lookup("/channel-ids");
      } catch (NotContextException nce) {
        LogService.log(LogService.ERROR, "CHeader.getUserXML(): Could not find subcontext /channel-ids in JNDI");
      } catch (NamingException e) {
        LogService.log(LogService.ERROR, e);
      }
      try {
        if (canUserPublish()) {
          // Create <chan-mgr-chanid> element under <header>
          Element chanMgrChanidEl = doc.createElement("chan-mgr-chanid");
          chanMgrChanidEl.appendChild(doc.createTextNode((String)globalIDContext.lookup("/portal/channelmanager/general")));
          headerEl.appendChild(chanMgrChanidEl);
        }
      } catch (NotContextException nce) {
        LogService.log(LogService.ERROR, "CHeader.getUserXML(): Could not find channel ID for fname=/portal/channelmanager/general for UID="
            + staticData.getPerson().getID() + ". Be sure that the channel is in their layout.");
      } catch (NamingException e) {
        LogService.log(LogService.ERROR, e);
      }
      try {
        // Create <preferences-chanid> element under <header>
        Element preferencesChanidEl = doc.createElement("preferences-chanid");
        preferencesChanidEl.appendChild(doc.createTextNode((String)globalIDContext.lookup("/portal/userpreferences/general")));
        headerEl.appendChild(preferencesChanidEl);
      } catch (NotContextException nce) {
        LogService.log(LogService.ERROR, "CHeader.getUserXML(): Could not find channel ID for fname=/portal/userpreferences/general for UID="
            + staticData.getPerson().getID() + ". Be sure that the channel is in their layout.");
      } catch (NamingException e) {
        LogService.log(LogService.ERROR, e);
      }
    }
    doc.appendChild(headerEl);
    return doc;
  }

  /**
   * ICacheable method - generates cache key
   * @return key the cache key
   */  
  public ChannelCacheKey generateKey() {
    ChannelCacheKey k = new ChannelCacheKey();
    StringBuffer sbKey = new StringBuffer(1024);

    sbKey.append("org.jasig.portal.CHeader: ");

    if(staticData.getPerson().isGuest()) {
        // guest users are cached system-wide. 
        k.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
        sbKey.append("userId:").append(staticData.getPerson().getID()).append(", ");
    } else {
        // otherwise cache is instance-specific
        k.setKeyScope(ChannelCacheKey.INSTANCE_KEY_SCOPE);
    }
    sbKey.append("authenticated:").append(staticData.getPerson().getSecurityContext().isAuthenticated()).append(", ");
    sbKey.append("baseActionURL:").append(runtimeData.getBaseActionURL()).append(", ");
    sbKey.append("hasPermissionToPublish:").append(String.valueOf(canUserPublish())).append(", ");
    sbKey.append("stylesheetURI:");
    try {
      String sslUri = ResourceLoader.getResourceAsURLString(this.getClass(), sslLocation);
      sbKey.append(XSLT.getStylesheetURI(sslUri, runtimeData.getBrowserInfo()));
    } catch (Exception e) {
      sbKey.append("not defined");
    }
    k.setKey(sbKey.toString());
    k.setKeyValidity(new Long(System.currentTimeMillis()));
    return k;
  }  
  
  /**
   * ICacheable method - checks validity of cache
   * @param validity the validity object
   * @return cacheValid <code>true</code> if cache is still valid, otherwise <code>false</code>
   */    
  public boolean isCacheValid (Object validity) {
    boolean cacheValid = false;
    if (validity instanceof Long) {
      Long oldtime = (Long)validity;
      if (!staticData.getPerson().getSecurityContext().isAuthenticated()) {
        // cache entries for unauthenticated users don't expire
        cacheValid = true;
      } else if (System.currentTimeMillis() - oldtime.longValue() < 1*60*1000) {
        cacheValid = true;
      }
    }
    return cacheValid;
  }

  /**
   * Render method.
   * @param out the content handler
   * @exception PortalException
   */
  public void renderXML (ContentHandler out) throws PortalException {
    // Perform the transformation
    XSLT xslt = new XSLT(this);
    xslt.setXML(getUserXML());
    xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
    if (staticData.getPerson().getSecurityContext().isAuthenticated()) {
      xslt.setStylesheetParameter("authenticated", "true");
    }
    xslt.transform();
  }
}
