/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.security.IPerson;

/**
 * Provides access to IPortletEntity objects and convenience methods for creating
 * and converting them and related objects.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletEntityRegistryImpl implements IPortletEntityRegistry {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
//    private IPortletDefinitionRegistry portletDefinitionRegistry;
    

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#createPortletEntity(java.lang.String, java.lang.String, org.jasig.portal.security.IPerson)
     */
    public IPortletEntity createPortletEntity(String channelPublishId, String channelSubscribeId, IPerson person) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getOrCreatePortletEntity(java.lang.String, java.lang.String, org.jasig.portal.security.IPerson)
     */
    public IPortletEntity getOrCreatePortletEntity(String channelPublishId, String channelSubscribeId, IPerson person) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getParentPortletDefinition(org.jasig.portal.portlet.om.IPortletEntityId)
     */
    public IPortletDefinition getParentPortletDefinition(IPortletEntityId portletEntityId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getPortletEntity(org.jasig.portal.portlet.om.IPortletEntityId)
     */
    public IPortletEntity getPortletEntity(IPortletEntityId portletEntityId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getPortletEntity(java.lang.String, org.jasig.portal.security.IPerson)
     */
    public IPortletEntity getPortletEntity(String channelSubscribeId, IPerson person) {
        // TODO Auto-generated method stub
        return null;
    }

}
