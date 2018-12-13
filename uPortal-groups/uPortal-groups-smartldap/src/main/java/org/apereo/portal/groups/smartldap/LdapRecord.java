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
package org.apereo.portal.groups.smartldap;

import java.util.Collections;
import java.util.List;
import org.apereo.portal.groups.IEntityGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LdapRecord {

    private static final Logger logger = LoggerFactory.getLogger(LdapRecord.class);

    // Instance Members.
    private final IEntityGroup group;
    private final List<String> keysOfChildren;

    /*
     * Public API.
     */

    public LdapRecord(IEntityGroup group, List<String> keysOfChildren) {
        // Assertions.
        if (group == null) {
            String msg = "Argument 'group' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (keysOfChildren == null) {
            String msg = "Argument 'keysOfChildren' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        if (logger.isDebugEnabled()) {
            String keys = String.join(",", keysOfChildren);
            logger.debug(
                    "Instantiating LdapRecord for group: {}/{} with children: {}",
                    group.getLocalKey(),
                    group.getName(),
                    keys);
        }
        // Instance Members.
        this.group = group;
        this.keysOfChildren = Collections.unmodifiableList(keysOfChildren);
    }

    /**
     * <strong>NOTE</strong> two instances of {@link LdapRecord} are equal if the groups they
     * contain share the same key.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof LdapRecord)) {
            return false;
        }
        LdapRecord lr = (LdapRecord) o;
        // NB:  There is code that relies on this definition of equals()
        return lr.getGroup().getKey().equals(getGroup().getKey());
    }

    public IEntityGroup getGroup() {
        return group;
    }

    public List<String> getKeysOfChildren() {
        return keysOfChildren;
    }

    @Override
    public int hashCode() {
        return getGroup().getKey().hashCode();
    }
}
