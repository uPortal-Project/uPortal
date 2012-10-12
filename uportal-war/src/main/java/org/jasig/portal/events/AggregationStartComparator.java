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
package org.jasig.portal.events;

import java.util.Comparator;

import org.jasig.portal.events.aggr.BaseAggregation;
import org.joda.time.DateTime;


public class AggregationStartComparator implements Comparator<BaseAggregation<?>> {

	@Override
	public int compare(BaseAggregation<?> aggr1, BaseAggregation<?> aggr2) {
        final DateTime entryDate1 = aggr1.getDateTime();
        final DateTime entryDate2 = aggr2.getDateTime();

		return entryDate1.compareTo(entryDate2);
	}

}
