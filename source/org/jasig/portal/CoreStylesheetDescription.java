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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Description of properties common for all core stylesheets
 * (i.e. structure, theme and style transformation stylesheets)
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class CoreStylesheetDescription {
    protected int stylesheetId=-1;
    protected String stylesheetName;
    protected String stylesheetWordDescription;
    protected String stylesheetURI;
    protected String stylesheetDescriptionURI;

    // hashtable of DescriptionDefaultValuePair objects
    Hashtable parameterTable;

    public CoreStylesheetDescription() {
        parameterTable=new Hashtable();
    }


    public int getId() { return this.stylesheetId; }
    public void setId(int id) { this.stylesheetId=id; }

    public String getStylesheetName() { return stylesheetName; }
    public void setStylesheetName(String name) { stylesheetName=name; }

    public String getStylesheetURI() { return this.stylesheetURI; }
    public void setStylesheetURI(String uri) { this.stylesheetURI=uri; }

    public String getStylesheetDescriptionURI() { return this.stylesheetDescriptionURI; }
    public void setStylesheetDescriptionURI(String uri) { this.stylesheetDescriptionURI=uri; }

    public String getStylesheetWordDescription() { return stylesheetWordDescription; }
    public void setStylesheetWordDescription(String description) { stylesheetWordDescription=description; }

    public Enumeration getStylesheetParameterNames() {
        return parameterTable.keys();
    }

    public String getStylesheetParameterWordDescription(String parameterName) {
        DescriptionDefaultValuePair pair=(DescriptionDefaultValuePair) parameterTable.get(parameterName);
        if(pair!=null) return pair.wordDescription;
        else return null;
    }
    public String getStylesheetParameterDefaultValue(String parameterName) {
        DescriptionDefaultValuePair pair=(DescriptionDefaultValuePair) parameterTable.get(parameterName);
        if(pair!=null) return pair.defaultValue;
        else return null;
    }

    public boolean containsParameterName(String parameterName) {
        return parameterTable.containsKey(parameterName);
    }

    public void addStylesheetParameter(String name, String defaultValue, String wordDescription) {
        DescriptionDefaultValuePair pair=new DescriptionDefaultValuePair();
        pair.defaultValue=defaultValue; pair.wordDescription=wordDescription;
        parameterTable.put(name,pair);
    }

    public void setStylesheetParameterDefaultValue(String parameterName,String defaultValue) {
	DescriptionDefaultValuePair pair=(DescriptionDefaultValuePair) parameterTable.get(parameterName);
	if(pair!=null) pair.defaultValue=defaultValue;
    }

    public void setStylesheetParameterWordDescription(String parameterName,String wordDescription) {
	DescriptionDefaultValuePair pair=(DescriptionDefaultValuePair) parameterTable.get(parameterName);
	if(pair!=null) pair.wordDescription=wordDescription;
    }

    protected class DescriptionDefaultValuePair {
        public String defaultValue;
        public String wordDescription;
    }
}



