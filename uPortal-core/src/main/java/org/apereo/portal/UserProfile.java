/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal;

import java.io.Serializable;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.portal.i18n.LocaleManager;

/** A user profile associates a user layout with a structure and theme stylesheet. */
public class UserProfile implements Serializable, IUserProfile {
    private static final long serialVersionUID = 1L;

    private int id = -1;
    private String fname = null;
    private String pName;
    private String description;
    private int layout_id;
    private int struct_ss_id;
    private int theme_ss_id;
    private boolean system = false;
    private LocaleManager localeManager;

    public UserProfile() {};

    public UserProfile(
            int id,
            String fname,
            String name,
            String desc,
            int layout_id,
            int struct_ss,
            int theme_ss) {
        this.id = id;
        this.fname = fname;
        pName = name;
        description = desc != null ? desc : "";
        this.layout_id = layout_id;
        struct_ss_id = struct_ss;
        theme_ss_id = theme_ss;
    }

    public UserProfile(UserProfile userProfile) {
        this.id = userProfile.id;
        this.fname = userProfile.fname;
        pName = userProfile.pName;
        description = userProfile.description;
        this.layout_id = userProfile.layout_id;
        struct_ss_id = userProfile.struct_ss_id;
        theme_ss_id = userProfile.theme_ss_id;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IUserProfile#getProfileId()
     */
    @Override
    public int getProfileId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IUserProfile#getProfileFname()
     */
    @Override
    public String getProfileFname() {
        return fname;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IUserProfile#getProfileName()
     */
    @Override
    public String getProfileName() {
        return pName;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IUserProfile#getProfileDescription()
     */
    @Override
    public String getProfileDescription() {
        return description;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IUserProfile#getLayoutId()
     */
    @Override
    public int getLayoutId() {
        return layout_id;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IUserProfile#getStructureStylesheetId()
     */
    @Override
    public int getStructureStylesheetId() {
        return struct_ss_id;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IUserProfile#getThemeStylesheetId()
     */
    @Override
    public int getThemeStylesheetId() {
        return theme_ss_id;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IUserProfile#isSystemProfile()
     */
    @Override
    public boolean isSystemProfile() {
        return system;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IUserProfile#setProfileId(int)
     */
    @Override
    public void setProfileId(int id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IUserProfile#setProfileFname(java.lang.String)
     */
    @Override
    public void setProfileFname(String fname) {
        this.fname = fname;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IUserProfile#setProfileName(java.lang.String)
     */
    @Override
    public void setProfileName(String name) {
        pName = name;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IUserProfile#setProfileDescription(java.lang.String)
     */
    @Override
    public void setProfileDescription(String desc) {
        description = desc;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IUserProfile#setLayoutId(int)
     */
    @Override
    public void setLayoutId(int layout_id) {
        this.layout_id = layout_id;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IUserProfile#setStructureStylesheetId(int)
     */
    @Override
    public void setStructureStylesheetId(int ss_id) {
        struct_ss_id = ss_id;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IUserProfile#setThemeStylesheetId(int)
     */
    @Override
    public void setThemeStylesheetId(int ss_id) {
        theme_ss_id = ss_id;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IUserProfile#setSystemProfile(boolean)
     */
    @Override
    public void setSystemProfile(boolean s) {
        system = s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfile that = (UserProfile) o;
        return id == that.id && system == that.system;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, system);
    }

    // uPortal i18n
    /* (non-Javadoc)
     * @see org.apereo.portal.IUserProfile#setLocaleManager(org.apereo.portal.i18n.LocaleManager)
     */
    @Override
    public void setLocaleManager(LocaleManager lm) {
        localeManager = lm;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.IUserProfile#getLocaleManager()
     */
    @Override
    public LocaleManager getLocaleManager() {
        return localeManager;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("fname", fname)
                .append("name", pName)
                .append("description", description)
                .append("layoutId", layout_id)
                .append("struct_ss_id", struct_ss_id)
                .append("theme_ss_id", theme_ss_id)
                .append("system", system)
                .append("localeManager", localeManager)
                .toString();
    }
}
