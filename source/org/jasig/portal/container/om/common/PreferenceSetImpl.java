/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
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
}
