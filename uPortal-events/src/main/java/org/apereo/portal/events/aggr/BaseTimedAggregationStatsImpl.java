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

import java.io.Serializable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.apereo.portal.events.aggr.stat.JpaStatisticalSummary;

/**
 * Base for aggregate entities that track timed statistics
 *
 */
@Access(AccessType.FIELD)
@MappedSuperclass
public abstract class BaseTimedAggregationStatsImpl<
                K extends BaseAggregationKey, D extends BaseGroupedAggregationDiscriminator>
        extends BaseAggregationImpl<K, D> implements TimedAggregationStatistics, Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "TIME_COUNT", nullable = false)
    private int count;

    @Column(name = "SUM_TIME", nullable = false)
    private double sum;

    @Column(name = "SUMSQ_TIME", nullable = false)
    private double sumsq;

    @Column(name = "MEAN_TIME", nullable = false)
    private double mean;

    @Column(name = "STD_DEVIATION_TIME", nullable = false)
    private double standardDeviation;

    @Column(name = "VARIANCE_TIME", nullable = false)
    private double variance;

    @Column(name = "POPULATION_VARIANCE_TIME", nullable = false)
    private double populationVariance;

    @Column(name = "MAX_TIME", nullable = false)
    private double max;

    @Column(name = "MIN_TIME", nullable = false)
    private double min;

    @Column(name = "GEOMETRIC_MEAN_TIME", nullable = false)
    private double geometricMean;

    @Column(name = "SUM_OF_LOGS_TIME", nullable = false)
    private double sumOfLogs;

    @Column(name = "SECOND_MOMENT_TIME", nullable = false)
    private double secondMoment;

    @Embedded private JpaStatisticalSummary statisticalSummary;

    @Column(name = "STATS_COMPLETE", nullable = false)
    private boolean complete = false;

    @Transient private boolean modified = false;

    protected BaseTimedAggregationStatsImpl() {
        super();
    }

    protected BaseTimedAggregationStatsImpl(
            TimeDimension timeDimension,
            DateDimension dateDimension,
            AggregationInterval interval,
            AggregatedGroupMapping aggregatedGroup) {
        super(timeDimension, dateDimension, interval, aggregatedGroup);
    }

    @Override
    public final long getN() {
        updateStats();
        return this.count;
    }

    public final double getSum() {
        updateStats();
        return this.sum;
    }

    public final double getSumsq() {
        updateStats();
        return this.sumsq;
    }

    public final double getMean() {
        updateStats();
        return this.mean;
    }

    public final double getStandardDeviation() {
        updateStats();
        return this.standardDeviation;
    }

    public final double getVariance() {
        updateStats();
        return this.variance;
    }

    public final double getPopulationVariance() {
        updateStats();
        return this.populationVariance;
    }

    public final double getMax() {
        updateStats();
        return this.max;
    }

    public final double getMin() {
        updateStats();
        return this.min;
    }

    public final double getGeometricMean() {
        updateStats();
        return this.geometricMean;
    }

    public final double getSumOfLogs() {
        updateStats();
        return this.sumOfLogs;
    }

    public final double getSecondMoment() {
        updateStats();
        return this.secondMoment;
    }

    /** Check if the interval is complete, must be called by super classes if overridden */
    @Override
    protected boolean isComplete() {
        return this.count > 0 && (this.complete || this.statisticalSummary == null);
    }

    /** Completes the stats interval, must be called by super classes if overridden */
    @Override
    protected void completeInterval() {
        updateStats();

        this.statisticalSummary = null;
        this.complete = true;
    }

    /** Add the value to the summary statistics */
    public final void addValue(double v) {
        if (isComplete()) {
            this.getLogger()
                    .warn(
                            "{} is already closed, the new value of {} will be ignored on: {}",
                            this.getClass().getSimpleName(),
                            v,
                            this);
            return;
        }

        //Lazily init the statistics object
        if (this.statisticalSummary == null) {
            this.statisticalSummary = new JpaStatisticalSummary();
        }

        this.statisticalSummary.addValue(v);

        this.modified = true;
    }

    /**
     * Update the individual statistic fields if the {@link JpaStatisticalSummary} has been
     * modified, called automatically by the getter of each field
     */
    @PrePersist
    @PreUpdate
    final void updateStats() {
        if (!this.modified || this.statisticalSummary == null) {
            return;
        }

        //Update statistic values
        this.count = (int) this.statisticalSummary.getN();
        this.sum = this.statisticalSummary.getSum();
        this.sumsq = this.statisticalSummary.getSumsq();
        this.mean = this.statisticalSummary.getMean();
        this.standardDeviation = this.statisticalSummary.getStandardDeviation();
        this.variance = this.statisticalSummary.getVariance();
        this.populationVariance = this.statisticalSummary.getPopulationVariance();
        this.max = this.statisticalSummary.getMax();
        this.min = this.statisticalSummary.getMin();
        this.geometricMean = this.statisticalSummary.getGeometricMean();
        this.sumOfLogs = this.statisticalSummary.getSumOfLogs();
        this.secondMoment = this.statisticalSummary.getSecondMoment();

        this.modified = false;
    }
}
