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
import java.util.Map;

import org.apache.pluto.om.common.Parameter;
import org.apache.pluto.om.common.ParameterSet;
import org.apache.pluto.om.common.ParameterSetCtrl;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ParameterSetImpl implements ParameterSet, ParameterSetCtrl, Serializable {

    private Map parameters = null; // Parameter name --> Parameter
    
    public ParameterSetImpl() {
        parameters = new HashMap();
    }
    
    // ParameterSet methods

    public Iterator iterator() {
        return parameters.values().iterator();
    }

    public Parameter get(String name) {
        return (Parameter)parameters.get(name);
    }

    // ParameterSetCtrl methods
    
    public Parameter add(String name, String value) {
        ParameterImpl parameter = new ParameterImpl(name, value);
        parameters.put(parameter.getName(), parameter);
        return parameter;
    }

    public Parameter remove(String name) {
        return (Parameter)parameters.remove(name);
    }

    public void remove(Parameter parameter) {
        parameters.remove(parameter.getName());
    }
    
    // Additional methods
    
    public void add(Parameter parameter) {
        parameters.put(parameter.getName(), parameter);
    }

}
