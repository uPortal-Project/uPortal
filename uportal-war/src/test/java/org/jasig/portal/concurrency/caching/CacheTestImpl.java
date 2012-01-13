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
public class CacheTestImpl implements CacheTestInterface {
    private int testMethodNoCache = 0;
    private int testMethodCacheDefault = 0;
    private int testMethodCacheDefaultNoArgs = 0;
    private int testMethodCacheNull = 0;
    private int testMethodCacheThrows = 0;
    
    @Override
    public void reset() {
        this.testMethodNoCache = 0;
        this.testMethodCacheDefault = 0;
        this.testMethodCacheDefaultNoArgs = 0;
        this.testMethodCacheNull = 0;
        this.testMethodCacheThrows = 0;
    }

    @Override
    public String testMethodNoCache(String arg, boolean returnNull, boolean throwEx) {
        this.testMethodNoCache++;
        if (returnNull) {
            return null;
        }
        if (throwEx) {
            throw new RuntimeException("TEST");
        }
        return "testMethodNoCache(" + arg + ")";
    }

    @Override
    public int testMethodNoCacheCount() {
        return testMethodNoCache;
    }
    
    @RequestCache
    @Override
    public String testMethodCacheDefaultNoArgs() {
        this.testMethodCacheDefaultNoArgs++;
        return "testMethodCacheDefaultNoArgs()";
    }

    @Override
    public int testMethodNoCacheCountNoArgsCount() {
        return testMethodCacheDefaultNoArgs;
    }

    @RequestCache
    @Override
    public String testMethodCacheDefault(String arg, boolean returnNull, boolean throwEx) {
        this.testMethodCacheDefault++;
        if (returnNull) {
            return null;
        }
        if (throwEx) {
            throw new RuntimeException("TEST");
        }
        return "testMethodCacheDefault(" + arg + ")";
    }

    @Override
    public int testMethodCacheDefaultCount() {
        return testMethodCacheDefault;
    }

    @RequestCache(cacheNull=true)
    @Override
    public String testMethodCacheNull(String arg, boolean returnNull, boolean throwEx) {
        this.testMethodCacheNull++;
        if (returnNull) {
            return null;
        }
        if (throwEx) {
            throw new RuntimeException("TEST");
        }
        return "testMethodCacheNull(" + arg + ")";
    }

    @Override
    public int testMethodCacheNullCount() {
        return testMethodCacheNull;
    }

    @RequestCache(cacheException=true)
    @Override
    public String testMethodCacheThrows(String arg, boolean returnNull, boolean throwEx) {
        this.testMethodCacheThrows++;
        if (returnNull) {
            return null;
        }
        if (throwEx) {
            throw new RuntimeException("TEST");
        }
        return "testMethodCacheThrows(" + arg + ")";
    }

    @Override
    public int testMethodCacheThrowsCount() {
        return testMethodCacheThrows;
    }

}
