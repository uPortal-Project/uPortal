/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.api.portlet;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Set state and mode for the delegate portlet
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DelegateState {
    private final PortletMode portletMode;
    private final WindowState windowState;
    
    /**
     * @param portletMode Mode for the delegate portlet, if null the current or default mode is used
     * @param windowState State for the delegate portlet, if null the current or default state is used
     */
    public DelegateState(PortletMode portletMode, WindowState windowState) {
        this.portletMode = portletMode;
        this.windowState = windowState;
    }
    
    
    public PortletMode getPortletMode() {
        return portletMode;
    }

    public WindowState getWindowState() {
        return windowState;
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
        DelegateState rhs = (DelegateState) object;
        return new EqualsBuilder()
            .append(this.windowState, rhs.getWindowState())
            .append(this.portletMode, rhs.getPortletMode())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(1445247369, -1009176817)
            .append(this.windowState)
            .append(this.portletMode)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("windowState", this.windowState)
            .append("portletMode", this.portletMode)
            .toString();
    }
}
