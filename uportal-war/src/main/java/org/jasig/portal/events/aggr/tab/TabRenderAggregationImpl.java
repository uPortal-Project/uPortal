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
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.apache.commons.lang.Validate;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NaturalId;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.BaseAggregationImpl;
import org.jasig.portal.events.aggr.DateDimension;
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
    
    @Id
    @GeneratedValue(generator = "UP_TAB_RENDER_AGGR_GEN")
    @Column(name="ID")
    private final long id;
    
    @NaturalId
    @Column(name = "TAB_NAME", nullable = false, length=400)
    private final String tabName;
    
    @Column(name = "RENDER_COUNT", nullable = false)
    private int renderCount;
    
    @Column(name = "GEOMETRIC_MEAN_TIME", nullable = false)
    private double geometricMean;
    
    @Column(name = "MAX_TIME", nullable = false)
    private double max;
    
    @Column(name = "MEAN_TIME", nullable = false)
    private double mean;
    
    @Column(name = "MIN_TIME", nullable = false)
    private double min;
    
    @Column(name = "NINETIETH_PERCENTILE", nullable = false)
    private double ninetiethPercentile;
    
    @Column(name = "STANDARD_DEVIATION", nullable = false)
    private double standardDeviation;
    
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(
            name = "UP_TAB_RENDER_AGGR__TIMES",
            joinColumns = @JoinColumn(name = "TAB_RENDER_ID")
        )
    @Column(name="RENDER_TIME", nullable=false, updatable=false)
    private Collection<Long> renderTimes = new ArrayList<Long>();
    
    @Transient
    private DescriptiveStatistics statistics;
    @Transient
    private SummaryStatistics summaryStatistics;
    
    @SuppressWarnings("unused")
    private TabRenderAggregationImpl() {
        super();
        this.id = -1;
        this.tabName = null;
    }
    
    TabRenderAggregationImpl(TimeDimension timeDimension, DateDimension dateDimension, 
            AggregationInterval interval, AggregatedGroupMapping aggregatedGroup, String tabName) {
        super(timeDimension, dateDimension, interval, aggregatedGroup);

        Validate.notNull(dateDimension);
        
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
    public double getGeometricMeanTime() {
        return this.geometricMean;
    }

    @Override
    public double getMaxTime() {
        return this.max;
    }

    @Override
    public double getMeanTime() {
        return this.mean;
    }

    @Override
    public double getMinTime() {
        return this.min;
    }

    @Override
    public double getNinetiethPercentileTime() {
        return this.ninetiethPercentile;
    }

    @Override
    public double getStandardDeviationTime() {
        return this.standardDeviation;
    }

    @Override
    protected boolean isComplete() {
        return this.renderCount > 0 && this.renderTimes.isEmpty();
    }

    @Override
    protected void completeInterval() {
        this.renderTimes.clear();
    }
    
    void countRender(long executionTime) {
        checkState();
        
        //Lazily init the statistics object
        if (this.statistics == null) {
            if (this.renderTimes.isEmpty()) {
                this.statistics = new DescriptiveStatistics();
            }
            else {
                final double[] doubleRenderTimes = new double[this.renderTimes.size()];
                int i = 0;
                for (final Long renderTime : this.renderTimes) {
                    doubleRenderTimes[i++] = renderTime.doubleValue();
                }
                this.statistics = new DescriptiveStatistics(doubleRenderTimes);
            }
        }
        
        this.renderTimes.add(executionTime);
        this.renderCount++;
        this.statistics.addValue(executionTime);
        
        //Update statistic values
        this.geometricMean = this.statistics.getGeometricMean();
        this.max = this.statistics.getMax();
        this.mean = this.statistics.getMean();
        this.min = this.statistics.getMin();
        this.ninetiethPercentile = this.statistics.getPercentile(90);
        this.standardDeviation = this.statistics.getStandardDeviation();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(geometricMean);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(max);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(mean);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(min);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(ninetiethPercentile);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + renderCount;
        temp = Double.doubleToLongBits(standardDeviation);
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
        if (Double.doubleToLongBits(max) != Double.doubleToLongBits(other.max))
            return false;
        if (Double.doubleToLongBits(mean) != Double.doubleToLongBits(other.mean))
            return false;
        if (Double.doubleToLongBits(min) != Double.doubleToLongBits(other.min))
            return false;
        if (Double.doubleToLongBits(ninetiethPercentile) != Double.doubleToLongBits(other.ninetiethPercentile))
            return false;
        if (renderCount != other.renderCount)
            return false;
        if (Double.doubleToLongBits(standardDeviation) != Double.doubleToLongBits(other.standardDeviation))
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
        return "TabRenderAggregationImpl [id=" + id + ", dateDimension=" + getDateDimension() + ", timeDimension="
                + getTimeDimension() + ", interval=" + getInterval() + ", aggregatedGroup=" + getAggregatedGroup()
                + ", duration=" + getDuration() + " tabName=" + tabName + ", renderCount=" + renderCount
                + ", geometricMean=" + geometricMean + ", max=" + max + ", mean=" + mean + ", min=" + min
                + ", ninetiethPercentileTime=" + ninetiethPercentile + ", standardDeviation=" + standardDeviation
                + "]";
    }    
    
}
