package org.jasig.portal.events.aggr.login;

import java.io.Serializable;

import org.apache.commons.lang.Validate;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.AggregationIntervalInfo;
import org.jasig.portal.events.aggr.BaseAggregationImpl;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;

/**
 * EmptyLoginAggregation represents a login aggregation interval with no logins.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
public class EmptyLoginAggregation implements LoginAggregation, Serializable {

    private LoginAggregationKeyImpl aggregationKey;
    private int duration;

    public EmptyLoginAggregation(AggregationIntervalInfo info, AggregatedGroupMapping aggregatedGroup) {
        Validate.notNull(info);
        Validate.notNull(aggregatedGroup);
        
        this.duration = info.getTotalDuration();
        this.aggregationKey = new LoginAggregationKeyImpl(info.getDateDimension(), info.getTimeDimension(), info.getAggregationInterval(), aggregatedGroup);
    }

    @Override
    public int getLoginCount() {
        return 0;
    }

    @Override
    public int getUniqueLoginCount() {
        return 0;
    }

    @Override
    public LoginAggregationKey getAggregationKey() {
    	return this.aggregationKey;
    }

	@Override
	public DateDimension getDateDimension() {
		return this.aggregationKey.getDateDimension();
	}

	@Override
	public TimeDimension getTimeDimension() {
		return this.aggregationKey.getTimeDimension();
	}

	@Override
	public AggregationInterval getInterval() {
		return this.aggregationKey.getInterval();
	}

	@Override
	public AggregatedGroupMapping getAggregatedGroup() {
		return this.aggregationKey.getAggregatedGroup();
	}

	@Override
	public int getDuration() {
		return this.duration;
	}

}
