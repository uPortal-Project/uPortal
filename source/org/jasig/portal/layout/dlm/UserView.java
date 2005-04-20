
package org.jasig.portal.layout.dlm;

import org.jasig.portal.UserProfile;
import org.w3c.dom.Document;


class UserView
{
    public static final String RCS_ID = "@(#) $Header$";

    DistributedUserPreferences structUserPrefs = null;
    DistributedUserPreferences themeUserPrefs = null;
    Document layout = null;
    int layoutId = 0;
    int profileId = 1;
    int structureStylesheetId = 0;
    int themeStylesheetId = 0;

    UserView ()
    {
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
}
