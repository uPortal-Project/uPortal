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

    public UserProfile getUserProfileById(int userId,String profileName);
    public Hashtable getUserProfileList(int userId);


    public UserProfile getSystemProfile(String userAgent);
    public void setSystemProfile(UserProfile profile);
    public UserProfile getSystemProfileByName(String profileName);
    public Hashtable getSystemProfileList();

    public void setUserBrowserMapping(int userId,String userAgent,String profileName);
    public void setSystemBrowserMapping(String userAgent,String systemProfileName);

    public String getUserBrowserMapping(int userId,String userAgent);
    public String getSystemBrowserMapping(String userAgent);


    //    public UserPreferences getUserPreferences(int userId, String profileName);
    public UserPreferences getUserPreferences(int userId, UserProfile profile);

    public void putUserPreferences(int userId, UserPreferences up);

    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences(int userId,String profileName,String stylesheetName);
    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences(int userId,String profileName,String stylesheetName);


    public void setStructureStylesheetUserPreferences(int userId,String profileName, StructureStylesheetUserPreferences fsup);
    public void setThemeStylesheetUserPreferences(int userId,String profileName, ThemeStylesheetUserPreferences ssup);


}
