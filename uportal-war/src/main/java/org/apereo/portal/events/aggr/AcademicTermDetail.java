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

import org.joda.time.DateMidnight;

/**
 * Details about a term of the year
 *
 */
public interface AcademicTermDetail
        extends DateRange<DateMidnight>, Comparable<AcademicTermDetail> {
    /** @return The name of the term, cannot be null */
    String getTermName();

    /** Set the name of the term */
    void setTermName(String termName);

    /** Set the start of the term, inclusive */
    void setStart(DateMidnight start);

    /** Set the end of the term, exclusive */
    void setEnd(DateMidnight end);

    /** Compare to another {@link AcademicTermDetail}, must sort by {@link #getStart()} */
    @Override
    int compareTo(AcademicTermDetail o);
}
