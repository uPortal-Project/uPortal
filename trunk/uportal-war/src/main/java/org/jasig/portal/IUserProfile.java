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

import org.jasig.portal.i18n.LocaleManager;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IUserProfile {

    public static final String USER_PROFILE = "UserProfile";
    public static final String DEFAULT_PROFILE_FNAME = "default";

    public abstract int getProfileId();

    public abstract String getProfileFname();

    public abstract String getProfileName();

    public abstract String getProfileDescription();

    public abstract int getLayoutId();

    public abstract int getStructureStylesheetId();

    public abstract int getThemeStylesheetId();

    public abstract boolean isSystemProfile();

    public abstract void setProfileId(int id);

    public abstract void setProfileFname(String fname);

    public abstract void setProfileName(String name);

    public abstract void setProfileDescription(String desc);

    public abstract void setLayoutId(int layout_id);

    public abstract void setStructureStylesheetId(int ss_id);

    public abstract void setThemeStylesheetId(int ss_id);

    public abstract void setSystemProfile(boolean s);

    // uPortal i18n
    public abstract void setLocaleManager(LocaleManager lm);

    public abstract LocaleManager getLocaleManager();

}