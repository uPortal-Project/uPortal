/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.layout.dlm;

import java.util.List;

/**
 * Defines the service calls for DLM fragment definition DAO implementations.
 * 
 * @author awills
 */
public interface IFragmentDefinitionDao {
    
    /**
     * Obtains the complete set of {@link FragmentDefinition} objects contained 
     * within this data source.
     * 
     * @return The fragment with the corresponding name, or <code>null</code>
     */
    List<FragmentDefinition> getAllFragments();
    
    /**
     * Obtains the {@link FragmentDefinition} object with the specified name.
     * 
     * @param name The unique name of a fragment
     * @return The fragment with the corresponding name, or <code>null</code>
     */
    FragmentDefinition getFragmentDefinition(String name);
    
    /**
     * Updates the specified {@link FragmentDefinition} within the data source.
     * 
     * @param fd A fragment definition that has been modified
     */
    void updateFragmentDefinition(FragmentDefinition fd);
    
    /**
     * Deletes the specified {@link FragmentDefinition} within the data source.
     * 
     * @param fd A fragment definition that exists within this data source
     */
    void removeFragmentDefinition(FragmentDefinition fd);
    
}
