/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.om;

/**
 * A portlet entity represents what a user subscribes to (adds to their layout) in
 * the portal object model.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletEntity {
    /**
     * @return The unique identifier for this portlet entity.
     */
    public IPortletEntityId getPortletEntityId();
}
