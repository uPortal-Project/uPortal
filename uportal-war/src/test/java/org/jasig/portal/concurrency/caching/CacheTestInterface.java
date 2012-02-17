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

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface CacheTestInterface {

    String testMethodNoCache(String arg, boolean returnNull, boolean throwEx);
    int testMethodNoCacheCount();
    
    String testMethodCacheDefaultNoArgs();
    int testMethodNoCacheCountNoArgsCount();
    
    String testMethodCacheDefault(String arg, boolean returnNull, boolean throwEx);
    int testMethodCacheDefaultCount();
    
    String testMethodCacheNull(String arg, boolean returnNull, boolean throwEx);
    int testMethodCacheNullCount();
    
    String testMethodCacheThrows(String arg, boolean returnNull, boolean throwEx);
    int testMethodCacheThrowsCount();
    
    void reset();
}
