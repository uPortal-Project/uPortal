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
package org.apereo.portal.events.aggr.groups;

import java.util.Comparator;
import org.apereo.portal.utils.ComparableExtractingComparator;

public class AggregatedGroupMappingNameComparator
    extends ComparableExtractingComparator<AggregatedGroupMapping, String> {

  public static Comparator<AggregatedGroupMapping> INSTANCE =
      new AggregatedGroupMappingNameComparator();

  @Override
  protected String getComparable(AggregatedGroupMapping o) {
    return o.getGroupName();
  }
}
