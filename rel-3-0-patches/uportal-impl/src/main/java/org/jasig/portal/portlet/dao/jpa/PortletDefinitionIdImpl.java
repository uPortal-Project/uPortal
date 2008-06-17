/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.dao.jpa;

import org.jasig.portal.portlet.om.AbstractObjectId;
import org.jasig.portal.portlet.om.IPortletDefinitionId;

/**
 * Identifies a portlet definition
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class PortletDefinitionIdImpl extends AbstractObjectId implements IPortletDefinitionId {
    private static final long serialVersionUID = 1L;

    /**
     * @param objectId
     */
    public PortletDefinitionIdImpl(long portletDefinitionId) {
        super(Long.toString(portletDefinitionId));
    }
}
