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
package org.apereo.portal.layout.dlm.remoting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;

import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.AuthorizationException;
import org.apereo.portal.layout.IUserLayoutStore;
import org.apereo.portal.layout.dlm.ConfigurationLoader;
import org.apereo.portal.layout.dlm.Evaluator;
import org.apereo.portal.layout.dlm.FragmentDefinition;
import org.apereo.portal.layout.dlm.IFragmentDefinitionDao;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.security.AdminEvaluator;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.xml.xpath.XPathOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Spring controller that returns a JSON representation of DLM fragments in response to requests by
 * portal administrators.
 *
 * <p>Optional Request parameter:
 *
 * <ul>
 *   <li>sort : PRECEDENCE or NAME. Defaults to PRECEDENCE. Sort by precedence or name of fragment.
 * </ul>
 *
 * Implementation note: currently the UI client for this JSON service, in fragment-audit.jsp, does
 * not implement support for user selection of sort order.
 */
@Controller
@RequestMapping("/fragments")
public class FragmentListController {

    private static final Sort DEFAULT_SORT = Sort.PRECEDENCE;
    private static final String CHANNEL_FNAME_XPATH = "//channel/@fname";

    private ConfigurationLoader dlmConfig;
    private IPersonManager personManager;
    private IUserLayoutStore userLayoutStore;
    private IPortletDefinitionRegistry portletRegistry;
    private XPathOperations xpathOperations;
    private IFragmentDefinitionDao fragmentDefinitionDao;
    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    public void setFragmentDefinitionDao(IFragmentDefinitionDao fragmentDefinitionDao) {
        this.fragmentDefinitionDao = fragmentDefinitionDao;
    }

    @Autowired
    public void setXpathOperations(XPathOperations xpathOperations) {
        this.xpathOperations = xpathOperations;
    }

    @Autowired
    public void setConfigurationLoader(ConfigurationLoader dlmConfig) {
        this.dlmConfig = dlmConfig;
    }

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setUserLayoutStore(IUserLayoutStore userLayoutStore) {
        this.userLayoutStore = userLayoutStore;
    }

    @Autowired
    public void setPortletRegistry(IPortletDefinitionRegistry portletRegistry) {
        this.portletRegistry = portletRegistry;
    }

    /**
     * Returns a model of fragments --> List<FragmentBean> , sorted by precedence (default) or by
     * fragment name depending on sort parameter, to be rendered by the jsonView.
     *
     * @param req the servlet request, bound via SpringWebMVC to GET method invocations of this
     *     controller.
     * @param sortParam PRECEDENCE, NAME, or null.
     * @return ModelAndView with a List of FragmentBeans to be rendered by the jsonView.
     * @throws ServletException on Exception in underlying attempt to get at the fragments
     * @throws AuthorizationException if request is for any user other than a Portal Administrator.
     * @throws IllegalArgumentException if sort parameter has an unrecognized value
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView listFragments(
            HttpServletRequest req,
            @RequestParam(value = "sort", required = false) String sortParam)
            throws ServletException {

        this.log.debug("===========================================");
        this.log.debug("Starting  listFragments() method ");
        this.log.debug("===========================================");

        // Verify that the user is allowed to use this service
        IPerson user = personManager.getPerson(req);
        if (!AdminEvaluator.isAdmin(user)) {
            throw new AuthorizationException(
                    "User " + user.getUserName() + " not an administrator.");
        }

        Map<String, Document> fragmentLayoutMap = null;
        if (userLayoutStore != null) {
            try {
                fragmentLayoutMap = userLayoutStore.getFragmentLayoutCopies();
            } catch (Exception e) {
                String msg = "Failed to access fragment layouts";
                log.error(msg, e);
                throw new ServletException(msg, e);
            }
        }

        List<FragmentBean> fragments = new ArrayList<FragmentBean>();
        for (FragmentDefinition frag : dlmConfig.getFragments()) {

            Document layout =
                    fragmentLayoutMap != null ? fragmentLayoutMap.get(frag.getOwnerId()) : null;

            List<String> portlets = null;
            if (layout != null) {
                portlets = new ArrayList<String>();
                NodeList channelFNames =
                        this.xpathOperations.evaluate(
                                CHANNEL_FNAME_XPATH, layout, XPathConstants.NODESET);
                for (int i = 0; i < channelFNames.getLength(); i++) {
                    String fname = channelFNames.item(i).getTextContent();
                    IPortletDefinition pDef = portletRegistry.getPortletDefinitionByFname(fname);

                    if (null != pDef) {
                        portlets.add(pDef.getTitle());
                    }
                }
            }

            fragments.add(FragmentBean.create(frag, portlets));
        }

        // Determine & follow sorting preference...
        Sort sort = DEFAULT_SORT;
        if (sortParam != null) {
            sort = Sort.valueOf(sortParam);
        }
        Collections.sort(fragments, sort.getComparator());
        this.log.debug("===========================================");
        this.log.debug("Returning fragments " + fragments.toString());
        this.log.debug("===========================================");

        return new ModelAndView("jsonView", "fragments", fragments);
    }

    @RequestMapping(value = "/v1/create", method = RequestMethod.POST)
    public ModelAndView create(
            HttpServletRequest req,
            @RequestParam(value = "name", required = true) String name,
            @RequestParam(value = "ownerID", required = false) String ownerID,
            @RequestParam(value = "precedence", required = true) Double precedence) {

        // Step 1:
        // Verify that the user is allowed to use this service
        IPerson user = personManager.getPerson(req);
        if (!AdminEvaluator.isAdmin(user)) {
            throw new AuthorizationException(
                    "User " + user.getUserName() + " not an administrator.");
        }

        // Step 2:
        // Create a Document Object with some basic information

        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = dbfac.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.debug("Exception in creating a document instance ", e);
        }
        Document doc = docBuilder.newDocument();

        Element attribute = doc.createElement("attribute");
        attribute.setAttribute("mode", "deepMemberOf");
        attribute.setAttribute("name", "Everyone");

        Element paren = doc.createElement("paren");
        paren.setAttribute("mode", "OR");
        paren.appendChild(attribute);

        Element audience = doc.createElement("dlm:audience");
        audience.setAttribute(
                "evaluatorFactory",
                "org.apereo.portal.layout.dlm.providers.GroupMembershipEvaluatorFactory");
        audience.appendChild(paren);

        Element fragment = doc.createElement("dlm:fragment");
        fragment.setAttribute("name", name);
        fragment.setAttribute("ownerID", (ownerID != null ? ownerID : user.getUserName()));
        fragment.setAttribute("precedence", String.valueOf(precedence));
        fragment.appendChild(audience);

        Element fragmentDefinition = doc.createElement("fragment-definition");
        fragmentDefinition.setAttribute(
                "script", "classpath://org/jasig/portal/io/import-fragment-definition_v3-1.crn");
        fragmentDefinition.setAttribute("xmlns:dlm", "http://org.apereo.portal.layout.dlm.config");
        fragmentDefinition.appendChild(fragment);

        doc.appendChild(fragmentDefinition);

        // Step 3
        // Check if the object exists

        final Element fragmentDefElement =
                this.xpathOperations.evaluate(
                        "//*[local-name() = 'fragment']", doc, XPathConstants.NODE);
        final String fragmentName = fragmentDefElement.getAttribute("name");
        FragmentDefinition fd = this.fragmentDefinitionDao.getFragmentDefinition(fragmentName);

        // Step 4
        // if the object doesn't exist in database
        // Save data in UP_DLM_EVALUATOR and in UP_DLM_EVALUATOR_PAREN

        if (fd == null) {
            fd = new FragmentDefinition(fragmentDefElement);
            fd.loadFromEelement(fragmentDefElement);
            this.fragmentDefinitionDao.updateFragmentDefinition(fd);
        }

        // Step 5
        // Return response with FragmentBean

        return new ModelAndView(
                "jsonView", "fragment", FragmentBean.create(fd, new ArrayList<String>()));
    }

    @RequestMapping(value = "/read", method = RequestMethod.GET)
    public ModelAndView read(
            HttpServletRequest req, @RequestParam(value = "name", required = true) String name) {

        FragmentDefinition fd = this.fragmentDefinitionDao.getFragmentDefinition(name);

        return new ModelAndView("jsonView", "fragment", FragmentBean.create(fd, new ArrayList<String>()));
    }

    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public ModelAndView update(HttpServletRequest req, @RequestBody FragmentBean fragment) {
        return null;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public ModelAndView delete() {
        return null;
    }

    /*
     * Nested Types
     */

    private enum Sort {
        PRECEDENCE {
            @Override
            public Comparator<FragmentBean> getComparator() {
                return new Comparator<FragmentBean>() {
                    @Override
                    public int compare(FragmentBean frag1, FragmentBean frag2) {
                        // When sorting by precedence, use reverse order to
                        // match the order in which the portal will sort them
                        // as tabs.
                        return frag2.getPrecedence().compareTo(frag1.getPrecedence());
                    }
                };
            }
        },

        NAME {
            @Override
            public Comparator<FragmentBean> getComparator() {
                return new Comparator<FragmentBean>() {
                    @Override
                    public int compare(FragmentBean frag1, FragmentBean frag2) {
                        return frag1.getName().compareTo(frag2.getName());
                    }
                };
            }
        };

        public abstract Comparator<FragmentBean> getComparator();
    }

    /** Very simple class representing a DLM fragment. */
    public static final class FragmentBean {

        // Instance Members.
        private final String name;
        private final String ownerId;
        private final Double precedence;
        private final List<String> audience;
        private final List<String> portlets;

        public static FragmentBean create(FragmentDefinition frag, List<String> portlets) {

            Validate.notNull(frag, "Cannot create a FragmentBean for a null fragment.");

            // NB: 'portlets' may be null

            return new FragmentBean(
                    frag.getName(),
                    frag.getOwnerId(),
                    frag.getPrecedence(),
                    frag.getEvaluators(),
                    portlets);
        }

        public String getName() {
            return name;
        }

        public String getOwnerId() {
            return ownerId;
        }

        public Double getPrecedence() {
            return precedence;
        }

        public List<String> getAudience() {
            return audience;
        }

        public List<String> getPortlets() {
            return portlets;
        }

        private FragmentBean(
                String name,
                String ownerId,
                Double precedence,
                List<Evaluator> audience,
                List<String> portlets) {

            this.name = name;
            this.ownerId = ownerId;
            this.precedence = precedence;

            List<String> list = new ArrayList<String>();
            for (Evaluator ev : audience) {
                list.add(ev.getSummary());
            }
            this.audience = Collections.unmodifiableList(list);
            if (portlets != null) {
                this.portlets = Collections.unmodifiableList(portlets);
            } else {
                this.portlets = Collections.emptyList();
            }
        }
    }
}
