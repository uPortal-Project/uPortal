package org.jasig.portal;

import java.util.Vector;

/**
 * Stylesheet description for stylesheets performing theme transformation
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class ThemeStylesheetDescription extends CoreXSLTStylesheetDescription {
    // vector holds the list of first stage stylesheets that can be used with the current stylesheet
    protected Vector structureStylesheets;
    protected String mimeType;
    protected String serializerName;
    protected String customUPClassLocator;
    

    public Vector getStructureStylesheetList() { return structureStylesheets; }
    public void setStructureStylesheetList(Vector list) {
	structureStylesheets=list;
    }

    public String getMimeType() { return this.mimeType; }
    public void setMimeType(String type) { this.mimeType=type; }

    public String getSerializerName() { return this.serializerName; }
    public void setSerializerName(String name) { this.serializerName=name; }

    public String getCustomUserPreferencesManager() { return customUPClassLocator; }
    public void setCustomUserPreferencesManager(String classLocator) { customUPClassLocator=classLocator; }

}
