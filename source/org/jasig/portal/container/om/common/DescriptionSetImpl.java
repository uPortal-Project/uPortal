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

import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.DescriptionSet;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class DescriptionSetImpl implements DescriptionSet, Serializable {

    Map descriptions = null; // Locale --> Description

    public DescriptionSetImpl() {
        this.descriptions = new HashMap();
    }

    // DescriptionSet methods
    
    public Iterator iterator() {
        return descriptions.values().iterator();
    }

    public Description get(Locale locale) {
        return (Description)descriptions.get(locale);
    }
    
    // Additional methods
    
    public void add(String description, Locale locale) {
        descriptions.put(locale, new DescriptionImpl(description, locale));
    }
    
    public void add(Description description) {
        descriptions.put(description.getLocale(), description);
    }

}
