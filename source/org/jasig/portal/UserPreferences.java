package org.jasig.portal;


/**
 * Object managing user preferences.
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class UserPreferences {

    protected UserProfile profile;
    
    protected StructureStylesheetUserPreferences fsup;
    protected ThemeStylesheetUserPreferences ssup;

    /*
     * Copy-constructor
     */
    public UserPreferences(UserPreferences up) {
	fsup=new StructureStylesheetUserPreferences(up.getStructureStylesheetUserPreferences());
 	ssup=new ThemeStylesheetUserPreferences(up.getThemeStylesheetUserPreferences());
	profile=up.getProfile();
    }

    
    public void setProfile(UserProfile p) { profile=p; }
    public UserProfile getProfile() { return profile; }

    public UserPreferences(UserProfile p) { this.profile=p; }

    public void setStructureStylesheetUserPreferences(StructureStylesheetUserPreferences up) {
	this.fsup=up;
    }

    public void setThemeStylesheetUserPreferences(ThemeStylesheetUserPreferences up) {
	this.ssup=up;
    }

    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences() {
	return this.fsup;
    }

    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences() {
	return this.ssup;
    }

}
