package org.jasig.portal;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Description of properties common for all core stylesheets
 * (i.e. structure, theme and style transformation stylesheets)
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class CoreStylesheetDescription {
    protected String stylesheetName;
    protected String stylesheetWordDescription;
    protected String stylesheetURI;
    protected String stylesheetDescriptionURI;
    
    // hashtable of DescriptionDefaultValuePair objects
    Hashtable parameterTable;

    public CoreStylesheetDescription() {
	parameterTable=new Hashtable();
    }

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

    protected class DescriptionDefaultValuePair {
	public String defaultValue;
	public String wordDescription;
    }
}



