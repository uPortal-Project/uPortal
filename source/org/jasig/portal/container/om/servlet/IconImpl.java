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
public class IconImpl implements Serializable {

    private String smallIcon;
    private String largeIcon;
    
    public String getLargeIcon() {
        return largeIcon;
    }

    public String getSmallIcon() {
        return smallIcon;
    }
    
    public void setSmallIcon(String smallIcon) {
        this.smallIcon = smallIcon;
    }
    
    public void setLargeIcon(String largeIcon) {
        this.largeIcon = largeIcon;
    }

}
