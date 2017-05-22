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
package org.apereo.portal.events.tincan;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apereo.portal.events.tincan.providers.ITinCanAPIProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract base class for tinCanSchedulers. This class just has the basics for managing a set of
 * tin can providers.
 *
 */
public abstract class AbstractTinCanEventScheduler implements ITinCanEventScheduler {
    private static final Logger log = LoggerFactory.getLogger(AbstractTinCanEventScheduler.class);

    protected List<ITinCanAPIProvider> providers;

    @PostConstruct
    public void postConstruct() {
        for (ITinCanAPIProvider provider : providers) {
            try {
                provider.init();
            } catch (Exception e) {
                log.error("Error initializing xApi provider", e);
            }
        }
    }

    @PreDestroy
    public void preDestroy() {
        for (ITinCanAPIProvider provider : providers) {
            provider.destroy();
        }
    }

    @Autowired
    @Override
    public void setProviders(final List<ITinCanAPIProvider> providers) {
        this.providers = providers;
    }
}
