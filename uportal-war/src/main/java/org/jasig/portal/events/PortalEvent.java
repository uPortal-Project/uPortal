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

import org.apache.commons.lang.Validate;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.utils.SerializableObject;
import org.joda.time.DateTime;
import org.springframework.context.ApplicationEvent;

/**
 * ApplicationEvent specific to the Portal.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.6
 *
 */
@JsonAutoDetect(value=JsonMethod.FIELD, fieldVisibility=Visibility.ANY)
@JsonTypeInfo(use=Id.MINIMAL_CLASS)
@JsonIgnoreProperties(value="source")
public abstract class PortalEvent extends ApplicationEvent {
    public static final Object UNKNOWN_SOURCE = new SerializableObject();
    
    private static final long serialVersionUID = 1L;
   
    private final String serverId;
    private final String eventSessionId;
    private final String userName;
    @JsonIgnore
    private final IPerson person;
    @JsonIgnore
    private DateTime timestampAsDate;
    
    PortalEvent() {
        super(UNKNOWN_SOURCE);
        this.serverId = null;
        this.eventSessionId = null;
        this.person = null;
        this.userName = null;
    }

    PortalEvent(PortalEventBuilder eventBuilder) {
        super(eventBuilder.source);
        
        this.serverId = eventBuilder.serverName;
        this.eventSessionId = eventBuilder.eventSessionId;
        this.person = eventBuilder.person;
        this.userName = this.person.getUserName();
    }
    
    /**
     * @return Get the {@link #getTimestamp()} as a {@link Date}
     */
    public final DateTime getTimestampAsDate() {
        DateTime d = this.timestampAsDate;
        if (d == null) {
            d = new DateTime(this.getTimestamp());
            this.timestampAsDate = d;
        }
        
        return d;
    }

    /**
     * @return the serverId that created the event
     */
    public final String getServerId() {
        return this.serverId;
    }

    /**
     * @return The unique id that groups a set of events.
     */
    public final String getEventSessionId() {
        return this.eventSessionId;
    }

    /**
     * @return The user name for the event
     */
    public final String getUserName() {
        return this.userName;
    }

    /**
     * @return The person the event was for, may return null if this event was loaded from a persistent store
     */
    public final IPerson getPerson() {
        return this.person;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [serverId=" + this.serverId +
                ", eventSessionId=" + this.eventSessionId + 
                ", userName=" + this.userName + 
                ", timestampAsDate=" + this.getTimestampAsDate();
    }
    
    /**
     * Builder to simplify construction of PortalEvents, should be extended by
     * any subclass of PortalEvent that wants to simplify its constructor
     */
    static class PortalEventBuilder {
        private final Object source;
        private final String serverName;
        private final String eventSessionId;
        private final IPerson person;
        
         PortalEventBuilder(PortalEventBuilder portalEventBuilder) {
            this(portalEventBuilder.source, 
                    portalEventBuilder.serverName, 
                    portalEventBuilder.eventSessionId, 
                    portalEventBuilder.person);
        }

        PortalEventBuilder(Object source, String serverName, String eventSessionId, IPerson person) {
            Validate.notNull(source, "source");
            Validate.notNull(serverName, "serverId");
            Validate.notNull(eventSessionId, "eventSessionId");
            Validate.notNull(person, "person");
            
            this.source = source;
            this.serverName = serverName;
            this.eventSessionId = eventSessionId;
            this.person = person;
        }
    }
}
