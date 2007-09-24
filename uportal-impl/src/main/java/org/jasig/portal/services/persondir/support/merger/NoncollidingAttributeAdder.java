/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.merger;

import java.util.Iterator;
import java.util.Map;

/**
 * Merger which implements accumulation of Map entries such that entries once
 * established are individually immutable.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class NoncollidingAttributeAdder implements IAttributeMerger {

    /**
     * For entries in toConsider the keys of which are not keys in toModify, 
     * adds the entry to toModify and returns toModify.
     * @param toModify - base Map the entries of which are considered here
     * to be immutable.
     * @param toConsider - Map to merge into toModify for all noncolliding keys.
     * @return toModify with entries the intersection of our method arguments,
     * with preference given to toModify's existing entries for all key collisions.
     */
    public Map mergeAttributes(Map toModify, Map toConsider) {
        if (toModify == null)
            throw new IllegalArgumentException("toModify argument illegally null.");
        if (toConsider == null)
            throw new IllegalArgumentException("toConsider argument illegally null.");
        
        for (Iterator iter = toConsider.keySet().iterator(); iter.hasNext();){
            String key = (String) iter.next();
            if (! toModify.containsKey(key))
                toModify.put(key, toConsider.get(key));
        }
    
        return toModify;
    }

}