package org.jasig.portal;

/**
 * Interface through which portal talks to the user preferences database
 * @author Peter Kharchenko
 * @version $Revision$
 */

import java.util.Hashtable;

public interface IUserPreferencesDB {

    public UserProfile getUserProfile(int userId, String userAgent);
    public void setUserProfile(int userId,UserProfile profile);

    public UserProfile getUserProfileById(int userId,int profileId);
    public Hashtable getUserProfileList(int userId);


    public UserProfile getSystemProfile(String userAgent);
    public void setSystemProfile(UserProfile profile);
    public UserProfile getSystemProfileById(int profileId);
    public Hashtable getSystemProfileList();

    public void setUserBrowserMapping(int userId,String userAgent,int profileId);
    public void setSystemBrowserMapping(String userAgent,int systemProfileId);

    // returns profileId
    public Integer getUserBrowserMapping(int userId,String userAgent);
    public Integer getSystemBrowserMapping(String userAgent);


    public UserPreferences getUserPreferences(int userId, UserProfile profile);

    public void putUserPreferences(int userId, UserPreferences up);

    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences(int userId,int profileId,String stylesheetName);
    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences(int userId,int profileId,String stylesheetName);


    public void setStructureStylesheetUserPreferences(int userId,int profileId, StructureStylesheetUserPreferences fsup);
    public void setThemeStylesheetUserPreferences(int userId,int profileId, ThemeStylesheetUserPreferences ssup);


}
