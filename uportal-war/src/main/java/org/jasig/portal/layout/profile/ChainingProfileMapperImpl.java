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
package org.jasig.portal.layout.profile;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.security.IPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This concrete implementation of {@link IProfileMapper} decorates one or more enclosed profile mapper instances.  It
 * will invoke the enclosed mappers in the order specified.  The first mapper that returns a value is the "winner" --
 * subsequent mappers are not checked.  This implementation also supports a default profile fname;  If none of the
 * enclosed mappers returns a value, the specified default will be returned.
 *
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
public final class ChainingProfileMapperImpl implements IProfileMapper {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private String defaultProfileName = "default";

    private List<IProfileMapper> subMappers = Collections.<IProfileMapper>emptyList();

    @Autowired
    private IUserLayoutStore layoutStore;

    public void setDefaultProfileName(String defaultProfileName) {
        this.defaultProfileName = defaultProfileName;
    }


    public void setSubMappers(List<IProfileMapper> subMappers) {
        // Defensive copy
        this.subMappers = Collections.unmodifiableList(subMappers);
    }

    @Override
    public String getProfileFname(IPerson person, HttpServletRequest request) {

        for (IProfileMapper mapper : subMappers) {
            final String fname = mapper.getProfileFname(person, request);
            if (fname != null) {
                logger.debug("Profile mapper {} found profile fname={}", mapper, fname);
                return fname;
            }
        }

        logger.trace("None of the chained profile mappers [{}] mapped to a profile, " +
                "so returning default profile [{}].",
                subMappers, defaultProfileName);

        return defaultProfileName;
    }

}
