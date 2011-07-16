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
 * Represents a description of a check as well as the result of having performed that
 * check.
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class CheckAndResult {

    /**
     * A String describing what check was performed.
     */
    private final String checkDescription;
    
    /**
     * The result of the check.
     */
    private final CheckResult result;
    
    public CheckAndResult(String checkDescription, CheckResult result) {
        this.checkDescription = checkDescription;
        this.result = result;
    }
    
    /**
     * @return Returns the checkDescription.
     */
    public String getCheckDescription() {
        return this.checkDescription;
    }
    /**
     * @return Returns the result.
     */
    public CheckResult getResult() {
        return this.result;
    }
    
    public boolean isSuccess() {
        return this.result.isSuccess();
    }

    public boolean isFatal() {
        return this.result.isFatal();
    }
}
