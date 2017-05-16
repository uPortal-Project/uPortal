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
package org.apereo.portal.persondir.support;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.url.IPortalRequestUtils;
import org.jasig.services.persondir.support.ICurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides the username of the current portal user
 *
 */
public class PersonManagerCurrentUserProvider implements ICurrentUserProvider {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private IPersonManager personManager;
    private IPortalRequestUtils portalRequestUtils;

    public IPersonManager getPersonManager() {
        return personManager;
    }
    /** @param personManager the personManager to set */
    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    public IPortalRequestUtils getPortalRequestUtils() {
        return portalRequestUtils;
    }
    /** @param portalRequestUtils the portalRequestUtils to set */
    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.ICurrentUserProvider#getCurrentUserName()
     */
    public String getCurrentUserName() {
        final HttpServletRequest portalRequest;
        try {
            portalRequest = this.portalRequestUtils.getCurrentPortalRequest();
        } catch (IllegalStateException ise) {
            this.logger.warn(
                    "No current portal request available, cannot determine current user name.");
            return null;
        }

        final IPerson person = this.personManager.getPerson(portalRequest);
        if (person == null) {
            this.logger.warn(
                    "IPersonManager returned no IPerson for request, cannot determine current user name. "
                            + portalRequest);
            return null;
        }

        return person.getUserName();
    }
}
