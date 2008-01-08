/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

 package org.jasig.portal;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 * Description of user preferences that are common to all of the core stylesheets
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class StylesheetUserPreferences implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private Hashtable<String, String> parameters;

    public StylesheetUserPreferences() {
        parameters=new Hashtable<String, String>();
    }

    public StylesheetUserPreferences(StylesheetUserPreferences sup) {
        this.id=sup.id;
        this.parameters=new Hashtable<String, String>(sup.getParameterValues());
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
        return (parameterName!=null)?parameters.get(parameterName):null;
    }

    public void putParameterValue(String parameterName,String parameterValue) {
      if ( parameterName != null && parameterValue != null )  
        parameters.put(parameterName,parameterValue);
    }


    public void deleteParameter(String parameterName) {
      if ( parameterName != null )  
       parameters.remove(parameterName);
    }

    public Hashtable<String, String> getParameterValues() {
        return parameters;
    }

    public void setParameterValues(Hashtable<String, String> parameters) {
      if ( parameters != null )  
        this.parameters=parameters;
      else
        this.parameters.clear();
    }

    public String getCacheKey() {
        StringBuffer sbKey = new StringBuffer();
        for(Enumeration<String> e=parameters.keys();e.hasMoreElements();) {
            String pName=e.nextElement();
            sbKey.append(pName).append("=").append(parameters.get(pName));
        }
        return sbKey.toString();
    }

}
