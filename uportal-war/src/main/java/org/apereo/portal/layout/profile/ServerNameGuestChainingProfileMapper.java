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
package org.apereo.portal.layout.profile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apereo.portal.security.IPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * ChainingProfileMapper which add a prefix on profil Fname if the user is a guest. The prefix added
 * is "guest-[server name]-[sub fname]". If the server name used is not authorized use "guest-[sub
 * fname]" instead.
 *
 */
public class ServerNameGuestChainingProfileMapper implements IProfileMapper, InitializingBean {

    protected Logger logger = LoggerFactory.getLogger(ServerNameGuestChainingProfileMapper.class);

    private static final String SEPARATOR = "-";

    private String defaultProfileName = "default";

    private Map<String, String> authorizedServerNames;

    private List<IProfileMapper> subMappers = Collections.<IProfileMapper>emptyList();

    @Override
    public String getProfileFname(IPerson person, HttpServletRequest request) {
        final StringBuilder fname = new StringBuilder(64);
        if (person.isGuest()) {
            final String serverName = authorizedServerNames.get(request.getServerName());
            // If guest user
            fname.append("guest" + ServerNameGuestChainingProfileMapper.SEPARATOR);
            if (StringUtils.hasText(serverName)) {
                // If use an authorized server name we add the guest prefix
                fname.append(serverName);
                fname.append(ServerNameGuestChainingProfileMapper.SEPARATOR);
            }
        }

        boolean subFnameFound = false;
        for (IProfileMapper mapper : subMappers) {
            final String subFname = mapper.getProfileFname(person, request);
            if (StringUtils.hasText(subFname)) {
                fname.append(subFname);
                subFnameFound = true;
                break;
            }
        }

        if (!subFnameFound) {
            fname.append(this.defaultProfileName);
        }

        logger.debug("Profile fname: [{}].", fname.toString());
        return fname.toString();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notEmpty(authorizedServerNames, "No authorized server name provided !");
    }

    public void setDefaultProfileName(String defaultProfileName) {
        this.defaultProfileName = defaultProfileName;
    }

    public void setSubMappers(List<IProfileMapper> subMappers) {
        this.subMappers = subMappers;
    }

    public void setAuthorizedServerNames(Map<String, String> authorizedServerNames) {
        this.authorizedServerNames = authorizedServerNames;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("defaultProfileName", this.defaultProfileName)
                .append("subMappers", this.subMappers)
                .toString();
    }
}
