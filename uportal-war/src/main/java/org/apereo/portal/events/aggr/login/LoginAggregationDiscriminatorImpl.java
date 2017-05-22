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
package org.apereo.portal.events.aggr.login;

import org.apereo.portal.events.aggr.BaseGroupedAggregationDiscriminatorImpl;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.apereo.portal.utils.ComparableExtractingComparator;

public final class LoginAggregationDiscriminatorImpl extends BaseGroupedAggregationDiscriminatorImpl
        implements LoginAggregationDiscriminator {
    private static final long serialVersionUID = 1L;

    public LoginAggregationDiscriminatorImpl(AggregatedGroupMapping aggregatedGroupMapping) {
        super(aggregatedGroupMapping);
    }

    public LoginAggregationDiscriminatorImpl(LoginAggregation baseAggregation) {
        super(baseAggregation);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LoginAggregationDiscriminator)) return false;
        return super.equals(obj);
    }

    // Compare discriminators based on the group name
    public static class Comparator
            extends ComparableExtractingComparator<LoginAggregationDiscriminator, String> {

        public static Comparator INSTANCE = new Comparator();

        @Override
        protected String getComparable(LoginAggregationDiscriminator o) {
            return o.getAggregatedGroup().getGroupName();
        }
    }

    @Override
    public String toString() {
        return "LoginAggregationDiscriminator [" + super.toString() + "]";
    }
}
