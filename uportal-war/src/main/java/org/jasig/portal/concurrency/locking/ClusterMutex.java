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

package org.jasig.portal.concurrency.locking;

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
import javax.persistence.Version;

import org.apache.commons.lang.Validate;
import org.hibernate.annotations.NaturalId;
import org.springframework.util.Assert;

/**
 * Used to coordinate cluster wide locking via the database. Tracks the server that currently owns the lock, when
 * the lock started, last updated and released.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(name = "UP_MUTEX")
@SequenceGenerator(
        name="UP_MUTEX_GEN",
        sequenceName="UP_MUTEX_SEQ",
        allocationSize=1
    )
@TableGenerator(
        name="UP_MUTEX_GEN",
        pkColumnValue="UP_MUTEX_PROP",
        allocationSize=1
    )
//THIS CLASS CANNOT BE CACHED
class ClusterMutex implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_MUTEX_GEN")
    @Column(name="MUTEX_ID")
    private final long id;
    
    @SuppressWarnings("unused")
    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;
    
    @NaturalId
    @Column(name="MUTEX_NAME", length=200, nullable=false)
    private final String name;
    
    @Column(name="LOCKED", nullable=false)
    private boolean locked = false;
    
    @Column(name="SERVER_ID", length=200)
    private String serverId;
    
    @Column(name="LOCK_START", nullable=false)
    private Date lockStart = new Date(0);
    
    @Column(name="LOCK_UPDATE", nullable=false)
    private Date lastUpdate = new Date(0);
    
    @Column(name="LOCK_END", nullable=false)
    private Date lockEnd = new Date(0);
    
    @SuppressWarnings("unused")
    private ClusterMutex() {
        this.id = -1;
        this.entityVersion = -1;
        this.name = null;
    }

    ClusterMutex(String name) {
        Validate.notNull(name, "name");
        
        this.id = -1;
        this.entityVersion = 0;
        this.name = name;
    }

    /**
     * @return the id
     */
    public long getId() {
        return this.id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return If the lock is currently held
     */
    public boolean isLocked() {
        return this.locked;
    }

    /**
     * @return the serverId
     */
    public String getServerId() {
        return this.serverId;
    }

    /**
     * @return the lockStart
     */
    public long getLockStart() {
        return this.lockStart.getTime();
    }

    /**
     * @return the lastUpdate
     */
    public long getLastUpdate() {
        return this.lastUpdate.getTime();
    }

    /**
     * @return the lockEnd
     */
    public long getLockEnd() {
        return this.lockEnd.getTime();
    }
    

    /**
     * Mark the mutex as locked by the specific server
     */
    void lock(String serverId) {
        Assert.notNull(serverId);
        if (this.locked) {
            throw new IllegalStateException("Cannot lock already locked mutex: " + this);
        }
        this.locked = true;
        this.lockStart = new Date();
        this.lastUpdate = this.lockStart;
        this.serverId = serverId;
    }
    
    void unlock() {
        if (!this.locked) {
            throw new IllegalStateException("Cannot unlock already unlocked mutex: " + this);
        }
        this.locked = false;
        this.lockEnd = new Date();
        this.serverId = null;
    }

    /**
     * @param lastUpdate the lastUpdate to set
     */
    void updateLock() {
        this.lastUpdate = new Date();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClusterMutex other = (ClusterMutex) obj;
        if (this.name == null) {
            if (other.name != null)
                return false;
        }
        else if (!this.name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ClusterMutex [id=" + this.id + ", name=" + this.name + ", locked=" + this.locked + ", serverId="
                + this.serverId + ", lockStart=" + this.lockStart + ", lastUpdate=" + this.lastUpdate + ", lockEnd="
                + this.lockEnd + "]";
    }
}
