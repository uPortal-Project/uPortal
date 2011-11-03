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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.commons.lang.Validate;
import org.hibernate.annotations.NaturalId;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(name = "UP_MUTEX")
@Inheritance(strategy=InheritanceType.JOINED)
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
//Cannot be marked @Immutable otherwise it cannot be locked on
class ClusterMutex implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_MUTEX_GEN")
    @Column(name="MUTEX_ID")
    private final long id;
    
    @NaturalId
    @Column(name="MUTEX_NAME", length=200, nullable=false)
    private final String name;
    
    @SuppressWarnings("unused")
    private ClusterMutex() {
        this.id = -1;
        this.name = null;
    }

    ClusterMutex(String name) {
        Validate.notNull(name, "name");
        
        this.id = -1;
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ClusterMutex [id=" + this.id + ", name=" + this.name + "]";
    }
}
