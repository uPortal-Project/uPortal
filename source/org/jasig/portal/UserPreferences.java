package org.jasig.portal;


/**
 * Object managing user preferences.
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class UserPreferences {

    protected String media;
    
    protected StructureStylesheetUserPreferences fsup;
    protected ThemeStylesheetUserPreferences ssup;
    protected CoreCSSStylesheetUserPreferences cssup;    

    /*
     * Copy-constructor
     */
    public UserPreferences(UserPreferences up) {
	fsup=new StructureStylesheetUserPreferences(up.getStructureStylesheetUserPreferences());
 	ssup=new ThemeStylesheetUserPreferences(up.getThemeStylesheetUserPreferences());
 	cssup=new CoreCSSStylesheetUserPreferences(up.getCoreCSSStylesheetUserPreferences());
    }

    
    public void setMedia(String m) { media=m; }
    public String getMedia() { return media; }

    public UserPreferences(String mediaName) { this.media=mediaName; }

    public void setStructureStylesheetUserPreferences(StructureStylesheetUserPreferences up) {
	this.fsup=up;
    }

    public void setThemeStylesheetUserPreferences(ThemeStylesheetUserPreferences up) {
	this.ssup=up;
    }

    public void setCoreCSSStylesheetUserPreferences(CoreCSSStylesheetUserPreferences up) {
	this.cssup=up;
    }

    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences() {
	return this.fsup;
    }

    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences() {
	return this.ssup;
    }

    public CoreCSSStylesheetUserPreferences getCoreCSSStylesheetUserPreferences() {
	return this.cssup;
    }

}
