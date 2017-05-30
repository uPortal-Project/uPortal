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
package org.apereo.portal.events.aggr.stat;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Transient;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.SecondMoment;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.stat.descriptive.summary.SumOfLogs;
import org.apache.commons.math3.stat.descriptive.summary.SumOfSquares;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;
import org.apache.commons.math3.util.Precision;
import org.apereo.portal.events.aggr.TimedAggregationStatistics;

/**
 * Semi-Clone of {@link SummaryStatistics} that can be persisted in a database
 *
 */
@Embeddable
public class JpaStatisticalSummary implements TimedAggregationStatistics {

    /** SecondMoment is used to compute the mean and variance */
    @Embedded private SecondMoment secondMoment;

    /** sum of values that have been added */
    @Embedded private Sum sum;

    /** sum of the square of each value that has been added */
    @Embedded private SumOfSquares sumsq;

    /** min of values that have been added */
    @Embedded private Min min;

    /** max of values that have been added */
    @Embedded private Max max;

    /** sumLog of values that have been added */
    @Embedded private SumOfLogs sumLog;

    /** geoMean of values that have been added */
    @Transient private GeometricMean geoMean;

    /** mean of values that have been added */
    @Transient private Mean mean;

    /** variance of values that have been added */
    @Transient private Variance variance;

    //***** ALL FIELDS ARE LAZILY INITIALIZED HERE *****//

    private SecondMoment _getSecondMoment() {
        if (this.secondMoment == null) {
            this.secondMoment = new SecondMoment();
        }
        return this.secondMoment;
    }

    private Sum _getSum() {
        if (this.sum == null) {
            this.sum = new Sum();
        }
        return this.sum;
    }

    private SumOfSquares _getSumsq() {
        if (this.sumsq == null) {
            this.sumsq = new SumOfSquares();
        }
        return this.sumsq;
    }

    private Min _getMin() {
        if (this.min == null) {
            this.min = new Min();
        }
        return this.min;
    }

    private Max _getMax() {
        if (this.max == null) {
            this.max = new Max();
        }
        return this.max;
    }

    private SumOfLogs _getSumLog() {
        if (this.sumLog == null) {
            this.sumLog = new SumOfLogs();
        }
        return this.sumLog;
    }

    private GeometricMean _getGeoMean() {
        if (this.geoMean == null) {
            this.geoMean = new GeometricMean(this._getSumLog());
        }
        return this.geoMean;
    }

    private Mean _getMean() {
        if (this.mean == null) {
            this.mean = new Mean(this._getSecondMoment());
        }
        return this.mean;
    }

    private Variance _getVariance() {
        if (this.variance == null) {
            this.variance = new Variance(this._getSecondMoment());
        }
        return this.variance;
    }

    public void addValue(double value) {
        _getSum().increment(value);
        _getSumsq().increment(value);
        _getMin().increment(value);
        _getMax().increment(value);
        _getSumLog().increment(value);
        _getSecondMoment().increment(value);
    }

    /**
     * Returns the number of available values
     *
     * @return The number of available values
     */
    public long getN() {
        return _getSum().getN();
    }

    /**
     * Returns the sum of the values that have been added
     *
     * @return The sum or <code>Double.NaN</code> if no values have been added
     */
    public double getSum() {
        return _getSum().getResult();
    }

    /**
     * Returns the sum of the squares of the values that have been added.
     *
     * <p>Double.NaN is returned if no values have been added.
     *
     * @return The sum of squares
     */
    public double getSumsq() {
        return _getSumsq().getResult();
    }

    /**
     * Returns the mean of the values that have been added.
     *
     * <p>Double.NaN is returned if no values have been added.
     *
     * @return the mean
     */
    public double getMean() {
        return _getMean().getResult();
    }

    /**
     * Returns the standard deviation of the values that have been added.
     *
     * <p>Double.NaN is returned if no values have been added.
     *
     * @return the standard deviation
     */
    public double getStandardDeviation() {
        double stdDev = Double.NaN;
        if (getN() > 0) {
            if (getN() > 1) {
                stdDev = FastMath.sqrt(getVariance());
            } else {
                stdDev = 0.0;
            }
        }
        return stdDev;
    }

    /**
     * Returns the (sample) variance of the available values.
     *
     * <p>This method returns the bias-corrected sample variance (using {@code n - 1} in the
     * denominator). Use {@link #getPopulationVariance()} for the non-bias-corrected population
     * variance.
     *
     * <p>Double.NaN is returned if no values have been added.
     *
     * @return the variance
     */
    public double getVariance() {
        return _getVariance().getResult();
    }

    /**
     * Returns the <a href="http://en.wikibooks.org/wiki/Statistics/Summary/Variance">population
     * variance</a> of the values that have been added.
     *
     * <p>Double.NaN is returned if no values have been added.
     *
     * @return the population variance
     */
    public double getPopulationVariance() {
        Variance populationVariance = new Variance(_getSecondMoment());
        populationVariance.setBiasCorrected(false);
        return populationVariance.getResult();
    }

    /**
     * Returns the maximum of the values that have been added.
     *
     * <p>Double.NaN is returned if no values have been added.
     *
     * @return the maximum
     */
    public double getMax() {
        return _getMax().getResult();
    }

    /**
     * Returns the minimum of the values that have been added.
     *
     * <p>Double.NaN is returned if no values have been added.
     *
     * @return the minimum
     */
    public double getMin() {
        return _getMin().getResult();
    }

    /**
     * Returns the geometric mean of the values that have been added.
     *
     * <p>Double.NaN is returned if no values have been added.
     *
     * @return the geometric mean
     */
    public double getGeometricMean() {
        return _getGeoMean().getResult();
    }

    /**
     * Returns the sum of the logs of the values that have been added.
     *
     * <p>Double.NaN is returned if no values have been added.
     *
     * @return the sum of logs
     * @since 1.2
     */
    public double getSumOfLogs() {
        return _getSumLog().getResult();
    }

    /**
     * Returns a statistic related to the Second Central Moment. Specifically, what is returned is
     * the sum of squared deviations from the sample mean among the values that have been added.
     *
     * <p>Returns <code>Double.NaN</code> if no data values have been added and returns <code>0
     * </code> if there is just one value in the data set.
     *
     * <p>
     *
     * @return second central moment statistic
     * @since 2.0
     */
    public double getSecondMoment() {
        return _getSecondMoment().getResult();
    }

    /**
     * Generates a text report displaying summary statistics from values that have been added.
     *
     * @return String with line feeds displaying statistics
     * @since 1.2
     */
    @Override
    public String toString() {
        StringBuilder outBuffer = new StringBuilder();
        outBuffer.append("SummaryStatistics:").append("\n");
        outBuffer.append("n: ").append(getN()).append("\n");
        outBuffer.append("min: ").append(getMin()).append("\n");
        outBuffer.append("max: ").append(getMax()).append("\n");
        outBuffer.append("mean: ").append(getMean()).append("\n");
        outBuffer.append("geometric mean: ").append(getGeometricMean()).append("\n");
        outBuffer.append("variance: ").append(getVariance()).append("\n");
        outBuffer.append("sum of squares: ").append(getSumsq()).append("\n");
        outBuffer.append("standard deviation: ").append(getStandardDeviation()).append("\n");
        outBuffer.append("sum of logs: ").append(getSumOfLogs()).append("\n");
        return outBuffer.toString();
    }

    /**
     * Returns true iff <code>object</code> is a <code>SummaryStatistics</code> instance and all
     * statistics have the same values as this.
     *
     * @param object the object to test equality against.
     * @return true if object equals this
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof SummaryStatistics == false) {
            return false;
        }
        SummaryStatistics stat = (SummaryStatistics) object;
        return Precision.equalsIncludingNaN(stat.getGeometricMean(), getGeometricMean())
                && Precision.equalsIncludingNaN(stat.getMax(), getMax())
                && Precision.equalsIncludingNaN(stat.getMean(), getMean())
                && Precision.equalsIncludingNaN(stat.getMin(), getMin())
                && Precision.equalsIncludingNaN(stat.getN(), getN())
                && Precision.equalsIncludingNaN(stat.getSum(), getSum())
                && Precision.equalsIncludingNaN(stat.getSumsq(), getSumsq())
                && Precision.equalsIncludingNaN(stat.getVariance(), getVariance());
    }

    /**
     * Returns hash code based on values of statistics
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        int result = 31 + MathUtils.hash(getGeometricMean());
        result = result * 31 + MathUtils.hash(getGeometricMean());
        result = result * 31 + MathUtils.hash(getMax());
        result = result * 31 + MathUtils.hash(getMean());
        result = result * 31 + MathUtils.hash(getMin());
        result = result * 31 + MathUtils.hash(getN());
        result = result * 31 + MathUtils.hash(getSum());
        result = result * 31 + MathUtils.hash(getSumsq());
        result = result * 31 + MathUtils.hash(getVariance());
        return result;
    }
}
