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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

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

    public void setValues(Collection values) {
        this.values = ( values != null ) ? values : new ArrayList();
    }

    public void setReadOnly(String readOnly) {
        setReadOnly(Boolean.getBoolean(readOnly));
    }
    
    // Additional methods
    
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    
}
