/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.merger;

import java.util.Map;

/**
 * Interface for merging attributes from sibling PersonAttributeDaos. 
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public interface AttributeMerger {

    /**
     * Modify the "toModify" argument in consideration of the "toConsider" 
     * argument.  Return the resulting Map, which may or may not be the same
     * reference as the "toModify" argument.
     * The modification performed is implementation-specific -- implementations
     * of this interface exist to perform some particular transformation on
     * the toModify argument given the toConsider argument.
     * @param toModify - modify this map
     * @param toConsider - in consideration of this map
     * @return the modified Map
     * @throws IllegalArgumentException if either toModify or toConsider is null
     */
    public Map mergeAttributes(Map toModify, Map toConsider);
    
}