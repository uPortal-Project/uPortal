/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.servlet;

import java.io.Serializable;

import org.apache.pluto.om.common.DisplayNameSet;

/**
 * Data structure to support WebApplicationDefinition for
 * marshalling and unmarshalling of web.xml.
 * Not needed by the Pluto container.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class SecurityConstraintImpl implements Serializable {

    private DisplayNameSet displayNames;
    private WebResourceCollectionImpl[] webResourceCollections;
    private AuthConstraintImpl authConstraint;
    private UserDataConstraintImpl userDataConstraint;

    public AuthConstraintImpl getAuthConstraint() {
        return authConstraint;
    }

    public DisplayNameSet getDisplayNames() {
        return displayNames;
    }

    public UserDataConstraintImpl getUserDataConstraint() {
        return userDataConstraint;
    }

    public WebResourceCollectionImpl[] getWebResourceCollections() {
        return webResourceCollections;
    }

    public void setAuthConstraint(AuthConstraintImpl authConstraint) {
        this.authConstraint = authConstraint;
    }

    public void setDisplayNames(DisplayNameSet displayNames) {
        this.displayNames = displayNames;
    }

    public void setUserDataConstraint(UserDataConstraintImpl userDataConstraint) {
        this.userDataConstraint = userDataConstraint;
    }

    public void setWebResourceCollections(WebResourceCollectionImpl[] webResourceCollections) {
        this.webResourceCollections = webResourceCollections;
    }

}
