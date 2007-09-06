/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license distributed with this
 * file and available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.tools.deployer;

import java.io.File;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Base config object for the {@link AbstractEarDeployer}. Specifies where the EAR resides,
 * {@link AbstractEarDeployer} impls will likely extend this class to provide container specific
 * deployment configuration.
 * <br>
 * <br>
 * 
 * Properties:
 * <ul>
 *  <li>earLocation - The location of the EAR file to deploy.</li>
 *  <li>extractWars - If the WARs should be extracted when deployed.</li>
 *  <li>removeExistingDirectories - If extracted WAR directories exist should they be removed before deploying.</li>
 * </ul>
 *  
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DeployerConfig {
    private File earLocation;
    private boolean extractWars = false;
    private boolean removeExistingDirectories = false;

    public File getEarLocation() {
        return this.earLocation;
    }
    public void setEarLocation(File earLocation) {
        this.earLocation = earLocation;
    }
    public boolean isExtractWars() {
        return this.extractWars;
    }
    public void setExtractWars(boolean extractWars) {
        this.extractWars = extractWars;
    }
    public boolean isRemoveExistingDirectories() {
        return this.removeExistingDirectories;
    }
    public void setRemoveExistingDirectories(boolean removeExistingDirectories) {
        this.removeExistingDirectories = removeExistingDirectories;
    }
    
    
    /**
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof DeployerConfig)) {
            return false;
        }
        DeployerConfig rhs = (DeployerConfig)object;
        return new EqualsBuilder()
            .append(this.earLocation, rhs.earLocation)
            .append(this.extractWars, rhs.extractWars)
            .append(this.removeExistingDirectories, rhs.removeExistingDirectories)
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return new HashCodeBuilder(-110713495, -1544877739)
            .append(this.earLocation)
            .append(this.extractWars)
            .append(this.removeExistingDirectories)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("earLocation", this.earLocation)
            .append("extractWars", this.extractWars)
            .append("removeExistingDirectories", this.removeExistingDirectories)
            .toString();
    }
}
