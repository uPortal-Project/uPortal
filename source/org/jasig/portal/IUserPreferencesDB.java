package org.jasig.portal;

/**
 * Interface through which portal talks to the user preferences database
 * @author Peter Kharchenko
 * @version $Revision$
 */

public interface IUserPreferencesDB {
    
    public UserPreferences getUserPreferences(String userName, String media);
    public void putUserPreferences(String userName, UserPreferences up);

    public String getStructureStylesheetName(String userName, String media);
    public String getThemeStylesheetName(String userName, String media);
    public String getCSSStylesheetName(String userName, String media);

    public void setStructureStylesheetName(String stylesheetName,String userName, String media);
    public void setThemeStylesheetName(String stylesheetName,String userName, String media);
    public void setCSSStylesheetName(String stylesheetName,String userName, String media);

    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences(String userName,String stylesheetName);
    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences(String userName,String stylesheetName);
    public CoreCSSStylesheetUserPreferences getCSSStylesheetUserPreferences(String userName,String stylesheetName);

    public void setStructureStylesheetUserPreferences(String userName,StructureStylesheetUserPreferences fsup);
    public void setThemeStylesheetUserPreferences(String userName, ThemeStylesheetUserPreferences ssup);
    public void setCSSStylesheetUserPreferences(String userName, CoreCSSStylesheetUserPreferences cssup);

}
