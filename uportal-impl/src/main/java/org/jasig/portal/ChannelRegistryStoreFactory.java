/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

/**
 * Produces an implementation of IChannelRegistryStore
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated Use the bean named 'channelRegistryStore' from the Spring application context 
 */
public class ChannelRegistryStoreFactory {

    /**
     * Returns an instance of the IChannelRegistryStore specified in portal.properties
     * @return an IChannelRegistryStore implementation
     */
    public static IChannelRegistryStore getChannelRegistryStoreImpl() {
        final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
        return (IChannelRegistryStore) applicationContext.getBean("channelRegistryStore", IChannelRegistryStore.class);
    }
}
