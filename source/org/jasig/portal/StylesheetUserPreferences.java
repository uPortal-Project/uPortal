/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

 package org.jasig.portal;

import java.util.Enumeration;
import java.util.Hashtable;


/**
 * Description of user preferences that are common to all of the core stylesheets
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class StylesheetUserPreferences {
    private int id;
    private Hashtable parameters;

    public StylesheetUserPreferences() {
        parameters=new Hashtable();
    }

    public StylesheetUserPreferences(StylesheetUserPreferences sup) {
        this.id=sup.id;
        this.parameters=new Hashtable(sup.getParameterValues());
    }
    
    /**
     * Provides a copy of this object with all fields instantiated to reflect 
     * the values of this object. This allows subclasses to override to add
     * correct copying behavior for their added fields.
     * 
     * @return a copy of this object
     */
    public Object newInstance()
    {
        return new StylesheetUserPreferences(this);
    }

    public int getStylesheetId() { return id; }
    public void setStylesheetId(int n) { id=n; }

    public String getParameterValue(String parameterName) {
        return (parameterName!=null)?(String)parameters.get(parameterName):null;
    }

    public void putParameterValue(String parameterName,String parameterValue) {
      if ( parameterName != null && parameterValue != null )  
        parameters.put(parameterName,parameterValue);
    }


    public void deleteParameter(String parameterName) {
      if ( parameterName != null )  
       parameters.remove(parameterName);
    }

    public Hashtable getParameterValues() {
        return parameters;
    }

    public void setParameterValues(Hashtable parameters) {
      if ( parameters != null )  
        this.parameters=parameters;
      else
        this.parameters.clear();
    }

    public String getCacheKey() {
        StringBuffer sbKey = new StringBuffer();
        for(Enumeration e=parameters.keys();e.hasMoreElements();) {
            String pName=(String)e.nextElement();
            sbKey.append(pName).append("=").append((String)parameters.get(pName));
        }
        return sbKey.toString();
    }

}
