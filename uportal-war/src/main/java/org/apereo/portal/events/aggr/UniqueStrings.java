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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility entity used to work around the collection cache invalidation behavior of Hibernate. Code
 * that needs to maintain a set of unique strings over time can add a new {@link UniqueStrings} in
 * each jpa session. This will result in the set of UniqueStringsSegments being reloaded for the
 * parent entity but the contents of each UniqueStringsSegment will not need to be modified.
 *
 */
@Entity
@Table(name = "UP_UNIQUE_STR")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(
    name = "UP_UNIQUE_STR_GEN",
    sequenceName = "UP_UNIQUE_STR_SEQ",
    allocationSize = 1000
)
@TableGenerator(
    name = "UP_UNIQUE_STR_GEN",
    pkColumnValue = "UP_UNIQUE_STR_PROP",
    allocationSize = 1000
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public final class UniqueStrings {
    private static final int MAXIMUM_SEGMENT_COUNT = 1440;

    private static final int SEGMENT_MERGE_RATIO = 2;

    private static final int SMALL_SEGMENT_THRESHOLD = 10;

    private static final Logger LOGGER = LoggerFactory.getLogger(UniqueStrings.class);

    @Id
    @GeneratedValue(generator = "UP_UNIQUE_STR_GEN")
    @Column(name = "UNIQUE_STR_ID")
    private final long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "UNIQUE_STR_ID", nullable = false)
    @Fetch(FetchMode.JOIN)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<UniqueStringsSegment> uniqueStringSegments = new HashSet<UniqueStringsSegment>(0);

    @Transient private UniqueStringsSegment currentUniqueUsernamesSegment;

    public UniqueStrings() {
        this.id = -1;
    }

    public boolean add(String e) {
        int stringCount = 0;
        int smallSegments = 0;
        //Check if the username exists in any segment
        for (final UniqueStringsSegment uniqueUsernamesSegment : this.uniqueStringSegments) {
            if (uniqueUsernamesSegment.contains(e)) {
                return false;
            }
            final int size = uniqueUsernamesSegment.size();
            stringCount += size;
            if (size <= SMALL_SEGMENT_THRESHOLD) {
                smallSegments++;
            }
        }

        //Make sure a current segment exists
        if (this.currentUniqueUsernamesSegment == null) {
            final int segmentCount = this.uniqueStringSegments.size();
            //For more than 1440 segments or a string/segment ratio worse than 2:1 merge old segments into new
            if (segmentCount >= MAXIMUM_SEGMENT_COUNT
                    || (segmentCount > 1 && stringCount / segmentCount <= SEGMENT_MERGE_RATIO)) {
                LOGGER.debug(
                        "Merging {} segments with {} strings into a single segment",
                        segmentCount,
                        stringCount);
                this.currentUniqueUsernamesSegment = new UniqueStringsSegment();

                //Add all existing unique strings into one segment
                for (final UniqueStringsSegment uniqueUsernamesSegment :
                        this.uniqueStringSegments) {
                    this.currentUniqueUsernamesSegment.addAll(uniqueUsernamesSegment);
                }

                //Remove all old segments
                this.uniqueStringSegments.clear();

                //Add the new segment
                this.uniqueStringSegments.add(this.currentUniqueUsernamesSegment);
            }
            //If there is more than 1 existing segment with only a few strings in it join together the small segments into the new segment
            else if (smallSegments > 0) {
                LOGGER.debug("Merging {} small segments into a single segment", smallSegments);
                this.currentUniqueUsernamesSegment = new UniqueStringsSegment();

                for (final Iterator<UniqueStringsSegment> uniqueStringsSegmentItr =
                                this.uniqueStringSegments.iterator();
                        uniqueStringsSegmentItr.hasNext();
                        ) {
                    final UniqueStringsSegment uniqueStringsSegment =
                            uniqueStringsSegmentItr.next();
                    if (uniqueStringsSegment.size() <= SMALL_SEGMENT_THRESHOLD) {
                        uniqueStringsSegmentItr.remove();
                        this.currentUniqueUsernamesSegment.addAll(uniqueStringsSegment);
                    }
                }

                //Add the new segment
                this.uniqueStringSegments.add(this.currentUniqueUsernamesSegment);
            }
            //Just create a new segment
            else {
                this.currentUniqueUsernamesSegment = new UniqueStringsSegment();
                this.uniqueStringSegments.add(this.currentUniqueUsernamesSegment);
            }
        }

        return this.currentUniqueUsernamesSegment.add(e);
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
        UniqueStrings other = (UniqueStrings) obj;
        if (id != other.id) return false;
        return true;
    }

    @Override
    public String toString() {
        return "UniqueStrings [id=" + id + ", size=" + uniqueStringSegments.size() + "]";
    }
}
