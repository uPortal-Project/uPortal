/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels;

import java.util.Enumeration;
import java.util.Hashtable;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.PortalException;
import org.jasig.portal.StandaloneChannelRenderer;
import org.jasig.portal.UserLayoutStoreFactory;
import org.jasig.portal.UserProfile;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

/**
 * CSelectSystemProfile channel allows to establish mapping from user-Agent to a system profile.
 * This channel is meant to be used by a guest user only.  Logged in users are expected to use
 * CUserPreferences profile management screens to achieve the establish user-Agent mappings.
 * 
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


  public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
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

  protected Hashtable getSystemProfileList() throws PortalException {
      if (systemProfileList == null) {
          try {
              systemProfileList = ulsdb.getSystemProfileList();
          } catch (Exception e) {
              throw new PortalException(e.getMessage(), e);
          }
      }
      
      return  systemProfileList;
  }


  public void renderXML(ContentHandler out) throws PortalException {
    Document doc = DocumentFactory.getNewDocument();
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

    XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
    xslt.setXML(doc);
    xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
    xslt.transform();
  }
}



