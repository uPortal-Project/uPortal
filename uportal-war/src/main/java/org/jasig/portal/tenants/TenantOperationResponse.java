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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class encapsulates the different items of information that a concrete
 * {@link ITenantOperationsListener} might want to communicate back to the
 * service about the process.  These include whether the operation succeeded,
 * whether to abort the operation (if not), a handler for callbacks for
 * commit/rollback support, and information to display in the summary report.
 *
 * @since uPortal 4.3
 * @author drewwills
 */
public class TenantOperationResponse {

    public enum Result {

        /**
         * Indicates the {@link ITenantOperationsListener} was successful.
         */
        SUCCESS,

        /**
         * Indicates the {@link ITenantOperationsListener} was not successful, but
         * stops short of canceling the whole operation.
         */
        FAIL,

        /**
         * Indicates the {@link ITenantOperationsListener} was not successful and
         * that the operation should be cancelled and, if possible, rolled back.
         */
        ABORT,

        /**
         * Instructs the {@link TenantService} to keep this response out of the report.
         */
        IGNORE

    }

    // Instance Members
    private final ITenantOperationsListener listener;
    private final Result result;
    private final List<String> messages = new ArrayList<>();

    public TenantOperationResponse(ITenantOperationsListener listener, Result result) {
        this.listener = listener;
        this.result = result;
    }

    public ITenantOperationsListener getTenantOperationsListener() {
        return listener;
    }

    public Result getResult() {
        return result;
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }

}
