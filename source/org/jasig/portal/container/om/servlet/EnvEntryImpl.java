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
public class EnvEntryImpl implements Serializable {

    private DescriptionSet descriptions;
    private String envEntryName;
    private String envEntryValue;
    private String envEntryType;

    public DescriptionSet getDescriptions() {
        return descriptions;
    }
    
    public String getEnvEntryName() {
        return envEntryName;
    }

    public String getEnvEntryType() {
        return envEntryType;
    }

    public String getEnvEntryValue() {
        return envEntryValue;
    }

    public void setDescriptions(DescriptionSet descriptions) {
         this.descriptions = descriptions;
    }

    public void setEnvEntryName(String envEntryName) {
        this.envEntryName = envEntryName;
    }

    public void setEnvEntryType(String envEntryType) {
        this.envEntryType = envEntryType;
    }

    public void setEnvEntryValue(String envEntryValue) {
        this.envEntryValue = envEntryValue;
    }
    
}
