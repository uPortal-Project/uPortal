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

package org.jasig.portal.concurrency.caching;

import java.lang.annotation.AnnotationFormatError;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.web.context.request.RequestAttributes;

/**
 * Marks a method whose result should be cached in the current {@link RequestAttributes}. If there is
 * no current request no caching is done.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestCache {
    /**
     * If null return values should be cached
     */
    boolean cacheNull() default false;
    
    /**
     * If exceptions should be cached and rethrown
     */
    boolean cacheException() default false;
    
    /**
     * Array of booleans designating which parameters should be used in the key. If the array is empty
     * all parameters are included. If the array length is different than the number of parameters
     * an {@link AnnotationFormatError} is thrown
     */
    boolean[] keyMask() default {}; 
}
