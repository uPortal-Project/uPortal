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
package org.apereo.portal.rest;

import java.util.ArrayList;
import java.util.List;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.IUserPreferencesManager;
import org.apereo.portal.IUserProfile;
import org.apereo.portal.layout.IUserLayoutStore;
import org.apereo.portal.layout.LayoutPortlet;
import org.apereo.portal.layout.dlm.DistributedUserLayout;
import org.apereo.portal.portlet.dao.IPortletDefinitionDao;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.apereo.portal.rest.layout.TabListOfNodes;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.url.IPortalUrlBuilder;
import org.apereo.portal.url.IPortalUrlProvider;
import org.apereo.portal.url.IPortletUrlBuilder;
import org.apereo.portal.url.UrlType;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Controller
public class LayoutRESTController {

    protected final Log log = LogFactory.getLog(getClass());

    IUserLayoutStore userLayoutStore;

    @Autowired(required = true)
    public void setUserLayoutStore(IUserLayoutStore userLayoutStore) {
        this.userLayoutStore = userLayoutStore;
    }

    IPersonManager personManager;

    @Autowired(required = true)
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    private IPortalUrlProvider urlProvider;

    @Autowired(required = true)
    public void setPortalUrlProvider(IPortalUrlProvider urlProvider) {
        this.urlProvider = urlProvider;
    }

    private IUserInstanceManager userInstanceManager;

    @Autowired(required = true)
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    private IPortletDefinitionDao portletDao;

    @Autowired(required = true)
    public void setPortletDao(IPortletDefinitionDao portletDao) {
        this.portletDao = portletDao;
    }

    /**
     * A REST call to get a json feed of the current users layout. Intent was to provide a layout
     * document without per-tab information for mobile device rendering.
     *
     * @param request The servlet request. Utilized to get the users instance and eventually there
     *     layout
     * @param tab The tab name of which you would like to filter; optional; if not provided, will
     *     return entire layout.
     * @return json feed of the layout
     * @deprecated Use /api/v4-3/dlm/layout.json. It has much more information about portlets and
     *     includes regions and breakout per tab
     */
    @Deprecated
    @RequestMapping(value = "/layoutDoc", method = RequestMethod.GET)
    public ModelAndView getRESTController(
            HttpServletRequest request, @RequestParam(value = "tab", required = false) String tab) {
        final IPerson person = personManager.getPerson(request);
        List<LayoutPortlet> portlets = new ArrayList<LayoutPortlet>();

        try {

            final IUserInstance ui = userInstanceManager.getUserInstance(request);

            final IUserPreferencesManager upm = ui.getPreferencesManager();

            final IUserProfile profile = upm.getUserProfile();
            final DistributedUserLayout userLayout = userLayoutStore.getUserLayout(person, profile);
            Document document = userLayout.getLayout();

            NodeList portletNodes = null;
            if (tab != null) {
                NodeList folders = document.getElementsByTagName("folder");
                for (int i = 0; i < folders.getLength(); i++) {
                    Node node = folders.item(i);
                    if (tab.equalsIgnoreCase(
                            node.getAttributes().getNamedItem("name").getNodeValue())) {
                        TabListOfNodes tabNodes = new TabListOfNodes();
                        tabNodes.addAllChannels(node.getChildNodes());
                        portletNodes = tabNodes;
                        break;
                    }
                }
            } else {
                portletNodes = document.getElementsByTagName("channel");
            }
            for (int i = 0; i < portletNodes.getLength(); i++) {
                try {

                    NamedNodeMap attributes = portletNodes.item(i).getAttributes();
                    IPortletDefinition def =
                            portletDao.getPortletDefinitionByFname(
                                    attributes.getNamedItem("fname").getNodeValue());
                    LayoutPortlet portlet = new LayoutPortlet(def);

                    portlet.setNodeId(attributes.getNamedItem("ID").getNodeValue());

                    //get alt max URL
                    String alternativeMaximizedLink = def.getAlternativeMaximizedLink();
                    if (alternativeMaximizedLink != null) {
                        portlet.setUrl(alternativeMaximizedLink);
                        portlet.setAltMaxUrl(true);
                    } else {
                        // get the maximized URL for this portlet
                        final IPortalUrlBuilder portalUrlBuilder =
                                urlProvider.getPortalUrlBuilderByLayoutNode(
                                        request,
                                        attributes.getNamedItem("ID").getNodeValue(),
                                        UrlType.RENDER);
                        final IPortletWindowId targetPortletWindowId =
                                portalUrlBuilder.getTargetPortletWindowId();
                        if (targetPortletWindowId != null) {
                            final IPortletUrlBuilder portletUrlBuilder =
                                    portalUrlBuilder.getPortletUrlBuilder(targetPortletWindowId);
                            portletUrlBuilder.setWindowState(WindowState.MAXIMIZED);
                        }
                        portlet.setUrl(portalUrlBuilder.getUrlString());
                        portlet.setAltMaxUrl(false);
                    }
                    portlets.add(portlet);

                } catch (Exception e) {
                    log.warn("Exception construction JSON representation of mobile portlet", e);
                }
            }

            ModelAndView mv = new ModelAndView();
            mv.addObject("layout", portlets);
            mv.setViewName("json");
            return mv;
        } catch (Exception e) {
            log.error("Error retrieving user layout document", e);
        }

        return null;
    }
}
