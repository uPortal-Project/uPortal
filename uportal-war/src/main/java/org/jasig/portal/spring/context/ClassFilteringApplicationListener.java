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

package org.jasig.portal.spring.context;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.GenericTypeResolver;

import com.google.common.collect.MapMaker;

/**
 * Extension of SmartApplicationListener that allows configuration of a set of supported event classes
 * 
 * extend GenericApplicationListenerAdapter
 */
public class ClassFilteringApplicationListener<E extends ApplicationEvent> implements ApplicationEventFilter<E>, InitializingBean {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final Map<Class<? extends ApplicationEvent>, Boolean> supportedEventsCache = new MapMaker().weakKeys().makeMap();
	private Class<?> typeArg;
	
    private Set<Class<? extends ApplicationEvent>> supportedEvents;

    /**
     * @return the supportedEvents
     */
    public Collection<Class<? extends ApplicationEvent>> getSupportedEvents() {
        return Collections.unmodifiableCollection(supportedEvents);
    }
    /**
     * If no <code>supportedEvents</code> {@link Collection} is configured all {@link ApplicationEvent} sub-classes are
     * supported otherwise matching is done. The property defaults to null (all event types)
     * 
     * @param supportedEvents the supportedEvents to set
     */
    public void setSupportedEvents(Collection<Class<? extends ApplicationEvent>> supportedEvents) {
        this.supportedEventsCache.clear();
        if (supportedEvents == null) {
            this.supportedEvents = null;
        }
        else {
            this.supportedEvents = new LinkedHashSet<Class<? extends ApplicationEvent>>(supportedEvents);
        }
    }
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //Figure out the generic type's Class, cribbed from SmartApplicationListener
        Class<?> typeArg = GenericTypeResolver.resolveTypeArgument(this.getClass(), ApplicationListener.class);
        if (typeArg == null || typeArg.equals(ApplicationEvent.class)) {
            Class<?> targetClass = AopUtils.getTargetClass(this);
            if (targetClass != this.getClass()) {
                typeArg = GenericTypeResolver.resolveTypeArgument(targetClass, ApplicationListener.class);
            }
        }
        this.typeArg = typeArg;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.spring.context.ApplicationEventFilter#supports(org.springframework.context.ApplicationEvent)
     */
    @Override
    public boolean supports(E event) {
        final Class<? extends ApplicationEvent> eventType = event.getClass();
        final Boolean cachedSupports = this.supportedEventsCache.get(eventType);
        if (cachedSupports != null) {
            return cachedSupports;
        }
        
        final boolean supports = supportsEventTypeHelper(eventType);
        this.supportedEventsCache.put(eventType, supports);
        return supports;
    }
    
    private boolean supportsEventTypeHelper(Class<? extends ApplicationEvent> eventType) {
        if (typeArg != null && !typeArg.isAssignableFrom(eventType)) {
            return false;
        }
        
        //Check inheritance for includes match if no explicitly matching
        for (final Class<? extends ApplicationEvent> includedType : this.supportedEvents) {
            if (includedType.isAssignableFrom(eventType)) {
                return true;
            }
        }
        
        return false;
    }

}
