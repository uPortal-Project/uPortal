/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
