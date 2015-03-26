/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.security.audit.dao.jpa;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Type;
import org.jasig.portal.security.audit.IUserLogin;
import org.joda.time.Instant;
import org.joda.time.ReadableInstant;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;
import java.io.Serializable;
import java.util.Date;

/**
 * This is an implementation of the IUserLogin object that persists via JPA into a
 * most-recent-login table.
 * @since uPortal 4.2
 */
@Entity
@Table(name = "UP_USER_LAST_LOGIN")
@SequenceGenerator(
    name="UP_USER_LAST_LOGIN_GEN",
    sequenceName="UP_USER_LAST_LOGIN_SEQ",
    allocationSize=1
)
@TableGenerator(
    name="UP_USER_LAST_LOGIN_GEN",
    pkColumnValue="UP_USER_LAST_LOGIN",
    allocationSize=1
)
@NaturalIdCache
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
/* package-private */ class MostRecentUserLogin
    implements Serializable, IUserLogin {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_USER_LAST_LOGIN_GEN")
    @Column(name = "LAST_LOGIN_ID", nullable=false)
    private final int internalId;

    @Version
    @Column(name = "ENTITY_VERSION", nullable=false)
    private final long entityVersion;

    @NaturalId
    @Column(name = "USERNAME", nullable=false, length = 100) // UP_USER.USERNAME has length 100.
    private final String username;

    @Column(name = "LAST_LOGIN", nullable=false)
    private final Date lastLogin;

    /**
     * Instantiate representing that username logged in at instant.
     * @param username non-null username
     * @param instant non-null moment in time at which user logged in.
     */
    public MostRecentUserLogin(final String username, final ReadableInstant instant) {

        Validate.notNull(username, "Cannot have a most recent login for a null username.");
        Validate.notNull(instant, "Cannot have a most recent login at a null instant in time.");

        this.username = username;
        this.lastLogin = instant.toInstant().toDate();

        this.internalId = -1;
        this.entityVersion = -1;

    }

    /**
     * Default constructor used by Hibernate.
     */
    @SuppressWarnings("unused") // Hibernate uses via reflection not apparent at compile time
    private MostRecentUserLogin() {
        this.internalId = -1;
        this.entityVersion = -1;
        this.username = null;
        this.lastLogin = null;
    }

    @Override
    public String getUserIdentifier() {
        return this.username;
    }

    @Override
    public ReadableInstant getInstant() {
        return new Instant(this.lastLogin.getTime());
    }

    /*
     * IUserLogin instances are equal if
     * they represent the same user having logged in at the same instant.
     */
    @Override
    public boolean equals(Object object) {

        if (object == null) { return false; }
        if (object == this) { return true; }
        if (! (object instanceof IUserLogin)) {
            return false;
        }
        final IUserLogin rhs = (IUserLogin) object;
        return new EqualsBuilder()
            .append(this.username, rhs.getUserIdentifier())
            .append(this.lastLogin, rhs.getInstant())
            .isEquals();

    }

    /*
     * hashCode() considers username and instant,
     * exactly the same properties as equals() considers,
     * as it should be.
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(23, 63).
            append(this.username).
            append(this.lastLogin).
            toHashCode();
    }

    /*
     * MostRecentUserLogin[username=jwhelwig,instant=yyyy-MM-ddTHH:mm:ss.SSSZZ]
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
            append("username", this.username).
            append("moment", this.lastLogin).
            toString();
    }

}
