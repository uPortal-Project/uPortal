/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.merger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Merger that retains values from both maps. If a value exists for
 * a key in both maps the following is done:
 * <ul>
 *  <li>If both maps have a {@link List} they are merged into a single {@link List}</li>
 *  <li>If one map has a {@link List} and the other a single value the value is added to the {@link List}</li>
 *  <li>If both maps have a single value a {@link List} is created from the two.</li>
 * </ul>
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision $
 */
public class MultivaluedAttributeMerger implements IAttributeMerger {

    /**
     * Please note that the <code>dest</code> map is modified.
     * 
     * @see org.jasig.portal.services.persondir.support.merger.IAttributeMerger#mergeAttributes(java.util.Map, java.util.Map)
     */
    public Map mergeAttributes(final Map dest, final Map source) {
        if (dest == null)
            throw new IllegalArgumentException("toModify cannot be null");
        if (source == null)
            throw new IllegalArgumentException("toConsider cannot be null");
        
        final Map sourceCopy = new HashMap(source);
        
        //Iterate through the dest Map
        for (final Iterator destKeyItr = dest.keySet().iterator(); destKeyItr.hasNext();) {
            final Object destKey = destKeyItr.next();
            final Object sourceValue = sourceCopy.remove(destKey);
            
            //If there is a corresponding entry in source
            if (sourceValue != null) {
                //Get the dest entry
                final Object destValue = dest.get(destKey);
                
                //If they are both Lists merge them
                if (destValue instanceof List && sourceValue instanceof List) {
                    final List destList = (List)destValue;
                    final List sourceList = (List)sourceValue;
                    
                    destList.addAll(sourceList);
                    
                    dest.put(destKey, destList);
                }
                //If dest value is a List add the source value to it
                else if (destValue instanceof List) {
                    final List destList = (List)destValue;
                    
                    destList.add(sourceValue);
                    
                    dest.put(destKey, destList);
                }
                //If the source value is a List add the dest value to it
                else if (sourceValue instanceof List) {
                    final List sourceList = (List)sourceValue;
                    
                    sourceList.add(destValue);
                    
                    dest.put(destKey, sourceList);
                }
                //Neither are a List, create a new List
                else {
                    final List newList = new ArrayList(2);
                    
                    newList.add(destValue);
                    newList.add(sourceValue);
                    
                    dest.put(destKey, newList);
                }
            }
        }
        
        //Add the remaining items from source to dest
        dest.putAll(sourceCopy);
        
        return dest;
    }

}
