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

package org.jasig.portal.portlets.registerportal;

import java.util.Map;
import java.util.Set;

/**
 * Collects data about the portal
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalDataCollator {
    /**
     * Implementation gets all possible data keys
     */
    public Set<String> getPossibleDataKeys();

    /**
     * Implementation gets all data the implementation knows how to collect
     */
    public Map<String, Map<String, String>> getCollectedData();

    /**
     * Implementation gets data for only the keys specified in the keysToCollect Set
     */
    public Map<String, Map<String, String>> getCollectedData(Set<String> keysToCollect);
}
