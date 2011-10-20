/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.utils;

/**
 * Utilities for working with primitives
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class PrimitiveUtils {
    private PrimitiveUtils() {
    }
    
    /**
     * Check if the specified Class is a primitive and converts it to the non-primitive type
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> toReferenceClass(Class<T> type) {
        if(!type.isPrimitive()) {
            return type;
        }
        if(type == boolean.class) {
            return (Class<T>) Boolean.class;
        }
        if(type == char.class) {
            return (Class<T>) Character.class;
        }
        if(type == byte.class) {
            return (Class<T>) Byte.class;
        }
        if(type == short.class) {
            return (Class<T>) Short.class;
        }
        if(type == int.class) {
            return (Class<T>) Integer.class;
        }
        if(type == long.class) {
            return (Class<T>) Long.class;
        }
        if(type == float.class) {
            return (Class<T>) Float.class;
        }
        if(type == double.class) {
            return (Class<T>) Double.class;
        }
        
        // void.class remains
        throw new IllegalArgumentException(type + " is not permitted");
    }
}
