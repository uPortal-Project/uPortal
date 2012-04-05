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

import java.io.Serializable;
import java.lang.annotation.AnnotationFormatError;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.utils.web.PortalWebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;

/**
 * Aspect that caches the results of a method invocation in the current {@link RequestAttributes}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Aspect
@Component("requestCacheAspect")
public class RequestCacheAspect {
    private static final String CACHE_MAP = RequestCacheAspect.class.getName() + ".CACHE_MAP";
    private static final Object NULL_PLACEHOLDER = new Object();
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private volatile boolean logTimes = false;
    private final AtomicLong hitTime = new AtomicLong();
    private final AtomicLong missTime = new AtomicLong();
    private final AtomicInteger hitCount = new AtomicInteger();
    private final AtomicInteger missCount = new AtomicInteger();
    
    private IPortalRequestUtils portalRequestUtils;

    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    @Pointcut(value="execution(public * *(..))")
    public void anyPublicMethod() { }
    
    @Around("anyPublicMethod() && @annotation(requestCache)")
    public Object cacheRequest(ProceedingJoinPoint pjp, RequestCache requestCache) throws Throwable {
        final long start;
        if (this.logger.isTraceEnabled()) {
            final int hits = this.hitCount.get();
            final int misses = this.missCount.get();
            final long hitTimeAvg = this.hitTime.get() / (hits == 0 ? 1 : hits);
            final long missTimeAvg = this.missTime.get() / (misses == 0 ? 1 : misses);
            this.logger.trace(
                      "Hits " + hits + 
                    ", Miss " + misses + 
                    ", TPH " + hitTimeAvg + 
                    ", TPM " + missTimeAvg);
            
            start = System.nanoTime();
        }
        else {
            start = 0;
        } 
        
        final CacheKey cacheKey = createCacheKey(pjp, requestCache);
        
        final HttpServletRequest currentPortalRequest;
        try {
            currentPortalRequest = this.portalRequestUtils.getCurrentPortalRequest();
        }
        catch (IllegalStateException e) {
            logger.debug("No current portal request, will not cache result of: {}", cacheKey);
            //No current request, simply proceed
            return pjp.proceed();
        }
        
        //Check in the cache for a result
        final ConcurrentMap<CacheKey, Object> cache = PortalWebUtils.getMapRequestAttribute(currentPortalRequest, CACHE_MAP);
        Object result = cache.get(cacheKey);
        
        //Return null if placeholder was cached
        if (requestCache.cacheNull() && result == NULL_PLACEHOLDER) {
            logHit(start);
            logger.debug("Found cached null for invocation of: {}", cacheKey);
            return null;
        }
        //Rethrow if exception was cached
        if (requestCache.cacheException() && result instanceof ExceptionHolder) {
            logHit(start);
            logger.debug("Found cached exception for invocation of: {}", cacheKey);
            throw ((ExceptionHolder)result).getThrowable();
        }
        //Return cached result
        if (result != null) {
            logHit(start);
            logger.debug("Found cached result for invocation of: {}", cacheKey);
            return result;
        }
        
        try {
            //Execute the annotated emthod
            result = pjp.proceed();
            logMiss(start);

            if (result != null) {
                //Cache the not-null result
                cache.put(cacheKey, result);
                logger.debug("Cached result for invocation of: {}", cacheKey);
            }
            else if (requestCache.cacheNull()) {
                //If caching nulls cache the placeholder
                cache.put(cacheKey, NULL_PLACEHOLDER);
                logger.debug("Cached null for invocation of: {}", cacheKey);
            }
            
            return result;
        }
        catch (Throwable t) {
            logMiss(start);
            if (requestCache.cacheException()) {
                //If caching exceptions wrapp the exception and cache it
                cache.put(cacheKey, new ExceptionHolder(t));
                logger.debug("Cached exception for invocation of: {}", cacheKey);
            }
            throw t;
        }
    }
    
    public final boolean isLogTimes() {
        return logTimes || logger.isTraceEnabled();
    }

    public final void setLogTimes(boolean logTimes) {
        this.logTimes = logTimes;
    }

    public final long getHitTimeAvg() {
        final int hits = this.getHitCount();
        return this.hitTime.get() / (hits == 0 ? 1 : hits);
    }

    public final long getMissTimeAvg() {
        final int misses = this.getMissCount();
        return this.missTime.get() / (misses == 0 ? 1 : misses);
    }

    public final int getHitCount() {
        return hitCount.get();
    }

    public final int getMissCount() {
        return missCount.get();
    }

    private void logMiss(final long start) {
        this.missCount.incrementAndGet();
        if (this.isLogTimes()) {
            this.missTime.addAndGet(TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - start));
        }
    }

    private void logHit(final long start) {
        this.hitCount.incrementAndGet();
        if (this.isLogTimes()) {
            this.hitTime.addAndGet(TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - start));
        }
    }

    protected CacheKey createCacheKey(ProceedingJoinPoint pjp, RequestCache requestCache) {
        final Signature signature = pjp.getSignature();
        final Class<?> declaringType = signature.getDeclaringType();
        final String signatureLongString = signature.toLongString();
        
        final boolean[] keyMask = requestCache.keyMask();
        final Object[] args = pjp.getArgs();
        
        final Object[] keyArgs;
        if (keyMask.length == 0) {
            keyArgs = args;
        }
        else if (keyMask.length != args.length) {
            throw new AnnotationFormatError("RequestCache.keyMask has an invalid length on: " + signature.toLongString());
        }
        else {
            keyArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                if (keyMask[i]) {
                    keyArgs[i] = args[i];
                }
            }
        }
        
        return new CacheKey(declaringType, signatureLongString, keyArgs);
    }
    
    private static class ExceptionHolder implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final Throwable t;
        
        public ExceptionHolder(Throwable t) {
            this.t = t;
        }
        
        public Throwable getThrowable() {
            return this.t;
        }
    }
    
    private static class CacheKey implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Class<?> declaringType;
        private final String signatureLongString;
        private final Object[] parameters;
        private int hashCode = -1;

        /**
         * Constructs a default cache key
         *
         * @param parameters the paramters to use
         */
        public CacheKey(Class<?> declaringType, String signatureLongString, Object[] parameters) {
            this.declaringType = declaringType;
            this.signatureLongString = signatureLongString;
            this.parameters = parameters;
        }

        @Override
        public int hashCode() {
            int h = this.hashCode;
            if (h == -1) {
                final int prime = 31;
                h = 1;
                h = prime * h + ((declaringType == null) ? 0 : declaringType.hashCode());
                h = prime * h + ((signatureLongString == null) ? 0 : signatureLongString.hashCode());
                h = prime * h + Arrays.deepHashCode(parameters);
            }
            return h;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CacheKey other = (CacheKey) obj;
            if (declaringType == null) {
                if (other.declaringType != null)
                    return false;
            }
            else if (!declaringType.equals(other.declaringType))
                return false;
            if (signatureLongString == null) {
                if (other.signatureLongString != null)
                    return false;
            }
            else if (!signatureLongString.equals(other.signatureLongString))
                return false;
            if (!Arrays.equals(parameters, other.parameters))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return this.signatureLongString + " " + Arrays.deepToString(parameters); 
        }
    }
}
