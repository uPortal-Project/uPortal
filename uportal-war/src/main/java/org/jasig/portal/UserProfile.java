/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
    public static final String USER_PROFILE = "UserProfile";
    public static final String DEFAULT_PROFILE_FNAME = "default";
    protected int id=-1;
    protected String fname = null;
    protected String pName;
    protected String description;
    protected int layout_id;
    protected int struct_ss_id;
    protected int theme_ss_id;
    protected boolean system=false;
    protected LocaleManager localeManager;

    public UserProfile() {};
    public UserProfile(int id, String fname, String name, String desc, int layout_id, int  struct_ss,int theme_ss) {
    	this.id=id;
        this.fname=fname;
        pName=name;
        description=desc != null ? desc : "";
        this.layout_id=layout_id;
        struct_ss_id=struct_ss;
        theme_ss_id=theme_ss;
    }

    public UserProfile(UserProfile userProfile) {
        this.id=userProfile.id;
        this.fname=userProfile.fname;
        pName=userProfile.pName;
        description=userProfile.description;
        this.layout_id=userProfile.layout_id;
        struct_ss_id=userProfile.struct_ss_id;
        theme_ss_id=userProfile.theme_ss_id;
    }

    public int getProfileId() { return id; }
    public String getProfileFname() { return fname; }
    public String getProfileName() { return pName; }
    public String getProfileDescription() { return description; }
    public int getLayoutId() { return layout_id; }
    public int getStructureStylesheetId() { return struct_ss_id; }
    public int getThemeStylesheetId() { return theme_ss_id; }
    public boolean isSystemProfile(){return system; }

    public void setProfileId(int id) { this.id=id; }
    public void setProfileFname(String fname) { this.fname=fname; }
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
