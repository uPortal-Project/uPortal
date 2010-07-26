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

package org.jasig.portal.events;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.events.handlers.db.StatsSession;
import org.jasig.portal.security.IPerson;
import org.springframework.context.ApplicationEvent;

/**
 * ApplicationEvent specific to the Portal.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.6
 *
 */
public abstract class PortalEvent extends ApplicationEvent {
    private final IPerson person;
    private EventType eventType;
    private long id;
    private Date timeStampAsDate;
    private StatsSession statsSession;

    public PortalEvent(final Object source, final IPerson person) {
        super(source);
        this.person = person;
        this.eventType = EventType.getEventType(this.getClass().getName(), null);
    }
    
    public PortalEvent(final Object source, final IPerson person, final EventType eventType) {
        super(source);
        this.person = person;
        this.eventType = eventType;
    }

    public final IPerson getPerson() {
        return this.person;
    }
    public final Date getTimestampAsDate() {
        //Don't bother with syncronization, doesn't matter if multiple Dates are created in a race condition
        if (this.timeStampAsDate == null) {
            final long timestamp = this.getTimestamp();
            this.timeStampAsDate = new Date(timestamp);
        }

        return this.timeStampAsDate;
    }

    public final String getDescription() {
        return this.toString();
    }
    public final void setDescription(String description) {
        //ignore, method required for hibernate
    }
    public final void setTimestampAsDate(Date timestamp) {
        //ignore, method required for hibernate
    }
    
    public EventType getEventType() {
        return eventType;
    }
    
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    
    public StatsSession getStatsSession() {
        return statsSession;
    }
    public void setStatsSession(StatsSession statsSession) {
        this.statsSession = statsSession;
    }

    protected String getDisplayName() {
        if (person == null) {
            return "NULL_PERSON";
        }

        final String userName = StringUtils.trimToEmpty(((String) person.getAttribute(IPerson.USERNAME)));

        if (person.isGuest()) {
            return "GUEST_USER (" + userName + ")";
        }

        final String firstName = StringUtils.trimToEmpty((String) person.getAttribute("givenName"));
        final String lastName = StringUtils.trimToEmpty((String) person.getAttribute("sn"));

        return firstName + " " + lastName + " (" + userName + ")";
    }
    
    /* (non-Javadoc)
     * @see java.util.EventObject#toString()
     */
    @Override
    public String toString() {
        return this.getClass().getName() + " for "  + this.getDisplayName() + " at " + this.getTimestampAsDate();
    }
}
