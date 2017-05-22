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
package org.apereo.portal.layout.profile.dao.jpa;

import java.io.Serializable;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;
import org.apereo.portal.layout.profile.IProfileSelection;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

/**
 * JPA implementation of IProfileSelection domain object.
 *
 * @since 4.2
 */
@Entity
@Table(name = "UP_PROFILE_SELECTION")
@SequenceGenerator(
    name = "UP_PROFILE_SELECTION_GEN",
    sequenceName = "UP_PROFILE_SELECTION_SEQ",
    allocationSize = 1
)
@TableGenerator(
    name = "UP_PROFILE_SELECTION_GEN",
    pkColumnValue = "UP_PROFILE_SELECTION",
    allocationSize = 1
)
@NaturalIdCache
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class ProfileSelection implements Serializable, IProfileSelection {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_PROFILE_SELECTION_GEN")
    @Column(name = "SELECTION_ID")
    private final int internalId;

    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;

    @NaturalId
    @Column(name = "USERNAME", length = 70, nullable = false)
    private final String userName;

    @Column(name = "PROFILE_FNAME", length = 70, nullable = false)
    private String profileFName;

    /** Default constructor used by Hibernate. */
    @SuppressWarnings("unused")
    private ProfileSelection() {
        this.internalId = -1;
        this.entityVersion = -1;
        this.userName = null;
        this.profileFName = null;
    }

    public ProfileSelection(final String userName, final String profileFName) {
        this.internalId = -1;
        this.entityVersion = -1;
        this.userName = userName;
        this.profileFName = profileFName;
    }

    @Override
    public String getUserName() {
        return this.userName;
    }

    @Override
    public String getProfileFName() {
        return this.profileFName;
    }

    @Override
    public void setProfileFName(String fname) {

        if (null == fname) {
            throw new IllegalArgumentException(
                    "ProfileSelections represent selection of a profile fname, not selection of null.");
        }

        this.profileFName = fname;
    }

    @Override
    public String toString() {
        return ("[User "
                + this.userName
                + " prefers profile with fname "
                + this.profileFName
                + "]");
    }

    @Override
    public int hashCode() {
        return (this.userName + this.profileFName).hashCode();
    }

    @Override
    public boolean equals(final Object object) {

        if (object == null) {
            return false;
        }

        if (!(object instanceof IProfileSelection)) {
            return false;
        }

        final IProfileSelection otherProfileSelection = (IProfileSelection) object;

        if (!this.profileFName.equals(otherProfileSelection.getProfileFName())
                || !this.userName.equals(otherProfileSelection.getProfileFName())) {
            return false;
        }

        return true;
    }
}
