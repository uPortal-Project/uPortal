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
        indexes = @Index(name = "IDX_UP_CONC_USER_AGGR_DTI", columnNames = { "DATE_DIMENSION_ID", "TIME_DIMENSION_ID", "AGGR_INTERVAL" })
        )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TabRenderAggregationImpl extends BaseAggregationImpl implements TabRenderAggregation, Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(generator = "UP_TAB_RENDER_AGGR_GEN")
    @Column(name="ID")
    @SuppressWarnings("unused")
    private final long id;
    
    @NaturalId
    @Column(name = "TAB_NAME", nullable = false)
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
            final double[] doubleRenderTimes = new double[this.renderTimes.size()];
            int i = 0;
            for (final Long renderTime : this.renderTimes) {
                doubleRenderTimes[i++] = renderTime.doubleValue();
            }
            this.statistics = new DescriptiveStatistics(doubleRenderTimes);
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
    public String toString() {
        return "TabRenderAggregationImpl [dateDimension=" + getDateDimension() + ", timeDimension="
                + getTimeDimension() + ", interval=" + getInterval() + ", aggregatedGroup=" + getAggregatedGroup()
                + ", duration=" + getDuration() + " tabName=" + tabName + ", renderCount=" + renderCount
                + ", geometricMean=" + geometricMean + ", max=" + max + ", mean=" + mean + ", min=" + min
                + ", ninetiethPercentileTime=" + ninetiethPercentile + ", standardDeviation=" + standardDeviation
                + "]";
    }    
    
}
