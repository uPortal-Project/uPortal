/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.servlet;

import java.io.Serializable;

import org.apache.pluto.om.common.DescriptionSet;
import org.apache.pluto.om.common.DisplayNameSet;
import org.apache.pluto.om.common.ParameterSet;

/**
 * Data structure to support WebApplicationDefinition for
 * marshalling and unmarshalling of web.xml.
 * Not needed by the Pluto container.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class FilterImpl implements Serializable {

    private IconImpl icon;
    private String filterName;
    private DisplayNameSet displayNames;
    private DescriptionSet descriptions;
    private String filterClass;
    private ParameterSet initParams;

    public IconImpl getIcon() {
        return icon;
    }
    
    public void setIcon(IconImpl icon) {
        this.icon = icon;
    }
    
    public String getFilterName() {
        return filterName;
    }
    
    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }
    
    public DisplayNameSet getDisplayNames() {
        return displayNames;
    }
    
    public void setDisplayNames(DisplayNameSet displayNames) {
        this.displayNames = displayNames;
    }

    public DescriptionSet getDescriptions() {
        return descriptions;
    }
    
    public void setDescriptions(DescriptionSet descriptions) {
        this.descriptions = descriptions;
    }

    public String getFilterClass() {
        return filterClass;
    }
    
    public void setFilterClass(String filterClass) {
        this.filterClass = filterClass;
    }
    
    public ParameterSet getInitParameters() {
        return initParams;
    }
    
    public void setInitParamteters(ParameterSet initParams) {
        this.initParams = initParams;
    }
}
