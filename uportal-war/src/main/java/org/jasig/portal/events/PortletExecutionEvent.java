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

import org.hibernate.annotations.Type;
import org.jasig.portal.dao.usertype.FunctionalNameType;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(name = "UPE_PORTLET_EXEC_EVENT")
@Inheritance(strategy=InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name="EVENT_ID")
public abstract class PortletExecutionEvent extends PortalEvent {
    private static final long serialVersionUID = 1L;
    
    @Column(name = "FNAME", length = 255, nullable = false)
    @Type(type = "fname")
    private final String fname;
    
    @Column(name="EXECUTION_TIME", nullable=false)
    private final long executionTime;

    PortletExecutionEvent() {
        super();
        this.fname = null;
        this.executionTime = -1;
    }

    PortletExecutionEvent(PortalEventBuilder eventBuilder, String fname, long executionTime) {
        super(eventBuilder);
        FunctionalNameType.validate(fname);
        this.fname = fname;
        this.executionTime = executionTime;
    }

    /**
     * @return the executionTime
     */
    public long getExecutionTime() {
        return this.executionTime;
    }
    
    /**
     * @return the fname
     */
    public String getFname() {
        return this.fname;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString() + 
                ", fname=" + this.fname + 
                ", executionTime=" + this.executionTime;
    }
}
