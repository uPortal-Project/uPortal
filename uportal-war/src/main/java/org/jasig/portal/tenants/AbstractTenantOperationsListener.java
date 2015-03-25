/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.tenants;

/**
 * Implements all methods of {@link ITenantOperationsListener} as no-ops and can
 * therefore serve as a base class for concrete listeners that need to override
 * some but not all of the methods.
 * 
 * @since 4.1
 * @author awills
 */
public abstract class AbstractTenantOperationsListener implements ITenantOperationsListener {

    private boolean failOnError = true;  // default

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    /**
     * Whether a failure in this listener represents total failure and brings 
     * the process to an abrupt halt.  The default is <code>true</code>.
     */
    @Override
    public boolean isFailOnError() {
        return failOnError;
    }

    @Override
    public void onCreate(ITenant tenant) {}

    @Override
    public void onUpdate(ITenant tenant) {}

    @Override
    public void onDelete(ITenant tenant) {}

}
