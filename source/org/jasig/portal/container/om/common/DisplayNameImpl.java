/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.common;

import java.io.Serializable;
import java.util.Locale;

import org.apache.pluto.om.common.DisplayName;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class DisplayNameImpl implements DisplayName, Serializable {
    
    private String displayName = null;
    private Locale locale = null;

    public DisplayNameImpl() {
        
    }
    
    public DisplayNameImpl(String displayName, Locale locale) {
        this.displayName = displayName;
        this.locale = locale;
    }

    // DisplayName methods
    
    public String getDisplayName() {
        return displayName;
    }

    public Locale getLocale() {
        return locale;
    }
    
    // Additional methods
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

}
