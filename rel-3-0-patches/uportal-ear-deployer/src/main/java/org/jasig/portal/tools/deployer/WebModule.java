/* Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.deployer;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents a //application/module/web Node in the EAR descriptor.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class WebModule {
    private String webUri;
    private String contextRoot;
    
    
    public String getContextRoot() {
        return this.contextRoot;
    }
    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }
    public String getWebUri() {
        return this.webUri;
    }
    public void setWebUri(String webUri) {
        this.webUri = webUri;
    }
    
    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof WebModule)) {
            return false;
        }
        WebModule rhs = (WebModule)object;
        return new EqualsBuilder()
            .append(this.contextRoot, rhs.contextRoot)
            .append(this.webUri, rhs.webUri)
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-110713495, -1544877739)
            .append(this.contextRoot)
            .append(this.webUri)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("contextRoot", this.contextRoot)
            .append("webUri", this.webUri)
            .toString();
    }
}
