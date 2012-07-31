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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.hibernate.management.impl.EhcacheHibernateMbeanNames;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.utils.ConcurrentMapUtils;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.utils.web.PortalWebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.MBeanExportOperations;
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
public class RequestCacheAspect implements InitializingBean {
    private static final String CACHE_MAP = RequestCacheAspect.class.getName() + ".CACHE_MAP";
    private static final Object NULL_PLACEHOLDER = new Object();
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final ConcurrentMap<String, CacheStatistics> methodStats = new ConcurrentHashMap<String, CacheStatistics>();
    private final CacheStatistics overallStats = new CacheStatistics();

    private IPortalRequestUtils portalRequestUtils;
    private MBeanExportOperations mBeanExportOperations;
    
    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }
    @Autowired(required=false)
    public void setmBeanExportOperations(MBeanExportOperations mBeanExportOperations) {
        this.mBeanExportOperations = mBeanExportOperations;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.mBeanExportOperations != null) {
            final ObjectName name = new ObjectName("uPortal:section=Cache,RequestCache=RequestCache,name=OverallStatistics");
            registerMbean(this.overallStats, name);
        }
    }
    
    @Pointcut(value="execution(public * *(..))")
    public void anyPublicMethod() { }
    
    @Around("anyPublicMethod() && @annotation(requestCache)")
    public Object cacheRequest(ProceedingJoinPoint pjp, RequestCache requestCache) throws Throwable {
        final long start = System.nanoTime();
        
        final CacheKey cacheKey = createCacheKey(pjp, requestCache);
        
        final HttpServletRequest currentPortalRequest;
        try {
            currentPortalRequest = this.portalRequestUtils.getCurrentPortalRequest();
        }
        catch (IllegalStateException e) {
            logger.trace("No current portal request, will not cache result of: {}", cacheKey);
            //No current request, simply proceed
            return pjp.proceed();
        }
        
        final CacheStatistics cacheStatistics = this.getCacheStatistics(pjp, requestCache);
        
        //Check in the cache for a result
        final ConcurrentMap<CacheKey, Object> cache = PortalWebUtils.getMapRequestAttribute(currentPortalRequest, CACHE_MAP);
        Object result = cache.get(cacheKey);
        
        //Return null if placeholder was cached
        if (requestCache.cacheNull() && result == NULL_PLACEHOLDER) {
            final long time = System.nanoTime() - start;
            cacheStatistics.recordHit(time);
            overallStats.recordHit(time);
            logger.debug("Found cached null for invocation of: {}", cacheKey);
            return null;
        }
        //Rethrow if exception was cached
        if (requestCache.cacheException() && result instanceof ExceptionHolder) {
            final long time = System.nanoTime() - start;
            cacheStatistics.recordHit(time);
            overallStats.recordHit(time);
            logger.debug("Found cached exception for invocation of: {}", cacheKey);
            throw ((ExceptionHolder)result).getThrowable();
        }
        //Return cached result
        if (result != null) {
            final long time = System.nanoTime() - start;
            cacheStatistics.recordHit(time);
            overallStats.recordHit(time);
            logger.debug("Found cached result for invocation of: {}", cacheKey);
            return result;
        }
        
        try {
            //Execute the annotated emthod
            result = pjp.proceed();
            final long time = System.nanoTime() - start;
            cacheStatistics.recordMissAndLoad(time);
            overallStats.recordMissAndLoad(time);

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
            final long time = System.nanoTime() - start;
            cacheStatistics.recordMissAndException(time);
            overallStats.recordMissAndException(time);
            if (requestCache.cacheException()) {
                //If caching exceptions wrapp the exception and cache it
                cache.put(cacheKey, new ExceptionHolder(t));
                logger.debug("Cached exception for invocation of: {}", cacheKey);
            }
            throw t;
        }
    }
    
    protected void registerMbean(Object object, ObjectName name) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        this.mBeanExportOperations.registerManagedResource(object, name);
    }
    
    protected final CacheStatistics getCacheStatistics(ProceedingJoinPoint pjp, RequestCache requestCache) {
        final Signature signature = pjp.getSignature();
        final String signatureString = signature.toString();
        
        CacheStatistics cacheStatistics = this.methodStats.get(signatureString);
        if (cacheStatistics == null) {
            final CacheStatistics newStats = new CacheStatistics();
            cacheStatistics = ConcurrentMapUtils.putIfAbsent(this.methodStats, signatureString, newStats);
            
            if (this.mBeanExportOperations != null && cacheStatistics == newStats) {
                final String nameString = "uPortal:section=Cache,RequestCache=RequestCache,name=" + EhcacheHibernateMbeanNames.mbeanSafe(signatureString);
                try {
                    final ObjectName name = new ObjectName(nameString);
                    registerMbean(cacheStatistics, name);
                }
                catch (MalformedObjectNameException e) {
                    logger.warn("Failed to create ObjectName {} the corresponding CacheStatistics will not be registered with JMX", nameString, e);
                }
                catch (NullPointerException e) {
                    logger.warn("Failed to create ObjectName {} the corresponding CacheStatistics will not be registered with JMX", nameString, e);
                }
                catch (InstanceAlreadyExistsException e) {
                    logger.warn("ObjectName {} is already registered, the corresponding CacheStatistics will not be registered with JMX", nameString, e);
                }
                catch (MBeanRegistrationException e) {
                    logger.warn("Failed to register ObjectName {} the corresponding CacheStatistics will not be registered with JMX", nameString, e);
                }
                catch (NotCompliantMBeanException e) {
                    logger.warn("Failed to register ObjectName {} the corresponding CacheStatistics will not be registered with JMX", nameString, e);
                }
            }
        }
        
        return cacheStatistics;
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
        
        return CacheKey.build(signatureLongString, declaringType, keyArgs);
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
}
