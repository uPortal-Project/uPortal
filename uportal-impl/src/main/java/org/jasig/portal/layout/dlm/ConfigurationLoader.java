/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.layout.dlm;

import java.util.List;


/**
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public interface ConfigurationLoader {
    
    /**
     * @return The available fragment definitions
     */
    public List<FragmentDefinition> getFragments();
    
    /**
     * @return The DLM configuration property
     */
    public String getProperty(String propertyName);

    /**
     * @return The number of properties
     */
    public int getPropertyCount();
}
