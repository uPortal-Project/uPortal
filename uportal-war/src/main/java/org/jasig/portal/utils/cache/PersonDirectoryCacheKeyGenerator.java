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
package org.jasig.portal.utils.cache;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.cache.CacheKey.CacheKeyBuilder;
import org.springmodules.cache.key.CacheKeyGenerator;

public class PersonDirectoryCacheKeyGenerator implements CacheKeyGenerator {
    private static final Serializable POSSIBLE_USER_ATTRIBUTE_NAMES_VALUE = new Serializable() { private static final long serialVersionUID = 1L; };
    private static final Serializable AVAILABLE_QUERY_ATTRIBUTES_VALUE = new Serializable() { private static final long serialVersionUID = 1L; };

    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private String defaultAttributeName = "username";
    private boolean ignoreEmptyAttributes = false;
    

    /**
     * If seed attributes with empty values (null, empty string or empty list values) should be ignored when generating
     * the cache key. Defaults to false. 
     */
    public void setIgnoreEmptyAttributes(boolean ignoreEmptyAttributes) {
        this.ignoreEmptyAttributes = ignoreEmptyAttributes;
    }
    

    @Override
    public Serializable generateKey(MethodInvocation methodInvocation) {
        //Determine the tareted CachableMethod
        final CachableMethod cachableMethod = this.resolveCacheableMethod(methodInvocation);

        //Use the resolved cachableMethod to determine the seed Map and then get the hash of the key elements
        final Object[] methodArguments = methodInvocation.getArguments();
     
        final CacheKeyBuilder<String, Serializable> cacheKeyBuilder = CacheKey.builder(cachableMethod.getName());
        
        switch (cachableMethod) {
            //Both methods that take a Map argument can just have the first argument returned
            case PEOPLE_MAP:
            case PEOPLE_MULTIVALUED_MAP:
            case MULTIVALUED_USER_ATTRIBUTES__MAP:
            case USER_ATTRIBUTES__MAP: {
                for (final Map.Entry<String, Object> e : ((Map<String, Object>)methodArguments[0]).entrySet()) {
                    final String key = e.getKey();
                    final Object value = e.getValue();
                    
                    //Skip null/empty attribute values
                    if (ignoreEmptyAttributes &&
                            (
                                    value == null ||
                                    (value instanceof Collection && ((Collection)value).isEmpty()) ||
                                    (value instanceof Map && ((Map)value).isEmpty()) ||
                                    (value.getClass().isArray() && Array.getLength(value) == 0)
                            )
                        ) {
                        continue;
                    }
                    
                    if (value == null || value instanceof Serializable) {
                        cacheKeyBuilder.put(key, (Serializable)value);
                    }
                    else {
                        cacheKeyBuilder.put(key, value.getClass());
                    }
                }
                break;
            }

            //The multivalued attributes with a string needs to be converted to Map<String, List<Object>>
            case MULTIVALUED_USER_ATTRIBUTES__STR: {
                final String uid = (String)methodArguments[0];
                if (StringUtils.isEmpty(uid)) {
                    break;
                }
                
                cacheKeyBuilder.put(this.defaultAttributeName, (Serializable)Collections.singletonList(uid));
                break;
            }
            
            //The single valued attributes with a string needs to be converted to Map<String, Object>
            case PERSON_STR:
            case USER_ATTRIBUTES__STR: {
                final String uid = (String)methodArguments[0];
                if (StringUtils.isEmpty(uid)) {
                    break;
                }
                cacheKeyBuilder.put(this.defaultAttributeName, (String)methodArguments[0]);
                break;
            }
            
            //The getPossibleUserAttributeNames has a special Map seed that we return to represent calls to it 
            case POSSIBLE_USER_ATTRIBUTE_NAMES: {
                cacheKeyBuilder.put("getPossibleUserAttributeNames_seedMap", POSSIBLE_USER_ATTRIBUTE_NAMES_VALUE);
                break;
            }
            
            //The getAvailableQueryAttributes has a special Map seed that we return to represent calls to it 
            case AVAILABLE_QUERY_ATTRIBUTES: {
                cacheKeyBuilder.put("getAvailableQueryAttributes_seedMap", AVAILABLE_QUERY_ATTRIBUTES_VALUE);
                break;
            }
            
            default: {
                throw new IllegalArgumentException("Unsupported CachableMethod resolved: '" + cachableMethod + "'");
            }
        }
        
        if (cacheKeyBuilder.size() == 0) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("No cache key generated for MethodInvocation='" + methodInvocation + "'");
            }
            
            return null;
        }
        
        final CacheKey cacheKey = cacheKeyBuilder.build();
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Generated cache key '" + cacheKey + "' for MethodInvocation='" + methodInvocation + "'");
        }
        return cacheKey;
    }
    
    /**
     * Iterates over the {@link CachableMethod} instances to determine which instance the
     * passed {@link MethodInvocation} applies to.
     */
    protected CachableMethod resolveCacheableMethod(MethodInvocation methodInvocation) {
        final Method targetMethod = methodInvocation.getMethod();
        final Class<?> targetClass = targetMethod.getDeclaringClass();
        
        for (final CachableMethod cachableMethod : CachableMethod.values()) {
            Method cacheableMethod = null;
            try {
                cacheableMethod = targetClass.getMethod(cachableMethod.getName(), cachableMethod.getArgs());
            }
            catch (SecurityException e) {
                this.logger.warn("Security exception while attempting to if the target class '" + targetClass + "' implements the cachable method '" + cachableMethod + "'", e);
            }
            catch (NoSuchMethodException e) {
                final String message = "Taret class '" + targetClass + "' does not implement possible cachable method '" + cachableMethod + "'. Is the advice applied to the correct bean and methods?";
                
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug(message, e);
                }
                else {
                    this.logger.warn(message);
                }
            }

            if (targetMethod.equals(cacheableMethod)) {
                return cachableMethod;
            }
        }
        
        throw new IllegalArgumentException("Do not know how to generate a cache for for '" + targetMethod + "' on class '" + targetClass + "'. Is the advice applied to the correct bean and methods?");
    }


    /**
     * Methods on {@link org.jasig.services.persondir.IPersonAttributeDao} that are cachable
     */
    public enum CachableMethod {
        @Deprecated
        MULTIVALUED_USER_ATTRIBUTES__MAP("getMultivaluedUserAttributes", Map.class),
        @Deprecated
        MULTIVALUED_USER_ATTRIBUTES__STR("getMultivaluedUserAttributes", String.class),
        @Deprecated
        USER_ATTRIBUTES__MAP("getUserAttributes", Map.class),
        @Deprecated
        USER_ATTRIBUTES__STR("getUserAttributes", String.class),
        
        PERSON_STR("getPerson", String.class),
        PEOPLE_MAP("getPeople", Map.class),
        PEOPLE_MULTIVALUED_MAP("getPeopleWithMultivaluedAttributes", Map.class),
        POSSIBLE_USER_ATTRIBUTE_NAMES("getPossibleUserAttributeNames"),
        AVAILABLE_QUERY_ATTRIBUTES("getAvailableQueryAttributes");
        
        private final String name;
        private final Class<?>[] args;
        
        private CachableMethod(String name, Class<?>... args) {
            this.name = name;
            this.args = args;
        }

        /**
         * @return the name
         */
        public String getName() {
            return this.name;
        }
        /**
         * @return the args
         */
        public Class<?>[] getArgs() {
            return this.args;
        }

        @Override
        public String toString() {
            return this.name + "(" + Arrays.asList(this.args) + ")";
        }
    }
}
