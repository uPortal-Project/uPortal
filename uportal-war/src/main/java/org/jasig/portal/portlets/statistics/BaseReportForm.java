package org.jasig.portal.portlets.statistics;

import java.util.ArrayList;
import java.util.List;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.joda.time.DateMidnight;

public abstract class BaseReportForm {
    public enum OutputFormat {
        json,
        csv,
        html;
    }

    private OutputFormat format = OutputFormat.json;
    private DateMidnight start;
    private DateMidnight end;
    private AggregationInterval interval;
    private List<Long> groups = new ArrayList<Long>();
    
    public final OutputFormat getFormat() {
        return format;
    }

    public final void setFormat(OutputFormat format) {
        this.format = format;
    }

    public final DateMidnight getStart() {
        return start;
    }

    public final void setStart(DateMidnight start) {
        this.start = start;
    }

    public final DateMidnight getEnd() {
        return end;
    }

    public final void setEnd(DateMidnight end) {
        this.end = end;
    }

    public final AggregationInterval getInterval() {
        return interval;
    }

    public final void setInterval(AggregationInterval interval) {
        this.interval = interval;
    }

    public final List<Long> getGroups() {
        return groups;
    }

    public final void setGroups(List<Long> groups) {
        this.groups = groups;
    }
}
