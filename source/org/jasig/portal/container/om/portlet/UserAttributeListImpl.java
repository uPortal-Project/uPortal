/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.portlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class UserAttributeListImpl implements Serializable {

    private Map userAttributes = null;

    public UserAttributeListImpl() {
        userAttributes = new HashMap();
    }

    public Iterator iterator() {
        return userAttributes.values().iterator();
    }

    public UserAttributeImpl get(String name) {
        return (UserAttributeImpl)userAttributes.get(name);
    }
        
    public void add(UserAttributeImpl userAttributeImpl) {
        userAttributes.put(userAttributeImpl.getName(), userAttributeImpl);
    }

}
