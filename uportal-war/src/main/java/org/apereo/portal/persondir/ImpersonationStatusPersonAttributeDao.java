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
package org.apereo.portal.persondir;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.IdentitySwapperManager;
import org.apereo.portal.url.IPortalRequestUtils;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.AbstractDefaultAttributePersonAttributeDao;
import org.jasig.services.persondir.support.CaseInsensitiveNamedPersonImpl;
import org.jasig.services.persondir.support.IUsernameAttributeProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * When collecting attributes for the current logged in user, indicates whether the user is
 * impersonating or not. Provides no information about users other than the current logged in user.
 */
public class ImpersonationStatusPersonAttributeDao
        extends AbstractDefaultAttributePersonAttributeDao {

    private static final String IMPERSONATING_ATTRIBUTE_NAME = "impersonating";

    @Autowired() private IPortalRequestUtils portalRequestUtils;

    @Autowired() private IPersonManager personManager;

    @Autowired() private IdentitySwapperManager identitySwapperManager;

    /**
     * Returns an empty <code>Set</code>, per the API documentation, because we don't use any
     * attributes in queries.
     */
    @Override
    public Set<String> getAvailableQueryAttributes() {
        final IUsernameAttributeProvider usernameAttributeProvider =
                super.getUsernameAttributeProvider();
        return Collections.singleton(usernameAttributeProvider.getUsernameAttribute());
    }

    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(
            Map<String, List<Object>> query) {

        Set<IPersonAttributes> rslt = null; // default (per spec?)

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("invoking getPeopleWithMultivaluedAttributes(" + query + ")");
        }

        final IUsernameAttributeProvider usernameAttributeProvider =
                super.getUsernameAttributeProvider();
        final String queryUid = usernameAttributeProvider.getUsernameFromQuery(query);
        if (queryUid == null) {
            this.logger.debug("No username attribute found in query, returning null");
        } else {

            final HttpServletRequest req = portalRequestUtils.getCurrentPortalRequest();
            final IPerson person = personManager.getPerson(req);
            final String currentUid = person.getUserName();
            if (currentUid.equals(queryUid)) {
                final String value = identitySwapperManager.isImpersonating(req) ? "true" : "false";
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug(
                            "Gathering attributes for the current user ["
                                    + currentUid
                                    + "];  impersonating="
                                    + value);
                }
                final List<Object> values = Collections.singletonList((Object) value);
                final Map<String, List<Object>> attrs =
                        Collections.singletonMap(IMPERSONATING_ATTRIBUTE_NAME, values);
                final IPersonAttributes ipa = new CaseInsensitiveNamedPersonImpl(currentUid, attrs);
                rslt = Collections.singleton(ipa);
            }
        }

        return rslt;
    }

    /**
     * Returns <code>null</code>, per the API documentation, because we don't know what attributes
     * may be available.
     */
    @Override
    public Set<String> getPossibleUserAttributeNames() {
        return null;
    }
}
