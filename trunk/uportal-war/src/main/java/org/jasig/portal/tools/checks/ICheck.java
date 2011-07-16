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

/**
 * A single runtime check that to be performed to validate an application deployment.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public interface ICheck {

    /**
     * Perform an arbitrary check.  The result of this method should be 
     * a CheckResult representing either a success or failure of the check.  
     * 
     * Implementations should catch their own exceptions and translate them into
     * CheckResults representing failures, since the intent of this API is to translate
     * arcane deployment issues into friendly results with remediation messages.
     * 
     * However, the
     * implementation of this method may throw any RuntimeException, and 
     * clients must cope with such exceptions.  Cope with probably means translate
     * it into a CheckResult representing a failure of this check.  The
     * client of a Check implementation will be less effective in translating a thrown Throwable
     * into an intelligent CheckResult representing a failure than the Check would have been
     * in doing this itself.
     * 
     * @return a CheckResult representing the result of the check
     */
    public CheckResult doCheck();
    
    /**
     * Get a description of what it is the check is intended to check.
     * Implementations of this method must always return a non-null String and
     * should not throw anything.
     * @return a description of what it is that the check checks.
     */
    public String getDescription();
    
}
