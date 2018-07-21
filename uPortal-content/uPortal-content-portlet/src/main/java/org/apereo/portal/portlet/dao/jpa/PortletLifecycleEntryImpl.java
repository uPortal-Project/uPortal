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
package org.apereo.portal.portlet.dao.jpa;

import java.util.Date;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.portal.portlet.om.IPortletLifecycleEntry;
import org.apereo.portal.portlet.om.PortletLifecycleState;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/** JPA implementation of the IPortletParameter interface. */
@Entity
@Table(name = "UP_PORTLET_LIFECYCLE")
@SequenceGenerator(
        name = "UP_PORTLET_LIFECYCLE_GEN",
        sequenceName = "UP_PORTLET_LIFECYCLE_SEQ",
        allocationSize = 5)
@TableGenerator(
        name = "UP_PORTLET_LIFECYCLE_GEN",
        pkColumnValue = "UP_PORTLET_LIFECYCLE",
        allocationSize = 5)
@Cacheable
@Cache(
        usage =
                CacheConcurrencyStrategy
                        .READ_ONLY) // Should work, AFAIK, since these objects are immutable
public class PortletLifecycleEntryImpl implements IPortletLifecycleEntry {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_PORTLET_LIFECYCLE_GEN")
    @Column(name = "ENTRY_ID")
    private final long id;

    @Column(name = "USER_ID")
    private int userId = -1;

    @Column(name = "LIFECYCLE_STATE_ID")
    private int lifecycleStateId = -1;

    @Column(name = "ENTRY_DATE")
    private Date date = null;

    /** Default constructor required by Hibernate */
    public PortletLifecycleEntryImpl() {
        this.id = -1;
    }

    public PortletLifecycleEntryImpl(int userId, PortletLifecycleState lifecycleState, Date date) {
        this.id = -1;
        this.userId = userId;
        this.lifecycleStateId = lifecycleState.getOrder();
        this.date = date;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public PortletLifecycleState getLifecycleState() {
        return PortletLifecycleState.forOrderValue(lifecycleStateId);
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public int compareTo(IPortletLifecycleEntry o) {
        int rslt = date.compareTo(o.getDate());
        if (rslt == 0) {
            rslt = lifecycleStateId - o.getLifecycleState().getOrder();
        }
        return rslt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PortletLifecycleEntryImpl that = (PortletLifecycleEntryImpl) o;

        if (userId != that.userId) return false;
        if (lifecycleStateId != that.lifecycleStateId) return false;
        return date != null ? date.equals(that.date) : that.date == null;
    }

    @Override
    public int hashCode() {
        int result = userId;
        result = 31 * result + lifecycleStateId;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("userId", userId)
                .append("lifecycleStateId", lifecycleStateId)
                .append("date", date)
                .toString();
    }
}
