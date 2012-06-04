package org.jasig.portal.events.aggr.login;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.BaseAggregationKeyImpl;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;

final class LoginAggregationKeyImpl extends BaseAggregationKeyImpl implements LoginAggregationKey {
    private static final long serialVersionUID = 1L;

    public LoginAggregationKeyImpl(AggregationInterval aggregationInterval,
            AggregatedGroupMapping aggregatedGroupMapping) {
        super(aggregationInterval, aggregatedGroupMapping);
    }

    public LoginAggregationKeyImpl(LoginAggregation baseAggregation) {
        super(baseAggregation);
    }

    public LoginAggregationKeyImpl(DateDimension dateDimension, TimeDimension timeDimension,
            AggregationInterval aggregationInterval, AggregatedGroupMapping aggregatedGroupMapping) {
        super(dateDimension, timeDimension, aggregationInterval, aggregatedGroupMapping);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof LoginAggregationKey))
            return false;
        return super.equals(obj);
    }
    
    @Override
    public String toString() {
        return "LoginAggregationKey [dateDimension=" + getDateDimension() + ", timeDimension="
                + getTimeDimension() + ", interval=" + getInterval() + ", aggregatedGroup=" + getAggregatedGroup()
                + "]";
    }
}
