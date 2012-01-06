/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.events.aggr;

import java.util.Date;


/**
 * Calculates the correct {@link IntervalInfo} that contains the spcified {@link Date} for the specified
 * {@link Interval}.
 * 
 * @author Eric Dalquist
 * @version $Revision: 18025 $
 */
public interface IntervalHelper {
    /**
     * @param interval Interval to get info about
     * @param date Date that the interval should contain
     * @return Information about the calculated interval
     */
    public IntervalInfo getIntervalDates(Interval interval, Date date);
}
