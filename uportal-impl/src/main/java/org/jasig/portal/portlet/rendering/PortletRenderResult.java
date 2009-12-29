/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.rendering;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jasig.portal.api.portlet.DelegateState;

/**
 * The result of rendering a portlet
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletRenderResult {
    private final String title;

    public PortletRenderResult(String title) {
        this.title = title;
    }

    /**
     * @return The title set by the portlet, null if none was set
     */
    public String getTitle() {
        return title;
    }

    
    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof DelegateState)) {
            return false;
        }
        PortletRenderResult rhs = (PortletRenderResult) object;
        return new EqualsBuilder()
            .append(this.title, rhs.getTitle())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(1445247369, -1009176817)
            .append(this.title)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("title", this.title)
            .toString();
    }
}
