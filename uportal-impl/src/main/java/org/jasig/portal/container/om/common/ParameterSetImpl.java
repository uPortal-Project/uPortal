/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
    
    public int size() {
        return parameters.size();
    }

}
