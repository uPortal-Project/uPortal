/**
 * Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.layout.channels;

import java.io.InputStream;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.IPrivileged;
import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.PortalSessionManager;
import org.jasig.portal.UserLayoutStoreFactory;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.utils.XSLT;
import org.xml.sax.ContentHandler;

/**
 * A channel for selecting skins.
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
  public class CSkinSelector extends BaseChannel implements IPrivileged {

    private static final String SKINS_PATH = "media/org/jasig/portal/layout/AL_TabColumn/integratedModes";
    private static final String sslLocation = "/org/jasig/portal/channels/CSkinSelector/CSkinSelector.ssl";
    private PortalControlStructures controlStructures;
    private IUserPreferencesManager upm;
    private static IUserLayoutStore store = UserLayoutStoreFactory.getUserLayoutStoreImpl();

    public CSkinSelector() {
       super();
    }

     /**
     * Passes portal control structure to the channel.
     * @see PortalControlStructures
     */
    public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
        controlStructures = pcs;
        if ( upm == null )
          upm = controlStructures.getUserPreferencesManager();
    }


    public void setRuntimeData (ChannelRuntimeData rd) throws PortalException {
        runtimeData = rd;
        String action = runtimeData.getParameter("action");
        if (action != null) {
         if (runtimeData.getParameter("submitSave")!=null) {
                String skinName = runtimeData.getParameter("skinName");
                UserPreferences userPrefs = upm.getUserPreferences();
                userPrefs.getThemeStylesheetUserPreferences().putParameterValue("skin",skinName);
                saveUserPreferences(userPrefs);
         }
        }
    }


     private void saveUserPreferences ( UserPreferences userPrefs ) throws PortalException {
      try {
          store.putUserPreferences(staticData.getPerson(), userPrefs);
      } catch (Exception e) {
        throw new PortalException(e.getMessage(), e);
      }
     }


    public void renderXML (ContentHandler out) throws PortalException {

      InputStream xmlStream = PortalSessionManager.getResourceAsStream(SKINS_PATH + "/skinList.xml");
      UserPreferences userPrefs = upm.getUserPreferences();
      String currentSkin = userPrefs.getThemeStylesheetUserPreferences().getParameterValue("skin");

      XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
      xslt.setXML(xmlStream);
      xslt.setXSL(sslLocation, "skinSelector", runtimeData.getBrowserInfo());
      xslt.setTarget(out);
      xslt.setStylesheetParameter("skinsPath", SKINS_PATH);
      xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
      if(currentSkin!=null)
        xslt.setStylesheetParameter("currentSkin", currentSkin);
      xslt.transform();
    }

  }