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
