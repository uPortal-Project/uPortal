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
 */

package org.jasig.portal;

import org.jasig.portal.i18n.LocaleManager;

/**
 * A user profile associates a user layout with a structure and theme stylesheet.
 * @author Peter Kharchenko
 * @version $Revision$
 */
public class UserProfile {
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
}
