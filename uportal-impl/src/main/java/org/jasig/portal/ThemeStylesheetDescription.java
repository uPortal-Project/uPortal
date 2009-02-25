/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
 package org.jasig.portal;


/**
 * Stylesheet description for stylesheets performing theme transformation
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class ThemeStylesheetDescription extends CoreXSLTStylesheetDescription {
    // vector holds the list of first stage stylesheets that can be used with the current stylesheet
    protected int structureStylesheetId;
    protected String mimeType;
    protected String samplePictureURI;
    protected String sampleIconURI;
    protected String deviceType;
    protected String serializerName;
    protected String customUPClassLocator;


    public int getStructureStylesheetId() { return structureStylesheetId; }
    public void setStructureStylesheetId(int ssid) { structureStylesheetId=ssid; }

    public String getMimeType() { return this.mimeType; }
    public void setMimeType(String type) { this.mimeType=type; }

    public String getDeviceType() { return this.deviceType; }
    public void setDeviceType(String type) { this.deviceType=type; }

    public String getSamplePictureURI() { return this.samplePictureURI; }
    public void setSamplePictureURI(String uri) { this.samplePictureURI=uri; }

    public String getSampleIconURI() { return this.sampleIconURI; }
    public void setSampleIconURI(String uri) { this.sampleIconURI=uri; }

    public String getSerializerName() { return this.serializerName; }
    public void setSerializerName(String name) { this.serializerName=name; }

    public String getCustomUserPreferencesManagerClass() { return customUPClassLocator; }
    public void setCustomUserPreferencesManagerClass(String classLocator) { customUPClassLocator=classLocator; }

}
