/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.io;

import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;
import org.dom4j.Element;
import org.jasig.portal.portlet.dao.jpa.PortletDefinitionImpl;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.spring.locator.ApplicationContextLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that parses an XML element to determine if it is
 * <group>groupName</group>
 * <portlet>fname</portlet>
 * <literal>usernameSpecialPermissionStringEtc</literal>
 */
public class GroupKeyLiteralOrPortletPhrase extends GroupKeyOrLiteralPhrase {
    // From Cernunnos API
    public static final String FILE_LOCATION = "Attributes.LOCATION";

    private Logger log = LoggerFactory.getLogger(this.getClass());

    // Dynamically created so Autowired won't work until we implement AspectJ AOP. Using init method to populate.
    private IPortletDefinitionRegistry portletDefinitionRegistry;

    /*
     * Public API.
     */

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {ELEMENT};
        return new SimpleFormula(GroupKeyLiteralOrPortletPhrase.class, reagents);
    }

    // Must execute this method at runtime rather than Spring Context initialization since the ApplicationContent
    // is not available when this class is constructed (cernnunos scripts are parsed and objects initialized
    // during Spring Context initialization). An alternative is to invoke this code via Groovy rather than
    // cernnunos.  No need to make syncrhonized or use transient field since multiple threads of execution
    // would get the same value.
    private void postInit() {
        if (portletDefinitionRegistry == null) {
            portletDefinitionRegistry = (IPortletDefinitionRegistry)
                    ApplicationContextLocator.getApplicationContext().getBean("portletDefinitionRegistry");
        }
    }

    public Object evaluate(TaskRequest req, TaskResponse res) {
        // Ugly, but must do at runtime and not class initialization time
        postInit();

        String rslt = null;

        Element e = (Element) element.evaluate(req, res);
        if (e.getName().equals("group")) {
            rslt = getValidatedGroupName(e);
        } else if (e.getName().equals("portlet")) {
            String fname = e.getText();
            IPortletDefinition portlet = portletDefinitionRegistry.getPortletDefinitionByFname(fname);
            if (portlet == null) {
                throw new RuntimeException("Invalid portlet fname '" + fname + "' in file "
                        + req.getAttribute(FILE_LOCATION));
            } else if (portlet instanceof PortletDefinitionImpl) {
                rslt = IPermission.PORTLET_PREFIX +
                        ((PortletDefinitionImpl) portlet).getPortletDefinitionId().getLongId();
            } else {
                throw new RuntimeException("Unknown portlet type fname '" + fname + "'. Is not a PortletDefinitionImpl");
            }
        } else if (e.getName().equals("literal")) {
            rslt = e.getText();
            if (rslt != null && rslt.startsWith(IPermission.PORTLET_PREFIX)) {
                throw new RuntimeException("Invalid literal string " + rslt + " in file "
                        + req.getAttribute(FILE_LOCATION) + ". This is probably due to export of permission"
                        + " using an older version of uPortal doing an export that didn't support the new"
                        + " <portlet> element.  You will need to lookup the portlet with the ID value '"
                        + rslt.substring(IPermission.PORTLET_PREFIX.length())
                        + "' in the UP_PORTLET_DEF table and replace the <literal> element in the XML file with"
                        + " <portlet>portletFname</portlet> then re-import.");
            }
        } else {
            String msg = "Unsupported element type:  " + e.getName();
            throw new RuntimeException(msg);
        }

        return rslt;

    }

}