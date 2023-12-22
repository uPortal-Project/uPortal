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
package org.apereo.portal.layout.dlm.providers;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.groups.GroupsException;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IGroupConstants;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.layout.dlm.Evaluator;
import org.apereo.portal.layout.dlm.EvaluatorFactory;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.services.GroupService;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Answers isApplicable() in the affirmative if the user represented by the passed in IPerson is a
 * member of the group whose name is passed to the constructor of this class.
 *
 * <p>Added support for a 'deepMemberOf' mode, from 2.5 patches (UP-1284) and cache group key rather
 * than the group itself (UP-1532). d.e 2006/12/19.
 *
 * @since 2.5
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class GroupMembershipEvaluator extends Evaluator {
    private static final int MEMBER_OF_MODE = 0;

    private static final int DEEP_MEMBER_OF_MODE = 1;

    @Column(name = "GROUP_NAME")
    private String groupName = null;

    @Column(name = "GROUP_KEY")
    private String groupKey = null;

    @Column(name = "EVALUATOR_MODE")
    private int evaluatorMode;

    /** Zero-arg constructor required by JPA. Other Java code should not use it. */
    public GroupMembershipEvaluator() {}

    public GroupMembershipEvaluator(String mode, String name) {
        if (mode.equals("memberOf")) {
            evaluatorMode = MEMBER_OF_MODE;
        } else if (mode.equals("deepMemberOf")) {
            evaluatorMode = DEEP_MEMBER_OF_MODE;
        } else {
            throw new RuntimeException(
                    "Unsupported mode '"
                            + mode
                            + "' specified. Only 'memberOf' and 'deepMemberOf' are "
                            + "supported at this time.");
        }
        this.groupName = name;
        this.groupKey = getGroupKey();
    }

    // Internal search, thus case sensitive.
    private String getGroupKey() {
        EntityIdentifier[] groups = null;
        try {
            groups =
                    GroupService.searchForGroups(
                            groupName, IGroupConstants.SearchMethod.DISCRETE, IPerson.class);
        } catch (GroupsException e1) {
            throw new RuntimeException(
                    "An exception occurred searching for " + "the group " + groupName + ".", e1);
        }
        if (groups == null || groups.length == 0)
            throw new RuntimeException(
                    "Group with name '"
                            + groupName
                            + "' not found for "
                            + this.getClass().getName()
                            + ". All evaluations will return false.");

        return groups[0].getKey();
    }

    private IEntityGroup getGroup(String key) {
        try {
            return GroupService.findGroup(key);
        } catch (GroupsException e) {
            throw new RuntimeException(
                    "An exception occurred retrieving " + "the group " + groupName + ".", e);
        }
    }

    @Override
    public boolean isApplicable(IPerson p) {
        if (groupKey == null || p == null) return false;

        IEntityGroup group = getGroup(groupKey);

        // Should not happen, but with the XML to Entity PAGS change some sites may have altered
        // their configuration
        // but not updated their group keys in the database or vice versa, especially with an
        // update.  To help
        // troubleshoot this, try to catch this error and give a bit more useful error message than
        // you'd get from
        // the lower-level methods.
        if (group == null) {
            throw new RuntimeException(
                    "Error in evaluation. Group key "
                            + groupKey
                            + " for group name "
                            + groupName
                            + " did not find a group.");
        }
        EntityIdentifier ei = p.getEntityIdentifier();

        try {
            IGroupMember groupMember = GroupService.getGroupMember(ei);
            boolean isMember = false;

            if (evaluatorMode == MEMBER_OF_MODE) {
                isMember = groupMember.isMemberOf(group);
            } else {
                isMember = groupMember.isDeepMemberOf(group);
            }
            return isMember;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to determine if user '"
                            + p.getFullName()
                            + "' is in group '"
                            + groupName
                            + "'",
                    e);
        }
    }

    @Override
    public void toElement(Element parent) {

        // Assertions.
        if (parent == null) {
            String msg = "Argument 'parent' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        String mde = null;
        switch (this.evaluatorMode) {
            case MEMBER_OF_MODE:
                mde = "memberOf";
                break;
            case DEEP_MEMBER_OF_MODE:
                mde = "deepMemberOf";
                break;
            default:
                throw new IllegalStateException(
                        "Unrecognized evaluatorMode constant:  " + this.evaluatorMode);
        }

        Element result = DocumentHelper.createElement("attribute");
        result.addAttribute("mode", mde);
        result.addAttribute("name", this.groupName);
        parent.add(result);
    }

    @Override
    public Class<? extends EvaluatorFactory> getFactoryClass() {
        return GroupMembershipEvaluatorFactory.class;
    }

    @Override
    public String getSummary() {

        StringBuilder result = new StringBuilder();
        result.append("(");
        result.append("MEMBER OF '").append(this.groupName).append("'");
        if (evaluatorMode == DEEP_MEMBER_OF_MODE) {
            result.append(" OR ANY DESCENDANT GROUP");
        }
        result.append(")");
        return result.toString();
    }
}
