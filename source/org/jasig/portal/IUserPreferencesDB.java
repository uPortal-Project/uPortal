package org.jasig.portal;

/**
 * Interface through which portal talks to the user preferences database
 * @author Peter Kharchenko
 * @version $Revision$
 */

import java.util.Hashtable;

public interface IUserPreferencesDB {
    
    public UserProfile getUserProfile(String userName, String userAgent);
    public void setUserProfile(String userName,UserProfile profile);
    
    public UserProfile getUserProfileByName(String userName,String profileName);
    public Hashtable getUserProfileList(String userName);


    public UserProfile getSystemProfile(String userAgent);
    public void setSystemProfile(UserProfile profile);
    public UserProfile getSystemProfileByName(String profileName);
    public Hashtable getSystemProfileList();

    public void setUserBrowserMapping(String userName,String userAgent,String profileName);
    public void setSystemBrowserMapping(String userAgent,String systemProfileName);
    
    public String getUserBrowserMapping(String userName,String userAgent);
    public String getSystemBrowserMapping(String userAgent);
    

    //    public UserPreferences getUserPreferences(String userName, String profileName);
    public UserPreferences getUserPreferences(String userName, UserProfile profile);
    
    public void putUserPreferences(String userName, UserPreferences up);

    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences(String userName,String profileName,String stylesheetName);
    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences(String userName,String profileName,String stylesheetName);


    public void setStructureStylesheetUserPreferences(String userName,String profileName, StructureStylesheetUserPreferences fsup);
    public void setThemeStylesheetUserPreferences(String userName,String profileName, ThemeStylesheetUserPreferences ssup);


}
