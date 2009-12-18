/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.registry;


/**
 * Caches transient portlet window instances as request attributes, passes all operations on non-transient windows to the
 * parent {@link PortletWindowRegistryImpl}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TransientPortletWindowRegistryImpl extends PortletWindowRegistryImpl implements ITransientPortletWindowRegistry {
    public static final String TRANSIENT_WINDOW_ID_PREFIX = PortletWindowRegistryImpl.TRANSIENT_WINDOW_ID_PREFIX;
    public static final String TRANSIENT_PORTLET_WINDOW_MAP_ATTRIBUTE = PortletWindowRegistryImpl.TRANSIENT_PORTLET_WINDOW_MAP_ATTRIBUTE;
    
}