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

package org.jasig.portal.rest;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.dlm.DistributedUserLayout;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.rest.layout.LayoutPortlet;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletUrlBuilder;
import org.jasig.portal.url.UrlType;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
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
    
    @RequestMapping(value="/layoutDoc", method = RequestMethod.GET)
    public ModelAndView getRESTController(HttpServletRequest request, HttpServletResponse response) {
        final IPerson person = personManager.getPerson(request);
        List<LayoutPortlet> portlets = new ArrayList<LayoutPortlet>();
        
        
        try {
            
            final IUserInstance ui = userInstanceManager.getUserInstance(request);

            final IUserPreferencesManager upm = ui.getPreferencesManager();

            final IUserProfile profile = upm.getUserProfile();
            final DistributedUserLayout userLayout = userLayoutStore.getUserLayout(person, profile);
            Document document = userLayout.getLayout();
            
            NodeList portletNodes = document.getElementsByTagName("channel");
            for (int i = 0; i < portletNodes.getLength(); i++) {
                try {
                    
                    LayoutPortlet portlet = new LayoutPortlet();
                    NamedNodeMap attributes = portletNodes.item(i).getAttributes();
                    portlet.setTitle(attributes.getNamedItem("title").getNodeValue());
                    portlet.setDescription(attributes.getNamedItem("description").getNodeValue());
                    portlet.setNodeId(attributes.getNamedItem("ID").getNodeValue());
                    
                    IPortletDefinition def = portletDao.getPortletDefinitionByFname(attributes.getNamedItem("fname").getNodeValue());
                    IPortletDefinitionParameter iconParam = def.getParameter("iconUrl");
                    if (iconParam != null) {
                        portlet.setIconUrl(iconParam.getValue());                        
                    }
                    
                    // get the maximized URL for this portlet
                    final IPortalUrlBuilder portalUrlBuilder = urlProvider.getPortalUrlBuilderByLayoutNode(request, attributes.getNamedItem("ID").getNodeValue(), UrlType.RENDER);
                    final IPortletWindowId targetPortletWindowId = portalUrlBuilder.getTargetPortletWindowId();
                    if (targetPortletWindowId != null) {
                        final IPortletUrlBuilder portletUrlBuilder = portalUrlBuilder.getPortletUrlBuilder(targetPortletWindowId);
                        portletUrlBuilder.setWindowState(WindowState.MAXIMIZED);
                    }
                    portlet.setUrl(portalUrlBuilder.getUrlString());
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
