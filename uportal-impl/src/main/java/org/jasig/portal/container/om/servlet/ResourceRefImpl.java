/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.servlet;

import java.io.Serializable;

import org.apache.pluto.om.common.DescriptionSet;

/**
 * Data structure to support WebApplicationDefinition for
 * marshalling and unmarshalling of web.xml.
 * Not needed by the Pluto container.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ResourceRefImpl implements Serializable {

    private DescriptionSet descriptions;
    private String resRefName;
    private String resType;
    private String resAuth;
    private String resSharingScope;
    
    public DescriptionSet getDescriptions() {
        return descriptions;
    }

    public String getResRefName() {
        return resRefName;
    }

    public String getResType() {
        return resType;
    }
    
    public String getResAuth() {
        return resAuth;
    }

    public String getResSharingScope() {
        return resSharingScope;
    }
    
    public void setDescriptions(DescriptionSet descriptions) {
        this.descriptions = descriptions;
    }

    public void setResRefName(String resRefName) {
        this.resRefName = resRefName;
    }

    public void setResType(String resType) {
        this.resType = resType;
    }
    
    public void setResAuth(String resAuth) {
        this.resAuth = resAuth;
    }
    
    public void setResSharingScope(String resSharingScope) {
        this.resSharingScope = resSharingScope;
    }

}
