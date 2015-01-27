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

package org.jasig.portal.events.handlers;

import org.jasig.portal.events.LoginEvent;
import org.jasig.portal.security.audit.IAuditService;
import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Handles LoginEvent, invoking the AuditService to report the login.
 *
 * Currently a naive implementation.  Will almost certainly have to get more complicated and
 * extend QueueingEventHandler or so for performance.
 */
@Component
public class AuditingLoginEventHandler
    implements ApplicationListener<LoginEvent> {

    /**
     * Audit Service that this event handler apprises of logins.
     */
    private IAuditService auditService;

    @Override
    public void onApplicationEvent(final LoginEvent event) {

        final String username = event.getPerson().getUserName();
        final Instant instant = new Instant(event.getTimestamp());

        auditService.recordLogin(username, instant);

    }

    @Autowired
    public void setAuditService(final IAuditService auditService) {
        this.auditService = auditService;
    }
}
