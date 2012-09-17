package org.jasig.portal.portlets.statistics;

import java.util.ArrayList;
import java.util.List;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.joda.time.DateMidnight;

public class LoginReportForm {

    private DateMidnight start;
    private DateMidnight end;
    private AggregationInterval interval;
    private boolean uniqueOnly = false;
    private List<Long> groups = new ArrayList<Long>();

    public DateMidnight getStart() {
        return start;
    }

    public void setStart(DateMidnight start) {
        this.start = start;
    }

    public DateMidnight getEnd() {
        return end;
    }

    public void setEnd(DateMidnight end) {
        this.end = end;
    }

    public AggregationInterval getInterval() {
        return interval;
    }

    public void setInterval(AggregationInterval interval) {
        this.interval = interval;
    }

    public List<Long> getGroups() {
        return groups;
    }

    public void setGroups(List<Long> groups) {
        this.groups = groups;
    }

	public boolean isUniqueOnly() {
		return uniqueOnly;
	}

	public void setUniqueOnly(boolean uniqueOnly) {
		this.uniqueOnly = uniqueOnly;
	}

}
