/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.events.aggr.tab;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.commons.lang.Validate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NaturalId;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.BaseAggregationImpl;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.JpaStatisticalSummary;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;

/**
 * @author Eric Dalquist
 */
@Entity
@Table(name = "UP_TAB_RENDER_AGGR")
@Inheritance(strategy=InheritanceType.JOINED)
@SequenceGenerator(
        name="UP_TAB_RENDER_AGGR_GEN",
        sequenceName="UP_TAB_RENDER_AGGR_SEQ",
        allocationSize=100
    )
@TableGenerator(
        name="UP_TAB_RENDER_AGGR_GEN",
        pkColumnValue="UP_TAB_RENDER_AGGR_PROP",
        allocationSize=100
    )
@org.hibernate.annotations.Table(
        appliesTo = "UP_TAB_RENDER_AGGR",
        indexes = @Index(name = "IDX_UP_TAB_REND_AGGR_DTI", columnNames = { "DATE_DIMENSION_ID", "TIME_DIMENSION_ID", "AGGR_INTERVAL" })
        )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TabRenderAggregationImpl extends BaseAggregationImpl implements TabRenderAggregation, Serializable {
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(generator = "UP_TAB_RENDER_AGGR_GEN")
    @Column(name="ID")
    private final long id;
    
    @NaturalId
    @Column(name = "TAB_NAME", nullable = false, length=400)
    private final String tabName;
    
    @Column(name = "RENDER_COUNT", nullable = false)
    private int renderCount;

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
    
    @OneToOne(cascade = { CascadeType.ALL }, orphanRemoval=true)
    @JoinColumn(name = "STATS_SUMMARY_ID", nullable = true)
    @Fetch(FetchMode.JOIN)
    private JpaStatisticalSummary statisticalSummary;
//    
//    @ElementCollection(fetch=FetchType.EAGER)
//    @CollectionTable(
//            name = "UP_TAB_RENDER_AGGR__TIMES",
//            joinColumns = @JoinColumn(name = "TAB_RENDER_ID")
//        )
//    @Column(name="RENDER_TIME", nullable=false, updatable=false)
//    private Collection<Long> renderTimes = new ArrayList<Long>();
//    
//    @Transient
//    private DescriptiveStatistics statistics;
    
    @SuppressWarnings("unused")
    private TabRenderAggregationImpl() {
        super();
        this.id = -1;
        this.tabName = null;
    }
    
    TabRenderAggregationImpl(TimeDimension timeDimension, DateDimension dateDimension, 
            AggregationInterval interval, AggregatedGroupMapping aggregatedGroup, String tabName) {
        super(timeDimension, dateDimension, interval, aggregatedGroup);

        Validate.notNull(tabName);
        
        this.id = -1;
        this.tabName = tabName;
    }

    @Override
    public String getTabName() {
        return this.tabName;
    }

    @Override
    public int getRenderCount() {
        return this.renderCount;
    }

    @Override
    public long getN() {
        return this.renderCount;
    }

    public double getSum() {
        return this.sum;
    }

    public double getSumsq() {
        return this.sumsq;
    }

    public double getMean() {
        return this.mean;
    }

    public double getStandardDeviation() {
        return this.standardDeviation;
    }

    public double getVariance() {
        return this.variance;
    }

    public double getPopulationVariance() {
        return this.populationVariance;
    }

    public double getMax() {
        return this.max;
    }

    public double getMin() {
        return this.min;
    }

    public double getGeometricMean() {
        return this.geometricMean;
    }

    public double getSumOfLogs() {
        return this.sumOfLogs;
    }

    public double getSecondMoment() {
        return this.secondMoment;
    }

    @Override
    protected boolean isComplete() {
        return this.renderCount > 0 && this.statisticalSummary == null;
    }

    @Override
    protected void completeInterval() {
        this.statisticalSummary = null;
    }
    
    void countRender(long executionTime) {
        checkState();
        
        //Lazily init the statistics object
        if (this.statisticalSummary == null) {
            this.statisticalSummary = new JpaStatisticalSummary();
        }
        
        this.statisticalSummary.addValue(executionTime);

        //Update statistic values
        this.renderCount = (int)this.statisticalSummary.getN();
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
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(geometricMean);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((tabName == null) ? 0 : tabName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        TabRenderAggregationImpl other = (TabRenderAggregationImpl) obj;
        if (Double.doubleToLongBits(geometricMean) != Double.doubleToLongBits(other.geometricMean))
            return false;
        if (tabName == null) {
            if (other.tabName != null)
                return false;
        }
        else if (!tabName.equals(other.tabName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TabRenderAggregationImpl [tabName=" + tabName + ", renderCount=" + renderCount + ", sum=" + sum
                + ", sumsq=" + sumsq + ", mean=" + mean + ", standardDeviation=" + standardDeviation + ", variance="
                + variance + ", populationVariance=" + populationVariance + ", max=" + max + ", min=" + min
                + ", geometricMean=" + geometricMean + ", sumOfLogs=" + sumOfLogs + ", secondMoment=" + secondMoment
                + ", getTimeDimension" + getTimeDimension() + ", getDateDimension" + getDateDimension()
                + ", getInterval" + getInterval() + ", getDuration" + getDuration() + ", getAggregatedGroup"
                + getAggregatedGroup() + "]";
    }
}
