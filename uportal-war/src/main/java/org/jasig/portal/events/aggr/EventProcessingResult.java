package org.jasig.portal.events.aggr;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

/**
 * Captures the results of an event processing operation, includes the number of objects processed, completion
 * status and the date range of the processing
 */
public final class EventProcessingResult {
    private final int processed;
    private final DateTime start;
    private final DateTime end;
    private final boolean complete;
    private final double creationRate;
    
    protected EventProcessingResult(int processed, DateTime start, DateTime end, boolean complete) {
        this.processed = processed;
        this.start = start;
        this.end = end;
        this.complete = complete;
        
        if (start == null || end == null) {
            creationRate = 0;
        }
        else {
            creationRate = (double)processed / Math.abs(Seconds.secondsBetween(start, end).getSeconds());
        }
    }
    
    public double getCreationRate() {
        return creationRate;
    }

    public int getProcessed() {
        return processed;
    }

    public DateTime getStart() {
        return start;
    }

    public DateTime getEnd() {
        return end;
    }

    public boolean isComplete() {
        return complete;
    }
}