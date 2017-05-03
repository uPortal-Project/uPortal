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
package org.apereo.portal.dao.usertype;

import org.hibernate.HibernateException;
import org.jadira.usertype.spi.shared.AbstractStringColumnMapper;

/**
 */
public class NullSafeStringColumnMapper extends AbstractStringColumnMapper<String> {
    private static final long serialVersionUID = 1L;

    public static final char NOT_NULL_PREFIX = '_';

    @Override
    public String fromNonNullValue(String s) {
        if (s.charAt(0) != NOT_NULL_PREFIX) {
            throw new HibernateException(
                    "Persistent storage of "
                            + this.getClass().getName()
                            + " corrupted, database contained string ["
                            + s
                            + "] which should be prefixed by: "
                            + NOT_NULL_PREFIX);
        }

        return s.substring(1);
    }

    @Override
    public String toNonNullValue(String value) {
        return NOT_NULL_PREFIX + value;
    }
}
