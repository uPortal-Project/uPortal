/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.servlet;

import java.io.Serializable;

/**
 * Data structure to support WebApplicationDefinition for
 * marshalling and unmarshalling of web.xml.
 * Not needed by the Pluto container.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class FormLoginConfigImpl implements Serializable {

    private String formLoginPage;
    private String formErrorPage;
    
    public String getFormErrorPage() {
        return formErrorPage;
    }

    public String getFormLoginPage() {
        return formLoginPage;
    }

    public void setFormErrorPage(String formErrorPage) {
        this.formErrorPage = formErrorPage;
    }

    public void setFormLoginPage(String formLoginPage) {
        this.formLoginPage = formLoginPage;
    }

}
