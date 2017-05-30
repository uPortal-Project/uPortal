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

import java.util.Comparator;

/**
 * Sorts {@link BaseAggregation} instaces by Date and Time
 *
 */
public class BaseAggregationDateTimeComparator implements Comparator<BaseAggregation<?, ?>> {
    public static final Comparator<BaseAggregation<?, ?>> INSTANCE =
            new BaseAggregationDateTimeComparator();

    @Override
    public int compare(BaseAggregation<?, ?> o1, BaseAggregation<?, ?> o2) {
        if (o1 == o2) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        final int dataCmp =
                o1.getDateDimension().getDate().compareTo(o2.getDateDimension().getDate());
        if (dataCmp != 0) {
            return dataCmp;
        }

        return o1.getTimeDimension().getTime().compareTo(o2.getTimeDimension().getTime());
    }
}
