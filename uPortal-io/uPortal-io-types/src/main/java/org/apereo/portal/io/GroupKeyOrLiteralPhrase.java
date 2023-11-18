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
import org.apereo.portal.groups.IGroupConstants;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.services.GroupService;
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

public class GroupKeyOrLiteralPhrase implements Phrase {

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
                    "Element representing either a principal or a target.");

    @Override
    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {ELEMENT};
        return new SimpleFormula(GroupKeyOrLiteralPhrase.class, reagents);
    }

    @Override
    public void init(EntityConfig config) {

        // Instance Members.
        this.element = (Phrase) config.getValue(ELEMENT);
    }

    // Internal search, thus case sensitive.
    @Override
    public Object evaluate(TaskRequest req, TaskResponse res) {

        String result = null;

        Element e = (Element) element.evaluate(req, res);
        if (e.getName().equals("group")) {
            // This is a group name, we need to look up the key...
            try {

                Class[] types = new Class[] {IPerson.class, IPortletDefinition.class};

                for (Class c : types) {
                    EntityIdentifier[] eis =
                            GroupService.searchForGroups(
                                    e.getText(), IGroupConstants.SearchMethod.DISCRETE, c);
                    switch (eis.length) {
                        case 1:
                            // This is good -- what we hope for...
                            result = GroupService.findGroup(eis[0].getKey()).getKey();
                            break;
                        case 0:
                            // This is ok -- try the next type...
                            continue;
                        default:
                            String msg2 = "Ambiguous group name:  " + e.getText();
                            throw new RuntimeException(msg2);
                    }
                }

                // We better have a match by now...
                if (result == null) {
                    String msg = "No group with the specified name was found:  " + e.getText();
                    throw new RuntimeException(msg);
                }

            } catch (Throwable t) {
                String msg = "Error looking up the specified group:  " + e.getText();
                throw new RuntimeException(msg, t);
            }
        } else if (e.getName().equals("literal")) {
            result = e.getText();
        } else {
            String msg = "Unsupported element type:  " + e.getName();
            throw new RuntimeException(msg);
        }

        return result;
    }
}
