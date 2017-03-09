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
package org.apereo.portal.portlets.statistics;

import java.util.HashSet;
import java.util.Set;

/** @author Chris Waymire <cwaymire@unicon.net> */
public class BasePortletLayoutReportForm extends BaseReportForm {
  private Set<String> portlets = new HashSet<String>();

  public final Set<String> getPortlets() {
    return portlets;
  }

  public final void setPortlets(Set<String> portlets) {
    this.portlets = portlets;
  }
}
