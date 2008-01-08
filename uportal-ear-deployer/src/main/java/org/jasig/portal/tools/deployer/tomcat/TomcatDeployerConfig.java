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
    private File webAppsDir;
    private File jarDir;
    

    /**
     * @return the webAppsDir
     */
    public File getWebAppsDir() {
        return webAppsDir;
    }
    /**
     * @param webAppsDir the webAppsDir to set
     */
    public void setWebAppsDir(File webAppsDir) {
        this.webAppsDir = webAppsDir;
    }
    /**
     * @return the jarDir
     */
    public File getJarDir() {
        return jarDir;
    }
    /**
     * @param jarDir the jarDir to set
     */
    public void setJarDir(File jarDir) {
        this.jarDir = jarDir;
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
            .append(this.webAppsDir, rhs.webAppsDir)
            .append(this.jarDir, rhs.jarDir)
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-110713495, -1544877739)
            .appendSuper(super.hashCode())
            .append(this.webAppsDir)
            .append(this.jarDir)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("webAppsDir", this.webAppsDir)
            .append("jarDir", this.jarDir)
            .appendSuper(super.toString())
            .toString();
    }
}
