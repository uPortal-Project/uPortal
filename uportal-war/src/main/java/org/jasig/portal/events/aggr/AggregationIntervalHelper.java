/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.events.aggr;

import org.joda.time.DateTime;


/**
 * Calculates the correct {@link AggregationIntervalInfo} that contains the spcified {@link DateTime} for the specified
 * {@link AggregationInterval}.
 * 
 * @author Eric Dalquist
 * @version $Revision: 18025 $
 */
public interface AggregationIntervalHelper {
    /**
     * @param interval {@link AggregationInterval} to get info about
     * @param date Date that the interval should contain
     * @return Information about the calculated interval, null if the specified interval is not currently supported
     */
    public AggregationIntervalInfo getIntervalInfo(AggregationInterval interval, DateTime date);
}
