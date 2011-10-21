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

package org.jasig.portal.events.handlers.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;
import org.jasig.portal.events.PortalEvent;

/**
 * Persistent wrapper for storing portal events
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(name = "UPE_RAW_EVENTS")
@Inheritance(strategy=InheritanceType.JOINED)
@SequenceGenerator(
        name="UPE_RAW_EVENTS_GEN",
        sequenceName="UPE_RAW_EVENTS_SEQ",
        allocationSize=1000
    )
@TableGenerator(
        name="UPE_RAW_EVENTS_GEN",
        pkColumnValue="UPE_RAW_EVENTS_PROP",
        allocationSize=1000
    )
@Immutable
public class PersistentPortalEvent implements Serializable {
    private static final long serialVersionUID = 1L;
   
    @Id
    @GeneratedValue(generator = "UPE_RAW_EVENTS_GEN")
    @Column(name="EVENT_ID")
    @SuppressWarnings("unused")
    private final long id;
    
    @Column(name="TIMESTAMP", nullable=false)
    @SuppressWarnings("unused")
    private final Date timestamp;
    
    @Column(name="SERVER_ID", length=200, nullable=false)
    @SuppressWarnings("unused")
    private final String serverId;
    
    @Column(name="SESSION_ID", length=500, nullable=false)
    @SuppressWarnings("unused")
    private final String eventSessionId;
    
    @Column(name="USER_NAME", length=35, nullable=false)
    @SuppressWarnings("unused")
    private final String userName;
    
    @Column(name="EVENT_DATA", nullable=false)
    @Type(type="jsonClob")
    private final PortalEvent portalEvent;
    
    /**
     * no-arg needed by hibernate
     */
    @SuppressWarnings("unused")
    private PersistentPortalEvent() {
        this.id = -1;
        this.portalEvent = null;
        this.timestamp = null;
        this.serverId = null;
        this.eventSessionId = null;
        this.userName = null;
    }
    
    PersistentPortalEvent(PortalEvent portalEvent) {
        this.id = -1;
        this.portalEvent = portalEvent;
        this.timestamp = this.portalEvent.getTimestampAsDate();
        this.serverId = this.portalEvent.getServerId();
        this.eventSessionId = this.portalEvent.getEventSessionId();
        this.userName = this.portalEvent.getUserName();
    }

    /**
     * @return the portalEvent
     */
    public PortalEvent getPortalEvent() {
        return this.portalEvent;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.portalEvent.toString();
    }
}
