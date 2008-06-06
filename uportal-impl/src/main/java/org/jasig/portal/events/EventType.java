/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.events;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jasig.portal.utils.threading.MapCachingDoubleCheckedCreator;

/**
 * Descriptor object for portal events, added to support the legacy table structure of the UW-Madison database
 * portal statistics gathering schema.
 * <br/>
 * This class can only be created through the static <code>getEventType</code> methods to ensure only once instance
 * for each {@link #getType()} is created. The {@link #intern()} method furthers this functionality allowing client
 * code to ensure they have the correct, single instance. 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class EventType {
    private static final EventTypeCreator EVENT_TYPES = new EventTypeCreator();
    
    public static EventType getEventType(String type) {
        return getEventType(type, null);
    }
    
    public static EventType getEventType(String type, String description) {
        return EVENT_TYPES.get(type, description);
    }
    
    private long id;
    private String type;
    private String description;
    
    //Only used by JPA/Hibernate
    private EventType() {
    }
    
    private EventType(String type, String description) {
        this.type = type;
        this.description = description;
    }
    
    /**
     * Used to sync the loaded instance with the interned instance after persistence actions.
     */
    @SuppressWarnings("unused")
    @PostLoad
    @PostPersist
    @PostUpdate
    @PostRemove
    private void init() {
        final EventType eventType = this.intern();
        eventType.id = this.id;
    }
    
    
    /**
     * @return the id
     */
    public long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    
    /**
     * @return The single unique EventType instance for the specified type
     */
    public EventType intern() {
        final EventType eventType = getEventType(this.type, this.description);
        return eventType;
    }

    
    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof EventType)) {
            return false;
        }
        EventType rhs = (EventType) object;
        return new EqualsBuilder()
            .append(this.id, rhs.getId())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143)
            .append(this.id)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("id", this.id)
            .append("type", this.type)
            .append("description", this.description)
            .toString();
    }
    
    private static final class EventTypeCreator extends MapCachingDoubleCheckedCreator<String, EventType> {
        public EventTypeCreator() {
            super(new HashMap<String, EventType>(), new ReentrantReadWriteLock());
        }
        
        /* (non-Javadoc)
         * @see org.jasig.portal.utils.threading.MapCachingDoubleCheckedCreator#getKey(java.lang.Object[])
         */
        @Override
        protected String getKey(Object... args) {
            final String type = (String) args[0];
            return type;
        }

        
        /* (non-Javadoc)
         * @see org.jasig.portal.utils.threading.MapCachingDoubleCheckedCreator#createInternal(java.lang.Object, java.lang.Object[])
         */
        @Override
        protected EventType createInternal(String type, Object... args) {
            final String description = (String) args[1];
            return new EventType(type, description);
        }
    }
}
