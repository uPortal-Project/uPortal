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

package org.jasig.portal.events.aggr.login;

import java.io.Serializable;
import java.util.LinkedHashSet;
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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.BaseAggregationImpl;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(name = "UP_LOGIN_EVENT_AGGREGATE")
@Inheritance(strategy=InheritanceType.JOINED)
@SequenceGenerator(
        name="UP_LOGIN_EVENT_AGGREGATE_GEN",
        sequenceName="UP_LOGIN_EVENT_AGGREGATE_SEQ",
        allocationSize=1000
    )
@TableGenerator(
        name="UP_LOGIN_EVENT_AGGREGATE_GEN",
        pkColumnValue="UP_LOGIN_EVENT_AGGREGATE_PROP",
        allocationSize=1000
    )
@org.hibernate.annotations.Table(
        appliesTo = "UP_LOGIN_EVENT_AGGREGATE",
        indexes = @Index(name = "IDX_UP_LOGIN_EVENT_AGGR_DTI", columnNames = { "DATE_DIMENSION_ID", "TIME_DIMENSION_ID", "AGGR_INTERVAL" })
        )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class LoginAggregationImpl extends BaseAggregationImpl implements LoginAggregation, Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(generator = "UP_LOGIN_EVENT_AGGREGATE_GEN")
    @Column(name="ID")
    @SuppressWarnings("unused")
    private final long id;
    
    @Column(name = "LOGIN_COUNT", nullable = false)
    private int loginCount;
    
    @Column(name = "UNIQUE_LOGIN_COUNT", nullable = false)
    private int uniqueLoginCount;
    
    @ElementCollection(fetch=FetchType.LAZY)
    @CollectionTable(
            name = "UP_LOGIN_EVENT_AGGREGATE__UIDS",
            joinColumns = @JoinColumn(name = "LOGIN_AGGR_ID")
        )
    @Column(name="UNIQUEUSERNAMES", nullable=false, updatable=false, length=255)
    private Set<String> uniqueUserNames = new LinkedHashSet<String>();
    
    @SuppressWarnings("unused")
    private LoginAggregationImpl() {
        this.id = -1;
    }
    
    LoginAggregationImpl(TimeDimension timeDimension, DateDimension dateDimension, 
            AggregationInterval interval, AggregatedGroupMapping aggregatedGroup) {
        super(timeDimension, dateDimension, interval, aggregatedGroup);
        this.id = -1;
    }

    @Override
    public int getLoginCount() {
        return this.loginCount;
    }

    @Override
    public int getUniqueLoginCount() {
        return this.uniqueLoginCount;
    }
    
    @Override
    protected boolean isComplete() {
        return this.loginCount > 0 && this.uniqueUserNames.isEmpty();
    }

    @Override
    protected void completeInterval() {
        this.uniqueUserNames.clear();
    }

    void countUser(String userName) {
        checkState();
        
        if (this.uniqueUserNames.add(userName)) {
            this.uniqueLoginCount++;
        }
        this.loginCount++;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + loginCount;
        result = prime * result + uniqueLoginCount;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        LoginAggregationImpl other = (LoginAggregationImpl) obj;
        if (loginCount != other.loginCount)
            return false;
        if (uniqueLoginCount != other.uniqueLoginCount)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LoginAggregationImpl [id=" + id + ", timeDimension=" + getTimeDimension() + ", dateDimension=" + getDateDimension()
                + ", interval=" + getInterval() + ", groupName=" + getAggregatedGroup() + ", duration=" + getDuration() + ", loginCount="
                + loginCount + ", uniqueLoginCount=" + uniqueLoginCount + "]";
    }
}
