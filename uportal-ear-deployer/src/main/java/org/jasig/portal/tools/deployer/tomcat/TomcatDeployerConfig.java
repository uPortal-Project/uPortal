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
 * Deployer configuration with tomcat specific properties. tomcatHome is
 * where 'common' JARs go, tomcatBase is where WARs and 'shared' JARs go.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TomcatDeployerConfig extends DeployerConfig {
    private File tomcatHome;
    private File tomcatBase;
    
    
    public File getTomcatHome() {
        return this.tomcatHome;
    }
    public void setTomcatHome(File tomcatHome) {
        this.tomcatHome = tomcatHome;
    }
    public File getTomcatBase() {
        return this.tomcatBase;
    }
    public void setTomcatBase(File tomcatBase) {
        this.tomcatBase = tomcatBase;
    }
    


    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
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
            .append(this.tomcatHome, rhs.tomcatHome)
            .append(this.tomcatBase, rhs.tomcatBase)
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-110713495, -1544877739)
            .appendSuper(super.hashCode())
            .append(this.tomcatHome)
            .append(this.tomcatBase)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("tomcatHome", this.tomcatHome)
            .append("tomcatBase", this.tomcatBase)
            .appendSuper(super.toString())
            .toString();
    }
}
