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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal.channels;

import  org.jasig.portal.*;
import  org.jasig.portal.security.IPerson;
import  org.jasig.portal.utils.XSLT;
import  org.xml.sax.ContentHandler;
import  java.util.*;
import  javax.servlet.http.*;
import  org.w3c.dom.*;
import  java.io.StringWriter;
import  java.net.URL;


/** <p>Initial profile selection</p>
 * <p> CSelectSystemProfile channel allows to establish mapping from user-Agent to a system profile.
 * This channel is meant to be used by a guest user only. Logged in users are expected to use
 * CUserPreferences profile management screens to achieve the establish user-Agent mappings. </p>
 * @author Peter Kharchenko, peterk@interactivebusiness.com
 * @version $Revision$
 */
public class CSelectSystemProfile extends StandaloneChannelRenderer {
  private static final String sslLocation = UtilitiesBean.fixURI("webpages/stylesheets/org/jasig/portal/channels/CSelectSystemProfile/CSelectSystemProfile.ssl");
  IUserPreferencesStore updb;
  private Hashtable systemProfileList;


    public CSelectSystemProfile() {
    }

  /**
   * put your documentation comment here
   * @param rd
   * @exception PortalException
   */
  public void setRuntimeData (ChannelRuntimeData rd) throws PortalException {
    super.setRuntimeData(rd);
    String action = runtimeData.getParameter("action");
    if (action != null) {
      String profileId = runtimeData.getParameter("profileId");
      boolean systemProfile = false;
      if (profileId != null) {
        String profileType = runtimeData.getParameter("profileType");
        if (action.equals("map")) {
          this.getUserPreferencesStore().setSystemBrowserMapping(this.runtimeData.getBrowserInfo().getUserAgent(), Integer.parseInt(profileId));
        }
      }
    }
  }

  /**
   * put your documentation comment here
   * @return
   * @exception PortalException
   */
  private IUserPreferencesStore getUserPreferencesStore () throws PortalException {
    // this should be obtained from the JNDI context
    if (updb == null) {
      updb = UserPreferencesStoreFactory.getUserPreferencesStoreImpl();
    }
    if (updb == null) {
      throw  new ResourceMissingException("", "User preference database", "Unable to obtain the list of user profiles, since the user preference database is currently down");
    }
    return  updb;
  }

  /**
   * put your documentation comment here
   * @return
   * @exception PortalException
   */
  protected Hashtable getSystemProfileList () throws PortalException {
    if (systemProfileList == null)
      systemProfileList = this.getUserPreferencesStore().getSystemProfileList();
    return  systemProfileList;
  }

  /**
   * put your documentation comment here
   * @param out
   * @exception PortalException
   */
  public void renderXML (ContentHandler out) throws PortalException {
    Document doc = new org.apache.xerces.dom.DocumentImpl();
    Element edEl = doc.createElement("profiles");
    doc.appendChild(edEl);
    // fill out system-defined profiles
    Element sEl = doc.createElement("system");
    for (Enumeration spe = this.getSystemProfileList().elements(); spe.hasMoreElements();) {
      UserProfile p = (UserProfile)spe.nextElement();
      Element pEl = doc.createElement("profile");
      pEl.setAttribute("id", Integer.toString(p.getProfileId()));
      pEl.setAttribute("name", p.getProfileName());
      Element dEl = doc.createElement("description");
      dEl.appendChild(doc.createTextNode(p.getProfileDescription()));
      pEl.appendChild(dEl);
      sEl.appendChild(pEl);
    }
    edEl.appendChild(sEl);
    /*  try {
     LogService.instance().log(LogService.DEBUG,UtilitiesBean.dom2PrettyString(doc));
     } catch (Exception e) {
     LogService.instance().log(LogService.ERROR,e);
     }
     */
    Hashtable params = new Hashtable();
    params.put("baseActionURL", runtimeData.getBaseActionURL());
    try {
      XSLT.transform(doc, new URL(sslLocation), out, params, runtimeData.getBrowserInfo());
    } catch (java.io.IOException i) {
      throw  new GeneralRenderingException("IOException has been encountered");
    }
  }
}



