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

import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.Interval;
import org.jasig.portal.events.aggr.TimeDimension;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
class LoginAggregationImpl implements LoginAggregation {
    
    private final TimeDimension timeDimension;
    private final DateDimension dateDimension;
    private final Interval interval;
    private final String groupName;
    private int duration;
    private int loginCount;
    private int uniqueLoginCount;
    
    private LoginAggregationImpl() {
        this.timeDimension = null;
        this.dateDimension = null;
        this.interval = null;
        this.groupName = null;
    }
    
    LoginAggregationImpl(TimeDimension timeDimension, DateDimension dateDimension, 
            Interval interval, String groupName) {
        
        this.timeDimension = timeDimension;
        this.dateDimension = dateDimension;
        this.interval = interval;
        this.groupName = groupName;
    }

    @Override
    public TimeDimension getTimeDimension() {
        return this.timeDimension;
    }

    @Override
    public DateDimension getDateDimension() {
        return this.dateDimension;
    }

    @Override
    public Interval getInterval() {
        return this.interval;
    }

    @Override
    public int getDuration() {
        return this.duration;
    }

    @Override
    public String getGroupName() {
        return this.groupName;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dateDimension == null) ? 0 : dateDimension.hashCode());
        result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
        result = prime * result + ((interval == null) ? 0 : interval.hashCode());
        result = prime * result + ((timeDimension == null) ? 0 : timeDimension.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LoginAggregationImpl other = (LoginAggregationImpl) obj;
        if (dateDimension == null) {
            if (other.dateDimension != null)
                return false;
        }
        else if (!dateDimension.equals(other.dateDimension))
            return false;
        if (groupName == null) {
            if (other.groupName != null)
                return false;
        }
        else if (!groupName.equals(other.groupName))
            return false;
        if (interval != other.interval)
            return false;
        if (timeDimension == null) {
            if (other.timeDimension != null)
                return false;
        }
        else if (!timeDimension.equals(other.timeDimension))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LoginAggregationImpl [timeDimension=" + timeDimension + ", dateDimension=" + dateDimension
                + ", interval=" + interval + ", groupName=" + groupName + ", duration=" + duration + ", loginCount="
                + loginCount + ", uniqueLoginCount=" + uniqueLoginCount + "]";
    }
}
