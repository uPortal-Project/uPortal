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
package org.apereo.portal.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class provides a "factory" method that returns a security context constructed based on
 * application configuration, including all relevant subcontexts.
 */
@Component
public class InitialSecurityContextFactory {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired private Set<ISecurityContextFactory> securityContextFactories;

    private Set<ISecurityContextFactory> enabledSecurityContextFactories;

    @PostConstruct
    public void init() {
        final Set<ISecurityContextFactory> set = new HashSet<>();
        for (ISecurityContextFactory fac : securityContextFactories) {
            if (fac.isEnabled()) {
                set.add(fac);
            }
        }
        enabledSecurityContextFactories = Collections.unmodifiableSet(set);
        logger.info(
                "The following Security Context Factories are enabled: {}",
                enabledSecurityContextFactories);
    }

    public ISecurityContext getInitialContext() throws PortalSecurityException {
        final ISecurityContext result = new UnionSecurityContext();
        for (ISecurityContextFactory fac : enabledSecurityContextFactories) {
            ISecurityContext ctx = fac.getSecurityContext();
            result.addSubContext(fac.getName(), ctx);
        }
        return result;
    }
}
