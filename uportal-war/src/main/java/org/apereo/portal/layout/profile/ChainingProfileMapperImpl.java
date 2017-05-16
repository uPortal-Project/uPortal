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
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apereo.portal.security.IPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This concrete implementation of {@link IProfileMapper} decorates one or more enclosed profile
 * mapper instances. It will invoke the enclosed mappers in the order specified. The first mapper
 * that returns a value is the "winner" -- subsequent mappers are not checked. This implementation
 * also supports a default profile fname; If none of the enclosed mappers returns a value, the
 * specified default will be returned.
 *
 * <p>Fails gracefully. This means that if a chained mapper fails, the error will be logged and
 * ignored, with the chaining mapper continuing along the chain looking for a non-failing mapper or
 * falling back on the default if there are no answering mappers, just as if the failing mapper had
 * returned null indicating no opinion rather than throwing.
 *
 */
public final class ChainingProfileMapperImpl implements IProfileMapper {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private String defaultProfileName = "default";

    private List<IProfileMapper> subMappers = Collections.<IProfileMapper>emptyList();

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
            try {
                final String fname = mapper.getProfileFname(person, request);
                if (fname != null) {
                    logger.debug("Profile mapper {} found profile fname={}", mapper, fname);
                    return fname;
                }
            } catch (final Exception mapperThrownException) {
                logger.error(
                        "Profile mapper " + mapper + " threw on attempt to map profile.",
                        mapperThrownException);
                // ignore, treating the mapper as if it has no available opinion about the mapping
            }
        }

        logger.trace(
                "None of the chained profile mappers [{}] mapped to a profile, "
                        + "so returning default profile [{}].",
                subMappers,
                defaultProfileName);

        return defaultProfileName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("subMappers", subMappers)
                .append("defaultProfileName", defaultProfileName)
                .toString();
    }
}
