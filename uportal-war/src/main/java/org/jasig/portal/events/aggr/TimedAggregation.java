package org.jasig.portal.events.aggr;

/**
 * Methods that describe the various views of statistical data for an aggregation where timing is involved.
 * 
 * @author Eric Dalquist
 */
public interface TimedAggregation {

    double getGeometricMeanTime();

    double getMaxTime();

    double getMeanTime();

    double getMinTime();

    double getNinetiethPercentileTime();

    double getStandardDeviationTime();

}