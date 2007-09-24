/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.merger;

import java.util.Map;

/**
 * Attribute merge strategy whereby considered attributes over-write
 * previously set values for attributes with colliding names.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class ReplacingAttributeAdder 
    implements IAttributeMerger {

    /**
     * Simply puts all the values in toConsider into toModify and returns toModify.
     * This means that for keys in both toConsider and toModify, the
     * value in toConsider will be controlling.
     * @param toModify - the Map we are to modify
     * @param toConsider - the Map we are to consider in modifying toModify
     * @return the result of toModify.putAll(toConsider)
     */
    public Map mergeAttributes(Map toModify, Map toConsider) {
        if (toModify == null)
            throw new IllegalArgumentException("toModify illegally null");
        if (toConsider == null)
            throw new IllegalArgumentException("toConsider illegally null");
        
        toModify.putAll(toConsider);
        return toModify;
    }
}
