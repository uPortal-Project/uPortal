package org.jasig.portal;

import java.util.Hashtable;

/**
 * Interface by which portal talks to the stylesheet description database
 * @author Peter Kharchenko
 * @version $Revision$
 */

public interface ICoreStylesheetDescriptionDB {

    // functions that allow one to browse available core stylesheets in various ways
    public Hashtable getStructureStylesheetList(String media);
    public Hashtable getThemeStylesheetList(String structureStylesheetName);
    public Hashtable getCSSStylesheetList(String themeStylesheetName);

    // functions that allow access to the entire CoreStylesheetDescription object.
    // These functions are used when working with the stylesheet, and not for browsing purposes.
    public StructureStylesheetDescription getStructureStylesheetDescription(String stylesheetName);
    public ThemeStylesheetDescription getThemeStylesheetDescription(String stylesheetName);
    public CoreCSSStylesheetDescription getCSSStylesheetDescription(String stylesheetName);

    // functions that allow to manage core stylesheet description collection
    public void removeStructureStylesheetDescription(String stylesheetName);
    public void removeThemeStylesheetDescription(String stylesheetName);
    public void removeCSSStylesheetDescription(String stylesheetName);
    public void addStructureStylesheetDescription(String stylesheetDescriptionURI,String stylesheetURI);
    public void addThemeStylesheetDescription(String stylesheetDescriptionURI,String stylesheetURI);
    public void addCSSStylesheetDescription(String stylesheetDescriptionURI,String stylesheetURI);

};
