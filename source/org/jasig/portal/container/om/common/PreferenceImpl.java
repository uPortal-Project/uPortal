/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.pluto.om.common.Preference;
import org.apache.pluto.om.common.PreferenceCtrl;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PreferenceImpl implements Preference, PreferenceCtrl, Serializable {
    
    private String name = null;
    private boolean readOnly = false;
    private Collection values = null; // list of Strings
    

	public PreferenceImpl() {
		values = new ArrayList();
	}
    
    public PreferenceImpl(String name, Collection values, boolean readOnly) {
        this();
        this.name = name;
        this.values.addAll(values);
        this.readOnly = readOnly;	
    }
    
    // Preference methods
    
    public String getName() {
        return name;
    }

    public Iterator getValues() {
        return values.iterator();
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isValueSet() {
        return values.size() > 0;
    }
    
    // PreferenceCtrl methods
    
    public void setName(String name) {
        this.name = name;
    }

    public void setValues(List values) {
        this.values = ( values != null ) ? values : new ArrayList();
    }

    public void setReadOnly(String readOnly) {
        setReadOnly(Boolean.getBoolean(readOnly));
    }
    
    // Additional methods
    
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    public String getFirstValue() {
        Iterator iter = values.iterator();
        String firstValue = null;
        if (iter.hasNext()) {
            firstValue = (String)iter.next();
        }
        return firstValue;
    }

    
}
