/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

 package org.jasig.portal;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * CoreStyleSheetDescription contains properties common to all core stylesheets.
 * This includes structure, theme and style transformation stylesheets.
 * 
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



