/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
