/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.PreferencesValidator;

import org.apache.pluto.om.common.Preference;
import org.apache.pluto.om.common.PreferenceSet;
import org.apache.pluto.om.common.PreferenceSetCtrl;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PreferenceSetImpl implements PreferenceSet, PreferenceSetCtrl, Serializable {

    Map preferences = null; // Preference name --> Preference
    PreferencesValidator validator = null;
    String validatorClassName = null;
    ClassLoader classLoader = null;
    
    public PreferenceSetImpl() {
        preferences = new HashMap();
    }

    // PreferenceSet methods
    
    public Iterator iterator() {
        return preferences.values().iterator();
    }

    public Preference get(String name) {
        return (Preference)preferences.get(name);
    }

    public PreferencesValidator getPreferencesValidator() {
        if (validator == null) {
            if (this.classLoader == null) {
                throw new IllegalStateException("Portlet class loader is not yet available to load preferences validator.");
            }
            try {
                if (validatorClassName != null) {
                    Object o = classLoader.loadClass(validatorClassName).newInstance();
                    if (o instanceof PreferencesValidator) {
                        validator = (PreferencesValidator)o;
                    }
                }
                    
            } catch (Exception e) {
                // Do nothing
                e.printStackTrace();
            }
        }
        return validator;
    }
    
    // PreferenceSetCtrl methods
    
    public Preference add(String name, List values) {
        return add(name, values, false);
    }

    public Preference remove(String name) {
        return (Preference)preferences.remove(name);
    }

    public void remove(Preference preference) {
        preferences.remove(preference.getName());
    }

    // Additional methods
    
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    public void setPreferencesValidator(PreferencesValidator validator) {
        this.validator = validator;
    }
    
    public void setPreferencesValidator(String validatorClassName) {
        this.validatorClassName = validatorClassName;
    }
    
    public void clear() {
        preferences.clear();
    }
    
    public Preference add(String name, List values, boolean readOnly) {
        PreferenceImpl preference = new PreferenceImpl();
        preference.setName(name);
        preference.setValues(values);
        preference.setReadOnly(readOnly);
        preferences.put(name, preference);
        return preference;
    }

    public void addAll(PreferenceSet preferences) {
        Iterator iter = preferences.iterator();
        while (iter.hasNext()) {
            Preference preference = (Preference)iter.next();
            this.preferences.put(preference.getName(), preference);
        }
    }
    
    public int size() {
        return preferences.size();
    }
}
