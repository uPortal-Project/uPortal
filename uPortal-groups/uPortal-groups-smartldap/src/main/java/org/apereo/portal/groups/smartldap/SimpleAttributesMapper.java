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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.groups.EntityTestingGroupImpl;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.security.IPerson;
import org.springframework.ldap.core.AttributesMapper;

public final class SimpleAttributesMapper implements AttributesMapper {

    private static final String GROUP_DESCRIPTION =
            "This group was pulled from the directory server.";

    /** Name of the LDAP attribute on a group that tells you its key (normally 'dn'). */
    private String keyAttributeName = null;

    /** Name of the LDAP attribute on a group that tells you the name of the group. */
    private String groupNameAttributeName = null;

    /** Name of the LDAP attribute on a group that tells you who its children are. */
    private String membershipAttributeName = null;

    private final Log log = LogFactory.getLog(getClass());

    /*
     * Public API.
     */
    @Override
    public Object mapFromAttributes(Attributes attr) {

        // Assertions.
        if (keyAttributeName == null) {
            String msg = "The property 'keyAttributeName' must be set.";
            throw new IllegalStateException(msg);
        }
        if (groupNameAttributeName == null) {
            String msg = "The property 'groupNameAttributeName' must be set.";
            throw new IllegalStateException(msg);
        }
        if (membershipAttributeName == null) {
            String msg = "The property 'membershipAttributeName' must be set.";
            throw new IllegalStateException(msg);
        }

        if (log.isDebugEnabled()) {
            String msg =
                    "SimpleAttributesMapper.mapFromAttributes() :: settings:  keyAttributeName='"
                            + keyAttributeName
                            + "', groupNameAttributeName='"
                            + groupNameAttributeName
                            + "', membershipAttributeName='"
                            + membershipAttributeName
                            + "'";
            log.debug(msg);
        }

        LdapRecord result;

        try {
            if (log.isDebugEnabled()) {
                log.debug("Attribute data set: " + attr);
                log.debug("Attribute's value of keyAttributeName: " + attr.get(keyAttributeName));
            }
            String key = (String) attr.get(keyAttributeName).get();
            String groupName = (String) attr.get(groupNameAttributeName).get();

            IEntityGroup g = new EntityTestingGroupImpl(key, IPerson.class);
            g.setCreatorID("System");
            g.setName(groupName);
            g.setDescription(GROUP_DESCRIPTION);
            List<String> membership = new ArrayList<String>();
            Attribute m = attr.get(membershipAttributeName);
            if (m != null) {
                for (Enumeration<?> en = m.getAll(); en.hasMoreElements(); ) {
                    membership.add((String) en.nextElement());
                }
            }
            result = new LdapRecord(g, membership);

            if (log.isDebugEnabled()) {
                StringBuilder msg = new StringBuilder();
                msg.append("Record Details:")
                        .append("\n\tkey=")
                        .append(key)
                        .append("\n\tgroupName=")
                        .append(groupName)
                        .append("\n\tmembers:");
                for (String s : membership) {
                    msg.append("\n\t\t").append(s);
                }
                log.debug(msg.toString());
            }

        } catch (Throwable t) {
            log.error("Error in SimpleAttributesMapper", t);
            String msg =
                    "SimpleAttributesMapper failed to create a LdapRecord "
                            + "from the specified Attributes:  "
                            + attr;
            throw new RuntimeException(msg, t);
        }

        return result;
    }

    public void setKeyAttributeName(String keyAttributeName) {
        this.keyAttributeName = keyAttributeName;
    }

    public void setGroupNameAttributeName(String groupNameAttributeName) {
        this.groupNameAttributeName = groupNameAttributeName;
    }

    public void setMembershipAttributeName(String membershipAttributeName) {
        this.membershipAttributeName = membershipAttributeName;
    }
}
