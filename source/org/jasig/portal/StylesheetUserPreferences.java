package org.jasig.portal;

import java.util.*;


/**
 * Description of user preferences that are common to all of the core stylesheets
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class StylesheetUserPreferences {
    String name;
    protected Hashtable parameters;
    
    public StylesheetUserPreferences() {
	parameters=new Hashtable();
    }
    
    public StylesheetUserPreferences(StylesheetUserPreferences sup) {
	this.parameters=new Hashtable(sup.parameters);
    }

    public String getStylesheetName() { return name; }
    public void setStylesheetName(String n) { name=n; }

    public String getParameterValue(String parameterName) {
	return (String) parameters.get(parameterName);
    }

    public void putParameterValue(String parameterName,String parameterValue) {
	this.parameters.put(parameterName,parameterValue);
    }


    public void deleteParameter(String parameterName) {
	this.parameters.remove(parameterName);
    }

    public Hashtable getParameterValues() {
	return parameters;
    }

    public void setParameterValues(Hashtable parameterTable) {
	this.parameters=parameterTable;
    }


    public void synchronizeWithDescription(CoreStylesheetDescription sd) {
	// make sure only the existing parameters are included
	// check if all of the parameters in the preferences occur in the description
	for (Enumeration e = parameters.keys(); e.hasMoreElements();) {
	    String pname=(String) e.nextElement();
	    if(!sd.containsParameterName(pname)) 
		parameters.remove(pname);
	}
    }

    public void completeWithDescriptionInformation(CoreStylesheetDescription sd) {
	// check if all of the parameters in the description occur in the preferences
	// This fills out "null" values with the defaults.
	for (Enumeration e = sd.getStylesheetParameterNames() ; e.hasMoreElements() ;) {
	    String pname=(String) e.nextElement();
	    if(parameters.get(pname)==null) {
		parameters.put(pname,sd.getStylesheetParameterDefaultValue(pname));
	    }
	}
    }

}
