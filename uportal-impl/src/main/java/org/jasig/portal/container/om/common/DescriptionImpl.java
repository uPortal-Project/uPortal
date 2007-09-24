/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.common;

import java.io.Serializable;
import java.util.Locale;

import org.apache.pluto.om.common.Description;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class DescriptionImpl implements Description, Serializable {

    private String description = null;
    private Locale locale = null;

    public DescriptionImpl() {
        
    }
    
    public DescriptionImpl(String description, Locale locale) {
        this();
        this.description = description;
        this.locale = locale;
    }

    // Description methods
    
    public String getDescription() {
        return description;
    }

    public Locale getLocale() {
        return locale;
    }
    
    // Additional methods
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
}
