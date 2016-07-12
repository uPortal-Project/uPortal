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

package org.jasig.portal.soffit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apereo.portlet.soffit.connector.AbstractSoffitLoader;
import org.apereo.portlet.soffit.connector.ISoffitLoader;
import org.apereo.portlet.soffit.model.v1_0.User;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Concrete {@link ISoffitLoader} implementation responsible for loading the
 * payload with user attributes from Person Directory.
 *
 * @author drewwills
 */
@Component
public class UserAttributesSoffitLoader extends AbstractSoffitLoader {

    @Autowired
    private IPersonAttributeDao personAttributeDao;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public UserAttributesSoffitLoader() {
        super(ISoffitLoader.DEFAULT_LOADER_ORDER + 1);
    }

    @Override
    public void load(org.apereo.portlet.soffit.model.v1_0.Payload soffit,
            RenderRequest renderRequest, RenderResponse renderResponse) {

        logger.debug("Loading user attributes for REMOTE_USER='{}'", renderRequest.getRemoteUser());

        final String username = renderRequest.getRemoteUser();
        if (username != null) {
            final IPersonAttributes person = personAttributeDao.getPerson(username);
            if (person != null) {
                final User user = soffit.getUser();
                for (Entry<String, List<Object>> y : person.getAttributes().entrySet()) {
                    final List<String> values = new ArrayList<>();
                    for (Object value : y.getValue()) {
                        if (value instanceof String) {
                            values.add((String) value);
                        }
                    }
                    user.setAttribute(y.getKey(), values);
                }
                logger.debug("Loaded the following user attributes for username='{}':  {}", username, user.getAttributes());
            }
        }

    }

}
