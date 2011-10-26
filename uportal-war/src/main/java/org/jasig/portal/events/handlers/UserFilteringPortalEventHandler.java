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

package org.jasig.portal.events.handlers;

import java.util.Collection;
import java.util.Set;

import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.spring.context.ApplicationEventFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

import com.google.common.collect.ImmutableSet;

/**
 * Filters {@link PortalEvent}s based on 
 */
public final class UserFilteringPortalEventHandler<E extends PortalEvent> implements ApplicationEventFilter<E> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/** The list of supported classes. */
	private boolean supportGuest = true;
	private Set<String> supportedUserNames;
	private Set<String> ignoredUserNames;
    private boolean requireAll = true;

    /**
     * If no <code>supportedUserNames</code> {@link Collection} is configured all user-names are supported otherwise
     * exact String equality matching is done to determine supported userNames. The property defaults to null (all user
     * names)
     * 
     * @param supportedUserNames the supportedUserNames to set
     */
    public void setSupportedUserNames(Collection<String> supportedUserNames) {
        if (supportedUserNames == null) {
            this.supportedUserNames = null;
        }
        else {
            this.supportedUserNames = ImmutableSet.copyOf(supportedUserNames);
        }
    }
    
    /**
     * If no <code>ignoredUserNames</code> {@link Collection} is configured all user-names are supported otherwise
     * exact String equality matching is done to determine ignored userNames. The property defaults to null (all user
     * names)
     * 
     * @param ignoredUserNames the ignoredUserNames to set
     */
    public void setIgnoredUserNames(Collection<String> ignoredUserNames) {
        if (ignoredUserNames == null) {
            this.ignoredUserNames = null;
        }
        else {
            this.ignoredUserNames = ImmutableSet.copyOf(ignoredUserNames);
        }
    }

    /**
     * If the <code>supportGuest</code> property is true {@link ApplicationEvent}s where {@link IPerson#isGuest()} is true or
     * false will be supported. If the <code>supportGuest</code> property is false only {@link ApplicationEvent}s where
     * {@link IPerson#isGuest()} is false will be supported. The property defaults to true.
     * 
     * @param supportGuest the supportGuest to set
     */
    public void setSupportGuest(boolean supportGuest) {
        this.supportGuest = supportGuest;
    }

    /**
     * The <code>requireAll</code> can be used to require either any one criteria match for support or all three
     * criteria.
     * 
     * @param requireAll the requireAll to set
     */
    public void setRequireAll(boolean requireAll) {
        this.requireAll = requireAll;
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.spring.context.ApplicationEventFilter#supports(org.springframework.context.ApplicationEvent)
     */
    @Override
    public boolean supports(E event) {
        
        //Guest support check
        final IPerson person = event.getPerson();
        if (this.supportGuest || !person.isGuest()) {
            if (!this.requireAll) {
                return true;
            }
        }
        else {
            if (this.requireAll) {
                return false;
            }
        }
        
        //userName check
        final String userName = person.getUserName();
        if (this.supportedUserNames == null || this.supportedUserNames.contains(userName)) {
            if (!this.requireAll) {
                return true;
            }
        }
        else {
            if (this.requireAll) {
                return false;
            }
        }
        
        //ignored userName check
        if (this.ignoredUserNames == null || !this.ignoredUserNames.contains(userName)) {
            if (!this.requireAll) {
                return true;
            }
        }
        else {
            if (this.requireAll) {
                return false;
            }
        }
        
        return false;
    }
}
