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

package org.jasig.portal.tools.checks;

import java.util.List;

/**
 * Interface for objects that execute IChecks and return Lists of CheckAndResult
 * objects.
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public interface ICheckRunner {
    
    /**
     * Execute some checks and return a List of CheckAndResult instances
     * representing the results.
     * @return a List of CheckAndResult instances.
     */
    public List<CheckAndResult> doChecks();
}
