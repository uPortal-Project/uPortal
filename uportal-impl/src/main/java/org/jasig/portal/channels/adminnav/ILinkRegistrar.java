/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channels.adminnav;

import java.util.Map;

/**
 * Represents an object that can add links to the AdminNavChannel's underlying
 * model.
 * 
 * @author mboyd@sungardsct.com
 *
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface ILinkRegistrar
{
    /**
     * Dynamically adds a link to the implementation.
     * 
     * @param fname
     * @param labelId
     * @param resolver
     * @param parameters
     */
    public void addLink(String fname, String labelId, ILabelResolver resolver,
            Map parameters);
}
