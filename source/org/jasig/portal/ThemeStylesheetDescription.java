/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
