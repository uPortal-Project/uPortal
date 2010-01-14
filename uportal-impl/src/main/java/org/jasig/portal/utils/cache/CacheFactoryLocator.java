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


/**
 *
 * Convenience locator class for locating the CacheFactory without each class
 * needing the cache factory to do the lookup in the application context.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.5
 *
 * @see CacheFactory
 * @deprecated Code should be injected with the 'cacheFactory' wherever possible 
 */
public class CacheFactoryLocator {

    /**
     * Method to retrieve the cache factory
     * @return the cache factory.
     */
    public static final CacheFactory getCacheFactory() {
        return org.jasig.portal.spring.locator.CacheFactoryLocator.getCacheFactory();
    }
}
