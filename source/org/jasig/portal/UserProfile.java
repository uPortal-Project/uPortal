/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.io.Serializable;

import org.jasig.portal.i18n.LocaleManager;

/**
 * A user profile associates a user layout with a structure and theme stylesheet.
 * @author Peter Kharchenko
 * @version $Revision$
 */
public class UserProfile implements Serializable {
    protected int id=-1;
    protected String pName;
    protected String description;
    protected int layout_id;
    protected int struct_ss_id;
    protected int theme_ss_id;
    protected boolean system=false;
    protected LocaleManager localeManager;

    public UserProfile() {};
    public UserProfile(int id, String name, String desc, int layout_id, int  struct_ss,int theme_ss) {
        this.id=id;
        pName=name;
        description=desc != null ? desc : "";
        this.layout_id=layout_id;
        struct_ss_id=struct_ss;
        theme_ss_id=theme_ss;
    }

    public int getProfileId() { return id; }
    public String getProfileName() { return pName; }
    public String getProfileDescription() { return description; }
    public int getLayoutId() { return layout_id; }
    public int getStructureStylesheetId() { return struct_ss_id; }
    public int getThemeStylesheetId() { return theme_ss_id; }
    public boolean isSystemProfile(){return system; }

    public void setProfileId(int id) { this.id=id; }
    public void setProfileName(String name) { pName=name; }
    public void setProfileDescription(String desc) { description=desc; }
    public void setLayoutId(int layout_id) { this.layout_id=layout_id; }
    public void setStructureStylesheetId(int ss_id) { struct_ss_id=ss_id; }
    public void setThemeStylesheetId(int ss_id) { theme_ss_id=ss_id; }
    public void setSystemProfile(boolean s) { system=s; }
    
    public boolean equals(Object o) {
      boolean retValue = false;
      if (o instanceof UserProfile) {
        UserProfile profile = (UserProfile)o;
        retValue = this.id == profile.id && this.system == profile.system;
      }
      return retValue;
    }

    // uPortal i18n
    public void setLocaleManager(LocaleManager lm) { localeManager = lm; }
    public LocaleManager getLocaleManager() {
	return localeManager;
    }
    public String toString(){
        return "name :"+pName+","+
	        "description: "+description+","+
	        "layout_id: "+layout_id+","+
	        "struct_ss_id: "+struct_ss_id+","+
	        "theme_ss_id: "+theme_ss_id+","+
	        "system: "+system+","+
	        "localeManager: "+localeManager;
    }
}
