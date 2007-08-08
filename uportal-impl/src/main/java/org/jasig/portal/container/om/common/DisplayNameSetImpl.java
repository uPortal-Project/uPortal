/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.pluto.om.common.DisplayName;
import org.apache.pluto.om.common.DisplayNameSet;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class DisplayNameSetImpl implements DisplayNameSet, Serializable {
    
    Map displayNames = null; // Locale --> DisplayName

    public DisplayNameSetImpl() {
        displayNames = new HashMap();
    }

    // DisplayNameSet methods
    
    public Iterator iterator() {
        return displayNames.values().iterator();
    }

    public DisplayName get(Locale locale) {
        return (DisplayName)displayNames.get(locale);
    }
    
    // Additional methods
    
    public void add(String displayName, Locale locale) {
        displayNames.put(locale, new DisplayNameImpl(displayName, locale));
    }
    
    public void add(DisplayName displayName) {
        displayNames.put(displayName.getLocale(), displayName);
    }

}
