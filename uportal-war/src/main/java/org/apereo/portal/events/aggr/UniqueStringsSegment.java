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
package org.apereo.portal.events.aggr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.PostLoad;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Utility entity used to work around the collection cache invalidation behavior of Hibernate. Code
 * that needs to maintain a set of unique strings over time can add a new {@link
 * UniqueStringsSegment} in each jpa session. This will result in the set of UniqueStringsSegments
 * being reloaded for the parent entity but the contents of each UniqueStringsSegment will not need
 * to be modified.
 *
 */
@Entity
@Table(name = "UP_UNIQUE_STR_SEGMENT")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(
    name = "UP_UNIQUE_STR_SEGMENT_GEN",
    sequenceName = "UP_UNIQUE_STR_SEGMENT_SEQ",
    allocationSize = 1000
)
@TableGenerator(
    name = "UP_UNIQUE_STR_SEGMENT_GEN",
    pkColumnValue = "UP_UNIQUE_STR_SEGMENT_PROP",
    allocationSize = 1000
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public final class UniqueStringsSegment {
    @Id
    @GeneratedValue(generator = "UP_UNIQUE_STR_SEGMENT_GEN")
    @Column(name = "UNIQUE_STR_SEGMENT_ID")
    private final long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "UP_UNIQUE_STR_SEGMENT__UIDS",
        joinColumns = @JoinColumn(name = "UNIQUE_STR_SEGMENT_ID")
    )
    @Column(name = "UNIQUE_STR", nullable = false, updatable = false, length = 255)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private final Set<String> uniqueStrings = new HashSet<String>();

    @Transient private boolean closed = false;

    public UniqueStringsSegment() {
        this.id = -1;
    }

    public UniqueStringsSegment(Collection<UniqueStringsSegment> uniqueStringsSegments) {
        this.id = -1;
        for (final UniqueStringsSegment uniqueStringsSegment : uniqueStringsSegments) {
            this.uniqueStrings.addAll(uniqueStringsSegment.uniqueStrings);
        }
    }

    @PostLoad
    @PreUpdate
    void closeSegment() {
        //As soon as the segment is persisted (or loaded from a persistent store) mark it closed
        closed = true;
    }

    public int size() {
        return uniqueStrings.size();
    }

    public boolean contains(String o) {
        return uniqueStrings.contains(o);
    }

    public boolean add(String e) {
        if (closed) {
            throw new IllegalStateException("Segment is already closed");
        }
        return uniqueStrings.add(e);
    }

    public boolean addAll(UniqueStringsSegment s) {
        return uniqueStrings.addAll(s.uniqueStrings);
    }

    public boolean addAll(Collection<? extends String> c) {
        return uniqueStrings.addAll(c);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (id == -1) //If id is -1 then equality must be by instance
        return false;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        UniqueStringsSegment other = (UniqueStringsSegment) obj;
        if (id != other.id) return false;
        return true;
    }

    @Override
    public String toString() {
        return "UniqueStringsSegment [id="
                + id
                + ", size="
                + uniqueStrings.size()
                + ", closed="
                + closed
                + "]";
    }
}
