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
package org.apereo.portal.fragment.subscribe.dao.jpa;

import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.fragment.subscribe.IUserFragmentSubscription;
import org.apereo.portal.security.IPerson;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NaturalId;

/**
 * UserFragmentSubscriptionImpl is the default JPA-based implementation of {@link
 * IUserFragmentSubscription}.
 *
 */
@Entity
@Table(name = "UP_USER_FRAGMENT_SUBSCRIPTION")
@SequenceGenerator(
    name = "UP_USER_FRAGMENT_SUBSCRIPTION_GEN",
    sequenceName = "UP_USER_FRAGMENT_SUB_SEQ",
    allocationSize = 10
)
@TableGenerator(
    name = "UP_USER_FRAGMENT_SUBSCRIPTION_GEN",
    pkColumnValue = "UP_USER_FRAGMENT_SUBSCRIPTION",
    allocationSize = 10
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class UserFragmentSubscriptionImpl implements IUserFragmentSubscription {

    //Properties are final to stop changes in code, hibernate overrides the final via reflection to set their values
    @Id
    @GeneratedValue(generator = "UP_USER_FRAGMENT_SUBSCRIPTION_GEN")
    @Column(name = "USER_FRAGMENT_SUBSCRIPTION_ID")
    private final long userFragmentInfoId;

    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;

    @NaturalId
    @Column(name = "USER_ID", updatable = false, nullable = false)
    @Index(name = "IDX_USER_FRAG__USER", columnNames = "USER_ID")
    private final int userId;

    @NaturalId
    @Column(name = "FRAGMENT_OWNER", updatable = false, nullable = false)
    private final String fragmentOwner;

    @Column(name = "ACTIVE", updatable = true, nullable = false)
    private boolean active;

    @Column(name = "CREATED_BY", updatable = false, nullable = false)
    private String createdBy;

    @Column(name = "CREATION_DATE", updatable = false, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar creationDate;

    @Column(name = "LAST_UPDATED_DATE", updatable = true, nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar lastUpdatedDate;

    @Transient
    private EntityIdentifier entityIdentifier =
            new EntityIdentifier(null, IUserFragmentSubscription.class);

    /** Used by the ORM layer to create instances of the object. */
    @SuppressWarnings("unused")
    private UserFragmentSubscriptionImpl() {
        this.userFragmentInfoId = -1;
        this.entityVersion = -1;
        this.active = false;
        this.userId = -1;
        this.fragmentOwner = "";
        this.createdBy = "";
    }

    public UserFragmentSubscriptionImpl(IPerson person, IPerson fragmentOwner) {
        this.userFragmentInfoId = -1;
        this.entityVersion = -1;
        this.active = true;
        this.fragmentOwner = fragmentOwner.getUserName();
        this.entityIdentifier =
                new EntityIdentifier(
                        String.valueOf(this.fragmentOwner), IUserFragmentSubscription.class);
        this.userId = person.getID();
        this.createdBy = person.getUserName();
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getFragmentOwner() {
        return fragmentOwner;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Calendar getCreationDate() {
        return creationDate;
    }

    public Calendar getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    @Override
    public long getId() {
        return userFragmentInfoId;
    }

    @PreUpdate
    @SuppressWarnings("unused")
    private void setLastUpdatedDate() {
        this.lastUpdatedDate = new GregorianCalendar();
    }

    @PrePersist
    @SuppressWarnings("unused")
    private void setCreationDate() {
        this.creationDate = new GregorianCalendar();
    }

    @Override
    public void setInactive() {
        this.active = false;
    }

    /**
     * Returns an EntityIdentifier for this fragment owner. The key contains the value of the
     * eudPerson fragment owner attribute, or null
     *
     * @return EntityIdentifier with attribute 'fragment owner' as key.
     */
    @Override
    public EntityIdentifier getEntityIdentifier() {
        return entityIdentifier;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fragmentOwner == null) ? 0 : fragmentOwner.hashCode());
        result = prime * result + (int) (userFragmentInfoId ^ (userFragmentInfoId >>> 32));
        result = prime * result + userId;
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof UserFragmentSubscriptionImpl)) return false;
        UserFragmentSubscriptionImpl other = (UserFragmentSubscriptionImpl) obj;
        if (fragmentOwner == null) {
            if (other.fragmentOwner != null) return false;
        } else if (!fragmentOwner.equals(other.fragmentOwner)) return false;
        if (userFragmentInfoId != other.userFragmentInfoId) return false;
        if (userId != other.userId) return false;
        return true;
    }

    @Override
    public String toString() {
        return "UserFragmentSubscriptionImpl [userFragmentInfoId="
                + this.userFragmentInfoId
                + ", entityVersion="
                + this.entityVersion
                + ", userId="
                + this.userId
                + ", fragmentOwner="
                + this.fragmentOwner
                + ", active="
                + this.active
                + ", createdBy="
                + this.createdBy
                + ", creationDate="
                + this.creationDate
                + ", lastUpdatedDate="
                + this.lastUpdatedDate
                + ", entityIdentifier="
                + this.entityIdentifier
                + "]";
    }
}
