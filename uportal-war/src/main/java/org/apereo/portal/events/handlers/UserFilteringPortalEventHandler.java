/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.events.handlers;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;
import org.apereo.portal.events.PortalEvent;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.spring.context.ApplicationEventFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

/** Filters {@link PortalEvent}s based on */
public final class UserFilteringPortalEventHandler<E extends PortalEvent>
        implements ApplicationEventFilter<E> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The list of supported classes. */
    private boolean supportGuest = true;

    private Set<String> supportedUserNames;
    private Set<String> ignoredUserNames;
    private Set<Pattern> ignoredUserNamePatterns;

    /**
     * If no <code>supportedUserNames</code> {@link Collection} is configured all user-names are
     * supported otherwise exact String equality matching is done to determine supported userNames.
     * The property defaults to null (all user names)
     *
     * @param supportedUserNames the supportedUserNames to set
     */
    public void setSupportedUserNames(Collection<String> supportedUserNames) {
        if (supportedUserNames == null) {
            this.supportedUserNames = null;
        } else {
            this.supportedUserNames = ImmutableSet.copyOf(supportedUserNames);
        }
    }

    /**
     * If no <code>ignoredUserNames</code> {@link Collection} is configured all user-names are
     * supported otherwise exact String equality matching is done to determine ignored userNames.
     * The property defaults to null (all user names)
     *
     * @param ignoredUserNames the ignoredUserNames to set
     */
    public void setIgnoredUserNames(Collection<String> ignoredUserNames) {
        if (ignoredUserNames == null) {
            this.ignoredUserNames = null;
        } else {
            this.ignoredUserNames = ImmutableSet.copyOf(ignoredUserNames);
        }
    }

    /**
     * If no <code>ignoredUserNamePatterns</code> {@link Collection} is configured all user-names
     * are supported otherwise exact regex pattern matching is done to determine ignored userNames.
     * The property defaults to null (all user names)
     *
     * @param ignoredUserNamePatterns the ignoredUserNamePatterns to set
     */
    public void setIgnoredUserNamePatterns(Collection<Pattern> ignoredUserNamePatterns) {
        if (ignoredUserNamePatterns == null) {
            this.ignoredUserNamePatterns = null;
        } else {
            this.ignoredUserNamePatterns = ImmutableSet.copyOf(ignoredUserNamePatterns);
        }
    }

    /**
     * If the <code>supportGuest</code> property is true {@link ApplicationEvent}s where {@link
     * IPerson#isGuest()} is true or false will be supported. If the <code>supportGuest</code>
     * property is false only {@link ApplicationEvent}s where {@link IPerson#isGuest()} is false
     * will be supported. The property defaults to true.
     */
    public void setSupportGuest(boolean supportGuest) {
        this.supportGuest = supportGuest;
    }

    @Override
    public boolean supports(E event) {
        if (!(event instanceof PortalEvent)) {
            return false;
        }

        //Guest support check
        final IPerson person = event.getPerson();
        if (person != null && !this.supportGuest && person.isGuest()) {
            return false;
        }

        //userName check
        final String userName = event.getUserName();
        if (this.supportedUserNames != null && this.supportedUserNames.contains(userName)) {
            return true;
        }

        //ignored userName check
        if (this.ignoredUserNames != null && this.ignoredUserNames.contains(userName)) {
            return false;
        }

        //ignored userName pattern check
        if (this.ignoredUserNamePatterns != null) {
            for (final Pattern ignoredUserNamePattern : this.ignoredUserNamePatterns) {
                if (ignoredUserNamePattern.matcher(userName).matches()) {
                    return false;
                }
            }
        }

        return this.supportedUserNames == null || this.supportedUserNames.isEmpty();
    }
}
