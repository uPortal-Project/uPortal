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
public class ErrorPageImpl implements Serializable {

    private String errorCode;
    private String exceptionType;
    private String location;
    
    public String getErrorCode() {
        return errorCode;
    }

    public String getExceptionType() {
        return exceptionType;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
