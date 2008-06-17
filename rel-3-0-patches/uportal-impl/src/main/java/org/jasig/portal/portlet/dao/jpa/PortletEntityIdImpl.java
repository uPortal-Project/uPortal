/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.dao.jpa;

import org.jasig.portal.portlet.om.AbstractObjectId;
import org.jasig.portal.portlet.om.IPortletEntityId;


/**
 * Identifies a portlet entity
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class PortletEntityIdImpl extends AbstractObjectId implements IPortletEntityId {
    private static final long serialVersionUID = 1L;

    public PortletEntityIdImpl(long portletEntityId) {
        super(Long.toString(portletEntityId));
    }
}
