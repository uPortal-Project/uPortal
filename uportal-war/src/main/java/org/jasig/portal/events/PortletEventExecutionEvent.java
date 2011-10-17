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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.portlet.EventRequest;
import javax.xml.namespace.QName;

import org.apache.commons.lang.Validate;
import org.hibernate.annotations.Type;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(name = "UPE_PORTLET_EVENT_EVENT")
@Inheritance(strategy=InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name="EVENT_ID")
public final class PortletEventExecutionEvent extends PortletExecutionEvent {
    private static final long serialVersionUID = 1L;

    @Column(name = "QNAME", length = 1000, nullable = false)
    @Type(type = "qname")
    private final QName eventName;
    
    @SuppressWarnings("unused")
    private PortletEventExecutionEvent() {
        this.eventName = null;
    }

    PortletEventExecutionEvent(PortalEventBuilder eventBuilder, String fname, long executionTime, QName eventName) {
        super(eventBuilder, fname, executionTime);
        Validate.notNull(eventName, "eventName");
        this.eventName = eventName;
    }

    /**
     * @return the eventName
     * @see EventRequest#getEvent()
     */
    public QName getEventName() {
        return this.eventName;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString() + 
                ", eventName=" + this.eventName + "]";
    }
}
