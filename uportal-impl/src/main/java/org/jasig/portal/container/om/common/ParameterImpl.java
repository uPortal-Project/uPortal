/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.common;

import java.io.Serializable;
import java.util.Locale;

import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.DescriptionSet;
import org.apache.pluto.om.common.Parameter;
import org.apache.pluto.om.common.ParameterCtrl;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ParameterImpl implements Parameter, ParameterCtrl, Serializable {

    private String name = null;
    private String value = null;
    private DescriptionSet descriptions = null;
    
    public ParameterImpl() {
        descriptions = new DescriptionSetImpl();
    }
    
    public ParameterImpl(String name, String value) {
        this();
        this.name = name;
        this.value = value;
    }
    
    public ParameterImpl(String name, String value, DescriptionSet descriptions) {
        this(name, value);
        this.descriptions = descriptions;
    }

    // Parameter methods
    
    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Description getDescription(Locale locale) {
        return descriptions.get(locale);
    }

    // ParameterCrtl methods
    
    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setDescriptionSet(DescriptionSet descriptions) {
        this.descriptions = ( descriptions != null ) ? descriptions : new DescriptionSetImpl();
    }
    
}
