package org.jasig.portal;

import java.util.Vector;

/**
 * Stylesheet description for stylesheets performing style transformation
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class CoreCSSStylesheetDescription extends CoreStylesheetDescription {
    // a vector containing names of second stage XSLT stylesheets that can be used with this CSS
    protected Vector themeStylesheets;

    public Vector getThemeStylesheetList() { return themeStylesheets; }
    public void setThemeStylesheetList(Vector list) {
	themeStylesheets=list;
    }
}
