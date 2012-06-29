package org.jasig.portal.events.aggr;

import java.util.HashSet;
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
 * Utility entity used to work around the collection cache invalidation behavior of Hibernate. Code that needs to
 * maintain a set of unique strings over time can add a new {@link UniqueStrings} in each jpa session. This
 * will result in the set of UniqueStringsSegments being reloaded for the parent entity but the contents of each
 * UniqueStringsSegment will not need to be modified.
 * 
 * @author Eric Dalquist
 */
@Entity
@Table(name = "UP_UNIQUE_STR")
@Inheritance(strategy=InheritanceType.JOINED)
@SequenceGenerator(
        name="UP_UNIQUE_STR_GEN",
        sequenceName="UP_UNIQUE_STR_SEQ",
        allocationSize=1000
    )
@TableGenerator(
        name="UP_UNIQUE_STR_GEN",
        pkColumnValue="UP_UNIQUE_STR_PROP",
        allocationSize=1000
    )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public final class UniqueStrings {
    private static final Logger LOGGER = LoggerFactory.getLogger(UniqueStrings.class);
    
    @Id
    @GeneratedValue(generator = "UP_UNIQUE_STR_GEN")
    @Column(name="UNIQUE_STR_ID")
    private final long id;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "UNIQUE_STR_ID", nullable = false)
    @Fetch(FetchMode.JOIN)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<UniqueStringsSegment> uniqueStringSegments = new HashSet<UniqueStringsSegment>(0);
    @Transient
    private UniqueStringsSegment currentUniqueUsernamesSegment;
    
    public UniqueStrings() {
        this.id = -1;
    }

    public boolean add(String e) {
        int stringCount = 0;
        
        //Check if the username exists in any segment
        for (final UniqueStringsSegment uniqueUsernamesSegment : this.uniqueStringSegments) {
            if (uniqueUsernamesSegment.contains(e)) {
                return false;
            }
            stringCount += uniqueUsernamesSegment.size();
        }
        
        //Make sure a current segment exists
        if (this.currentUniqueUsernamesSegment == null) {
            final int segmentCount = this.uniqueStringSegments.size();
            //For more than 1440 segments or a string/segment ratio worse than 2:1 merge old segments into new
            if (segmentCount >= 1440 || (segmentCount > 1 && stringCount / segmentCount <= 2)) {
                LOGGER.debug("Merging {} segments with {} strings into a single segment", segmentCount, stringCount);
                this.currentUniqueUsernamesSegment = new UniqueStringsSegment();
                
                //Add all existing unique strings into one segment
                for (final UniqueStringsSegment uniqueUsernamesSegment : this.uniqueStringSegments) {
                    this.currentUniqueUsernamesSegment.addAll(uniqueUsernamesSegment);
                }
                
                //Remove all old segments
                this.uniqueStringSegments.clear();
                
                //Set the add the new segment
                this.uniqueStringSegments.add(this.currentUniqueUsernamesSegment);
            }
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
        if (this == obj)
            return true;
        if (id == -1) //If id is -1 then equality must be by instance
            return false;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UniqueStrings other = (UniqueStrings) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "UniqueStrings [id=" + id + ", size=" + uniqueStringSegments.size() + "]";
    }
}
