/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlets.registerportal;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalRegistrationRequest implements Serializable {
    private String institutionName;
    private String deployerName;
    private String deployerAddress;
    private String portalName;
    private String portalUrl;
    private boolean shareInfo = false;
    private Map<String, String> dataToSubmit;
    
    /**
     * @return the institutionName
     */
    public String getInstitutionName() {
        return institutionName;
    }
    /**
     * @param institutionName the institutionName to set
     */
    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }
    /**
     * @return the deployerName
     */
    public String getDeployerName() {
        return deployerName;
    }
    /**
     * @param deployerName the deployerName to set
     */
    public void setDeployerName(String deployerName) {
        this.deployerName = deployerName;
    }
    /**
     * @return the deployerAddress
     */
    public String getDeployerAddress() {
        return deployerAddress;
    }
    /**
     * @param deployerAddress the deployerAddress to set
     */
    public void setDeployerAddress(String deployerAddress) {
        this.deployerAddress = deployerAddress;
    }
    /**
     * @return the portalName
     */
    public String getPortalName() {
        return portalName;
    }
    /**
     * @param portalName the portalName to set
     */
    public void setPortalName(String portalName) {
        this.portalName = portalName;
    }
    /**
     * @return the portalUrl
     */
    public String getPortalUrl() {
        return portalUrl;
    }
    /**
     * @param portalUrl the portalUrl to set
     */
    public void setPortalUrl(String portalUrl) {
        this.portalUrl = portalUrl;
    }
    /**
     * @return the shareInfo
     */
    public boolean isShareInfo() {
        return shareInfo;
    }
    /**
     * @param shareInfo the shareInfo to set
     */
    public void setShareInfo(boolean shareInfo) {
        this.shareInfo = shareInfo;
    }
    /**
     * @return the dataToSubmit
     */
    public Map<String, String> getDataToSubmit() {
        return dataToSubmit;
    }
    /**
     * @param dataToSubmit the dataToSubmit to set
     */
    public void setDataToSubmit(Map<String, String> dataToSubmit) {
        this.dataToSubmit = dataToSubmit;
    }
    
    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof PortalRegistrationRequest)) {
            return false;
        }
        PortalRegistrationRequest rhs = (PortalRegistrationRequest) object;
        return new EqualsBuilder()
            .append(this.dataToSubmit, rhs.dataToSubmit)
            .append(this.deployerAddress, rhs.deployerAddress)
            .append(this.portalName, rhs.portalName)
            .append(this.deployerName, rhs.deployerName)
            .append(this.institutionName, rhs.institutionName)
            .append(this.portalUrl, rhs.portalUrl)
            .append(this.shareInfo, rhs.shareInfo)
            .isEquals();
    }
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(554822571, 313513477)
        .append(this.dataToSubmit)
        .append(this.deployerAddress)
        .append(this.portalName)
        .append(this.deployerName)
        .append(this.institutionName)
        .append(this.portalUrl)
        .append(this.shareInfo)
        .toHashCode();
    }
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("dataToSubmit", this.dataToSubmit)
        .append("deployerAddress", this.deployerAddress)
        .append("portalName", this.portalName)
        .append("deployerName", this.deployerName)
        .append("institutionName", this.institutionName)
        .append("portalUrl", this.portalUrl)
        .append("shareInfo", this.shareInfo)
        .toString();
    }
    
    
}
