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
package org.apereo.portal.io;

import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IGroupConstants;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.services.GroupService;
import org.apereo.portal.spring.locator.PortletDefinitionRegistryLocator;
import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;
import org.dom4j.Element;

public class GetMemberKeyPhrase implements Phrase {

    // Instance Members.
    private Phrase element;

    /*
     * Public API.
     */

    public static final Reagent ELEMENT =
            new SimpleReagent(
                    "ELEMENT",
                    "descendant-or-self::text()",
                    ReagentType.PHRASE,
                    Element.class,
                    "Element whose text is a member name.");

    @Override
    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {ELEMENT};
        return new SimpleFormula(GetMemberKeyPhrase.class, reagents);
    }

    @Override
    public void init(EntityConfig config) {

        // Instance Members.
        this.element = (Phrase) config.getValue(ELEMENT);
    }

    @Override
    public Object evaluate(TaskRequest req, TaskResponse res) {
        Element e = (Element) element.evaluate(req, res);
        return getPhrase(e.getName(), e.getText());
    }

    // Internal search, thus case sensitive.
    public static String getPhrase(String name, String memberValue) {

        String result = null;

        // We can cut & run now if the element is a <literal>...
        if (name.equals("literal")) {
            return memberValue;
        }

        try {

            // Next see if it's a <channel> element...
            if (name.equals("channel")) {
                IPortletDefinition def =
                        PortletDefinitionRegistryLocator.getPortletDefinitionRegistry()
                                .getPortletDefinitionByFname(memberValue);
                return String.valueOf(def.getPortletDefinitionId().getStringId());
            }

            // Must be a group...
            Class[] leafTypes = new Class[] {IPerson.class, IPortletDefinition.class};
            for (int i = 0; i < leafTypes.length && result == null; i++) {
                EntityIdentifier[] eis =
                        GroupService.searchForGroups(
                                memberValue, IGroupConstants.SearchMethod.DISCRETE, leafTypes[i]);
                if (eis.length == 1) {
                    // Match!
                    IEntityGroup g = GroupService.findGroup(eis[0].getKey());
                    result = g.getLocalKey();
                    break;
                } else if (eis.length > 1) {
                    String msg = "Ambiguous member name:  " + memberValue;
                    throw new RuntimeException(msg);
                }
            }

        } catch (Throwable t) {
            String msg = "Error looking up the specified member:  " + memberValue;
            throw new RuntimeException(msg, t);
        }

        if (result == null) {
            String msg = "The specified member was not found:  " + memberValue;
            throw new RuntimeException(msg);
        }

        return result;
    }
}
