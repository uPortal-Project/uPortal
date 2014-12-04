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
package org.jasig.portal.portlet.marketplace;

import org.jasig.portal.security.IPerson;

/**
 * Registry of Marketplace objects, responsible for applying caching.
 * @since uPortal 4.2
 */
public interface IMarketplaceRegistry {

    /**
     * Get a MarketplacePortletDefinition for the given portlet fname as viewed by the given user.
     * @param fname non-null fname of the portlet
     * @param user non-null user for whom the marketplace entry should be tailored
     * @return definition representing that portlet for that user or null if no such portlet.
     */
    public MarketplacePortletDefinition marketplacePortletDefinition(String fname, IPerson user);
}
