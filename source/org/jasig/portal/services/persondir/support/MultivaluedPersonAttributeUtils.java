/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;



/**
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public final class MultivaluedPersonAttributeUtils {
    private MultivaluedPersonAttributeUtils() { }
    
    /**
     * Set the {@link Map} to use for mapping from a attribute name to another
     * attribute name or {@link Set} of attribute names.
     * <br>
     * The passed {@link Map} must have keys of type {@link String} and values
     * of type {@link String} or a {@link Set} of {@link String}.
     * 
     * @param mapping {@link Map} from column names to attribute names.
     * @throws IllegalArgumentException If the {@link Map} doesn't follow the rules stated above.
     */
    public static  Map parseAttributeToAttributeMapping(final Map mapping) {
        //null is assumed to be an empty map
        if (mapping == null) {
            return Collections.EMPTY_MAP;
        }
        //do a defenisve copy of the map
        else {
            final Map mappedAttributesBuilder = new HashMap();
            
            for (final Iterator sourceAttrNameItr = mapping.keySet().iterator(); sourceAttrNameItr.hasNext(); ) {
                final String sourceAttrName = (String)sourceAttrNameItr.next();
                
                //The column name must exist
                if (sourceAttrName == null)
                    throw new IllegalArgumentException("The map from attribute names to attributes must not have any null keys.");
                
                final Object mappedAttribute = mapping.get(sourceAttrName);
                
                //mapping cannot be null
                if (mappedAttribute == null)
                    throw new IllegalArgumentException("Values in the map cannot be null. key='" + sourceAttrName + "'");
                
                //Create a single item set for the string mapping
                if (mappedAttribute instanceof String) {
                    final Set mappedSet = Collections.singleton(mappedAttribute);
                    mappedAttributesBuilder.put(sourceAttrName, mappedSet);
                }
                //Create a defenisve copy of the mapped set & verify it's contents are strings
                else if (mappedAttribute instanceof Set) {
                    final Set sourceSet = (Set)mappedAttribute;
                    final Set mappedSet = new HashSet();
                    
                    for (final Iterator sourceSetItr = sourceSet.iterator(); sourceSetItr.hasNext(); ) {
                        final Object mappedAttributeName = sourceSetItr.next();
                        
                        if (mappedAttributeName instanceof String) {
                            mappedSet.add(mappedAttributeName);
                        }
                        else {
                            throw new IllegalArgumentException("Invalid mapped type. key='" + sourceAttrName + "', value type='" + mappedAttribute.getClass().getName() + "', sub value type='" + mappedAttributeName.getClass().getName() + "'");
                        }
                    }
                    
                    mappedAttributesBuilder.put(sourceAttrName, Collections.unmodifiableSet(mappedSet));
                }
                //Not a valid type for the mapping
                else {
                    throw new IllegalArgumentException("Invalid mapped type. key='" + sourceAttrName + "', value type='" + mappedAttribute.getClass().getName() + "'");
                }
            }
            
            return Collections.unmodifiableMap(mappedAttributesBuilder);
        }
    }
    
    /**
     * Adds a key/value pair to the specified {@link Map}, creating multi-valued
     * values when appropriate.
     * <br>
     * Since multi-valued attributes end up with a value of type
     * {@link List}, passing in a {@link List} of any type will
     * cause it's contents to be added to the <code>results</code>
     * {@link Map} directly under the specified <code>key</code>
     * 
     * @param results The {@link Map} to modify.
     * @param key The key to add the value for.
     * @param value The value to add for the key.
     */
    public static void addResult(final Map results, final Object key, final Object value) {
        final Object currentValue = results.get(key);

        //Key doesn't have a value yet, add the value 
        if (currentValue == null) {
            results.put(key, value);
        }
        //Set of values
        else if (value instanceof List) {
            final List newValues = (List)value;
            
            //Key exists with List, add to it
            if (currentValue instanceof List) {
                final List values = (List)currentValue;
                
                values.addAll(newValues);

                results.put(key, values);
            }
            //Key exists with a single value, create a List
            else {
                final List values = new ArrayList(newValues.size() + 1);

                values.add(currentValue);
                values.addAll(newValues);
                
                results.put(key, values);
            }
        }
        //Standard value
        else {
            //Key exists with List, add to it
            if (currentValue instanceof List) {
                final List values = (List)currentValue;
                
                values.add(value);

                results.put(key, values);
            }
            //Key exists with a single value, create a List
            else {
                final List values = new ArrayList(2);
                
                values.add(currentValue);
                values.add(value);
                
                results.put(key, values);
            }
        }
    }
    
    /**
     * Takes a {@link Collection} and creates a flattened {@link Collection} out
     * of it.
     * 
     * @param source The {@link Collection} to flatten.
     * @return A flattened {@link Collection} that contains all entries from all levels of <code>source</code>.
     */
    public static Collection flattenCollection(final Collection source) {
        final Collection result = new LinkedList();
        
        for (final Iterator setItr = source.iterator(); setItr.hasNext();) {
            final Object value = setItr.next();
            
            if (value instanceof Collection) {
                final Collection flatCollection = flattenCollection((Collection)value);
                result.addAll(flatCollection);
            }
            else {
                result.add(value);
            }   
        }
        
        return result;
    }
}
