package org.jasig.portal;

import org.w3c.dom.*;
import java.util.*;

/**
 * Object managing user preferences.
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class UserPreferences {

    protected UserProfile profile;
    
    protected StructureStylesheetUserPreferences fsup;
    protected ThemeStylesheetUserPreferences ssup;

    /*
     * Copy-constructor
     */
    public UserPreferences(UserPreferences up) {
	fsup=new StructureStylesheetUserPreferences(up.getStructureStylesheetUserPreferences());
 	ssup=new ThemeStylesheetUserPreferences(up.getThemeStylesheetUserPreferences());
	profile=up.getProfile();
    }

    
    public void setProfile(UserProfile p) { profile=p; }
    public UserProfile getProfile() { return profile; }

    public UserPreferences(UserProfile p) { this.profile=p; }

    public void setStructureStylesheetUserPreferences(StructureStylesheetUserPreferences up) {
	this.fsup=up;
    }

    public void setThemeStylesheetUserPreferences(ThemeStylesheetUserPreferences up) {
	this.ssup=up;
    }

    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences() {
	return this.fsup;
    }

    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences() {
	return this.ssup;
    }
    
    public void synchronizeWithUserLayoutXML(Document uLayoutXML) {
	// make a list of channels in the XML Layout
        NodeList channelNodes=uLayoutXML.getElementsByTagName("channel");
        HashSet channelSet=new HashSet();
        for(int i=0;i<channelNodes.getLength();i++) {
            Element el=(Element) channelNodes.item(i);
            if(el!=null) {
                String chID=el.getAttribute("ID");
                if(!fsup.hasChannel(chID)) {
                    fsup.addChannel(chID);
                    //		    Logger.log(Logger.DEBUG,"UserLayoutManager::synchUserPreferencesWithLayout() : StructureStylesheetUserPreferences were missing a channel="+chID);
                }
                if(!ssup.hasChannel(chID)) {
                    ssup.addChannel(chID);
                    //		    Logger.log(Logger.DEBUG,"UserLayoutManager::synchUserPreferencesWithLayout() : ThemeStylesheetUserPreferences were missing a channel="+chID);
                }
                channelSet.add(chID);
            }

        }

        // make a list of categories in the XML Layout
        NodeList folderNodes=uLayoutXML.getElementsByTagName("folder");
        HashSet folderSet=new HashSet();
        for(int i=0;i<folderNodes.getLength();i++) {
            Element el=(Element) folderNodes.item(i);
            if(el!=null) {
                String caID=el.getAttribute("ID");
                if(!fsup.hasFolder(caID)) {
                    fsup.addFolder(caID);
                    //		    Logger.log(Logger.DEBUG,"UserLayoutManager::synchUserPreferencesWithLayout() : StructureStylesheetUserPreferences were missing a folder="+caID);
                }
                folderSet.add(caID);
            }
        }

        // cross check
        for(Enumeration e=fsup.getChannels();e.hasMoreElements();) {
            String chID=(String)e.nextElement();
            if(!channelSet.contains(chID)) {
                fsup.removeChannel(chID);
                //		Logger.log(Logger.DEBUG,"UserLayoutManager::synchUserPreferencesWithLayout() : StructureStylesheetUserPreferences had a non-existent channel="+chID);
            }
        }

        for(Enumeration e=fsup.getFolders();e.hasMoreElements();) {
            String caID=(String)e.nextElement();
            if(!folderSet.contains(caID)) {
                fsup.removeFolder(caID);
                //		Logger.log(Logger.DEBUG,"UserLayoutManager::synchUserPreferencesWithLayout() : StructureStylesheetUserPreferences had a non-existent folder="+caID);
            }
        }

        for(Enumeration e=ssup.getChannels();e.hasMoreElements();) {
            String chID=(String)e.nextElement();
            if(!channelSet.contains(chID)) {
                ssup.removeChannel(chID);
                //		Logger.log(Logger.DEBUG,"UserLayoutManager::synchUserPreferencesWithLayout() : ThemeStylesheetUserPreferences had a non-existent channel="+chID);
            }
        }

    }

}
