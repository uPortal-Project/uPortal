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

package org.jasig.portal.events.aggr;

import java.util.Set;

import org.jasig.portal.utils.IncludeExcludeUtils;

/**
 * Describes common Include/Exclude configuration of aggregation dimensions
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface BaseAggregatedDimensionConfig<D> {

    /**
     * @return The aggregator the includes/excludes are for
     */
    Class<? extends IPortalEventAggregator> getAggregatorType();
    
    /**
     * @return The revision number of the config object, can be used to detect changes
     */
    long getVersion();
    
    /**
     * @param dimension The dimension to test
     * @return true if it is included
     * @see IncludeExcludeUtils#included(Object, java.util.Collection, java.util.Collection)
     */
    boolean isIncluded(D dimension);

    /**
     * Dimensions listed in this set will be excluded from aggregation.
     */
    Set<D> getIncluded();

    /**
     * If not empty only dimensions listed in this set will included in aggregation.
     */
    Set<D> getExcluded();
    
    /**
     * @return If this dimension equals another, equality is based solely on {@link #getAggregatorType()}
     */
    @Override
    boolean equals(Object o);
    
    /**
     * @return The hash code of this dimension, the hash is based solely on {@link #getAggregatorType()}
     */
    @Override
    int hashCode();

}