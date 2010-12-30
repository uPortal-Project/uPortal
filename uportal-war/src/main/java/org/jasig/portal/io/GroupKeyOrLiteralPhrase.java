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

package org.jasig.portal.io;

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
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;

public class GroupKeyOrLiteralPhrase implements Phrase {

    // Instance Members.
    private Phrase element;

    /*
     * Public API.
     */

    public static final Reagent ELEMENT = new SimpleReagent("ELEMENT", "descendant-or-self::text()", ReagentType.PHRASE,
                                            Element.class, "Element representing either a principal or a target.");

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {ELEMENT};
        return new SimpleFormula(GroupKeyOrLiteralPhrase.class, reagents);
    }

    public void init(EntityConfig config) {

        // Instance Members.
        this.element = (Phrase) config.getValue(ELEMENT);

    }

    public Object evaluate(TaskRequest req, TaskResponse res) {

        String rslt = null;

        Element e = (Element) element.evaluate(req, res);
        if (e.getName().equals("group")) {
            // This is a group name, we need to look up the key...
            try {

                Class[] types = new Class[] {IPerson.class, IPortletDefinition.class};

                for (Class c : types) {
                    EntityIdentifier[] eis = GroupService.searchForGroups(e.getText(), IGroupConstants.IS, c);
                    switch (eis.length) {
                        case 1:
                            // This is good -- what we hope for...
                            rslt = GroupService.findGroup(eis[0].getKey()).getKey();
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
                if (rslt == null) {
                    String msg = "No group with the specified name was found:  " + e.getText();
                    throw new RuntimeException(msg);
                }

            } catch (Throwable t) {
                String msg = "Error looking up the specified group:  " + e.getText();
                throw new RuntimeException(msg, t);
            }
        } else if (e.getName().equals("literal")) {
            rslt = e.getText();
        } else {
            String msg = "Unsupported element type:  " + e.getName();
            throw new RuntimeException(msg);
        }

        return rslt;

    }

}