/* Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.deployer.tomcat;

import java.io.File;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jasig.portal.tools.deployer.DeployerConfig;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TomcatDeployerConfig extends DeployerConfig {
    private File catalinaShared;
    private File catalinaWebapps;
    
    
    public File getCatalinaShared() {
        return this.catalinaShared;
    }
    public void setCatalinaShared(File catalinaShared) {
        this.catalinaShared = catalinaShared;
    }
    public File getCatalinaWebapps() {
        return this.catalinaWebapps;
    }
    public void setCatalinaWebapps(File catalinaWebapps) {
        this.catalinaWebapps = catalinaWebapps;
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
        TomcatDeployerConfig rhs = (TomcatDeployerConfig)object;
        return new EqualsBuilder()
            .appendSuper(super.equals(object))
            .append(this.catalinaShared, rhs.catalinaShared)
            .append(this.catalinaWebapps, rhs.catalinaWebapps)
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return new HashCodeBuilder(-110713495, -1544877739)
            .appendSuper(super.hashCode())
            .append(this.catalinaShared)
            .append(this.catalinaWebapps)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("catalinaShared", this.catalinaShared)
            .append("catalinaWebapps", this.catalinaWebapps)
            .appendSuper(super.toString())
            .toString();
    }
}
