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
  private static final String sslLocation = "CSelectSystemProfile/CSelectSystemProfile.ssl";
  IUserLayoutStore ulsdb;
  private Hashtable systemProfileList;


    public CSelectSystemProfile() {
      ulsdb = UserLayoutStoreFactory.getUserLayoutStoreImpl();
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
          try {
            ulsdb.setSystemBrowserMapping(this.runtimeData.getBrowserInfo().getUserAgent(), Integer.parseInt(profileId));
          } catch (Exception e) {
            throw new PortalException(e.getMessage(), e);
          }
        }
      }
    }
  }

  /**
   * put your documentation comment here
   * @return hashtable of system profile
   * @exception PortalException
   */
  protected Hashtable getSystemProfileList () throws PortalException {
    if (systemProfileList == null)
      try {
        systemProfileList = ulsdb.getSystemProfileList();
      } catch (Exception e) {
        throw new PortalException(e.getMessage(), e);
      }

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

    XSLT xslt = new XSLT(this);
    xslt.setXML(doc);
    xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
    xslt.transform();
  }
}



