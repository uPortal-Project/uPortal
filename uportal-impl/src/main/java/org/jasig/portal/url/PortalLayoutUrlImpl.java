/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url;


import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Implementation of a portal URL
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class PortalLayoutUrlImpl extends AbstractPortalUrl implements IPortalLayoutUrl {
    private final String targetFolderId;
    private boolean renderInNormal;

    
    public PortalLayoutUrlImpl(HttpServletRequest request, IUrlGenerator urlGenerator, String targetFolderId) {
        super(request, urlGenerator);
        Validate.notNull(targetFolderId, "targetFolderId can not be null");
        
        this.targetFolderId = targetFolderId;
        //TODO set renderInNormal based on current request
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalLayoutUrl#isRenderInNormal()
     */
    public boolean isRenderInNormal() {
        return this.renderInNormal;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalLayoutUrl#setRenderInNormal(boolean)
     */
    public void setRenderInNormal(boolean renderInNormal) {
        this.renderInNormal = renderInNormal;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.urlGenerator.generatePortalUrl(this.request, this, this.targetFolderId);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-942605321, 2130461357)
            .appendSuper(super.hashCode())
            .append(this.renderInNormal)
            .append(this.targetFolderId)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof PortalLayoutUrlImpl)) {
            return false;
        }
        PortalLayoutUrlImpl rhs = (PortalLayoutUrlImpl) object;
        return new EqualsBuilder()
            .appendSuper(super.equals(object))
            .append(this.targetFolderId, rhs.targetFolderId)
            .append(this.renderInNormal, rhs.renderInNormal)
            .isEquals();
    }
}
