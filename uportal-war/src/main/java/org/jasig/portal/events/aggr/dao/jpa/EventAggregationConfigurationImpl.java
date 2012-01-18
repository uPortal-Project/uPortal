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

package org.jasig.portal.events.aggr.dao.jpa;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.AcademicTermDetails;
import org.jasig.portal.events.aggr.EventAggregationConfiguration;
import org.jasig.portal.events.aggr.IPortalEventAggregator;
import org.jasig.portal.events.aggr.Interval;
import org.jasig.portal.events.aggr.QuarterDetails;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMappingImpl;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(name = "UP_EVENT_AGGR_CONFIG")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class EventAggregationConfigurationImpl implements EventAggregationConfiguration, Serializable {
    static long INSTANCE_ID = 1;
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name="ID")
    @SuppressWarnings("unused")
    private final long id = INSTANCE_ID;
    
    @OneToMany(targetEntity=AggregatedGroupMappingImpl.class, fetch=FetchType.EAGER)
    @JoinTable(name="UP_EVENT_AGGR_CONFIG_INCGRP", joinColumns = @JoinColumn(name = "UP_EVENT_AGGR_CONFIG_ID"), inverseJoinColumns = @JoinColumn(name = "GROUP_ID"))
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private final Set<AggregatedGroupMapping> includedGroups;
    
    @OneToMany(targetEntity=AggregatedGroupMappingImpl.class, fetch=FetchType.EAGER)
    @JoinTable(name="UP_EVENT_AGGR_CONFIG_EXCGRP", joinColumns = @JoinColumn(name = "UP_EVENT_AGGR_CONFIG_ID"), inverseJoinColumns = @JoinColumn(name = "GROUP_ID"))
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private final Set<AggregatedGroupMapping> excludedGroups;
    
    @OneToMany(targetEntity=AggregatedGroupMappingImpl.class, fetch=FetchType.EAGER)
    @JoinTable(name="UP_EVENT_AGGR_CONFIG_INCAGRP", joinColumns = @JoinColumn(name = "UP_EVENT_AGGR_CONFIG_ID"), inverseJoinColumns = @JoinColumn(name = "GROUP_ID"))
    @MapKeyColumn(name="AGGREGATOR_CLASS")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private final Map<Class<? extends IPortalEventAggregator<? extends PortalEvent>>, Set<AggregatedGroupMapping>> includedGroupsForAggregator;
    
    @OneToMany(targetEntity=AggregatedGroupMappingImpl.class, fetch=FetchType.EAGER)
    @JoinTable(name="UP_EVENT_AGGR_CONFIG_EXCAGRP", joinColumns = @JoinColumn(name = "UP_EVENT_AGGR_CONFIG_ID"), inverseJoinColumns = @JoinColumn(name = "GROUP_ID"))
    @MapKeyColumn(name="AGGREGATOR_CLASS")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private final Map<Class<? extends IPortalEventAggregator<? extends PortalEvent>>, Set<AggregatedGroupMapping>> excludedGroupsForAggregator;
    
    @ElementCollection(fetch=FetchType.EAGER, targetClass=Interval.class)
    @JoinTable(name="UP_EVENT_AGGR_CONFIG_EXCINT", joinColumns = @JoinColumn(name = "UP_EVENT_AGGR_CONFIG_ID"))
    @Column(name="INTERVAL", nullable = false)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    @Enumerated(EnumType.STRING)
    private final Set<Interval> excludedIntervals;
    
    @ElementCollection(fetch=FetchType.EAGER, targetClass=Interval.class)
    @JoinTable(name="UP_EVENT_AGGR_CONFIG_EXCAINT", joinColumns = @JoinColumn(name = "UP_EVENT_AGGR_CONFIG_ID"))
    @MapKeyColumn(name="AGGREGATOR_CLASS")
    @Column(name="INTERVAL", nullable = false)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    @Enumerated(EnumType.STRING)
    private final Map<Class<? extends IPortalEventAggregator<? extends PortalEvent>>, Set<Interval>> excludedIntervalsForAggregator;
    
    @OneToMany(targetEntity=QuarterDetailsImpl.class, fetch=FetchType.EAGER, orphanRemoval=true)
    @JoinTable(name="UP_EVENT_AGGR_CONFIG_QRTS", joinColumns = @JoinColumn(name = "UP_EVENT_AGGR_CONFIG_ID"), inverseJoinColumns = @JoinColumn(name = "QUARTER_ID"))
    @Sort(type=SortType.NATURAL)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private final SortedSet<QuarterDetails> quarterDetails;
    
    @OneToMany(targetEntity=AcademicTermDetailsImpl.class, fetch=FetchType.EAGER, orphanRemoval=true)
    @JoinTable(name="UP_EVENT_AGGR_CONFIG_TERMS", joinColumns = @JoinColumn(name = "UP_EVENT_AGGR_CONFIG_ID"), inverseJoinColumns = @JoinColumn(name = "TERM_ID"))
    @Sort(type=SortType.NATURAL)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private final SortedSet<AcademicTermDetails> academicTermDetails;
    
    @SuppressWarnings("unused")
    private EventAggregationConfigurationImpl() {
        this.includedGroups = null;
        this.excludedGroups = null;
        this.includedGroupsForAggregator = null;
        this.excludedGroupsForAggregator = null;
        this.excludedIntervals = null;
        this.excludedIntervalsForAggregator = null;
        this.quarterDetails = null;
        this.academicTermDetails = null;
    }
    
    EventAggregationConfigurationImpl(Set<QuarterDetails> defaultQuarters) {
        this.includedGroups = new LinkedHashSet<AggregatedGroupMapping>();
        this.excludedGroups = new LinkedHashSet<AggregatedGroupMapping>();
        this.includedGroupsForAggregator = new LinkedHashMap<Class<? extends IPortalEventAggregator<? extends PortalEvent>>, Set<AggregatedGroupMapping>>();
        this.excludedGroupsForAggregator = new LinkedHashMap<Class<? extends IPortalEventAggregator<? extends PortalEvent>>, Set<AggregatedGroupMapping>>();
        this.excludedIntervals = new LinkedHashSet<Interval>();
        this.excludedIntervalsForAggregator = new LinkedHashMap<Class<? extends IPortalEventAggregator<? extends PortalEvent>>, Set<Interval>>();
        this.quarterDetails = new TreeSet<QuarterDetails>(defaultQuarters);
        this.academicTermDetails = new TreeSet<AcademicTermDetails>();
    }
    

    @Override
    public Set<AggregatedGroupMapping> getIncludedGroups() {
        return this.includedGroups;
    }

    @Override
    public Set<AggregatedGroupMapping> getExcludedGroups() {
        return this.excludedGroups;
    }

    @Override
    public Set<AggregatedGroupMapping> getIncludedGroupsForAggregator(Class<? extends IPortalEventAggregator<? extends PortalEvent>> aggregatorType) {
        Set<AggregatedGroupMapping> includedGroups = this.includedGroupsForAggregator.get(aggregatorType);
        if (includedGroups == null) {
            includedGroups = new HashSet<AggregatedGroupMapping>();
            this.includedGroupsForAggregator.put(aggregatorType, includedGroups);
        }
        return includedGroups;
    }

    @Override
    public void clearIncludedGroupsForAggregator(Class<? extends IPortalEventAggregator<? extends PortalEvent>> aggregatorType) {
        this.includedGroupsForAggregator.remove(aggregatorType);
    }

    @Override
    public Set<AggregatedGroupMapping> getExcludedGroupsForAggregator(
            Class<? extends IPortalEventAggregator<? extends PortalEvent>> aggregatorType) {
        Set<AggregatedGroupMapping> excludedGroups = this.excludedGroupsForAggregator.get(aggregatorType);
        if (excludedGroups == null) {
            excludedGroups = new HashSet<AggregatedGroupMapping>();
            this.excludedGroupsForAggregator.put(aggregatorType, excludedGroups);
        }
        return excludedGroups;
    }

    @Override
    public void clearExcludedGroupsForAggregator(
            Class<? extends IPortalEventAggregator<? extends PortalEvent>> aggregatorType) {
        this.excludedGroupsForAggregator.remove(aggregatorType);
    }

    @Override
    public Set<Interval> getExcludedIntervals() {
        return this.excludedIntervals;
    }

    @Override
    public Set<Interval> getExcludedIntervalsForAggregators(
            Class<? extends IPortalEventAggregator<? extends PortalEvent>> aggregatorType) {
        Set<Interval> excludedIntervals = this.excludedIntervalsForAggregator.get(aggregatorType);
        if (excludedIntervals == null) {
            excludedIntervals = EnumSet.noneOf(Interval.class);
            this.excludedIntervalsForAggregator.put(aggregatorType, excludedIntervals);
        }
        return excludedIntervals;
    }

    @Override
    public void clearExcludedIntervalsForAggregators(
            Class<? extends IPortalEventAggregator<? extends PortalEvent>> aggregatorType) {
        this.excludedGroupsForAggregator.remove(aggregatorType);
    }

    @Override
    public SortedSet<QuarterDetails> getQuartersDetails() {
        return this.quarterDetails;
    }

    @Override
    public SortedSet<AcademicTermDetails> getAcademicTermDetails() {
        return this.academicTermDetails;
    }
}
