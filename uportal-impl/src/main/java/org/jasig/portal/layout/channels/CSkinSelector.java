/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/


package org.jasig.portal.layout.channels;

import java.io.IOException;
import java.io.InputStream;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.IPrivileged;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.PortalSessionManager;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.UserLayoutStoreFactory;
import org.jasig.portal.properties.PropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.XSLT;
import org.xml.sax.ContentHandler;

/**
 * A channel for selecting skins.
 *
 * The path to the available skins is configured by the portal.properties
 * property "org.jasig.portal.layout.channels.CSkinSelector.skins_path" as
 * mediated by PropertiesManager.  If that property is not configured, the path
 * "media/skins/universality/" is used.
 *
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
  public class CSkinSelector extends BaseChannel implements IPrivileged {

      private static final Log log = LogFactory.getLog(CSkinSelector.class);

      private static final String SKINS_PATH = PropertiesManager.getProperty(CSkinSelector.class.getName() + ".skins_path", "media/skins/universality/");

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
        throw new PortalException(e);
      }
     }


    public void renderXML (ContentHandler out) throws PortalException {

    	InputStream xmlStream = null;
        try {
      xmlStream = PortalSessionManager.getResourceAsStream(SKINS_PATH + "/skinList.xml");
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
    } finally {
			try {
				if (xmlStream != null)
					xmlStream.close();
			} catch (IOException ioe) {
				log.error("CSkinSelector:renderXML():: Can not close InputStream "+ ioe);
			}
		}
	}

  }