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
package org.apereo.portal.events.aggr;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

/**
 * Captures the results of an event processing operation, includes the number of objects processed,
 * completion status and the date range of the processing
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
        } else {
            creationRate =
                    (double) processed / Math.abs(Seconds.secondsBetween(start, end).getSeconds());
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
