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
package org.apereo.portal.soffit;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.services.GroupService;
import org.apereo.portal.soffit.connector.AbstractHeaderProvider;
import org.apereo.portal.soffit.model.v1_0.Bearer;
import org.apereo.portal.soffit.service.BearerService;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Prepares the standard HTTP Authorization header. This component is defined explicitly in the
 * portlet context (not by annotation).
 *
 * @since 5.0
 */
public class AuthorizationHeaderProvider extends AbstractHeaderProvider {

    @Autowired private IPersonAttributeDao personAttributeDao;

    @Autowired private BearerService bearerService;

    @Override
    public Header createHeader(RenderRequest renderRequest, RenderResponse renderResponse) {

        // Username
        final String username = getUsername(renderRequest);

        // Attributes
        final Map<String, List<String>> attributes = new HashMap<>();
        final IPersonAttributes person = personAttributeDao.getPerson(username);
        if (person != null) {
            for (Entry<String, List<Object>> y : person.getAttributes().entrySet()) {
                final List<String> values = new ArrayList<>();
                for (Object value : y.getValue()) {
                    if (value instanceof String) {
                        values.add((String) value);
                    }
                }
                attributes.put(y.getKey(), values);
            }
        }
        logger.debug(
                "Found the following user attributes for username='{}':  {}", username, attributes);

        // Groups
        final List<String> groups = new ArrayList<>();
        final IGroupMember groupMember = GroupService.getGroupMember(username, IPerson.class);
        if (groupMember != null) {
            Set<IEntityGroup> ancestors = groupMember.getAncestorGroups();
            for (IEntityGroup g : ancestors) {
                groups.add(g.getName());
            }
        }
        logger.debug(
                "Found the following group affiliations for username='{}':  {}", username, groups);

        // Expiration of the Bearer token
        final PortletSession portletSession = renderRequest.getPortletSession();
        final Date expires =
                new Date(
                        portletSession.getLastAccessedTime()
                                + ((long) portletSession.getMaxInactiveInterval() * 1000L));

        // Authorization header
        final Bearer bearer = bearerService.createBearer(username, attributes, groups, expires);
        final Header rslt =
                new BasicHeader(
                        Headers.AUTHORIZATION.getName(),
                        Headers.BEARER_TOKEN_PREFIX + bearer.getEncryptedToken());
        logger.debug(
                "Produced the following Authorization header for username='{}':  {}",
                username,
                rslt);

        return rslt;
    }
}
