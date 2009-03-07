/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.layout.dlm;

import org.jasig.portal.UserProfile;
import org.w3c.dom.Document;


/**
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
class UserView
{
    public static final String RCS_ID = "@(#) $Header$";

    private int userId = -1;
    DistributedUserPreferences structUserPrefs = null;
    DistributedUserPreferences themeUserPrefs = null;
    Document layout = null;
    int layoutId = 0;
    int profileId = 1;
    int structureStylesheetId = 0;
    int themeStylesheetId = 0;

    UserView (int userId)
    {
        this.userId = userId;
    }
    
    UserView(UserProfile profile, Document layout,
            DistributedUserPreferences ssup, DistributedUserPreferences tsup)
    {
        layoutId = profile.getLayoutId();
        profileId = profile.getProfileId();
        structureStylesheetId = profile.getStructureStylesheetId();
        themeStylesheetId = profile.getThemeStylesheetId();
        this.layout = layout;
        structUserPrefs = ssup;
        themeUserPrefs = tsup;
    }
    
    public int getUserId() {
        return this.userId;
    }
    
}
